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
package com.topodroid.TDX;

import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

// import java.lang.Math;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
import android.graphics.PathDashPathEffect;
// import android.graphics.PathEffect;
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

import java.util.ArrayList;

/**
 * generic brush
 */
@SuppressWarnings("SameParameterValue")
public class BrushManager
{
  static final int WIDTH_CURRENT = 1;
  static final int WIDTH_FIXED   = 1;
  static final int WIDTH_PREVIEW = 1;

  // TODO make private
  static public  SymbolPointLibrary mPointLib = null; // TH2EDIT private
  static public  SymbolLineLibrary  mLineLib  = null; // TH2EDIT private
  static public  SymbolAreaLibrary  mAreaLib  = null; // TH2EDIT private
  static private SymbolPoint mStationSymbol   = null;
  static private int mAlpha = 0xff; // splay alpha
  static private boolean mHasSymbolLibraries = false;

  // -----------------------------------------------------------
  /* LOAD_MISSING
  static boolean tryLoadMissingPoint( String thname ) { return mPointLib != null && mPointLib.tryLoadMissingPoint( thname ); }
  static boolean tryLoadMissingLine( String thname )  { return mLineLib  != null && mLineLib.tryLoadMissingLine( thname ); }
  static boolean tryLoadMissingArea( String thname )  { return mAreaLib  != null && mAreaLib.tryLoadMissingArea( thname ); }
  */

  /** @return true if the manager has the three symbol libraries
   */
  static boolean hasSymbolLibraries() { return mHasSymbolLibraries; }

  /** set has SymbolLibraries
   * @note called by the MainWindow setup thread
   */
  static void setHasSymbolLibraries( boolean what ) { mHasSymbolLibraries = what; }

  public static SymbolPointLibrary getPointLib() { return mPointLib; }
  public static SymbolLineLibrary  getLineLib()  { return mLineLib; }
  public static SymbolAreaLibrary  getAreaLib()  { return mAreaLib; }

  /** @return true if the point symbol round-trip has the given value
   * @param path   symbol instance
   * @param rt     round-trip value
   */
  public static boolean isPointRoundTrip( DrawingPointPath path, int rt )
  { return (mPointLib != null) && mPointLib.getSymbolByIndex( path.mPointType ).mRoundTrip == rt; }

  /** @return true if the line symbol round-trip has the given value
   * @param path   symbol instance
   * @param rt     round-trip value
   */
  public static boolean isLineRoundTrip( DrawingLinePath path, int rt )
  { return (mLineLib != null) && mLineLib.getSymbolByIndex( path.mLineType ).mRoundTrip == rt; }

  /** @return true if the area symbol round-trip has the given value
   * @param path   symbol instance
   * @param rt     round-trip value
   */
  public static boolean isAreaRoundTrip( DrawingAreaPath path, int rt )
  { return (mAreaLib != null) && mAreaLib.getSymbolByIndex( path.mAreaType ).mRoundTrip == rt; }

  /** @return the list of the names of the line symbols (or null)
   */
  static ArrayList< String > getLineNames() { return (mLineLib == null)? (new ArrayList<>()) : mLineLib.getSymbolNames(); }

  /** @return the list of the names of the line symbols, excluding the "section" line (or null)
   */
  static ArrayList< String > getLineNamesNoSection() { return (mLineLib == null)? (new ArrayList<>()) : mLineLib.getSymbolNamesExcept( SymbolLibrary.SECTION ); }

  /** @return the list of the names of the area symbols (or null)
   */
  static ArrayList< String > getAreaNames() { return (mAreaLib == null)? (new ArrayList<>()) : mAreaLib.getSymbolNames(); }

  /** @return the index of a point symbol in the library (or -1)
   * @param point   point symbol
   */
  static int getPointIndex( Symbol point ) { return (mPointLib == null)? -1 : mPointLib.getSymbolIndex( point ); }

  /** @return the index of a line symbol in the library (or -1)
   * @param line   line symbol
   */
  static int getLineIndex( Symbol line )   { return (mLineLib  == null)? -1 : mLineLib.getSymbolIndex( line ); }

  /** @return the index of an area symbol in the library (or -1)
   * @param area   area symbol
   */
  static int getAreaIndex( Symbol area )   { return (mAreaLib  == null)? -1 : mAreaLib.getSymbolIndex( area ); }

  /** @return the index of a point symbol in the library by its Therion name (or -1)
   * @param thname   Therion name of the point
   */
  public static int getPointIndexByThName( String thname ) { return (mPointLib == null)? -1 : mPointLib.getSymbolIndexByThName( thname ); }

  /** @return the index of a line symbol in the library by its Therion name (or -1)
   * @param thname   Therion name of the line
   */
  public static int getLineIndexByThName( String thname )  { return (mLineLib  == null)? -1 : mLineLib.getSymbolIndexByThName( thname ); }

  /** @return the index of an area symbol in the library by its Therion name (or -1)
   * @param thname   Therion name of the area
   */
  public static int getAreaIndexByThName( String thname )  { return (mAreaLib  == null)? -1 : mAreaLib.getSymbolIndexByThName( thname ); } // TH2EDIT package

