package com.rtlabs.reqtool.ui.editors;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.rtlabs.common.edit_support.EditContext;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.ui.Activator;

/**
 * Displays and edits a list of requirements.
 */
class SpreadSheetPage extends FormPage implements IEditingDomainProvider {
	private EditContext editContext;
	private IObservableValue<Specification> specification;


	public SpreadSheetPage(SpreadSheetEditor editor, IObservableValue<Specification> specification) {
		super(editor, Activator.PLUGIN_ID, "Requirements");
		this.editContext = editor;
		this.specification = specification;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		// super.init(new MultiPageEditorSite((MultiPageEditorPart) site.getPart(), this), input);
		super.init(site, input);
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		Composite body = form.getBody();
		toolkit.paintBordersFor(body);
		body.setLayout(new FillLayout());
		
		// parent.setLayout(new GridLayout());

		// Create/load the model

		RequirementTableBuilder tableBuilder = new RequirementTableBuilder(editContext.getAdapterFactory(), 
			specification.getValue(), body, getSite());
		
		tableBuilder.build();
		NatTable natTable = tableBuilder.getTable();
		
		getSite().setSelectionProvider(tableBuilder.getRowSelectionProvider());

		// Listen to model changes, refresh UI
		specification.getValue().eAdapters().add(new EContentAdapter() {
			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				natTable.refresh();
			}
		});
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
