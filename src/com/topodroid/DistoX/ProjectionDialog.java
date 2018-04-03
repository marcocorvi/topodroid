/* @file ProjectionDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid main drawing activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Dialog;
import android.content.Context;
// import android.content.res.Resources;

// import android.graphics.Paint;
// import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Path;

import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
// import android.view.Menu;
// import android.view.SubMenu;
// import android.view.MenuItem;
// import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
// import android.widget.LinearLayout;
// import android.widget.PopupWindow;
// import android.view.ViewGroup;
// import android.view.Display;
// import android.util.DisplayMetrics;
// import android.view.ContextMenu;
// import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
// import android.widget.ZoomControls;
// import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
// import android.widget.Toast;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import android.text.TextWatcher;
import android.text.Editable;

import java.util.List;
// import java.util.ArrayList;
import java.util.Locale;

// import java.util.concurrent.RejectedExecutionException;
// import java.util.Deque; // only API-9

// import android.util.Log;

/**
 */
class ProjectionDialog extends MyDialog
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      , OnZoomListener
                                      , IZoomer
{
  private View mZoomView;

  private TopoDroidApp mApp;
  private ShotWindow mParent;
  private DrawingUtil mDrawingUtil;

  private ProjectionSurface mDrawingSurface;
  private SeekBar mSeekBar;
  private Button  mBtnOk;
  private Button  mBtnPlus;
  private Button  mBtnMinus;
  private EditText mETazimuth;
  private DistoXNum mNum;

  private ZoomButtonsController mZoomBtnsCtrl = null;
  private boolean mZoomBtnsCtrlOn = false;
  private float oldDist;  // zoom pointer-spacing

  private int mTouchMode = DrawingWindow.MODE_MOVE;
  private float mSaveX;
  private float mSaveY;
  private float mSave0X;
  private float mSave0Y;
  private float mSave1X;
  private float mSave1Y;
  private PointF mOffset = new PointF( 0f, 0f );

  private PointF mDisplayCenter;
  private float mZoom  = 1.0f;

  private long   mSid;  // survey id
  private String mName;
  private String mFrom;
  private int  mAzimuth = 0;

  private float mBorderRight      = 4096;
  private float mBorderLeft       = 0;
  private float mBorderInnerRight = 4096;
  private float mBorderInnerLeft  = 0;
  private float mBorderBottom     = 4096;

  List<DBlock> mList = null;

  boolean mETazimuthChanged = false;

  ProjectionDialog( Context context, ShotWindow parent, long sid, String name, String from )
  {
    super( context, R.string.ProjectionDialog ); // FIXME
    mParent = parent;
    mDrawingUtil = new DrawingUtilPortrait();
    mSid    = sid;
    mName   = name;
    mFrom   = from;
    mAzimuth = 0;
    mApp     = mParent.getApp();
  }

  private void updateEditText()
  { 
    mETazimuth.setText( String.format(Locale.US, "%d", mAzimuth ) );
    // Log.v("DistoX", "set azimuth " + mAzimuth );
  }

  // -------------------------------------------------------------------
  // ZOOM CONTROLS

  public float zoom() { return mZoom; }

  @Override
  public void onVisibilityChanged(boolean visible)
  {
    if ( mZoomBtnsCtrlOn && mZoomBtnsCtrl != null ) {
      mZoomBtnsCtrl.setVisible( visible || ( TDSetting.mZoomCtrl > 1 ) );
    }
  }

  @Override
  public void onZoom( boolean zoomin )
  {
    if ( zoomin ) changeZoom( DrawingWindow.ZOOM_INC );
    else changeZoom( DrawingWindow.ZOOM_DEC );
  }

  private void changeZoom( float f ) 
  {
    float zoom = mZoom;
    mZoom     *= f;
    // Log.v( TopoDroidApp.TAG, "zoom " + mZoom );
    mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
    mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
    // Log.v("DistoX", "change zoom " + mOffset.x + " " + mOffset.y + " " + mZoom );
    mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    // mDrawingSurface.refresh();
    // mZoomCtrl.hide();
    // if ( mZoomBtnsCtrlOn ) mZoomBtnsCtrl.setVisible( false );
  }

  void zoomIn()  { changeZoom( DrawingWindow.ZOOM_INC ); }
  void zoomOut() { changeZoom( DrawingWindow.ZOOM_DEC ); }
  // public void zoomOne() { resetZoom( ); }

  // public void zoomView( )
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "zoomView ");
  //   DrawingZoomDialog zoom = new DrawingZoomDialog( mDrawingSurface.getContext(), this );
  //   zoom.show();
  // }

  // -----------------------------------------------------------------

  private void addFixedLine( DBlock blk, float x1, float y1, float x2, float y2, boolean splay )
  {
    DrawingPath dpath = null;
    if ( splay ) {
      dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
      dpath.setPaint( BrushManager.fixedShotPaint );
    } else {
      dpath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk );
      dpath.setPaint( BrushManager.labelPaint );
    }
    // mDrawingUtil.makePath( dpath, x1, y1, x2, y2, mOffset.x, mOffset.y );
    dpath.mPath = new Path();
    dpath.mPath.moveTo( x1, y1 );
    dpath.mPath.lineTo( x2, y2 );
    mDrawingSurface.addFixedPath( dpath, splay );
  }

  // --------------------------------------------------------------------------------------

  private void computeReferences( )
  {
    mDrawingSurface.clearReferences( );
    // Log.v("DistoX", "refs " + mOffset.x + " " + mOffset.y + " " + mZoom + " " + mAzimuth );

    float cosp = TDMath.cosd( mAzimuth );
    float sinp = TDMath.sind( mAzimuth );

    List< NumStation > stations = mNum.getStations();
    List< NumShot > shots       = mNum.getShots();
    List< NumSplay > splays     = mNum.getSplays();

    float h1, h2, v1, v2;
    // float dx = 0; // mOffset.x;
    // float dy = 0; // mOffset.y;
    for ( NumShot sh : shots ) {
      NumStation st1 = sh.from;
      NumStation st2 = sh.to;
      if ( st1.show() && st2.show() ) {
	float x1 = (float)( st1.e * cosp + st1.s * sinp ); // - dx;
	float x2 = (float)( st2.e * cosp + st2.s * sinp ); // - dx;
	float y1 =(float)( st1.v ); // - dy;
	float y2 = (float)( st2.v ); // - dy;
        h1 = mDrawingUtil.toSceneX( x1, y1 );
        h2 = mDrawingUtil.toSceneX( x2, y2 );
        v1 = mDrawingUtil.toSceneY( x1, y1 );
        v2 = mDrawingUtil.toSceneY( x2, y2 );
        addFixedLine( sh.getFirstBlock(), h1, v1, h2, v2, false );
      }
    } 
    for ( NumSplay sp : splays ) {
      NumStation st = sp.from;
      if ( st.show() ) {
	float x1 = (float)( st.e * cosp + st.s * sinp ); // - dx;
	float x2 = (float)( sp.e * cosp + sp.s * sinp ); // - dx;
	float y1 = (float)( st.v ); // - dy;
	float y2 = (float)( sp.v ); // - dy;
        h1 = mDrawingUtil.toSceneX( x1, y1 );
        h2 = mDrawingUtil.toSceneX( x2, y2 );
        v1 = mDrawingUtil.toSceneY( x1, y1 );
        v2 = mDrawingUtil.toSceneY( x2, y2 );
        addFixedLine( sp.getBlock(), h1, v1, h2, v2, true );
      }
    }
    for ( NumStation st : stations ) {
      if ( st.show() ) {
	float x1 = (float)( st.e * cosp + st.s * sinp ); // - dx;
	float y1 = (float)( st.v ); // - dy;
        h1 = mDrawingUtil.toSceneX( x1, y1 );
        v1 = mDrawingUtil.toSceneY( x1, y1 );
        mDrawingSurface.addDrawingStationName( st, h1, v1 );
      }
    }

    setTitle( String.format( mContext.getResources().getString(R.string.title_projection), mAzimuth ) );
  }

  // --------------------------------------------------------------

  // this method is a callback to let other objects tell the activity to use zooms or not
  private void switchZoomCtrl( int ctrl )
  {
    // Log.v("DistoX", "DEBUG switchZoomCtrl " + ctrl + " ctrl is " + ((mZoomBtnsCtrl == null )? "null" : "not null") );
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

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    mZoomBtnsCtrlOn = (TDSetting.mZoomCtrl > 1);  // do before setting content

    // Display display = getWindowManager().getDefaultDisplay();
    // DisplayMetrics dm = new DisplayMetrics();
    // display.getMetrics( dm );
    // int width = dm widthPixels;
    int width = mContext.getResources().getDisplayMetrics().widthPixels;

    // mIsNotMultitouch = ! FeatureChecker.checkMultitouch( this );

    setContentView( R.layout.projection_dialog );
    mSeekBar   = (SeekBar) findViewById(R.id.seekbar );
    mETazimuth = (EditText) findViewById( R.id.textform );
    mBtnOk     = (Button) findViewById( R.id.btn_ok );
    mBtnPlus   = (Button) findViewById( R.id.btn_plus );
    mBtnMinus  = (Button) findViewById( R.id.btn_minus );
    mBtnOk.setOnClickListener( this );
    mBtnPlus.setOnClickListener( this );
    mBtnMinus.setOnClickListener( this );
    mZoom = TopoDroidApp.mScaleFactor;    // canvas zoom

    float displayWidth = TopoDroidApp.mDisplayWidth;
    float displayHeight = TopoDroidApp.mDisplayHeight;
    
    mBorderRight  = displayWidth * 15 / 16;
    mBorderLeft   = displayWidth / 16;
    mBorderInnerRight  = displayWidth * 3 / 4;
    mBorderInnerLeft   = displayWidth / 4;
    mBorderBottom = displayHeight * 7 / 8;

    mDisplayCenter = new PointF( displayWidth / 2, displayHeight / 2);
    // Log.v("DistoX", "surface " + mOffset.x + " " + mOffset.y + " " + mZoom );

    mDrawingSurface = (ProjectionSurface) findViewById(R.id.drawingSurface);
    // mDrawingSurface.setZoomer( this );
    mDrawingSurface.setProjectionDialog( this );
    mDrawingSurface.setOnTouchListener(this);
    // mDrawingSurface.setOnLongClickListener(this);
    // mDrawingSurface.setBuiltInZoomControls(true);

    mZoomView = (View) findViewById(R.id.zoomView );
    mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
    // FIXME ZOOM_CTRL mZoomCtrl = (ZoomControls) mZoomBtnsCtrl.getZoomControls();
    // ViewGroup vg = mZoomBtnsCtrl.getContainer();
    // switchZoomCtrl( TDSetting.mZoomCtrl );

    updateEditText();

    mSeekBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
      public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
        mAzimuth = (int)( (160 + progress)%360 );
        if ( progress < 10 ) {
          seekbar.setProgress( progress + 360 );
        } else if ( progress > 390 ) {
          seekbar.setProgress( progress - 360 );
        } else {
          computeReferences();
          if ( ! mETazimuthChanged ) updateEditText();
	  mETazimuthChanged = false;
        }
        // Log.v("DistoX", "set azimuth " + mAzimuth );
      }
      public void onStartTrackingTouch(SeekBar seekbar) { }
      public void onStopTrackingTouch(SeekBar seekbar) { }
    } );

    // mETazimuth.setOnFocusChangeListener( new View.OnFocusChangeListener() {
    //   public void onFocusChange( View v, boolean b ) {
    //     if ( ! b ) { // focus lost
    //       try {
    //         setAzimuth( Integer.parseInt( mETazimuth.getText().toString() ) );
    //       } catch ( NumberFormatException e ) { }
    //     }
    //   }
    // } );

    mETazimuth.addTextChangedListener( new TextWatcher() {
      @Override
      public void afterTextChanged( Editable e ) { }

      @Override
      public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) { }

      @Override
      public void onTextChanged( CharSequence cs, int start, int before, int cnt ) 
      {
        try {
          int azimuth = Integer.parseInt( mETazimuth.getText().toString() );
          // if ( azimuth < 0 || azimuth > 360 ) azimuth = 0;
          setAzimuth( azimuth, false );
          // updateSeekBar();
          // updateView();
        } catch ( NumberFormatException e ) { }
      }
    } );

    // mETazimuth.setOnKeyListener( new View.OnKeyListener() {
    //   public boolean onKey( View v, int code, KeyEvent event )
    //   {
    //     // Log.v("DistoX", "key code " + code );
    //     if ( code == 66 /* KeyEvent.KEYCODE_ENTER */ ) {
    //       try {
    //         setAzimuth( Integer.parseInt( mETazimuth.getText().toString() ) );
    //       } catch ( NumberFormatException e ) { }
    //       return true;
    //     }
    //     return false; 
    //   }
    // } );

    doStart();
  }

  private void setAzimuth( int a, boolean edit_text )
  {
    mAzimuth = a;
    if ( mAzimuth < 0 || mAzimuth >= 360 ) { mAzimuth = 0; edit_text = true; }
    mETazimuthChanged = ! edit_text;
    computeReferences();
    mSeekBar.setProgress( ( mAzimuth < 180 )? 200 + mAzimuth : mAzimuth - 160 );
    if ( edit_text ) updateEditText();
  }

  void setSize( int w, int h )
  {
    // mOffset.x = w/2;
    // mOffset.y = h/2;
    // mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
  }

