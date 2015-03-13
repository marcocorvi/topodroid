/** @file NumSurveyPoint.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created fron DistoXNum
 */
package com.topodroid.DistoX;

public class NumSurveyPoint
{
  public float s; // south Y downward ( world coordinate )
  public float e; // east X rightward
  public float v; // Z vertical downward
  public float h; // horizontal rightward

  NumSurveyPoint()
  {
    s = 0.0f;
    e = 0.0f;
    v = 0.0f;
    h = 0.0f;
  }

  Vector toVector() { return new Vector(e,s,v); }

}