  /** @return a point symbol in the library by its Therion name (or null)
   * @param thname   Therion name of the point
   */
  static Symbol getPointByThName( String thname ) { return (mPointLib == null)? null : mPointLib.getSymbolByThName( thname ); }

  /** @return a line symbol in the library by its Therion name (or null)
   * @param thname   Therion name of the line
   */
  static Symbol getLineByThName( String thname )  { return (mLineLib  == null)? null : mLineLib.getSymbolByThName( thname ); }

  /** @return an area symbol in the library by its Therion name (or null)
   * @param thname   Therion name of the area
   */
  static Symbol getAreaByThName( String thname )  { return (mAreaLib  == null)? null : mAreaLib.getSymbolByThName( thname ); }

  public static SymbolPoint getPointByIndex( int idx ) { return (mPointLib == null)? null : (SymbolPoint)mPointLib.getSymbolByIndex( idx ); }
  public static SymbolLine  getLineByIndex(  int idx ) { return (mLineLib  == null)? null : (SymbolLine)mLineLib.getSymbolByIndex( idx ); }
  public static SymbolArea  getAreaByIndex(  int idx ) { return (mAreaLib  == null)? null : (SymbolArea)mAreaLib.getSymbolByIndex( idx ); }

  static boolean hasPointByThName( String thname ) { return mPointLib != null && mPointLib.hasSymbolByThName( thname ); }
  static boolean hasLineByThName( String thname )  { return mLineLib  != null && mLineLib.hasSymbolByThName( thname ); }
  static boolean hasAreaByThName( String thname )  { return mAreaLib  != null && mAreaLib.hasSymbolByThName( thname ); }

  static int getPointIndexByThNameOrGroup( String thname, String group ) { return (mPointLib == null)? -1 : mPointLib.getSymbolIndexByThNameOrGroup( thname, group ); }
  static int getLineIndexByThNameOrGroup( String thname, String group )  { return (mLineLib  == null)? -1 : mLineLib.getSymbolIndexByThNameOrGroup( thname, group ); }
  static int getAreaIndexByThNameOrGroup( String thname, String group )  { return (mAreaLib  == null)? -1 : mAreaLib.getSymbolIndexByThNameOrGroup( thname, group ); }

  static void setRecentPoints( Symbol[] points ) { if (mPointLib != null) mPointLib.setRecentSymbols( points ); }
  static void setRecentLines( Symbol[] lines )   { if (mLineLib  != null) mLineLib.setRecentSymbols( lines ); }
  static void setRecentAreas( Symbol[] areas )   { if (mAreaLib  != null) mAreaLib.setRecentSymbols( areas ); }

  public static String getPointThName( int idx ) { return (mPointLib == null)? null : mPointLib.getSymbolThName( idx ); }
  public static String getLineThName( int idx )  { return (mLineLib  == null)? null : mLineLib.getSymbolThName( idx ); }
  public static String getAreaThName( int idx )  { return (mAreaLib  == null)? null : mAreaLib.getSymbolThName( idx ); }

  public static String getPointFullThName( int idx ) { return (mPointLib == null)? null : mPointLib.getSymbolFullThName( idx ); }
  public static String getLineFullThName( int idx )  { return (mLineLib  == null)? null : mLineLib.getSymbolFullThName( idx ); }
  public static String getAreaFullThName( int idx )  { return (mAreaLib  == null)? null : mAreaLib.getSymbolFullThName( idx ); }

  public static String getPointFullThNameEscapedColon( int idx ) { return (mPointLib == null)? null : mPointLib.getSymbolFullThNameEscapedColon( idx ); }
  public static String getLineFullThNameEscapedColon( int idx )  { return (mLineLib  == null)? null : mLineLib.getSymbolFullThNameEscapedColon( idx ); }
  public static String getAreaFullThNameEscapedColon( int idx )  { return (mAreaLib  == null)? null : mAreaLib.getSymbolFullThNameEscapedColon( idx ); }

  static String getPointGroup( int idx ) { return (mPointLib == null)? null : mPointLib.getSymbolGroup( idx ); }
  static String getLineGroup( int idx )  { return (mLineLib  == null)? null : mLineLib.getSymbolGroup( idx ); }
  static String getAreaGroup( int idx )  { return (mAreaLib  == null)? null : mAreaLib.getSymbolGroup( idx ); }
  public static String getLineWallGroup() { return (mLineLib == null)? SymbolLibrary.WALL : getLineGroup(BrushManager.mLineLib.mLineWallIndex); }

  static String getPointDefaultOptions( int idx ) { return (mPointLib == null)? null : mPointLib.getSymbolDefaultOptions(idx ); }
  static String getLineDefaultOptions( int idx ) { return (mLineLib == null)? null : mLineLib.getSymbolDefaultOptions(idx ); }
  static String getAreaDefaultOptions( int idx ) { return (mAreaLib == null)? null : mAreaLib.getSymbolDefaultOptions(idx ); }

