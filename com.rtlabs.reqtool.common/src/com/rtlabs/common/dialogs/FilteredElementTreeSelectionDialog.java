/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.rtlabs.common.dialogs;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.SearchPattern;

/**
 * An element tree selection dialog with a filter box on top.
 */
public class FilteredElementTreeSelectionDialog extends ElementTreeSelectionDialog {

	// private static int SEARCH_RULES = SearchPattern.RULE_EXACT_MATCH | SearchPattern.RULE_PATTERN_MATCH | SearchPattern.RULE_CAMELCASE_MATCH | SearchPattern.RULE_BLANK_MATCH;
	
	private String fInitialFilter;
	private boolean fQuickSelectionMode = false;

	public FilteredElementTreeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
		super(parent, labelProvider, contentProvider);
		fInitialFilter = null;
	}

	/**
	 * A comma separate list of patterns that are filled in initial filter list.
	 * Example is: '*.jar, *.zip'
	 *
	 * @param initialFilter the initial filter or <code>null</code>.
	 */
	public void setInitialFilter(String initialFilter) {
		fInitialFilter = initialFilter;
	}

	public void setQuickSelectionMode(boolean fQuickSelectionMode) {
		this.fQuickSelectionMode = fQuickSelectionMode;
	}
	
//	ViewerFilter deepFilter = new ViewerFilter() {
//		@Override
//		public boolean select(Viewer v, Object parent, Object elem) {
//			StructuredViewer viewer = (StructuredViewer) v;
//			ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
//
//			if (contentProvider.hasChildren(elem)) {
//				for (Object child : contentProvider.getChildren(elem)) {
//					if (select(viewer, elem, child)) return true;
//				}
//			} else {
//				// Also apply deep filtering to the other registered filters
//				for (ViewerFilter otherFilter : viewer.getFilters()) {
//					if (otherFilter == this) continue;
//					if (!otherFilter.select(viewer, elem, elem)) return false;
//				}
//			}
//			
//			return true;
//		}
//
//	};

	@Override
	public TreeViewer getTreeViewer() {
		return super.getTreeViewer();
	}
	
	
	private PatternFilter filter = new PatternFilter() {
		private SearchPattern pattern = new SearchPattern();
		// private SearchPattern pattern = new SearchPattern(SEARCH_RULES);
		
		@Override
		protected boolean wordMatches(String text) {
			return pattern.matches(text);
		}
		
		@Override
		public void setPattern(String patternText) {
			super.setPattern("<DUMMY>");
			pattern.setPattern(patternText == null ? "" : patternText);
		}

		@Override
		public boolean isElementVisible(Viewer v, Object elem) {
			StructuredViewer viewer = (StructuredViewer) v;
			ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();

			if (!super.isElementVisible(viewer, elem)) return false;

			// Also apply deep filtering to the other registered filters
			for (ViewerFilter f : viewer.getFilters()) {
				if (f == this) continue;
				if (!f.select(viewer, contentProvider.getParent(elem), elem)) return false;
			}
			return true;
		}

	};

	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {

		FilteredTree tree = new FilteredTree(parent, style, filter, true) {
			@Override
			protected void updateTreeSelection(boolean setFocus) {
				getViewer().getTree().deselectAll();
				super.updateTreeSelection(setFocus);
			}
		};
		
		filter.setPattern(fInitialFilter);
		
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setQuickSelectionMode(fQuickSelectionMode);
		applyDialogFont(tree);
		return tree.getViewer();
	}
}
