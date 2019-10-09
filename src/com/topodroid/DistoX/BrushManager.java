/* @file BrushManager.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: brushes (points and lines)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.lang.Math;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
// import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.BitmapShader;
// import android.graphics.drawable.BitmapDrawable;
import android.graphics.Shader.TileMode;

import android.content.Context;
import android.content.res.Resources;

// import android.util.Log;

/**
 * gereric brush 
 */
class BrushManager
{
  static final private int WIDTH_CURRENT = 1;
  static final private int WIDTH_FIXED   = 1;
  static final         int WIDTH_PREVIEW = 1;

  static SymbolPointLibrary mPointLib = null;
  static SymbolLineLibrary  mLineLib = null;
  static SymbolAreaLibrary  mAreaLib = null;
  static SymbolPoint mStationSymbol = null;

  // -----------------------------------------------------------
  static String getPointName( int idx ) { return mPointLib.getSymbolName( idx ); }
  static int getPointLevel( int idx ) { return mPointLib.getSymbolLevel( idx ); }

  static boolean pointHasText( int index ) { return mPointLib.pointHasText( index ); }
  static boolean pointHasValue( int index ) { return mPointLib.pointHasValue( index ); }
  static boolean pointHasTextOrValue( int index ) { return mPointLib.pointHasTextOrValue( index ); }

  static int getPointCsxLayer( int index ) { return mPointLib.pointCsxLayer( index ); }
  static int getPointCsxType( int index ) { return mPointLib.pointCsxType( index ); }
  static int getPointCsxCategory( int index ) { return mPointLib.pointCsxCategory( index ); }
  static String getPointCsx( int index ) { return mPointLib.pointCsx( index ); }

  static int getLineCsxLayer( int index ) { return mLineLib.lineCsxLayer( index ); }
  static int getLineCsxType( int index ) { return mLineLib.lineCsxType( index ); }
  static int getLineCsxCategory( int index ) { return mLineLib.lineCsxCategory( index ); }
  static int getLineCsxPen( int index ) { return mLineLib.lineCsxPen( index ); }
  static String getLineGroup( int index ) { return mLineLib.getLineGroup( index ); }
  static int getLineLevel( int idx ) { return mLineLib.getSymbolLevel( idx ); }

  static int getAreaCsxLayer( int index ) { return mAreaLib.areaCsxLayer( index ); }
  static int getAreaCsxType( int index ) { return mAreaLib.areaCsxType( index ); }
  static int getAreaCsxCategory( int index ) { return mAreaLib.areaCsxCategory( index ); }
  static int getAreaCsxPen( int index ) { return mAreaLib.areaCsxPen( index ); }
  static int getAreaCsxBrush( int index ) { return mAreaLib.areaCsxBrush( index ); }
  static int getAreaLevel( int idx ) { return mAreaLib.getSymbolLevel( idx ); }

  static boolean isPointOrientable( int index ) { return mPointLib.isSymbolOrientable( index ); }
  static double getPointOrientation( int index ) { return mPointLib.getPointOrientation( index ); }
  // test should not be necessary but Xperia Z Ultra Android 5.1 crashed 2019-10-07
  static void resetPointOrientations( ) { if ( mPointLib != null ) mPointLib.resetOrientations(); }
  static void rotateGradPoint( int index, double a ) { mPointLib.rotateGrad( index, a ); }
  static Path getPointPath( int i ) { return mPointLib.getPointPath( i ); }
  static Path getPointOrigPath( int i ) { return mPointLib.getPointOrigPath( i ); }

  static boolean isPointLabel( int index ) { return index == mPointLib.mPointLabelIndex; }
  static boolean isPointPhoto( int index ) { return index == mPointLib.mPointPhotoIndex; }
  static boolean isPointAudio( int index ) { return index == mPointLib.mPointAudioIndex; }
  static boolean isPointSection( int index ) { return index == mPointLib.mPointSectionIndex; }

  static int getPointLabelIndex() { return mPointLib.mPointLabelIndex; }
  static int getPointSectionIndex() { return mPointLib.mPointSectionIndex; }


  static String getPointSymbolName( int idx ) { return mPointLib.getSymbolName( idx ); } 
  static String getLineSymbolName( int idx )  { return mLineLib.getSymbolName( idx ); } 
  static String getAreaSymbolName( int idx )  { return mAreaLib.getSymbolName( idx ); } 

  // LINE CLOSED
  static boolean isLineClosed( int index ) { return mLineLib.isClosed( index ); }
  static boolean isLineSection( int index ) { return index == mLineLib.mLineSectionIndex; }
  // static int getLineSectionIndex() { return mLineLib.mLineSectionIndex; }
  static int getLineWallIndex() { return mLineLib.mLineWallIndex; }

