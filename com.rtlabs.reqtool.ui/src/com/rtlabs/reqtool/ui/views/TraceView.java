package com.rtlabs.reqtool.ui.views;

import java.util.Optional;

import org.eclipse.app4mc.capra.generic.adapters.TraceMetamodelAdapter;
import org.eclipse.app4mc.capra.generic.adapters.TracePersistenceAdapter;
import org.eclipse.app4mc.capra.generic.helpers.ExtensionPointHelper;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import com.rtlabs.reqtool.model.requirements.Requirement;

public class TraceView extends ViewPart implements IZoomableWorkbenchPart {

	private GraphViewer viewer;
	private ISelectionListener selectionListener;
	private TraceMetamodelAdapter metaModelAdapter;
	private TracePersistenceAdapter tracePersistenceAdapter;

	private class GlobalDeleteActionHandler extends Action {

		@Override
		public void run() {
			if (viewer.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Object element = selection.getFirstElement();
				if (element instanceof EntityConnectionData) {
					EntityConnectionData connectionData = (EntityConnectionData) element;
					EObject source = (EObject) connectionData.source;
					EObject dest = (EObject) connectionData.dest;
					
					Optional<EObject> traceModel = tracePersistenceAdapter.getTraceModel(source);
					
					metaModelAdapter.deleteTrace(source, dest, traceModel);
					viewer.refresh();
					viewer.applyLayout();
				}				
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		metaModelAdapter = ExtensionPointHelper.getTraceMetamodelAdapter().get();
		tracePersistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
		
		viewer = new GraphViewer(parent, SWT.BORDER);
		viewer.setContentProvider(new TraceNodeContentProvider(metaModelAdapter, tracePersistenceAdapter));
		viewer.setLabelProvider(new TraceNodeLabelProvider());
		viewer.setInput(null);
		
		LayoutAlgorithm layout = setLayout();
		viewer.setLayoutAlgorithm(layout, true);
	    viewer.applyLayout();
	    fillToolBar();
	    
	    
	    selectionListener = new ISelectionListener() {
			
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Object element = structuredSelection.getFirstElement();
					if (element instanceof Requirement) {
						viewer.setInput(element);
						viewer.refresh();
					}
				}
			}
		};
		getSite().getPage().addSelectionListener(selectionListener);		
	}

	
	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(selectionListener);
		super.dispose();
	}

	private LayoutAlgorithm setLayout() {
	    LayoutAlgorithm layout;
	    // layout = new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    layout = new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    // layout = new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    // layout = new HorizontalTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    // layout = new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    return layout;
	}

	@Override
	public void setFocus() {
	}

	private void fillToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		
		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
		bars.getMenuManager().add(toolbarZoomContributionViewItem);
		
		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), new GlobalDeleteActionHandler());
	}

	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}
}
