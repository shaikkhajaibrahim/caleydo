package org.caleydo.core.view.opengl.canvas.grouper.drawingstrategies.group;

import javax.media.opengl.GL;

import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.view.opengl.canvas.grouper.GrouperRenderStyle;
import org.caleydo.core.view.opengl.canvas.grouper.compositegraphic.GroupRepresentation;

import com.sun.opengl.util.j2d.TextRenderer;

public class GroupDrawingStrategyNormal
	extends AGroupDrawingStrategyRectangular {

	private PickingManager pickingManager;
	private GrouperRenderStyle renderStyle;
	private int iViewID;

	public GroupDrawingStrategyNormal(PickingManager pickingManager, int iViewID,
		GrouperRenderStyle renderStyle) {
		this.pickingManager = pickingManager;
		this.iViewID = iViewID;
		this.renderStyle = renderStyle;
	}

	@Override
	public void draw(GL gl, GroupRepresentation groupRepresentation, TextRenderer textRenderer) {

		gl.glPushName(pickingManager.getPickingID(iViewID, EPickingType.GROUPER_GROUP_SELECTION,
			groupRepresentation.getID()));
		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);

		gl.glColor4fv(renderStyle.getGroupColorForLevel(groupRepresentation.getHierarchyLevel()), 0);

		drawGroupRectangular(gl, groupRepresentation, textRenderer);

		gl.glPopName();

		gl.glPushName(pickingManager.getPickingID(iViewID, EPickingType.GROUPER_COLLAPSE_BUTTON_SELECTION,
			groupRepresentation.getID()));

		drawCollapseButton(gl, groupRepresentation, textRenderer);

		gl.glPopName();

		gl.glPopAttrib();

		drawChildren(gl, groupRepresentation, textRenderer);

	}

	@Override
	public void drawAsLeaf(GL gl, GroupRepresentation groupRepresentation, TextRenderer textRenderer) {

		gl.glPushName(pickingManager.getPickingID(iViewID, EPickingType.GROUPER_GROUP_SELECTION,
			groupRepresentation.getID()));
		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);

		gl.glColor4fv(GrouperRenderStyle.TEXT_BG_COLOR, 0);

		drawLeafRectangular(gl, groupRepresentation, textRenderer);

		gl.glPopAttrib();

		gl.glPopName();

	}

}
