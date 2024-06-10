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
import java.util.ArrayList;
import java.util.List;

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

  /** @return string tokenization on multiple spaces
   * @param str input string
   */
  public static String[] splitOnSpaces( String str )
  {
    return (str == null)? null : str.replaceAll("\\s+", " ").split(" ");
  }

  /** split a line on spaces taking into account quoted strings
   * @param str   input line
   * @return array of string tokens
   */ 
  public static String[] splitOnStrings( String str )
  {
    ArrayList< String > strs = new ArrayList<>();
    int len = str.length();
    int inString = 0; // 0 no, 1 normal, 2 quoted, 3 double-quoted
    int pos = -1;
    for ( int i = 0; i < len; ++i ) {
      char ch = str.charAt( i );
      if ( inString == 0 ) {
        if ( ! Character.isSpaceChar( ch ) ) {
          if ( ch == '\'' ) {
            inString = 2;
            ++i;
          } else if ( ch == '"' ) {
            inString = 3;
            ++i;
          } else {
            inString = 1;
          }
          pos = i;
        }
      } else if ( inString == 1 ) {
        if ( Character.isSpaceChar( ch ) ) {
          inString = 0;
          if ( pos >= 0 ) {
            if ( i > pos ) strs.add( str.substring( pos, i ) );
            pos = -1;
          }
        }
      } else if ( inString == 2 ) {
        if ( ch == '\'' ) {
          inString = 0;
          if ( pos >= 0 ) {
            if ( i > pos ) strs.add( str.substring( pos, i ) );
            pos = -1;
          }
        }
      } else if ( inString == 3 ) {
        if ( ch == '"' ) {
          inString = 0;
          if ( pos >= 0 ) {
            if ( i > pos ) strs.add( str.substring( pos, i ) );
            pos = -1;
          }
        }
      }
    }
    if ( inString == 1 && pos >= 0 && pos < len ) {
      strs.add( str.substring( pos ) );
    }
    int cnt = strs.size();
    // TDLog.v("Split On Strings <" + str + "> " + cnt );
    if ( cnt == 0 ) return null;
    String[] ret = new String[ cnt ];
    for ( int i=0; i<cnt; ++i ) ret[i] = (String)strs.get( i );
    return ret;
  }

  /** @return string with comma replaced by point
   * @param str input string
   */
  public static String commaToPoint( String str )
  {
    return (str == null)? null : str.replaceAll(",", ".");
  }

  /** @return string with spaces replaced by underscores
   * @param str input string
   */
  public static String spacesToUnderscores( String str )
  {
    return (str == null)? null : str.replaceAll(" ", "_");
  }

  /** @return string with underscores replaced by spaces
   * @param str input string
   */
  public static String underscoresToSpaces( String str )
  {
    return (str == null)? null : str.replaceAll("_", " ");
  }


  public static String escape( String str )
  {
    if ( str == null ) return "";
    return str.replace('"', '\u001b' ); // replace all occurrences
  }

  public static String unescape( String str )
  {
    if ( str == null ) return "";
    return str.replace('\u001b', '"' ); // replace all occurrences
  }

  /** escape CSV separator
   * @param separator   separator (char)
   * @param str         string where to replace
   * @return string with replacements
   */
  public static String escapeSeparator( char sep, String str )
  {
    if ( str == null ) return "";
    String separator = String.format("%c", sep );
    String replacement = String.format("\\%c", sep );
    return str.replace( separator, replacement ); // replace each comma with backslash-comma
  }

  // -----------------------------------------------------------
  // mutable string

  private char[] mChr;  // array of char's (char is 16 bits)
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

  /** @return true if this string is equal to the given string
   * @param str    string to compare with
   */
  public boolean equals( TDString str )
  {
    if ( mSize != str.size() ) return false;
    for ( int i=0; i<mSize; ++i ) if ( mChr[i] != str.at(i) ) return false;
    return true;
  }
    
  /** @return -1 if this string is lexicographically smaller than the given string
   *          +1 if this string is lexicographically larger than the given string
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

  /** @return true if this string is less than a given string
   * @param str    given string
   */
  public boolean isLessThan( TDString str )
  {
    int sz = str.size();
    if ( mSize <= sz ) {
      for ( int i=0; i<mSize; ++i ) {
        if ( mChr[i] < str.mChr[i] ) return true;
        if ( mChr[i] > str.mChr[i] ) return false;
      }
      return ( mSize < sz );
    } else { // mSize > sz
      for ( int i=0; i<sz; ++i ) {
        if ( mChr[i] < str.mChr[i] ) return true;
        if ( mChr[i] > str.mChr[i] ) return false;
      }
      return false;
    }
  }

  /** @return true if this string is less than a given string
   * @param str    given string
   */
  public boolean isLessThan( String str )
  {
    int sz = str.length();
    if ( mSize <= sz ) {
      for ( int i=0; i<mSize; ++i ) {
        if ( mChr[i] < str.charAt(i) ) return true;
        if ( mChr[i] > str.charAt(i) ) return false;
      }
      return ( mSize < sz );
    } else { // mSize > sz
      for ( int i=0; i<sz; ++i ) {
        if ( mChr[i] < str.charAt(i) ) return true;
        if ( mChr[i] > str.charAt(i) ) return false;
      }
      return false;
    }
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

  /** replace the char in this string with the given string
   * @param k     index where to replace
   * @param str   string to replace
   */
  public TDString set( int k, TDString str )
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
   * @param k     index where to replace
   * @param str   string to replace
   */
  public TDString set( int k, String str )
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
   * @param k   index 
   * @param ch  char to set
   */
  public void set( int k, char ch )
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

  /** @return an immutable String
   */
  @Override
  public String toString() { return new String( mChr, 0, mSize ); }
    

  /** @return true is this string is null or empty
   */
  public boolean isNullOrEmpty( ) { return mSize == 0; }

  /** @return string with spaces removes
   */
  public TDString noSpace( )
  {
    int j = 0;
    for ( int i=0; i<mSize; ++i ) {
      if ( ! Character.isSpaceChar( mChr[i] ) ) {
        mChr[j] = mChr[i];
        ++j;
      }
    }
    mSize = j;
    nullTerminate();
    return this;
  }

  /** @return this string with spaces replaced by underscore
   */
  public TDString spacesToUnderscore( )
  {
    for ( int i=0; i<mSize; ++i ) {
      if ( Character.isSpaceChar( mChr[i] ) ) mChr[i] = '_';
    }
    return this;
  }

  /** @return this string with multiple spaces replaced by single space, and trimmed at the ends
   */
  public TDString spacesToSpace( )
  {
    int j = 0;
    boolean in_space = true;
    for ( int i=0; i<mSize; ++i ) {
      if ( Character.isSpaceChar( mChr[i] ) ) {
        if ( ! in_space ) {
          in_space = true;
          mChr[j] = ' ';
          ++j;
        }
      } else {
        in_space = false;
        mChr[j] = mChr[i];
        ++j;
      }
    }
    mSize = j;
    while ( mSize > 0 && Character.isSpaceChar( mChr[mSize] ) ) -- mSize;
    nullTerminate();
    return this;
  }

  /** @return this string tokenization on multiple spaces
   */
  public List<String> splitOnSpaces( )
  {
    ArrayList< String > ret = new ArrayList<>();
    int off = -1;
    int cnt = 0;
    for ( int i=0; i<mSize; ++i ) {
      if ( Character.isSpaceChar( mChr[i] ) ) {
        if ( cnt > 0 ) {
          ret.add( new String( mChr, off, cnt ) );
          cnt = 0;
          off = -1;
        }
      } else {
        if ( off < 0 ) off = i;
        ++ cnt;
      }
    }
    if ( cnt > 0 ) ret.add( new String( mChr, off, cnt ) );
    return ret;
  }

  /** @return this string with comma replaced by point
   */
  public TDString commaToPoint( )
  {
    for ( int i=0; i<mSize; ++i ) {
      if ( mChr[i] == ',' ) mChr[i] = '.';
    }
    return this;
  }

  /** @return this string with double-quotes escaped
   */
  public TDString escape( )
  {
    for ( int i=0; i<mSize; ++i ) {
      if ( mChr[i] == '"' ) mChr[i] = '\u001b';
    }
    return this;
  }

  /** @return this string with double-quotes unescaped
   */
  public TDString unescape( )
  {
    for ( int i=0; i<mSize; ++i ) {
      if ( mChr[i] == '\u001b' ) mChr[i] = '"';
    }
    return this;
  }

  // ------------ array of strings ---------------------------

  /** @return the next index of non-empty string in an array (the array length if not found)
   * @param vals   string array
   * @param idx    start from the index after this
   */
  public static int nextIndex( String[] vals, int idx )
  {
    ++idx;
    while ( idx < vals.length && vals[idx].length() == 0 ) ++idx;
    return idx;
  }

  /** @return the previous index of non-empty string in an array (-1 if not found)
   * @param vals   string array
   * @param idx    start from the index before this
   */
  public static int prevIndex( String[] vals, int idx )
  {
    --idx;
    while ( idx >= 0 && vals[idx].length() == 0 ) --idx;
    return idx;
  }
    
}
