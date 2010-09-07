package org.caleydo.rcp;

import java.util.HashMap;
import java.util.Map;

import org.caleydo.core.manager.GeneralManager;

public class AbstractSourceProvider
	extends org.eclipse.ui.AbstractSourceProvider {
	
    public final static String RELEASE_STATE = "org.caleydo.rcp.isReleaseState"; 
	
    private final static String RELEASE_VERSION = "releaseVersion";
    private final static String FULL_VERSION = "fullVersion"; 
    
    boolean isReleaseVersion = GeneralManager.RELEASE_MODE;;
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map getCurrentState() {
        Map<String, String> currentState = new HashMap<String, String>(1);
        String currentStateTmp =  isReleaseVersion?FULL_VERSION:RELEASE_VERSION;
        currentState.put(RELEASE_STATE, currentStateTmp);
        return currentState; 
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {RELEASE_STATE};
	}

}