  static boolean hasPoint( int idx ) { return mPointLib != null && idx < mPointLib.size(); }
  static boolean hasLine( int idx )  { return mLineLib  != null && idx < mLineLib.size(); }
  static boolean hasArea( int idx )  { return mAreaLib  != null && idx < mAreaLib.size(); }

  static Paint getPointPaint( int idx ) { return (mPointLib == null)? errorPaint : mPointLib.getSymbolPaint( idx ); }
  static Paint getLinePaint(  int idx ) { return (mLineLib  == null)? errorPaint : mLineLib.getSymbolPaint( idx ); }
  static Paint getLinePaint(  int idx, boolean reversed ) { return (mLineLib  == null)? errorPaint : mLineLib.getLinePaint( idx, reversed ); }
  static Paint getAreaPaint(  int idx ) { return (mAreaLib  == null)? errorPaint : mAreaLib.getSymbolPaint( idx ); }

  static int getPointColor(  int idx ) { return (mPointLib  == null)? 0xffffffff : mPointLib.getSymbolPaint( idx ).getColor(); }
  static int getLineColor(  int idx )  { return (mLineLib  == null)? 0xffffffff : mLineLib.getSymbolPaint( idx ).getColor(); }
  static int getAreaColor(  int idx )  { return (mAreaLib  == null)? 0xffffffff : mAreaLib.getSymbolPaint( idx ).getColor(); }

  public static int getPointLibSize() { return ( mPointLib == null )? 0 : mPointLib.size(); }
  public static int getLineLibSize()  { return ( mLineLib  == null )? 0 : mLineLib.size(); }
  public static int getAreaLibSize()  { return ( mAreaLib  == null )? 0 : mAreaLib.size(); }

  public static boolean hasLineEffect( int index ) { return mLineLib != null && mLineLib.hasEffect( index ); }
  static int getLineStyleX( int index ) { return (mLineLib == null)? 1 : mLineLib.getStyleX( index ); }

  static boolean isAreaCloseHorizontal( int index ) { return mAreaLib != null && mAreaLib.isCloseHorizontal( index ); }

  // -----------------------------------------------------------
  static int getPointLevel( int idx )   { return (mPointLib == null)? DrawingLevel.LEVEL_BASE : mPointLib.getSymbolLevel( idx ); }

  static boolean pointHasText( int index )        { return mPointLib != null && mPointLib.pointHasText( index ); }
  static boolean pointHasValue( int index )       { return mPointLib != null && mPointLib.pointHasValue( index ); }
  static boolean pointHasTextOrValue( int index ) { return mPointLib != null && mPointLib.pointHasTextOrValue( index ); }

  // static int getPointCsxLayer( int index )    { return (mPointLib == null)? 0 : mPointLib.pointCsxLayer( index ); }
  // static int getPointCsxType( int index )     { return (mPointLib == null)? 0 : mPointLib.pointCsxType( index ); }
  // static int getPointCsxCategory( int index ) { return (mPointLib == null)? 0 : mPointLib.pointCsxCategory( index ); }
  // static String getPointCsx( int index )      { return (mPointLib == null)? "" : mPointLib.pointCsx( index ); }

  // static int getLineCsxLayer( int index )    { return (mLineLib == null)? 0 : mLineLib.lineCsxLayer( index ); }
  // static int getLineCsxType( int index )     { return (mLineLib == null)? 0 : mLineLib.lineCsxType( index ); }
  // static int getLineCsxCategory( int index ) { return (mLineLib == null)? 0 : mLineLib.lineCsxCategory( index ); }
  // static int getLineCsxPen( int index )      { return (mLineLib == null)? 0 : mLineLib.lineCsxPen( index ); }

  // static String getLineGroup( int index ) { return (mLineLib == null)? "" : mLineLib.getLineGroup( index ); }
  static int getLineLevel( int idx )         { return (mLineLib == null)? DrawingLevel.LEVEL_BASE : mLineLib.getSymbolLevel( idx ); }

  // static int getAreaCsxLayer( int index )    { return (mAreaLib == null)? 0 : mAreaLib.areaCsxLayer( index ); }
  // static int getAreaCsxType( int index )     { return (mAreaLib == null)? 0 : mAreaLib.areaCsxType( index ); }
  // static int getAreaCsxCategory( int index ) { return (mAreaLib == null)? 0 : mAreaLib.areaCsxCategory( index ); }
  // static int getAreaCsxPen( int index )      { return (mAreaLib == null)? 0 : mAreaLib.areaCsxPen( index ); }
  // static int getAreaCsxBrush( int index )    { return (mAreaLib == null)? 0 : mAreaLib.areaCsxBrush( index ); }
  static int getAreaLevel( int idx )         { return (mAreaLib == null)? DrawingLevel.LEVEL_BASE : mAreaLib.getSymbolLevel( idx ); }

