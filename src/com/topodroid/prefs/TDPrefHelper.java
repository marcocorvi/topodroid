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

  /** cstr
   * @param ctx  context
   */
  public TDPrefHelper( Context ctx )
  {
    mPrefs = PreferenceManager.getDefaultSharedPreferences( ctx );
  }

  /** @return the shared preferences
   */
  SharedPreferences getSharedPrefs() { return mPrefs; }

  /** @return the value of a string preference, or the provided default value
   * @param name      preference name
   * @param def_value preference default value
   */
  public String getString( String name, String def_value )    { return mPrefs.getString( name, def_value ); }

  /** @return the value of a boolean preference, or the provided default value
   * @param name      preference name
   * @param def_value preference default value
   */
  public boolean getBoolean( String name, boolean def_value ) { return mPrefs.getBoolean( name, def_value ); }

  // ---------------------------------------------------------------
  // static update functions

  /** update the value of a string preference
   * @param name  preference name
   * @param value preference value
   */
  public static void update( String name, String value )
  {
    // TDLog.v( "TDPrefHelper set pref " + name + " " + value );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putString( name, value );
    TDandroid.applyEditor( editor );
  }

  /** update the value of two string preference
   * @param name1  first preference name
   * @param value1 first preference value
   * @param name2  second preference name
   * @param value2 second preference value
   */
  public static void update( String name1, String value1, String name2, String value2 )
  {
    // TDLog.v( "TDPrefHelper set pref " + name1 + " " + value1 + "   " + name2 + " " + value2 );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putString( name1, value1 );
    editor.putString( name2, value2 );
    TDandroid.applyEditor( editor );
  }

  /** update the value of three string preference
   * @param name1  first preference name
   * @param value1 first preference value
   * @param name2  second preference name
   * @param value2 second preference value
   * @param name3  third preference name
   * @param value3 third preference value
   */
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

  /** update the value of a boolean preference
   * @param name  preference name
   * @param value preference value
   */
  public static void update( String name, boolean value )
  {
    // TDLog.v( "TDPrefHelper set b-pref " + name + " " + value );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putBoolean( name, value );
    TDandroid.applyEditor( editor );
  }

  /** update the value of a long preference
   * @param name  preference name
   * @param value preference value
   */
  public static void update( String name, long value )
  {
    // TDLog.v( "TDPrefHelper set l-pref " + name + " " + value );
    SharedPreferences.Editor editor = TDInstance.getPrefs().edit();
    editor.putLong( name, value );
    TDandroid.applyEditor( editor );
  }

}