  // FIXME AREA_ORIENT
  static void resetAreaOrientations( ) { mAreaLib.resetOrientations(); }
  static double getAreaOrientation( int index ) { return mAreaLib.getAreaOrientation( index ); }
  static void rotateGradArea( int index, double a ) { mAreaLib.rotateGrad( index, a ); }

  static Bitmap getAreaBitmap( int index ) { return mAreaLib.getAreaBitmap( index ); }
  static Shader getAreaShader( int index ) { return mAreaLib.getAreaShader( index ); }
  static BitmapShader cloneAreaShader( int index ) { return mAreaLib.cloneAreaShader( index ); }
  static TileMode getAreaXMode( int index ) { return mAreaLib.getAreaXMode( index ); }
  static TileMode getAreaYMode( int index ) { return mAreaLib.getAreaYMode( index ); }

  // --------------------------------------------------------------------------
  // LINES

  // -----------------------------------------------------------------------
  // AREAS

  static int getAreaColor( int index ) { return mAreaLib.getAreaColor( index ); }

  // --------------------------------------------------------------------------

  static Paint errorPaint      = null;
  static Paint highlightPaint  = null;
  static Paint highlightPaint2 = null;
  static Paint highlightPaint3 = null;
  static Paint fixedShotPaint  = null;
  static Paint fixedBluePaint  = null;
  static Paint deepBluePaint   = null;
  static Paint darkBluePaint   = null;
  static Paint lightBluePaint  = null;
  static Paint fixedRedPaint   = null;
  static Paint fixedYellowPaint  = null;
  static Paint fixedOrangePaint  = null;
  static Paint paintSplayLRUD  = null;
  static Paint paintSplayXB = null;
  static Paint paintSplayComment = null;  // commented splay
  static Paint paintSplayXViewed = null;  // cross-section splay2 (at viewed station)
  static Paint paintSplayXBdash = null;  // dash splay
  static Paint paintSplayXBdot = null;  // dot splay
  static Paint paintSplayXVdash = null;  // blue dash splay
  static Paint paintSplayXVdot = null;  // blue dot splay
  static Paint fixedGridPaint  = null;
  static Paint fixedGrid10Paint  = null;
  static Paint fixedGrid100Paint  = null;
  static Paint fixedStationPaint  = null;
  static Paint fixedStationSavedPaint   = null;
  static Paint fixedStationActivePaint  = null;
  static Paint fixedStationBarrierPaint = null;
  static Paint fixedStationHiddenPaint  = null;
  static Paint labelPaint  = null;
  static Paint borderPaint = null;
  static Paint duplicateStationPaint = null;
  static Paint mSectionPaint = null;
  static Paint referencePaint = null;
  // static Paint stationPaint = null;

  // static BitmapDrawable mSymbolHighlight = null;

  // ===========================================================================
  static private boolean mReloadSymbols = true; // whether to reload symbols

  static void makePaths( Context ctx, Resources res )
  {
    if ( mStationSymbol == null ) {
      mStationSymbol = new SymbolPoint( "station", "station", "station", 0xffff6633, 
        "addCircle 0 0 0.4 moveTo -3.0 1.73 lineTo 3.0 1.73 lineTo 0.0 -3.46 lineTo -3.0 1.73", false, DrawingLevel.LEVEL_WALL );
    }
    // if ( mSymbolHighlight == null ) {
    //   mSymbolHighlight = MyButton.getButtonBackground( mApp, res, R.drawable.symbol_highlight );
    // }

    if ( mPointLib == null ) mPointLib = new SymbolPointLibrary( ctx, res );
    if ( mLineLib == null ) mLineLib = new SymbolLineLibrary( res );
    if ( mAreaLib == null ) mAreaLib = new SymbolAreaLibrary( res );

    if ( mReloadSymbols ) {
      mPointLib.loadUserPoints( ctx );
      mLineLib.loadUserLines();
      mAreaLib.loadUserAreas();
      mReloadSymbols = false;
    }
  }

  static void reloadAllLibraries( Context ctx, Resources res ) 
  {
    reloadPointLibrary( ctx, res );
    reloadLineLibrary( res );
    reloadAreaLibrary( res );
  }

  static void reloadPointLibrary( Context ctx, Resources res )
  {
    mPointLib = new SymbolPointLibrary( ctx, res );
    // mPointLib.loadUserPoints();
  }

  static void reloadLineLibrary( Resources res )
  {
    mLineLib = new SymbolLineLibrary( res );
    // mLineLib.loadUserLines();
  }

  static void reloadAreaLibrary( Resources res )
  {
    mAreaLib = new SymbolAreaLibrary( res );
    // mAreaLib.loadUserAreas();
  }

