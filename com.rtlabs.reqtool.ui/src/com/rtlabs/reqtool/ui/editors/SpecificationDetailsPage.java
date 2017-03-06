package com.rtlabs.reqtool.ui.editors;

import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__DESCRIPTION;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__DOCUMENT_PREFIX_FILE;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__DOCUMENT_SUFFIX_FILE;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__TITLE;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.rtlabs.common.edit_support.EditContext;
import com.rtlabs.common.model_gui_builder.ModelGuiBuilder;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.ui.Activator;

/**
 * Displays and edits the details of a {@link Specification}. That is, the information
 * that is not requirements.
 */
class SpecificationDetailsPage extends FormPage implements IEditingDomainProvider {
	private EditContext editContext;
	private IObservableValue<Specification> specification;

	public SpecificationDetailsPage(SpreadSheetEditor editor, IObservableValue<Specification> spec) {
		super(editor, Activator.PLUGIN_ID + ".specificationDetailsPage", "Specification Details");
		this.editContext = editor;
		this.specification = spec;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) {
		 super.init(site, input);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		Composite body = form.getBody();
		toolkit.paintBordersFor(body);
		ColumnLayout layout = new ColumnLayout();
		layout.maxNumColumns = 2;
		body.setLayout(layout);
		
		Section section = toolkit.createSection(body, Section.TITLE_BAR);
		section.setText("Specification Details");
		toolkit.paintBordersFor(section);
		
		Composite container = toolkit.createComposite(section);
		section.setClient(container);
		container.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).spacing(15, 15).create());
		toolkit.paintBordersFor(container);
		
		ModelGuiBuilder<Specification> guiBuilder = new ModelGuiBuilder<>(toolkit, editContext, specification);
		
		guiBuilder.createFeatureControl(container, SPECIFICATION__TITLE);
		guiBuilder.createFeatureControl(container, SPECIFICATION__DESCRIPTION);
		guiBuilder.createFeatureControl(container, SPECIFICATION__DOCUMENT_PREFIX_FILE);
		guiBuilder.createFeatureControl(container, SPECIFICATION__DOCUMENT_SUFFIX_FILE);
	}

	@Override
	public EditingDomain getEditingDomain() {
		return editContext.getEditingDomain();
	}
	
	@Override
	public boolean isEditor() {
		// Return true to avoid being initialised a second time in FormEditor, with the wrong editor site
		return true;
	}
}
