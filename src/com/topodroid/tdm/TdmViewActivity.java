/* @file TdmViewActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager main drawing activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.util.TDLocale;
import com.topodroid.util.TDLog;
import com.topodroid.util.TDVersion;
import com.topodroid.util.TDAnalytics;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MotionEventWrap;
import com.topodroid.help.HelpDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TDandroid;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TopoDroidAlertDialog;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.MyActivity;
import com.topodroid.TDX.R;

import android.os.Bundle;
import android.app.Activity;
// import android.content.Context;
// import android.content.Intent;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.pm.PackageManager;

// import android.graphics.Paint;
// import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
// import android.graphics.Path;
// import android.view.Menu;
// import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
// import android.view.ViewGroup.LayoutParams;
// import android.view.ViewGroup;
// import android.view.Display;
// import android.widget.LinearLayout;
// import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
// import android.widget.TextView;
import android.widget.ZoomControls;
// import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

// import android.util.FloatMath;
// import android.util.DisplayMetrics;

import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

/**
 */
public class TdmViewActivity extends MyActivity
                           implements View.OnTouchListener
                                      , OnZoomListener
                                      , OnClickListener
                                      , OnLongClickListener
                                      , OnItemClickListener
{

  MyHorizontalListView mListView;
  MyHorizontalButtonView mButtonView1;

  Button   mImage;
  ListView mMenu;
  ArrayAdapter<String> mMenuAdapter;
  Button[] mButton1;
  private int mButtonSize;

  private TdmViewSurface mDrawingSurface;
  private boolean mIsNotMultitouch;

  private boolean mEditMove;    // whether moving the selected point
  private int mTouchMode = MODE_MOVE;

  ZoomButtonsController mZoomBtnsCtrl;
  View mZoomView;
  ZoomControls mZoomCtrl;
  // ZoomButton mZoomOut;
  // ZoomButton mZoomIn;
  private float oldDist;  // zoom pointer-spacing

  private static final float ZOOM_INC = 1.4f;
  private static final float ZOOM_DEC = 1.0f/ZOOM_INC;

  public static final int MODE_MOVE  = 1;
  public static final int MODE_SHIFT = 2; // change point symbol position
  public static final int MODE_ZOOM  = 3;

  public int mMode   = MODE_SHIFT;
  private float mSaveX;
  private float mSaveY;
  private float mSave0X;  // first pointer saved coords
  private float mSave0Y;
  private float mSave1X;  // second pointer saved coords
  private float mSave1Y;
  // private PointF mOffset  = new PointF( 0f, 0f );
  // private PointF mOffset0 = new PointF( 0f, 0f );
  private boolean doMove = false;
  private int mNrSurveys = 0; // number of surveys

  private int mWithStation = 0;
  private TdmViewCommand mSelectedCommand = null;

  private float mTouchSlop; // pxl
  private float mTouchSlop3; // pxl

  @Override
  public void onVisibilityChanged(boolean visible)
  {
    mZoomBtnsCtrl.setVisible( visible );
  }

    @Override
    public void onZoom( boolean zoomin )
    {
      if ( zoomin ) changeZoom( ZOOM_INC );
      else changeZoom( ZOOM_DEC );
    }

    private void changeZoom( float f ) 
    {
      // TDLog.v("change zoom " + f );
      mDrawingSurface.changeZoom( f );
    }

    /** change the display-frequency of the station names
     * @param change   display-frequency change (log-2)
     */
    public void changeStationRate( int change ) 
    {
      mDrawingSurface.changeStationRate( change );
    }

    /** reset display-frequency of station names to 1
     */
    public void resetStationRate() { mDrawingSurface.resetStationRate(); }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }

    static final float SCALE_FIX = 20.0f; 

    static float worldToSceneX( float x ) { return x * SCALE_FIX; }
    static float worldToSceneY( float y ) { return y * SCALE_FIX; }

    static float sceneToWorldX( float x ) { return x/SCALE_FIX; }
    static float sceneToWorldY( float y ) { return y/SCALE_FIX; }

    void zoomFit( float width, float height )
    {
      RectF box = mDrawingSurface.getBoundingBox();
      float zw = 0.5f * TopoDroidApp.mDisplayWidth / ( box.right - box.left );
      float zh = 0.5f * TopoDroidApp.mDisplayHeight / ( box.bottom - box.top );
      float x  = ( box.right + box.left )/2;
      float y  = ( box.bottom + box.top )/2;
      // TDLog.v("BBox " + box.left + " " + box.right + "   " + box.top + " " + box.bottom + " zw " + zw );
      float z  = ( zh < zw )? zh : zw;
      mDrawingSurface.transform( width/(2.0f * z) - x, height/(2.0f * z) - y, z ); // was zoom = 1
     
      // mDrawingSurface.changeZoom( zw );
    }
    
    // --------------------------------------------------------------------------------------

    protected void setTheTitle()
    {
      // TODO
    }

    ArrayList< TdmViewCommand > getCommands() { return mDrawingSurface.mCommandManager; }

    /** @return true if the set of surveys contans a disconnected survey
     */
    private boolean isMultipleSurvey() 
    {
      HashSet< String > names = new HashSet<>();
      for ( TdmViewCommand cmd : mDrawingSurface.mCommandManager ) {
        String name = cmd.name();
        if ( names.contains( name ) ) return true;
        names.add( name );
      }
      return false;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);

      TopoDroidApp.updateAnalytic( TDAnalytics.TDM_EQ_VIEW );
      getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

      ViewConfiguration view_config = ViewConfiguration.get( this );
      mTouchSlop = 2 * view_config.getScaledTouchSlop(); // was 50
      mTouchSlop3 = mTouchSlop * 3;
      // TDLog.v("View touch slop " + mTouchSlop );

      // Display display = getWindowManager().getDefaultDisplay();
      // DisplayMetrics dm = new DisplayMetrics();
      // display.getMetrics( dm );
      // int width = dm widthPixels;
      int width  = getResources().getDisplayMetrics().widthPixels;
      int height = getResources().getDisplayMetrics().heightPixels;

      mIsNotMultitouch = ! getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );

      setContentView(R.layout.tdview_activity);
      // getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

      mDrawingSurface = (TdmViewSurface) findViewById(R.id.drawingSurface);
      mDrawingSurface.setActivity( this );
      mDrawingSurface.setOnTouchListener(this);
      // mDrawingSurface.setBuiltInZoomControls(true);
      mDrawingSurface.setDisplayCenter( width/2.0f, height/2.0f );

      if ( mIsNotMultitouch ) {
        mZoomView = (View) findViewById(R.id.zoomView );
        mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( true );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        mZoomCtrl = (ZoomControls) mZoomBtnsCtrl.getZoomControls();
        // ViewGroup vg = mZoomBtnsCtrl.getContainer();
      }

      setTheTitle();

      // Bundle extras = getIntent().getExtras();

      mListView = (MyHorizontalListView) findViewById(R.id.listview);
      resetButtonBar();

      mImage = (Button) findViewById( R.id.handle );
      mImage.setOnClickListener( this );
      TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( (TopoDroidApp)getApplication(), getResources(), R.drawable.iz_menu ) );
      mMenu = (ListView) findViewById( R.id.menu );
      mMenuAdapter = null;
      mMenu.setOnItemClickListener( this );

      doStart();
      zoomFit( width, height );
      // mDrawingSurface.transform( width/(2.0f * z), height/(2.0f * z), z ); // was zoom = 1 - moved in zoomFit
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

    @Override
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
      doStop();
    }

    private void doResume()
    {
      mDrawingSurface.setDrawing( true );
    }

    private void doPause()
    {
      if ( mIsNotMultitouch ) mZoomBtnsCtrl.setVisible(false);
      mDrawingSurface.setDrawing( false );
    }

    private void doStop()
    {
    }

  @Override public void onDestroy() { super.onDestroy(); }

  @Override public void onBackPressed() { super.onBackPressed(); }

  /** handle key up event // alternative-169
   * @param code  key code
   * @param ev    key event
   */
  @Override public boolean onKeyUp( int code, KeyEvent event )
  {
    TDLog.v("Tdm view Window key up: code " + code );
    return backKeyUp( code, event );
  }

  @Override public boolean onKeyDown( int code, KeyEvent event )
  {
    TDLog.v("Tdm view Window key down: code " + code );
    if ( code == KeyEvent.KEYCODE_BACK ) {
      return backKeyDown( code, event );
    }
    return false;
  }

