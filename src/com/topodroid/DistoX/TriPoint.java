/* @file TriPoint.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid centerline computation: trilateration vertex
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;
  
class TriPoint
{
  String name;
  // int n;       // nr legs with this point
  double x, y; // coords x = East, y = North
  double dx, dy; // coords variations
  boolean used;    // work flag

  TriPoint( String n )
  { 
    name = n;
    x = y = dx = dy = 0;
    used = false;
  }
}
