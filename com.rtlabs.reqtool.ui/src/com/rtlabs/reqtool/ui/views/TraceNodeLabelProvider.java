package com.rtlabs.reqtool.ui.views;

import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapper;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;

import com.rtlabs.reqtool.model.requirements.Requirement;

public class TraceNodeLabelProvider extends LabelProvider implements IEntityStyleProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof Requirement) {
			Requirement requirement = (Requirement) element;
			return requirement.getName();			
		}
		else if (element instanceof ArtifactWrapper) {
			ArtifactWrapper artifactWrapper = (ArtifactWrapper) element;
			return artifactWrapper.getUri();			
		}
		else if (element instanceof EntityConnectionData) {
			return null;
		}
		else if (element != null) {
			return element.toString();
		}
		return null;			
	}

	@Override
	public Color getNodeHighlightColor(Object entity) {
		return null;
	}

	@Override
	public Color getBorderColor(Object entity) {
		return null;
	}

	@Override
	public Color getBorderHighlightColor(Object entity) {
		return null;
	}

	@Override
	public int getBorderWidth(Object entity) {
		return 0;
	}

	@Override
	public Color getBackgroundColour(Object entity) {
		return null;
	}

	@Override
	public Color getForegroundColour(Object entity) {
		return null;
	}

	@Override
	public IFigure getTooltip(Object entity) {
		if (entity instanceof Requirement) {
			Requirement requirement = (Requirement) entity;
			IFigure tooltip = new Figure();
			tooltip.setBorder(new MarginBorder(5,5,5,5));
			FlowLayout layout = new FlowLayout(false);
			layout.setMajorSpacing(3);
			layout.setMinorAlignment(3);
			tooltip.setLayoutManager(new FlowLayout(false));
			tooltip.add(new Label(requirement.getBody()));
			return tooltip;
		}
		return null;
	}

	@Override
	public boolean fisheyeNode(Object entity) {
		return false;
	}

	
}
