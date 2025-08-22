/** @file TopoGL.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief 3D Topo-GL activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDLocale;
// import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDVersion;

// import com.topodroid.TDX.TDandroid;

// import com.topodroid.c3bt.ConnectionState;
// import com.topodroid.c3bt.BleUtils;
// import com.topodroid.c3bt.BricComm;
// import com.topodroid.c3bt.SapComm;
// import com.topodroid.c3bt.DistoXComm;

import com.topodroid.c3in.ParserTh;
import com.topodroid.c3in.ParserTro;
import com.topodroid.c3in.ParserTrox;
import com.topodroid.c3in.ParserDat;
import com.topodroid.c3in.Parser3d;
import com.topodroid.c3in.ParserLox;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch; // NO_C3D
import com.topodroid.c3in.ParserException;

import com.topodroid.c3out.ExportData;
import com.topodroid.c3out.ExportTask;

import com.topodroid.c3walls.cw.CWConvexHull;

import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
// import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDsafUri;
import com.topodroid.help.HelpDialog;

import com.topodroid.ui.MyButton;
// import com.topodroid.ui.MyMenuAdapter;
// import com.topodroid.ui.MyMenuItem;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;

// import java.io.StringWriter;
// import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
// import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
// import java.util.Set;
import java.util.Locale;

// import android.os.Environment;
import android.os.Build; // FINERPRINT and MODEL
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask;
// import android.os.Message;
import android.os.ParcelFileDescriptor;

import android.app.Activity;
import android.app.ActivityManager;
import android.preference.PreferenceManager;

// import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
// import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
// import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.pm.ConfigurationInfo;
// import android.content.pm.PackageManager;
// import android.content.pm.PackageManager.NameNotFoundException;

import android.provider.MediaStore;
import android.database.Cursor;

import android.graphics.RectF;
import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
// import android.view.Menu;
// import android.view.MenuItem;
// import android.view.MotionEvent;

import android.widget.Toast;
import android.widget.Button;
import android.widget.ListView;
// import android.widget.CheckBox;
// import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


// import android.util.DisplayMetrics;

// import android.bluetooth.BluetoothAdapter;
// import android.bluetooth.BluetoothDevice;

import android.net.Uri;

// import android.opengl.GLSurfaceView;

public class TopoGL extends Activity 
                    implements OnClickListener
                    , OnLongClickListener
                    , OnItemClickListener
                    // , OnSharedPreferenceChangeListener
                    , GPS.GPSListener // WITH-GPS 
{
  // runtime flags
  // BLUETOOTH
  // final static boolean BLUETOOTH = true;
  // final static boolean BLUETOOTH_REMOTE = false;

  // public static final int MESSAGE_BLOCK = 1;
  // public static final String BLOCK_D= "BLOCK_D";
  // public static final String BLOCK_B= "BLOCK_B";
  // public static final String BLOCK_C= "BLOCK_C";
  // public static final String BLOCK_T= "BLOCK_T";

  // 
  // private Handler mBluetoothHandler = new Handler();

  // public Message obtainMessage( int type )
  // {
  //   return (mBluetoothHandler == null)? null : mBluetoothHandler.obtainMessage( type );
  // }

  // public void sendMessage( final Message msg ) 
  // {
  //   final TopoGL app = this;
  //   runOnUiThread( new Runnable() {
  //     @Override public void run() {
  //       // TDLog.v("TopoGL got message - type " + msg.what );
  //       if ( msg == null ) return;
  //       Bundle data = msg.getData();
  //       switch ( msg.what ) {
  //         case MESSAGE_BLOCK:
  //           handleBlock( app, data );
  //           break;
  //       }
  //     }
  //   } );
  // }

  // private void handleBlock( TopoGL app, Bundle data )
  // {
  //   final int   t = (int)( data.getInt( BLOCK_T ) );
  //   final float d = (float)( data.getDouble( BLOCK_D ) );
  //   final float b = (float)( data.getDouble( BLOCK_B ) );
  //   final float c = (float)( data.getDouble( BLOCK_C ) );
  //   // TDToast.make( String.format(Locale.US, "Data %d: %.2f %.2f %.2f", t, d, b, c ) );
  //   // add data to the bluetooth parser
  //   app.handleRegularData( d, b, c );
  // }

  // public static String VERSION = "";
  // public static int VERSION_CODE = 0;

  // private static final int REQUEST_OPEN_FILE = 1;

  private static final int HELP_PAGE = R.string.TopoGL;

  boolean doSketches = false;

  // SKETCH
  private Cave3DShot mSketchLeg = null;
 
  /** @return the 3D sketch-leg
   */
  Cave3DShot getSketchLeg() { return mSketchLeg; } 

  /** set the 3D sketch-leg
   * @param leg  new 3D sketch-leg
   */
  void setSketchLeg( Cave3DShot leg )
  {
    mSketchLeg = leg;
  }

  /** set the sketch-leg with the given stations
   * @param from   FROM station
   * @param to     TO station
   */
  Cave3DShot setSketchLeg( String from, String to )
  {
    mSketchLeg = mParser.getLeg( from, to );
    return mSketchLeg;
  }

  /** @return the array of splays at a given station
   * @param st    station
   */
  ArrayList< Cave3DShot > getSplaysAt( Cave3DStation st ) { return (mParser == null)? null : mParser.getSplaysAt( st ); }

  /** @return the array of legs at a given station
   * @param st    station
   */
  ArrayList< Cave3DShot > getLegsAt( Cave3DStation st ) { return (mParser == null)? null : mParser.getLegsAt( st ); }

  // -------------------------------------------------------

  private BitmapDrawable mBMmeasureOn;
  private BitmapDrawable mBMmeasureOff;
  private BitmapDrawable mBMfixOn;
  private BitmapDrawable mBMfixOff;
  private BitmapDrawable mBMclose;

  static int mCheckPerms = -1;

  // ---------------------------------
  String mFilename;    // opened filename
  // private static boolean mExportUri = false; // from TDSetting

  // public static float mScaleFactor   = 1.0f; // from TopoDroidApp
  // public static float mDisplayWidth  = 200f;
  // public static float mDisplayHeight = 320f;

  private boolean mIsNotMultitouch;

  private boolean supportsES2 = false;

  String mDEMname = null;
  String mTextureName = null;

  static boolean mSelectStation = true;
  static boolean mHasC3d = false;
  // static boolean mHasFractal = false; // FRACTAL
  // static boolean mHasTemperature = false; // TEMPERATURE

  // --------------------------------- OpenGL stuff
  private GlSurfaceView glSurfaceView;
  private GlRenderer mRenderer = null;

  private LinearLayout mLayout;
  // private TextView     mText;
  private boolean rendererSet = false;
  private TglParser mParser = null;

  private LinearLayout mLayoutStation;
  private Button mCurrentStation;
  private Button mCurrentCoords;
  Button mMeasureStation;
  Button mFixStation;
  Button mCloseStation;
  boolean isMeasuring = false;
  boolean isFixed = false;

  /** @return true is there is the renderer and it has a surface
   * @note used also by DialogSurface
   */
  boolean hasSurface() { return ( mRenderer != null ) && mRenderer.hasSurface(); }

  /** @return true is there is a model parser and it has an origin
   */
  boolean withOsm() { return mParser != null && mParser.hasOrigin(); }

  GPS mGPS = null; // WITH-GPS

  // ---------------------------------------------------------------
  // LIFECYCLE
  private boolean mHasExtra = false;

  private boolean mFileDialog = true;
  // private int mVersionCheck = -1;
  private String mSurveyBase   = null; // TopoDroid CWD fullpath
  private String mSurveyName   = null; 
  private String mThconfigName = null; 

  /** handle extra parameters
   * @note when called, FeatureChecker.checkPermissions( this ) >= 0
   */
  private void handleExtra()
  {
    // TDLog.v("handle extra " + mHasExtra );
    // if ( FeatureChecker.checkLocation( this ) ) mGPS = new GPS( this ); // WITH-GPS
    if ( TDandroid.checkLocation( this ) ) mGPS = new GPS( this ); // WITH-GPS

    if ( mHasExtra ) {
      // if ( mVersionCheck >= 0 ) {
        if ( mSurveyBase != null && mSurveyBase.length() > 0 ) {
          // TDLog.v("Survey mSurveyBase <" + mSurveyBase + ">" );
          // Cave3DFile.setTopoDroidPaths( mSurveyBase ); // BLUETOOTH

          if ( mThconfigName != null ) { // used by TdManager
            TDLog.v("TdManager input tdconfig " + mThconfigName );
            mFileDialog = false;
            doOpenFile( null, mThconfigName, true ); // asynch
          } else if ( mSurveyName != null ) {
            TDLog.v("TopoDroid input survey " + mSurveyName + " mSurveyBase " + mSurveyBase);
            if ( doOpenSurvey( mSurveyName, mSurveyBase ) ) {
              doSketches = true;
              mFileDialog = false;
            } else {
              TDLog.e("Cannot open input survey " + mSurveyName );
            }
          } else {
            TDLog.e("No input file or survey");
          }          
        } else {
          TDLog.e("No input base cwd");
        }
      // } else {
      //   TDLog.e("failed version check " + mVersionCheck );
      //   TDToast.make( R.string.no_permissions ).show();
      // }
    } else {
      if ( mFileDialog ) {
        // TDLog.v("open file dialog - not implemented");
        // (new DialogOpenFile( this, this )).show();
        // // openFile();
      } else {
        TDToast.makeLong( R.string.no_permissions );
        TDLog.e("PERM finishing activity ... perms " + mCheckPerms );
        // if ( perms_dialog != null ) perms_dialog.dismiss();
        finish();
      }
    }
  }

  @Override
  public void onDestroy( )
  {
    super.onDestroy();
    ((TopoDroidApp)getApplication()).mTopoGL = null;
    if ( doubleBackHandler != null ) {
      doubleBackHandler.removeCallbacks( doubleBackRunnable );
    }
  }

  @Override
  public void onCreate( Bundle savedInstanceState ) 
  {
    super.onCreate(savedInstanceState);

    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

    // TDLog.v("on create: Not Android 10 " + NOT_ANDROID_10 + " 11 " + NOT_ANDROID_11 );
    TDLog.v("GL on create - texture root: " + mTextureRoot );
    checkPreferences();

    mHasC3d     &= TDLevel.overExpert;
    // mHasFractal &= TDLevel.overExpert;
    // mHasTemperature &= TDLevel.overExpert;
   
    ((TopoDroidApp)getApplication()).mTopoGL = this;

    setContentView( R.layout.cave3d_activity );
    mLayout = (LinearLayout) findViewById( R.id.view_layout );
    // mText   = (TextView) findViewById( R.id.text );

    // DisplayMetrics dm = getResources().getDisplayMetrics(); // from TopoDroidApp
    // float density  = dm.density;
    // mDisplayWidth  = dm.widthPixels;
    // mDisplayHeight = dm.heightPixels;
    // mScaleFactor   = (mDisplayHeight / 320.0f) * density;
    // TDLog.v( "display " + TopoDroidApp.mDisplayWidth + " " + TopoDroidApp.mDisplayHeight + " scale " + TopoDroidApp.mScaleFactor + " density " + density );

    GlModel.setWidthAndHeight( TopoDroidApp.mDisplayWidth, TopoDroidApp.mDisplayHeight );
    
    mLayoutStation = (LinearLayout) findViewById( R.id.layout_station );
    mCurrentStation = (Button) findViewById( R.id.current_station ); // name of the highlighted station in the station dialog-bar
    mCurrentCoords  = (Button) findViewById( R.id.current_coords );
    // mCurrentStation.setOnClickListener( this ); // better use onLongClick on layout
    // mCurrentStation.setOnLongClickListener( this );

    mMeasureStation = (Button) findViewById( R.id.measure_station );
    mFixStation = (Button) findViewById( R.id.fix_station );
    mCloseStation = (Button) findViewById( R.id.close_station );
    mMeasureStation.setOnClickListener( this );
    mFixStation.setOnClickListener( this );
    mCloseStation.setOnClickListener( this );

    mLayoutStation.setVisibility( View.GONE );
    // mLayoutStation.setOnLongClickListener( this ); // 20250113 replaced with "close" button
    
    // setWallButton( mRenderer.wall_mode );

    // FIXME moved to onStart
    // mListView = (MyHorizontalListView) findViewById(R.id.listview);
    // int size = resetButtonBar();
    // mMenuImage = (Button) findViewById( R.id.handle );
    // mMenuImage.setOnClickListener( this );
    // TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, size, R.drawable.iz_menu ) );
    // mMenu = (ListView) findViewById( R.id.menu );
    // mMenu.setOnItemClickListener( this );

    // glSurfaceView = (GLSurfaceView) findViewById( R.id.view );

    // setContentView(glSurfaceView);
    // TDLog.v("on create mid");
    mParser = null; // new TglParser( this, filename );

    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      mHasExtra = true;
      // mVersionCheck = TDVersion.checkTopoDroidVersion( this );
      // // FIXME ANDROID-11 etc.
      // if ( mVersionCheck == -1 && TDandroid.ABOVE_API_29 ) { 
      //   mVersionCheck = 0;
      // }
      // if ( mVersionCheck < -1 ) {
      //   TDToast.makeLong( R.string.bad_topodroid );
      // } else {
        // the uri string is the absolute basepath
        // String uri_str = extras.getString( "BASE_URI" );
        // Uri uri = Uri.parse( uri_str );
        // TDLog.v("URI + uri.toString() );

        mSurveyBase   = extras.getString( "SURVEY_BASE" ); // TopoDroid CWD fullpath
        mThconfigName = extras.getString( "INPUT_THCONFIG" );  // thconfig
        mSurveyName   = extras.getString( "INPUT_SURVEY" );
        // TDLog.v("3D extras: base " + mSurveyBase + " survey " + mSurveyName + " config " + mThconfigName );
      // }
    }

    // if ( FeatureChecker.checkPermissions( this ) < 0 ) {
    //   mRequestPermissionTime = 1;
    //   checkPermissions();
    // } else {
      handleExtra();
    // }
  }

  /** lifecycle: when the window pauses displaying
   */
  @Override
  protected void onPause()
  {
    super.onPause();
    if ( rendererSet ) {
      // TDLog.v("TopoGL on pause");
      if ( glSurfaceView != null ) glSurfaceView.onPause();
      if ( mRenderer != null ) mRenderer.unbindTextures();
    }
  }

  /** lifecycle: when the window starts
   */
  @Override
  protected void onStart()
  {
    super.onStart();
    TDLog.v("TopoGL on Start");
    if ( ! TDandroid.canManageExternalStorage( this ) ) {
      TDandroid.requestExternalStorage( this, this );
    }
    if ( TDandroid.canRun( this, this ) ) {
      TopoDroidApp app = (TopoDroidApp) getApplication();
      app.initEnvironmentFirst( );
    } else {
    // copied from MainWindow
    //   ++ mRequestPermissionTime;
    //   // TDLog.v("MAIN cannot run - has db " + TopoDroidApp.hasTopoDroidDatabase() + " request perms time " + mRequestPermissionTime );
    //   if ( TDandroid.createPermissions( this, this, mRequestPermissionTime ) == 0 ) {
    //     app.initEnvironmentFirst( );
    //     // TDLog.v("MAIN show init dialogs [2]");
    //     showInitDialogs( false /* ! TopoDroidApp.hasTopoDroidDatabase() */ );
    //     // resetButtonBar();
    //   // } else {  // the followings are delayed after the permissions have been granted
    //   //   app.initEnvironmentFirst( );
    //   //   if ( perms == 0 ) showInitDialogs( ! TopoDroidApp.hasTopoDroidDatabase() );
    //   }
    }
    // if ( TDSetting.isFlagButton() ) resetButtonBar(); // 6.0.33

    TDLocale.resetTheLocale();

    mListView = (MyHorizontalListView) findViewById(R.id.listview); // button bar on the top
    int size = resetButtonBar();

    mMenuImage = (Button) findViewById( R.id.handle );
    mMenuImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, size, R.drawable.iz_menu ) );
    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );

    setMenuAdapter( getResources() );
    closeMenu();
    makeSurface();
  }

  /** lifecycle: when the window resumes displaying
   */
  @Override
  protected void onResume()
  {
    super.onResume();
    TDLog.v("TopoGL on Resume");
    if ( rendererSet ) {
      // TDLog.v("TopoGL on resume");
      // glSurfaceView.setMinimumWidth( mLayout.getWidth() );
      // glSurfaceView.setMinimumHeight( mLayout.getHeight() );
      glSurfaceView.onResume();
      // mRenderer.rebindTextures(); // this method is empty-unused
      // mRenderer.onSurfaceChanged( null, glSurfaceView.getWidth(), glSurfaceView.getHeight() );
      // glSurfaceView.requestRender();
    }
    // if ( BLUETOOTH  && mPrefs != null ) {
    //   String name = mPrefs.getString( "CAVE3D_BLUETOOTH_DEVICE", "" );
    //   checkBluetooth( name );
    // }
  }

  /** react to a change of configuration
   * @param cfg  new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration cfg )
  {
    super.onConfigurationChanged( cfg );
    // TDLog.v("TOPOGL config change" );
    TDLocale.resetTheLocale( );
  }

  // -----------------------------------------------------------------
  // BACK PRESSED

  private boolean doubleBack = false;
  private Handler doubleBackHandler = new Handler();
  private Toast   doubleBackToast = null;

  private final Runnable doubleBackRunnable = new Runnable() {
    @Override 
    public void run() {
      doubleBack = false;
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
    }
  };

  /** react to a BACK key tap
   */
  @Override
  public void onBackPressed () // askClose
  {
    // TDLog.Log( TDLog.LOG_INPUT, "MainWindow onBackPressed()" );
    if ( onMenu ) {
      closeMenu();
      return;
    }
    if ( TDSetting.mSingleBack ) {
      super.onBackPressed();
    } else if ( doubleBack ) {
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
      super.onBackPressed();
    } else {
      doubleBack = true;
      doubleBackToast = Toast.makeText( this, R.string.double_back, Toast.LENGTH_SHORT );
      doubleBackToast.show();
      doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
    }
  }

  // ----------------------------------------------------------------

  /** create a model parser for a file
   * @param filename   name of the model file
   */
  void makeParser( String filename )
  {
    // TDLog.v("TopoGL parser " + filename );
    mParser = new TglParser( this, filename );
    if ( mRenderer != null ) mRenderer.setParser( mParser, true );
  }

  /** set the title 
   * @param str   title string
   */
  void setTheTitle( String str ) 
  { 
    // if ( mBtRemoteName != null ) {
    //   setTitle( str ); 
    // } else {
      setTitle( str ); 
    // }
  }

  /** make the rendering GL surface
   */
  private void makeSurface()
  {
    if ( glSurfaceView != null ) return;
    // TDLog.v("TopoGL make surface");
    glSurfaceView = new GlSurfaceView(this, this);
    glSurfaceView.setMinimumWidth( mLayout.getWidth() );
    glSurfaceView.setMinimumHeight( mLayout.getHeight() );
    mLayout.addView( glSurfaceView );
    mLayout.invalidate();

    // Check if the system supports OpenGL ES 2.0.
    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
    /*
    final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
     */
    // Even though the latest emulator supports OpenGL ES 2.0,
    // it has a bug where it doesn't set the reqGlEsVersion so
    // the above check doesn't work. The below will detect if the
    // app is running on an emulator, and assume that it supports
    // OpenGL ES 2.0.
    final boolean supportsEs2 =
        configurationInfo.reqGlEsVersion >= 0x20000
            || ( // ( TDandroid.AT_LEAST_API_15 ) && 
                 ( Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")));

    if (supportsEs2) {
        // TDToast.make("This device supports OpenGL ES 2.0." );
        // Request an OpenGL ES 2.0 compatible context.
        glSurfaceView.setEGLContextClientVersion(2);

        // Assign our renderer
        if ( mRenderer == null ) {
          GlModel model = new GlModel( this );
          mRenderer = new GlRenderer( this, model );
          if ( mParser != null ) mRenderer.setParser( mParser, true );
        }
        glSurfaceView.setRenderer( mRenderer );
        // glSurfaceView.setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
        rendererSet = true;
    } else {
        /*
         * This is where you could create an OpenGL ES 1.x compatible
         * renderer if you wanted to support both ES 1 and ES 2. Since we're
         * not doing anything, the app will crash if the device doesn't
         * support OpenGL ES 2.0. If we publish on the market, we should
         * also add the following to AndroidManifest.xml:
         * 
         * <uses-feature android:glEsVersion="0x00020000"
         * android:required="true" />
         * 
         * This hides our app from those devices which don't support OpenGL
         * ES 2.0.
         */
        TDToast.make( "This device does not support OpenGL ES 2.0." );
        return;
    }
  }
  // ---------------------------------------------------------------
  // MENU

  // final static int MENU_BT   =  2; // bluetooth
  // // final static int MENU_WALL =  8; // bluetooth

  // final static int MENU_C3D         =  9; // sketch // NO_C3D
  // final static int MENU_TEMPERATURE = 10; // temperature
  // final static int MENU_FRACTAL     = 11; // fractal

  Button     mMenuImage = null;
  ListView   mMenu = null;
  boolean    onMenu = false;

  private static final int[] menus = {
    R.string.cmenu_open,       // 0
    R.string.menu_export,
    // R.string.cmenu_ble, // FIXME BLUETOOTH  MENU
    R.string.cmenu_info,       // 2
    // R.string.cmenu_surveys,
    R.string.cmenu_ico,
    R.string.cmenu_rose,
    R.string.cmenu_reset,
    R.string.cmenu_viewpoint,  // 6
    R.string.cmenu_alpha,
    R.string.cmenu_wall,       // 8
    // R.string.cmenu_sketch,     // 9  C3D NO_C3D
    // R.string.cmenu_temp,       // 10 TEMPERATURE
    // R.string.cmenu_fractal,    // 11 FRACTAL
    R.string.cmenu_search,
    R.string.menu_options,
    R.string.menu_help
  };

  private static final int[] help_menus = {
                          R.string.help_open_3d,
                          R.string.help_export_3d,
                          R.string.help_info_3d,
                          // R.string.help_surveys,
                          R.string.help_ico_3d,
                          R.string.help_rose_3d,
                          R.string.help_reset_3d,
                          R.string.help_viewpoint_3d,
                          R.string.help_alpha_3d,
                          R.string.help_wall_3d,
                          // R.string.help_sketch_3d, // NO_C3D
                          // R.string.help_temperature, // TEMPERATURE
                          // R.string.help_fractal, // FRACTAL
                          R.string.help_search,
                          R.string.help_prefs,
                          R.string.help_help
  };

  /** create the list of menus
   * @param res   resources
   */
  void setMenuAdapter( Resources res )
  {
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>( this, R.layout.menu );

    for ( int k=0; k<menus.length; ++k ) {
      // if ( k == MENU_BT  && ! ( mWithBluetooth && hasBluetoothName() ) ) continue; // FIXME BLUETOOTH  MENU
      // if ( k == MENU_C3D && ! mHasC3d ) continue; // NO_C3D
      // if ( k == MENU_TEMPERATURE && ! mHasTemperature ) continue;
      // if ( k == MENU_FRACTAL && ! mHasFractal ) continue;
      menu_adapter.add( res.getString( menus[k] ) );
    }
    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

  /** hide the menus
   * @note used by GlSurfaceView
   */
  void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  /** handle a tap on a menu
   * @param pos  index of tapped menu
   */
  private void handleMenu( int pos ) 
  {
    closeMenu();
    // TDToast.make( item.toString() );
    int p = 0;
    if ( p++ == pos ) { // OPEN
      // (new DialogOpenFile( this, this )).show();
      selectImportFile();
    } else if ( p++ == pos ) { // EXPORT
      if ( mParser != null ) {
        // (new DialogExport( this, this, mParser )).show(); // this calls selectExportFile( export );
        if ( mParser.hasFixes() ) { // this calls selectExportFile( export );
          (new ExportDialogModel( this, this, mParser, TDConst.mModelExportTypes, R.string.export_model_title)).show();
        } else {
          (new ExportDialogModel( this, this, mParser, TDConst.mModelExportTypesNoGeo, R.string.export_model_title)).show();
        }
      } else {
        TDToast.makeBad( R.string.no_model );
      }
    // FIXME BLUETOOTH  MENU
    // } else if ( mWithBluetooth && hasBluetoothName() && (p++ == pos) ) { // BLUETOOTH SURVEY
    //   // TODO 
    //   (new DialogBluetoothSurveyList( this, this )).show();

    } else if ( p++ == pos ) { // INFO
      if ( mParser != null ) {
        new DialogInfo(this, mParser, mRenderer).show();
      } else {
        TDToast.makeBad( R.string.no_model );
      }
    // } else if ( p++ == pos ) { // SURVEYS
    //   if ( mParser != null ) {
    //     new DialogSurveys(this, mParser, mRenderer).show();
    //   } else {
    //     TDToast.makeBad( R.string.no_model );
    //   }
    } else if ( p++ == pos ) { // ICO
      if ( mParser != null ) {
        new DialogIco(this, mParser).show();
      } else {
        TDToast.makeBad( R.string.no_model );
      }
    } else if ( p++ == pos ) { // ROSE
      if ( mParser != null ) {
        new DialogRose(this, mParser).show();
      } else {
        TDToast.makeBad( R.string.no_model );
      }
    } else if ( p++ == pos ) { // RESET
      GlModel.resetModes();
      GlNames.resetStations();
      if ( mRenderer != null ) mRenderer.resetTopGeometry();
      mSelectStation = true;
      resetButtons();
    } else if ( p++ == pos ) { // VIEWPOINT
      if ( mParser != null ) {
        if ( mRenderer != null ) new DialogView( this, this, mRenderer ).show();
      } else {
        TDToast.makeBad( R.string.no_model );
      }
    } else if ( p++ == pos ) { // SURFACE ALPHA
      if ( mParser != null ) {
        if ( onDEMloading ) {
          TDToast.make( R.string.dem_loading );
        } else {
          new DialogSurface( this, this ).show();
        }
      } else {
        TDToast.makeBad( R.string.no_model );
      }
    } else if ( p++ == pos ) { // DO_WALLS
      if ( mParser != null ) {
        new DialogWalls( this, this, mParser ).show();
      } else {
        TDToast.makeBad( R.string.no_model );
      }
    // } else if ( mHasC3d && p++ == pos ) { // SKETCH // NO_C3D
    //   if ( mParser != null ) {
    //     if ( doSketches ) {
    //       if ( mRenderer != null ) new DialogSketches( this, this, mRenderer ).show(); // C3D
    //     } else {
    //       TDToast.makeBad( R.string.no_topodroid_model );
    //     }
    //   } else {
    //     TDToast.makeBad( R.string.no_model );
    //   }
    // } else if ( mHasTemperature && p++ == pos ) { // TEMPERATURE
    //   if ( mParser != null ) {
    //     selectTemperatureFile();
    //   } else {
    //     TDToast.makeBad( R.string.no_model );
    //   }
    // } else if ( mHasFractal && p++ == pos ) { // FRACTAL
    //   new DialogFractal( this, mParser ).show();
    } else if ( p++ == pos ) { // SEARCH
      if ( mParser != null ) {
        (new HighlightDialog( this, this )).show();
      } else {
        TDToast.makeBad( R.string.no_model );
      }
    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( this, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_CAVE3D );
      startActivity( intent );
      // startActivity( new Intent( this, TopoGLPreferences.class ) );
    } else if ( p++ == pos ) { // HELP
      // new DialogHelp(this).show();	    
      new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE )).show();
    }
  }

  /** highlight station points with name that starts with a given prefix
   * @param prefix    station name prefix (null: clear highlight)
   */
  void highlightStations( String prefix ) { mRenderer.highlightStations( prefix ); }

  // ---------------------------------------------------------------------

  /** handle a user tap on an item
   * @param parent   parent view
   * @param view     tapped item
   * @param pos      index of tapped item
   * @param id       ...
   */
  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) { // MENU
      handleMenu( pos );
    }
  }

  // ---------------------------------------------------------
  // BUTTONS

  MyHorizontalListView mListView = null;
  MyHorizontalButtonView mButtonView1;
  Button[] mButton1 = null;
  static int mNrButton1 = 8;
  private static final int[] izons = {
    R.drawable.iz_light,
    R.drawable.iz_orthogonal,
    R.drawable.iz_station_no_dot,
    R.drawable.iz_splays_none,
    R.drawable.iz_wall_no,
    R.drawable.iz_surface_off,
    R.drawable.iz_color,
    R.drawable.iz_frame_grid,
    // R.drawable.iz_bt_down, // FIXME BLUETOOTH BUTTON
    // secondary bitmaps
    R.drawable.iz_wall,
    R.drawable.iz_perspective,
    R.drawable.iz_surface_on
    // R.drawable.iz_bt_off, // FIXME BLUETOOTH BUTTON
    // R.drawable.iz_bt_wait,
    // R.drawable.iz_bt_ready,
    // R.drawable.iz_bt_laser,
    // R.drawable.iz_bt_scan,
    // R.drawable.iz_view
  };

  private static final int[] help_icons = { R.string.help_light_3d,
                          R.string.help_projection_3d,
                          R.string.help_stations_3d,
                          R.string.help_splays_3d,
                          R.string.help_walls_3d,
                          R.string.help_surface_3d,
                          R.string.help_color_3d,
                          R.string.help_frame_3d
  };

  int BTN_MOVE     = 0;
  int BTN_PROJECT  = 1;
  int BTN_STATION  = 2;
  int BTN_SPLAYS   = 3;
  int BTN_WALL     = 4;
  int BTN_SURFACE  = 5;
  int BTN_COLOR    = 6;
  int BTN_FRAME    = 7;
  // int BTN_BLE      = 8; // FIXME BLUETOOTH BUTTON
  BitmapDrawable mBMlight;
  BitmapDrawable mBMmove;
  BitmapDrawable mBMturn;
  BitmapDrawable mBMhull;

  // BitmapDrawable mBMbleDown  = null; // FIXME BLUETOOTH BUTTON STATE-ICONS
  // BitmapDrawable mBMbleOff   = null;
  // BitmapDrawable mBMbleOn    = null; 
  // BitmapDrawable mBMbleWait  = null;
  // BitmapDrawable mBMbleReady = null;
  // BitmapDrawable mBMbleLaser = null;
  // BitmapDrawable mBMbleShot  = null;
  // BitmapDrawable mBMbleScan  = null;

  BitmapDrawable mBMdelaunay;
  BitmapDrawable mBMpowercrust;
  // BitmapDrawable mBMconvex;
  BitmapDrawable mBMwallNo;
  BitmapDrawable mBMsurfaceNo;
  BitmapDrawable mBMwall;
  BitmapDrawable mBMperspective;
  BitmapDrawable mBMorthogonal;
  BitmapDrawable mBMsurface;

  BitmapDrawable mBMstationNo;
  BitmapDrawable mBMstationPoint;
  BitmapDrawable mBMstationName;
  BitmapDrawable mBMstationLeg;
  // BitmapDrawable mBMstation;
  BitmapDrawable mBMstationNoDot;
  BitmapDrawable mBMstationPointDot;
  BitmapDrawable mBMstationNameDot;
  BitmapDrawable mBMstationLegDot;
  // BitmapDrawable mBMstationDot;

  BitmapDrawable mBMsplaysNo;
  BitmapDrawable mBMsplaysLine;
  BitmapDrawable mBMsplaysPoint;
  // BitmapDrawable mBMsplays;

  BitmapDrawable mBMcolorNo;
  BitmapDrawable mBMcolorSurvey;
  BitmapDrawable mBMcolorDepth;
  BitmapDrawable mBMcolorSurface;
  // BitmapDrawable mBMcolorTemp; // TEMPERATURE

  BitmapDrawable mBMframeNo;
  BitmapDrawable mBMframeGrid;
  BitmapDrawable mBMframeAxes;


  /** reset the button bar
   */
  private int resetButtonBar()
  {
    int size = 42;
    if ( mListView != null ) {
      size = TopoDroidApp.setListViewHeight( this, mListView );
      MyButton.resetButtonCache( size );

      // if ( BLUETOOTH )  ++mNrButton1; 
      mButton1 = new Button[ mNrButton1 + 1 ]; // one extra space for empty button
      mButton1[0] = MyButton.getButton( this, this, izons[0] );
      mButton1[1] = MyButton.getButton( this, this, izons[1] );
      mButton1[2] = MyButton.getButton( this, this, izons[2] );
      mButton1[3] = MyButton.getButton( this, this, izons[3] );
      mButton1[4] = MyButton.getButton( this, this, izons[4] );
      mButton1[5] = MyButton.getButton( this, this, izons[5] );
      mButton1[6] = MyButton.getButton( this, this, izons[6] );
      mButton1[7] = MyButton.getButton( this, this, izons[7] );
      // if ( BLUETOOTH ) mButton1[8] = MyButton.getButton( this, this, izons[8] );
      mButton1[ mNrButton1 ] = MyButton.getButton( this, null, R.drawable.iz_empty );

      // mButton1[ 0 ].setOnLongClickListener( this );
      mButton1[ 1 ].setOnLongClickListener( this ); // projection params
      mButton1[ 2 ].setOnLongClickListener( this ); // stations
      mButton1[ 3 ].setOnLongClickListener( this ); // splays: new manual leg
      mButton1[ 6 ].setOnLongClickListener( this ); // surveys
      mButton1[ 7 ].setOnLongClickListener( this ); // frame: leg-visibility

      mBMlight = MyButton.getButtonBackground( this, size, R.drawable.iz_light );
      mBMturn  = MyButton.getButtonBackground( this, size, R.drawable.iz_turn );
      mBMmove  = MyButton.getButtonBackground( this, size, R.drawable.iz_move );
      // mBMconvex = mButton1[BTN_WALL].getBitmap();

      mBMorthogonal  = MyButton.getButtonBackground( this, size, R.drawable.iz_orthogonal);
      mBMperspective = MyButton.getButtonBackground( this, size, R.drawable.iz_perspective);

      mBMstationNoDot   = MyButton.getButtonBackground( this, size, R.drawable.iz_station_no_dot );
      mBMstationPointDot= MyButton.getButtonBackground( this, size, R.drawable.iz_station_point_dot );
      mBMstationNameDot = MyButton.getButtonBackground( this, size, R.drawable.iz_station_name_dot );
      mBMstationLegDot  = MyButton.getButtonBackground( this, size, R.drawable.iz_station_leg_dot );
      // mBMstationDot     = MyButton.getButtonBackground( this, size, R.drawable.iz_station_dot );

      mBMstationNo   = MyButton.getButtonBackground( this, size, R.drawable.iz_station_no );
      mBMstationPoint= MyButton.getButtonBackground( this, size, R.drawable.iz_station_point );
      mBMstationName = MyButton.getButtonBackground( this, size, R.drawable.iz_station_name );
      mBMstationLeg  = MyButton.getButtonBackground( this, size, R.drawable.iz_station_leg );
      // mBMstation     = MyButton.getButtonBackground( this, size, R.drawable.iz_station );

      mBMsplaysNo    = MyButton.getButtonBackground( this, size, R.drawable.iz_splays_none );
      mBMsplaysLine  = MyButton.getButtonBackground( this, size, R.drawable.iz_splays_line );
      mBMsplaysPoint = MyButton.getButtonBackground( this, size, R.drawable.iz_splays_point );

      mBMwallNo      = MyButton.getButtonBackground( this, size, R.drawable.iz_wall_no );
      mBMwall        = MyButton.getButtonBackground( this, size, R.drawable.iz_wall );

      mBMsurfaceNo   = MyButton.getButtonBackground( this, size, R.drawable.iz_surface_off );
      mBMsurface     = MyButton.getButtonBackground( this, size, R.drawable.iz_surface_on );

      mBMcolorNo     = MyButton.getButtonBackground( this, size, R.drawable.iz_color );
      mBMcolorSurvey = MyButton.getButtonBackground( this, size, R.drawable.iz_color_survey );
      mBMcolorDepth  = MyButton.getButtonBackground( this, size, R.drawable.iz_color_depth );
      mBMcolorSurface= MyButton.getButtonBackground( this, size, R.drawable.iz_color_surface );
      // mBMcolorTemp   = MyButton.getButtonBackground( this, size, R.drawable.iz_color_temp ); // TEMPERATURE

      mBMframeGrid   = MyButton.getButtonBackground( this, size, R.drawable.iz_frame_grid );
      mBMframeNo     = MyButton.getButtonBackground( this, size, R.drawable.iz_frame_no );
      mBMframeAxes   = MyButton.getButtonBackground( this, size, R.drawable.iz_frame_axes );

      // if ( BLUETOOTH ) { // bluetooth button state-icons
      //   mBMbleDown  = MyButton.getButtonBackground( this, size, R.drawable.iz_bt_down );
      //   mBMbleOff   = MyButton.getButtonBackground( this, size, R.drawable.iz_bt_off );
      //   mBMbleOn    = MyButton.getButtonBackground( this, size, R.drawable.iz_bt_on  );
      //   mBMbleWait  = MyButton.getButtonBackground( this, size, R.drawable.iz_bt_wait  );
      //   mBMbleReady = MyButton.getButtonBackground( this, size, R.drawable.iz_bt_ready );
      //   mBMbleLaser = MyButton.getButtonBackground( this, size, R.drawable.iz_bt_laser );
      //   mBMbleShot  = MyButton.getButtonBackground( this, size, R.drawable.iz_bt_shot  );
      //   mBMbleScan  = MyButton.getButtonBackground( this, size, R.drawable.iz_bt_scan  );
      //   mButton1[ BTN_BLE ].setVisibility( hasBluetoothName()? View.VISIBLE : View.GONE );
      //   mButton1[ BTN_BLE ].setOnLongClickListener( this ); // bluetooth
      //   // mButton1[ BTN_BLE ].setOnClickListener( this );
      // }

      // mButtonView1 = new HorizontalImageButtonView( mButton1 );
      mButtonView1 = new MyHorizontalButtonView( mButton1 );
      mListView.setAdapter( mButtonView1.mAdapter );

      resetButtons();

      // mButton1[ BTN_WALL ].setOnClickListener( this );
      // mButton1[ BTN_SURFACE ].setOnClickListener( this );

      mBMmeasureOn  = MyButton.getButtonBackground( this, size, R.drawable.iz_ruler_on );
      mBMmeasureOff = MyButton.getButtonBackground( this, size, R.drawable.iz_ruler_off );
      mBMfixOn      = MyButton.getButtonBackground( this, size, R.drawable.iz_station_on );
      mBMfixOff     = MyButton.getButtonBackground( this, size, R.drawable.iz_station_off );
      mBMclose      = MyButton.getButtonBackground( this, size, R.drawable.iz_clear_green );
    }
    return size;
  }

  /** report to user long-tap on a button
   * @param v   tapped view
   * @return true if the long-tap has been handled
   * @note for projection: adjust focal length
   * @note for stations: toggle station-selectability
   * @note for splays: insert a new leg manually (only for BT surveying mode)
   * @note for surveys: list surveys to disable display
   * @note for legs: leg visibility
   */
  @Override 
  public boolean onLongClick( View v ) 
  {
    // if ( v.getId() == R.id.current_station ) {
    //   centerAtCurrentStation();
    //   return true;
    // }
    // if ( v.getId() == R.id.layout_station ) { // 20250113 replaced with "close" button
    //   closeCurrentStation();
    //   return true;
    // }

    Button b = (Button) v;
    if ( b == mButton1[ BTN_PROJECT ] ) {
      if ( GlRenderer.projectionMode != GlRenderer.PROJ_PERSPECTIVE ) return false;
      new DialogProjection( this, mRenderer ).show();
    } else if ( b == mButton1[ BTN_STATION ] ) {
      mSelectStation = ! mSelectStation;
      // TDLog.v("TopoGL on long click " + mSelectStation );
      setButtonStation();
      closeCurrentStation();
    } else if ( b == mButton1[ BTN_SPLAYS ] ) {
      // if ( BLUETOOTH && mWithBluetooth ) {
      //   new DialogManualLeg( this, this ).show();
      // } else {
        onClick( v );
      // }
    } else if ( b == mButton1[ BTN_COLOR ] ) {
      if ( mParser == null || mParser.getSurveyNumber() < 2 ) return false;
      new DialogSurveys( this, this, mParser.getSurveys() ).show();
    } else if ( b == mButton1[ BTN_FRAME ] ) {
      new DialogLegs( this ).show();
    // } else if ( BLUETOOTH && b == mButton1[ BTN_BLE ] ) {
    //   // TDLog.v("BT button long click ");
    //   if ( mWithBluetooth ) {
    //     doBluetoothLongClick();
    //   } else {
    //     onClick( v );
    //   }
    }
    return true;
  }

  /** reset the state of the buttons
   */
  private void resetButtons()
  {
    setButtonProjection();
    setButtonSurface();
    setButtonWall();
    setButtonMove();
    setButtonStation();
    setButtonSplays();
    setButtonColor();
    setButtonFrame();
  }

  /** set the state of the STATION button, according to the selectability and the station-mode (none, point, name)
   */
  private void setButtonStation()
  {
    TDLog.v("GL set button station - select " + mSelectStation + " station " + GlNames.getStationMode() );
    if ( mSelectStation ) {
      switch ( GlNames.getStationMode() ) {
        case GlNames.STATION_NONE:
          mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationNoDot );
          break;
        case GlNames.STATION_POINT:
          mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationPointDot );
          break;
        case GlNames.STATION_NAME:
          mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationNameDot );
          break;
        case GlNames.STATION_LEG:
          mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationLegDot );
          break;
        // case GlNames.STATION_ALL:
        //   mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationDot );
        //   break;
      }
    } else {
      switch ( GlNames.getStationMode() ) {
        case GlNames.STATION_NONE:
          mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationNo );
          break;
        case GlNames.STATION_POINT:
          mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationPoint );
          break;
        case GlNames.STATION_NAME:
          mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationName );
          break;
        case GlNames.STATION_LEG:
          mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstationLeg );
          break;
        // case GlNames.STATION_ALL:
        //   mButton1[ BTN_STATION ].setBackgroundDrawable( mBMstation );
        //   break;
      }
    }
  }

  /** set the FRAME button, according to the frame-mode
   */
  private void setButtonFrame()
  {
    if ( mRenderer == null ) return;
    switch ( GlModel.frameMode ) {
      case GlModel.FRAME_NONE:
        mButton1[ BTN_FRAME ].setBackgroundDrawable( mBMframeNo );
        break;
      case GlModel.FRAME_GRID:
        mButton1[ BTN_FRAME ].setBackgroundDrawable( mBMframeGrid );
        break;
      case GlModel.FRAME_AXES:
        mButton1[ BTN_FRAME ].setBackgroundDrawable( mBMframeAxes );
        break;
    }
  }

  /** set the COLOR button, according to the color-mode (grey, by-survey, by-depth, by-surface)
   * @note the color could also be set by an attribute, eg, temperature, if there are enough values
   */
  private void setButtonColor()
  {
    if ( mRenderer == null ) return;
    switch ( mRenderer.getColorMode() ) {
      case GlLines.COLOR_NONE:
        mButton1[ BTN_COLOR ].setBackgroundDrawable( mBMcolorNo );
        break;
      case GlLines.COLOR_SURVEY:
        mButton1[ BTN_COLOR ].setBackgroundDrawable( mBMcolorSurvey );
        break;
      case GlLines.COLOR_DEPTH:
        mButton1[ BTN_COLOR ].setBackgroundDrawable( mBMcolorDepth );
        break;
      case GlLines.COLOR_SURFACE:
        mButton1[ BTN_COLOR ].setBackgroundDrawable( mBMcolorSurface );
        break;
      // case GlLines.COLOR_TEMP:
      //   mButton1[ BTN_COLOR ].setBackgroundDrawable( mBMcolorTemp );
      //   break;
    }
  }

  /** set the SPLAYS button, according to the splay-mode (none, line, endpoint)
   */
  private void setButtonSplays()
  {
    switch ( GlModel.splayMode ) {
      case GlModel.DRAW_NONE:
        mButton1[ BTN_SPLAYS ].setBackgroundDrawable( mBMsplaysNo );
        break;
      case GlModel.DRAW_LINE:
        mButton1[ BTN_SPLAYS ].setBackgroundDrawable( mBMsplaysLine );
        break;
      case GlModel.DRAW_POINT:
        mButton1[ BTN_SPLAYS ].setBackgroundDrawable( mBMsplaysPoint );
        break;
    }
  }

  /** set the SURFACE button: either shown or hidden
   */
  private void setButtonSurface()
  {
    mButton1[ BTN_SURFACE ].setBackgroundDrawable( GlModel.surfaceMode ? mBMsurface : mBMsurfaceNo );
  }

  /** set the WALLS button: either shown or hidden
   */
  private void setButtonWall() 
  {
    mButton1[ BTN_WALL ].setBackgroundDrawable( GlModel.wallMode ? mBMwall : mBMwallNo );
  }

  /** set the PROJECTION button: either orthogonal or perspective
   */
  private void setButtonProjection()
  {
    mButton1[ BTN_PROJECT ].setBackgroundDrawable( 
     ( mRenderer != null && GlRenderer.projectionMode == GlRenderer.PROJ_PERSPECTIVE )? mBMperspective : mBMorthogonal );
  }

  /** set the MOVE button: light, pan, rotate
   */
  private void setButtonMove()
  {
    if ( GlSurfaceView.mLightMode ) {
      if ( hasSurface() ) {
        mButton1[BTN_MOVE].setBackgroundDrawable( mBMlight );
      } else {
        mButton1[BTN_MOVE].setBackgroundDrawable( mBMmove );
      }
    } else { // MODE_ROTATE:
      mButton1[BTN_MOVE].setBackgroundDrawable( mBMturn );
    }
  }

  // ------------------------------------------------------
  // SELECTED STATION

  /** show the selected station
   * @param station   station name
   * @param coords    station coords
   */
  void showCurrentStation( String station, String coords )
  {
    mCurrentStation.setText( station );
    mCurrentCoords.setText( coords );
    mLayoutStation.setVisibility( View.VISIBLE );
    isMeasuring = false;
    isFixed = false;
    mMeasureStation.setBackground( mBMmeasureOff );
    mFixStation.setBackground( mBMfixOff );
    mCloseStation.setBackground( mBMclose );
  }
 
  /** hide the selected station
   */
  void closeCurrentStation()
  {
    mLayoutStation.setVisibility( View.GONE );
    isMeasuring = false;
    isFixed = false;
    if ( mRenderer != null ) mRenderer.clearStationHighlight();
    if ( mParser   != null ) mParser.clearStartStation();
    GlNames.setHLcolorG( 0.0f );
  }

  /** set the rotation center at the selected station
   * @return true if success
   */
  boolean centerAtCurrentStation( )
  {
    boolean res = false;
    if ( mRenderer != null ) res = mRenderer.setCenter();
    // TDToast.make( res ? R.string.center_set : R.string.center_clear );
    GlNames.setHLcolorG( res ? 0.5f : 0.0f );
    return res;
  }

  // ---------------------------------------------------------

  /** respond to user taps
   * @param view tapped view
   */
  @Override
  public void onClick(View view)
  { 
    // TDLog.v("TopoGL on click ...");
    if ( onMenu ) {
      closeMenu();
      return;
    }
    int id = view.getId();
    if ( id == R.id.handle ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    } 
    // if ( id == R.id.current_station ) {
    //   closeCurrentStation();
    //   return;
    // }

    Button b0 = (Button)view;
    int k1 = 0;
    if ( b0 == mButton1[k1++] ) { // MOVE - TURN
      GlSurfaceView.toggleLightMode();
      setButtonMove();
    } else if ( b0 == mButton1[k1++] ) { // PROJECTION
      if ( mRenderer != null ) {
        GlRenderer.toggleProjectionMode();
        setButtonProjection();
        refresh(); // does not help
      }
    } else if ( b0 == mButton1[k1++] ) { // STATIONS
      GlNames.toggleStations();
      setButtonStation();
    } else if ( b0 == mButton1[k1++] ) { // SPLAYS
      GlModel.toggleSplays();
      setButtonSplays();
    } else if ( b0 == mButton1[k1++] ) { // WALLS
      GlModel.toggleWallMode();
      setButtonWall();
    } else if ( b0 == mButton1[k1++] ) { // SURFACE
      GlModel.toggleSurface();
      setButtonSurface();
      setButtonMove();
    } else if ( b0 == mButton1[k1++] ) { // COLOR
      if ( mRenderer != null ) {
        mRenderer.toggleColorMode();
        setButtonColor();
      }
    } else if ( b0 == mButton1[k1++] ) { // FRAME
      if ( mRenderer != null ) {
        mRenderer.toggleFrameMode();
        setButtonFrame();
      }
    // } else if ( BLUETOOTH && b0 == mButton1[k1++] ) { // BLUETOOTH
    //   // TDLog.v("TopoGL BT button click ");
    //   if ( hasBluetoothName() ) doBluetoothClick();

    } else if ( b0 == mMeasureStation ) {
      if ( isMeasuring ) {
        mMeasureStation.setBackground( mBMmeasureOff );
        isMeasuring = false;
      } else {
        mMeasureStation.setBackground( mBMmeasureOn );
        isMeasuring = true;
      }
    } else if ( b0 == mFixStation ) {
      if ( centerAtCurrentStation() ) {
        mFixStation.setBackground( mBMfixOn );
        isFixed = true;
      } else {
        mFixStation.setBackground( mBMfixOff );
        isFixed = false;
      }
    } else if ( b0 == mCloseStation ) {
      closeCurrentStation();
    }
  }

  // -------------------------------------------------------------------

  /** set the survey (model) name
   * @param name   survey name
   */
  void setSurveyName( String name )
  {
    if ( name == null ) {
      mSurveyName = "_";
    } else {
      int pos = name.lastIndexOf(":");
      if ( pos >= 0 ) name = name.substring( pos+1 );
      pos = name.lastIndexOf("/");
      if ( pos >= 0 ) name = name.substring( pos+1 );
      pos = name.lastIndexOf(".");
      if ( pos >= 0 ) name = name.substring( 0, pos );
      mSurveyName = ( name.length() > 0 )? name : "_";
    }
  }

  /** set the title
   * @param clino    clino of the viewpoint
   * @param phi      azimuth of the viewpoint
   */
  void showTitle( double clino, double phi )
  {
    if ( mSurveyName != null ) {
      setTitle( String.format( getResources().getString(R.string.ctitle), mSurveyName, -clino, 360-phi ) );
    } else {
      setTitle( "C A V E _ 3 D ");
    }
  }

  /** open a TopoDroid survey
   * @param survey   survey name
   * @param base     topodroid base folder
   * @return true if success
   */
  private boolean doOpenSurvey( String survey, String base )
  {
    // Cave3DFile.checkAppBasePath( this );
    // Cave3DFile.mAppBasePath = base;
    mFilename   = survey;
    setSurveyName( survey );
    boolean ret = initRendering1( survey, base );
    // TDLog.v( "TopoGL do open survey: " + base + "/" + survey + " " + (ret? "true" : "false" ) );
    return true;
  }

  /** load model from a file
   * @param uri      file URI
   * @param pathname file name
   * @param asynch   ...
   * @return true if success
   * @note always called asynch
   *       asynch call returns always false
   *       synch call return true if successful
   */
  private boolean doOpenFile( final Uri uri, final String pathname, boolean asynch )
  {
    mFilename = null;
    setSurveyName( pathname );
    doSketches = false;
    // // setTitle( pathname );
    // int idx = pathname.lastIndexOf( '/' );
    // final String path = ( idx >= 0 )? pathname.substring( idx+1 ) : pathname;
    // TDLog.v("TopoGL do open file: " + pathname + " asynch " + asynch );

    if ( uri == null ) {
      // String path = Cave3DFile.THCONFIG_PATH + "/" + pathname + ".tdconfig";
      String path = TDPath.getTdconfigDir() + "/" + pathname + ".tdconfig";
      TDLog.v("TopoGL input stream from tdconfig " + pathname + " path " + path );
      // synchronized( TDPath.mTdrLock ) // FIXME-THREAD_SAFE ?
      try {
        // final InputStream is = new FileInputStream( path );
        TDLog.v("GL get input stream of path " + path );
        InputStream is = TDFile.getTopoDroidFileInputStream( path );
        if ( is == null ) {
          TDToast.makeBad( R.string.no_file );
          return false;
        } else {
          TDToast.make( String.format( getResources().getString( R.string.reading_file ), path ) );
          // if ( asynch ) {
          //   (new AsyncTask<Void, Void, Boolean>() {
          //     @Override public Boolean doInBackground(Void ... v ) {
          //       // TDLog.v("exec init rendering in background ... ");
          //       return initRendering2( is, path, pathname );
          //     }
          //     @Override public void onPostExecute( Boolean b )
          //     {
          //       if ( b ) {
          //         mFilename = path;
          //         CWConvexHull.resetCounters();
          //         if ( mRenderer != null ) mRenderer.setParser( mParser, true );
          //       }
          //     }
          //   } ).execute();
          //   return false;
          // } else { // synchronous
          if ( initRendering2( is, path, mSurveyName ) ) {
            mFilename = path;
            CWConvexHull.resetCounters();
            if ( mRenderer != null ) mRenderer.setParser( mParser, true );
          }
          // }
        }
        return ( mFilename != null );
      } catch ( FileNotFoundException e ) {
        TDLog.e("file not found: " + e.getMessage() );
      } catch ( IOException e ) {
        TDLog.e("io error: " + e.getMessage() );
      }
    } else {
      String path = pathname; // uri.getPath();
      TDLog.v("TopoGL input stream from uri " + path );
      try { 
        final InputStream is = this.getContentResolver().openInputStream( uri );
        if ( initRendering2( is, path, mSurveyName ) ) {
          mFilename = path;
          CWConvexHull.resetCounters();
          if ( mRenderer != null ) mRenderer.setParser( mParser, true );
        }
      } catch ( FileNotFoundException e ) {
        TDLog.e( e.getMessage() );
      }
      return ( mFilename != null );
    }
    return false;
  }

  // /** start request to get a model file to read
  //  */
  // private void openFile()
  // {
  //   Intent openFileIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DialogOpenFile.class );
  //   startActivityForResult( openFileIntent, REQUEST_OPEN_FILE );
  // }

  // FIXME UNUSED
  // /** set the WALL button - this method is empty - unused
  //  * @param wall_mode    walls mode
  //  */
  // private void setWallButton( int wall_mode )
  // {
  //   switch ( wall_mode ) {
  //     case TglParser.WALL_NONE:
  //       TDToast.make( "wall mode NONE" );
  //       // mButton1[BTN_WALL].setBackgroundDrawable( mBMnone );
  //       break;
  //     case TglParser.WALL_CW:
  //       TDToast.make( "wall mode CONVEX_HULL" );
  //       // mButton1[BTN_WALL].setBackgroundDrawable( mBMconvex );
  //       break;
  //     case TglParser.WALL_POWERCRUST:
  //       TDToast.make( "wall mode POWERCRUST" );
  //       // mButton1[BTN_WALL].setBackgroundDrawable( mBMpowercrust );
  //       break;
  //     case TglParser.WALL_DELAUNAY:
  //       TDToast.make( "wall mode DELAUNAY" );
  //       // mButton1[BTN_WALL].setBackgroundDrawable( mBMdelaunay );
  //       break;
  //     case TglParser.WALL_HULL:
  //       TDToast.make( "wall mode HULL" );
  //       // mButton1[BTN_WALL].setBackgroundDrawable( mBMhull );
  //       break;
  //     default:
  //       TDToast.make( "wall mode NONE" );
  //   }
  // }

  // ------- NO-C3D ------------------------------ SKETCH
  // /** open a sketch file (in Cave3D format)
  //  * @param pathname    file full pathname
  //  * @param filename    ...
  //  */
  // void openSketch( String pathname, String filename ) 
  // {
  //   // TDLog.v("DEM " + pathname );
  //   if ( ! pathname.toLowerCase( Locale.getDefault() ).endsWith( ".c3d" ) ) return;
  //   ParserSketch sketch = new ParserSketch( pathname );

  //   // final double dd = mDEMbuffer;
  //   (new AsyncTask<ParserSketch, Void, Boolean>() {
  //     ParserSketch my_sketch = null;

  //     public Boolean doInBackground( ParserSketch ... sketch ) 
  //     {
  //       my_sketch = sketch[0];
  //       my_sketch.readData( );
  //       return true;
  //     }

  //     public void onPostExecute( Boolean b )
  //     {
  //       // my_sketch.log();
  //       if ( b ) {
  //         if ( mRenderer != null ) mRenderer.notifySketch( my_sketch );
  //         TDToast.make( R.string.sketch_ok );
  //       } else {
  //         TDToast.make( R.string.sketch_failed );
  //       }
  //     }
  //   }).execute( sketch );
  // }

  // ------------------------------ DEM

  /** open a DEM file
   * @param uri   file URI
   */
  void openDEM( Uri uri )
  {
    // InputStreamReader isr = null;
    // ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
    String pathname = uri.getPath();
    String filename = uri.getLastPathSegment();
    // TDLog.v("DEM Path " + pathname + " file " + filename );
    mDEMname = filename;
    (new AsyncTask< Uri, Void, Boolean>() {
      ParserDEM dem = null;
      public Boolean doInBackground( Uri ... uri ) 
      {
        ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri[0] );
        InputStreamReader isr = new InputStreamReader( TDsafUri.docFileInputStream( pfd ) );
        String pathname = uri[0].getPath();
        if ( pathname.toLowerCase( Locale.getDefault() ).endsWith( ".grid" ) ) {
          dem = new DEMgridParser( isr, pathname, mDEMmaxsize );
        } else if ( pathname.toLowerCase( Locale.getDefault() ).endsWith( ".asc" ) || pathname.toLowerCase( Locale.getDefault() ).endsWith(".ascii") ) {
          Cave3DFix origin = mParser.getOrigin();
          // origin.log();
          double xunit = mParser.getWEradius(); // radius * PI/180
          double yunit = mParser.getSNradius(); // radius * PI/180
          // TDLog.v("xunit " + xunit + " yunit " + yunit );
          isr = new InputStreamReader( TDsafUri.docFileInputStream( pfd ) );
          dem = new DEMasciiParser( isr, pathname, mDEMmaxsize, false, xunit, yunit ); // false: flip horz
        } else { 
          return false;
        }
        if ( ! dem.valid() ) return false;
        final double dd = mDEMbuffer;
        // mParser = survey data parser
        // TDLog.v("BBox X " + mParser.emin + " " + mParser.emax + " Y " + mParser.nmin + " " + mParser.nmax + " Z " + mParser.zmin + " " + mParser.zmax );
        dem.readData( mParser.emin - dd, mParser.emax + dd, mParser.nmin - dd, mParser.nmax + dd );
        return dem.valid();
      }

      public void onPostExecute( Boolean b )
      {
        if ( b ) {
          if ( mRenderer != null ) mRenderer.notifyDEM( dem );
          TDToast.make( R.string.dem_ok );
        } else {
          mDEMname = null; // failed
          TDToast.make( R.string.dem_failed );
        }
        onDEMloading = false;
      }
    }).execute( uri );
  }

  // TEMPERATURE
  // void openTemperature( Uri uri )
  // {
  //   if ( mRenderer == null ) return;
  //   String pathname = uri.getPath();
  //   try {
  //     InputStream is = this.getContentResolver().openInputStream( uri );
  //     InputStreamReader isr = ( is == null )? null : new InputStreamReader( is );
  //     loadTemperature( isr, pathname );
  //   } catch ( FileNotFoundException e ) {
  //   }
  // }

  // private void loadTemperature( final InputStreamReader isr, final String pathname )
  // {
  //   (new AsyncTask<String, Void, Boolean>() {
  //     public Boolean doInBackground( String ... files ) {
  //       // String file = files[0];
  //       boolean ret = ( mParser == null )? false : mParser.computeTemperature( isr, pathname );
  //       if ( ret && mRenderer != null ) mRenderer.prepareTemperatures();
  //       return ret;
  //     }

  //     public void onPostExecute( Boolean b )
  //     {
  //       if ( b ) {
  //         TDToast.make( R.string.temperature_ok );
  //       } else {
  //         TDToast.make( R.string.temperature_failed );
  //       }
  //     }
  //   }).execute( pathname );
  // }
  // END TEMPERATURE

  /** load a texture file (either GeoTIFF or OSM)
   * @param uri   texture-file uri
   */
  void openTexture( Uri uri )
  {
    if ( mRenderer == null ) return;
    final RectF  bounds = mRenderer.getSurfaceBounds();
    if ( bounds == null ) return;

    String pathname = uri.getPath();
    String filename = uri.getLastPathSegment();
    // TDLog.v("Texture Path " + pathname + " file " + filename );
    // get the model tie-point (upper left corner), the width-height, and the pixel scale
    
    TDLog.v("texture " + pathname + " bbox " + bounds.left + " " + bounds.bottom + "  " + bounds.right + " " + bounds.top );
    ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
    InputStreamReader isr = new InputStreamReader( TDsafUri.docFileInputStream( pfd ) );

    mTextureName = filename;
    if ( filename.toLowerCase( Locale.getDefault() ).endsWith( ".osm" ) ) {
      loadTextureOSM( isr, pathname, bounds );
    } else if ( filename.toLowerCase( Locale.getDefault() ).endsWith( ".tif" ) || filename.toLowerCase( Locale.getDefault() ).endsWith( ".tiff" )) {
      loadTextureGeotiff( isr, pathname, bounds );
    }
  }

  /** load a geotiff texture from file
   * @param isr      the input-stream reader is not used
   * @param pathname TIFF file full pathname
   * @param bounds   clipping rectangle
   */
  private void loadTextureGeotiff( final InputStreamReader isr, final String pathname, final RectF bounds )
  {
    TDToast.makeLong("TIFF " + pathname );
    String filepath = pathname.replaceFirst(".*:", mTextureRoot ); // new File( file ).getAbsolutePath();
    (new AsyncTask<String, Void, Integer>() {
      Bitmap bitmap = null;

      // @return: 0 ok, 1 fail, 2 no-lib
      public Integer doInBackground( String ... files ) {
        String file = files[0];
        // String filepath = file.replaceFirst(".*:", mTextureRoot ); // new File( file ).getAbsolutePath();
        // TDLog.v("texture [1] " + pathname );
        try { 
          bitmap = (Bitmap)( TiffFactory.getBitmap( filepath, bounds.left, bounds.bottom, bounds.right, bounds.top ) );
        } catch ( java.lang.UnsatisfiedLinkError e ) {
          TDLog.e( e.getMessage() );
          return 1; 
        }
        if ( bitmap != null ) {
          TDLog.v("texture [2] " + filepath + " size " + bitmap.getWidth() + " " + bitmap.getHeight() );
        } else {
          TDLog.v("texture [3] " + filepath + " null bitmap ");
        }

        return (bitmap != null)? 0 : 2;
      }

      // @param b exec return value
      public void onPostExecute( Integer b )
      {
        switch ( b.intValue() ) {
          case 0: 
            if ( mRenderer != null ) mRenderer.notifyTexture( bitmap ); // FIXME do in doInBackground
            TDToast.make( R.string.texture_ok );
            break;
          case 1:
            TDToast.make( R.string.no_native_lib );
            break;
          // case 2:
          default:
            TDToast.make( R.string.texture_failed );
        }
        onDEMloading = false;
      }
    }).execute( pathname );
  }

  /** load an OSM texture file
   * @param isr      file input stream
   * @param pathname file pathname
   * @param bounds   clipping rectangle
   */
  private void loadTextureOSM( final InputStreamReader isr, final String pathname, final RectF bounds )
  {
    (new AsyncTask<String, Void, Boolean>() {
      Bitmap bitmap = null;

      public Boolean doInBackground( String ... files ) {
        String file = files[0];
        Cave3DFix origin = mParser.getOrigin();
        if ( origin == null ) {
          TDLog.e("OSM with null origin");
          return false;
        } 

        OsmFactory osm = new OsmFactory( bounds.left, bounds.bottom, bounds.right, bounds.top, origin );
        bitmap = osm.getBitmap( isr, pathname );
        // if ( bitmap != null ) {
        //   // TDLog.v("texture " + file + " size " + bitmap.getWidth() + " " + bitmap.getHeight() );
        // }

        return (bitmap != null);
      }

      public void onPostExecute( Boolean b )
      {
        if ( isr != null ) {
          try {
            isr.close();
          } catch ( IOException e ) {
            TDLog.e( e.getMessage() );
          }
        }
        if ( b ) {
          if ( mRenderer != null ) mRenderer.notifyTexture( bitmap ); // FIXME do in doInBackground
          TDToast.make( R.string.texture_ok );
        } else {
          TDToast.make( R.string.texture_failed );
        }
        onDEMloading = false;
      }
    }).execute( pathname );
  }

  // ---------------------------------------- PERMISSIONS
  // TglPerms perms_dialog = null;
  static int mRequestPermissionTime = 1;

  /** check settings: load settings 
   */
  private void checkPreferences()
  {
    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
    loadPreferences( mPrefs );
    // mPrefs.registerOnSharedPreferenceChangeListener( this );

    // This is in TDVersion
    // try {
    //   VERSION = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName;
    //   VERSION_CODE = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionCode;
    // } catch ( NameNotFoundException e ) {
    //   e.printStackTrace(); // FIXME
    // }
  }

  /* FeatureChecker
  @Override
  public void onRequestPermissionsResult( int code, final String[] perms, int[] results )
  {
    // TDLog.v("PERM req code " + code + " results length " + results.length );
    if ( code == FeatureChecker.REQUEST_PERMISSIONS ) {
      if ( results.length > 0 ) {
	for ( int k = 0; k < results.length; ++ k ) {
	  FeatureChecker.GrantedPermission[k] = ( results[k] == PackageManager.PERMISSION_GRANTED );
	  // TDLog.v( "PERM " + k + " perms " + perms[k] + " result " + results[k] );
	}
        ++ mRequestPermissionTime;
        checkPermissions();
      }
    }
  }

  private void checkPermissions()
  {
    boolean granted = FeatureChecker.createPermissions( this, this, mRequestPermissionTime );
    // TDLog.v("PERM check perms: granted " + granted + " at time " + mRequestPermissionTime );
    // if ( ! TDandroid.canRun( mApp, this ) ) 
    if ( FeatureChecker.checkPermissions( this ) < 0 ) {
      if ( mRequestPermissionTime > 2 ) { 
        TDToast.make( "Permissions not granted. Goodbye" );
        finish();
      } else {
        TDToast.make( "Permissions not granted. Retry" );
      }
    } else {
      handleExtra();
    }
  }
  */

  // --------------------------------------- DIMENSIONS
  // private static int setListViewHeight( Context context, HorizontalListView listView )
  // {
  //   int size = TopoDroidApp.getScaledSize( context );
  //   if ( listView != null ) {
  //     LayoutParams params = listView.getLayoutParams();
  //     params.height = size + 10;
  //     listView.setLayoutParams( params );
  //   }
  //   return size;
  // }

  // // default button size
  // private static int getScaledSize( Context context )
  // {
  //   return (int)( TDSetting.mSizeButtons * context.getResources().getSystem().getDisplayMetrics().density );
  // }

  // private static int getDefaultSize( Context context )
  // {
  //   return (int)( 42 * context.getResources().getSystem().getDisplayMetrics().density );
  // }

  // private boolean isMultitouch()
  // {
  //   return getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
  // }

  public static final int DEM_SHRINK = 1;
  public static final int DEM_CUT    = 2;

  

  // ---------------------------------- PREFERENCES
  // private SharedPreferences mPrefs;

  public static float mSelectionRadius = 50.0f;
  // static int mButtonSize      = 1; // replaced 42 * mButtonSize by TDSetting.mSizeButtons
  // static boolean mPreprojection = true;
  public static float mDEMbuffer  = 200;
  public static int   mDEMmaxsize = 400;
  public static int   mDEMreduce  = DEM_SHRINK;
  public static String mTextureRoot = "/sdcard/"; // Environment.getExternalStorageDirectory().getPath();
  // static boolean mWallConvexHull = false;
  // static boolean mWallPowercrust = false;
  // static boolean mWallDelaunay   = false;
  // static boolean mWallHull       = false;
  public static boolean mStationDialog  = false;
  // static boolean mUseSplayVector = true; // ??? Hull with 3D splays or 2D splay projections
  public static boolean mMeasureToast   = false;
  // static boolean mWithBluetooth  = false; // FIXME BLUETOOTH  SETTING
  
  static public boolean mSplayProj      = false;
  static public float   mSplayThr       = 0.5f;

  // static final String CAVE3D_BASE_PATH        = "CAVE3D_BASE_PATH";
  // static final String CAVE3D_TEXT_SIZE        = "CAVE3D_TEXT_SIZE";
  // static final String CAVE3D_BUTTON_SIZE      = "CAVE3D_BUTTON_SIZE";

  // same as TDPrefKey.CAVE3D[]
  static final String CAVE3D_NEG_CLINO          = "CAVE3D_NEG_CLINO";
  static final String CAVE3D_STATION_POINTS     = "CAVE3D_STATION_POINTS";
  static final String CAVE3D_STATION_POINT_SIZE = "CAVE3D_STATION_POINT_SIZE";
  static final String CAVE3D_STATION_TEXT_SIZE  = "CAVE3D_STATION_TEXT_SIZE";
  static final String CAVE3D_SELECTION_RADIUS   = "CAVE3D_SELECTION_RADIUS";
  static final String CAVE3D_MEASURE_DIALOG     = "CAVE3D_MEASURE_DIALOG";
  static final String CAVE3D_STATION_TOAST      = "CAVE3D_STATION_TOAST";
  static final String CAVE3D_GRID_ABOVE         = "CAVE3D_GRID_ABOVE";
  static final String CAVE3D_GRID_EXTENT        = "CAVE3D_GRID_EXTENT";

  // static final String CAVE3D_BLUETOOTH_DEVICE = "CAVE3D_BLUETOOTH_DEVICE"; // FIXME BLUETOOTH SETTING

  // same as TDPrefKey.DEM3D[]
  static final String CAVE3D_DEM_BUFFER       = "CAVE3D_DEM_BUFFER";
  static final String CAVE3D_DEM_MAXSIZE      = "CAVE3D_DEM_MAXSIZE";
  static final String CAVE3D_DEM_REDUCE       = "CAVE3D_DEM_REDUCE";

  // WALLS category - same as TDPrefKey.WALLS3D
  static final String CAVE3D_SPLAY_USE        = "CAVE3D_SPLAY_USE";
  static final String CAVE3D_ALL_SPLAY        = "CAVE3D_ALL_SPLAY";
  static final String CAVE3D_SPLAY_PROJ       = "CAVE3D_SPLAY_PROJ";
  static final String CAVE3D_SPLAY_THR        = "CAVE3D_SPLAY_THR";
  static final String CAVE3D_SPLIT_TRIANGLES  = "CAVE3D_SPLIT_TRIANGLES";
  static final String CAVE3D_SPLIT_RANDOM     = "CAVE3D_SPLIT_RANDOM";
  static final String CAVE3D_SPLIT_STRETCH    = "CAVE3D_SPLIT_STRETCH";
  static final String CAVE3D_POWERCRUST_DELTA = "CAVE3D_POWERCRUST_DELTA";
  // static final String CAVE3D_CONVEX_HULL      = "CAVE3D_CONVEX_HULL";
  // static final String CAVE3D_POWERCRUST       = "CAVE3D_POWERCRUST";
  // static final String CAVE3D_DELAUNAY         = "CAVE3D_DELAUNAY";
  // static final String CAVE3D_HULL             = "CAVE3D_HULL";

