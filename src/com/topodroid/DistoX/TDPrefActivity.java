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

import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;

import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;

import android.util.Log;

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
  static final int PREF_CATEGORY_SVG       = 11;
  static final int PREF_CATEGORY_DXF       = 12;
  static final int PREF_CATEGORY_PNG       = 13;
  static final int PREF_CATEGORY_KML       = 14;
  static final int PREF_SHOT_DATA          = 15; 
  static final int PREF_SHOT_UNITS         = 16; 
  static final int PREF_ACCURACY           = 17; 
  static final int PREF_LOCATION           = 18; 
  static final int PREF_PLOT_SCREEN        = 19; 
  static final int PREF_TOOL_LINE          = 20; 
  static final int PREF_TOOL_POINT         = 21; 
  static final int PREF_PLOT_WALLS         = 22; 
  static final int PREF_PLOT_DRAW          = 23; 
  static final int PREF_PLOT_ERASE         = 24; 
  static final int PREF_PLOT_EDIT          = 25; 
  static final int PREF_CATEGORY_LOG       = 26; // this must be the last

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
    R.string.title_settings_svg,      // 11
    R.string.title_settings_dxf,
    R.string.title_settings_png,
    R.string.title_settings_kml,
    R.string.title_settings_shot,     
    R.string.title_settings_units,    // 16
    R.string.title_settings_accuracy,
    R.string.title_settings_location,
    R.string.title_settings_screen,
    R.string.title_settings_line,
    R.string.title_settings_point,    // 21
    R.string.title_settings_walls,
    R.string.title_settings_draw,
    R.string.title_settings_erase,
    R.string.title_settings_edit,
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

    // Log.v("DistoX", "TDPrefActivity::onCreate");

    // mApp = (TopoDroidApp) getApplication();
    mCtx = TDInstance.context;

    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      mPrefCategory = extras.getInt( PREF_CATEGORY );
      if ( mPrefCategory < PREF_CATEGORY_ALL || mPrefCategory > PREF_CATEGORY_LOG ) {
        mPrefCategory = PREF_CATEGORY_ALL;
      }
    }
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
    Resources res = getResources();
    TDPrefHelper hlp = TopoDroidApp.mPrefHlp;

    // Log.v("DistoX", "Pref create. category " + mPrefCategory );
    switch ( mPrefCategory ) {
      case PREF_CATEGORY_ALL:       mPrefs = TDPref.makeMainPrefs( res, hlp );   break;
      case PREF_CATEGORY_SURVEY:    mPrefs = TDPref.makeSurveyPrefs( res, hlp ); break;
      case PREF_CATEGORY_PLOT:      mPrefs = TDPref.makePlotPrefs( res, hlp );   break;
      case PREF_CATEGORY_CALIB:     mPrefs = TDPref.makeCalibPrefs( res, hlp );  break;
      case PREF_CATEGORY_DEVICE:    mPrefs = TDPref.makeDevicePrefs( res, hlp ); break;
      case PREF_CATEGORY_EXPORT:    mPrefs = TDPref.makeExportPrefs( res, hlp ); break;
      case PREF_CATEGORY_IMPORT:    mPrefs = TDPref.makeImportPrefs( res, hlp ); break;
      case PREF_CATEGORY_SVX:       mPrefs = TDPref.makeSvxPrefs( res, hlp ); break;
      case PREF_CATEGORY_TH:        mPrefs = TDPref.makeThPrefs( res, hlp ); break;
      case PREF_CATEGORY_DAT:       mPrefs = TDPref.makeDatPrefs( res, hlp ); break;
      case PREF_CATEGORY_SVG:       mPrefs = TDPref.makeSvgPrefs( res, hlp ); break;
      case PREF_CATEGORY_DXF:       mPrefs = TDPref.makeDxfPrefs( res, hlp ); break;
      case PREF_CATEGORY_PNG:       mPrefs = TDPref.makePngPrefs( res, hlp ); break;
      case PREF_CATEGORY_KML:       mPrefs = TDPref.makeKmlPrefs( res, hlp ); break;
      case PREF_SHOT_DATA:          mPrefs = TDPref.makeShotPrefs( res, hlp );   break;
      case PREF_SHOT_UNITS:         mPrefs = TDPref.makeUnitsPrefs( res, hlp );  break;
      case PREF_ACCURACY:           mPrefs = TDPref.makeAccuracyPrefs( res, hlp ); break;
      case PREF_LOCATION:           mPrefs = TDPref.makeLocationPrefs( res, hlp ); break;
      case PREF_PLOT_SCREEN:        mPrefs = TDPref.makeScreenPrefs( res, hlp ); break;
      case PREF_TOOL_LINE:          mPrefs = TDPref.makeLinePrefs( res, hlp );   break;
      case PREF_TOOL_POINT:         mPrefs = TDPref.makePointPrefs( res, hlp );  break;
      case PREF_PLOT_WALLS:         mPrefs = TDPref.makeWallsPrefs( res, hlp );  break;
      case PREF_PLOT_DRAW:          mPrefs = TDPref.makeDrawPrefs( res, hlp );   break;
      case PREF_PLOT_ERASE:         mPrefs = TDPref.makeErasePrefs( res, hlp );  break;
      case PREF_PLOT_EDIT:          mPrefs = TDPref.makeEditPrefs( res, hlp );   break;
      case PREF_CATEGORY_SKETCH:    mPrefs = TDPref.makeSketchPrefs( res, hlp ); break;
      case PREF_CATEGORY_LOG:       mPrefs = TDPref.makeLogPrefs( res, hlp );    break;
      default:                      mPrefs = TDPref.makeMainPrefs( res, hlp );   break;
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
      
      linkPreference( "DISTOX_SURVEY_PREF", PREF_CATEGORY_SURVEY );
      linkPreference( "DISTOX_PLOT_PREF",   PREF_CATEGORY_PLOT );
      linkPreference( "DISTOX_EXPORT_PREF", PREF_CATEGORY_EXPORT );
      linkPreference( "DISTOX_SKETCH_PREF", PREF_CATEGORY_SKETCH );
      linkPreference( "DISTOX_DEVICE_PREF", PREF_CATEGORY_DEVICE );
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
      linkPreference( "DISTOX_EXPORT_SVX_PREF",  PREF_CATEGORY_SVX );
      linkPreference( "DISTOX_EXPORT_TH_PREF",   PREF_CATEGORY_TH );
      linkPreference( "DISTOX_EXPORT_DAT_PREF",  PREF_CATEGORY_DAT );
      linkPreference( "DISTOX_EXPORT_SVG_PREF",  PREF_CATEGORY_SVG );
      linkPreference( "DISTOX_EXPORT_DXF_PREF",  PREF_CATEGORY_DXF );
      linkPreference( "DISTOX_EXPORT_PNG_PREF",  PREF_CATEGORY_PNG );
      linkPreference( "DISTOX_EXPORT_KML_PREF",  PREF_CATEGORY_KML );
    } else if (mPrefCategory == PREF_CATEGORY_SURVEY ) {
      linkPreference( "DISTOX_LOCATION_SCREEN", PREF_LOCATION );
      linkPreference( "DISTOX_ACCURACY_SCREEN", PREF_ACCURACY );
      linkPreference( "DISTOX_SHOT_UNITS_SCREEN", PREF_SHOT_UNITS );
      linkPreference( "DISTOX_SHOT_DATA_SCREEN", PREF_SHOT_DATA );
    } else if (mPrefCategory == PREF_CATEGORY_PLOT ) {
      linkPreference( "DISTOX_PLOT_SCREEN", PREF_PLOT_SCREEN );
      linkPreference( "DISTOX_TOOL_LINE",   PREF_TOOL_LINE );
      linkPreference( "DISTOX_TOOL_POINT",  PREF_TOOL_POINT );
      linkPreference( "DISTOX_PLOT_WALLS",  PREF_PLOT_WALLS );
    } else if (mPrefCategory == PREF_CATEGORY_DEVICE ) {
      linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
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
	  // Log.v("DistoX", "click on " + pref.name );
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

}
