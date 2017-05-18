/* @file SurveyWindow.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Locale;
import java.util.List;
import java.util.Calendar;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.DatePickerDialog;
// import android.app.Dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.ActivityNotFoundException;
import android.content.res.Resources;

import android.location.LocationManager;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.Toast;

import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
// for FRAGMENT
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;

public class SurveyWindow extends Activity
                            implements IExporter
                            , OnItemClickListener
                            , View.OnClickListener
{
  private static int izons[] = { 
                        R.drawable.iz_note,
                        R.drawable.iz_info, // ic_details,
                        R.drawable.iz_3d,
                        R.drawable.iz_gps,
                        R.drawable.iz_camera,
                        R.drawable.iz_sensor
                     };
  private static int menus[] = {
                        R.string.menu_close,
                        R.string.menu_export,
                        R.string.menu_rename,
                        R.string.menu_delete,
                        R.string.menu_manual_calibration,
                        R.string.menu_calib_check,
                        R.string.menu_options,
                        R.string.menu_help
                      };

  private static int help_icons[] = { 
                        R.string.help_note,
                        R.string.help_info_shot,
                        R.string.help_3d,
                        R.string.help_loc,
                        R.string.help_photo,
                        R.string.help_sensor
                        };
  private static int help_menus[] = { 
                        R.string.help_close,
                        R.string.help_export_survey,
                        R.string.help_rename,
                        R.string.help_delete_survey,
                        R.string.help_manual_calibration,
                        R.string.help_calib_check,
                        R.string.help_prefs,
                        R.string.help_help
                      };
  // private static int icons00[];

  // private ShotWindow mParent;
  private Context mContext;
  private Activity mActivity = null;

  private EditText mTextName;
  private Button mEditDate;
  private EditText mEditTeam;
  private EditText mEditDecl;
  private EditText mEditComment;

  MyDateSetListener mDateListener;

  private Button[] mButton1;
  private int mNrButton1 = 0;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  ListView   mMenu;
  Button     mImage;
  // HOVER
  // MyMenuAdapter mMenuAdapter;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;
  String mInitStation = null;

  TopoDroidApp mApp;
  private boolean mustOpen;

  String getSurveyName() { return mApp.mySurvey; }

  void renameSurvey( String name ) 
  {
    name = TopoDroidUtil.noSpaces( name );
    if ( mApp.renameCurrentSurvey( mApp.mSID, name, true ) ) {
      mTextName.setText( name );
    } else {
      Toast.makeText( mActivity, R.string.cannot_rename, Toast.LENGTH_SHORT).show();
    }
  } 
    

// -------------------------------------------------------------------
  
  boolean updateDisplay()
  {
    // TDLog.Log( TDLog.LOG_SURVEY, "app mSID " + mApp.mSID );
    if ( mApp.mSID < 0 ) return false;
    SurveyInfo info = mApp.getSurveyInfo();
    mTextName.setText( info.name );
    mInitStation = info.initStation;

    mEditDate.setText( info.date );
    if ( info.comment != null && info.comment.length() > 0 ) {
      mEditComment.setText( info.comment );
    } else {
      mEditComment.setHint( R.string.description );
    }
    if ( info.team != null && info.team.length() > 0 ) {
      mEditTeam.setText( info.team );
    } else {
      mEditTeam.setHint( R.string.team );
    }
    setDeclination( info.declination );
    return true;
  }



  @Override
  protected void onCreate( Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );

    mApp = (TopoDroidApp)getApplication();
    mApp.mSurveyWindow = this;
	mActivity = this;

    mContext = this;
    mustOpen = false;
    // oldSid = -1L;
    // oldId  = -1L;
    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      if ( extras.getInt( TDTag.TOPODROID_SURVEY ) == 1 ) mustOpen = true;
      // oldSid = extras.getLong( TDTag.TOPODROID_OLDSID );
      // oldId  = extras.getLong( TDTag.TOPODROID_OLDID );
    }

    setContentView(R.layout.survey_activity);
    setTitle( R.string.title_survey );
    mTextName    = (EditText) findViewById(R.id.survey_name);
    mEditDate    = (Button) findViewById(R.id.survey_date);
    mEditTeam    = (EditText) findViewById(R.id.survey_team);
    mEditDecl    = (EditText) findViewById(R.id.survey_decl);
    mEditComment = (EditText) findViewById(R.id.survey_comment);

    mDateListener = new MyDateSetListener( mEditDate );
    mEditDate.setOnClickListener( this );

    if ( ! updateDisplay() ) {
      TDLog.Error( "opening non-existent survey" );
      setResult( RESULT_CANCELED );
      finish();
    }

    // mFixed = new ArrayList< FixedInfo >();
    // mPhoto = new ArrayList< PhotoInfo >();

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );

    Resources res = getResources();
    mNrButton1 = TDSetting.mLevelOverNormal ? 6 
               : TDSetting.mLevelOverBasic ? 3 : 2;
    mButton1 = new Button[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
    }

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );
    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter( res );
    closeMenu();
    // HOVER
    mMenu.setOnItemClickListener( this );
  }

  void setTheTitle()
  {
    setTitle( mApp.getConnectionStateTitleStr() +
              getResources().getString( R.string.title_survey ) );
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    mApp.resetLocale();
    float decl = mApp.mData.getSurveyDeclination( mApp.mSID );
    mEditDecl.setText( String.format(Locale.US, "%.4f", decl ) );
  }

  // ------------------------------------------
   
  @Override
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
    } else if ( b == mEditDate ) {
      String date = mEditDate.getText().toString();
      int y = TopoDroidUtil.dateParseYear( date );
      int m = TopoDroidUtil.dateParseMonth( date );
      int d = TopoDroidUtil.dateParseDay( date );
      new DatePickerDialog( mActivity, mDateListener, y, m, d ).show();
      saveSurvey();
      return;
    }

    int k = 0;
    if ( k < mNrButton1 && b == mButton1[k++] ) {  // note
      doNotes();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // INFO STATISTICS
      new SurveyStatDialog( mActivity, mApp.mData.getSurveyStat( mApp.mSID ) ).show();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // 3D
      do3D();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // GPS
      mActivity.startActivity( new Intent( mActivity, FixedActivity.class ) );
      // FIXME update declination
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // photo camera
      mActivity.startActivity( new Intent( mActivity, PhotoActivity.class ) );
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // sensors data
      mActivity.startActivity( new Intent( mActivity, SensorListActivity.class ) );
    }
  }

  @Override
  public void onStop()
  {
    saveSurvey();
    super.onStop();
  }

  private void doArchive()
  {
    while ( ! mApp.mEnableZip ) Thread.yield();

    if ( TDSetting.mExportShotsFormat >= 0 ) {
      doExport( TDSetting.mExportShotsFormat, false );
    }
    Archiver archiver = new Archiver( mApp );
    if ( archiver.archive( ) ) {
      String msg = getResources().getString( R.string.zip_saved ) + " " + archiver.zipname;
      Toast.makeText( mActivity, msg, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( mActivity, R.string.zip_failed, Toast.LENGTH_SHORT).show();
    }
  }

  private void askDelete()
  {
    TopoDroidAlertDialog.makeAlert( mActivity, getResources(), R.string.survey_delete,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doDelete();
        }
    } );
  }

  // ===============================================================

  private void do3D()
  {
    if ( mApp.exportSurveyAsTh() != null ) { // make sure to have survey exported as therion
      try {
        Intent intent = new Intent( "Cave3D.intent.action.Launch" );
        intent.putExtra( "survey", TDPath.getSurveyThFile( mApp.mySurvey ) );
        mActivity.startActivity( intent );
      } catch ( ActivityNotFoundException e ) {
        Toast.makeText( mActivity, R.string.no_cave3d, Toast.LENGTH_SHORT).show();
      }
    }
  }

  // private void doOpen()
  // {
  //   // TDLog.Log( TDLog.LOG_SURVEY, "do OPEN " );
  //   // dismiss();
  //   Intent openIntent = new Intent( mActivity, ShotWindow.class );
  //   mActivity.startActivity( openIntent );
  // }


  private void doNotes()
  {
    if ( mApp.mySurvey != null ) {
      (new DistoXAnnotations( mActivity, mApp.mySurvey )).show();
    } else { // SHOULD NEVER HAPPEN
      Toast.makeText( mActivity, R.string.no_survey, Toast.LENGTH_SHORT).show();
    }
  }

  void setDeclination( float decl )
  {
    mEditDecl.setText( String.format(Locale.US, "%.4f", decl ) );
    mApp.mData.updateSurveyDeclination( mApp.mSID, decl, true );
  }

  // float getDeclination()
  // {
  //   if ( mEditDecl.getText() != null ) {
  //     String decl_str = mEditDecl.getText().toString();
  //     if ( decl_str != null || decl_str.length() > 0 ) {
  //       try {
  //         return Float.parseFloat( decl_str );
  //       } catch ( NumberFormatException e ) {
  //         // ignore
  //       }
  //     }
  //   }
  //   return 0.0f;
  // }

  // ---------------------------------------------------------------

  private void saveSurvey( )
  {
    // String name = mTextName.getText().toString(); // RENAME is special
    // if ( name == null || name.length == 0 ) {
    // }
    String date = mEditDate.getText().toString();
    String team = mEditTeam.getText().toString();
    String comment = mEditComment.getText().toString();
    float decl = 0.0f;
    if ( mEditDecl.getText() != null ) {
      String decl_str = mEditDecl.getText().toString();
      if ( decl_str != null && decl_str.length() > 0 ) {
        try {
          decl = Float.parseFloat( decl_str );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse Float error: declination " + decl_str );
        }
      }
    }

    // FIXME FORCE NAMES WITHOUT SPACES
    // name = TopoDroidUtil.noSpaces( name );
    if ( date != null ) { date = date.trim(); } else { date = ""; }
    if ( team != null ) { team = team.trim(); } else { team = ""; }
    if ( comment != null ) { comment = comment.trim(); } else { comment = ""; }

    // TDLog.Log( TDLog.LOG_SURVEY, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
    mApp.mData.updateSurveyInfo( mApp.mSID, date, team, decl, comment, mInitStation, true );
  }

  public void doExport( String type )
  {
    saveSurvey();
    int index = TDConst.surveyExportIndex( type );
    if ( index == TDConst.DISTOX_EXPORT_ZIP ) {
      doArchive();
    } else if ( index >= 0 ) {
      doExport( index, true );
    }
  }
  
  private void doExport( int exportType, boolean warn )
  {
    if ( mApp.mSID < 0 ) {
      if ( warn ) {
        Toast.makeText( mActivity, R.string.no_survey, Toast.LENGTH_SHORT).show();
      }
    } else {
      String filename = null;
      switch ( exportType ) {
        // case TDConst.DISTOX_EXPORT_TLX:
        //   filename = mApp.exportSurveyAsTlx();
        //   break;
        case TDConst.DISTOX_EXPORT_DAT:
          filename = mApp.exportSurveyAsDat();
          break;
        case TDConst.DISTOX_EXPORT_SVX:
          filename = mApp.exportSurveyAsSvx();
          break;
        case TDConst.DISTOX_EXPORT_TRO:
          filename = mApp.exportSurveyAsTro();
          break;
        case TDConst.DISTOX_EXPORT_CSV:
          filename = mApp.exportSurveyAsCsv();
          break;
        case TDConst.DISTOX_EXPORT_DXF:
          List<DBlock> list = mApp.mData.selectAllShots( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
          DBlock blk = list.get( 0 );
          if ( blk != null ) {
            // Log.v( TopoDroidApp.TAG, "DISTOX_EXPORT_DXF from " + blk.mFrom );
            float decl = mApp.mData.getSurveyDeclination( mApp.mSID );
            DistoXNum num = new DistoXNum( list, blk.mFrom, null, null, decl );
            filename = mApp.exportSurveyAsDxf( num );
          }
          break;
        case TDConst.DISTOX_EXPORT_KML: // KML
          filename = mApp.exportSurveyAsKml( ); // can return ""
          break;
        case TDConst.DISTOX_EXPORT_PLT: // Track file
          filename = mApp.exportSurveyAsPlt( ); // can return ""
          break;
        case TDConst.DISTOX_EXPORT_CSX: // cSurvey
          filename = mApp.exportSurveyAsCsx( null, null );
          break;
        case TDConst.DISTOX_EXPORT_TOP: // PocketTopo
          filename = mApp.exportSurveyAsTop( null, null );
          break;
        case TDConst.DISTOX_EXPORT_SRV: // Walls
          filename = mApp.exportSurveyAsSrv();
          break;
        case TDConst.DISTOX_EXPORT_PLG: // Polygon
          filename = mApp.exportSurveyAsPlg();
          break;
        case TDConst.DISTOX_EXPORT_CAV: // Topo
          filename = mApp.exportSurveyAsCav();
          break;
        case TDConst.DISTOX_EXPORT_GRT: // Grottolf
          Toast.makeText( this, "WARNING Grottolf export is untested", Toast.LENGTH_SHORT ).show(); 
          filename = mApp.exportSurveyAsGrt();
          break;
        case TDConst.DISTOX_EXPORT_GTX: // GHTopo
          Toast.makeText( this, "WARNING GHTopo export is untested", Toast.LENGTH_SHORT ).show(); // FIXME TROBOT
          filename = mApp.exportSurveyAsGtx();
          break;
        case TDConst.DISTOX_EXPORT_SUR: // WinKarst
          Toast.makeText( this, "WARNING WinKarst export is untested", Toast.LENGTH_SHORT ).show(); 
          filename = mApp.exportSurveyAsSur();
          break;
        case TDConst.DISTOX_EXPORT_TRB: // TopoRobot
          Toast.makeText( this, "WARNING TopoRobot export is untested", Toast.LENGTH_SHORT ).show(); 
          filename = mApp.exportSurveyAsTrb();
          break;

        case TDConst.DISTOX_EXPORT_TH:
        default:
          filename = mApp.exportSurveyAsTh();
          break;
      }
      if ( warn ) { 
        if ( filename == null ) {
          Toast.makeText( mActivity, R.string.saving_file_failed, Toast.LENGTH_SHORT).show();
        } else if ( filename.length() == 0 ) {
          Toast.makeText( mActivity, R.string.no_geo_station, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText( mActivity, mActivity.getString(R.string.saving_) + filename, Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  private void doDelete()
  {
    if ( mApp.mSID < 0 ) return;
    String survey = mApp.mySurvey;

    TDPath.deleteSurveyFiles( survey );

    for ( int status = 0; status < 2; ++status ) {
      List< PlotInfo > plots = mApp.mData.selectAllPlots( mApp.mSID, status );
      if ( plots.size() > 0 ) {
        TDPath.deleteSurveyPlotFiles( survey, plots );
      }
    }

    // TODO delete 3D-files
    for ( int status = 0; status < 2; ++status ) {
      List< Sketch3dInfo > sketches = mApp.mData.selectAllSketches( mApp.mSID, status );
      if ( sketches.size() > 0 ) {
        TDPath.deleteSurvey3dFiles( survey, sketches );
      }
    }

    mApp.mData.doDeleteSurvey( mApp.mSID );
    mApp.setSurveyFromName( null, false ); // tell app to clear survey name and id
    setResult( RESULT_OK, new Intent() );
    finish();
    // dismiss();
  }


  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( mActivity, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
    mActivity.startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        // saveSurvey(); 
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
        return onSearchRequested();
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.SurveyWindow );
        if ( help_page != null ) UserManualActivity.showHelpPage( mActivity, help_page );
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }
  // ---------------------------------------------------------

  private void setMenuAdapter( Resources res )
  {
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( mActivity, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );
    mMenuAdapter = new ArrayAdapter<String>(mActivity, R.layout.menu );

    for ( int k = 0; k < menus.length; ++k ) {
      mMenuAdapter.add( res.getString( menus[k] ) );
    }
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
    int p = 0;
    if ( p++ == pos ) { // CLOSE
      // saveSurvey();
      super.onBackPressed();
    } else if ( p++ == pos ) { // EXPORT
      new ExportDialog( mActivity, this, TDConst.mSurveyExportTypes, R.string.title_survey_export ).show();
    } else if ( p++ == pos ) { // RENAME
      new SurveyRenameDialog( mActivity, this ).show();
    } else if ( p++ == pos ) { // DELETE
      askDelete();
    } else if ( p++ == pos ) { // INSTRUMENTS CALIBRATION
      new SurveyCalibrationDialog( mActivity, this ).show();
    } else if ( p++ == pos ) { // CALIBRATION CHECK SHOTS
      List< DBlock > shots = mApp.mData.selectAllShots( mApp.mSID, TopoDroidApp.STATUS_CHECK );
      if ( shots.size() == 0 ) {
        Toast.makeText( mActivity, R.string.no_calib_check, Toast.LENGTH_SHORT).show();
      } else {
        new CalibCheckDialog( mActivity, this, shots ).show();
      }
    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( mActivity, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
      mActivity.startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      (new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, menus.length ) ).show();
    }
    // updateDisplay();
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    closeMenu();
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    // if ( onMenu ) {
    //   closeMenu();
    //   return;
    // }
  }

  // TDR BINARY
  // private void startConvertTdrTh2Task()
  // {
  //   // final Activity currentActivity = this; 
  //   Handler convert_handler= new Handler(){
  //     @Override
  //     public void handleMessage(Message msg) {
  //       Toast.makeText( mActivity, R.string.converted_tdr2th2, Toast.LENGTH_SHORT).show();
  //     }
  //   };
  //   (new ConvertTdr2Th2Task( mActivity, convert_handler, mApp )).execute();
  // }

}