  public static boolean isPointDeclinable( int index )  { return mPointLib != null && mPointLib.isSymbolDeclinable( index ); } // TH2EDIT package
  public static boolean isPointOrientable( int index )  { return mPointLib != null && mPointLib.isSymbolOrientable( index ); } // TH2EDIT package
  static double getPointOrientation( int index ) { return (mPointLib == null)? 0 : mPointLib.getPointOrientation( index ); }
  // test should not be necessary but Xperia Z Ultra Android 5.1 crashed 2019-10-07
  public static void resetPointOrientations( )              { if ( mPointLib != null ) mPointLib.resetOrientations(); } // TH2EDIT package
  public static void rotateGradPoint( int index, double a ) { if ( mPointLib != null ) mPointLib.rotateGrad( index, a ); } // TH2EDIT package
  static Path getPointPath( int i )                  { return (mPointLib == null)? null : mPointLib.getPointPath( i ); }
  static Path getPointOrigPath( int i )              { return (mPointLib == null)? null : mPointLib.getPointOrigPath( i ); }

  // system and special points
  public static boolean isPointUser( int index )    { return mPointLib != null && index == mPointLib.mPointUserIndex; }
  public static boolean isPointLabel( int index )   { return mPointLib != null && index == mPointLib.mPointLabelIndex; }
  public static boolean isPointPhoto( int index )   { return mPointLib != null && index == mPointLib.mPointPhotoIndex; }
  public static boolean isPointAudio( int index )   { return mPointLib != null && index == mPointLib.mPointAudioIndex; }
  public static boolean isPointSection( int index ) { return mPointLib != null && index == mPointLib.mPointSectionIndex; }
  public static boolean isPointPicture( int index ) { return mPointLib != null && index == mPointLib.mPointPictureIndex; }

  public static boolean isPointMedia( int index )
  { return mPointLib != null && ( index == mPointLib.mPointPictureIndex || index == mPointLib.mPointPhotoIndex || index == mPointLib.mPointAudioIndex ); }

  static boolean isPointEnabled( String name ) { return mPointLib != null && mPointLib.isSymbolEnabled( name ); }
  static boolean isLineEnabled( String name )  { return mLineLib  != null && mLineLib.isSymbolEnabled( name ); }
  static boolean isAreaEnabled( String name )  { return mAreaLib  != null && mAreaLib.isSymbolEnabled( name ); }

  public static int getPointLabelIndex()   { return (mPointLib == null)? 1 : mPointLib.mPointLabelIndex; } // TH2EDIT package
  static int getPointPhotoIndex()   { return (mPointLib == null)? 0 : mPointLib.mPointPhotoIndex; }
  static int getPointAudioIndex()   { return (mPointLib == null)? 0 : mPointLib.mPointAudioIndex; }
  static int getPointSectionIndex() { return (mPointLib == null)? 2 : mPointLib.mPointSectionIndex; }
  static int getPointPictureIndex() { return (mPointLib == null)? 0 : mPointLib.mPointPictureIndex; }

  static String getPointName( int idx ) { return (mPointLib == null)? "" : mPointLib.getSymbolName( idx ); } 
  static String getLineName( int idx )  { return (mLineLib  == null)? "" : mLineLib.getSymbolName( idx ); } 
  static String getAreaName( int idx )  { return (mAreaLib  == null)? "" : mAreaLib.getSymbolName( idx ); } 

  // LINE CLOSED
  static boolean isLineClosed( int index )  { return mLineLib != null && mLineLib.isClosed( index ); }
  static boolean isLineSection( int index ) { return mLineLib != null && index == mLineLib.mLineSectionIndex; }
  static boolean isLineUser( int idx )      { return mLineLib != null && idx == mLineLib.mLineUserIndex; }
  static boolean isLineWall( int idx )      { return mLineLib != null && idx == mLineLib.mLineWallIndex; }
  static boolean isLineWallGroup( int idx ) { return mLineLib != null && mLineLib.isWall( idx ); }
  static boolean isLineSlope( int idx )     { return mLineLib != null && idx == mLineLib.mLineSlopeIndex; }

  /** check if the line type is joinable
   * @param idx  line type index
   */
  static boolean isLineJoinable( int idx )  { return mLineLib != null && idx != mLineLib.mLineSectionIndex && ! mLineLib.isClosed( idx ); }

  static int getLineSectionIndex()          { return (mLineLib == null)? 2 : mLineLib.mLineSectionIndex; }
  static int getLineWallIndex()             { return (mLineLib == null)? 1 : mLineLib.mLineWallIndex; }
  static int getLineSlopeIndex()            { return (mLineLib == null)? -1 : mLineLib.mLineSlopeIndex; }

  // FIXME AREA_ORIENT
  static boolean isAreaOrientable( int index )      { return mAreaLib != null && mAreaLib.isSymbolOrientable( index ); }
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

  static boolean isLineStraight( int index ) { return mLineLib != null && mLineLib.isStyleStraight( index ); }

  // --------------------------------------------------------------------------

  // public static final Paint wavyPaint = makeWavyPaint( 0xffbbbbbb, WIDTH_FIXED, Paint.Style.STROKE); // color of fixedShotPaint
  public static final Paint wavyPaint = makeWavyPaint( TDColor.DARK_ORANGE, WIDTH_FIXED, Paint.Style.STROKE); 

