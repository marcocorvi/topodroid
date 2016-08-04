/* @file TopoDroidActivity.java
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

import android.os.AsyncTask;
import android.os.Debug;

// import java.lang.Long;
// import java.lang.reflect.Method;
// import java.lang.reflect.InvocationTargetException;

import android.app.Application;
import android.app.Activity;
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
import android.net.Uri;

import android.bluetooth.BluetoothDevice;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.Dialog;
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

public class TopoDroidActivity extends Activity
                               implements OnItemClickListener
                               , OnItemLongClickListener
                               , View.OnClickListener
                               , OnCancelListener
                               , OnDismissListener
{
  private TopoDroidApp mApp;

  private boolean onMenu; // whether menu is displaying

  private static final int REQUEST_ENABLE_BT = 2;

  private LinearLayout mLayout;
  private ListView mList;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;

  private Button[] mButton1;
  private static int izons[] = {
                          R.drawable.iz_disto2b, // iz_disto,
                          R.drawable.iz_plus,
                          R.drawable.iz_import,
                          R.drawable.iz_tools
                          // FIXME THMANAGER
                          // R.drawable.iz_therion,
                          // R.drawable.iz_database
                          };

  private static int menus[] = { 
                          // R.string.menu_palette,
                          R.string.menu_logs,
                          R.string.menu_join_survey,
                          R.string.menu_about,
                          R.string.menu_options,
                          R.string.menu_help
                          };

  private static int help_icons[] = { R.string.help_device,
                          R.string.help_add_topodroid,
                          R.string.help_import,
                          R.string.help_symbol
                          // FIXME THMANAGER
                          // R.string.help_therion,
                          // R.string.help_database
                          };
  private static int help_menus[] = {
                          // R.string.help_symbol,
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
    
  public void updateDisplay( )
  {
    DataHelper data = mApp.mData;
    if ( data != null ) {
      List<String> list = data.selectAllSurveys();
      updateList( list );
      if ( say_no_survey && list.size() == 0 ) {
        say_no_survey = false;
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
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
    // TDLog.Log( TDLog.LOG_INPUT, "TopoDroidActivity onClick() " + view.toString() );
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
          Toast.makeText( this, R.string.no_bt, Toast.LENGTH_SHORT ).show();
        } else {
          if ( mApp.mBTAdapter.isEnabled() ) {
            // TDLog.Debug( "start device window");
            startActivity( new Intent( Intent.ACTION_VIEW ).setClass( this, DeviceActivity.class ) );
          } else {
            Toast.makeText( this, R.string.not_enabled, Toast.LENGTH_SHORT ).show();
          }
        }
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // NEW SURVEY/CALIB
        mApp.setSurveyFromName( null, true ); // new-survey dialog: tell app to clear survey name and id
        (new SurveyNewDialog( this, this, -1, -1 )).show(); 
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // IMPORT
        (new ImportDialog( this, this, mApp )).show();
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // PALETTE
        DrawingBrushPaths.makePaths( getResources() );
        (new SymbolEnableDialog( this, this, mApp )).show();

      // FIXME THMANAGER
      // } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // THERION MANAGER ThManager
      //   try {
      //     intent = new Intent( "ThManager.intent.action.Launch" );
      //     // intent.putExtra( "survey", mApp.getSurveyThFile() );
      //     startActivity( intent );
      //   } catch ( ActivityNotFoundException e ) {
      //     Toast.makeText( this, R.string.no_thmanager, Toast.LENGTH_SHORT ).show();
      //   }
      // } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // DATABASE
      //   try {
      //     intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + TDPath.getDatabase() ) );
      //     intent.addCategory("com.kokufu.intent.category.APP_DB_VIEWER");
      //     startActivity( intent );
      //   } catch ( ActivityNotFoundException e ) {
      //     Toast.makeText( this, R.string.no_db_viewer, Toast.LENGTH_SHORT ).show();
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
    Intent surveyIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SurveyActivity.class );
    surveyIntent.putExtra( TopoDroidTag.TOPODROID_SURVEY, mustOpen );
    // surveyIntent.putExtra( TopoDroidTag.TOPODROID_OLDSID, old_sid );
    // surveyIntent.putExtra( TopoDroidTag.TOPODROID_OLDID,  old_id );
    startActivity( surveyIntent );
  }

  void startSplitSurvey( long old_sid, long old_id )
  {
    mApp.setSurveyFromName( null, false ); // FIXME JOIN-SURVEY
    (new SurveyNewDialog( this, this, old_sid, old_id )).show(); // WITH SPLIT
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
    Intent openIntent = new Intent( this, ShotActivity.class );
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
    Intent openIntent = new Intent( this, ShotActivity.class );
    startActivity( openIntent );
  }

  void setTheTitle()
  {
    // String title = getResources().getString( R.string.app_name );
    setTitle( mApp.getConnectionStateTitleStr() + mApp.mCWD );
    setTitleColor( TDConst.COLOR_NORMAL );
    // Log.v("DistoX", "TopoDroid activity set the title <" + mApp.getConnectionStateTitleStr() + title + ">" );
  }

  // ---------------------------------------------------------------
  // FILE IMPORT
  
  private class ImportTherionTask extends AsyncTask<String, Integer, Long >
  {
    @Override
    protected Long doInBackground( String... str )
    {
      long sid = 0;
      try {
        ParserTherion parser = new ParserTherion( str[0], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();
        ArrayList< ParserShot > splays = parser.getSplays();
        ArrayList< ParserTherion.Station > stations = parser.getStations();
        ArrayList< ParserTherion.Fix > fixes = parser.getFixes();

        sid = mApp.setSurveyFromName( str[1], false ); // IMPORT TH no forward
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );
        mApp.mData.updateSurveyInitStation( sid, parser.initStation(), false );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
        mApp.mData.insertShots( sid, id, splays );

        // FIXME this suppose CS long-lat, ie, e==long, n==lat
        // WorldMagneticModel wmm = new WorldMagneticModel( mApp );
        // for ( ParserTherion.Fix fix : fixes ) {
        //   // double asl = fix.z;
        //   double alt = wmm.geoidToEllipsoid( fix.n, fix.e, fix.z );
        //   mApp.mData.insertFixed( sid, -1L, fix.name, fix.e, fix.n, alt, fix.z, "", 0 );
        // }

        for ( ParserTherion.Station st : stations ) {
          mApp.mData.insertStation( sid, st.name, st.comment, st.flag );
        }
      } catch ( ParserException e ) {
        // Toast.makeText(this, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
      }
      return sid;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute(Long result) {
      setTheTitle( );
      updateDisplay( );
    }
  }
 
  private class ImportCompassTask extends AsyncTask<String, Integer, Long >
  {
    @Override
    protected Long doInBackground( String... str )
    {
      long sid = 0;
      try {
        ParserCompass parser = new ParserCompass( str[0], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();
        ArrayList< ParserShot > splays = parser.getSplays();

        sid = mApp.setSurveyFromName( parser.mName, false ); // IMPORT DAT no forward
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );
        mApp.mData.updateSurveyInitStation( sid, parser.initStation(), false );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
        mApp.mData.insertShots( sid, id, splays );
      } catch ( ParserException e ) {
        // Toast.makeText(this, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
      }
      return sid;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute(Long result) {
      setTheTitle( );
      updateDisplay( );
    }
  }

  private class ImportVisualTopoTask extends AsyncTask< String, Integer, Long >
  {
    @Override
    protected Long doInBackground( String... str )
    {
      long sid = 0;
      try {
        ParserVisualTopo parser = new ParserVisualTopo( str[0], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();
        ArrayList< ParserShot > splays = parser.getSplays();

        sid = mApp.setSurveyFromName( parser.mName, false );
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );
        mApp.mData.updateSurveyInitStation( sid, parser.initStation(), false );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
        mApp.mData.insertShots( sid, id, splays );
      } catch ( ParserException e ) {
        // Toast.makeText(this, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
      }
      return sid;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute(Long result) {
      setTheTitle( );
      updateDisplay( );
    }
  }

  private class ImportPocketTopoTask extends AsyncTask< String, Integer, Long >
  {
    @Override
    protected Long doInBackground( String... str )
    {
      long sid = 0;
      try {
        // import PocketTopo (only data for the first trip)
        ParserPocketTopo parser = new ParserPocketTopo( str[0], str[1], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();

        sid = mApp.setSurveyFromName( parser.mName, false );
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );
        mApp.mData.updateSurveyInitStation( sid, parser.initStation(), false );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
        TDLog.Log( TDLog.LOG_PTOPO, "SID " + sid + " inserted shots. return " + id );

        if ( parser.mStartFrom != null ) {
          mApp.insert2dPlot( sid, "1", parser.mStartFrom, true, 0 ); // true = plan-extended plot, 0 = proj_dir
        }

        // DistoXDBlock blk = mApp.mData.selectShot( 1, sid );
        // String plan = parser.mOutline;
        // String extended = parser.mSideview;
        // if ( blk != null /* && plan != null || extended != null */ ) {
        //   // insert plot in DB
        //   // long pid = 
        //     mApp.insert2dPlot( sid, "1", blk.mFrom );

        //   if ( plan == null ) plan = "";
        //   if ( extended == null ) extended = "";
        //   TDLog.Log( TDLog.LOG_PTOPO, "SID " + sid + " scraps " + plan.length() + " " + extended.length() );
        //   try {
        //     FIXME tdr vs. th2
        //     String filename1 = TDPath.getTh2File( parser.mName + "-1p.th2" );
        //     TDPath.checkPath( filename1 );
        //     FileWriter fw1 = new FileWriter( filename1 );
        //     PrintWriter pw1 = new PrintWriter( fw1 );
        //     pw1.format("%s", plan );
        //     
        //     String filename2 = TDPath.getTh2File( parser.mName + "-1s.th2" );
        //     TDPath.checkPath( filename2 );
        //     FileWriter fw2 = new FileWriter( filename2 );
        //     PrintWriter pw2 = new PrintWriter( fw2 );
        //     pw2.format("%s", extended );

        //   } catch ( IOException e ) {
        //     TDLog.Error( "SID " + sid + " scraps IO error " + e );
        //   }
        // }
      } catch ( ParserException e ) {
        // Toast.makeText(this, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
      }
      return sid;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute(Long result) {
      setTheTitle( );
      updateDisplay( );
    }
  }

   
  
  private class ImportZipTask extends AsyncTask< String, Integer, Long >
  {
    TopoDroidActivity activity;

    ImportZipTask( TopoDroidActivity act ) { activity = act; }

    @Override
    protected Long doInBackground( String... str )
    {
      String filename = str[0];
      Archiver archiver = new Archiver( mApp );
      int ret = archiver.unArchive( TDPath.getZipFile( filename ), filename.replace(".zip", ""));
      return (long)ret;
    }

    // @Override
    // protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute(Long result) {
      activity.setTheTitle( );
      activity.updateDisplay( );
      if ( result < -4 ) {
        Toast.makeText( activity, R.string.unzip_fail, Toast.LENGTH_SHORT).show();
      } else if ( result == -4 ) {
        Toast.makeText( activity, R.string.unzip_fail_survey, Toast.LENGTH_SHORT).show();
      } else if ( result == -3 ) {
        Toast.makeText( activity, R.string.unzip_fail_db, Toast.LENGTH_SHORT).show();
      } else if ( result == -2 ) {
        Toast.makeText( activity, R.string.unzip_fail_td, Toast.LENGTH_SHORT).show();
      } else if ( result == -1 ) {
        Toast.makeText( activity, R.string.import_already, Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText( activity, R.string.import_zip_ok, Toast.LENGTH_SHORT).show();
      }
    }
  }

  void importFile( String filename )
  {
    // FIXME connect-title string
    setTitle( R.string.import_title );
    setTitleColor( TDConst.COLOR_CONNECTED );
    if ( filename.endsWith(".th") ) {
      String filepath = TDPath.getImportFile( filename );
      String name = filename.replace(".th", "" );
      if ( mApp.mData.hasSurveyName( name ) ) {
        Toast.makeText(this, R.string.import_already, Toast.LENGTH_SHORT).show();
        return;
      }
      // Toast.makeText(this, R.string.import_wait, Toast.LENGTH_SHORT).show();
      new ImportTherionTask().execute( filepath, name );
    } else if ( filename.endsWith(".dat") ) {
      String filepath = TDPath.getImportFile( filename );
      new ImportCompassTask().execute( filepath );
    } else if ( filename.endsWith(".top") ) {
      String filepath = TDPath.getImportFile( filename );
      new ImportPocketTopoTask().execute( filepath, filename ); // TODO pass the drawer as arg
    } else if ( filename.endsWith(".tro") ) {
      String filepath = TDPath.getImportFile( filename );
      new ImportVisualTopoTask().execute( filepath ); 
    } else if ( filename.endsWith(".zip") ) {
      Toast.makeText(this, R.string.import_zip_wait, Toast.LENGTH_LONG).show();
      new ImportZipTask( this ) .execute( filename );
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
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );

    // mMenuAdapter.add( res.getString( menus[0] ) );
    if ( TDSetting.mLevelOverAdvanced ) mMenuAdapter.add( res.getString( menus[0] ) );
    if ( TDSetting.mLevelOverAdvanced && mApp.mCosurvey ) mMenuAdapter.add( res.getString( menus[1] ) );
    mMenuAdapter.add( res.getString( menus[2] ) );
    mMenuAdapter.add( res.getString( menus[3] ) );
    mMenuAdapter.add( res.getString( menus[4] ) );
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
    // Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show();
    int p = 0;
    // if ( p++ == pos ) { // PALETTE
    //   DrawingBrushPaths.makePaths( getResources() );
    //   (new SymbolEnableDialog( this, this, mApp )).show();
    // } else { 
      Intent intent;
      if ( TDSetting.mLevelOverAdvanced && p++ == pos ) { // LOGS
        intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_LOG );
        startActivity( intent );
      } else {  
        if ( TDSetting.mLevelOverAdvanced && mApp.mCosurvey && p++ == pos ) {  // CO-SURVEY
          (new ConnectDialog( this, mApp )).show();
        } else { 
          if ( p++ == pos ) { // ABOUT
            (new TopoDroidAbout( this )).show();
          } else { 
            if ( p++ == pos ) { // SETTINGS
              intent = new Intent( this, TopoDroidPreferences.class );
              intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
              startActivity( intent );
            } else { 
              if ( p++ == pos ) { // HELP
                (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, menus.length ) ).show();
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
    mApp.mActivity = this;

    // mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
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

    if ( mApp.mWelcomeScreen ) {
      mApp.setBooleanPreference( "DISTOX_WELCOME_SCREEN", false );
      mApp.mWelcomeScreen = false;
      mTopoDroidAbout = new TopoDroidAbout( this );
      mTopoDroidAbout.setOnCancelListener( this );
      mTopoDroidAbout.setOnDismissListener( this );
      mTopoDroidAbout.show();
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
    //     DrawingBrushPaths.doMakePaths( );
    //     WorldMagneticModel.loadEGM9615( mApp );
    //     // int n_terms = MagUtil.CALCULATE_NUMTERMS( 12 );
    //     // WorldMagneticModel.loadWMM( mApp, n_terms );
    //     return null;
    //   }
    // };
    // TDLog.Profile("TDActivity thread");
    Thread loader = new Thread() {
      @Override
      public void run() {
        mApp.startupStep2();
        Resources res = getResources();
        DrawingBrushPaths.reloadPointLibrary( res ); // reload symbols
        DrawingBrushPaths.reloadLineLibrary( res );
        DrawingBrushPaths.reloadAreaLibrary( res );
        DrawingBrushPaths.doMakePaths( );
        WorldMagneticModel.loadEGM9615( mApp );
      }
    };
    loader.setPriority( Thread.MIN_PRIORITY );
    loader.start();

    setTheTitle();
  }
  
  int mNrButton1 = 4;

  void resetButtonBar()
  {
    int size = mApp.setListViewHeight( mListView );
    MyButton.resetCache( /* mApp, */ size );

    // FIXME THMANAGER
    // mNrButton1 = 3 + ( TDSetting.mLevelOverAdvanced ? 2 : 0 );
    mButton1 = new Button[mNrButton1];

    mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, getResources(), R.drawable.iz_menu ) );
    for (int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( this, this, izons[k] );
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
          startActivityForResult( enableIntent, REQUEST_ENABLE_BT );
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
    if ( TopoDroidApp.isTracing ) {
      Debug.stopMethodTracing();
    }
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
      case REQUEST_ENABLE_BT:
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
    Intent intent = new Intent( this, TopoDroidPreferences.class );
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
        String help_page = getResources().getString( R.string.TopoDroidActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

}
