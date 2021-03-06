package com.wind.smartcover;

import java.util.List;

import com.wind.smartcover.R;
import com.wind.smartcover.Util.Wind;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.widget.Toast;

public class LightController {
	private static final String TAG = "LightController";

	private Context mContext;
	private Camera mCamera = null;
	private boolean isLightOpen = false;
	private boolean mOpenCamering = false;
	private static LightController mLightController = null;
	
	private CameraManager mCameraManager;
	private FlashlightController mFlashlightController;
    private TrochCallback mTrochCallback = new TrochCallback();


    class TrochCallback extends CameraManager.TorchCallback{
        @Override
        public void onTorchModeUnavailable(String cameraId) {
            super.onTorchModeUnavailable(cameraId);
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
        }
    }

	private LightController(Context context) {
		mContext = context;
		if (null == mCameraManager)
			mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
		if(mFlashlightController == null){
			mFlashlightController = new FlashlightController(context);
		}
	}

	public static LightController getInstance(Context context) {
		Wind.Log(TAG, "getInstance mLightController=" + mLightController);
		if (null == mLightController)
			mLightController = new LightController(context);
		
		return mLightController;
	}

	public boolean isOpenCamera() {
		if(Wind.IS_SYS_M){
			return mOpenCamering;
		}
		
		return (mCamera != null);
	}

	public boolean isLightOpen() {
		return isLightOpen;
	}

	public void openCamera() {
		if(Wind.IS_SYS_M){
			mOpenCamering = true;
	        mCameraManager.registerTorchCallback(mTrochCallback,new Handler());
			return;
		}
		
		Wind.Log(TAG, "openCamera isLightOpen=" + isLightOpen + " mCamera="
				+ mCamera + " mOpenCamering=" + mOpenCamering);
		if (isLightOpen || (mCamera != null) || mOpenCamering)
			return;
		mOpenCamering = true;
		
		try {
			mCamera = Camera.open();
		} catch (RuntimeException e) {
			Wind.Log(TAG, "openCamera RuntimeException=" + e.toString());
			mCamera = null;
		}
		mOpenCamering = false;
		Wind.Log(TAG, "openCamera isLightOpen=" + isLightOpen + " mCamera="
				+ mCamera + " mOpenCamering=" + mOpenCamering);
	}

	public void releaseCamera() {
		if(Wind.IS_SYS_M){
			mOpenCamering = false;
	        mCameraManager.unregisterTorchCallback(mTrochCallback);
			return;
		}
		Wind.Log(TAG, "releaseCamera mCamera=" + mCamera);
		if (null != mCamera) {
			try {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			} catch (RuntimeException e) {
				Wind.Log(TAG, "openCamera RuntimeException=" + e.toString());
				mCamera = null;
			}
		}
		Wind.Log(TAG, "releaseCamera mCamera=" + mCamera);
	}
	
	
	public void openMLight() {
		if(isLightOpen == true)
			return;
		
		isLightOpen = true;
		resetLightState(true);
		Wind.Log(TAG, "openMLight isLightOpen="+isLightOpen);
	}

	public void resetLightState(boolean state) {
		mFlashlightController.setFlashlight(state);
		Wind.Log(TAG, "resetLightState state=" + state);
		String [] list;
		if (null == mCameraManager)
			mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
		try {
			list = mCameraManager.getCameraIdList();
			android.util.Log.i("wind/", "" + list.length);
			if (list != null && list.length >= 1) {
				mCameraManager.setTorchMode(list[0], state);
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
			return;
		}
	}

	public void openLight() {
		if(Wind.IS_SYS_M){
			openMLight();
			return;
		}
		Wind.Log(TAG, "openLight mCamera=" + mCamera);
		
		openCamera();
		if (mCamera != null) {
			Parameters params = mCamera.getParameters();
			List<String> list = params.getSupportedFlashModes();
			if (list.contains(Parameters.FLASH_MODE_TORCH)) {
				params.setFlashMode(Parameters.FLASH_MODE_TORCH);
			} else {
				Toast.makeText(mContext,
						mContext.getString(R.string.device_not_support_camera),
						Toast.LENGTH_SHORT).show();
			}
			mCamera.setParameters(params);
			mCamera.startPreview();
			isLightOpen = true;
		}
		Wind.Log(TAG, "openLight mCamera=" + mCamera);
	}


	public void closeMLight() {
		if(isLightOpen == false)
			return;
		isLightOpen = false;
		resetLightState(false);
		Wind.Log(TAG, "closeMLight isLightOpen="+isLightOpen);
	}
	
	public void closeLight() {
		if(Wind.IS_SYS_M){
			closeMLight();
			return;
		}
		
		Wind.Log(TAG, "closeLight isLightOpen=" + isLightOpen + " mCamera="
				+ mCamera);
		if (isLightOpen && (mCamera != null)) {
			Parameters closepParameters = mCamera.getParameters();
			closepParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(closepParameters);
			isLightOpen = false;
		}
		Wind.Log(TAG, "closeLight isLightOpen=" + isLightOpen + " mCamera="
				+ mCamera);
	}

	public void changeStatus() {
		if (isLightOpen)
			closeLight();
		else
			openLight();
	}
}