/*
  public void onSharedPreferenceChanged( SharedPreferences sp, String k ) 
  {
    // Cave3DFile.checkAppBasePath( this );
    // if ( k.equals( CAVE3D_BASE_PATH ) ) { 
    //   Cave3DFile.setAppBasePath( sp.getString( k, Cave3DFile.HOME_PATH ) );
    // } else

    // if ( k.equals( CAVE3D_TEXT_SIZE ) ) {
    //   try {
    //     int size = Integer.parseInt( sp.getString( k, "10" ) );
    //     GlNames.setTextSize( size );
    //   } catch ( NumberFormatException e ) { }
    // } else if ( k.equals( CAVE3D_BUTTON_SIZE ) ) {
    //   try {
    //     int size = Integer.parseInt( sp.getString( k, "1" ) );
    //     if ( size > 0 ) mButtonSize = size;
    //   } catch ( NumberFormatException e ) { }
    //   resetButtonBar();
    // } else 

    if ( k.equals( CAVE3D_SELECTION_RADIUS ) ) { 
      try {
        float radius = Float.parseFloat( sp.getString( k, "50.0" ) );
        if ( radius > 0.0f ) mSelectionRadius = radius;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( CAVE3D_STATION_TOAST ) ) { 
      mStationDialog = sp.getBoolean( k, false );
    } else if ( k.equals( CAVE3D_STATION_POINTS ) ) { 
      GlModel.mStationPoints = sp.getBoolean( k, false );
    } else if ( k.equals( CAVE3D_STATION_POINT_SIZE ) ) { 
      try {
        GlNames.setPointSize( Integer.parseInt( sp.getString( k, "8" ) ) );
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( CAVE3D_STATION_TEXT_SIZE ) ) { 
      try {
        GlNames.setTextSize( Integer.parseInt( sp.getString( k, "20" ) ) );
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( CAVE3D_MEASURE_DIALOG ) ) { 
      mMeasureToast  = sp.getBoolean( k, false );
    } else if ( k.equals( CAVE3D_GRID_ABOVE ) ) { 
      GlModel.mGridAbove = sp.getBoolean( k, false );
    } else if ( k.equals( CAVE3D_GRID_EXTENT ) ) { 
      try {
        int extent = Integer.parseInt( sp.getString( k, "10" ) );
        if ( extent > 1 && extent < 100 ) GlModel.mGridExtent = extent;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( CAVE3D_NEG_CLINO ) ) { 
      GlRenderer.mMinClino = sp.getBoolean( k, false ) ? 90: 0;
    // } else if ( k.equals( CAVE3D_BLUETOOTH_DEVICE ) ) { // FIXME BLUETOOTH SETTING
    //   // TDLog.v("on bluetooth preference changed");
    //   checkBluetooth( sp.getString( k, "" ) );
    } else if ( k.equals( CAVE3D_ALL_SPLAY ) ) { 
      GlModel.mAllSplay = sp.getBoolean( k, true );
    } else if ( k.equals( CAVE3D_SPLAY_USE ) ) { 
      TglParser.mSplayUse = Integer.parseInt( sp.getString( k, "1" ) );
    } else if ( k.equals( CAVE3D_SPLAY_PROJ ) ) { 
      mSplayProj = sp.getBoolean( k, false );
    } else if ( k.equals( CAVE3D_SPLAY_THR ) ) { 
      try {
        float buffer = Float.parseFloat( sp.getString( k, "0.5" ) );
        mSplayThr = buffer; 
      } catch ( NumberFormatException e ) { }

    } else if ( k.equals( CAVE3D_DEM_BUFFER ) ) { 
      try {
        float buffer = Float.parseFloat( sp.getString( k, "200" ) );
        if ( buffer >= 0 ) mDEMbuffer = buffer;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( CAVE3D_DEM_MAXSIZE ) ) { 
      try {
        int size = Integer.parseInt( sp.getString( k, "400" ) );
        if ( size >= 50 ) mDEMmaxsize = size;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( CAVE3D_DEM_REDUCE ) ) { 
      try {
        int reduce = Integer.parseInt( sp.getString( k, "1" ) );
        if ( reduce == 1 ) mDEMreduce = DEM_SHRINK;
        else               mDEMreduce = DEM_CUT;
      } catch ( NumberFormatException e ) { }

    // } else if ( k.equals( CAVE3D_PREPROJECTION ) ) { 
    //   mPreprojection = sp.getBoolean( k, true );
    } else if ( k.equals( CAVE3D_SPLIT_TRIANGLES ) ) { 
      GlModel.mSplitTriangles = sp.getBoolean( k, true );
    } else if ( k.equals( CAVE3D_SPLIT_RANDOM ) ) { 
      try {
        float r = Float.parseFloat( sp.getString( k, "0.1" ) );
        if ( r > 0.0001f ) {
          GlModel.mSplitRandomizeDelta = r;
          GlModel.mSplitRandomize = true;
        } else {
          GlModel.mSplitRandomize = false;
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( CAVE3D_SPLIT_STRETCH ) ) { 
      try {
        float r = Float.parseFloat( sp.getString( k, "0.1" ) );
        if ( r > 0.0001f ) {
          GlModel.mSplitStretchDelta = r;
          GlModel.mSplitStretch = true;
        } else {
          GlModel.mSplitStretch = false;
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( CAVE3D_POWERCRUST_DELTA ) ) { 
      try {
        float delta = Float.parseFloat( sp.getString( k, "0.1" ) );
        if ( delta > 0 ) GlModel.mPowercrustDelta = delta;
      } catch ( NumberFormatException e ) { }

    // } else if ( k.equals( CAVE3D_CONVEX_HULL ) ) { 
    //   mWallConvexHull = sp.getBoolean( k, true );
    // } else if ( k.equals( CAVE3D_POWERCRUST ) ) { 
    //   mWallPowercrust = sp.getBoolean( k, false );
    //   setMenuAdapter( getResources() );
    // } else if ( k.equals( CAVE3D_DELAUNAY ) ) { 
    //   mWallDelaunay = sp.getBoolean( k, false );
    // } else if ( k.equals( CAVE3D_HULL ) ) { 
    //   mWallHull = sp.getBoolean( k, false );
    }
  }
*/

  /** load the 3D viewer settings
   * @param sp shared preferences
   */
  private void loadPreferences( SharedPreferences sp )
  {
    float r;
    // Cave3DFile.checkAppBasePath( this ); // BLUETOOTH
    // Cave3DFile.setAppBasePath( sp.getString( CAVE3D_BASE_PATH, Cave3DFile.HOME_PATH ) );
    // try {
    //   int size = Integer.parseInt( sp.getString( CAVE3D_TEXT_SIZE, "10" ) );
    //   GlNames.setTextSize( size );
    // } catch ( NumberFormatException e ) { }
    GlNames.setTextSize( (int)(TDSetting.mStationSize) );

    // try {
    //   int size = Integer.parseInt( sp.getString( CAVE3D_BUTTON_SIZE, "1" ) );
    //   if ( size > 0 ) {
    //     mButtonSize = size;
    //     resetButtonBar();
    //   }
    // } catch ( NumberFormatException e ) { }

    try {
      float radius = Float.parseFloat( sp.getString( CAVE3D_SELECTION_RADIUS, "50.0" ) );
      if ( radius > 0.0f ) mSelectionRadius = radius;
    } catch ( NumberFormatException e ) {
      mSelectionRadius = 50;
    }
    mStationDialog = sp.getBoolean( CAVE3D_STATION_TOAST, false );
    GlModel.mStationPoints = sp.getBoolean( CAVE3D_STATION_POINTS, false );
    try {
      GlNames.setPointSize( Integer.parseInt( sp.getString( CAVE3D_STATION_POINT_SIZE, "8" ) ) );
    } catch ( NumberFormatException e ) {
      GlNames.setPointSize( 8 );
    }
    try {
      GlNames.setTextSize( Integer.parseInt( sp.getString( CAVE3D_STATION_TEXT_SIZE, "20" ) ) );
    } catch ( NumberFormatException e ) { 
      GlNames.setTextSize( 20 );
    }
    mMeasureToast  = sp.getBoolean( CAVE3D_MEASURE_DIALOG, false );
    GlModel.mGridAbove = sp.getBoolean( CAVE3D_GRID_ABOVE, false );
    try {
      int extent = Integer.parseInt( sp.getString( CAVE3D_GRID_EXTENT, "10" ) );
      if ( extent > 1 && extent < 1000 ) GlModel.mGridExtent = extent;
    } catch ( NumberFormatException e ) {
      GlModel.mGridExtent = 10;
    }
    GlRenderer.mMinClino  = sp.getBoolean( CAVE3D_NEG_CLINO, false ) ? 90 : 0;
    GlModel.mAllSplay     = sp.getBoolean( CAVE3D_ALL_SPLAY, true );
    TglParser.mSplayUse   = Integer.parseInt( sp.getString( CAVE3D_SPLAY_USE, "1" ) );
    // TDLog.v("TopoGL load BT preference");
    // checkBluetooth( sp.getString( CAVE3D_BLUETOOTH_DEVICE, "" ) ); // FIXME BLUETOOTH SETTING
    mSplayProj = sp.getBoolean( CAVE3D_SPLAY_PROJ, false );
    try {
      float buffer = Float.parseFloat( sp.getString( CAVE3D_SPLAY_THR, "0.5" ) );
      mSplayThr = buffer;
    } catch ( NumberFormatException e ) {
      mSplayThr = 0.5f;
    }
    try {
      float buffer = Float.parseFloat( sp.getString( CAVE3D_DEM_BUFFER, "200" ) );
      if ( buffer >= 0 ) mDEMbuffer = buffer;
    } catch ( NumberFormatException e ) {
      mDEMbuffer = 200;
    }
    try {
      int size = Integer.parseInt( sp.getString( CAVE3D_DEM_MAXSIZE, "400" ) );
      if ( size >= 50 ) mDEMmaxsize = size;
    } catch ( NumberFormatException e ) {
      mDEMmaxsize = 400;
    }
    try {
      int reduce = Integer.parseInt( sp.getString( CAVE3D_DEM_REDUCE, "1" ) );
      if ( reduce == 1 ) mDEMreduce = DEM_SHRINK;
      else               mDEMreduce = DEM_CUT;
    } catch ( NumberFormatException e ) {
      mDEMreduce = DEM_SHRINK;
    }

    // mPreprojection  = sp.getBoolean( CAVE3D_PREPROJECTION, true );
    GlModel.mSplitTriangles = sp.getBoolean( CAVE3D_SPLIT_TRIANGLES, true );
    GlModel.mSplitRandomize = false;
    try {
      r = Float.parseFloat( sp.getString( CAVE3D_SPLIT_RANDOM, "0.1" ) );
      if ( r > 0.0001f ) {
        GlModel.mSplitRandomizeDelta = r;
        GlModel.mSplitRandomize = true;
      }
    } catch ( NumberFormatException e ) {
      GlModel.mSplitRandomizeDelta = 0.1f;
      GlModel.mSplitRandomize = true;
    }
    try {
      r = Float.parseFloat( sp.getString( CAVE3D_SPLIT_STRETCH, "0.1" ) );
      if ( r > 0.0001f ) {
        GlModel.mSplitStretchDelta = r;
        GlModel.mSplitStretch = true;
      }
    } catch ( NumberFormatException e ) {
      // GlModel.mSplitStretchDelta = 0.1f;
      GlModel.mSplitStretch = false;
    }
    // mWallConvexHull = sp.getBoolean( CAVE3D_CONVEX_HULL, true );
    // mWallPowercrust = sp.getBoolean( CAVE3D_POWERCRUST,  false );
    // mWallDelaunay   = sp.getBoolean( CAVE3D_DELAUNAY,    false );
    // mWallHull       = sp.getBoolean( CAVE3D_HULL,        false );
  }

  // ---------------------------------------------------------------

  /** first "init rendering"
   * @param survey    survey name
   * @param base      TopoDroid base folder
   * @return true if success
   * FIXME isr == null
   */
  private boolean initRendering1( String survey, String base ) 
  {
    TDLog.v("TopoGL init rendering " + survey + " base " + base );
    doSketches = false;
    try {
      // FIXME null InputStream
      mParser = new ParserTh( this, null, survey, base ); // survey data directly from TopoDroid database
      CWConvexHull.resetCounters();
      if ( mRenderer != null ) {
        mRenderer.clearModel();
        mRenderer.setParser( mParser, true );
      }
      doSketches = true;
      // TDLog.v( "Station " + mParser.getStationNumber() + " shot " + mParser.getShotNumber() );
    } catch ( ParserException e ) {
      TDToast.makeBad( TDInstance.formatString( R.string.error_parser_error, survey + " " + e.msg() ) );
      mParser = null;
      // TDLog.e( "parser exception " + filename );
    }
    return (mParser != null);
  }

  /** second "init rendering"
   * @param is         file input stream
   * @param filepath   file path 
   * @param surveyname survey name
   * @return true if success
   * @note called only by doOpenFile
   */
  private boolean initRendering2( InputStream is, String filepath, String surveyname )
  {
    doSketches = false;
    String ext  = TDPath.getExtension( filepath );
    String name = TDPath.getMainname( filepath );
    TDLog.v("init rendering (2) file " + filepath + " survey " + surveyname + " name " + name + " ext " + ext );
    try {
      mParser = null;
      if ( mRenderer != null ) mRenderer.clearModel();
      // resetAllPaths();
      if ( ext.equals( "tdconfig" ) ) { // isr not used  // FIXME may null pointer
        TDLog.v("init rendering (2) TDCONGIF"); // TopoDroid TDM projects
        InputStreamReader isr = ( is == null )? null : new InputStreamReader( is );
        mParser = new ParserTh( this, isr, filepath, ParserTh.TDCONFIG ); // tdconfig files are saved with therion syntax
        doSketches = true;
      } else if ( ext.equals( "thconfig" ) ) { // isr not used
        TDLog.v("init rendering (2) THCONGIF");
        InputStreamReader isr = ( is == null )? null : new InputStreamReader( is );
        mParser = new ParserTh( this, isr, filepath, ParserTh.THCONFIG ); 
      } else if ( ext.equals( "th" ) ) { // isr not used
        InputStreamReader isr = ( is == null )? null : new InputStreamReader( is );
        mParser = new ParserTh( this, isr, filepath, ParserTh.TH ); 

      } else if ( ext.equals( "mak" ) ) {
        InputStreamReader isr = ( is == null )? null : new InputStreamReader( is );
        mParser = new ParserDat( this, isr, name, filepath );
      } else if ( ext.equals( "dat" ) ) {
        InputStreamReader isr = ( is == null )? null : new InputStreamReader( is );
        mParser = new ParserDat( this, isr, name );
      } else if ( ext.equals( "tro" ) ) {
        InputStreamReader isr = ( is == null )? null : new InputStreamReader( is );
        mParser = new ParserTro( this, isr, name );
      } else if ( ext.equals( "trox" ) ) {
        InputStreamReader isr = ( is == null )? null : new InputStreamReader( is );
        mParser = new ParserTrox( this, isr, name );

      } else if ( ext.equals( "lox" ) ) {
        DataInputStream dis = new DataInputStream( is );
        mParser = new ParserLox( this, dis, name );
      } else if ( ext.equals( "3d" ) ) {
        DataInputStream dis = new DataInputStream( is );
        mParser = new Parser3d( this, dis, name );
      } else {
        return false;
      }
      // CWConvexHull.resetCounters();
      // if ( mRenderer != null ) mRenderer.setParser( mParser, true );
      // TDLog.v("Station " + mParser.getStationNumber() + " shot " + mParser.getShotNumber() + " splay " + mParser.getSplayNumber() + " surveys " + mParser.getSurveyNumber() );
    } catch ( ParserException e ) {
      // TDLog.e("parser exception " + e.msg() );
      TDToast.makeBad( TDInstance.formatString( R.string.error_parser_error, e.msg() ) );
      mParser = null;
    }
    return (mParser != null);
  }

  /** toast after a notification
   * @param res   notification result, either true or false
   * @param ok    resource for true result
   * @param no    resource for false result
   */
  private void notify( boolean res, int ok, int no )
  {
    if ( res ) {
      TDToast.make( ok );
    } else {
      TDToast.make( no );
    }
  }

  /** react to notification of wall-compute completion
   * @param type   computation type
   * @param result true if computation successful
   * @note run on onPostExecute
   */
  void notifyWall( int type, boolean result )
  {
    // TDLog.v("TopoGL app notify wall " + type );
    if (type == TglParser.WALL_CW ) {
      notify( result, R.string.done_convexhull, R.string.fail_convexhull );
    } else if ( type == TglParser.WALL_POWERCRUST ) {
      notify ( result, R.string.done_powercrust, R.string.fail_powercrust );
    } else if ( type == TglParser.WALL_HULL ) {
      notify ( result, R.string.done_hull, R.string.fail_hull );
    } else if ( type == TglParser.WALL_TUBE ) {
      notify ( result, R.string.done_tube, R.string.fail_tube );
    } else if ( type == TglParser.WALL_BUBBLE ) {
      notify ( result, R.string.done_bubble, R.string.fail_bubble );
    }
    if ( mRenderer != null ) mRenderer.notifyWall( type, result );
  }

  /** refresh the display
   */
  void refresh()
  {
    // TDLog.v("TopoGL refresh. mode " + mRenderer.projectionMode );
    if ( mRenderer != null ) setTheTitle( mRenderer.getAngleString() );
    if ( glSurfaceView != null ) { // neither of these help
      glSurfaceView.requestRender(); 
      // glSurfaceView.onTouchEvent( null );
    }
  }

  // /** open the dialog to load a survey sketch (c3d format)
  //  */
  // void loadSketch() // NO_C3D
  // {
  //   new DialogSketch( this, this ).show();
  // }

  /** react to location notify
   * @param lng longitude [WGS84 decimal degrees]
   * @param lat latitude
   * @param h_ell ellipsoid altitude [m]
   * @note WITH-GPS
   */
  public void notifyLocation( double lng, double lat, double h_ell )
  {
    // TDLog.v("TopoGL GPS notified location " + lng + " " + lat );
    // TODO
    // [1] convert to model CRS
    if ( mParser != null && mParser.hasWGS84() ) {
      double n = mParser.latToNorth( lat, h_ell );
      double e = mParser.lngToEast( lng, lat, h_ell, n );
      // TDLog.v("TopoGL GPS has origin " + mParser.hasOrigin() + " location " + e + " " + n );
      // [2] get Z from surface
      // [3] mRenderer.setLocation( new Vector3D( e, n, z ) );
      addGPSpoint( e, n );
    }
  }

