/* @file BrushManager.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: brushes (points and lines)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
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
  static final int WIDTH_CURRENT = 1;
  static final int WIDTH_FIXED   = 1;
  static final int WIDTH_PREVIEW = 1;

  static SymbolPointLibrary mPointLib = null;
  static SymbolLineLibrary  mLineLib = null;
  static SymbolAreaLibrary  mAreaLib = null;
  static SymbolPoint mStationSymbol = null;

  // -----------------------------------------------------------
  static String getPointName( int idx ) { return mPointLib.getSymbolName( idx ); }

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

  static int getAreaCsxLayer( int index ) { return mAreaLib.areaCsxLayer( index ); }
  static int getAreaCsxType( int index ) { return mAreaLib.areaCsxType( index ); }
  static int getAreaCsxCategory( int index ) { return mAreaLib.areaCsxCategory( index ); }
  static int getAreaCsxPen( int index ) { return mAreaLib.areaCsxPen( index ); }
  static int getAreaCsxBrush( int index ) { return mAreaLib.areaCsxBrush( index ); }

  static boolean isPointOrientable( int index ) { return mPointLib.isSymbolOrientable( index ); }
  static double getPointOrientation( int index ) { return mPointLib.getPointOrientation( index ); }
  static void resetPointOrientations( ) { mPointLib.resetOrientations(); }
  static void rotateGradPoint( int index, double a ) { mPointLib.rotateGrad( index, a ); }
  static Path getPointPath( int i ) { return mPointLib.getPointPath( i ); }
  static Path getPointOrigPath( int i ) { return mPointLib.getPointOrigPath( i ); }

  static boolean isPointLabel( int index ) { return index == mPointLib.mPointLabelIndex; }
  static boolean isPointPhoto( int index ) { return index == mPointLib.mPointPhotoIndex; }
  static boolean isPointAudio( int index ) { return index == mPointLib.mPointAudioIndex; }
  static boolean isPointSection( int index ) { return index == mPointLib.mPointSectionIndex; }

  static int getPointLabelIndex() { return mPointLib.mPointLabelIndex; }

  // LINE CLOSED
  static boolean isLineClosed( int index ) { return mLineLib.isClosed( index ); }

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
  static Paint fixedRedPaint  = null;
  static Paint fixedGreenPaint  = null;
  static Paint fixedYellowPaint  = null;
  static Paint fixedOrangePaint  = null;
  static Paint fixedSplayPaint = null;
  static Paint fixedSplay0Paint = null;  // commented splay
  static Paint fixedSplay2Paint = null;  // cross-section splay2 (at viewed station)
  static Paint fixedSplay3Paint = null;  // dash splay
  static Paint fixedSplay4Paint = null;  // dot splay
  static Paint fixedSplay23Paint = null;  // blue dash splay
  static Paint fixedSplay24Paint = null;  // blue dot splay
  static Paint fixedGridPaint  = null;
  static Paint fixedGrid10Paint  = null;
  static Paint fixedGrid100Paint  = null;
  static Paint fixedStationPaint  = null;
  static Paint fixedStationBarrierPaint  = null;
  static Paint fixedStationHiddenPaint  = null;
  static Paint labelPaint  = null;
  static Paint duplicateStationPaint = null;
  // static Paint stationPaint = null;

  // static BitmapDrawable mSymbolHighlight = null;

  // ===========================================================================
  static private boolean mReloadSymbols = true; // whether to reload symbols

  static void makePaths( Context ctx, Resources res )
  {
    if ( mStationSymbol == null ) {
      mStationSymbol = new SymbolPoint( "station", "station", "station", 0xffff6633, 
        "addCircle 0 0 0.4 moveTo -3.0 1.73 lineTo 3.0 1.73 lineTo 0.0 -3.46 lineTo -3.0 1.73", false );
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

  static private boolean doneMakePaths = false;

  // paint for fixed path
  static Paint makePaint( int color ) { return makePaint( color, WIDTH_FIXED, Paint.Style.STROKE ); }

  // static Paint makePaint( int color, int width ) { return makePaint( color, width, Paint.Style.STROKE ); }

  static Paint makePaint( int color, int width, Paint.Style style )
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

  static void doMakePaths()
  {
    if ( ! doneMakePaths ) {
      errorPaint       = makePaint( TDColor.FULL_VIOLET,  WIDTH_CURRENT, Paint.Style.FILL_AND_STROKE );
      highlightPaint   = makePaint( TDColor.HIGH_PINK,    WIDTH_CURRENT, Paint.Style.STROKE );
      highlightPaint2  = makePaint( TDColor.HIGH_GREEN,   WIDTH_CURRENT, Paint.Style.FILL );
      highlightPaint3  = makePaint( TDColor.HIGH_RED,     WIDTH_CURRENT, Paint.Style.STROKE );
      fixedShotPaint   = makePaint( 0xffbbbbbb,           WIDTH_CURRENT, Paint.Style.STROKE);
      fixedBluePaint   = makePaint( 0xff9999ff,           WIDTH_CURRENT, Paint.Style.STROKE);
      fixedRedPaint    = makePaint( TDColor.FIXED_RED,    WIDTH_CURRENT, Paint.Style.STROKE);
      fixedYellowPaint = makePaint( TDColor.FIXED_YELLOW, WIDTH_CURRENT, Paint.Style.STROKE);
      fixedGreenPaint  = makePaint( TDColor.GREEN,        WIDTH_CURRENT, Paint.Style.STROKE);
      fixedOrangePaint = makePaint( TDColor.FIXED_ORANGE, WIDTH_CURRENT, Paint.Style.STROKE);
      fixedSplayPaint  = makePaint( TDColor.LIGHT_BLUE,   WIDTH_CURRENT, Paint.Style.STROKE);
      fixedSplay0Paint = makePaint( TDColor.VERYDARK_GRAY,WIDTH_CURRENT, Paint.Style.STROKE);
      fixedSplay2Paint = makePaint( TDColor.BLUE,         WIDTH_CURRENT, Paint.Style.STROKE);
      fixedSplay3Paint = makePaint( TDColor.LIGHT_BLUE,   WIDTH_CURRENT, Paint.Style.STROKE);

      float[] x = new float[2];
      x[0] = 24; // FIXME
      x[1] =  8;
      DashPathEffect dash3 = new DashPathEffect( x, 0 );
      fixedSplay3Paint.setPathEffect( dash3 );

      fixedSplay23Paint = makePaint( TDColor.BLUE,        WIDTH_CURRENT, Paint.Style.STROKE);
      fixedSplay23Paint.setPathEffect( dash3 );

      fixedSplay4Paint  = makePaint( TDColor.LIGHT_BLUE,  WIDTH_CURRENT, Paint.Style.STROKE);
      // float[] x = new float[2];
      x[0] = 14; // FIXME
      x[1] =  8; 
      DashPathEffect dash4 = new DashPathEffect( x, 0 );
      fixedSplay4Paint.setPathEffect( dash4 );

      fixedSplay24Paint  = makePaint( TDColor.BLUE,       WIDTH_CURRENT, Paint.Style.STROKE);
      fixedSplay24Paint.setPathEffect( dash4 );

      fixedGridPaint    = makePaint( TDColor.DARK_GRID,   WIDTH_FIXED, Paint.Style.STROKE);
      fixedGrid10Paint  = makePaint( TDColor.GRID,        WIDTH_FIXED, Paint.Style.STROKE);
      fixedGrid100Paint = makePaint( TDColor.LIGHT_GRID,  WIDTH_FIXED, Paint.Style.STROKE);
      fixedStationPaint = makePaint( TDColor.REDDISH,     WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      fixedStationBarrierPaint = makePaint( TDColor.FULL_RED, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      fixedStationHiddenPaint  = makePaint( 0xFF9966ff,  WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      labelPaint = makePaint( TDColor.WHITE, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
      // stationPaint = makePaint( 0xFFFF6666, WIDTH_FIXED, Paint.Style.STROKE);
      duplicateStationPaint = makePaint( 0xFFFF66FF, WIDTH_FIXED, Paint.Style.STROKE);
      // DEBUG
      // debugRed = makePaint( TDColor.FULL_RED, WIDTH_FIXED, Paint.Style.STROKE);
      // debugGreen = makePaint( TDColor.FULL_GREEN, WIDTH_FIXED, Paint.Style.STROKE);
      // debugBlue  = makePaint( TDColor.FULL_BLUE,  WIDTH_FIXED, Paint.Style.STROKE);

      // fixedGridPaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
      // fixedGrid10Paint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
      doneMakePaths = true;
    }
    setStrokeWidths();
    setTextSizes();
  }

  static void setStrokeWidths()
  {
    if (fixedShotPaint != null)
      fixedShotPaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (fixedBluePaint != null)
      fixedBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (fixedSplayPaint != null)
      fixedSplayPaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    if (fixedSplay2Paint != null)
      fixedSplay2Paint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
  }

  static void setTextSizes()
  {
    if ( labelPaint != null )
      labelPaint.setTextSize( TDSetting.mLabelSize );
    if ( fixedStationPaint != null )
      fixedStationPaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationBarrierPaint != null ) 
      fixedStationBarrierPaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationHiddenPaint != null )
      fixedStationHiddenPaint.setTextSize( TDSetting.mStationSize );
    if ( duplicateStationPaint != null ) 
      duplicateStationPaint.setTextSize( TDSetting.mStationSize );
  }

}
