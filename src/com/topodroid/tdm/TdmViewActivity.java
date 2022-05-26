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

import com.topodroid.utils.TDLocale;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MotionEventWrap;
import com.topodroid.help.HelpDialog;
import com.topodroid.TDX.TDandroid;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TopoDroidAlertDialog;
import com.topodroid.TDX.TDToast;
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
// import android.graphics.Path;
// import android.view.Menu;
// import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;
// import android.view.ViewGroup;
// import android.view.Display;
// import android.widget.LinearLayout;
// import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.ArrayList;

/**
 */
public class TdmViewActivity extends Activity
                           implements View.OnTouchListener
                                      , OnZoomListener
                                      , OnClickListener
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
  private PointF mOffset  = new PointF( 0f, 0f );
  // private PointF mOffset0 = new PointF( 0f, 0f );
  private boolean doMove = false;

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
      mDrawingSurface.changeZoom( f );
    }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }

    static final float SCALE_FIX = 20.0f; 

    static float worldToSceneX( float x ) { return x * SCALE_FIX; }
    static float worldToSceneY( float y ) { return y * SCALE_FIX; }

    static float sceneToWorldX( float x ) { return x/SCALE_FIX; }
    static float sceneToWorldY( float y ) { return y/SCALE_FIX; }

    
    // --------------------------------------------------------------------------------------

    protected void setTheTitle()
    {
    }

    ArrayList< TdmViewCommand > getCommands() { return mDrawingSurface.mCommandManager; }

    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);

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

      Bundle extras = getIntent().getExtras();

      mListView = (MyHorizontalListView) findViewById(R.id.listview);
      resetButtonBar();

      mImage = (Button) findViewById( R.id.handle );
      mImage.setOnClickListener( this );
      TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( (TopoDroidApp)getApplication(), getResources(), R.drawable.iz_menu ) );
      mMenu = (ListView) findViewById( R.id.menu );
      mMenuAdapter = null;
      mMenu.setOnItemClickListener( this );

      doStart();
      mDrawingSurface.transform( width/2.0f, height/2.0f, 1 );
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
      mDrawingSurface.isDrawing = true;
    }

    private void doPause()
    {
      if ( mIsNotMultitouch ) mZoomBtnsCtrl.setVisible(false);
      mDrawingSurface.isDrawing = false;
    }

    private void doStop()
    {
    }

