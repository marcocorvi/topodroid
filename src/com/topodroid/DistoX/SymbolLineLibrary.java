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
 * CHANGES
 * 20121201 created
 * 20121211 locale
 * 20140422 iso
 */
package com.topodroid.DistoX;

import java.util.Locale;
import java.util.ArrayList;
import java.util.TreeSet;
import java.io.File;
import java.io.PrintWriter;

import android.graphics.Paint;
import android.content.res.Resources;

import android.util.Log;

class SymbolLineLibrary
{
  // ArrayList< SymbolLine > mLine;
  ArrayList< SymbolLine > mAnyLine;
  int mLineUserIndex;
  int mLineWallIndex;
  int mLineSlopeIndex;
  int mLineSectionIndex;
  int mAnyLineNr;

  SymbolLineLibrary( Resources res )
  {
    // Log.v( "TopoDroid", "cstr SymbolLineLibrary()" );
    // mLine = new ArrayList< SymbolLine >();
    mAnyLine = new ArrayList< SymbolLine >();
    mLineUserIndex    =  0;
    mLineWallIndex    = -1;
    mLineSlopeIndex   = -1;
    mLineSectionIndex = -1;
    loadSystemLines( res );
    loadUserLines();
    makeEnabledList();
  }

  // int size() { return mLine.size(); }

  // SymbolLine getLine( int k ) 
  // {
  //   if ( k < 0 || k >= mAnyLineNr ) return null;
  //   SymbolLine l =  mAnyLine.get( k );
  //   return l.isEnabled() ? l : null;
  // }

  SymbolLine getAnyLine( int k ) 
  {
    if ( k < 0 || k >= mAnyLineNr ) return null;
    return mAnyLine.get( k );
  }

  boolean hasLine( String th_name ) 
  {
    for ( SymbolLine l : mAnyLine ) {
      if ( th_name.equals( l.mThName ) ) {
        return l.isEnabled();
      }
    }
    return false;
  }

  boolean hasAnyLine( String th_name ) 
  {
    for ( SymbolLine l : mAnyLine ) {
      if ( th_name.equals( l.mThName ) ) {
        return true;
      }
    }
    return false;
  }

  private SymbolLine getSymbolAnyLine( String th_name ) 
  {
    for ( SymbolLine l : mAnyLine ) {
      if ( th_name.equals( l.mThName ) ) return l;
    }
    return null;
  }

  // boolean removeLine( String th_name ) 
  // {
  //   for ( SymbolLine l : mAnyLine ) {
  //     if ( th_name.equals( l.mThName ) ) {
  //       l.setEnabled( false ); // mAnyLine.remove( l );
  //       TopoDroidApp.mData.setSymbolEnabled( "l_" + th_name, false );
  //       return true;
  //     }
  //   }
  //   return false;
  // }

  String getLineName( int k )
  {
    if ( k < 0 || k >= mAnyLineNr ) return null;
    return mAnyLine.get(k).mName;
  }

  String getLineThName( int k )
  {
    if ( k < 0 || k >= mAnyLineNr ) return null;
    return mAnyLine.get(k).mThName;
  }

  Paint getLinePaint( int k, boolean reversed )
  {
    if ( k < 0 || k >= mAnyLineNr ) return null;
    return reversed ? mAnyLine.get(k).mRevPaint : mAnyLine.get(k).mPaint;
  }

  int lineCsxLayer( int k )
  {
    if ( k < 0 || k >= mAnyLineNr ) return -1;
    return mAnyLine.get(k).mCsxLayer;
  }

  int lineCsxType( int k )
  {
    if ( k < 0 || k >= mAnyLineNr ) return -1;
    return mAnyLine.get(k).mCsxType;
  }

  int lineCsxCategory( int k )
  {
    if ( k < 0 || k >= mAnyLineNr ) return -1;
    return mAnyLine.get(k).mCsxCategory;
  }

