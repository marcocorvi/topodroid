/* @file TDLock.java
 *
 * @author marco corvi
 * @date nov 2026
 *
 * @brief TopoDroid survey/tdconfig lock
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.util;

import com.topodroid.TDX.TopoDroidApp;

public class TDLock
{
  private final static String SURVEY_LOCK = "survey_lock";
  private final static String TDCONFIG_LOCK = "tdconfig_lock";

  /** add a name to the lock
   * @param name   name to add
   * @param lock   lock name
   * @return false if the name is already in the lock
   */
  private static boolean addLock( String name, String lock ) 
  {
    if ( name == null || name.length() == 0 ) return true;
    String old_lock = TopoDroidApp.mData.getValue( lock );
    if ( old_lock != null && old_lock.length() > 0 ) {
      String[] locks = old_lock.split(" ");
      for ( String lck : locks ) if ( lck.equals( name ) ) return false;
      name = name + " " + old_lock;
    }
    TopoDroidApp.mData.setValue( lock, name );
    return true;
  }

  /** drop a name from the lock
   * @param name   name to drop
   * @param lock   lock name
   * @return false if the name was not in the lock
   */
  private static boolean dropLock( String name, String lock ) 
  {
    if ( name == null || name.length() == 0 ) return true;
    String old_lock = TopoDroidApp.mData.getValue( lock );
    if ( old_lock == null || old_lock.length() == 0 ) return false;
    boolean ret = false;
    boolean space = false;
    String[] locks = old_lock.split(" ");
    StringBuilder sb = new StringBuilder();
    for ( String lck : locks ) {
      if ( lck.equals( name ) ) {
        ret = true;
      } else { 
        if ( space ) sb.append(" ");
        sb.append( lck );
        space = true;
      }
    }
    if ( ret ) TopoDroidApp.mData.setValue( lock, sb.toString() );
    return ret;
  }

  private static void clearLock( String lock ) { TopoDroidApp.mData.setValue( lock, "" ); }


  /** set the name of the open survey into the lock
   * @param name   name of the survey
   * @return the previous name of the lock or null if the survey is already in the lock
   */
  private static boolean acquireSurveyLock( String name ) { return true /* addLock( name, SURVEY_LOCK ) */ ; } 

  /** set the name of the open tdconfig into the lock
   * @param name   name of the tdconfig
   */
  private static boolean acquireTdConfigLock( String name ) { return true /* addLock( name, TDCONFIG_LOCK ) */; }

  // ----------------------

  public static void waitSurveyLock( String name ) { while ( ! acquireSurveyLock( name ) ) TDUtil.slowDown( 100 ); }
  public static void clearSurveyLock() { /* clearLock( SURVEY_LOCK ); */ }
  public static boolean releaseSurveyLock( String name ) { return true /* dropLock( name, SURVEY_LOCK ) */ ; } 

  public static void waitTdConfigLock( String name ) { while ( ! acquireTdConfigLock( name ) ) TDUtil.slowDown( 100 ); }
  public static void clearTdConfigLock() { /* clearLock( TDCONFIG_LOCK ); */ }
  public static boolean releaseTdConfigLock( String name ) { return true /* dropLock( name, TDCONFI_LOCK ) */ ; } 

}
