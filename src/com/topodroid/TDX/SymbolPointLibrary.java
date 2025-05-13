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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDLocale;

// import java.util.Locale;
// import java.util.ArrayList;
// import java.util.TreeSet;

import java.io.File; // external app files

// import android.graphics.Paint;
import android.graphics.Path;

import android.content.Context;
import android.content.res.Resources;

public class SymbolPointLibrary extends SymbolLibrary
{
  static final private String[] DefaultPoints = {
    AIR_DRAUGHT, BLOCKS, CLAY, CONTINUATION, DANGER, DEBRIS, DIG, ENTRANCE, HELICTITE, ICE, PEBBLES, PILLAR, POPCORN,
    ROOT, SAND, SECTION, SNOW, STALACTITE, STALAGMITE, WATER_FLOW
  };

  // ArrayList< SymbolPoint > mPoint;    // enabled points
  int mPointUserIndex; 
  int mPointLabelIndex;
  int mPointPhotoIndex;
  int mPointPictureIndex;
  int mPointAudioIndex;
  // int mPointDangerIndex;
  int mPointSectionIndex;

  /** default cstr - empty point library
   */
  SymbolPointLibrary( )
  {
    super( "p_" );
    mPointUserIndex   = 0;
    mPointLabelIndex  = -1;
    mPointPhotoIndex  = -1;
    mPointPictureIndex = -1;
    mPointAudioIndex  = -1;
    // mPointDangerIndex = -1;
    mPointSectionIndex = -1;
    initIndices();
  }

  /** cstr
   * @param ctx    context
   * @param res    resources
   */
  SymbolPointLibrary( Context ctx, Resources res )
  {
    super( "p_" );
    mPointUserIndex   = 0;
    mPointLabelIndex  = -1;
    mPointPhotoIndex  = -1;
    mPointPictureIndex = -1;
    mPointAudioIndex  = -1;
    // mPointDangerIndex = -1;
    mPointSectionIndex = -1;
    loadSystemPoints( res );
    loadUserPoints( ctx );
    initIndices();
  }

  void initIndices()
  {
    // makeEnabledList();
    makeConfigList();
    makeSpecialIndices();
  }

  /** @return true if the point has text value
   * @param k    point index
   */
  boolean pointHasText( int k )        { return k >= 0 && k < size() && ((SymbolPoint)mSymbols.get(k)).mHasText == 1; }

  /** @return true if the point has numeric value
   * @param k    point index
   */
  boolean pointHasValue( int k )       { return k >= 0 && k < size() && ((SymbolPoint)mSymbols.get(k)).mHasText == 2; }

  /** @return true if the point has either text or numeric value
   * @param k    point index
   */
  boolean pointHasTextOrValue( int k ) { return k >= 0 && k < size() && ((SymbolPoint)mSymbols.get(k)).mHasText > 0; }

  /** @return the orientation current value of the point symbol
   * @param k    point index
   */
  double getPointOrientation( int k )
  { return ( k < 0 || k >= size() )? 0.0 : ((SymbolPoint)mSymbols.get(k)).mOrientation; }

  /** reset the orientation of all points
   */
  @Override
  void resetOrientations()
  {
    for ( Symbol sp : mSymbols ) ((SymbolPoint)sp).resetOrientation();
  }

  /** rotate (clockwise?) the current orientation value of a point
   * @param k    point index
   * @param a    rotation angle [degrees]
   */
  void rotateGrad( int k, double a )
  {
    if ( k >= 0 && k < size() ) ((SymbolPoint)mSymbols.get(k)).rotateGradP( a );
  }

  /** @return the current drawing path of a point
   * @param k    point index
   */
  Path getPointPath( int k ) { 
  {
    return ( k < 0 || k >= size() )? null : ((SymbolPoint)mSymbols.get(k)).getPath( ); }
  }

  /** @return the original drawing path of a point
   * @param k    point index
   */
  Path getPointOrigPath( int k )
  {
    return ( k < 0 || k >= size() )? null : ((SymbolPoint)mSymbols.get(k)).getOrigPath( );
  }

