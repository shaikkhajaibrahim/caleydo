/**
 * 
 */
package org.caleydo.core.data.collection.set.selection;

import java.util.ArrayList;

import org.caleydo.core.data.collection.ISet;

/**
 * Special ISet for selection. 
 * Used to exchange selection data between ViewRep's.
 * Interface provides writing and reading access
 * to the selection data.
 * 
 * @see cerverus.view.gui.AViewRep
 * @see org.caleydo.core.manager.event.mediator.IMediatorReceiver
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 *
 */
public interface ISetSelection 
extends ISet {

	public void setSelectionIdArray(ArrayList<Integer> iAlSelectionId);
	
	public void setGroupArray(ArrayList<Integer> iAlSelectionGroup);

	public void setOptionalDataArray(ArrayList<Integer> iAlSelectionOptionalData);

	public void setAllSelectionDataArrays(ArrayList<Integer> iAlSelectionId, 
			ArrayList<Integer> iAlSelectionGroup, 
			ArrayList<Integer> iAlSelectionOptionalData);
	
	public ArrayList<Integer> getSelectionIdArray();
	
	public ArrayList<Integer> getGroupArray();
	
	public ArrayList<Integer> getOptionalDataArray();
}
