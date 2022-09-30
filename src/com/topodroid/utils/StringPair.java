/* @file StringPair.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid (unordered) pair of strings
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

public class StringPair
{
  String first;
  String second;

  /** cstr
   * @param f  first string
   * @param s  second string
   */
  public StringPair( String f, String s ) 
  {
    if ( f.compareTo(s) < 0 ) {
      first  = f;
      second = s;
    } else {
      first  = s;
      second = f;
    }
  }

  /** @return true if the string pair is equal to the given string pair
   * @param sp string pair
   */
  public boolean equals( StringPair sp ) { return first.equals( sp.first ) && second.equals( sp.second ); }

  /** @return true if the string pair is equal to the given strings
   * @param f  first string
   * @param s  second string
   */
  public boolean equals( String f, String s ) 
  {
    if ( f.compareTo(s) < 0 ) {
      return first.equals( f ) && second.equals( s );
    } else {
      return first.equals( s ) && second.equals( f );
    }
  }

  /** @return string presentation of the string pair
   */
  @Override
  public String toString() 
  {
    return "<" + first + " " + second + ">";
  }
}