  // ========================================================================

  static final private String p_label   = "moveTo 0 3 lineTo 0 -6 lineTo -3 -6 lineTo 3 -6"; // "T" shape
  static final private String p_user    = "addCircle 0 0 6";                                 // "o" shape
  static final private String p_section = "moveTo -2 0 lineTo 2 0 moveTo 0 -2 lineTo 0 2";   // small cross
  static final private String p_picture = "moveTo -5 -5 lineTo -5 5 lineTo 5 5 lineTo 5 -5 lineTo -5 -5 lineTo 5 5"; // square with diagonal

  /** load the system points, "user", "label", "section"
   * @param res   resources
   */
  private void loadSystemPoints( Resources res )
  {
    SymbolPoint symbol;
    // TDLog.v(  "Symbol Point Library: load system points" );

    mPointUserIndex = mSymbols.size(); // 0 = no-text, no-value. thname   group fname
    // String user = res.getString( R.string.p_user );
    symbol = new SymbolPoint( res.getString(R.string.thp_user), USER, null, USER, 0xffffffff, p_user, false, 0, DrawingLevel.LEVEL_USER, Symbol.W2D_DETAIL_SYM );
    addSymbol( symbol );

    mPointLabelIndex = mSymbols.size(); // 1 = text
    // String label = res.getString( R.string.p_label );
    symbol = new SymbolPoint( res.getString(R.string.thp_label), LABEL, null, LABEL, 0xffffffff, p_label, true, 1, DrawingLevel.LEVEL_LABEL, Symbol.W2D_NONE );
    addSymbol( symbol );

    mPointSectionIndex = mSymbols.size();
    // String section = res.getString( R.string.p_section );
    symbol = new SymbolPoint( res.getString(R.string.thp_section), SECTION, null, SECTION, 0xffcccccc, p_section, false, 0, DrawingLevel.LEVEL_USER, Symbol.W2D_DETAIL_SYM );
    addSymbol( symbol );

    mPointPictureIndex = mSymbols.size();
    // String section = res.getString( R.string.p_section );
    symbol = new SymbolPoint( res.getString(R.string.thp_picture), PICTURE, null, PICTURE, 0xffcccccc, p_picture, false, 0, DrawingLevel.LEVEL_USER, Symbol.W2D_DETAIL_SYM );
    addSymbol( symbol );

    // TDLog.v("PointLibrary user " + mPointUserIndex + " label " + mPointLabelIndex + " section " + mPointSectionIndex );
    if ( TopoDroidApp.mData != null ) {
      TopoDroidApp.mData.setSymbolEnabled( "p_" +  USER, true );
      TopoDroidApp.mData.setSymbolEnabled( "p_" + LABEL, true );
    }
  }