  public static final Paint sideDragPaint    = makePaint( 0x99333333,           WIDTH_CURRENT, Paint.Style.FILL );
  public static final Paint errorPaint       = makePaint( TDColor.FULL_VIOLET,  WIDTH_CURRENT, Paint.Style.FILL_AND_STROKE );
  public static final Paint highlightPaint   = makePaint( TDColor.HIGH_PINK,    WIDTH_CURRENT, Paint.Style.STROKE );
  public static final Paint highlightPaint2  = makePaint( TDColor.HIGH_GREEN,   WIDTH_CURRENT, Paint.Style.FILL );
  public static final Paint highlightPaint3  = makePaint( TDColor.HIGH_RED,     WIDTH_CURRENT, Paint.Style.STROKE );
  public static final Paint fixedShotPaint   = makePaint( 0xffbbbbbb,           WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint fixedBluePaint   = makePaint( 0xff9999ff,           WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint deepBluePaint    = makePaint( 0xff3366ff,           WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint darkBluePaint    = makePaint( 0x663366ff,           WIDTH_CURRENT, Paint.Style.STROKE); // same as deepBluePaint but with alpha
  public static final Paint lightBluePaint   = makePaint( 0xff66ccff,           WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint fixedRedPaint    = makePaint( TDColor.FIXED_RED,    WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint fixedYellowPaint = makePaint( TDColor.FIXED_YELLOW, WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint fixedOrangePaint = makePaint( TDColor.FIXED_ORANGE, WIDTH_CURRENT, Paint.Style.STROKE);

  public static final Paint paintSplayLRUD    = makePaint( TDColor.SPLAY_LRUD,    WIDTH_CURRENT, Paint.Style.STROKE);  // GREEN
  public static final Paint paintSplayXB      = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);  // BLUE
  public static final Paint paintSplayComment = makePaint( TDColor.SPLAY_COMMENT, WIDTH_CURRENT, Paint.Style.STROKE);  // VERYDARK_GRAY
  public static final Paint paintSplayXViewed = makePaint( TDColor.SPLAY_NORMAL,  WIDTH_CURRENT, Paint.Style.STROKE);  // MID_BLUE
  public static final Paint paintSplayXBdash  = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);  // BLUE
  public static final Paint paintSplayXBdot   = makePaint( TDColor.SPLAY_LIGHT,   WIDTH_CURRENT, Paint.Style.STROKE);  // BLUE
  public static final Paint paintSplayXVdash  = makePaint( TDColor.SPLAY_NORMAL,  WIDTH_CURRENT, Paint.Style.STROKE);  // MID_BLUE
  public static final Paint paintSplayXVdot   = makePaint( TDColor.SPLAY_NORMAL,  WIDTH_CURRENT, Paint.Style.STROKE);  // MID_BLUE
  public static final Paint paintSplayLatest  = makePaint( TDColor.SPLAY_LATEST,  WIDTH_CURRENT, Paint.Style.STROKE);

  public static final Paint paintSplayFeature   = makePaint( TDColor.SPLAY_FEATURE,   WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint paintSplayRidge     = makePaint( TDColor.SPLAY_RIDGE,     WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint paintSplayBacksight = makePaint( TDColor.SPLAY_BACKSIGHT, WIDTH_CURRENT, Paint.Style.STROKE);
  public static final Paint paintSplayGeneric   = makePaint( TDColor.SPLAY_GENERIC,   WIDTH_CURRENT, Paint.Style.STROKE);

  public static final Paint fixedGridPaint    = makePaint( TDColor.DARK_GRID,   WIDTH_FIXED, Paint.Style.STROKE);
  public static final Paint fixedGrid10Paint  = makePaint( TDColor.GRID,        WIDTH_FIXED, Paint.Style.STROKE);
  public static final Paint fixedGrid100Paint = makePaint( TDColor.LIGHT_GRID,  WIDTH_FIXED, Paint.Style.STROKE);

  public static final Paint fixedStationPaint = makePaint( TDColor.REDDISH,     WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  public static final Paint fixedStationSavedPaint   = makePaint( TDColor.ORANGE, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  public static final Paint fixedStationActivePaint  = makePaint( TDColor.LIGHT_GREEN, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  public static final Paint fixedStationBarrierPaint = makePaint( TDColor.FULL_RED, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  public static final Paint fixedStationHiddenPaint  = makePaint( 0xFF9966ff,  WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  public static final Paint labelPaint = makePaint( TDColor.WHITE, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  public static final Paint blackPaint = makePaint( TDColor.BLACK, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  public static final Paint borderPaint = makePaint( 0x99ffffff, WIDTH_FIXED, Paint.Style.STROKE);
  // public static final Paint stationPaint = makePaint( 0xFFFF6666, WIDTH_FIXED, Paint.Style.STROKE);
  public static final Paint duplicateStationPaint = makePaint( 0xFFFF66FF, WIDTH_FIXED, Paint.Style.STROKE);
  public static final Paint referencePaint = makePaint( 0xFFffffff, WIDTH_FIXED, Paint.Style.FILL_AND_STROKE);
  public static final Paint badLoopPaint = errorPaint;
  public static final Paint fgLinePaint = makePaint( 0xffff9933, WIDTH_FIXED, Paint.Style.STROKE);
  public static final Paint bgLinePaint = makePaint( 0x99ff9933, WIDTH_FIXED, Paint.Style.STROKE);
  // DEBUG
  public static final Paint fullRedPaint   = makePaint( TDColor.FULL_RED,   WIDTH_FIXED, Paint.Style.STROKE);
  public static final Paint fullGreenPaint = makePaint( TDColor.FULL_GREEN, WIDTH_FIXED, Paint.Style.STROKE);
  public static final Paint fullBluePaint  = makePaint( TDColor.FULL_BLUE,  WIDTH_FIXED, Paint.Style.STROKE);

  // fixedGridPaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
  // fixedGrid10Paint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
  public static final Paint mSectionPaint = makePaint( TDColor.ORANGE, 2 * WIDTH_FIXED, Paint.Style.FILL_AND_STROKE );
  public static final Paint mLSidePaint   = makePaint( TDColor.WHITE, 2 * WIDTH_FIXED, Paint.Style.FILL_AND_STROKE );
  // public static final Paint stationPaint = null;

  // static BitmapDrawable mSymbolHighlight = null;

  // ===========================================================================
  /** make the point symbol "station"
   * @param res   resources
   */
  static private void makeStationSymbol( Resources res )
  {
    if ( mStationSymbol == null ) {
      // String th_name = res.getString( R.string.p_station );
      String name    = res.getString( R.string.thp_station );
      mStationSymbol = new SymbolPoint( name, SymbolLibrary.STATION, null, SymbolLibrary.STATION, 0xffff6633, 
        "addCircle 0 0 0.4 moveTo -3.0 1.73 lineTo 3.0 1.73 lineTo 0.0 -3.46 lineTo -3.0 1.73", false, DrawingLevel.LEVEL_WALL, Symbol.W2D_WALLS_SYM );
    }
  }

  /** @return the paint of the point symbol "station"
   */
  static Paint getStationPaint() { return (mStationSymbol == null)? errorPaint : mStationSymbol.mPaint; }

  /** @return the drawing path of the point symbol "station"
   */
  static Path  getStationPath()  { return (mStationSymbol == null)? null : mStationSymbol.mPath; }

  /** reload the three symbols libraries
   * @param ctx   context
   * @param res   resources
   */
  static void loadAllSymbolLibraries( Context ctx, Resources res ) 
  {
    // TDLog.v("BRUSH load libraries" );
    // mHasSymbolLibraries = false; // TODO FIXME needed ?
    makeStationSymbol( res );
    reloadPointLibrary( ctx, res );
    reloadLineLibrary( res );
    reloadAreaLibrary( res );
    // mHasSymbolLibraries = true;
  }

  /** reset symbol config-enabled values and all that
   */
  static void initAllSymbolIndices()
  {
    if ( mPointLib != null ) mPointLib.initIndices();
    if ( mLineLib  != null ) mLineLib.initIndices();
    if ( mAreaLib  != null ) mAreaLib.initIndices();
  }

  /** reload the point symbols
   * @param ctx   context
   * @param res   resources
   */
  public static void reloadPointLibrary( Context ctx, Resources res )
  {
    // TDLog.v("BRUSH load point library" );
    mPointLib = new SymbolPointLibrary( ctx, res );
    // mPointLib.loadUserPoints();
  }

  /** reload the line symbols
   * @param res   resources
   */
  public static void reloadLineLibrary( Resources res )
  {
    // TDLog.v("BRUSH load line library" );
    mLineLib = new SymbolLineLibrary( res );
    // mLineLib.loadUserLines();
  }

  /** reload the area symbols
   * @param res   resources
   */
  public static void reloadAreaLibrary( Resources res )
  {
    // TDLog.v("BRUSH load area library" );
    mAreaLib = new SymbolAreaLibrary( res );
    // mAreaLib.loadUserAreas();
  }

  /** make the list of enabled symbols starting from a palette
   * @param palette    set of symbols filenames
   * @param clear      whether the disable all symbols first
   */
  static void makeEnabledListFromPalette( SymbolsPalette palette, boolean clear ) 
  {
    if ( palette == null ) return;
    // TDLog.v("make enable list from palette - clear " + clear );
    if ( mPointLib != null ) mPointLib.makeEnabledListFromPalette( palette, clear );
    if ( mLineLib  != null ) mLineLib.makeEnabledListFromPalette( palette, clear );
    if ( mAreaLib  != null ) mAreaLib.makeEnabledListFromPalette( palette, clear );
  }

  /** make the list of enabled symbols from configuration
   * @param palette    set of symbols filenames
   * @note disable all symbols first
   */
  static void makeEnabledListFromConfig( )
  {
    // TDLog.v("make enable list from config ");
    if ( mPointLib != null ) mPointLib.makeEnabledListFromConfig( false );
    if ( mLineLib  != null ) mLineLib.makeEnabledListFromConfig( false );
    if ( mAreaLib  != null ) mAreaLib.makeEnabledListFromConfig( true );
  }

  /** make a STROKE paint with fixed width (for fixed path)
   * @param color   paint color
   * @return the paint
   */
  static Paint makePaint( int color ) { return makePaint( color, WIDTH_FIXED, Paint.Style.STROKE ); }

  // static Paint makePaint( int color, int width ) { return makePaint( color, width, Paint.Style.STROKE ); }

  /** make a paint 
   * @param color   paint color
   * @param width   paint stroke width
   * @param style   paint type (STROKE, FILL, or FILL_AND_STROKE)
   * @return the paint
   * @note used by TdmViewCommand
   */
  static public Paint makePaint( int color, int width, Paint.Style style )
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

  static public Paint makeWavyPaint( int color, int width, Paint.Style style )
  {
    Paint paint = new Paint();
    paint.setDither(true);
    paint.setColor( color );
    paint.setStyle( style );
    paint.setStrokeJoin(Paint.Join.ROUND);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth( width );
    Path path = new Path();
    path.moveTo(  0,  1 );
    path.lineTo( 10, 11 );
    path.lineTo( 30, -9 );
    path.lineTo( 40,  1 );
    path.lineTo( 40, -1 );
    path.lineTo( 30,-11 );
    path.lineTo( 10,  9 );
    path.moveTo(  0, -1 );
    paint.setPathEffect( new PathDashPathEffect( path, 40, 0, PathDashPathEffect.Style.MORPH ) );
    return paint;
  }

  /** set the color of splay dash lines
   * @param color   color (rrggbb)
   */
  static public void setSplayDashColor( int color )
  {
    // if ( paintSplayXBdash != null ) { // always true
      paintSplayXBdash.setColor( color );
      paintSplayXBdash.setAlpha( mAlpha );
    // } else {
    //   TDLog.v("Warning: null splay dash paint");
    // }
  }

  /** set the color of splay dot lines
   * @param color   color (rrggbb)
   */
  static public void setSplayDotColor( int color )
  {
    // if ( paintSplayXBdot != null ) { // always true
      paintSplayXBdot.setColor( color );
      paintSplayXBdot.setAlpha( mAlpha );
    // } else {
    //   TDLog.v("Warning: null splay dot paint");
    // }
  }

  /** set the latest-splay color
   * @param color   color (rrggbb)
   */
  static public void setSplayLatestColor( int color )
  {
    paintSplayLatest.setColor( color );
    paintSplayLatest.setAlpha( mAlpha );
  }

  /** set the splay opacity
   * @param alpha   opacity, in [0,100], 0=transparent
   * @note the alpha is saved in mAlpha
   */
  public static void setSplayAlpha( int alpha )
  {
    mAlpha = (alpha * 255)/100;
    /* if ( paintSplayLRUD    != null ) */ paintSplayLRUD.setAlpha( mAlpha );
    /* if ( paintSplayXB      != null ) */ paintSplayXB.setAlpha( mAlpha );
    /* if ( paintSplayComment != null ) */ paintSplayComment.setAlpha( mAlpha );  // commented splay
    /* if ( paintSplayXViewed != null ) */ paintSplayXViewed.setAlpha( mAlpha );  // cross-section splay2 (at viewed station)
    /* if ( paintSplayXBdash  != null ) */ paintSplayXBdash.setAlpha( mAlpha );  // dash splay
    /* if ( paintSplayXBdot   != null ) */ paintSplayXBdot.setAlpha( mAlpha );  // dot splay
    /* if ( paintSplayXVdash  != null ) */ paintSplayXVdash.setAlpha( mAlpha );  // blue dash splay
    /* if ( paintSplayXVdot   != null ) */ paintSplayXVdot.setAlpha( mAlpha );  // blue dot splay
  }

  /** guard for doMakePaths()
   */
  static private boolean doneMakePaths = false;

  // /** @return true if paths have been made
  //  */
  // static boolean hasMadePaths() { return doneMakePaths; }

  /** make the splay path effects - protected by doneMakePaths to make paths only once
   */
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

      // TDLog.v("density " + TopoDroidApp.getDisplayDensity() + " " + TopoDroidApp.getDisplayDensityDpi() );

      float dot = 2 * TopoDroidApp.getDisplayDensity();
      float[] x1 = new float[2];
      x1[1] = 2 * dot;
      x1[0] = 6 * dot;
      DashPathEffect dash3 = new DashPathEffect( x1, 0 );
      paintSplayXBdash.setPathEffect( dash3 );
      paintSplayXVdash.setPathEffect( dash3 );

      float[] x2 = new float[2];
      x2[1] = 2 * dot;
      x2[0] = dot;
      DashPathEffect dash4 = new DashPathEffect( x2, 0 );
      paintSplayXBdot.setPathEffect( dash4 );
      paintSplayXVdot.setPathEffect( dash4 );

      doneMakePaths = true;
    }
    setSplayAlpha( TDSetting.mSplayAlpha );
    setStrokeWidths();
    setTextSizes();
  }

  /** set the width of certain paints according to the relevant settings
   */
  public static void setStrokeWidths()
  {
    /* if (fixedShotPaint != null)    */ fixedShotPaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    /* if (fixedBluePaint != null)    */ fixedBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    /* if (lightBluePaint != null)    */ lightBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    /* if (darkBluePaint != null)     */ darkBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    /* if (deepBluePaint != null)     */ deepBluePaint.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    /* if (paintSplayXB != null)      */ paintSplayXB.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
    /* if (paintSplayXViewed != null) */ paintSplayXViewed.setStrokeWidth( WIDTH_FIXED * TDSetting.mFixedThickness );
  }

  /** set the text size of certain paints according to the relevant settings
   */
  public static void setTextSizes()
  {
    if ( labelPaint != null )               labelPaint.setTextSize( TDSetting.mLabelSize );
    if ( fixedStationPaint != null )        fixedStationPaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationSavedPaint != null )   fixedStationSavedPaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationActivePaint != null )  fixedStationActivePaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationBarrierPaint != null ) fixedStationBarrierPaint.setTextSize( TDSetting.mStationSize );
    if ( fixedStationHiddenPaint != null )  fixedStationHiddenPaint.setTextSize( TDSetting.mStationSize );
    /* if ( duplicateStationPaint != null )    */ duplicateStationPaint.setTextSize( TDSetting.mStationSize );
    /* if ( referencePaint != null )           */ referencePaint.setTextSize( TDSetting.mStationSize );
  }

  /** prepare the symbols palette
   */
  static SymbolsPalette preparePalette()
  {
    SymbolsPalette palette = new SymbolsPalette();
    // populate local palette with default symbols
    palette.addPointFilename( SymbolLibrary.USER ); // make sure local palette contains "user" symbols
    palette.addLineFilename( SymbolLibrary.USER );
    palette.addAreaFilename( SymbolLibrary.USER );
    SymbolPointLibrary points = mPointLib;
    if ( points != null ) {
      for ( Symbol p : points.getSymbols() ) if ( p.isEnabled() ) {
        String fname = p.getThName();
        if ( fname == null ) {
          TDLog.e("point symbol with null ThName" );
          p.setEnabled( false ); // disable
        } else {
          if ( !  SymbolLibrary.USER.equals(fname) ) palette.addPointFilename( fname );
        }
      }
    }
    SymbolLineLibrary lines = mLineLib;
    if ( lines != null ) {
      for ( Symbol p : lines.getSymbols() ) if ( p.isEnabled() ) {
        String fname = p.getThName();
        if ( fname == null ) {
          TDLog.e("line symbol with null ThName" );
          p.setEnabled( false ); // disable
        } else {
          if ( ! SymbolLibrary.USER.equals(fname) ) palette.addLineFilename( fname );
        }
      }
    }
    SymbolAreaLibrary areas = mAreaLib;
    if ( areas != null ) {
      for ( Symbol p : areas.getSymbols() ) if ( p.isEnabled() ) {
        String fname = p.getThName();
        if ( fname == null ) {
          TDLog.e("area symbol with null ThName" );
          p.setEnabled( false ); // disable
        } else {
          if ( ! SymbolLibrary.USER.equals(fname) ) palette.addAreaFilename( fname );
        }
      }
    }
    return palette;
  }

  /** serialize the symbol libraries
   * @param dos   output stream
   */
  static void toDataStream( DataOutputStream dos )
  {
    if ( mPointLib != null ) { mPointLib.toDataStream(dos); } else { try { dos.writeUTF(""); } catch (IOException e) { TDLog.e("IO " + e.getMessage() ); } }
    if ( mLineLib  != null ) { mLineLib.toDataStream(dos);  } else { try { dos.writeUTF(""); } catch (IOException e) { TDLog.e("IO " + e.getMessage() ); } }
    if ( mAreaLib  != null ) { mAreaLib.toDataStream(dos);  } else { try { dos.writeUTF(""); } catch (IOException e) { TDLog.e("IO " + e.getMessage() ); } }
  }

  /** @return the xored color
   * @param color input color
   */
  static int xorColor( int color )
  {
    int a = color & 0xff000000; 
    int r = (color >> 16) & 0xff; int r1 = 0xff - r;
    int g = (color >>  8) & 0xff; int g1 = 0xff - g;
    int b = (color      ) & 0xff; int b1 = 0xff - b;
    float m = (( r1 > g1 )? ( ( r1 > b1 )? r1 : b1 ) : ( (g1 > b1 )? g1 : b1 ) ) / 256.0f;
    r = (int)( r * m ) & 0xff;
    g = (int)( g * m ) & 0xff;
    b = (int)( b * m ) & 0xff;
    int ret = a | ( r << 16 ) | ( g << 8 ) | b;
    return ret;
  }
    
}
