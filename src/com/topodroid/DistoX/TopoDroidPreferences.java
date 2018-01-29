/* @file TopoDroidPreferences.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid options dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
// import android.preference.EditTextPreference;
// import android.preference.ListPreference;
// import android.view.Menu;
// import android.view.MenuItem;

import android.widget.ListAdapter;

import android.util.Log;

/**
 */
public class TopoDroidPreferences extends PreferenceActivity 
{
  static final int REQUEST_CWD         = 1;
  static final int REQUEST_PLOT_SCREEN = 2;
  static final int REQUEST_TOOL_SCREEN = 3;
  static final int REQUEST_LOCATION    = 4;
  static final int REQUEST_ACCURACY    = 5;
  static final int REQUEST_SHOT_DATA   = 6;
  static final int REQUEST_PT_CMAP     = 7;

  static final String PREF_CATEGORY = "PrefCategory";
  static final int PREF_CATEGORY_ALL    = 0;
  static final int PREF_CATEGORY_SURVEY = 1;
  static final int PREF_CATEGORY_PLOT   = 2;
  static final int PREF_CATEGORY_CALIB  = 3;
  static final int PREF_CATEGORY_DEVICE = 4;
  static final int PREF_CATEGORY_SKETCH = 5;
  static final int PREF_CATEGORY_IMPORT_EXPORT = 6;

  static final int PREF_SHOT_DATA       = 7; 
  static final int PREF_SHOT_UNITS      = 8; 
  static final int PREF_ACCURACY        = 9; 
  static final int PREF_LOCATION        = 10; 
  static final int PREF_PLOT_SCREEN     = 11; 
  static final int PREF_TOOL_LINE       = 12; 
  static final int PREF_TOOL_POINT      = 13; 
  static final int PREF_PLOT_WALLS      = 14; 
  static final int PREF_PLOT_DRAW       = 15; 
  static final int PREF_PLOT_ERASE      = 16; 
  static final int PREF_PLOT_EDIT       = 17; 

  static final int PREF_CATEGORY_LOG    = 18; // this must be the last

  private int mPrefCategory = PREF_CATEGORY_ALL; // preference category

  Preference mCwdPreference;
  Preference mPtCmapPreference;

  TopoDroidApp mApp;

  @Override
  public void onDestroy( )
  {
    super.onDestroy();
    // if (mPrefCategory == PREF_CATEGORY_ALL ) { mApp.mPrefActivity = null; }
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    mApp = (TopoDroidApp) getApplication();

    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      mPrefCategory = extras.getInt( PREF_CATEGORY );
      if ( mPrefCategory < PREF_CATEGORY_ALL || mPrefCategory > PREF_CATEGORY_LOG ) {
        mPrefCategory = PREF_CATEGORY_ALL;
      }
    }

    // Log.v("DistoX", "Pref create. category " + mPrefCategory + " level " + TDSetting.mActivityLevel );

    if (mPrefCategory == PREF_CATEGORY_ALL ) { mApp.mPrefActivity = this; }

