package com.redapesolutions.syncnow;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.civolution.syncnow.AwmSyncDetector;
import com.civolution.syncnow.AwmSyncDetectorListener;

public class AudioCapture extends Thread {
	private static String LOG_TAG = AudioCapture.class.getName();

	/*
	 * Sample rate is hard coded to 44100Hz, since it is the Android audio
	 * requirement for devices to support. Official Android states: "44100Hz is
	 * currently the only rate that is guaranteed to work on all devices" Ref:
	 * http://developer.android.com/reference/android/media/AudioRecord.html
	 */
	int mSampleRate = 44100; // sample rate in Hz
	byte[] mBuffer = null; // PCM input audio buffer
	int mBufferSize = 0; // PCM input audio buffer size

	static final int BUFFER_SIZE_MIN = 1024;

	private AwmSyncDetector mDetector = null; // SDK instance
	private AwmSyncDetectorListener mListener = null; // SDL listener instance

	// audio mic configuration: MONO(1 channel) PCM 16bits
	public int mChannels = AudioFormat.CHANNEL_IN_MONO;
	private int mNumChannels = (AudioFormat.CHANNEL_IN_MONO == mChannels) ? 1
			: 2;
	public int mEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private int mNumBitsPerChannel = (AudioFormat.ENCODING_PCM_16BIT == mEncoding) ? 16
			: 8;

	// record input fields
	private AudioRecord mAudio = null; // audio recording Android class API

	public AudioCapture(AwmSyncDetector detector,
			AwmSyncDetectorListener listener) {
		mDetector = detector;
		mListener = listener;
	}

	private int init() throws Exception {
		mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannels,
				mEncoding);
		if (mBufferSize <= 0) {
			Log.e(LOG_TAG, "Error in getting audio");
			return -1;
		}

		// check buffer size
		if (mBufferSize <= BUFFER_SIZE_MIN) {
			mBufferSize = BUFFER_SIZE_MIN * mChannels;
		}

		// configure the SDK with the input audio signal parameters
		boolean success = mDetector.setAudioParameters(mSampleRate,
				mNumBitsPerChannel, mNumChannels, mBufferSize);
		if (!success) {
			Log.e(LOG_TAG, "error: audio: format");
			return -2;
		}

		// audio recording init
		mBuffer = new byte[mBufferSize];
		mAudio = new AudioRecord(AudioSource.MIC, mSampleRate, mChannels,
				mEncoding, mBufferSize);
		int status = mAudio.getState();

		if (status != AudioRecord.STATE_INITIALIZED) {
			throw new Exception("error: audio: detector: fail to init "
					+ status);
		}

		if (!mDetector.initialize()) {
			throw new Exception("error: audio: detector: fail to init "
					+ status);
		}

		// start the audio recording now!
		mAudio.startRecording();
		return 0;
	}

	@Override
	public void run() {
		int status = 0;

		try {
			while (!isInterrupted()) {
				if (mAudio == null) {
					status = init();
					if (0 != status) {
						throw new Exception("error: audio: init: " + status);
					}
				} else {
					if (mBufferSize > 0) {
						status = mAudio.read(mBuffer, 0, mBufferSize);
						if (status < 0) {
							Log.e(LOG_TAG, "error: audio: read: " + status);
						}
					}

					if (null != mDetector) {
						if (true != mDetector.pushAudioBuffer(mBuffer,
								mBufferSize))
							throw new Exception(
									"error: pushAudioBuffer()=false");
					}
				}
			}

		} catch (Exception e) {
			Log.e(LOG_TAG, "run(): Exceptions audio loop - msg=" + e.getMessage());
		} finally {
			if (mAudio != null) {
				status = mAudio.getRecordingState();
				Log.i(LOG_TAG, "status=" + status);
				if (status == AudioRecord.RECORDSTATE_RECORDING) {
					mAudio.stop();
				}

				status = mAudio.getState();
				Log.i(LOG_TAG, "status=" + status);
				if (status == AudioRecord.STATE_INITIALIZED) {
					mAudio.release();
				}

				status = mAudio.getState();
				Log.i(LOG_TAG, "status=" + status);
				mAudio = null;
			}
		}
	}

}
