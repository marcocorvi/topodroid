/** @file SymbolAreaLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: area symbol library
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121201 created
 * 20121211 locale
 * 20131119 area color getter
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

class SymbolAreaLibrary
{
  // ArrayList< SymbolArea > mArea;
  ArrayList< SymbolArea > mAnyArea;
  int mAreaUserIndex;
  // int mAreaNr;
  int mAnyAreaNr;

  SymbolAreaLibrary( Resources res )
  {
    // Log.v(  TopoDroidApp.TAG, "cstr SymbolAreaLibrary()" );
    // mArea = new ArrayList< SymbolArea >();
    mAnyArea = new ArrayList< SymbolArea >();
    mAreaUserIndex = 0;
    loadSystemAreas( res );
    loadUserAreas();
    makeEnabledList();
  }

  // int size() { return mArea.size(); }

  // SymbolArea getArea( int k ) 
  // {
  //   if ( k < 0 || k >= mmAnyAreaNr ) return null;
  //   return mArea.get( k );
  // }

  SymbolArea getAnyArea( int k ) 
  {
    if ( k < 0 || k >= mAnyAreaNr ) return null;
    return mAnyArea.get( k );
  }

  boolean hasArea( String th_name ) 
  {
    for ( SymbolArea a : mAnyArea ) {
      if ( th_name.equals( a.mThName ) ) {
        return a.isEnabled();
      }
    }
    return false;
  }

  boolean hasAnyArea( String th_name ) 
  {
    for ( SymbolArea a : mAnyArea ) {
      if ( th_name.equals( a.mThName ) ) {
        return true;
      }
    }
    return false;
  }

  private SymbolArea getSymbolAnyArea( String th_name )
  {
    for ( SymbolArea a : mAnyArea ) {
      if ( th_name.equals( a.mThName ) ) return a;
    }
    return null;
  }

  // boolean removeArea( String th_name ) 
  // {
  //   for ( SymbolArea a : mAnyArea ) {
  //     if ( th_name.equals( a.mThName ) ) {
  //       a.setEnabled( false ); // mAnyArea.remove( a );
  //       TopoDroidApp.mData.setSymbolEnabled( "a_" + th_name, false );
  //       return true;
  //     }
  //   }
  //   return false;
  // }

  String getAreaName( int k )
  {
    if ( k < 0 || k >= mAnyAreaNr ) return null;
    return mAnyArea.get(k).mName;
  }

  String getAreaThName( int k )
  {
    if ( k < 0 || k >= mAnyAreaNr ) return null;
    return mAnyArea.get(k).mThName;
  }

  Paint getAreaPaint( int k )
  {
    if ( k < 0 || k >= mAnyAreaNr ) return null;
    return mAnyArea.get(k).mPaint;
  }

  int getAreaColor( int k )
  {
    if ( k < 0 || k >= mAnyAreaNr ) return 0xffffffff; // white
    return mAnyArea.get(k).mColor;
  }
  
  int areaCsxLayer( int k )
  {
    if ( k < 0 || k >= mAnyAreaNr ) return -1;
    return mAnyArea.get(k).mCsxLayer;
  }

  int areaCsxType( int k )
 {
    if ( k < 0 || k >= mAnyAreaNr ) return -1;
    return mAnyArea.get(k).mCsxType;
  }

  int areaCsxCategory( int k )
  {
    if ( k < 0 || k >= mAnyAreaNr ) return -1;
    return mAnyArea.get(k).mCsxCategory;
  }

  int areaCsxPen( int k )
  {
    if ( k < 0 || k >= mAnyAreaNr ) return -1;
    return mAnyArea.get(k).mCsxPen;
  }

  int areaCsxBrush( int k )
  {
    if ( k < 0 || k >= mAnyAreaNr ) return -1;
    return mAnyArea.get(k).mCsxBrush;
  }

  // ========================================================================

  private void loadSystemAreas( Resources res )
  {
    // Log.v( TopoDroidApp.TAG, "load system areas");
    if ( mAnyArea.size() > 0 ) return;

    SymbolArea symbol = new SymbolArea( res.getString( R.string.tha_user ),  "user",  0x66ffffff );
    symbol.mCsxLayer = 2;
    symbol.mCsxType  = 3;   
    symbol.mCsxCategory = 46;
    symbol.mCsxPen   = 1;
    symbol.mCsxBrush = 2;
    mAnyArea.add( symbol );

    symbol = new SymbolArea( res.getString( R.string.tha_water ),  "water",  0x660000ff );
    symbol.mCsxLayer = 2;
    symbol.mCsxType  = 3;   
    symbol.mCsxCategory = 46;
    symbol.mCsxPen   = 1;
    symbol.mCsxBrush = 2;
    mAnyArea.add( symbol );

    mAnyAreaNr = mAnyArea.size();
  }

  void loadUserAreas()
  {
    // Log.v( TopoDroidApp.TAG, "load user areas");
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    File dir = new File( TopoDroidPath.APP_AREA_PATH );
    if ( dir.exists() ) {
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolArea symbol = new SymbolArea( file.getPath(), locale, iso );
        if ( ! hasAnyArea( symbol.mThName ) ) {
          mAnyArea.add( symbol );
          symbol.setEnabled( TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.mThName ) );
        }
      }
      mAnyAreaNr = mAnyArea.size();
    } else {
      dir.mkdirs( );
    }
  }

  boolean tryLoadMissingArea( String p )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    if ( hasArea( p ) ) return true;
    SymbolArea symbol = getSymbolAnyArea( p );
    if ( symbol == null ) {
      // Log.v( TopoDroidApp.TAG, "load missing area " + p );
      File file = new File( TopoDroidPath.APP_SAVE_AREA_PATH + p );
      if ( ! file.exists() ) return false;

      symbol = new SymbolArea( file.getPath(), locale, iso );
      mAnyArea.add( symbol );
    } else {
      // Log.v( TopoDroidApp.TAG, "enabling missing area " + p );
    }
    if ( symbol == null ) return false;

    symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.mThName ) );
    
    makeEnabledList();
    return true;
  }

  void makeEnabledList()
  {
    // Log.v( TopoDroidApp.TAG, "make enabled list before: " + mAnyArea.size() );
    // mArea.clear();
    for ( SymbolArea symbol : mAnyArea ) {
      TopoDroidApp.mData.setSymbolEnabled( "a_" + symbol.mThName, symbol.mEnabled );
      // Log.v( TopoDroidApp.TAG, "area symbol " + symbol.mThName + " enabled " + symbol.mEnabled );
      // if ( symbol.mEnabled ) {
      //   mArea.add( symbol );
      // }
    }
    // mAreaNr = mArea.size();
    // Log.v( TopoDroidApp.TAG, "make enabled list after: " + mAnyArea.size() );
  }

  void makeEnabledListFromPalette( SymbolsPalette palette )
  {
    for ( SymbolArea symbol : mAnyArea ) symbol.setEnabled( false );
    for ( String p : palette.mPaletteArea ) {
      SymbolArea symbol = getSymbolAnyArea( p );
      if ( symbol != null ) symbol.setEnabled( true );
    }
    makeEnabledList();
  }

  void writePalette( PrintWriter pw ) 
  {
    for ( SymbolArea symbol : mAnyArea ) {
      if ( symbol.isEnabled( ) ) pw.format( " %s", symbol.getThName() );
    }
  }

}    
