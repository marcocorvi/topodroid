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
package com.topodroid.DistoX;

import android.util.Log;

// import android.app.Activity;
import android.app.Dialog;
// import android.content.Intent;
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
// import android.widget.FrameLayout;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

import android.graphics.drawable.BitmapDrawable;

import java.util.Locale;

class QCamCompass extends Dialog
                  implements OnClickListener
                  , OnTouchListener
                  , OnZoomListener
                  , IBearingAndClino
{
  private final Context mContext;
  // DrawingWindow mDrawer;
  // long mPid;
  private float mBorderInnerRight = 4096;
  private float mBorderInnerLeft  = 0;
  private float mBorderBottom     = 4096;

  private IPhotoInserter mInserter;
  private QCamDrawingSurface mSurface;
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
  private boolean mHasBearingAndClino;
  private IBearingAndClino mCallback;
  private boolean mWithBox;
  private boolean mWithDelay;
  private boolean mHasSaved;
  private boolean mHasShot;

  QCamCompass( Context context, IBearingAndClino callback, IPhotoInserter inserter, boolean with_box, boolean with_delay )
  {
    super( context );
    mContext   = context;
    mCallback  = callback;
    // mDrawer    = drawer;
    // mPid       = pid;
    mInserter  = inserter;
    mWithBox   = with_box;
    mWithDelay = with_delay;
    mHasSaved  = false;
    mHasShot   = false;
  }

  void enableButtons( boolean enable )
  {
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

  private void enableButtonSave( boolean enable )
  {
    buttonSave.setEnabled( enable );
    // buttonSave.setVisibility( enable? View.VISIBLE : View.GONE );
    TDandroid.setButtonBackground( buttonSave, (enable? mBDsaveok : mBDsaveoff) );
  }


  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature( Window.FEATURE_NO_TITLE );
    setContentView(R.layout.qcam_compass);

    mBorderInnerRight = TopoDroidApp.mDisplayWidth * 3 / 4;
    mBorderInnerLeft  = TopoDroidApp.mDisplayWidth / 4;
    mBorderBottom     = TopoDroidApp.mDisplayHeight * 7 / 8;

    mSurface = (QCamDrawingSurface) findViewById( R.id.drawingSurface );
    mSurface.mQCam = this;
    // mSurface.setOnTouchListener( this );
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
    buttonCancel = MyButton.getButton( mContext, this, R.drawable.iz_cancel_transp );

    mBDcameraRed = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_camera_red_transp );
    mBDcamera    = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_camera_transp );
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

  // implements
  public void setBearingAndClino( float b, float c, int o )
  {
    mBearing = b;
    mClino   = c;
    mOrientation = MyBearingAndClino.getCameraOrientation( o );
    // Log.v("DistoX", "QCam compass orient " + o + " --> " + mOrientation );
    mTVdata.setText( String.format(Locale.US, "%.2f %.2f", mBearing, mClino ) );
    mHasBearingAndClino = true;

    // take snapshot
    mHasShot = mSurface.takePicture( mOrientation );
    enableButtonSave( true );
    TDandroid.setButtonBackground( buttonClick, (mHasShot ? mBDcamera : mBDcameraRed) );
    // buttonClick.setText( mContext.getString( mHasShot ? R.string.button_redo : R.string.button_eval ) );
  }


  public void setJpegData( byte[] data ) { }

  @Override
  public void onClick(View v)
  {
    Button b = (Button)v;
    if ( b == buttonClick ) {
      if ( mHasShot ) {
        mHasShot = false;
        TDandroid.setButtonBackground( buttonClick, mBDcameraRed );
        // buttonClick.setText( mContext.getString( R.string.button_eval ) );
        mSurface.start();
        enableButtons( true );
      } else {
        int wait  = TDSetting.mTimerWait;
        int count = 10;
        if ( ! mWithDelay ) {
          wait = 0;
          count = 3;
        }
        enableButtons( false );
        enableButtonSave( false );
        TimerTask timer = new TimerTask( this, -TimerTask.Z_AXIS, wait, count );
        timer.execute();
      }
      return;
    } else if ( b == buttonSave ) {
      if ( mHasBearingAndClino ) {
        if ( mCallback != null ) {
          // Log.v("DistoX", "Orientation " + mOrientation );
          mCallback.setBearingAndClino( mBearing, mClino, mOrientation );
          mCallback.setJpegData( mSurface.mJpegData );
          mHasSaved = true;
        }
      }
    // } else if ( b == buttonCancel ) {
      /* nothing */
    }
    mSurface.close();
    TDUtil.slowDown( 100 );

    if ( mHasSaved ) {
      if ( mInserter != null ) mInserter.insertPhoto();
      // if ( mDrawer   != null ) mDrawer.notifyAzimuthClino( mPid, mBearing, mClino );
    }
    dismiss();
  }

  float mZoomD0 = 0;
  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    // MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
    MotionEvent event = rawEvent;
    int act = event.getAction();
    int action = act & MotionEvent.ACTION_MASK;
    boolean ret = false;
    Log.v("DistoX-QCAM", "on touch ptr " + event.getPointerCount() + " " + event.getX(0) + " " + event.getY(0) );
    if ( action == MotionEvent.ACTION_MOVE ) { 
      if (event.getPointerCount() == 2 ) {
        float x0 = event.getX(1) - event.getX(0);
        float y0 = event.getY(1) - event.getY(0);
        float d = TDMath.sqrt( x0*x0 + y0*y0 );
        ret = true;
        if ( d > mZoomD0 * 1.1 ) {
          mSurface.zoom( +1 );
          mZoomD0 = d;
        } else if ( d < mZoomD0 * 0.9 ) {
          mSurface.zoom( -1 );
          mZoomD0 = d;
        }
      }
    } else if ( action == MotionEvent.ACTION_POINTER_DOWN) {
      // Log.v("DistoX-QCAM", "POINTER DOWN" );
      if (event.getPointerCount() == 2) {
        float x0 = event.getX(1) - event.getX(0);
        float y0 = event.getY(1) - event.getY(0);
        mZoomD0 = TDMath.sqrt( x0*x0 + y0*y0 );
        ret = true;
      }
    } else if ( action == MotionEvent.ACTION_POINTER_UP) {
      // Log.v("DistoX-QCAM", "POINTER UP" );
      if (event.getPointerCount() == 2) {
        ret = true;
      }
    } else if (action == MotionEvent.ACTION_DOWN) { 
      float x0 = event.getX(0);
      float y0 = event.getY(0);
      // Log.v("DistoX-QCAM", "DOWNi " + x0 + " " + y0 );
      if ( y0 > mBorderBottom ) {
        if ( mZoomBtnsCtrlOn && x0 > mBorderInnerLeft && x0 < mBorderInnerRight ) {
          mZoomBtnsCtrl.setVisible( true );
        }
      }
    } else if (action == MotionEvent.ACTION_UP) { 
      // Log.v("DistoX-QCAM", "UP" );
    }
    return false;
  }

  @Override
  public void onVisibilityChanged(boolean visible)
  {
    if ( mZoomBtnsCtrlOn && mZoomBtnsCtrl != null ) {
      mZoomBtnsCtrl.setVisible( visible || ( TDSetting.mZoomCtrl > 1 ) );
    }
  }

  public void checkZoomBtnsCtrl()
  {
    // if ( mZoomBtnsCtrl == null ) return; // not necessary
    if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
      mZoomBtnsCtrl.setVisible( true );
    }
  }

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
    mSurface.zoom( zoomin? mDeltaZoom : -mDeltaZoom );
    mZoomTime = time;
  }


}
