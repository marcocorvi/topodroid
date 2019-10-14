/* @file SymbolPointLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: point symbol library
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

import java.util.Locale;
// import java.util.ArrayList;
// import java.util.TreeSet;
import java.io.File;

// import android.graphics.Paint;
import android.graphics.Path;

import android.content.Context;
import android.content.res.Resources;

class SymbolPointLibrary extends SymbolLibrary
{
  static final private String[] DefaultPoints = {
    "air-draught", "blocks", "clay", "continuation", "danger", "debris", "dig", "entrance", "helictite", "ice", "pebbles", "pillar", "popcorn", "root", "sand", "section", "snow", "stalactite", "stalagmite", "water-flow"
  };

  // ArrayList< SymbolPoint > mPoint;    // enabled points
  int mPointUserIndex; // PRIVATE
  int mPointLabelIndex;
  int mPointPhotoIndex;
  int mPointAudioIndex;
  // int mPointDangerIndex;
  int mPointSectionIndex;

  SymbolPointLibrary( Context ctx, Resources res )
  {
    super( "p_" );
    mPointUserIndex   = 0;
    mPointLabelIndex  = -1;
    mPointPhotoIndex  = -1;
    mPointAudioIndex  = -1;
    // mPointDangerIndex = -1;
    mPointSectionIndex = -1;
    loadSystemPoints( res );
    loadUserPoints( ctx );
    makeEnabledList();
  }

  boolean pointHasText( int k )        { return k >= 0 && k < size() && ((SymbolPoint)mSymbols.get(k)).mHasText == 1; }
  boolean pointHasValue( int k )       { return k >= 0 && k < size() && ((SymbolPoint)mSymbols.get(k)).mHasText == 2; }
  boolean pointHasTextOrValue( int k ) { return k >= 0 && k < size() && ((SymbolPoint)mSymbols.get(k)).mHasText > 0; }

  double getPointOrientation( int k )
  { return ( k < 0 || k >= size() )? 0.0 : ((SymbolPoint)mSymbols.get(k)).mOrientation; }

  @Override
  void resetOrientations()
  {
    for ( Symbol sp : mSymbols ) ((SymbolPoint)sp).resetOrientation();
  }

  void rotateGrad( int k, double a )
  {
    if ( k >= 0 && k < size() ) ((SymbolPoint)mSymbols.get(k)).rotateGradP( a );
  }

  Path getPointPath( int k ) { 
  {
    return ( k < 0 || k >= size() )? null : ((SymbolPoint)mSymbols.get(k)).getPath( ); }
  }

  Path getPointOrigPath( int k )
  {
    return ( k < 0 || k >= size() )? null : ((SymbolPoint)mSymbols.get(k)).getOrigPath( );
  }

  int pointCsxLayer( int k ) { return getSymbolCsxLayer( k ); }
  int pointCsxType( int k )  { return getSymbolCsxType( k ); }
  int pointCsxCategory( int k ) { return getSymbolCsxCategory( k ); }

  String pointCsx( int k )
  {
    return ( k < 0 || k >= size() )? "" : ((SymbolPoint)mSymbols.get(k)).mCsx;
  }

  // ========================================================================

  static final private String p_label = "moveTo 0 3 lineTo 0 -6 lineTo -3 -6 lineTo 3 -6"; // "T" shape
  static final private String p_user = "addCircle 0 0 6";                                  // "o" shape
  static final private String p_section = "moveTo -5 -5 lineTo -5 5 lineTo 5 5 lineTo 5 -5 lineTo -5 -5"; // square

  private void loadSystemPoints( Resources res )
  {
    SymbolPoint symbol;
    // Log.v(  TopoDroidApp.TAG, "SymbolPointLibrary::loadSystemPoints()" );

    mPointUserIndex = mSymbols.size(); // 0 = no-text, no-value. thname   group fname
    String user = res.getString( R.string.p_user );
    symbol = new SymbolPoint( res.getString(R.string.thp_user), "u:user", null, user, 0xffffffff, p_user, false, 0, DrawingLevel.LEVEL_USER );
    symbol.mCsxLayer = 6;
    symbol.mCsxType  = 8;
    symbol.mCsxCategory = 81;
    addSymbol( symbol );

    mPointLabelIndex = mSymbols.size(); // 1 = text
    String label = res.getString( R.string.p_label );
    symbol = new SymbolPoint( res.getString(R.string.thp_label), label, null, label, 0xffffffff, p_label, true, 1, DrawingLevel.LEVEL_LABEL );
    symbol.mCsxLayer = 6;
    symbol.mCsxType  = 8;
    symbol.mCsxCategory = 81;
    addSymbol( symbol );

    mPointSectionIndex = mSymbols.size();
    String section = res.getString( R.string.p_section );
    symbol = new SymbolPoint( res.getString(R.string.thp_section), section, null, section, 0xffcccccc, p_section, false, 0, DrawingLevel.LEVEL_USER );
    symbol.mCsxLayer = 6;
    symbol.mCsxType  = 6;
    symbol.mCsxCategory = 80;
    addSymbol( symbol );
  }

  void loadUserPoints( Context ctx )
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
      if ( files == null ) {
         TDLog.Error("null symbol-file list" );
	 return;
      }
      for ( File file : files ) { // there is a null-pointer exception here, but files cannot be null !!!
        String fname = file.getName();

        if ( fname.equals("photo") && ! TDandroid.checkCamera( ctx ) ) continue;
        if ( fname.equals("audio") && ! TDandroid.checkMicrophone( ctx ) ) continue;

        // if ( fname.equals("user") || fname.equals("label") || fname.equals("section") ) continue;

        SymbolPoint symbol = new SymbolPoint( file.getPath(), fname, locale, iso );
        if ( symbol.mThName == null ) {
          TDLog.Error( "point with null ThName " + fname );
          continue;
        }
        // Log.v("DistoX-Pt", "Symbol point <" + fname + "> th_name <" + symbol.mThName + ">" );
        if ( ! hasSymbolByThName( symbol.mThName ) ) {
          addSymbol( symbol );
          String thname = symbol.mThName;
          String name = "p_" + thname;
          boolean enable = false;
          if ( symbol.mThName.equals("section") ) { // FIXME_SECTION_POINT always enabled
            enable = true;
	  } else {
            if ( ! TopoDroidApp.mData.hasSymbolName( name ) ) {
              for ( int k=0; k<DefaultPoints.length; ++k ) { 
                if ( DefaultPoints[k].equals( thname ) ) { enable = true; break; }
              }
              TopoDroidApp.mData.setSymbolEnabled( name, enable );
            } else {
              enable = TopoDroidApp.mData.getSymbolEnabled( name );
            }
	  }
          symbol.setEnabled( enable );
        } else {
          TDLog.Error( "point " + symbol.mThName + " already in library" );
        }
      }
      sortSymbolByName( systemNr );
    } else {
      TDLog.Error( "No symbol directory" );
      if ( ! dir.mkdirs( ) ) TDLog.Error( "mkdir error" );
    }
  }

  // thname   symbol th-name
  boolean tryLoadMissingPoint( String thname )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    if ( isSymbolEnabled( thname ) ) return true;
    Symbol symbol = getSymbolByThName( thname );
    // APP_SAVE SYMBOLS
    if ( symbol == null ) {
      String filename = thname.startsWith("u:")? thname.substring(2) : thname ;
      // Log.v( "DistoX", "load missing point " + thname + " filename " + filename );
      File file = new File( TDPath.APP_SAVE_POINT_PATH + filename );
      if ( ! file.exists() ) return false;
      symbol = new SymbolPoint( file.getPath(), file.getName(), locale, iso );
      if ( symbol.mThName == null || symbol.mThName.length() == 0 ) return false;
      addSymbol( symbol );
    // } else {
    //   // Log.v( "DistoX", "enabling missing point " + thname );
    }
    // if ( symbol == null ) return false; // ALWAYS false

    symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.mThName ) );
    makeEnabledList();
    return true;
  }

// ------------------------------------------------------------------
  
  @Override
  protected void makeEnabledList()
  {
    super.makeEnabledList();
    mPointUserIndex    = getSymbolIndexByThName( "user" );
    mPointLabelIndex   = getSymbolIndexByThName( "label" );
    mPointPhotoIndex   = getSymbolIndexByThName( "u:photo" );
    mPointAudioIndex   = getSymbolIndexByThName( "u:audio" );
    // mPointDangerIndex  = getSymbolIndexByThName( "danger" );
    mPointSectionIndex = getSymbolIndexByThName( "section" ); 

    // Log.v("DistoX", "Pt label " + mPointLabelIndex 
    //                 + " photo " + mPointPhotoIndex
    //                 + " audio " + mPointAudioIndex
    //                 + " section " + mPointSectionIndex );
  }

  void makeEnabledListFromPalette( SymbolsPalette palette, boolean clear )
  {
    makeEnabledListFromStrings( palette.mPalettePoint, clear );
  }

}    
