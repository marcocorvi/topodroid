/* @file TDPrefActivity.java
 *
 * @author marco corvi
 * @date aug 2018
 *
 * @brief TopoDroid options activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

import android.content.Intent;
import android.content.Context;
// import android.content.res.Resources;
import android.content.res.Configuration;

import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;

import android.widget.LinearLayout;
// import android.widget.ScrollView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;

/**
 */
public class TDPrefActivity extends Activity 
{
  static TDPrefActivity mPrefActivityAll = null;
  static TDPrefActivity mPrefActivitySurvey = null;

  static final int REQUEST_CWD         = 1;
  static final int REQUEST_PLOT_SCREEN = 2;
  static final int REQUEST_TOOL_SCREEN = 3;
  static final int REQUEST_LOCATION    = 4;
  static final int REQUEST_ACCURACY    = 5;
  static final int REQUEST_SHOT_DATA   = 6;
  static final int REQUEST_PT_CMAP     = 7;

  static final String PREF_CATEGORY = "PrefCategory";
  static final int PREF_CATEGORY_ALL       =  0;
  static final int PREF_CATEGORY_SURVEY    =  1;
  static final int PREF_CATEGORY_PLOT      =  2;
  static final int PREF_CATEGORY_CALIB     =  3;
  static final int PREF_CATEGORY_DEVICE    =  4;
  static final int PREF_CATEGORY_SKETCH    =  5;
  static final int PREF_CATEGORY_EXPORT    =  6;
  static final int PREF_CATEGORY_IMPORT    =  7;
  static final int PREF_CATEGORY_SVX       =  8;
  static final int PREF_CATEGORY_TH        =  9;
  static final int PREF_CATEGORY_DAT       = 10;
  static final int PREF_CATEGORY_TRO       = 11;
  static final int PREF_CATEGORY_SVG       = 12;
  static final int PREF_CATEGORY_DXF       = 13;
  static final int PREF_CATEGORY_SHP       = 14;
  static final int PREF_CATEGORY_PNG       = 15;
  static final int PREF_CATEGORY_KML       = 16;
  static final int PREF_CATEGORY_CSV       = 17;
  static final int PREF_SHOT_DATA          = 18; 
  static final int PREF_SHOT_UNITS         = 19; 
  static final int PREF_ACCURACY           = 20; 
  static final int PREF_LOCATION           = 21; 
  static final int PREF_PLOT_SCREEN        = 22; 
  static final int PREF_TOOL_LINE          = 23; 
  static final int PREF_TOOL_POINT         = 24; 
  static final int PREF_PLOT_WALLS         = 25; 
  static final int PREF_PLOT_DRAW          = 26; 
  static final int PREF_PLOT_ERASE         = 27; 
  static final int PREF_PLOT_EDIT          = 28; 
  static final int PREF_CATEGORY_GEEK      = 29; 
  static final int PREF_GEEK_SHOT          = 30; 
  static final int PREF_GEEK_PLOT          = 31; 
  static final int PREF_GEEK_LINE          = 32; 
  static final int PREF_GEEK_DEVICE        = 33; 
  static final int PREF_GEEK_IMPORT        = 34; 
  static final int PREF_CATEGORY_LOG       = 35; // this must be the last

  static int[] mTitleRes = {
    R.string.title_settings_main,     // 0
    R.string.title_settings_survey,
    R.string.title_settings_plot,
    R.string.title_settings_calib,
    R.string.title_settings_device,
    R.string.title_settings_sketch,   // 5
    R.string.title_settings_export,
    R.string.title_settings_import,
    R.string.title_settings_svx,
    R.string.title_settings_th,
    R.string.title_settings_dat,
    R.string.title_settings_tro,
    R.string.title_settings_svg,      // 12
    R.string.title_settings_dxf,
    R.string.title_settings_shp,
    R.string.title_settings_png,
    R.string.title_settings_kml,      // 16
    R.string.title_settings_csv,
    R.string.title_settings_shot,     
    R.string.title_settings_units,    // 19
    R.string.title_settings_accuracy,
    R.string.title_settings_location,
    R.string.title_settings_screen,   // 22
    R.string.title_settings_line,
    R.string.title_settings_point,    // 24
    R.string.title_settings_walls,
    R.string.title_settings_draw,
    R.string.title_settings_erase,    // 27
    R.string.title_settings_edit,
    R.string.title_settings_geek,
    R.string.title_settings_survey,   // 30
    R.string.title_settings_plot,
    R.string.title_settings_line,     // 32
    R.string.title_settings_device,   // 33
    R.string.title_settings_import,   // 34
    R.string.title_settings_log 
  };