  // palette    set of symbols filenames
  // clear      whether the disable all symbols first
  static void makeEnabledListFromPalette( SymbolsPalette palette, boolean clear ) 
  {
    mPointLib.makeEnabledListFromPalette( palette, clear );
    mLineLib.makeEnabledListFromPalette( palette, clear );
    mAreaLib.makeEnabledListFromPalette( palette, clear );
  }

  static private boolean doneMakePaths = false;

  // paint for fixed path
  static Paint makePaint( int color ) { return makePaint( color, WIDTH_FIXED, Paint.Style.STROKE ); }

  // static Paint makePaint( int color, int width ) { return makePaint( color, width, Paint.Style.STROKE ); }

  static private Paint makePaint( int color, int width, Paint.Style style )
  {
    Paint paint = new Paint();
    paint.setDither(true);
    paint.setColor( color );
    paint.setStyle( style );
    paint.setStrokeJoin(Paint.Join.ROUND);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth( width );
    return paint;
  }

  // alpha in [0,100]
  static void setSplayAlpha( int alpha )
  {
    alpha = (alpha * 255)/100;
    if ( paintSplayLRUD    != null ) paintSplayLRUD.setAlpha( alpha );
    if ( paintSplayXB      != null ) paintSplayXB.setAlpha( alpha );
    if ( paintSplayComment != null ) paintSplayComment.setAlpha( alpha );  // commented splay
    if ( paintSplayXViewed != null ) paintSplayXViewed.setAlpha( alpha );  // cross-section splay2 (at viewed station)
    if ( paintSplayXBdash  != null ) paintSplayXBdash.setAlpha( alpha );  // dash splay
    if ( paintSplayXBdot   != null ) paintSplayXBdot.setAlpha( alpha );  // dot splay
    if ( paintSplayXVdash  != null ) paintSplayXVdash.setAlpha( alpha );  // blue dash splay
    if ( paintSplayXVdot   != null ) paintSplayXVdot.setAlpha( alpha );  // blue dot splay
  }

  static void doMakePaths()
  {
    if ( ! doneMakePaths ) {
      errorPaint       = makePaint( TDColor.FULL_VIOLET,  WIDTH_CURRENT, Paint.Style.FILL_AND_STROKE );
      highlightPaint   = makePaint( TDColor.HIGH_PINK,    WIDTH_CURRENT, Paint.Style.STROKE );
      highlightPaint2  = makePaint( TDColor.HIGH_GREEN,   WIDTH_CURRENT, Paint.Style.FILL );
      highlightPaint3  = makePaint( TDColor.HIGH_RED,     WIDTH_CURRENT, Paint.Style.STROKE );
      fixedShotPaint   = makePaint( 0xffbbbbbb,           WIDTH_CURRENT, Paint.Style.STROKE);
      fixedBluePaint   = makePaint( 0xff9999ff,           WIDTH_CURRENT, Paint.Style.STROKE);
      deepBluePaint    = makePaint( 0xff3366ff,           WIDTH_CURRENT, Paint.Style.STROKE);
      darkBluePaint    = makePaint( 0x663366ff,           WIDTH_CURRENT, Paint.Style.STROKE); // same as deepBluePaint but with alpha
      lightBluePaint   = makePaint( 0xff66ccff,           WIDTH_CURRENT, Paint.Style.STROKE);
      fixedRedPaint    = makePaint( TDColor.FIXED_RED,    WIDTH_CURRENT, Paint.Style.STROKE);
      fixedYellowPaint = makePaint( TDColor.FIXED_YELLOW, WIDTH_CURRENT, Paint.Style.STROKE);
      fixedOrangePaint = makePaint( TDColor.FIXED_ORANGE, WIDTH_CURRENT, Paint.Style.STROKE);
      paintSplayLRUD    = makePaint( TDColor.SPLAY_LRUD,    WIDTH_CURRENT, Paint.Style.STROKE);
      paintSplayXB      = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);
      paintSplayComment = makePaint( TDColor.SPLAY_COMMENT,WIDTH_CURRENT, Paint.Style.STROKE);
      paintSplayXViewed = makePaint( TDColor.SPLAY_NORMAL,         WIDTH_CURRENT, Paint.Style.STROKE);
      paintSplayXBdash  = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);

      float[] x = new float[2];
      x[0] = 24; // FIXME
      x[1] =  8;
      DashPathEffect dash3 = new DashPathEffect( x, 0 );
      paintSplayXBdash.setPathEffect( dash3 );

      paintSplayXVdash = makePaint( TDColor.SPLAY_NORMAL,        WIDTH_CURRENT, Paint.Style.STROKE);
      paintSplayXVdash.setPathEffect( dash3 );

