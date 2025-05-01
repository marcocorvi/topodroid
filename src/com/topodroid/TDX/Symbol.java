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
package com.topodroid.TDX;

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

  /** set the symbols size-factors from the resources
   */
  static void setSizeFactors()
  { 
    if ( hasSizeFactors ) return;
    try {
      sizeFactorXP = Float.parseFloat( TDInstance.context.getResources().getString( R.string.size_factor_xp ) );
      sizeFactorYP = Float.parseFloat( TDInstance.context.getResources().getString( R.string.size_factor_yp ) );
      sizeFactorXL = Float.parseFloat( TDInstance.context.getResources().getString( R.string.size_factor_xl ) );
      sizeFactorYL = Float.parseFloat( TDInstance.context.getResources().getString( R.string.size_factor_yl ) );
    } catch ( NumberFormatException e ) {
      TDLog.e("SIZE FACTORS exception " + e.getMessage() );
    }
    hasSizeFactors = true;
  }

  public static final int W2D_NONE       = 0; // Walls roundtrip values
  public static final int W2D_WALLS_SHP  = 1;
  public static final int W2D_WALLS_SYM  = 2;
  public static final int W2D_DETAIL_SHP = 3;
  public static final int W2D_DETAIL_SYM = 4;

  public static final int TYPE_NONE  = 0;
  public static final int TYPE_POINT = 1;
  public static final int TYPE_LINE  = 2;
  public static final int TYPE_AREA  = 3;
  // public static final int TYPE_EXTRA = 4;

  int     mSymbolType; // the Symbol class needs a field for the symbol type, POINT, LINE, AREA
  private boolean mEnabled;  //!< whether the symbol is enabled in the library
  private boolean mConfig;   // whether the symbol is enabled in the config
  private String  mThName;   // therion name
  private String  mThPrefix = null; // therion prefix ("u:" or null) 2023-01-31
  String  mGroup;    // group of this symbol (null if no group)
  // String  mFilename; // filename coincide with therion name
  protected String mDefaultOptions;

  int     mLevel;       // canvas levels [flag]
  int     mRoundTrip;

  // /** default cstr - UNUSED
  //  * @param s_type    symbol type
  //  */
  // Symbol( int s_type )
  // {
  //   mEnabled = true;
  //   mThName  = null;
  //   mGroup   = null;
  //   mDefaultOptions = null;
  //   // mFilename = null;
  //   mLevel = DrawingLevel.LEVEL_DEFAULT;
  //   mRoundTrip = W2D_NONE;
  //   mSymbolType = s_type;
  // }

  /** cstr
   * @param s_type    symbol type
   * @param th_name   therion name (must be non-null)
   * @param group     symbol group (can be null)
   * @param filename  not used
   * @param rt        roundtrip (1 walls_shp, 2 walls_sym, 3 detail_shp, 4 detail_sym)
   */
  Symbol( int s_type, String th_name, String group, String filename, int rt ) 
  { 
    mSymbolType = s_type;
    mEnabled  = true;
    setThName( th_name );
    mGroup    = group;
    mDefaultOptions = null;
    // mFilename = filename;
    mLevel = DrawingLevel.LEVEL_DEFAULT;
    mRoundTrip = rt;
  }

  // /** cstr - with only the "enabled" field - UNUSED
  //  * @param s_type    symbol type
  //  * @param enabled  whether the symbol is enabled
  //  */
  // Symbol( int s_type, boolean enabled ) 
  // { 
  //   mEnabled  = enabled; 
  //   mThName   = null;
  //   mGroup    = null;
  //   mDefaultOptions = null;
  //   // mFilename = null;
  //   mLevel = DrawingLevel.LEVEL_DEFAULT;
  //   mRoundTrip = W2D_NONE;
  //   mSymbolType = s_type;
  // }

  // ------------------------------------------------------------------------

  /** @return true if the symbol (therion name) is "section"
   * @note used by ItemDrawer updateRecentPoint
   */
  boolean isSection() { return mThName.equals("section"); }

  boolean isPicture() { return mSymbolType == TYPE_POINT && mThName.equals("picture"); }

  /** @return true if this is a POINT symbol
   */
  boolean isPoint() { return mSymbolType == TYPE_POINT; }

  // /** @return true if this is a LINE symbol - UNUSED
  //  */
  // boolean isLine() { return mSymbolType == TYPE_LINE; }

  // /** @return true if this is a AREA symbol - UNUSED
  //  */
  // boolean isArea() { return mSymbolType == TYPE_AREA; }

  /** @return the symbol default options
   */
  public String getDefaultOptions() { return mDefaultOptions; }

  // SymbolInterface methods
  /** set the symbol Therion name
   * @param name    symbol Therion name
   * 2023-01-31 saved prefix
   */
  protected void setThName( String name )
  {
    if ( name == null ) return;
    if ( name.startsWith("u:") ) {
      if ( name.length() == 2 ) return;
      mThPrefix = "u:";
      mThName = name.substring(2);
    } else {
      if ( name.length() == 0 ) return;
      mThPrefix = null;
      mThName = name;
    }
  }

  /** @return the symbol Therion name
   */
  public String getThName() { return mThName; }

  /** @return the symbol full Therion name, possibly including the "u:" prefix (2023-01-31)
   */
  public String getFullThName() { return (mThPrefix == null)? mThName : mThPrefix + mThName; }

  public String getFullThNameEscapedColon() { return ( (mThPrefix == null)? mThName : (mThPrefix + mThName) ).replaceAll(":", "_"); }

  /** @return true if the symbol Therion name is non-null and coincides with the given name
   * @param th_name   given name
   */
  boolean hasThName( String th_name ) { return mThName != null && mThName.equals( th_name ); } 

  /** @return true if the symbol Therion name coincides with the given name, or both are null
   * @param name   given name
   */
  boolean isThName( String name ) { return (name == null && mThName == null) || mThName.equals( name ); }

  /** @return true if the symbol Therion name is null or empty
   */
  boolean isThNameEmpty() { return mThName == null || mThName.length() == 0; }

  /** @return the symbol group
   */
  String getGroup() { return mGroup; }

  /** @return true if the symbol group is the given name
   * @param group   given name
   */
  boolean hasGroup( String group ) { return mGroup != null && mGroup.equals( group ); }

  // filename is not used - symbols are distinguished by their th_name
  // public String getFilename()   { return mThName.startsWith("u") ? mThName.substring(2) : mThName; /* mFilename; */ }

  /** @return the symbol name - default implementation returning "undefined"
   */
  public String getName() { return "undefined"; }

  // FIXME this is half not-translatable: mGroup must be english
  /** @return the symbol group, or "-" if the group is null
   */
  public String getGroupName()   { return (mGroup == null)? "-" : mGroup; }

  /** @return the symbol paint - default to null
   */
  public Paint  getPaint()      { return null; } // Overridden

  /** @return the symbol path - default to null
   */
  public Path   getPath()       { return null; }

  /** @return true if the symbol is orientable - default to false
   */
  public boolean isOrientable() { return false; }

  /** @return true if the symbol is declinable - default to false
   */
  public boolean isDeclinable() { return false; }

  /** @return the symbol color - either the paint color of full white
   */
  public int getColor( ) { return getColor( 0xffffffff ); }

  /** @return the symbol color - either the paint color of the provided color
   * @param color  provided color
   */
  public int getColor( int color )
  { 
    Paint paint = getPaint();
    return ( paint == null )? color : paint.getColor();
  }

  /** @return true if the symbol is enabled
   */
  public boolean isEnabled() { return mEnabled; }

  /** @return true if the symbol is config-enabled
   */
  public boolean isConfigEnabled() { return mConfig; }

  /** set the symbol "enabled" flag
   * @param enabled   new enabled flag
   */
  public void setEnabled( boolean enabled ) { mEnabled = enabled; }

  /** set the value of enabled from config-enabled
   */
  public void setEnabledConfig() { mEnabled = mConfig; }

  /** set the value of config-enabled
   * @param enabled    new value of config-enabled
   */
  public void setConfigEnabled( boolean enabled ) { mConfig = enabled; }

  /** set the value of config-enabled from enabled value
   */
  public void setConfigEnabled( ) { mConfig = mEnabled; }

  /** toggle the symbol "enabled" flag between ON and OFF
   */
  public void toggleEnabled() { mEnabled = ! mEnabled; }
  
  /** set the symbol orientation angle
   * @param angle    angle [degrees]
   * @return true if the orientation has been set, false by default
   */
  public boolean setAngle( float angle ) { return false; }

  /** @return the symbol orientation angle, 0 degrees by default
   */
  public int getAngle() { return 0; }

  /** @return the symbol X-dimension
   * @param type   symbol type (point, line, or area)
   */
  static float sizeX( int type ) { return ( type == SymbolType.POINT )? TDSetting.mUnitIcons * sizeFactorXP : TDSetting.mUnitIcons * sizeFactorXL; }

  /** @return the symbol Y-dimension
   * @param type   symbol type (point, line, or area)
   */
  static float sizeY( int type ) { return ( type == SymbolType.POINT )? TDSetting.mUnitIcons * sizeFactorYP : TDSetting.mUnitIcons * sizeFactorYL; }

  /** @return the therion name, removing the "u:" prefix if present
   * @param name  full therion name
   */
  static String deprefix_u( String name ) { return (name == null)? null : (name.startsWith("u:"))? name.substring(2) : name; }

  /** @return true if the name starts with "u:"
   * @param name   full therion name
   */
  static boolean hasUPrefix( String name ) { return name.startsWith("u:"); }

}
