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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDFile;

// import java.util.Locale;
// import java.util.Stack;
// import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Paint;
import android.graphics.Path;

public class SymbolLibrary
{
  // Therion names (as well as filenames)
  public final static String USER    = "u:user";
  public final static String LABEL   = "label";
  public final static String STATION = "station";
  public final static String WALL    = "wall";
  public final static String SECTION = "section";
  public final static String SLOPE   = "slope";
  public final static String WATER   = "water";
  public final static String AUDIO   = "audio";
  public final static String PHOTO   = "photo";
  public final static String PICTURE = "picture";
  public final static String ARROW   = "arrow";
  public final static String BORDER  = "border";
  public final static String CHIMNEY = "chimney";
  public final static String CONTOUR = "contour";
  public final static String PIT     = "pit";
  public final static String FAULT   = "fault";
  public final static String FLOOR_MEANDER   = "floor-meander";
  public final static String CEILING_MEANDER = "ceiling-meander";
  public final static String WALL_PRESUMED   = "wall:presumed";
  public final static String ROCK_BORDER = "rock-border";
  public final static String BLOCKS  = "blocks";
  public final static String CLAY    = "clay";
  public final static String DEBRIS  = "debris";
  public final static String SAND    = "sand";
  public final static String AIR_DRAUGHT  = "air-draught";
  public final static String CONTINUATION = "continuation";
  public final static String ARCHEO       = "archeo";
  public final static String COLUMN       = "column";
  public final static String CURTAIN      = "curtain";
  public final static String FLOWSTONE    = "flowstone";
  public final static String DANGER       = "danger";
  public final static String DIG          = "dig";
  public final static String GRADIENT     = "gradient";
  public final static String GUANO        = "guano";
  public final static String ENTRANCE     = "entrance";
  public final static String HELICTITE    = "helictite";
  public final static String ICE          = "ice";
  public final static String MUD          = "mud";
  public final static String PEBBLES      = "pebbles";
  public final static String PILLAR       = "pillar";
  public final static String POPCORN      = "popcorn";
  public final static String ROOT         = "root";
  public final static String SNOW         = "snow";
  public final static String SODA_STRAW   = "soda-straw";
  public final static String STALACTITE   = "stalactite";
  public final static String STALAGMITE   = "stalagmite";
  public final static String WATER_FLOW   = "water-flow";

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

  public int size() { return mSymbols.size(); } // TH2EDIT package

  // ----------------------------------------------------

  // used by DrawingDxf and DrawingSurface (for the palette)
  public ArrayList< Symbol > getSymbols() { return mSymbols; }

