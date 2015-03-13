/* @file DrawingBrushPaths.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: brushes (points and lines)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120614 new points: end (narrow-end, low-end) pebbles, snow
 * 20120614 new area: snow
 * 20120619 new line: overhang
 * 20120725 TopoDroidApp log
 * 20121109 new point: dig
 * 20121113 new point: sink/spring
 * 20121115 new point: crystal, merged together points ice/snow
 * 20121122 new points: moonmilk (overloaded to flowstone), choke (overloaded to dig)
 * 20121201 using SymbolPoint class
 * 20121212 clearPaths()
 * 20121220 reload symbols (to add new symbols)
 * 20120108 station symbol (a red dot)
 * 20131119 area color getter
 * 20131210 second grid paint
 * 20140305 symbol units changes take immadiate effect (N.B. old symbols must still redrawn)
 */
package com.topodroid.DistoX;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;

import android.content.res.Resources;

// import android.util.Log;

import java.lang.Math;


/**
 * gereric brush 
 */
public class DrawingBrushPaths
{
  static final int WIDTH_CURRENT = 1;
  static final int WIDTH_FIXED   = 1;
  static final int WIDTH_PREVIEW = 1;

  static SymbolPointLibrary mPointLib = null;
  static SymbolLineLibrary  mLineLib = null;
  static SymbolAreaLibrary  mAreaLib = null;
  static SymbolPoint mStationSymbol = null;
  static boolean mReloadSymbols = false; // whether to reload symbols

  static String getPointName( int idx ) { return mPointLib.getAnyPointName( idx ); }

  static String getPointThName( int index ) { return mPointLib.getPointThName( index ); }

  static Paint getPointPaint( int index ) { return mPointLib.getPointPaint( index ); }

  static boolean pointHasText( int index ) { return mPointLib.pointHasText( index ); }

  static int getPointCsxLayer( int index ) { return mPointLib.pointCsxLayer( index ); }

  static int getPointCsxType( int index ) { return mPointLib.pointCsxType( index ); }

  static int getPointCsxCategory( int index ) { return mPointLib.pointCsxCategory( index ); }

  static String getPointCsx( int index ) { return mPointLib.pointCsx( index ); }

  static int getLineCsxLayer( int index ) { return mLineLib.lineCsxLayer( index ); }

  static int getLineCsxType( int index ) { return mLineLib.lineCsxType( index ); }

  static int getLineCsxCategory( int index ) { return mLineLib.lineCsxCategory( index ); }

  static int getLineCsxPen( int index ) { return mLineLib.lineCsxPen( index ); }


  static int getAreaCsxLayer( int index ) { return mAreaLib.areaCsxLayer( index ); }

  static int getAreaCsxType( int index ) { return mAreaLib.areaCsxType( index ); }

  static int getAreaCsxCategory( int index ) { return mAreaLib.areaCsxCategory( index ); }

  static int getAreaCsxPen( int index ) { return mAreaLib.areaCsxPen( index ); }

  static int getAreaCsxBrush( int index ) { return mAreaLib.areaCsxBrush( index ); }



  static boolean canRotate( int index ) { return mPointLib.canRotate( index ); }

  static double getPointOrientation( int index ) { return mPointLib.getPointOrientation( index ); }

  static void resetPointOrientations( ) { mPointLib.resetOrientations(); }

  static void rotateRad( int index, double a )
  {
    a = a * TopoDroidUtil.RAD2GRAD;
    rotateGrad( index, a );
  }

  static int getPointLabelIndex() { return mPointLib.mPointLabelIndex; }

  static void rotateGrad( int index, double a ) { mPointLib.rotateGrad( index, a ); }


  // --------------------------------------------------------------------------
  // LINES

  static final int highlightColor = 0xffff9999;
  static final int highlightFill  = 0x6600cc00;

  static String getLineName( int idx ) { return mLineLib.getLineName( idx ); }

  static String getLineThName( int index ) { return mLineLib.getLineThName( index ); }

  static Paint getLinePaint( int index, boolean reversed ) { return mLineLib.getLinePaint( index, reversed ); }

  // -----------------------------------------------------------------------
  // AREAS


  static String getAreaName( int idx ) { return mAreaLib.getAreaName( idx ); }

  static String getAreaThName( int index ) { return mAreaLib.getAreaThName( index ); }

  static Paint getAreaPaint( int index ) { return mAreaLib.getAreaPaint( index ); }

  static int getAreaColor( int index ) { return mAreaLib.getAreaColor( index ); }

  // --------------------------------------------------------------------------

