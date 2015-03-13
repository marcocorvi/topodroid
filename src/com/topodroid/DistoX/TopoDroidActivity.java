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
 * CHANGES
 * 20120520 created from DistoX.java
 * 20120606 import survey (therion format)
 * 20120610 import zip (unarchive)
 * 20120619 added "long-press" for immediate survey opening
 * 20121211 thconfig-manager and symbol-manager menus
 * 20121212 AsyncTask to import therion files
 * 20130307 made Annotations into a dialog
 * 20130910 startSurvey takes old sid/id to populate new survey
 * 20131201 button bar new interface. reorganized actions
 * 20140415 commented TdSymbol stuff
 * 20140416 menus: palette options help logs about
 * 20140526 removed oldSID and oldID from startSurvey 
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.StringWriter;
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
// import android.os.Handler;
// import android.os.Message;
// import android.os.Parcelable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.net.Uri;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.preference.PreferenceManager;

// import android.view.Menu;
// import android.view.MenuItem;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

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

  // private static final int REQUEST_DEVICE    = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  // statuses
  // private static final int STATUS_NONE   = 0;
  // private static final int STATUS_SURVEY = 1;
  // private static final int STATUS_CALIB  = 2;

  // private int mStatus    = STATUS_SURVEY;
  // private int mOldStatus = STATUS_SURVEY;

  private LinearLayout mLayout;
  private ListView mList;

  // private Button   mBtnSurveys = null;
  // private Button   mBtnCalibs  = null;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;

  private Button[] mButton1;
  // private static int icons[] = {
  //                         R.drawable.ic_disto,
  //                         R.drawable.ic_add,
  //                         R.drawable.ic_import,
  //                         R.drawable.ic_therion,
  //                         R.drawable.ic_database
  //                         };
  // private static int ixons[] = {
  //                         R.drawable.ix_disto,
  //                         R.drawable.ix_add,
  //                         R.drawable.ix_import,
  //                         R.drawable.ix_therion,
  //                         R.drawable.ix_database
  //                         };
  private static int izons[] = {
                          R.drawable.iz_disto,
                          R.drawable.iz_plus,
                          R.drawable.iz_import,
                          R.drawable.iz_therion,
                          R.drawable.iz_database
                          };
  // private int icons00[];

  private static int menus[] = { 
                          R.string.menu_palette,
                          R.string.menu_options,
                          R.string.menu_logs,
                          R.string.menu_join_survey,
                          R.string.menu_about,
                          R.string.menu_help
                          };

  private static int help_icons[] = { R.string.help_device,
                          R.string.help_add_topodroid,
                          R.string.help_import,
                          R.string.help_therion,
                          R.string.help_database
                          };
  private static int help_menus[] = {
                          R.string.help_symbol,   
                          R.string.help_prefs,
                          R.string.help_log,
                          R.string.help_join_survey,
                          R.string.help_info_topodroid,
                          R.string.help_help
                        };

  // -------------------------------------------------------------
  private boolean say_no_survey = true;
  private boolean say_no_calib  = true;
  private boolean say_not_enabled = true; // whether to say that BT is not enabled
  boolean do_check_bt = true;             // one-time bluetooth check sentinel

  // -------------------------------------------------------------------

  @Override
  public void onBackPressed () // askClose
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }
    new TopoDroidAlertDialog( this, getResources(),
                              getResources().getString( R.string.ask_close ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          finish(); // doClose()
        }
      }
    );
  }


    
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
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "TopoDroidActivity onClick() " + view.toString() );
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
        if ( mApp.mBTAdapter == null ) {
          Toast.makeText( this, R.string.no_bt, Toast.LENGTH_SHORT ).show();
        } else {
          if ( mApp.mBTAdapter.isEnabled() ) {
             intent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
            startActivity( intent );
          } else {
            Toast.makeText( this, R.string.not_enabled, Toast.LENGTH_SHORT ).show();
          }
        }
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // NEW SURVEY/CALIB
        mApp.setSurveyFromName( null, true ); // new-survey dialog: tell app to clear survey name and id
        (new SurveyNewDialog( this, this, -1, -1 )).show(); 
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // IMPORT
        (new ImportDialog( this, this, mApp )).show();

      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // THERION MANAGER ThManager
        try {
          intent = new Intent( "ThManager.intent.action.Launch" );
          // intent.putExtra( "survey", mApp.getSurveyThFile() );
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( this, R.string.no_thmanager, Toast.LENGTH_SHORT ).show();
        }
      } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // DATABASE
        try {
          intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + TopoDroidPath.getDatabase() ) );
          intent.addCategory("com.kokufu.intent.category.APP_DB_VIEWER");
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( this, "DB_viewer mApp not found", Toast.LENGTH_SHORT ).show();
        }
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
    Intent surveyIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, SurveyActivity.class );
    surveyIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, mustOpen );
    // surveyIntent.putExtra( TopoDroidApp.TOPODROID_OLDSID, old_sid );
    // surveyIntent.putExtra( TopoDroidApp.TOPODROID_OLDID,  old_id );
    startActivity( surveyIntent );
  }

  void startSplitSurvey( long old_sid, long old_id )
  {
    mApp.setSurveyFromName( null, false ); // FIXME JOIN-SURVEY
    (new SurveyNewDialog( this, this, old_sid, old_id )).show(); // WITH SPLIT
  }

  private void startCalib( String value, int mustOpen )
  {
    mApp.setCalibFromName( value );
    Intent calibIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, CalibActivity.class );
    calibIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, mustOpen ); // FIXME not handled yet
    startActivity( calibIntent );
  }

  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( onMenu ) {
      closeMenu();
      return true;
    }
    CharSequence item = ((TextView) view).getText();
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "TopoDroidActivity onItemLongClick() " + item.toString() );
    // switch ( mStatus ) {
    //   case STATUS_SURVEY:
    //     startSurvey( item.toString(), 0 ); // , -1, -1 );
    //     return true;
    //   case STATUS_CALIB:
    //     startCalib( item.toString(), 0 );
    //     return true;
    // }
    // return false;
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
      closeMenu();
      // Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show();
      int p = 0;
      if ( p++ == pos ) { // PALETTE
        DrawingBrushPaths.makePaths( getResources() );
        (new SymbolEnableDialog( this, this )).show();
      } else { 
        Intent intent;
        if ( p++ == pos ) { // SETTINGS
          intent = new Intent( this, TopoDroidPreferences.class );
          intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
          startActivity( intent );
        } else { 
          if ( TopoDroidSetting.mLevelOverAdvanced && p++ == pos ) { // LOGS
            intent = new Intent( this, TopoDroidPreferences.class );
            intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_LOG );
            startActivity( intent );
          } else {  
            if ( TopoDroidSetting.mLevelOverAdvanced && mApp.mCosurvey && p++ == pos ) {  // CO-SURVEY
              (new ConnectDialog( this, mApp )).show();
            } else { 
              if ( p++ == pos ) { // ABOUT
                (new TopoDroidAbout( this )).show();
              } else { 
                if ( p++ == pos ) { // HELP
                  (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 6 ) ).show();
                }
              }
            }
          }
        }
      }
      // updateDisplay();
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }

    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "TopoDroidActivity onItemClick() " + item.toString() );
    // switch ( mStatus ) {
    //   case STATUS_SURVEY:
    //     // startSurvey( item.toString(), 1); // , -1, -1 ); // start survey and open it
    //     mApp.setSurveyFromName( item.toString(), true ); 
    //     Intent openIntent = new Intent( this, ShotActivity.class );
    //     startActivity( openIntent );
    //     break;
    //   case STATUS_CALIB:
    //     startCalib( item.toString(), 1 );
    //     break;
    // }
    mApp.setSurveyFromName( item.toString(), true ); 
    Intent openIntent = new Intent( this, ShotActivity.class );
    startActivity( openIntent );
  }

  // void handleSurveyActivity( int result )
  // {
  // }

  void setTheTitle()
  {
    String title = getResources().getString( R.string.app_name );
    setTitle( mApp.getConnectionStateTitleStr() + title );
    setTitleColor( TopoDroidConst.COLOR_NORMAL );
    Log.v("DistoX", "TopoDroid activity set the title <" + mApp.getConnectionStateTitleStr() + title + ">" );
  }

  // ---------------------------------------------------------------
  // FILE IMPORT
  
  private class ImportTherionTask extends AsyncTask<String , Integer, Long >
  {
    @Override
    protected Long doInBackground( String... str )
    {
      long sid = 0;
      try {
        TherionParser parser = new TherionParser( str[0], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();
        ArrayList< ParserShot > splays = parser.getSplays();

        sid = mApp.setSurveyFromName( str[1], false ); // IMPORT TH no forward
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
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
 
  private class ImportCompassTask extends AsyncTask<String , Integer, Long >
  {
    @Override
    protected Long doInBackground( String... str )
    {
      long sid = 0;
      try {
        CompassParser parser = new CompassParser( str[0], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();
        ArrayList< ParserShot > splays = parser.getSplays();

        sid = mApp.setSurveyFromName( parser.mName, false ); // IMPORT DAT no forward
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
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
        VisualTopoParser parser = new VisualTopoParser( str[0], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();

        sid = mApp.setSurveyFromName( parser.mName, false );
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
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
        PocketTopoParser parser = new PocketTopoParser( str[0], str[1], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();

        sid = mApp.setSurveyFromName( parser.mName, false );
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
        TopoDroidLog.Log( TopoDroidLog.LOG_PTOPO, "SID " + sid + " inserted shots. return " + id );

        if ( parser.mStartFrom != null ) {
          mApp.insert2dPlot( sid, "1", parser.mStartFrom );
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
        //   TopoDroidLog.Log( TopoDroidLog.LOG_PTOPO, "SID " + sid + " scraps " + plan.length() + " " + extended.length() );
        //   try {
        //     String filename1 = TopoDroidPath.getTh2File( parser.mName + "-1p.th2" );
        //     TopoDroidApp.checkPath( filename1 );
        //     FileWriter fw1 = new FileWriter( filename1 );
        //     PrintWriter pw1 = new PrintWriter( fw1 );
        //     pw1.format("%s", plan );
        //     
        //     String filename2 = TopoDroidPath.getTh2File( parser.mName + "-1s.th2" );
        //     TopoDroidApp.checkPath( filename2 );
        //     FileWriter fw2 = new FileWriter( filename2 );
        //     PrintWriter pw2 = new PrintWriter( fw2 );
        //     pw2.format("%s", extended );

        //   } catch ( IOException e ) {
        //     TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "SID " + sid + " scraps IO error " + e );
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

   
  
  private class ImportZipTask extends AsyncTask<String , Integer, Long >
  {
    TopoDroidActivity activity;

    ImportZipTask( TopoDroidActivity act ) { activity = act; }

    @Override
    protected Long doInBackground( String... str )
    {
      String filename = str[0];
      Archiver archiver = new Archiver( mApp );
      int ret = archiver.unArchive( TopoDroidPath.getZipFile( filename ), filename.replace(".zip", ""));
      return (long)ret;
    }

    // @Override
    // protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute(Long result) {
      activity.setTheTitle( );
      activity.updateDisplay( );
      if ( result == -2 ) {
        Toast.makeText( activity, R.string.unzip_fail, Toast.LENGTH_SHORT).show();
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
    setTitleColor( TopoDroidConst.COLOR_CONNECTED );
    if ( filename.endsWith(".th") ) {
      String filepath = TopoDroidPath.getImportFile( filename );
      String name = filename.replace(".th", "" );
      if ( mApp.mData.hasSurveyName( name ) ) {
        Toast.makeText(this, R.string.import_already, Toast.LENGTH_SHORT).show();
        return;
      }
      // Toast.makeText(this, R.string.import_wait, Toast.LENGTH_SHORT).show();
      new ImportTherionTask().execute( filepath, name );
    } else if ( filename.endsWith(".dat") ) {
      String filepath = TopoDroidPath.getImportFile( filename );
      new ImportCompassTask().execute( filepath );
    } else if ( filename.endsWith(".top") ) {
      String filepath = TopoDroidPath.getImportFile( filename );
      new ImportPocketTopoTask().execute( filepath, filename ); // TODO pass the drawer as arg
    } else if ( filename.endsWith(".tro") ) {
      String filepath = TopoDroidPath.getImportFile( filename );
      new ImportVisualTopoTask().execute( filepath ); 
    } else if ( filename.endsWith(".zip") ) {
      Toast.makeText(this, R.string.import_zip_wait, Toast.LENGTH_LONG).show();
      new ImportZipTask( this ) .execute( filename );
    }
    // FIXME SYNC updateDisplay();
  }
  
  // ---------------------------------------------------------------
  // private Button mButtonHelp;

  TopoDroidAbout mTopoDroidAbout = null;
  // TdSymbolDialog mTdSymbolDialog = null;
 
  HorizontalListView mListView;
  // HorizontalImageButtonView mButtonView1;
  HorizontalButtonView mButtonView1;
  Button     mMenuImage;
  ListView   mMenu;
  ArrayAdapter< String > mMenuAdapter;
  

  void setMenuAdapter( Resources res )
  {
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    // FIXME TextView tv = ((TextVie)mMenuAdaptergetView()).setTextSize( 24 );

    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    if ( TopoDroidSetting.mLevelOverAdvanced ) mMenuAdapter.add( res.getString( menus[2] ) );
    if ( TopoDroidSetting.mLevelOverAdvanced && mApp.mCosurvey ) mMenuAdapter.add( res.getString( menus[3] ) );
    mMenuAdapter.add( res.getString( menus[4] ) );
    mMenuAdapter.add( res.getString( menus[5] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    
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

    mMenuImage = (Button) findViewById( R.id.handle );
    mMenuImage.setOnClickListener( this );
    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter( getResources() );
    closeMenu();
    mMenu.setOnItemClickListener( this );

    // mBtnSurveys = (Button) findViewById( R.id.btn_surveys );
    // mBtnCalibs  = (Button) findViewById( R.id.btn_calibs );

    // if ( mApp.VERSION30 ) {
    //   mBtnSurveys.setVisibility( View.GONE );
    //   mBtnCalibs.setVisibility( View.GONE );
    // } else {
    //   mBtnSurveys.setOnClickListener( this );
    //   mBtnCalibs.setOnClickListener( this );
    // }

    // mButtonHelp = (Button)findViewById( R.id.help );
    // mButtonHelp.setOnClickListener( this );
    // if ( TopoDroidApp.mHideHelp ) {
    //   mButtonHelp.setVisibility( View.GONE );
    // } else {
    //   mButtonHelp.setVisibility( View.VISIBLE );
    // }

    mListView = (HorizontalListView) findViewById(R.id.listview);
    resetButtonBar();

    // if ( savedInstanceState == null) {
    //   TopoDroidLog.Log(TopoDroidLog.LOG_MAIN, "onCreate null savedInstanceState" );
    // } else {
    //   Bundle map = savedInstanceState.getBundle(DISTOX_KEY);
    //   restoreInstanceState( map );
    // }
    // restoreInstanceFromFile();
    // restoreInstanceFromData();
    if ( mApp.mWelcomeScreen ) {
      mApp.setBooleanPreference( "DISTOX_WELCOME_SCREEN", false );
      mApp.mWelcomeScreen = false;
      mTopoDroidAbout = new TopoDroidAbout( this );
      mTopoDroidAbout.setOnCancelListener( this );
      mTopoDroidAbout.setOnDismissListener( this );
      mTopoDroidAbout.show();
    // } else if ( mApp.mTdSymbol ) {
    //   startTdSymbolDialog();
    }
    if ( mApp.askSymbolUpdate ) {
      (new TopoDroidVersionDialog(this, mApp)).show();
    }

    DrawingBrushPaths.doMakePaths( );

    // setTitleColor( 0x006d6df6 );

  }
  
  int mNrButton1 = 3;

  void resetButtonBar()
  {
    int size = mApp.setListViewHeight( mListView );

    // icons00 = ( TopoDroidSetting.mSizeButtons == 2 )? ixons : icons;
    mNrButton1 = 3 + ( TopoDroidSetting.mLevelOverAdvanced ? 2 : 0 );
    mButton1 = new Button[mNrButton1];

    // mMenuImage.setBackgroundResource( ( TopoDroidSetting.mSizeButtons == 2 )? R.drawable.ix_menu : R.drawable.ic_menu );
    mApp.setButtonBackground( mMenuImage, size, R.drawable.iz_menu );

    for (int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      // mButton1[k].setBackgroundResource(  icons00[k] );
      // // mButton1[k].setImageResource(  icons00[k] );
      mApp.setButtonBackground( mButton1[k], size, izons[k] );
    }

    // mButtonView1 = new HorizontalImageButtonView( mButton1 );
    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );
  }

  // public void rescaleButtons()
  // {
  //   Toast.makeText( this, "You must close TopoDroid\nto make change effective", Toast.LENGTH_LONG ).show();
  //   // int size = mApp.setListViewHeight( mListView );
  //   // // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "scale " + TopoDroidApp.mSizeButtons );
  //   // icons00 = ( TopoDroidApp.mSizeButtons == 2 )? ixons : icons;

  //   // for (int k=0; k<mNrButton1; ++k ) {
  //   //   mButton1[k].setBackgroundResource( icons00[k] );
  //   //   mButton1[k].invalidate();
  //   // }
  //   // mListView.invalidate();
  //   // mLayout.invalidate();
  // }

  @Override
  public void onDismiss( DialogInterface d )
  { 
    // if ( d == (Dialog)mTdSymbolDialog ) {
    //   if ( mApp.mStartTdSymbol ) {
    //     Intent intent = new Intent( "TdSymbol.intent.action.Launch" );
    //     try {
    //       startActivity( intent );
    //       DrawingBrushPaths.mReloadSymbols = true;
    //     } catch ( ActivityNotFoundException e ) {
    //       Toast.makeText( this, R.string.no_tdsymbol, Toast.LENGTH_SHORT ).show();
    //     }
    //   }
    //   mTdSymbolDialog = null;
    // } else
    if ( d == (Dialog)mTopoDroidAbout ) {
      // startTdSymbolDialog();
      mTopoDroidAbout = null;
    }
  }

  @Override
  public void onCancel( DialogInterface d )
  {
    if ( d == (Dialog)mTopoDroidAbout ) {
      // startTdSymbolDialog();
      mTopoDroidAbout = null;
    }
  }

  // private void startTdSymbolDialog()
  // {
  //   mTdSymbolDialog = new TdSymbolDialog( this, mApp );
  //   // mTdSymbolDialog.setOnCancelListener( this );
  //   mTdSymbolDialog.setOnDismissListener( this );
  //   mTdSymbolDialog.show();
  // }

  // private void restoreInstanceState(Bundle map )
  // {
  //   if ( map != null ) {
  //     TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "onRestoreInstanceState non-null bundle");
  //     mStatus        = map.getInt( DISTOX_KEY_STATUS );
  //     mOldStatus     = map.getInt( DISTOX_KEY_OLD_STATUS );
  //     mSplay         = map.getBoolean( DISTOX_KEY_SPLAY );
  //     mLeg           = map.getBoolean( DISTOX_KEY_CENTERLINE );
  //     mBlank         = map.getBoolean( DISTOX_KEY_BLANK );
  //     String survey  = map.getString( DISTOX_KEY_SURVEY );
  //     String calib   = map.getString( DISTOX_KEY_CALIB );
  //     if ( survey != null ) setSurveyFromName( survey, false );
  //     if ( calib  != null ) setCalibFromName( calib );
  //   } else {
  //     TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "onRestoreInstanceState null bundle");
  //     // mStatus ??
  //   }
  // }

  // private void restoreInstanceFromData()
  // { 
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "restoreInstanceFromData ");
  //   DataHelper data = mApp.mData;
  //   // String status = data.getValue( "DISTOX_STATUS" );
  //   // // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "restore STATUS " + status );
  //   // if ( status != null ) {
  //   //   String[] vals = status.split( " " );
  //   //   // FIXME
  //   // }
  //   String survey = data.getValue( "DISTOX_SURVEY" );
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "restore SURVEY >" + survey + "<" );
  //   if ( survey != null && survey.length() > 0 ) {
  //     mApp.setSurveyFromName( survey, false );
  //   } else {
  //     mApp.setSurveyFromName( null, false );
  //   }
  //   String calib = data.getValue( "DISTOX_CALIB" );
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "restore CALIB >" + calib + "<" );
  //   if ( calib != null && calib.length() > 0 ) {
  //     mApp.setCalibFromName( calib );
  //   } else {
  //     mApp.setCalibFromName( null );
  //   }
  // }
    
  // private void saveInstanceToData()
  // {
  //   // TopoDroidLog.Log(TopoDroidLog.LOG_MAIN, "saveInstanceToData");
  //   DataHelper data = mApp.mData;
  //   StringWriter sw = new StringWriter();
  //   PrintWriter pw = new PrintWriter( sw );
  //   pw.format("%d ", 1 ); // mStatus );
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "save STATUS " + sw.getBuffer().toString() );
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "save SURVEY >" + mApp.mySurvey + "<" );
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "save CALIB >" + mApp.getCalib() + "<" );
  //   data.setValue( "DISTOX_STATUS", sw.getBuffer().toString() );
  //   // data.setValue( "DISTOX_SURVEY", (mApp.mySurvey == null)? "" : mApp.mySurvey );
  //   data.setValue( "DISTOX_CALIB", (mApp.myCalib == null)? "" : mApp.myCalib );
  // }


  // @Override
  // public void onSaveInstanceState(Bundle outState) 
  // {
  //   TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "onSaveInstanceState");
  //   // outState.putBundle(DISTOX_KEY, mList.saveState());
  //   outState.putInt(DISTOX_KEY_STATUS, mStatus );
  //   outState.putInt(DISTOX_KEY_OLD_STATUS, mOldStatus );
  //   outState.putBoolean(DISTOX_KEY_SPLAY, mSplay );
  //   outState.putBoolean(DISTOX_KEY_CENTERLINE, mLeg );
  //   outState.putBoolean(DISTOX_KEY_BLANK, mBlank );
  //   outState.putString(DISTOX_KEY_SURVEY, mySurvey );
  //   outState.putString(DISTOX_KEY_CALIB, myCalib );
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
    // TopoDroidLog.Log( TopoDroidLoLogOG_MAIN, "onStart check BT " + mApp.mCheckBT + " enabled " + mApp.mBTAdapter.isEnabled() );

    if ( do_check_bt ) {
      do_check_bt = false;
      if ( mApp.mBTAdapter == null ) {
        Toast.makeText( this, R.string.no_bt, Toast.LENGTH_SHORT ).show();
      } else {
        if ( TopoDroidSetting.mCheckBT == 1 && ! mApp.mBTAdapter.isEnabled() ) {    
          Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
          startActivityForResult( enableIntent, REQUEST_ENABLE_BT );
        } else {
          // nothing to do: scanBTDEvices(); is called by menu CONNECT
        }
        // FIXME_BT
        // setBTMenus( mApp.mBTAdapter.isEnabled() );
      }
    }
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "onResume " );
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
    // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "onPause " );
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

  // @Override
  // public synchronized void onDestroy() 
  // {
  //   super.onDestroy();
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "onDestroy " );
  //   // FIXME if ( mApp.mComm != null ) { mApp.mComm.interrupt(); }
  //   saveInstanceToData();
  // }

  // ------------------------------------------------------------------


  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "onActivityResult() request " + mRequestName[request] + " result: " + result );
    DataHelper data = mApp.mData;
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case REQUEST_ENABLE_BT:
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
    // TopoDroidLog.Log( TopoDroidLog.LOG_MAIN, "onActivityResult() done " );
  }

  // ---------------------------------------------------------------
  /* OPTIONS MENU

  private MenuItem mMIsymbol;
  private MenuItem mMIoptions;
  private MenuItem mMIlogs;
  // private MenuItem mMImanual;
  private MenuItem mMIjoin;   // join-survey
  private MenuItem mMIhelp;
  private MenuItem mMIabout;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIsymbol  = menu.add( R.string.menu_palette );
    mMIsymbol.setIcon(  icons[5] );

    mMIoptions = menu.add( R.string.menu_options );
    mMIoptions.setIcon( icons[6] );

    if ( TopoDroidApp.mLevelOverAdvanced ) {
      mMIlogs = menu.add( R.string.menu_logs );
      mMIlogs.setIcon(    icons[7] );
    }

    // mMImanual  = menu.add( R.string.menu_manual  );
    // mMImanual.setIcon(  icons[8] );

    if ( TopoDroidApp.mLevelOverAdvanced ) {
      mMIjoin  = menu.add( R.string.menu_join_survey );
      mMIjoin.setIcon(  icons[8] );
    }

    mMIabout   = menu.add( R.string.menu_about );
    mMIabout.setIcon(   icons[9] );

    mMIhelp    = menu.add( R.string.menu_help  );
    mMIhelp.setIcon(    icons[10] );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    Intent intent;
    if ( item == mMIsymbol ) { 
      DrawingBrushPaths.makePaths( getResources() );
      (new SymbolEnableDialog( this, this )).show();
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
      startActivity( intent );
    } else if ( item == mMIlogs ) { // LOG OPTIONS DIALOG
      intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_LOG );
      startActivity( intent );
    // } else if ( item == mMImanual ) { // USER MANUAL DIALOG
    //   startActivity( new Intent( Intent.ACTION_VIEW ).setClass( this, DistoXManualDialog.class ) );
    } else if ( item == mMIjoin ) { // JOIN-SURVEY DIALOG
      (new ConnectDialog( this, mApp )).show();
    } else if ( item == mMIabout ) { // ABOUT DIALOG
      (new TopoDroidAbout( this )).show();
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 6 ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  */
}
