/* @file SymbolLineLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: line symbol library
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDLocale;

// import android.util.Log;

// import java.util.Locale;
// import java.util.ArrayList;
// import java.util.TreeSet;

import java.io.File; // external app files

import android.graphics.Paint;
import android.graphics.DashPathEffect;
import android.content.res.Resources;

public class SymbolLineLibrary extends SymbolLibrary
{
  static final private String[] DefaultLines = {
    ARROW, BORDER, CHIMNEY, PIT, WALL_PRESUMED, ROCK_BORDER, SLOPE
  };

  int mLineUserIndex; // PRIVATE
  int mLineWallIndex;
  int mLineSlopeIndex;
  int mLineSectionIndex;

  SymbolLineLibrary( Resources res )
  {
    super( "l_" );
    mLineUserIndex    =  0;
    mLineWallIndex    = -1;
    mLineSlopeIndex   = -1;
    mLineSectionIndex = -1;
    loadSystemLines( res );
    loadUserLines();
    makeEnabledList();
  }

  // int size() { return mLine.size(); }

  boolean isStyleStraight( int k ) { return ( k < 0 || k >= size() ) || ((SymbolLine)mSymbols.get(k)).mStyleStraight; }

  boolean isClosed( int k ) { return k >= 0 && k < size() && ((SymbolLine)mSymbols.get(k)).mClosed; }

  int getStyleX( int k ) { return ( k < 0 || k >= size() )? 1 : ((SymbolLine)mSymbols.get(k)).mStyleX; }

  // String getLineGroup( int k ) { return ( k < 0 || k >= size() )? null : ((SymbolLine)mSymbols.get(k)).mGroup; }

  boolean isWall( int k ) { return k >= 0 && k < size() && WALL.equals(((SymbolLine)mSymbols.get(k)).mGroup); }

  boolean hasEffect( int k ) { return k >= 0  && k < size() && ((SymbolLine)mSymbols.get(k)).mHasEffect; }

  Paint getLinePaint( int k, boolean reversed )
  {
    if ( k < 0 || k >= size() ) return BrushManager.errorPaint;
    SymbolLine s = (SymbolLine)mSymbols.get(k);
    return reversed ? s.mRevPaint : s.mPaint;
  }

  // ========================================================================

  private void loadSystemLines( Resources res )
  {
    if ( mSymbols.size() > 0 ) return;                                  //  th_name   group fname
    // String user = res.getString ( R.string.p_user );
    SymbolLine symbol = new SymbolLine( res.getString( R.string.thl_user ), USER, null, USER, 0xffffffff, 1, DrawingLevel.LEVEL_USER, Symbol.W2D_DETAIL_SHP );
    addSymbol( symbol );

    // String wall = res.getString ( R.string.p_wall );
    symbol = new SymbolLine( res.getString( R.string.thl_wall ), WALL, WALL, WALL, 0xffff0000, 2, DrawingLevel.LEVEL_WALL, Symbol.W2D_WALLS_SHP );
    addSymbol( symbol );

    float[] x = new float[2];
    x[0] = 5;
    x[1] = 10;
    DashPathEffect dash = new DashPathEffect( x, 0 );
    // String section = res.getString ( R.string.p_section );
    symbol = new SymbolLine( res.getString( R.string.thl_section ), SECTION, null, SECTION, 0xffcccccc, 1, dash, dash, DrawingLevel.LEVEL_USER, Symbol.W2D_DETAIL_SHP );
    addSymbol( symbol );

    // mSymbolNr = mSymbols.size();
  }

  void loadUserLines()
  {
    String locale = "name-" + TDLocale.getLocaleCode(); // TopoDroidApp.mLocale.toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    File dir = TDFile.getExternalDir( TDPath.getSymbolLineDirname() );
    if ( dir.exists() ) {
      int systemNr = mSymbols.size();
      File[] files = dir.listFiles();
      if ( files == null ) return;
      for ( File file : files ) {
        String fname = file.getName();

        // if ( fname.equals(USER) || fname.equals(WALL) || fname.equals(SECTION) ) continue;

        SymbolLine symbol = new SymbolLine( file.getPath(), fname, locale, iso );
        if ( symbol.isThName( null ) ) {
          TDLog.Error( "line with null ThName " + fname );
          // Log.v( "DistoX-SL", "line with null ThName " + fname );
          continue;
        }
        if ( ! hasSymbolByThName( symbol.getThName() ) ) {
          addSymbol( symbol );
          String thname = symbol.getThName();
          String name = mPrefix + thname;
          boolean enable = false;
          if ( ! TopoDroidApp.mData.hasSymbolName( name ) ) {
            for ( int k=0; k<DefaultLines.length; ++k ) { 
              if ( DefaultLines[k].equals( thname ) ) { enable = true; break; }
            }
            TopoDroidApp.mData.setSymbolEnabled( name, enable );
          } else {
            enable = TopoDroidApp.mData.getSymbolEnabled( name );
          }
          symbol.setEnabled( enable );
        } else {
          TDLog.Error( "line " + symbol.getThName() + " already in library" );
          // Log.v( "DistoX-SL", "line " + symbol.getThname() + " already in library" );
        }
      }
      // mSymbolNr = mSymbols.size();
      sortSymbolByName( systemNr );
    } else {
      if ( ! dir.mkdirs( ) ) TDLog.Error( "mkdir error" );
    }
  }

  /* LOAD_MISSING - APP_SAVE SYMNBOLS
  // thname  symbol th-name
  boolean tryLoadMissingLine( String thname )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    if ( isSymbolEnabled( thname ) ) return true;
    Symbol symbol = getSymbolByThName( thname );
    if ( symbol == null ) {
      String filename = Symbol.deprefix_u( thname );
      // Log.v( "DistoX", "load missing line " + thname + " filename " + filename );
      File file = TDFile.getFile( TDPath.getSymbolSaveLinePath( filename ) );
      if ( ! file.exists() ) return false;
      symbol = new SymbolLine( file.getPath(), file.getName(), locale, iso );
      addSymbol( symbol );
    // } else {
    //   // Log.v( TopoDroidApp.TAG, "enabling missing line " + thname );
    }
    // if ( symbol == null ) return false; // ALWAYS false

    symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.getThname() ) );
    
    makeEnabledList();
    return true;
  }
  */
  
  @Override
  protected void makeEnabledList()
  {
    // mLine.clear();
    super.makeEnabledList();
    mLineUserIndex    = getSymbolIndexByThName( USER );
    mLineWallIndex    = getSymbolIndexByThName( WALL );
    mLineSlopeIndex   = getSymbolIndexByThName( SLOPE );
    mLineSectionIndex = getSymbolIndexByThName( SECTION );
  }

  void makeEnabledListFromPalette( SymbolsPalette palette, boolean clear )
  {
    makeEnabledListFromStrings( palette.mPaletteLine, clear ); 
  }

  // ArrayList< String > getSymbolNamesNoSection()
  // {
  //   ArrayList< String > ret = new ArrayList<>();
  //   for ( int k = 0; k < mSymbols.size(); ++ k ) {
  //     if ( k != mTypeSection ) ret.add( s.getName() );
  //   }
  //   return ret;
  // }

}    