    if ( TDLevel.overTester ) {  // =======================  DEVELOPER
      switch ( mPrefCategory ) {
      case PREF_CATEGORY_SURVEY: addPreferencesFromResource(R.xml.prefs_3_survey); break;
      case PREF_CATEGORY_PLOT:   addPreferencesFromResource(R.xml.prefs_4_plot);
        // TODO handle ZOOM_CONTROLS separatedly
        break;
      case PREF_CATEGORY_CALIB:  addPreferencesFromResource(R.xml.prefs_5_calib); break;
      case PREF_CATEGORY_DEVICE: addPreferencesFromResource(R.xml.prefs_3_device);
        linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
        break;
      case PREF_CATEGORY_IMPORT_EXPORT: addPreferencesFromResource(R.xml.prefs_3_export); break;
      case PREF_SHOT_DATA:       addPreferencesFromResource(R.xml.prefs_3_shot_data);   break;
      case PREF_SHOT_UNITS:      addPreferencesFromResource(R.xml.prefs_3_shot_units);  break;
      case PREF_ACCURACY:        addPreferencesFromResource(R.xml.prefs_3_accuracy);    break;
      case PREF_LOCATION:        addPreferencesFromResource(R.xml.prefs_3_location);    break;
      case PREF_PLOT_SCREEN:     addPreferencesFromResource(R.xml.prefs_3_plot_screen); break;
      case PREF_TOOL_LINE:       addPreferencesFromResource(R.xml.prefs_3_tool_line);   break;
      case PREF_TOOL_POINT:      addPreferencesFromResource(R.xml.prefs_3_tool_point);  break;
      case PREF_PLOT_WALLS:      addPreferencesFromResource(R.xml.prefs_4_plot_walls);  break;
      case PREF_PLOT_DRAW:       addPreferencesFromResource(R.xml.prefs_plot_draw);     break;
      case PREF_PLOT_ERASE:      addPreferencesFromResource(R.xml.prefs_plot_erase);    break;
      case PREF_PLOT_EDIT:       addPreferencesFromResource(R.xml.prefs_plot_edit);     break;
      case PREF_CATEGORY_SKETCH: addPreferencesFromResource(R.xml.prefs_sketch);        break;
      case PREF_CATEGORY_LOG:    addPreferencesFromResource(R.xml.prefs_log);           break;
      default:                   addPreferencesFromResource(R.xml.prefs_5);             break;
      }
    } else if ( TDLevel.overExpert ) { // ----------------- TESTER
      switch ( mPrefCategory ) {
      case PREF_CATEGORY_SURVEY: addPreferencesFromResource(R.xml.prefs_3_survey); break;
      case PREF_CATEGORY_PLOT:   addPreferencesFromResource(R.xml.prefs_4_plot);
        // TODO handle ZOOM_CONTROLS separatedly
        break;
      case PREF_CATEGORY_CALIB:  addPreferencesFromResource(R.xml.prefs_4_calib); break;
      case PREF_CATEGORY_DEVICE: addPreferencesFromResource(R.xml.prefs_3_device);
        linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
        break;
      case PREF_CATEGORY_IMPORT_EXPORT: addPreferencesFromResource(R.xml.prefs_3_export); break;
      case PREF_SHOT_DATA:       addPreferencesFromResource(R.xml.prefs_3_shot_data);   break;
      case PREF_SHOT_UNITS:      addPreferencesFromResource(R.xml.prefs_3_shot_units);  break;
      case PREF_ACCURACY:        addPreferencesFromResource(R.xml.prefs_3_accuracy);    break;
      case PREF_LOCATION:        addPreferencesFromResource(R.xml.prefs_3_location);    break;
      case PREF_PLOT_SCREEN:     addPreferencesFromResource(R.xml.prefs_3_plot_screen); break;
      case PREF_TOOL_LINE:       addPreferencesFromResource(R.xml.prefs_3_tool_line);   break;
      case PREF_TOOL_POINT:      addPreferencesFromResource(R.xml.prefs_3_tool_point);  break;
      case PREF_PLOT_WALLS:      addPreferencesFromResource(R.xml.prefs_4_plot_walls);  break;
      case PREF_PLOT_DRAW:       addPreferencesFromResource(R.xml.prefs_plot_draw);     break;
      case PREF_PLOT_ERASE:      addPreferencesFromResource(R.xml.prefs_plot_erase);    break;
      case PREF_PLOT_EDIT:       addPreferencesFromResource(R.xml.prefs_plot_edit);     break;
      case PREF_CATEGORY_LOG:    addPreferencesFromResource(R.xml.prefs_log);           break;
      default:                   addPreferencesFromResource(R.xml.prefs_4);             break;
      }
    } else if ( TDLevel.overAdvanced ) { // ----------------- EXPERT
      switch ( mPrefCategory ) {
      case PREF_CATEGORY_SURVEY: addPreferencesFromResource(R.xml.prefs_3_survey); break;
      case PREF_CATEGORY_PLOT:   addPreferencesFromResource(R.xml.prefs_3_plot);
        // TODO handle ZOOM_CONTROLS separatedly
        break;
      case PREF_CATEGORY_CALIB:  addPreferencesFromResource(R.xml.prefs_3_calib); break;
      case PREF_CATEGORY_DEVICE: addPreferencesFromResource(R.xml.prefs_3_device);
        linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
        break;
      case PREF_CATEGORY_IMPORT_EXPORT: addPreferencesFromResource(R.xml.prefs_3_export); break;
      case PREF_SHOT_DATA:       addPreferencesFromResource(R.xml.prefs_3_shot_data);   break;
      case PREF_SHOT_UNITS:      addPreferencesFromResource(R.xml.prefs_3_shot_units);  break;
      case PREF_ACCURACY:        addPreferencesFromResource(R.xml.prefs_3_accuracy);    break;
      case PREF_LOCATION:        addPreferencesFromResource(R.xml.prefs_3_location);    break;
      case PREF_PLOT_SCREEN:     addPreferencesFromResource(R.xml.prefs_3_plot_screen); break;
      case PREF_TOOL_LINE:       addPreferencesFromResource(R.xml.prefs_3_tool_line);   break;
      case PREF_TOOL_POINT:      addPreferencesFromResource(R.xml.prefs_3_tool_point);  break;
      // case PREF_PLOT_WALLS:   addPreferencesFromResource(R.xml.prefs_n_plot_walls);  break;
      case PREF_PLOT_DRAW:       addPreferencesFromResource(R.xml.prefs_plot_draw);     break;
      case PREF_PLOT_ERASE:      addPreferencesFromResource(R.xml.prefs_plot_erase);    break;
      case PREF_PLOT_EDIT:       addPreferencesFromResource(R.xml.prefs_plot_edit);     break;
      case PREF_CATEGORY_LOG:    addPreferencesFromResource(R.xml.prefs_log);           break;
      default:                   addPreferencesFromResource(R.xml.prefs_3);             break;
      }

    } else if ( TDLevel.overNormal ) { // ---------------- ADVANCED
      switch ( mPrefCategory ) {
      case PREF_CATEGORY_SURVEY: addPreferencesFromResource(R.xml.prefs_2_survey); break;
      case PREF_CATEGORY_PLOT:   addPreferencesFromResource(R.xml.prefs_2_plot);
        // TODO handle ZOOM_CONTROLS separatedly
        break;
      case PREF_CATEGORY_CALIB:  addPreferencesFromResource(R.xml.prefs_2_calib); break;
      case PREF_CATEGORY_DEVICE: addPreferencesFromResource(R.xml.prefs_2_device);
        linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
        break;
      case PREF_CATEGORY_IMPORT_EXPORT: addPreferencesFromResource(R.xml.prefs_2_export); break;
      case PREF_SHOT_DATA:       addPreferencesFromResource(R.xml.prefs_2_shot_data);   break;
      case PREF_SHOT_UNITS:      addPreferencesFromResource(R.xml.prefs_2_shot_units);  break;
      case PREF_ACCURACY:        addPreferencesFromResource(R.xml.prefs_2_accuracy);    break;
      case PREF_LOCATION:        addPreferencesFromResource(R.xml.prefs_2_location);    break;
      case PREF_PLOT_SCREEN:     addPreferencesFromResource(R.xml.prefs_2_plot_screen); break;
      case PREF_TOOL_LINE:       addPreferencesFromResource(R.xml.prefs_2_tool_line);   break;
      case PREF_TOOL_POINT:      addPreferencesFromResource(R.xml.prefs_2_tool_point);  break;
      // case PREF_PLOT_WALLS:   addPreferencesFromResource(R.xml.prefs_n_plot_walls);  break;
      case PREF_PLOT_DRAW:       addPreferencesFromResource(R.xml.prefs_plot_draw);     break;
      case PREF_PLOT_ERASE:      addPreferencesFromResource(R.xml.prefs_plot_erase);    break;
      case PREF_PLOT_EDIT:       addPreferencesFromResource(R.xml.prefs_plot_edit);     break;
      // case PREF_CATEGORY_LOG: addPreferencesFromResource(R.xml.prefs_log);           break;
      default:                   addPreferencesFromResource(R.xml.prefs_2);             break;
      }
    } else if ( TDLevel.overBasic ) { // ------------------- NORMAL
      switch ( mPrefCategory ) {
      case PREF_CATEGORY_SURVEY: addPreferencesFromResource(R.xml.prefs_1_survey); break;
      case PREF_CATEGORY_PLOT:   addPreferencesFromResource(R.xml.prefs_1_plot);
        // TODO handle ZOOM_CONTROLS separatedly
        break;
      case PREF_CATEGORY_CALIB:  addPreferencesFromResource(R.xml.prefs_1_calib); break;
      case PREF_CATEGORY_DEVICE: addPreferencesFromResource(R.xml.prefs_1_device);
        linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
        break;
      case PREF_CATEGORY_IMPORT_EXPORT: addPreferencesFromResource(R.xml.prefs_1_export); break;
      case PREF_SHOT_DATA:       addPreferencesFromResource(R.xml.prefs_1_shot_data);   break;
      case PREF_SHOT_UNITS:      addPreferencesFromResource(R.xml.prefs_1_shot_units);  break;
      case PREF_ACCURACY:        addPreferencesFromResource(R.xml.prefs_1_accuracy);    break;
      case PREF_LOCATION:        addPreferencesFromResource(R.xml.prefs_1_location);    break;
      case PREF_PLOT_SCREEN:     addPreferencesFromResource(R.xml.prefs_1_plot_screen); break;
      case PREF_TOOL_LINE:       addPreferencesFromResource(R.xml.prefs_1_tool_line);   break;
      case PREF_TOOL_POINT:      addPreferencesFromResource(R.xml.prefs_1_tool_point);  break;
      // case PREF_PLOT_WALLS:   addPreferencesFromResource(R.xml.prefs_n_plot_walls);  break;
      case PREF_PLOT_DRAW:       addPreferencesFromResource(R.xml.prefs_plot_draw);     break;
      case PREF_PLOT_ERASE:      addPreferencesFromResource(R.xml.prefs_plot_erase);    break;
      case PREF_PLOT_EDIT:       addPreferencesFromResource(R.xml.prefs_plot_edit);     break;
      // case PREF_CATEGORY_LOG: addPreferencesFromResource(R.xml.prefs_log);           break;
      default:                   addPreferencesFromResource(R.xml.prefs_1);             break;
      }
    } else { // ---------------- BASIC
      switch ( mPrefCategory ) {
      case PREF_CATEGORY_SURVEY: addPreferencesFromResource(R.xml.prefs_0_survey); break;
      case PREF_CATEGORY_PLOT:   addPreferencesFromResource(R.xml.prefs_0_plot);
        // TODO handle ZOOM_CONTROLS separatedly
        break;
      case PREF_CATEGORY_CALIB:  addPreferencesFromResource(R.xml.prefs_0_calib); break;
      case PREF_CATEGORY_DEVICE: addPreferencesFromResource(R.xml.prefs_0_device);
        linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
        break;
      case PREF_CATEGORY_IMPORT_EXPORT: addPreferencesFromResource(R.xml.prefs_0_export); break;
      case PREF_SHOT_DATA:       addPreferencesFromResource(R.xml.prefs_0_shot_data);   break;
      case PREF_SHOT_UNITS:      addPreferencesFromResource(R.xml.prefs_0_shot_units);  break;
      // case PREF_ACCURACY:     addPreferencesFromResource(R.xml.prefs_0_accuracy);    break;
      case PREF_LOCATION:        addPreferencesFromResource(R.xml.prefs_0_location);    break;
      case PREF_PLOT_SCREEN:     addPreferencesFromResource(R.xml.prefs_0_plot_screen); break;
      case PREF_TOOL_LINE:       addPreferencesFromResource(R.xml.prefs_0_tool_line);   break;
      case PREF_TOOL_POINT:      addPreferencesFromResource(R.xml.prefs_0_tool_point);  break;
      // case PREF_PLOT_WALLS:   addPreferencesFromResource(R.xml.prefs_n_plot_walls);  break;
      case PREF_PLOT_DRAW:       addPreferencesFromResource(R.xml.prefs_plot_draw);     break;
      case PREF_PLOT_ERASE:      addPreferencesFromResource(R.xml.prefs_plot_erase);    break;
      // case PREF_PLOT_EDIT:    addPreferencesFromResource(R.xml.prefs_plot_edit);     break;
      // case PREF_CATEGORY_LOG: addPreferencesFromResource(R.xml.prefs_log);           break;
      default:                   addPreferencesFromResource(R.xml.prefs_0);             break;
      }
    }

