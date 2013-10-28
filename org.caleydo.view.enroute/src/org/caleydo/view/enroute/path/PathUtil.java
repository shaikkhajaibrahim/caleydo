/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.enroute.path;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

/**
 *
 * Utility class that provides methods to compute several pathway path related things.
 *
 * @author Christian Partl
 *
 */
public final class PathUtil {

	private PathUtil() {
	}

	/**
	 * @param segments
	 * @return One list of {@link PathwayVertexRep}s that contains all objects of the list of lists.
	 */
	public static List<PathwayVertexRep> flattenSegments(List<List<PathwayVertexRep>> segments) {
		List<PathwayVertexRep> vertexReps = new ArrayList<>();
		for (List<PathwayVertexRep> segment : segments) {
			vertexReps.addAll(segment);
		}
		return vertexReps;
	}

	/**
	 * Determines, whether the specified target path segments are shown by the source path segments. If the specified
	 * pathway is not null, only segments referring to this pathway are considered.
	 *
	 * @param segments
	 * @return
	 */
	public static boolean isPathShown(List<List<PathwayVertexRep>> sourcePathSegments,
			List<List<PathwayVertexRep>> targetPathSegments, PathwayGraph pathway) {
		List<PathwayVertexRep> sourceSegments = flattenSegments(sourcePathSegments);
		List<PathwayVertexRep> targetSegments = flattenSegments(targetPathSegments);
		int startIndex = 0;
		boolean equalityStarted = false;
		for (PathwayVertexRep vTarget : targetSegments) {
			// Ignore other pathway paths if this renderer only repersents a single pathway
			if (pathway != null && pathway != vTarget.getPathway())
				continue;
			if (startIndex >= sourceSegments.size())
				return false;
			for (int i = startIndex; i < sourceSegments.size(); i++) {
				PathwayVertexRep vSource = sourceSegments.get(i);
				startIndex = i + 1;
				// Ignore other pathway paths if this renderer only repersents a single pathway
				if (pathway != null && pathway != vSource.getPathway())
					continue;
				if (vTarget == vSource) {
					equalityStarted = true;
					break;
				} else if (equalityStarted) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Determines whether the specified path segments contain a vertex rep.
	 *
	 * @param pathSegments
	 * @param vertexRep
	 * @return
	 */
	public static boolean containsVertexRep(List<List<PathwayVertexRep>> pathSegments, PathwayVertexRep vertexRep) {
		List<PathwayVertexRep> segments = flattenSegments(pathSegments);

		for (PathwayVertexRep vSource : segments) {
			if (vSource == vertexRep)
				return true;
		}
		return false;
	}

	/**
	 * Determines the number of equal vertices of the specified paths.
	 *
	 * @param segments
	 * @return
	 */
	public static int getNumEqualVertices(List<List<PathwayVertexRep>> sourcePathSegments,
			List<List<PathwayVertexRep>> targetPathSegments) {
		List<PathwayVertexRep> sourceSegments = flattenSegments(sourcePathSegments);
		List<PathwayVertexRep> targetSegments = flattenSegments(targetPathSegments);

		int numEqualVertices = 0;
		for (PathwayVertexRep vTarget : targetSegments) {
			for (PathwayVertexRep vSource : sourceSegments) {
				if (vSource == vTarget) {
					numEqualVertices++;
					break;
				}
			}
		}

		return numEqualVertices;
	}

	/**
	 * Determines the set of vertices the specified paths have in common.
	 *
	 * @param sourcePathSegments
	 * @param targetPathSegments
	 * @return
	 */
	public static Set<PathwayVertexRep> getCommonVertices(List<List<PathwayVertexRep>> sourcePathSegments,
			List<List<PathwayVertexRep>> targetPathSegments) {
		List<PathwayVertexRep> sourceSegments = flattenSegments(sourcePathSegments);
		List<PathwayVertexRep> targetSegments = flattenSegments(targetPathSegments);

		Set<PathwayVertexRep> commonVertices = new LinkedHashSet<>();
		for (PathwayVertexRep vTarget : targetSegments) {
			for (PathwayVertexRep vSource : sourceSegments) {
				if (vSource == vTarget) {
					commonVertices.add(vSource);
					break;
				}
			}
		}

		return commonVertices;
	}

}
