package com.rtlabs.reqtool.ui.editors;

import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__DESCRIPTION;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__DOCUMENT_PREFIX_FILE;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__DOUCMENT_POSTFIX_FILE;
import static com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals.SPECIFICATION__TITLE;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
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
class SpecificationDetailsPage extends FormPage {
	private EditContext editor;
	private IObservableValue<Specification> specification;

	public SpecificationDetailsPage(SpreadSheetEditor editor, IObservableValue<Specification> spec) {
		super(editor, Activator.PLUGIN_ID + ".specificationDetailsPage", "Specification Details");
		this.editor = editor;
		this.specification = spec;
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
		
		ModelGuiBuilder<Specification> guiBuilder = new ModelGuiBuilder<>(toolkit, editor, specification);
		
		guiBuilder.createFeatureControl(container, SPECIFICATION__TITLE);
		guiBuilder.createFeatureControl(container, SPECIFICATION__DESCRIPTION);
		guiBuilder.createFeatureControl(container, SPECIFICATION__DOCUMENT_PREFIX_FILE);
		guiBuilder.createFeatureControl(container, SPECIFICATION__DOUCMENT_POSTFIX_FILE);
	}
}