// ----------------------------------------------------------------------------

    private void doStart()
    {
      mList = TopoDroidApp.mData.selectAllShots( mSid, TDStatus.NORMAL );
      if ( mList.size() == 0 ) {
        dismiss();
        TDToast.make( mContext, R.string.few_data );
      } else {
        // float decl = mApp.mData.getSurveyDeclination( mSid );
        mNum = new DistoXNum( mList, mFrom, "", "", 0.0f );
        mSeekBar.setProgress( 200 );
        float de = - mNum.surveyEmin();
        if ( mNum.surveyEmax() > de ) de = mNum.surveyEmax();
        float ds = - mNum.surveySmin();
        if ( mNum.surveySmax() > ds ) ds = mNum.surveySmax();
        mZoom *= 2 / (float)Math.sqrt( de*de + ds*ds );
        // mOffset.x = 2 * mDisplayCenter.x; // + (mNum.surveyEmax() + mNum.surveyEmin()) * mDrawingUtil.SCALE_FIX/2;
        // mOffset.y = 2 * mDisplayCenter.y; // - (mNum.surveySmax() + mNum.surveySmin()) * mDrawingUtil.SCALE_FIX/2;
        // Log.v("DistoX", "start " + de + " " + ds + " " + dr + " off " + mOffset.x + " " + mOffset.y + " " + mZoom );

        computeReferences();
        mOffset.x = ( mNum.surveyEmax() + mNum.surveyEmin() )/ 2;
        mOffset.y = ( mNum.surveySmax() + mNum.surveySmin() )/ 2;

        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      }
   }

   private float spacing( MotionEventWrap ev )
   {
      int np = ev.getPointerCount();
      if ( np < 2 ) return 0.0f;
      float x = ev.getX(1) - ev.getX(0);
      float y = ev.getY(1) - ev.getY(0);
      return (float)Math.sqrt(x*x + y*y);
   }

   private void saveEventPoint( MotionEventWrap ev )
   {
      int np = ev.getPointerCount();
      if ( np >= 1 ) {
        mSave0X = ev.getX(0);
        mSave0Y = ev.getY(0);
        if ( np >= 2 ) {
          mSave1X = ev.getX(1);
          mSave1Y = ev.getY(1);
        } else {
          mSave1X = mSave0X;
          mSave1Y = mSave0Y;
        } 
      }
   }

    
   private void shiftByEvent( MotionEventWrap ev )
   {
      float x0 = 0.0f;
      float y0 = 0.0f;
      float x1 = 0.0f;
      float y1 = 0.0f;
      int np = ev.getPointerCount();
      if ( np >= 1 ) {
        x0 = ev.getX(0);
        y0 = ev.getY(0);
        if ( np >= 2 ) {
          x1 = ev.getX(1);
          y1 = ev.getY(1);
        } else {
          x1 = x0;
          y1 = y0;
        } 
      }
      float x_shift = ( x0 - mSave0X + x1 - mSave1X ) / 2;
      float y_shift = ( y0 - mSave0Y + y1 - mSave1Y ) / 2;
      mSave0X = x0;
      mSave0Y = y0;
      mSave1X = x1;
      mSave1Y = y1;

      moveCanvas( x_shift, y_shift );
   }

   private void moveCanvas( float x_shift, float y_shift )
   {
      if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        // Log.v("DistoX", "move canvas " + mOffset.x + " " + mOffset.y + " " + mZoom );
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        // mDrawingSurface.refresh();
      }
   }

   public void checkZoomBtnsCtrl()
   {
      // if ( mZoomBtnsCtrl == null ) return; // not necessary
      if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
        mZoomBtnsCtrl.setVisible( true );
      }
   }

   private boolean pointerDown = false;

   public boolean onTouch( View view, MotionEvent rawEvent )
   {
      checkZoomBtnsCtrl();

      MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
      // TDLog.Log( TDLog.LOG_INPUT, "DrawingWindow onTouch() " );
      // dumpEvent( event );

      int act = event.getAction();
      int action = act & MotionEvent.ACTION_MASK;
      int id = 0;

      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        mTouchMode = DrawingWindow.MODE_ZOOM;
        oldDist = spacing( event );
        saveEventPoint( event );
        pointerDown = true;
        return true;
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        int np = event.getPointerCount();
        if ( np > 2 ) return true;
        mTouchMode = DrawingWindow.MODE_MOVE;
        id = 1 - ((act & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        // int idx = rawEvent.findPointerIndex( id );
        /* fall through */
      }
      float x_canvas = event.getX(id);
      float y_canvas = event.getY(id);
      // Log.v("DistoX", "touch " + x_canvas + " " + y_canvas + " (" + mOffset.x + " " + mOffset.y + " " + mZoom + ")" );

      // ---------------------------------------- DOWN
      if (action == MotionEvent.ACTION_DOWN) {
        // TDLog.Log( TDLog.LOG_PLOT, "DOWN at X " + x_canvas + " [" +mBorderInnerLeft + " " + mBorderInnerRight + "] Y " 
        //                                          + y_canvas + " / " + mBorderBottom );
        if ( y_canvas > mBorderBottom ) {
          if ( mZoomBtnsCtrlOn && x_canvas > mBorderInnerLeft && x_canvas < mBorderInnerRight ) {
            mTouchMode = DrawingWindow.MODE_ZOOM;
            mZoomBtnsCtrl.setVisible( true );
            // mZoomCtrl.show( );
          } else if ( TDSetting.mSideDrag ) {
            mTouchMode = DrawingWindow.MODE_ZOOM;
          }
        } else if ( TDSetting.mSideDrag && ( x_canvas > mBorderRight || x_canvas < mBorderLeft ) ) {
          mTouchMode = DrawingWindow.MODE_ZOOM;
        }

        // setTheTitle( );
        mSaveX = x_canvas; // FIXME-000
        mSaveY = y_canvas;
        return false;

      // ---------------------------------------- MOVE
      } else if ( action == MotionEvent.ACTION_MOVE ) {
        if ( mTouchMode == DrawingWindow.MODE_MOVE) {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          moveCanvas( x_shift, y_shift );
          mSaveX = x_canvas; 
          mSaveY = y_canvas;
        } else { // mTouchMode == DrawingWindow.MODE_ZOOM
          float newDist = spacing( event );
          if ( newDist > 16.0f && oldDist > 16.0f ) {
            float factor = newDist/oldDist;
            if ( factor > 0.05f && factor < 4.0f ) {
              changeZoom( factor );
              oldDist = newDist;
            }
          }
          shiftByEvent( event );
        }

      // ---------------------------------------- UP

      } else if (action == MotionEvent.ACTION_UP) {
        if ( mTouchMode == DrawingWindow.MODE_ZOOM ) {
          mTouchMode = DrawingWindow.MODE_MOVE;
        } else {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
        }
      }
      return true;
   }

   @Override
   public void onClick(View view)
   {
     Button b = (Button)view;
     if ( b == mBtnOk ) {
       mDrawingSurface.stopDrawingThread();
       mParent.doProjectedProfile( mName, mFrom, mAzimuth );
       dismiss();
     } else if ( b == mBtnPlus ) {
       setAzimuth( mAzimuth + 1, true );
     } else if ( b == mBtnMinus ) {
       setAzimuth( mAzimuth - 1, true );
     }
   }

   @Override
   public void onBackPressed()
   {
     mDrawingSurface.stopDrawingThread();
     dismiss();
   }

}
