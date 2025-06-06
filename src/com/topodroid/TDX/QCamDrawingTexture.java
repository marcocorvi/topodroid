/* @file QCamDrawingTexture.java
 *
 * @author marco corvi
 * @date apr. 2022
 *
 * @brief TopoDroid quick cam drawing texture - using Camera2 API (new camera)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDThread;
// import com.topodroid.ui.ExifInfo;
// import com.topodroid.prefs.TDSetting;

// import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// import android.Manifest;
// import android.annotation.TargetApi;
// import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
// import android.content.pm.PackageManager;
// import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;

// API-21 use android.hardware.camera2 classes
// import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.params.StreamConfigurationMap;

import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.Surface;
import android.view.OrientationEventListener;
import android.view.Display;
import android.view.View;

import android.media.Image;
import android.media.ImageReader;

import android.util.AttributeSet;
import android.util.Size;

/** this is the camera preview class
 *  It access the camera via the QCamPreview
 *  @note this class must be public
 *
 *  @note QCamDrawingTexture is used only with API at least 21
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class QCamDrawingTexture extends TextureView {
  private static final int MAX_PREVIEW_WIDTH = 1920; // expect landscape image
  private static final int MAX_PREVIEW_HEIGHT = 1080;

  static final int MAX_CAPTURES = 5;

  QCamCompass mQCam;
  private Context mContext;

  private Boolean mDoDraw;

  private String mCameraId;
  private CameraDevice mCamera = null;
  private CameraManager mManager = null;
  private CameraCaptureSession mCaptureSession;
  private Size mPreviewSize;
  private HandlerThread mBackgroundThread;
  private Handler mBackgroundHandler; // FIXME the background handler does not do anything 
  private ImageReader mImageReader = null;
  private CaptureRequest mPreviewRequest;
  private CaptureRequest.Builder mPreviewRequestBuilder;
  private Semaphore mLock = new Semaphore(1);

  private Display mDisplay = null;
  private byte[] mJpegData = null;

  private OrientationEventListener mOrientationListener = null;
  private int mOrientation = 0; // sensor orientation
  private boolean mHasFlash = false;
  private int mNrCaptures = 0;
  private float mZoomLevel = 3;

  // ------------------------------------------------------------------------
  /** surface-texture listener
   */
  private final SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int w, int h) {
      // TDLog.v("QCAM2 surface listener on texture available");
      if (Build.VERSION.SDK_INT < 21) return;
      if ( ! openCamera(w, h) ) {
        TDLog.e("QCAM2 failed open camera " + w + "x" + h );
      }
      // mCamera.setPreviewTexture( texture );
      // mCamera.startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int w, int h)
    {
      // TDLog.v("QCAM2 surface listener on size changed");
      if (Build.VERSION.SDK_INT < 21) return;
      configureTransform(w, h);
    }

    /** repeatedly invoked every time there is a new preview frame
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
      // TDLog.v("QCAM2 surface listener on texture destroyed");
      // mCamera.stopPreview();
      return true;
    }
  };

  // ------------------------------------------------------------------------

  private final ImageReader.OnImageAvailableListener mImageAvailable = new ImageReader.OnImageAvailableListener() {
    @Override
    public void onImageAvailable(ImageReader reader) 
    {
      if (Build.VERSION.SDK_INT < 21) return;
      // TDLog.v("QCAM2 image available");
      try {
        Image image = reader.acquireLatestImage();
        Image.Plane[] planes = image.getPlanes();
        if (planes.length > 0) {
          ByteBuffer data = planes[0].getBuffer();
          if (data != null) {
            int size = data.limit();
            if (size > 0) {
              // TDLog.v("QCAM2 planes " + planes.length + " size " + size + " width " + image.getWidth() + " height " + image.getHeight() );
              mJpegData = new byte[size];
              data.get(mJpegData, 0, size);
            }
            // mQCam.setJpegData( data.array() );
            mQCam.enableButtonsOnUiThread( mJpegData != null );
          }
        }
        mState = STATE_PICTURE_DONE;
        // mBackgroundHandler.post( new ImageSaver( reader.acquireLatestImage(), mQCam ) );
      } catch (RuntimeException e) {
        TDLog.e("QCAM2 runtime " + e.getMessage());
        startPreview(); // restart preview
      }
    }
  };

  // ------------------------------------------------------------------------
  /** camera state callback
   */
  // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
  private final StateCallback mStateCallback = new StateCallback() {
    @Override
    public void onOpened(CameraDevice camera) // called when camera is opened - start preview here
    {
      // TDLog.v("QCAM2 camera state on opened");
      mLock.release();
      mCamera = camera;
      createCameraPreviewSession();
    }

    @Override
    public void onDisconnected(CameraDevice camera)
    {
      // TDLog.v("QCAM2 camera state on disconnected");
      mLock.release();
      mCamera.close();
      mCamera = null;
    }

    @Override
    public void onError(CameraDevice camera, int error)
    {
      // TDLog.v("QCAM2 camera state on error " + error );
      mLock.release();
      mCamera.close();
      mCamera = null;
      // TODO TDToast
      mQCam.dismiss(); // finish
    }
  };
  // }

  // ------------------------------------------------------------------------
  final static int STATE_PREVIEW = 0;
  final static int STATE_WAITING_LOCK = 1;
  final static int STATE_WAITING_PRECAPTURE = 2;
  final static int STATE_WAITING_NON_PRECAPTURE = 3;
  final static int STATE_PICTURE_TAKEN = 4;
  final static int STATE_PICTURE_DONE = 5;

  int mState = STATE_PREVIEW;

  String[] mStateStr = { "preview", "wait_lock", "wait_precapture", "wait_non-precapture", "picture_taken", "picture_done" };

  // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
  final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
    @Override
    public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber) {
      // TDLog.v("QCAM2 callback: capture buffer lost");
    }

    // this is called when the request has been sent 
    // https://stackoverflow.com/questions/32961771/camera2-api-oncapturecomplete-is-called-but-camera-state-is-still-control-ae-s
    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)
    {
      // if (Build.VERSION.SDK_INT < 21) return;
      // TDLog.v("QCAM2 callback: capture completed");
      process( result );
    }

    @Override
    public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure)
    {
      // TDLog.v("QCAM2 callback: capture failed");
    }

    @Override
    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
      if (Build.VERSION.SDK_INT < 21) return;
      // TDLog.v("QCAM2 callback: capture progressed");
      process( partialResult );
    }

    @Override
    public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
      // TDLog.v("QCAM2 callback: capture sequence aborted");
    }

    @Override
    public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
      // TDLog.v("QCAM2 callback: capture sequence completed");
    }

    @Override
    public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber)
    {
      // TDLog.v("QCAM2 callback: capture started");
    }

    private void process( CaptureResult result )
    {
      // TDLog.v("QCAM2 callback: process state " + mStateStr[mState] );
      // if (Build.VERSION.SDK_INT < 21) return;
      switch (mState) {
        case STATE_PREVIEW: // nothing
          // TDLog.v("QCAM2 callback: state PREVIEW" );
          break;
        case STATE_WAITING_LOCK:
          // TDLog.v("QCAM2 callback: state WAIT LOCK" );
          Integer af = result.get(CaptureResult.CONTROL_AF_STATE);
          if (af == null) {
            capturePicture();
          } else if (af == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || af == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
            Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
            if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
              mState = STATE_PICTURE_TAKEN;
              // TDLog.v("QCAM2 callback: state WAITING LOCK --> PICTURE TAKEN");
              capturePicture();
            } else {
              runPrecaptureSequence();
            }
          } else {
            capturePicture();
          }
          break;
        case STATE_WAITING_PRECAPTURE:
          // TDLog.v("QCAM2 callback: state WAIT PRECAPTURE" );
          Integer ae1 = result.get(CaptureResult.CONTROL_AE_STATE);
          if (ae1 == null || ae1 == CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
            mState = STATE_WAITING_NON_PRECAPTURE;
            // TDLog.v("QCAM2 callback: state WAITING PRECAPTURE --> WAITING NON PRECAPTURE");
          }
          break;
        case STATE_WAITING_NON_PRECAPTURE:
          // TDLog.v("QCAM2 callback: state WAIT NON-PRECAPTURE" );
          Integer ae2 = result.get(CaptureResult.CONTROL_AE_STATE);
          if (ae2 == null || ae2 != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
            mState = STATE_PICTURE_TAKEN;
            // TDLog.v("QCAM2 callback: state WAITING NON PRECAPTURE --> PICTURE TAKEN");
            capturePicture();
          }
          break;
        case STATE_PICTURE_TAKEN:
          // TDLog.v("QCAM2 callback: state PICTURE TAKEN");
          break;
        case STATE_PICTURE_DONE:
          // TDLog.v("QCAM2 callback: state PICTURE DONE");
          // endCapturePicture();
          break;
      }
    }
  };
  // }

  // ------------------------------------------------------------------------

  /** cstr
   * @param context  context
   * @param attrs    attributes
   */
  public QCamDrawingTexture(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    mContext = context;
    // TDLog.v( "QCAM Texture cstr" );
  }

  /** called by the parent dialog when it is opened
   * @param qcam   parent dialog
   */
  public void start(QCamCompass qcam)
  {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return;
    // TDLog.v("QCAM2 start camera");
    mQCam = qcam;
    mBackgroundThread = TDThread.startThread("camera thread");
    mBackgroundHandler = TDThread.getHandler(mBackgroundThread);
    if (isAvailable()) { // already available: open immediately the camera
      if ( ! openCamera(getWidth(), getHeight()) ) {
        TDLog.e("QCAM2 failed open camera");
      }
    } else { // npt yet available: set a listener
      setSurfaceTextureListener(mSurfaceTextureListener);
    }
  }

  /** called by the parent dialog when it is closed
   */
  public void stop()
  {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return;
    // TDLog.v("QCAM2 stop camera");
    closeCamera();
    if (TDThread.stopThread(mBackgroundThread)) {
      mBackgroundThread = null;
      mBackgroundHandler = null;
    } else { // interrupted
    }
  }

  /** open the camera
   * @param w   width
   * @param h   height
   * @return true if success
   * @note called by start() and onSurfaceTextureAvailable()
   * @note permissions has been checked upstream
   */
  @SuppressLint("MissingPermission")
  private boolean openCamera(int w, int h)
  {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return false;
    // TDLog.v("QCAM2 open camera");
    mManager = (CameraManager) (mContext.getSystemService(Context.CAMERA_SERVICE));
    if ( ! setupCameraOutput( w, h ) ) return false;
    configureTransform(w, h);
    try {
      if (!mLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        return false; // throw
      }
      if ( ! TDandroid.checkCamera(mContext) ) {
        TDLog.e("No CAMERA permission");
        return false;
      }
      mManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
      return true;
    } catch ( CameraAccessException e ) {
      TDLog.e("QCAM2 " + e.getMessage() );
      // e.printStackTrace();
    } catch ( InterruptedException e ) {
      TDLog.e("QCAM2 " + e.getMessage() );
      // TODO throw
    }
    return false;
  }

  /** close the camera, the capture session, and the image reader
   * @note called by stop()
   */
  private void closeCamera()
  {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return;
    // TDLog.v("QCAM2 close camera");
    try {
      mLock.acquire();
      if ( mCaptureSession != null ) { mCaptureSession.close(); mCaptureSession = null; }
      if ( mCamera         != null ) { mCamera.close();         mCamera = null; }
      if ( mImageReader    != null ) { mImageReader.close();    mImageReader    = null; }
    } catch ( InterruptedException e ) {
      TDLog.e("QCAM2 " + e.getMessage() );
      // TODO
    } finally {
      mLock.release();
    }
  }

  /** setup the output
   * @param w   width
   * @param h   height
   * @note called by openCamera()
   */
  private boolean setupCameraOutput( int w, int h )
  {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return false;
    try {
      for ( String id : mManager.getCameraIdList() ) {
        // TDLog.v("QCAM2 setup camera output. Camera ID " + id );
        CameraCharacteristics chr = mManager.getCameraCharacteristics( id );
        Integer facing = chr.get( CameraCharacteristics.LENS_FACING );
        if ( facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT ) continue;
        StreamConfigurationMap map = chr.get( CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP );
        if ( map == null ) continue;
        Size largest = Collections.max( Arrays.asList( map.getOutputSizes( ImageFormat.JPEG ) ), new CompareSizeByArea() );
        mImageReader = ImageReader.newInstance( largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, MAX_CAPTURES );
        mImageReader.setOnImageAvailableListener( mImageAvailable, mBackgroundHandler );

        mOrientation = chr.get( CameraCharacteristics.SENSOR_ORIENTATION ); // 0, 90, 180, or 270
        Point displaySize = new Point();
        int rot = 0;
        try {
          DisplayManager dm = (DisplayManager)( mContext.getSystemService( Context.DISPLAY_SERVICE ) );
          mDisplay = dm.getDisplay( Display.DEFAULT_DISPLAY );
          mDisplay.getSize( displaySize );
          rot = mDisplay.getRotation();
        } catch ( ClassCastException e ) {
          TDLog.e("QCAM2 class cast " + e.getMessage() );
        }
        boolean swapped = false; // whether the swap width and height
        switch ( rot ) {
          case Surface.ROTATION_0:   // 0 display is portrait
          case Surface.ROTATION_180: // 2
            if ( mOrientation == 90 || mOrientation == 270 ) swapped = true; // sensor is right or left
            break;
          case Surface.ROTATION_90:  // 1 display is landscape
          case Surface.ROTATION_270: // 3
            if ( mOrientation == 0 || mOrientation == 180 ) swapped = true; // sensor is up or down
            break;
          default:
            TDLog.e("QCAM2 invalid rotation " + rot );
        }

        int rot_w = w;
        int rot_h = h;
        int max_w = displaySize.x;
        int max_h = displaySize.y;
        if ( swapped ) { // if sensor orientation and display do not agree swap width and height
          rot_w = h;
          rot_h = w;
          max_w = displaySize.y;
          max_h = displaySize.x;
        }
        // TDLog.v("QCAM2 rot " + rot_w + "x" + rot_h + " max " + max_w + "x" + max_h + " swapped " + swapped + " rotation " + rot + " sensor orient " + mOrientation );
        if ( max_w > MAX_PREVIEW_WIDTH  ) max_w = MAX_PREVIEW_WIDTH;
        if ( max_h > MAX_PREVIEW_HEIGHT ) max_h = MAX_PREVIEW_HEIGHT;
        mPreviewSize = optimalSize( map.getOutputSizes( SurfaceTexture.class ), rot_w, rot_h, max_w, max_h, largest );
     
        int orientation = mContext.getResources().getConfiguration().orientation; 
        if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
          setAspectRatio( mPreviewSize.getWidth(), mPreviewSize.getHeight() );
        } else {
          setAspectRatio( mPreviewSize.getHeight(), mPreviewSize.getWidth() );
        }
        Boolean flash = chr.get( CameraCharacteristics.FLASH_INFO_AVAILABLE );
        mHasFlash = ( flash == null )? false : flash;

        mCameraId = id;
        // TDLog.v("QCAM2 preview size " + mPreviewSize.getWidth() + "x" + mPreviewSize.getHeight() + " config orient " + orientation + " rotation " + rot + " flash " + mHasFlash );
        return true;
      }
    } catch ( CameraAccessException e ) {
      TDLog.e("QCAM2 access " + e.getMessage() );
      e.printStackTrace();
    } catch ( NullPointerException e ) {
      TDLog.e("QCAM2 null ptr " + e.getMessage() );
      // camera2 API used but not supported
      e.printStackTrace();
    }
    return false;
  }

  

  /** create a preview session
   * @note called by the state-callback on Opened
   */
  private void createCameraPreviewSession()
  {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return;
    // TDLog.v("QCAM2 create camera preview session");
    try {
      SurfaceTexture texture = getSurfaceTexture();
      if ( texture == null ) {
        TDLog.e("QCAM2 null surface texture");
        return; // or throw
      }
      texture.setDefaultBufferSize( mPreviewSize.getWidth(), mPreviewSize.getHeight() );
      Surface surface = new Surface( texture );
      mPreviewRequestBuilder = mCamera.createCaptureRequest( CameraDevice.TEMPLATE_PREVIEW );
      mPreviewRequestBuilder.addTarget( surface );
      mCamera.createCaptureSession( Arrays.asList( surface, mImageReader.getSurface() ),
        new CameraCaptureSession.StateCallback()
        {
          @Override
          public void onConfigured( CameraCaptureSession session )
          {
            if ( mCamera == null ) {
              // TDLog.v("QCAM2 capture session on configured: null camera");
              return;
            }
            // TDLog.v("QCAM2 capture session on configured");
            mCaptureSession = session;
            // called in startPreview()
            // mPreviewRequestBuilder.set( CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE ); // autofocus
            // setAutoFlash( mPreviewRequestBuilder );
            // mPreviewRequest = mPreviewRequestBuilder.build();
            startPreview();
          }
          @Override
          public void onConfigureFailed( CameraCaptureSession session )
          {
            TDLog.e("QCAM2 failed configure");
            // TDToast
          }
        }, null );
    } catch ( CameraAccessException e ) {
      TDLog.e("QCAM2 access " + e.getMessage() );
      e.printStackTrace();
    }
  }

  /** start previewing
   */
  public boolean startPreview()
  {
    if ( Build.VERSION.SDK_INT < 21 ) return false;
    // TDLog.v("QCAM2 start preview " + mNrCaptures );
    if ( ! canCapture() ) return false;
    mState = STATE_PREVIEW;
    if ( mCamera != null /* && mPreviewRequest != null */ ) {
      try { // continuously send capture requests: necessary to send pictures to the surface
        mPreviewRequestBuilder.set( CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL );
        mPreviewRequestBuilder.set( CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE ); // autofocus
        setAutoFlash( mPreviewRequestBuilder );
        mPreviewRequest = mPreviewRequestBuilder.build();
        if ( mPreviewRequest == null ) return false;

        mCaptureSession.setRepeatingRequest( mPreviewRequest, null, mBackgroundHandler ); // null CaptureCallback
        return true;
      } catch ( CameraAccessException e ) {
        TDLog.e("QCAM2 start preview access " + e.getMessage() );
        e.printStackTrace();
      }
    }
    return false;
  }

  /** stop previewing
   */
  public boolean stopPreview()
  {
    if ( Build.VERSION.SDK_INT < 21 ) return false;
    // TDLog.v("QCAM2 stop preview");
    if ( mCamera != null && mCaptureSession != null ) {
      try { 
        mCaptureSession.stopRepeating();
        return true;
      } catch ( CameraAccessException e ) {
        TDLog.e("QCAM2 access " + e.getMessage() );
        e.printStackTrace();
      }
    }
    return false;
  }

  /**
   * @param choices  size choices
   * @param tw   texture view width
   * @param th   texture view height
   * @param mw   max width
   * @param mh   max height
   * @param aspectRatio aspect ratio
   */
  private static Size optimalSize( Size[] choices, int tw, int th, int mw, int mh, Size aspectRatio )
  {
    if ( Build.VERSION.SDK_INT < 21 ) return null;
    // TDLog.v("QCAM2 optimal size " + tw + " " + th + " max " + mw + " " + mh );
    List< Size > bigEnough = new ArrayList<>();
    List< Size > notEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for ( Size sz : choices ) {
      int sw = sz.getWidth();
      int sh = sz.getHeight();
      if ( sw <= mw && sh <= mh && sh == sw * h / w ) {
        if ( sw >= tw && sh >= th ) {
          bigEnough.add( sz );
        } else {
          notEnough.add( sz );
        }
      }
    }
    if ( bigEnough.size() > 0 ) return Collections.min( bigEnough, new CompareSizeByArea() );
    if ( notEnough.size() > 0 ) return Collections.max( notEnough, new CompareSizeByArea() );
    // TDLog.v("QCAM2 no suitable size");
    return choices[0];
  }

  /** compare sizes by the area
   */
  private static class CompareSizeByArea implements Comparator< Size >
  { 
    @Override
    public int compare( Size s1, Size s2 )
    {
      if ( Build.VERSION.SDK_INT < 21 ) return 0;
      return Long.signum( (long)s1.getWidth() * s1.getHeight() - (long) s2.getWidth() * s2.getHeight() );
    }
  }
    

  /** configure matrix transform to texture view
   * @param vw   view width
   * @param vh   view height
   * @note called by openCamera() and onSurfaceSizeChanged()
   */
  private void configureTransform( int vw, int vh )
  {
    if ( Build.VERSION.SDK_INT < 21 ) return;
    if ( mPreviewSize == null ) {
      // TDLog.v( "QCAM2 configure transform no preview-size");
      return;
    }
    if ( mDisplay == null ) {
      // TDLog.v( "QCAM2 configure transform no display");
      return;
    }
    int r = mDisplay.getRotation(); // 0: up, 1: left, 3: right 2: down
    int pw = mPreviewSize.getWidth();
    int ph = mPreviewSize.getHeight();
    // TDLog.v( "QCAM2 configure transform: view " + vw + " " + vh + " preview " + pw + " " + ph + " rotation " + r );
    Matrix mat = new Matrix();
    RectF viewRect = new RectF( 0, 0, vw, vh );
    // RectF bufRect  = new RectF( 0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth() );
    RectF bufRect  = new RectF( 0, 0, ph, pw );
    float cx = viewRect.centerX();
    float cy = viewRect.centerY();
    if ( r == Surface.ROTATION_90 || r == Surface.ROTATION_270 ) {
      bufRect.offset( cx - bufRect.centerX(), cy - bufRect.centerY() );
      mat.setRectToRect( viewRect, bufRect, Matrix.ScaleToFit.FILL );
      float scale = Math.max( (float)vh / (float)ph, (float)vw / (float)pw );
      mat.postScale( scale, scale, cx, cy );
      mat.postRotate( 90 * (r - 2), cx, cy );
    } else if ( r == Surface.ROTATION_180 ) {
      mat.postRotate( 180, cx, cy );
    } else {
      // nothing
    }
    setTransform( mat );
  }

  /** @return the JPEG data
   */
  byte[] getJpegData() { return mJpegData; }

  /** @return true if the number of captures is smaller than the max limit
   */
  boolean canCapture() { return mNrCaptures < MAX_CAPTURES; }


  /** take a picture
   * @param orientation   display orientation ???
   * @return true on success
   */
  boolean takePicture( int orientation )
  {
    if ( Build.VERSION.SDK_INT < 21 ) return false;
    if ( ! canCapture() ) {
      // TDLog.v( "QCAM2 take picture: cannot capture");
      return false;
    }
    mNrCaptures ++;
    // TDLog.v( "QCAM2 take picture " + orientation + " nr.captures " + mNrCaptures );
    lockFocus();
    return true;
  }

  /** lock the focus
   */
  private void lockFocus()
  {
    if ( Build.VERSION.SDK_INT < 21 ) return;
    // TDLog.v( "QCAM2 lock focus");
    try {
      mPreviewRequestBuilder.set( CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START );
      mState = STATE_WAITING_LOCK;
      // TDLog.v("QCAM2 state --> WAITING LOCK");
      mCaptureSession.capture( mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler );
    } catch ( CameraAccessException e ) {
      TDLog.e("QCAM2 access " + e.getMessage() );
    }
  }

  /** run the still picture pre-capture
   */
  private void runPrecaptureSequence()
  {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return;
    // TDLog.v("QCAM2 run precapture sequence");
    try {
      mPreviewRequestBuilder.set( CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START );
      mState = STATE_WAITING_PRECAPTURE;
      // TDLog.v("QCAM2 state --> WAITING PRECAPTURE");
      mCaptureSession.capture( mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler );
    } catch ( CameraAccessException e ) {
      TDLog.e("QCAM2 access " + e.getMessage() );
    }
  }

  /** capture a still picture
   */
  private void capturePicture()
  {
    if ( mCamera == null ) return;
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) return;
    // TDLog.v("QCAM2 capture picture");
    try {
      final CaptureRequest.Builder captureBuilder = mCamera.createCaptureRequest( CameraDevice.TEMPLATE_STILL_CAPTURE );
      captureBuilder.addTarget( mImageReader.getSurface() );
      captureBuilder.set( CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE );
      setAutoFlash( captureBuilder );
      int r = mDisplay.getRotation();
      captureBuilder.set( CaptureRequest.JPEG_ORIENTATION, getOrientation( r ) );
      CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result ) {
          unlockFocus();
        }
      };
      mCaptureSession.stopRepeating(); 
      mCaptureSession.abortCaptures(); 
      mCaptureSession.capture( captureBuilder.build(), mCaptureCallback, null );
    } catch ( CameraAccessException e ) {
      TDLog.e("QCAM2 access " + e.getMessage() );
    }
  }

  // unused
  // private void endCapturePicture()
  // {
  //   if ( mCamera == null ) return;
  //   // TDLog.v("QCAM2 end capture picture");
  //   try {
  //     mCaptureSession.stopRepeating(); 
  //     mCaptureSession.abortCaptures(); 
  //     mState = STATE_PREVIEW;
  //   } catch ( CameraAccessException e ) {
  //     TDLog.e("QCAM2 access " + e.getMessage() );
  //   }
  // }

  /** unlock focus - and restart previewing
   */
  private void unlockFocus()
  {
    if ( Build.VERSION.SDK_INT < 21 ) return;
    // TDLog.v("QCAM2 unlock focus");
    mState = STATE_PICTURE_DONE;
  }

  // /** restart the preview
  //  */
  // private void restartPreview()
  // {
  //   // TDLog.v("QCAM2 restart preview");
  //   try {
  //     mPreviewRequestBuilder.set( CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL );
  //     setAutoFlash( mPreviewRequestBuilder );
  //     mCaptureSession.capture( mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler );
  //     mState = STATE_PREVIEW;
  //     // TDLog.v("QCAM2 state --> PREVIEW");
  //     mCaptureSession.setRepeatingRequest( mPreviewRequest, mCaptureCallback, mBackgroundHandler );
  //   } catch ( CameraAccessException e ) {
  //     TDLog.e("QCAM2 access " + e.getMessage() );
  //   }
  // }

  /** set the auto-flash
   * @param builder   capture request builder
   */
  private void setAutoFlash( CaptureRequest.Builder builder )
  {
    if ( Build.VERSION.SDK_INT < 21 ) return;
    // TDLog.v("QCAM2 set auto-flash " + mHasFlash );
    if ( ! mHasFlash ) return;
    builder.set( CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH );
  }


  /** @return orientation from the rotation
   * @param rot  rotation
   *
   * static {
   *   ORIENTATIONS.append(Surface.ROTATION_0, 90);
   *   ORIENTATIONS.append(Surface.ROTATION_90, 0);
   *   ORIENTATIONS.append(Surface.ROTATION_180, 270);
   *   ORIENTATIONS.append(Surface.ROTATION_270, 180);
   * }
   */
  private int getOrientation( int rot )
  {
    // return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    if ( rot == Surface.ROTATION_0 )   return  90;
    if ( rot == Surface.ROTATION_90 )  return   0;
    if ( rot == Surface.ROTATION_180 ) return 270;
    if ( rot == Surface.ROTATION_270 ) return 180;
    return 90;
  } 

  // /** image saver class - not used because image saving is just setting a pointer
  //  */
  // private static class ImageSaver implements Runnable
  // {
  //   private final Image mImage;
  //   private QCamCompass mCompass;
  //   
  //   ImageSaver( Image image, QCamCompass compass ) 
  //   {
  //     mImage = image;
  //     mCompass = compass;
  //   }
 
  //   @Override
  //   public void run()
  //   {
  //     if ( mImage != null ) {
  //       Image.Plane[] planes = mImage.getPlanes();
  //       // TDLog.v("QCAM2 planes " + planes.length );
  //       if ( planes.length > 0 ) {
  //         ByteBuffer data = planes[0].getBuffer();
  //         if ( data != null ) {
  //           mCompass.setJpegData( data.array() );
  //         }
  //       }
  //     }
  //   }
  // }

  // from AutoFitTextureView -------------------------------------------------------------
  // https://github.com/googlearchive/android-Camera2Basic/blob/master/kotlinApp/Application/src/main/java/com/example/android/camera2basic/AutoFitTextureView.kt

  private int ratioWidth  = 0;
  private int ratioHeight = 0;

  /** set the aspect ratio
   * @param w   width
   * @param h   height
   */
  private void setAspectRatio( int w, int h ) 
  {
    if (w < 0 || h < 0) {
      throw new IllegalArgumentException("QCAM2 Size cannot be negative.");
    }
    ratioWidth  = w;
    ratioHeight = h;
    requestLayout();
  }

  /** react to a request to remeasure
   * @param widthMeasureSpec   width
   * @param heightMeasureSpec  height
   */
  @Override
  public void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
  {
    super.onMeasure( widthMeasureSpec, heightMeasureSpec );
    int width  = View.MeasureSpec.getSize( widthMeasureSpec );
    int height = View.MeasureSpec.getSize( heightMeasureSpec );
    if (ratioWidth == 0 || ratioHeight == 0) {
      setMeasuredDimension(width, height);
    } else {
      int wrh = width * ratioHeight;
      int hrw = height * ratioWidth;
      // if ( wrh == hrw ) return;
      // TDLog.v("QCAM2 WxH " + width + "x" + height + " ratio " + ratioWidth + "x" + ratioHeight );
      if ( wrh < hrw ) {
        setMeasuredDimension(width, width * ratioHeight / ratioWidth);
      } else {
        setMeasuredDimension(height * ratioWidth / ratioHeight, height);
      }
    }
  }

  // zoom functions from https://stackoverflow.com/questions/52158395/how-to-zoom-camera-using-android-camera2-api

  /** @return the current zoom level
   */
  public float getCurrentZoom() 
  {
    return mZoomLevel;
  }

  /** set the current zoom level
   * @param zoomLevel  new zoom level
   */
  public void setCurrentZoom( float zoomLevel )
  {
    Rect zoomRect = getZoomRect( zoomLevel );
    if ( zoomRect != null ) {
      try {
        //you can try to add the synchronized object here 
        mPreviewRequestBuilder.set( CaptureRequest.SCALER_CROP_REGION, zoomRect );
        mCaptureSession.setRepeatingRequest( mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler );
      } catch ( Exception e ) {
        TDLog.e("Error updating preview: " + e.getMessage() );
      }
      mZoomLevel = (int) zoomLevel;
      TDLog.v("set current zoom " + mZoomLevel );
    }
  }

  /** @return the zoom rectangle for a given level
   * @param zoomLevel  zoom level
   */
  private Rect getZoomRect( float zoomLevel )
  {
    try {
      CameraCharacteristics characteristics = mManager.getCameraCharacteristics( mCameraId );
      float maxZoom = (characteristics.get( CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;
      Rect activeRect = characteristics.get( CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
      if ( ( zoomLevel <= maxZoom ) && ( zoomLevel > 1 ) ) {
        int minW = (int) (activeRect.width() / maxZoom);
        int minH = (int) (activeRect.height() / maxZoom);
        int difW = activeRect.width() - minW;
        int difH = activeRect.height() - minH;
        int cropW = difW / 100 * (int) zoomLevel;
        int cropH = difH / 100 * (int) zoomLevel;
        cropW -= cropW & 3;
        cropH -= cropH & 3;
        return new Rect(cropW, cropH, activeRect.width() - cropW, activeRect.height() - cropH);
      } else if ( zoomLevel == 0 ){
        return new Rect(0, 0, activeRect.width(), activeRect.height());
      }
      return null;
    } catch (Exception e) {
      TDLog.e("Error get zoom rect " + e.getMessage());
      return null;
    }
  }

  /** get the maximum zoom value
   */
  public int getMaxZoom()
  {
    // return 100; // (mCamera != null )? mCamera.getParameters().getMaxZoom() : 100;
    try {
      float max = (mManager.getCameraCharacteristics( mCameraId ).get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;
      TDLog.v("max zoom " + max );
      return (int)max;
    } catch (Exception e) {
      TDLog.e("Error get max zoom " + e.getMessage());
      return -1;
    }
  }

  /** zoom in/out
   * @param delta_zoom   zoom change
   */
  void zoom( int delta_zoom )
  {
    if ( mCamera != null ) {
      setCurrentZoom( mZoomLevel + delta_zoom );
    }
  }

}
