/** @file GlModel.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D model
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch; // NO_C3D
import com.topodroid.c3walls.cw.CWConvexHull;
import com.topodroid.c3walls.cw.CWTriangle;
import com.topodroid.c3walls.cw.ConvexHullComputer;
import com.topodroid.c3walls.pcrust.PowercrustComputer;
import com.topodroid.c3walls.pcrust.PCSegment;
import com.topodroid.c3walls.pcrust.PCPolygon;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.RectF;

import android.opengl.Matrix;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class GlModel
{
  // FIXME_INCREMENTAL
  static final int LEG_INCREMENT   = 0; // 16;
  static final int SPLAY_INCREMENT = 0; // 64;
  static final int NAME_INCREMENT  = 0; // 16;

  Context mContext;
  static float   mWidth  = 0;
  static float   mHalfWidth = 0;
  static float   mHeight = 0; // unused

  static boolean showLegsSurface   = true;
  static boolean showLegsDuplicate = true;
  static boolean showLegsCommented = true;

  TglParser mParser = null;
  RectF mSurfaceBounds = null;
  // List< GlSketch > glSketches = null; // NO_C3D

  private double mXmed, mYmed, mZmed; // XYZ openGL
  float grid_size = 1;
  static  double mZMin, mZDelta; // draw params
  private double mZ0Min;        // minimum model altitude (survey frame)

  private double mY0med    = 0; // used in drawing
  private double mDiameter = 0;

  public GlSurface glSurface = null; // surface
  public GlNames glNames   = null; // stations
  public GlPoint glPoint   = null; // GPS point // WITH-GPS
  public GlLines glLegs    = null;
  public GlLines glLegsS   = null; // surface
  public GlLines glLegsD   = null; // duplicate
  public GlLines glLegsC   = null; // commented
  public GlLines glSplays  = null;
  GlLines glGrid    = null;
  GlLines glFrame   = null;
  GlLines glSurfaceLegs = null;
  GlWalls glWalls = null;
  GlLines glPlan    = null; // plan projection
  GlLines glProfile = null;
  GlPath  glPath    = null;

  // -----------------------------------------------------------------
  // WITH-GPS GPS LOCATION
  
  /** set the GPS location - use null vector to clear location
   * @param v   GPS location (E, N, Z)
   */
  synchronized void setLocation( Vector3D v ) 
  {
    if ( glPoint != null ) {
      if ( v != null ) {
        glPoint.setLocation( v, mXmed, mYmed, mZmed );
      } else {
        glPoint.clearLocation();
      }
    }
  }

  // synchronized void clearLocation( ) { if ( glPoint != null ) glPoint.clearLocation(); }

  // --------------------------------------------------------------
  // MAINTENANCE

  /** @return true if the rendered has a data parser
   */
  boolean hasParser() { return mParser != null; }

  /** @return true if the rendered has a DEM surface
   */
  boolean hasSurface() { return /* surfaceMode && */ glSurface != null; }

  // /** rebind textures - empty: unused
  //  */
  // void rebindTextures()
  // {
  //   // TDLog.v("GL MODEL rebind textures");
  //   // if ( glNames != null ) glNames.resetTexture(); // nothing to do
  //   // if ( glSurface != null ) glSurface.rebindBitmap(); // this method is empty - commented out
  // }

  /** unbind rendered textures ( station names and surface bitmap)
   */
  void unbindTextures()
  {
    // TDLog.v("Model unbind textures");
    if ( glNames != null ) glNames.unbindTexture();
    if ( glSurface != null ) glSurface.unbindTexture();
  }

  /** @return the diameter of the model
   */
  double getDiameter() { return mDiameter; } 

  /** @return the center of the model
   */
  Vector3D getCenter() { return ( glNames != null )? glNames.getCenter() : null; }

  /** @return the size of the grid cells
   */
  float getGridCell() { return grid_size; }

  // double getDx0() { return ((glLegs == null)? 0 : glLegs.xmed ); }
  // double getDy0() { return ((glLegs == null)? 0 : glLegs.ymin ); }
  // double getDz0() { return ((glLegs == null)? 0 : glLegs.zmin ); }

  /** completely clear the 3D model
   */
  synchronized void clearAll() 
  {
    glSurfaceLegs = null;
    glSurface = null;
    glGrid    = null;
    glFrame   = null;
    glNames   = null;
    glWalls   = null;
    glPlan    = null;
    glProfile = null;
    glSplays  = null;
    glLegs    = null;
    glLegsS   = null;
    glLegsD   = null;
    glLegsC   = null;
    glPath    = null;
    mParser   = null;
    glPoint   = null; // WITH-GPS
  }

  /** set the width and the height
   * @param w  width
   * @param h  height
   */
  static void setWidthAndHeight( float w, float h ) 
  {
    mHalfWidth = w/2.0f;
    mWidth  = w;
    mHeight = h;
  }

  /** initialize the openGL
   */
  void initGL()
  {
    GlLines.initGL( mContext ); // init GL programs
    GlNames.initGL( mContext );
    GlSurface.initGL( mContext );
    GlWalls.initGL( mContext );
    GlPath.initGL( mContext );
    // GlSketch.initGL( mContext ); // NO_C3D
    GlPoint.initGL( mContext );
  }

  // ----------------------------------------------------------------------------------
  // DISPLAY MODE

  static final int FRAME_NONE = 0;
  static final int FRAME_GRID = 1;
  static final int FRAME_AXES = 2;
  static final int FRAME_MAX  = 3;

  static final int DRAW_NONE  = 0;
  static final int DRAW_LINE  = 1;
  static final int DRAW_POINT = 2;
  static final int DRAW_ALL   = 3; 
  static final int DRAW_MAX   = 3; // splayMode < DRAW_MAX
  // static final int DRAW_WIRE  = 3; 

  static final int PROJ_NONE    = 0;
  static final int PROJ_PLAN    = 1;
  static final int PROJ_PROFILE = 2;

  // -------------------------- DISPLAY MODE
  static boolean surfaceMode = false;     // show_surface;
  static boolean surfaceLegsMode = false; // show_surface_legs;
  static boolean surfaceTexture  = true;  // show_surface texture;
  static boolean wallMode = false;        // shaw_walls
  // static int planviewMode = 0;            // howto_show_planview = 0;
  static int splayMode = DRAW_NONE;       // howto_show_splays;
  static int frameMode = FRAME_GRID;      // howto_show_grid/frame
  static int projMode  = PROJ_NONE;       // howto_show_proj

  static public boolean mStationPoints = false;
  static public boolean mAllSplay  = true;
  static public boolean mGridAbove = false;
  static public int     mGridExtent = 10;

  static public boolean mSplitTriangles = true;
  static public boolean mSplitRandomize = true;
  static public boolean mSplitStretch   = false;
  static public double mSplitRandomizeDelta = 0.1f; // meters
  static public double mSplitStretchDelta   = 0.1f;
  static public double mPowercrustDelta     = 0.1f; // meters

  /** cycle through the modes of the display of the splays
   */
  static void toggleSplays()   { splayMode = (splayMode+1) % DRAW_MAX; }

  // static void togglePlanview() { planviewMode = ( planviewMode + 1 ) % 4; }

  /** toggle the display of the wall-model
   */
  static void toggleWallMode() { wallMode = ! wallMode; }

  /** toggle the display of the DEM surface
   */
  static void toggleSurface() { surfaceMode = ! surfaceMode; }

  // static void toggleSurfaceLegs() { surfaceLegsMode = ! surfaceLegsMode; }

  /** cycle through the modes of the display of the frame
   */
  static void toggleFrameMode()   { frameMode = (frameMode + 1) % FRAME_MAX; }

  /** reset to the default display modes
   */
  static void resetModes()
  {
    splayMode    = DRAW_NONE;
    // planviewMode = 0;
    wallMode     = false;
    frameMode    = FRAME_GRID;
    projMode     = PROJ_NONE;
    surfaceMode  = false;
    surfaceLegsMode   = false;
    surfaceTexture    = true;
    showLegsSurface   = true;
    showLegsDuplicate = true;
    showLegsCommented = true;
    // GlSketch.reloadSymbols( TDPath.PATH_SYMBOL_POINT );
  }

  /** reset the leg/splay color modes
   */
  void resetColorMode()
  {
    if ( glLegs   != null ) glLegs.setColorMode(   GlLines.COLOR_NONE, GlLines.COLOR_MAX );
    if ( glLegsS  != null ) glLegsS.setColorMode(  GlLines.COLOR_NONE, GlLines.COLOR_MAX );
    if ( glLegsD  != null ) glLegsD.setColorMode(  GlLines.COLOR_NONE, GlLines.COLOR_MAX );
    if ( glLegsC  != null ) glLegsC.setColorMode(  GlLines.COLOR_NONE, GlLines.COLOR_MAX );
    if ( glSplays != null ) glSplays.setColorMode( GlLines.COLOR_NONE, GlLines.COLOR_MAX );
    clearStationHighlight();
  }
  
  /** clear the height of the stations
   */
  void clearStationHighlight()
  {
    if ( glNames  != null ) glNames.clearHighlight();
    clearPath( );
  }

  /** cycle through the leg/splay color modes
   */
  synchronized void toggleColorMode() { 
    if ( glLegs != null ) {
      // int max = ( mParser.has_temperature )? GlLines.COLOR_MAX : GlLines.COLOR_MAX - 1; // TEMPERATURE
      // TDLog.v("max color " + max );
      int max = GlLines.COLOR_MAX;

      glLegs.toggleColorMode( max );
      if ( glLegsS  != null ) glLegsS.toggleColorMode( max );
      if ( glLegsD  != null ) glLegsD.toggleColorMode( max );
      if ( glLegsC  != null ) glLegsC.toggleColorMode( max );
      if ( glSplays != null ) glSplays.setColorMode( glLegs.mColorMode, GlLines.COLOR_MAX ); 
    }
  }

  /** @return the leg/splay color mode
   */
  int getColorMode() 
  {
    return ( glLegs != null )? glLegs.mColorMode : GlLines.COLOR_NONE;
  }

  /** zoom fit - empty
   */
  public void zoomOne() { /* TODO */ }

  private boolean modelCreated = false;

  // ---------------------------------------------------------------------------
  /** cstr
   * @param ctx   context
   */
  GlModel ( Context ctx )
  { 
    mContext = ctx;
    // NO_C3D
    // GlSketch.loadSymbols( TDPath.getPointDir() ); 
    // glSketches = Collections.synchronizedList(new ArrayList< GlSketch >());
  }

  void hideOrShow( List< Cave3DSurvey > surveys )
  {
    int max = 0;
    for ( Cave3DSurvey survey : surveys ) if ( survey.number > max ) max = survey.number;
    boolean[] visible = new boolean[ max + 1 ];
    StringBuilder sb = new StringBuilder();
    for ( Cave3DSurvey survey : surveys ) {
      visible[ survey.number ] = survey.visible;
      sb.append( (survey.visible? " V" : " H") );
    }
    // TDLog.v("Model hide of show " + surveys.size() + " max " + max + sb.toString() );

    synchronized( this ) {
      // glLegsS.hideOrShow( visible );
      // glLegsD.hideOrShow( visible );
      // glLegsC.hideOrShow( visible );
      glLegs.hideOrShow( visible );
    }
  }

  void draw( float[] mvp_matrix, float[] mv_matrix, Vector3D light ) 
  { 
    if ( ! modelCreated ) return;

    boolean with_surface = false;
    GL.enableDepth( true );
    if ( surfaceMode ) {
      GlSurface gl_surface = null;
      synchronized( this ) { gl_surface = glSurface; }
      if ( gl_surface != null ) {
        with_surface = true;
        gl_surface.draw( mvp_matrix, mv_matrix, light );
      }
    }

    if ( frameMode == FRAME_GRID ) { 
      GlLines grid = null;
      synchronized( this ) { grid = glGrid; } 
      if ( grid != null ) {
        GL.setLineWidth( 1.0f );
        if ( mGridAbove ) {
          float[] revMatrix = new float[16];
          float[] matrix = new float[16];
          Matrix.setIdentityM( revMatrix, 0 );
          revMatrix[13] = (float)mY0med; // glLegs.getYmed();
          revMatrix[ 5] = -1;
          Matrix.multiplyMM( matrix, 0, mvp_matrix, 0, revMatrix, 0 );
          grid.draw( matrix, DRAW_LINE ); 
        } else {
          grid.draw( mvp_matrix, DRAW_LINE );
        }
      }
    } else if ( frameMode == FRAME_AXES  ) { 
      GlLines frame = null;
      synchronized( this ) { frame = glFrame; } 
      if ( frame != null ) {
        GL.setLineWidth( 2.0f );
        // if ( mGridAbove ) {
        //   float[] revMatrix = new float[16];
        //   float[] matrix = new float[16];
        //   Matrix.setIdentityM( revMatrix, 0 );
        //   revMatrix[14] = (glLegs.getZmax() + glLegs.getZmin())/2;
        //   revMatrix[10] = -1;
        //   Matrix.multiplyMM( matrix, 0, mvp_matrix, 0, revMatrix, 0 );
        //   frame.draw( matrix, DRAW_LINE ); 
        // } else {
          frame.draw( mvp_matrix, DRAW_LINE  );
        // }
      }
    }

    if ( wallMode ) {
      GlWalls walls = null;
      synchronized( this ) { walls = glWalls; }
      if ( walls != null ) { 
        // GL.enableCull( false );
        walls.draw( mvp_matrix, mv_matrix, light );
        // GL.enableCull( true );
      }
    }
    GL.enableDepth( false );

    if ( with_surface ) { // WITH-GPS
      GlPoint point = null;
      synchronized( this ) { point = glPoint; }
      if ( point != null ) point.draw( mvp_matrix );
    }

    if ( surfaceLegsMode ) {
      GlLines surface_legs = null;
      synchronized( this ) { surface_legs = glSurfaceLegs; }
      if ( surface_legs != null ) {
        GL.setLineWidth( 2.0f );
        surface_legs.draw( mvp_matrix, DRAW_ALL );
      }
    }

    GL.setLineWidth( 1.0f );
    if ( projMode == PROJ_PLAN ) {
      GlLines plan = null;
      synchronized( this ) { plan = glPlan; }
      if ( plan != null ) plan.draw( mvp_matrix, DRAW_LINE );
    } else if ( projMode == PROJ_PROFILE ) {
      GlLines profile = null;
      synchronized( this ) { profile = glProfile; }
      if ( profile != null ) profile.draw( mvp_matrix, DRAW_LINE );
    }

    GlLines splays = null;
    synchronized( this ) { splays = glSplays; }
    if ( splays != null ) splays.draw( mvp_matrix, splayMode );

    GlLines legs = null;
    GL.setLineWidth( 2.0f );
    if ( showLegsSurface ) {
      synchronized( this ) { legs = glLegsS; }
      // TDLog.v("Model surface draw " + legs.size() );
      if ( legs   != null ) legs.draw( mvp_matrix, DRAW_LINE, mStationPoints, TglColor.ColorLegS );
    }
    if ( showLegsDuplicate ) {
      synchronized( this ) { legs = glLegsD; }
      if ( legs   != null ) legs.draw( mvp_matrix, DRAW_LINE, mStationPoints, TglColor.ColorLegD );
    }
    if ( showLegsCommented ) {
      synchronized( this ) { legs = glLegsC; }
      if ( legs   != null ) legs.draw( mvp_matrix, DRAW_LINE, mStationPoints, TglColor.ColorLegC );
    }
    synchronized( this ) { legs = glLegs; }
    if ( legs   != null ) legs.draw( mvp_matrix, DRAW_LINE, mStationPoints, TglColor.ColorLeg );

    GlNames names = null;
    synchronized( this ) { names = glNames; }
    if ( names  != null ) names.draw( mvp_matrix );

    GlPath gl_path = null;
    synchronized( this ) { gl_path = glPath; }
    if ( gl_path != null ) {
      GL.setLineWidth( 4.0f );
      gl_path.draw( mvp_matrix );
    }
    // if ( glPath != null ) {
    //   GL.setLineWidth( 3.0f );
    //   glPath.draw( mvp_matrix );
    // }

    // synchronized( glSketches ) { // NO_C3D
    //   GlSketch gl_sketch;
    //   for ( GlSketch sketch : glSketches ) {
    //     synchronized( this ) { gl_sketch = sketch; }
    //     gl_sketch.draw( mvp_matrix );
    //   }
    // }

  }

  /** check if there is a station name at point (x,y) of the display
   * @param x    X coord
   * @param y    Y coord
   * @param mvpMatrix MVP matrix
   * @param dmin      maximum distance
   * @param highlight whether to highlight the station
   * @return the station name or null
   */
  String checkNames( float x, float y, float[] mvpMatrix, float dmin, boolean highlight ) 
  { 
    // TDLog.v("Model check names at " + x + " " + y + " highlight " + highlight + " ret " + ret );
    return ( glNames != null )? glNames.checkName( x, y, mvpMatrix, dmin, highlight ) : null;
  }

  // String checkNames( float[] zn, float[] zf, float dmin, boolean highlight ) 
  // { 
  //   return ( glNames != null )? glNames.checkName( zn, zf, dmin, highlight ) : null;
  // }

  Cave3DShot checkLines( float x, float y, float[] mvpMatrix, float dmin )
  {
    return glLegs.checkLines( x, y, mvpMatrix, dmin );
  }

  // ----------------------------------------------------------------------
  synchronized void clearWalls( ) 
  { 
    glWalls   = null; 
    glPlan    = null;
    glProfile = null;
  }

  void clearPath()
  {
    // TDLog.v("Model path clear");
    synchronized( this ) { glPath = null; }
  }

  boolean setPath( ArrayList< Cave3DStation > path, boolean is_bluetooth )
  {
    boolean ret = false;
    // GlPath gl_path_old = glPath;
    if ( path != null && path.size() > 1 ) {
      // TDLog.v("Model path make: size " + path.size() );
      GlPath gl_path = new GlPath( mContext, TglColor.ColorStation );
	  
      // FIXME INCREMENTAL
      // if ( is_bluetooth ) {
      //   for ( Cave3DStation station : path ) {
      //     gl_path.addBluetoothVertex( station );
      //   }
      // } else {
        for ( Cave3DStation station : path ) {
          gl_path.addVertex( station, mXmed, mYmed, mZmed );
        }
      // }
	  
      // TDLog.v("MODEL path init data");
      gl_path.initData();
      // gl_path.logMinMax();
      synchronized( this ) { glPath = gl_path; }
      ret = true;
    } else {
      // TDLog.v("clear path");
      synchronized( this ) { glPath = null; }
    }
    // if ( gl_path_old != null ) gl_path_old.releaseBuffer(); // FIXME does not do anything
    // if ( glPath != null ) glPath.logMinMax();
    return ret;
  }

  /** prepare the ground X-Y grid and the frame
   * @param legs        ???
   * @param grid_size   grid cell size
   * @param delta       additional border around the model area
   * @note legs must have already been reduced ( bbox must be symmetric )
   */
  void prepareGridAndFrame( GlLines legs, float grid_size, float delta )
  {
    // TDLog.v("Model BBox " + legs.getBBoxString() );

    float xmin = (float)legs.getXmin() - delta;
    float xmax = (float)legs.getXmax() + delta;
    float zmin = (float)legs.getZmin() - delta;
    float zmax = (float)legs.getZmax() + delta;
    
    makeGrid(  xmin, xmax, zmin, zmax, (float)legs.getYmin(), (float)legs.getYmax(), grid_size );
    makeFrame( xmin, xmax, zmin, zmax, (float)legs.getYmin(), (float)legs.getYmax() );
  }

  /** prepare the walls triangles using the Convex-Hull model
   * @param computer   convex-hull computer
   * @param make       ???
   */
  void prepareWalls( ConvexHullComputer computer, boolean make )
  {
    if ( ! make ) {
      clearWalls();
      return;
    }
    if ( computer == null ) return;
    GlWalls walls = new GlWalls( mContext, GlWalls.WALL_FACE );
    for ( CWConvexHull cw : computer.getWalls() ) {
      for ( CWTriangle tr : cw.mFace ) {
        Vector3D v1 = new Vector3D( tr.v1.x - mXmed, tr.v1.z - mYmed, -tr.v1.y - mZmed );
        Vector3D v2 = new Vector3D( tr.v2.x - mXmed, tr.v2.z - mYmed, -tr.v2.y - mZmed );
        Vector3D v3 = new Vector3D( tr.v3.x - mXmed, tr.v3.z - mYmed, -tr.v3.y - mZmed );
        walls.addTriangle( v1, v2, v3 );
      }
    }
    // TDLog.v("MODEL walls (1) init data");
    walls.initData();
    synchronized( this ) { glWalls = walls; }
    // TDLog.v("Model CW-Hull triangles " + walls.triangleCount );
  }

  /** prepare the walls triangles using the powercrust model
   * @param computer   powercrust computer
   * @param make       ???
   */
  void prepareWalls( PowercrustComputer computer, boolean make )
  {
    if ( ! make ) {
      clearWalls();
      return;
    }
    if ( computer == null ) return;
    GlWalls walls = new GlWalls( mContext, GlWalls.WALL_FACE );
    for ( Triangle3D tr : computer.getTriangles() ) {
      // Vector3D v1 = new Vector3D( tr.vertex[0].x - mXmed, tr.vertex[0].z - mYmed, -tr.vertex[0].y - mZmed );
      // Vector3D v2 = new Vector3D( tr.vertex[1].x - mXmed, tr.vertex[1].z - mYmed, -tr.vertex[1].y - mZmed );
      // Vector3D v3 = new Vector3D( tr.vertex[2].x - mXmed, tr.vertex[2].z - mYmed, -tr.vertex[2].y - mZmed );
      // walls.addTriangle( v1, v2, v3 );
      walls.addTriangle( tr, mXmed, mYmed, mZmed );
    }
    // TDLog.v("MODEL walls (2) init data");
    walls.initData();
    synchronized( this ) { glWalls = walls; }
    // TDLog.v("Model powercrust triangles " + walls.triangleCount );
    preparePlanAndProfile( computer );
  }

  /** prepare the walls triangles using the wall model
   * @param computer   wall computer
   * @param make       ???
   */
  void prepareWalls( WallComputer computer, boolean make )
  {
    if ( ! make ) {
      clearWalls();
      return;
    }
    if ( computer == null ) return;
    GlWalls walls = new GlWalls( mContext, GlWalls.WALL_FACE );
    
    for ( Triangle3D tr : computer.getTriangles() ) {
      // tr.dump();
      // Vector3D v1 = new Vector3D( tr.vertex[0].x - mXmed, tr.vertex[0].z - mYmed, -tr.vertex[0].y - mZmed );
      // Vector3D v2 = new Vector3D( tr.vertex[1].x - mXmed, tr.vertex[1].z - mYmed, -tr.vertex[1].y - mZmed );
      // Vector3D v3 = new Vector3D( tr.vertex[2].x - mXmed, tr.vertex[2].z - mYmed, -tr.vertex[2].y - mZmed );
      // walls.addTriangle( v1, v2, v3 );
      walls.addTriangle( tr, mXmed, mYmed, mZmed );
    }
    // TDLog.v("MODEL walls (3) init data");
    walls.initData();
    synchronized( this ) { glWalls = walls; }
    // TDLog.v("powercrust triangles " + walls.triangleCount );
  }

  /** prepare the projected plan and profile using the powercrust model
   * @param computer   powercrust computer
   */
  private void preparePlanAndProfile( PowercrustComputer computer )
  {
    if ( computer.hasPlanview() ) {
      // FIXME INCREMENTAL : , 0 );
      GlLines plan = new GlLines( mContext, TglColor.ColorPlan, 0 );
      for ( PCPolygon poly : computer.getPlanview() ) {
        int nn = poly.size();
        if ( nn > 2 ) {
          // leave polygon border open, otherwise there can be long lines that are artifacts
          Vector3D p1 = new Vector3D( poly.get( 0 ) );
          p1.z = mZ0Min;
          for ( int k = 1; k < nn; ++k ) {
            Vector3D p2 = new Vector3D( poly.get( k ) );
            p2.z = mZ0Min;
            plan.addLine( p1, p2, 4, -1, false, mXmed, mYmed, mZmed, null );
            p1 = p2;
          }
        }
      }
      // TDLog.v("MODEL plan init data");
      plan.initData();
      synchronized( this ) { glPlan = plan; }
    }
    if ( computer.hasProfilearcs() ) {
      // FIXME INCREMENTAL : , 0 );
      GlLines profile = new GlLines( mContext, TglColor.ColorPlan, 0 );
      for ( PCSegment sgm : computer.getProfilearcs() ) {
        profile.addLine( sgm.getV1(), sgm.getV2(), 4, -1, false, mXmed, mYmed, mZmed, null );
      }
      // TDLog.v("MODEL profile init data");
      profile.initData();
      synchronized( this ) { glProfile = profile; }
    }
  }

  // TEMPERATURE
  // void prepareTemperatures()
  // {
  //   double inv_temp_zmax = prepareStationTemperatureDepth( mParser );
  //   // TDLog.v("Model prepare temperatures - inv zmax " + inv_temp_zmax );
  //   glLegs.prepareTemperatureBuffer(  legsSurvey );
  //   glLegsS.prepareTemperatureBuffer( legsSurface );
  //   glLegsD.prepareTemperatureBuffer( legsDuplicate );
  //   glLegsC.prepareTemperatureBuffer( legsCommented );
  // }

  // called by GlRenderer notifyDEM
  void prepareDEM( ParserDEM dem ) 
  {
    if ( dem == null ) return;
    mSurfaceBounds = dem.getBounds();
    // TDLog.v("Model prepare DEM");
    GlSurface surface = new GlSurface( mContext );
    // TDLog.v("MODEL surface DEM  init data");
    surface.initData( dem, mXmed, mYmed, mZmed );
    synchronized( this ) { glSurface = surface; }

    // glLegs.prepareDepthBuffer( mParser.getShots(), dem );
    double inv_surface_zmax = prepareStationSurfaceDepth( mParser, dem );
    glLegs.prepareDepthBuffer(  legsSurvey );
    glLegsS.prepareDepthBuffer( legsSurface );
    glLegsD.prepareDepthBuffer( legsDuplicate );
    glLegsC.prepareDepthBuffer( legsCommented );

    prepareSurfaceLegs( mParser, dem );
  }
  
  private void prepareSurfaceLegs( TglParser parser, DEMsurface surface )
  {
    if ( parser == null || surface == null ) return;
    // TDLog.v("Model prepare surface legs. Shots " + parser.getShots().size() );
    // FIXME INCREMENTAL : , 0 );
    GlLines surface_legs = new GlLines( mContext, TglColor.ColorSurfaceLeg, 0 );
    for ( Cave3DShot leg : parser.getShots() ) {
      if ( leg.from_station == null || leg.to_station == null ) continue; // skip fake-legs
      Vector3D v1 = new Vector3D( leg.from_station );
      Vector3D v2 = new Vector3D( leg.to_station );
      v1.z = surface.computeZ( v1.x, v1.y );
      v2.z = surface.computeZ( v2.x, v2.y );
      surface_legs.addLine( v1, v2, 3, leg.getSurveyNr(), false, mXmed, mYmed, mZmed, null ); // 3: color_index, false: fixed colors
    }
    surface_legs.computeBBox();
    // surface_legs.logMinMax();
    // TDLog.v("MODEL surface legs init data");
    surface_legs.initData();
    synchronized( this ) { glSurfaceLegs = surface_legs; }

    GlPoint point = new GlPoint( mContext ); // WITH-GPS
    synchronized( this ) { glPoint = point; }
  }

  /** prepare the surface texture: set the texture to the surface
   * @param texture   surface texture bitmap
   */
  void prepareTexture( Bitmap texture )
  {
    if ( texture != null && glSurface != null ) {
      glSurface.setTextureBitmap( texture );
    }
  }

  // ------- NO_C3D ---------------------------------------------------------
  // /** ???
  //  * @param sketch_parser sketch parser
  //  */
  // void prepareSketch( ParserSketch sketch_parser )
  // {
  //   // TDLog.v("Model prepare sketch " + sketch_parser.mName );
  //   dropSketch( sketch_parser.mName );
  //   GlSketch gl_sketch = new GlSketch( mContext, sketch_parser.mName, sketch_parser.mType, sketch_parser.mPoints, sketch_parser.mLines, sketch_parser.mAreas );
  //   // gl_sketch.logMinMax();
  //   // TDLog.v("MODEL sketch init data");
  //   gl_sketch.initData( mXmed, mYmed, mZmed, sketch_parser.xoff, sketch_parser.yoff, sketch_parser.zoff );
  //   addSketch( gl_sketch );
  // }

  // /** drop a sketch
  //  * @param name  sketch name
  //  */
  // private void dropSketch( String name )
  // {
  //   synchronized( glSketches ) {
  //     for ( int k = 0; k < glSketches.size(); ++k ) {
  //       GlSketch sketch = glSketches.get( k );
  //       if ( sketch.mName.equals( name ) ) {
  //         glSketches.remove( k );
  //         break;
  //       }
  //     }
  //   }
  // }

  // /** add a sketch
  //  * @param sketch  GL sketch
  //  */
  // private void addSketch( GlSketch sketch ) 
  // {
  //   if ( sketch == null || sketch.mName == null ) return;
  //   synchronized( glSketches ) {
  //     glSketches.add( sketch );
  //   }
  // }

  // List< GlSketch > getSketches() 
  // {
  //   List< GlSketch > sketches = new ArrayList< GlSketch >();
  //   if ( glSketches == null ) return sketches;
  //   synchronized( glSketches ) {
  //     sketches.addAll( glSketches ); // for ( GlSketch sketch : glSketches ) sketches.add( sketch );
  //   }
  //   return sketches;
  // }

  // void updateSketches() 
  // {
  //   if ( glSketches == null ) return;
  //   List< GlSketch > sketches = Collections.synchronizedList(new ArrayList< GlSketch >());
  //   synchronized( glSketches ) {
  //     for ( GlSketch sketch : glSketches ) {
  //       if ( ! sketch.mDelete ) sketches.add( sketch );
  //     }
  //   }
  //   synchronized( this ) { glSketches = sketches; }
  // }

  // ----------------------------------------------------------------------
  /** prepare the X-Y grid
   * @param x1  left X (west)
   * @param x2  right X (east)
   * @param z1  top Y (north) ?
   * @param z2  bottom Y (south) ?
   * @param y1  lower Z (ground)
   * @param y2  upper Z (only for vertical axis line)
   * @param step  grid cell size
   */
  private void makeGrid( float x1, float x2, float z1, float z2, float y1, float y2, float step )
  {
    // step = 2 * step;
    int nx = 1 + (int)((x2-x1)/step);
    int nz = 1 + (int)((z2-z1)/step);
    // TDLog.v("Model Grid NX " + nx + " NY " + nz + " cell " + step + " X0 " + x1 + " Y0 " + y1 + " Z0 " + z1 );
    int count = nx + nz + 1;
    float[] data = new float[ count*2 * 7 ];
    int k = 0;
    float x0 = x1;
    for ( int i=0; i<nx; ++i ) {
      data[ k++ ] = x0;
      data[ k++ ] = y1;
      data[ k++ ] = z1;
      data[ k++ ] = 0; data[ k++ ] = 0.3f; data[ k++ ] = 1.0f; data[ k++ ] = 1.0f;
      data[ k++ ] = x0;
      data[ k++ ] = y1;
      data[ k++ ] = z2;
      data[ k++ ] = 0.8f; data[ k++ ] = 0.2f; data[ k++ ] = 1.0f; data[ k++ ] = 1.0f;
      x0 += step;
    }

    float z0 = z1;
    for ( int i=0; i<nz; ++i ) {
      data[ k++ ] = x1;
      data[ k++ ] = y1;
      data[ k++ ] = z0;
      data[ k++ ] = 0.8f; data[ k++ ] = 0.7f; data[ k++ ] = 0.2f; data[ k++ ] = 1.0f;
      data[ k++ ] = x2;
      data[ k++ ] = y1;
      data[ k++ ] = z0;
      data[ k++ ] = 0f; data[ k++ ] = 0.8f; data[ k++ ] = 0f; data[ k++ ] = 1.0f;
      z0 += step;
    }

    data[ k++ ] = x1;
    data[ k++ ] = y1;
    data[ k++ ] = z2;
    data[ k++ ] = 1; data[ k++ ] = 0; data[ k++ ] = 0; data[ k++ ] = 1.0f;
    data[ k++ ] = x1;
    data[ k++ ] = y2;
    data[ k++ ] = z2;
    data[ k++ ] = 1; data[ k++ ] = 0; data[ k++ ] = 0; data[ k++ ] = 1.0f;
    
    // FIXME INCREMENTAL : , 0 );
    glGrid = new GlLines( mContext, GlLines.COLOR_SURVEY, 0 );
    glGrid.setAlpha( 0.5f );
    // TDLog.v("MODEL grid init data");
    glGrid.initData( data, count ); // , R.raw.line_acolor_vertex, R.raw.line_fragment );
  }
      
  private void makeFrame( float x1, float x2, float z1, float z2, float y1, float y2 )
  { 
    float[] data = new float[ 3*2 * 7 ];
    int k = 0;
    // line y1-y2
    data[ k++ ] = x1;
    data[ k++ ] = y1;
    data[ k++ ] = z2;
    data[ k++ ] = 1; data[ k++ ] = 0; data[ k++ ] = 0; data[ k++ ] = 1.0f;
    data[ k++ ] = x1;
    data[ k++ ] = y2;
    data[ k++ ] = z2;
    data[ k++ ] = 1; data[ k++ ] = 0; data[ k++ ] = 0; data[ k++ ] = 1.0f;

    // line x1-x2
    data[ k++ ] = x1;
    data[ k++ ] = y1;
    data[ k++ ] = z2;
    data[ k++ ] = 0; data[ k++ ] = 0.7f; data[ k++ ] = 0; data[ k++ ] = 1.0f;
    data[ k++ ] = x2;
    data[ k++ ] = y1;
    data[ k++ ] = z2;
    data[ k++ ] = 0; data[ k++ ] = 0.7f; data[ k++ ] = 0; data[ k++ ] = 1.0f;

    // line z1-z2
    data[ k++ ] = x1;
    data[ k++ ] = y1;
    data[ k++ ] = z1;
    data[ k++ ] = 0; data[ k++ ] = 0; data[ k++ ] = 1; data[ k++ ] = 1.0f;
    data[ k++ ] = x1;
    data[ k++ ] = y1;
    data[ k++ ] = z2;
    data[ k++ ] = 0; data[ k++ ] = 0; data[ k++ ] = 1; data[ k++ ] = 1.0f;
    
    // FIXME INCREMENTAL : , 0 );
    glFrame = new GlLines( mContext, GlLines.COLOR_SURVEY, 0);
    glFrame.setAlpha( 0.9f );
    // TDLog.v("MODEL frame init data");
    glFrame.initData( data, 3 ); // , R.raw.line_acolor_vertex, R.raw.line_fragment );
  }

  ArrayList< Cave3DShot > legsSurvey;
  ArrayList< Cave3DShot > legsSurface;
  ArrayList< Cave3DShot > legsDuplicate;
  ArrayList< Cave3DShot > legsCommented;

  /** create the 3D model
   * @param parser  parser with the 3D data
   * @param reduce  ???
   */
  void prepareModel( TglParser parser, boolean reduce )
  {
    // TDLog.v("Model prepare full");
    modelCreated = false;
    if ( parser == null || parser.getShotNumber() == 0 ) {
      TDLog.Error("Model Error. Cannot create model without shots");
      return;
    }
    mParser = parser;
    mZ0Min  = parser.getCaveZMin();
	
    // FIXME INCREMENTAL
    GlLines legs   = new GlLines( mContext, GlLines.COLOR_NONE, 0 );
    GlLines legsS  = new GlLines( mContext, GlLines.COLOR_NONE, 0 );
    GlLines legsD  = new GlLines( mContext, GlLines.COLOR_NONE, 0 );
    GlLines legsC  = new GlLines( mContext, GlLines.COLOR_NONE, 0 );
    GlLines splays = new GlLines( mContext, GlLines.COLOR_NONE, 0 );
    GlNames names  = new GlNames( mContext, NAME_INCREMENT );
 
    legsSurvey    = new ArrayList< Cave3DShot >();
    legsSurface   = new ArrayList< Cave3DShot >();
    legsDuplicate = new ArrayList< Cave3DShot >();
    legsCommented = new ArrayList< Cave3DShot >();

    // TDLog.v("3D-Model create. surveys " + parser.getSurveyNumber() + " shots " + parser.getShotNumber() + "/" + parser.getSplayNumber() + " stations " + parser.getStationNumber() );

    for ( Cave3DShot leg : parser.getShots() ) {
      if ( leg.from_station == null || leg.to_station == null ) continue; // skip fake-legs
      int survey_nr = leg.getSurveyNr();
      int color = mParser.getSurveyFromIndex( survey_nr ).getColor();
      if ( leg.isSurvey() ) {
        legsSurvey.add( leg );
        legs.addLine(  leg.from_station, leg.to_station, color, survey_nr, true, leg ); 
      } else if ( leg.isSurface() ) {
        legsSurface.add( leg );
        legsS.addLine( leg.from_station, leg.to_station, color, survey_nr, true, null );
      } else if ( leg.isDuplicate() ) {
        legsDuplicate.add( leg );
        legsD.addLine( leg.from_station, leg.to_station, color, survey_nr, true, null );
      } else if ( leg.isCommented() ) {
        legsCommented.add( leg );
        legsC.addLine( leg.from_station, leg.to_station, color, survey_nr, true, null );
      } else {
        legsSurvey.add( leg );
        legs.addLine(  leg.from_station, leg.to_station, color, survey_nr, true, leg ); 
      }
    }
    // legs.logMinMax();
    mXmed = (legs.getXmin() + legs.getXmax())/2;
    mYmed = (legs.getYmin() + legs.getYmax())/2;
    mZmed = (legs.getZmin() + legs.getZmax())/2;

    if ( ! reduce ) {
      mXmed = mYmed = mZmed = 0;
    } else {
      legs.reduceData( mXmed, mYmed, mZmed );
      legsS.reduceData( mXmed, mYmed, mZmed );
      legsD.reduceData( mXmed, mYmed, mZmed );
      legsC.reduceData( mXmed, mYmed, mZmed );
    }

    // TDLog.v("Model center " + mXmed + " " + mYmed + " " + mZmed );
    // legs.logMinMax();
    for ( Cave3DShot splay : parser.getSplays() ) {
      int survey_nr = splay.getSurveyNr();
      int color = mParser.getSurveyFromIndex( survey_nr ).getColor();
      if ( splay.from_station != null ) {
        splays.addLine( splay.from_station, splay.toPoint3D(), color, survey_nr, true, mXmed, mYmed, mZmed, null );
      } else if ( splay.to_station != null ) {
        splays.addLine( splay.to_station, splay.toPoint3D(), color, survey_nr, true, mXmed, mYmed, mZmed, null );
      }
    }
    splays.computeBBox();
    mZMin = legs.getYmin();
    double mZMax = legs.getYmax();
    // TDLog.v("Model med " + mXmed + " " + mYmed + " " + mZmed + " Z " + mZMin + " " + mZMax );
    if ( mZMin > splays.getYmin() ) mZMin = splays.getYmin();
    if ( mZMax < splays.getYmax() ) mZMax = splays.getYmax();
    mZDelta = mZMax - mZMin;
    // TDLog.v("Model after reduce Z " + mZMin + " " + mZMax + " DZ " + mZDelta );
    // splays.logMinMax();

    for ( Cave3DStation st : parser.getStations() ) {
      String name = st.getShortName();
      if ( name != null && name.length() > 0 && ( ! name.equals("-") ) && ( ! name.equals(".") ) ) {
        // TDLog.v("Model name add " + st.short_name + " " + st.name );
        names.addName( st, st.getShortName(), st.getFullName(), mXmed, mYmed, mZmed );
      }
    }
    // names.logMinMax();

    boolean valid_surface = false;
    DEMsurface surface = parser.getSurface();
    if ( surface != null ) {
      mSurfaceBounds = surface.getBounds();
      // TDLog.v("Model parser has surface");
      GlSurface gl_surface = new GlSurface( mContext );
      // TDLog.v("MODEL surface init data");
      valid_surface = gl_surface.initData( surface, mXmed, mYmed, mZmed, parser.surfaceFlipped() );
      if ( valid_surface ) {
        if ( parser != null && parser.mBitmap != null ) {
          Bitmap texture = parser.mBitmap.getBitmap( surface.mEast1, surface.mNorth1, surface.mEast2, surface.mNorth2 );
          if ( texture != null ) {
            gl_surface.setTextureBitmap( texture );
          }
        }
        synchronized( this ) { glSurface = gl_surface; }
        prepareSurfaceLegs( parser, surface );
      }
    }

    // TDLog.v("MODEL leg surface init data");
    legsS.initData( );
    // TDLog.v("MODEL leg duplicate init data");
    legsD.initData( );
    // TDLog.v("MODEL leg commented init data");
    legsC.initData( );
    // TDLog.v("MODEL legs init data");
    legs.initData( );
    // TDLog.v("MODEL splays init data");
    splays.initData( );
    // TDLog.v("MODEL names init data");
    names.initData( );
    float grid_size = (float)parser.getGridSize();
    prepareGridAndFrame( legs, grid_size, mGridExtent*grid_size );

    if ( valid_surface /* && surface != null */ ) {
      double surface_zmax = prepareStationSurfaceDepth( parser, surface );
      legs.prepareDepthBuffer(  legsSurvey );
      legsS.prepareDepthBuffer( legsSurface );
      legsD.prepareDepthBuffer( legsDuplicate );
      legsC.prepareDepthBuffer( legsCommented );
    }

    mDiameter = legs.diameter();
    mY0med    = legs.getYmed();

    // legs.logMinMax();
    // splays.logMinMax();

    synchronized( this ) {
      glLegs   = legs;
      glLegsS  = legsS;
      glLegsD  = legsD;
      glLegsC  = legsC;
      glSplays = splays;
      glNames  = names;
    }
    // FIXME INCREMENTAL
    glSplays.setDebug( true );
  }

  // BLUETOOTH
  // void addBluetoothStation( Cave3DStation st )
  // {
  //   if ( st == null ) return;
  //   String name = st.short_name;
  //   // TDLog.v("Model add BT station " + st.short_name + " " + st.name );
  //   synchronized( glNames ) {
  //     glNames.addBluetoothName( st, st.short_name, st.name ); // mXmed, mYmed, mZmed );
  //     glNames.initData();
  //   }
  // }

  // void addBluetoothLeg( Cave3DShot leg ) 
  // {
  //   // TDLog.v("Model add BT leg");
  //   synchronized( glLegs ) {
  //     legsSurvey.add( leg );
  //     int survey_nr = leg.getSurveyNr(); // leg getSurveyNr() = color-index
  //     glLegs.addLine( leg.from_station, leg.to_station, survey_nr, survey_nr, true, leg ); 
  //     glLegs.initData( );
  //   }
  // }

  // // BT splays have a TO-station (with name "-")
  // void addBluetoothSplay( Cave3DShot splay ) 
  // {
  //   // TDLog.v("Model add BT splay");
  //   synchronized( glSplays ) {
  //     int survey_nr = splay.getSurveyNr(); // splay getSurveyNr() = color-index
  //     glSplays.addLine( splay.from_station, splay.to_station, survey_nr, survey_nr, true, null );
  //     glSplays.initData( );
  //   }
  // }

  // void prepareEmptyModel( TglParser parser )
  // {
  //   // TDLog.v("GL model prepare empty" );
  //   modelCreated = false;
  //   mParser = parser;
  //   mZ0Min  = 0;
  //   GlLines legs   = new GlLines( mContext, GlLines.COLOR_NONE, LEG_INCREMENT );
  //   GlLines legsS  = new GlLines( mContext, GlLines.COLOR_NONE, 0 );
  //   GlLines legsD  = new GlLines( mContext, GlLines.COLOR_NONE, 0 );
  //   GlLines legsC  = new GlLines( mContext, GlLines.COLOR_NONE, 0 );
  //   GlLines splays = new GlLines( mContext, GlLines.COLOR_NONE, SPLAY_INCREMENT );
  //   GlNames names  = new GlNames( mContext, NAME_INCREMENT );

  //   legsSurvey    = new ArrayList< Cave3DShot >();
  //   legsSurface   = new ArrayList< Cave3DShot >();
  //   legsDuplicate = new ArrayList< Cave3DShot >();
  //   legsCommented = new ArrayList< Cave3DShot >();

  //   // TDLog.v("GL Model create. shots " + parser.getShotNumber() + "/" + parser.getSplayNumber() + " stations " + parser.getStationNumber() );
  //   legs.initEmptyBBox();
  //   mXmed = (legs.getXmin() + legs.getXmax())/2;
  //   mYmed = (legs.getYmin() + legs.getYmax())/2;
  //   mZmed = (legs.getZmin() + legs.getZmax())/2;

  //   // TDLog.v("Model center " + mXmed + " " + mYmed + " " + mZmed );
  //   // legs.logMinMax();
  //   
  //   mZMin = legs.getYmin();
  //   double mZMax = legs.getYmax();
  //   if ( mZMin > splays.getYmin() ) mZMin = splays.getYmin();
  //   if ( mZMax < splays.getYmax() ) mZMax = splays.getYmax();
  //   mZDelta = mZMax - mZMin;

  //   boolean valid_surface = false;

  //   legsS.initData( );
  //   legsD.initData( );
  //   legsC.initData( );
  //   legs.initData( );
  //   splays.initData( );
  //   names.initData( );
  //   float grid_size = 1.0f;
  //   prepareGridAndFrame( legs, grid_size, 10*mGridExtent*grid_size );

  //   mDiameter = 100; // legs.diameter();
  //   mY0med    = 0; // legs.getYmed();

  //   // legs.logMinMax();
  //   // splays.logMinMax();

  //   synchronized( this ) {
  //     glLegs   = legs;
  //     glLegsS  = legsS;
  //     glLegsD  = legsD;
  //     glLegsC  = legsC;
  //     glSplays = splays;
  //     glNames  = names;
  //   }
  //   // glSplays.setDebug( true );
  //   // if ( parser instanceof ParserBluetooth ) {
  //   //   addBluetoothStation( ((ParserBluetooth)parser).getLastStation() );
  //   // } else {
  //   //   addBluetoothStation( new Cave3DStation( "0", 0, 0, 0 ) );
  //   // }
  // }
  // // ENDS INCREMENTAL

  // return inverse of max surface depth
  private double prepareStationSurfaceDepth( TglParser parser, DEMsurface surface )
  {
    if ( surface == null ) return 1.0f;
    double zmax = 0;
    for ( Cave3DStation st : parser.getStations() ) {
      st.surface_depth = surface.computeZ( st.x, st.y ) - st.z;
      if ( st.surface_depth > zmax ) { zmax = st.surface_depth; }
      else if ( st.surface_depth < 0.0f ) { st.surface_depth = 0.0f; }
    }
    if ( zmax > 0 ) { // always true
      zmax = 1.0 / zmax;
      for ( Cave3DStation st : parser.getStations() ) {
        st.surface_depth *= zmax;
        if ( st.surface_depth > 1.0f ) st.surface_depth = 1.0f;
      }
    }
    // TDLog.v("Model surface inv depth " + zmax + " stations " + parser.getStations().size() );
    return zmax;
  }

  // TEMPERATURE
  // return inverse of max temperature depth
  // private double prepareStationTemperatureDepth( TglParser parser )
  // {
  //   double zmin =  10000;
  //   double zmax = -10000;
  //   for ( Cave3DStation st : parser.getStations() ) {
  //     if ( st.temp < zmin ) zmin = st.temp;
  //     if ( st.temp > zmax ) zmax = st.temp;
  //   }
  //   if ( zmax <= zmin ) return 1.0;
  //   zmax = 1.0 / ( zmax - zmin );
  //   for ( Cave3DStation st : parser.getStations() ) {
  //     st.temp = ( st.temp - zmin ) * zmax;
  //   }
  //   return zmax;
  // }

  void createModel( )
  {
    if ( mParser == null ) return;
    modelCreated = true;
  }
}
