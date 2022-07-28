/* @file TDString.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid fixed strings
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import java.util.Arrays;

public class TDString
{
  public static final String EMPTY = "";
  public static final String SPACE = " ";

  public static final String ZERO  = "0";
  public static final String ONE   = "1";
  public static final String TWO   = "2";
  public static final String THREE = "3";
  public static final String FOUR  = "4";
  public static final String FIVE  = "5";
  public static final String TEN   = "10";
  public static final String TWENTY    = "20";
  public static final String TWENTYFOUR = "24";
  public static final String FIFTY     = "50";
  public static final String SIXTY     = "60";
  public static final String NINETY    = "90";
  public static final String NINETYONE = "91";

  public static final String OPTION_SCRAP = "-scrap";

  /** @return true is the given string is null or empty
   * @param str    string
   */
  public static boolean isNullOrEmpty( String str )
  {
    return str == null || str.length() == 0;
  }

  /** @return string with spaces removes
   * @param str input string
   */
  public static String noSpace( String str )
  {
    return (str == null)? null : str.replaceAll("\\s+", "");
  }

  /** @return string with spaces replaced by underscore
   * @param str input string
   */
  public static String spacesToUnderscore( String str )
  {
    return (str == null)? null : str.replaceAll("\\s+", "_");
  }

  /** @return string with multiple spaces replaced by single space
   * @param str input string
   */
  public static String spacesToSpace( String str )
  {
    return (str == null)? null : str.replaceAll("\\s+", " ");
  }

  /** @return string tokenisation on multiple spaces
   * @param str input string
   */
  public static String[] splitOnSpaces( String str )
  {
    return (str == null)? null : str.replaceAll("\\s+", " ").split(" ");
  }

  /** @return string with comma replaced by point
   * @param str input string
   */
  public static String commaToPoint( String str )
  {
    return (str == null)? null : str.replaceAll(",", ".");
  }

  public static String escape( String str )
  {
    if ( str == null ) return "";
    return str.replace('"', '\u001b' );
  }

  public static String unescape( String str )
  {
    if ( str == null ) return "";
    return str.replace('\u001b', '"' );
  }

  // -----------------------------------------------------------
  private char[] mChr;
  private int    mCapacity;
  private int    mSize;
  private final static int CAPACITY = 16;

  /** default cstr - empty string
   */
  public TDString() 
  {
    mSize = 0;
    mCapacity = CAPACITY;
    mChr  = new char[ mCapacity ];
    nullTerminate();
  }

  /** cstr
   * @param str   initializer string
   */
  public TDString( String str )
  {
    mSize = str.length();
    mCapacity = mSize + CAPACITY;
    mChr  = new char[ mCapacity ];
    for ( int i=0; i<mSize; ++i ) mChr[i] = str.charAt(i);
    nullTerminate();
  }

  /** copy cstr
   * @param str  string to copy
   */
  public TDString( TDString str )
  {
    mSize = str.mSize;
    mCapacity = str.mCapacity;
    mChr = Arrays.copyOf( str.mChr, mCapacity );
    nullTerminate();
  }
 
  /** append a string to this
   * @param str  string to append
   */
  public TDString append( TDString str )
  {
    int size2 = str.size();
    int size = mSize + size2;
    setCapacity( size + CAPACITY );
    for ( int i=0; i<size2; ++i ) {
      mChr[mSize] = str.mChr[i];
    }
    nullTerminate();
    return this;
  }

  /** append a char
   * @param ch   char to append
   */
  public TDString append( char ch )
  {
    if ( mSize + 1 >= mCapacity ) {
      setCapacity( mCapacity + CAPACITY );
    }
    mChr[mSize] = ch;
    ++ mSize;
    nullTerminate();
    return this;
  }

  /** @return true if this striung is equal to the given string
   * @param str    string to compare with
   */
  public boolean equals( TDString str )
  {
    if ( mSize != str.size() ) return false;
    for ( int i=0; i<mSize; ++i ) if ( mChr[i] != str.at(i) ) return false;
    return true;
  }
    
  /** @return -1 if this string is lexicographicalle smaller than the given string
   *          +1 if this string is lexicographicalle larger than the givaen string
   *           0 if they are equal
   * @param str given string
   */
  public int compare( TDString str )
  {
    int sz = str.size();
    if ( mSize <= sz ) {
      for ( int i=0; i<mSize; ++i ) {
        if ( mChr[i] < str.mChr[i] ) return -1;
        if ( mChr[i] > str.mChr[i] ) return  1;
      }
      if ( mSize < sz ) return -1;
      return 0;
    } else {
      for ( int i=0; i<sz; ++i ) {
        if ( mChr[i] < str.mChr[i] ) return -1;
        if ( mChr[i] > str.mChr[i] ) return  1;
      }
      return 1;
    }
  }

  /** replace the char in this string with the given string
   * @param str   string to replace
   * @param k     index where to replace
   */
  public TDString replace( TDString str, int k )
  {
    int size2 = str.size();
    if ( k + size2 >= mCapacity ) {
      mCapacity = k + size2 + CAPACITY;
      char[] tmp = Arrays.copyOf( mChr, mCapacity );
      mChr = tmp;
      for ( int i=0; i<size2; ++i ) mChr[k+i] = str.mChr[i];
      nullTerminate();
    } else {
      for ( int i=0; i<size2; ++i ) mChr[k+i] = str.mChr[i];
      if ( k+size2 > mSize ) {
        mSize = k + size2;
        nullTerminate();
      }
    }
    return this;
  }

  /** replace the char in this string with the given string
   * @param str   string to replace
   * @param k     index where to replace
   */
  public TDString replace( String str, int k )
  {
    int size2 = str.length();
    if ( k + size2 >= mCapacity ) {
      mCapacity = k + size2 + CAPACITY;
      char[] tmp = Arrays.copyOf( mChr, mCapacity );
      mChr = tmp;
      for ( int i=0; i<size2; ++i ) mChr[k+i] = str.charAt(i);
      nullTerminate();
    } else {
      for ( int i=0; i<size2; ++i ) mChr[k+i] = str.charAt(i);
      if ( k+size2 > mSize ) {
        mSize = k + size2;
        nullTerminate();
      }
    }
    return this;
  }

  /** set a char
   * @param ch  char to set
   * @param k   index 
   */
  public void set( char ch, int k ) 
  {
    assert( k >= 0 && k < mSize );
    mChr[k] = ch;
  }

  /** @return the size of the string
   */
  public int size() { return mSize; }

  /** @return the capacity of the char array
   */
  public int capacity() { return mCapacity; }

  /** expand the capacity
   * @param cap  new capacity
   */
  public int setCapacity( int cap )
  {
    mCapacity = cap;
    char[] tmp = Arrays.copyOf( mChr, cap );
    mChr = tmp;
    if ( mSize >= mCapacity ) {
      mSize = mCapacity - 1;
      nullTerminate();
    }
    return mCapacity;
  }

  /** @return the char at a given index
   * @param k    index
   */
  public char at( int k )
  {
    assert( k >= 0 && k < mSize );
    return mChr[k];
  }

  /** null-terminate the char array
   */
  private void nullTerminate() { mChr[mSize] = 0; }
    
    
}
