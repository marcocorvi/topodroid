/* @file DLNSideList.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid Delaunay: side of the "convex" hull
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dln;


public class DLNSideList
{
  public DLNSide side;
  public DLNSideList next;
  public DLNSideList prev;

  DLNSideList( DLNSide s ) 
  { 
    side = s;
    next = null;
    prev = null;
  }
}
