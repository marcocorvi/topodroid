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

import java.io.DataOutputStream;
import java.io.IOException;

// import android.util.Log;

/**
 * gereric brush 
 */
class BrushManager
{
  static final private int WIDTH_CURRENT = 1;
  static final private int WIDTH_FIXED   = 1;
  static final         int WIDTH_PREVIEW = 1;

  // TODO make private
  static SymbolPointLibrary mPointLib = null;
  static SymbolLineLibrary  mLineLib  = null;
  static SymbolAreaLibrary  mAreaLib  = null;
  static private SymbolPoint mStationSymbol   = null;

  // -----------------------------------------------------------
  static boolean tryLoadMissingPoint( String thname ) { return mPointLib != null && mPointLib.tryLoadMissingPoint( thname ); }
  static boolean tryLoadMissingLine( String thname )  { return mLineLib  != null && mLineLib.tryLoadMissingLine( thname ); }
  static boolean tryLoadMissingArea( String thname )  { return mAreaLib  != null && mAreaLib.tryLoadMissingArea( thname ); }

  static int getPointIndex( Symbol point ) { return (mPointLib == null)? -1 : mPointLib.getSymbolIndex( point ); }
  static int getLineIndex( Symbol line )   { return (mLineLib  == null)? -1 : mLineLib.getSymbolIndex( line ); }
  static int getAreaIndex( Symbol area )   { return (mAreaLib  == null)? -1 : mAreaLib.getSymbolIndex( area ); }

  static int getPointIndexByThName( String thname ) { return (mPointLib == null)? -1 : mPointLib.getSymbolIndexByThName( thname ); }
  static int getLineIndexByThName( String thname )  { return (mLineLib  == null)? -1 : mLineLib.getSymbolIndexByThName( thname ); }
  static int getAreaIndexByThName( String thname )  { return (mAreaLib  == null)? -1 : mAreaLib.getSymbolIndexByThName( thname ); }

  static Symbol getPointByThName( String thname ) { return (mPointLib == null)? null : mPointLib.getSymbolByThName( thname ); }
  static Symbol getLineByThName( String thname )  { return (mLineLib  == null)? null : mLineLib.getSymbolByThName( thname ); }
  static Symbol getAreaByThName( String thname )  { return (mAreaLib  == null)? null : mAreaLib.getSymbolByThName( thname ); }

  static SymbolPoint getPointByIndex( int idx ) { return (mPointLib == null)? null : (SymbolPoint)mPointLib.getSymbolByIndex( idx ); }
  static SymbolLine  getLineByIndex(  int idx ) { return (mLineLib  == null)? null : (SymbolLine)mLineLib.getSymbolByIndex( idx ); }
  static SymbolArea  getAreaByIndex(  int idx ) { return (mAreaLib  == null)? null : (SymbolArea)mAreaLib.getSymbolByIndex( idx ); }

  static boolean hasPointByThName( String thname ) { return (mPointLib != null) && mPointLib.hasSymbolByThName( thname ); }
  static boolean hasLineByThName( String thname )  { return (mLineLib  != null) && mLineLib.hasSymbolByThName( thname ); }
  static boolean hasAreaByThName( String thname )  { return (mAreaLib  != null) && mAreaLib.hasSymbolByThName( thname ); }

  static int getPointIndexByThNameOrGroup( String thname, String group ) { return (mPointLib == null)? -1 : mPointLib.getSymbolIndexByThNameOrGroup( thname, group ); }
  static int getLineIndexByThNameOrGroup( String thname, String group )  { return (mLineLib  == null)? -1 : mLineLib.getSymbolIndexByThNameOrGroup( thname, group ); }
  static int getAreaIndexByThNameOrGroup( String thname, String group )  { return (mAreaLib  == null)? -1 : mAreaLib.getSymbolIndexByThNameOrGroup( thname, group ); }

