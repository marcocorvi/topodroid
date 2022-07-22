/* @file QCamCompass.java
 *
 * @author marco corvi
 * @date jan. 2017
 *
 * @brief TopoDroid quick cam compass
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.TDLayout;
import com.topodroid.ui.ExifInfo;
import com.topodroid.prefs.TDSetting;

import android.app.Dialog;
import android.app.Activity;
import android.content.Context;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.MotionEvent;
import android.view.Window;

import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

import android.graphics.drawable.BitmapDrawable;

import java.util.Locale;
// import java.nio.ByteBuffer;

class QCamCompass extends Dialog
                  implements OnClickListener
                  , OnTouchListener
                  , OnZoomListener
                  , IBearingAndClino
{
  private final Context mContext;
  // DrawingWindow mDrawer;
  // long mPid;

  private Activity mParent; // parent activity;
  private IPhotoInserter mInserter;
  private QCamDrawingSurface mSurface = null;
  private QCamDrawingTexture mTexture = null; // TEXTURE
  // private QCamBox mBox;
  private Button buttonClick;
  private Button buttonSave;
  private Button buttonCancel;

  private View mZoomView;
  private ZoomButtonsController mZoomBtnsCtrl = null;
  private boolean mZoomBtnsCtrlOn = false;

  private BitmapDrawable mBDcameraRed;
  private BitmapDrawable mBDcamera;
  private BitmapDrawable mBDsaveok;
  private BitmapDrawable mBDsaveoff;

  private TextView mTVdata;
  private float mBearing;
  private float mClino;
  private int   mOrientation;
  private int   mAccuracy;
  
  private boolean mHasBearingAndClino;
  private IBearingAndClino mCallback;
  private boolean mWithBox;
  private boolean mWithDelay;
  private boolean mHasSaved;
  private boolean mHasShot;
  private int mCamera;

  QCamCompass( Context context, Activity parent, IBearingAndClino callback, IPhotoInserter inserter, boolean with_box, boolean with_delay, int camera )
  {
    super( context );
    mContext   = context;
    mParent    = parent; 
    mCallback  = callback;
    // mDrawer    = drawer;
    // mPid       = pid;
    mInserter  = inserter;
    mWithBox   = with_box;
    mWithDelay = with_delay;
    mHasSaved  = false;
    mHasShot   = false;
    mCamera    = camera;
    // TDLog.Log( TDLog.LOG_PHOTO, "QCAM compass. Box " + mWithBox + " delay " + mWithDelay );
  }

  void enableButtons( boolean enable )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "QCAM compass enable buttons " + enable );
    buttonClick.setEnabled( enable );
    buttonCancel.setEnabled( enable );
    // if ( enable ) {
    //   buttonClick.setVisibility( View.VISIBLE );
    //   buttonCancel.setVisibility( View.VISIBLE );
    // } else {
    //   buttonClick.setVisibility( View.GONE );
    //   buttonCancel.setVisibility( View.GONE );
    // }
  }

  /** enable buttons 
   * @param enable  whether to enable
   * @note used by QCamDrawingTexture
   */
  void enableButtonsOnUiThread( final boolean enable ) // TEXTURE
  {
    if ( mParent != null ) {
      mParent.runOnUiThread( new Runnable() {
        @Override
        public void run() { enableButtons( enable ); }
      } );
    }
  }

  private void enableButtonSave( boolean enable )
  {
    // TDLog.v( "QCAM compass enable save button " + enable );
    buttonSave.setEnabled( enable );
    // buttonSave.setVisibility( enable? View.VISIBLE : View.GONE );
    TDandroid.setButtonBackground( buttonSave, (enable? mBDsaveok : mBDsaveoff) );
  }


  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature( Window.FEATURE_NO_TITLE );
    // TDLog.v("QCAM camera " + mCamera );
    if ( mCamera == PhotoInfo.CAMERA_TOPODROID ) {
      setContentView(R.layout.qcam_compass);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
      // lock screen orientation
      mSurface = (QCamDrawingSurface) findViewById( R.id.drawingSurface ); // TEXTURE
      mSurface.mQCam = this;
      // mSurface.setOnTouchListener( this );
    } else {
      setContentView(R.layout.qcam_compass_two);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
      // lock screen orientation
      mTexture = (QCamDrawingTexture) findViewById( R.id.drawingTexture );
      mTexture.start( this );
    }

    findViewById( R.id.qcam_layout ).setOnTouchListener( this );

    mZoomView = (View) findViewById(R.id.zoomView );
    mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
    // mZoomBtnsCtrlOn = (TDSetting.mZoomCtrl > 1);  // do before setting content
    switchZoomCtrl( 1 ); // temporary zoom buttons

    // buttonClick  = (Button) findViewById(R.id.buttonClick);
    // buttonSave   = (Button) findViewById(R.id.buttonSave);
    // buttonCancel = (Button) findViewById(R.id.buttonQuit);

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    buttonClick  = MyButton.getButton( mContext, this, R.drawable.iz_camera_red_transp );
    buttonSave   = MyButton.getButton( mContext, this, R.drawable.iz_save_off_transp );
    buttonCancel = MyButton.getButton( mContext, this, R.drawable.iz_cancel ); // iz_cancel_transp

    mBDcameraRed = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_camera_red_transp );
    mBDcamera    = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_camera ); // iz_camera_transp
    mBDsaveok    = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_save_transp );
    mBDsaveoff   = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_save_off_transp );

    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );
    LinearLayout ll_help = (LinearLayout) findViewById( R.id.help );
    ll_help.setMinimumHeight( size + 20 );
    ll_help.addView( buttonClick, lp );
    ll_help.addView( buttonSave, lp );
    ll_help.addView( buttonCancel, lp );

    enableButtonSave( false );

    mHasShot = false;

    mTVdata = (TextView)findViewById( R.id.data );
    mHasBearingAndClino = false; 

    if ( mWithBox ) {
      QCamBox box = new QCamBox( mContext );
      addContentView( box, new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ) );
    }
  }

  // /** compute the azimuth of the z-vector from Android azimuth, pitch and roll
  //  * @param a   azimuth [radians]
  //  * @param p   pitch
  //  * @param r   roll
  //  * @return z-axis azimuth in [0, 360] degrees
  //  *
  //  * Android axes are (SensorEvent) 
  //  *    Y long side upward
  //  *    X short side rightward
  //  *    Z outward from the screen 
  //  *    z inward the screen ( z = -Z )
  //  *
  //  * given (a,p,r) azimuth, pitch, roll from Android [radians], where
  //  *    a = angle between projection of Y (long side) on horizontal plane and North
  //  *    p = angle between Y and its projection on the horizontal plane
  //  *    r = angle between the intersection of the device plane amd the horizontal plane and X axis (short side)
  //  * 
  //  * Y = N c(a) c(p)                       + E s(a) c(p)                     - V s(p)
  //  * X = N[ - s(a) c(r) - s(p) s(r) c(a) ] + E[ c(a) c(r) - s(p) s(r) s(a) ] - V c(p) s(r)
  //  * Z = N[   s(a) s(r) - s(p) c(r) s(a) ] - E[ c(a) s(r) + s(p) c(r) s(a) ] - V c(p) c(r)
  //  * z = N[ - s(a) s(r) + s(p) c(r) s(a) ] + E[ c(a) s(r) + s(p) c(r) s(a) ] + V c(p) c(r)
  //  *
  //  * where V vertical downward (N,E,V right triplet)
  //  */
  // static float getZBearing( float a, float p, float r )
  // {
  //   float sa = TDMath.sin( a );
  //   float ca = TDMath.cos( a );
  //   float sp = TDMath.sin( p );
  //   // float cp = TDMath.cos( p );
  //   float sr = TDMath.sin( r );
  //   float cr = TDMath.cos( r );
  //   float zn = - sa * sr + sp * cr + ca;
  //   float ze =   ca * sr + sp * cr * sa;
  //   // float zv =   cp * cr;
  //   return TDMath.in360( TDMath.atan2d( ze, zn ) );
  // }

  // /** compute the clino of the z-vector (inward to the screen) from Android azimuth, pitch and roll
  //  * @param a   azimuth [radians]
  //  * @param p   pitch
  //  * @param r   roll
  //  * @return z-axis clino in [-90, 90] degrees
  //  * @see getZBearing()
  //  */
  // static float getZClino( float a, float p, float r )
  // {
  //   float sa = TDMath.sin( a );
  //   float ca = TDMath.cos( a );
  //   float sp = TDMath.sin( p );
  //   float cp = TDMath.cos( p );
  //   float sr = TDMath.sin( r );
  //   float cr = TDMath.cos( r );
  //   float zn = - sa * sr + sp * cr + ca;
  //   float ze =   ca * sr + sp * cr * sa;
  //   float zh = TDMath.sqrt( ze*ze + zn*zn );
  //   float zv =   cp * cr;
  //   return TDMath.atan2d( zv, zh );
  // }

  /** @implements
   * @param b   azimuth
   * @param c   clino
   * @param o   orientation: 0 up, 90 right, 180 down, 270 left
   * @param a   sensor accuracy 
   * @param cam camera API (1 or 2)
   */
  public void setBearingAndClino( float b, float c, int o, int a, int cam )
  {
    mBearing     = b;
    mClino       = c;
    mOrientation = ExifInfo.getCameraOrientation( o );
    mAccuracy    = a;
    mCamera = cam;
    // TDLog.v( "QCAM compass camera " + cam + " orient " + o + " --> " + mOrientation );
    // TDLog.v( "QCAM compass set orientation " + o + " bearing " + b + " clino " + c + " orientation " + o + " -> " + mOrientation );

    mTVdata.setText( String.format(Locale.US, "%.2f %.2f", mBearing, mClino ) );
    mHasBearingAndClino = true;

    // take snapshot
    if ( mSurface != null ) {
      mHasShot = mSurface.takePicture( mOrientation );
    } else if ( mTexture != null ) {
      mHasShot = mTexture.takePicture( mOrientation );
    }
    enableButtonSave( true );
    TDandroid.setButtonBackground( buttonClick, (mHasShot ? mBDcamera : mBDcameraRed) );
    // buttonClick.setText( mContext.getString( mHasShot ? R.string.button_redo : R.string.button_eval ) );
  }

  /** react to a user tap
   * @param v  tapped view
   */
  @Override
  public void onClick(View v)
  {
    boolean dismiss = true;
    Button b = (Button)v;
    if ( b == buttonClick ) {
      // TDLog.v( "QCAM compass. Click picture button");
      if ( mHasShot ) {
        if ( mTexture != null && ! mTexture.canCapture() ) {
          TDToast.makeWarn( "Too many pictures" );
          enableButtons( false );
        } else {
          mHasShot = false;
          TDandroid.setButtonBackground( buttonClick, mBDcameraRed );
          // buttonClick.setText( mContext.getString( R.string.button_eval ) );

          // QCamDrawingSurface.startPreview() when it is created
          if ( mSurface != null ) {
            mSurface.startPreview(); // TEXTURE
          } else if ( mTexture != null ) {
            // TODO
            mTexture.startPreview();
          }
          enableButtons( true );
        }
      } else {
        int wait  = TDSetting.mTimerWait;
        int count = 10;
        if ( ! mWithDelay ) {
          wait = 0;
          count = 3;
        }
        enableButtons( false );
        enableButtonSave( false );
        if ( mTexture != null && ! mTexture.canCapture() ) {
          buttonClick.setVisibility( View.GONE );
          TDToast.makeWarn( mContext.getResources().getString( R.string.photo_max_captures ) );
        }
        TimerTask timer = new TimerTask( this, -TimerTask.Z_AXIS, wait, count );
        timer.execute();
      }
      return;
    } else if ( b == buttonSave ) {
      // TDLog.v( "QCAM compass. Click save button");
      if ( mHasBearingAndClino ) {
        if ( mCallback != null ) {
          // TDLog.v( "Orientation " + mOrientation + " " + mBearing + " " + mClino );
          if ( mSurface != null ) {
            mCallback.setBearingAndClino( mBearing, mClino, mOrientation, mAccuracy, 1 ); // camera API
            mHasSaved = mCallback.setJpegData( mSurface.getJpegData() );
          } else if ( mTexture != null ) {
            mCallback.setBearingAndClino( mBearing, mClino, mOrientation, mAccuracy, 2 ); // camera2 API
            mHasSaved = mCallback.setJpegData( mTexture.getJpegData() );
          }
          if ( ! mHasSaved ) {
            TDToast.makeBad( mContext.getResources().getString( R.string.photo_failed ) );
            dismiss = false;
          }
        }
      }
    } else if ( b == buttonCancel ) {
      mHasSaved = false;
      // TDLog.v( "QCAM compass. Click cancel button");
    } else {
      TDLog.Error( "QCAM compass. Click unexpected view");
    }
    // TDLog.v("QCAM has saved data " + mHasSaved + " dismiss " + dismiss );
    if ( dismiss ) {
      // if ( mSurface != null ) mSurface.close();
      TDUtil.slowDown( 100 );

      if ( mHasSaved ) {
        if ( mInserter != null ) mInserter.insertPhoto();
        // if ( mDrawer   != null ) mDrawer.notifyAzimuthClino( mPid, mBearing, mClino );
      }
      // unlock screen orientation
      // // mSurface.close(); 
      if ( mTexture != null ) mTexture.stop(); // TEXTURE 
      dismiss();
    } else {
      enableButtons( true );
      enableButtonSave( false );
    }
  }

  /** BACK pressed is like "close" but data are not saved
   */
  @Override
  public void onBackPressed()
  {
    // TDLog.v( "QCAM compass. BACK pressed");
    onClick( buttonCancel );
  }

  float mZoomD0 = 0;

  /** react to a user screen touch
   * @param view     touched view
   * @param rawEvent touch event
   * @return true if the event has been handled
   */
  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    // MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
    MotionEvent event = rawEvent;
    int act = event.getAction();
    int action = act & MotionEvent.ACTION_MASK;
    boolean ret = false;
    // TDLog.Log( TDLog.LOG_PHOTO, "QCAM compass. Touch ptrs " + event.getPointerCount() + " " + event.getX(0) + " " + event.getY(0) );
    if ( action == MotionEvent.ACTION_MOVE ) { 
      if (event.getPointerCount() == 2 ) {
        float x0 = event.getX(1) - event.getX(0);
        float y0 = event.getY(1) - event.getY(0);
        float d = TDMath.sqrt( x0*x0 + y0*y0 );
        ret = true;
        if ( d > mZoomD0 * 1.1 ) {
          if ( mSurface != null ) {
            mSurface.zoom( +1 );
          } else if ( mTexture != null ) {
            mTexture.zoom( +1 );
          }
          mZoomD0 = d;
        } else if ( d < mZoomD0 * 0.9 ) {
          if ( mSurface != null ) {
            mSurface.zoom( -1 );
          } else if ( mTexture != null ) {
            mTexture.zoom( -1 );
          }
          mZoomD0 = d;
        }
      }
    } else if ( action == MotionEvent.ACTION_POINTER_DOWN) {
      // TDLog.v("Qcam POINTER DOWN" );
      if (event.getPointerCount() == 2) {
        float x0 = event.getX(1) - event.getX(0);
        float y0 = event.getY(1) - event.getY(0);
        mZoomD0 = TDMath.sqrt( x0*x0 + y0*y0 );
        ret = true;
      }
    } else if ( action == MotionEvent.ACTION_POINTER_UP) {
      // TDLog.v("Qcam POINTER UP" );
      if (event.getPointerCount() == 2) {
        ret = true;
      }
    } else if (action == MotionEvent.ACTION_DOWN) { 
      float x0 = event.getX(0);
      float y0 = event.getY(0);
      // TDLog.v("Qcam DOWN " + x0 + " " + y0 );
      if ( y0 > TopoDroidApp.mBorderBottom ) {
        if ( mZoomBtnsCtrlOn && x0 > TopoDroidApp.mBorderInnerLeft && x0 < TopoDroidApp.mBorderInnerRight ) {
          mZoomBtnsCtrl.setVisible( true );
        }
      }
    // } else if (action == MotionEvent.ACTION_UP) {
      // TDLog.v("Qcam UP" );
    }
    return false;
  }

  /** @implement from OnZoomListener: called when zoom controls visibility changes.
   * @param visible whether controls are visible
   */
  @Override
  public void onVisibilityChanged(boolean visible)
  {
    if ( mZoomBtnsCtrlOn && mZoomBtnsCtrl != null ) {
      mZoomBtnsCtrl.setVisible( visible || ( TDSetting.mZoomCtrl > 1 ) );
    }
  }

  // /** THIS IS NOT: @implement from IZoomer
  //  */
  // public void checkZoomBtnsCtrl()
  // {
  //   // if ( mZoomBtnsCtrl == null ) return; // not necessary
  //   if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
  //     mZoomBtnsCtrl.setVisible( true );
  //   }
  // }

  // this method is a callback to let other objects tell the activity to use zooms or not
  private void switchZoomCtrl( int ctrl )
  {
    if ( mZoomBtnsCtrl == null ) return;
    mZoomBtnsCtrlOn = (ctrl > 0);
    switch ( ctrl ) {
      case 0:
        mZoomBtnsCtrl.setOnZoomListener( null );
        mZoomBtnsCtrl.setVisible( false );
        mZoomBtnsCtrl.setZoomInEnabled( false );
        mZoomBtnsCtrl.setZoomOutEnabled( false );
        mZoomView.setVisibility( View.GONE );
        break;
      case 1:
        mZoomView.setVisibility( View.VISIBLE );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( false );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        break;
      case 2:
        mZoomView.setVisibility( View.VISIBLE );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( true );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        break;
    }
  }


  // int mDeltaZoom = 1;
  long mZoomTime = 0;

  /** @implements from OnZoomListener: view needs to be zoomed
   * @param zoomin whether it is a zoom-in
   */
  @Override
  public void onZoom( boolean zoomin )
  {
    int mDeltaZoom = 1;
    long time = System.currentTimeMillis();
    if ( time - mZoomTime < 300 ) {
      mDeltaZoom = 5;
      // int max = mSurface.getMaxZoom() / 4;
      // if ( mDeltaZoom > max ) mDeltaZoom = max;
    // } else {
    //   mDeltaZoom = 1;
    }
    if ( mSurface != null ) {
      mSurface.zoom( zoomin? mDeltaZoom : -mDeltaZoom );
    } else if ( mTexture != null ) {
      mTexture.zoom( zoomin? mDeltaZoom : -mDeltaZoom );
    }
    mZoomTime = time;
  }

  /** @implement set the JPEG data - use default (comment wen minsdk = 24)
   * @param data   JPEG image data
   * @return true on success
   */
  public boolean setJpegData( byte[] data ) { return false; } 

}
