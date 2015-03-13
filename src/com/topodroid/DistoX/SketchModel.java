/** @file SketchModel.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: sketch 3D model
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130216 created
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Locale;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import android.graphics.Matrix;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.PointF;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;

import android.util.FloatMath;

import android.util.Log;

class SketchModel
{
  private static final float mCloseness = TopoDroidSetting.mCloseness;
  DistoXNum mNum;

  Sketch3dInfo mInfo;
  SketchPainter mPainter;
  SketchSurface mCurrentSurface;

  Matrix mMatrix;
  List< NumStation > stations;
  // List< NumShot >    shots;
  // List< NumSplay >   splays;
  List< SketchPath > mPaths;
  private List<SketchFixedPath>   mFixedStack;
  private List<SketchStationName> mStations;
  // private List<SketchSectionSet> mSectionSets;
  List<SketchSurface>  mSurfaces;
  List<SketchSurface>  mJoins;

  ArrayList< SketchRefinement > mRefines;
  ArrayList< Vector > mBorder3d;
  // SketchLinePath mEditLine;
  // ArrayList< PointF > mBorder;

  SketchUndo mUndo;
  SketchUndo mRedo;

  int mDisplayMode = SketchDef.DISPLAY_NGBH;

  public static final int highlightColor = 0xffff9999;
  int cnt;

  private float pi4 = (float)Math.PI/4;
  private float pi2 = pi4 * 2;

  private Selection mSelection;
  private SelectionSet mSelected;


  SketchModel( Sketch3dInfo info, DistoXNum num, SketchPainter painter ) 
  {
    mInfo    = info;
    mPainter = painter;
    mCurrentSurface = null;
    setNum( num );

    mPaths       = Collections.synchronizedList( new ArrayList< SketchPath >() );
    // points      = Collections.synchronizedList( new ArrayList< SketchPointPath >() );
    mFixedStack  = Collections.synchronizedList(new ArrayList<SketchFixedPath>());
    mStations    = Collections.synchronizedList(new ArrayList<SketchStationName>());
    mSurfaces    = Collections.synchronizedList(new ArrayList<SketchSurface>());
    mJoins       = Collections.synchronizedList(new ArrayList<SketchSurface>());
    // mSectionSets = Collections.synchronizedList( new ArrayList<SketchSectionSet>() );

    mMatrix  = new Matrix();
    // mSectionBaseVector = null;
    cnt = 0;

    mUndo = null;
    mRedo = null;

    mSelection = new Selection();
    mSelected  = new SelectionSet();

    mRefines  = null;
    // mEditLine = null;
    // mBorder   = null;
    mBorder3d = null;
  }

  void setRefinement( ArrayList< SketchRefinement > refines ) { mRefines = refines; }
  // void setEditLine( SketchLinePath line ) { mEditLine = line; }
  // void setBorder( ArrayList< PointF > border ) { mBorder = border; }

  int doRefinement( ) 
  {
    if ( mRefines == null || mRefines.size() == 0 ) return 0;
    int ret = 0;
    ArrayList< PointF > border = new ArrayList< PointF >();
    mBorder3d = new ArrayList< Vector >();
    synchronized( mRefines ) {
      PointF p = new PointF(0,0);
      mInfo.worldToSceneOrigin( mRefines.get(0).v2, p );
      border.add( p );
      for ( SketchRefinement ref : mRefines ) {
        p = new PointF(0,0);
        mBorder3d.add( ref.v3 );
        mInfo.worldToSceneOrigin( ref.v3, p );
        border.add( p );
      }
      for ( SketchRefinement ref : mRefines ) {
        refineTriangleAtVertex( ref.t, ref.v, ref.v2, ref.v3 );
      }
      ret = findTrianglesInside( border );
    }
    return ret;
  }

  boolean joinSurfacesAtStation( SketchStationName st )
  {
    String name = st.mName; 
    Log.v("DistoX", "join surfaces at station " + name );
    // for ( SketchSurface surface : mJoins ) {
    //   if ( surface.st1.equals( name ) ) { 
    //     mJoins.remove( surface );
    //     break;
    //   }
    // }
    // SketchSectionSet sections = getSectionsAt ( name );
    // Vector center = new Vector( st.x, st.y, st.z );
    // return createJoinSurface( sections, name, center );
    
    // TODO JOIN SURFACE(S)
    return true;
  }

  /**
   * @param sections  station sections
   * @param name station name
   */
  // private boolean createJoinSurface( SketchSectionSet sections, String name, Vector center )
  // {
  //   // Log.v("DistoX", "join sections nr: " + sections.size() );
  //   if ( sections.size() < 2 ) { // FIXME more than 2
  //     return false;
  //   }
  //   // Vector n0 = sections.getSection(0).getNormal();
  //   // Vector n1 = sections.getSection(1).getNormal();
  //   // if ( n1.dot(n0) < 0.0f ) {
  //   //   sections.getSection(1).reverseLine();
  //   // }

  //   ArrayList< SketchFixedPath > splays1 = new ArrayList< SketchFixedPath >();
  //   if ( TopoDroidApp.mSketchUsesSplays ) {
  //     for ( SketchFixedPath p : mFixedStack ) {
  //       if ( p.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
  //         if ( p.st1.equals( name ) ) {
  //           splays1.add( p );
  //         }
  //       }
  //     }
  //     // Log.v("DistoX", "make surface, ns " + ns + " splays " + splays1.size() + " " + splays2.size() );
  //   }
  //   SketchSurface surface = new SketchSurface( mInfo.st1, mInfo.st2, mPainter );
  //   surface.makeJoinTriangles( sections, mInfo, splays1, center ); 
  //   mJoins.add( surface );
  //   return true;
  // }

  // ------------------------------------------------------------------

  void setLabelToLastPoint( String label )
  {
    int np = mPaths.size();
    if ( np == 0 ) return;
    SketchPath path = mPaths.get( np - 1 );
    if ( path.mType != DrawingPath.DRAWING_PATH_POINT ) return;
    SketchPointPath point = (SketchPointPath)path;
    point.mLabel = label;
  }

  void setNum( DistoXNum num ) 
  {
    mNum  = num;
    stations = num.getStations();
    // shots    = num.getShots();
    // splays   = num.getSplays();
  }

  /** called from SketchActivity
   * only 3D lines can be highlighted
   */
  // void highlightLineRegion( )
  // {
  //   int np = mPaths.size();
  //   if ( np == 0 ) return;
  //   SketchPath path = mPaths.get( np - 1 );
  //   if ( path.mType != DrawingPath.DRAWING_PATH_LINE) return;

  //   if ( mCurrentSurface == null || path.mSurface != mCurrentSurface ) return;
  //   mCurrentSurface.computeInnerBorder( (SketchLinePath)path, mInfo );
  // }

  void makeCut( )
  {
    if ( mCurrentSurface != null ) mCurrentSurface.makeCut();
  }

  // void makeExtrude( ArrayList<Vector> pts )
  // {
  //   if ( mBorder3d == null ) return;
  //   if ( mCurrentSurface != null ) mCurrentSurface.makeExtrude( pts, mBorder3d );
  //   mBorder3d = null;
  // }

  void makeStretch( ArrayList<Vector> pts )
  {
    if ( mBorder3d == null ) return;
    if ( mCurrentSurface != null ) mCurrentSurface.makeStretch( pts, mBorder3d );
    mBorder3d = null;
  }

  void removeSurface( boolean with_sections )
  {
    if ( mCurrentSurface != null /* && mCurrentSurface.st1.equals( mInfo.station1 ) && mCurrentSurface.st2.equals( mInfo.station2 ) */ ) { 
      if ( with_sections ) {
        // remove joins at the endpoints:
        if ( mJoins != null ) {
          synchronized( mJoins ) {
            final Iterator i = mJoins.iterator();
            while ( i.hasNext() ) {
              final SketchSurface surface = (SketchSurface) i.next();
              if ( mInfo.isConnectedTo(mCurrentSurface, SketchDef.DISPLAY_NGBH ) ) {
                mJoins.remove( surface ); 
              }
            }
          }
        }
        // clearSections();
      }
      if ( mPaths != null) {
        synchronized( mPaths ) {
          final Iterator i = mPaths.iterator();
          while ( i.hasNext() ) {
            final SketchPath path = (SketchPath) i.next();
            if ( path.mSurface == mCurrentSurface ) {
              mPaths.remove( path );
            }
          }
          // mRedoStack.add( path );
        }
      }
      mSurfaces.remove( mCurrentSurface );
    }
    mCurrentSurface = null;
  }

  void addPoint( SketchPointPath point ) 
  {
    // points.add( point );
    mPaths.add( point );
    point.mSurface = mCurrentSurface;
    mUndo = new SketchUndo( mUndo, point );
    mRedo = null;
  }

  // array of canvas 2d points
  int findTrianglesInside( ArrayList<PointF> border )
  {
    return ( mCurrentSurface == null )? 0 : mCurrentSurface.findTrianglesInside( border );
  }

  int refineToMaxSide( float max_size )
  {
    return ( mCurrentSurface == null )? 0 : mCurrentSurface.refineToMaxSide( max_size );
  }

  void refineSurfaceAtCenters()
  {
    if ( mCurrentSurface != null ) mCurrentSurface.refineAtCenters();
  }

  void refineSurfaceAtSides()
  {
    if ( mCurrentSurface != null ) mCurrentSurface.refineAtSides();
  }

  boolean refineTriangleAtVertex( SketchTriangle t, SketchVertex v, float t1, float t2, boolean add )
  {
    return mCurrentSurface != null && mCurrentSurface.refineTriangleAtVertex( t, v, t1, t1, add );
  }

  boolean refineTriangleAtVertex( SketchTriangle t, SketchVertex vv, Vector v12, Vector v13 )
  {
    return mCurrentSurface != null && mCurrentSurface.refineTriangleAtVertex( t, vv, v12, v13, mInfo );
  }

  // boolean removeTriangle( SketchTriangle t )
  // {
  //   return mCurrentSurface != null && mCurrentSurface.removeTriangle( t );
  // }

  void makeConvexSurface()
  {
    NumStation st1 = mInfo.station1;
    NumStation st2 = mInfo.station2;
    List<NumSplay> splay1 = mNum.getSplaysAt( st1 );
    List<NumSplay> splay2 = mNum.getSplaysAt( st2 );
    // Log.v("DistoX", "splays at 1: " + splay1.size() + " at 2: " + splay2.size() );
    if ( splay1.size() < 2 || splay2.size() < 2 ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "makeConvexSurface too few spalys " + splay1.size() + " " + splay2.size() );
      return;
    }

    ArrayList< Vector > pts = new ArrayList<Vector>();
    for ( NumSplay sp : splay1 ) {
      pts.add( sp.toVector() );
    }
    for ( NumSplay sp : splay2 ) {
      pts.add( sp.toVector() );
    }
    Vector v1 = st1.toVector();
    Vector v2 = st2.toVector();

    ConvexHull hull = new ConvexHull( v1, v2, pts );
    mCurrentSurface = new SketchSurface( mInfo.st1, mInfo.st2, mPainter );
    mCurrentSurface.makeTriangles( mInfo, hull );
    mSurfaces.add( mCurrentSurface );
  }

  void clearReferences()
  {
    synchronized( mFixedStack ) {
      mFixedStack.clear();
    }
    synchronized( mStations ) {
      mStations.clear();
    }
  }

  void addSketchPath( SketchPath path )
  {
    mPaths.add( path );
    path.mSurface = mCurrentSurface;
    mUndo = new SketchUndo( mUndo, path );
    mRedo = null;
  }

  public void addFixedPath( SketchFixedPath path )
  {
    mFixedStack.add( path );
  }

  public void addFixedStation( SketchStationName st )
  {
    mStations.add( st );
  }

  public void redo()
  {
    if ( mRedo != null ) {
      SketchUndo redo = mRedo;
      mRedo = mRedo.mNext;
      switch ( redo.mType ) {
        case SketchUndo.UNDO_PATH:
          mPaths.add( redo.mPath );
          break;
      }
      redo.mNext = mUndo;
      mUndo = redo;
    }
  }

  public void undo ()
  {
    if ( mUndo != null ) {
      SketchUndo undo = mUndo;
      mUndo = mUndo.mNext;
      switch ( undo.mType ) {
        case SketchUndo.UNDO_PATH:
          int len = mPaths.size();
          if ( len > 0) {
            SketchPath path = mPaths.get( len-1 );
            // if ( path == undo.mPath ) {
              mPaths.remove( len-1 );
            // }
          }
          // mPaths.remove( undo.mPath );
          break;
      }
      undo.mNext = mRedo;
      mRedo = undo;
    }
  }

  public void setTransform( float dx, float dy, float s )
  {
    mMatrix = new Matrix();
    mMatrix.postTranslate( dx, dy );
    mMatrix.postScale( s, s );
  }

  public void executeAll( Canvas canvas, Handler doneHandler )
  {
    if ( mFixedStack != null ) {
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ) {
          final SketchFixedPath fixed = (SketchFixedPath) i.next();
          if ( mInfo.isConnectedTo(fixed, mDisplayMode) ) {
            fixed.draw( canvas, mMatrix, mInfo );
          }
          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
 
    // FIXME TODO draw for VIEW_3D
    if ( mStations != null ) {  
      synchronized( mStations ) {
        for ( SketchStationName st : mStations ) {
          // if ( mInfo.contains( st.mName ) ) {
            st.draw( canvas, mMatrix, mInfo );
          // }
        }
      }
    }

    if ( mSurfaces != null ) {
      synchronized( mSurfaces ) {
        final Iterator i = mSurfaces.iterator();
        while ( i.hasNext() ) {
          final SketchSurface surface = (SketchSurface) i.next();
          if ( mInfo.isConnectedTo(surface, mDisplayMode) ) {
            surface.draw( canvas, mMatrix, mInfo );
          }
        }
      }
    }

    if ( mRefines != null && mRefines.size() > 0 ) {
      synchronized( mRefines ) {
        Path path = new Path();
        PointF p = new PointF(0,0);
        mInfo.worldToSceneOrigin( mRefines.get(mRefines.size()-1).v3, p );
        path.moveTo( p.x, p.y );
        for ( SketchRefinement ref : mRefines ) {
          mInfo.worldToSceneOrigin( ref.v3, p );
          path.lineTo( p.x, p.y );
        }
        // path.close();
        path.transform( mMatrix );
        canvas.drawPath( path, mPainter.bluePaint );
      }
    }

    if ( mJoins != null ) {
      synchronized( mJoins ) {
        final Iterator i = mJoins.iterator();
        while ( i.hasNext() ) {
          final SketchSurface surface = (SketchSurface) i.next();
          if ( mInfo.isConnectedTo(surface, mDisplayMode) ) {
            surface.draw( canvas, mMatrix, mInfo );
          }
        }
      }
    }

    if ( mPaths != null ) {
      synchronized( mPaths ) {
        final Iterator i = mPaths.iterator();
        while ( i.hasNext() ) {
          final SketchPath path = (SketchPath) i.next();
          if ( path.mType == DrawingPath.DRAWING_PATH_LINE || path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            SketchLinePath line = ( SketchLinePath ) path;
            if ( mInfo.isConnectedTo(line, mDisplayMode) ) {
              line.draw( canvas, mMatrix, mInfo );
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            SketchPointPath point = (SketchPointPath) path;
            if ( mInfo.isConnectedTo(point, mDisplayMode) ) {
              point.draw( canvas, mMatrix, mInfo );
            }
          }
        }
      }
    }

    // synchronized( points ) {
    //   final Iterator i = points.iterator();
    //   while ( i.hasNext() ) {
    //     final SketchPointPath point = (SketchPointPath) i.next();
    //     if ( mInfo.isConnectedTo(point, mDisplayMode) ) {
    //       point.draw( canvas, mMatrix, mInfo );
    //     }
    //   }
    // }   

    // if ( mSectionBaseVector != null ) {
    //   synchronized( mSectionBaseVector ) {
    //     if ( mSectionType != SketchSection.SECTION_NONE ) {
    //       PointF q0 = new PointF();
    //       PointF q1 = new PointF();
    //       PointF q3 = new PointF();
    //       mInfo.worldToSceneOrigin( mSectionBaseVector.x, mSectionBaseVector.y, mSectionBaseVector.z, q0 );
    //       if ( mSectionType == SketchSection.SECTION_VERT ) {
    //         mInfo.worldToSceneOrigin( mSectionBaseVector.x-mInfo.ns, mSectionBaseVector.y+mInfo.ne, mSectionBaseVector.z, q1 );
    //         mInfo.worldToSceneOrigin( mSectionBaseVector.x, mSectionBaseVector.y, mSectionBaseVector.z-1, q3 );
    //       } else if ( mSectionType == SketchSection.SECTION_HORIZ ) {
    //         mInfo.worldToSceneOrigin( mSectionBaseVector.x+1, mSectionBaseVector.y,   mSectionBaseVector.z, q1 );
    //         mInfo.worldToSceneOrigin( mSectionBaseVector.x,   mSectionBaseVector.y+1, mSectionBaseVector.z, q3 );
    //       } 
    //       Path path1 = new Path();
    //       Path path2 = new Path();
    //       path1.moveTo(q0.x, q0.y);
    //       path1.lineTo(q1.x, q1.y);
    //       path2.moveTo(q0.x, q0.y);
    //       path2.lineTo(q3.x, q3.y);
    //       path1.transform( mMatrix );
    //       path2.transform( mMatrix );
    //       canvas.drawPath( path1, mPainter.greenPaint ); // green = horiz/east
    //       canvas.drawPath( path2, mPainter.bluePaint );  // blue  = vert/north
    //       // Log.v("DistoX", "draw base vector at " + q.x + " " + q.y );
    //     }
    //   }
    // }

    // synchronized( mSectionSets ) {
    //   final Iterator i = mSectionSets.iterator();
    //   while ( i.hasNext() ) {
    //     final SketchSectionSet s = (SketchSectionSet) i.next();
    //     if ( s.mFrom == mInfo.station1 && s.mTo == mInfo.station2 ) {
    //       // draw all sections of the set
    //       int ns = s.size();
    //       for ( int n = 0; n < ns; ++n ) {
    //         s.getSection(n).draw( canvas, mMatrix, mInfo, mPainter.bluePaint );
    //       }
    //       break;
    //     }
    //   }
    // }

  }

  public SketchTriangle selectTriangleAt( float x_scene, float y_scene, SketchTriangle tri )
  {
    if ( tri != null ) {
      // try tri first and its ngbhs
      SketchSurface sfc = tri.surface;
      tri = sfc.selectTriangleAt( x_scene, y_scene, mInfo, tri );
      // sfc.mSelectedTriangle = tri;
    }
    if ( tri == null ) {
      for ( SketchSurface surface : mSurfaces ) {
        if ( surface.isSameShotAs( mInfo ) ) {
          tri = surface.selectTriangleAt( x_scene, y_scene, mInfo, null );
          // surface.mSelectedTriangle = tri;
          if ( tri != null ) return tri;
        }
      }
    }
    return null;
  }

  public SketchFixedPath selectShotAt( float x, float y ) // (x,y) scene coords, view-mode
  {
    float min_dist = SketchDef.MIN_DISTANCE;
    SketchFixedPath ret = null;
    for ( SketchFixedPath p : mFixedStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
        float d = p.distance( x, y );
        if ( d < mCloseness && d < min_dist ) { 
          min_dist = d;
          ret = p;
        }
      }
    }
    return ret;
  }

  public SketchLinePath selectLineAt( float x, float y, float z, int v ) // (x,y,z) world coords, view-mode
  {
    float min_dist = SketchDef.MIN_DISTANCE;
    SketchLinePath ret = null;
    for ( SketchPath p0 : mPaths ) {
      if ( p0.mType == DrawingPath.DRAWING_PATH_LINE ) {
        SketchLinePath p = (SketchLinePath)p0;
        float d = p.distance( x, y, z );
        if ( d < mCloseness && d < min_dist ) {
          min_dist = d;
          ret = p;
        }
      }
    }
    return ret;
  }
  
  public SketchStationName selectStationAt( float x, float y ) // (x,y) scene coords, view-mode
  {
    float min_dist = SketchDef.MIN_DISTANCE;
    SketchStationName ret = null;
    for ( SketchStationName st : mStations ) {
      float d = st.sceneDistance( x, y );
      if ( d < mCloseness && d < min_dist ) { 
        min_dist = d;
        ret = st;
      }
    }
    return ret;
  }

  public void deleteLine( SketchLinePath line )
  {
    mPaths.remove( line );
  }

  public void exportTherion( BufferedWriter out, String sketch_name, String proj_name )
  {
    // commandManager.exportTherion( out, sketch_name, plot_name );
    try {
      out.write("encoding utf-8");
      out.newLine();
      out.newLine();
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.format("scrap %s -projection %s -scale [0 0 1 0 0.0 0.0 1 0.0 m]", sketch_name, proj_name );
      out.write( sw.getBuffer().toString() );
      out.newLine();
      for ( NumStation st : stations ) {
        StringWriter sw1 = new StringWriter();
        PrintWriter  pw1 = new PrintWriter( sw1 );
        pw1.format( Locale.ENGLISH, "  point %.2f %.2f %.2f station -name %s\n\n", st.e, -st.s, -st.v, st.name );
        out.write( sw1.getBuffer().toString() );
      }
      for (  SketchPath path : mPaths ) {
        switch ( path.mType ) {
          case DrawingPath.DRAWING_PATH_LINE:
          case DrawingPath.DRAWING_PATH_AREA:
            StringWriter sw2 = new StringWriter();
            PrintWriter  pw2 = new PrintWriter( sw2 );
            SketchLinePath line = ( SketchLinePath ) path;
            {
              if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
                pw2.format("  line %s %s -shot %s %s\n", "3d",
                                                         DrawingBrushPaths.getLineName( line.mThType ),
                                                         line.st1, line.st2 );
              } else {
                pw2.format("  area %s %s -shot %s %s\n", "3d",
                                                         DrawingBrushPaths.getAreaName( line.mThType ),
                                                         line.st1, line.st2 );
              }
            }
            for ( Vector pt : line.mLine.points ) {
              pw2.format( Locale.ENGLISH, "    %.2f %.2f %.2f\n", pt.x, -pt.y, -pt.z );
            }          
            {
              if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
                pw2.format("  endline\n\n");
              } else {
                pw2.format("  endarea\n\n");
              }
            }
            out.write( sw2.getBuffer().toString() );
            break;
          case DrawingPath.DRAWING_PATH_POINT:
            SketchPointPath point = ( SketchPointPath ) path;
            out.write( point.toTherion() );
            break;
        }
      }
      for ( SketchSurface surface : mSurfaces ) {
        StringWriter sw3 = new StringWriter();
        PrintWriter  pw3 = new PrintWriter( sw3 );
        surface.toTherion( pw3, "surface" );
        out.write( sw3.getBuffer().toString() );
      }
      for ( SketchSurface surface : mJoins ) {
        StringWriter sw4 = new StringWriter();
        PrintWriter  pw4 = new PrintWriter( sw4 );
        surface.toTherion( pw4, "join" );
        out.write( sw4.getBuffer().toString() );
      }
      // for ( SketchPointPath point : points ) {
      //   out.write( point.toTherion() );
      // }
      out.write("endscrap\n\n");
    } catch ( IOException e ) {
      Log.e( "DistoX", e.toString() );
    }
  }

  private String readLine( BufferedReader br )
  {
    String line = null;
    try {
      line = br.readLine();
    } catch ( IOException e ) {
      // e.printStackTrace();
      Log.e( "DistoX", e.toString() );
    }
    if ( line != null ) {
      int comment = line.indexOf('#');
      if ( comment == 0 ) {
        return "";
      } else if (comment > 0 ) {
        line = line.substring( 0, comment );
      }
      line = line.trim();
      line.replaceAll(" *", " ");
      // line.replaceAll("\\s+", " ");
    }
    return line;
  } 

  public boolean loadTh3( String filename, SymbolsPalette missingSymbols, SketchPainter painter )
  {
    float x, y, z, x1, y1, z1, x2, y2, z2;
    int i1, i2, i3;
    String type;
    String label_text;
    String fromStation = null, toStation = null;
    if ( missingSymbols != null ) missingSymbols.resetSymbolLists();

    // Log.v( "DistoX", "loadTh3 " + filename );
    DrawingBrushPaths.resetPointOrientations();
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = null;
      while ( (line = readLine(br)) != null ) {
        int k1 = 1;
        line = line.trim();
        if ( line.length() == 0 ) continue;
        String[] vals = line.split( " " );
        if ( vals.length == 0 ) continue;
        fromStation = null;
        toStation   = null;
        for ( k1 = 1; k1<vals.length-2; ++k1 ) { // look for "-shot A B"
          if ( vals[k1].equals( "-shot" ) ) {
            fromStation = vals[k1+1];
            toStation   = vals[k1+2];
            k1 += 3;
            break;
          }
        }
        int k = 0;
        if ( vals[k].equals( "encoding" ) ) {
          continue;
        } else if ( vals[k].equals( "scrap" ) ) {
          continue;
        } else if ( vals[k].equals( "endscrap" ) ) {
          continue;
        } else if ( vals[k].equals( "surface" ) || vals[k].equals( "join" ) ) {
          boolean is_surface = vals[k].equals( "surface" );
          boolean is_join    = vals[k].equals( "join" );
          // ****** THERION SURFACE ********************************
          // surface nr_vertices nr_triangles
          // offset ...
          // vertex
          // index x y z
          // ...
          // endvertex
          // side 
          // index v1 v2 <-- ignored
          // ...
          // endside
          // triangle
          // v1 v2 v3
          // ...
          // endtriangle
          // endsurface
          // -------------------------------------------------------
          int nv = Integer.parseInt( vals[k1] ); // number of vertices
          // int ns = Integer.parseInt( vals[k1+1] );
          int nt = Integer.parseInt( vals[k1+1] );
          // Log.v("DistoX", "surface nv " + nv + " nt " + nt );

          line = readLine( br );
          if ( ! line.equals( "endsurface" ) && ! line.equals( "endjoin" ) ) { 
            boolean ok = true;
            SketchSurface surface = new SketchSurface( fromStation, toStation, painter );
            if ( is_surface && fromStation.equals( mInfo.st1 ) && toStation.equals( mInfo.st2 ) ) {
              mCurrentSurface = surface;
            }
            while ( ! line.equals( "endsurface" ) && ! line.equals( "endjoin" ) ) {
              if ( line.startsWith( "vertex" ) ) { // read vertices
                line = readLine( br );
                while ( ! line.equals( "endvertex" ) ) {
                  String[] pt = line.split( "\\s+" );
                  int i = Integer.parseInt( pt[0] );
                  x =   Float.parseFloat( pt[1] ); // / TopoDroidConst.TO_THERION;
                  y = - Float.parseFloat( pt[2] ); // / TopoDroidConst.TO_THERION;
                  z = - Float.parseFloat( pt[3] ); // / TopoDroidConst.TO_THERION;
                  i1 = surface.addVertex( i, x, y, z );
                  line = readLine( br );
                }
                if ( surface.mVertices.size() != nv ) {
                  Log.e("DistoX", "surface " + fromStation + " " + toStation + " vertex size mismatch " + nv + " " + surface.mVertices.size() );
                  nv = surface.mVertices.size();
                  ok = false;
                }
              } else if ( line.startsWith( "side" ) ) {
                line = readLine( br );
                while ( ! line.equals( "endside" ) ) {
                  // String[] pt = line.split( "\\s+" );
                  // i1 = Integer.parseInt( pt[0] ); // index
                  // i2 = Integer.parseInt( pt[1] ); // first vertex
                  // i3 = Integer.parseInt( pt[2] ); // second vertex
                  // surface.addSide( i1, i2, i3 );
                  line = readLine( br );
                }
                // if ( surface.sides.size() != ns ) {
                //   Log.e("DistoX", "surface " + fromStation + " " + toStation + " side size mismatch " + ns + " " + surface.sides.size() );
                //   ok = false;
                //   ns = surface.sides.size();
                // }
              } else if ( line.startsWith( "triangle" ) ) {
                line = readLine( br );
                while ( ! line.equals( "endtriangle" ) ) {
                  String[] pt = line.split( "\\s+" );
                  i1 = Integer.parseInt( pt[0] );
                  i2 = Integer.parseInt( pt[1] );
                  i3 = Integer.parseInt( pt[2] );
                  if ( i1 != i2 && i2 != i3 && i3 != i1 ) {
                    // int s23 = Integer.parseInt( pt[3] );
                    // int s31 = Integer.parseInt( pt[4] );
                    // int s12 = Integer.parseInt( pt[5] );
                    // surface.addTriangle( i1, i2, i3, s23, s31, s12 );
                 
                    surface.addTriangle( i1, i2, i3 );
                  }
                  line = readLine( br );
                }
                if ( surface.mTriangles.size() != nt ) {
                  Log.e("DistoX", "surface " + fromStation + " " + toStation + " triangle size mismatch " + nt + " " + surface.mTriangles.size() );
                  ok = false;
                  nt = surface.mTriangles.size();
                }
              }
              line = readLine( br );
            }
            // surface.dump();
            if ( ok ) {
              if ( is_surface ) {
                surface.computeBorders();
                mSurfaces.add( surface );
              } else if ( is_join ) {
                mJoins.add( surface );
              }
            }
          }
        } else if ( vals[k].equals( "point" ) ) {
          // ****** THERION POINT **********************************
          // int ptType = DrawingBrushPaths.mPointLib.mAnyPointNr;
          // boolean has_orientation = false;
          // float orientation = 0.0f;
          // int scale = DrawingPointPath.SCALE_M;
          // String options = null;
          x = y = z = 0.0f;
          if ( ++k < vals.length ) {
            x =   Float.parseFloat( vals[k] ); // / TopoDroidConst.TO_THERION; // th3 has east
          }
          if ( ++k < vals.length ) {
            y = - Float.parseFloat( vals[k] ); // / TopoDroidConst.TO_THERION;   // north
          }
          if ( ++k < vals.length ) {
            z = - Float.parseFloat( vals[k] ); // / TopoDroidConst.TO_THERION; // pos-Z
          }
          if ( ++k < vals.length ) {
            type = vals[k];
            if ( type.equals( "station" ) ) {
              // stations are automatic in the 3D model
              continue;
            } else {
              int ptindex = 0;
              for ( ; ptindex < DrawingBrushPaths.mPointLib.mAnyPointNr; ++ptindex ) {
                if ( type.equals( DrawingBrushPaths.getPointThName( ptindex ) ) ) break;
              }
              if ( ptindex < DrawingBrushPaths.mPointLib.mAnyPointNr ) {
                SketchPointPath path = new SketchPointPath( ptindex, fromStation, toStation, x, y, z );
                addPoint( path );
                // parse options
                while ( ++k < vals.length ) {
                  if ( vals[k].equals("-orientation") ) {
                    x = y = z = 0.0f;
                    if ( ++k < vals.length ) {
                      x =   Float.parseFloat( vals[k] ); // / TopoDroidConst.TO_THERION; 
                    }
                    if ( ++k < vals.length ) {
                      y = - Float.parseFloat( vals[k] ); // / TopoDroidConst.TO_THERION;
                    }
                    if ( ++k < vals.length ) {
                      z = - Float.parseFloat( vals[k] ); // / TopoDroidConst.TO_THERION;
                    }
                    path.setOrientation( new Vector(x,y,z), mInfo );
                  }
                }
              }
            }
          }
        } else if ( vals[k].equals( "line" ) || vals[k].equals( "area" ) ) {
          boolean is_line = vals[k].equals("line");
          // ********* THERION LINES ************************************************************
          int th_type = 0;
          boolean closed = false;
          String options = null;
          if ( ++k < vals.length ) {
            type = vals[k];
            // assert type.equals("3d")
          }
          if ( ++k < vals.length ) {
            if ( is_line ) {
              int lnTypeMax = DrawingBrushPaths.mLineLib.mAnyLineNr;
              for ( th_type=0; th_type < lnTypeMax; ++th_type ) {
                 if ( vals[k].equals( DrawingBrushPaths.getLineThName( th_type ) ) ) break;
              }
            } else {
              closed = true;
              int lnTypeMax = DrawingBrushPaths.mAreaLib.mAnyAreaNr;
              for ( th_type=0; th_type < lnTypeMax; ++th_type ) {
                 if ( vals[k].equals( DrawingBrushPaths.getAreaThName( th_type ) ) ) break;
              }
            }
          }
          
            // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "line type " + vals[1] );
           
            for ( ++k; k < vals.length; ++k ) {
              if ( vals[k].length() == 0 ) {
                continue;
              }
              if ( vals[k].equals( "-close" ) ) {
                if ( ++k < vals.length && vals[k].equals( "on" ) ) {
                  closed = true;
                }
              } else if ( vals[k].equals( "-shot" ) ) {
                k += 2; // skip "-shot A B"
              } else {
                if ( options == null ) {
                  options = vals[k];
                } else {
                  options += " " + vals[k];
                }
              } 
            }
            // Log.v("DistoX", "load line " + vals[1] + " stations " + fromStation + " " + toStation );
            
            // insert new line-path
            // FIXME SOON line and area paint
            line = readLine( br );
            if ( ! ( line.equals( "endline" ) || line.equals( "endarea" ) ) ) { 
              SketchLinePath path = null;
              if ( is_line ) {
                path = new SketchLinePath( DrawingPath.DRAWING_PATH_LINE, th_type, fromStation, toStation, painter );
              } else {
                path = new SketchLinePath( DrawingPath.DRAWING_PATH_AREA, th_type, fromStation, toStation, painter );
              }
           
              // if ( closed ) path.mClosed = true;
              // if ( options != null ) path.mOptions = options;

              String[] pt = line.split( "\\s+" );
              x =   Float.parseFloat( pt[0] ); // / TopoDroidConst.TO_THERION;
              y = - Float.parseFloat( pt[1] ); // / TopoDroidConst.TO_THERION;
              z = - Float.parseFloat( pt[2] ); // / TopoDroidConst.TO_THERION;

              path.addLinePoint( x, y, z );
              while ( (line = readLine( br )) != null ) {
                if ( line.equals( "endline" ) || line.equals( "endarea" ) ) {
                  if ( path != null ) {
                    addSketchPath( path );
                  }
                  break;
                }
                if ( path != null ) {
                  String[] vals2 = line.split( " " );
                  if ( vals2.length == 3 ) {
                    x  =   Float.parseFloat( vals2[0] ); // / TopoDroidConst.TO_THERION;
                    y  = - Float.parseFloat( vals2[1] ); // / TopoDroidConst.TO_THERION;
                    z  = - Float.parseFloat( vals2[2] ); // / TopoDroidConst.TO_THERION;
                    path.addLinePoint( x, y, z );
                  } else if ( vals2.length == 9 ) {
                    x1 =   Float.parseFloat( vals2[0] ); // / TopoDroidConst.TO_THERION;
                    y1 = - Float.parseFloat( vals2[1] ); // / TopoDroidConst.TO_THERION;
                    z1 = - Float.parseFloat( vals2[2] ); // / TopoDroidConst.TO_THERION;
                    x2 =   Float.parseFloat( vals2[3] ); // / TopoDroidConst.TO_THERION;
                    y2 = - Float.parseFloat( vals2[4] ); // / TopoDroidConst.TO_THERION;
                    z2 = - Float.parseFloat( vals2[5] ); // / TopoDroidConst.TO_THERION;
                    x  =   Float.parseFloat( vals2[6] ); // / TopoDroidConst.TO_THERION;
                    y  = - Float.parseFloat( vals2[7] ); // / TopoDroidConst.TO_THERION;
                    z  = - Float.parseFloat( vals2[8] ); // / TopoDroidConst.TO_THERION;
                    // path.addPoint3( x1, y1, z1, x2, y2, z2, x, y, z, false );
                  }
                }
              }
            }
          // }
        } else {
          Log.e("DistoX", "loadTh3: unknown line type >" + line + "<" );
        }
      }
    } catch ( FileNotFoundException e ) {
      // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    // remove repeated names
    // Log.v( "DistoX", "loadTh3 " + filename + " done" );
    return (missingSymbols != null )? missingSymbols.isOK() : true;
  }

  // -----------------------------------------------------------------
  // SELECTION

  SelectionPoint hotItem()
  {
    return mSelected.mHotItem;
  }

  void shiftHotItem( float dx, float dy )
  {
    mSelected.shiftHotItem( dx, dy );
  }

  SelectionPoint nextHotItem()
  {
    return mSelected.nextHotItem();
  }

  SelectionPoint prevHotItem()
  {
    return mSelected.prevHotItem();
  }

  void clearSelected()
  {
    synchronized( mSelected ) {
      mSelected.clear();
    }
  }

  SketchVertex getVertexAt( float x, float y, float d ) // (x,y) canvas point
  { 
    if ( mCurrentSurface == null ) return null;
    return mCurrentSurface.getVertexAt( x, y, d );
  }

  SketchVertex getSelectedVertex()
  {
    if ( mCurrentSurface == null ) return null;
    return mCurrentSurface.getSelectedVertex();
  }

  void setSelectedVertex( SketchVertex v )
  {
    if ( mCurrentSurface != null ) mCurrentSurface.setSelectedVertex( v );
  }

  void refineSurfaceAtSelectedVertex()
  {
    if ( mCurrentSurface == null ) return;
    mCurrentSurface.refineAtSelectedVertex();
  }
    
}
