/* @file MainWindow.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid main class: survey/calib list
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDRequest;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.help.UserManDownload;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;

import com.topodroid.mag.WorldMagneticModel;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
// import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;

import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.os.Handler;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.content.pm.PackageManager;
// import android.net.Uri;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Button;

import android.view.View;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

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
  private DataHelper mApp_mData;
  // private boolean mApp_mCosurvey = false; // IF_COSURVEY
  private int mApp_mCheckPerms;

  private Activity mActivity = null;
  private boolean onMenu; // whether menu is displaying

  // private LinearLayout mTdLayout;
  // private RelativeLayout mRelLayout;
  // private ListView mList;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;

  private Button[] mButton1;
  private static final int[] izons = {
                          R.drawable.iz_disto2b, // iz_disto,
                          R.drawable.iz_plus,
                          R.drawable.iz_import,
                          R.drawable.iz_palette,
                          R.drawable.iz_manager, // FIXME THMANAGER
                          // R.drawable.iz_database
			  R.drawable.iz_empty
                          };

  private static final int[] menus = {
                          R.string.menu_palette,
                          R.string.menu_logs,
			  R.string.menu_backups,
                          // R.string.menu_join_survey,
			  // R.string.menu_updates, // UPDATES
                          R.string.menu_about,
                          R.string.menu_options,
                          R.string.menu_help,
                          };

  private static final int[] help_icons = { R.string.help_device,
                          R.string.help_add_topodroid,
                          R.string.help_import,
                          R.string.help_symbol,
                          R.string.help_therion,
                          // FIXME THMANAGER
                          // R.string.help_database
                          };
  private static final int[] help_menus = {
                          R.string.help_symbol,
                          R.string.help_log,
			  R.string.help_backups,
                          // R.string.help_join_survey,
                          // R.string.help_updates, // UPDATES
                          R.string.help_info_topodroid,
                          R.string.help_prefs,
                          R.string.help_help,
                        };

  private static final int HELP_PAGE = R.string.MainWindow;

  // -------------------------------------------------------------
  private boolean say_no_survey = true;
  private boolean say_not_enabled = true; // whether to say that BT is not enabled
  private boolean do_check_bt = true;             // one-time bluetooth check sentinel

  private boolean mPaletteButtonEnabled = false;
  private void enablePaletteButton() { mPaletteButtonEnabled = true; }

  // -------------------------------------------------------------------

  TopoDroidApp getApp() { return mApp; }
    
  public void updateDisplay( )
  {
    // DataHelper data = mApp.mData;
    if ( mApp_mData != null ) {
      List< String > list = mApp_mData.selectAllSurveys();
      updateList( list );
      if ( say_no_survey && list.size() == 0 ) {
        say_no_survey = false;
        TDToast.make( R.string.no_survey );
      } 
    }
  }

  private void updateList( List< String > list )
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

  private void startDistoXActivity()
  {
    if ( DeviceUtil.hasAdapter() ) {
      if ( DeviceUtil.isAdapterEnabled() ) {
        // TDLog.Debug( "start device window");
        startActivity( new Intent( Intent.ACTION_VIEW ).setClass( mActivity, DeviceActivity.class ) );
      } else {
        TDToast.makeBad( R.string.not_enabled );
      }
    } else {
      TDToast.makeBad( R.string.no_bt );
    }
  }

  private void startSurveyDialog()
  {
    mApp.setSurveyFromName( null, SurveyInfo.DATAMODE_NORMAL, true ); // new-survey dialog: tell app to clear survey name and id
    (new SurveyNewDialog( mActivity, this, -1, -1 )).show(); 
  }

  @Override
  public void onClick(View view)
  { 
    // TDLog.Log( TDLog.LOG_INPUT, "MainWindow onClick() " + view.toString() );
    if ( onMenu ) {
      closeMenu();
      return;
    }
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
        startDistoXActivity();
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // NEW SURVEY
        startSurveyDialog();
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // IMPORT
        File[] files = TDPath.getImportFiles();
        File[] zips = TDPath.getZipFiles();
        int len = ( ( files != null )? files.length : 0 ) + ( ( zips != null )? zips.length : 0 );
        if ( len > 0 ) {
          (new ImportDialog( mActivity, this, files, zips )).show();
        } else {
          TDToast.makeWarn( R.string.import_none );
        }
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // PALETTE
	if ( mPaletteButtonEnabled ) {
          SymbolEnableDialog dlg = new SymbolEnableDialog( mActivity );
	  dlg.anchorTop();
	  dlg.show();
        } else {
	  TDToast.makeBad( "palette not yet loaded" );
	}
      // FIXME THMANAGER
      } else if ( TDLevel.overExpert && k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // THERION MANAGER TdManager
        try {
          intent = new Intent( Intent.ACTION_VIEW ).setClass( this, com.topodroid.tdm.TdManagerActivity.class );
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          // TDToast.makeBad( R.string.no_thmanager );
          TDLog.Error( "Td Manager activity not started" );
        }
      }
    }
    // if ( status != mStatus ) {
    //   updateDisplay( );
    // }
  }

  // splitSurvey invokes this method with args: null, 0, old_sid, old_id
  //
  private void startSurvey( String value, int mustOpen ) // , long old_sid, long old_id )
  {
    mApp.setSurveyFromName( value, -1, true ); // open survey activity: tell app to update survey name+id
    Intent surveyIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SurveyWindow.class );
    surveyIntent.putExtra( TDTag.TOPODROID_SURVEY, mustOpen );
    // surveyIntent.putExtra( TDTag.TOPODROID_OLDSID, old_sid );
    // surveyIntent.putExtra( TDTag.TOPODROID_OLDID,  old_id );
    mActivity.startActivity( surveyIntent );
  }

  void startSplitSurvey( long old_sid, long old_id )
  {
    mApp.setSurveyFromName( null, SurveyInfo.DATAMODE_NORMAL, true ); // FIXME CO-SURVEY
    (new SurveyNewDialog( mActivity, this, old_sid, old_id )).show(); // WITH SPLIT
  }

  void startMoveSurvey( long old_sid, long old_id, String new_survey )
  {
    // Log.v("DistoX-MOVE", "start move survey");
    if ( mApp.moveSurveyData( old_sid, old_id, new_survey ) ) {
      mApp.setSurveyFromName( null, SurveyInfo.DATAMODE_NORMAL, true ); // FIXME CO-SURVEY
    // } else {
    }
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
    mApp.setSurveyFromName( name, -1, true ); // open survey: tell app to update survey name+id
    Intent openIntent = new Intent( this, ShotWindow.class );
    startActivity( openIntent );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }

    if ( ! ( view instanceof TextView ) ) {
      TDLog.Error("import view instance of " + view.toString() );
      return;
    }
    CharSequence item = ((TextView) view).getText();
    mApp.setSurveyFromName( item.toString(), -1, true ); 
    Intent openIntent = new Intent( this, ShotWindow.class );
    startActivity( openIntent );
  }

  void setTheTitle()
  {
    // String title = getResources().getString( R.string.app_name );
    setTitle( // mApp.getConnectionStateTitleStr() + // IF_COSURVEY
              TDInstance.cwd ); 
    setTitleColor( TDColor.TITLE_NORMAL );
    // Log.v("DistoX", "TopoDroid activity set the title <" + mApp.getConnectionStateTitleStr() + title + ">" );
  }

  // ---------------------------------------------------------------
  // FILE IMPORT

  private void setTitleImport()
  {
    mActivity.setTitle( R.string.import_title );
    mActivity.setTitleColor( TDColor.CONNECTED );
  }

  void importDatFile( String filepath, int datamode, boolean lrud, boolean leg_first )
  {
    setTitleImport();
    new ImportCompassTask( this, datamode, lrud, leg_first ).execute( filepath );
  }

  void importTroFile( String filepath, boolean lrud, boolean leg_first )
  {
    setTitleImport();
    new ImportVisualTopoTask( this, lrud, leg_first ).execute( filepath );
  }

  void importFile( String filename )
  {
    // FIXME connect-title string
    if ( filename.endsWith(".th") || filename.endsWith(".TH") ) {
      String filepath = TDPath.getImportFile( filename );
      String name = filename.replace(".th", "" ).replace(".TH", "");
      if ( mApp_mData.hasSurveyName( name ) ) {
        TDToast.makeBad(R.string.import_already );
        return;
      }
      // TDToast.make( R.string.import_wait );
      setTitleImport();
      new ImportTherionTask( this ).execute( filepath, name );
    } else if ( filename.endsWith(".dat") || filename.endsWith(".DAT") ) {
      String filepath = TDPath.getImportFile( filename );
      (new ImportDatDialog( this, this, filepath )).show();
      // new ImportCompassTask( this ).execute( filepath );
    } else if ( filename.endsWith(".top") || filename.endsWith(".TOP") ) {
      String filepath = TDPath.getImportFile( filename );
      setTitleImport();
      new ImportPocketTopoTask( this ).execute( filepath, filename ); // TODO pass the drawer as arg
    } else if ( filename.endsWith(".tro") || filename.endsWith(".TRO") ) {
      String filepath = TDPath.getImportFile( filename );
      (new ImportTroDialog( this, this, filepath )).show();
    } else if ( filename.endsWith(".svx") || filename.endsWith(".SVX") ) {
      String filepath = TDPath.getImportFile( filename );
      setTitleImport();
      new ImportSurvexTask( this ).execute( filepath ); 
    } else if ( filename.endsWith(".csn") || filename.endsWith(".CSN") ) { // CaveSniper text file
      String filepath = TDPath.getImportFile( filename );
      new ImportCaveSniperTask( this ).execute( filepath ); 
      setTitleImport();
    } else if ( filename.endsWith(".zip") ) {
      // TDToast.makeLong( R.string.import_zip_wait );
      setTitleImport();
      new ImportZipTask( this, false ) .execute( filename ); // force = true (skip version checks)
    // } else {
    //   setTheTitle( );
    }
    // FIXME SYNC updateDisplay();
  }
  
  // ---------------------------------------------------------------


  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;
  private Button     mImage;
  private ListView   mMenu;
  
  // FIXME TOOLBAR Toolbar mToolbar;
  
  private boolean mWithPalette  = false;
  private boolean mWithPalettes = false;
  private boolean mWithLogs     = false;
  private boolean mWithBackupsClear = false;

  void setMenuAdapter( Resources res )
  {
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>(mActivity, R.layout.menu );

    mWithPalette  = TDLevel.overNormal;
    mWithPalettes = TDLevel.overExpert && TDSetting.mPalettes; // mWithPalettes ==> mWithPalette
    mWithLogs     = TDLevel.overAdvanced;
    mWithBackupsClear = TDLevel.overExpert && TDSetting.mBackupsClear;

    if ( mWithPalette ) menu_adapter.add( res.getString( menus[0] ) ); // PALETTE
    if ( mWithLogs )    menu_adapter.add( res.getString( menus[1] ) ); // LOGS
    if ( mWithBackupsClear ) menu_adapter.add( res.getString( menus[2] ) ); // CLEAR_BACKUPS
    // if ( TDLevel.overExpert && mApp_mCosurvey ) menu_adapter.add( res.getString( menus[2] ) ); // IF_COSURVEY
    // if ( TDLevel.overExpert )   menu_adapter.add( res.getString( menus[3] ) ); // UPDATES
    menu_adapter.add( res.getString( menus[3] ) ); // ABOUT
    menu_adapter.add( res.getString( menus[4] ) ); // SETTINGS
    menu_adapter.add( res.getString( menus[5] ) ); // HELP
    mMenu.setAdapter( menu_adapter );
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
    // TDToast.make(item.toString() );
    int p = 0;
      Intent intent;
      if ( mWithPalette && p++ == pos ) { // PALETTE EXTRA SYMBOLS
        // (new SymbolEnableDialog( mActivity )).show();
        (new SymbolReload( mActivity, mApp, mWithPalettes )).show();
      } else if ( mWithLogs && p++ == pos ) { // LOGS
        intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
        intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_LOG );
        startActivity( intent );
      } else if ( mWithBackupsClear && p++ == pos ) { // CLEAR_BACKUPS
        TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.ask_backups_clear,
          new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { doBackupsClear(); }
          }
        );
      // } else if ( TDLevel.overExpert && mApp_mCosurvey && p++ == pos ) {  // IF_COSURVEY
      //   (new ConnectDialog( mActivity, mApp )).show();
      // } else if ( TDLevel.overExpert && p++ == pos ) {  // UPDATES
      //   // (new TDUpdatesDialog( mActivity, mApp )).show();
      } else if ( p++ == pos ) { // ABOUT
        (new TopoDroidAbout( mActivity, this, -2 )).show();
      } else if ( p++ == pos ) { // SETTINGS
        intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
        intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_ALL );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE )).show();
      }
    // }
    // updateDisplay();
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    
    TDandroid.setScreenOrientation( this );

    // TDLog.Profile("TDActivity onCreate");
    setContentView(R.layout.topodroid_activity);
    mApp = (TopoDroidApp) getApplication();
    mActivity = this;
    TopoDroidApp.mActivity = this;
    mApp_mData       = TopoDroidApp.mData;
    // mApp_mCosurvey   = TopoDroidApp.mCosurvey; // IF_COSURVEY

    // mArrayAdapter = new ArrayAdapter<>( this, R.layout.message );
    mArrayAdapter = new ListItemAdapter( this, R.layout.message );

    // mTdLayout = (LinearLayout) findViewById( R.id.td_layout );
    // mRelLayout = (RelativeLayout) findViewById( R.id.rel_layout );

    ListView list = (ListView) findViewById(R.id.td_list);
    list.setAdapter( mArrayAdapter );
    list.setOnItemClickListener( this );
    list.setLongClickable( true );
    list.setOnItemLongClickListener( this );
    list.setDividerHeight( 2 );

    // TDLog.Profile("TDActivity menu");
    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mMenu = (ListView) findViewById( R.id.menu );

    setMenuAdapter( getResources() );
    closeMenu();
    mMenu.setOnItemClickListener( this );

    // TDLog.Profile("TDActivity buttons");
    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlacholder( true );
    resetButtonBar();

    // FIXME TOOLBAR mToolbar = (Toolbar) findViewById( R.id.toolbar );
    // setActionBar( mToolbar );
    // resetToolbar();

    // if ( ! TDandroid.canRun( this, this ) ) {
    //   Log.v("DistoX-PERMS", "cannot run");
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), "Required Permissions not granted", 
    //     new DialogInterface.OnClickListener() {
    //       @Override
    //       public void onClick( DialogInterface dialog, int btn ) { 
    //         Log.v("DistoX-PERMS", "create perms");
    //         TDandroid.createPermissions( mApp, mActivity );
    //       }
    //     } );
    // } 

    mApp_mCheckPerms = TopoDroidApp.mCheckPerms;
    TDandroid.createPermissions( mApp, mActivity );

    if ( mApp_mCheckPerms != 0 ) {
      // Log.v("DistoX-PERMS", "check perms " + mApp_mCheckPerms );
      new TopoDroidPerms( this, mApp_mCheckPerms ).show();
      if ( mApp_mCheckPerms < 0 ) finish();
    } else {
      if ( mApp.mWelcomeScreen ) {
        mApp.setBooleanPreference( "DISTOX_WELCOME_SCREEN", false );
        mApp.mWelcomeScreen = false;
        if ( mApp.mSetupScreen ) {
          mApp.setBooleanPreference( "DISTOX_SETUP_SCREEN", false );
          mApp.mSetupScreen = false;
          doNextSetup( SETUP_WELCOME );
        } else {
          (new TopoDroidAbout( this, this, -2 )).show();
          // TopoDroidAbout tda = new TopoDroidAbout( this, this, -2 );
          // tda.setOnCancelListener( this );
          // tda.setOnDismissListener( this );
          // tda.show();
        }
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

    // setTitleColor( TDColor.TITLE_NORMAL );

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
    if ( mApp_mCheckPerms >= 0 ) {
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
	  enablePaletteButton();
        }
      };
      loader.setPriority( Thread.MIN_PRIORITY );
      loader.start();
    }
    setTheTitle();

    Intent intent = getIntent();
    if ( intent != null ) {
      Bundle extras = intent.getExtras();
      if ( extras != null ) {
        String action = intent.getExtras().getString("action");
        if ( action != null ) {
          // Log.v("DistoX-SHORT", "action " + action);
          if ( action.equals("new_survey") ) {
            startSurveyDialog( );
          } else if ( action.equals("distox") ) {
            startDistoXActivity();
          }
        }
      }
    }
  }

  private void doBackupsClear()
  {
    Thread cleaner = new Thread() {
      @Override
      public void run() { 
        File dir = TDPath.getTdrDir();
	// Log.v("DistoXX", "clear tdr backups from " + dir.getPath() );
        File[] files = dir.listFiles( new FileFilter() { 
          public boolean accept( File f ) { 
              return f.getName().matches( ".*tdr.bck.?" );
          }
        } );
        if ( files != null ) {
          for (File f : files) {
            TDUtil.deleteFile( f );
          }
        }
      }
    };
    // cleaner.setPriority( Thread.MIN_PRIORITY );
    cleaner.start();
  }

  private int mNrButton1 = 5;

  void resetButtonBar()
  {
    int size = TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );
    MyButton.resetCache( /* mApp, */ size );
    // TDToast.make( "SIZE " + size );

    // FIXME THMANAGER
    mNrButton1 = 4;
    if ( TDLevel.overExpert ) mNrButton1 ++; // TH MANAGER
    mButton1 = new Button[ mNrButton1 + 1 ];

    TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( mApp, getResources(), R.drawable.iz_menu ) );
    for (int k=0; k < mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
      // mButton1[k].setElevation(40);
    }
    mButton1[mNrButton1] = MyButton.getButton( mActivity, this, R.drawable.iz_empty );

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    // mRelLayout.invalidate();
  }

  // FIXME TOOLBAR void resetToolbar()
  // {
  //   int size = TDSetting.mSizeButtons;
  //   MyButton.resetCache( /* mApp, */ size );

  //   LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

  //   // FIXME THMANAGER
  //   mNrButton1 = 4;
  //   if ( TDLevel.overExpert ) mNrButton1 ++; // TH MANAGER
  //   mButton1 = new Button[mNrButton1];

  //   for (int k=0; k<mNrButton1; ++k ) {
  //     mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
  //     mToolbar.addView( mButton1[k], params );
  //   }

  //   mImage = MyButton.getButton( mActivity, this, R.drawable.ic_menu );
  //   TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( mApp, getResources(), R.drawable.iz_menu ) );
  //   mToolbar.addView( mImage, params );
  // }

  @Override
  public void onDismiss( DialogInterface d )
  { 
    if ( d == (Dialog)mTopoDroidAbout ) {
      doNextSetup( mTopoDroidAbout.nextSetup() );
      mTopoDroidAbout = null;
    }
  }

  @Override
  public void onCancel( DialogInterface d )
  {
    if ( d == (Dialog)mTopoDroidAbout ) {
      doNextSetup( mTopoDroidAbout.nextSetup() );
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
    // TDLog.Log( TDLog.LOG_MAIN, "onStart check BT " + mApp.mCheckBT + " enabled " + DeviceUtil.isAdapterEnabled() );

    // TDLog.Profile("TDActivity onStart");
    if ( do_check_bt ) {
      do_check_bt = false;
      if ( DeviceUtil.hasAdapter() ) {
        if ( TDSetting.mCheckBT == 1 && ! DeviceUtil.isAdapterEnabled() ) {    
          Intent enableIntent = new Intent( DeviceUtil.ACTION_REQUEST_ENABLE );
          startActivityForResult( enableIntent, TDRequest.REQUEST_ENABLE_BT );
        // } else {
          // nothing to do: scanBTDEvices(); is called by menu CONNECT
        }
        // FIXME_BT
        // setBTMenus( DeviceUtil.isAdapterEnabled() );
      } else {
        TDToast.makeBad( R.string.no_bt );
      }
    }
    // mApp.checkAutoPairing(); // already done when prefs are loaded

    // THIS IS COMMENTED BECAUSE I'M NOT SURE IT IS A GOOD THING
    // if ( ! TDLevel.mDeveloper ) new TDVersionDownload( this ).execute(); 
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // mApp.resetLocale(); // FIXME-LOCALE

    // TDLog.Profile("TDActivity onResume");
    // TDLog.Log( TDLog.LOG_MAIN, "onResume " );
    mApp.resumeComm();

    // restoreInstanceFromFile();

    // This is necessary: switching display off/on there is the call sequence
    //    [off] onSaveInstanceState
    //    [on]  onResume
    updateDisplay( );
    if ( TopoDroidApp.mCheckManualTranslation ) {
      TopoDroidApp.mCheckManualTranslation = false;
      checkManualTranslation();
    }
    // TDToast.make( ( TDPath.hasPath11()? "Path 11 " : "No Path 11 " ) + TDPath.EXTERNAL_STORAGE_PATH );
    if ( ( ! TDPath.BELOW_ANDROID_10 ) && TDPath.BELOW_ANDROID_11 ) { // only for Android 10
      // Log.v( "DistoX-PATH11", "has path11 " + TDPath.hasPath11() + " no-again " + TopoDroidApp.hasPath11NoAgain() );
      if ( ! TDPath.hasPath11() && ! TopoDroidApp.hasPath11NoAgain() && TDPath.hasPath10() ) {
        ( new Path11Dialog( this, this, mApp )).show();
      }
    }
  }


  void moveToPath11()
  { 
    if ( TDPath.moveToPath11() ) {
      TDToast.make(R.string.path11_move );
      // finish();
      // android.os.Process.killProcess( android.os.Process.myPid() );
    } else {
      TDToast.make(R.string.path11_not_move );
    }
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
    // TDLog.Log( TDLog.LOG_INPUT, "MainWindow onBackPressed()" );
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
    doubleBackToast = TDToast.makeToast( R.string.double_back );
    doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
  }

  // ------------------------------------------------------------------
 
  @Override
  protected void attachBaseContext( Context ctx )
  {
    TDInstance.context = ctx;
    super.attachBaseContext( TopoDroidApp.resetLocale( ) );
  }


  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // TDLog.Log( TDLog.LOG_MAIN, "on Activity Result: request " + mRequestName[request] + " result: " + result );
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case TDRequest.REQUEST_ENABLE_BT:
        // mApp.resetLocale(); // OK-LOCALE apparently this does not affect locale
        if ( result == Activity.RESULT_OK ) {
          // nothing to do: scanBTDEvices() is called by menu CONNECT
        } else if ( say_not_enabled ) {
          say_not_enabled = false;
          TDToast.makeBad(R.string.not_enabled );
          // finish();
        }
        // FIXME_BT
        // FIXME mApp.mBluetooth = ( result == Activity.RESULT_OK );
        // setBTMenus( DeviceUtil.isAdapterEnabled() );
        updateDisplay( );
        break;

    }
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        UserManualActivity.showHelpPage( mActivity, getResources().getString( HELP_PAGE ));
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  /* FIXME-23 */
  // this is called only for androidx.appcompat.app.AppCompatActivity so it is pretty useless
  @Override
  public void onRequestPermissionsResult( int code, final String[] perms, int[] results )
  {
    // Log.v("DistoX-PERM", "MAIN perm request result " + results.length );
    // TDLog.Log(TDLog.LOG_PERM, "MAIN req code " + code + " results length " + results.length );
    if ( code == TDandroid.REQUEST_PERMISSIONS ) {
      if ( results.length > 0 ) {
	for ( int k = 0; k < results.length; ++ k ) {
	  TDandroid.GrantedPermission[k] = ( results[k] == PackageManager.PERMISSION_GRANTED );
	  // Log.v("DistoX-PERM", "MAIN perm " + k + " perms " + perms[k] + " result " + results[k] );
	}
      }
      // if ( ! TDandroid.canRun( this, this ) ) {
      //   // Log.v("DistoX-PERM", "MAIN perm cannot run");
      //   // TELL THE USER TDToast.make("Cannot run");
      // }
    }
    // Log.v("DistoXX", "MAIN must restart " + TDandroid.MustRestart );
    // if ( ! TDandroid.MustRestart ) {
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.perm_required,
    //     new DialogInterface.OnClickListener() {
    //       @Override public void onClick( DialogInterface dialog, int btn ) { finish(); }
    //     }
    //   );
    // }
  }
  /* */
  /* FIXME-16 FIXME-8 nothing */
  

  // ----------------------------------------------------------------------------
  // setup
  private TopoDroidAbout mTopoDroidAbout = null;

  static final private int SETUP_WELCOME = 0;
  static final private int SETUP_TEXTSIZE = 1;
  static final private int SETUP_BUTTONSIZE = 2;
  static final private int SETUP_DRAWINGUNIT = 3;
  static final private int SETUP_DONE = 4; // this must be the last

  void doNextSetup( int setup ) 
  { 
    switch (setup) {
      case SETUP_WELCOME: 
        mTopoDroidAbout = new TopoDroidAbout( this, this, setup );
        mTopoDroidAbout.setOnCancelListener( this );
        mTopoDroidAbout.setOnDismissListener( this );
        mTopoDroidAbout.show();
        break;
      case SETUP_TEXTSIZE: 
        (new SetupTextSizeDialog( this, this, setup, TDSetting.mTextSize )).show();
	break;
      case SETUP_BUTTONSIZE:
        (new SetupButtonSizeDialog( this, this, setup, TDSetting.mSizeButtons )).show();
	break;
      case SETUP_DRAWINGUNIT:
        (new SetupDrawingUnitDialog( this, this, setup, TDSetting.mUnitIcons )).show();
	break;
      case SETUP_DONE:
        (new SetupDoneDialog( this, this, setup )).show();
	break;
      default:
	break;
    }
  }

  // check whether ask user to update manual translation
  private void checkManualTranslation()
  {
    File manifest = new File( TDPath.getManFile( "manifest" ) );
    if ( manifest.exists() ) {
      String lang = null;
      int version = -1;
      try {
        FileReader fr = new FileReader( manifest );
        BufferedReader br = new BufferedReader( fr );
        while ( lang == null || version < 0 ) {
          String line = br.readLine();
          if ( line == null ) break;
          String[] token = line.trim().replaceAll("\\s+", "").split("=");
          if ( token.length > 1 ) {
            String key = token[0].toUpperCase();
            if ( key.equals("LANG") ) { 
              lang = token[1].toLowerCase();
            } else if ( key.equals("VERSION") ) {
              try {
                version = Integer.parseInt( token[1] );
              } catch ( NumberFormatException e ) { }
            }
          }
        }
        br.close();
      } catch ( FileNotFoundException e ) {
      } catch ( IOException e ) {
      }
      // Log.v("DistoX-MAN", "manifest " + lang + " " + version );
      if ( lang != null && version > 0 ) {
        int res = 0;
        int man = 0;
        if ( "ru".equals( lang ) ) { 
          res = R.string.man_version_ru;
          man = R.string.user_man_ru;
        } else if ( "it".equals( lang ) ) {
          res = R.string.man_version_it;
          man = R.string.user_man_it;
        } else if ( "fr".equals( lang ) ) {
          res = R.string.man_version_fr;
          man = R.string.user_man_fr;
        } else if ( "es".equals( lang ) ) {
          res = R.string.man_version_es;
          man = R.string.user_man_es;
        }
        if ( res > 0 ) {
          int current = Integer.parseInt( getResources().getString( res ) );
          if ( current > version ) { // prompt user
            final String url = getResources().getString( man );
            String msg = String.format( getResources().getString( R.string.ask_manual_update ), current, version );
            TopoDroidAlertDialog.makeAlert( this, getResources(), msg, 
              new DialogInterface.OnClickListener() {
                @Override public void onClick( DialogInterface dialog, int btn ) { 
                  (new UserManDownload( url )).execute();
                }
              }
            );
          }
        }
      }
    }
  }

}