  int lineCsxPen( int k )
  {
    if ( k < 0 || k >= mAnyLineNr ) return -1;
    return mAnyLine.get(k).mCsxPen;
  }

  
  // ========================================================================

  private void loadSystemLines( Resources res )
  {
    if ( mAnyLine.size() > 0 ) return;
    SymbolLine symbol = new SymbolLine( res.getString( R.string.thl_user ),  "user", 0xffffffff, 1 );
    symbol.mCsxLayer = 5;
    symbol.mCsxType  = 4;
    symbol.mCsxCategory = 1;
    symbol.mCsxPen   = 1;
    mAnyLine.add( symbol );

    symbol = new SymbolLine( res.getString( R.string.thl_wall ),  "wall", 0xffff0000, 2 );
    symbol.mCsxLayer = 5;
    symbol.mCsxType  = 4;
    symbol.mCsxCategory = 1;
    symbol.mCsxPen   = 1;
    mAnyLine.add( symbol );

    mAnyLineNr = mAnyLine.size();
  }

  void loadUserLines()
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    File dir = new File( TopoDroidPath.APP_LINE_PATH );
    if ( dir.exists() ) {
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolLine symbol = new SymbolLine( file.getPath(), locale, iso );
        if ( ! hasAnyLine( symbol.mThName ) ) {
          mAnyLine.add( symbol );
          symbol.setEnabled( TopoDroidApp.mData.isSymbolEnabled( "l_" + symbol.mThName ) );
        }
      }
      mAnyLineNr = mAnyLine.size();
    } else {
      dir.mkdirs( );
    }
  }

  boolean tryLoadMissingLine( String p )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    if ( hasLine( p ) ) return true;
    SymbolLine symbol = getSymbolAnyLine( p );
    if ( symbol == null ) {
      // Log.v( TopoDroidApp.TAG, "load missing line " + p );
      File file = new File( TopoDroidPath.APP_SAVE_LINE_PATH + p );
      if ( ! file.exists() ) return false;

      symbol = new SymbolLine( file.getPath(), locale, iso );
      mAnyLine.add( symbol );
    } else {
      // Log.v( TopoDroidApp.TAG, "enabling missing line " + p );
    }
    if ( symbol == null ) return false;

    symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.mThName ) );
    
    makeEnabledList();
    return true;
  }
      
  void makeEnabledList()
  {
    // mLine.clear();
    mLineUserIndex    =  0;
    mLineWallIndex    = -1;
    mLineSlopeIndex   = -1;
    mLineSectionIndex = -1;
    int k = 0;
    for ( SymbolLine symbol : mAnyLine ) {
      TopoDroidApp.mData.setSymbolEnabled( "l_" + symbol.mThName, symbol.mEnabled );
      if ( symbol.mEnabled ) {
        if ( symbol.mThName.equals("user") )  mLineUserIndex = k;
        if ( symbol.mThName.equals("wall") )  mLineWallIndex = k;
        if ( symbol.mThName.equals("slope") ) mLineSlopeIndex = k;
        if ( symbol.mThName.equals("section") ) mLineSectionIndex = k;
        // mLine.add( symbol );
      }
      ++ k;
    }

    // Log.v( TopoDroidApp.TAG, "lines " + mAnyLine.size() + " wall " + mLineWallIndex + " slope " + mLineSlopeIndex + " section " + mLineSectionIndex );
  }
 
  void makeEnabledListFromPalette( SymbolsPalette palette )
  {
    for ( SymbolLine symbol : mAnyLine ) symbol.setEnabled( false );
    for ( String p : palette.mPaletteLine ) {
      SymbolLine symbol = getSymbolAnyLine( p );
      if ( symbol != null ) symbol.setEnabled( true );
    }
    makeEnabledList();
  }

  void writePalette( PrintWriter pw ) 
  {
    for ( SymbolLine symbol : mAnyLine ) {
      if ( symbol.isEnabled( ) ) pw.format( " %s", symbol.getThName() );
    }
  }

}    
