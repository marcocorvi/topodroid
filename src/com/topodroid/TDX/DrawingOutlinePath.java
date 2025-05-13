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
  private String mScrapName;  // scrap name of the xsection
  DrawingLinePath mPath;
  private int mScrapId;       // scrap index of the outline (non-negative)

  /** cstr
   * @param name     xsection scrap name
   * @param path     outline path
   * @param scrap_id ID of the scrap of the section point
   */ 
  DrawingOutlinePath( String name, DrawingLinePath path, int scrap_id )
  {
    mScrapName = name;
    mPath  = path;
    mScrapId = scrap_id;
  }

  // DEBUG
  // String getScrapName() { return (mScrapName != null )? mScrapName : "none"; }

  /** @return true if the given name is the scrap name
   * @param name   given name
   */
  boolean isScrapName( String name ) { return mScrapName.equals( name ); }

  /** @return true if the given ID is the scrap_id of the section point
   * @param id   given id
   */
  boolean isScrapId( int id ) { return id == mScrapId; }

}

