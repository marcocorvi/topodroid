/* @file MyEditPreferences.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid option value
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.preference.Preference.OnPreferenceChangeListener;

/**
 */
public class MyEditPreference extends EditTextPreference
{
  public MyEditPreference( Context c, AttributeSet a ) 
  {
    super(c,a);
    init();
  }

  public MyEditPreference( Context c )
  {
    super( c );
    init();
  }

  private void init()
  {
    setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange( Preference p, Object v ) 
      {
        p.setSummary( getText() );
        // String key = p.getKey();
        // TopoDroidLog.Log( TopoDroidLog.LOG_PREFS, "pref, key " + key + " val " + getText() );
        return true;
      }
    } );
  }

  @Override
  public CharSequence getSummary() { return super.getText(); }
}

