package com.rtlabs.reqtool.ui.specification_document_generation;

import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.REQUIREMENT__BODY;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.REQUIREMENT__CREATED;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.REQUIREMENT__MODIFIED;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.REQUIREMENT__NAME;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.REQUIREMENT__OWNER;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.REQUIREMENT__PRIORITY;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.REQUIREMENT__STATE;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.REQUIREMENT__TYPE;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__REQUIREMENTS;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.TRACEABLE__CHILDREN;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.TRACEABLE__PARENTS;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.ImmutableList;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.ui.specification_document_generation.DocGenerator.ObjectGenerator;
import com.rtlabs.reqtool.ui.specification_document_generation.DocGenerator.TableGenerator;

/**
 * A specification document generator. Generates Markdown documents from a {@link Specification} object.
 */
public class SpecDocGenerator {

	private static final List<EStructuralFeature> TABLE_FEATURES = ImmutableList.of(
		REQUIREMENT__NAME,
		REQUIREMENT__BODY,
		REQUIREMENT__PRIORITY,
		REQUIREMENT__STATE,
		REQUIREMENT__OWNER,
		REQUIREMENT__CREATED);
	
	private static final List<EStructuralFeature> REQ_FEATURES = ImmutableList.of(
		REQUIREMENT__NAME,
		REQUIREMENT__PRIORITY,
		REQUIREMENT__STATE,
		REQUIREMENT__CREATED,
		REQUIREMENT__MODIFIED,
		REQUIREMENT__OWNER,
		REQUIREMENT__TYPE,
		TRACEABLE__PARENTS,
		TRACEABLE__CHILDREN,
		REQUIREMENT__BODY);

	
	/**
	 * Generates a Markdown document from spec.
	 * @param selectedReqs A list of requirements which should be included in the report. These should be a subset of
	 *    the requirements in spec.
	 * @param adapterFactory Used to lookup info about model features and classes. 
	 */
	public static String run(Specification spec, List<Requirement> selectedReqs, AdapterFactory adapterFactory) {
		CharArrayWriter writer = new CharArrayWriter();
		PrintWriter b = new PrintWriter(writer);
		DocGenerator gen = new DocGenerator(adapterFactory);
		
		b.println(gen.section(spec.getTitle()));
		
		// Intro: Title and description
		if (spec.getDescription() != null) b.println(spec.getDescription());
		
		b.println();

		b.println(gen.section(SPECIFICATION__REQUIREMENTS));
		
		TableGenerator<Requirement> tableGen = gen.tableGenerator(TABLE_FEATURES, selectedReqs); 

		// Table
		b.println(tableGen.table());
		
		// Requirement list
		for (ObjectGenerator<Requirement> objGen : gen.objectGenerators(selectedReqs)) {
			b.println(gen.section(objGen.object().getName()));
			for (EStructuralFeature f : REQ_FEATURES) {
				b.println(objGen.featureDetails(f));
			}
			
			gen.endSection();
		}
		
		gen.endSection();
		
		return writer.toString();
	}
}
