/* @file Scanline.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SQLite loader readfile helper
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class Scanline
{
  private String val;
  private int pos,  // string position
              len;  // string length

  Scanline( String line, int p, int l )
  {
    val = line;
    pos = p;
    len = l;
    skipSpaces();
  }

  void skipSpaces( )
  {
    while ( pos < len && val.charAt(pos) == ' ' ) ++ pos;
  }

  void skipCommaAndSpaces( )
  {
    if ( pos < len && val.charAt(pos) == ',' ) ++pos;
    while ( pos < len && val.charAt(pos) == ' ' ) ++ pos;
  }
  
  int nextQuote( )
  {
    int next = pos;
    while ( next < len && val.charAt(next) != '"' ) ++next; 
    return next;
  }

  // return the position of next comma or space
  // the return value is guaranteed >= pos
  int nextCommaOrSpace( )
  {
    int next = pos;
    while ( next < len && val.charAt(next) != ',' && val.charAt(next) != ' ' ) ++next; 
    return next;
  }

  String stringValue( ) 
  {
    ++pos; // skip '"'
    int next_pos = nextQuote( );
    String ret = (pos == next_pos )? "" : val.substring(pos, next_pos );
    // TDLog.Log( TDLog.LOG_DB, "stringValue <" + ret + ">" );
    pos = next_pos + 1;
    skipCommaAndSpaces( );
    return ret;
  }

  long longValue( )
  {
    long ret = -1;
    int next_pos = nextCommaOrSpace( );
    // TDLog.Log( TDLog.LOG_DB, "longValue " + pos + " " + next_pos + " " + len + " <" + val.substring(pos,next_pos) + ">" );
    String toParse = val.substring( pos, next_pos ); // N.B. next_pos >= pos --> toParse != null
    if ( ! toParse.equals("\"null\"") ) {
      try {
        ret = Long.parseLong( val.substring( pos, next_pos ) );
      } catch ( NumberFormatException e ) {
        TDLog.Error( "longValue error: " + val.substring( pos, next_pos ) );
      }
    }
    pos = next_pos;
    skipCommaAndSpaces( );
    return ret;
  }

  double doubleValue( )
  {
    int next_pos = nextCommaOrSpace( );
    double ret = 0.0;
    try {
      ret = Double.parseDouble( val.substring(pos, next_pos ) );
      // TDLog.Log( TDLog.LOG_DB, "doubleValue " + pos + " " + next_pos + " " + len + " <" + val.substring(pos,next_pos) + ">" );
    } catch ( NumberFormatException e ) {
      TDLog.Error( "doubleValue error: " + val.substring(pos, next_pos) );
    }
    pos = next_pos;
    skipCommaAndSpaces( );
    return ret;
  }

}
