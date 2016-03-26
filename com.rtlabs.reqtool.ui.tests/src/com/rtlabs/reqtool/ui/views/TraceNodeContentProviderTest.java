package com.rtlabs.reqtool.ui.views;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.eclipse.app4mc.capra.adapter.DefaultTraceMetamodelAdapter;
import org.eclipse.app4mc.capra.generic.adapters.TraceMetamodelAdapter;
import org.eclipse.emf.ecore.EObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rtlabs.reqtool.model.fixture.RequirementService;
import com.rtlabs.reqtool.model.persistence.TracePersistenceAdapter;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Specification;

public class TraceNodeContentProviderTest {
	
	private static TraceMetamodelAdapter metaModelAdapter;
	private static TracePersistenceAdapter tracePersistenceAdapter;	
	private static Specification specification;
	private static List<Requirement> requirements;

	// Class under test
	private TraceNodeContentProvider provider;
	private static Optional<EObject> traceModel;

	@BeforeClass
	public static void beforeClass() {
		metaModelAdapter = new DefaultTraceMetamodelAdapter();
		tracePersistenceAdapter = new TracePersistenceAdapter();
		specification = RequirementService.getInstance().getSpecification();
		traceModel = Optional.of(specification.getTraceModel());
		requirements = specification.getRequirements();		
	}

	@Before
	public void setup() {
		provider = new TraceNodeContentProvider(metaModelAdapter, tracePersistenceAdapter);
	}
	
	@Test
	public void testGetElements() {		
		Requirement entity = requirements.get(6);
		Object[] elements = provider.getElements(entity, traceModel);
		// Should return all reachable elements including itself
		assertEquals(17, elements.length);
	}

	@Test
	public void testGetConnectedToSingle() {
		Requirement entity = requirements.get(5);
		Object[] connections = provider.getConnectedTo(entity, traceModel);
		assertEquals(1, connections.length);
	}

	@Test
	public void testGetConnectedToMany() {
		Requirement entity = requirements.get(0);
		Object[] connections = provider.getConnectedTo(entity, traceModel);
		assertEquals(7, connections.length);
	}

}
