package com.rtlabs.reqtool.ui.editors;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.concurrent.CompletionStage;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.google.common.base.Strings;
import com.rtlabs.common.edit_support.EditContext;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.ui.Activator;
import com.rtlabs.reqtool.ui.editors.support.RequirementTableBuilder;
import com.rtlabs.reqtool.ui.editors.support.RequirementTableBuilder.TestResultStatusProvider;
import com.rtlabs.reqtool.ui.editors.support.TestResultManager;
import com.rtlabs.reqtool.ui.editors.support.TestResultManager.TestCaseRunResult;
import com.rtlabs.reqtool.ui.editors.support.TestResultManager.TestRunResult;

/**
 * Displays and edits a list of requirements.
 */
class RequirementTablePage extends FormPage implements IEditingDomainProvider {
	private EditContext editContext;
	private IObservableValue<Specification> specification;
	private TestRunResult lastTestRunResult = null;
	private NatTable natTable;

	public RequirementTablePage(SpecificationEditor editor, IObservableValue<Specification> specification) {
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
			specification.getValue(), body, getSite(), createTestResultToolTipProvider());
		
		tableBuilder.setDecorateRequirementsAction(new DecorateRequirementsAction());
		
		tableBuilder.build();
		natTable = tableBuilder.getTable();
		
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

	private TestResultStatusProvider createTestResultToolTipProvider() {
		return (Requirement elem) -> {
				if (lastTestRunResult == null) return null;
				
				Collection<TestCaseRunResult> reqResults = lastTestRunResult.getResults(elem);
				
				int severity = reqResults.stream()
					.map(r -> r.getStatus())
					.mapToInt(IStatus::getSeverity)
					.max()
					.orElse(-1);
				
				if (severity == -1) return null;

				String messages = reqResults.stream()
					.map(caseResult -> formatTestResult(caseResult))
					.collect(joining("\n"));
				
				return new Status(severity, Activator.PLUGIN_ID, messages);
		};
	}

	private String formatTestResult(TestCaseRunResult caseResult) {
		StringBuilder b = new StringBuilder();
		
		b.append("Test with status ").append(caseResult.getStatus().getMessage()).append(": ");
		
		if (caseResult.getStatus().getSeverity() > IStatus.INFO) {
			b.append(caseResult.getMessage());
		}
		
		b.append("\n");
		b.append("  Suite: " + caseResult.getClassName() + "\n");
		b.append("  Case: " + caseResult.getFragment());
		
		return b.toString();
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
	
	public void decorateWithTestResults() {
		Specification spec = specification.getValue();
		
		if (Strings.isNullOrEmpty(spec.getBuildServerUrl())) {
			MessageDialog.openError(getSite().getShell(), "Error when fetching test results", 
				"No build server URL has been configured.");
			return;
		}

		if (Strings.isNullOrEmpty(spec.getBuildPlan())) {
			MessageDialog.openError(getSite().getShell(), "Error when fetching test results", 
				"No build plan has been configured.");
			return;
		}
		
		CompletionStage<TestRunResult> future = TestResultManager.fetchTestResults(
			spec.getBuildServerUrl(), spec.getBuildPlan(), 
			spec.getBuildNumber(), spec.getRequirements(), null);
		
		future.thenAcceptAsync(result -> {
				if (!result.getStatus().isOK()) {
					ErrorDialog dialog = new ErrorDialog(getSite().getShell(), 
						"Error when fetching test results", "An error occured while fetching test results.", 
						result.getStatus(), IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
					dialog.open();
				}

				lastTestRunResult = result;
				natTable.refresh();
			}, 
			getSite().getShell().getDisplay()::asyncExec);
	}
	
	private class DecorateRequirementsAction extends Action {
		public DecorateRequirementsAction() {
			this.setText("Decorate Requirements with Test Results");
		}
		@Override
		public void run() {
			decorateWithTestResults();
		}
	}
}
