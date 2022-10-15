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
package com.topodroid.TDX;

class DrawingOutlinePath
{
  private String mScrapName;  // scrap name
  DrawingLinePath mPath;

  /** cstr
   * @param name   scrap name
   * @param path   outline path
   */ 
  DrawingOutlinePath( String name, DrawingLinePath path )
  {
    mScrapName = name;
    mPath  = path;
  }

  // DEBUG
  // String getScrapName() { return (mScrapName != null )? mScrapName : "none"; }

  /** @return true if the given name is the scrap name
   * @param name   given name
   */
  boolean isScrapName( String name ) { return mScrapName.equals( name ); }

}

