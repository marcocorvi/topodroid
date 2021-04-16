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
import com.topodroid.common.SymbolType;

import android.graphics.Paint;
import android.graphics.Path;

public class Symbol implements SymbolInterface
{
  static final int W2D_NONE       = 0; // Walls roundtrip values
  static final int W2D_WALLS_SHP  = 1;
  static final int W2D_WALLS_SYM  = 2;
  static final int W2D_DETAIL_SHP = 3;
  static final int W2D_DETAIL_SYM = 4;

  boolean mEnabled;  //!< whether the symbol is enabled in the library
  String  mThName;   // therion name
  String  mGroup;    // group of this symbol (null if no group)
  // String  mFilename; // filename coincide with therion name
  protected String mDefaultOptions;

  int     mLevel;       // canvas levels [flag]
  int     mRoundTrip;

  /** default cstr
   */
  Symbol()
  {
    mEnabled = true;
    mThName  = null;
    mGroup   = null;
    mDefaultOptions = null;
    // mFilename = null;
    mLevel = DrawingLevel.LEVEL_DEFAULT;
    mRoundTrip = W2D_NONE;
  }

  // @param th_name   therion name (must be non-null)
  // @param group     symbol group (can be null)
  // @param filename  not used
  // @param rt        roundtrip (1 walls_shp, 2 walls_sym, 3 detail_shp, 4 detail_sym)
  Symbol( String th_name, String group, String filename, int rt ) 
  { 
    mEnabled  = true;
    mThName   = th_name;
    mGroup    = group;
    mDefaultOptions = null;
    // mFilename = filename;
    mLevel = DrawingLevel.LEVEL_DEFAULT;
    mRoundTrip = rt;
  }

  /** cstr 
   * @param enabled  whether the symbol is enabled
   */
  Symbol(boolean enabled ) 
  { 
    mEnabled  = enabled; 
    mThName   = null;
    mGroup    = null;
    mDefaultOptions = null;
    // mFilename = null;
    mLevel = DrawingLevel.LEVEL_DEFAULT;
    mRoundTrip = W2D_NONE;
  }

  public String getDefaultOptions() { return mDefaultOptions; }

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

  public Paint  getPaint()      { return null; } // Overridden
  public Path   getPath()       { return null; }
  public boolean isOrientable() { return false; }
  public int    getColor() 
  { 
    Paint paint = getPaint();
    return ( paint == null )? 0xffffffff : paint.getColor();
  }

  public boolean isEnabled() { return mEnabled; }
  public void setEnabled( boolean enabled ) { mEnabled = enabled; }
  public void toggleEnabled() { mEnabled = ! mEnabled; }

  public boolean setAngle( float angle ) { return false; }
  public int getAngle() { return 0; }

  static float sizeX( int type ) { return ( type == SymbolType.POINT )? TDSetting.mUnitIcons * 1.5f : TDSetting.mUnitIcons * 2.2f; }
  static float sizeY( int type ) { return ( type == SymbolType.POINT )? TDSetting.mUnitIcons * 1.5f : TDSetting.mUnitIcons * 1.7f; }

}