  static Paint highlightPaint  = null;
  static Paint highlightPaint2 = null;
  static Paint highlightPaint3 = null;
  static Paint fixedShotPaint  = null;
  static Paint fixedBluePaint  = null;
  static Paint fixedSplayPaint = null;
  static Paint fixedSplay2Paint = null;  // cross-section splay2 (at viewed station)
  static Paint fixedGridPaint  = null;
  static Paint fixedGrid10Paint  = null;
  static Paint fixedStationPaint  = null;
  static Paint labelPaint  = null;
  static Paint duplicateStationPaint = null;

  // ===========================================================================

  static Path getPointPath( int i ) { return mPointLib.getPointPath( i ); }

  static Path getPointOrigPath( int i ) { return mPointLib.getPointOrigPath( i ); }

  static void makePaths( Resources res )
  {
    if ( mStationSymbol == null ) {
      mStationSymbol = new SymbolPoint( "station", "station", 0xffff6633, 
        "addCircle 0 0 0.4 moveTo -3.0 1.73 lineTo 3.0 1.73 lineTo 0.0 -3.46 lineTo -3.0 1.73", false );
    }

    if ( mPointLib == null ) mPointLib = new SymbolPointLibrary( res );
    if ( mLineLib == null ) mLineLib = new SymbolLineLibrary( res );
    if ( mAreaLib == null ) mAreaLib = new SymbolAreaLibrary( res );

    if ( mReloadSymbols ) {
      mPointLib.loadUserPoints();
      mLineLib.loadUserLines();
      mAreaLib.loadUserAreas();
      mReloadSymbols = false;
    }
  }

  static void reloadPointLibrary( Resources res )
  {
    mPointLib = new SymbolPointLibrary( res );
    mPointLib.loadUserPoints();
  }

  static void reloadLineLibrary( Resources res )
  {
    mLineLib = new SymbolLineLibrary( res );
    mLineLib.loadUserLines();
  }