  static void setRecentPoints( Symbol[] points ) { if (mPointLib != null) mPointLib.setRecentSymbols( points ); }
  static void setRecentLines( Symbol[] lines )   { if (mLineLib  != null) mLineLib.setRecentSymbols( lines ); }
  static void setRecentAreas( Symbol[] areas )   { if (mAreaLib  != null) mAreaLib.setRecentSymbols( areas ); }

  static String getPointThName( int idx ) { return (mPointLib == null)? null : mPointLib.getSymbolThName( idx ); }
  static String getLineThName( int idx )  { return (mLineLib  == null)? null : mLineLib.getSymbolThName( idx ); }
  static String getAreaThName( int idx )  { return (mAreaLib  == null)? null : mAreaLib.getSymbolThName( idx ); }

  static String getPointGroup( int idx ) { return (mPointLib == null)? null : mPointLib.getSymbolGroup( idx ); }
  static String getLineGroup( int idx )  { return (mLineLib  == null)? null : mLineLib.getSymbolGroup( idx ); }
  static String getAreaGroup( int idx )  { return (mAreaLib  == null)? null : mAreaLib.getSymbolGroup( idx ); }

  static boolean hasPoint( int idx ) { return (mPointLib != null) && idx < mPointLib.size(); }
  static boolean hasLine( int idx )  { return (mLineLib  != null) && idx < mLineLib.size(); }
  static boolean hasArea( int idx )  { return (mAreaLib  != null) && idx < mAreaLib.size(); }

  static Paint getPointPaint( int idx ) { return (mPointLib == null)? errorPaint : mPointLib.getSymbolPaint( idx ); }
  static Paint getLinePaint(  int idx ) { return (mLineLib  == null)? errorPaint : mLineLib.getSymbolPaint( idx ); }
  static Paint getLinePaint(  int idx, boolean reversed ) { return (mLineLib  == null)? errorPaint : mLineLib.getLinePaint( idx, reversed ); }
  static Paint getAreaPaint(  int idx ) { return (mAreaLib  == null)? errorPaint : mAreaLib.getSymbolPaint( idx ); }

  static int getPointLibSize() { return ( mPointLib == null )? 0 : mPointLib.size(); }
  static int getLineLibSize()  { return ( mLineLib  == null )? 0 : mLineLib.size(); }
  static int getAreaLibSize()  { return ( mAreaLib  == null )? 0 : mAreaLib.size(); }


  // -----------------------------------------------------------
  static int getPointLevel( int idx )   { return (mPointLib == null)? DrawingLevel.LEVEL_BASE : mPointLib.getSymbolLevel( idx ); }

  static boolean pointHasText( int index )        { return mPointLib != null && mPointLib.pointHasText( index ); }
  static boolean pointHasValue( int index )       { return mPointLib != null && mPointLib.pointHasValue( index ); }
  static boolean pointHasTextOrValue( int index ) { return mPointLib != null && mPointLib.pointHasTextOrValue( index ); }

  static int getPointCsxLayer( int index )    { return (mPointLib == null)? 0 : mPointLib.pointCsxLayer( index ); }
  static int getPointCsxType( int index )     { return (mPointLib == null)? 0 : mPointLib.pointCsxType( index ); }
  static int getPointCsxCategory( int index ) { return (mPointLib == null)? 0 : mPointLib.pointCsxCategory( index ); }
  static String getPointCsx( int index )      { return (mPointLib == null)? "" : mPointLib.pointCsx( index ); }

  static int getLineCsxLayer( int index )    { return (mLineLib == null)? 0 : mLineLib.lineCsxLayer( index ); }
  static int getLineCsxType( int index )     { return (mLineLib == null)? 0 : mLineLib.lineCsxType( index ); }
  static int getLineCsxCategory( int index ) { return (mLineLib == null)? 0 : mLineLib.lineCsxCategory( index ); }
  static int getLineCsxPen( int index )      { return (mLineLib == null)? 0 : mLineLib.lineCsxPen( index ); }
  // static String getLineGroup( int index ) { return (mLineLib == null)? "" : mLineLib.getLineGroup( index ); }
  static int getLineLevel( int idx )         { return (mLineLib == null)? DrawingLevel.LEVEL_BASE : mLineLib.getSymbolLevel( idx ); }

