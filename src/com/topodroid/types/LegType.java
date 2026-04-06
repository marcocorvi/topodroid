/* @file LegType.java
 *
 * @author marco corvi
 * @date oct 2015
 *
 * @brief TopoDroid shot "leg" types
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.types;

public class LegType
{
  public static final int INVALID = -1;
  public static final int NORMAL  = 0; // either leg, splay, or blank
  public static final int EXTRA   = 1; // additional leg shots
  public static final int SPLAY   = 2; // cross splay
  public static final int XSPLAY  = 3; // cross splay
  public static final int HSPLAY  = 4; // horizontal splay
  public static final int VSPLAY  = 5; // vertical splay
  public static final int SCAN    = 6; // scan splay
  public static final int XSCAN   = 7; // scan splay
  public static final int HSCAN   = 8; // scan splay
  public static final int VSCAN   = 9; // scan splay
  public static final int BLUNDER = 10; // blunder leg UNUSED
  public static final int BACK    = 11; // back leg

  // string presentation of the leg types
  private static final String[] asString = { "n", "a", "S", "X", "H", "V", "s", "sX", "sH", "sV", "*", "b" };

  /** @return the short string presentation of a leg type
   * @param leg_type  leg-type
   */
  public static String getString( long leg_type ) 
  {
    if ( leg_type < 0 || leg_type > 9L ) return null;
    return asString[ (int)leg_type ];
  }

  /** block-type to leg-type table
   */
  public static final long[] BlockToLeg = {
    LegType.NORMAL, // 0 BLANK
    LegType.NORMAL, // 1 LEG
    LegType.EXTRA,  // 2 SEC_LEG
    LegType.NORMAL, // 3 BLANK_LEG
    LegType.BACK,   // 4 BACK_LEG
    LegType.BLUNDER, // 5 BLUNDER_LEG (UNUSED)
    LegType.SPLAY,  // 6 SPLAY
    LegType.XSPLAY, // 7
    LegType.HSPLAY, // 8
    LegType.VSPLAY, // 9
    LegType.SCAN,   // 10 SCAN
    LegType.XSCAN,  // 11
    LegType.HSCAN,  // 12
    LegType.VSCAN,  // !3 VSCAN
  };

  /** @return the leg-type for a given block-type
   * @param block_type  block-type
   */
  static long getLegType( int block_type ) { return BlockToLeg[ block_type ]; }


  /** @return the next splay/scan leg type in cycle fashion
   * @param type current leg type
   * @note scan-splay do not enter the cycle
   */
  public static int nextSplayClass( int type ) 
  {
    switch( type ) {
      case SPLAY:  return XSPLAY;
      case XSPLAY: return HSPLAY;
      case HSPLAY: return VSPLAY;
      case VSPLAY: return SPLAY;
      case SCAN:   return SCAN;   // scan do not cycle
      case XSCAN:  return XSCAN;
      case HSCAN:  return HSCAN;
      case VSCAN:  return VSCAN;
      // case EXTRA:  return EXTRA; // others are invalied
      // case BACK:   return BACK;
      default: return INVALID;
    }
  }

}
