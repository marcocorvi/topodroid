/** @file QCamPreview.java
 *
 * @author marco corvi
 * @date jan. 2017
 *
 * @brief TopoDroid quick cam preview
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

import android.util.Log;

class QCamPreview 
{
  Context mContext;
  Camera mCamera = null;
  QCamCompass mQuickCam;

  Camera.PreviewCallback mPreviewCallback;
  Camera.PictureCallback mRaw;
  Camera.PictureCallback mJpeg;
  Camera.ShutterCallback mShutter;
  int count = 0;

  QCamPreview( Context context, QCamCompass qcam ) 
  {
    mContext  = context;
    mQuickCam = qcam;
    mCamera = null;

    mShutter = new ShutterCallback() {
      public void onShutter( )
      {
        // Log.v( "DistoX", "Shutter callback " );
      }
    };

    mRaw = new PictureCallback() {
      public void onPictureTaken( byte[] data, Camera c ) 
      {
        // Log.v( "DistoX", "Picture Raw callback data " + ((data==null)? "null" : data.length) );
      }
    };

    mJpeg = new PictureCallback() {
      public void onPictureTaken( byte[] data, Camera c ) 
      { 
        // Log.v( "DistoX", "Picture JPEG callback data " + ((data==null)? "null" : data.length) );
      }
    };
        
    mPreviewCallback = new PreviewCallback() {
        // called every time startPreview
        public void onPreviewFrame(byte[] data, Camera arg1) {
          Log.v("DistoX", "on preview frame");
          mQuickCam.mSurface.drawReference();
        }
    };

    // new AcquireThread().start();
  }

  // public void acquire()
  // {
  //   Log.v("DistoX", "acquire" );
  //   mDoAcquire = true;
  // }

  public boolean open()
  {
    if ( mCamera != null ) return true;
    // Log.v("DistoX", "open qcam" );
    try {
      mCamera = Camera.open();
      Camera.Parameters params = mCamera.getParameters();
      params.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );
      params.setSceneMode( Camera.Parameters.SCENE_MODE_AUTO );
      List< Integer > formats = params.getSupportedPreviewFormats();
      for ( Integer fmt : formats ) {
        if ( fmt.intValue() == ImageFormat.JPEG ) {
          // Log.v("DistoX", "Set preview format JPEG" );
          params.setPreviewFormat( ImageFormat.JPEG );
        }
        // Log.v("DistoX", "QCamPreview formats " + fmt );
      }
      mCamera.setParameters( params );
      mCamera.setPreviewCallback( mPreviewCallback );
      int format = params.getPreviewFormat();
      // Log.v( "DistoX", "QCamPreview Format " + format );

      Camera.Size size = params.getPreviewSize();
      // Log.v( "DistoX", "QCamPreview size " + size.width + " " + size.height );
      try {
        mCamera.setPreviewDisplay( mQuickCam.mSurface.surfaceHolder );
      } catch ( IOException e ) {
        TDLog.Error( "cannot set preview display" );
      }
      mCamera.startPreview();
      return true;
    } catch ( RuntimeException e ) { // fail to connect to canera service
      if ( mCamera != null ) mCamera.release();
      mCamera = null;
      TDLog.Error( e.getMessage() );
    }
    return false;
  }

  public void setSize( int w, int h, SurfaceHolder surfaceHolder )
  {
    if ( mCamera == null ) return;
    // Log.v( "DistoX", "set size " + w + " " + h );
    Camera.Parameters params = mCamera.getParameters();
    params.setPreviewSize( w, h );
    // FIXME mCamera.setParameters( params );
    // mCamera.setPreviewCallback( mPreviewCallback );
    try {
      mCamera.setDisplayOrientation( 90 );
      mCamera.setPreviewDisplay( surfaceHolder );
    } catch ( IOException e ) {
      TDLog.Error( "cannot set preview display" );
    }
  }
    
    
  public void close()
  {
    // mDoRun = false;
    // mAcquireThread.join();
    mCamera.stopPreview();
    mCamera.release();
    mCamera = null;
  }

  // boolean mDoRun;
  // boolean mDoAcquire;

  // class AcquireThread extends Thread
  // {

  //   public void run()
  //   {
  //     Log.v( "DistoX", "AcquireThread run ...");
  //     mDoRun     = true;
  //     mDoAcquire = false;
  //     open();
  //     mCamera.startPreview();
  //     while ( mDoRun ) {
  //       if ( mDoAcquire ) {
  //         Log.v( "DistoX", "AcquireThread acquire");
  //         count = 0;
  //         // mCamera.takePicture( mShutter, mRaw, null, mJpeg);
  //         mCamera.startPreview();
  //         // mCamera.stopPreview();
  //         mDoAcquire = false;
  //       } else {
  //         try { Thread.sleep( 500 ); } catch( InterruptedException e ) { }
  //       }
  //     }
  //     // mCamera.stopPreview();
  //     Log.v( "DistoX", "AcquireThread exit");
  //   }
  // }
        


}
