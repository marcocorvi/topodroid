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
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDVersion;
import com.topodroid.utils.CWDfolder;
import com.topodroid.common.ExportInfo;

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
import com.topodroid.inport.ImportBricCsvTask;
import com.topodroid.inport.ImportCavwayCsvTask;
import com.topodroid.inport.ImportCompassTask;
import com.topodroid.inport.ImportVisualTopoTask;
import com.topodroid.inport.ImportTherionTask;
import com.topodroid.inport.ImportPocketTopoTask;
import com.topodroid.inport.ImportSurvexTask;
import com.topodroid.inport.ImportWallsTask;
import com.topodroid.inport.ImportTRobotTask;
import com.topodroid.inport.ImportCaveSniperTask;
import com.topodroid.inport.ImportZipTask;
// import com.topodroid.inport.ImportDialog;
// import com.topodroid.inport.ImportDatDialog;
// import com.topodroid.inport.ImportTroDialog;

import com.topodroid.TDX.TDToast;

import com.topodroid.mag.WorldMagneticModel;

// import java.io.File;
import java.io.FileInputStream;
// import java.io.InputStreamReader;
// import java.io.FileFilter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
// import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.ActivityNotFoundException;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.pm.PackageManager;

// import android.provider.Settings;

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

import java.util.Locale;