  boolean addSymbol( Symbol v )
  {
    if ( v == null ) return false; // prerequisite
    boolean ret = true;
    SymbolNode n = new SymbolNode( v );
    if ( mRoot == null ) {
      mRoot = n;
      mRoot.color = BLACK;
    } else {
      for ( SymbolNode n0 = mRoot; ; ) {
        int c = compare( n0.value.getThName(), v.getThName() );
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
          TDLog.e( "Double insertion of symbol " + mPrefix + v.getThName() );
          ret = false;
          break;
        }
      }
      // re-balance
      if ( ret ) insert_case1( n );
    }
    if ( ret ) {
      mSymbols.add( v );
      // mSymbolNr = mSymbols.size();
    }
    return ret;
  }

  protected Symbol get( String name ) 
  {
    return ( mRoot == null )? null : mRoot.get( Symbol.deprefix_u(name) );
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

  /** @return the index of a symbol given its Therion name
   * @param name  symbol Therion name
   */
  int getSymbolIndexByThName( String name )
  {
    String th_name = Symbol.deprefix_u( name );
    int nr = mSymbols.size();
    for ( int k=0; k<nr; ++k ) if ( mSymbols.get(k).hasThName( th_name) ) return k;
    return -1;
  }

  /** @return the index of a symbol given its Therion name or group
   * @param name  symbol Therion name 
   * @param group symbol group
   */
  int getSymbolIndexByThNameOrGroup( String name, String group )
  {
    String th_name = Symbol.deprefix_u( name );
    int nr = mSymbols.size();
    // TDLog.v("ThName <" + th_name + "> group <" + ( (group == null)? "null" : group ) + ">" );
    for ( int k=0; k<nr; ++k ) if ( mSymbols.get(k).hasThName( th_name) ) return k;
    for ( int k=0; k<nr; ++k ) if ( mSymbols.get(k).hasGroup( group) ) return k;
    return -1;
  }

  // int getSymbolIndexByFilename( String fname )
  // {
  //   int nr = mSymbols.size();
  //   for ( int k=0; k<nr; ++k ) if ( mSymbols.get(k).isThName( fname ) ) return k;
  //   return -1;
  // }

  // ===============================================
  // SymbolInterface

  String getSymbolDefaultOptions( int k ) { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getDefaultOptions(); }

  // this is used only by PT Cmap
  boolean hasSymbolByThName( String name ) { return ( null != get( name ) ); }

  // this is used by loadUserXXX 
  // boolean hasSymbolByFilename( String fname ) { return ( null != get( fname ) ); }

  // Symbol getSymbolByFilename( String fname ) { return get( fname ); }
  Symbol getSymbolByThName( String name ) { return get( name ); }

  Symbol getSymbolByIndex( int k ) { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get( k ); }

  /** #return the name of the k-th symbol 
   * @param k  symbol index
   */
  String getSymbolName( int k )   { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getName(); }

  /** #return the Therion name of the k-th symbol 
   * @param k  symbol index
   */
  String getSymbolThName( int k ) { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getThName(); }

  /** #return the Therion full-name (including prefix) of the k-th symbol 
   * @param k  symbol index
   */
  String getSymbolFullThName( int k ) { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getFullThName(); }

  /** #return the Therion full-name (including prefix), with ':' replaced by '_', of the k-th symbol 
   * @param k  symbol index
   */
  String getSymbolFullThNameEscapedColon( int k ) { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getFullThNameEscapedColon(); }

  /** #return the group of the k-th symbol 
   * @param k  symbol index
   */
  String getSymbolGroup( int k )  { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getGroup(); }

  /** #return the paint of the k-th symbol 
   * @param k  symbol index
   */
  Paint getSymbolPaint( int k )   { return ( k < 0 || k >= mSymbols.size() )? BrushManager.errorPaint : mSymbols.get(k).getPaint(); }

  /** #return the path of the k-th symbol 
   * @param k  symbol index
   */
  Path  getSymbolPath( int k )    { return ( k < 0 || k >= mSymbols.size() )? null : mSymbols.get(k).getPath(); }

  /** #return true if the k-th symbol is orientable
   * @param k  symbol index
   */
  boolean isSymbolOrientable( int k )   { return k >= 0 && k < mSymbols.size() && mSymbols.get( k ).isOrientable(); }

  /** #return true if the k-th symbol is declinable
   * @param k  symbol index
   */
  boolean isSymbolDeclinable( int k )   { return k >= 0 && k < mSymbols.size() && mSymbols.get( k ).isDeclinable(); }

  /** #return true if the k-th symbol is enabled
   * @param k  symbol index
   */
  boolean isSymbolEnabled( int k )      { return k >= 0 && k < mSymbols.size() && mSymbols.get( k ).isEnabled(); }

  /** #return the level of the k-th symbol 
   * @param k  symbol index
   */
  int getSymbolLevel( int k ) { return ( ( k<0 || k>=mSymbols.size() )? 0xff : mSymbols.get( k ).mLevel ); }

  /** @return an array with the symbol names
   */
  ArrayList< String > getSymbolNames()
  {
    ArrayList< String > ret = new ArrayList<>();
    for ( Symbol s : mSymbols ) ret.add( s.getName() );
    return ret;
  }

  /** @return an array with the symbol names except a name
   * @param skip name to skip
   */
  ArrayList< String > getSymbolNamesExcept( String skip )
  {
    ArrayList< String > ret = new ArrayList<>();
    for ( Symbol s : mSymbols ) {
      if ( s.getThName().equals( skip ) ) continue;
      ret.add( s.getName() );
    }
    return ret;
  }

  /** @return true if a symbol is enabled
   * @param th_name   symbol Therion name
   */
  boolean isSymbolEnabled( String th_name ) 
  {
    Symbol a = get( th_name );
    return ( a != null ) && a.isEnabled();
  }

  void resetOrientations() { for ( Symbol s : mSymbols ) s.setAngle(0); }
  
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
  //     File file = TDFile.getFile( filename );
  //     if ( ! file.exists() ) return false;
  //     symbol = new Symbol( file.getPath(), locale, iso );
  //     mSymbols.add( symbol );
  //   }
  //   if ( symbol == null ) return false;
  //   // TDLog.v( "enabling missing symbol " + prefix + th_name );
  //   symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.getThName() ) );
  //   makeEnabledList( );
  //   return true;
  // }

  /** set the config values of the symbols from the configuration
   */
  void makeConfigList()
  {
    if ( TopoDroidApp.mData == null ) return;
    // TDLog.v("Symbol Lib " + mPrefix + ": make config list - symbols " + mSymbols.size() );
    for ( Symbol symbol : mSymbols ) {
      symbol.setConfigEnabled( TopoDroidApp.mData.getSymbolEnabled( mPrefix + symbol.getThName() ) );
      symbol.setEnabledConfig();
    }
  }

  // prefix: p_ l_ a_
  protected void makeConfigEnabledList( )
  {
    if ( TopoDroidApp.mData == null ) return;
    // TDLog.v("Symbol Lib " + mPrefix + ": make config enabled list - symbols " + mSymbols.size() );
    for ( Symbol symbol : mSymbols ) {
      // TopoDroidApp.mData.setSymbolEnabled( mPrefix + symbol.getThName(), symbol.isEnabled() ); // CONFIG_ENABLE
      symbol.setConfigEnabled( ); 
    }
  }

  protected void makeSpecialIndices() { }

  // prefix: p_ l_ a_
  // protected void makeEnabledList( )
  // {
  //   if ( TopoDroidApp.mData == null ) return;
  //   // TDLog.v("Symbol lib make enabled list");
  //   for ( Symbol symbol : mSymbols ) {
  //     // TopoDroidApp.mData.setSymbolEnabled( mPrefix + symbol.getThName(), symbol.isEnabled() ); // CONFIG_ENABLE
  //   }
  // }

  /** make the list of enabled symbols starting from a palette
   * @param symbols    filenames of the palette
   * @param clear      whether to clear the current enable list first, ie, disable, all symbols first
   */
  void makeEnabledListFromStrings( TreeSet<String> symbols, boolean clear )
  {
    // TDLog.v("Symbol lib " + mPrefix + ": make enabled list from palette - palette " + symbols.size() );  
    if ( clear ) {
      for ( Symbol symbol : mSymbols ) {
        // TDLog.v("symbol clear " + symbol.getThName() );
        symbol.setEnabled( false );
      }
    }
    for ( String name : symbols ) {
      Symbol symbol = getSymbolByThName( name );
      if ( symbol != null ) {
        symbol.setEnabled( true );
        if ( ! symbol.isConfigEnabled() ) {
          // TDLog.v("symbol set config " + name );
          symbol.setConfigEnabled( true );
        }
      }
    }
    // makeEnabledList( ); // ENABLED_LIST - CONFIG_ENABLE not needed, maybe 
    makeSpecialIndices();
  }

  void makeEnabledListFromConfig( boolean log )
  {
    // TDLog.v("Symbol lib " + mPrefix + ": make enabled list from config" );
    // for ( Symbol symbol : mSymbols ) symbol.setEnabled( false );
    for ( Symbol symbol : mSymbols ) {
      // boolean enabled = TopoDroidApp.mData.getSymbolEnabled( mPrefix + symbol.getThName() );
      // symbol.setEnabled( enabled );
      symbol.setEnabledConfig(); // CONFIG_ENABLE
      // if ( log ) TDLog.v("Symbol " + symbol.getThName() + " enabled " + enabled );
    }
  }

  /** set the array of recently used symbols
   * @param recent  array of recent symbols
   */
  void setRecentSymbols( Symbol[] recent )
  {
    int k = 0;
    for ( Symbol symbol : mSymbols ) {
      if ( symbol.isEnabled() ) {
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

  /** serialize the symbols 
   * @param dos   data output stream
   */
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
    } catch ( IOException e ) {
      TDLog.e( e.getMessage() );
    }
  }

  // -------------------------------------------------------------
  static final private boolean BLACK = true;
  static final private boolean RED = false;

  /** compare two strings, return -1, 0, +1 as strcmp()
   * @param s1   first string
   * @param s2   second string
   */
  static private int compare( String s1, String s2 )
  { 
    if ( s1 == null ) return ((s2 == null)? 0 : +1);
    if ( s2 == null ) return -1;
    int l1 = s1.length();
    int l2 = s2.length();
    int kk = Math.min(l1, l2);
    for ( int k=0; k < kk; ++k ) {
      if ( s1.charAt(k) < s2.charAt(k) ) return -1;
      if ( s1.charAt(k) > s2.charAt(k) ) return +1;
    }
    if ( l1 < l2 ) return -1;
    if ( l1 > l2 ) return +1;
    return 0;
  }

  /** symbol node in the RB-tree of symbols
   */
  private static class SymbolNode
  {
    SymbolNode parent;
    SymbolNode left;
    SymbolNode right;
    boolean color;
    Symbol value;    // the Node value is the Symbol

    /** cstr
     * @param v  symbol
     */
    SymbolNode( Symbol v )
    {
      parent = null;
      left = null;
      right = null;
      color = RED;
      value = v;
    }

    /** @return the symbol given the name (= filename)
     * @param name  query key (symbol filename)
     */
    Symbol get( String name )
    {
      int c = compare( value.getThName(), name );
      if ( c == 0 ) return value;
      if ( c < 0 ) return ( left == null )? null : left.get( name );
      return ( right == null )? null : right.get( name );
    }
  }

  // -----------------------------------------------------
  /** insert a node in the RB-tree: case 1
   * @param n   node
   */
  private void insert_case1( SymbolNode n ) 
  {
    if ( n.parent == null ) {
      n.color = BLACK;
    } else {
      insert_case2( n );
    }
  }

  /** insert a node in the RB-tree: case 2
   * @param n   node
   * @note the parent of the node is non-null
   */
  private void insert_case2( SymbolNode n ) 
  {
    if ( n.parent.color ) return; // isBlack( n.parent )
    insert_case3( n );
  }

  /** insert a node in the RB-tree: case 3
   * @param n   node
   * @note the parent of the node is RED and non-null 
   */
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

  /** insert a node in the RB-tree: case 4
   * @param n   node
   * @note the parent of the node is RED, and
   *   the node is the RIGHT child of the parent, and the parent is a LEFT child of the grandparent,
   *   or the symmetric case
   */
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

  /** insert a node in the RB-tree: case 5
   * @param n   node
   * @note the parent of the node is RED, but the uncle node is BLACK
   */
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

  /** @return the grandparent of a node (or null)
   * @param n   node
   */
  private SymbolNode grandparent( SymbolNode n )
  {
    return ( n != null && n.parent != null )?  n.parent.parent : null;
  }

  /** @return the uncle of a node (or null): the brother of the parent
   * @param n   node
   */
  private SymbolNode uncle( SymbolNode n ) 
  {
    SymbolNode p = n.parent;
    if ( p == null ) return null;
    SymbolNode g = p.parent;
    if ( g == null ) return null;
    return ( p == g.left )? g.right : g.left;
  }

  // private boolean isBlack( SymbolNode n ) { return ( n == null ) || n.color; }

  /** @return true if the node is RED
   * @param n   node
   */
  private boolean isRed( SymbolNode n ) { return ( n != null ) && (! n.color ); }

  /** @return true if the node is LEFT child
   * @param n   node
   * @note prerequisite: the node parentis non-null
   */
  private boolean isLeft( SymbolNode n ) { return ( n == n.parent.left ); }

  /** @return true if the node is RIGHT child
   * @param n   node
   * @note prerequisite: the node parentis non-null
   */
  private boolean isRight( SymbolNode n ) { return ( n == n.parent.right ); }

}    
