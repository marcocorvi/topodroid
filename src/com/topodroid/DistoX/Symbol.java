/* @file Symbol.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing symbol: 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.prefs.TDSetting;

import android.graphics.Paint;
import android.graphics.Path;

class Symbol implements SymbolInterface
{
  static final int POINT = 1;
  static final int LINE  = 2;
  static final int AREA  = 3;

  boolean mEnabled;  //!< whether the symbol is enabled in the library
  String  mThName;   // therion name
  String  mGroup;    // group of this symbol (null if no group)
  // String  mFilename; // filename coincide with therion name
  int     mCsxLayer;    // cSurvey layer
  int     mCsxType;
  int     mCsxCategory;
  int     mCsxPen;      // pen (for lines)
  int     mCsxBrush;    // brush type (for areas)
  String  mCsx;         // clipart path 
  int     mLevel;       // canvas levels [flag]

  /** default cstr
   */
  Symbol()
  {
    mEnabled = true;
    mThName  = null;
    mGroup   = null;
    // mFilename = null;
    mCsxLayer = -1;
    mCsxType  = -1;
    mCsxCategory = -1;
    mCsxPen   = 1;     // default pen is "1"
    mCsxBrush = 1;     // default brush is "1"
    mCsx = null;
    mLevel = DrawingLevel.LEVEL_DEFAULT;
  }

  // @param th_name   therion name (must be non-null)
  // @param group     symbol group (can be null)
  // @param filename  not used
  Symbol( String th_name, String group, String filename ) 
  { 
    mEnabled  = true;
    mThName   = th_name;
    mGroup    = group;
    // mFilename = filename;
    mCsxLayer = -1;
    mCsxType  = -1;
    mCsxCategory = -1;
    mCsxPen   = 1;
    mCsxBrush = 1; 
    mCsx = null;
    mLevel = DrawingLevel.LEVEL_DEFAULT;
  }

  /** cstr 
   * @param enabled  whether the symbol is enabled
   */
  Symbol(boolean enabled ) 
  { 
    mEnabled  = enabled; 
    mThName   = null;
    mGroup    = null;
    // mFilename = null;
    mCsxLayer = -1;
    mCsxType  = -1;
    mCsxCategory = -1;
    mCsxPen   = 1;
    mCsxBrush = 1; 
    mCsx = null;
    mLevel = DrawingLevel.LEVEL_DEFAULT;
  }

  // SymbolInterface methods
  public String getThName()     { return mThName; }
  boolean hasThName( String th_name ) { return mThName != null && mThName.equals( th_name ); } 

  String getGroup()      { return mGroup; }
  boolean hasGroup( String group ) { return mGroup != null && mGroup.equals( group ); }

  // filename is not used - symbols are distinguished by their th_name
  // public String getFilename()   { return mThName.startsWith("u") ? mThName.substring(2) : mThName; /* mFilename; */ }

  public String getName()       { return "undefined"; }

  // FIXME this is half not-translatable: mGroup must be english
  public String getGroupName()   { return (mGroup == null)? "-" : mGroup; }

  public Paint  getPaint()      { return null; }
  public Path   getPath()       { return null; }
  public boolean isOrientable() { return false; }

  public boolean isEnabled() { return mEnabled; }
  public void setEnabled( boolean enabled ) { mEnabled = enabled; }
  public void toggleEnabled() { mEnabled = ! mEnabled; }

  public boolean setAngle( float angle ) { return false; }
  public int getAngle() { return 0; }

  static float sizeX( int type ) { return ( type == POINT )? TDSetting.mUnitIcons * 1.5f : TDSetting.mUnitIcons * 2.2f; }
  static float sizeY( int type ) { return ( type == POINT )? TDSetting.mUnitIcons * 1.5f : TDSetting.mUnitIcons * 1.7f; }

}
