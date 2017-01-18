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

		public RichTextMultiLineCellPainter() {
			super(false);
		}

		@Override
		public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
			setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
			String htmlText = CellDisplayConversionUtils.convertDataType(cell, configRegistry);

			// Using a zero size rectangle for calculation results in a content related preferred size
			this.richTextPainter.preCalculate(htmlText, gc, new Rectangle(0, 0, cell.getBounds().width, 0), false);

			// Subtract the top and bottom paragraph space
			int contentHight = this.richTextPainter.getPreferredSize().y - 2 * this.richTextPainter.getParagraphSpace();
			
			// Adjustment value choosen by trying it in the GUI
			contentHight -= 20;
			
			if (contentHight > bounds.height) {
				int padding = cell.getBounds().height - bounds.height;
				int newHeight = contentHight + padding;
				
				// This doesn't seem necessary. But it kind of makes sense...
				//  bounds.y = newHeight;
				
				// Use the same way of adjusting size as TextPainter.paintCel
				cell.getLayer().doCommand(new RowResizeCommand(cell.getLayer(), cell.getRowPosition(), newHeight));
			}
			
			super.paintCell(cell, gc, bounds, configRegistry);
		}
	}