package com.ballofknives.criminalintent;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CrimeCameraFragment extends Fragment {
    private static final String TAG = "CrimeCameraFragment";
    public static final String EXTRA_PHOTO_FILENAME = "com.ballofknives.criminalintent.photo_filename";
    private View mProgressContainer;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback(){
      public void onPictureTaken(byte[] data, Camera camera){
          String filename = UUID.randomUUID().toString() + ".jpg";
          FileOutputStream os = null;
          boolean success = true;
          try{
              os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
              os.write(data);
          } catch (Exception e){
              Log.e(TAG, "Error writing to file", e);
              success = false;
          } finally {
              try{
                  if(os != null ){
                      os.close();
                  }
              }catch(Exception e){
                  Log.e(TAG, "Error closing file", e);
                  success = false;
              }
          }
          if(success){
              //Log.i(TAG, "JPEG saved at " + filename);
              Intent i = new Intent();
              i.putExtra(EXTRA_PHOTO_FILENAME, filename);
              getActivity().setResult(Activity.RESULT_OK, i);
          }
          else {
              getActivity().setResult(Activity.RESULT_CANCELED);
          }
          getActivity().finish();
      }
    };


    private Camera mCamera;
    private SurfaceView mSurfaceView;

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_camera, container, false);

        mProgressContainer = v.findViewById(R.id.crime_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        Button takePictureButton = (Button)v.findViewById(R.id.crime_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getActivity().finish();
                if(mCamera != null){
                    mCamera.takePicture(mShutterCallback, null, mJpegCallback);
                }
            }
        });
        mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try{
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(surfaceHolder);
                    }
                } catch (IOException e){
                    Log.e(TAG, "Error setting up preview display", e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                if(mCamera == null) return;

                Camera.Parameters parameters = mCamera.getParameters();
                //Camera.Size s = null;
                Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), i1, i2);
                parameters.setPreviewSize(s.width, s.height);
                s = getBestSupportedSize(parameters.getSupportedPictureSizes(), i1, i2);
                parameters.setPictureSize(s.width, s.height);
                mCamera.setParameters(parameters);
                try{
                    mCamera.startPreview();
                } catch (Exception e){
                    Log.e(TAG, "Cannot start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
        });
        return v;
    }

    @TargetApi(19)
    @Override
    public void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height){
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for ( Camera.Size s : sizes){
            int area = s.width * s.height;
            if( area > largestArea){
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }
}