  static int getAreaCsxLayer( int index )    { return (mAreaLib == null)? 0 : mAreaLib.areaCsxLayer( index ); }
  static int getAreaCsxType( int index )     { return (mAreaLib == null)? 0 : mAreaLib.areaCsxType( index ); }
  static int getAreaCsxCategory( int index ) { return (mAreaLib == null)? 0 : mAreaLib.areaCsxCategory( index ); }
  static int getAreaCsxPen( int index )      { return (mAreaLib == null)? 0 : mAreaLib.areaCsxPen( index ); }
  static int getAreaCsxBrush( int index )    { return (mAreaLib == null)? 0 : mAreaLib.areaCsxBrush( index ); }
  static int getAreaLevel( int idx )         { return (mAreaLib == null)? DrawingLevel.LEVEL_BASE : mAreaLib.getSymbolLevel( idx ); }

  static boolean isPointOrientable( int index )  { return mPointLib != null && mPointLib.isSymbolOrientable( index ); }
  static double getPointOrientation( int index ) { return (mPointLib == null)? 0 : mPointLib.getPointOrientation( index ); }
  // test should not be necessary but Xperia Z Ultra Android 5.1 crashed 2019-10-07
  static void resetPointOrientations( )              { if ( mPointLib != null ) mPointLib.resetOrientations(); }
  static void rotateGradPoint( int index, double a ) { if ( mPointLib != null ) mPointLib.rotateGrad( index, a ); }
  static Path getPointPath( int i )                  { return (mPointLib == null)? null : mPointLib.getPointPath( i ); }
  static Path getPointOrigPath( int i )              { return (mPointLib == null)? null : mPointLib.getPointOrigPath( i ); }

  static boolean isPointLabel( int index )   { return mPointLib != null && index == mPointLib.mPointLabelIndex; }
  static boolean isPointPhoto( int index )   { return mPointLib != null && index == mPointLib.mPointPhotoIndex; }
  static boolean isPointAudio( int index )   { return mPointLib != null && index == mPointLib.mPointAudioIndex; }
  static boolean isPointSection( int index ) { return mPointLib != null && index == mPointLib.mPointSectionIndex; }

  static boolean isPointEnabled( String name ) { return mPointLib != null && mPointLib.isSymbolEnabled( name ); }
  static boolean isLineEnabled( String name )  { return mLineLib  != null && mLineLib.isSymbolEnabled( name ); }
  static boolean isAreaEnabled( String name )  { return mAreaLib  != null && mAreaLib.isSymbolEnabled( name ); }

  static int getPointLabelIndex()   { return (mPointLib == null)? 1 : mPointLib.mPointLabelIndex; }
  static int getPointPhotoIndex()   { return (mPointLib == null)? 0 : mPointLib.mPointPhotoIndex; }
  static int getPointAudioIndex()   { return (mPointLib == null)? 0 : mPointLib.mPointAudioIndex; }
  static int getPointSectionIndex() { return (mPointLib == null)? 2 : mPointLib.mPointSectionIndex; }

  static String getPointName( int idx ) { return (mPointLib == null)? "" : mPointLib.getSymbolName( idx ); } 
  static String getLineName( int idx )  { return (mLineLib  == null)? "" : mLineLib.getSymbolName( idx ); } 
  static String getAreaName( int idx )  { return (mAreaLib  == null)? "" : mAreaLib.getSymbolName( idx ); } 

  // LINE CLOSED
  static boolean isLineClosed( int index )  { return mLineLib != null && mLineLib.isClosed( index ); }
  static boolean isLineSection( int index ) { return mLineLib != null && index == mLineLib.mLineSectionIndex; }
  static boolean isLineWall( int idx )      { return mLineLib != null && idx == mLineLib.mLineWallIndex; }
  static boolean isLineWallGroup( int idx ) { return mLineLib != null && mLineLib.isWall( idx ); }
  static boolean isLineSlope( int idx )     { return mLineLib != null && idx == mLineLib.mLineSlopeIndex; }
  // static int getLineSectionIndex()       { return (mLineLib == null)? 2 : mLineLib.mLineSectionIndex; }
  static int getLineWallIndex()             { return (mLineLib == null)? 1 : mLineLib.mLineWallIndex; }

