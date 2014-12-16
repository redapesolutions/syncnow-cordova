package com.redapesolutions.syncnow;

import org.apache.cordova.CallbackContext;

import android.content.Context;
import android.util.Log;

import com.civolution.syncnow.AwmSyncDetector;
import com.civolution.syncnow.AwmSyncDetectorFactory;
import com.civolution.syncnow.AwmSyncDetectorListener;
import com.civolution.syncnow.AwmSyncDetector.SdkDetectorType;

public class SyncNowPlugin implements AwmSyncDetectorListener {
	private CallbackContext _callback; // global callback
	private AwmSyncDetector mDetector = null;
	private int _mNumIdentifierBits, _mNumTimeStampBits;
	private boolean _mTimeStampLoop;
	private String _license;

	private static String LOG_TAG = SyncNowPlugin.class.getName();

	private Thread mAudioThread = null;

	public SyncNowPlugin(String license, int mNumIdentifierBits,
			int mNumTimeStampBits, boolean mTimeStampLoop,
			CallbackContext callback) {
		_callback = callback;
		_license = license;
		_mNumIdentifierBits = mNumIdentifierBits;
		_mNumTimeStampBits = mNumTimeStampBits;
		_mTimeStampLoop = mTimeStampLoop;
	}

	public SyncNowPlugin(String license, int mNumIdentifierBits,
			int mNumTimeStampBits, boolean mTimeStampLoop) {
		_license = license;
		_mNumIdentifierBits = mNumIdentifierBits;
		_mNumTimeStampBits = mNumTimeStampBits;
		_mTimeStampLoop = mTimeStampLoop;
	}

	public CallbackContext getCallback() {
		return _callback;
	}

	public void setCallback(CallbackContext _callback) {
		this._callback = _callback;
	}

	public Thread getmAudioThread() {
		return mAudioThread;
	}

	public synchronized boolean startDetector(Context context) {
		boolean status = true;
		SdkDetectorType licenseRetCode = SdkDetectorType.DETECTOR_TYPE_ERROR;

		mDetector = AwmSyncDetectorFactory.createAwmSyncDetector(context);

		if (null == mDetector) {
			_callback.error("SDK error. SDK can not be instantiated.");
			return false;
		}

		Log.i(LOG_TAG, mDetector.getVersion());

		mDetector.setListener(this);

		licenseRetCode = mDetector.setLicense(_license);

		status = configureDetectionOptions(licenseRetCode);
		
		if (!status)
			return status;

		// start the worker thread to capture the audio input
		mAudioThread = new AudioCapture(mDetector, this);
		mAudioThread.setPriority(Thread.MAX_PRIORITY);
		mAudioThread.start();

		return status;
	}

	/**
	 * Setting the license is mandatory to enable the detection. This is where
	 * the detection technology setup (SyncNow 2G, SyncNow 3G or SyncNow 3G
	 * SNAP) is performed. The license contains the watermarking key that was
	 * used to watermark the content.
	 * 
	 * @param licenseRetCode
	 * @return
	 */
	private boolean configureDetectionOptions(SdkDetectorType licenseRetCode) {
		boolean result = false;
		switch (licenseRetCode) {
		case DETECTOR_TYPE_SNAP:
			// provide the SNAP detection parameters
			AwmSyncDetector.SnapDetectorParameters params = new AwmSyncDetector.SnapDetectorParameters();
			params.mMode = AwmSyncDetector.MODE_LIVE;
			params.mTimeStampLoop = _mTimeStampLoop;

			if (!mDetector.setSnapDetectorParameters(params)) {
				Log.e(LOG_TAG,
						"SNAP detection parameters error: please check your settings");
				break;
			}
			result = true;
			break;
		case DETECTOR_TYPE_SYNCNOW:
			// provide the SyncNow detection parameters
			AwmSyncDetector.DetectorParameters syncParams = new AwmSyncDetector.DetectorParameters();
			syncParams.mNumIdentifierBits = _mNumIdentifierBits;
			syncParams.mNumTimeStampBits = _mNumTimeStampBits;
			syncParams.mMode = AwmSyncDetector.MODE_LIVE;
			syncParams.mTimeStampLoop = _mTimeStampLoop;

			if (!mDetector.setDetectorParameters(syncParams)) {
				Log.e(LOG_TAG,
						"Detection parameters error: please check your settings");
				break;
			}
			result = true;
			break;
		default:
			Log.e(LOG_TAG, licenseRetCode.name());
			Log.e(LOG_TAG, "Error on detection options");
			break;
		}
		return result;
	}

	@Override
	public void onAlarm(AlarmEvent arg0) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onAlarm=" + arg0.message);

	}

	@Override
	public void onDebug(String arg0) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onDebug=" + arg0);

	}

	@Override
	public void onPayload(PayloadEvent arg0) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onPayload=" + arg0.toString());

	}

}
