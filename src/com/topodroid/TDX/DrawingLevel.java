/* @file DrawingLevel.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing canvas levels
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *  @note this is actually DrawingUtilPortrait.java as the Landscape is never used
 *        and it is made all static (state-less)
 */
package com.topodroid.TDX;

import com.topodroid.prefs.TDSetting;

class DrawingLevel
{
  static final int LEVEL_BASE   =   1;
  static final int LEVEL_FLOOR  =   2;  // soil features
  static final int LEVEL_FILL   =   4;  // deposits, fillings
  static final int LEVEL_CEIL   =   8;  // ceiling
  static final int LEVEL_ARTI   =  16;  // man made
  // static final int LEVEL_FORM   =  32;  // symbolic 
  // static final int LEVEL_WATER  =  64;
  // static final int LEVEL_TEXT   = 128;  // text

  static final int LEVEL_USER   =  1;
  static final int LEVEL_LABEL  = 17;
  static final int LEVEL_WALL   = 31;
  static final int LEVEL_WATER  =  7;

  static final int LEVEL_ANY    = 31;
  static final int LEVEL_DEFAULT  = 1;

  static private int mDisplayLevel = DrawingLevel.LEVEL_ANY;

  /** set the current display level
   * @param level  current display level
   */
  static void setDisplayLevel( int level ) { mDisplayLevel = level; }

  /** @return the current display level
   */
  static int getDisplayLevel( ) { return mDisplayLevel; }

  /** @return true if the level is visible
   * @param level  display level
   */
  static boolean isVisible( int level ) { return (level & mDisplayLevel ) != 0; }

  /** @return true is the drawing path is visible according to the display level
   * @param path   drawing path
   */
  static  boolean isLevelVisible( DrawingPath path )
  {
    if ( TDSetting.mWithLevels == 0 || path == null ) return true; // visibility is filtered only if path is non-null
    if ( TDSetting.mWithLevels == 1 ) {
      int level = 0xff;
      if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
        level = BrushManager.getPointLevel( ((DrawingPointPath)path).mPointType );
      } else if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
        level = BrushManager.getLineLevel( ((DrawingLinePath)path).mLineType );
      } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
        level = BrushManager.getAreaLevel( ((DrawingAreaPath)path).mAreaType );
      }
      return isVisible( level ); 
    }
    // TDSetting.mWithLevels == 2
    return isVisible( path.mLevel ); 
  }
}

