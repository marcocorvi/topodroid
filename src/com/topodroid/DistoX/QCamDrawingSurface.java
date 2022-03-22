/* @file QCamDrawingSurface.java
 *
 * @author marco corvi
 * @date jan. 2017
 *
 * @brief TopoDroid quick cam drawing surface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;

import java.io.IOException;
import java.util.List;

import android.content.Context;

import android.graphics.ImageFormat;

// API-21 use android.hardware.camera2 clases
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.util.AttributeSet;

/** this is the camera preview class
 *  It access the camera via the QCamPreview
 *  note this class must be public
 */
public class QCamDrawingSurface extends SurfaceView
                                implements SurfaceHolder.Callback 
{
  QCamCompass mQCam;

  private Boolean mDoDraw;
  private SurfaceHolder mHolder;
  int mWidth;            // canvas width
  int mHeight;           // canvas height

  private Camera mCamera = null;
  private Camera.PreviewCallback mPreviewCallback;
  private Camera.PictureCallback mRaw;
  private Camera.PictureCallback mJpeg;
  private Camera.ShutterCallback mShutter;
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
  //     TDLog.v("QCAM on Orientation Change " + orientation );
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

  /** cstr
   * @param context  context
   * @param attrs    attributes
   */
  public QCamDrawingSurface(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    // mContext = context;
    // TDLog.v( "QCam Surface cstr" );
    mHolder = getHolder();
    mHolder.addCallback(this);
    // mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // required on android <= API-11

    mQCam = null;
    mCamera   = null;
    mJpegData = null;

    createCallbacks();
  }


  /** called when the surface is changed
   * @param holder    surface holder
   * @param format    ... (unused)
   * @param width     width (unused)
   * @param height    height (unused)
   */
  public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) 
  {
    // TDLog.v( "surface changed " );
    if ( mHolder.getSurface() == null) { // preview surface does not exist
      return;
    }
    stop();
    // set preview size and make any resize, rotate or reformatting changes here
    start();
  }

  /** called when the surface is created
   * @param holder    surface holder
   */
  public void surfaceCreated(SurfaceHolder holder) 
  {
    // TDLog.v( "QCAM surface created " );
    open();
    // try {
    //   mCamera = Camera.open();
    //   mCamera.setPreviewDisplay( holder );
    //   // mCamera.startPreview();
    // } catch (Exception e) {
    //   TDLog.Error( "Error setting camera preview: " + e.getMessage());
    // }
  }

  /** called when the surface is destroyed
   * @param holder    surface holder
   */
  public void surfaceDestroyed(SurfaceHolder holder) // release the camera preview in QCamCompass
  {
    // TDLog.v( "surface destroyed " );
    close();
  }

  /** react to a measure of the view and its content - invoked by measure( int, in )
   * @param measuredWidth  measured width
   * @param measuredHeight measured height
   */
  @Override
  public void onMeasure( int measuredWidth, int measuredHeight )
  {
    int w = getSuggestedMinimumWidth();
    int h = getSuggestedMinimumHeight();
    // TDLog.v( "QCAM surface on measure " + measuredWidth + " " + measuredHeight + " " + w + " " + h );
    if ( w == 0 || h == 0 ) { 
      super.onMeasure( measuredWidth, measuredHeight );
    } else {
      // exchange w-h because the orientation is 90
      setMeasuredDimension( h, w );
    }
  }

  /** take a pictture
   * @param orientation   display orientation ???
   * @return true on success
   */
  boolean takePicture( int orientation )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "QCAM surface take picture. Orientation " + orientation );
    boolean ret = false;
    if ( mCamera != null ) {
      try {
        mCamera.getParameters().setRotation( orientation );
        mCamera.takePicture( mShutter, mRaw, null, mJpeg);
        ret = true;
      } catch ( RuntimeException e ) {
        TDLog.Error("camera runtime exception " + e.getMessage() );
      }
    }
    // mQCam.enableButtons( true );
    mQCam.enableButtons( ret );
    return ret;
  }

  /** get the maximum zoom value
   */
  int getMaxZoom()
  {
    return (mCamera != null )? mCamera.getParameters().getMaxZoom() : 100;
  }

  /** zoom in/out
   * @param delta_zoom   zoom change
   */
  void zoom( int delta_zoom )
  {
    if ( mCamera != null ) {
      Camera.Parameters params = mCamera.getParameters();
      int max = params.getMaxZoom();
      int zoom = params.getZoom() + delta_zoom;
      if ( zoom > 0 && zoom < max ) {
        // TDLog.v("DistoX-QCAM", "set zoom " + zoom + "/" + max );
        params.setZoom( zoom );
        mCamera.setParameters( params );
      }
    }
  }

  /** close the camera
   */
  void close()
  {
    TDLog.Log( TDLog.LOG_PHOTO, "QCAM surface close");
    // if ( mOrientationListener != null ) mOrientationListener.disable( );
    if ( mCamera != null ) {
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
  }

  /** open the camera
   * @return true on success
   */
  boolean open()
  {
    TDLog.Log( TDLog.LOG_PHOTO, "QCAM surface open");
    close();
    try {
      mCamera = Camera.open();
      Camera.Parameters params = mCamera.getParameters();
      params.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );
      params.setSceneMode( Camera.Parameters.SCENE_MODE_AUTO );
      params.setFlashMode( Camera.Parameters.FLASH_MODE_AUTO );
      List< Integer > formats = params.getSupportedPreviewFormats();
      for ( Integer fmt : formats ) {
        if ( fmt.intValue() == ImageFormat.JPEG ) {
          // TDLog.v( "Set preview format JPEG" );
          params.setPreviewFormat( ImageFormat.JPEG );
        }
        // TDLog.v( "QCamPreview formats " + fmt );
      }
      mCamera.setParameters( params );
      mCamera.setPreviewCallback( mPreviewCallback );
      int format = params.getPreviewFormat();
      // TDLog.v( "QCamPreview Format " + format );
      // mOrientationListener = new MyOrientationListener( mContext, params );

      Camera.Size size = params.getPreviewSize();
      // mWidth  = size.width;
      // mHeight = size.height;
      // TDLog.v( "QCam preview size " + size.width + " " + size.height );
      setMinimumWidth( size.width );
      setMinimumHeight( size.height );
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

  /** start the preview
   * @note display orientation is 90
   */
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

  /** stop the preview
   */
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

  /** create the callbacks, mostly empty functions. 
   * @note onPictureTaken store the JPEG data
   */
  private void createCallbacks()
  {
    mShutter = new ShutterCallback() {
      public void onShutter( ) {
        // TDLog.v( "Shutter callback " );
      }
    };
    mRaw = new PictureCallback() {
      public void onPictureTaken( byte[] data, Camera c ) {
        // TDLog.v( "Picture Raw callback data " + ((data==null)? "null" : data.length) );
      }
    };
    mJpeg = new PictureCallback() {
      public void onPictureTaken( byte[] data, Camera c ) { 
        // TDLog.v( "Picture JPEG callback data " + ((data==null)? "null" : data.length) );
        mJpegData = data;
      }
    };
    mPreviewCallback = new PreviewCallback() { // called every time startPreview
        public void onPreviewFrame(byte[] data, Camera c ) {
          // TDLog.v("on preview frame");
        }
    };
  }

}
