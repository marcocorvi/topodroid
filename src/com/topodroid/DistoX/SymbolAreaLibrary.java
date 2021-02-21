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

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDLocale;

// import java.util.Locale;
// import java.util.ArrayList;
// import java.util.TreeSet;
import java.io.File;

// import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.BitmapShader;
import android.graphics.Shader.TileMode;
import android.content.res.Resources;

// import android.util.Log;

class SymbolAreaLibrary extends SymbolLibrary
{
  static final private String[] DefaultAreas = {
    "blocks", "clay", "debris", "sand"
  };
  /* private */ int mAreaUserIndex;

  SymbolAreaLibrary( Resources res )
  {
    super( "a_" );
    mAreaUserIndex = 0;
    loadSystemAreas( res );
    loadUserAreas();
    makeEnabledList();
    // Log.v("DistoX", "Areas " + size() );
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
  
  // ========================================================================

  private void loadSystemAreas( Resources res )
  {
    // Log.v( TopoDroidApp.TAG, "load system areas");
    if ( size() > 0 ) return;

    String user = res.getString( R.string.p_user );
    SymbolArea symbol = new SymbolArea( res.getString( R.string.tha_user ),  "u:user", null, user, 0x67cccccc, null, TileMode.REPEAT, TileMode.REPEAT, false, DrawingLevel.LEVEL_USER, Symbol.W2D_DETAIL_SHP );
    addSymbol( symbol );

    String water = res.getString( R.string.p_water );
    symbol = new SymbolArea( res.getString( R.string.tha_water ), water, null, water, 0x663366ff, null, TileMode.REPEAT, TileMode.REPEAT, true, DrawingLevel.LEVEL_WATER, Symbol.W2D_DETAIL_SHP );
    addSymbol( symbol );
  }

  void loadUserAreas()
  {
    String locale = "name-" + TDLocale.getLocaleCode(); // TopoDroidApp.mLocale.toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";
    // Log.v("DistoX", "area user symbols. locale <" + locale + ">");

    File dir = new File( TDPath.getSymbolAreaDir() );
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
        if ( ! hasSymbolByThName( symbol.mThName ) ) {
          addSymbol( symbol );
          String thname = symbol.mThName;
          String name   = mPrefix + thname;
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
        } else {
          TDLog.Error( "area " + symbol.mThName + " already in library" );
        }
      }
      sortSymbolByName( systemNr );
    } else {
      if ( ! dir.mkdirs( ) ) TDLog.Error( "mkdir error" );
    }
  }

  /* LOAD_MISSING
  boolean tryLoadMissingArea( String thname )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";
    Symbol symbol = getSymbolByThName( thname );
    // APP_SAVE SYMBOLS
    if ( symbol == null ) {
      String filename = thname.startsWith("u:")? thname.substring(2) : thname; 
      File file = new File( TDPath.getSymbolSaveAreaPath( filename ) );
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
  */

  void makeEnabledListFromPalette( SymbolsPalette palette, boolean clear )
  {
    makeEnabledListFromStrings( palette.mPaletteArea, clear );
  }

}    
