package com.rtlabs.reqtool.ui.views;

import org.eclipse.capra.core.adapters.ArtifactMetaModelAdapter;
import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.adapters.TracePersistenceAdapter;
import org.eclipse.capra.core.handlers.ArtifactHandler;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import com.rtlabs.reqtool.model.requirements.Artifact;
import com.rtlabs.reqtool.model.requirements.Requirement;

public class TraceView extends ViewPart implements IZoomableWorkbenchPart, IShowInSource {

	private GraphViewer viewer;
	private ISelectionListener selectionListener;
	private TraceMetaModelAdapter metaModelAdapter;
	private TracePersistenceAdapter tracePersistenceAdapter;
	private GlobalDeleteActionHandler deleteAction;
	private ArtifactMetaModelAdapter artifactAdapter;

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
					
					EObject traceModel = tracePersistenceAdapter.getTraceModel(source);
					
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
		artifactAdapter = ExtensionPointHelper.getArtifactWrapperMetaModelAdapter().get();
		
		viewer = new GraphViewer(parent, SWT.BORDER);
		viewer.setContentProvider(new TraceNodeContentProvider());
		viewer.setLabelProvider(new TraceNodeLabelProvider());
		viewer.setInput(null);
		
		LayoutAlgorithm layout = setLayout();
		viewer.setLayoutAlgorithm(layout, true);
	    viewer.applyLayout();
	    makeActions();
	    hookContextMenu();
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

	private void makeActions() {
		deleteAction = new GlobalDeleteActionHandler();		
		deleteAction.setText("Delete");
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
		
		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
	}

	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}
	
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		fillContextMenu(menuMgr);

		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);

			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		
		IShowInSource showInSource = (IShowInSource) getAdapter(IShowInSource.class);
		if (showInSource != null) {
			ShowInContext context = showInSource.getShowInContext();
			if (context != null) {
				ISelection sel = context.getSelection();
				if (sel != null && !sel.isEmpty()) {
					MenuManager showInSubMenu = new MenuManager("Show In");

					showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN.create(getViewSite().getWorkbenchWindow()));
					manager.add(showInSubMenu);
				}
			}
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(deleteAction);
	}

	@Override
	public ShowInContext getShowInContext() {
		IStructuredSelection selection = viewer.getStructuredSelection();
		if (selection.size() == 1) { 
			Object o = selection.getFirstElement();
			if (o instanceof Artifact) {
				Artifact artifact = (Artifact) o;
				ArtifactHandler handler = artifactAdapter.getArtifactHandlerInstance(artifact);
				Object handle = handler.resolveArtifact(artifact);
				return new ShowInContext(viewer.getInput(), new StructuredSelection(handle));
			}
		}
		return null;
	}

}