    if (mPrefCategory == PREF_CATEGORY_ALL ) {
      final Intent cwd_intent = new Intent( this, CWDActivity.class );
      mCwdPreference = (Preference) findPreference( "DISTOX_CWD" );
      if ( mCwdPreference != null ) {
        mCwdPreference.setSummary( mApp.mCWD );
        mCwdPreference.setOnPreferenceClickListener( 
          new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick( Preference pref ) 
            {
              startActivityForResult( cwd_intent, REQUEST_CWD );
              return true;
            }
        } );
      }
      linkPreference( "DISTOX_SURVEY_PREF", PREF_CATEGORY_SURVEY );
      linkPreference( "DISTOX_PLOT_PREF", PREF_CATEGORY_PLOT );
      linkPreference( "DISTOX_IMPORT_EXPORT_PREF", PREF_CATEGORY_IMPORT_EXPORT );
      linkPreference( "DISTOX_SKETCH_PREF", PREF_CATEGORY_SKETCH );
      linkPreference( "DISTOX_DEVICE_PREF", PREF_CATEGORY_DEVICE );
      // linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
    }

    if (mPrefCategory == PREF_CATEGORY_IMPORT_EXPORT ) {
      final Intent pt_intent = new Intent( this, PtCmapActivity.class );
      mPtCmapPreference = (Preference) findPreference( "DISTOX_PT_CMAP" );
      if ( mPtCmapPreference != null ) {
        mPtCmapPreference.setOnPreferenceClickListener( 
          new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick( Preference pref ) 
            {
              startActivityForResult( pt_intent, REQUEST_PT_CMAP );
              return true;
            }
        } );
      }
    }

    if (mPrefCategory == PREF_CATEGORY_PLOT ) {
      linkPreference( "DISTOX_PLOT_SCREEN", PREF_PLOT_SCREEN );
      linkPreference( "DISTOX_TOOL_LINE",   PREF_TOOL_LINE );
      linkPreference( "DISTOX_TOOL_POINT",  PREF_TOOL_POINT );
      // if ( TDLevel.overExpert ) 
        linkPreference( "DISTOX_PLOT_WALLS",  PREF_PLOT_WALLS );
    }

    if (mPrefCategory == PREF_CATEGORY_SURVEY ) {
      // if ( TDLevel.overBasic ) 
        linkPreference( "DISTOX_LOCATION_SCREEN", PREF_LOCATION );
      // if ( TDLevel.overNormal ) 
        linkPreference( "DISTOX_ACCURACY_SCREEN", PREF_ACCURACY );
      linkPreference( "DISTOX_SHOT_UNITS_SCREEN", PREF_SHOT_UNITS );
      linkPreference( "DISTOX_SHOT_DATA_SCREEN", PREF_SHOT_DATA );
    }
  }

  void reloadPreferences()
  {
    if (mPrefCategory != PREF_CATEGORY_ALL ) return;
    mApp.mPrefActivity = null;
    finish();
    Intent intent = new Intent( mApp, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
    startActivity( intent );
  }

  private void linkPreference( String pref_name, int category )
  {
    // if ( pref_name == null ) return;
    Preference pref = findPreference( pref_name );
    if ( pref == null ) return;
    final Intent intent = new Intent( this, TopoDroidPreferences.class );
    intent.putExtra( PREF_CATEGORY, category );
    pref.setOnPreferenceClickListener( 
      new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick( Preference pref ) 
        {
          startActivity( intent );
          return true;
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
          mCwdPreference.setSummary( cwd );
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
          // mPtCmapPreference.
        }
        break;
    }
  }

  // public void setPreferenceText( String key, String text )
  // {
  //   EditPreference ep = (EditPreference) findPreference( key );
  //   if ( ep != null ) ep.setText( text );
  // }
  // public void setPreferenceText( String key, int i )   { setPreferenceText( key, Integer.toString(i) ); }
  // public void setPreferenceText( String key, float f ) { setPreferenceText( key, Float.toString(f) ); }

}
