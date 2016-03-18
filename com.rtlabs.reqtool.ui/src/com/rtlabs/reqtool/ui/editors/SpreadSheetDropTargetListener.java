package com.rtlabs.reqtool.ui.editors;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.graphics.Point;

import com.rtlabs.reqtool.model.requirements.Requirement;
import com.rtlabs.reqtool.model.requirements.Specification;
import com.rtlabs.reqtool.ui.TraceManager;

public class SpreadSheetDropTargetListener implements DropTargetListener {
	private final NatTable natTable;
	private final Specification specification;
	private final DataLayer bodyDataLayer;
	private GridLayer gridLayer;
	private int lastPosition;

	SpreadSheetDropTargetListener(NatTable natTable, GridLayer gridLayer, Specification specification,
			DataLayer bodyDataLayer) {
		this.natTable = natTable;
		this.gridLayer = gridLayer;
		this.specification = specification;
		this.bodyDataLayer = bodyDataLayer;
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
	}

	@Override
	public void drop(DropTargetEvent event) {
		if (event.data instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) event.data;
			Point pt = event.display.map(null, natTable, event.x, event.y);
			int position = natTable.getRowPositionByY(pt.y);
			if (position > 0) {
				position = LayerUtil.convertRowPosition(gridLayer.getBodyLayer(), position - 1, bodyDataLayer);
				Requirement requirement = specification.getRequirements().get(position);
				TraceManager traceManager = new TraceManager();
				traceManager.createTrace(requirement, selection);
			}
		}
	}

	@Override
	public void dragOver(DropTargetEvent event) {
		//event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
		Point pt = event.display.map(null, natTable, event.x, event.y);
		int position = natTable.getRowPositionByY(pt.y);
		if (position > 0 && position != lastPosition) {
			natTable.doCommand(new SelectRowsCommand(gridLayer.getBodyLayer(), 0, position - 1, false, false));
			lastPosition = position;
		}
	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {				
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
		natTable.doCommand(new ClearAllSelectionsCommand());
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		lastPosition = -1;
		event.detail = DND.DROP_COPY;
	}
}

