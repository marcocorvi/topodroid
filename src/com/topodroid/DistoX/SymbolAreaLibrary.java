/* @file SymbolAreaLibrary.java
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

import java.util.Locale;
// import java.util.ArrayList;
// import java.util.TreeSet;
import java.io.File;
// import java.io.PrintWriter;
// import java.io.DataOutputStream;
// import java.io.IOException;

// import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.BitmapShader;
import android.graphics.Shader.TileMode;
import android.content.res.Resources;

// import android.util.Log;

class SymbolAreaLibrary extends SymbolLibrary
{
  static final private String DefaultAreas[] = {
    "blocks", "clay", "debris", "sand"
  };
  private int mAreaUserIndex;

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

  boolean isCloseHorizontal( int k ) 
  {
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s != null ) && s.mCloseHorizontal;
  }

  // FIXME AREA_ORIENT
  double getAreaOrientation( int k )
  {
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s == null )? 0 : s.mOrientation;
  }

  // FIXME AREA_ORIENT
  void rotateGrad( int k, double a ) 
  {
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    if ( s != null ) s.rotateGradArea( a );
  }

  Bitmap getAreaBitmap( int k ) 
  { 
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s == null )? null : s.mBitmap; 
  }
  
  Shader getAreaShader( int k ) 
  { 
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    return ( s == null )? null : s.mShader; 
  }
   
  BitmapShader cloneAreaShader( int k ) 
  { 
    SymbolArea s = (SymbolArea)getSymbolByIndex(k);
    if ( s == null ) return null; 
    Bitmap b = s.mShaderBitmap;
    if ( b == null ) return null;
    return new BitmapShader( b, s.mXMode, s.mYMode );
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

    SymbolArea symbol = new SymbolArea( res.getString( R.string.tha_user ),  "u:user", "user", 0x67cccccc, null, TileMode.REPEAT, TileMode.REPEAT, false );
    symbol.mCsxLayer = 2;
    symbol.mCsxType  = 3;   
    symbol.mCsxCategory = 46;
    symbol.mCsxPen   = 1;
    symbol.mCsxBrush = 2;
    addSymbol( symbol );

    symbol = new SymbolArea( res.getString( R.string.tha_water ),  "water", "water", 0x663366ff, null, TileMode.REPEAT, TileMode.REPEAT, true );
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

    File dir = new File( TDPath.APP_AREA_PATH );
    if ( dir.exists() ) {
      int systemNr = size();
      File[] files = dir.listFiles();
      if ( files == null ) return;
      for ( File file : files ) {
        SymbolArea symbol = new SymbolArea( file.getPath(), file.getName(), locale, iso );
        if ( symbol.mThName == null ) {
          TDLog.Error( "area with null ThName " + file.getName() );
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
      if ( ! dir.mkdirs( ) ) TDLog.Error( "mkdir error" );
    }
  }

  boolean tryLoadMissingArea( String fname )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    Symbol symbol = getSymbolByFilename( fname );
    // APP_SAVE SYMBOLS
    if ( symbol == null ) {
      String filename = TDPath.APP_SAVE_AREA_PATH + fname;
      File file = new File( filename );
      if ( ! file.exists() ) return false;
      symbol = new SymbolArea( file.getPath(), file.getName(), locale, iso );
      addSymbol( symbol );
    }
    // if ( symbol == null ) return false; // ALWAYS false
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
