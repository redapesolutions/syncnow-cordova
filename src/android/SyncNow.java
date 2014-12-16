package com.redapesolutions.syncnow;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.civolution.syncnow.AwmSyncDetectorListener;
import com.redapesolutions.syncnow.SyncNowActions;

/**
 * This class echoes a string called from JavaScript.
 */
public class SyncNow extends CordovaPlugin {
	private String mLicenseKey = "license",
			mNumIdentifierBitsKey = "identifierBits",
			mNumTimeStampBitsKey = "mNumTimeStampBits",
			mTimeStampLoopKey = "mTimeStampLoop";
	
	private static SyncNowPlugin syncNow;

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		int resId = cordova
				.getActivity()
				.getResources()
				.getIdentifier(mLicenseKey, "string",
						cordova.getActivity().getPackageName());
		int mNumIdentifierBitsId = cordova
				.getActivity()
				.getResources()
				.getIdentifier(mNumIdentifierBitsKey, "string",
						cordova.getActivity().getPackageName());
		int mNumTimeStampBitsId = cordova
				.getActivity()
				.getResources()
				.getIdentifier(mNumTimeStampBitsKey, "string",
						cordova.getActivity().getPackageName());
		int mTimeStampLoopId = cordova
				.getActivity()
				.getResources()
				.getIdentifier(mTimeStampLoopKey, "string",
						cordova.getActivity().getPackageName());

		String license = cordova.getActivity().getString(resId);
		int mNumIdentifierBits = Integer.getInteger(cordova.getActivity().getString(
				mNumIdentifierBitsId), 4);
		int mNumTimeStampBits = Integer.getInteger(cordova.getActivity().getString(mNumTimeStampBitsId), 4);
		boolean mTimeStampLoop = Boolean.getBoolean(cordova.getActivity().getString(mTimeStampLoopId));
		syncNow = new SyncNowPlugin(license, mNumIdentifierBits, mNumTimeStampBits, mTimeStampLoop);
		super.initialize(cordova, webView);
	}

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		if (action.equals(SyncNowActions.RECORD.toString())) {
			syncNow.startDetector(this.cordova.getActivity());
			return true;
		} else if (action.equals(SyncNowActions.INIT.toString())) {
			syncNow.setCallback(callbackContext);
			callbackContext.success(new JSONArray(SyncNowActions.names()));
			return true;
		}
		return false;
	}
}
