/** @file SymbolPointLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: point symbol library
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

import android.graphics.Paint;
import android.graphics.Path;
import android.content.res.Resources;

import android.util.Log;

class SymbolPointLibrary extends SymbolLibrary
{
  static final String DefaultPoints[] = {
    "air-draught", "blocks", "clay", "continuation", "debris", "entrance", "sand", "stalactite", "stalagmite", "water-flow"
  };

  // ArrayList< SymbolPoint > mPoint;    // enabled points
  int mPointUserIndex;
  int mPointLabelIndex;
  int mPointDangerIndex;


  SymbolPointLibrary( Resources res )
  {
    super( "p_" );
    mPointUserIndex   = 0;
    mPointLabelIndex  = -1;
    mPointDangerIndex = -1;
    loadSystemPoints( res );
    loadUserPoints();
    makeEnabledList();
  }

  boolean pointHasText( int k ) { return ( k < 0 || k >= mSymbolNr )? false : ((SymbolPoint)mSymbols.get(k)).mHasText; }

  double getPointOrientation( int k )
  { return ( k < 0 || k >= mSymbolNr )? 0.0 : ((SymbolPoint)mSymbols.get(k)).mOrientation; }

  @Override
  void resetOrientations()
  {
    for ( Symbol sp : mSymbols ) ((SymbolPoint)sp).resetOrientation();
  }

  void rotateGrad( int k, double a )
  {
    if ( k >= 0 && k < mSymbolNr ) ((SymbolPoint)mSymbols.get(k)).rotateGrad( a );
  }

  Path getPointPath( int k ) { 
  {
    return ( k < 0 || k >= mSymbolNr )? null : ((SymbolPoint)mSymbols.get(k)).getPath( ); }
  }

  Path getPointOrigPath( int k )
  {
    return ( k < 0 || k >= mSymbolNr )? null : ((SymbolPoint)mSymbols.get(k)).getOrigPath( );
  }

  int pointCsxLayer( int k ) { return getSymbolCsxLayer( k ); }
  int pointCsxType( int k )  { return getSymbolCsxType( k ); }
  int pointCsxCategory( int k ) { return getSymbolCsxCategory( k ); }

  String pointCsx( int k )
  {
    return ( k < 0 || k >= mSymbolNr )? "" : ((SymbolPoint)mSymbols.get(k)).mCsx;
  }

  // ========================================================================

  final String p_label = "moveTo 0 3 lineTo 0 -6 lineTo -3 -6 lineTo 3 -6";
  final String p_user = "addCircle 0 0 6";

  private void loadSystemPoints( Resources res )
  {
    SymbolPoint symbol;
    // Log.v(  TopoDroidApp.TAG, "SymbolPointLibrary::loadSystemPoints()" );

    mPointUserIndex = mSymbols.size();
    symbol = new SymbolPoint( res.getString(R.string.thp_user), "user", "user", 0xffffffff, p_user, false, false );
    symbol.mCsxLayer = 6;
    symbol.mCsxType  = 8;
    symbol.mCsxCategory = 81;
    addSymbol( symbol );

    mPointLabelIndex = mSymbols.size();
    symbol = new SymbolPoint( res.getString(R.string.thp_label), "label", "label", 0xffffffff, p_label, false, true );
    symbol.mCsxLayer = 6;
    symbol.mCsxType  = 8;
    symbol.mCsxCategory = 81;
    addSymbol( symbol );
  }

  void loadUserPoints()
  {
    String locale = "name-" + TopoDroidApp.mLocale.toString().substring(0,2);
    // String iso = "ISO-8859-1";
    String iso = "UTF-8";
    // if ( locale.equals( "name-de" ) ) iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";
    // Charset.forName("ISO-8859-1")

    File dir = new File( TDPath.APP_POINT_PATH );
    if ( dir.exists() ) {
      int systemNr = mSymbols.size();
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolPoint symbol = new SymbolPoint( file.getPath(), file.getName(), locale, iso );
        if ( symbol.mThName == null ) {
          TDLog.Error( "point with null ThName" );
          continue;
        }
        if ( ! hasSymbolByFilename( symbol.mThName ) ) {
          addSymbol( symbol );
          String thname = symbol.mThName;
          String name = "p_" + thname;
          boolean enable = false;
          if ( ! TopoDroidApp.mData.hasSymbolName( name ) ) {
            for ( int k=0; k<DefaultPoints.length; ++k ) { 
              if ( DefaultPoints[k].equals( thname ) ) { enable = true; break; }
            }
            TopoDroidApp.mData.setSymbolEnabled( name, enable );
          } else {
            enable = TopoDroidApp.mData.getSymbolEnabled( name );
          }
          symbol.setEnabled( enable );
        }
      }
      sortSymbolByName( systemNr );
    } else {
      dir.mkdirs( );
    }
  }

  boolean tryLoadMissingPoint( String fname )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    if ( isSymbolEnabled( fname ) ) return true;
    Symbol symbol = getSymbolByFilename( fname );
    if ( symbol == null ) {
      // Log.v( TopoDroidApp.TAG, "load missing point " + fname );
      File file = new File( TDPath.APP_SAVE_POINT_PATH + fname );
      if ( ! file.exists() ) return false;

      symbol = new SymbolPoint( file.getPath(), file.getName(), locale, iso );
      addSymbol( symbol );
    } else {
      // Log.v( TopoDroidApp.TAG, "enabling missing point " + fname );
    }
    if ( symbol == null ) return false;

    symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.mThName ) );
    makeEnabledList();
    return true;
  }

// ------------------------------------------------------------------
  
  @Override
  protected void makeEnabledList()
  {
    super.makeEnabledList();
    mPointUserIndex   = getSymbolIndexByThName( "user" );
    mPointLabelIndex  = getSymbolIndexByThName( "label" );
    mPointDangerIndex = getSymbolIndexByThName( "danger" );
  }


  void makeEnabledListFromPalette( SymbolsPalette palette )
  {
    makeEnabledListFromStrings( palette.mPalettePoint );
  }

}    
