/* @file MyEditPreferences.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid option value
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.content.SharedPreferences;
// import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
// import android.preference.Preference.OnPreferenceChangeListener;

// import android.util.Log;

/**
 * @note this class must be public
 */
public class MyEditPreference extends EditTextPreference
{
  SharedPreferences sp;

  public MyEditPreference( Context c, AttributeSet a )
  {
    super(c,a);
    init();
    sp = PreferenceManager.getDefaultSharedPreferences( c );
  }

  public MyEditPreference( Context c )
  {
    super( c );
    init();
    sp = PreferenceManager.getDefaultSharedPreferences( c );
  }

  private void init()
  {
    setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange( Preference p, Object v ) 
      {
        String value = (String)v;
        String new_value = TDSetting.enforcePreferenceBounds( p.getKey(), value );
        // Log.v("DistoX", p.getKey() + ": value " + ((String)v) + " -> " + new_value + " text " + getText() );
        // if ( ! new_value.equals( value ) )
        {
          SharedPreferences.Editor editor = sp.edit();
          editor.putString( p.getKey(), new_value );
          editor.commit();
          EditTextPreference ep = (EditTextPreference)p;
          ep.setSummary( new_value );
          ep.setText( new_value );
        }
        return false; // state of preference has already been updated
      }
    } );
  }

  @Override
  public CharSequence getSummary() { return super.getText(); }
}

