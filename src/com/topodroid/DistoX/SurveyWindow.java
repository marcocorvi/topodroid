/* @file SurveyWindow.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Locale;
import java.util.List;
// import java.util.Calendar;
// import java.util.ArrayList;

// import java.io.File;
// import java.io.IOException;

import android.app.Activity;
import android.app.DatePickerDialog;
// import android.app.Dialog;

import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.ActivityNotFoundException;
import android.content.res.Resources;

// import android.location.LocationManager;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

// import android.widget.Toast;

import android.util.Log;

import android.view.View;
import android.view.View.OnFocusChangeListener;
// import android.view.View.OnClickListener;
import android.view.KeyEvent;
// for FRAGMENT
// import android.view.ViewGroup;
// import android.view.LayoutInflater;

// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.graphics.drawable.BitmapDrawable;

// import android.net.Uri;

public class SurveyWindow extends Activity
                            implements IExporter
                            , OnItemClickListener
                            , View.OnClickListener
{
  private static final int[] izons = {
                        R.drawable.iz_note,
                        R.drawable.iz_info, // ic_details,
                        R.drawable.iz_3d,
                        R.drawable.iz_gps,
                        R.drawable.iz_camera,
                        R.drawable.iz_sensor,
			R.drawable.iz_empty
                     };
  private static final int[] menus = {
                        R.string.menu_close,
                        R.string.menu_export,
                        R.string.menu_rename,
                        R.string.menu_delete,
                        R.string.menu_color,
                        R.string.menu_manual_calibration,
                        R.string.menu_calib_check,
                        R.string.menu_options,
                        R.string.menu_help
                      };

  private static final int[] help_icons = {
                        R.string.help_note,
                        R.string.help_info_shot,
                        R.string.help_3d,
                        R.string.help_loc,
                        R.string.help_photo,
                        R.string.help_sensor
                        };
  private static final int[] help_menus = {
                        R.string.help_close,
                        R.string.help_export_survey,
                        R.string.help_rename,
                        R.string.help_delete_survey,
                        R.string.help_color,
                        R.string.help_manual_calibration,
                        R.string.help_calib_check,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.SurveyWindow;

  // private static int icons00[];

  // private ShotWindow mParent;
  private Context mContext;
  private Activity mActivity = null;

  private EditText mTextName;
  private Button   mEditDate;
  private EditText mEditTeam;
  private EditText mEditDecl;
  private EditText mEditComment;
  private TextView mTVxsections;
  private TextView mTVdatamode;

  private MyDateSetListener mDateListener;

  private Button[] mButton1;
  private int mNrButton1 = 0;
  private HorizontalListView mListView;
  private HorizontalButtonView mButtonView1;
  private ListView   mMenu;
  private Button     mImage;
  // HOVER
  // MyMenuAdapter mMenuAdapter;
  private ArrayAdapter< String > mMenuAdapter;
  private boolean onMenu;
  private String mInitStation = null;
  private int mXSections;
  private int mDatamode;

  private TopoDroidApp mApp;
  private DataHelper   mApp_mData;
  private boolean mustOpen;
  private int mNameColor;

  // String getSurveyName() { return TDInstance.survey; }

  void renameSurvey( String name ) 
  {
    name = TopoDroidUtil.noSpaces( name );
    if ( mApp.renameCurrentSurvey( TDInstance.sid, name, true ) ) {
      mTextName.setText( name );
      mTextName.setTextColor( mNameColor );
    } else {
      TDToast.make( R.string.cannot_rename );
    }
  } 
    

// -------------------------------------------------------------------
  
  boolean updateDisplay()
  {
    // TDLog.Log( TDLog.LOG_SURVEY, "app SID " + TDInstance.sid );
    if ( TDInstance.sid < 0 ) return false;
    SurveyInfo info = TopoDroidApp.getSurveyInfo();
    if ( info == null ) return false;
    mTextName.setText( info.name );
    mTextName.setTextColor( mNameColor );
    mInitStation = info.initStation;
    mXSections   = info.xsections;
    mDatamode    = info.datamode;

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

    mTVxsections.setText( (mXSections == SurveyInfo.XSECTION_SHARED)? R.string.xsections_shared : R.string.xsections_private );
    mTVdatamode.setText( (mDatamode == SurveyInfo.DATAMODE_NORMAL)? R.string.datamode_normal : R.string.datamode_diving );
    return true;
  }



  @Override
  protected void onCreate( Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );

    mApp = (TopoDroidApp)getApplication();
    mApp_mData = TopoDroidApp.mData;
    mApp.mSurveyWindow = this;
    mActivity = this;
    mNameColor = getResources().getColor( R.color.textfixed );

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
    mTVxsections = (TextView) findViewById(R.id.survey_xsections);
    mTVdatamode  = (TextView) findViewById(R.id.survey_datamode);

    mEditDecl.setOnFocusChangeListener( new OnFocusChangeListener() {
      @Override
      public void onFocusChange( View v, boolean hasfocus ) {
        if ( ! hasfocus ) {
          if ( SurveyInfo.declinationOutOfRange( mEditDecl ) ) {
            mEditDecl.setText("");
	  }
	} 
      }
    } );

    mDateListener = new MyDateSetListener( mEditDate );
    mEditDate.setOnClickListener( this );

    // mTextName.setEditable( false );
    mTextName.setKeyListener( null );
    mTextName.setClickable( false );
    mTextName.setFocusable( false );

    if ( ! updateDisplay() ) {
      TDLog.Error( "opening non-existent survey" );
      setResult( RESULT_CANCELED );
      finish();
    }

    // mFixed = new ArrayList<>();
    // mPhoto = new ArrayList<>();

    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlacholder( true );
    /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    Resources res = getResources();
    mNrButton1 = TDLevel.overNormal ? 6 
               : TDLevel.overBasic ? 3 : 2;
    mButton1 = new Button[ mNrButton1 + 1 ];
    for ( int k=0; k < mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
    }
    mButton1[mNrButton1] = MyButton.getButton( mActivity, this, R.drawable.iz_empty );

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
    setTitle( // mApp.getConnectionStateTitleStr() + // IF_COSURVEY
              getResources().getString( R.string.title_survey ) );
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // mApp.resetLocale(); // FIXME-LOCALE
    doSetDeclination( mApp_mData.getSurveyDeclination( TDInstance.sid ) );
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
    if ( k < mNrButton1 && b == mButton1[k++] ) {  // NOTES
      doNotes();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // INFO STATISTICS
      new SurveyStatDialog( mActivity, mApp_mData.getSurveyStat( TDInstance.sid ) ).show();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // 3D
      do3D();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // GPS LOCATION
      mActivity.startActivity( new Intent( mActivity, FixedActivity.class ) );
      // FIXME update declination
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // PHOTO CAMERA
      mActivity.startActivity( new Intent( mActivity, PhotoActivity.class ) );
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // SENSORS DATA
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
    while ( ! TopoDroidApp.mEnableZip ) Thread.yield();

    (new ExportZipTask( getApplicationContext(), mApp )).execute();
    // TopoDroidApp.doExportDataSync( TDSetting.mExportShotsFormat );
    // Archiver archiver = new Archiver( mApp );
    // if ( archiver.archive( ) ) {
    //   String msg = getResources().getString( R.string.zip_saved ) + " " + archiver.zipname;
    //   TDToast.make( msg );
    // } else {
    //   TDToast.make( R.string.zip_failed );
    // }
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
    if ( TopoDroidApp.exportSurveyAsThSync( ) ) { // make sure to have survey exported as therion
      try {
        Intent intent = new Intent( "Cave3D.intent.action.Launch" );
        intent.putExtra( "survey", TDPath.getSurveyThFile( TDInstance.survey ) );
        mActivity.startActivity( intent );
      } catch ( ActivityNotFoundException e ) {
        TDToast.make( R.string.no_cave3d );
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
    if ( TDInstance.survey != null ) {
      (new DistoXAnnotations( mActivity, TDInstance.survey )).show();
    } else { // SHOULD NEVER HAPPEN
      TDToast.make( R.string.no_survey );
    }
  }

  // set the text/hint of mEditDecl 
  // @param decl   declination value
  private void doSetDeclination( float decl )
  {
    if ( decl < SurveyInfo.DECLINATION_MAX ) {
      mEditDecl.setText( String.format(Locale.US, "%.2f", decl ) );
    } else {
      mEditDecl.setHint( getResources().getString( R.string.declination ) );
      decl = SurveyInfo.DECLINATION_UNSET;
    }
  }

  void setDeclination( float decl )
  {
    doSetDeclination( decl );
    mApp_mData.updateSurveyDeclination( TDInstance.sid, decl, true );
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
    float decl = SurveyInfo.declination( mEditDecl );
    doSetDeclination( decl );

    // FORCE NAMES WITHOUT SPACES
    // name = TopoDroidUtil.noSpaces( name );
    // date, team, comment always non-null
    /* if ( date != null ) */ { date = date.trim(); } // else { date = ""; }
    /* if ( team != null ) */ { team = team.trim(); } // else { team = ""; }
    /* if ( comment != null ) */ { comment = comment.trim(); } // else { comment = ""; }

    // TDLog.Log( TDLog.LOG_SURVEY, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
    mApp_mData.updateSurveyInfo( TDInstance.sid, date, team, decl, comment, mInitStation, mXSections, true );
  }

  // interface IExporter
  public void doExport( String type )
  {
    saveSurvey();
    int index = TDConst.surveyExportIndex( type );
    if ( index == TDConst.DISTOX_EXPORT_ZIP ) {
      doArchive();
    } else if ( index >= 0 ) {
      TopoDroidApp.doExportDataAsync( getApplicationContext(), index, true );
    }
  }

  private void doDelete()
  {
    if ( TDInstance.sid < 0 ) return;
    String survey = TDInstance.survey;
    TDPath.deleteSurveyFiles( survey );

    for ( int status = 0; status < 2; ++status ) {
      List< PlotInfo > plots = mApp_mData.selectAllPlots( TDInstance.sid, status );
      if ( plots.size() > 0 ) {
        TDPath.deleteSurveyPlotFiles( survey, plots );
      }
    }

    // TODO delete 3D-files
    for ( int status = 0; status < 2; ++status ) {
      List< Sketch3dInfo > sketches = mApp_mData.selectAllSketches( TDInstance.sid, status );
      if ( sketches.size() > 0 ) {
        TDPath.deleteSurvey3dFiles( survey, sketches );
      }
    }

    mApp_mData.doDeleteSurvey( TDInstance.sid );
    mApp.setSurveyFromName( null, SurveyInfo.DATAMODE_NORMAL, false, false ); // tell app to clear survey name and id
    setResult( RESULT_OK, new Intent() );
    TopoDroidApp.mSurveyWindow = null;
    super.onBackPressed();
    // finish();
  }


  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( mActivity, TDPrefActivity.class );
    intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_SURVEY );
    mActivity.startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        // saveSurvey(); 
        TopoDroidApp.mSurveyWindow = null;
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
        return onSearchRequested();
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
  // ---------------------------------------------------------

  private void setMenuAdapter( Resources res )
  {
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( mActivity, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );
    mMenuAdapter = new ArrayAdapter<>(mActivity, R.layout.menu );

    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    if ( TDLevel.overExpert   ) mMenuAdapter.add( res.getString( menus[2] ) );
    if ( TDLevel.overNormal   ) mMenuAdapter.add( res.getString( menus[3] ) );
    if ( TDLevel.overExpert   ) mMenuAdapter.add( res.getString( menus[4] ) );
    if ( TDLevel.overAdvanced ) mMenuAdapter.add( res.getString( menus[5] ) );
    if ( TDLevel.overAdvanced ) mMenuAdapter.add( res.getString( menus[6] ) );
    mMenuAdapter.add( res.getString( menus[7] ) );
    mMenuAdapter.add( res.getString( menus[8] ) );

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
      TopoDroidApp.mSurveyWindow = null;
      super.onBackPressed();
    } else if ( p++ == pos ) { // EXPORT
      new ExportDialog( mActivity, this, TDConst.mSurveyExportTypes, R.string.title_survey_export ).show();
    } else if ( TDLevel.overExpert && p++ == pos ) { // RENAME
      new SurveyRenameDialog( mActivity, this ).show();
    } else if ( TDLevel.overNormal && p++ == pos ) { // DELETE
      askDelete();
    } else if ( TDLevel.overExpert && p++ == pos ) { // CLEAR COLOR
      mApp_mData.resetShotColor( TDInstance.sid );
    } else if ( TDLevel.overAdvanced && p++ == pos ) { // INSTRUMENTS CALIBRATION
      new SurveyCalibrationDialog( mActivity, mApp ).show();
    } else if ( TDLevel.overAdvanced && p++ == pos ) { // CALIBRATION CHECK SHOTS
      List< DBlock > shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.CHECK );
      if ( shots.size() == 0 ) {
        TDToast.make( R.string.no_calib_check );
      } else {
        new CalibCheckDialog( mActivity, this, shots ).show();
      }
    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( mActivity, TDPrefActivity.class );
      intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_SURVEY );
      mActivity.startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
    // updateDisplay();
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    closeMenu();
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      // return;
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
  //       TDToast.make( R.string.converted_tdr2th2 );
  //     }
  //   };
  //   (new ConvertTdr2Th2Task( mActivity, convert_handler, mApp )).execute();
  // }

}