// ----------------------------------------------------------------------------


    private void doStart()
    {
      ArrayList< TdmSurvey > surveys = TdmConfigActivity.mTdmConfig.getViewSurveys();
      if ( surveys == null ) return;
      // TdmConfig config = mApp.mConfig;
      ArrayList< TdmEquate > equates = TdmConfigActivity.mTdmConfig.getEquates();

      // TDLog.v( "TdmView nr. surveys " + surveys.size() + " equates " + equates.size() );

      int[] color = new int[6];
      color[0] = 0xffffffff;
      color[1] = 0xffff00ff;
      color[2] = 0xffffff00;
      color[3] = 0xff00ffff;
      color[4] = 0xffff0000;
      color[5] = 0xff00ff00;
      int k = 0;
      for ( TdmSurvey survey : surveys ) {
        mDrawingSurface.addSurvey( survey, color[k%6], 0, 0, equates );
        ++k;
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
    

    float spacing( MotionEventWrap ev )
    {
      int np = ev.getPointerCount();
      if ( np < 2 ) return 0.0f;
      float x = ev.getX(1) - ev.getX(0);
      float y = ev.getY(1) - ev.getY(0);
      return (float)Math.sqrt(x*x + y*y);
    }

    void saveEventPoint( MotionEventWrap ev )
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

    
    void shiftByEvent( MotionEventWrap ev )
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
    
      float zoom = mDrawingSurface.mZoom;
      if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
        x_shift /= zoom;               // add shift to offset
        y_shift /= zoom; 
        mDrawingSurface.transform( x_shift, y_shift, 1 );
      }
    }


    int mWithStation = 0;
    TdmViewCommand mSelectedCommand = null;

    public boolean onTouch( View view, MotionEvent rawEvent )
    {
      MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
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

      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        mTouchMode = MODE_ZOOM;
        oldDist = spacing( event );
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

      // ---------------------------------------- DOWN
      } else if (action == MotionEvent.ACTION_DOWN) {
        // check if selected a station
        mSaveX = x_canvas;
        mSaveY = y_canvas;
        doMove = true;
        if ( mWithStation == 0 ) {
          boolean ret = mDrawingSurface.getSurveyAt( mSaveX, mSaveY, null );
          // TDLog.v( "DOWN at " + mSaveX + " " + mSaveY + " at " + ret );
          if ( ret ) {
            mWithStation = 1;
            mSelectedCommand = mDrawingSurface.selectedCommand();
            setTitle( "TopoDroid Manager " + mDrawingSurface.selectedCommandName() + " " + mDrawingSurface.selectedStationName() );
          } else {
            setTitle( "TopoDroid Manager" );
          }
        } else if ( mWithStation == 1 ) {
          mWithStation = 2;
        }

      // ---------------------------------------- MOVE
      } else if ( action == MotionEvent.ACTION_MOVE ) {
        if ( mTouchMode == MODE_MOVE) {
          // TDLog.v( "MOVE (move) to " + x_canvas + " " + y_canvas );
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          if ( doMove ) {
            if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
              float zoom = mDrawingSurface.mZoom;
              x_shift /= zoom;                // add shift to offset
              y_shift /= zoom; 
              mDrawingSurface.shift( x_shift, y_shift );
              // mDrawingSurface.refresh();
              mSaveX = x_canvas; 
              mSaveY = y_canvas;
            }
          }
          doMove = true;
        } else { // mTouchMode == MODE_ZOOM
          float newDist = spacing( event );
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
        // TDLog.v( "UP");
        if ( mWithStation == 2 ) {
          mDrawingSurface.resetStation();
          mWithStation = 0;
          mSelectedCommand = null;
        }
        if ( mTouchMode == MODE_ZOOM ) {
          mTouchMode = MODE_MOVE;
        }
        // mSaveX = x_canvas; 
        // mSaveY = y_canvas;
        doMove = false;
      }
      return true;
    }

  // -------------------------------------------------
  boolean onMenu;
  int mNrButton1 = 2;
  private static int[] izons = { 
    R.drawable.iz_equate,
    R.drawable.iz_equates,
    // R.drawable.iz_exit,
  };
  private static final int[] help_icons = {
    R.string.help_add_equate,
    R.string.help_equates,
  };

  int mNrMenus   = 2;
  private static int[] menus = { 
    // R.string.menu_equate,
    // R.string.menu_equates,
    R.string.menu_close,
    R.string.menu_help,
  };
  private static final int[] help_menus = {
    R.string.help_close,
    R.string.help_help
  };

  private static final int HELP_PAGE = R.string.TdmConfigWindow;

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
      new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }

  // ---------------------------------------------------------

  private void handleEquate()
  {
    if ( mSelectedCommand == null ) {
      // TDToast.make( R.string.equate_no_station );
      // manually add equate
      new TdmEquateNewDialog( this, this, mDrawingSurface.mCommandManager ).show();
    } else {
      // TdmViewCommand cmd1 = mSelectedCommand;
      // TdmSurvey srv1 = cmd1.mSurvey;
      TdmViewStation vst1 = mSelectedCommand.mSelected;
      // TdmStation stn1 = vts1.mStation;
      float x = vst1.x + mSelectedCommand.mXoff;
      float y = vst1.y + mSelectedCommand.mYoff;
      // TDLog.v( "selected station " + vst1.x + " " + vst1.y + " point " + x + " " + y );

      String st = mDrawingSurface.selectedStationName() + "@" + mDrawingSurface.selectedCommandName();
      int len = st.length();
      while ( len > 0 && st.charAt( len - 1 ) == '.' ) -- len;
      final String st1 = st.substring(0,len);
      if ( mDrawingSurface.getSurveyAt( x, y, mSelectedCommand ) ) {
        // TdmViewCommand cmd2 = mDrawingSurface.selectedCommand();
        // TdmSurvey srv2 = cmd2.mSurvey;
        // TdmViewStation vst2 = mDrawingSurface.selectedStation();
        // TdmStation stn2 = vts2.mStation;
        st = mDrawingSurface.selectedStationName() + "@" + mDrawingSurface.selectedCommandName();
        len = st.length();
        while ( len > 0 && st.charAt( len - 1 ) == '.' ) -- len;
        final String st2 = st.substring(0,len);

        // String title = "Equate " + st1 + " with " + st2;
        String title = String.format( getResources().getString( R.string.title_equate_with ), st1, st2 );
        TopoDroidAlertDialog.makeAlert( this, getResources(), title, 
          new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) {
              makeEquate( st1, st2 );
            }
          } );
      } else {
        TDToast.make( R.string.equate_no_nearby );
      }
    }
  }

  void makeEquate( String st1, String st2 )
  {
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
    TdmEquate equate = new TdmEquate();
    for ( String st : sts ) equate.addStation( st );
    // TDLog.v( "add equate: " + equate.stationsString() );
    TdmConfigActivity.mTdmConfig.addEquate( equate );
    updateViewEquates();
  }



  void updateViewEquates()
  {
    mDrawingSurface.addEquates( TdmConfigActivity.mTdmConfig.getEquates() );
  }

  @Override
  public void onClick(View view)
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
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // SHOW EQUATES
      (new TdmEquatesDialog( this, TdmConfigActivity.mTdmConfig, this )).show();
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
}