/* ------------
  final static int CRS_CONVERSION_REQUEST = 2;
  final static int CRS_INPUT_REQUEST = 3; 

  void doProj4Conversion( String cs_to, double lng, double lat )
  {
    double h_geo = 0;
    // if ( cs_to == null ) return;
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "request", "CRS_CONVERSION_REQUEST" ); // Proj4 request
      intent.putExtra( "cs_from", "Long-Lat" ); // NOTE MUST USE SAME NAME AS Proj4
      intent.putExtra( "cs_to", cs_to ); 
      intent.putExtra( "longitude", lng );
      intent.putExtra( "latitude",  lat );
      intent.putExtra( "altitude",  h_geo );
      startActivityForResult( intent, CRS_CONVERSION_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      // TODO TDToast.makeBad( R.string.no_proj4 );
    }
  }

  void getProj4Coords( )
  {
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "request", "CRS_INPUT_REQUEST" ); // Proj4 request
      startActivityForResult( intent, CRS_INPUT_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      // TODO TDToast.makeBad( R.string.no_proj4 );
    }
  }

  public void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    // mApp.resetLocale(); // OK-LOCALE
    if ( resCode == Activity.RESULT_OK ) {
      if ( reqCode == CRS_CONVERSION_REQUEST ) {
        Bundle bundle = intent.getExtras();
        if ( bundle != null ) {
          String cs = bundle.getString( "cs_to" );
          double e  = bundle.getDouble( "longitude");
          double n = bundle.getDouble( "latitude");
          // double h_geo = bundle.getDouble( "altitude");
	  // long   n_dec = bundle.containsKey( "decimals" )? bundle.getLong( "decimals" ) : 2;
          addGPSpoint( e, n );
        }
      } else if ( reqCode == CRS_INPUT_REQUEST ) {
        Bundle bundle = intent.getExtras();
        if ( bundle != null ) {
          // bundle.getDouble( "longitude" )
          // bundle.getDouble( "latitude" )
          // bundle.getDouble( "altitude" )
        }
      }
    }
  }
------------ */

  // WITH-GPS -----------------------------------------------------------------
  /** set the GPS status
   * @param status  either on (true) or off (false)
   */
  void setGPSstatus( boolean status )
  {
    // TDLog.v("TopoGL GPS set status " + status );
    if ( mGPS == null ) return;
    if ( status ) {
      mGPS.setGPSon();
      mGPS.setListener( this );
    } else {
      mGPS.setGPSoff();
      mGPS.setListener( null );
    }
  }

  /** @return the GPS status (on or off)
   */  
  boolean getGPSstatus()
  {
    return mGPS != null && mGPS.mIsLocating;
  }

  /** add a geolocalized point
   * @param e   East coordinate
   * @param n   North coordinate
   */
  void addGPSpoint( double e, double n )
  {
    double z = mRenderer.getDEM_Z( e, n );
    if ( z > 0 ) {
      z += 0.1; // add 0.1 meter
      // TDLog.v("TopoGL GPS set location " + e + " " + n + " " + z );
      mRenderer.setLocation( new Vector3D( e, n, z ) );
    } else {
      TDLog.e("GPS location " + e + " " + n + " out of DEM" );
    }
  }
  // end WITH-GPS ------------------------------------------------------------

  /** forward to the rendered a list of surveys to hide or show
   * @param surveys  list of surveys
   */
  void hideOrShow( List< Cave3DSurvey > surveys )
  {
    if ( mRenderer != null ) {
      mRenderer.hideOrShow( surveys );
    }
  }

  void setSurveyColors(  List< Cave3DSurvey > surveys )
  {
    if ( mRenderer != null ) {
      mRenderer.setSurveyColors( surveys );
    }
  }

  // ---------------------------------------- EXPORT

  /** export model as GTLF
   * @note this is run inside ExportTask
   * @note export model as GLTF works only with pathname, not with URI 
   * @param type      must be GLTF
   * @param fos       output stream
   * @param pathname  export file path 
   * @param export    export data struct
   * @return true if success
   */
  public boolean exportGltfModel( int type, DataOutputStream fos, final String pathname, ExportData export )
  { 
    if ( type == ModelType.GLTF ) {
      return mRenderer.exportGltf( fos, pathname, export );
    }
    return false;
  }

  /** export model as Shapefile
   * @param type      export type (SHP_ASCII)
   * @param fos       output stream
   * @param pathname  ... 
   * @param export    export data struct
   * @return true if success
   */
  public boolean exportShpModel( int type, DataOutputStream fos, final String pathname, ExportData export )
  { 
    if ( type == ModelType.SHP_ASCII ) {
      return mParser.exportShp( fos, pathname, export );
    }
    return false;
  }

  /*
  // BLUETOOTH -----------------------------------------------------------------------

  static int bearing = 0; 
  static int clino   = 0;
  
  private final static int BLUETOOTH_DOWN   = 0;
  private final static int BLUETOOTH_OFF    = 1;
  private final static int BLUETOOTH_ON     = 2;
  private final static int BLUETOOTH_WAIT   = 3;
  private final static int BLUETOOTH_READY  = 4;
  private final static int BLUETOOTH_LASER  = 5;
  private final static int BLUETOOTH_SHOT   = 6;
  private final static int BLUETOOTH_SCAN   = 7;

  private final static String[] BtState = { "DOWN", "OFF", "ON|", "WAIT", "READY", "LASER", "SHOT", "SCAN" };

  private String          mBtRemoteName = null;
  private BluetoothDevice mBtRemoteDevice = null;
  private BluetoothComm   mBluetoothComm = null;
  private int     mBleStatus = ConnectionState.CONN_DISCONNECTED;
  private int     mBluetoothState = BLUETOOTH_OFF; 

  private final static int DATA_NONE  = 0;
  private final static int DATA_SHOT  = 1;
  private final static int DATA_SPLAY = 2;
  private int mDataType = DATA_NONE;

  void sendCommand( int cmd ) 
  { 
    // if ( mBluetoothComm == null ) return;
    // if ( ! mBluetoothComm.isConnected() ) return;
    // if ( cmd == BluetoothCommand.CMD_LASER ) {
    //   mDataType = DATA_NONE;
    //   mBluetoothComm.sendCommand( BluetoothCommand.CMD_LASER );
    // } else if ( cmd == BluetoothCommand.CMD_SCAN ) {
    //   mDataType = DATA_SPLAY;
    //   mBluetoothComm.sendCommand( BluetoothCommand.CMD_SCAN );
    // } else if ( cmd == BluetoothCommand.CMD_SHOT ) {
    //   mDataType = DATA_SHOT;
    //   mBluetoothComm.sendCommand( BluetoothCommand.CMD_LASER );
    //   BleUtils.slowDown( 500 );
    //   mBluetoothComm.sendCommand( BluetoothCommand.CMD_SHOT );
    // }
  }

  // ------------------------------------------------------------------
  // BT state and button
  //

  public int getBluetoothState() { return mBluetoothState; }

  // check if there is a connectable BLUETOOTH device
  public boolean hasBluetoothName() { return mBtRemoteName != null; }

  private boolean hasBluetoothComm() { return mBluetoothComm != null; } 


  public void notifyStatus( int status )
  {
    // TDLog.v("Topo GL app notify status " + ConnectionState.statusString[ status ] );
    mBleStatus = status;
    switch ( status ) {
      case ConnectionState.CONN_DISCONNECTED:
        setBluetoothState( BLUETOOTH_ON );
        break;
      case ConnectionState.CONN_CONNECTED:
        setBluetoothState( BLUETOOTH_READY );
        break;
      case ConnectionState.CONN_WAITING:
        setBluetoothState( BLUETOOTH_WAIT );
        break;
    }
  }

  public void onShotData()
  {
    // TDLog.v("Topo GL on shot data ... BT state " + BtState[ mBluetoothState ] );
    if ( mBluetoothState == BLUETOOTH_SHOT ) {
      setBluetoothState( BLUETOOTH_READY );
    }
  }

  private boolean startBluetooth()
  {
    // TDLog.v("starting bluetooth - remote " + mBtRemoteName );
    if ( mBluetoothComm == null ) {
      // mBluetoothComm = new BluetoothComm( this, this, mBtRemoteDevice );
      if ( Device.isBric( mBtRemoteName ) ) {
        mBluetoothComm = new BricComm( this, this, mBtRemoteDevice );
      } else if ( Device.isSap( mBtRemoteName ) ) {
        mBluetoothComm = new SapComm( this, this, mBtRemoteDevice );
      } else if ( mBtRemoteName.startsWith( Device.NAME_DISTOX2 ) ) {
        mBluetoothComm = new DistoXComm( this, this, mBtRemoteDevice, mBtRemoteDevice.getAddress() );
      } else if ( mBtRemoteName.startsWith( Device.NAME_DISTOXBLE ) ) {
        mBluetoothComm = new DistoXBLEComm( this, this, mBtRemoteDevice, mBtRemoteDevice.getAddress() );
      }
      // if ( mBluetoothComm != null ) {
      //   mDataType = DATA_NONE;
      //   mBluetoothState = BLUETOOTH_READY;
      //   // (new AsyncTask<Void, Void, Boolean>() {
      //   //   @Override public Boolean doInBackground(Void ... v ) {
      //       boolean ret = mBluetoothComm.connectDevice( lister ? );
      //       setBluetoothParser( mBtRemoteName );
      //   //     return ret;
      //   //   }
      //   //   @Override public void onPostExecute( Boolean ret )
      //   //   {
      //       if ( ret ) {
      //         // TDLog.v("connect OK");
      //         setBluetoothState( BLUETOOTH_READY );
      //       } else {
      //         // TDLog.v("connect failed");
      //         setBluetoothState( BLUETOOTH_OFF );
      //       }
      //   //   }
      //   // } ).execute();
      // }
    }
    setBluetoothState( (mBluetoothComm != null)? BLUETOOTH_OFF : BLUETOOTH_DOWN );
    return ( mBluetoothComm != null);
  }

  private void stopBluetooth()
  {
    // TDLog.v("stop bluetooth - remote " + ( (mBtRemoteName != null)? mBtRemoteName : "null") );
    if ( hasBluetoothComm() ) {
      mBluetoothComm.disconnectDevice();
      // mBluetoothComm = null;
      // mBtRemoteName  = null;
    }
    mDataType = DATA_NONE;
    setBluetoothState( BLUETOOTH_OFF );
    closeBluetoothSurvey();
  }

  private void shutdownBluetooth( boolean set_state )
  {
    // TDLog.v("shutdown bluetooth - remote " + ( (mBtRemoteName != null)? mBtRemoteName : "null") );
    if ( hasBluetoothComm() ) {
      mBluetoothComm.disconnectDevice();
    }
    mBluetoothComm = null;
    mDataType = DATA_NONE;
    // mBtRemoteName  = null; // hasBluetoothName() returns false
    if ( set_state ) setBluetoothState( BLUETOOTH_DOWN );
    closeBluetoothSurvey();
  }

  // @param name   BT remote device name
  private void checkBluetooth( String name )
  {
    if ( ! BLUETOOTH ) return;
    boolean with_bluetooth = checkBluetoothName( name );
    // TDLog.v("check bluetooth " + name + " " + with_bluetooth );
    mWithBluetooth = with_bluetooth;
    if ( mButton1 != null ) {
      setBluetoothState( hasBluetoothName()? BLUETOOTH_OFF : BLUETOOTH_DOWN );
    }
    if ( ! mWithBluetooth ) stopBluetooth();
    setMenuAdapter( getResources() );
  }

  private boolean checkBluetoothName( String name )
  {
    mBtRemoteName   = null;
    mBtRemoteDevice = null;
    if ( ! BLUETOOTH ) return false;
    if ( TDString.isNullOrEmpty( name ) ) return false;
    // WARNING BT name must have a prefix "++"
    // if ( ! ( name.startsWith("++") ) ) return false;
    // name = name.substring( 2 );
    // TDLog.v("check BT name <" + name + ">" );

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter == null ) {
      TDLog.e("TopoGL check BT name : no adapter");
      return false;
    }
    Set< BluetoothDevice > devices = adapter.getBondedDevices();
    if ( devices == null ) {
      TDLog.e("TopoGL check BT name : no devices");
      return false;
    }
    for ( BluetoothDevice device : devices ) {
      if ( device.getName().equals( name ) ) {
        // TDLog.v("TopoGL check BT name : found device");
        if ( ! name.equals( mBtRemoteName ) && mBluetoothComm != null ) {
          shutdownBluetooth( false );
        }
        mBtRemoteName   = name;
        mBtRemoteDevice = device;
        return true;
      }
    }
    // TDLog.v("check BT name : device not found");
    return false;
  }

  private void doConnectDevice()
  {
    // TDLog.v("Cave3D connect device");
    if ( mBluetoothComm != null ) {
      (new AsyncTask<Void, Void, Void>() {
        @Override public Void doInBackground(Void ... v ) {
          mBluetoothComm.connectDevice( lister ? );
          return null;
        }
      } ).execute();
    } else {
      TDLog.e("TopoGL Error null BT comm");
    }
  }

  private void doDisconnectDevice()
  {
    mBluetoothComm.disconnectDevice();
  }

  // Bluetooth states                            (connected)                       ======> SCAN
  //              (start)      (open)                       \        ------> LASER ------> SHOT
  //                     \    /      \                        READY  <-------------(data)-----
  // DOWN<==(shutdown)=== OFF          \    <===(disconnect)===={    <========================
  //                       <===(stop)=== ON -----(connect)---> WAIT 
  //                                        <--(disconnect)---     \
  //                                                                (wait)
  //             

  private void doBluetoothClick()
  {
    // TDLog.v("BT click: state " + BtState[mBluetoothState] + " name " + mBtRemoteName + " has BT comm " + hasBluetoothComm() + " has Bt name " + hasBluetoothName() );
    if ( hasBluetoothName() ) {
      switch ( mBluetoothState ) {
        case BLUETOOTH_DOWN:
          TDToast.makeBad( R.string.bt_not_started );
          break;
        case BLUETOOTH_OFF:
          TDToast.makeBad( R.string.bt_no_survey );
          // startBluetooth();
          // setBluetoothState( (mBluetoothComm == null)? BLUETOOTH_OFF : BLUETOOTH_ON );
          break;
        case BLUETOOTH_ON: 
          setBluetoothState( BLUETOOTH_WAIT );
          doConnectDevice();
          break;
        case BLUETOOTH_WAIT: 
          doDisconnectDevice();
          setBluetoothState( BLUETOOTH_ON );
          break;
        case BLUETOOTH_READY:
          if ( BLUETOOTH_REMOTE ) {
            // sendCommand( BluetoothCommand.CMD_LASER_ON );
            setBluetoothState( BLUETOOTH_LASER );
          }
          break;
        case BLUETOOTH_LASER: 
          if ( BLUETOOTH_REMOTE ) {
            // sendCommand( BluetoothCommand.CMD_SHOT );
            setBluetoothState( BLUETOOTH_SHOT );
          }
          break;
        case BLUETOOTH_SHOT: 
        case BLUETOOTH_SCAN: 
          // sendCommand( BluetoothCommand.CMD_LASER_OFF );
          setBluetoothState( BLUETOOTH_READY );
          break;
      }
    } else {
      TDToast.makeBad( R.string.bt_no_comm );
      // if ( mBluetoothState == BLUETOOTH_OFF ) {
      //   // TDLog.v("start BT ...");
      //   boolean ret = startBluetooth();
      //   // TDLog.v("start BT returns " + ret );
      //   setBluetoothState( (mBluetoothComm == null)? BLUETOOTH_OFF : BLUETOOTH_ON );
      // }
    }
  }

  private void doBluetoothLongClick()
  {
    // TDLog.v("bluetooth long click - state " + mBluetoothState );
    if ( hasBluetoothComm() ) {
      switch ( mBluetoothState ) {
        case BLUETOOTH_DOWN:
          TDToast.makeBad( R.string.bt_not_started );
          break;
        case BLUETOOTH_OFF:
          shutdownBluetooth( true );
          break;
        case BLUETOOTH_ON: 
          setBluetoothState( BLUETOOTH_OFF );
          stopBluetooth();
          break;
        case BLUETOOTH_WAIT: 
          doDisconnectDevice( new ListerHandler(this) ); // requires this to be ILister
          setBluetoothState( BLUETOOTH_ON );
          break;
        case BLUETOOTH_READY: 
          doDisconnectDevice();
          setBluetoothState( BLUETOOTH_ON );
          break;
        case BLUETOOTH_LASER:
          if ( BLUETOOTH_REMOTE ) {
            // sendCommand( BluetoothCommand.CMD_SCAN );
            setBluetoothState( BLUETOOTH_SCAN );
          }
          break;
        case BLUETOOTH_SHOT: 
        case BLUETOOTH_SCAN: 
          // sendCommand( BluetoothCommand.CMD_LASER_OFF );
          setBluetoothState( BLUETOOTH_READY );
          break;
      }
    } else {
      switch ( mBluetoothState ) {
        case BLUETOOTH_DOWN:
          TDToast.makeBad( R.string.bt_no_comm );
          break;
        case BLUETOOTH_OFF:
          shutdownBluetooth( true );
          break;
      }
    }
  }

  private void setBluetoothState( int state )
  {
    if ( mButton1 == null || mButton1[BTN_BLE] == null ) return;
    if ( ! BLUETOOTH ) return;
    // TDLog.v("set BT state " + BtState[state] + " device " + mBtRemoteName );
    mBluetoothState = state;
    if ( ! hasBluetoothName() ) {
      mButton1[BTN_BLE].setVisibility( View.GONE );
    } else {
      mButton1[BTN_BLE].setVisibility( View.VISIBLE );
      if ( mBluetoothState == BLUETOOTH_DOWN ) {
        mButton1[BTN_BLE].setBackgroundDrawable( mBMbleDown );
      } else if ( mBluetoothState == BLUETOOTH_OFF ) {
        mButton1[BTN_BLE].setBackgroundDrawable( mBMbleOff );
      } else if ( mBluetoothState == BLUETOOTH_ON ) {
        mButton1[BTN_BLE].setBackgroundDrawable( mBMbleOn );
      } else if ( mBluetoothState == BLUETOOTH_WAIT ) {
        mButton1[BTN_BLE].setBackgroundDrawable( mBMbleWait );
      } else if ( mBluetoothState == BLUETOOTH_READY ) {
        mButton1[BTN_BLE].setBackgroundDrawable( mBMbleReady );
      } else if ( mBluetoothState == BLUETOOTH_LASER ) {
        mButton1[BTN_BLE].setBackgroundDrawable( mBMbleLaser );
      } else if ( mBluetoothState == BLUETOOTH_SHOT ) {
        mButton1[BTN_BLE].setBackgroundDrawable( mBMbleShot );
      } else if ( mBluetoothState == BLUETOOTH_SCAN ) {
        mButton1[BTN_BLE].setBackgroundDrawable( mBMbleScan );
      }
    }
  }

  // BT SURVEY ---------------------------------------------------------
  private BluetoothSurvey mBtSurvey = null; // current survey

  void openBluetoothSurvey( BluetoothSurvey bt_survey )
  {
    if  ( bt_survey == null ) {
      // TDLog.v("start BT survey null");
      closeBluetoothSurvey();
      return;
    }
    // TDLog.v("start BT survey " + bt_survey.getNickname() );
    mBtSurvey = bt_survey;
    setSurveyName( mBtSurvey.getNickname() );
    if ( ! startBluetooth() ) {
      TDToast.makeBad( R.string.bt_no_comm );
      return;
    }
    setBluetoothState( (mBluetoothComm == null)? BLUETOOTH_OFF : BLUETOOTH_ON );
    setBluetoothParser( );
  }

  void closeBluetoothSurvey() 
  {
    // TDLog.v("close BT survey " + ( (mBtSurvey == null)? "null" : mBtSurvey.getNickname() ) );
    if ( mBtSurvey != null ) {
      // String filename = Cave3DFile.getBluetoothFilename( mBtSurvey.getNickname() ); // filename is in ParserBluetooth
      mBtSurvey.saveSurvey( );
    }
    mBtSurvey = null;
  }

  private void setBluetoothParser( )
  {
    // if ( bt_survey == null ) return; // this is guaranteed
    // TDLog.v("TopoGL set BT parser " + mBtSurvey.getNickname() );
    try {
      String filename = mBtSurvey.getFilename();
      String filepath = Cave3DFile.getBluetoothFilename( filename );
      ParserBluetooth bt_parser = new ParserBluetooth( this, filepath, filename );
      mBtSurvey.setBluetoothParser( bt_parser );
      mParser = bt_parser;
      mRenderer.setParser( mParser, false );
    } catch ( ParserException e ) { 
      // TODO
    }
  }

  private class DataLog 
  {
    double e, n, z;

    DataLog( double e0, double n0, double z0 )
    {
      e = e0;
      n = n0;
      z = z0; 
    }

    boolean isClose( DataLog log, double eps ) 
    {
      return log != null && Math.abs( e - log.e ) < eps && Math.abs( n - log.n ) < eps && Math.abs( z - log.z ) < eps;
    }
  } 

  private DataLog[] mDataLog = { null, null };
  private boolean   mOnShot  = false;
  private final static double EPS = 0.1;

  // add a leg attaching it to the selected station (or to the last station)
  public void handleManualLeg( double dist, double bear, double clino, boolean surface, boolean duplicate, boolean commented )
  {
    double h = dist * Math.cos( clino * Cave3DShot.DEG2RAD );
    double z = dist * Math.sin( clino * Cave3DShot.DEG2RAD );
    double n = h * Math.cos( bear * Cave3DShot.DEG2RAD );
    double e = h * Math.sin( bear * Cave3DShot.DEG2RAD );
    // DataLog data_log = new DataLog( e, n, z );
    if ( mBtSurvey != null && mBtSurvey.hasParser() ) {
      Cave3DShot leg = mBtSurvey.addLeg( dist, bear, clino, e, n, z );
      if ( leg != null ) {
        leg.setFlags( surface, duplicate, commented );
        mRenderer.addBluetoothStation( mBtSurvey.getLastStation() );
        mRenderer.addBluetoothLeg( leg );
      }
      mOnShot = false;
    }
  }

  public void handleRegularData( double dist, double bear, double clino )
  {
    double h = dist * Math.cos( clino * Cave3DShot.DEG2RAD );
    double z = dist * Math.sin( clino * Cave3DShot.DEG2RAD );
    double n = h * Math.cos( bear * Cave3DShot.DEG2RAD );
    double e = h * Math.sin( bear * Cave3DShot.DEG2RAD );
    // TDLog.v( String.format("TopoGL handle regular data %.2f %.1f %.1f --> %.2f %.2f %.2f", dist, bear, clino, e, n, z ) );
    DataLog data_log = new DataLog( e, n, z );
    boolean is_shot = data_log.isClose( mDataLog[0], EPS ) && data_log.isClose( mDataLog[1], EPS );
    mDataLog[1] = mDataLog[0];
    mDataLog[0] = data_log;
    if ( mBtSurvey != null && mBtSurvey.hasParser() ) {
      if ( is_shot ) { // if ( mDataType == DATA_SHOT ) 
        if ( ! mOnShot ) {
          Cave3DShot leg = mBtSurvey.addLeg( dist, bear, clino, e, n, z );
          if ( leg != null ) {
            mRenderer.addBluetoothStation( mBtSurvey.getLastStation() );
            mRenderer.addBluetoothLeg( leg );
          }
        } else {
        }
        mOnShot = true;
      } else { // if ( mDataType == DATA_SPLAY ) 
        Cave3DShot splay = mBtSurvey.addSplay( dist, bear, clino, e, n, z );
        if ( splay != null ) {
          mRenderer.addBluetoothSplay( splay );
        }
        mOnShot = false;
      }
    }
    onShotData();
  }
  // end BLUETOOTH ---------------------------------------------------------------
  */

  // --------------------------------------------------------------------------------------------
  final static private int REQUEST_DEM_FILE     = 101;
  final static private int REQUEST_TEXTURE_FILE = 102;
  final static private int REQUEST_IMPORT_FILE  = 103;
  final static private int REQUEST_EXPORT_FILE  = 104;
  // final static private int REQUEST_TEMPERATURE_FILE  = 105;

  private ExportData mExport = null; // type; FLAGS: splays, walls, surface, station; mime
  boolean onDEMloading = false;

  /** request selection of a DEM file
   */
  void selectDEMFile( )
  {
    onDEMloading = true;
    // selectFile( REQUEST_DEM_FILE, Intent.ACTION_OPEN_DOCUMENT, null, R.string.select_dem_file, null ); 
    selectFile( REQUEST_DEM_FILE, false, -1, R.string.select_dem_file, null ); 
  }

  /** request selection of a texture file
   */
  void selectTextureFile( )
  {
    onDEMloading = true;
    // selectFile( REQUEST_TEXTURE_FILE, Intent.ACTION_OPEN_DOCUMENT, null, R.string.select_texture_file, null ); 
    selectFile( REQUEST_TEXTURE_FILE, false, -1, R.string.select_texture_file, null ); 
  }

  /** request selection of an import file
   */
  void selectImportFile( )  
  {
    // selectFile( REQUEST_IMPORT_FILE,  Intent.ACTION_OPEN_DOCUMENT, null, R.string.select_survey_file,  null );
    selectFile( REQUEST_IMPORT_FILE,  false, -1, R.string.select_survey_file,  null );
  }

  // /** request selection of an attribute (temperature) file
  //  */
  // void selectTemperatureFile( )
  // {
  //   // selectFile( REQUEST_TEMPERATURE_FILE,  Intent.ACTION_OPEN_DOCUMENT, null, R.string.select_temp_file, null );
  //   selectFile( REQUEST_TEMPERATURE_FILE,  false, -1, R.string.select_temp_file, null );
  // }

  /** export file selection: send a request to select a file, on success return the model is exported
   * @param export   export data-struct
   */
  void selectExportFile( ExportData export )
  {
    mExport = export;
    mExport.mName = mSurveyName; 
    mExport.debug();
    // if ( TDSetting.mExportUri ) {
      // TDLog.v( "export with URI - survey " + mSurveyName );
      // FIXME mExport.mMime is always "application/octet-stream" therefore index 0 is used
      // selectFile( REQUEST_EXPORT_FILE, Intent.ACTION_CREATE_DOCUMENT, mExport.mMime, R.string.select_export_file, TDConst.getModelFilename( mExport.mType, mSurveyName ) );
      selectFile( REQUEST_EXPORT_FILE, true, 0, R.string.select_export_file, TDConst.getModelFilename( mExport.mType, mSurveyName ) );
    //} else {
    //  // TDLog.v( "export with task - survey " + mSurveyName );
    //  (new ExportTask( this, mParser, null, mExport )).execute(); // null = URI
    //}
  }

  /** file selection
   * @param request   selection request type (code)
   * @param create    whether to create (or open) a file
   * @param mime      index of file mime type 
   * @param res       dialog title resource
   * @param filename  suggested filename (optional)
   */
  // void selectFile( int request, String action, String mime, int res, String filename )
  void selectFile( int request, boolean create, int mime, int res, String filename )
  {
    // TDLog.v("TopoGL select file");
    // Intent intent = new Intent( action );
    // intent.setType( (mime == null)? "*/*" : mime );
    // intent.addCategory( Intent.CATEGORY_OPENABLE );
    // intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    Intent intent = create? TDandroid.getCreateDocumentIntent( mime ) : TDandroid.getOpenDocumentIntent( mime );

    if ( filename != null ) intent.putExtra( Intent.EXTRA_TITLE, filename );
    startActivityForResult( Intent.createChooser(intent, getResources().getString( res ) ), request );
  }

  /** callback to activity requests
   * @param request   request code
   * @param result    result code
   * @param intent    result intent
   */
  public void onActivityResult( int request, int result, Intent intent ) 
  {
    if ( result != Activity.RESULT_OK ) return;
    Uri uri = intent.getData();
    switch ( request ) {
      case REQUEST_DEM_FILE:
        if ( uri != null ) {
          openDEM( uri );
        } else {
          onDEMloading = false;
        }
        break;
      case REQUEST_TEXTURE_FILE:
        if ( uri != null ) {
          openTexture( uri );
        } else {
          onDEMloading = false;
        }
        break;
      // case REQUEST_TEMPERATURE_FILE:
      //   if ( uri != null ) openTemperature( uri );
      //   break;
      case REQUEST_IMPORT_FILE:
        if ( uri != null ) importSurvey( uri );
        break;
      case REQUEST_EXPORT_FILE:
        // if ( TDSetting.mExportUri ) {
          if ( uri != null && mExport != null ) {
            // TDLog.v("export survey. uri " + uri.toString() );
            (new ExportTask( this, mParser, uri, mExport )).execute(); 
          }
        // }
        mExport = null;
        break;
    }
  }

  /** extract a path from a URI
   * @param ctx    context
   * @param uri    URI
   * @return path
   */
  private String getPathFromUri( Context ctx, Uri uri )
  {
    Cursor cursor = null;
    try {
      String[] proj = { MediaStore.MediaColumns.DISPLAY_NAME };
      cursor = ctx.getContentResolver().query( uri, proj, null, null, null );
      int col = cursor.getColumnIndexOrThrow( MediaStore.MediaColumns.DISPLAY_NAME );
      cursor.moveToFirst();
      return cursor.getString( col );
    } finally {
      if ( cursor != null ) cursor.close();
    }
  }

  /** import a survey reading from a URI
   * @param uri    URI
   * @note called after a REQUEST_IMPORT_FILE
   */
  private void importSurvey( Uri uri )
  {
    String pathname = uri.getPath();
    int pos = pathname.lastIndexOf(":");
    if ( pos >= 0 ) pathname = pathname.substring( pos+1 );
    pos = pathname.lastIndexOf("/");
    if ( pos >= 0 ) pathname = pathname.substring( pos+1 );
    String pathname_lc = pathname.toLowerCase( Locale.getDefault() );
    for ( int trial = 0; trial < 2; ++ trial ) { 
      // TDLog.v("Import trial " + trial + ": survey " + pathname + " uri-path " + uri.getPath() );
      if ( pathname_lc.endsWith( ".th" ) 
        || pathname_lc.endsWith( "thconfig" )
        || pathname_lc.endsWith( "tdconfig" )
        || pathname_lc.endsWith( ".lox" )
        || pathname_lc.endsWith( ".mak" )
        || pathname_lc.endsWith( ".dat" )
        || pathname_lc.endsWith( ".tro" )
        || pathname_lc.endsWith( ".trox" )
        || pathname_lc.endsWith( ".3d" ) ) {
        doOpenFile( uri, pathname, true );
      } else {
        pathname = ( trial == 0 )? getPathFromUri( this, uri ) : null;
        if ( pathname == null ) {
          TDToast.makeBad( R.string.unsupported_format );
        }
      }
    }
  }

  void sketchLeg( Cave3DShot leg ) 
  {
    if ( leg == null ) return; // safety check
    TDLog.v("TopoGL - sketch leg: " + leg.from + " " + leg.to );
    setSketchLeg( leg );
    Intent intent = new Intent( this, com.topodroid.TDX.SketchWindow.class );
    intent.putExtra( "TOPOGL_SKETCH_VERTICAL", true );
    startActivity( intent );
  }

}
