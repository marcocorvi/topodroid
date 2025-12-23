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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
// import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDRequest;
import com.topodroid.utils.TDLocale;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDsafUri;
// import com.topodroid.utils.TDVersion;
import com.topodroid.common.ExportInfo;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
import com.topodroid.calib.CalibCheckDialog;
import com.topodroid.common.PlotType;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
// import java.util.Calendar;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import android.app.Activity;
import android.app.DatePickerDialog;
// import android.app.Dialog;

import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
import android.os.ParcelFileDescriptor;

// import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.ActivityNotFoundException;
import android.content.res.Resources;
import android.content.res.Configuration;

// import android.location.LocationManager;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.View;
import android.view.View.OnFocusChangeListener;
// import android.view.View.OnClickListener;
import android.view.KeyEvent;
// for FRAGMENT
// import android.view.ViewGroup;
// import android.view.LayoutInflater;

import android.text.InputType;

// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;
// import android.net.Uri.Builder;

public class SurveyWindow extends Activity
                            implements IExporter
                            , ITeamText
                            , OnItemClickListener
                            , View.OnClickListener
{
  private static final int[] izons = {
                        R.drawable.iz_note,
                        R.drawable.iz_info, // ic_details,
                        R.drawable.iz_3d,
                        R.drawable.iz_location,
                        R.drawable.iz_picture,
                        R.drawable.iz_sensor
			// R.drawable.iz_empty // EMPTY
                     };
  private static final int BTN_PHOTO = 4;
  private static final int INDEX_3D = 2; // index of button-3D if any

  private static final int[] menus = {
                        R.string.menu_close,
                        R.string.menu_export,
                        R.string.menu_rename,
                        R.string.menu_delete,
                        R.string.menu_color,
                        R.string.menu_manual_calibration,
                        R.string.menu_calib_check,
                        R.string.menu_stations,
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
                        R.string.help_stations,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.SurveyWindow;

  // private static int icons00[];

  // private ShotWindow mParent;
  // private Context mContext;
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
  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;
  private ListView   mMenu;
  private Button     mMenuImage;
  private boolean onMenu;
  private String mInitStation = null;
  private int mXSections;
  private int mDatamode;

  private TopoDroidApp mApp;
  private DataHelper   mApp_mData;
  // private boolean mustOpen; // unused
  private int mNameColor;

  private boolean mSplayColor = false;

  private boolean mWarnTeam = true;

  private BitmapDrawable mBMpicture;
  private BitmapDrawable mBMpicture_no;

  // String getSurveyName() { return TDInstance.survey; }

  /** rename the current survey
   * @param name   new survey name
   */
  void renameSurvey( String name ) 
  {
    name = TDString.noSpaces( name );
    if ( mApp.renameCurrentSurvey( TDInstance.sid, name ) ) {
      mTextName.setText( name );
      mTextName.setTextColor( mNameColor );
      DrawingSurface.clearManagersCache();
      TDPath.setSurveyPaths( name );
    } else {
      TDToast.makeBad( R.string.cannot_rename );
    }
  } 

  /** add a prefix to the station names of a survey
   * @param prefix  prefix
   */
  void prefixStations( String prefix )
  {
    prefix = TDString.noSpaces(prefix );
    mApp.prefixSurveyStations(TDInstance.sid, prefix );
  }
    

// -------------------------------------------------------------------
  
  // called only once by onCreate
  boolean updateDisplay()
  {
    mSplayColor = TDLevel.overExpert && TDSetting.mSplayColor;

    // TDLog.Log( TDLog.LOG_SURVEY, "app SID " + TDInstance.sid );
    if ( TDInstance.sid < 0 ) return false;
    SurveyInfo info = TopoDroidApp.getSurveyInfo();
    if ( info == null ) return false;
    mTextName.setText( info.name );
    mTextName.setTextColor( mNameColor );
    mInitStation = info.initStation;
    mXSections   = info.xsections;
    mDatamode    = info.datamode;
    // mExtend      = info.getExtend(); // info.mExtend; // FIXME NOT USED

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
    if ( TDSetting.mTeamNames > 1 ) {
      mEditTeam.setInputType( InputType.TYPE_NULL );
      mEditTeam.setOnClickListener( this );
    } 
    setDeclination( info.declination );

    mTVxsections.setText( (mXSections == SurveyInfo.XSECTION_SHARED)? R.string.xsections_shared : R.string.xsections_private );
    if ( TDSetting.mDivingMode ) {
      mTVdatamode.setText( (mDatamode == SurveyInfo.DATAMODE_NORMAL)? R.string.datamode_normal : R.string.datamode_diving );
    } else {
      mTVdatamode.setVisibility( View.GONE );
    }
    return true;
  }



  @Override
  protected void onCreate( Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );

    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

    TDandroid.setScreenOrientation( this );

    mApp = (TopoDroidApp)getApplication();
    mApp_mData = TopoDroidApp.mData;
    TopoDroidApp.mSurveyWindow = this;
    mActivity = this;
    mNameColor = getResources().getColor( R.color.textfixed );

    // mContext = this;
    // mustOpen = false;
    // oldSid = -1L;
    // oldId  = -1L;

    // Bundle extras = getIntent().getExtras();
    // if ( extras != null ) {
    //   if ( extras.getInt( TDTag.TOPODROID_SURVEY ) == 1 ) mustOpen = true;
    //   // oldSid = extras.getLong( TDTag.TOPODROID_OLDSID );
    //   // oldId  = extras.getLong( TDTag.TOPODROID_OLDID );
    // }

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
      public void onFocusChange( View v, boolean has_focus ) {
        if ( ! has_focus ) {
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
      TDLog.e( "opening non-existent survey" );
      setResult( RESULT_CANCELED );
      finish();
    }

    // mFixed = new ArrayList<>();
    // mPhoto = new ArrayList<>();

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlaceholder( true );
    /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    Resources res = getResources();
    mNrButton1 = TDLevel.overNormal ? ( TDSetting.mWithSensors ? 6 : 5 ) : 2;

    mButton1 = new Button[ mNrButton1 + 1 ];
    int kb = 0;
    for ( int k=0; k < mNrButton1; ++k ) {
      mButton1[kb++] = MyButton.getButton( mActivity, this, izons[k] );
    }
    mNrButton1 = kb;
    mButton1[mNrButton1] = MyButton.getButton( mActivity, null, R.drawable.iz_empty );

    mBMpicture = MyButton.getButtonBackground( this, res, R.drawable.iz_picture );
    mBMpicture_no = MyButton.getButtonBackground( this, res, R.drawable.iz_picture_no );

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mMenuImage = (Button) findViewById( R.id.handle );
    mMenuImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, res, R.drawable.iz_menu ) );
    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );
  }

  /** lifecycle: on start
   */
  @Override
  public void onStart() 
  {
    super.onStart();
    TDLog.v("Survey Activity on Start " );
    mWarnTeam = true;
    TDLocale.resetTheLocale();
    setMenuAdapter( getResources() );
    closeMenu();
  }

  /** set the window title: the survey name
   */
  void setTheTitle()
  {
    setTitle( // mApp.getConnectionStateTitleStr() + // IF_COSURVEY
              getResources().getString( R.string.title_survey ) );
    if ( TDSetting.WITH_IMMUTABLE && ! TDInstance.isSurveyMutable ) { // IMMUTABLE
      mActivity.setTitleColor( 0xffff3333 );
    }
  }

  /** lifecycle: on resume
   */
  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    TDLog.v("Survey Activity on Resume " );
    int nrPhoto = 0;
    mApp_mData = TopoDroidApp.mData;
    if ( mApp_mData != null ) {
      doSetDeclination( mApp_mData.getSurveyDeclination( TDInstance.sid ) );
      nrPhoto = mApp_mData.countAllPhotos( TDInstance.sid, TDStatus.NORMAL );
    }
    setButtonPhoto( nrPhoto );
  }

  private void setButtonPhoto( int nr_photo )
  {
    if ( TDLevel.overNormal ) {
      // TDLog.v("nr photo " + nr_photo );
      if ( nr_photo == 0 ) {
        TDandroid.setButtonBackground( mButton1[ BTN_PHOTO ], mBMpicture_no );
        mButton1[ BTN_PHOTO ].setOnClickListener( null );
      } else {
        TDandroid.setButtonBackground( mButton1[ BTN_PHOTO ], mBMpicture );
        mButton1[ BTN_PHOTO ].setOnClickListener( this );
      }
    }
  }

  // ------------------------------------------

  public void setTeamText( String team ) 
  {
    if ( team != null ) {
      mEditTeam.setText( team );
    }
  }
   
  @Override
  public void onClick(View view)
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }
    if ( view instanceof EditText ) {
      if ( TDSetting.WITH_IMMUTABLE && ! TDInstance.isSurveyMutable ) {
        TDToast.makeWarn("Immutable survey");
        return;
      } 
      ArrayList< String > names = new ArrayList< String >();
      CharSequence chars = mEditTeam.getText();
      if ( chars != null ) {
        String[] tmp = chars.toString().split(";");
        for ( String t : tmp ) {
          t.trim();
          if ( t.length() > 0 ) names.add( t );
        }
      }
      (new TeamDialog( this, this, names )).show();
      return;
    } else if ( view instanceof Button ) {
      Button b = (Button)view;

      if ( b == mMenuImage ) {
        if ( mMenu.getVisibility() == View.VISIBLE ) {
          mMenu.setVisibility( View.GONE );
          onMenu = false;
        } else {
          mMenu.setVisibility( View.VISIBLE );
          onMenu = true;
        }
        return;
      } else if ( b == mEditDate ) {
        if ( TDSetting.WITH_IMMUTABLE && ! TDInstance.isSurveyMutable ) {
          TDToast.makeWarn("Immutable survey");
          return;
        } 
        String date = TDUtil.stringToDate( mEditDate.getText().toString() );
        int y = TDUtil.dateParseYear( date );
        int m = TDUtil.dateParseMonth( date );
        int d = TDUtil.dateParseDay( date );
        new DatePickerDialog( mActivity, mDateListener, y, m, d ).show();
        saveSurvey( false );
        return;
      }

      int k = 0;
      if ( k < mNrButton1 && b == mButton1[k++] ) {  // NOTES
        doNotes();
      } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // INFO STATISTICS
        new SurveyStatDialog( mActivity, mApp.getSurveyStat( TDInstance.sid ) ).show();
      } else if ( TDLevel.overNormal ) {
        if ( k < mNrButton1 && b == mButton1[k++] ) {  // 3D
          do3D();
        } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // GPS LOCATION
          mActivity.startActivity( new Intent( mActivity, FixedActivity.class ) );
          // FIXME update declination
        } else if ( k < mNrButton1 && b == mButton1[k++] ) {  // PHOTO CAMERA
          // mActivity.startActivity( new Intent( mActivity, PhotoActivity.class ) );
          (new PhotoListDialog( this, mApp_mData )).show();
        } else if ( TDSetting.mWithSensors && k < mNrButton1 && b == mButton1[k++] ) {  // SENSORS DATA
          mActivity.startActivity( new Intent( mActivity, SensorListActivity.class ) );
        }
      }
    }
  }

  @Override
  public void onPause()
  {
    TDLog.v("Survey Activity on Pause " );
    super.onPause();
  }

  @Override
  public void onStop()
  {
    TDLog.v("Survey Activity on Stop " );
    saveSurvey( false );
    super.onStop();
  }

  // /** archive on default zipfile
  //  */
  // private void doArchive()
  // {
  //   while ( ! TopoDroidApp.mEnableZip ) Thread.yield();
  //   (new ExportZipTask( getApplicationContext(), mApp, null, true )).execute(); // true = toast
  // }

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
    // if ( TopoDroidApp.exportSurveyAsThSync( ) ) { // make sure to have survey exported as therion
      // int check = TDVersion.checkCave3DVersion( this );
      // // TDLog.v( "check Cave3D version: " + check );
      // if ( check < 0 ) {
      //   TDToast.makeBad( R.string.no_cave3d );
      // } else if ( check == 0 ) {
        try {
          // FIXME-CAVE3D Intent intent = new Intent( "Cave3D.intent.action.Launch" );
          Intent intent = new Intent( Intent.ACTION_VIEW ).setClass( this, com.topodroid.TDX.TopoGL.class );
          intent.putExtra( "INPUT_SURVEY", TDInstance.survey );   // survey name
          intent.putExtra( "SURVEY_BASE", TDPath.getPathBase() ); // current work directory - full path
            // uri string is "/storage/emulated/0/TopoDroid" so the same string as from getPathBase()
            // Uri.Builder uri_builder = new Uri.Builder();
            // uri_builder.path( TDPath.getPathBase() ); 
            // Uri uri = uri_builder.build();
            // TDLog.v("TopoDroid " + uri.toString() );
            // intent.putExtra( "BASE_URI", uri.toString() );
          mActivity.startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          TDToast.makeBad( R.string.no_cave3d );
        }
      // } else {
      //   TDToast.makeBad( R.string.outdated_cave3d );
      // }
    // }
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
      (new DialogAnnotations( mActivity, TDInstance.survey )).show();
    } else { // SHOULD NEVER HAPPEN
      TDToast.makeWarn( R.string.no_survey );
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
    mApp_mData.updateSurveyDeclination( TDInstance.sid, decl );
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

  /** save survey metadata
   * @param check_team   whether to check if the "Team" field is not empty
   * @return true if the survey data have been saved
   */
  private boolean saveSurvey( boolean check_team )
  {
    // TDLog.v("Save warn " + mWarnTeam + " check " + check_team );
    // String name = mTextName.getText().toString(); // RENAME is special
    // ...
    String team = mEditTeam.getText().toString();
    // if ( team != null ) team = team.trim();
    if ( TDString.isNullOrEmpty( team ) ) {
      if ( mWarnTeam && check_team ) {
        mEditTeam.setError( getResources().getString( R.string.error_team_required ) );
        TDToast.makeBad( R.string.survey_not_saved );
        mWarnTeam = false;
        return false;
      } else {
        team = "";
      }
    }
    String date    = TDUtil.stringToDate( mEditDate.getText().toString() );
    String comment = TDUtil.getTextOrEmpty( mEditComment ); // COMMENT can be empty 
    float decl = SurveyInfo.declination( mEditDecl );
    doSetDeclination( decl );

    // FORCE NAMES WITHOUT SPACES
    // name = TDString.noSpaces( name );
    // date, team, comment always non-null
    // /* if ( date != null ) */ { date = date.trim(); } // else { date = ""; }
    /* if ( team != null ) */ { team = team.trim(); } // else { team = ""; }
    // /* if ( comment != null ) */ { comment = comment.trim(); } // else { comment = ""; }

    // TDLog.v( "UPDATE survey id " + TDInstance.sid + " team " + team + " date " + date + " comment " + comment );
    mApp_mData.updateSurveyInfo( TDInstance.sid, date, team, decl, comment, mInitStation, mXSections );
    mWarnTeam = true;
    return true;
  }

  /** export the survey data
   * @param type      export file format
   * @param filename  export filename - short name, eg, "survey.dat"
   * @param prefix    station name prefix (Compass, VTopo, Winkarst)
   * @param first     not used
   * @param second    whether to export the second view (unused: only plan or profile in DrawingWindow) - not used here
   * @note interface IExporter
   */
  public void doExport( String type, String filename, String prefix, long first, boolean second )
  {
    TDLog.v( "SURVEY do export - name " + filename + " prefix " + prefix );
    if ( ! saveSurvey( false ) ) {
      TDLog.e( "SURVEY do export - name " + filename + " : save survey failed" );
      return;
    }
    TDSetting.mExportPrefix = prefix; // save export-prefix
    // mExportInfo = null;
    int index = TDConst.surveyFormatIndex( type );
    // TDLog.v( "SURVEY do export: type " + type + " index " + index );
    // if ( index == TDConst.SURVEY_FORMAT_ZIP ) {
    //   doArchive();
    // } else 
    if ( index >= 0 ) {
      if ( TDInstance.sid < 0 ) {
        TDToast.makeBad( R.string.no_survey );
      } else {
        ExportInfo export_info = new ExportInfo( index, prefix, filename, first );
        // APP_OUT_DIR
        // // if ( TDSetting.mExportUri ) { // FIXME-URI unused URI_EXPORT
        //   selectExportFromProvider( index, filename );
        // // } else {
        // //   mApp.doExportDataAsync( getApplicationContext(), export_info, true, false ); // uri = null
        // // }

        //if ( index == TDConst.SURVEY_FORMAT_ZIP ) { // EXPORT ZIP
        //  // selectExportFromProvider( index, filename );
        //  mApp.doExportDataAsync( getApplicationContext(), export_info, true, false ); // uri = null
        //} else {
        //  mApp.doExportDataAsync( getApplicationContext(), export_info, true, false ); // uri = null
        //}
        // 20251208 since there is no need for the "if"
        mApp.doExportDataAsync( getApplicationContext(), export_info, true, false ); // uri = null
      }
    } else {
      TDLog.e("Survey Window export - negative index " + index );
    }
  }

  // private static ExportInfo mExportInfo; // index of the export-type 

  /* unused URI_EXPORT
   *
  // FIXME_URI
  private void selectExportFromProvider( int index, String filename ) // EXPORT
  {
    // if ( ! TDSetting.mExportUri ) return; // FIXME-URI
    // Intent intent = new Intent( Intent.ACTION_INSERT_OR_EDIT );
    // Intent intent = new Intent( Intent.ACTION_CREATE_DOCUMENT );
    // intent.setType( TDConst.mMimeType[index] );
    // intent.addCategory(Intent.CATEGORY_OPENABLE);
    // intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    Intent intent = TDandroid.getOpenDocumentIntent( index ); // 20230118 replace previous 4 lines
    // intent.putExtra( "exporttype", index ); // index is not returned to the app
    intent.putExtra( Intent.EXTRA_TITLE, filename );
    mExportInfo.setIndex( index );
    startActivityForResult( Intent.createChooser(intent, getResources().getString( R.string.export_data_title ) ), TDRequest.REQUEST_GET_EXPORT );
  }

  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // TDLog.Log( TDLog.LOG_MAIN, "on Activity Result: request " + mRequestName[request] + " result: " + result );
    // if ( ! TDSetting.mExportUri ) return; // FIXME-URI
    if ( intent == null ) return;
    // Bundle extras = intent.getExtras();
    switch ( request ) {
      case TDRequest.REQUEST_GET_EXPORT:
        if ( result == Activity.RESULT_OK ) {
          // int index = intent.getIntExtra( "exporttype", -1 );
          Uri uri = intent.getData();
          // TDLog.v( "SURVEY export: index " + mExportInfor.index + " uri " + uri.toString() );
          mApp.doExportDataAsync( getApplicationContext(), uri, mExportInfo, true, false );
        }
    }
  }
  */

  private void doDelete()
  {
    if ( TDInstance.sid < 0 ) return;
    String survey = TDInstance.survey;

    // // TDPath.deleteShpDirs( survey, mApp_mData.selectPlotNames( TDInstance.sid ) );
    // TDPath.deleteSurveyFiles( survey );
    // for ( int status = 0; status < 2; ++status ) {
    //   List< PlotInfo > plots = mApp_mData.selectAllPlots( TDInstance.sid, status );
    //   if ( plots.size() > 0 ) {
    //     TDPath.deleteSurveyPlotFiles( survey, plots );
    //   }
    // }
    // /* FIXME_SKETCH_3D *
    // // delete 3D files
    // for ( int status = 0; status < 2; ++status ) {
    //   List< Sketch3dInfo > sketches = mApp_mData.selectAllSketches( TDInstance.sid, status );
    //   if ( sketches.size() > 0 ) {
    //     TDPath.deleteSurvey3dFiles( survey, sketches );
    //   }
    // }
    //  * FIXME_SKETCH_3D */
    // // TDPath.deleteSurveyOverviewFiles( survey );

    TDPath.deleteSurveyDir( survey );

    mApp_mData.doDeleteSurvey( TDInstance.sid );
    mApp.setSurveyFromName( null, SurveyInfo.DATAMODE_NORMAL, false, true ); // tell app to clear survey name and id
    setResult( RESULT_OK, new Intent() );
    TopoDroidApp.mSurveyWindow = null;
    super.onBackPressed();
    // finish();
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        if ( ! saveSurvey( true ) ) return true;
        TopoDroidApp.mSurveyWindow = null;
        super.onBackPressed();
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
  // ---------------------------------------------------------

  // called after updateDisplay
  private void setMenuAdapter( Resources res )
  {
    ArrayAdapter< String > menu_adapter;
    menu_adapter = new ArrayAdapter<>(mActivity, R.layout.menu );

    menu_adapter.add( res.getString( menus[0] ) );
    menu_adapter.add( res.getString( menus[1] ) );
    if ( TDLevel.overExpert   ) menu_adapter.add( res.getString( menus[2] ) );
    if ( TDLevel.overNormal   ) menu_adapter.add( res.getString( menus[3] ) );
    if ( mSplayColor          ) menu_adapter.add( res.getString( menus[4] ) );
    if ( TDLevel.overAdvanced ) menu_adapter.add( res.getString( menus[5] ) );
    if ( TDLevel.overAdvanced ) menu_adapter.add( res.getString( menus[6] ) );
    if ( TDLevel.overExpert   ) menu_adapter.add( res.getString( menus[7] ) );
    menu_adapter.add( res.getString( menus[8] ) );
    menu_adapter.add( res.getString( menus[9] ) );

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
    int p = 0;
    if ( p++ == pos ) { // CLOSE
      TopoDroidApp.mSurveyWindow = null;
      super.onBackPressed();
    } else if ( p++ == pos ) { // EXPORT
      boolean diving = (mDatamode == SurveyInfo.DATAMODE_DIVING );
      String[] types = TDConst.surveyExportTypes( mApp_mData.hasFixed( TDInstance.sid, TDStatus.NORMAL) );
      new ExportDialogShot( mActivity, this, types, R.string.title_survey_export, TDInstance.survey, diving, true ).show(); // with_name = true
    } else if ( TDLevel.overExpert && p++ == pos ) { // RENAME
      new SurveyRenameDialog( mActivity, this ).show();
    } else if ( TDLevel.overNormal && p++ == pos ) { // DELETE
      askDelete();
    } else if ( mSplayColor && p++ == pos ) { // CLEAR COLOR
      mApp_mData.resetShotColor( TDInstance.sid );
    } else if ( TDLevel.overAdvanced && p++ == pos ) { // INSTRUMENTS CALIBRATION
      new SurveyCalibrationDialog( mActivity /*, mApp */ ).show();
    } else if ( TDLevel.overAdvanced && p++ == pos ) { // CALIBRATION CHECK SHOTS
      List< DBlock > shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.CHECK );
      if ( TDUtil.isEmpty(shots) ) {
        TDToast.makeWarn( R.string.no_calib_check );
      } else {
        new CalibCheckDialog( mActivity, this, shots ).show();
      }
    } else if ( TDLevel.overExpert && p++ == pos ) { // STATION NAMES
      Set< String > stations = mApp_mData.selectAllSurveyStations( TDInstance.sid );
      if ( stations.isEmpty() ) {
        TDToast.makeWarn( R.string.no_stations );
      } else {
        new StationsDialog( mActivity, this, stations ).show();
      }
    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_SURVEY );
      mActivity.startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
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

  /** react to a change in the configuration
   * @param new_cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    TDLocale.resetTheLocale();
  }

  /** remap the names of the stations
   * @para, name_map  names mapping
   */
  void remapStationNames( List< StationMap > name_map )
  {

    DrawingSurface.clearManagersCache();

    List< String > xs_names   = mApp_mData.getSurveyPlotNames( TDInstance.sid, PlotType.PLOT_X_SECTION );
    // TDLog.v("xs XSection " + xs_names.size() );
    if ( ! xs_names.isEmpty() ) {
      String prefix = TDInstance.survey + "-xs-";
      List< String > plan_names = mApp_mData.getSurveyPlotNames( TDInstance.sid, PlotType.PLOT_PLAN ); 
      for ( String plan : plan_names ) {
        String filename = TDPath.getSurveyPlotTdrFile( TDInstance.survey, plan );
        DrawingIO.doRemapStationXSections( filename, name_map, prefix );
      }
    }
    List< String > xh_names   = mApp_mData.getSurveyPlotNames( TDInstance.sid, PlotType.PLOT_XH_SECTION );
    // TDLog.v("xh XSection " + xh_names.size() );
    if ( ! xh_names.isEmpty() ) {
      String prefix = TDInstance.survey + "-xh-";
      List< String > ext_names  = mApp_mData.getSurveyPlotNames( TDInstance.sid, PlotType.PLOT_EXTENDED ); 
      for ( String ext : ext_names ) {
        String filename = TDPath.getSurveyPlotTdrFile( TDInstance.survey, ext );
        DrawingIO.doRemapStationXSections( filename, name_map, prefix );
      }
      List< String > proj_names = mApp_mData.getSurveyPlotNames( TDInstance.sid, PlotType.PLOT_PROJECTED ); 
      for ( String proj : proj_names ) {
        String filename = TDPath.getSurveyPlotTdrFile( TDInstance.survey, proj );
        DrawingIO.doRemapStationXSections( filename, name_map, prefix );
      }
    }

    ArrayList< StationMap > tmp_map1 = new ArrayList< StationMap >();
    for ( StationMap sm : name_map ) {
      if ( sm.mFrom.equals( sm.mTo ) ) continue;
      tmp_map1.add( sm );
    }
    int nr = tmp_map1.size();
    if ( nr < 1 ) return;
    String prefix = "";
    boolean ok = false;
    while ( ! ok ) {
      ok = true;
      prefix = prefix + "$";
      for ( StationMap sm : name_map ) { // use name_map: avoid conflict with other names
        if ( sm.startsWith( prefix ) ) { ok = false; break; }
      }
    }
    // TDLog.v("Remap station names " + nr + ". Prefix <" + prefix + ">" );
    ArrayList< StationMap > tmp_map2 = new ArrayList< StationMap >();
    for ( int k = 0; k < nr; ++k ) {
      String tmp_name = prefix + k;
      StationMap sm = tmp_map1.get( k );
      tmp_map2.add( new StationMap( tmp_name, sm.mTo ) );
      sm.mTo = tmp_name;
    }
    // TDLog.v("Remap station names. First pass");
    mApp_mData.remapStationNames( TDInstance.sid, tmp_map1 );
    for ( StationMap sm : tmp_map1 ) TDPath.renameStationXSectionFiles( TDInstance.survey, sm.mFrom, sm.mTo );
    // TDLog.v("Remap station names. Second pass");
    mApp_mData.remapStationNames( TDInstance.sid, tmp_map2 );
    for ( StationMap sm : tmp_map2 ) TDPath.renameStationXSectionFiles( TDInstance.survey, sm.mFrom, sm.mTo );

  }

  StationsDialog mStationsDialog = null;

  void doReadNameMap( StationsDialog dialog )
  {
    mStationsDialog = dialog;
    int index = TDConst.SURVEY_FORMAT_TEXT;
    Intent intent = TDandroid.getOpenDocumentIntent( index ); 
    startActivityForResult( Intent.createChooser(intent, getResources().getString( R.string.title_read_names ) ), TDRequest.REQUEST_READ_NAMES );
  }
    
  public void onActivityResult( int request, int result, Intent intent ) 
  {
    switch ( request ) {
      case TDRequest.REQUEST_READ_NAMES:
        if ( result == Activity.RESULT_OK ) {
          if ( mStationsDialog != null ) {
            Uri uri = intent.getData();   // import uri - may NullPointerException
            String mimetype = TDsafUri.getDocumentType( uri );
            // String path = TDsafUri.getDocumentPath(this, uri); // 2025-11-26
            // // TDLog.v("MIME " + mimetype + " " + path );
            // if (path != null) {
              try {
                ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
                if ( pfd != null ) {
                  InputStreamReader isr = new InputStreamReader( TDsafUri.docFileInputStream( pfd ) );
                  mStationsDialog.readNames( isr );
                  isr.close();
                }
              } catch ( IOException e ) {
                TDLog.e("IO error " + e.getMessage() );
              }
            // }
          }
        } else {
          TDLog.e("READ canceled");
        }
        mStationsDialog = null;
        break;
    }
  }

}
