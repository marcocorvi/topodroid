/** @file SketchLine.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief TopoDroid sketch Line
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.TDX.Vector3D;

// import com.topodroid.utils.TDLog;

import java.util.ArrayList;

public class SketchLine
{
  public ArrayList< Vector3D > pts;
  String thname;
  public float red, green, blue;
  public float alpha;

  public SketchLine( String th, float r, float g, float b ) // LINES
  {
    pts = new ArrayList< Vector3D >();
    thname = th;
    red   = r;
    green = g;
    blue  = b;
    alpha = 1.0f;
  }

  public SketchLine( String th, float r, float g, float b, float a ) // AREAS
  {
    pts = new ArrayList< Vector3D >();
    thname = th;
    red   = r;
    green = g;
    blue  = b;
    alpha = a;
  }

  public void insertPoint( double x, double y, double z )
  {
    pts.add( new Vector3D( x, y, z ) );
  }

  public int size() { return pts.size(); }
}

