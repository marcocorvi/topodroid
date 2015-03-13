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
 * CHANGES
 * 20120520 created from DistoX.java
 * 20120524 fixed station management
 * 20120524 changing to dialog / menu buttons
 * 20120531 implementing survey delete
 * 20120603 fixed-info update/delete methods
 * 20120607 added 3D button / rearranged buttons layout
 * 20120610 archive (zip) button
 * 20120619 handle "mustOpen" (immediate) request
 * 20130213 unified export and zip (export dialog)
 * 20130307 made Annotations into a dialog
 * 20130910 populate survey with old-survey data (oldSid and oldId)
 * 20130921 handling return from Proj4 request (coord. conversion only on-demand for now)
 * 20130921 bug-fix long/lat swapped in add FixedInfo
 * 20131201 button bar new interface. reorganized actions
 * 20140221 if geodetic height fails, altimetric height is negative
 * 20140526 removed oldSid oldId
 */
package com.topodroid.DistoX;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.List;

import java.io.File;
import java.io.IOException;
// import java.io.StringWriter;
// import java.io.PrintWriter;

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

import android.app.Application;
import android.view.Menu;
import android.view.MenuItem;

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
                            implements OnItemClickListener
                            , View.OnClickListener
{
  // private static int icons[] = {
  //                       R.drawable.ic_note,
  //                       R.drawable.ic_info, // ic_details,
  //                       R.drawable.ic_3d,
  //                       R.drawable.ic_gps,
  //                       R.drawable.ic_camera,
  //                       R.drawable.ic_sensor 
  //                   };
  // private static int ixons[] = { 
  //                       R.drawable.ix_note,
  //                       R.drawable.ix_info, // ic_details,
  //                       R.drawable.ix_3d,
  //                       R.drawable.ix_gps,
  //                       R.drawable.ix_camera,
  //                       R.drawable.ix_sensor
  //                    };
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
                        R.string.menu_delete,
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
                        R.string.help_delete_survey,
                        R.string.help_prefs,
                        R.string.help_help
                      };
  // private static int icons00[];

  // private ShotActivity mParent;
  private Context mContext;

  private EditText mTextName;
  private EditText mEditDate;
  private EditText mEditTeam;
  private EditText mEditDecl;
  private EditText mEditComment;

  // private Button mButtonHelp;
  private Button[] mButton1;
  private int mNrButton1 = 0;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  private TopoDroidApp mApp;
  // private SurveyInfo mInfo; // FIXME this is local to updateDisplay()
  // private DistoX mDistoX;
  private boolean mustOpen;
  // private long oldSid;   // old survey id
  // private long oldId;    // old shot id

  // private ArrayList< FixedInfo > mFixed; // fixed stations
  // private ArrayList< PhotoInfo > mPhoto; // photoes

