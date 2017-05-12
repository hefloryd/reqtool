package com.rtlabs.reqtool.ui.editors.support;

import org.eclipse.jface.viewers.IToolTipProvider;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.swt.widgets.Event;

public class ModelTooltip extends NatTableContentTooltip {

	private IRowDataProvider<?> dataProvider;
	private IToolTipProvider toolTips;

	public ModelTooltip(NatTable natTable, IRowDataProvider<?> dataProvider, IToolTipProvider toolTips, String... tooltipRegions) {
		super(natTable, tooltipRegions);
		this.dataProvider = dataProvider;
		this.toolTips = toolTips;

		if (tooltipRegions.length < 1) throw new IllegalArgumentException();
	}

	@Override
	protected String getText(Event event) {
		int row = natTable.getRowPositionByY(event.y);
		if (row == -1) return null;
		int rowIx = natTable.getRowIndexByPosition(row);
		if (rowIx == -1) return null;
		Object o = dataProvider.getRowObject(rowIx);
		if (o == null) return null;
		return toolTips.getToolTipText(o);
	}
}