  // FIXME AREA_ORIENT
  static boolean isAreaOrientable( int index )      { return (mAreaLib != null) && mAreaLib.isSymbolOrientable( index ); }
  static double getAreaOrientation( int index )     { return (mAreaLib == null)? 0 : mAreaLib.getAreaOrientation( index ); }
  static void resetAreaOrientations( )              { if (mAreaLib != null) mAreaLib.resetOrientations(); }
  static void rotateGradArea( int index, double a ) { if (mAreaLib != null) mAreaLib.rotateGrad( index, a ); }

  static Bitmap getAreaBitmap( int index )  { return (mAreaLib == null)? null : mAreaLib.getAreaBitmap( index ); }
  static Shader getAreaShader( int index )  { return (mAreaLib == null)? null : mAreaLib.getAreaShader( index ); }
  static TileMode getAreaXMode( int index ) { return (mAreaLib == null)? TileMode.REPEAT : mAreaLib.getAreaXMode( index ); }
  static TileMode getAreaYMode( int index ) { return (mAreaLib == null)? TileMode.REPEAT : mAreaLib.getAreaYMode( index ); }
  static BitmapShader cloneAreaShader( int index ) { return (mAreaLib == null)? null : mAreaLib.cloneAreaShader( index ); }

  // --------------------------------------------------------------------------
  // LINES

  static boolean isLineStraight( int index ) { return (mLineLib != null) && mLineLib.isStyleStraight( index ); }

  // -----------------------------------------------------------------------
  // AREAS

  static int getAreaColor( int index ) { return mAreaLib.getAreaColor( index ); }

  // --------------------------------------------------------------------------

