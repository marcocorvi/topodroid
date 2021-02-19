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
package com.topodroid.num;

import com.topodroid.math.TDVector;

public class NumSurveyPoint
{
  public float s; // south Y downward ( world coordinate )
  public float e; // east X rightward
  public float v; // Z vertical downward
  public float h; // horizontal rightward

  // ########## geolocalized 
  public double gs; 
  public double ge; 
  public double gv; 

  NumSurveyPoint()
  {
    s = e = v = h = 0.0f;
    gs = ge = gv = 0;
  }

  public TDVector toVector() { return new TDVector( (float)e, (float)s, (float)v); }

}

