package org.caleydo.view.tourguide.v3.ui;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.tourguide.v3.model.ARankColumnModel;
import org.caleydo.view.tourguide.v3.model.IRow;
import org.caleydo.view.tourguide.v3.model.mixin.IRankableColumnMixin;

public class TableColumnUI extends GLElementContainer implements IGLLayout {
	private final ARankColumnModel model;

	public TableColumnUI(ARankColumnModel model) {
		this.model = model;
		this.setLayoutData(model);
		this.setLayout(this);
	}

	public TableColumnUI setData(Collection<IRow> rows) {
		Set<IRow> target = new LinkedHashSet<>(rows);
		for (Iterator<GLElement> it = asList().iterator(); it.hasNext();) {
			GLElement elem = it.next();
			IRow row = elem.getLayoutDataAs(IRow.class, null);
			if (!target.remove(row))
				it.remove();
		}
		for (IRow row : target) {
			this.add(new GLElement(model.getValueRenderer()).setLayoutData(row));
		}
		return this;
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		((IColumModelLayout) getParent()).layoutRows(model, children, w, h);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (model instanceof IRankableColumnMixin) {
			// IRankableColumnMixin m = (IRankableColumnMixin) model;
			//
			// final GL2 gl = g.gl;
			// final float z = g.z();
			// // render the quad strip of between the columns
			// g.color(model.getBgColor());
			// gl.glBegin(GL2.GL_QUAD_STRIP);
			// gl.glVertex3f(0, 0, z);
			// gl.glVertex3f(w, 0, z);
			// for (GLElement elem : this) {
			// Vec2f xy = elem.getLocation();
			// Vec2f wh = elem.getSize();
			// if (wh.x() <= 0)
			// break;
			// float v = m.getValue(elem.getLayoutDataAs(IRow.class, null));
			// gl.glVertex3f(xy.x(), xy.y() + 2, z);
			// gl.glVertex3f(xy.x() + w * v, xy.y() + 2, z);
			// gl.glVertex3f(xy.x(), xy.y() + wh.y() - 2, z);
			// gl.glVertex3f(xy.x() + w * v, xy.y() + wh.y() - 2, z);
			// }
			// g.gl.glEnd();
			//

		}
		super.renderImpl(g, w, h);
	}
}