/* @file TDPrefHelper.java
 *
 * @author marco corvi
 * @date aug 2018
 *
 * @brief TopoDroid options helper
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.prefs;

import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TDandroid;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Context;

public class TDPrefHelper
{
  private SharedPreferences mPrefs;

  public TDPrefHelper( Context ctx )
  {
    mPrefs = PreferenceManager.getDefaultSharedPreferences( ctx );
  }

  SharedPreferences getSharedPrefs() { return mPrefs; }

  public String getString( String name, String def_value )    { return mPrefs.getString( name, def_value ); }
  public boolean getBoolean( String name, boolean def_value ) { return mPrefs.getBoolean( name, def_value ); }

  // ---------------------------------------------------------------
  // static update functions

  public static void update( String name, String value )
  {
    // TDLog.v( "TDPrefHelper set pref " + name + " " + value );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putString( name, value );
    TDandroid.applyEditor( editor );
  }

  public static void update( String name1, String value1, String name2, String value2 )
  {
    // TDLog.v( "TDPrefHelper set pref " + name1 + " " + value1 + "   " + name2 + " " + value2 );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putString( name1, value1 );
    editor.putString( name2, value2 );
    TDandroid.applyEditor( editor );
  }

  public static void update( String name1, String value1, String name2, String value2, String name3, String value3 )
  {
    // TDLog.v( "TDPrefHelper set pref " + name1 + " " + value1 + "   " + name2 + " " + value2 + "   " + name3 + " " + value3 );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putString( name1, value1 );
    editor.putString( name2, value2 );
    editor.putString( name3, value3 );
    TDandroid.applyEditor( editor );
  }

  // unused
  // public static void update( String[] name, String[] value )
  // {
  //   TDLog.v( "TDPrefHelper set pref " + name[0] + " " + value[0] + " ... ");
  //   SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
  //   for ( int k = 0; k < name.length; ++k ) {
  //     editor.putString( name[k], value[k] );
  //   }
  //   TDandroid.applyEditor( editor );
  // }

  public static void update( String name, boolean value )
  {
    // TDLog.v( "TDPrefHelper set b-pref " + name + " " + value );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putBoolean( name, value );
    TDandroid.applyEditor( editor );
  }

  public static void update( String name, long value )
  {
    // TDLog.v( "TDPrefHelper set l-pref " + name + " " + value );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putLong( name, value );
    TDandroid.applyEditor( editor );
  }

}
