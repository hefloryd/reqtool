package com.rtlabs.reqtool.ui.editors;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RichTextCellPainter;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellDisplayConversionUtils;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A painter which adjusts the hight of a cell to that of the painted markup. 
 */
class RichTextMultiLineCellPainter extends RichTextCellPainter {

	private final boolean adjustCellHeight;

	public RichTextMultiLineCellPainter(boolean adjustCellHeight) {
		super(false);
		this.adjustCellHeight = adjustCellHeight;
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		// Much of the code in this method is copied from the super class method. The difference
		// is code that adjust the hight. That code is copied from TextCellPaiter.
		
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		String htmlText = CellDisplayConversionUtils.convertDataType(cell, configRegistry);

		if (adjustCellHeight) {
		
			// Using a zero size rectangle for calculation results in a content related preferred size
			richTextPainter.preCalculate(htmlText, gc, new Rectangle(0, 0, cell.getBounds().width, 0), false);
	
			// Subtract the top and bottom paragraph space
			int contentHight = richTextPainter.getPreferredSize().y - 2 * this.richTextPainter.getParagraphSpace();
			
			// Adjustment value choosen by trying it in the GUI
			contentHight -= 20;
			
			if (contentHight > bounds.height) {
				int padding = cell.getBounds().height - bounds.height;
				int newHeight = contentHight + padding;
				
				// This doesn't seem necessary. But it kind of makes sense...
				//  bounds.height = newHeight;
				
				// Use the same way of adjusting size as TextPainter.paintCel
				cell.getLayer().doCommand(new RowResizeCommand(cell.getLayer(), cell.getRowPosition(), newHeight));
			}
		}
		
		richTextPainter.paintHTML(htmlText, gc, new Rectangle(
			bounds.x, bounds.y - richTextPainter.getParagraphSpace(), bounds.width, bounds.height));
	}
}
