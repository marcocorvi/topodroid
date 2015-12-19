/* @file SurveyActivity.java
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

import java.io.File;
import java.io.IOException;

import android.app.Activity;
// import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import android.location.LocationManager;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.app.DatePickerDialog;

import android.app.Application;
import android.view.KeyEvent;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import android.widget.Toast;

// import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;

public class SurveyActivity extends Activity
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
                        R.string.menu_export,
                        R.string.menu_rename,
                        R.string.menu_delete,
                        R.string.menu_manual_calibration,
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
                        R.string.help_export_survey,
                        R.string.help_rename,
                        R.string.help_delete_survey,
                        R.string.help_manual_calibration,
                        R.string.help_prefs,
                        R.string.help_help
                      };
  // private static int icons00[];

  // private ShotActivity mParent;
  private Context mContext;

  private EditText mTextName;
  private Button mEditDate;
  private EditText mEditTeam;
  private EditText mEditDecl;
  private EditText mEditComment;

  MyDateSetListener mDateListener;

  // private Button mButtonHelp;
  private Button[] mButton1;
  private int mNrButton1 = 0;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;
  String mInitStation;

  TopoDroidApp mApp;
  private boolean mustOpen;

  String getSurveyName() { return mApp.mySurvey; }

  void renameSurvey( String name ) 
  {
    name = TopoDroidUtil.noSpaces( name );
    if ( mApp.renameCurrentSurvey( mApp.mSID, name, true ) ) {
      mTextName.setText( name );
    } else {
      Toast.makeText( this, R.string.cannot_rename, Toast.LENGTH_SHORT).show();
    }
  } 
    

// -------------------------------------------------------------------
  // private final static int LOCATION_REQUEST = 1;
  private static int CRS_CONVERSION_REQUEST = 2; // not final ?
  
  // private LocationDialog mLocation;
  private FixedDialog mFixedDialog;

  void tryProj4( FixedDialog dialog, String cs_to, FixedInfo fxd )
  {
    if ( cs_to == null ) return;
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      // Intent intent = new Intent( Intent.ACTION_DEFAULT, "com.topodroid.Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "cs_from", "Long-Lat" ); // NOTE MUST USE SAME NAME AS Proj4
      intent.putExtra( "cs_to", cs_to ); 
      intent.putExtra( "longitude", fxd.lng );
      intent.putExtra( "latitude",  fxd.lat );
      intent.putExtra( "altitude",  fxd.alt );

      mFixedDialog = dialog;
      TopoDroidLog.Log( TopoDroidLog.LOG_LOC, "CONV. REQUEST " + fxd.lng + " " + fxd.lat + " " + fxd.alt );
      startActivityForResult( intent, SurveyActivity.CRS_CONVERSION_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      mFixedDialog = null;
      Toast.makeText( this, R.string.no_proj4, Toast.LENGTH_SHORT).show();
    }
  }

  public void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    if ( resCode == RESULT_OK ) {
      if ( reqCode == CRS_CONVERSION_REQUEST ) {
        if ( mFixedDialog != null ) {
          Bundle bundle = intent.getExtras();
          String cs = bundle.getString( "cs_to" );
          String title = String.format(Locale.ENGLISH, "%.2f %.2f %.2f",
             bundle.getDouble( "longitude"),
             bundle.getDouble( "latitude"),
             bundle.getDouble( "altitude") );
          TopoDroidLog.Log( TopoDroidLog.LOG_LOC, "CONV. RESULT " + title );
          mFixedDialog.setTitle( title );
          mFixedDialog.setCSto( cs );
          mFixedDialog = null;
        }
      }
    }
  }

  boolean updateDisplay()
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_SURVEY, "app mSID " + mApp.mSID );
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
    mApp.mSurveyActivity = this;
    mFixedDialog = null;

    mContext = this;
    mustOpen = false;
    // oldSid = -1L;
    // oldId  = -1L;
    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      if ( extras.getInt( TopoDroidTag.TOPODROID_SURVEY ) == 1 ) mustOpen = true;
      // oldSid = extras.getLong( TopoDroidTag.TOPODROID_OLDSID );
      // oldId  = extras.getLong( TopoDroidTag.TOPODROID_OLDID );
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
      TopoDroidLog.Error( "opening non-existent survey" );
      setResult( RESULT_CANCELED );
      finish();
    }

    // mFixed = new ArrayList< FixedInfo >();
    // mPhoto = new ArrayList< PhotoInfo >();

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );
    // icons00 = ( TopoDroidSetting.mSizeButtons == 2 )? ixons : icons;

    mNrButton1 = TopoDroidSetting.mLevelOverNormal ? 6 
               : TopoDroidSetting.mLevelOverBasic ? 3 : 2;
    mButton1 = new Button[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      // mButton1[k].setBackgroundResource( icons00[k] );
      mApp.setButtonBackground( mButton1[k], size, izons[k] );
    }

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    // mImage.setBackgroundResource( ( TopoDroidSetting.mSizeButtons == 2 )? R.drawable.ix_menu : R.drawable.ic_menu );
    mApp.setButtonBackground( mImage, size, R.drawable.iz_menu );
    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter();
    closeMenu();
    mMenu.setOnItemClickListener( this );

  }

  void setTheTitle()
  {
    setTitle( mApp.getConnectionStateTitleStr() +
              getResources().getString( R.string.title_survey ) );
  }

  // @Override
  // public synchronized void onResume() 
  // {
  //   super.onResume();
  //   if ( mustOpen ) {
  //     mustOpen = false;
  //     doOpen();
  //   }
  // }

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
      new DatePickerDialog( this, mDateListener, y, m, d ).show();
      return;
    }

    int k = 0;
    if ( k < mNrButton1 && b == mButton1[k++] ) {  // note
      doNotes();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // details
      (new SurveyStatDialog( this, mApp.mData.getSurveyStat( mApp.mSID ) )).show();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // 3D
      do3D();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // GPS
      doLocation();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // photo camera
      Intent photoIntent = new Intent( this, PhotoActivity.class );
      startActivity( photoIntent );
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // sensors data
      Intent intent = new Intent( this, SensorListActivity.class );
      startActivity( intent );
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

    doExport( TopoDroidSetting.mExportShotsFormat, false );
    Archiver archiver = new Archiver( mApp );
    if ( archiver.archive( ) ) {
      String msg = getResources().getString( R.string.zip_saved ) + " " + archiver.zipname;
      Toast.makeText( this, msg, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( this, R.string.zip_failed, Toast.LENGTH_SHORT).show();
    }
  }

  private void askDelete()
  {
    new TopoDroidAlertDialog( this, getResources(),
                      getResources().getString( R.string.survey_delete ),
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
    mApp.exportSurveyAsTh(); // make sure to have survey exported as therion
    try {
      Intent intent = new Intent( "Cave3D.intent.action.Launch" );
      intent.putExtra( "survey", TopoDroidPath.getSurveyThFile( mApp.mySurvey ) );
      startActivity( intent );
    } catch ( ActivityNotFoundException e ) {
      Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_SHORT).show();
    }
  }

  // private void doOpen()
  // {
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_SURVEY, "do OPEN " );
  //   // dismiss();
  //   Intent openIntent = new Intent( mContext, ShotActivity.class );
  //   mContext.startActivity( openIntent );
  // }

  private void doLocation()
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "doLocation" );
    LocationManager lm = (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
    new LocationDialog( mContext, this, mApp, lm ).show();
  }

  private void doNotes()
  {
    if ( mApp.mySurvey != null ) {
      (new DistoXAnnotations( this, mApp.mySurvey )).show();
    } else { // SHOULD NEVER HAPPEN
      Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_SHORT).show();
    }
  }

  void setDeclination( float decl )
  {
    mEditDecl.setText( String.format(Locale.ENGLISH, "%.4f", decl ) );
  }

  float getDeclination()
  {
    if ( mEditDecl.getText() != null ) {
      String decl_str = mEditDecl.getText().toString();
      if ( decl_str != null || decl_str.length() > 0 ) {
        try {
          return Float.parseFloat( decl_str );
        } catch ( NumberFormatException e ) {
          // ignore
        }
      }
    }
    return 0.0f;
  }

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
          TopoDroidLog.Error( "parse Float error: declination " + decl_str );
        }
      }
    }

    // FIXME FORCE NAMES WITHOUT SPACES
    // name = TopoDroidUtil.noSpaces( name );
    if ( date != null ) { date = date.trim(); }
    if ( team != null ) { team = team.trim(); }
    if ( comment != null ) { comment = comment.trim(); }

    // TopoDroidLog.Log( TopoDroidLog.LOG_SURVEY, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
    if ( team == null ) team = "";
    mApp.mData.updateSurveyInfo( mApp.mSID, date, team, decl, comment, mInitStation, true );
  }

  public void doExport( String type )
  {
    int index = TopoDroidConst.surveyExportIndex( type );
    if ( index == TopoDroidConst.DISTOX_EXPORT_ZIP ) {
      doArchive();
    } else if ( index >= 0 ) {
      doExport( index, true );
    }
  }
  
  private void doExport( int exportType, boolean warn )
  {
    if ( mApp.mSID < 0 ) {
      if ( warn ) {
        Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_SHORT).show();
      }
    } else {
      String filename = null;
      switch ( exportType ) {
        // case TopoDroidConst.DISTOX_EXPORT_TLX:
        //   filename = mApp.exportSurveyAsTlx();
        //   break;
        case TopoDroidConst.DISTOX_EXPORT_DAT:
          filename = mApp.exportSurveyAsDat();
          break;
        case TopoDroidConst.DISTOX_EXPORT_SVX:
          filename = mApp.exportSurveyAsSvx();
          break;
        case TopoDroidConst.DISTOX_EXPORT_TRO:
          filename = mApp.exportSurveyAsTro();
          break;
        case TopoDroidConst.DISTOX_EXPORT_CSV:
          filename = mApp.exportSurveyAsCsv();
          break;
        case TopoDroidConst.DISTOX_EXPORT_DXF:
          List<DistoXDBlock> list = mApp.mData.selectAllShots( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
          DistoXDBlock blk = list.get( 0 );
          if ( blk != null ) {
            // Log.v( TopoDroidApp.TAG, "DISTOX_EXPORT_DXF from " + blk.mFrom );
            DistoXNum num = new DistoXNum( list, blk.mFrom, null, null );
            filename = mApp.exportSurveyAsDxf( num );
          }
          break;
        case TopoDroidConst.DISTOX_EXPORT_KML:
          filename = mApp.exportSurveyAsKml( );
          break;
        case TopoDroidConst.DISTOX_EXPORT_PLT:
          filename = mApp.exportSurveyAsPlt( );
          break;
        case TopoDroidConst.DISTOX_EXPORT_CSX:
          filename = mApp.exportSurveyAsCsx( null, null );
          break;
        case TopoDroidConst.DISTOX_EXPORT_TOP:
          filename = mApp.exportSurveyAsTop( null, null );
          break;
        case TopoDroidConst.DISTOX_EXPORT_SRV:
          filename = mApp.exportSurveyAsSrv();
          break;

        case TopoDroidConst.DISTOX_EXPORT_TH:
        default:
          filename = mApp.exportSurveyAsTh();
          break;
      }
      if ( warn ) { 
        if ( filename != null ) {
          Toast.makeText( mContext, mContext.getString(R.string.saving_) + filename, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText( mContext, R.string.saving_file_failed, Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  private void doDelete()
  {
    if ( mApp.mSID < 0 ) return;
    String survey = mApp.mySurvey;

    TopoDroidPath.deleteSurveyFiles( survey );

    for ( int status = 0; status < 2; ++status ) {
      List< PlotInfo > plots = mApp.mData.selectAllPlots( mApp.mSID, status );
      if ( plots.size() > 0 ) {
        TopoDroidPath.deleteSurveyPlotFiles( survey, plots );
      }
    }

    // TODO delete 3D-files
    for ( int status = 0; status < 2; ++status ) {
      List< Sketch3dInfo > sketches = mApp.mData.selectAllSketches( mApp.mSID, status );
      if ( sketches.size() > 0 ) {
        TopoDroidPath.deleteSurvey3dFiles( survey, sketches );
      }
    }

    mApp.mData.doDeleteSurvey( mApp.mSID );
    mApp.setSurveyFromName( null, false ); // tell app to clear survey name and id
    setResult( RESULT_OK, new Intent() );
    finish();
    // dismiss();
  }

  public boolean hasLocation( String station )
  {
    return mApp.mData.hasFixed( mApp.mSID, station );
  }
 
  public FixedInfo addLocation( String station, double longitude, double latitude, double h_ellpsoid, double altimetric )
  {
    long id = mApp.mData.insertFixed( mApp.mSID, -1L, station, longitude, latitude, h_ellpsoid, altimetric, "", 0L );
    return new FixedInfo( id, station, longitude, latitude, h_ellpsoid, altimetric, "" ); // FIXME comment
  }

  boolean updateFixed( FixedInfo fxd, String station )
  {
    return mApp.mData.updateFixedStation( fxd.id, mApp.mSID, station );
  }

  void updateFixedData( FixedInfo fxd )
  {
    mApp.mData.updateFixedData( fxd.id, mApp.mSID, fxd.lng, fxd.lat, fxd.alt );
  }

  void updateFixedAltitude( FixedInfo fxd )
  {
    mApp.mData.updateFixedAltitude( fxd.id, mApp.mSID, fxd.alt, fxd.asl );
  }

  public void dropFixed( FixedInfo fxd )
  {
    mApp.mData.updateFixedStatus( fxd.id, mApp.mSID, TopoDroidApp.STATUS_DELETED );
  }


  @Override
  public boolean onSearchRequested()
  {
    // TopoDroidLog.Error( "search requested" );
    Intent intent = new Intent( this, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
    startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        return onSearchRequested();
      case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        TopoDroidLog.Error( "key down: code " + code );
    }
    return false;
  }
  // ---------------------------------------------------------

  private void setMenuAdapter()
  {
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    mMenuAdapter.add( res.getString( menus[2] ) );
    mMenuAdapter.add( res.getString( menus[3] ) );
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
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // EXPORT
        // new SurveyExportDialog( this, this ).show();
        new ExportDialog( this, this, TopoDroidConst.mSurveyExportTypes, R.string.title_survey_export ).show();
      } else if ( p++ == pos ) { // RENAME
        new SurveyRenameDialog( this, this ).show();
      } else if ( p++ == pos ) { // DELETE
        askDelete();
      } else if ( p++ == pos ) { // INSTRUMENTS CALIBRATION
        new SurveyCalibrationDialog( this, this ).show();
      } else if ( p++ == pos ) { // OPTIONS
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 6 ) ).show();
      }
      // updateDisplay();
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }
  }

}
