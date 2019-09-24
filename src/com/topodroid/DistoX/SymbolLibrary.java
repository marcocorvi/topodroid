/* @file SymbolLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: area symbol library
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.Locale;
// import java.util.Stack;
// import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

// import java.io.File;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Paint;
import android.graphics.Path;

// import android.util.Log;

class SymbolLibrary
{
  String mPrefix;

  ArrayList< Symbol > mSymbols;
  private SymbolNode mRoot;
  // int mSymbolNr;

  SymbolLibrary( String prefix )
  { 
    mPrefix = prefix;
    mRoot = null;
    mSymbols = new ArrayList<>();
  }

  int size() { return mSymbols.size(); }

  // ----------------------------------------------------

  // used by DrawingDxf and DrawingSurface (for the palette)
  ArrayList< Symbol > getSymbols() { return mSymbols; }

  boolean addSymbol( Symbol v )
  {
    if ( v == null ) return false; // prereq.
    boolean ret = true;
    SymbolNode n = new SymbolNode( v );
    if ( mRoot == null ) {
      mRoot = n;
      mRoot.color = BLACK;
    } else {
      for ( SymbolNode n0 = mRoot; ; ) {
        int c = compare( n0.value.mThName, v.mThName );
        if ( c < 0 ) {
          if ( n0.left == null ) {
            n0.left = n;
            n.parent = n0;
            break;
          } else {
            n0 = n0.left;
          }
        } else if ( c > 0 ) {
          if ( n0.right == null ) {
            n0.right = n;
            n.parent = n0;
            break;
          } else {
            n0 = n0.right;
          }
        } else {
          TDLog.Error( "Double insertion of symbol " + mPrefix + v.mThName );
          ret = false;
          break;
        }
      }
      // rebalance
      if ( ret ) insert_case1( n );
    }
    if ( ret ) {
      mSymbols.add( v );
      // mSymbolNr = mSymbols.size();
    }
    return ret;
  }

  protected Symbol get( String th_name ) 
  {
    return ( mRoot == null )? null : mRoot.get( th_name );
  }

  // ============================================================

  int getSymbolIndex( Symbol symbol )
  {
    int nr = mSymbols.size();
    for ( int k=0; k<nr; ++k ) {
      if ( symbol == mSymbols.get(k) ) return k;
    }
    return -1;
  }

  int getSymbolIndexByThName( String th_name )
  {
    int nr = mSymbols.size();
    for ( int k=0; k<nr; ++k ) if ( mSymbols.get(k).mThName.equals( th_name) ) return k;
    return -1;
  }

  // int getSymbolIndexByFilename( String fname )
  // {
  //   int nr = mSymbols.size();
  //   for ( int k=0; k<nr; ++k ) if ( mSymbols.get(k).mThName.equals( fname) ) return k;
  //   return -1;
  // }

  // ===============================================
  // SymbolInterface

  // this is used only by PT Cmap
  boolean hasSymbolByThName( String th_name ) { return ( null != get( th_name ) ); }

  // this is used by loadUserXXX 
  // boolean hasSymbolByFilename( String fname ) { return ( null != get( fname ) ); }

  // Symbol getSymbolByFilename( String fname ) { return get( fname ); }
  Symbol getSymbolByThName( String th_name ) { return get( th_name ); }

  Symbol getSymbolByIndex( int k ) { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get( k ); }

  String getSymbolName( int k )   { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getName(); }
  String getSymbolThName( int k ) { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getThName(); }
  Paint getSymbolPaint( int k )   { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getPaint(); }
  Path  getSymbolPath( int k )    { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getPath(); }
  boolean isSymbolOrientable( int k )   { return k >= 0 && k < mSymbols.size() && mSymbols.get( k ).isOrientable(); }
  boolean isSymbolEnabled( int k )      { return k >= 0 && k < mSymbols.size() && mSymbols.get( k ).isEnabled(); }
  int getSymbolLevel( int k ) { return ( ( k<0 || k>=mSymbols.size() )? 0xff : mSymbols.get( k ).mLevel ); }

  ArrayList<String> getSymbolNames()
  {
    ArrayList<String> ret = new ArrayList<>();
    for ( Symbol s : mSymbols ) ret.add( s.getName() );
    return ret;
  }

  boolean isSymbolEnabled( String th_name ) 
  {
    Symbol a = get( th_name );
    return ( a != null ) && a.isEnabled();
  }

  void resetOrientations() { for ( Symbol s : mSymbols ) s.setAngle(0); }
  
  // ========================================================================
  // CSURVEY attributes

  int getSymbolCsxLayer( int k )    { return ( k < 0 || k >= mSymbols.size() )? -1 : mSymbols.get(k).mCsxLayer; }
  int getSymbolCsxType( int k )     { return ( k < 0 || k >= mSymbols.size() )? -1 : mSymbols.get(k).mCsxType; }
  int getSymbolCsxCategory( int k ) { return ( k < 0 || k >= mSymbols.size() )? -1 : mSymbols.get(k).mCsxCategory; }
  int getSymbolCsxPen( int k )      { return ( k < 0 || k >= mSymbols.size() )? -1 : mSymbols.get(k).mCsxPen; }
  int getSymbolCsxBrush( int k )    { return ( k < 0 || k >= mSymbols.size() )? -1 : mSymbols.get(k).mCsxBrush; }

  // ========================================================================

  void sortSymbolByName( int start )
  {
    int nr = mSymbols.size();
    for ( int k=start+1; k<nr; ) {
      Symbol prev = mSymbols.get(k-1);
      Symbol curr = mSymbols.get(k);
      if ( prev.getName().compareTo(curr.getName()) > 0  ) { // swap
        mSymbols.set( k-1, curr );
        mSymbols.set( k, prev );
        if ( k > start+1 ) --k;
      } else {
        ++k;
      }
    }
  }

  // protected boolean tryLoadMissingSymbol( String prefix, String th_name, String fname )
  // {
  //   String locale = "name-" + Locale.getDefault().toString().substring(0,2);
  //   String iso = "ISO-8859-1";
  //   // String iso = "UTF-8";
  //   // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

  //   Symbol symbol = getSymbolByFilename( fname );
  //   if ( symbol == null ) {
  //     File file = new File( filename );
  //     if ( ! file.exists() ) return false;
  //     symbol = new Symbol( file.getPath(), locale, iso );
  //     mSymbols.add( symbol );
  //   }
  //   if ( symbol == null ) return false;
  //   // Log.v( TopoDroidApp.TAG, "enabling missing symbol " + prefix + th_name );
  //   symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.mThName ) );
  //   makeEnabledList( );
  //   return true;
  // }

  // prefix: p_ l_ a_
  protected void makeEnabledList( )
  {
    for ( Symbol symbol : mSymbols ) {
      TopoDroidApp.mData.setSymbolEnabled( mPrefix + symbol.mThName, symbol.mEnabled );
      // if ( symbol.mEnabled ) {
        // TODO what ?
      // }
    }
  }

  // symbols = palette.mPaletteAreas etc. (filenames)
  // clear     if true disable all symbols before enabling the symbols in the palette
  void makeEnabledListFromStrings( TreeSet<String> symbols, boolean clear )
  {
    if ( clear ) {
      for ( Symbol symbol : mSymbols ) symbol.setEnabled( false );
    }
    for ( String thname : symbols ) {
      Symbol symbol = getSymbolByThName( thname );
      if ( symbol != null ) symbol.setEnabled( true );
    }
    makeEnabledList( );
  }

  void setRecentSymbols( Symbol[] recent )
  {
    int k = 0;
    for ( Symbol symbol : mSymbols ) {
      if ( symbol.mEnabled ) {
        recent[k++] = symbol;
        if ( k >= ItemDrawer.NR_RECENT ) break;
      }
    }
  }

  // UNUSED
  // void writePalette( PrintWriter pw ) 
  // {
  //   for ( Symbol symbol : mSymbols ) {
  //     if ( symbol.isEnabled( ) ) pw.format( " %s", symbol.getFilename() );
  //   }
  // }

  void toDataStream( DataOutputStream dos ) 
  {
    StringBuilder sb = new StringBuilder();
    for ( Symbol symbol : mSymbols ) {
      if ( symbol.isEnabled( ) ) { sb.append( symbol.getThName() ).append(","); }
    }
    try {
      // int str_len = sb.length();
      // dos.writeInt( str_len );
      dos.writeUTF( sb.toString() );
    } catch ( IOException e ) { }
  }

  // -------------------------------------------------------------
  static final private boolean BLACK = true;
  static final private boolean RED = false;

  static private int compare( String s1, String s2 )
  { 
    if ( s1 == null ) return ((s2 == null)? 0 : +1);
    if ( s2 == null ) return -1;
    int l1 = s1.length();
    int l2 = s2.length();
    int kk = ( l1 < l2 )? l1 : l2;
    for ( int k=0; k < kk; ++k ) {
      if ( s1.charAt(k) < s2.charAt(k) ) return -1;
      if ( s1.charAt(k) > s2.charAt(k) ) return +1;
    }
    if ( l1 < l2 ) return -1;
    if ( l1 > l2 ) return +1;
    return 0;
  }

  private class SymbolNode
  {
    SymbolNode parent;
    SymbolNode left;
    SymbolNode right;
    boolean color;
    Symbol value;    // the Node value is the Symbol

    SymbolNode( Symbol v )
    {
      parent = null;
      left = null;
      right = null;
      color = RED;
      value = v;
    }

    // @param name  query key (symbol filename)
    Symbol get( String name )
    {
      int c = compare( value.mThName, name );
      if ( c == 0 ) return value;
      if ( c < 0 ) return ( left == null )? null : left.get( name );
      return ( right == null )? null : right.get( name );
    }
  }

  // -----------------------------------------------------
  private void insert_case1( SymbolNode n ) 
  {
    if ( n.parent == null ) {
      n.color = BLACK;
    } else {
      insert_case2( n );
    }
  }

  // n.parent != null
  private void insert_case2( SymbolNode n ) 
  {
    if ( n.parent.color ) return; // isBlack( n.parent )
    insert_case3( n );
  }

  // n.parent != null && n.parent RED
  private void insert_case3( SymbolNode n )
  {
    SymbolNode u = uncle( n );
    if ( /* u != null && */ isRed( u ) ) {
      n.parent.color = BLACK;
      u.color = BLACK;
      SymbolNode g = grandparent( n );
      g.color = RED;
      insert_case1( g );
    } else {
      insert_case4( n );
    }
  }

  // n.parent == g.left && n = n.parent.right ( n.parent RED )
  // or symmetric
  private void insert_case4( SymbolNode n )
  {
    SymbolNode p = n.parent;
    SymbolNode g = p.parent;
    if ( isRight( n ) && isLeft( p ) ) { // rotate_left( n, p, g );
      n.parent = g;
      if ( g != null ) {
        g.left = n;
      } else {
        mRoot = n;
      }
      if ( n.left != null ) n.left.parent = p;
      p.right = n.left;
      p.parent = n;
      n.left  = p;

      n = n.left; // continue with n.left
    } else if ( isLeft( n ) && isRight( p ) ) { // rotate_right( n, p, g );
      n.parent = g;
      if ( g != null ) {
        g.right = n;
      } else {
        mRoot = n;
      }
      if ( n.right != null ) n.right.parent = p;
      p.left  = n.right;
      p.parent = n;
      n.right = p;

      n = n.right; // continue with n.right
    }
    insert_case5( n );
  }

  // n.parent RED but n.uncle BLACK
  private void insert_case5( SymbolNode n )
  {
    SymbolNode g = grandparent( n );
    SymbolNode p = n.parent;
    p.color = BLACK;
    g.color = RED;
    SymbolNode gp = g.parent;
    if ( gp != null ) {
      if ( g == gp.left ) {
	gp.left = p;
      } else {
	gp.right = p;
      }
    } else {
      mRoot = p;
    }
    p.parent = gp;
    if ( isLeft( n ) ) { // rotate_right( n.parent, g, g.parent );
      // assert( p == g.left );
      g.left = p.right;
      if ( p.right != null ) p.right.parent = g;
      p.right = g;
      g.parent = p;
    } else { // rotate_left( n.parent, g, g.parent );
      // assert( p == g.right );
      g.right = p.left;
      if ( p.left != null ) p.left.parent = g;
      p.left = g;
      g.parent = p;
    }
  }

  private SymbolNode grandparent( SymbolNode n )
  {
    return ( n != null && n.parent != null )?  n.parent.parent : null;
  }

  private SymbolNode uncle( SymbolNode n ) 
  {
    SymbolNode p = n.parent;
    if ( p == null ) return null;
    SymbolNode g = p.parent;
    if ( g == null ) return null;
    return ( p == g.left )? g.right : g.left;
  }

  // private boolean isBlack( SymbolNode n ) { return ( n == null ) || n.color; }
  private boolean isRed( SymbolNode n ) { return ( n != null ) && (! n.color ); }

  // prereq. n.parent != null
  private boolean isLeft( SymbolNode n ) { return ( n == n.parent.left ); }
  private boolean isRight( SymbolNode n ) { return ( n == n.parent.right ); }
  // ===========================================================================
}    
