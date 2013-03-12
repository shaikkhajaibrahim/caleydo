/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.core.view.opengl.canvas;


/**
 * @author Samuel Gratzl
 *
 */
public interface IGLKeyListener {

	/**
	 * @param e
	 */
	void keyPressed(IKeyEvent e);

	/**
	 * @param e
	 */
	void keyReleased(IKeyEvent e);


	public interface IKeyEvent {
		boolean isKey(char c);

		boolean isKey(ESpecialKey c);

		int getKeyCode();

		/**
		 * @return whether the shift key was already down or become down
		 */
		boolean isShiftDown();

		/**
		 * @return whether the control key was already down or become down
		 */
		boolean isControlDown();
		
		boolean isAltDown();
		
		boolean isKeyDown(char c);
	}

	public enum ESpecialKey {
		ALT, CONTROL, SHIFT, LEFT, RIGHT, UP, DOWN 
	}
}
