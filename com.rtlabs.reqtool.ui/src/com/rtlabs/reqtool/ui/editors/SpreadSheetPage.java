package com.rtlabs.reqtool.ui.editors;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.rtlabs.reqtool.ui.Activator;

/**
 * Displays and edits a list of requirements.
 */
class SpreadSheetPage extends FormPage {
	private SpreadSheetEditor editor;


	public SpreadSheetPage(SpreadSheetEditor editor) {
		super(editor, Activator.PLUGIN_ID, "Requirements");
		this.editor = editor;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		// super.init(site, input);
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

		RequirementTableBuilder tableBuilder = new RequirementTableBuilder(editor.getAdapterFactory(), editor.getSpecificationValue(), body, getSite());
		tableBuilder.build();
		NatTable natTable = tableBuilder.getTable();
		
		getSite().setSelectionProvider(tableBuilder.getRowSelectionProvider());

		// Listen to model changes, refresh UI
		editor.getSpecificationValue().eAdapters().add(new EContentAdapter() {
			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				natTable.refresh();
			}
		});
	}
}
