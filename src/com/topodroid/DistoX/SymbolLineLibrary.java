/** @file SymbolLineLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: line symbol library
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Locale;
import java.util.ArrayList;
import java.util.TreeSet;
import java.io.File;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Paint;
import android.content.res.Resources;

import android.util.Log;

class SymbolLineLibrary extends SymbolLibrary
{
  static final String DefaultLines[] = {
    "arrow", "border", "pit", "rock-border", "slope"
  };

  int mLineUserIndex;
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

  boolean isStyleStraight( int k ) { return ( k < 0 || k >= mSymbolNr )? true : ((SymbolLine)mSymbols.get(k)).mStyleStraight; }

  boolean isClosed( int k ) { return ( k < 0 || k >= mSymbolNr )? false : ((SymbolLine)mSymbols.get(k)).mClosed; }

  int getStyleX( int k ) { return ( k < 0 || k >= mSymbolNr )? 1 : ((SymbolLine)mSymbols.get(k)).mStyleX; }

  String getLineGroup( int k ) { return ( k < 0 || k >= mSymbolNr )? null : ((SymbolLine)mSymbols.get(k)).mGroup; }

  Paint getLinePaint( int k, boolean reversed )
  {
    if ( k < 0 || k >= mSymbolNr ) return null;
    SymbolLine s = (SymbolLine)mSymbols.get(k);
    return reversed ? s.mRevPaint : s.mPaint;
  }

  int lineCsxLayer( int k )    { return getSymbolCsxLayer(k); }
  int lineCsxType( int k )     { return getSymbolCsxType(k); }
  int lineCsxCategory( int k ) { return getSymbolCsxCategory(k); }
  int lineCsxPen( int k )      { return getSymbolCsxPen(k); }
  
  // ========================================================================

  private void loadSystemLines( Resources res )
  {
    if ( mSymbols.size() > 0 ) return;
    SymbolLine symbol = new SymbolLine( res.getString( R.string.thl_user ), "u:user", "user", "user", 0xffffffff, 1 );
    symbol.mCsxLayer    = 0; // base
    symbol.mCsxType     = 1; // free-hand
    symbol.mCsxCategory = 0; // cSurvey line cat: NONE
    symbol.mCsxPen      = 2; // generic border
    addSymbol( symbol );

    symbol = new SymbolLine( res.getString( R.string.thl_wall ),  "wall", "wall", "wall", 0xffff0000, 2 );
    symbol.mCsxLayer    = 5; //
    symbol.mCsxType     = 4; // inverted free-hand
    symbol.mCsxCategory = 1; // cSurvey line cat: CAVE_BORDER
    symbol.mCsxPen      = 1; // cave border
    addSymbol( symbol );

    mSymbolNr = mSymbols.size();
  }

  void loadUserLines()
  {
    String locale = "name-" + TopoDroidApp.mLocale.toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    File dir = new File( TDPath.APP_LINE_PATH );
    if ( dir.exists() ) {
      int systemNr = mSymbols.size();
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolLine symbol = new SymbolLine( file.getPath(), file.getName(), locale, iso );
        if ( symbol.mThName == null ) {
          TDLog.Error( "line with null ThName " + file.getName() );
          continue;
        }
        if ( ! hasSymbolByFilename( symbol.mThName ) ) {
          addSymbol( symbol );
          String thname = symbol.mThName;
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
        }
      }
      // mSymbolNr = mSymbols.size();
      sortSymbolByName( systemNr );
    } else {
      dir.mkdirs( );
    }
  }

  boolean tryLoadMissingLine( String fname )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    if ( isSymbolEnabled( fname ) ) return true;
    Symbol symbol = getSymbolByFilename( fname );
    // APP_SAVE SYMNBOLS
    if ( symbol == null ) {
      // Log.v( TopoDroidApp.TAG, "load missing line " + fname );
      File file = new File( TDPath.APP_SAVE_LINE_PATH + fname );
      if ( ! file.exists() ) return false;
      symbol = new SymbolLine( file.getPath(), file.getName(), locale, iso );
      addSymbol( symbol );
    } else {
      // Log.v( TopoDroidApp.TAG, "enabling missing line " + fname );
    }
    if ( symbol == null ) return false;

    symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.mThName ) );
    
    makeEnabledList();
    return true;
  }
  
  @Override
  protected void makeEnabledList()
  {
    // mLine.clear();
    super.makeEnabledList();
    mLineUserIndex    = getSymbolIndexByThName( "user" );
    mLineWallIndex    = getSymbolIndexByThName( "wall" );
    mLineSlopeIndex   = getSymbolIndexByThName( "slope" );
    mLineSectionIndex = getSymbolIndexByThName( "section" );
  }

  void makeEnabledListFromPalette( SymbolsPalette palette )
  {
    makeEnabledListFromStrings( palette.mPaletteLine ); 
  }

}    