  private int mPrefCategory = PREF_CATEGORY_ALL; // preference category

  private TDPref mCwdPref;
  private TDPref mPtCmapPref;

  // private TopoDroidApp mApp;
  private Context mCtx;
  private TDPref[] mPrefs;

  TDPref findPreference( String name ) 
  {
    if ( mPrefs == null ) return null;
    for ( TDPref pref : mPrefs ) {
      if ( name.equals( pref.name ) ) return pref;
    }
    return null;
  }

  @Override
  public void onDestroy( )
  {
    super.onDestroy();
    if ( this == mPrefActivityAll ) mPrefActivityAll = null;
    if ( this == mPrefActivitySurvey ) mPrefActivitySurvey = null;
    // if (mPrefCategory == PREF_CATEGORY_ALL ) { TopoDroidApp.mPrefActivityAll = null; }
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    TDandroid.setScreenOrientation( this );


    // mApp = (TopoDroidApp) getApplication();
    mCtx = TDInstance.context;

    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      mPrefCategory = extras.getInt( PREF_CATEGORY );
      if ( mPrefCategory < PREF_CATEGORY_ALL || mPrefCategory > PREF_CATEGORY_LOG ) {
        mPrefCategory = PREF_CATEGORY_ALL;
      }
    }
    // Log.v("DistoX", "TDPrefActivity::onCreate category " + mPrefCategory );
    if ( loadPreferences() ) {
      if (mPrefCategory == PREF_CATEGORY_ALL )    { mPrefActivityAll    = this; }
      if (mPrefCategory == PREF_CATEGORY_SURVEY ) { mPrefActivitySurvey = this; }
      setTheTitle();
    } else {
      finish();
    }
  }

  @Override
  public void onBackPressed () 
  {
    if (mPrefCategory == PREF_CATEGORY_ALL )    { mPrefActivityAll    = null; }
    if (mPrefCategory == PREF_CATEGORY_SURVEY ) { mPrefActivitySurvey = null; }
    if ( mPrefs != null ) {
      for ( TDPref pref : mPrefs ) if ( pref.wtype == TDPref.EDITTEXT ) {
        pref.commitValueString();
      }
    }
    // finish();
    super.onBackPressed();
  }

  private void setTheTitle()
  {
    setTitle( getResources().getString( mTitleRes[ mPrefCategory ] ) );
  }

  private boolean loadPreferences( )
  {
    setContentView( R.layout.pref_activity );
    LinearLayout layout = (LinearLayout) findViewById( R.id.layout );
    LayoutInflater li = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );

    mPrefs = null;
    // Resources res = getResources();
    TDPrefHelper hlp = TopoDroidApp.mPrefHlp;

    // Log.v("DistoX-PREF", "Load Pref create. category " + mPrefCategory );
    switch ( mPrefCategory ) {
      case PREF_CATEGORY_ALL:       mPrefs = TDPref.makeMainPrefs(     this, hlp ); break;
      case PREF_CATEGORY_SURVEY:    mPrefs = TDPref.makeSurveyPrefs(   this, hlp ); break;
      case PREF_CATEGORY_PLOT:      mPrefs = TDPref.makePlotPrefs(     this, hlp ); break;
      case PREF_CATEGORY_CALIB:     mPrefs = TDPref.makeCalibPrefs(    this, hlp ); break;
      case PREF_CATEGORY_DEVICE:    mPrefs = TDPref.makeDevicePrefs(   this, hlp ); break;
      case PREF_CATEGORY_EXPORT:    mPrefs = TDPref.makeExportPrefs(   this, hlp ); break;
      case PREF_CATEGORY_IMPORT:    mPrefs = TDPref.makeImportPrefs(   this, hlp ); break;
      case PREF_CATEGORY_SVX:       mPrefs = TDPref.makeSvxPrefs(      this, hlp ); break;
      case PREF_CATEGORY_TH:        mPrefs = TDPref.makeThPrefs(       this, hlp ); break;
      case PREF_CATEGORY_DAT:       mPrefs = TDPref.makeDatPrefs(      this, hlp ); break;
      case PREF_CATEGORY_TRO:       mPrefs = TDPref.makeTroPrefs(      this, hlp ); break;
      case PREF_CATEGORY_SVG:       mPrefs = TDPref.makeSvgPrefs(      this, hlp ); break;
      case PREF_CATEGORY_DXF:       mPrefs = TDPref.makeDxfPrefs(      this, hlp ); break;
      case PREF_CATEGORY_SHP:       mPrefs = TDPref.makeShpPrefs(      this, hlp ); break;
      case PREF_CATEGORY_PNG:       mPrefs = TDPref.makePngPrefs(      this, hlp ); break;
      case PREF_CATEGORY_KML:       mPrefs = TDPref.makeKmlPrefs(      this, hlp ); break;
      case PREF_CATEGORY_CSV:       mPrefs = TDPref.makeCsvPrefs(      this, hlp ); break;
      case PREF_SHOT_DATA:          mPrefs = TDPref.makeShotPrefs(     this, hlp ); break;
      case PREF_SHOT_UNITS:         mPrefs = TDPref.makeUnitsPrefs(    this, hlp ); break;
      case PREF_ACCURACY:           mPrefs = TDPref.makeAccuracyPrefs( this, hlp ); break;
      case PREF_LOCATION:           mPrefs = TDPref.makeLocationPrefs( this, hlp ); break;
      case PREF_PLOT_SCREEN:        mPrefs = TDPref.makeScreenPrefs(   this, hlp ); break;
      case PREF_TOOL_LINE:          mPrefs = TDPref.makeLinePrefs(     this, hlp ); break;
      case PREF_TOOL_POINT:         mPrefs = TDPref.makePointPrefs(    this, hlp ); break;
      case PREF_PLOT_WALLS:         mPrefs = TDPref.makeWallsPrefs(    this, hlp ); break;
      case PREF_PLOT_DRAW:          mPrefs = TDPref.makeDrawPrefs(     this, hlp ); break;
      case PREF_PLOT_ERASE:         mPrefs = TDPref.makeErasePrefs(    this, hlp ); break;
      case PREF_PLOT_EDIT:          mPrefs = TDPref.makeEditPrefs(     this, hlp ); break;
      case PREF_CATEGORY_GEEK:      mPrefs = TDPref.makeGeekPrefs(     this, hlp ); break;
      case PREF_GEEK_SHOT:          mPrefs = TDPref.makeGeekShotPrefs( this, hlp ); break;
      case PREF_GEEK_PLOT:          mPrefs = TDPref.makeGeekPlotPrefs( this, hlp ); break;
      case PREF_GEEK_LINE:          mPrefs = TDPref.makeGeekLinePrefs( this, hlp ); break;
      case PREF_GEEK_IMPORT:        mPrefs = TDPref.makeGeekImportPrefs( this, hlp ); break;
      case PREF_GEEK_DEVICE:        mPrefs = TDPref.makeGeekDevicePrefs( this, hlp ); break;
      // case PREF_CATEGORY_SKETCH:    mPrefs = TDPref.makeSketchPrefs(   this, hlp ); break; // FIXME_SKETCH_3D
      case PREF_CATEGORY_LOG:       mPrefs = TDPref.makeLogPrefs(      this, hlp ); break;
      default:                      mPrefs = TDPref.makeMainPrefs(     this, hlp ); break;
    }
    int cnt = 0;
    if ( mPrefs != null ) {
      for ( TDPref pref : mPrefs ) {
	if ( TDLevel.mLevel >= pref.level ) {
          LinearLayout.LayoutParams lp = TDLayout.getLayoutParamsFill( 10, 10, 10, 40 );
          layout.addView( pref.getView( this, li, null ), lp );
	  ++ cnt;
	}
      }
    }
    // Log.v("DistoX-PREF", "Level " + TDLevel.mLevel + " found " + cnt + " prefs" );
    if ( cnt == 0 ) return false;

    if (mPrefCategory == PREF_CATEGORY_ALL ) {
      mCwdPref = findPreference( "DISTOX_CWD" );
      if ( mCwdPref != null ) {
        mCwdPref.setValue( TDInstance.cwd );
        View v = mCwdPref.getView();
        if ( v != null ) {
          final Intent cwd_intent = new Intent( mCtx, CWDActivity.class ); // this
          v.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( View v ) { startActivityForResult( cwd_intent, REQUEST_CWD ); }
          } );
        }
      }
      if ( TDLevel.overAdvanced ) {
        TDPref export_settings = findPreference( "DISTOX_EXPORT_SETTINGS" );
        View v = export_settings.getView();
        if ( v != null ) {
          v.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( View v ) {
              ( new AsyncTask< Void, Void, Void >() { // FIXME static or LEAK
                @Override
                protected Void doInBackground(Void... v)
                {
                  // Log.v("DistoX", "export settings");
                  TDSetting.exportSettings();
                  return null;
                }
                @Override
                protected void onPostExecute( Void v )
                {
                  TDToast.make( String.format( getResources().getString( R.string.exported_settings ), TDPath.getSettingsPath() ) );
                }
              }).execute();
              finish();
          } } );
        }
      }
      
      linkPreference( "DISTOX_SURVEY_PREF", PREF_CATEGORY_SURVEY );
      linkPreference( "DISTOX_PLOT_PREF",   PREF_CATEGORY_PLOT );
      linkPreference( "DISTOX_EXPORT_PREF", PREF_CATEGORY_EXPORT );
      linkPreference( "DISTOX_DEVICE_PREF", PREF_CATEGORY_DEVICE );
      linkPreference( "DISTOX_GEEK_PREF",   PREF_CATEGORY_GEEK );
    } else if (mPrefCategory == PREF_CATEGORY_IMPORT ) {
      mPtCmapPref = findPreference( "DISTOX_PT_CMAP" );
      if ( mPtCmapPref != null ) {
        View v = mPtCmapPref.getView();
	if ( v != null ) {
          final Intent pt_intent = new Intent( mCtx, PtCmapActivity.class ); // this
          v.setOnClickListener( 
            new OnClickListener() {
              @Override
              public void onClick( View v ) { startActivityForResult( pt_intent, REQUEST_PT_CMAP ); }
          } );
	}
      }
    } else if (mPrefCategory == PREF_CATEGORY_EXPORT ) {
      linkPreference( "DISTOX_EXPORT_IMPORT_PREF",  PREF_CATEGORY_IMPORT );
      linkPreference( "DISTOX_EXPORT_SVX_PREF",     PREF_CATEGORY_SVX );
      linkPreference( "DISTOX_EXPORT_TH_PREF",      PREF_CATEGORY_TH );
      linkPreference( "DISTOX_EXPORT_DAT_PREF",     PREF_CATEGORY_DAT );
      linkPreference( "DISTOX_EXPORT_TRO_PREF",     PREF_CATEGORY_TRO );
      linkPreference( "DISTOX_EXPORT_SVG_PREF",     PREF_CATEGORY_SVG );
      linkPreference( "DISTOX_EXPORT_DXF_PREF",     PREF_CATEGORY_DXF );
      linkPreference( "DISTOX_EXPORT_SHP_PREF",     PREF_CATEGORY_SHP );
      linkPreference( "DISTOX_EXPORT_PNG_PREF",     PREF_CATEGORY_PNG );
      linkPreference( "DISTOX_EXPORT_KML_PREF",     PREF_CATEGORY_KML );
      linkPreference( "DISTOX_EXPORT_CSV_PREF",     PREF_CATEGORY_CSV );
    } else if (mPrefCategory == PREF_CATEGORY_SURVEY ) {
      linkPreference( "DISTOX_LOCATION_SCREEN",     PREF_LOCATION );
      linkPreference( "DISTOX_ACCURACY_SCREEN",     PREF_ACCURACY );
      linkPreference( "DISTOX_SHOT_UNITS_SCREEN",   PREF_SHOT_UNITS );
      linkPreference( "DISTOX_SHOT_DATA_SCREEN",    PREF_SHOT_DATA );
    } else if (mPrefCategory == PREF_CATEGORY_PLOT ) {
      linkPreference( "DISTOX_PLOT_SCREEN",         PREF_PLOT_SCREEN );
      linkPreference( "DISTOX_TOOL_LINE",           PREF_TOOL_LINE );
      linkPreference( "DISTOX_TOOL_POINT",          PREF_TOOL_POINT );
      // linkPreference( "DISTOX_PLOT_WALLS",          PREF_PLOT_WALLS );
    } else if (mPrefCategory == PREF_CATEGORY_DEVICE ) {
      linkPreference( "DISTOX_CALIB_PREF",          PREF_CATEGORY_CALIB );
    } else if (mPrefCategory == PREF_CATEGORY_GEEK ) {
      linkPreference( "DISTOX_GEEK_SHOT",           PREF_GEEK_SHOT );
      linkPreference( "DISTOX_GEEK_PLOT",           PREF_GEEK_PLOT );
      linkPreference( "DISTOX_GEEK_LINE",           PREF_GEEK_LINE );
      linkPreference( "DISTOX_PLOT_WALLS",          PREF_PLOT_WALLS );
      linkPreference( "DISTOX_GEEK_DEVICE",         PREF_GEEK_DEVICE );
      linkPreference( "DISTOX_GEEK_IMPORT",         PREF_GEEK_IMPORT );
      // linkPreference( "DISTOX_SKETCH_PREF",         PREF_CATEGORY_SKETCH ); // FIXME_SKETCH_3D
    }

    return true;
  }

  // called by TopoDroidApp.setLocale
  void reloadPreferences()
  {
    // Log.v("DistoXPref", "reload prefs. cat " + mPrefCategory );
    if ( mPrefCategory == PREF_CATEGORY_ALL ) {
      if ( loadPreferences() ) {
        setTheTitle();
      }
      // mPrefActivityAll = null;
      // finish();
      // Intent intent = new Intent( mCtx, TDPrefActivity.class );
      // intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_ALL );
      // startActivity( intent );
      // linkPreference( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_ALL );
    } else if ( mPrefCategory == PREF_CATEGORY_SURVEY ) {
      if ( loadPreferences() ) {
        setTheTitle();
      }
      // mPrefActivitySurvey = null;
      // finish();
      // Intent intent = new Intent( mCtx, TDPrefActivity.class );
      // intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_SURVEY );
      // startActivity( intent );
      // // linkPreference( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_SURVEY );
    }
  }

  private void linkPreference( String pref_name, int category )
  {
    // if ( pref_name == null ) return;
    // Log.v("DistoX", "link pref " + pref_name );
    final TDPref pref = findPreference( pref_name );
    if ( pref == null ) return;
    View v = pref.getView();
    if ( v == null ) return;
    final Intent intent = new Intent( mCtx, TDPrefActivity.class ); // this
    intent.putExtra( PREF_CATEGORY, category );
    v.setOnClickListener( 
      new View.OnClickListener() {
        @Override
        public void onClick( View v )
        {
	  // Log.v("DistoX-PREF", "click on " + pref.name + " categoy " + category );
          startActivity( intent );
        }
    } );
  }

  public void onActivityResult( int request, int result, Intent intent ) 
  {
    Bundle extras = (intent != null)? intent.getExtras() : null;
    switch ( request ) {
      case REQUEST_CWD:
        if ( result == RESULT_OK && extras != null ) {
          String cwd = extras.getString( TDTag.TOPODROID_CWD );
          mCwdPref.setButtonValue( cwd );
          // Log.v("DistoX", "got CWD " + cwd );
        } else if ( result == RESULT_CANCELED ) {
	  TDLog.Error("could not set CWD");
	}
        break;
      case REQUEST_PLOT_SCREEN:
      case REQUEST_TOOL_SCREEN:
      case REQUEST_LOCATION:
      case REQUEST_ACCURACY:
      case REQUEST_SHOT_DATA:
        break;
      case REQUEST_PT_CMAP:
        if ( extras != null ) {
          String cmap = extras.getString( TDTag.TOPODROID_CMAP );
          // mPtCmapPref.
        }
        break;
    }
  }

  // @Override
  // public void onConfigurationChanged( Configuration new_cfg )
  // {
  //   super.onConfigurationChanged( new_cfg );
  //   if ( new_cfg.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
  //     TDLog.Error( "landscape" );
  //   } else if ( new_cfg.orientation == Configuration.ORIENTATION_PORTRAIT ) {
  //     TDLog.Error( "portrait" );
  //   } else {
  //     TDLog.Error( "unknown" );
  //   }
  // }

}