  static Paint errorPaint       = makePaint( TDColor.FULL_VIOLET,  WIDTH_CURRENT, Paint.Style.FILL_AND_STROKE );
  static Paint highlightPaint   = makePaint( TDColor.HIGH_PINK,    WIDTH_CURRENT, Paint.Style.STROKE );
  static Paint highlightPaint2  = makePaint( TDColor.HIGH_GREEN,   WIDTH_CURRENT, Paint.Style.FILL );
  static Paint highlightPaint3  = makePaint( TDColor.HIGH_RED,     WIDTH_CURRENT, Paint.Style.STROKE );
  static Paint fixedShotPaint   = makePaint( 0xffbbbbbb,           WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint fixedBluePaint   = makePaint( 0xff9999ff,           WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint deepBluePaint    = makePaint( 0xff3366ff,           WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint darkBluePaint    = makePaint( 0x663366ff,           WIDTH_CURRENT, Paint.Style.STROKE); // same as deepBluePaint but with alpha
  static Paint lightBluePaint   = makePaint( 0xff66ccff,           WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint fixedRedPaint    = makePaint( TDColor.FIXED_RED,    WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint fixedYellowPaint = makePaint( TDColor.FIXED_YELLOW, WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint fixedOrangePaint = makePaint( TDColor.FIXED_ORANGE, WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint paintSplayLRUD    = makePaint( TDColor.SPLAY_LRUD,    WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint paintSplayXB      = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint paintSplayComment = makePaint( TDColor.SPLAY_COMMENT,WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint paintSplayXViewed = makePaint( TDColor.SPLAY_NORMAL,         WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint paintSplayXBdash  = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);

  static Paint paintSplayXBdot  = makePaint( TDColor.SPLAY_LIGHT,  WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint paintSplayXVdash = makePaint( TDColor.SPLAY_NORMAL,        WIDTH_CURRENT, Paint.Style.STROKE);
  static Paint paintSplayXVdot  = makePaint( TDColor.SPLAY_NORMAL,       WIDTH_CURRENT, Paint.Style.STROKE);

  static Paint fixedGridPaint    = makePaint( TDColor.DARK_GRID,   WIDTH_FIXED, Paint.Style.STROKE);
  static Paint fixedGrid10Paint  = makePaint( TDColor.GRID,        WIDTH_FIXED, Paint.Style.STROKE);
  static Paint fixedGrid100Paint = makePaint( TDColor.LIGHT_GRID,  WIDTH_FIXED, Paint.Style.STROKE);
  static Paint fixedStationPaint = makePaint( TDColor.REDDISH,     WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  static Paint fixedStationSavedPaint   = makePaint( TDColor.ORANGE, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  static Paint fixedStationActivePaint  = makePaint( TDColor.LIGHT_GREEN, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  static Paint fixedStationBarrierPaint = makePaint( TDColor.FULL_RED, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  static Paint fixedStationHiddenPaint  = makePaint( 0xFF9966ff,  WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  static Paint labelPaint = makePaint( TDColor.WHITE, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  static Paint borderPaint = makePaint( 0x99ffffff, WIDTH_FIXED, Paint.Style.STROKE);
  // stationPaint = makePaint( 0xFFFF6666, WIDTH_FIXED, Paint.Style.STROKE);
  static Paint duplicateStationPaint = makePaint( 0xFFFF66FF, WIDTH_FIXED, Paint.Style.STROKE);
  static Paint referencePaint = makePaint( 0xFFffffff, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  // DEBUG
  // static Paint debugRed = makePaint( TDColor.FULL_RED, WIDTH_FIXED, Paint.Style.STROKE);
  // static Paint debugGreen = makePaint( TDColor.FULL_GREEN, WIDTH_FIXED, Paint.Style.STROKE);
  // static Paint debugBlue  = makePaint( TDColor.FULL_BLUE,  WIDTH_FIXED, Paint.Style.STROKE);

  // static Paint fixedGridPaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
  // static Paint fixedGrid10Paint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
  static Paint mSectionPaint = makePaint( TDColor.ORANGE, 2 * WIDTH_FIXED, Paint.Style.FILL_AND_STROKE );
  // static Paint stationPaint = null;

  // static BitmapDrawable mSymbolHighlight = null;

  // ===========================================================================
  static void makeStationSymbol( Resources res )
  {
    if ( mStationSymbol == null ) {
      String th_name = res.getString( R.string.p_station );
      String name    = res.getString( R.string.thp_station );
      mStationSymbol = new SymbolPoint( name, th_name, null, th_name, 0xffff6633, 
        "addCircle 0 0 0.4 moveTo -3.0 1.73 lineTo 3.0 1.73 lineTo 0.0 -3.46 lineTo -3.0 1.73", false, DrawingLevel.LEVEL_WALL );
    }
  }

  static Paint getStationPaint() { return (mStationSymbol == null)? errorPaint : mStationSymbol.mPaint; }
  static Path  getStationPath()  { return (mStationSymbol == null)? null : mStationSymbol.mPath; }

  static void loadAllLibraries( Context ctx, Resources res ) 
  {
    makeStationSymbol( res );
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
    if ( palette == null ) return;
    if ( mPointLib != null ) mPointLib.makeEnabledListFromPalette( palette, clear );
    if ( mLineLib  != null ) mLineLib.makeEnabledListFromPalette( palette, clear );
    if ( mAreaLib  != null ) mAreaLib.makeEnabledListFromPalette( palette, clear );
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
      // errorPaint       = makePaint( TDColor.FULL_VIOLET,  WIDTH_CURRENT, Paint.Style.FILL_AND_STROKE );
      // highlightPaint   = makePaint( TDColor.HIGH_PINK,    WIDTH_CURRENT, Paint.Style.STROKE );
      // highlightPaint2  = makePaint( TDColor.HIGH_GREEN,   WIDTH_CURRENT, Paint.Style.FILL );
      // highlightPaint3  = makePaint( TDColor.HIGH_RED,     WIDTH_CURRENT, Paint.Style.STROKE );
      // fixedShotPaint   = makePaint( 0xffbbbbbb,           WIDTH_CURRENT, Paint.Style.STROKE);
      // fixedBluePaint   = makePaint( 0xff9999ff,           WIDTH_CURRENT, Paint.Style.STROKE);
      // deepBluePaint    = makePaint( 0xff3366ff,           WIDTH_CURRENT, Paint.Style.STROKE);
      // darkBluePaint    = makePaint( 0x663366ff,           WIDTH_CURRENT, Paint.Style.STROKE); // same as deepBluePaint but with alpha
      // lightBluePaint   = makePaint( 0xff66ccff,           WIDTH_CURRENT, Paint.Style.STROKE);
      // fixedRedPaint    = makePaint( TDColor.FIXED_RED,    WIDTH_CURRENT, Paint.Style.STROKE);
      // fixedYellowPaint = makePaint( TDColor.FIXED_YELLOW, WIDTH_CURRENT, Paint.Style.STROKE);
      // fixedOrangePaint = makePaint( TDColor.FIXED_ORANGE, WIDTH_CURRENT, Paint.Style.STROKE);
      // paintSplayLRUD    = makePaint( TDColor.SPLAY_LRUD,    WIDTH_CURRENT, Paint.Style.STROKE);
      // paintSplayXB      = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);
      // paintSplayComment = makePaint( TDColor.SPLAY_COMMENT,WIDTH_CURRENT, Paint.Style.STROKE);
      // paintSplayXViewed = makePaint( TDColor.SPLAY_NORMAL,         WIDTH_CURRENT, Paint.Style.STROKE);
      // paintSplayXBdash  = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);

      float[] x = new float[2];
      x[0] = 24; // FIXME
      x[1] =  8;
      DashPathEffect dash3 = new DashPathEffect( x, 0 );
      paintSplayXBdash.setPathEffect( dash3 );

      // paintSplayXVdash = makePaint( TDColor.SPLAY_NORMAL,        WIDTH_CURRENT, Paint.Style.STROKE);
      paintSplayXVdash.setPathEffect( dash3 );

      // paintSplayXBdot  = makePaint( TDColor.SPLAY_LIGHT,  WIDTH_CURRENT, Paint.Style.STROKE);
      // float[] x = new float[2];
      x[0] = 14; // FIXME
      x[1] =  8; 
      DashPathEffect dash4 = new DashPathEffect( x, 0 );
      paintSplayXBdot.setPathEffect( dash4 );

      // paintSplayXVdot  = makePaint( TDColor.SPLAY_NORMAL,       WIDTH_CURRENT, Paint.Style.STROKE);
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
        if ( ! "user".equals(fname) ) palette.addPointFilename( fname );
      }
    }
    SymbolLineLibrary lines = mLineLib;
    if ( lines != null ) {
      for ( Symbol p : lines.getSymbols() ) if ( p.isEnabled() ) {
        String fname = p.getThName();
        if ( ! "user".equals(fname) ) palette.addLineFilename( fname );
      }
    }
    SymbolAreaLibrary areas = mAreaLib;
    if ( areas != null ) {
      for ( Symbol p : areas.getSymbols() ) if ( p.isEnabled() ) {
        String fname = p.getThName();
        if ( ! "user".equals(fname) ) palette.addAreaFilename( fname );
      }
    }
    return palette;
  }

  static void toDataStream( DataOutputStream dos )
  {
    if ( mPointLib != null ) { mPointLib.toDataStream(dos); } else { try { dos.writeUTF(""); } catch (IOException e) { } }
    if ( mLineLib  != null ) { mLineLib.toDataStream(dos);  } else { try { dos.writeUTF(""); } catch (IOException e) { } }
    if ( mAreaLib  != null ) { mAreaLib.toDataStream(dos);  } else { try { dos.writeUTF(""); } catch (IOException e) { } }
  }

}