// ----------------------------------------------------------------------------


    private void doStart()
    {
      ArrayList< TdmSurvey > surveys = TdmConfigActivity.mTdmConfig.getViewSurveys();
      if ( surveys == null || surveys.size() == 0 ) {
        TDToast.make( R.string.no_survey );
        return;
      }
      mNrSurveys = surveys.size();
      // TDLog.v("Tdm view activity start - config surveys " + surveys.size() );
      // TdmConfig config = mApp.mConfig;
      ArrayList< TdmEquate > equates = TdmConfigActivity.mTdmConfig.getEquates();

      // for ( TdmSurvey sr : surveys ) {
      //   TDLog.v("VIEW survey >" + sr.getFullName() + "<" );
      // }
      // for ( TdmEquate eq : equates ) {
      //   TDLog.v("VIEW equate >" + eq.stationsString() + "<");
      // }

      // TDLog.v( "TdmView nr. surveys " + surveys.size() + " equates " + equates.size() );

      // int[] color = new int[6];
      // color[0] = 0xffffffff;
      // color[1] = 0xffff00ff;
      // color[2] = 0xffffff00;
      // color[3] = 0xff00ffff;
      // color[4] = 0xffff0000;
      // color[5] = 0xff00ff00;
      // int k = 0;
      for ( TdmSurvey survey : surveys ) {
        mDrawingSurface.addTdmSurvey( survey, survey.getColor(), 0, 0, equates );
        // ++k;
      }
      updateViewEquates();
    }

    private void doSelectAt( float x_scene, float y_scene )
    {
    }

    // private void dumpEvent( MotionEventWrap ev )
    // {
    //   String[] name = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
    //   StringBuilder sb = new StringBuilder();
    //   int action = ev.getAction();
    //   int actionCode = action & MotionEvent.ACTION_MASK;
    //   sb.append( "Event action_").append( name[actionCode] );
    //   if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
    //     sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
    //   }
    //   sb.append( " [" );
    //   for (int i=0; i<ev.getPointerCount(); ++i ) {
    //     sb.append( "#" ).append( i );
    //     sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
    //     if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
    //   }
    //   sb.append( "]" );
    //   TDLog.v( "Tdm View " + sb.toString() );
    // }
    

    private float getEventPointerSpacing( MotionEventWrap ev )
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
      float x_shft = ( x0 - mSave0X + x1 - mSave1X ) / 2;
      float y_shft = ( y0 - mSave0Y + y1 - mSave1Y ) / 2;
      mSave0X = x0;
      mSave0Y = y0;
      mSave1X = x1;
      mSave1Y = y1;
    
      float zoom = mDrawingSurface.mZoom;
      if ( Math.abs( x_shft ) < 60 && Math.abs( y_shft ) < 60 ) {
        x_shft /= zoom; // add shift to offset
        y_shft /= zoom; 
        mDrawingSurface.transform( x_shft, y_shft, 1 );
      }
    }

    private static boolean mTouched = false;

    static boolean resetTouched()
    {
      boolean ret = mTouched;
      mTouched = false;
      return ret;
    }

    public boolean onTouch( View view, MotionEvent rawEvent )
    {
      // TDLog.v("onTouch enter");
      MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
      mTouched = true;
      // dumpEvent( event );

      float x_canvas = event.getX();
      float y_canvas = event.getY();

      if ( mIsNotMultitouch && y_canvas > mDrawingSurface.mHeight-20 ) {
        mZoomBtnsCtrl.setVisible( true );
        // mZoomCtrl.show( );
      }
      // TDLog.v( "touch canvas " + x_canvas + " " + y_canvas ); 
      // float x_scene = mDrawingSurface.canvasToSceneX( x_canvas );
      // float y_scene = mDrawingSurface.canvasToSceneY( y_canvas );
      // TDLog.v( "touch scene " + x_scene + " " + y_scene );

      int action = event.getAction() & MotionEvent.ACTION_MASK;

      // ---------------------------------------- DOWN
      if (action == MotionEvent.ACTION_DOWN) {
        // check if selected a station
        mSaveX = x_canvas;
        mSaveY = y_canvas;
        doMove = true;
        // TDLog.v( "DOWN at " + mSaveX + " " + mSaveY + " with station " + mWithStation );
        if ( mWithStation == 0 ) {
          boolean ret = mDrawingSurface.getSurveyAt( mSaveX, mSaveY, null, mTouchSlop ); // this uses getStationAt
          if ( ret ) {
            // TDLog.v("got survey at point");
            boolean added = false;
            if ( hasPossibeEquates() ) {
              TdmViewStation st = mDrawingSurface.selectedStation();
              List< TdmPossibleEquate > eqs = mDrawingSurface.getPossibleEquates( st );
              TDLog.v("possible eqs size " + eqs.size() );
              if ( eqs.size() > 0 ) {
                added = true;
                for ( TdmPossibleEquate eq : eqs ) {
                  final String st1 = eq.getStationFullname( 1 );
                  final String st2 = eq.getStationFullname( 2 );
                  TDLog.v("make equate <" + st1 + "> <" + st2 + ">" );
                  makeEquate( st1, st2 );
                }
              }
            }
            if ( added ) {
              // TDLog.v("compute possible equates");
              clearPossibleEquates();
              computePossibleEquates();
            } else {
              // TDLog.v("set with station 1");
              mWithStation = 1;
              mSelectedCommand = mDrawingSurface.selectedCommand();
              setTitle( TDVersion.APP_NAME_MANAGER + " " + mDrawingSurface.selectedCommandName() + " " + mDrawingSurface.selectedStationName() );
            }
          } else {
            setTitle( TDVersion.APP_NAME_MANAGER );
          }
        } else if ( mWithStation == 1 ) {
          mWithStation = 2;
        }

      // ---------------------------------------- MOVE
      } else if ( action == MotionEvent.ACTION_MOVE ) {
        // TDLog.v( "MOVE (move) to " + x_canvas + " " + y_canvas );
        if ( mTouchMode == MODE_MOVE) {
          float x_shft = x_canvas - mSaveX; // compute shift
          float y_shft = y_canvas - mSaveY;
          if ( doMove ) {
            if ( Math.abs( x_shft ) < mTouchSlop3 && Math.abs( y_shft ) < mTouchSlop3 ) {
              float zoom = mDrawingSurface.mZoom;
              x_shft /= zoom;                // add shift to offset
              y_shft /= zoom; 
              mDrawingSurface.shift( x_shft, y_shft );
              // mDrawingSurface.refresh();
              mSaveX = x_canvas; 
              mSaveY = y_canvas;
            } else {
              doMove = false;
              mDrawingSurface.clearSelectedStation();
              mWithStation = 0;
              mSelectedCommand = null;
            }
          } else {
            doMove = true;
          }
        } else { // mTouchMode == MODE_ZOOM
          float newDist = getEventPointerSpacing( event );
          // TDLog.v( "MOVE (zoom) dist " + newDist );
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
        TDLog.v( "UP withStation " + mWithStation );
        if ( mWithStation != 1 ) {
          TDLog.v(" clear selected stations" );
          mDrawingSurface.clearSelectedStation();
        }
        if ( mWithStation == 2 ) {
          mWithStation = 0;
          mSelectedCommand = null;
        }
        if ( mTouchMode == MODE_ZOOM ) {
          mTouchMode = MODE_MOVE;
        }
        // mSaveX = x_canvas; 
        // mSaveY = y_canvas;
        doMove = false;
      } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
        mTouchMode = MODE_ZOOM;
        oldDist = getEventPointerSpacing( event );
        saveEventPoint( event );
        // TDLog.v( "POINTER DOWN old dist " + oldDist );
        doMove = false;

      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        mTouchMode = MODE_MOVE;
        /* nothing */
        doMove = false;
        mSaveX = x_canvas;
        mSaveY = y_canvas;
        // TDLog.v( "POINTER UP " + mSaveX + " " + mSaveY );
      } else if ( action == MotionEvent.ACTION_CANCEL ) {
        // TDLog.v( "CANCEL action");
        doMove = false;
        mTouchMode = MODE_MOVE;
        mMode   = MODE_SHIFT;
      } else {
        // TDLog.v( "UNKNOWN action " + action );
        // return false;
      }
      // TDLog.v("onTouch return");
      return true;
    }

  // -------------------------------------------------
  boolean onMenu;
  private final static int mNrButton1 = 6;
  private final static int BTN_SURVEYS = 5;

  private final static int[] izons = { 
    R.drawable.iz_equate,
    R.drawable.iz_equate_all,
    R.drawable.iz_equates,
    R.drawable.iz_numbers_minus,
    R.drawable.iz_numbers_plus,
    R.drawable.iz_surveys,
    // R.drawable.iz_exit,
  };
  private final static int[] help_icons = {
    R.string.help_add_equate,
    R.string.help_all_equates,
    R.string.help_equates,
    R.string.help_stations_minus,
    R.string.help_stations_plus,
    R.string.help_equate_surveys,
  };

  private final static int mNrMenus   = 2;
  private final static int[] menus = { 
    // R.string.menu_equate,
    // R.string.menu_equates,
    R.string.menu_close,
    R.string.menu_help,
  };
  private static final int[] help_menus = {
    R.string.help_close,
    R.string.help_help
  };

  private static final int HELP_PAGE = R.string.TdmViewActivity;

  private void resetButtonBar()
  {
    // mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, getResources(), R.drawable.iz_menu ) );

    if ( mNrButton1 > 0 ) {
      mButtonSize = TopoDroidApp.setListViewHeight( this, mListView );
      // // MyButton.resetCache( size );
      // int size = TopoDroidApp.getScaledSize( this );
      // LinearLayout layout = (LinearLayout) findViewById( R.id.list_layout );
      // layout.setMinimumHeight( size + 40 );
      // LayoutParams lp = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
      // lp.setMargins( 10, 10, 10, 10 );
      // lp.width  = size;
      // lp.height = size;

      // FIXME TDMANAGER
      mButton1 = new Button[mNrButton1];

      for (int k=0; k<mNrButton1; ++k ) {
        mButton1[k] = MyButton.getButton( this, this, izons[k] );
        // layout.addView( mButton1[k], lp );
      }
      // mButton1[ BTN_SURVEYS ].setOnLongClickListener( this );

      mButtonView1 = new MyHorizontalButtonView( mButton1 );
      mListView.setAdapter( mButtonView1.mAdapter );
    }
  }

  private void setMenuAdapter( Resources res )
  {
    mMenuAdapter = new ArrayAdapter<String>( this, R.layout.menu );
    for ( int k=0; k<mNrMenus; ++k ) {
      mMenuAdapter.add( res.getString( menus[k] ) );  
    }
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
    if ( p++ == pos ) {        // CLOSE
      finish();
    } else if ( p++ == pos ) { // HELP
      new HelpDialog(this, this, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }

  // ---------------------------------------------------------

  private void handleEquate()
  {
    if ( mSelectedCommand == null ) {
      // TDToast.make( R.string.equate_no_station );
      // manually add equate
      if ( mNrSurveys <= 1 ) {
         /* nothing */
      // } else if ( isMultipleSurvey() ) {
      //   // TDToast.make( R.string.tdm_disconnected_survey );
      } else {
        // TDLog.v("handle equate: manually add an equate");
        new TdmEquateNewDialog( this, this, mDrawingSurface.mCommandManager ).show();
      }
    } else {
      boolean ok = false;
      TdmViewCommand cmd1 = mSelectedCommand;
      TdmViewStation vst1 = mSelectedCommand.getSelected();
      // TdmSurvey srv1 = cmd1.mSurvey;
      if ( vst1 != null ) {
        // TdmStation stn1 = vts1.mStation;
        float x = vst1.x + mSelectedCommand.mXoff;
        float y = vst1.y + mSelectedCommand.mYoff;
        // String name1 = mDrawingSurface.selectedStationName();
		String name1 = cmd1.getSelectedName();
        // TDLog.v( "handle equate: selected station " + vst1.x + " " + vst1.y + " point " + x + " " + y + " name " + name1 );
        if ( name1 != null ) {
          // final String st1 = name1 + "@" + mDrawingSurface.selectedCommandName();
          final String st1 = name1 + "@" + cmd1.name();
		  // TDLog.v( "handle equate: equate " + st1 + " with ... " );
          // boolean tried_equate = false;
          List< TdmViewCommand > cmds = mDrawingSurface.getAllSelectedCommands();
          for ( TdmViewCommand cmd : cmds ) {
            if ( cmd == cmd1 ) continue;
            // String name2 = mDrawingSurface.selectedStationName( cmd );
			String name2 = cmd.getSelectedName();
            if ( name2 != null ) {
              final String st2 = name2 + "@" + cmd.name();
              // TDLog.v( "handle equate: equate with " + st2 );
              String title = String.format( getResources().getString( R.string.title_equate_with ), st1, st2 );
              TopoDroidAlertDialog.makeAlert( this, getResources(), title, 
                new DialogInterface.OnClickListener() {
                  @Override public void onClick( DialogInterface dialog, int btn ) {
                    makeEquate( st1, st2 );
                  }
                }
              );
              ok = true; // a station to equate with has been found and the user has been asked
            }
          }
          mDrawingSurface.clearSelectedStation();

          // ArrayList< TdmViewCommand > excluded_cmd = new ArrayList<>();
          // excluded_cmd.add( mSelectedCommand );
          // while ( mDrawingSurface.getSurveyAt( x, y, excluded_cmd, mTouchSlop/2 ) ) {
          //   TdmViewCommand cmd = mDrawingSurface.selectedCommand();
          //   if ( cmd == null ) break;
          //   tried_equate = true;
          //   excluded_cmd.add( cmd );
          //   String name2 = mDrawingSurface.selectedStationName();
          //   if ( name2 != null ) {
          //     final String st2 = name2 + "@" + mDrawingSurface.selectedCommandName();
          //     TDLog.v( "Equate " + st1 + " with " + st2 );
          //     String title = String.format( getResources().getString( R.string.title_equate_with ), st1, st2 );
          //     TopoDroidAlertDialog.makeAlert( this, getResources(), title, 
          //       new DialogInterface.OnClickListener() {
          //         @Override public void onClick( DialogInterface dialog, int btn ) {
          //           makeEquate( st1, st2 );
          //         }
          //       }
          //     );
          //   }
          // }
          // if ( tried_equate ) {
          //   TDLog.v("tried equate: clear selected stations" );
          //   mDrawingSurface.clearSelectedStation();
          // }
        }
      }
      if ( ! ok ) { 
        TDToast.make( R.string.equate_no_nearby );
      }
    }
  }

  /** create an equate between two stations
   * @param st1  first station fullname
   * @param st2  second station
   */
  void makeEquate( String st1, String st2 )
  {
    if ( TdmConfigActivity.mTdmConfig.hasEquate( st1, st2 ) ) {
      TDToast.make(  R.string.equate_exists );
      return;
    }
    TdmEquate equate = new TdmEquate();
    equate.addStation( st1 );
    equate.addStation( st2 );
    // TDLog.v( "add equate: " + equate.stationsString() );
    TdmConfigActivity.mTdmConfig.addEquate( equate );
    updateViewEquates();
  }

  void makeEquate( List< String > sts )
  {
    if ( sts.size() <= 1 ) {
      TDToast.make( R.string.equate_no_stations );
      return;
    }
    if ( TdmConfigActivity.mTdmConfig.hasEquate( sts ) ) {
      TDToast.make(  R.string.equate_exists );
      return;
    }
    TdmEquate equate = new TdmEquate();
    for ( String st : sts ) equate.addStation( st );
    // TDLog.v( "add equate: " + equate.stationsString() );
    TdmConfigActivity.mTdmConfig.addEquate( equate );
    updateViewEquates();
  }

  void updateViewEquates()
  {
    mDrawingSurface.addViewEquates( TdmConfigActivity.mTdmConfig.getEquates() );
  }

  @Override
  public boolean onLongClick( View view ) 
  {
    Button b0 = (Button)view;
    if ( b0 == mButton1[5] ) {
      showAllSurveys();
      return true;
    }
    return false;
  }

  @Override
  public void onClick( View view )
  { 
    if ( onMenu ) {
      closeMenu();
      return;
    }
    Button b0 = (Button)view;

    if ( b0 == mImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    }
    int k1 = 0;
    if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // EQUATE
      handleEquate();
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // ALL POSSIBLE EQUATES
      if ( hasPossibeEquates() ) {
        clearPossibleEquates();
      } else {
        computePossibleEquates();
      }
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // SHOW EQUATES
      if ( TdmConfigActivity.mTdmConfig.hasEquates() ) {
        (new TdmEquatesDialog( this, TdmConfigActivity.mTdmConfig, this )).show();
      } else {
        TDToast.make( R.string.no_equate );
      }
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // FEWER STATIONS
      changeStationRate( -1 );
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // MORE STATIONS
      changeStationRate( 1 );
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // SURVEY STATIONS
      makeSurveysDialog();
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // EXIT
      finish();
    }
  }


  @Override
  public void onItemClick( AdapterView<?> parent, View view, int pos, long id )
  {
    // CharSequence item = ((TextView) view).getText();
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( onMenu ) {
      closeMenu();
      // return;
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
  }

  // POSSIBLE EQUATES -----------------------------------------------
  /** clear the list of possible equates
   */
  void clearPossibleEquates() { mDrawingSurface.clearPossibleEquates(); }

  boolean addPossibleEquate( TdmViewStation st1, TdmViewStation st2 ) { return mDrawingSurface.addPossibleEquate( st1, st2 ); }

  boolean hasPossibeEquates() { return mDrawingSurface.hasPossibeEquates(); }

  
  void makeSurveysDialog()
  {
    ArrayList< TdmViewCommand > commands = getCommands();
    int nr_surveys = commands.size();
    if ( nr_surveys <= 1 ) return;
    ArrayList< String > surveys = new ArrayList<>();
    for ( int i=0; i < nr_surveys; ++i ) {
      TdmViewCommand cmd = commands.get(i);
      if ( cmd.isParentRoot() ) {
        surveys.add( cmd.name() );
      }
    }
    (new TdmSurveysDialog( this, this, surveys )).show();
  }

  /** show the stations of onle one survey
   * @param name  survey name
   */
  void showOnlySurvey( String name )
  {
    ArrayList< TdmViewCommand > commands = getCommands();
    for ( TdmViewCommand cmd : commands ) {
      cmd.setShowStations( cmd.name().equals( name ) );
    }
  }

  /** show stations of all surveys
   */
  void showAllSurveys()
  {
    ArrayList< TdmViewCommand > commands = getCommands();
    for ( TdmViewCommand cmd : commands ) {
      cmd.setShowStations( true );
    }
  }

    
  /** compute possible equates:
   * a possible equate is an equate between stations of two surveys with the same name
   */
  boolean computePossibleEquates()
  {
    mDrawingSurface.clearPossibleEquates(); // necessary for recompute
    ArrayList< TdmViewCommand > commands = getCommands();
    int nr_surveys = commands.size();
    if ( nr_surveys <= 1 ) return false;
    for ( int i=0; i < nr_surveys; ++i ) {
      TdmViewCommand cmd1 = commands.get(i);
      // List< TdmViewStation > stations1 = cmd1.mStations;
      for ( int j=i+1; j < nr_surveys; ++j ) {
        TdmViewCommand cmd2 = commands.get(j);
        // skip if surveys have an equate
        if ( mDrawingSurface.hasEquated( cmd1, cmd2 ) ) continue;
        // List< TdmViewStation > stations2 = cmd2.mStations;
        // ArrayList< TdmViewStation > equated = new ArrayList<>();
        // for ( TdmViewStation st1 : stations1 )
		HashSet< TdmViewStation > equated = new HashSet<>();
        for ( TdmViewStation st1 : cmd1.mStations ) {
          if ( st1.mEquated ) continue; // skip equated stations
          // String name = st1.name();
          // for ( TdmViewStation st2 : stations2 ) {
          //   if ( st2.mEquated ) continue;
          //   if ( equated.contains( st2 ) ) continue;
          //   if ( st2.name().equals( name ) ) {
          //     if ( mDrawingSurface.addPossibleEquate( st1, st2 ) ) {
          //       equated.add( st2 );
          //       break;
          //     }
          //   }
		  TdmViewStation st2 = cmd2.getViewStation( st1.name() );
          if ( st2 == null || st2.mEquated ) continue;
          if ( equated.contains( st2 ) ) continue;
          if ( mDrawingSurface.addPossibleEquate( st1, st2 ) ) {
            equated.add( st2 );
          } // stations in survey j
        } // stations in survey i
      } // survey j
    } // survey i
    // TDLog.v("Possible equates " + mDrawingSurface.nrPossibleEquates() );
    return mDrawingSurface.hasPossibeEquates();
  }
  

  // ----------------------------------------------------------------
  // TITLE BAR

  @Override
  public void setTitle( CharSequence t )
  {
    ((TextView)findViewById( R.id.title )).setText( t );
  }

  @Override
  public void setTitleColor( int color )
  {
    ((TextView)findViewById( R.id.title )).setTextColor( color );
  }
}
