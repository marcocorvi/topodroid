/* @file DistoXStationName.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid station name increment (static)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

class DistoXStationName
{
  static String mInitialStation;
  static String mSecondStation;

  static void setInitialStation( String init )
  {
    if ( init == null || init.length() == 0 ) init = TDSetting.mInitStation;
    mInitialStation = init;
    mSecondStation  = incrementName( init );
  }

  private static final char[] lc = {
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' 
  };
  private static final char[] uc = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' 
  };

  static boolean isLessOrEqual( String lhs, String rhs )
  {
    int l1 = lhs.length();
    int l2 = rhs.length();
    if ( l1 == 0 ) return true;   // "" <= any_string
    if ( l2 == 0 ) return false;  // "..." > ""
    
    char[] ch1 = lhs.toCharArray();
    char[] ch2 = rhs.toCharArray();
    
    int n1 = 0, n2 = 0; // compare as numbers
    int k1 = 0, k2 = 0;
    for ( ; k1<l1; ++k1 ) {
      if ( ch1[k1] >= '0' && ch1[k1] <= '9' ) {
        n1 = n1*10 + ( ch1[k1] - '0' );
      } else {
        break;
      }
    }
    for ( ; k2<l2; ++k2 ) {
      if ( ch2[k2] >= '0' && ch2[k2] <= '9' ) {
        n2 = n2*10 + ( ch2[k2] - '0' );
      } else {
        break;
      }
    }
    if ( n1 < n2 ) return true;    
    if ( n1 > n2 ) return false;
    // n1 == n2 compare rest of the string (note k1 == k2)
    if ( k1 == l1 ) return true;  // nnn < nnn...
    if ( k2 == l2 ) return false; // nnn... > nnn
    int len = ( l1 < l2 )? l1 : l2;
    for ( int k = k1; k < len; ++k ) {
      if ( ch1[k] < ch2[k] ) return true;
      if ( ch1[k] > ch2[k] ) return false;
    }
    return ( l1 <= l2 );
  }

  static String incrementName( String name, Set<String> set )
  {
    String n = name;
    do {
      n = incrementName( n ); 
    } while ( set.contains( n ) );
    return n;
  }

  static String incrementName( String name, List<DBlock> list )
  {
    String n = name;
    do {
      n = incrementName( n ); 
    } while ( listHasName( list, n ) );
    return n;
  }

  static String incrementName( String name, ArrayList<String> sts )
  {
    String n = name;
    do {
      n = incrementName( n ); 
    } while ( orderContains( sts, n ) );
    return n;
  }


  static String incrementName( String name )
  {
    // if name is numeric
    // Log.v( "DistoX-SN", "incrementing " + name );

    if ( name != null /* && name.length() > 0 */ ) {
      int len = name.length();
      if ( len > 0 ) {
        char ch = name.charAt( len - 1 );
        int k = Character.getNumericValue( ch );
        if ( k >= 10 && k < 35 ) {
          k -= 9; // - 10 + 1
          // TDLog.Log( TDLog.LOG_NAME, "not numeric " + k );
          return name.substring( 0, len - 1 ) + ( Character.isLowerCase( ch )? lc[k] : uc[k] );
        } else if ( k >= 0 && k < 10 ) {
          int n = 0;
          int s = 1;
          // TDLog.Log( TDLog.LOG_NAME, "name >" + name + "< n " + n );
          while ( len > 0 ) {
            -- len;
            k = Character.getNumericValue( name.charAt(len) );
            if ( k < 0 || k >= 10 ) { ++len; break; }
            n += s * k;
            s *= 10;
            // TDLog.Log( TDLog.LOG_NAME, "k " + k + " n " + n + " len " + len);
          }
          if ( len > 0 ) {
            return name.substring( 0, len ) + Integer.toString( n+1 );
          } 
          return Integer.toString( n+1 );
        } else {
          return name + 1;
        }
      }
    }
    return ""; // default is the empty string
  }

  // used by SketchNewShotDialog
  static private boolean listHasName( List<DBlock> list, String name )
  {
    if ( name != null ) {
      for ( DBlock b : list ) {
        if ( ! b.isLeg() ) continue;
        if ( name.equals( b.mFrom ) || name.equals( b.mTo ) ) return true;
      }
    }
    return false;
  }

  // used by PocketTopo export
  static int toInt( String name )
  {
    if ( name == null ) return -1;
    if ( StationPolicy.doTopoRobot() ) {
      int pos = name.indexOf( '.' );
      if ( pos >= 0 ) {
        int pre = 0;
        if ( pos > 0 ) {
          try {
            pre = Integer.parseInt( name.substring( 0, pos ) );
          } catch ( NumberFormatException e ) { }
        }
        try {
          return pre * 1000 + Integer.parseInt( name.substring( pos+1 ) );
        } catch ( NumberFormatException e ) { }
      } else {   
        try {
          return Integer.parseInt( name );
        } catch ( NumberFormatException e ) { }
      }
    }
    int ret = 0;
    int len = name.length();
    for ( int k = 0; k < len; ++k ) {
      char ch = name.charAt( k );
      if ( ch >= '0' && ch <= '9' ) { ret = ret*10 + (int)(ch - '0'); }
      else if ( ch >= 'a' && ch <= 'z') { ret = ret*100 + (int)(ch - 'a'); }
      else if ( ch >= 'A' && ch <= 'Z') { ret = ret*100 + 50 + (int)(ch - 'A'); }
      else { ret = ret*100 + 99; }
    }
    return ret;
  }

  // check if the ordered list of names contains a name
  static private boolean orderContains( ArrayList< String > a, String s )
  {
    int n1 = 0;
    int n2 = a.size();
    if ( n2 == 0 ) {
      return false;
    }
    int cmp = s.compareTo( a.get(n1) );
    if ( cmp == 0 ) {
      return true;
    } else if ( cmp < 0 ) {
      return false;
    }
    n2 --;
    cmp = s.compareTo( a.get(n2) );
    if ( cmp == 0 ) {
      return true;
    } else if ( cmp > 0 ) {
      return false;
    }
    // here a[n1] < s < a[n2]
    while ( n1+1 < n2 ) {
      int n0 = (n1+n2)/2;
      cmp = s.compareTo( a.get(n0) );
      if ( cmp == 0 ) {
        return true;
      } else if ( cmp < 0 ) {
        n2 = n0;
      } else /* if ( cmp > 0 ) */ {
        n1 = n0;
      }
    }
    return false;
  }

  // insert a name in the ordered list of names
  static void orderInsert( ArrayList< String > a, String s )
  {
    int n1 = 0;
    int n2 = a.size();
    if ( n2 == 0 ) {
      a.add( s );
      return;
    }
    int cmp = s.compareTo( a.get(n1) );
    if ( cmp == 0 ) {
      return;
    } else if ( cmp < 0 ) {
      a.add(0, s); // insert at head
      return;
    }
    n2 --;
    cmp = s.compareTo( a.get(n2) );
    if ( cmp == 0 ) {
      return;
    } else if ( cmp > 0 ) {
      a.add( s ); // add at end
      return;
    }
    // here a[n1] < s < a[n2]
    while ( n1+1 < n2 ) {
      int n0 = (n1+n2)/2;
      cmp = s.compareTo( a.get(n0) );
      if ( cmp == 0 ) {
        return;
      } else if ( cmp < 0 ) {
        n2 = n0;
      } else /* if ( cmp > 0 ) */ {
        n1 = n0;
      }
    }
    a.add( n2, s ); // add s after a[n1] at pos n2
  }

}
