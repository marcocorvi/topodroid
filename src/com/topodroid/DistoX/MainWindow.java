/* @file MainWindow.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid main class: survey/calib list
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

// import android.os.AsyncTask;
import android.os.Debug;

// import java.lang.Long;
// import java.lang.reflect.Method;
// import java.lang.reflect.InvocationTargetException;

import android.app.Application;
import android.app.Activity;
import android.app.Dialog;

import android.content.ActivityNotFoundException;
// import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
// import android.os.Message;
// import android.os.Parcelable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.bluetooth.BluetoothDevice;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.preference.PreferenceManager;

// import android.view.Menu;
// import android.view.MenuItem;

import android.graphics.Color;
import android.graphics.PorterDuff;

import android.util.Log;

/*
  Method m = device.getClass().getMethod( "createRfcommSocket", new Class[] (int.class) );
  socket = (BluetoothSocket) m.invoke( device, 2 );
  socket.connect();
*/

public class MainWindow extends Activity
                        implements OnItemClickListener
                        , OnItemLongClickListener
                        , View.OnClickListener
                        , OnCancelListener
                        , OnDismissListener
{
  private TopoDroidApp mApp;
  private Activity mActivity = null;
  private boolean onMenu; // whether menu is displaying

  private LinearLayout mLayout;
  private ListView mList;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;

  private Button[] mButton1;
  private static int izons[] = {
                          R.drawable.iz_disto2b, // iz_disto,
                          R.drawable.iz_plus,
                          R.drawable.iz_import,
                          R.drawable.iz_palette,
                          R.drawable.iz_manager,
                          // FIXME THMANAGER
                          // R.drawable.iz_database
                          };

  private static int menus[] = { 
                          R.string.menu_palette,
                          R.string.menu_logs,
                          R.string.menu_join_survey,
                          R.string.menu_about,
                          R.string.menu_options,
                          R.string.menu_help
                          };

  private static int help_icons[] = { R.string.help_device,
                          R.string.help_add_topodroid,
                          R.string.help_import,
                          R.string.help_symbol,
                          R.string.help_therion,
                          // FIXME THMANAGER
                          // R.string.help_database
                          };
  private static int help_menus[] = {
                          R.string.help_symbol,
                          R.string.help_log,
                          R.string.help_join_survey,
                          R.string.help_info_topodroid,
                          R.string.help_prefs,
                          R.string.help_help
                        };

  // -------------------------------------------------------------
  private boolean say_no_survey = true;
  private boolean say_not_enabled = true; // whether to say that BT is not enabled
  boolean do_check_bt = true;             // one-time bluetooth check sentinel

  // -------------------------------------------------------------------

  TopoDroidApp getApp() { return mApp; }
    
  public void updateDisplay( )
  {
    DataHelper data = mApp.mData;
    if ( data != null ) {
      List<String> list = data.selectAllSurveys();
      updateList( list );
      if ( say_no_survey && list.size() == 0 ) {
        say_no_survey = false;
        Toast.makeText( mActivity, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      } 
    }
  }

  private void updateList( List<String> list )
  {
    mArrayAdapter.clear();
    if ( list.size() > 0 ) {
      for ( String item : list ) {
        mArrayAdapter.add( item );
      }
    }
  }

  // ---------------------------------------------------------------
  // list items click

  public void onClick(View view)
  { 
    if ( onMenu ) {
      closeMenu();
      return;
    }
    // TDLog.Log( TDLog.LOG_INPUT, "MainWindow onClick() " + view.toString() );
    Intent intent;
    // int status = mStatus;
    Button b0 = (Button)view;

    if ( b0 == mImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      // updateDisplay();
      return;
    }

    int k1 = 0;
    int k2 = 0;
    {
      if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) { // mBtnDevice
        if ( mApp.mBTAdapter == null ) {
          Toast.makeText( mActivity, R.string.no_bt, Toast.LENGTH_SHORT ).show();
        } else {
          if ( mApp.mBTAdapter.isEnabled() ) {
            // TDLog.Debug( "start device window");
            startActivity( new Intent( Intent.ACTION_VIEW ).setClass( mActivity, DeviceActivity.class ) );
          } else {
            Toast.makeText( mActivity, R.string.not_enabled, Toast.LENGTH_SHORT ).show();
          }
        }
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // NEW SURVEY/CALIB
        mApp.setSurveyFromName( null, true ); // new-survey dialog: tell app to clear survey name and id
        (new SurveyNewDialog( mActivity, this, -1, -1 )).show(); 
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // IMPORT
        (new ImportDialog( mActivity, this, mApp )).show();
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // PALETTE
        BrushManager.makePaths( mApp, getResources() );
        (new SymbolEnableDialog( mActivity, mApp )).show();

      // FIXME THMANAGER
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // THERION MANAGER ThManager
        try {
          intent = new Intent( "ThManager.intent.action.Launch" );
          // intent.putExtra( "survey", mApp.getSurveyThFile() );
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( mActivity, R.string.no_thmanager, Toast.LENGTH_SHORT ).show();
        }
      // } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // DATABASE
      //   try {
      //     intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + TDPath.getDatabase() ) );
      //     intent.addCategory("com.kokufu.intent.category.APP_DB_VIEWER");
      //     startActivity( intent );
      //   } catch ( ActivityNotFoundException e ) {
      //     Toast.makeText( mActivity, R.string.no_db_viewer, Toast.LENGTH_SHORT ).show();
      //   }
      }
    }
    // if ( status != mStatus ) {
    //   updateDisplay( );
    // }
  }

  // splitSurvey invokes this method with args: null, 0, old_sid, old_id
  //
  void startSurvey( String value, int mustOpen ) // , long old_sid, long old_id )
  {
    mApp.setSurveyFromName( value, false ); // open survey activity: tell app to update survey name+id, no forward
    Intent surveyIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SurveyWindow.class );
    surveyIntent.putExtra( TDTag.TOPODROID_SURVEY, mustOpen );
    // surveyIntent.putExtra( TDTag.TOPODROID_OLDSID, old_sid );
    // surveyIntent.putExtra( TDTag.TOPODROID_OLDID,  old_id );
    mActivity.startActivity( surveyIntent );
  }

  void startSplitSurvey( long old_sid, long old_id )
  {
    mApp.setSurveyFromName( null, false ); // FIXME JOIN-SURVEY
    (new SurveyNewDialog( mActivity, this, old_sid, old_id )).show(); // WITH SPLIT
  }

  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( onMenu ) {
      closeMenu();
      return true;
    }
    CharSequence item = ((TextView) view).getText();
    startSurvey( item.toString(), 0 ); // , -1, -1 );
    return true;
  }

  void doOpenSurvey( String name )
  {
    mApp.setSurveyFromName( name, false ); // open survey: tell app to update survey name+id, no forward
    Intent openIntent = new Intent( this, ShotWindow.class );
    startActivity( openIntent );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    CharSequence item = ((TextView) view).getText();
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }

    mApp.setSurveyFromName( item.toString(), true ); 
    Intent openIntent = new Intent( this, ShotWindow.class );
    startActivity( openIntent );
  }

  void setTheTitle()
  {
    // String title = getResources().getString( R.string.app_name );
    setTitle( mApp.getConnectionStateTitleStr() + mApp.mCWD );
    setTitleColor( TDColor.NORMAL );
    // Log.v("DistoX", "TopoDroid activity set the title <" + mApp.getConnectionStateTitleStr() + title + ">" );
  }

  // ---------------------------------------------------------------
  // FILE IMPORT

  void importFile( String filename )
  {
    // FIXME connect-title string
    mActivity.setTitle( R.string.import_title );
    mActivity.setTitleColor( TDColor.CONNECTED );
    if ( filename.endsWith(".th") || filename.endsWith(".TH") ) {
      String filepath = TDPath.getImportFile( filename );
      String name = filename.replace(".th", "" ).replace(".TH", "");
      if ( mApp.mData.hasSurveyName( name ) ) {
        Toast.makeText(mActivity, R.string.import_already, Toast.LENGTH_SHORT).show();
        setTheTitle();
        return;
      }
      // Toast.makeText(mActivity, R.string.import_wait, Toast.LENGTH_SHORT).show();
      new ImportTherionTask( this ).execute( filepath, name );
    } else if ( filename.endsWith(".dat") || filename.endsWith(".DAT") ) {
      String filepath = TDPath.getImportFile( filename );
      new ImportCompassTask( this ).execute( filepath );
    } else if ( filename.endsWith(".top") || filename.endsWith(".TOP") ) {
      String filepath = TDPath.getImportFile( filename );
      new ImportPocketTopoTask( this ).execute( filepath, filename ); // TODO pass the drawer as arg
    } else if ( filename.endsWith(".tro") || filename.endsWith(".TRO") ) {
      String filepath = TDPath.getImportFile( filename );
      new ImportVisualTopoTask( this ).execute( filepath ); 
    } else if ( filename.endsWith(".csn") || filename.endsWith(".CSN") ) { // CaveSniper text file
      String filepath = TDPath.getImportFile( filename );
      new ImportCaveSniperTask( this ).execute( filepath ); 
    } else if ( filename.endsWith(".zip") ) {
      Toast.makeText(mActivity, R.string.import_zip_wait, Toast.LENGTH_LONG).show();
      new ImportZipTask( this ) .execute( filename );
    } else {
      setTheTitle( );
    }
    // FIXME SYNC updateDisplay();
  }
  
  // ---------------------------------------------------------------

  TopoDroidAbout mTopoDroidAbout = null;
 
  HorizontalListView mListView;
  // HorizontalImageButtonView mButtonView1;
  HorizontalButtonView mButtonView1;
  Button     mImage;
  ListView   mMenu;
  // HOVER
  // MyMenuAdapter mMenuAdapter = null;
  ArrayAdapter< String > mMenuAdapter;
  

  void setMenuAdapter( Resources res )
  {
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( this, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );
    mMenuAdapter = new ArrayAdapter<>(mActivity, R.layout.menu );

    if ( TDLevel.overNormal )   mMenuAdapter.add( res.getString( menus[0] ) ); // PALETTE
    if ( TDLevel.overAdvanced ) mMenuAdapter.add( res.getString( menus[1] ) ); // LOGS
    if ( TDLevel.overExpert && mApp.mCosurvey ) mMenuAdapter.add( res.getString( menus[2] ) ); // CO-SURVEY
    mMenuAdapter.add( res.getString( menus[3] ) ); // ABOUT
    mMenuAdapter.add( res.getString( menus[4] ) ); // SETTINGS
    mMenuAdapter.add( res.getString( menus[5] ) ); // HELP
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    // HOVER
    // mMenuAdapter.resetBgColor();
    onMenu = false;
  }

  private void handleMenu( int pos ) 
  {
    closeMenu();
    // Toast.makeText(mActivity, item.toString(), Toast.LENGTH_SHORT).show();
    int p = 0;
      Intent intent;
      if ( TDLevel.overNormal && p++ == pos ) { // PALETTE EXTRA SYMBOLS
        // BrushManager.makePaths( getResources() );
        // (new SymbolEnableDialog( mActivity, mApp )).show();

        (new SymbolReload( mActivity, mApp, TDLevel.overExpert )).show();
      } else 
      if ( TDLevel.overAdvanced && p++ == pos ) { // LOGS
        intent = new Intent( mActivity, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_LOG );
        startActivity( intent );
      } else {  
        if ( TDLevel.overExpert && mApp.mCosurvey && p++ == pos ) {  // CO-SURVEY
          (new ConnectDialog( mActivity, mApp )).show();
        } else { 
          if ( p++ == pos ) { // ABOUT
            (new TopoDroidAbout( mActivity )).show();
          } else { 
            if ( p++ == pos ) { // SETTINGS
              intent = new Intent( mActivity, TopoDroidPreferences.class );
              intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
              startActivity( intent );
            } else { 
              if ( p++ == pos ) { // HELP
                (new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, menus.length ) ).show();
              }
            }
          }
        }
      }
    // }
    // updateDisplay();
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    
    // TDLog.Profile("TDActivity onCreate");
    setContentView(R.layout.topodroid_activity);
    mApp = (TopoDroidApp) getApplication();
    mActivity = this;
    mApp.mActivity = this;

    // mArrayAdapter = new ArrayAdapter<>( this, R.layout.message );
    mArrayAdapter = new ListItemAdapter( this, R.layout.message );

    mLayout = (LinearLayout) findViewById( R.id.td_layout );

    mList = (ListView) findViewById(R.id.td_list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setLongClickable( true );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );

    // TDLog.Profile("TDActivity menu");
    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mMenu = (ListView) findViewById( R.id.menu );
    mMenuAdapter = null;

    setMenuAdapter( getResources() );
    closeMenu();
    // HOVER
    mMenu.setOnItemClickListener( this );

    // TDLog.Profile("TDActivity buttons");
    mListView = (HorizontalListView) findViewById(R.id.listview);
    resetButtonBar();

    if ( mApp.mCheckPerms != 0 ) {
      new TopoDroidPerms( this, mApp.mCheckPerms ).show();
      if ( mApp.mCheckPerms < 0 ) finish();
    } else {
      if ( mApp.mWelcomeScreen ) {
        mApp.setBooleanPreference( "DISTOX_WELCOME_SCREEN", false );
        mApp.mWelcomeScreen = false;
        mTopoDroidAbout = new TopoDroidAbout( this );
        mTopoDroidAbout.setOnCancelListener( this );
        mTopoDroidAbout.setOnDismissListener( this );
        mTopoDroidAbout.show();
      }
    }

    // FIXME INSTALL_SYMBOL
    // if ( mApp.askSymbolUpdate ) {
    //   // (new TopoDroidVersionDialog(this, mApp)).show();
    //   // FIXME SYMBOL if symbol have not been updated TopoDroid exits
    //   // if ( mApp.askSymbolUpdate ) finish();
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.version_ask,
    //     new DialogInterface.OnClickListener() {
    //       @Override
    //       public void onClick( DialogInterface dialog, int btn ) {
    //         mApp.installSymbols( true );
    //       }
    //   } );
    // }
    // // mApp.installSymbols( true );

    // setTitleColor( 0x006d6df6 );

    // new AsyncTask<Void,Void,Void>() {
    //   @Override
    //   protected Void doInBackground(Void... arg0) 
    //   { 
    //     BrushManager.doMakePaths( );
    //     WorldMagneticModel.loadEGM9615( mApp );
    //     // int n_terms = MagUtil.CALCULATE_NUMTERMS( 12 );
    //     // WorldMagneticModel.loadWMM( mApp, n_terms );
    //     return null;
    //   }
    // };
    // TDLog.Profile("TDActivity thread");
    if ( mApp.mCheckPerms >= 0 ) {
      Thread loader = new Thread() {
        @Override
        public void run() {
          mApp.startupStep2();
          Resources res = getResources();
          BrushManager.reloadPointLibrary( mApp, res ); // reload symbols
          BrushManager.reloadLineLibrary( res );
          BrushManager.reloadAreaLibrary( res );
          BrushManager.doMakePaths( );
          WorldMagneticModel.loadEGM9615( mApp );
        }
      };
      loader.setPriority( Thread.MIN_PRIORITY );
      loader.start();
    }
    setTheTitle();
  }
  
  int mNrButton1 = 5;

  void resetButtonBar()
  {
    int size = mApp.setListViewHeight( mListView );
    MyButton.resetCache( /* mApp, */ size );

    // FIXME THMANAGER
    mNrButton1 = 4;
    if ( TDLevel.overExpert ) mNrButton1 ++; // TH MANAGER
    mButton1 = new Button[mNrButton1];

    mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, getResources(), R.drawable.iz_menu ) );
    for (int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
    }

    // mButtonView1 = new HorizontalImageButtonView( mButton1 );
    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );
  }

  @Override
  public void onDismiss( DialogInterface d )
  { 
    if ( d == (Dialog)mTopoDroidAbout ) {
      mTopoDroidAbout = null;
    }
  }

  @Override
  public void onCancel( DialogInterface d )
  {
    if ( d == (Dialog)mTopoDroidAbout ) {
      mTopoDroidAbout = null;
    }
  }

  // private void restoreInstanceState(Bundle map )
  // {
  // }

  // private void restoreInstanceFromData()
  // { 
  // }
    
  // private void saveInstanceToData()
  // {
  // }

  // @Override
  // public void onSaveInstanceState(Bundle outState) 
  // {
  // }

  // ------------------------------------------------------------------
  // LIFECYCLE
  //
  // onCreate --> onStart --> onResume
  //          --> onSaveInstanceState --> onPause --> onStop | drawing | --> onStart --> onResume
  //          --> onSaveInstanceState --> onPause [ off/on ] --> onResume
  //          --> onPause --> onStop --> onDestroy

  @Override
  public void onStart()
  {
    super.onStart();
    // restoreInstanceFromFile();
    // TDLog.Log( TopoDroidLoLogOG_MAIN, "onStart check BT " + mApp.mCheckBT + " enabled " + mApp.mBTAdapter.isEnabled() );

    // TDLog.Profile("TDActivity onStart");
    if ( do_check_bt ) {
      do_check_bt = false;
      if ( mApp.mBTAdapter == null ) {
        Toast.makeText( this, R.string.no_bt, Toast.LENGTH_SHORT ).show();
      } else {
        if ( TDSetting.mCheckBT == 1 && ! mApp.mBTAdapter.isEnabled() ) {    
          Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
          startActivityForResult( enableIntent, TDRequest.REQUEST_ENABLE_BT );
        } else {
          // nothing to do: scanBTDEvices(); is called by menu CONNECT
        }
        // FIXME_BT
        // setBTMenus( mApp.mBTAdapter.isEnabled() );
      }
    }
    // mApp.checkAutoPairing(); // already done when prefs are loaded
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    mApp.resetLocale();
    // TDLog.Profile("TDActivity onResume");
    // TDLog.Log( TDLog.LOG_MAIN, "onResume " );
    // mApp.resetLocale();
    mApp.resumeComm();

    // restoreInstanceFromFile();

    // This is necessary: switching display off/on there is the call sequence
    //    [off] onSaveInstanceState
    //    [on]  onResume
    updateDisplay( );

  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    // TDLog.Log( TDLog.LOG_MAIN, "onPause " );
    mApp.suspendComm();
  }

  @Override
  public void onStop()
  {
    super.onStop();
    // if ( TopoDroidApp.isTracing ) {
    //   Debug.stopMethodTracing();
    // }
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    if ( doubleBackHandler != null ) {
      doubleBackHandler.removeCallbacks( doubleBackRunnable );
    }
    // TDLog.Log( TDLog.LOG_MAIN, "onDestroy " );
    // FIXME if ( mApp.mComm != null ) { mApp.mComm.interrupt(); }
    // FIXME BT_RECEIVER mApp.resetCommBTReceiver();
    // saveInstanceToData();

    mApp.stopPairingRequest();
  }

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

  @Override
  public void onBackPressed () // askClose
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }
    if ( doubleBack ) {
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
      super.onBackPressed();
      return;
    }
    doubleBack = true;
    doubleBackToast = Toast.makeText( this, R.string.double_back, Toast.LENGTH_SHORT );
    doubleBackToast.show();
    doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
  }

  // ------------------------------------------------------------------


  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // TDLog.Log( TDLog.LOG_MAIN, "on Activity Result: request " + mRequestName[request] + " result: " + result );
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case TDRequest.REQUEST_ENABLE_BT:
        // mApp.resetLocale(); // apparently this does not affect locale
        if ( result == Activity.RESULT_OK ) {
          // nothing to do: scanBTDEvices() is called by menu CONNECT
        } else if ( say_not_enabled ) {
          say_not_enabled = false;
          Toast.makeText(this, R.string.not_enabled, Toast.LENGTH_SHORT).show();
          // finish();
        }
        // FIXME_BT
        // FIXME mApp.mBluetooth = ( result == Activity.RESULT_OK );
        // setBTMenus( mApp.mBTAdapter.isEnabled() );
        updateDisplay( );
        break;

    }
  }

  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( mActivity, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
    startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
        return onSearchRequested();
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.MainWindow );
        if ( help_page != null ) UserManualActivity.showHelpPage( mActivity, help_page );
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }


}
