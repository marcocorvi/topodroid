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
import com.topodroid.utils.TDLog;

import android.graphics.Paint;
import android.graphics.Path;

public class Symbol implements SymbolInterface
{
  private static boolean hasSizeFactors = false;
  private static float sizeFactorXP = 1.5f;
  private static float sizeFactorYP = 1.5f;
  private static float sizeFactorXL = 2.2f;
  private static float sizeFactorYL = 1.7f;

  static void setSizeFactors()
  { 
    if ( hasSizeFactors ) return;
    try {
      sizeFactorXP = Float.parseFloat( TDInstance.context.getResources().getString( R.string.size_factor_xp ) );
      sizeFactorYP = Float.parseFloat( TDInstance.context.getResources().getString( R.string.size_factor_yp ) );
      sizeFactorXL = Float.parseFloat( TDInstance.context.getResources().getString( R.string.size_factor_xl ) );
      sizeFactorYL = Float.parseFloat( TDInstance.context.getResources().getString( R.string.size_factor_yl ) );
    } catch ( NumberFormatException e ) {
      TDLog.v("SIZE FACTORS exception " + e.getMessage() );
    }
    hasSizeFactors = true;
  }

  public static final int W2D_NONE       = 0; // Walls roundtrip values
  public static final int W2D_WALLS_SHP  = 1;
  public static final int W2D_WALLS_SYM  = 2;
  public static final int W2D_DETAIL_SHP = 3;
  public static final int W2D_DETAIL_SYM = 4;

  boolean mEnabled;  //!< whether the symbol is enabled in the library
  private String  mThName;   // therion name
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
    setThName( th_name );
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

  // ------------------------------------------------------------------------

  public String getDefaultOptions() { return mDefaultOptions; }

  // SymbolInterface methods
  protected void setThName( String name )
  {
    if ( name == null ) return;
    if ( name.startsWith("u:") ) {
      if ( name.length() == 2 ) return;
      mThName = name.substring(2);
    } else {
      if ( name.length() == 0 ) return;
      mThName = name;
    }
  }
  public String getThName()           { return mThName; }
  boolean hasThName( String th_name ) { return mThName != null && mThName.equals( th_name ); } 
  boolean isThName( String name )     { return (name == null && mThName == null) || mThName.equals( name ); }
  boolean isThNameEmpty()             { return mThName == null || mThName.length() == 0; }

  String getGroup()                { return mGroup; }
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

  static float sizeX( int type ) { return ( type == SymbolType.POINT )? TDSetting.mUnitIcons * sizeFactorXP : TDSetting.mUnitIcons * sizeFactorXL; }
  static float sizeY( int type ) { return ( type == SymbolType.POINT )? TDSetting.mUnitIcons * sizeFactorYP : TDSetting.mUnitIcons * sizeFactorYL; }

  static String deprefix_u( String name ) { return (name == null)? null : (name.startsWith("u:"))? name.substring(2) : name; }

}
