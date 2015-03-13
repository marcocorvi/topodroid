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
 * CHANGES
 * 20120516 length and angle units
 * 20120521 converted from DistoXPreferences.java
 * 20120715 loading only per-category preferences
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

  static final String PREF_CATEGORY = "PrefCategory";
  static final int PREF_CATEGORY_ALL    = 0;
  static final int PREF_CATEGORY_SURVEY = 1;
  static final int PREF_CATEGORY_PLOT   = 2;
  static final int PREF_CATEGORY_CALIB  = 3;
  static final int PREF_CATEGORY_DEVICE = 4;
  static final int PREF_CATEGORY_SKETCH = 5;

  static final int PREF_SHOT_DATA       = 6; 
  static final int PREF_SHOT_UNITS      = 7; 
  static final int PREF_ACCURACY        = 8; 
  static final int PREF_LOCATION        = 9; 
  static final int PREF_PLOT_SCREEN     = 10; 
  static final int PREF_TOOL_SCREEN     = 11; 

  static final int PREF_CATEGORY_LOG    = 12; // this must be the last

  private int mPrefCategory = PREF_CATEGORY_ALL; // preference category

  Preference mCwdPreference;

  TopoDroidApp mApp;

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

    // Log.v("DistoX", "Pref cat " + mPrefCategory );

    if (mPrefCategory == PREF_CATEGORY_SURVEY ) {
      addPreferencesFromResource(R.xml.preferences_survey);
    } else if (mPrefCategory == PREF_CATEGORY_PLOT ) {
      addPreferencesFromResource(R.xml.preferences_plot);
    } else if (mPrefCategory == PREF_CATEGORY_CALIB ) {
      addPreferencesFromResource(R.xml.preferences_calib);
    } else if (mPrefCategory == PREF_CATEGORY_DEVICE ) {
      addPreferencesFromResource(R.xml.preferences_device);
    } else if (mPrefCategory == PREF_CATEGORY_SKETCH ) {
      addPreferencesFromResource(R.xml.preferences_sketch);
    } else if (mPrefCategory == PREF_SHOT_DATA ) {
      addPreferencesFromResource(R.xml.preferences_shot_data);
    } else if (mPrefCategory == PREF_SHOT_UNITS ) {
      addPreferencesFromResource(R.xml.preferences_shot_units);
    } else if (mPrefCategory == PREF_ACCURACY ) {
      addPreferencesFromResource(R.xml.preferences_accuracy);
    } else if (mPrefCategory == PREF_LOCATION ) {
      addPreferencesFromResource(R.xml.preferences_location);
    } else if (mPrefCategory == PREF_PLOT_SCREEN ) {
      addPreferencesFromResource(R.xml.preferences_plot_screen);
    } else if (mPrefCategory == PREF_TOOL_SCREEN ) {
      addPreferencesFromResource(R.xml.preferences_tool_screen);
    } else if (mPrefCategory == PREF_CATEGORY_LOG ) {
      addPreferencesFromResource(R.xml.preferences_log);
    } else {
      addPreferencesFromResource(R.xml.preferences);
    }

    if (mPrefCategory == PREF_CATEGORY_ALL ) {
      final Intent cwd_intent = new Intent( this, CWDActivity.class );
      mCwdPreference = (Preference) findPreference( "DISTOX_CWD" );
      mCwdPreference.setOnPreferenceClickListener( 
        new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick( Preference pref ) 
          {
            startActivityForResult( cwd_intent, REQUEST_CWD );
            return true;
          }
        } );

      linkPreference( "DISTOX_SURVEY_PREF", PREF_CATEGORY_SURVEY );
      linkPreference( "DISTOX_PLOT_PREF", PREF_CATEGORY_PLOT );
      linkPreference( "DISTOX_DEVICE_PREF", PREF_CATEGORY_DEVICE );
      linkPreference( "DISTOX_CALIB_PREF", PREF_CATEGORY_CALIB );
    }

    if (mPrefCategory == PREF_CATEGORY_PLOT ) {
      linkPreference( "DISTOX_PLOT_SCREEN", PREF_PLOT_SCREEN );
      linkPreference( "DISTOX_TOOL_SCREEN", PREF_TOOL_SCREEN );
    }

    if (mPrefCategory == PREF_CATEGORY_SURVEY ) {
      linkPreference( "DISTOX_LOCATION_SCREEN", PREF_LOCATION );
      linkPreference( "DISTOX_ACCURACY_SCREEN", PREF_ACCURACY );
      linkPreference( "DISTOX_SHOT_UNITS_SCREEN", PREF_SHOT_UNITS );
      linkPreference( "DISTOX_SHOT_DATA_SCREEN", PREF_SHOT_DATA );

    }
  }

  private void linkPreference( String pref_name, int category )
  {
    final Intent intent = new Intent( this, TopoDroidPreferences.class );
    intent.putExtra( PREF_CATEGORY, category );
    ((Preference) findPreference( pref_name )).setOnPreferenceClickListener( 
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
        if ( extras != null ) {
          String cwd = extras.getString( TopoDroidApp.TOPODROID_CWD );
          mCwdPreference.setSummary( cwd );
          // Log.v("DistoX", "got CWD " + cwd );
        }
        break;
      case REQUEST_PLOT_SCREEN:
      case REQUEST_TOOL_SCREEN:
      case REQUEST_LOCATION:
      case REQUEST_ACCURACY:
      case REQUEST_SHOT_DATA:
        break;
    }
  }

}
