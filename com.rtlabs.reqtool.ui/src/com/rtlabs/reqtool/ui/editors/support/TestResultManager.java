package com.rtlabs.reqtool.ui.editors.support;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.capra.core.adapters.ArtifactMetaModelAdapter;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.builds.core.IBuild;
import org.eclipse.mylyn.builds.core.IBuildPlan;
import org.eclipse.mylyn.builds.core.IBuildServer;
import org.eclipse.mylyn.builds.core.ITestCase;
import org.eclipse.mylyn.builds.core.ITestResult;
import org.eclipse.mylyn.builds.core.ITestSuite;
import org.eclipse.mylyn.builds.core.spi.BuildConnector;
import org.eclipse.mylyn.builds.core.spi.BuildPlanRequest;
import org.eclipse.mylyn.builds.core.spi.BuildServerBehaviour;
import org.eclipse.mylyn.builds.core.spi.GetBuildsRequest;
import org.eclipse.mylyn.builds.core.spi.GetBuildsRequest.Kind;
import org.eclipse.mylyn.builds.core.spi.GetBuildsRequest.Scope;
import org.eclipse.mylyn.builds.ui.BuildsUi;
import org.eclipse.mylyn.commons.core.operations.OperationUtil;
import org.eclipse.ui.statushandlers.StatusManager;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.rtlabs.reqtool.ui.Activator;

/**
 * Fetches test results from a build server using the Mylyn API.  
 */
@SuppressWarnings("restriction")
public class TestResultManager {
	private static class IntermediateResult {
		ITestResult result;
		IStatus status;

		public IntermediateResult(ITestResult reslut) {
			this.result = reslut;
			this.status = Status.OK_STATUS;
		}
		
