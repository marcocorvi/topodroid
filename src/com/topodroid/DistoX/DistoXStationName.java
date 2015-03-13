/** @file DistoXStationName.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid station name increment (static)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created
 * 20120726 TopoDroid log
 * 20121118 method isLessOrEqual to compare station names
 * 20121223 bug-fix comparison between 9 and 10
 */
package com.topodroid.DistoX;

import java.util.List;

// import android.util.Log;

public class DistoXStationName
{
  private static char[] lc = {
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' 
  };
  private static char[] uc = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' 
  };

  public static boolean isLessOrEqual( String lhs, String rhs )
  {
    try {
      int nl = Integer.parseInt( lhs );
      int nr = Integer.parseInt( rhs );
      return nl <= nr;
    } catch (NumberFormatException e ) {
      // ok
    }
    int l1 = lhs.length();
    int l2 = rhs.length();
    int len = ( l1 < l2 )? l1 : l2;
    char[] ch1 = lhs.toCharArray();
    char[] ch2 = rhs.toCharArray();
    int k = 0;
    for (; k<len; ++k ) {
      if ( ch1[k] != ch2[k] ) { // try numbers again
        try {
          // Log.v( TopoDroidApp.TAG, lhs + " <= " + rhs + " : try " + lhs.substring(k) + " vs " + rhs.substring(k));
          int nl = Integer.parseInt( lhs.substring(k) );
          int nr = Integer.parseInt( rhs.substring(k) );
          return nl <= nr;
        } catch (NumberFormatException e ) {
          // ok
        }
      }
      if ( ch1[k] > ch2[k] ) {
        // Log.v( TopoDroidApp.TAG, lhs + " <= " + rhs + " : false ");
        return false;
      }
    }
    // Log.v( TopoDroidApp.TAG, lhs + " <= " + rhs + " : " + (l1 <= l2) );
    return ( l1 <= l2 );
  }


  public static String increment( String name )
  {
    // if name is numeric
    // Log.v( TopoDroidApp.TAG, "incrementing " + name );

    if ( name != null && name.length() > 0 ) {
      int len = name.length();
      if ( len > 0 ) {
        char ch = name.charAt( len - 1 );
        int k = Character.getNumericValue( ch );
        if ( k >= 10 && k < 35 ) {
          k -= 9; // - 10 + 1
          // TopoDroidLog.Log( TopoDroidLog.LOG_NAME, "not numeric " + k );
          return name.substring( 0, len - 1 ) + 
           ( Character.isLowerCase( ch )? lc[k] : uc[k] );
        } else if ( k >= 0 && k < 10 ) {
          int n = 0;
          int s = 1;
          // TopoDroidLog.Log( TopoDroidLog.LOG_NAME, "name >" + name + "< n " + n );
          while ( len > 0 ) {
            -- len;
            k = Character.getNumericValue( name.charAt(len) );
            if ( k < 0 || k >= 10 ) { ++len; break; }
            n += s * k;
            s *= 10;
            // TopoDroidLog.Log( TopoDroidLog.LOG_NAME, "k " + k + " n " + n + " len " + len);
          }
          if ( len > 0 ) {
            return name.substring( 0, len ) + Integer.toString( n+1 );
          } 
          return Integer.toString( n+1 );
        }
      }
    }
    return "";
  }

  static boolean listHasName( List<DistoXDBlock> list, String name )
  {
    for ( DistoXDBlock b : list ) {
      if ( b.mType != DistoXDBlock.BLOCK_MAIN_LEG ) continue;
      if ( name.equals( b.mFrom ) || name.equals( b.mTo ) ) return true;
    }
    return false;
  }


}
