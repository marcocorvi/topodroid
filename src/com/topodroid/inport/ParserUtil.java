/* @file ParserUtil.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid parser utility
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.util.TDUtil;
import com.topodroid.util.TDMath;
import com.topodroid.types.ExtendType;

import java.util.Locale;
import java.util.regex.Pattern;

class ParserUtil
{
  final static int DATA_DEFAULT    = 0;
  final static int DATA_NONE       = DATA_DEFAULT;
  final static int DATA_NORMAL     = 1;
  final static int DATA_TOPOFIL    = 2;
  final static int DATA_CARTESIAN  = 3;
  final static int DATA_CYLPOLAR   = 4;
  final static int DATA_DIVING     = 5;
  final static int DATA_PASSAGE    = 6;
  final static int DATA_DIMENSION  = DATA_PASSAGE;
  final static int DATA_NOSURVEY   = 7;

  final static int CASE_UPPER     = 0;
  final static int CASE_LOWER     = 1;
  final static int CASE_PRESERVE  = 2;

  /** convert a name to upper or lower
   * @param c   output case: 0 upper, 1 lower, 2 preserve case
   * @return case-converted name
   */
  static String applyCase( int c, String name ) 
  {
    switch ( c ) {
      case CASE_UPPER: return name.toUpperCase( Locale.getDefault() );
      case CASE_LOWER: return name.toLowerCase( Locale.getDefault() );
      case CASE_PRESERVE:
      default: return name;
    }
  }

  static float parseAngleUnit( String unit )
  {
    if ( unit == null ) return 1.0f; // default
    String u = unit.trim().toLowerCase( Locale.ROOT );
    // not handled "percent"
    if ( u.startsWith("min") )  return 1/60.0f;
    if ( u.startsWith("grad") ) return TDUtil.GRAD2DEG;
    if ( u.startsWith("mil") )  return TDUtil.MIL2DEG;
    if ( u.startsWith("rad") )  return TDUtil.RAD2DEG;
    // if ( unit.startsWith("deg") ) return 1.0f;
    return 1.0f;
  }

  static float parseLengthUnit( String unit )
  {
    if ( unit == null ) return 1.0f; // default
    String u = unit.trim().toLowerCase( Locale.ROOT );
    if ( u.startsWith("c") ) return 0.01f; // cm centimeter
    if ( u.startsWith("f") ) return TDUtil.FT2M; // ft feet
    if ( u.startsWith("i") ) return TDUtil.IN2M; // in inch
    if ( u.startsWith("milli") || u.equals("mm") ) return 0.001f; // mm millimeter
    if ( u.startsWith("y") ) return TDUtil.YD2M; // yd yard
    // if ( unit.startsWith("m") ) return 1.0f;
    return 1.0f;
  }

  static int parseExtend( String extend, int old_extend )
  {
    if ( extend == null ) return old_extend;
    extend = extend.trim().toLowerCase( Locale.ENGLISH ); // keywords are in English
    if ( extend.isEmpty() ) return old_extend;
    // skip: hide, start
    if ( extend.equals("hide") || extend.equals("start") ) {
      return old_extend;
    }
    if ( extend.equals("left") || extend.equals("reverse") ) {
      return ExtendType.EXTEND_LEFT;
    } 
    if ( extend.startsWith("vert") ) {
      return ExtendType.EXTEND_VERT;
    }
    if ( extend.startsWith("ignore") ) {
      return ExtendType.EXTEND_IGNORE;
    }
    // if ( extend.equals("right") || extend.equals("normal") ) {
    //   return ExtendType.EXTEND_RIGHT;
    // } 
    return ExtendType.EXTEND_RIGHT;
  }

  /** parse a float string
   * @note input string must use point as decimal separator
   * @param val   input string
   * @param def   default value, returned if input cannot be read or there is a parsing error
   * @return float value
   */
  static float parseFloat( String val, float def )
  {
    if ( val == null ) return def;
    try {
      return Float.parseFloat( val.trim() );
    } catch ( NumberFormatException e ) { }
    return def;
  }

  private static final Pattern WSNE = Pattern.compile( ".+[WSNE]$" );
  private static final Pattern WS   = Pattern.compile( ".+[WS]$" );
  private static final Pattern PM   = Pattern.compile( "^[+-].+" );
  private static final Pattern PM2  = Pattern.compile( "^[+-]{2,}.+" );

  /** parse a degree string
   * @param val input string
   * @return value [decimal degrees]
   * allowed inputs are DD.ddd DD:MM.mmm DD:MM:SS.sss
   * input string can be with sign ('-' or '+') or terminated with one of 'N', 'S', 'W'. 'E' (N.B. only uppercase letters)
   * negative sign '-' cannot come with a direction character
   * positive sign '+' cannot come with a negative direction character 'W', 'S'
   * @note -45N and +45S are rejected
   * @note float DD:MM:SS is borderline (7 digit precision)
   */
  static float parseDegrees( String val, float def ) 
  {
    if ( val == null ) return def;
    String v = val.trim();
    if ( v.isEmpty() ) return def;
    if ( v.endsWith(":") ) return def;               // 45: 45:23:
    // if ( PM2.matcher(v).matches() ) return def;
    boolean wsne_match = WSNE.matcher(v).matches();
    boolean start_minus = v.startsWith("-");
    boolean ws_match = WS.matcher(v).matches();
    boolean neg = ws_match || start_minus;
    if ( wsne_match && start_minus ) return def;     // -45W or -35S or -45N or -45E
    if ( ws_match && v.startsWith("+") ) return def; // +45W or +45S 
    if ( PM.matcher(v).matches() )   v = v.substring(1);
    if ( wsne_match ) v = v.substring(0, v.length() - 1);
    if ( v.indexOf('+') >= 0 || v.indexOf('-') >= 0 ) return def; // 45:-23 45:23:-13
    String[] p = v.split(":");
    if ( p.length > 3 ) return def;                  // 45:23:34:33
    try {
      // Float.parseFloat also accepts things like "45f", "45d", and hex floats ("0x1p3").
      // "1e5" is accepted (100000), while "1e-5" is rejected because of the -. None of this is likely to matter for survey files.
      // If you want strictness, validate each field against a pattern such as \d+(\.\d*)? before parsing.
      float d = Float.parseFloat( p[0] );
      if ( p.length > 1 ) {
        float m = Float.parseFloat( p[1] );
        if ( ! (m < 60) ) return def;                 // 45:76
        d += m / 60.0f;
        if ( p.length > 2 ) {
          float s = Float.parseFloat( p[2] );
          if ( ! ( s < 60 ) ) return def;             // 45:23:90
          d += s / 3600.0f;
        }
      }
      float r = neg ? -d : d;
      return ( Float.isNaN( r ) || Float.isInfinite( r ) )? def : r;
    } catch ( NumberFormatException e ) { } 
    return def;
  }

  /** parse a clino string
   * @param val   input string
   * @param percent whether the input is a percent inclination
   * @return clino value [decimal degrees]
   */
  static float parseClino( String val, boolean percent )
  {
    float f = ParserUtil.parseFloat( val, Float.NaN );
    if ( Float.isNaN(f) ) return f;
    return ( percent? TDMath.atand( f ) : f);
  }

}
