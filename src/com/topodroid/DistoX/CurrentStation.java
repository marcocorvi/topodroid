/* @file CurrentStation.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid current station
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

class CurrentStation
{
  public String mName;
  public String mComment;

  CurrentStation( String name, String comment )
  {
    mName = name;
    mComment = (comment == null)? "" : comment;
  }

  public String toString() 
  { 
    return mName + " " + mComment;
  }
}