		public IntermediateResult(String errorMessage, Exception exc) {
			result = null;
			status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMessage, exc);
		}
	}
	
	
	/**
	 * Returns test failures from a build server configured in Mylyn. The test results are matched
	 * agails the provided list of traces, pairing test failures with traces.
	 *  
	 * @param buildNumber Can be null or empty, in which case the latest build is used 
	 */
	public static CompletionStage<TestRunResult> fetchTestResults(
		String serverUri, String buildPlan, String buildNumber, List<? extends EObject> traces, EObject traceModel)
	{
		CompletableFuture<IntermediateResult> future = new CompletableFuture<>();
		
		new Job("Feching test results from server at '" + serverUri + "'.") {
			public IStatus run(IProgressMonitor mon) {
				fetchBuild(serverUri, buildPlan, buildNumber, future, mon);
				return Status.OK_STATUS;
			}

		}.schedule();
		
		return future.thenApply(result -> {
			if (!result.status.isOK()) {
				TestRunResult testRunResult = new TestRunResult();
				testRunResult.status = result.status;
				return testRunResult;
			} else {
				return TestResultManager.matchTestsWithTraces(traces, traceModel, result.result);
			}
		});
	}

	private static void fetchBuild(String serverUrl, String buildPlan, String buildNumber, CompletableFuture<IntermediateResult> future, IProgressMonitor mon) {
		SubMonitor monitor = SubMonitor.convert(mon, 20);

		IBuildServer server = BuildsUi.getModel().getServers().stream()
			.filter(b -> b.getUrl().equals(serverUrl))
			.findFirst().orElse(null);
		
		if (server == null) {
			future.complete(new IntermediateResult("Could not find server with URL '" + serverUrl + "'.", null));
			return;
		}
		
		BuildConnector conn = BuildsUi.getConnector(server);
		
		try {
			BuildServerBehaviour behav = conn.getBehaviour(server.getLocation());
			monitor.subTask("Feching build plan '" + buildPlan + "'.");

			List<IBuildPlan> plans = behav.getPlans(new BuildPlanRequest(ImmutableList.of(buildPlan)),
				OperationUtil.convert(monitor.split(10), 10));
		
			
			if (plans.isEmpty()) {
				future.complete(new IntermediateResult("No build plan '" + buildPlan + "' found on the build server.", null));
				return;
			}

			GetBuildsRequest buildsReq = Strings.isNullOrEmpty(buildNumber)
				? new GetBuildsRequest(plans.get(0), Kind.LAST)
				: new GetBuildsRequest(plans.get(0), ImmutableList.of(buildNumber), Scope.FULL);
				
			monitor.subTask("Feching builds.");
			
			List<IBuild> builds = behav.getBuilds(buildsReq, OperationUtil.convert(monitor.split(10), 10));
			
			if (builds.isEmpty()) {
				if (buildNumber == null) {
					future.complete(new IntermediateResult("No builds exist on the build server.", null));
				} else {
					future.complete(new IntermediateResult("No build number " + buildNumber + " exists on the build server.", null));
				}
				return;
			}
			
			future.complete(new IntermediateResult(builds.get(0).getTestResult()));
		} catch (Exception exc) {
			future.complete(new IntermediateResult("An unexpected error occured while feching test results.", exc));
		}
	}

	
	private static TestRunResult matchTestsWithTraces(List<? extends EObject> traces,
		EObject traceModel,
		ITestResult result) {
		// Build map from tets class name to test case
		Multimap<String, ITestCase> tests =  ArrayListMultimap.create();
		for (ITestSuite s : result.getSuites()) {
			for (ITestCase c : s.getCases()) {
//				if (c.getStatus() != TestCaseResult.PASSED && c.getStatus() != TestCaseResult.FIXED) { 
					tests.put(c.getClassName(), c);
//				}
			}
		}
		
		ArtifactMetaModelAdapter artifactAdapter = ExtensionPointHelper.getArtifactWrapperMetaModelAdapter().get();
		TraceMetaModelAdapter traceAdapter = ExtensionPointHelper.getTraceMetamodelAdapter().get();
		
		TestRunResult testRunResult = new TestRunResult();

		for (EObject req : traces) {
			for (Connection conn : traceAdapter.getConnectedElements(req, null)) {
				for (EObject child : conn.getTargets()) {
					String artUri = artifactAdapter.getArtifactUri(child);
					URIBuilder uriBuilder;
					try {
						uriBuilder = new URIBuilder(artUri);
					} catch (URISyntaxException exc) {
						StatusManager.getManager().handle(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 
							"Artifact '" + child + "' has the unparsable URL '" + artUri + "'"));
						continue;
					}
						
					String[] elems = StringUtils.split(uriBuilder.getFragment(), '/');

					if (elems == null || elems.length < 1 || Strings.isNullOrEmpty(elems[0])) {
						StatusManager.getManager().handle(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 
							"Encountered artifact URI '" + artUri +"' with no fragment part"));
						continue;
					}
					
					String artifactClass = elems[0];
					String artifactMethod = elems.length < 2 ? null : elems[1];

					for (ITestCase test : tests.get(artifactClass)) {
						if (test.getClassName().equals(artifactClass)
							&& (artifactMethod == null || Objects.equals(test.getLabel(), artifactMethod)))
						{
							testRunResult.results.put(req, new TestCaseRunResult(
								req, toStatus(test), test.getClassName(), test.getLabel(), test.getMessage()));
						}
					}
				}
			}
		}
		
		return testRunResult;
	}
	
	
	public static class TestRunResult {
		private IStatus status = Status.OK_STATUS;
		
		Multimap<EObject, TestCaseRunResult> results = ArrayListMultimap.create();
		
		public Collection<TestCaseRunResult> getResults(EObject req) {
			return results.get(req);
		}

		public IStatus getStatus() {
			return status;
		}
	}
	
	/**
	 * Contains information from a test run matched with a trace object. 
	 */
	public static class TestCaseRunResult {
		private final EObject trace;
		private final String className;
		private final String fragment;
		private final String message;
		
		private final IStatus status;

		public TestCaseRunResult(EObject trace, IStatus status, String className, String fragment, String message) {
			this.trace = trace;
			this.className = className;
			this.fragment = fragment;
			this.message = message;
			this.status = status;
		}

		/**
		 * @return OK if the operation succeded and there is a result. ERROR
		 *         with a message with the operaiont failed.
		 */
		public IStatus getStatus() {
			return status;
		}

		public EObject getTrace() {
			return trace;
		}

		public String getClassName() {
			return className;
		}

		public String getFragment() {
			return fragment;
		}

		public String getMessage() {
			return message;
		}

		public String getDisplayMessage() {
			return "Test with status " + getStatus().getMessage() + ": " + getMessage() + "\n"
				+ "Suite: '" + getClassName() + "'\n"
				+ "Case: '" + getFragment() + "')";
		}
	}

	private static IStatus toStatus(ITestCase c) {
		switch (c.getStatus()) {
			case FIXED:      return new Status(IStatus.INFO, Activator.PLUGIN_ID, "FIXED");
			case PASSED:     return new Status(IStatus.INFO, Activator.PLUGIN_ID, "PASSED");
			case FAILED:     return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "FAILED"); 
			case REGRESSION: return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "REGRESSION");
			case SKIPPED:    return new Status(IStatus.WARNING, Activator.PLUGIN_ID, "SKIPPED");
		}
		
		throw new IllegalArgumentException();
	}
}

