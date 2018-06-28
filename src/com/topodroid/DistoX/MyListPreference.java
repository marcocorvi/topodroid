/* @file MyListPreferences.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid option list
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.preference.Preference;
import android.preference.ListPreference;
import android.util.AttributeSet;
// import android.preference.Preference.OnPreferenceChangeListener;

/**
 * note this class must be public
 */
public class MyListPreference extends ListPreference
{
  public MyListPreference( Context c, AttributeSet a )
  {
    super(c,a);
    init();
  }

  public MyListPreference( Context c )
  {
    super( c );
    init();
  }

  private void init()
  {
    if ( getKey().equals("DISTOX_SURVEY_STATION" ) ) {
      if ( ! TDLevel.overBasic ) {
	setEntries( R.array.surveyStationsB );
      } else if ( ! TDLevel.overNormal ) {
	setEntries( R.array.surveyStationsN );
      } else if ( ! TDLevel.overAdvanced ) {
	setEntries( R.array.surveyStationsA );
      } else if ( ! TDLevel.overExpert ) {
	setEntries( R.array.surveyStationsE );
      }	
    }
    setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange( Preference p, Object v ) 
      {
        p.setSummary( getEntry() );
        return true;
      }
    } );
  }

  @Override
  public CharSequence getSummary() { return super.getEntry(); }
}

