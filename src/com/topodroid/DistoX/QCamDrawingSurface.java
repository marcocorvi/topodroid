/** @file QCamDrawingSurface.java
 *
 * @author marco corvi
 * @date jan. 2017
 *
 * @brief TopoDroid quick cam drawing surface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.IOException;
import java.util.List;

import android.content.Context;

import android.os.Handler;
import android.os.Message;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.ImageFormat;

import android.hardware.Camera;
// import android.hardware.Camera.CameraInfo; // UNUSED
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.SensorManager;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
// import android.view.OrientationEventListener;

import android.util.AttributeSet;
import android.util.Log;

/** this is the camera preview class
 *  It access the camera via the QCamPreview
 *  @note this class must be public
 */
public class QCamDrawingSurface extends SurfaceView
                                implements SurfaceHolder.Callback 
{
  private static final String TAG = "DistoX";
  Context mContext;
  QCamCompass mQCam;

  private Boolean mDoDraw;
  SurfaceHolder mHolder;
  int mWidth;            // canvas width
  int mHeight;           // canvas height

  Camera mCamera = null;
  Camera.Parameters mParameters;
  Camera.PreviewCallback mPreviewCallback;
  Camera.PictureCallback mRaw;
  Camera.PictureCallback mJpeg;
  Camera.ShutterCallback mShutter;
  byte[] mJpegData;

  // MyOrientationListener mOrientationListener = null;

  // class MyOrientationListener extends OrientationEventListener
  // {
  //   Camera.Parameters mParams;

  //   MyOrientationListener( Context ctx, Camera.Parameters params ) 
  //   {
  //     super(ctx, SensorManager.SENSOR_DELAY_NORMAL );
  //     mParams = params;
  //   }

  //   // Called when the orientation of the device has changed.
  //   // orientation parameter is in degrees, ranging from 0 to 359.
  //   // orientation is:
  //   //   0 degrees when the device is oriented in its natural position,
  //   //   90 degrees when its left side is at the top,
  //   //  180 degrees when it is upside down,
  //   //  270 degrees when its right side is to the top.
  //   //  ORIENTATION_UNKNOWN is returned when the device is close to flat
  //   //  and the orientation cannot be determined.
  //   //
  //   public void onOrientationChanged(int orientation)
  //   {
  //     Log.v("DistoX", "on Orientation Change " + orientation );
  //     if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
  //     CameraInfo info = new CameraInfo();
  //     Camera.getCameraInfo( 0, info );  // cameraId = 0
  //     orientation = ((orientation + 45) / 90) * 90;
  //     int rotation = 0;
  //     // if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
  //     //   rotation = (info.orientation - orientation + 360) % 360;
  //     // } else {  // back-facing camera
  //       rotation = (info.orientation + orientation) % 360;
  //     // }
  //     mParams.setRotation( orientation );
  //   }
  // } 

  public QCamDrawingSurface(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    mContext = context;
    // Log.v( TAG, "QCamDrawingSurface cstr" );
    mHolder = getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // required on android < 3.0

    mQCam = null;
    mCamera   = null;
    mJpegData = null;

    createCallbacks();
  }


  public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) 
  {
    // Log.v( TAG, "surface changed " );
    if ( mHolder.getSurface() == null) { // preview surface does not exist
      return;
    }
    stop();
    // set preview size and make any resize, rotate or reformatting changes here
    start();
  }

  public void surfaceCreated(SurfaceHolder holder) 
  {
    // Log.v( TAG, "surface created " );
    try {
      mCamera = Camera.open();
      mCamera.setPreviewDisplay( holder );
      // mCamera.startPreview();
    } catch (Exception e) {
      TDLog.Error( "Error setting camera preview: " + e.getMessage());
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) // release the camera preview in QCamCompass
  {
    // Log.v( TAG, "surface destroyed " );
    close();
  }

  boolean takePicture( int orientation )
  {
    boolean ret = false;
    if ( mCamera != null ) {
      mCamera.getParameters().setRotation( orientation );
      mCamera.takePicture( mShutter, mRaw, null, mJpeg);
      ret = true;
    }
    mQCam.enableButtons( true );
    return ret;
  }

  void close()
  {
    // if ( mOrientationListener != null ) mOrientationListener.disable( );
    if ( mCamera != null ) {
      // Log.v("DistoX", "close qcam" );
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
  }

  boolean open()
  {
    // Log.v("DistoX", "open qcam" );
    close();
    try {
      mCamera = Camera.open();
      mParameters = mCamera.getParameters();
      mParameters.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );
      mParameters.setSceneMode( Camera.Parameters.SCENE_MODE_AUTO );
      mParameters.setFlashMode( Camera.Parameters.FLASH_MODE_AUTO );
      List< Integer > formats = mParameters.getSupportedPreviewFormats();
      for ( Integer fmt : formats ) {
        if ( fmt.intValue() == ImageFormat.JPEG ) {
          // Log.v("DistoX", "Set preview format JPEG" );
          mParameters.setPreviewFormat( ImageFormat.JPEG );
        }
        // Log.v("DistoX", "QCamPreview formats " + fmt );
      }
      mCamera.setParameters( mParameters );
      mCamera.setPreviewCallback( mPreviewCallback );
      int format = mParameters.getPreviewFormat();
      // Log.v( "DistoX", "QCamPreview Format " + format );
      // mOrientationListener = new MyOrientationListener( mContext, mParameters );

      Camera.Size size = mParameters.getPreviewSize();
      // mWidth  = size.width;
      // mHeight = size.height;
      // Log.v( "DistoX", "QCamPreview size " + size.width + " " + size.height );
      try {
        mCamera.setDisplayOrientation( 90 );
        mCamera.setPreviewDisplay( mHolder );
      } catch ( IOException e ) {
        TDLog.Error( "cannot set preview display" );
      }
      // if ( mOrientationListener != null ) mOrientationListener.enable( );
      mCamera.startPreview();
      return true;
    } catch ( RuntimeException e ) { // fail to connect to canera service
      if ( mCamera != null ) mCamera.release();
      mCamera = null;
      TDLog.Error( e.getMessage() );
    }
    return false;
  }

  void start()
  {
    if ( mCamera != null ) {
      try { // start preview with new settings
        mCamera.setDisplayOrientation( 90 );
        mCamera.setPreviewDisplay(mHolder);
        // if ( mOrientationListener != null ) mOrientationListener.enable( );
        mCamera.startPreview();
      } catch ( Exception e ) {
        TDLog.Error( "Error starting camera preview: " + e.getMessage());
      }
    }
  }

  private void stop()
  {
    // if ( mOrientationListener != null ) mOrientationListener.disable( );
    if ( mCamera != null ) {
      try { // stop preview before making changes
        mCamera.stopPreview();
      } catch ( Exception e ) {
        // ignore: tried to stop a non-existent preview
      }
    }
  }

  private void createCallbacks()
  {
    mShutter = new ShutterCallback() {
      public void onShutter( ) {
        // Log.v( "DistoX", "Shutter callback " );
      }
    };
    mRaw = new PictureCallback() {
      public void onPictureTaken( byte[] data, Camera c ) {
        // Log.v( "DistoX", "Picture Raw callback data " + ((data==null)? "null" : data.length) );
      }
    };
    mJpeg = new PictureCallback() {
      public void onPictureTaken( byte[] data, Camera c ) { 
        // Log.v( "DistoX", "Picture JPEG callback data " + ((data==null)? "null" : data.length) );
        mJpegData = data;
      }
    };
    mPreviewCallback = new PreviewCallback() { // called every time startPreview
        public void onPreviewFrame(byte[] data, Camera arg1) {
          // Log.v("DistoX", "on preview frame");
        }
    };
  }

}
