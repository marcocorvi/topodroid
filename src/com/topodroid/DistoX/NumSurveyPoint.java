/* @file NumSurveyPoint.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class NumSurveyPoint
{
  float s; // south Y downward ( world coordinate )
  float e; // east X rightward
  float v; // Z vertical downward
  float h; // horizontal rightward

  NumSurveyPoint()
  {
    s = 0.0f;
    e = 0.0f;
    v = 0.0f;
    h = 0.0f;
  }

  Vector toVector() { return new Vector(e,s,v); }

}
