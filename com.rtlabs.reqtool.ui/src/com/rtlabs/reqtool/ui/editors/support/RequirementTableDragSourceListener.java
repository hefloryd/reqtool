package com.rtlabs.reqtool.ui.editors.support;

import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.selection.ISelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

public class RequirementTableDragSourceListener implements DragSourceListener {

	private SelectionLayer selectionLayer;
	private NatTable natTable;
	private List<?> selection;
	
	public RequirementTableDragSourceListener(SelectionLayer selectionLayer, NatTable natTable) {
		this.selectionLayer = selectionLayer;
		this.natTable = natTable;
	}

	@Override
	public void dragStart(DragSourceEvent event) {
        if (this.selectionLayer.getSelectedRowCount() == 0) {
            event.doit = false;
        } else if (!this.natTable.getRegionLabelsByXY(event.x, event.y)
                .hasLabel(GridRegion.BODY)) {
            event.doit = false;
        } else {
        	// Save selection when drag starts
        	ISelectionModel sm = this.selectionLayer.getSelectionModel();
        	if (sm instanceof RowSelectionModel<?>) {        	        	
                RowSelectionModel<?> selectionModel = (RowSelectionModel<?>) sm;
				selection = selectionModel.getSelectedRowObjects();                                  	
        	}
        }
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		// Return selection from when drag started
        event.data = new StructuredSelection(selection);
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
        // Clear selection
        this.selectionLayer.clear();
        this.natTable.refresh();
	}

}
