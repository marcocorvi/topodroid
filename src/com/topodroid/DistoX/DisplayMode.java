/* @file DisplayMode.java
 *
 * @author marco corvi
 * @date oct 2015
 *
 * @brief TopoDroid drawing: display mode consts
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class DisplayMode
{
  static final int DISPLAY_NONE     = 0;
  static final int DISPLAY_LEG      = 0x01;
  static final int DISPLAY_SPLAY    = 0x02;
  static final int DISPLAY_STATION  = 0x04;
  static final int DISPLAY_GRID     = 0x08;
  static final int DISPLAY_LATEST   = 0x10; // whether to display the latest shots
  static final int DISPLAY_SCALEBAR = 0x20; // whether to display the scale reference bar on not 
  static final int DISPLAY_OUTLINE  = 0x40; // whether to display only the outline
  static final int DISPLAY_ID       = 0x100; 
  static final int DISPLAY_BLANK    = 0x200; 

  // static final int DISPLAY_SHOT     = 0x0313; // leg splay latest blank id
  static final int DISPLAY_PLOT     = 0x2f;
  static final int DISPLAY_SECTION  = 0x3d; //        0x20 | 0x10 | 0x08 | 0x04 |        0x01
  // static final int DISPLAY_XXXXXXXX = 0x6d; // 0x40 | 0x20 |        0x08 | 0x04 |        0x01
  static final int DISPLAY_OVERVIEW = 0x2f; //        0x20 |        0x08 | 0x04 | 0x02 | 0x01 skip outline and latest 
  static final int DISPLAY_FULL     = 0x032f; 

  static boolean isLeg( int mode )     { return ( mode & DISPLAY_LEG     ) == DISPLAY_LEG; }
  static boolean isSplay( int mode )   { return ( mode & DISPLAY_SPLAY   ) == DISPLAY_SPLAY; }
  static boolean isStation( int mode ) { return ( mode & DISPLAY_STATION ) == DISPLAY_STATION; }
  static boolean isGrid( int mode )    { return ( mode & DISPLAY_GRID    ) == DISPLAY_GRID; }
  static boolean isLatest( int mode )  { return ( mode & DISPLAY_LATEST  ) == DISPLAY_LATEST; }
  static boolean isScalebar( int mode ){ return ( mode & DISPLAY_SCALEBAR) == DISPLAY_SCALEBAR; }
  static boolean isOutline( int mode ) { return ( mode & DISPLAY_OUTLINE ) == DISPLAY_OUTLINE; }
  static boolean isId( int mode )      { return ( mode & DISPLAY_ID )      == DISPLAY_ID; }
  static boolean isBlank( int mode )   { return ( mode & DISPLAY_BLANK )   == DISPLAY_BLANK; }

  static String toString( int mode )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ( ( mode & DISPLAY_LEG )      == DISPLAY_LEG )? "1" : "0" );
    sb.append( ( ( mode & DISPLAY_SPLAY )    == DISPLAY_SPLAY )? "1" : "0" );
    sb.append( ( ( mode & DISPLAY_STATION )  == DISPLAY_STATION )? "1" : "0" );
    sb.append( ( ( mode & DISPLAY_GRID )     == DISPLAY_GRID )? "1" : "0" );

    sb.append( ( ( mode & DISPLAY_LATEST )   == DISPLAY_LATEST )? "1" : "0" );
    sb.append( ( ( mode & DISPLAY_SCALEBAR ) == DISPLAY_SCALEBAR )? "1" : "0" );
    sb.append("0");  // sb.append( ( ( mode & DISPLAY_OUTLINE )  == DISPLAY_OUTLINE )? "1" : "0" );
    sb.append("0");

    sb.append( ( ( mode & DISPLAY_ID     )   == DISPLAY_ID     )? "1" : "0" );
    sb.append( ( ( mode & DISPLAY_BLANK  )   == DISPLAY_BLANK  )? "1" : "0" );

    return sb.toString();
  }

  static int parseString( String str )
  {
    if ( str == null ) return DISPLAY_FULL;
    int ret = 0;
    if ( str.charAt(0) == '1' ) ret |= DISPLAY_LEG;
    if ( str.charAt(1) == '1' ) ret |= DISPLAY_SPLAY;
    if ( str.charAt(2) == '1' ) ret |= DISPLAY_STATION;
    if ( str.charAt(3) == '1' ) ret |= DISPLAY_GRID;

    if ( str.charAt(4) == '1' ) ret |= DISPLAY_LATEST;
    if ( str.charAt(5) == '1' ) ret |= DISPLAY_SCALEBAR;
    // OUTLINE not saved

    if ( str.charAt(8) == '1' ) ret |= DISPLAY_ID;
    if ( str.charAt(9) == '1' ) ret |= DISPLAY_BLANK;
    return ret;
  }

}
