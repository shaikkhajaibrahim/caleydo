package org.caleydo.core.data.virtualarray.group;

import org.caleydo.core.data.virtualarray.StorageVirtualArray;
import org.caleydo.core.data.virtualarray.delta.StorageVADelta;

public class StorageGroupList
	extends GroupList<StorageGroupList, StorageVirtualArray, StorageVADelta> {

	@Override
	public StorageGroupList createInstance() {
		return new StorageGroupList();
	}

}