// -------------------------------------------------------------------
  // public SurveyActivity( Context context, ShotActivity parent )
  // {
  //   super( context );
  //   mContext = context;
  //   mParent  = parent;
  //   mApp = (TopoDroidApp)mParent.getApplication();
  // }

  private final static int LOCATION_REQUEST = 1;
  private static int CRS_CONVERSION_REQUEST = 2; // not final ?
  private DistoXLocation mLocation;
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

  boolean tryGPSAveraging( DistoXLocation loc )
  {
    mLocation = null;
    try {
      mLocation = loc;
      Intent intent = new Intent( "cz.destil.gpsaveraging.AVERAGED_LOCATION" );
      // Intent intent = new Intent( Intent.ACTION_DEFAULT, Uri.parse("cz.destil.gpsaveraging.AVERAGED_LOCATION") );
      startActivityForResult( intent, LOCATION_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ActivityNotFound " + e.toString() );
      mLocation = null;
      return false;
    }
    return true;
  }

  public void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    if ( resCode == RESULT_OK ) {
      if ( reqCode == LOCATION_REQUEST ) {
        if ( mLocation != null ) {
          Bundle bundle = intent.getExtras();
          mLocation.setPosition( 
            bundle.getDouble( "longitude" ),
            bundle.getDouble( "latitude" ),
            bundle.getDouble( "altitude" ) );
          // accuracy = bundle.getDouble( "accuracy" );
          // name = bundle.getStriung( "name" ); waypoint name

          mLocation = null;
        }
      } else if ( reqCode == CRS_CONVERSION_REQUEST ) {
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
      if ( extras.getInt( TopoDroidApp.TOPODROID_SURVEY ) == 1 ) mustOpen = true;
      // oldSid = extras.getLong( TopoDroidApp.TOPODROID_OLDSID );
      // oldId  = extras.getLong( TopoDroidApp.TOPODROID_OLDID );
    }

    setContentView(R.layout.survey_activity);
    setTitle( R.string.title_survey );
    mTextName    = (EditText) findViewById(R.id.survey_name);
    mEditDate    = (EditText) findViewById(R.id.survey_date);
    mEditTeam    = (EditText) findViewById(R.id.survey_team);
    mEditDecl    = (EditText) findViewById(R.id.survey_decl);
    mEditComment = (EditText) findViewById(R.id.survey_comment);

    if ( ! updateDisplay() ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "opening non-existent survey" );
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
    }

    int k = 0;
    // if ( k < mNrButton1 && b == mButton1[k++] ) {  // save
    //   doSave();
    // } else
    // if ( k < mNrButton1 && b == mButton1[k++] ) {  // export
    //   new SurveyExportDialog( this, this ).show();
    // } else
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
    doSave();
    super.onStop();
  }

  void doArchive()
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

  private void doSave()
  {
    saveSurvey( );
    // setMenus();
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
    new DistoXLocation( mContext, this, mApp, lm ).show();
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
    // String name = mTextName.getText().toString();
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
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse Float error: declination " + decl_str );
        }
      }
    }

    // FIXME FORCE NAMES WITHOUT SPACES
    // name = TopoDroidApp.noSpaces( name );
    if ( date != null ) { date = date.trim(); }
    if ( team != null ) { team = team.trim(); }
    if ( comment != null ) { comment = comment.trim(); }

    // TopoDroidLog.Log( TopoDroidLog.LOG_SURVEY, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
    if ( team == null ) team = "";
    mApp.mData.updateSurveyInfo( mApp.mSID, date, team, decl, comment, true );
  }
  
  void doExport( int exportType, boolean warn )
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
            DistoXNum num = new DistoXNum( list, blk.mFrom, null );
            filename = mApp.exportSurveyAsDxf( num );
          }
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

    File imagedir = new File( TopoDroidPath.getSurveyPhotoDir( survey ) );
    if ( imagedir.exists() ) {
      File[] fs = imagedir.listFiles();
      for ( File f : fs ) f.delete();
      imagedir.delete();
    }

    File t = new File( TopoDroidPath.getSurveyNoteFile( survey ) );
    if ( t.exists() ) t.delete();
    
    // t = new File( TopoDroidPath.getSurveyTlxFile( survey ) );
    // if ( t.exists() ) t.delete();
    
    t = new File( TopoDroidPath.getSurveyThFile( survey ) );
    if ( t.exists() ) t.delete();

    t = new File( TopoDroidPath.getSurveyCsvFile( survey ) );
    if ( t.exists() ) t.delete();
    
    t = new File( TopoDroidPath.getSurveyCsxFile( survey ) );
    if ( t.exists() ) t.delete();
    
    t = new File( TopoDroidPath.getSurveyDatFile( survey ) );
    if ( t.exists() ) t.delete();
    
    t = new File( TopoDroidPath.getSurveyDxfFile( survey ) );
    if ( t.exists() ) t.delete();
    
    t = new File( TopoDroidPath.getSurveySvxFile( survey ) );
    if ( t.exists() ) t.delete();
    
    t = new File( TopoDroidPath.getSurveySrvFile( survey ) );
    if ( t.exists() ) t.delete();
    
    t = new File( TopoDroidPath.getSurveyTopFile( survey ) );
    if ( t.exists() ) t.delete();
    
    t = new File( TopoDroidPath.getSurveyTroFile( survey ) );
    if ( t.exists() ) t.delete();
    
    for ( int status = 0; status < 2; ++status ) {
      List< PlotInfo > plots = mApp.mData.selectAllPlots( mApp.mSID, status );
      if ( TopoDroidPath.hasTh2Dir() ) {
        for ( PlotInfo p : plots ) {
          t = new File( TopoDroidPath.getSurveyPlotTh2File( survey, p.name ) );
          if ( t.exists() ) t.delete();
        }
      }
      if ( TopoDroidPath.hasPngDir() ) {
        for ( PlotInfo p : plots ) {
          t = new File( TopoDroidPath.getSurveyPlotPngFile( survey, p.name ) );
          if ( t.exists() ) t.delete();
        }
      }
      if ( TopoDroidPath.hasDxfDir() ) {
        for ( PlotInfo p : plots ) {
          t = new File( TopoDroidPath.getSurveyPlotDxfFile( survey, p.name ) );
          if ( t.exists() ) t.delete();
        }
      }
      if ( TopoDroidPath.hasSvgDir() ) {
        for ( PlotInfo p : plots ) {
          t = new File( TopoDroidPath.getSurveyPlotSvgFile( survey, p.name ) );
          if ( t.exists() ) t.delete();
        }
      }
    }


    mApp.mData.doDeleteSurvey( mApp.mSID );
    mApp.setSurveyFromName( null, false ); // tell app to clear survey name and id
    setResult( RESULT_OK, new Intent() );
    finish();
    // dismiss();
  }
 
  public FixedInfo addLocation( String station, double longitude, double latitude, double altitude, double altimetric )
  {
    // mApp.addFixed( station, longitude, latitude, altitude );
    // addFixed( station, longitude, latitude, altitude );
    long id = mApp.mData.insertFixed( mApp.mSID, -1L, station, longitude, latitude, altitude, altimetric, "", 0L ); // FIXME comment
    // TopoDroidLog.Log( TopoDroidLog.LOG_LOC, "addLocation mSID " + mApp.mSID + " id " + id );

    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format("\nfix %s %f %f %f m\n", station, latitude, longitude, altitude );
    // DistoXAnnotations.append( mApp.mySurvey, sw.getBuffer().toString() );

    return new FixedInfo( id, station, latitude, longitude, altitude, altimetric, "" ); // FIXME comment
  }

  public boolean updateFixed( FixedInfo fxd, String station )
  {
    return mApp.mData.updateFixedStation( fxd.id, mApp.mSID, station );
  }

  public void dropFixed( FixedInfo fxd )
  {
    mApp.mData.updateFixedStatus( fxd.id, mApp.mSID, TopoDroidApp.STATUS_DELETED );
  }

  // ---------------------------------------------------------
  /* MENU

  private MenuItem mMIexport;
  private MenuItem mMIoptions;
  private MenuItem mMIdelete;
  private MenuItem mMIhelp;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIexport  = menu.add( R.string.menu_export );
    mMIdelete  = menu.add( R.string.menu_delete );
    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIexport.setIcon(  icons[6] );
    mMIdelete.setIcon(  icons[7] );
    mMIoptions.setIcon( icons[8] );
    mMIhelp.setIcon(    icons[9] );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    Intent intent;
    if ( item == mMIoptions ) { // OPTIONS DIALOG
      intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
      startActivity( intent );
    } else if ( item == mMIexport  ) { // EXPORT
      new SurveyExportDialog( this, this ).show();
    } else if ( item == mMIdelete  ) { // DELETE DIALOG
      askDelete();
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 4 ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  */


  private void setMenuAdapter()
  {
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    mMenuAdapter.add( res.getString( menus[2] ) );
    mMenuAdapter.add( res.getString( menus[3] ) );
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
        new SurveyExportDialog( this, this ).show();
      } else if ( p++ == pos ) { // DELETE
        askDelete();
      } else if ( p++ == pos ) { // OPTIONS
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 4 ) ).show();
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
