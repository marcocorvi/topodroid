/* @file DrawingOutlinePath.java
 *
 * @author marco corvi
 * @date sept 2017
 *
 * @brief TopoDroid drawing: outline-path
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

class DrawingOutlinePath
{
  private String mScrapName;  // scrap name
  DrawingLinePath mPath;

  DrawingOutlinePath( String name, DrawingLinePath path )
  {
    mScrapName = name;
    mPath  = path;
  }

  // DEBUG
  String getScrapName() { return (mScrapName != null )? mScrapName : "none"; }

  boolean isScrapName( String name ) { return mScrapName.equals( name ); }

}

