package com.rtlabs.reqtool.ui.specification_document_generation;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;

import com.google.common.base.Strings;
import com.google.common.primitives.Primitives;

/**
 * Helper class for generating Markdown documents.
 */
class DocGenerator {
	private static final String ELLIPSIS = "...";
	
	private final AdapterFactory adapterFactory;
	private int sectionLevel = 0;
	
	public DocGenerator(AdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
	}
	
	public <T extends EObject> ObjectGenerator<T> objectGenerator(T object) {
		return new ObjectGenerator<>(object);
	}
	
	public <T  extends EObject> TableGenerator<T> tableGenerator(List<EStructuralFeature> tableFeatures, List<T> objects) {
		return new TableGenerator<>(tableFeatures, objects); 
	}
	
	public void section() {
		sectionLevel++;
	}

	public String section(String title) {
		section();
		return Strings.repeat("#", sectionLevel) + " " + title + "\n";
	}
	
	public String section(EStructuralFeature sectionFeature) {
		return section(displayName(sectionFeature));
	}

	public String section(EObject sectionObject) {
		return section(objectValue(sectionObject));
	}
	
	public void endSection() {
		sectionLevel--;
	}
	
	/**
	 * Helper for generating text for a single object.
	 */
	public class ObjectGenerator<T extends EObject> {
		private final T object;
		
		
		public ObjectGenerator(T object) {
			this.object = object;
		}

		public T object() {
			return object;
		}
		
		public String featureValue(EStructuralFeature f) {
			return DocGenerator.this.featureValue(object, f);
		}
		
		public String objectValue() {
			return DocGenerator.this.objectValue(object);
		}
		
		public String featureDetails(EStructuralFeature f) {
			return "**" + displayName(f) + ":** " + featureValue(f) + "\n";
		}
	}
	
	/**
	 * Helper for generating Markdown tables.
	 */
	public class TableGenerator<T extends EObject> {
		
		public final List<? extends EStructuralFeature> tableFeatures;
		private final List<? extends T> objects;
		
		private Map<EStructuralFeature, Integer> columnWidthMap = new HashMap<>();

		private int tableMaxColumnWidth = 40;
		private int tableCutoff = 5;

		public TableGenerator(List<? extends EStructuralFeature> tableFeatures, List<? extends T> objects) {
			this.tableFeatures = tableFeatures;
			this.objects = objects;
			computeColumnWidthMap();
		}

		private void computeColumnWidthMap() {
			for (EStructuralFeature feature : tableFeatures) {
				String fieldName = displayName(feature);
				columnWidthMap.put(feature, fieldName.length());
				for (EObject o : objects) {
					int valueLength = Optional.ofNullable(featureValue(o, feature))
						.map(v -> v.length())
						.orElse(0);
					
					columnWidthMap.merge(feature, valueLength, Math::max);
				}
			}
		}
		
		public String table() {
			StringBuilder b = new StringBuilder();
			
			b.append(tableHeader()).append("\n");
			b.append(tableLine()).append("\n");
			
			for (EObject o : objects) {
				b.append(tableRow(o)).append("\n");
			}
			
			return b.append("\n").toString();
		}
		
		public String tableRow(EObject req) {
			return tableFeatures.stream()
				.map(f -> toColumnEntry(f, Objects.toString(featureValue(req, f), "")))
				.collect(joining(" | ", "| ", " |"));
		}
		
		public String tableHeader() {
			return tableFeatures.stream()
				.map(f -> toColumnEntry(f, displayName(f)))
				.collect(joining(" | ", "| ", " |"));
		}

		private String toColumnEntry(EStructuralFeature feature, String text) {
			String output = text.replaceAll("[\n\r]+", " ");
			int el = ELLIPSIS.length();
			output = WordUtils.abbreviate(output, tableMaxColumnWidth - tableCutoff - el, tableMaxColumnWidth - el, ELLIPSIS); 
			output = StringUtils.rightPad(output, columnWidth(feature));
			return output;
		}
		
		private int columnWidth(EStructuralFeature f) {
			return Math.min(columnWidthMap.get(f), tableMaxColumnWidth);
		}

		public String tableLine() {
			return tableFeatures.stream()
				.map(f -> 
					StringUtils.repeat("-", columnWidth(f) - 1) 
						+ (isNumeric(f.getEType().getInstanceClass()) ? ":" : "-"))
				.collect(joining(" | ", "| ", " |"));
		}

		public void setTableMaxColumnWidth(int tableMaxColumnWidth) {
			this.tableMaxColumnWidth = tableMaxColumnWidth;
		}

		public void setTableCutoff(int tableCutoff) {
			this.tableCutoff = tableCutoff;
		}
	}

	private static boolean isNumeric(Class<?> cls) {
		Class<?> wrapped = Primitives.wrap(cls);
		return wrapped == Byte.class
			|| wrapped == Short.class
			|| wrapped == Integer.class
			|| wrapped == Long.class
			|| wrapped == BigInteger.class
			|| wrapped == BigDecimal.class;
	}

	private IItemPropertySource getPropertySource(EObject o) {
		return propertySourceMap.computeIfAbsent(o.eClass(),
			_k -> (IItemPropertySource) adapterFactory.adapt(o, IItemPropertySource.class));
	}
	
	private Map<EClass, IItemPropertySource> propertySourceMap = new HashMap<>();  
	
	private IItemPropertySource getFeaturePropertySource(EStructuralFeature f) {
		return propertySourceMap.computeIfAbsent(f.getEContainingClass(), 
			_k -> (IItemPropertySource) adapterFactory.adapt(
				createObject(f.getEContainingClass()), IItemPropertySource.class));
	}

	private static EObject createObject(EClass cls) {
		return cls.getEPackage().getEFactoryInstance().create(cls);
	}
	
	public String objectValue(EObject o) {
		IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory.adapt(o, IItemLabelProvider.class);
		return labelProvider.getText(o);
	}
	
	/**
	 * Looks up the display text for a feature value.
	 */
	public String featureValue(EObject o, EStructuralFeature feature) {
		Object value = getPropertySource(o).getPropertyDescriptor(o, feature).getPropertyValue(o);
		String text = value instanceof IItemLabelProvider 
			? ((IItemLabelProvider) value).getText(o)
			: Objects.toString(value, "");

		// Replace single and multiple newlines with double newlines. This make
		// many constructs render reasonably good in Markdown. 
		return text.replaceAll("[\r\n]+", "\n\n");
	}
	
	public String displayName(EStructuralFeature feature) {
		return getFeaturePropertySource(feature).getPropertyDescriptor(null, feature).getDisplayName(null);
	}

	public <T extends EObject> Collection<ObjectGenerator<T>> objectGenerators(Collection<T> reqs) {
		return reqs.stream().map(this::objectGenerator).collect(toList());
	}
}
