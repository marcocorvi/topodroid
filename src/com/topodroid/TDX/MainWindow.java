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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDRequest;
import com.topodroid.utils.TDLocale;
// import com.topodroid.utils.TDVersion;

import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.help.UserManDownload;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
import com.topodroid.dev.DeviceUtil;
import com.topodroid.inport.ImportData;
import com.topodroid.inport.ImportCompassTask;
import com.topodroid.inport.ImportVisualTopoTask;
import com.topodroid.inport.ImportTherionTask;
import com.topodroid.inport.ImportPocketTopoTask;
import com.topodroid.inport.ImportSurvexTask;
import com.topodroid.inport.ImportWallsTask;
import com.topodroid.inport.ImportCaveSniperTask;
import com.topodroid.inport.ImportZipTask;
// import com.topodroid.inport.ImportDialog;
// import com.topodroid.inport.ImportDatDialog;
// import com.topodroid.inport.ImportTroDialog;

import com.topodroid.mag.WorldMagneticModel;

// import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
// import java.io.FileFilter;
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
import android.os.ParcelFileDescriptor;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.pm.PackageManager;
import android.net.Uri;

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

import android.graphics.drawable.BitmapDrawable;

/*
  Method m = device.getClass().getMethod( "createRfcommSocket", new Class[] (int.class) );
  socket = (BluetoothSocket) m.invoke( device, 2 );
  socket.connect();
*/