  static void doMakePaths()
  {
    highlightPaint = new Paint();
    highlightPaint.setDither(true);
    highlightPaint.setColor( highlightColor );
    highlightPaint.setStyle(Paint.Style.STROKE);
    highlightPaint.setStrokeJoin(Paint.Join.ROUND);
    highlightPaint.setStrokeCap(Paint.Cap.ROUND);
    highlightPaint.setStrokeWidth( WIDTH_CURRENT );

    highlightPaint2 = new Paint();
    highlightPaint2.setDither(true);
    highlightPaint2.setColor( highlightFill );
    highlightPaint2.setStyle(Paint.Style.FILL);
    highlightPaint2.setStrokeJoin(Paint.Join.ROUND);
    highlightPaint2.setStrokeCap(Paint.Cap.ROUND);
    highlightPaint2.setStrokeWidth( WIDTH_CURRENT );

    highlightPaint3 = new Paint();
    highlightPaint3.setDither(true);
    highlightPaint3.setColor( highlightColor );
    highlightPaint3.setStyle(Paint.Style.STROKE);
    highlightPaint3.setStrokeJoin(Paint.Join.ROUND);
    highlightPaint3.setStrokeCap(Paint.Cap.ROUND);
    highlightPaint3.setStrokeWidth( WIDTH_CURRENT );

    fixedShotPaint = new Paint();
    fixedShotPaint = new Paint();
    fixedShotPaint.setDither(true);
    fixedShotPaint.setStyle(Paint.Style.STROKE);
    fixedShotPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedShotPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedShotPaint.setColor(0xFFbbbbbb); // light gray

    fixedBluePaint = new Paint();
    fixedBluePaint.setDither(true);
    fixedBluePaint.setStyle(Paint.Style.STROKE);
    fixedBluePaint.setStrokeJoin(Paint.Join.ROUND);
    fixedBluePaint.setStrokeCap(Paint.Cap.ROUND);
    fixedBluePaint.setColor(0xFF9999ff); // light blue

    fixedSplayPaint = new Paint();
    fixedSplayPaint.setDither(true);
    fixedSplayPaint.setStyle(Paint.Style.STROKE);
    fixedSplayPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedSplayPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedSplayPaint.setColor(0xFF666666); // dark gray

    fixedSplay2Paint = new Paint();
    fixedSplay2Paint.setDither(true);
    fixedSplay2Paint.setStyle(Paint.Style.STROKE);
    fixedSplay2Paint.setStrokeJoin(Paint.Join.ROUND);
    fixedSplay2Paint.setStrokeCap(Paint.Cap.ROUND);
    fixedSplay2Paint.setColor(0xFFAAAAAA); // gray

    fixedGridPaint = new Paint();
    fixedGridPaint.setDither(true);
    fixedGridPaint.setStyle(Paint.Style.STROKE);
    fixedGridPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedGridPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedGridPaint.setColor(0x99666666); // very dark gray

    fixedGrid10Paint = new Paint();
    fixedGrid10Paint.setDither(true);
    fixedGrid10Paint.setStyle(Paint.Style.STROKE);
    fixedGrid10Paint.setStrokeJoin(Paint.Join.ROUND);
    fixedGrid10Paint.setStrokeCap(Paint.Cap.ROUND);
    fixedGrid10Paint.setColor(0x99999999); // not so dark gray

    fixedStationPaint = new Paint();
    fixedStationPaint.setDither(true);
    fixedStationPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    fixedStationPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedStationPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedStationPaint.setStrokeWidth( WIDTH_FIXED );
    fixedStationPaint.setColor(0xFFFF66cc); // not very dark red

    labelPaint = new Paint();
    labelPaint.setDither(true);
    labelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    labelPaint.setStrokeJoin(Paint.Join.ROUND);
    labelPaint.setStrokeCap(Paint.Cap.ROUND);
    labelPaint.setStrokeWidth( WIDTH_FIXED );
    labelPaint.setColor(0xFFFFFFFF); // white

    duplicateStationPaint = new Paint();
    duplicateStationPaint.setDither(true);
    duplicateStationPaint.setStyle(Paint.Style.STROKE);
    duplicateStationPaint.setStrokeJoin(Paint.Join.ROUND);
    duplicateStationPaint.setStrokeCap(Paint.Cap.ROUND);
    duplicateStationPaint.setStrokeWidth( WIDTH_FIXED );
    duplicateStationPaint.setColor(0xFF3333FF); // very dark blue

    // DEBUG
    
    // debugRed = new Paint();
    // debugRed.setDither(true);
    // debugRed.setStyle(Paint.Style.STROKE);
    // debugRed.setStrokeJoin(Paint.Join.ROUND);
    // debugRed.setStrokeCap(Paint.Cap.ROUND);
    // debugRed.setStrokeWidth( WIDTH_FIXED );
    // debugRed.setColor(0xFFFF0000); // red

    // debugGreen = new Paint();
    // debugGreen.setDither(true);
    // debugGreen.setStyle(Paint.Style.STROKE);
    // debugGreen.setStrokeJoin(Paint.Join.ROUND);
    // debugGreen.setStrokeCap(Paint.Cap.ROUND);
    // debugGreen.setStrokeWidth( WIDTH_FIXED );
    // debugGreen.setColor(0xFF00FF00); // green

    // debugBlue = new Paint();
    // debugBlue.setDither(true);
    // debugBlue.setStyle(Paint.Style.STROKE);
    // debugBlue.setStrokeJoin(Paint.Join.ROUND);
    // debugBlue.setStrokeCap(Paint.Cap.ROUND);
    // debugBlue.setStrokeWidth( WIDTH_FIXED );
    // debugBlue.setColor(0xFF0000FF); // blue

    fixedGridPaint.setStrokeWidth( WIDTH_FIXED ); //* TopoDroidSetting.mFixedThickness );
    fixedGrid10Paint.setStrokeWidth( WIDTH_FIXED ); // * TopoDroidSetting.mFixedThickness );
    setStrokeWidths();
    setTextSizes();
  }

  static void setStrokeWidths()
  {
    /* if (fixedShotPaint != null) */ fixedShotPaint.setStrokeWidth( WIDTH_FIXED * TopoDroidSetting.mFixedThickness );
    /* if (fixedBluePaint != null) */ fixedBluePaint.setStrokeWidth( WIDTH_FIXED * TopoDroidSetting.mFixedThickness );
    /* if (fixedSplayPaint != null) */ fixedSplayPaint.setStrokeWidth( WIDTH_FIXED * TopoDroidSetting.mFixedThickness );
    /* if (fixedSplay2Paint != null) */ fixedSplay2Paint.setStrokeWidth( WIDTH_FIXED * TopoDroidSetting.mFixedThickness );
  }

  static void setTextSizes()
  {
    /* if ( labelPaint != null ) */ labelPaint.setTextSize( TopoDroidSetting.mLabelSize );
    /* if ( fixedStationPaint != null ) */ fixedStationPaint.setTextSize( TopoDroidSetting.mStationSize );
    /* if ( duplicateStationPaint != null ) */ duplicateStationPaint.setTextSize( TopoDroidSetting.mStationSize );
  }

}
