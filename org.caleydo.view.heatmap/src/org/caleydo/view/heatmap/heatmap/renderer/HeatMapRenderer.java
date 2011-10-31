package org.caleydo.view.heatmap.heatmap.renderer;

import static org.caleydo.view.heatmap.HeatMapRenderStyle.FIELD_Z;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.dimension.DataRepresentation;
import org.caleydo.core.data.selection.RecordSelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.mapping.color.ColorMapper;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.view.heatmap.heatmap.GLHeatMap;
import org.caleydo.view.heatmap.heatmap.template.AHeatMapTemplate;

public class HeatMapRenderer extends AHeatMapRenderer {

	public HeatMapRenderer(GLHeatMap heatMap) {
		super(heatMap);
	}

	@Override
	public void updateSpacing(ElementLayout parameters) {

		AHeatMapTemplate heatMapTemplate = heatMap.getTemplate();

		int nrRecordElements = heatMap.getDataContainer().getRecordPerspective()
				.getVirtualArray().size();

		RecordSelectionManager selectionManager = heatMap.getRecordSelectionManager();
		if (heatMap.isHideElements()) {

			nrRecordElements -= selectionManager
					.getNumberOfElements(GLHeatMap.SELECTION_HIDDEN);
		}

		recordSpacing.calculateRecordSpacing(nrRecordElements, heatMap.getDataContainer()
				.getDimensionPerspective().getVirtualArray().size(),
				parameters.getSizeScaledX(), parameters.getSizeScaledY(),
				heatMapTemplate.getMinSelectedFieldHeight());
		heatMapTemplate.setContentSpacing(recordSpacing);
	}

	@Override
	public void render(final GL2 gl) {

		ColorMapper colorMapper = heatMap.getDataDomain().getColorMapper();
		recordSpacing.getYDistances().clear();
		float yPosition = y;
		float xPosition = 0;
		float fieldHeight = 0;
		float fieldWidth = recordSpacing.getFieldWidth();

		// DimensionVirtualArray dimensionVA =

		for (Integer recordID : heatMap.getDataContainer().getRecordPerspective()
				.getVirtualArray()) {
			fieldHeight = recordSpacing.getFieldHeight(recordID);

			// we treat normal and deselected the same atm

			if (heatMap.isHideElements()
					&& heatMap.getRecordSelectionManager().checkStatus(
							GLHeatMap.SELECTION_HIDDEN, recordID)) {
				recordSpacing.getYDistances().add(yPosition);
				continue;
			}

			yPosition -= fieldHeight;
			xPosition = 0;

			for (Integer dimensionID : heatMap.getDataContainer()
					.getDimensionPerspective().getVirtualArray()) {

				renderElement(gl, dimensionID, recordID, yPosition, xPosition,
						fieldHeight, fieldWidth, colorMapper);

				xPosition += fieldWidth;

			}

			recordSpacing.getYDistances().add(yPosition);
		}
	}

	private void renderElement(final GL2 gl, final int dimensionID, final int recordID,
			final float fYPosition, final float fXPosition, final float fFieldHeight,
			final float fFieldWidth, ColorMapper colorMapper) {

		// GLHelperFunctions.drawPointAt(gl, 0, fYPosition, 0);

		float value = heatMap.getDataDomain().getTable()
				.getFloat(heatMap.getRenderingRepresentation(), dimensionID, recordID);

		float fOpacity = 1.0f;

		if (heatMap
				.getDataDomain()
				.getTable()
				.containsDataRepresentation(DataRepresentation.UNCERTAINTY_NORMALIZED,
						dimensionID, recordID)) {
			// setSelectedElements = heatMap.getContentSelectionManager()
			// .getElements(SelectionType.MOUSE_OVER);
			// for (Integer selectedElement : setSelectedElements) {
			// if (recordIndex == selectedElement.intValue()) {
			// fOpacity = dimension.getFloat(
			// EDataRepresentation.UNCERTAINTY_NORMALIZED,
			// recordIndex);
			// }
			// }
		} else if (heatMap.getRecordSelectionManager().checkStatus(
				SelectionType.DESELECTED, recordID)) {
			fOpacity = 0.3f;
		}

		float[] fArMappingColor = colorMapper.getColor(value);

		gl.glColor4f(fArMappingColor[0], fArMappingColor[1], fArMappingColor[2], fOpacity);

		gl.glPushName(heatMap.getPickingManager().getPickingID(heatMap.getID(),
				PickingType.HEAT_MAP_DIMENSION_SELECTION, dimensionID));
		gl.glPushName(heatMap.getPickingManager().getPickingID(heatMap.getID(),
				PickingType.HEAT_MAP_LINE_SELECTION, recordID));
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex3f(fXPosition, fYPosition, FIELD_Z);
		gl.glVertex3f(fXPosition + fFieldWidth, fYPosition, FIELD_Z);
		gl.glVertex3f(fXPosition + fFieldWidth, fYPosition + fFieldHeight, FIELD_Z);
		gl.glVertex3f(fXPosition, fYPosition + fFieldHeight, FIELD_Z);
		gl.glEnd();

		gl.glPopName();
		gl.glPopName();
	}

	public float getYCoordinateByContentIndex(int recordIndex) {
		if (recordSpacing != null)
			return y
					- recordSpacing.getYDistances().get(recordIndex)
					- recordSpacing.getFieldHeight(heatMap.getDataContainer()
							.getRecordPerspective().getVirtualArray().get(recordIndex))
					/ 2;
		return 0;
	}

	public float getXCoordinateByDimensionIndex(int dimensionIndex) {
		return recordSpacing.getFieldWidth() * dimensionIndex;
	}

	@Override
	public String toString() {
		return "HeatMapRenderer";
	}

}
