/* @file PlotReloadWindow.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketch overview activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDLocale;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MotionEventWrap;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;

// import android.util.TypedValue;

import android.graphics.PointF;
// import android.graphics.Path;
// import android.graphics.drawable.BitmapDrawable;
// import android.graphics.Matrix;

import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;

import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;

import android.widget.Button;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

// import java.io.File; // TDR FILE
import java.util.ArrayList;

public class PlotReloadWindow extends ItemDrawer
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      , OnItemClickListener
                                      , OnZoomListener
                                      , IZoomer
				      // , IExporter
{
  private static final int[] izons = {
                        R.drawable.iz_back,       // 0
                        R.drawable.iz_forw,          // 1
                        R.drawable.iz_reload,
			R.drawable.iz_empty
                      };
  // FIXME_OVER private static int BTN_PLOT = 2;

  private static final int[] menus = {
                        R.string.menu_close,
                        // R.string.menu_reload,
                        R.string.menu_help
                     };

  private static final int[] help_icons = {
                        R.string.help_reload_prev,
                        R.string.help_reload_next,
                        R.string.help_reload_plot
                      };
  private static final int[] help_menus = {
                        R.string.help_close,
			// R.string.help_reload_plot,
                        R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.PlotReloadWindow;

  // FIXME_OVER BitmapDrawable mBMextend;
  // FIXME_OVER BitmapDrawable mBMplan;

  private float mDDtotal = 0;
  private int mTotal = 0;

  private TopoDroidApp mApp;
  // private DrawingUtil mDrawingUtil;
  private String mFilename;

  // long getSID() { return TDInstance.sid; }
  // String getSurvey() { return TDInstance.survey; }
  // private static BezierInterpolator mBezierInterpolator = new BezierInterpolator();
  private DrawingSurface  mReloadSurface;

  private boolean mZoomBtnsCtrlOn = false;
  private ZoomButtonsController mZoomBtnsCtrl = null;
  private View mZoomView;

  private static final float ZOOM_INC = 1.4f;
  private static final float ZOOM_DEC = 1.0f/ZOOM_INC;

  static final private int MODE_MOVE = 2;
  static final private int MODE_ZOOM = 4;

  private int mTouchMode = MODE_MOVE;

  private float mSaveX;
  private float mSaveY;
  private PointF mOffset  = new PointF( 0f, 0f );
  private PointF mDisplayCenter;
  private float mZoom  = 1.0f;

  private long mSid;     // survey id
  private long mType;    // current plot type
  private boolean mLandscape;

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
    if ( zoomin ) changeZoom( ZOOM_INC );
    else changeZoom( ZOOM_DEC );
  }

  private void changeZoom( float f ) 
  {
    float zoom = mZoom;
    mZoom     *= f;
    //  TDLog.v( "zoom " + mZoom );
    mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
    mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
    mReloadSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    // mReloadSurface.refresh();
    // mZoomCtrl.hide();
    // mZoomBtnsCtrl.setVisible( false );
  }

  public void zoomIn()  { changeZoom( ZOOM_INC ); }
  public void zoomOut() { changeZoom( ZOOM_DEC ); }
  // public void zoomOne() { resetZoom( ); }

  // --------------------------------------------------------------------------------------
  class PlotBackup
  {
    String tdr;
    String desc; // (bck) age [size]
    String filename;

    PlotBackup( String t, String d, String f ) 
    { 
      tdr = t;
      desc = d;
      filename = f;
    }
  }

  private ArrayList< PlotBackup > mBackups;
  private int mPos = 0;
    
  // @param filename    survey-1p without ".tdr"
  private void populateBackups( String filename )
  {
    mBackups = new ArrayList< PlotBackup >();
    mPos = 0;

    String tdr = TDPath.getTdrFileWithExt( filename );   
    String filetdr = filename + ".tdr";

    long millis = System.currentTimeMillis();
    String age = TDFile.getTopoDroidFileAgeLength( tdr, millis );
    if ( age != null ) {
      String name = age + " ()";
      mBackups.add( new PlotBackup( tdr, name, filetdr ) );
    }
    tdr = tdr + TDPath.BCK_SUFFIX;
    filetdr = filetdr + TDPath.BCK_SUFFIX;
    age = TDFile.getTopoDroidFileAgeLength( tdr, millis );
    if ( age != null ) {
      String name = age + " (" + TDPath.BCK_SUFFIX + ")";
      mBackups.add( new PlotBackup( tdr, name, filetdr ) );
    }
    for ( int i=0; i< TDPath.NR_BACKUP; ++i ) {
      String tdr1 = tdr + i;
      age = TDFile.getTopoDroidFileAgeLength( tdr1, millis );
      if ( age != null ) {
        String name = age + " (" +  TDPath.BCK_SUFFIX + i + ")";
        String filetdr1 = filetdr + i;
        mBackups.add( new PlotBackup( tdr1, name, filetdr1 ) );
      }
    }
  }

  private void computeReferences( int type, float zoom )
  {
    mReloadSurface.setManager( DrawingSurface.DRAWING_OVERVIEW, type ); 
    mReloadSurface.addScaleRef( DrawingSurface.DRAWING_OVERVIEW, type );
  }
    
  // ------------------------------------------------------------------------------
  // BUTTON BAR
  
  private Button[] mButton1;  // primary
  private int mNrButton1 = 3; // main-primary
  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;
  private ListView   mMenu;
  private Button     mImage;
  private boolean onMenu;

  public float zoom() { return mZoom; }

  // this method is a callback to let other objects tell the activity to use zooms or not
  private void switchZoomCtrl( int ctrl )
  {
    // TDLog.v( "DEBUG switchZoomCtrl " + ctrl + " ctrl is " + ((mZoomBtnsCtrl == null )? "null" : "not null") );
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

    TDandroid.setScreenOrientation( this );

    // Display display = getWindowManager().getDefaultDisplay();
    // DisplayMetrics dm = new DisplayMetrics();
    // display.getMetrics( dm );
    // int width = dm widthPixels;
    int width = getResources().getDisplayMetrics().widthPixels;

    setContentView(R.layout.overview_activity);
    mApp = (TopoDroidApp)getApplication();
    mActivity = this;
    Resources res = getResources();
    // mZoom = mApp.mScaleFactor;    // canvas zoom

    mDisplayCenter = new PointF(TopoDroidApp.mDisplayWidth  / 2, TopoDroidApp.mDisplayHeight / 2);

    mReloadSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
    mReloadSurface.setZoomer( this );
    mReloadSurface.setOnTouchListener(this);

    mZoomView = (View) findViewById(R.id.zoomView );
    mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );

    switchZoomCtrl( TDSetting.mZoomCtrl );

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlaceholder( true );
    /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    mButton1 = new Button[ mNrButton1 + 1 ];
    for ( int k=0; k < mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
    }
    mButton1[mNrButton1] = MyButton.getButton( mActivity, this, R.drawable.iz_empty );

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    Bundle extras = getIntent().getExtras();
    mSid          = extras.getLong( TDTag.TOPODROID_SURVEY_ID );
    mZoom         = extras.getFloat( TDTag.TOPODROID_PLOT_ZOOM );
    mType         = (int)extras.getLong( TDTag.TOPODROID_PLOT_TYPE );
    mLandscape    = extras.getBoolean( TDTag.TOPODROID_PLOT_LANDSCAPE );
    String filename = extras.getString( TDTag.TOPODROID_PLOT_FILENAME ); // survey-1p without ".tdr"
    populateBackups( filename );
    // // mDrawingUtil = mLandscape ? (new DrawingUtilLandscape()) : (new DrawingUtilPortrait());
    // mDrawingUtil = new DrawingUtilPortrait();

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );
    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );
    
    //  TDLog.v("Reload " + filename + " offset " + mOffset.x + " " + mOffset.y );
    doStart();

    mOffset.x   += extras.getFloat( TDTag.TOPODROID_PLOT_XOFF );
    mOffset.y   += extras.getFloat( TDTag.TOPODROID_PLOT_YOFF );
    mReloadSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
  }

  @Override
  protected synchronized void onResume()
  {
    super.onResume();
    doResume();
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    doPause();
  }

  @Override // only calls super method
  protected synchronized void onStart()
  {
    super.onStart();
    TDLocale.resetTheLocale();
    setMenuAdapter( getResources() );
    closeMenu();
  }

  @Override
  protected synchronized void onStop()
  {
    super.onStop();
    mReloadSurface.setDisplayMode( mReloadSurface.getDisplayMode() & DisplayMode.DISPLAY_OVERVIEW );
  }

  private void doResume()
  {
    // PlotInfo info = mApp.mData.getPlotInfo( mSid, mName );
    // mOffset.x = info.xoffset;
    // mOffset.y = info.yoffset;
    // mZoom     = info.zoom;
    mReloadSurface.isDrawing = true;
    switchZoomCtrl( TDSetting.mZoomCtrl );
  }

  private void doPause()
  {
    switchZoomCtrl( 0 );
    mReloadSurface.isDrawing = false;
  }

//---------------------------------------------------------------------------

  private void doStart()
  {
    // TDLog.Log( TDLog.LOG_PLOT, "do Start " + mName1 + " " + mName2 );
    // mBlockList = mData.selectAllLegShots( mSid, TDStatus.NORMAL );
    mPos = 0;
    loadFile( ); 
  }

  // boolean mAllSymbols = true;

  private void loadFile( )
  {
    if ( mPos < 0 || mPos >= mBackups.size() ) return;
    PlotBackup plot = mBackups.get( mPos );
    mReloadSurface.resetManager( DrawingSurface.DRAWING_OVERVIEW, null, false ); // is_extended = false
    setTitle( plot.desc );
    // TDLog.v("Reload file pos " + mPos + " " + plot.tdr );
    mReloadSurface.addLoadDataStream( plot.tdr, 0, 0, plot.filename ); // save plot name in paths
  } 

  private float mSave0X, mSave0Y;
  private float mSave1X, mSave1Y;

  /*
  private void dumpEvent( MotionEventWrap ev )
  {
    String name[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
    StringBuilder sb = new StringBuilder();
    int action = ev.getAction();
    int actionCode = action & MotionEvent.ACTION_MASK;
    sb.append( "Event action_").append( name[actionCode] );
    if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
      sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
    }
    sb.append( " [" );
    for (int i=0; i<ev.getPointerCount(); ++i ) {
      sb.append( "#" ).append( i );
      sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
      if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
    }
    sb.append( "]" );
    // TDLog.Log(TDLog.LOG_PLOT, sb.toString() );
  }
  */

  private float spacing(MotionEventWrap ev)
  {
    int np = ev.getPointerCount();
    if ( np < 2 ) return 0.0f;
    float x = ev.getX(1) - ev.getX(0);
    float y = ev.getY(1) - ev.getY(0);
    return TDMath.sqrt(x*x + y*y);
  }

  private void saveEventPoint(MotionEventWrap ev)
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

  
  private void shiftByEvent(MotionEventWrap ev)
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
  
    if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
      mOffset.x += x_shift / mZoom;                // add shift to offset
      mOffset.y += y_shift / mZoom; 
      mReloadSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    }

  }

  private void moveCanvas( float x_shift, float y_shift )
  {
    if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
      mOffset.x += x_shift / mZoom;                // add shift to offset
      mOffset.y += y_shift / mZoom; 
      mReloadSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
      // mReloadSurface.refresh();
    }
  }


  private float oldDist = 0;
  private float mStartX = 0;
  private float mStartY = 0;
  private float mBaseX = 0;
  private float mBaseY = 0;
  
  private float deltaX( float x1, float x0 ) 
  { return (  (x1 - x0) / DrawingUtil.SCALE_FIX ) / TDSetting.mUnitGrid; }

  private float deltaY( float y1, float y0 )
  { return ( -(y1 - y0) / DrawingUtil.SCALE_FIX ) / TDSetting.mUnitGrid; }

  private double angleBase( float bx, float by )
  {
    double ba = Math.atan2( bx, by ) * 180 / Math.PI;
    if ( ba < 0 ) ba += 360;

    if ( mType == PlotType.PLOT_PLAN ) {
      /* nothing */
    } else {
      if ( ba <= 180 ) {
        ba = 90 - ba;
      } else {
        ba = ba - 270;
      } 
    }
    ba *= TDSetting.mUnitAngle;
    return ba;
  }

  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    float d0 = TDSetting.mCloseCutoff + TDSetting.mSelectness / mZoom;
    checkZoomBtnsCtrl();

    MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingWindow onTouch() " );
    // dumpEvent( event );

    float x_canvas = event.getX();
    float y_canvas = event.getY();
    // TDLog.v( "touch canvas " + x_canvas + " " + y_canvas ); 

    if ( mZoomBtnsCtrlOn && y_canvas > DrawingUtil.CENTER_Y*2-20 ) {
      mZoomBtnsCtrl.setVisible( true );
      // mZoomCtrl.show( );
    }
    float x_scene = x_canvas/mZoom - mOffset.x;
    float y_scene = y_canvas/mZoom - mOffset.y;
    // TDLog.v( "touch scene " + x_scene + " " + y_scene );

    int action = event.getAction() & MotionEvent.ACTION_MASK;

    if (action == MotionEvent.ACTION_POINTER_DOWN) {
      mTouchMode = MODE_ZOOM;
      oldDist = spacing( event );
      saveEventPoint( event );
    } else if ( action == MotionEvent.ACTION_POINTER_UP) {
      mTouchMode = MODE_MOVE;
      /* nothing */

    // ---------------------------------------- DOWN

    } else if (action == MotionEvent.ACTION_DOWN) {
      // check side-drag and zoom controls
      if ( y_canvas > TopoDroidApp.mBorderBottom ) {
        if ( mZoomBtnsCtrlOn && x_canvas > TopoDroidApp.mBorderInnerLeft && x_canvas < TopoDroidApp.mBorderInnerRight ) {
          mTouchMode = MODE_ZOOM;
          mZoomBtnsCtrl.setVisible( true );
          // mZoomCtrl.show( );
          return true;
        } else if ( TDSetting.mSideDrag ) {
          mTouchMode = MODE_ZOOM;
          return true;
        }
      } else if ( TDSetting.mSideDrag && ( x_canvas > TopoDroidApp.mBorderRight || x_canvas < TopoDroidApp.mBorderLeft ) ) {
        mTouchMode = MODE_ZOOM;
        return true;
      }

      mSaveX = x_canvas; // FIXME-000
      mSaveY = y_canvas;
    // ---------------------------------------- MOVE

    } else if ( action == MotionEvent.ACTION_MOVE ) {
      if ( mTouchMode == MODE_MOVE) {
        float x_shift = x_canvas - mSaveX; // compute shift
        float y_shift = y_canvas - mSaveY;
        if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
          mOffset.x += x_shift / mZoom;                // add shift to offset
          mOffset.y += y_shift / mZoom; 
          mReloadSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
        }
        mSaveX = x_canvas; 
        mSaveY = y_canvas;
      } else { // mTouchMode == MODE_ZOOM
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
      if ( onMenu ) {
        closeMenu();
        return true;
      }

      if ( mTouchMode == MODE_ZOOM ) {
        mTouchMode = MODE_MOVE;
      // } else {
        // NOTHING
        // if ( mOnMeasure == MEASURE_OFF ) {
        //   // float x_shift = x_canvas - mSaveX; // compute shift
        //   // float y_shift = y_canvas - mSaveY;
        // } else {
        // }
      }
    }
    return true;
  }


  public void onClick(View view)
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }

    Button b = (Button)view;
    if ( b == mImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    }
    if ( b == mButton1[0] ) { // prev
      if ( mPos > 0 ) { 
        --mPos;
        loadFile();
      }
    } else if ( b == mButton1[1] ) { // next
      if ( mPos < mBackups.size() - 1 ) {
        ++mPos;
        loadFile();
      }
    } else if ( b == mButton1[2] ) { // reload plot
      doReloadPlot();
    }
  }


  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWARE MENU (82)
        UserManualActivity.showHelpPage( mActivity, getResources().getString( HELP_PAGE ));
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  @Override
  public void onBackPressed()
  {
    // set activity result FAILURE and return
    setResult( RESULT_CANCELED );
    finish();
  }


  private void setMenuAdapter( Resources res )
  {
    ArrayAdapter< String > mMenuAdapter = new ArrayAdapter<>(mActivity, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    // mMenuAdapter.add( res.getString( menus[2] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) { // CLOSE
      onBackPressed();
    // } else if ( p++ == pos ) { // RELOAD : set activity result and return
    //   doReloadPlot();
    } else if ( p++ == pos ) { // HELP
      new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }
 
  private void doReloadPlot()
  {
    Intent intent = new Intent();
    intent.putExtra( TDTag.TOPODROID_PLOT_TYPE, mType );
    intent.putExtra( TDTag.TOPODROID_PLOT_FILENAME, mBackups.get(mPos).filename );
    setResult( RESULT_OK, intent );
    finish();
  }


  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
    }
  }

  public void checkZoomBtnsCtrl()
  {
    if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
      mZoomBtnsCtrl.setVisible( true );
    }
  }


  /** react to a change in the configuration
   * @param new_cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    TDLocale.resetTheLocale();
    mReloadSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
  }

}
