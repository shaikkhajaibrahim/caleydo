/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.pathway.v2.internal;

import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.pathway.v2.ui.AverageColorMappingAugmentation;
import org.caleydo.view.pathway.v2.ui.PathwayElement;
import org.caleydo.view.pathway.v2.ui.PathwayTextureRenderer;

import com.google.common.base.Supplier;

/**
 * factory element for creating pathway elements
 *
 * @author Samuel Gratzl
 *
 */
public class PathwayElementFactory implements IGLElementFactory {

	@Override
	public String getId() {
		return "pathway";
	}

	@Override
	public GLElement create(GLElementFactoryContext context) {
		PathwayGraph pathway = context.get(PathwayGraph.class, GLLayoutDatas.<PathwayGraph> throwInvalidException());
		PathwayElement elem = new PathwayElement();

		// TODO
		elem.setPathwayRepresentation(new PathwayTextureRenderer(pathway));
		AverageColorMappingAugmentation colorMappingAugmentation = new AverageColorMappingAugmentation(
				elem.getPathwayRepresentation());
		colorMappingAugmentation.setEventSpace(context.get("eventSpace", String.class, new Supplier<String>() {
			@Override
			public String get() {
				return EventPublisher.INSTANCE.createUniqueEventSpace();
			}
		}));
		elem.addBackgroundAugmentation(colorMappingAugmentation);

		return elem;
	}

	@Override
	public boolean apply(GLElementFactoryContext context) {
		if (context.get(PathwayGraph.class, null) == null)
			return false; // no pathway

		return true;
	}

}
