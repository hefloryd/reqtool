package com.rtlabs.reqtool.ui.views;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.eclipse.app4mc.capra.adapter.DefaultTraceMetamodelAdapter;
import org.eclipse.app4mc.capra.generic.adapters.TraceMetamodelAdapter;
import org.eclipse.app4mc.capra.simpletrace.tracemetamodel.SimpleTraceModel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rtlabs.reqtool.model.fixture.RequirementService;
import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Specification;

public class TraceNodeContentProviderTest {

	
	private static TraceMetamodelAdapter metaModelAdapter;
	private static Specification specification;
	private static List<Requirement> requirements;
	private static SimpleTraceModel traceModel;

	// Class under test
	private TraceNodeContentProvider provider;	

	@BeforeClass
	public static void beforeClass() {
		metaModelAdapter = new DefaultTraceMetamodelAdapter();		 
		specification = RequirementService.getInstance().getSpecification();
		requirements = specification.getRequirements();		
		traceModel = specification.getTraceModel();
	}

	@Before
	public void setup() {
		provider = new TraceNodeContentProvider(metaModelAdapter, Optional.of(traceModel));
	}
	
	@Test
	public void testGetElements() {		
		Requirement entity = requirements.get(6);
		Object[] elements = provider.getElements(entity);
		// Should return all reachable elements including itself
		assertEquals(17, elements.length);
	}

	@Test
	public void testGetConnectedToSingle() {
		Requirement entity = requirements.get(5);
		Object[] connections = provider.getConnectedTo(entity);
		assertEquals(1, connections.length);
	}

	@Test
	public void testGetConnectedToMany() {
		Requirement entity = requirements.get(0);
		Object[] connections = provider.getConnectedTo(entity);
		assertEquals(7, connections.length);
	}

}