      paintSplayXBdot  = makePaint( TDColor.SPLAY_LIGHT,  WIDTH_CURRENT, Paint.Style.STROKE);
      // float[] x = new float[2];
      x[0] = 14; // FIXME
      x[1] =  8; 
      DashPathEffect dash4 = new DashPathEffect( x, 0 );
      paintSplayXBdot.setPathEffect( dash4 );

      paintSplayXVdot  = makePaint( TDColor.SPLAY_NORMAL,       WIDTH_CURRENT, Paint.Style.STROKE);
      paintSplayXVdot.setPathEffect( dash4 );

      fixedGridPaint    = makePaint( TDColor.DARK_GRID,   WIDTH_FIXED, Paint.Style.STROKE);
      fixedGrid10Paint  = makePaint( TDColor.GRID,        WIDTH_FIXED, Paint.Style.STROKE);
      fixedGrid100Paint = makePaint( TDColor.LIGHT_GRID,  WIDTH_FIXED, Paint.Style.STROKE);
      fixedStationPaint = makePaint( TDColor.REDDISH,     WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      fixedStationSavedPaint   = makePaint( TDColor.ORANGE, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      fixedStationActivePaint  = makePaint( TDColor.LIGHT_GREEN, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      fixedStationBarrierPaint = makePaint( TDColor.FULL_RED, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      fixedStationHiddenPaint  = makePaint( 0xFF9966ff,  WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      labelPaint = makePaint( TDColor.WHITE, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      borderPaint = makePaint( 0x99ffffff, WIDTH_FIXED, Paint.Style.STROKE);
      // stationPaint = makePaint( 0xFFFF6666, WIDTH_FIXED, Paint.Style.STROKE);
      duplicateStationPaint = makePaint( 0xFFFF66FF, WIDTH_FIXED, Paint.Style.STROKE);
      referencePaint = makePaint( 0xFFffffff, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      // DEBUG
      // debugRed = makePaint( TDColor.FULL_RED, WIDTH_FIXED, Paint.Style.STROKE);
      // debugGreen = makePaint( TDColor.FULL_GREEN, WIDTH_FIXED, Paint.Style.STROKE);
      // debugBlue  = makePaint( TDColor.FULL_BLUE,  WIDTH_FIXED, Paint.Style.STROKE);

      // fixedGridPaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
      // fixedGrid10Paint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
      mSectionPaint = makePaint( TDColor.ORANGE, 2 * WIDTH_FIXED, Paint.Style.FILL_AND_STROKE );
      doneMakePaths = true;
    }
    setSplayAlpha( TDSetting.mSplayAlpha );
    setStrokeWidths();
    setTextSizes();
  }

  static void setStrokeWidths()
  {
    if (fixedShotPaint != null)    fixedShotPaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (fixedBluePaint != null)    fixedBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (lightBluePaint != null)    lightBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (darkBluePaint != null)     darkBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (deepBluePaint != null)     deepBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (paintSplayXB != null)      paintSplayXB.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (paintSplayXViewed != null) paintSplayXViewed.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
  }

  static void setTextSizes()
  {
    if ( labelPaint != null )               labelPaint.setTextSize( TDSetting.mLabelSize );
    if ( fixedStationPaint != null )        fixedStationPaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationSavedPaint != null )   fixedStationSavedPaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationActivePaint != null )  fixedStationActivePaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationBarrierPaint != null ) fixedStationBarrierPaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationHiddenPaint != null )  fixedStationHiddenPaint.setTextSize( TDSetting.mStationSize );
    if ( duplicateStationPaint != null )    duplicateStationPaint.setTextSize( TDSetting.mStationSize );
    if ( referencePaint != null )           referencePaint.setTextSize( TDSetting.mStationSize );
  }

  static SymbolsPalette preparePalette()
  {
    SymbolsPalette palette = new SymbolsPalette();
    // populate local palette with default symbols
    palette.addPointFilename("user"); // make sure local palette contains "user" symnbols
    palette.addLineFilename("user");
    palette.addAreaFilename("user");
    SymbolPointLibrary points = mPointLib;
    if ( points != null ) {
      for ( Symbol p : points.getSymbols() ) if ( p.isEnabled() ) {
        String fname = p.getThName();
        if ( ! fname.equals("user") ) palette.addPointFilename( fname );
      }
    }
    SymbolLineLibrary lines = mLineLib;
    if ( lines != null ) {
      for ( Symbol p : lines.getSymbols() ) if ( p.isEnabled() ) {
        String fname = p.getThName();
        if ( ! fname.equals("user") ) palette.addLineFilename( fname );
      }
    }
    SymbolAreaLibrary areas = mAreaLib;
    if ( areas != null ) {
      for ( Symbol p : areas.getSymbols() ) if ( p.isEnabled() ) {
        String fname = p.getThName();
        if ( ! fname.equals("user") ) palette.addAreaFilename( fname );
      }
    }
    return palette;
  }

}
