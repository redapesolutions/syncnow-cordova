package com.redapesolutions.syncnow;

public enum SyncNowActions {
	INIT,
	RECORD,
	STOP_RECORD;
	
	
	/**
	 * @return actions
	 */
	public static String[] names() {
		SyncNowActions[] actions = values();
	    String[] names = new String[actions.length];

	    for (int i = 0; i < actions.length; i++) {
	        names[i] = actions[i].name();
	    }

	    return names;
	}
}
