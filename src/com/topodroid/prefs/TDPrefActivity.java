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
package com.topodroid.prefs;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDTag;
import com.topodroid.ui.TDLayout;
import com.topodroid.TDX.TDandroid;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDLevel;
// import com.topodroid.TDX.TDToast;
// import com.topodroid.TDX.TDPath;
// import com.topodroid.TDX.TopoDroidApp;
// import com.topodroid.TDX.CWDActivity;
import com.topodroid.TDX.R;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
// import android.content.res.Resources;
// import android.content.res.Configuration;

import android.os.Bundle;
import android.app.Activity;

import android.widget.LinearLayout;
// import android.widget.ScrollView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;

/**
 */
public class TDPrefActivity extends Activity 
                            implements View.OnLongClickListener
{
  static TDPrefActivity mPrefActivityAll = null;
  static TDPrefActivity mPrefActivitySurvey = null;

  // static final int REQUEST_CWD         = 1;
  static final int REQUEST_PLOT_SCREEN = 2;
  static final int REQUEST_TOOL_SCREEN = 3;
  static final int REQUEST_LOCATION    = 4;
  static final int REQUEST_ACCURACY    = 5;
  static final int REQUEST_SHOT_DATA   = 6;
  static final int REQUEST_PT_CMAP     = 7;
  static final int REQUEST_GRAPH_PAPER_SCALE = 8;

  private int mPrefCategory = TDPrefCat.PREF_CATEGORY_ALL; // preference category

  // private TDPref mCwdPref;
  // private TDPref mBtAliasPref;
  private TDPref mPtCmapPref;
  private TDPref mGraphPaperScalePref;

  // private TopoDroidApp mApp;
  private Context mCtx;
  private TDPref[] mPrefs;

  /** find a preference by the name
   * @param name    preference name
   * @return the requested preference (or null if not found)
   */
  TDPref findPreference( String name ) 
  {
    if ( mPrefs == null ) return null;
    for ( TDPref pref : mPrefs ) {
      if ( name.equals( pref.name ) ) return pref;
    }
    return null;
  }

  /** lifecycle: activity destroy
   */
  @Override
  public void onDestroy( )
  {
    super.onDestroy();
    if ( this == mPrefActivityAll ) mPrefActivityAll = null;
    if ( this == mPrefActivitySurvey ) mPrefActivitySurvey = null;
    // if (mPrefCategory == TDPrefCat.PREF_CATEGORY_ALL ) { TopoDroidApp.mPrefActivityAll = null; }
  }

  /** lifecycle: activity create
   * @param savedInstanceState  saved state (unused)
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

    TDandroid.setScreenOrientation( this );


    // mApp = (TopoDroidApp) getApplication();
    mCtx = TDInstance.context;

    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      mPrefCategory = extras.getInt( TDPrefCat.PREF_CATEGORY );
      if ( mPrefCategory < TDPrefCat.PREF_CATEGORY_ALL || mPrefCategory > TDPrefCat.PREF_CATEGORY_MAX ) {
        mPrefCategory = TDPrefCat.PREF_CATEGORY_ALL;
      }
    }

    // TDLog.v( "TDPrefActivity::onCreate category " + mPrefCategory );
    if ( loadPreferences() ) {
      if (mPrefCategory == TDPrefCat.PREF_CATEGORY_ALL )    { mPrefActivityAll    = this; }
      if (mPrefCategory == TDPrefCat.PREF_CATEGORY_SURVEY ) { mPrefActivitySurvey = this; }
      setTheTitle();
    } else {
      setResult( RESULT_CANCELED );
      finish();
    }
  }

  /** react to a user tap on the BACK key
   */
  @Override
  public void onBackPressed () 
  {
    if (mPrefCategory == TDPrefCat.PREF_CATEGORY_ALL ) { 
      mPrefActivityAll = null;
    } else if (mPrefCategory == TDPrefCat.PREF_CATEGORY_SURVEY ) {
      mPrefActivitySurvey = null;
    }
    if ( mPrefs != null ) {
      for ( TDPref pref : mPrefs ) if ( pref.widget_type == TDPref.EDITTEXT ) {
        pref.commitValueString();
      }
    }
    setResult( RESULT_OK, null ); // result is always OK
    // finish();
    super.onBackPressed();
  }

  /** set the title of the window
   */
  private void setTheTitle()
  {
    setTitle( getResources().getString( TDPrefCat.mTitleRes[ mPrefCategory ] ) );
  }

  /** start the dialog to export/import settings
   * @param prefs    shared preferences
   */
  void startExportDialog( SharedPreferences prefs )
  {
    (new ExportDialogSettings( this, this, prefs, R.string.title_export_settings ) ).show();
  }

  public boolean onLongClick( View v ) 
  {
    ( new TDPrefSearchDialog( this, this )).show();
    return true;
  }

  boolean loadCategory( int cat )
  {
    mPrefCategory = cat;
    if ( ! loadPreferences() ) return false;
    setTheTitle();
    return true;
  }

  /** load the preferences
   */
  private boolean loadPreferences( )
  {
    setContentView( R.layout.pref_activity );
    LinearLayout layout = (LinearLayout) findViewById( R.id.layout );
    LayoutInflater li = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    layout.setOnLongClickListener( this );

    mPrefs = null;
    // Resources res = getResources();
    TDPrefHelper hlp = new TDPrefHelper( mCtx ); // TopoDroidApp.mPrefHlp;

    // TDLog.v( "PREF load category " + mPrefCategory );
    switch ( mPrefCategory ) {
      case TDPrefCat.PREF_CATEGORY_ALL:       mPrefs = TDPref.makeMainPrefs(     this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_SURVEY:    mPrefs = TDPref.makeSurveyPrefs(   this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_PLOT:      mPrefs = TDPref.makePlotPrefs(     this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_CALIB:     mPrefs = TDPref.makeCalibPrefs(    this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_DEVICE:    mPrefs = TDPref.makeDevicePrefs(   this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_EXPORT:    mPrefs = TDPref.makeExportPrefs(   this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_IMPORT:    mPrefs = TDPref.makeImportPrefs(   this, hlp ); break;
      // case TDPrefCat.PREF_CATEGORY_3D:        mPrefs = TDPref.make3DPrefs(       this, hlp ); break; // TODO
      case TDPrefCat.PREF_CATEGORY_EXPORT_ENABLE: mPrefs = TDPref.makeExportEnablePrefs(   this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_SVX:       mPrefs = TDPref.makeSvxPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_TH:        mPrefs = TDPref.makeThPrefs(       this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_DAT:       mPrefs = TDPref.makeDatPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_CSX:       mPrefs = TDPref.makeCsxPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_TRO:       mPrefs = TDPref.makeTroPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_SRV:       mPrefs = TDPref.makeSrvPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_PLY:       mPrefs = TDPref.makePlyPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_SVG:       mPrefs = TDPref.makeSvgPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_SHP:       mPrefs = TDPref.makeShpPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_DXF:       mPrefs = TDPref.makeDxfPrefs(      this, hlp ); break;
      // case TDPrefCat.PREF_CATEGORY_PNG:       mPrefs = TDPref.makePngPrefs(      this, hlp ); break; // NO_PNG
      case TDPrefCat.PREF_CATEGORY_GPX:       mPrefs = TDPref.makeGpxPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_KML:       mPrefs = TDPref.makeKmlPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_CSV:       mPrefs = TDPref.makeCsvPrefs(      this, hlp ); break;
      case TDPrefCat.PREF_SHOT_DATA:          mPrefs = TDPref.makeShotPrefs(     this, hlp ); break;
      case TDPrefCat.PREF_SHOT_UNITS:         mPrefs = TDPref.makeUnitsPrefs(    this, hlp ); break;
      case TDPrefCat.PREF_ACCURACY:           mPrefs = TDPref.makeAccuracyPrefs( this, hlp ); break;
      case TDPrefCat.PREF_LOCATION:           mPrefs = TDPref.makeLocationPrefs( this, hlp ); break;
      case TDPrefCat.PREF_PLOT_SCREEN:        mPrefs = TDPref.makeScreenPrefs(   this, hlp ); break;
      case TDPrefCat.PREF_TOOL_LINE:          mPrefs = TDPref.makeLinePrefs(     this, hlp ); break;
      case TDPrefCat.PREF_TOOL_POINT:         mPrefs = TDPref.makePointPrefs(    this, hlp ); break;
      // case TDPrefCat.PREF_PLOT_WALLS:         mPrefs = TDPref.makeWallsPrefs(    this, hlp ); break; // AUTOWALLS
      case TDPrefCat.PREF_PLOT_DRAW:          mPrefs = TDPref.makeDrawPrefs(     this, hlp ); break;
      case TDPrefCat.PREF_PLOT_ERASE:         mPrefs = TDPref.makeErasePrefs(    this, hlp ); break;
      case TDPrefCat.PREF_PLOT_EDIT:          mPrefs = TDPref.makeEditPrefs(     this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_CAVE3D:    mPrefs = TDPref.makeCave3DPrefs(   this, hlp ); break;
      case TDPrefCat.PREF_DEM3D:              mPrefs = TDPref.makeDem3DPrefs(    this, hlp ); break;
      case TDPrefCat.PREF_WALLS3D:            mPrefs = TDPref.makeWalls3DPrefs(  this, hlp ); break;
      case TDPrefCat.PREF_CATEGORY_GEEK:      mPrefs = TDPref.makeGeekPrefs(     this, hlp ); break;
      case TDPrefCat.PREF_GEEK_SHOT:          mPrefs = TDPref.makeGeekShotPrefs( this, hlp ); break;
      case TDPrefCat.PREF_GEEK_SPLAY:         mPrefs = TDPref.makeGeekSplayPrefs( this, hlp ); break;
      case TDPrefCat.PREF_GEEK_PLOT:          mPrefs = TDPref.makeGeekPlotPrefs( this, hlp ); break;
      case TDPrefCat.PREF_GEEK_LINE:          mPrefs = TDPref.makeGeekLinePrefs( this, hlp ); break;
      case TDPrefCat.PREF_GEEK_DEVICE:        mPrefs = TDPref.makeGeekDevicePrefs( this, hlp ); break;
      case TDPrefCat.PREF_GEEK_IMPORT:        mPrefs = TDPref.makeGeekImportPrefs( this, hlp ); break;
      case TDPrefCat.PREF_GEEK_SKETCH:        mPrefs = TDPref.makeSketchPrefs(   this, hlp ); break; // FIXME_SKETCH_3D
      // case TDPrefCat.PREF_CATEGORY_LOG:       mPrefs = TDPref.makeLogPrefs(      this, hlp ); break; // NO_LOGS
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
    // TDLog.v( "Level " + TDLevel.mLevel + " found " + cnt + " prefs" );
    if ( cnt == 0 ) return false;

    if (mPrefCategory == TDPrefCat.PREF_CATEGORY_ALL ) {
      // mCwdPref = findPreference( "DISTOX_CWD" );
      // if ( mCwdPref != null ) {
      //   mCwdPref.setValue( TDInstance.cwd );
      //   View v = mCwdPref.getView();
      //   if ( v != null ) {
      //     final Intent cwd_intent = new Intent( mCtx, com.topodroid.TDX.CWDActivity.class ); // this
      //     v.setOnClickListener( new OnClickListener() {
      //       @Override
      //       public void onClick( View v ) { startActivityForResult( cwd_intent, REQUEST_CWD ); }
      //     } );
      //   }
      // }
      if ( TDLevel.overAdvanced ) {
        TDPref export_settings = findPreference( "DISTOX_EXPORT_SETTINGS" );
        View v = export_settings.getView();
        if ( v != null ) {
          v.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( View v ) { startExportDialog( hlp.getSharedPrefs() ); }
          } );
        }
      }
      
      linkPreference( "DISTOX_SURVEY_PREF", TDPrefCat.PREF_CATEGORY_SURVEY );
      linkPreference( "DISTOX_PLOT_PREF",   TDPrefCat.PREF_CATEGORY_PLOT );
      linkPreference( "DISTOX_EXPORT_PREF", TDPrefCat.PREF_CATEGORY_EXPORT );
      linkPreference( "DISTOX_DEVICE_PREF", TDPrefCat.PREF_CATEGORY_DEVICE );
      linkPreference( "DISTOX_CAVE3D_PREF", TDPrefCat.PREF_CATEGORY_CAVE3D );
      linkPreference( "DISTOX_GEEK_PREF",   TDPrefCat.PREF_CATEGORY_GEEK );
      // TDLog.v( "PREF category ALL done");
    } else if (mPrefCategory == TDPrefCat.PREF_CATEGORY_IMPORT ) {
      mPtCmapPref = findPreference( "DISTOX_PT_CMAP" );
      if ( mPtCmapPref != null ) {
        View v = mPtCmapPref.getView();
	if ( v != null ) {
          final Intent pt_intent = new Intent( mCtx, com.topodroid.TDX.PtCmapActivity.class ); // this
          v.setOnClickListener( 
            new OnClickListener() {
              @Override
              public void onClick( View v ) { startActivityForResult( pt_intent, REQUEST_PT_CMAP ); }
          } );
	}
      }
    } else if (mPrefCategory == TDPrefCat.PREF_CATEGORY_EXPORT ) {
      linkPreference( "DISTOX_EXPORT_ENABLE_PREF",  TDPrefCat.PREF_CATEGORY_EXPORT_ENABLE );
      linkPreference( "DISTOX_EXPORT_IMPORT_PREF",  TDPrefCat.PREF_CATEGORY_IMPORT );
      linkPreference( "DISTOX_EXPORT_SVX_PREF",     TDPrefCat.PREF_CATEGORY_SVX );
      linkPreference( "DISTOX_EXPORT_TH_PREF",      TDPrefCat.PREF_CATEGORY_TH );
      linkPreference( "DISTOX_EXPORT_DAT_PREF",     TDPrefCat.PREF_CATEGORY_DAT );
      linkPreference( "DISTOX_EXPORT_CSX_PREF",     TDPrefCat.PREF_CATEGORY_CSX );
      linkPreference( "DISTOX_EXPORT_TRO_PREF",     TDPrefCat.PREF_CATEGORY_TRO );
      linkPreference( "DISTOX_EXPORT_SRV_PREF",     TDPrefCat.PREF_CATEGORY_SRV );
      linkPreference( "DISTOX_EXPORT_SVG_PREF",     TDPrefCat.PREF_CATEGORY_SVG );
      linkPreference( "DISTOX_EXPORT_SHP_PREF",     TDPrefCat.PREF_CATEGORY_SHP );
      linkPreference( "DISTOX_EXPORT_DXF_PREF",     TDPrefCat.PREF_CATEGORY_DXF );
      // linkPreference( "DISTOX_EXPORT_PNG_PREF",     TDPrefCat.PREF_CATEGORY_PNG ); // NO_PNG
      linkPreference( "DISTOX_EXPORT_GPX_PREF",     TDPrefCat.PREF_CATEGORY_GPX );
      linkPreference( "DISTOX_EXPORT_KML_PREF",     TDPrefCat.PREF_CATEGORY_KML );
      linkPreference( "DISTOX_EXPORT_CSV_PREF",     TDPrefCat.PREF_CATEGORY_CSV );
    } else if (mPrefCategory == TDPrefCat.PREF_CATEGORY_SURVEY ) {
      linkPreference( "DISTOX_LOCATION_SCREEN",     TDPrefCat.PREF_LOCATION );
      linkPreference( "DISTOX_ACCURACY_SCREEN",     TDPrefCat.PREF_ACCURACY );
      linkPreference( "DISTOX_SHOT_UNITS_SCREEN",   TDPrefCat.PREF_SHOT_UNITS );
      linkPreference( "DISTOX_SHOT_DATA_SCREEN",    TDPrefCat.PREF_SHOT_DATA );
    } else if (mPrefCategory == TDPrefCat.PREF_CATEGORY_PLOT ) {
      linkPreference( "DISTOX_PLOT_SCREEN",         TDPrefCat.PREF_PLOT_SCREEN );
      linkPreference( "DISTOX_TOOL_LINE",           TDPrefCat.PREF_TOOL_LINE );
      linkPreference( "DISTOX_TOOL_POINT",          TDPrefCat.PREF_TOOL_POINT );
    } else if (mPrefCategory == TDPrefCat.PREF_CATEGORY_DEVICE ) {
      linkPreference( "DISTOX_CALIB_PREF",          TDPrefCat.PREF_CATEGORY_CALIB );
    } else if (mPrefCategory == TDPrefCat.PREF_CATEGORY_CAVE3D ) {
      linkPreference( "DISTOX_DEM3D_PREF",          TDPrefCat.PREF_DEM3D );
      linkPreference( "DISTOX_WALLS3D_PREF",        TDPrefCat.PREF_WALLS3D );
    } else if (mPrefCategory == TDPrefCat.PREF_CATEGORY_GEEK ) {
      // TDLog.v("PREF link GEEK sub-categories");
      linkPreference( "DISTOX_GEEK_SHOT",           TDPrefCat.PREF_GEEK_SHOT );
      linkPreference( "DISTOX_GEEK_SPLAY",          TDPrefCat.PREF_GEEK_SPLAY );
      linkPreference( "DISTOX_GEEK_PLOT",           TDPrefCat.PREF_GEEK_PLOT );
      linkPreference( "DISTOX_GEEK_LINE",           TDPrefCat.PREF_GEEK_LINE );
      // linkPreference( "DISTOX_PLOT_WALLS",          TDPrefCat.PREF_PLOT_WALLS ); // AUTOWALLS
      linkPreference( "DISTOX_GEEK_DEVICE",         TDPrefCat.PREF_GEEK_DEVICE );
      linkPreference( "DISTOX_GEEK_IMPORT",         TDPrefCat.PREF_GEEK_IMPORT );
      linkPreference( "DISTOX_SKETCH_PREF",         TDPrefCat.PREF_GEEK_SKETCH ); // FIXME_SKETCH_3D
    } else if (mPrefCategory == TDPrefCat.PREF_GEEK_PLOT ) {
      mGraphPaperScalePref = findPreference( "DISTOX_GRAPH_PAPER_SCALE" );
      if ( mGraphPaperScalePref != null ) {
        // TDLog.v("GRAPH_PAPER setting " + TDSetting.mGraphPaperScale );
        mGraphPaperScalePref.setButtonValue( Integer.toString( TDSetting.mGraphPaperScale ) );
        View v = mGraphPaperScalePref.getView();
        if ( v != null ) {
          final Intent pt_intent = new Intent( mCtx, com.topodroid.TDX.GraphPaperScaleActivity.class ); // this
          v.setOnClickListener( 
            new OnClickListener() {
              @Override
              public void onClick( View v ) { startActivityForResult( pt_intent, REQUEST_GRAPH_PAPER_SCALE ); }
          } );
        }
      }
    } else if (mPrefCategory == TDPrefCat.PREF_GEEK_DEVICE ) {
      TDPref mBtAliasPref = findPreference( "DISTOX_BT_ALIAS" );
      if ( mBtAliasPref != null ) {
        View v = mBtAliasPref.getView();
        if ( v != null ) {
          final Intent bt_alias_intent = new Intent( mCtx, com.topodroid.TDX.BtAliasActivity.class ); // this
          v.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( View v ) { startActivity( bt_alias_intent ); }
          } );
        }
      }
    }

    return true;
  }

  /** reload preferences - only for the general preference screen
   * @note called by TopoDroidApp.setLocale and ExportDialogSettings
   */
  public static void reloadPreferences() { if ( mPrefActivityAll != null ) mPrefActivityAll.doReloadPreferences(); }

  /** reload preferences (implementation) - only for the general preference screen
   */
  private void doReloadPreferences()
  {
    // TDLog.v( "reload prefs. cat " + mPrefCategory );
    if ( mPrefCategory == TDPrefCat.PREF_CATEGORY_ALL ) {
      if ( loadPreferences() ) {
        setTheTitle();
      }
      // mPrefActivityAll = null;
      // finish();
      // Intent intent = new Intent( mCtx, TDPrefActivity.class );
      // intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_ALL );
      // startActivity( intent );
      // linkPreference( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_ALL );
    } else if ( mPrefCategory == TDPrefCat.PREF_CATEGORY_SURVEY ) {
      if ( loadPreferences() ) {
        setTheTitle();
      }
      // mPrefActivitySurvey = null;
      // finish();
      // Intent intent = new Intent( mCtx, TDPrefActivity.class );
      // intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_SURVEY );
      // startActivity( intent );
      // // linkPreference( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_SURVEY );
    }
  }

  /** link a sub-screen preference
   * @param pref_name   preference name
   * @param category    preference category
   */
  private void linkPreference( String pref_name, int category )
  {
    // if ( pref_name == null ) return;
    // TDLog.v("link pref " + pref_name );
    final TDPref pref = findPreference( pref_name );
    if ( pref == null ) return;
    View v = pref.getView();
    if ( v == null ) return;
    final Intent intent = new Intent( mCtx, TDPrefActivity.class ); // this
    intent.putExtra( TDPrefCat.PREF_CATEGORY, category );
    v.setOnClickListener( 
      new View.OnClickListener() {
        @Override
        public void onClick( View v )
        {
	  // TDLog.v( "PREF click on " + pref.name + " category " + category );
          startActivity( intent );
        }
    } );
  }

  /** react to the result of a sub-activity
   * @param request    request code
   * @param result     result code (either OK or CANCEL)
   * @param intent     return intent
   */
  public void onActivityResult( int request, int result, Intent intent ) 
  {
    Bundle extras = (intent != null)? intent.getExtras() : null;
    switch ( request ) {
      // case REQUEST_CWD:
      //   if ( result == RESULT_OK && extras != null ) {
      //     String cwd = extras.getString( TDTag.TOPODROID_CWD );
      //     mCwdPref.setButtonValue( cwd );
      //     // TDLog.v("got CWD " + cwd );
      //   } else if ( result == RESULT_CANCELED ) {
      //     TDLog.e("could not set CWD");
      //   }
      //   break;
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
      case REQUEST_GRAPH_PAPER_SCALE:
        if ( result == RESULT_OK && extras != null ) {  
          int density = (int)(intent.getIntExtra( TDTag.TOPODROID_GRAPH_PAPER_SCALE, -1 ));
          // TDLog.v("GRAPH_PAPER set density " + density );
          TDSetting.mGraphPaperScale = density;
          mGraphPaperScalePref.setButtonValue( Integer.toString( density ) );
          // save setting 
          TDPrefHelper pref_hlp = new TDPrefHelper( mCtx ); // TopoDroidApp.mPrefHlp;
          SharedPreferences prefs = pref_hlp.getSharedPrefs();
          TDSetting.setPreference( prefs, "DISTOX_GRAPH_PAPER_SCALE", Integer.toString(density) );
        }
        break;
    }
  }

  // @Override
  // public void onConfigurationChanged( Configuration cfg )
  // {
  //   super.onConfigurationChanged( cfg );
  //   if ( cfg.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
  //     TDLog.e( "landscape" );
  //   } else if ( cfg.orientation == Configuration.ORIENTATION_PORTRAIT ) {
  //     TDLog.e( "portrait" );
  //   } else {
  //     TDLog.e( "unknown" );
  //   }
  // }

}
