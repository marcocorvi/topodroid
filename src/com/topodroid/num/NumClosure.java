/* @file NumClosure.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief TopoDroid closure error-string and loop-string
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

public class NumClosure
{
  String mDesc; // description
  String mLoop; // loop sequence

  NumClosure( String desc, String loop ) 
  {
    mDesc = desc;
    mLoop = loop;
  }

  public String getDescription() { return mDesc; }

  public String getLoop() { return mLoop; }
}