public class MainWindow extends Activity
                        implements OnItemClickListener
                        , OnItemLongClickListener
                        , View.OnClickListener
                        // , View.OnLongClickListener
                        , OnCancelListener
                        , OnDismissListener
{
  private TopoDroidApp mApp;
  // private boolean mApp_mCosurvey = false; // IF_COSURVEY
  // private int mApp_mCheckPerms;

  private int mRequestPermissionTime = 0;

  private Activity mActivity = null;
  private boolean onMenu; // whether menu is displaying

  // private LinearLayout mTdLayout;
  // private RelativeLayout mRelLayout;
  // private ListView mList;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;

  private Button[] mButton1;
  private BitmapDrawable  mButtonDistoX1;
  private BitmapDrawable  mButtonDistoX2;
  private BitmapDrawable  mButtonSap5;
  private BitmapDrawable  mButtonBric4;

  private final  int BTN_DEVICE = 0;

  private static final int[] izons = {
                          R.drawable.iz_disto2b, // iz_disto,
                          R.drawable.iz_plus,
                          R.drawable.iz_import,
                          R.drawable.iz_tools,   // iz_palette
                          R.drawable.iz_3d,      // FIXME CAVE3D
                          R.drawable.iz_manager, // FIXME THMANAGER
                          // R.drawable.iz_database
			  R.drawable.iz_empty
                          };

  private static final int[] menus = {
                          R.string.menu_close,
                          R.string.menu_palette,
                          R.string.menu_logs,
			  // R.string.menu_backups, // CLEAR_BACKUPS
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
                          R.string.help_cave3d,
                          R.string.help_therion, // FIXME THMANAGER
                          // R.string.help_database
                          };
  private static final int[] help_menus = {
                          R.string.help_close_app,
                          R.string.help_symbol,
                          R.string.help_log,
			  // R.string.help_backups, // CLEAR_BACKUPS
                          // R.string.help_join_survey,
                          // R.string.help_updates, // UPDATES
                          R.string.help_info_topodroid,
                          R.string.help_prefs,
                          R.string.help_help,
                        };

  private static final int HELP_PAGE = R.string.MainWindow;

  // -------------------------------------------------------------
  private boolean say_dialogR     = ! TopoDroidApp.hasTopoDroidDatabase(); // updated by showInitDialogs

  private boolean say_no_survey   = true;
  private boolean say_not_enabled = true; // whether to say that BT is not enabled
  private boolean do_check_bt     = true;     // one-time bluetooth check sentinel

  private boolean mPaletteButtonEnabled = false;
  private void enablePaletteButton() { mPaletteButtonEnabled = true; }

  // -------------------------------------------------------------------

  public TopoDroidApp getApp() { return mApp; }
    
  public void updateDisplay( )
  {
    if ( TopoDroidApp.mData != null ) {
      List< String > list = TopoDroidApp.mData.selectAllSurveys();
      updateList( list );
      if ( /* ! say_dialogR && */ say_no_survey && list.size() == 0 ) {
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

  // @Override
  // public boolean onLongClick( View view )
  // {
  //   if ( view != mButton1[2] ) return false; // IMPORT ZIP
  //   // TDFile.ExtensionFilter ext_filter = new TDFile.ExtensionFilter( TDPath.getImportTypes() );
  //   // int len = filenames.size() + zipnames.size();
  //   // selectImportFromDialog();
  //   // ImportData import_data = new ImportData();
  //   mImportData = new ImportData();
  //   doImport( TDConst.mSurveyImportTypes[0], mImportData ); // ZIP
  //   return true;
  // }

  // private void selectImportFromDialog() // IMPORT directly from dialog
  // {
  //   // File[] files = TDPath.getImportFiles();
  //   File[] zips  = TDPath.getZipFiles();
  //   // int len = ( ( files != null )? files.length : 0 ) + ( ( zips != null )? zips.length : 0 );
  //   int len = ( zips != null )? zips.length : 0;
  //   if ( len > 0 ) {
  //     // (new ImportDialog( mActivity, this, files, zips )).show();
  //     (new ImportDialog( mActivity, this, zips )).show();
  //   } else {
  //     TDToast.makeWarn( R.string.import_none );
  //   }
  // }

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

    if ( b0 == mMenuImage ) {
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
        // select Import From Provider();
        (new ImportDialogShot( this, this, TDConst.mSurveyImportTypes, R.string.title_import_shot )).show();
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // PALETTE
	if ( mPaletteButtonEnabled ) {
          SymbolEnableDialog dlg = new SymbolEnableDialog( mActivity );
	  dlg.anchorTop();
	  dlg.show();
        } else {
	  TDToast.makeBad( R.string.warning_no_palette );
	}
      } else if ( TDLevel.overNormal && k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // CAVE3D
        // int check = TDVersion.checkCave3DVersion( this );
        // if ( check < 0 ) {
        //   TDToast.makeBad( R.string.no_cave3d );
        // } else { // start Cave3D even if the version is below required mimimum
          try {
            // FIXME_CAVE3D intent = new Intent( "Cave3D.intent.action.Launch" );
            intent = new Intent( Intent.ACTION_VIEW ).setClass( this, com.topodroid.TDX.TopoGL.class );
            startActivity( intent );
          } catch ( ActivityNotFoundException e ) {
            TDToast.makeBad( R.string.no_cave3d );
          }
        // }
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
    // TDLog.v( "start move survey");
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
    final CharSequence item = ((TextView) view).getText();
    // if ( TDInstance.isDeviceSap() ) {
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.sap_warning,
    //     new DialogInterface.OnClickListener() {
    //       @Override public void onClick( DialogInterface dialog, int btn ) { startShowWindow( item ); }
    //     }
    //   );
    // } else {
      startShowWindow( item );
    // }
  }

  public void startShowWindow( CharSequence item )
  {
    mApp.setSurveyFromName( item.toString(), -1, true ); 
    Intent openIntent = new Intent( this, ShotWindow.class );
    startActivity( openIntent );
  }

  public void setTheTitle()
  {
    // setTitle( getResources().getString( R.string.app_name ) + ": " + TDInstance.cwd );
    // setTitle( // mApp.getConnectionStateTitleStr() + TDInstance.cwd ); // IF_COSURVEY
    setTitle( TDInstance.cwd );
    setTitleColor( TDColor.TITLE_NORMAL );
    // TDLog.v( "TopoDroid activity set the title <" + mApp.getConnectionStateTitleStr() + title + ">" );
  }

  // ---------------------------------------------------------------
  // FILE IMPORT

  private void setTitleImport()
  {
    mActivity.setTitle( R.string.import_title );
    mActivity.setTitleColor( TDColor.CONNECTED );
  }

  // public void importDatFile( InputStreamReader isr, String filepath, int datamode, boolean lrud, boolean leg_first )
  // {
  //   setTitleImport();
  //   new ImportCompassTask( this, isr, datamode, lrud, leg_first ).execute( filepath );
  // }

  // public void importTroFile( InputStreamReader isr, String filepath, boolean lrud, boolean leg_first, boolean trox )
  // {
  //   setTitleImport();
  //   new ImportVisualTopoTask( this, isr, lrud, leg_first, trox ).execute( filepath );
  // }

  // public void importFile( String filename )
  // {
  //   // TDLog.v( "import file " + filename );
  //   // FIXME connect-title string
  //   if ( filename.toLowerCase().endsWith(".th") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     String name = filename.replace(".th", "" ).replace(".TH", "");
  //     if ( TopoDroidApp.mData.hasSurveyName( name ) ) {
  //       TDToast.makeBad(R.string.import_already );
  //       return;
  //     }
  //     // TDToast.make( R.string.import_wait );
  //     setTitleImport();
  //     new ImportTherionTask( this, null ).execute( filepath, name );  // null FileReader
  //   } else if ( filename.toLowerCase().endsWith(".dat") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     (new ImportDatDialog( this, this, null, filepath )).show();
  //     // new ImportCompassTask( this ).execute( filepath );
  //   } else if ( filename.toLowerCase().endsWith(".top") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     setTitleImport();
  //     new ImportPocketTopoTask( this, null ).execute( filepath, filename ); // TODO pass the drawer as arg
  //   } else if ( filename.toLowerCase().endsWith(".tro") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     (new ImportTroDialog( this, this, null, filepath )).show();
  //   } else if ( filename.toLowerCase().endsWith(".svx") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     setTitleImport();
  //     new ImportSurvexTask( this, null ).execute( filepath ); 
  //   } else if ( filename.toLowerCase().endsWith(".csn") ) { // CaveSniper text file
  //     String filepath = TDPath.getImportFile( filename );
  //     new ImportCaveSniperTask( this, null ).execute( filepath ); 
  //     setTitleImport();
  //   } else if ( filename.toLowerCase().endsWith(".zip") ) {
  //     // TDToast.makeLong( R.string.import_zip_wait );
  //     setTitleImport();
  //     new ImportZipTask( this, null, false ) .execute( filename ); // force = true (skip version checks)
  //   // } else {
  //   //   setTheTitle( );
  //   }
  //   // FIXME SYNC updateDisplay();
  // }

  public void importZipFile( String filename )
  {
    if ( ! filename.toLowerCase().endsWith(".zip") ) return;
    setTitleImport();
    new ImportZipTask( this, null, false ) .execute( filename ); // force = true (skip version checks)
  }
 
  /** import survey data from a URI
   * @param uri    input stream uri
   * @param name   file name
   * @param type   file extension (including the dot)
   */
  public void importStream( Uri uri, String name, String type )
  {
    // FIXME connect-title string
    // TDLog.v( "import with uri stream <" + name + "> type <" + type + ">" );
    if ( type.equals(".top") ) {
      setTitleImport();
      new ImportPocketTopoTask( this, uri ).execute( null, name );  // null filename (use fis); name = surveyname
    } else if ( type.equals(".zip") ) {
      // TDToast.makeLong( R.string.import_zip_wait );
      setTitleImport();
      new ImportZipTask( this, uri, false ) .execute( name ); // force = true (skip version checks)
    // } else {
    //   setTheTitle( );
    }
    // FIXME SYNC updateDisplay();
  }
  
  /** start the import task
   * @param uri     uri of the import file
   * @param name    name of the import file
   * @param type    import format type
   * @param data    import data
   */   
  public void importReader( Uri uri, String name, String type, ImportData data )
  {
    // FIXME connect-title string
    // TDLog.v( "import with reader <" + name + "> type <" + type + ">" );
    ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
    InputStreamReader isr = new InputStreamReader( TDsafUri.docFileInputStream( pfd ) );
    if ( type.equals(".th") ) {
      setTitleImport();
      new ImportTherionTask( this, isr ).execute( name, name );
    } else if ( type.equals(".dat") ) {
      setTitleImport();
      new ImportCompassTask( this, isr, data ).execute( name, name );
      // (new ImportDatDialog( this, this, isr, name )).show();
    } else if ( type.equals(".tro") || type.equals(".trox") ) {
      setTitleImport();
      // TDLog.v("type " + type + " data.trox " + data.mTrox );
      new ImportVisualTopoTask( this, isr, data ).execute( name, name );
      // (new ImportTroDialog( this, this, isr, name )).show();
    } else if ( type.equals(".svx") ) {
      setTitleImport();
      new ImportSurvexTask( this, isr ).execute( name ); 
    } else if ( type.equals(".srv") ) {
      setTitleImport();
      new ImportWallsTask( this, isr ).execute( name ); 
    } else if ( type.equals(".csn") ) {
      setTitleImport();
      new ImportCaveSniperTask( this, isr ).execute( name ); 
    // } else {
    //   setTheTitle( );
    }
    // FIXME SYNC updateDisplay();
  }
  
  // ---------------------------------------------------------------


  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;
  private Button     mMenuImage;
  private ListView   mMenu;
  
  // FIXME TOOLBAR Toolbar mToolbar;
  
  private boolean mWithPalette  = false;
  private boolean mWithPalettes = false;
  private boolean mWithLogs     = false;
  // private boolean mWithBackupsClear = false; // CLEAR_BACKUPS

  // /** react to a change in the configuration
  //  * @param cfg   new configuration
  //  */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    // TDLog.v("MAIN config changed");
    TDLocale.resetTheLocale();
  }

  /** set the adapter of the menu pull-down list
   */
  void setMenuAdapter( )
  {
    // TDLog.v("MAIN set menu adapter");
    Resources res = getResources();
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<String >(mActivity, R.layout.menu );

    mWithPalette  = TDLevel.overNormal;
    mWithPalettes = TDLevel.overExpert && TDSetting.mPalettes; // mWithPalettes ==> mWithPalette
    mWithLogs     = TDLevel.overAdvanced;
    // mWithBackupsClear = TDLevel.overExpert && TDSetting.mBackupsClear; // CLEAR_BACKUPS
    // TDLog.v("Main Window set menu adapter. With palette " + mWithPalette + " With palettes " + mWithPalettes );

    menu_adapter.add( res.getString( menus[0] ) ); // CLOSE
    if ( mWithPalette ) menu_adapter.add( res.getString( menus[1] ) ); // PALETTE
    if ( mWithLogs )    menu_adapter.add( res.getString( menus[2] ) ); // LOGS
    // if ( mWithBackupsClear ) menu_adapter.add( res.getString( menus[3] ) ); // CLEAR_BACKUPS
    // if ( TDLevel.overExpert && mApp_mCosurvey ) menu_adapter.add( res.getString( menus[2] ) ); // IF_COSURVEY
    // if ( TDLevel.overExpert )   menu_adapter.add( res.getString( menus[3] ) ); // UPDATES
    menu_adapter.add( res.getString( menus[3] ) ); // ABOUT
    menu_adapter.add( res.getString( menus[4] ) ); // SETTINGS
    menu_adapter.add( res.getString( menus[5] ) ); // HELP

    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

  /** close the menu pull-down list
   */
  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  /** handle a tap on a menu item
   * @param pos   menu item index
   */
  private void handleMenu( int pos ) 
  {
    closeMenu();
    // TDToast.make(item.toString() );
    int p = 0;
      Intent intent;
      if ( p++ == pos ) { // CLOSE askClose
        TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.ask_close_app,
          new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { doCloseApp(); }
          }
        );
      } else if ( mWithPalette && p++ == pos ) { // PALETTE EXTRA SYMBOLS
        // (new SymbolEnableDialog( mActivity )).show();
        (new SymbolReload( mActivity, mApp, mWithPalettes )).show();
      } else if ( mWithLogs && p++ == pos ) { // LOGS
        intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
        intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_LOG );
        startActivity( intent );
      // } else if ( mWithBackupsClear && p++ == pos ) { // CLEAR_BACKUPS
      //   TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.ask_backups_clear,
      //     new DialogInterface.OnClickListener() {
      //       @Override public void onClick( DialogInterface dialog, int btn ) { doBackupsClear(); }
      //     }
      //   );
      // } else if ( TDLevel.overExpert && mApp_mCosurvey && p++ == pos ) {  // IF_COSURVEY
      //   (new ConnectDialog( mActivity, mApp )).show();
      // } else if ( TDLevel.overExpert && p++ == pos ) {  // UPDATES
      //   // (new TDUpdatesDialog( mActivity, mApp )).show();
      } else if ( p++ == pos ) { // ABOUT
        (new TopoDroidAbout( mActivity, this, -2 )).show();
      } else if ( p++ == pos ) { // SETTINGS
        TDSetting.resetFlag();
        intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
        intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_ALL );
        startActivityForResult( intent, TDRequest.REQUEST_SETTINGS );
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
    TopoDroidApp.mMainActivity = this;
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
    mMenuImage = (Button) findViewById( R.id.handle );
    mMenuImage.setOnClickListener( this );
    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );
    // setMenuAdapter( ); // in on Start()
    // closeMenu();

    // TDLog.Profile("TDActivity buttons");
    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlaceholder( true );

    // resetButtonBar(); // moved to on Start()
    // setMenuAdapter( );
    // closeMenu();

    // FIXME TOOLBAR mToolbar = (Toolbar) findViewById( R.id.toolbar );
    // setActionBar( mToolbar );
    // resetToolbar();

    // if ( ! TDandroid.canRun( this, this ) ) {
    //   // TDLog.v("PERM " + "cannot run");
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), "Required Permissions not granted", 
    //     new DialogInterface.OnClickListener() {
    //       @Override
    //       public void onClick( DialogInterface dialog, int btn ) { 
    //         // TDLog.v("PERM " + "create perms");
    //         ++ mRequestPermissionTime;
    //         TDandroid.createPermissions( mApp, mActivity, mRequestPermissionTime );
    //       }
    //     } );
    // } 

    // mApp_mCheckPerms = TopoDroidApp.mCheckPerms;

    // TDLog.v("VERSION " + TDVersion.string() );

    // The following is good to get permission to the document-tree
    // which does not present a File API to the files
    // For example /sdcard/Documents is 
    //     content://com.android.externalstorage.documents/tree/primary%3ADocuments
    // 
    // if ( ! TDInstance.hasFolderPermission() ) {
    //   TDLog.v("no folder permission ");
    //   TDLog.v("request TREE URI");
    //   Intent intent = new Intent( Intent.ACTION_OPEN_DOCUMENT_TREE );
    //   intent.addFlags( Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    //                  | Intent.FLAG_GRANT_WRITE_URI_PERMISSION 
    //                  | Intent.FLAG_GRANT_READ_URI_PERMISSION );
    //   startActivityForResult( intent, TDRequest.REQUEST_TREE_URI );
    // }
  }

  static private boolean done_init_dialogs = false;

  /** display the init dialogs
   * @param say_dialog_r  whether to show the dialog R - this seems to be always false
   * @note called also by DialogR
   */
  void showInitDialogs( boolean say_dialog_r )
  {
    // TDLog.v( "INIT dialogs - already done: " + done_init_dialogs );
    if ( done_init_dialogs ) return;
    String app_dir = TDInstance.context.getExternalFilesDir( null ).getPath();
    // TDLog.v( "INIT dialogs: app_dir <" + app_dir + ">" );
    say_dialogR = say_dialog_r;
    // if ( false && say_dialogR ) { // FIXME_R
    //   TDLog.v( "DIALOG R: delaying init environment second");
    //   (new DialogR( this, this)).show();
    //   return;
    // } 
    done_init_dialogs = true;
    // TDLog.v( "INIT environment second");
    boolean ok_folder = TopoDroidApp.initEnvironmentSecond( say_dialogR );

    // TDLog.v( "INIT environment second done " + ok_folder );
    // if ( TDVersion.targetSdk() > 29 ) { // FIXME_TARGET_29
    //   TDLog.v( "init environment target " + TDVersion.targetSdk() );
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), ( ok_folder ? R.string.target_sdk : R.string.target_sdk_stale ),
    //     new DialogInterface.OnClickListener() {
    //       @Override public void onClick( DialogInterface dialog, int btn ) { finish(); }
    //     }
    //   );
    // } else 
    if ( ! ok_folder ) {
      TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString(R.string.tdx_stale), R.string.button_ok,
        -1, // R.string.button_help,
        new DialogInterface.OnClickListener() {
          @Override public void onClick( DialogInterface dialog, int btn ) { finish(); }
        }, 
        null
        // new DialogInterface.OnClickListener() {
        //   @Override public void onClick( DialogInterface dialog, int btn ) {
        //     new WebView
        //   }
        // }
      );
    }

    // TDLog.v( "INIT welcome screen");
    if ( mApp.mWelcomeScreen ) {
      TopoDroidApp.setBooleanPreference( "DISTOX_WELCOME_SCREEN", false );
      mApp.mWelcomeScreen = false;
      if ( mApp.mSetupScreen ) {
        TopoDroidApp.setBooleanPreference( "DISTOX_SETUP_SCREEN", false );
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
    
    // TDLog.v( "INIT symbols");
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
    // if ( mApp_mCheckPerms >= 0 )
    {
      Thread loader = new Thread() {
        @Override
        public void run() {
          mApp.startupStep2();
          Resources res = getResources();
          BrushManager.reloadPointLibrary( mApp, res ); // reload symbols
          BrushManager.reloadLineLibrary( res );
          BrushManager.reloadAreaLibrary( res );
          BrushManager.setHasSymbolLibraries( true );
          BrushManager.doMakePaths( );
          WorldMagneticModel.loadEGM9615( mApp );
	  enablePaletteButton();
        }
      };
      loader.setPriority( Thread.MIN_PRIORITY );
      loader.start();
    }
    setTheTitle();

    // TDLog.v( "INIT check intent");
    Intent intent = getIntent();
    if ( intent != null ) {
      Bundle extras = intent.getExtras();
      if ( extras != null ) {
        String action = intent.getExtras().getString("action");
        if ( action != null ) {
          // TDLog.v("SHORT action " + action);
          if ( action.equals("new_survey") ) {
            startSurveyDialog( );
          } else if ( action.equals("distox") ) {
            startDistoXActivity();
          }
        }
      }
    }
  }

  // /** delete tdr backup files --> CLEAR_BACKUPS MOVE TO SurveyWindow
  //  */
  // private void doBackupsClear()
  // {
  //   Thread cleaner = new Thread() {
  //     @Override
  //     public void run() { 
  //       File dir = TDPath.getTdrDir();
  //       // TDLog.v( "clear tdr backups from " + dir.getPath() );
  //       File[] files = dir.listFiles( new FileFilter() { 
  //         public boolean accept( File f ) { 
  //             return f.getName().matches( ".*tdr.bck.?" );
  //         }
  //       } );
  //       if ( files != null ) {
  //         for (File f : files) {
  //           TDFile.deleteFile( f );
  //         }
  //       }
  //     }
  //   };
  //   // cleaner.setPriority( Thread.MIN_PRIORITY );
  //   cleaner.start();
  // }

  private int mNrButton1 = 5;

  void resetButtonBar()
  {
    int size = TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );
    MyButton.resetCache( size );
    // TDToast.make( "SIZE " + size );
    Resources res = getResources();

    // FIXME THMANAGER
    mNrButton1 = 4;
    if ( TDLevel.overNormal ) {
      ++mNrButton1; // CAVE3D
    }
    if ( TDLevel.overExpert ) {
      ++ mNrButton1; // TH MANAGER
    }
    mButton1 = new Button[ mNrButton1 + 1 ];

    TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, res, R.drawable.iz_menu ) );
    for (int k=0; k < mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
      // mButton1[k].setElevation(40);
    }
    mButton1[mNrButton1] = MyButton.getButton( mActivity, this, R.drawable.iz_empty );

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mButtonDistoX2 = MyButton.getButtonBackground( this, res, R.drawable.iz_disto2b );
    mButtonDistoX1 = MyButton.getButtonBackground( this, res, R.drawable.iz_disto1b );
    mButtonSap5    = MyButton.getButtonBackground( this, res, R.drawable.iz_sap5 );
    mButtonBric4   = MyButton.getButtonBackground( this, res, R.drawable.iz_bric4 );

    // mButton1[2].setOnLongClickListener( this ); // IMPORT ZIP
    setButtonDevice();
    // mRelLayout.invalidate();
  }

  // FIXME TOOLBAR void resetToolbar()
  // {
  //   int size = TDSetting.mSizeButtons;
  //   MyButton.resetCache( size );

  //   LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

  //   // FIXME THMANAGER
  //   mNrButton1 = 4;
  //   if ( TDLevel.overExpert ) mNrButton1 ++; // TH MANAGER
  //   mButton1 = new Button[mNrButton1];

  //   for (int k=0; k<mNrButton1; ++k ) {
  //     mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
  //     mToolbar.addView( mButton1[k], params );
  //   }

  //   mMenuImage = MyButton.getButton( mActivity, this, R.drawable.ic_menu );
  //   TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, getResources(), R.drawable.iz_menu ) );
  //   mToolbar.addView( mMenuImage, params );
  // }

  /** set the icon of the "device" button
   */
  public void setButtonDevice()
  {
    if ( TDInstance.isDeviceX310() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonDistoX2 );
    } else if ( TDInstance.isDeviceA3() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonDistoX1 );
    } else if ( TDInstance.isDeviceSap() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonSap5 );
    } else if ( TDInstance.isDeviceBric() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonBric4 );
    } else {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonDistoX2 );
    }
  }
    

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

    // TDLog.Profile("Main Window on Start");
    // TDLog.v("MAIN on Start");
    if ( do_check_bt ) {
      do_check_bt = false;
      if ( DeviceUtil.hasAdapter() ) {
        if ( TDSetting.mCheckBT == 1 && ! DeviceUtil.isAdapterEnabled() ) {    
          if ( TDandroid.BELOW_API_31 ) {
            try {
              Intent enableIntent = new Intent(DeviceUtil.ACTION_REQUEST_ENABLE);
              startActivityForResult(enableIntent, TDRequest.REQUEST_ENABLE_BT);
            } catch ( SecurityException e ) {
              TDLog.Error("SECURITY error " + e.getMessage() );
            }
          } else {
            TDToast.makeBad( R.string.no_bt );
          }
        // } else {
          // nothing to do: scanBTDevices(); is called by menu CONNECT
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

    // if ( ! TDandroid.canManageExternalStorage( this ) ) {
    //   TDLog.v("MAIN cannot manage external storage");
    //   (new DialogR( this, this)).show(); // FIXME_R
    // } else
    {
      // the database is opened after the second step of envs initialization
      TDLog.v("MAIN can manage external storage - has db " + TopoDroidApp.hasTopoDroidDatabase() );
      // ++ mRequestPermissionTime;
      // int perms = TDandroid.createPermissions( mApp, mActivity, mRequestPermissionTime );

      if ( TDandroid.canRun( mApp, mActivity ) ) {
        // TDLog.v("MAIN can run - has db " + TopoDroidApp.hasTopoDroidDatabase() + " init envs first");
        mApp.initEnvironmentFirst( );
        // TDLog.v("MAIN show init dialogs [1]");
        showInitDialogs( false /* ! TopoDroidApp.hasTopoDroidDatabase() */ );
        // resetButtonBar();
      } else {
        ++ mRequestPermissionTime;
        TDLog.v("MAIN cannot run - has db " + TopoDroidApp.hasTopoDroidDatabase() + " request perms time " + mRequestPermissionTime );
        if ( TDandroid.createPermissions( mApp, mActivity, mRequestPermissionTime ) == 0 ) {
          mApp.initEnvironmentFirst( );
          // TDLog.v("MAIN show init dialogs [2]");
          showInitDialogs( false /* ! TopoDroidApp.hasTopoDroidDatabase() */ );
          // resetButtonBar();
        // } else {  // the followings are delayed after the permissions have been granted
        //   mApp.initEnvironmentFirst( );
        //   if ( perms == 0 ) showInitDialogs( ! TopoDroidApp.hasTopoDroidDatabase() );
        }
      }
    }

    if ( TDSetting.isFlagButton() ) resetButtonBar(); // 6.0.33
    // if ( TDSetting.isFlagMenu() ) {
    //   setMenuAdapter( );
    //   closeMenu();
    // }
    TDLocale.resetTheLocale();
    setMenuAdapter( );
    closeMenu();
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // TDLog.v("MAIN on Resume");
    // resetButtonBar();  // 6.0.33
    // setMenuAdapter();
    // closeMenu();

    // TDLog.v( "onResume runs on " + Thread.currentThread().getId() );

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
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    // TDLog.v("MAIN on Pause");
    // TDLog.Log( TDLog.LOG_MAIN, "onPause " );
    mApp.suspendComm();
  }

  @Override
  public void onStop()
  {
    super.onStop();
    // TDLog.v("MAIN on Stop");
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
    // TDLog.v( "MAIN on Destroy " );
    // FIXME if ( mApp.mComm != null ) { mApp.mComm.interrupt(); }
    // FIXME BT_RECEIVER mApp.resetCommBTReceiver();
    // saveInstanceToData();

    mApp.stopPairingRequest();
    if ( say_dialogR ) {
      android.os.Process.killProcess( android.os.Process.myPid() );
    }
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

  public void doCloseApp()
  {
    super.onBackPressed();
  }

  // ------------------------------------------------------------------
 
  /** attach base context
   * @param ctx   context
   */
  @Override
  protected void attachBaseContext( Context ctx )
  {
    // TDLog.v("MAIN attach base context");
    TDInstance.context = ctx;
    TDLocale.resetTheLocale( );
    super.attachBaseContext( TDInstance.context );
  }


  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // TDLog.v( "Main Window on Activity Result " + request + " " + result );
    // TDLog.v( "onActivityResult runs on " + Thread.currentThread().getId() );
    // TDLog.Log( TDLog.LOG_MAIN, "on Activity Result: request " + mRequestName[request] + " result: " + result );
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case TDRequest.REQUEST_ENABLE_BT:
        // TDLocale.resetTheLocale(); // OK-LOCALE apparently this does not affect locale
        if ( result == Activity.RESULT_OK ) {
          // nothing to do: scanBTDevices() is called by menu CONNECT
        } else if ( ! say_dialogR && say_not_enabled ) {
          say_not_enabled = false;
          TDToast.makeBad(R.string.not_enabled );
          // finish();
        }
        // FIXME_BT
        // FIXME mApp.mBluetooth = ( result == Activity.RESULT_OK );
        // setBTMenus( DeviceUtil.isAdapterEnabled() );
        updateDisplay( );
        break;
      case TDRequest.REQUEST_SETTINGS:
        if ( result == Activity.RESULT_OK ) {
          if ( TDSetting.isFlagMenu() ) {
            // TDLog.v("MAIN setting activity result: menu flag");
            setMenuAdapter( );
          }
        } else {
          TDLog.Error("SETTINGS canceled");
        }
        break;
      case TDRequest.REQUEST_GET_IMPORT:
        if ( result == Activity.RESULT_OK ) {
          String filename;
          Uri uri = intent.getData();
          String mimetype = TDsafUri.getDocumentType( uri );
          if ( mimetype == null ) {
            String path = TDsafUri.getDocumentPath(this, uri);
            if (path == null) {
              // filename = FilenameUtils.getName(uri.toString());
              filename = uri.getLastPathSegment();
              int ros = filename.indexOf(":"); // drop the "content" header
              if ( ros >= 0 ) filename = filename.substring( ros+1 ); 
              // TDLog.v("import path NULL filename " + filename );
              if ( filename != null ) {
                int pos = filename.lastIndexOf("/");
                filename = filename.substring( pos+1 );
              }
              TDLog.Error( "URI to import: " + uri.toString() + " null mime, null path, filename <" + filename + ">" );
            } else {
              // filename = (new File(path)).getName(); // FILE to get the survey name
              int pos = path.lastIndexOf('/');
              filename = ( pos >= 0 )? path.substring(pos+1) : path;
              int ros = filename.indexOf(":"); // drop the "content" header
              if ( ros >= 0 ) filename = filename.substring( ros+1 ); 
              // TDLog.v("import path " + path + " filename " + filename );
              TDLog.Error( "URI to import: " + uri.toString() + " null mime, filename <" + filename + ">" );
            }
          } else {
            filename = uri.getLastPathSegment();
            int ros = filename.indexOf(":"); // drop the "content" header
            if ( ros >= 0 ) filename = filename.substring( ros+1 ); 
            int pos   = filename.lastIndexOf("."); 
            int qos_1 = filename.lastIndexOf("/") + 1;
            String ext  = (pos >= 0 )? filename.substring( pos ).toLowerCase() : ""; // extension with leading '.'
            String name = (pos > qos_1 )? filename.substring( qos_1, pos ) : filename.substring( qos_1 );
            // TDLog.v( "URI to import: " + filename + " mime " + mimetype + " name <" + name + "> ext <" + ext + ">" );
            if ( mimetype.equals("application/zip") ) {
              // String surveyname = name;
              ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
              FileInputStream fis = TDsafUri.docFileInputStream( pfd );
              // if ( fis.markSupported() ) fis.mark();
              int manifest_ok = Archiver.getOkManifest( fis, /* name, */ name ); // NOTE last arg was surveyname
              try { fis.close(); } catch ( IOException e ) { }
              TDsafUri.closeFileDescriptor( pfd );
              if ( manifest_ok >= 0 ) {
                ParcelFileDescriptor pfd2 = TDsafUri.docReadFileDescriptor( uri );
                FileInputStream fis2 = TDsafUri.docFileInputStream( pfd2 );
                Archiver.unArchive( mApp, fis2, name ); 
                try { fis2.close(); } catch ( IOException e ) { }
                TDsafUri.closeFileDescriptor( pfd2 );
              } else {
                TDLog.Error("ZIP import: failed manifest " + manifest_ok );
                TDToast.makeBad( R.string.bad_manifest );
              }
            } else {
              String type = TDPath.checkImportTypeStream( ext );
              if ( type != null ) {
                importStream( uri, name, type );
              } else {
                type = TDPath.checkImportTypeReader( ext );
                if ( type != null ) {
                  // TDLog.v( "import reader type " + type + " name " + name);
                  importReader( uri, name, type, mImportData );
                } else {
                  TDLog.Error("import unsupported " + ext);
                }
              }
            }
            // Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
            // int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            // int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            // returnCursor.moveToFirst();
            // filename = returnCursor.getString(nameIndex);
            // String size = Long.toString(returnCursor.getLong(sizeIndex));
          }
          // sample URI: content://com.mixplorer.file/509!s/TopoDroid-03/zip/580test.zip
          // importFile( item );
        } else {
          TDLog.Error("IMPORT canceled");
        }
        break;
      // case TDRequest.REQUEST_TREE_URI:
      //   if ( result == Activity.RESULT_OK ) {
      //     TDInstance.handleRequestTreeUri( intent );
      //   }
      //   break;
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

  /* FIXME-23 */
  // this is called only for androidx.appcompat.app.AppCompatActivity so it is pretty useless
  @Override
  public void onRequestPermissionsResult( int code, final String[] perms, int[] results )
  {
    // TDLog.v("PERM " + "MAIN perm request result " + results.length );
    if ( code == TDandroid.REQUEST_PERMISSIONS ) {
      if ( results.length > 0 ) {
        int granted = 0;
	for ( int k = 0; k < results.length; ++ k ) {
	  TDandroid.GrantedPermission[k] = ( results[k] == PackageManager.PERMISSION_GRANTED );
	  // TDLog.v("PERM " + "MAIN perm " + k + " perms " + perms[k] + " result " + results[k] );
	}
        ++ mRequestPermissionTime;
        int not_granted = TDandroid.createPermissions( mApp, mActivity, mRequestPermissionTime );
        // TDLog.v("PERM " + "MAIN perm finish setup with " + not_granted + " at time " + mRequestPermissionTime );
        if ( ! TDandroid.canRun( mApp, this ) ) { // if ( not_granted > 0 /* && ! say_dialogR */ )
          TDToast.makeLong( "Permissions not granted. Goodbye" );
          if ( mRequestPermissionTime > 2 ) { 
            finish();
          }
        } else {
          // the database is opened after the second step of envs initialization
          mApp.initEnvironmentFirst( );
          showInitDialogs( false /* ! TopoDroidApp.hasTopoDroidDatabase() */ );
          resetButtonBar();
        }
      }
    }
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

  // check whether to ask user to update manual translation
  private void checkManualTranslation()
  {
    if ( TDFile.hasManFile( "manifest" ) ) {
      String lang = null;
      int version = -1;
      try {
        FileReader fr = TDFile.getManFileReader( "manifest" );
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
      // TDLog.v("MAN manifest " + lang + " " + version );
      if ( lang != null && version > 0 ) {
        int res = 0;
        int man = 0;
        if ( "ru".equals( lang ) ) { 
          res = R.string.man_version_ru;
          man = R.string.user_man_ru;
        } else if ( "it".equals( lang ) ) {
          res = R.string.man_version_it;
          man = R.string.user_man_it;
        } else if ( "es".equals( lang ) ) {
          res = R.string.man_version_es;
          man = R.string.user_man_es;
        } else if ( "hu".equals( lang ) ) {
          res = R.string.man_version_hu;
          man = R.string.user_man_hu;
        } else if ( "fr".equals( lang ) ) {
          res = R.string.man_version_fr;
          man = R.string.user_man_fr;
        // } else if ( "sl".equals( lang ) ) {
        //   res = R.string.man_version_sl;
        //   man = R.string.user_man_sl;
        }
        if ( res > 0 ) {
          int current = Integer.parseInt( getResources().getString( res ) );
          if ( current > version ) { // prompt user
            final String url = getResources().getString( man );
            TDLog.Log( TDLog.LOG_PREFS, "User Manual lang " + lang + " res " + res + " url " + url );
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

  private static int mImportType; 
  private static ImportData mImportData = new ImportData();

  ImportData getImportData() { return mImportData; }

  public void doImport( String type, ImportData data )
  {
    int index = TDConst.surveyFormatIndex( type );
    // TDLog.v( "import " + type + " " + index );
    selectImportFromProvider( index, data );
  }

  private void selectImportFromProvider( int index, ImportData data ) // IMPORT
  {
    // TDLog.v( "selectImportFromProvider runs on " + Thread.currentThread().getId() );
    Intent intent = new Intent( Intent.ACTION_OPEN_DOCUMENT );
    intent.setType( TDConst.mMimeType[ index ] );
    // TDLog.v( "Import from provider. index " + index + " mime " + TDConst.mMimeType[ index ] );
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    // intent.putExtra( "importtype", index ); // extra is not returned to the app
    mImportData = data;
    mImportData.mType = index;
    startActivityForResult( Intent.createChooser(intent, getResources().getString( R.string.title_import_shot ) ), TDRequest.REQUEST_GET_IMPORT );
  }


}