  /** load the user points
   * @param ctx   context
   */
  void loadUserPoints( Context ctx )
  {
    String locale = "name-" + TDLocale.getLocaleCode(); // TopoDroidApp.mLocale.toString().substring(0,2);
    // String iso = "ISO-8859-1";
    String iso = "UTF-8";
    // if ( locale.equals( "name-de" ) ) iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";
    // Charset.forName("ISO-8859-1")

    File dir = TDFile.getPrivateDir( TDPath.getSymbolPointDirname() );
    if ( dir.exists() ) {
      int systemNr = mSymbols.size();
      File[] files = dir.listFiles();
      if ( files == null ) {
         TDLog.e("null symbol-file list" );
	 return;
      }
      for ( File file : files ) { // there is a null-pointer exception here, but files cannot be null !!!
        String fname = file.getName();

        if ( fname.equals( PHOTO ) && ! TDandroid.checkCamera( ctx ) ) continue;
        // if ( fname.equals( PICTURE ) && ! TDandroid.checkCamera( ctx ) ) continue;
        if ( fname.equals( AUDIO ) && ! TDandroid.checkMicrophone( ctx ) ) continue;

        // if ( fname.equals(USER) || fname.equals(LABEL) || fname.equals(SECTION) ) continue;

        SymbolPoint symbol = new SymbolPoint( file.getPath(), fname, locale, iso );
        if ( symbol.isThName( null ) ) {
          TDLog.e( "Point Symbol with null ThName " + fname );
          continue;
        }
        // TDLog.v("Point Symbol <" + fname + "> th_name <" + symbol.getThName() + ">" );
        if ( ! hasSymbolByThName( symbol.getThName() ) ) {
          addSymbol( symbol );
          String thname = symbol.getThName();
          String name = "p_" + thname;
          // boolean enable = false;
          boolean enable = symbol.isConfigEnabled();
          if ( symbol.isThName( SECTION ) ) { // FIXME_SECTION_POINT always enabled
            enable = true;
	  } else if ( TopoDroidApp.mData != null ) {
            if ( ! TopoDroidApp.mData.hasSymbolName( name ) ) {
              enable = false;
              for ( int k=0; k<DefaultPoints.length; ++k ) { 
                if ( DefaultPoints[k].equals( thname ) ) { enable = true; break; }
              }
              TopoDroidApp.mData.setSymbolEnabled( name, enable ); // CONFIG_ENABLE
            } else {
              enable = TopoDroidApp.mData.getSymbolEnabled( name );
            }
	  }
          symbol.setEnabled( enable );
        } else {
          TDLog.e( "Point Symbol " + symbol.getThName() + " already in library" );
        }
      }
      sortSymbolByName( systemNr );
    } else {
      TDLog.e( "No symbol directory" );
      if ( ! dir.mkdirs( ) ) TDLog.e( "mkdir error" );
    }
  }

  /* LOAD_MISSING - APP_SAVE SYMBOLS
  // thname   symbol th-name
  boolean tryLoadMissingPoint( String thname )
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    if ( isSymbolEnabled( thname ) ) return true;
    Symbol symbol = getSymbolByThName( thname );
    if ( symbol == null ) {
      String filename = Symbol.deprefix_u( thname ); 
      // TDLog.v( "load missing point " + thname + " filename " + filename );
      File file = TDFile.getFile( TDPath.getSymbolSavePointPath( filename ) );
      if ( ! file.exists() ) return false;
      symbol = new SymbolPoint( file.getPath(), file.getName(), locale, iso );
      if ( symbol.isThName( null ) || symbol.getThName().length() == 0 ) return false;
      addSymbol( symbol );
    // } else {
    //   // TDLog.v( "enabling missing point " + thname );
    }
    // if ( symbol == null ) return false; // ALWAYS false

    symbol.setEnabled( true ); // TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.getThName() ) );
    makeEnabledList();
    return true;
  }
  */

// ------------------------------------------------------------------
  
  // /** make the list of enabled points
  //  */
  // @Override
  // protected void makeEnabledList()
  // {
  //   super.makeEnabledList();
  //   makeSpecialIndices();
  // }

  @Override
  protected void makeSpecialIndices()
  {
    mPointUserIndex    = getSymbolIndexByThName( USER );
    mPointLabelIndex   = getSymbolIndexByThName( LABEL );
    mPointPhotoIndex   = getSymbolIndexByThName( PHOTO );
    mPointPictureIndex = getSymbolIndexByThName( PICTURE );
    mPointAudioIndex   = getSymbolIndexByThName( AUDIO );
    // mPointDangerIndex  = getSymbolIndexByThName( "danger" );
    mPointSectionIndex = getSymbolIndexByThName( SECTION ); 
  }

  /** make the list of enabled points starting from a palette
   * @param palette    palette
   * @param clear      whether to clear the current enable list first, ie, disable, all symbols first
   */
  void makeEnabledListFromPalette( SymbolsPalette palette, boolean clear )
  {
    makeEnabledListFromStrings( palette.mPalettePoint, clear );
  }

}    
