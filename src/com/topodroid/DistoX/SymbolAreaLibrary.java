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
import android.graphics.Bitmap;
import android.graphics.Shader.TileMode;
import android.content.res.Resources;

import android.util.Log;

class SymbolAreaLibrary extends SymbolLibrary
{
  static final String DefaultAreas[] = {
    "blocks", "clay", "debris", "sand"
  };
  int mAreaUserIndex;

  SymbolAreaLibrary( Resources res )
  {
    super( "a_" );
    mAreaUserIndex = 0;
    loadSystemAreas( res );
    loadUserAreas();
    makeEnabledList();
    // Log.v("DistoX", "Areas " + mSymbolNr );
    // for ( Symbol s : mSymbols ) Log.v("DistoX", "area " + s.getName() + " " + s.getThName() );
  }

  double getAreaOrientation( int k )
  {
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s == null )? 0 : s.mOrientation;
  }

  void rotateGrad( int k, double a ) 
  {
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    if ( s != null ) s.rotateGrad( a );
  }

  Bitmap getAreaBitmap( int k ) 
  { 
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s == null )? null : s.mBitmap; 
  }
  
  TileMode getAreaXMode( int k ) 
  { 
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s == null )? TileMode.REPEAT : s.mXMode;
  }
  
  TileMode getAreaYMode( int k )
  { 
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s == null )? TileMode.REPEAT : s.mYMode;
  }
  
  int getAreaColor( int k )
  {
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s == null )? 0xffffffff : s.mColor;
  }
  
  int areaCsxLayer( int k )    { return getSymbolCsxLayer(k); }
  int areaCsxType( int k )     { return getSymbolCsxType(k); }
  int areaCsxCategory( int k ) { return getSymbolCsxCategory(k); }
  int areaCsxPen( int k )      { return getSymbolCsxPen(k); }
  int areaCsxBrush( int k )    { return getSymbolCsxBrush(k); }

  // ========================================================================

  private void loadSystemAreas( Resources res )
  {
    // Log.v( TopoDroidApp.TAG, "load system areas");
    if ( size() > 0 ) return;

    SymbolArea symbol = new SymbolArea( res.getString( R.string.tha_user ),  "user", "user", 0x66ffffff, null, TileMode.REPEAT, TileMode.REPEAT );
    symbol.mCsxLayer = 2;
    symbol.mCsxType  = 3;   
    symbol.mCsxCategory = 46;
    symbol.mCsxPen   = 1;
    symbol.mCsxBrush = 2;
    addSymbol( symbol );

    symbol = new SymbolArea( res.getString( R.string.tha_water ),  "water", "water", 0x660000ff, null, TileMode.REPEAT, TileMode.REPEAT );
    symbol.mCsxLayer = 2;
    symbol.mCsxType  = 3;   
    symbol.mCsxCategory = 46;
    symbol.mCsxPen   = 1;
    symbol.mCsxBrush = 2;
    addSymbol( symbol );
  }

  void loadUserAreas()
  {
    String locale = "name-" + TopoDroidApp.mLocale.toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    File dir = new File( TopoDroidPath.APP_AREA_PATH );
    if ( dir.exists() ) {
      int systemNr = size();
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolArea symbol = new SymbolArea( file.getPath(), file.getName(), locale, iso );
        if ( symbol.mThName == null ) {
          TopoDroidLog.Error( "area with null ThName" );
          continue;
        }
        if ( ! hasSymbolByFilename( symbol.mThName ) ) {
          addSymbol( symbol );
          String thname = symbol.mThName;
          String name = mPrefix + thname;
          boolean enable = false;
          if ( ! TopoDroidApp.mData.hasSymbolName( name ) ) {
            for ( int k=0; k<DefaultAreas.length; ++k ) { 
              if ( DefaultAreas[k].equals( thname ) ) { enable = true; break; }
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

  boolean tryLoadMissingArea( String fname )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    Symbol symbol = getSymbolByFilename( fname );
    if ( symbol == null ) {
      String filename = TopoDroidPath.APP_SAVE_AREA_PATH + fname;
      File file = new File( filename );
      if ( ! file.exists() ) return false;
      symbol = new SymbolArea( file.getPath(), file.getName(), locale, iso );
      addSymbol( symbol );
    }
    if ( symbol == null ) return false;
    if ( ! symbol.isEnabled() ) {
      symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( mPrefix + symbol.mThName ) );
      makeEnabledList();
    }
    return true;
  }

  void makeEnabledListFromPalette( SymbolsPalette palette )
  {
    makeEnabledListFromStrings( palette.mPaletteArea );
  }

}    