// import java.io.File;

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
                        , IExporter
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
  private BitmapDrawable  mButtonDistoX0;
  private BitmapDrawable  mButtonDistoX1;
  private BitmapDrawable  mButtonDistoX2;
  private BitmapDrawable  mButtonDistoX3;
  private BitmapDrawable  mButtonSap5;
  private BitmapDrawable  mButtonSap6;
  private BitmapDrawable  mButtonBric4;
  private BitmapDrawable  mButtonBric5;
  private BitmapDrawable  mButtonCavwayX1;

  private final  int BTN_DEVICE = 0;

  private static final int[] izons = {
                          R.drawable.iz_disto2b, // iz_disto,
                          R.drawable.iz_plus,
                          R.drawable.iz_import,
                          R.drawable.iz_tools,   // iz_palette
                          R.drawable.iz_3d,      // CAVE3D
                          R.drawable.iz_manager, // THMANAGER
                          R.drawable.iz_plot     // TH2EDIT
                          // R.drawable.iz_database
			  // R.drawable.iz_empty // EMPTY
                          };

  private static final int[] menus = {
                          R.string.menu_close,
                          R.string.menu_cwd,
                          R.string.menu_palette,
                          // R.string.menu_logs, // NO_LOGS
			  // R.string.menu_backups, // CLEAR_BACKUPS
                          // R.string.menu_join_survey,
			  // R.string.menu_updates, // UPDATES
                          R.string.menu_export,
                          R.string.menu_about,
                          R.string.menu_options,
                          R.string.menu_help,
                          };

  private static final int[] help_icons = { R.string.help_device,
                          R.string.help_add_topodroid,
                          R.string.help_import,
                          R.string.help_symbol,
                          R.string.help_cave3d,
                          R.string.help_therion, // THMANAGER
                          R.string.help_drawing, // TH2EDIT
                          // R.string.help_database
                          };
  private static final int[] help_menus = {
                          R.string.help_close_app,
                          R.string.help_cwd,
                          R.string.help_symbol,
                          // R.string.help_log, // NO_LOGS
			  // R.string.help_backups, // CLEAR_BACKUPS
                          // R.string.help_join_survey,
                          // R.string.help_updates, // UPDATES
                          R.string.help_export_surveys,
                          R.string.help_info_topodroid,
                          R.string.help_prefs,
                          R.string.help_help,
                        };

  private static final int HELP_PAGE = R.string.MainWindow;

  // -------------------------------------------------------------
  private boolean say_no_survey   = true;
  private boolean say_not_enabled = true; // whether to say that BT is not enabled
  private boolean do_check_bt     = true;     // one-time bluetooth check sentinel

  static private boolean mPaletteButtonEnabled = false;
  static void enablePaletteButton() { mPaletteButtonEnabled = true; }

  // -------------------------------------------------------------------

  /** @return the application
   */
  public TopoDroidApp getApp() { return mApp; }
    
  /** update the display
   */
  public void updateDisplay( )
  {
    if ( TopoDroidApp.mData != null ) {
      List< String > list = TopoDroidApp.mData.selectAllSurveys();
      updateList( list );
      if ( (! TopoDroidApp.sayDialogR()) && say_no_survey && TDUtil.isEmpty(list) ) { // PRIVATE_STORAGE : sayDialogR was commented
        say_no_survey = false;
        TDToast.make( R.string.no_survey );
      } 
    }
  }

  /** update the list of surveys
   * @param list   new list of survey names
   */
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
    mApp.setSurveyFromName( null, SurveyInfo.DATAMODE_NORMAL, true, true ); // new-survey dialog: tell app to clear survey name and id
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
        // } else { // start Cave3D even if the version is below required minimum
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
          TDLog.e( "Td Manager activity not started" );
        }
      } else if ( TDLevel.overExpert && TDSetting.mTh2Edit && k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // TH2EDIT DRAWING
        try {
          Intent plotIntent = new Intent( Intent.ACTION_VIEW ).setClass( this,  DrawingWindow.class );
          plotIntent.putExtra( TDTag.TOPODROID_SURVEY_ID, -1L );
          startActivity( plotIntent );
        } catch ( ActivityNotFoundException e ) {
          // TDToast.makeBad( R.string.no_thmanager );
          TDLog.e( "Th2 edit activity not started" );
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
    mApp.setSurveyFromName( value, -1, true, true ); // open survey activity: tell app to update survey name+id
    Intent surveyIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SurveyWindow.class );
    surveyIntent.putExtra( TDTag.TOPODROID_SURVEY, mustOpen );
    // surveyIntent.putExtra( TDTag.TOPODROID_OLDSID, old_sid );
    // surveyIntent.putExtra( TDTag.TOPODROID_OLDID,  old_id );
    mActivity.startActivity( surveyIntent );
  }

  void startSplitSurvey( long old_sid, long old_id )
  {
    TDLog.v( "start split survey");
    mApp.setSurveyFromName( null, SurveyInfo.DATAMODE_NORMAL, true, true ); // FIXME CO-SURVEY
    (new SurveyNewDialog( mActivity, this, old_sid, old_id )).show(); // WITH SPLIT
  }

  /** start to move survey data to a new survey
   * @param old_sid    source survey ID
   * @param old_id     start data ID
   * @param new_survey name of the new survey
   */
  void startMoveSurvey( long old_sid, long old_id, String new_survey )
  {
    TDLog.v( "start move survey");
    if ( mApp.moveSurveyData( old_sid, old_id, new_survey ) ) {
      mApp.setSurveyFromName( null, SurveyInfo.DATAMODE_NORMAL, true, true ); // FIXME CO-SURVEY
    // } else {
    }
  }

  /** react to a user long tap on an item in the survey list or on a menu
   * @param parent   view container
   * @param view     tapped view
   * @param pos      entry position
   * @param id       ???
   */
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

  /** open a survey
   * @param name survey name
   */
  void doOpenSurvey( String name )
  {
    mApp.setSurveyFromName( name, -1, true, true ); // open survey: tell app to update survey name+id
    Intent intent = new Intent( this, ShotWindow.class );
    intent.setAction( Intent.ACTION_VIEW ); // SDK-35
    startActivity( intent );
  }

  /** react to a user tap on an entry in the survey list
   * @param parent   view container
   * @param view     tapped view
   * @param pos      entry position
   * @param id       ???
   */
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
      TDLog.e("import view instance of " + view.toString() );
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

  /** start the shot window activity
   * @param item   survey name
   */
  public void startShowWindow( CharSequence item )
  {
    mApp.setSurveyFromName( item.toString(), -1, true, true ); 
    Intent intent = new Intent( this, ShotWindow.class );
    intent.setAction( Intent.ACTION_VIEW ); // SDK-35
    startActivity( intent );
  }

  /** default window title: current work directory
   */
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

  /** set the title while importing a survey from file
   */
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
  //   if ( filename.toLowerCase( Locale.getDefault() ).endsWith(".th") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     String name = filename.replace(".th", "" ).replace(".TH", "");
  //     if ( TopoDroidApp.mData.hasSurveyName( name ) ) {
  //       TDToast.makeBad(R.string.import_already );
  //       return;
  //     }
  //     // TDToast.make( R.string.import_wait );
  //     setTitleImport();
  //     new ImportTherionTask( this, null ).execute( filepath, name );  // null FileReader
  //   } else if ( filename.toLowerCase( Locale.getDefault() ).endsWith(".dat") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     (new ImportDatDialog( this, this, null, filepath )).show();
  //     // new ImportCompassTask( this ).execute( filepath );
  //   } else if ( filename.toLowerCase( Locale.getDefault() ).endsWith(".top") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     setTitleImport();
  //     new ImportPocketTopoTask( this, null ).execute( filepath, filename ); // TODO pass the drawer as arg
  //   } else if ( filename.toLowerCase( Locale.getDefault() ).endsWith(".tro") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     (new ImportTroDialog( this, this, null, filepath )).show();
  //   } else if ( filename.toLowerCase( Locale.getDefault() ).endsWith(".svx") ) {
  //     String filepath = TDPath.getImportFile( filename );
  //     setTitleImport();
  //     new ImportSurvexTask( this, null ).execute( filepath ); 
  //   } else if ( filename.toLowerCase( Locale.getDefault() ).endsWith(".csn") ) { // CaveSniper text file
  //     String filepath = TDPath.getImportFile( filename );
  //     new ImportCaveSniperTask( this, null ).execute( filepath ); 
  //     setTitleImport();
  //   } else if ( filename.toLowerCase( Locale.getDefault() ).endsWith(".zip") ) {
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
    if ( ! filename.toLowerCase( Locale.getDefault() ).endsWith(".zip") ) return;
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
    TDLog.v( "import with reader <" + name + "> type <" + type + ">" );
    ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
    // InputStreamReader isr = new InputStreamReader( TDsafUri.docFileInputStream( pfd ) );
    if ( type.equals( TDPath.TH ) ) {
      setTitleImport();
      new ImportTherionTask( this, pfd, data ).execute( name, name );
    } else if ( type.equals( TDPath.DAT ) ) {
      setTitleImport();
      new ImportCompassTask( this, pfd, data ).execute( name, name );
      // (new ImportDatDialog( this, this, pfd, name )).show();
    } else if ( type.equals( TDPath.TRO ) || type.equals( TDPath.TROX ) ) {
      setTitleImport();
      // TDLog.v("type " + type + " data.trox " + data.mTrox );
      new ImportVisualTopoTask( this, pfd, data ).execute( name, name );
      // (new ImportTroDialog( this, this, pfd, name )).show();
    } else if ( type.equals( TDPath.SVX ) ) {
      setTitleImport();
      new ImportSurvexTask( this, pfd ).execute( name ); 
    } else if ( type.equals( TDPath.SRV ) ) {
      setTitleImport();
      new ImportWallsTask( this, pfd ).execute( name ); 
    } else if ( type.equals( TDPath.TRB ) ) {
      setTitleImport();
      new ImportTRobotTask( this, pfd ).execute( name ); 
    } else if ( type.equals( TDPath.CSN ) ) {
      setTitleImport();
      new ImportCaveSniperTask( this, pfd ).execute( name ); 
    } else if ( type.equals( TDPath.CSV ) ) {
      setTitleImport();
      if ( data.mCsv == ImportData.CSV_BRIC ) {
        new ImportBricCsvTask( this, pfd ).execute( name ); 
      } else if ( data.mCsv == ImportData.CSV_CAVWAY ) {
        new ImportCavwayCsvTask( this, pfd ).execute( name ); 
      } else { // sould not occur
        TDLog.e( "no CSV type specified" );
        // TDToast.makeWarn( R.string.warning_no_csv_type );
      }
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
  // private boolean mWithLogs     = false; // NO_LOGS
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
    // mWithLogs     = TDLevel.overAdvanced; // NO_LOGS
    // mWithBackupsClear = TDLevel.overExpert && TDSetting.mBackupsClear; // CLEAR_BACKUPS
    // TDLog.v("Main Window set menu adapter. With palette " + mWithPalette + " With palettes " + mWithPalettes );

    menu_adapter.add( res.getString( menus[0] ) ); // CLOSE
    menu_adapter.add( res.getString( menus[1] ) ); // CWD
    if ( mWithPalette ) menu_adapter.add( res.getString( menus[2] ) ); // PALETTE
    // if ( mWithLogs )    menu_adapter.add( res.getString( menus[2] ) ); // NO_LOGS
    // if ( mWithBackupsClear ) menu_adapter.add( res.getString( menus[3] ) ); // CLEAR_BACKUPS
    // if ( TDLevel.overExpert && mApp_mCosurvey ) menu_adapter.add( res.getString( menus[2] ) ); // IF_COSURVEY
    // if ( TDLevel.overExpert )   menu_adapter.add( res.getString( menus[3] ) ); // UPDATES
    if ( TDLevel.overExpert )   menu_adapter.add( res.getString( menus[3] ) ); // EXPORT
    menu_adapter.add( res.getString( menus[4] ) ); // ABOUT
    menu_adapter.add( res.getString( menus[5] ) ); // SETTINGS
    menu_adapter.add( res.getString( menus[6] ) ); // HELP

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
      } else if ( p++ == pos ) { // DISTOX_CWD
        intent = new Intent( TDInstance.context, com.topodroid.TDX.CWDActivity.class ); // this
        startActivityForResult( intent, TDRequest.REQUEST_CWD );
      } else if ( mWithPalette && p++ == pos ) { // PALETTE EXTRA SYMBOLS
        // (new SymbolEnableDialog( mActivity )).show();
        (new SymbolReload( mActivity, mApp, mWithPalettes )).show();
      // } else if ( mWithLogs && p++ == pos ) { // NO_LOGS
      //   intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      //   intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_LOG );
      //   startActivity( intent );
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
      } else if ( TDLevel.overExpert && p++ == pos ) {  // EXPORT
        String[] types = TDConst.surveyExportTypes( false ); // with_geo=false
        new ExportDialogShot( mActivity, this, types, R.string.title_survey_export, TDInstance.survey, false, false ).show(); // diving=false
      } else if ( p++ == pos ) { // ABOUT
        (new TopoDroidAbout( mActivity, this, -2 )).show();
      } else if ( p++ == pos ) { // SETTINGS
        TDSetting.resetFlag();
        intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
        // intent.setAction( Intent.ACTION_VIEW ); // SDK-35
        intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_ALL );
        startActivityForResult( intent, TDRequest.REQUEST_SETTINGS );
      } else if ( p++ == pos ) { // HELP
        new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE )).show();
      }
    // }
    // updateDisplay();
  }

  void resetUiVisibility()
  {
    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    // TDLog.v("MAIN on Create");

    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

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
    //   // TDLog.v("no folder permission ");
    //   // TDLog.v("request TREE URI");
    //   Intent intent = new Intent( Intent.ACTION_OPEN_DOCUMENT_TREE );
    //   intent.addFlags( Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    //                  | Intent.FLAG_GRANT_WRITE_URI_PERMISSION 
    //                  | Intent.FLAG_GRANT_READ_URI_PERMISSION );
    //   startActivityForResult( intent, TDRequest.REQUEST_TREE_URI );
    // }
  }

  static private boolean done_init_dialogs = false;

  /** display the init dialogs
   * @note called also by DialogR
   */
  void showInitDialogs( ) // PRIVATE_STORAGE dropped arg say_dialog_r effectively unused
  {
    TDLog.v( "INIT dialogs - already done: " + done_init_dialogs /* + " say_dialog_r " + say_dialog_r */ );
    if ( TDandroid.PRIVATE_STORAGE && TopoDroidApp.sayDialogR() ) { // FIXME_R
      TDLog.v( "DIALOG R: delaying init environment second");
      (new DialogR( this, this)).show();
      // TopoDroidApp.setSayDialogR( false );
      return;
    } 
    if ( done_init_dialogs ) return;
    String app_dir = TDInstance.context.getExternalFilesDir( null ).getPath();
    // TDLog.v( "INIT dialogs: app_dir <" + app_dir + ">" );
    done_init_dialogs = true;
    // TDLog.v( "INIT environment second");
    // boolean ok_folder = 
    TopoDroidApp.initEnvironmentSecond( );

    // TDLog.v( "INIT environment second done " + ok_folder );
    // if ( TDVersion.targetSdk() > 29 ) { // FIXME_TARGET_29
    //   // TDLog.v( "init environment target " + TDVersion.targetSdk() );
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), ( ok_folder ? R.string.target_sdk : R.string.target_sdk_stale ),
    //     new DialogInterface.OnClickListener() {
    //       @Override public void onClick( DialogInterface dialog, int btn ) { finish(); }
    //     }
    //   );
    // } else 
    // if ( ! ok_folder ) {
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString(R.string.tdx_stale), R.string.button_ok,
    //     -1, // R.string.button_help,
    //     new DialogInterface.OnClickListener() {
    //       @Override public void onClick( DialogInterface dialog, int btn ) { finish(); }
    //     }, 
    //     null
    //     // new DialogInterface.OnClickListener() {
    //     //   @Override public void onClick( DialogInterface dialog, int btn ) {
    //     //     new WebView
    //     //   }
    //     // }
    //   );
    // }

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
          TDLog.v("Main: app start-up step 2");
          mApp.startupStep2();
          WorldMagneticModel.loadEGM9615( mApp );
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
    TDLog.v("Main Activity: reset button cache, size " + size );
    MyButton.resetButtonCache( size );

    // TDToast.make( "SIZE " + size );
    Resources res = getResources();

    // FIXME THMANAGER
    mNrButton1 = 4;
    if ( TDLevel.overNormal ) {
      ++mNrButton1; // CAVE3D
      if ( TDLevel.overExpert ) {
        ++ mNrButton1; // TH MANAGER
        if ( TDSetting.mTh2Edit ) ++ mNrButton1; // TH2EDIT DRAWING
      }
    }
    mButton1 = new Button[ mNrButton1 + 1 ];

    TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, res, R.drawable.iz_menu ) );
    for (int k=0; k < mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
      // mButton1[k].setElevation(40);
    }
    mButton1[mNrButton1] = MyButton.getButton( mActivity, null, R.drawable.iz_empty );

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mButtonDistoX0 = MyButton.getButtonBackground( this, res, R.drawable.iz_disto0b );
    mButtonDistoX1 = MyButton.getButtonBackground( this, res, R.drawable.iz_disto1b );
    mButtonDistoX2 = MyButton.getButtonBackground( this, res, R.drawable.iz_disto2b );
    mButtonDistoX3 = MyButton.getButtonBackground( this, res, R.drawable.iz_disto3b );
    mButtonSap5    = MyButton.getButtonBackground( this, res, R.drawable.iz_sap5 );
    mButtonSap6    = MyButton.getButtonBackground( this, res, R.drawable.iz_sap6 );
    mButtonBric4   = MyButton.getButtonBackground( this, res, R.drawable.iz_bric4 );
    mButtonBric5   = MyButton.getButtonBackground( this, res, R.drawable.iz_bric5 );
    mButtonCavwayX1 = MyButton.getButtonBackground( this, res, R.drawable.iz_cavwayx1 );

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

  //   mMenuImage = MyButton.getButton( mActivity, this, R.drawable.iz_menu );
  //   TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, getResources(), R.drawable.iz_menu ) );
  //   mToolbar.addView( mMenuImage, params );
  // }

  /** set the icon of the "device" button
   */
  public void setButtonDevice()
  {
    if ( TDInstance.isDeviceX310() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonDistoX2 );
    } else if ( TDInstance.isDeviceXBLE() ) { 
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonDistoX3 );
    } else if ( TDInstance.isDeviceA3() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonDistoX1 );
    } else if ( TDInstance.isDeviceBric4() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonBric4 );
    } else if ( TDInstance.isDeviceBric5() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonBric5 );
    } else if ( TDInstance.isDeviceSap5() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonSap5 );
    } else if ( TDInstance.isDeviceSap6() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonSap6 );
    } else if ( TDInstance.isDeviceCavwayX1() ) {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonCavwayX1 );
    } else {
      TDandroid.setButtonBackground( mButton1[BTN_DEVICE], mButtonDistoX0 );
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

  Intent mZipIntent = null; // ACTION_VIEW for zip intent

  @Override
  public void onStart()
  {
    super.onStart();
    TDLog.v( "Main Activity on Start " );
    // restoreInstanceFromFile();
    // TDLog.v( "MAIN on Start: check BT " + do_check_bt + " enabled " + DeviceUtil.isAdapterEnabled() );
    if ( ! TDandroid.canManageExternalStorage( this ) ) {
      // TDLog.v("MAIN cannot manage external storage");
      TDandroid.requestExternalStorage( this, this );
    }

    // TDLog.Profile("Main Window on Start");
    if ( do_check_bt ) {
      do_check_bt = false;
      if ( DeviceUtil.hasAdapter() ) {
        if ( TDSetting.mCheckBT == 1 && ! DeviceUtil.isAdapterEnabled() ) {    
          if ( TDandroid.BELOW_API_31 ) {
            try {
              Intent enableIntent = new Intent(DeviceUtil.ACTION_REQUEST_ENABLE);
              // enableIntent.setAction( Intent.ACTION_DEFAULT ); // SDK-35
              startActivityForResult(enableIntent, TDRequest.REQUEST_ENABLE_BT);
            } catch ( SecurityException e ) {
              // TDLog.e("SECURITY request bluetooth " + e.getMessage() );
              TDToast.makeBad("Security error: request bluetooth");
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
    //   // TDLog.v("MAIN cannot manage external storage");
    //   (new DialogR( this, this)).show(); // FIXME_R
    // } else
    {
      // the database is opened after the second step of envs initialization
      // TDLog.v("MAIN can manage external storage - has db " + TopoDroidApp.hasTopoDroidDatabase() );
      // int perms = TDandroid.createPermissions( mApp, mActivity, mRequestPermissionTime );

      if ( TDandroid.canRun( mApp, mActivity ) && ! TDandroid.PRIVATE_STORAGE ) {
        // TDLog.v("MAIN can run - has db " + TopoDroidApp.hasTopoDroidDatabase() + " init envs first [1]");
        mApp.initEnvironmentFirst( );
        // TDLog.v("MAIN show init dialogs [1]");
        showInitDialogs( );
        // resetButtonBar();
      } else {
        // if ( TDandroid.PRIVATE_STORAGE && ! TopoDroidApp.hasTopoDroidDatabase() ) TopoDroidApp.setSayDialogR( true );
        TDLog.v("MAIN: has db " + TopoDroidApp.hasTopoDroidDatabase() + " request perms time " + mRequestPermissionTime );
        if ( TDandroid.createPermissions( mApp, mActivity, mRequestPermissionTime ) == 0 ) {
          // TDLog.v("MAIN can run - init envs first [2]");
          mApp.initEnvironmentFirst( );
          // TDLog.v("MAIN show init dialogs [2]");
          showInitDialogs( );
          // resetButtonBar();
        // } else {  // the followings are delayed after the permissions have been granted
        //   mApp.initEnvironmentFirst( );
        //   if ( perms == 0 ) showInitDialogs( ); // this had arg "say_dialog_r = ! TopoDroidApp.hasTopoDroidDatabase()"
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

    Intent intent = getIntent();
    if ( intent != null ) {
      String action = intent.getAction();
      if ( action != null ) {
        // this code is for the action "TopoDroid.intent.action.Import"
        if ( action.equals("TopoDroid.intent.action.Import") ) {
          // boolean ok = false;
          Bundle extras = getIntent().getExtras();  
          if ( extras != null ) {
            String request  = extras.getString( "REQUEST" );
            if ( request != null && request.equals("unarchive") ) {
              Uri data = intent.getData();
              if ( data != null ) {
                mZipIntent = intent;
                // ok = true;
              }
            }
          }
          // if ( ! ok ) {
          //   // setResult( RESULT_CANCELED, new Intent() );
          //   finish();
          // }
        }
        // this code is for the action "android.intent.action.VIEW"
        // else if ( action.equals("android.intent.action.VIEW") ) {
        //   Uri data = intent.getData();
        //   if ( data != null ) {
        //     mZipIntent = intent;
        //     TDLog.v("MAIN got intent " + action + " - mZipPath " + data.getPath() );
        //   } else {
        //     TDLog.v("MAIN got intent " + action + " - null data");
        //   }
        // }
      }
    }
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    TDLog.v( "Main Activity on Resume " );
    // resetButtonBar();  // 6.0.33
    // setMenuAdapter();
    // closeMenu();

    // TODO OPEN DATABASE HERE
    boolean ok_folder = TopoDroidApp.initEnvironmentThird();

    // TDLog.v( "MAIN init environment third done return " + ok_folder );

    // if ( TDVersion.targetSdk() > 29 ) { // FIXME_TARGET_29
    //   // TDLog.v( "init environment target " + TDVersion.targetSdk() );
    //   TopoDroidAlertDialog.makeAlert( this, getResources(), ( ok_folder ? R.string.target_sdk : R.string.target_sdk_stale ),
    //     new DialogInterface.OnClickListener() {
    //       @Override public void onClick( DialogInterface dialog, int btn ) { finish(); }
    //     }
    //   );
    // } else 

    if ( ! ok_folder && ! TDandroid.PRIVATE_STORAGE && mRequestPermissionTime > 0 ) {
      TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString(R.string.tdx_stale), R.string.button_ok,
        -1, // R.string.button_help,
        new DialogInterface.OnClickListener() {
          @Override public void onClick( DialogInterface dialog, int btn ) { finish(); }
        }, 
        null
      );
      return;
    } 
    // FIXME added three calls - but they should not be necessary ...
    mListView.invalidate();
    mMenuImage.invalidate();
    updateDisplay( ); // this was already done
    ((ListView) findViewById(R.id.td_list)).invalidate();

    // TDLog.v( "onResume runs on " + TDLog.threadId() );

    // TDLog.Profile("TDActivity onResume");
    // TDLog.Log( TDLog.LOG_MAIN, "onResume " );
    mApp.resumeComm();

    // restoreInstanceFromFile();

    // This is necessary: switching display off/on there is the call sequence
    //    [off] onSaveInstanceState
    //    [on]  onResume
    if ( TopoDroidApp.mCheckManualTranslation ) {
      TopoDroidApp.mCheckManualTranslation = false;
      checkManualTranslation();
    }

    if ( mZipIntent != null ) {
      String filename = importFile( mZipIntent );
      mZipIntent = null;
      if ( filename != null ) {
        updateDisplay();
        TDToast.make( String.format( getResources().getString( R.string.imported_file ), filename ) );
      }
    }
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    TDLog.v("Main Activity on Pause");
    // TDLog.Log( TDLog.LOG_MAIN, "onPause " );
    mApp.suspendComm();
  }

  @Override
  public void onStop()
  {
    super.onStop();
    TDLog.v("Main Activity on Stop");
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
    TDLog.v( "MAIN on Destroy " );
    // FIXME if ( mApp.mComm != null ) { mApp.mComm.interrupt(); }
    // FIXME BT_RECEIVER mApp.resetCommBTReceiver();
    // saveInstanceToData();

    mApp.stopPairingRequest();
    if ( TopoDroidApp.sayDialogR() ) {
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

  /** handle the HW back-key press
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
      doubleBackToast = TDToast.makeToast( R.string.double_back );
      doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
    }
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

  /** handle the result of an activity request
   * @param request   request code
   * @param result    result code
   * @param intent    result intent
   */
  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // TDLog.v( "Main Window on Activity Result " + request + " " + result );
    // TDLog.v( "onActivityResult runs on " + TDLog.threadId() );
    // TDLog.Log( TDLog.LOG_MAIN, "on Activity Result: request " + mRequestName[request] + " result: " + result );
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case TDRequest.REQUEST_CWD:
        if ( result == RESULT_OK && extras != null ) {
          String cwd = extras.getString( TDTag.TOPODROID_CWD );
          TDLog.v("got CWD <" + cwd + ">" );
          if ( ! TDString.isNullOrEmpty( cwd ) ) {
            TopoDroidApp.setCWDPreference( CWDfolder.folderName( cwd ) );
          }
        } else if ( result == RESULT_CANCELED ) {
	  TDLog.e("could not set CWD");
	}
        break;
      case TDRequest.REQUEST_ENABLE_BT:
        // TDLocale.resetTheLocale(); // OK-LOCALE apparently this does not affect locale
        if ( result == Activity.RESULT_OK ) {
          // nothing to do: scanBTDevices() is called by menu CONNECT
        } else if ( ! TopoDroidApp.sayDialogR() && say_not_enabled ) {
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
          TDLog.e("SETTINGS canceled");
        }
        break;
      case TDRequest.REQUEST_GET_IMPORT: // handle a survey/zip import 
        if ( result == Activity.RESULT_OK ) {
          importFile( intent );
        } else {
          TDLog.e("IMPORT canceled");
        }
        break;
    }
  }

  /** IMPORT: import data from a file
   * @param intent   import intent
   * @return filename or null if fail
   */
  private String importFile( Intent intent )
  {
    String filename = null;       // import filename
    Uri uri = intent.getData();   // import uri - may NullPointerException
    String mimetype = TDsafUri.getDocumentType( uri );
    if ( mimetype == null ) {
      String path = TDsafUri.getDocumentPath(this, uri);
      if (path == null) {
        // filename = FilenameUtils.getName(uri.toString());
        filename = uri.getLastPathSegment();
        int ros = filename.indexOf(":"); // drop the "content" header
        if ( ros >= 0 ) filename = filename.substring( ros+1 ); 
          TDLog.v("MAIN import: path NULL, pathname " + filename );
        // if ( filename != null ) { // always true
          int pos = filename.lastIndexOf("/");
          filename = filename.substring( pos+1 );
        // }
        // TDLog.v( "URI to import: " + uri.toString() + " null mime, null path, filename <" + filename + ">" );
      } else {
        // filename = (new File(path)).getName(); // FILE to get the survey name
        int pos = path.lastIndexOf('/');
        filename = ( pos >= 0 )? path.substring(pos+1) : path;
        int ros = filename.indexOf(":"); // drop the "content" header
        if ( ros >= 0 ) filename = filename.substring( ros+1 ); 
        TDLog.v("MAIN import: path " + path + " filename " + filename );
        // TDLog.v( "URI to import: " + uri.toString() + " null mime, filename <" + filename + ">" );
      }
    } else { // mime not null
      filename = uri.getLastPathSegment();
      // TDLog.v( "MAIN import: uri " + uri.toString() + " mime " + mimetype + " filename <" + filename + ">" );
      int ros = filename.indexOf(":"); // drop the "content" header
      if ( ros >= 0 ) filename = filename.substring( ros+1 ); 
      int pos   = filename.lastIndexOf("."); 
      int qos_1 = filename.lastIndexOf("/") + 1;
      String ext  = (pos >= 0 )? filename.substring( pos ).toLowerCase( Locale.getDefault() ) : ""; // extension with leading '.'
      String name = TDString.spacesToUnderscore( (pos > qos_1 )? filename.substring( qos_1, pos ) : filename.substring( qos_1 ) );
      TDLog.v( "MAIN import URI: filename " + filename + " mime " + mimetype + " name <" + name + "> ext <" + ext + ">" );
      if ( mimetype.equals("application/zip") ) {
        ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
        FileInputStream fis = TDsafUri.docFileInputStream( pfd );
        // if ( fis.markSupported() ) fis.mark();
        int manifest_ok = Archiver.getOkManifest( mApp, fis );
        try { fis.close(); } catch ( IOException e ) {
          TDLog.e( e.getMessage() );
        }
        TDsafUri.closeFileDescriptor( pfd );
        if ( manifest_ok >= 0 ) {
          ParcelFileDescriptor pfd2 = TDsafUri.docReadFileDescriptor( uri );
          FileInputStream fis2 = TDsafUri.docFileInputStream( pfd2 );
          int ret = Archiver.unArchive( mApp, fis2 ); 
          try { fis2.close(); } catch ( IOException e ) {
            TDLog.e( e.getMessage() );
          }
          TDsafUri.closeFileDescriptor( pfd2 );
          if ( ret > 0 ) { // Archiver.ERR_OK_WITH_COLOR_RESET
            TDToast.makeWarn( R.string.archive_reset_color );
          }
        } else {
          TDLog.e("ZIP import: failed manifest error " + manifest_ok );
          int bad = -1;
          switch ( -manifest_ok ) {
            case  1: bad = R.string.bad_manifest_zip;      break;
            case  2: bad = R.string.bad_manifest_td;       break;
            case  3: bad = R.string.bad_manifest_db_old;   break;
            case  4: bad = R.string.bad_manifest_db_new;   break;
            // case  5: bad = R.string.bad_manifest_name;     break;
            case  6: bad = R.string.bad_manifest_present;  break;
            case  7: bad = R.string.bad_manifest_db;       break;
            case  8: bad = R.string.bad_manifest_sql;      break;
            case  9: bad = R.string.bad_manifest_number;   break;
            case 10: bad = R.string.bad_manifest_manifest; break;
            case 11: bad = R.string.bad_manifest_file;     break;
            case 12: bad = R.string.bad_manifest_error;    break;
            case 13: bad = R.string.bad_manifest_missing;  break;
            case 14: bad = R.string.bad_manifest_other;    break;
            default:
              TDToast.makeBad( String.format( getResources().getString( R.string.bad_manifest ), (-manifest_ok) ) );
          }
          if ( bad > 0 ) {
            TDToast.makeBad( bad );
          }
          return null;
        }
      } else {
        // TDLog.v( "MAIN import non-zip: ext " + ext + " mime " + mimetype );
        String type = TDPath.checkImportTypeStream( ext );
        if ( type != null ) {
          TDLog.v( "MAIN import stream: type " + type + " name " + name );
          importStream( uri, name, type );
        } else {
          type = TDPath.checkImportTypeReader( ext );
          if ( type != null ) {
            TDLog.v( "MAIN import reader: type " + type + " filename " + filename );
            importReader( uri, name, type, mImportData );
          } else {
            TDLog.e("MAIN import reader: unsupported type NULL - filename " + filename + " ext " + ext );
            TDToast.makeBad( String.format( getResources().getString( R.string.unsupported_extension ), ext ) );
            return null;
          }
        }
      }
    }
    return filename;
  }

  /** handle a HW key press
   * @param code   key code
   * @param event  key event
   * @return true if the key-press has been handled
   */
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
        // TDLog.e( "key down: code " + code );
    }
    return false;
  }

  /* FIXME-23 */
  // this is called only for androidx.appcompat.app.AppCompatActivity so it is pretty useless
  // it is called on Android-11 API-30
  @Override
  public void onRequestPermissionsResult( int code, final String[] perms, int[] results )
  {
    // TDLog.v(  "MAIN perm request result " + results.length + " request time " + mRequestPermissionTime );
    ++ mRequestPermissionTime;
    if ( code == TDandroid.REQUEST_PERMISSIONS ) {
      if ( results.length > 0 ) {
        int granted = 0;
	for ( int k = 0; k < results.length; ++ k ) {
	  TDandroid.GrantedPermission[k] = ( results[k] == PackageManager.PERMISSION_GRANTED );
	  // TDLog.v( "MAIN perm " + k + " perms " + perms[k] + " result " + results[k] );
	}
        ++ mRequestPermissionTime;
        int not_granted = TDandroid.createPermissions( mApp, mActivity, mRequestPermissionTime );
        if ( ! TDandroid.canRun( mApp, this ) ) { // if ( not_granted > 0 /* && ! TopoDroidApp.sayDialogR() */ )
          TDLog.e("MAIN perm finish setup with " + not_granted + " at time " + mRequestPermissionTime );
          // TDToast.makeLong( "Permissions not granted. Goodbye" );
          if ( mRequestPermissionTime > 2 ) { 
            finish();
          }
        } else {
          // the database is opened after the second step of envs initialization
          mApp.initEnvironmentFirst( );
          showInitDialogs( );
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

  /** check whether to ask user to update manual translation
   */
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
          String[] token = TDString.noSpace(line.trim() ).split("="); // line.trim().replaceAll("\\s+", "").split("=");
          if ( token.length > 1 ) {
            String key = token[0].toUpperCase( Locale.getDefault() );
            if ( key.equals("LANG") ) { 
              lang = token[1].toLowerCase( Locale.getDefault() );
            } else if ( key.equals("VERSION") ) {
              try {
                version = Integer.parseInt( token[1] );
              } catch ( NumberFormatException e ) {
                TDLog.e( e.getMessage() );
              }
            }
          }
        }
        br.close();
      } catch ( FileNotFoundException e ) {
        TDLog.e( e.getMessage() );
      } catch ( IOException e ) {
        TDLog.e( e.getMessage() );
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
        } else if ( "pt".equals( lang ) ) {
          res = R.string.man_version_pt;
          man = R.string.user_man_pt;
        // } else if ( "sl".equals( lang ) ) {
        //   res = R.string.man_version_sl;
        //   man = R.string.user_man_sl;
        } else if ( "cn".equals( lang ) ) {
          res = R.string.man_version_cn;
          man = R.string.user_man_cn;
        }
        if ( res > 0 ) {
          int current = Integer.parseInt( getResources().getString( res ) );
          if ( current > version ) { // prompt user
            final String url = getResources().getString( man );
            // TDLog.Log( TDLog.LOG_PREFS, "User Manual lang " + lang + " res " + res + " url " + url );
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

  // private static int mImportType;  // UNUSED
  private static ImportData mImportData = new ImportData();

  /** @return the import parameters
   */ 
  ImportData getImportData() { return mImportData; }

  /** import a survey
   * @param type   survey file format
   * @param data   import parameters
   */
  public void doImport( String type, ImportData data )
  {
    int index = TDConst.surveyImportFormatIndex( type ); // get the format index as in TDConst
    TDLog.v( "MAIN import type " + type + " index " + index );
    selectImportFromProvider( index, data );
  }

  /** get the import stream from the data provider
   * @param index  file format index (@see TDConst.surveyImportFormatIndex)
   * @param data   import parameters
   * this method saves the import parameters and starts a choice of a file (of the given type)
   */
  private void selectImportFromProvider( int index, ImportData data ) // IMPORT
  {
    if ( index < 0 || index >= TDConst.getMimeTypeLength() ) {
      TDLog.e("Bad import index " + index );
      TDToast.makeBad( String.format( getResources().getString( R.string.index_oob ), index ) );
      return;
    } 
    // TDLog.v( "selectImportFromProvider runs on " + TDLog.threadId() );
    // Intent intent = new Intent( Intent.ACTION_OPEN_DOCUMENT ); // API_19
    // intent.setType( TDConst.getMimeType( index ) );
    // intent.addCategory(Intent.CATEGORY_OPENABLE);
    // intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); // API_19
    Intent intent = TDandroid.getOpenDocumentIntent( index ); // 20230181 replace previous lines
    // intent.putExtra( "importtype", index ); // extra is not returned to the app
    mImportData = data;
    mImportData.mType = index;
    startActivityForResult( Intent.createChooser(intent, getResources().getString( R.string.title_import_shot ) ), TDRequest.REQUEST_GET_IMPORT );
  }

  /** handle the result of DialogR
   * @param res   result: true = accepted, false = rejected
   * @note this is the only place where sayDialogR is cleared (false)
   */
  void resultR( boolean res )
  {
    if ( ! TDandroid.PRIVATE_STORAGE ) return;
    // TDLog.v("MAIN result R " + res );
    if ( ! res ) {
      TopoDroidApp.setSayDialogR( true );
      finish();
    } else {
      TopoDroidApp.setSayDialogR( false );
    }
  }

  public void doExport( String type, String filename, String prefix, long first, boolean second )
  { 
    TDLog.v("Type " + type + " filename " + filename + " prefix " + prefix );
    TDSetting.mExportPrefix = prefix; // save export-prefix
    int index = TDConst.surveyFormatIndex( type );
    String extension = filename.substring( filename.lastIndexOf(".") );
    if ( index > 0 ) {
      // N.B. zip export not supported
      List< String > survey_list = TopoDroidApp.mData.selectAllSurveys();
      ExportInfo export_info = new ExportInfo( index, null, null, first );
      for ( String survey : survey_list ) {
        filename = survey + extension;
        TDLog.v("Index " + index + " Exporting " + filename + " prefix " + prefix );
        if ( prefix != null ) export_info.prefix = survey;
        export_info.name = filename;
        mApp.setSurveyFromName( survey, SurveyInfo.DATAMODE_NORMAL, false, false ); // all_info = false
        doExport( survey, index, filename, export_info );
      }
    }
  }
      

  /** export the data of one surveys
   * @param survey    survey name
   * @param index     export file format
   * @param filename  export filename - short name, eg, "survey.dat"
   * @param prefix    station name prefix (Compass, VTopo, Winkarst)
   * @note called from the public doExport()
   */
  private void doExport( String survey, int index, String filename, ExportInfo export_info )
  {
    TDLog.v( "MAIN do export " + survey + " filename " + filename );
    // if ( index >= 0 ) {
      if ( TDInstance.sid < 0 ) {
        TDToast.makeBad( R.string.no_survey );
      } else {
        if ( index == TDConst.SURVEY_FORMAT_ZIP ) { // EXPORT ZIP
          mApp.doExportDataAsync( getApplicationContext(), export_info, true ); // uri = null
        } else {
          mApp.doExportDataAsync( getApplicationContext(), export_info, true ); // uri = null
        }
      }
    // } else {
    //   TDLog.e("Main Window export - negative index " + index );
    // }
  }

}
