/* @file SketchModel.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: sketch 3D model
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
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
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.nio.ByteBuffer;

import android.graphics.Matrix;
import android.graphics.Canvas;
// import android.graphics.Bitmap;
// import android.graphics.PorterDuff;
// import android.graphics.RectF;
import android.graphics.PointF;
// import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;

import android.util.Log;

class SketchModel
{
  // private static final float mCloseness = TDSetting.mSelectness;
  DistoXNum mNum;

  final int SURFACE_NONE        = 0;
  final int SURFACE_CONVEX_HULL = 1;
  final int SURFACE_POWERCRUST  = 2;

  boolean mDisplaySplays;
  boolean mDisplayForeSurface;

  private Sketch3dInfo mInfo;
  private SketchPainter mPainter;
  SketchSurface mCurrentSurface;

  private Matrix mMatrix;
  private List< NumStation > stations;
  // List< NumShot >    shots;
  // List< NumSplay >   splays;
  List< SketchPath > mPaths;
  private List<SketchFixedPath>   mFixedStack;
  private List<SketchStationName> mStations;
  // private List<SketchSectionSet> mSectionSets;
  List<SketchSurface>  mSurfaces;
  List<SketchSurface>  mJoins;

  private ArrayList< SketchRefinement > mRefines;
  private ArrayList< Vector > mBorder3d;
  // SketchLinePath mEditLine;
  // ArrayList< PointF > mBorder;

  private SketchUndo mUndo;
  private SketchUndo mRedo;

  int mDisplayMode  = SketchDef.DISPLAY_NONE;
  int mActivityMode = SketchDef.MODE_MOVE;

  private int cnt;

  private float pi4 = (float)(Math.PI/4);
  private float pi2 = pi4 * 2;

  private Selection mSelection;
  private SelectionSet mSelected;


  SketchModel( Sketch3dInfo info, DistoXNum num, SketchPainter painter ) 
  {
    mInfo    = info;
    mPainter = painter;
    mCurrentSurface = null;
    setNum( num );

    mPaths       = Collections.synchronizedList(new ArrayList<SketchPath>() );
    // points    = Collections.synchronizedList(new ArrayList<>() );
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

    mDisplaySplays = false;
    mDisplayForeSurface = false;

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

/* if MODE_EDIT
  int doRefinement( ) 
  {
    if ( mRefines == null || mRefines.size() == 0 ) return 0;
    int ret = 0;
    ArrayList< PointF > border = new ArrayList<>();
    mBorder3d = new ArrayList<>();
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
*/

  boolean joinSurfacesAtStation( SketchStationName st )
  {
    String name = st.mName; 
    Log.v("DistoX", "TODO join surfaces at station " + name );
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

  // /**
  //  * @param sections  station sections
  //  * @param name station name
  //  */
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

  //   ArrayList< SketchFixedPath > splays1 = new ArrayList<>();
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

  /** called from SketchWindow
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

  // if MODE_EDIT
  // void makeCut( )
  // {
  //   if ( mCurrentSurface != null ) mCurrentSurface.makeCut();
  // }

  // void makeExtrude( ArrayList<Vector> pts )
  // {
  //   if ( mBorder3d == null ) return;
  //   if ( mCurrentSurface != null ) mCurrentSurface.makeExtrude( pts, mBorder3d );
  //   mBorder3d = null;
  // }

  // if MODE_EDIT
  // void makeStretch( ArrayList<Vector> pts )
  // {
  //   if ( mBorder3d == null ) return;
  //   if ( mCurrentSurface != null ) mCurrentSurface.makeStretch( pts, mBorder3d );
  //   mBorder3d = null;
  // }

// UNUSED
//   void removeCurrentSurface( boolean with_sections )
//   {
//     if ( mCurrentSurface != null /* && mCurrentSurface.st1.equals( mInfo.station1 ) && mCurrentSurface.st2.equals( mInfo.station2 ) */ ) { 
//       if ( with_sections ) {
//         // remove joins at the endpoints:
//         if ( mJoins != null ) {
//           synchronized( mJoins ) {
//             final Iterator i = mJoins.iterator();
//             while ( i.hasNext() ) {
//               final SketchSurface surface = (SketchSurface) i.next();
//               if ( mInfo.isConnectedTo(mCurrentSurface, SketchDef.DISPLAY_NGBH ) ) {
//                 mJoins.remove( surface ); 
//               }
//             }
//           }
//         }
//         // clearSections();
//       }
//       if ( mPaths != null) {
//         synchronized( mPaths ) {
//           final Iterator i = mPaths.iterator();
//           while ( i.hasNext() ) {
//             final SketchPath path = (SketchPath) i.next();
//             if ( path.mSurface == mCurrentSurface ) {
//               mPaths.remove( path );
//             }
//           }
//           // mRedoStack.add( path );
//         }
//       }
//       mSurfaces.remove( mCurrentSurface );
//     }
//     mCurrentSurface = null;
//   }

  void removeAllSurfaces( )
  {
    if ( mJoins != null ) {
      synchronized( mJoins ) {
        mJoins.clear();
      }
    }
    synchronized( mSurfaces ) {
      mCurrentSurface = null;
      mSurfaces.clear();
    }
    if ( mPaths != null) {
      synchronized( mPaths ) {
        mPaths.clear();
      }
    }
  }

  void addPoint( SketchPointPath point ) 
  {
    // points.add( point );
    mPaths.add( point );
    point.mSurface = mCurrentSurface;
    mUndo = new SketchUndo( mUndo, point );
    mRedo = null;
  }

/* if MODE_EDIT
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
*/

  // boolean removeTriangle( SketchTriangle t )
  // {
  //   return mCurrentSurface != null && mCurrentSurface.removeTriangle( t );
  // }

  

  void makeSurface( int type )
  {
    NumStation st1 = mInfo.station1;
    NumStation st2 = mInfo.station2;
    // Log.v("DistoX", "make surface at " + st1.name + " " + st2.name );
    // NumShot sh0 = mNum.getShot( st1, st2 ); 
    List<NumSplay> tmp1 = mNum.getSplaysAt( st1 );
    List<NumSplay> tmp2 = mNum.getSplaysAt( st2 );
    List<NumShot> shot1 = mNum.getShotsAt( st1, st2 ); // shots at st1 except [st1,st2]
    List<NumShot> shot2 = mNum.getShotsAt( st2, st1 );
    // Log.v("DistoX", "splays at 1: " + splay1.size() + " at 2: " + splay2.size() );

    Vector v0 = new Vector( st2.e - st1.e, st2.s - st1.s, st2.v - st1.v );
    float l = v0.LengthSquared();
    v0.timesEqual( 1/l );

    ArrayList< Vector > vec1 = new ArrayList<>();
    for ( NumShot sh : shot1 ) {
      Vector v = ( sh.from == st1 )?
                 new Vector( sh.to.e   - st1.e, sh.to.s   - st1.s, sh.to.v   - st1.v )
               : new Vector( sh.from.e - st1.e, sh.from.s - st1.s, sh.from.v - st1.v );
      l = v.LengthSquared();
      v.timesEqual( 1/l );
      vec1.add( v );
    }
    ArrayList< Vector > vec2 = new ArrayList<>();
    for ( NumShot sh : shot2 ) {
      Vector v = ( sh.from == st2 )?
                 new Vector( sh.to.e   - st2.e, sh.to.s   - st2.s, sh.to.v   - st2.v )
               : new Vector( sh.from.e - st2.e, sh.from.s - st2.s, sh.from.v - st2.v );
      l = v.LengthSquared();
      v.timesEqual( 1/l );
      vec2.add( v );
    }

    ArrayList< Vector > pts = new ArrayList<>();
    for ( NumSplay sp : tmp1 ) {
      Vector v = new Vector( sp.e - st1.e, sp.s - st1.s, sp.v - st1.v );
      float x0 = v.dot( v0 );
      boolean ok = true;
      if ( x0 < 0 /* || x0 > 1 */ ) { // if v projects before v0
        for ( Vector v1 : vec1 ) {
          if ( v.dot( v1 ) > 0 ) { ok = false; break; }
        }
      }
      if ( ok ) {
        pts.add( sp.toVector() );
      }
    }
    for ( NumSplay sp : tmp2 ) {
      Vector v = new Vector( sp.e - st2.e, sp.s - st2.s, sp.v - st2.v );
      float x0 = v.dot( v0 );
      boolean ok = true;
      if ( /* x0 < -1 || */ x0 > 0 ) { // if v projects beyond v0
        for ( Vector v2 : vec2 ) {
          if ( v.dot( v2 ) > 0 ) { ok = false; break; }
        }
      }
      if ( ok ) {
        pts.add( sp.toVector() );
      }
    }
        
    if ( pts.size() < 8 ) {
      TDLog.Error( "make Convex Surface too few splays " + pts.size() );
      return;
    }

    Vector vt1 = st1.toVector();
    Vector vt2 = st2.toVector();

    mCurrentSurface = new SketchSurface( mInfo.st1, mInfo.st2, mPainter );

    switch ( type ) {
      case SURFACE_POWERCRUST:
        Powercrust pc = new Powercrust( vt1, vt2, pts );
        pc.compute();
        mCurrentSurface.makePowercrustTriangles( mInfo, pc );
        pc.release();
        break;
      case SURFACE_CONVEX_HULL:
      default:
        ConvexHull hull = new ConvexHull( vt1, vt2, pts );
        mCurrentSurface.makeConvexHullTriangles( mInfo, hull );
        break;
    }

    removeSurface( mInfo.st1, mInfo.st2 );
    mSurfaces.add( mCurrentSurface );
    // Log.v("DistoX", "Nr surface " + mSurfaces.size() );
  }

  private void removeSurface( String st1, String st2 )
  {
    for ( SketchSurface surface : mSurfaces ) {
      if ( st1.equals( surface.st1 ) && st2.equals( surface.st2 ) ) {
        mSurfaces.remove( surface );
        break;
      }
    }
  }

  boolean hasSurface( String st1, String st2 )
  {
    for ( SketchSurface surface : mSurfaces ) {
      if ( st1.equals( surface.st1 ) && st2.equals( surface.st2 ) ) return true;
    }
    return false;
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

  void addFixedPath( SketchFixedPath path )
  {
    mFixedStack.add( path );
  }

  void addFixedStation( SketchStationName st )
  {
    mStations.add( st );
  }

  void redo()
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

  void undo ()
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

  void setTransform( float dx, float dy, float s )
  {
    mMatrix = new Matrix();
    mMatrix.postTranslate( dx, dy );
    mMatrix.postScale( s, s );
  }

  void executeAll( Canvas canvas, Handler doneHandler )
  {
    if ( mFixedStack != null ) {
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ) {
          final SketchFixedPath fixed = (SketchFixedPath) i.next();
          if ( mDisplaySplays || ! fixed.isSplay() ) {
            if ( mInfo.isConnectedTo(fixed, mDisplayMode) ) {
              fixed.draw( canvas, mMatrix, mInfo, mActivityMode );
            }
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

    if ( mDisplayMode != SketchDef.DISPLAY_NONE ) {
      if ( mSurfaces != null ) {
        synchronized( mSurfaces ) {
          final Iterator i = mSurfaces.iterator();
          while ( i.hasNext() ) {
            final SketchSurface surface = (SketchSurface) i.next();
            if ( mInfo.isConnectedTo(surface, mDisplayMode) ) {
              surface.draw( canvas, mMatrix, mInfo, mActivityMode, mDisplayForeSurface );
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
              surface.draw( canvas, mMatrix, mInfo, mActivityMode, mDisplayForeSurface );
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

  SketchTriangle selectTriangleAt( float x_scene, float y_scene, SketchTriangle tri, float size )
  {
    if ( tri != null ) {
      // try tri first and its ngbhs
      SketchSurface sfc = tri.surface;
      tri = sfc.selectTriangleAt( x_scene, y_scene, mInfo, tri, size );
      // sfc.mSelectedTriangle = tri;
    }
    if ( tri == null ) {
      for ( SketchSurface surface : mSurfaces ) {
        if ( surface.isSameShotAs( mInfo ) ) {
          tri = surface.selectTriangleAt( x_scene, y_scene, mInfo, null, size );
          // surface.mSelectedTriangle = tri;
          if ( tri != null ) return tri;
        }
      }
    }
    return null;
  }

  SketchFixedPath selectShotAt( float x, float y, float size ) // (x,y) scene coords, view-mode
  {
    float min_dist = SketchDef.MIN_DISTANCE;
    SketchFixedPath ret = null;
    for ( SketchFixedPath p : mFixedStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
        float d = p.distance( x, y );
        if ( d < size && d < min_dist ) { 
          min_dist = d;
          ret = p;
        }
      }
    }
    return ret;
  }

  SketchLinePath selectLineAt( float x, float y, float z, int v, float size ) // (x,y,z) world coords, view-mode
  {
    float min_dist = SketchDef.MIN_DISTANCE;
    SketchLinePath ret = null;
    for ( SketchPath p0 : mPaths ) {
      if ( p0.mType == DrawingPath.DRAWING_PATH_LINE ) {
        SketchLinePath p = (SketchLinePath)p0;
        float d = p.distance( x, y, z );
        if ( d < size && d < min_dist ) {
          min_dist = d;
          ret = p;
        }
      }
    }
    return ret;
  }
  
  SketchStationName selectStationAt( float x, float y, float size ) // (x,y) scene coords, view-mode
  {
    float min_dist = SketchDef.MIN_DISTANCE;
    SketchStationName ret = null;
    for ( SketchStationName st : mStations ) {
      float d = st.sceneDistance( x, y );
      if ( d < size && d < min_dist ) { 
        min_dist = d;
        ret = st;
      }
    }
    return ret;
  }

  void deleteLine( SketchLinePath line )
  {
    mPaths.remove( line );
  }

  void exportTherion( BufferedWriter out, String sketch_name, String proj_name )
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
        pw1.format( Locale.US, "  point %.2f %.2f %.2f station -name %s\n\n", st.e, -st.s, -st.v, st.name );
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
                                                         BrushManager.mLineLib.getSymbolName( line.mThType ),
                                                         line.st1, line.st2 );
              } else {
                pw2.format("  area %s %s -shot %s %s\n", "3d",
                                                         BrushManager.mAreaLib.getSymbolName( line.mThType ),
                                                         line.st1, line.st2 );
              }
            }
            for ( Vector pt : line.mLine.points ) {
              pw2.format( Locale.US, "    %.2f %.2f %.2f\n", pt.x, -pt.y, -pt.z );
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
            String pp_str = point.toTherion();
	    if ( pp_str != null ) out.write( pp_str );
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
      line = line.replaceAll(" *", " ");
      // line.replaceAll("\\s+", " ");
    }
    return line;
  } 

  boolean loadTh3( String filename, SymbolsPalette missingSymbols, SketchPainter painter )
  {
    float x, y, z, x1, y1, z1, x2, y2, z2;
    int i1, i2, i3;
    String type;
    String label_text;
    String fromStation = null, toStation = null;
    if ( missingSymbols != null ) missingSymbols.resetSymbolLists();

    // Log.v( "DistoX", "load Th3 " + filename );
    BrushManager.resetPointOrientations();
    try {
      // TDLog.Log( TDLog.LOG_IO, "read sketch file <" + filename + ">" );
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
                  x =   Float.parseFloat( pt[1] ); // / TDConst.TO_THERION;
                  y = - Float.parseFloat( pt[2] ); // / TDConst.TO_THERION;
                  z = - Float.parseFloat( pt[3] ); // / TDConst.TO_THERION;
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
          // int ptType = BrushManager.mPointLib.mSymbolNr;
          // boolean has_orientation = false;
          // float orientation = 0.0f;
          // int scale = DrawingPointPath.SCALE_M;
          // String options = null;
          x = y = z = 0.0f;
          if ( ++k < vals.length ) {
            x =   Float.parseFloat( vals[k] ); // / TDConst.TO_THERION; // th3 has east
          }
          if ( ++k < vals.length ) {
            y = - Float.parseFloat( vals[k] ); // / TDConst.TO_THERION;   // north
          }
          if ( ++k < vals.length ) {
            z = - Float.parseFloat( vals[k] ); // / TDConst.TO_THERION; // pos-Z
          }
          if ( ++k < vals.length ) {
            type = vals[k];
            if ( type.equals( "station" ) ) {
              // stations are automatic in the 3D model
              continue;
            } else {
              int ptindex = BrushManager.mPointLib.getSymbolIndexByThName( type );
              // for ( ; ptindex < BrushManager.mPointLib.mSymbolNr; ++ptindex ) {
              //   if ( type.equals( BrushManager.mPointLib.getSymbolThName( ptindex ) ) ) break;
              // }
              if ( ptindex >= 0 && ptindex < BrushManager.mPointLib.mSymbolNr ) {
                SketchPointPath path = new SketchPointPath( ptindex, fromStation, toStation, x, y, z );
                addPoint( path );
                // parse options
                while ( ++k < vals.length ) {
                  if ( vals[k].equals("-orientation") ) {
                    x = y = z = 0.0f;
                    if ( ++k < vals.length ) {
                      x =   Float.parseFloat( vals[k] ); // / TDConst.TO_THERION; 
                    }
                    if ( ++k < vals.length ) {
                      y = - Float.parseFloat( vals[k] ); // / TDConst.TO_THERION;
                    }
                    if ( ++k < vals.length ) {
                      z = - Float.parseFloat( vals[k] ); // / TDConst.TO_THERION;
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
              th_type = BrushManager.mLineLib.getSymbolIndexByThName( vals[k] );
              // int lnTypeMax = BrushManager.mLineLib.mSymbolNr;
              // for ( th_type=0; th_type < lnTypeMax; ++th_type ) {
              //    if ( vals[k].equals( BrushManager.mLineLib.getSymbolThName( th_type ) ) ) break;
              // }
            } else {
              closed = true;
              th_type = BrushManager.mAreaLib.getSymbolIndexByThName( vals[k] );
              // int lnTypeMax = BrushManager.mAreaLib.mSymbolNr;
              // for ( th_type=0; th_type < lnTypeMax; ++th_type ) {
              //    if ( vals[k].equals( BrushManager.mAreaLib.getSymbolThName( th_type ) ) ) break;
              // }
            }
          }
          if ( th_type == -1 ) {
            TDLog.Error("ERROR symbol not found " + vals[k] );
          }
          
            // TDLog.Log( TDLog.LOG_PLOT, "line type " + vals[1] );
           
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
              x =   Float.parseFloat( pt[0] ); // / TDConst.TO_THERION;
              y = - Float.parseFloat( pt[1] ); // / TDConst.TO_THERION;
              z = - Float.parseFloat( pt[2] ); // / TDConst.TO_THERION;

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
                    x  =   Float.parseFloat( vals2[0] ); // / TDConst.TO_THERION;
                    y  = - Float.parseFloat( vals2[1] ); // / TDConst.TO_THERION;
                    z  = - Float.parseFloat( vals2[2] ); // / TDConst.TO_THERION;
                    path.addLinePoint( x, y, z );
                  } else if ( vals2.length == 9 ) {
                    x1 =   Float.parseFloat( vals2[0] ); // / TDConst.TO_THERION;
                    y1 = - Float.parseFloat( vals2[1] ); // / TDConst.TO_THERION;
                    z1 = - Float.parseFloat( vals2[2] ); // / TDConst.TO_THERION;
                    x2 =   Float.parseFloat( vals2[3] ); // / TDConst.TO_THERION;
                    y2 = - Float.parseFloat( vals2[4] ); // / TDConst.TO_THERION;
                    z2 = - Float.parseFloat( vals2[5] ); // / TDConst.TO_THERION;
                    x  =   Float.parseFloat( vals2[6] ); // / TDConst.TO_THERION;
                    y  = - Float.parseFloat( vals2[7] ); // / TDConst.TO_THERION;
                    z  = - Float.parseFloat( vals2[8] ); // / TDConst.TO_THERION;
                    // path.addPoint3( x1, y1, z1, x2, y2, z2, x, y, z, false );
                  }
                }
              }
            }
          // }
        } else {
          Log.e("DistoX", "load Th3: unknown line type >" + line + "<" );
        }
      }
    } catch ( FileNotFoundException e ) {
      // this is OK
    // } catch ( IOException e ) {
    //   e.printStackTrace();
    }
    // remove repeated names
    // Log.v( "DistoX", "load Th3 " + filename + " done" );
    return (missingSymbols == null ) || missingSymbols.isOK();
  }

  // -----------------------------------------------------------------
  // SELECTION

  SelectionPoint hotItem()
  {
    return mSelected.mHotItem;
  }

  // void shiftHotItem( float dx, float dy, float range )
  void shiftHotItem( float dx, float dy )
  {
    // mSelected.shiftHotItem( dx, dy, range );
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

  // void clearSelected()
  // {
  //   synchronized( mSelected ) {
  //     mSelected.clear();
  //   }
  // }

  SketchVertex getVertexAt( float x, float y, float d ) // (x,y) canvas point
  { 
    return ( mCurrentSurface == null )? null : mCurrentSurface.getVertexAt( x, y, d );
  }

  SketchVertex getSelectedVertex()
  {
    return ( mCurrentSurface == null )? null : mCurrentSurface.getSelectedVertex();
  }

  void setSelectedVertex( SketchVertex v )
  {
    if ( mCurrentSurface != null ) mCurrentSurface.setSelectedVertex( v );
  }

  // if MODE_EDIT
  // void refineSurfaceAtSelectedVertex()
  // {
  //   if ( mCurrentSurface != null ) mCurrentSurface.refineAtSelectedVertex();
  // }

  static void toTdr( BufferedOutputStream bos, String s ) throws IOException
  {
    byte[] str = s.getBytes();
    int len = str.length;
    bos.write( (byte)(len & 0xff) );
    bos.write( str, 0, len );
  }

  static void toTdr( BufferedOutputStream bos, float x, float y, float z ) throws IOException
  {
    ByteBuffer b = ByteBuffer.allocate(12); // e, s, v
    b.putFloat( x );
    b.putFloat( y );
    b.putFloat( z );
    bos.write( b.array(), 0, 12 );
  }

  // public static void toTdr( BufferedOutputStream bos, int n ) throws IOException
  // {
  //   ByteBuffer b = ByteBuffer.allocate(4); 
  //   b.putInt( n );
  //   bos.write( b.array(), 0, 4 );
  // }

  static void toTdr( BufferedOutputStream bos, short n ) throws IOException
  {
    ByteBuffer b = ByteBuffer.allocate(2); 
    b.putShort( n );
    bos.write( b.array(), 0, 2 );
  }

  static void toTdr( BufferedOutputStream bos, byte b ) throws IOException
  {
    bos.write( b );
  }

  void exportTdr( BufferedOutputStream bos, String sketch_name, String proj_name )
  {
    // commandManager.exportTherion( out, sketch_name, plot_name );
    try {
      toTdr( bos, sketch_name );

      toTdr( bos, (short)(stations.size() ) );
      for ( NumStation st : stations ) {
        toTdr( bos, st.name );
        toTdr( bos, st.e, st.s, st.v );
      }

      toTdr( bos, (short)(mPaths.size() ) );
      for (  SketchPath path : mPaths ) {
        switch ( path.mType ) {
          case DrawingPath.DRAWING_PATH_LINE:
          case DrawingPath.DRAWING_PATH_AREA:
            SketchLinePath line = ( SketchLinePath ) path;
            {
              if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
                toTdr( bos, (short)2 ); // line
                toTdr( bos, BrushManager.mLineLib.getSymbolName( line.mThType ) );
              } else {
                toTdr( bos, (short)3 ); // area
                toTdr( bos, BrushManager.mAreaLib.getSymbolName( line.mThType ) );
              }
              toTdr( bos, line.st1 );
              toTdr( bos, line.st2 );
              toTdr( bos, (short)(line.mLine.points.size()) );
            }
            for ( Vector pt : line.mLine.points ) {
              toTdr( bos, pt.x, pt.y, pt.z );
            }          
            break;
          case DrawingPath.DRAWING_PATH_POINT:
            SketchPointPath point = ( SketchPointPath ) path;
            point.toTdr( bos );
            break;
        }
      }

      // Log.v("DistoX", "surfaces " + (short)(mSurfaces.size()) + " joins " + (short)(mJoins.size()) );

      toTdr( bos, (short)(mSurfaces.size()) );
      for ( SketchSurface surface : mSurfaces ) {
        surface.toTdr( bos, (short)4 );  // surface
      }

      toTdr( bos, (short)(mJoins.size()) );
      for ( SketchSurface join : mJoins ) {
        join.toTdr( bos, (short)5 ); // join
      }
      bos.flush();
    } catch ( IOException e ) {
      Log.e( "DistoX", e.toString() );
    }
  }

  private String fromTdrString( BufferedInputStream bis ) throws IOException
  {
    int len = bis.read();
    byte[] b = new byte[ len ];
    bis.read( b, 0, len );
    return new String( b );
  }

  private int fromTdrShort( BufferedInputStream bis ) throws IOException
  {
    byte[] b = new byte[2];
    bis.read( b, 0, 2 );
    return ByteBuffer.wrap( b, 0, 2 ).getShort( 0 );
  }

  private int fromTdrByte( BufferedInputStream bis ) throws IOException
  {
    return (int)( bis.read() );
  }

  private float fromTdrFloat( BufferedInputStream bis ) throws IOException
  {
    byte[] b = new byte[4];
    bis.read( b, 0, 4 );
    return ByteBuffer.wrap( b, 0, 4 ).getFloat( 0 );
  }

  private void fromTdrSurface( BufferedInputStream bis, SketchSurface surface, int nv, int nt ) throws IOException
  {
    for ( int k=0; k<nv; ++k ) {
      int idx = fromTdrShort( bis );
      float x = fromTdrFloat( bis );
      float y = fromTdrFloat( bis );
      float z = fromTdrFloat( bis );
      surface.addVertex( idx, x, y, z );
    }
    for ( int k=0; k<nt; ++k ) {
      int i1 = fromTdrShort( bis );
      int i2 = fromTdrShort( bis );
      int i3 = fromTdrShort( bis );
      surface.addTriangle( i1, i2, i3 );
    }
    // Log.v("DistoX", " V " + nv + "/" + surface.mVertices.size() + " T " + nt + "/" + surface.mTriangles.size() );
  }

  boolean loadTdr3( String filename, SymbolsPalette missingSymbols, SketchPainter painter )
  {
    int k, idx, np;
    float x, y, z;
    if ( missingSymbols != null ) missingSymbols.resetSymbolLists();

    // Log.v( "DistoX", "load Tdr3 " + filename );
    BrushManager.resetPointOrientations();
    try {
      // TDLog.Log( TDLog.LOG_IO, "read sketch file " + filename );
      FileInputStream fis = new FileInputStream( filename );
      BufferedInputStream bis = new BufferedInputStream( fis );

      // read sketch name
      String name = fromTdrString( bis );

      // read stations
      int nst = fromTdrShort( bis );
      // Log.v("DistoX", "sketch " + name + " ST " + nst );
      for ( k=0; k<nst; ++k ) { // NumStations
        String st_name = fromTdrString( bis );
        float e = fromTdrFloat( bis );
        float s = fromTdrFloat( bis );
        float v = fromTdrFloat( bis );
      }

      // read paths
      int npt = fromTdrShort( bis );
      // Log.v("DistoX", "Paths " + npt );
      for ( k=0; k<npt; ++k ) { // paths
        int type = fromTdrShort( bis );
        String thtype = fromTdrString( bis );
        String st1 = fromTdrString( bis );
        String st2 = fromTdrString( bis );
        if ( type == 1 ) { // point
          x = fromTdrFloat( bis );
          y = fromTdrFloat( bis );
          z = fromTdrFloat( bis );
          idx = BrushManager.mPointLib.getSymbolIndexByThName( thtype );
          SketchPointPath pt = null;
          if ( idx >= 0 && idx < BrushManager.mPointLib.mSymbolNr ) {
            pt = new SketchPointPath( idx, st1, st2, x, y, z );
            addPoint( pt );
          }
          if ( fromTdrByte( bis ) == 1 ) {
            x = fromTdrFloat( bis );
            y = fromTdrFloat( bis );
            z = fromTdrFloat( bis );
            if ( pt != null ) pt.setOrientation( new Vector(x,y,z), mInfo );
          }
        } else if ( type == 2 ) { // line
          idx = BrushManager.mLineLib.getSymbolIndexByThName( thtype );
          SketchLinePath line = new SketchLinePath( DrawingPath.DRAWING_PATH_LINE, idx, st1, st2, painter );
          np = fromTdrShort( bis );
          for ( k=0; k<np; ++k ) {
            x = fromTdrFloat( bis );
            y = fromTdrFloat( bis );
            z = fromTdrFloat( bis );
            line.addLinePoint( x, y, z );
          }
          addSketchPath( line );
        } else if ( type == 3 ) { // area
          idx = BrushManager.mAreaLib.getSymbolIndexByThName( thtype );
          SketchLinePath area = new SketchLinePath( DrawingPath.DRAWING_PATH_AREA, idx, st1, st2, painter );
          np = fromTdrShort( bis );
          for ( k=0; k<np; ++k ) {
            x = fromTdrFloat( bis );
            y = fromTdrFloat( bis );
            z = fromTdrFloat( bis );
            area.addLinePoint( x, y, z );
          }
          addSketchPath( area );
        }
      }
  
      int nsf = fromTdrShort( bis ); // surfaces
      // Log.v("DistoX", "Surfaces " + nsf );
      for ( k=0; k<nsf; ++k ) {
        int what = fromTdrShort( bis ); // must be 4
        String st1 = fromTdrString( bis );
        String st2 = fromTdrString( bis );
        int nv = fromTdrShort( bis );
        int nt = fromTdrShort( bis );
        // Log.v("DistoX", "Surface " + st1 + " " + st2 );
        SketchSurface surface = new SketchSurface( st1, st2, painter, nv, nt );
        if ( st1.equals( mInfo.st1 ) && st2.equals( mInfo.st2 ) ) mCurrentSurface = surface;
        fromTdrSurface( bis, surface, nv, nt );
        surface.computeBorders();
        mSurfaces.add( surface );
      }

      int njn = fromTdrShort( bis ); // joints
      // Log.v("DistoX", "Joins " + njn );
      for ( k=0; k<njn; ++k ) {
        int what = fromTdrShort( bis ); // must be 5
        String st1 = fromTdrString( bis );
        String st2 = fromTdrString( bis );
        int nv = fromTdrShort( bis );
        int nt = fromTdrShort( bis );
        SketchSurface join = new SketchSurface( st1, st2, painter, nv, nt );
        fromTdrSurface( bis, join, nv, nt );
        mJoins.add( join );
      }
    } catch ( FileNotFoundException e ) {
      // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    // remove repeated names
    // Log.v( "DistoX", "load Tdr3 " + filename + " done" );
    return (missingSymbols == null ) || missingSymbols.isOK();
  }
    
}
