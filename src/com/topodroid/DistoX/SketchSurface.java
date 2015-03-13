/** @file SketchSurface.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: 3D surface 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130224 created 
 * 20130601 started surface-editing ops, mostly determining inner and outer borders
 * 20130804 revised surface-editing ops (cut, stretch, extrude)
 * 20130831 join(s) between surfaces
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Comparator;

import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.PointF;

import android.util.FloatMath;
import android.util.Log;

class SketchSurface extends SketchShot
{
  HashMap< Integer, SketchVertex > mVertices;
  ArrayList< SketchSide > mSides;
  ArrayList< SketchTriangle > mTriangles;
  HashMap< Integer, PointF > mCorners;   // corners of forward surface: index-of-vertex, canvas-point
  int maxkey_vtx;
  // int maxkey_sds;
  private SketchVertex mSelectedVertex;
  
  // DEBUG 
  // void markTriangles( boolean hl )
  // {
  //   for ( SketchTriangle t : mTriangles ) t.highlight = hl; 
  // }

  /** borders are lists of sides with only one triangle,
   *  they are evaluated in computeBorders and are used donly by SketchDxf
   *  they are not saved in the th3 file
   *  they are displayed in the draw method
   */
  ArrayList< SketchBorder >   mBorders;
  ArrayList< SketchVertex >   mInsideVertices; 
  ArrayList< SketchTriangle > mInsideTriangles;
  ArrayList< SketchTriangle > mOutsideTriangles;

  SketchPainter mPainter;
  // SketchTriangle mSelectedTriangle;


  SketchSurface( String s1, String s2, SketchPainter painter )
  {
    super( s1, s2 );
    mVertices   = new HashMap< Integer, SketchVertex >();
    mTriangles  = new ArrayList< SketchTriangle >();
    mCorners    = new HashMap< Integer, PointF >();
    mSides      = new ArrayList<SketchSide>();
    mBorders    = new ArrayList< SketchBorder >();
    mInsideTriangles = new ArrayList< SketchTriangle >();
    mInsideVertices  = new ArrayList< SketchVertex >();
    mPainter    = painter;
    mSelectedVertex = null;
    reset();
  }

  // void dump()
  // {
  //   Log.v("DistoX", "Surface v " + mVertices.size() + " t " + mTriangles.size() );
  // }
        
  int findTrianglesInside( ArrayList<PointF> border )
  {
    mInsideTriangles.clear();
    int ntin = 0;
    for ( SketchTriangle t : mTriangles ) {
      if ( t.isInside( border ) ) {
        t.inside = true;
        ++ntin;
        mInsideTriangles.add( t );
      } else {
        t.inside = false;
      }
    }
    return ntin;
  }

  synchronized void refineAtCenters()
  {
    ArrayList< SketchTriangle > triangles = mTriangles;
    mTriangles = new ArrayList< SketchTriangle >();
    // ArrayList< SketchBorder > bborders   = new ArrayList< SketchBorder >();

    for ( SketchTriangle t : triangles ) {
      int h = addVertex( t.center );
      addTriangle( t.i, t.j, h );
      addTriangle( t.j, t.k, h );
      addTriangle( t.k, t.i, h );
    }
  }
        
  synchronized void refineAtSides()
  {
    ArrayList< SketchTriangle > triangles = mTriangles;
    mTriangles = new ArrayList< SketchTriangle >();
    // ArrayList< SketchBorder > bborders   = new ArrayList< SketchBorder >();

    for ( SketchTriangle t : triangles ) {
      Vector v1 = new Vector( (t.v2.x+t.v3.x)/2, (t.v2.y+t.v3.y)/2, (t.v2.z+t.v3.z)/2 );
      Vector v2 = new Vector( (t.v3.x+t.v1.x)/2, (t.v3.y+t.v1.y)/2, (t.v3.z+t.v1.z)/2 );
      Vector v3 = new Vector( (t.v1.x+t.v2.x)/2, (t.v1.y+t.v2.y)/2, (t.v1.z+t.v2.z)/2 );
      int h1 = addVertex( v1 );
      int h2 = addVertex( v2 );
      int h3 = addVertex( v3 );
      addTriangle( t.i, h3, h2 );
      addTriangle( t.j, h1, h3 );
      addTriangle( t.k, h2, h1 );
      addTriangle(h1, h2, h3 );
    }
  }

  synchronized void refineAtSelectedVertex( )
  { 
    if ( mSelectedVertex == null ) {
      Log.v("DistoX", "refine at selected vertex: null selected veretex");
      return;
    }
    ArrayList< SketchTriangle > triangles = mTriangles;
    mTriangles = new ArrayList< SketchTriangle >();
    for ( SketchTriangle t : triangles ) {
      refineTriangleAtVertex( t, mSelectedVertex, 0.5f, 0.5f, true );
    }
  }
 
  // project on (cos_clino*sin_azi, -cos_clino*cos_azimuth, -sin_clino)
  private float computeProjections( SketchTriangle tri, Sketch3dInfo info )
  {
    SketchVertex v1 = mVertices.get( tri.i );
    SketchVertex v2 = mVertices.get( tri.j );
    SketchVertex v3 = mVertices.get( tri.k );
    if ( v1 == null || v2 == null || v3 == null ) return -999;

    // project on (cos_clino*sin_azi, -cos_clino*cos_azimuth, -sin_clino)
    float z1 = info.worldToSceneOrigin( v1, tri.p1 );
    float z2 = info.worldToSceneOrigin( v2, tri.p2 );
    float z3 = info.worldToSceneOrigin( v3, tri.p3 );

    return (z1+z2+z3)/3;
  }

  private void computeProjections3V( SketchTriangle tri, Vector v1, Vector v2, Vector v3, Sketch3dInfo info )
  {
    // project on (cos_clino*sin_azi, -cos_clino*cos_azimuth, -sin_clino)
    PointF p = new PointF(0,0);
    info.worldToSceneOrigin( v1, p ); tri.p1.x = p.x; tri.p1.y = p.y;
    info.worldToSceneOrigin( v2, p ); tri.p2.x = p.x; tri.p2.y = p.y;
    info.worldToSceneOrigin( v3, p ); tri.p3.x = p.x; tri.p3.y = p.y;
  }

  boolean refineTriangleAtVertex( SketchTriangle t, SketchVertex vv, float t2, float t3, boolean add )
  {
    Vector w1=null, w2=null, w3=null;
    int i=t.i, j=t.j, k=t.k;
    if ( vv == t.v1 ) {
      w1 = t.v1;  i = t.i;
      w2 = t.v2;  j = t.j;
      w3 = t.v3;  k = t.k;
    } else if ( vv == t.v2 ) {
      w1 = t.v2;  i = t.j;
      w2 = t.v3;  j = t.k;
      w3 = t.v1;  k = t.i;
    } else if ( vv == t.v3 ) {
      w1 = t.v3;  i = t.k;
      w2 = t.v1;  j = t.i;
      w3 = t.v2;  k = t.j;
    } else {
      /* not found vertex */
    }
    if ( w1 != null ) {
      Vector v2 = new Vector( w1.x*t2+w2.x*(1-t2), w1.y*t2+w2.y*(1-t2), w1.z*t2+w2.z*(1-t2) );
      Vector v3 = new Vector( w1.x*t3+w3.x*(1-t3), w1.y*t3+w3.y*(1-t3), w1.z*t3+w3.z*(1-t3) );
      int h2 = addVertex( v2 );
      int h3 = addVertex( v3 );
      // PointF q2 = new PointF( p1.x*t2+p2.x*(1-t2), p1.y*t2+p2.y*(1-t2) );
      // PointF q3 = new PointF( p1.x*t3+p3.x*(1-t3), p1.y*t3+p3.y*(1-t3) );
      SketchTriangle tt = addTriangle( i, h2, h3 );
      // computeProjections( tt, info );
      if ( v2.distance( w3 ) > v3.distance( w2 ) ) {
        tt = addTriangle( j, h3, h2 );   //    h2 --- h3
                                         //     | .-' |
                                         //     j --- k
        // computeProjections( tt, info );
        tt = addTriangle( j, k,  h3 );
        // computeProjections( tt, info );
      } else {         
        tt = addTriangle( k, h3, h2 );   //    h2 --- h3
                                         //     | .-' |
                                         //     j --- k
        // computeProjections( tt, info );
        tt = addTriangle( j, k,  h2 );   
        // computeProjections( tt, info );
      }                            
      return true;
    } else if ( add ) {
      SketchTriangle tt = addTriangle(t.i, t.j, t.k );
      // computeProjections( tt, info );
    } 
    return false;
  }    

  boolean refineTriangleAtVertex( SketchTriangle t, SketchVertex vv, Vector v12, Vector v13, Sketch3dInfo info )
  {
    boolean first = true;
    int i=t.i, j=t.j, k=t.k;
    SketchVertex v2 = null, v3 = null;
    if ( vv == t.v1 ) {
      v2 = t.v2;  v3 = t.v3;
      first = ( v12.distance( v3 ) > v13.distance( v2 ) );
      i = t.i; j = t.j; k = t.k;
    } else if ( vv == t.v2 ) {
      v2 = t.v3;  v3 = t.v1;
      first = ( v12.distance( v3 ) > v13.distance( v2 ) );
      i = t.j; j = t.k; k = t.i;
    } else if ( vv == t.v3 ) {
      v2 = t.v1;  v3 = t.v2;
      first = ( v12.distance( v3 ) > v13.distance( v2 ) );
      i = t.k; j = t.i; k = t.j;
    } else {
      /* not found vertex */
      return false;
    }
    int h2 = addVertex( v12 );    
    int h3 = addVertex( v13 );   
    SketchVertex w2 = mVertices.get( h2 );
    SketchVertex w3 = mVertices.get( h3 );
    synchronized( mTriangles ) {
      addTriangle( i, h2, h3, vv, w2, w3, info );
      if ( first ) {
        addTriangle( j, h3, h2, v2, w3, w2, info );
        addTriangle( j, k,  h3, v2, v3, w3, info );
      } else {                          
        addTriangle( k, h3, h2, v3, w3, w2, info );
        addTriangle( j, k,  h2, v2, v3, w2, info );
      }
      mTriangles.remove( t );
    }
    return true;
  }

  // synchronized boolean removeTriangle( SketchTriangle t )
  // {
  //   return mTriangles.remove( t );
  // }

  SketchBorder getBorderAt( String name, DistoXNum num ) 
  {
    Vector s0 = null;
    if ( st1.equals( name ) || st2.equals( name ) ) {
      NumStation st = num.getStation( name );
      s0 = new Vector( st.e, st.s, st.v );
    }
    if ( s0 != null ) {
      float d0 = 10000f;
      SketchBorder b0 = null;
      for ( SketchBorder border : mBorders ) {
        float d = s0.distance( border.getCenter(this) );
        if ( d < d0 ) {
          d0 = d;
          b0 = border;
        }
      }
      return b0;
    }
    return null;
  }

  SketchVertex getSelectedVertex( ) { return mSelectedVertex; }

  void setSelectedVertex( SketchVertex v ) { mSelectedVertex = v; }

  private void reset()
  {
    mVertices.clear();
    mTriangles.clear();
    mSides.clear();
    mBorders.clear();

    maxkey_vtx = -1;
    // maxkey_sds = -1;
  }

  void clearHighlight()
  {
    for ( SketchTriangle t : mTriangles ) t.highlight = false;
  }

  /**   n01 + ------- + n11
   *        | `-._    |
   *        |     `-. |
   *    n02 + - - - - + n12
   *        | `-.     |
   */
  // synchronized void makeExtrude( ArrayList<Vector> pts, ArrayList< Vector > border3d )
  // {
  //   // TODO EXTRUDE
  // }

  class SketchSideComparator implements Comparator< SketchSide >
  {
    @Override
    public int compare( SketchSide lhs, SketchSide rhs ) 
    { 
      return ( lhs.length < rhs.length )? 1 :
             ( lhs.length == rhs.length )? 0 : -1;
    }
  }

  int refineToMaxSide( float max_size )
  {
    for ( SketchTriangle t : mTriangles ) t.splitted = false;
    // boolean repeat = true;
    // int cnt = 0;
    int split = 0; // total number of splits
    SketchSideComparator comp = new SketchSideComparator();
    // while ( repeat && cnt < 1 ) 
    {
      // ++ cnt; 
      // repeat = false;
      computeSidesWithLength();
      // sort sides by the length
      Collections.sort( mSides, comp );
      float len = mSides.get(0).length;
      for ( SketchSide s : mSides ) {
        if ( s.length > len ) {
          Log.v("DistoX", "not decresing side length " + len + " " + s.length );
        }
        len = s.length;
      }
          
      // ArrayList< SketchTriangles > triangles = mTriangles;
      // Log.v( "DistoX", "repeat " + cnt + " sides  " + mSides.size() );

      // float size = 0;
      synchronized( mTriangles ) {
        for ( SketchSide s : mSides ) {
          int k1 = s.v1;
          int k2 = s.v2;
          SketchVertex v1 = mVertices.get( s.v1 );
          SketchVertex v2 = mVertices.get( s.v2 );
          float d = v1.distance( v2 );
          // if ( d > size ) size = d;
          if ( d > max_size ) {
            SketchTriangle t1 = s.t1;
            SketchTriangle t2 = s.t2;
            if ( t1 != null && t2 != null && ! t1.splitted && ! t2.splitted ) {
              int k13 = -1, k23 = -1;
              boolean f1 = false, f2 = false;
                     if ( k1 == t1.i && k2 == t1.j ) {
                k13 = t1.k;
                f1 = true;
              } else if ( k1 == t1.j && k2 == t1.i ) {
                k13 = t1.k;
                f1 = false;
              } else if ( k1 == t1.j && k2 == t1.k ) {
                k13 = t1.i;
                f1 = true;
              } else if ( k1 == t1.k && k2 == t1.j ) {
                k13 = t1.i;
                f1 = false;
              } else if ( k1 == t1.k && k2 == t1.i ) {
                k13 = t1.j;
                f1 = true;
              } else if ( k1 == t1.i && k2 == t1.k ) {
                k13 = t1.j;
                f1 = false;
              }
                     if ( k1 == t2.i && k2 == t2.j ) {
                k23 = t2.k;
                f2 = true;
              } else if ( k1 == t2.j && k2 == t2.i ) {
                k23 = t2.k;
                f2 = false;
              } else if ( k1 == t2.j && k2 == t2.k ) {
                k23 = t2.i;
                f2 = true;
              } else if ( k1 == t2.k && k2 == t2.j ) {
                k23 = t2.i;
                f2 = false;
              } else if ( k1 == t2.k && k2 == t2.i ) {
                k23 = t2.j;
                f2 = true;
              } else if ( k1 == t2.i && k2 == t2.k ) {
                k23 = t2.j;
                f2 = false;
              }
              if ( k13 >= 0 && k23 >= 0 ) { // split side
                Vector w = new Vector( (v1.x+v2.x)/2, (v1.y+v2.y)/2, (v1.z+v2.z)/2 );
                // make triangles 
                int kw = addVertex( w );
                if ( f1 ) { // t1: v1 --(w)--> v2 ---> v13 ==> v1-w-v13 and w-v2-v13
                  addTriangle( kw, k13, k1 );
                  addTriangle( kw, k2, k13 );
                } else { // t1: v2 --> v1 --> v13
                  addTriangle( kw, k13, k2 );
                  addTriangle( kw, k1, k13 );
                }
                if ( f2 ) { 
                  addTriangle( kw, k23, k1 );
                  addTriangle( kw, k2, k23 );
                } else { 
                  addTriangle( kw, k23, k2 );
                  addTriangle( kw, k1, k23 );
                }
                t1.splitted = true;
                t2.splitted = true;
                // repeat = true;
                ++split;
              }
            }
          }
        }
        // Log.v("DistoX", "split " + split + " size " + size );
      }
    }
    ArrayList< SketchTriangle > triangles = new ArrayList< SketchTriangle >();
    for ( SketchTriangle t : mTriangles ) {
      if ( ! t.splitted ) triangles.add( t );
    }
    synchronized( mTriangles ) {
      mTriangles = triangles;
    }
    return split;
  }

  synchronized void makeCut( )
  {
    // remove inside triangles
    // ArrayList< SketchVertex > outerVertices = new ArrayList<SketchVertex>();
    // int ntin = 0;
    // for ( SketchTriangle t : mTriangles ) {
    //   if ( ! t.inside ) {
    //     if ( ! outerVertices.contains( t.v1 ) ) outerVertices.add( t.v1 );
    //     if ( ! outerVertices.contains( t.v2 ) ) outerVertices.add( t.v2 );
    //     if ( ! outerVertices.contains( t.v3 ) ) outerVertices.add( t.v3 );
    //   } else {
    //     ++ ntin;
    //   }
    // }
    // Log.v( "DistoX", "make cut: in triangles  " + ntin + " / " + mTriangles.size() );
    // Log.v( "DistoX", "make cut: outer vertices " + outerVertices.size() + " / " + mVertices.size() );
    

    // HashMap<SketchVertices> vertices = mVertices;
    ArrayList<SketchTriangle> triangles = mTriangles;
    mVertices   = new HashMap< Integer, SketchVertex >();
    mTriangles  = new ArrayList< SketchTriangle >();
    synchronized( mTriangles ) {
      for ( SketchTriangle t : triangles ) {
        if ( ! t.inside ) {
          int ka = addVertex( t.v1 );
          int kb = addVertex( t.v2 );
          int kc = addVertex( t.v3 );
          addTriangle( ka, kb, kc );
        }
      }
    }
  }

  // private float determinant( PointF p0, PointF p1, PointF p2 )
  // {
  //   return p1.x*p2.y - p1.y*p2.x + p0.x*p1.y - p0.y*p1.x + p2.x*p0.y - p2.y*p0.x;
  // }

  // --------------------------------------------------------
  // borders

  private SketchSide getSide( int v1, int v2 )
  {
    for ( SketchSide side : mSides ) {
      if ( side.v1 == v1 && side.v2 == v2 ) return side;
      if ( side.v1 == v2 && side.v2 == v1 ) return side;
    }
    return null;
  }

  /** called by computeSides() below
   */
  private void checkSide( SketchTriangle tri, int i, int j, Vector v1, Vector v2 ) 
  {
    SketchSide side = getSide( i, j );
    if ( side == null ) {
      int idx = mSides.size();
      side = new SketchSide( this, idx, i, j ); 
      if ( v1 != null && v2 != null ) side.length = v1.distance( v2 );
      mSides.add( side );
      mSides.get( idx ).t1 = tri;
    } else {
      if ( side.t1 == null ) {
        Log.e("DistoX", "null triangle-1 side");
        side.t1 = tri;
      } else if ( side.t2 == null ) {
        side.t2 = tri;
      } else {
        Log.e("DistoX", "multi T side " + side.t1.i + " " + side.t1.j + " " + side.t1.k + "  " + side.t2.i + " " + side.t2.j + " " + side.t2.k + "  " + tri.i + " " + tri.j + " " + tri.k );
      }
    }
  }      

  /** called by ComputeBorders, makeOuterTriangles, and makeExtrude
   */
  private void computeSides( )
  {
    mSides.clear();
    for ( SketchTriangle t : mTriangles ) {
      if ( ! t.splitted ) {
        checkSide( t, t.i, t.j, null, null );
        checkSide( t, t.j, t.k, null, null );
        checkSide( t, t.k, t.i, null, null );
      }
    }
  }

  private void computeSidesWithLength()
  {
    mSides.clear();
    for ( SketchTriangle t : mTriangles ) {
      if ( ! t.splitted ) {
        checkSide( t, t.i, t.j, t.v1, t.v2 );
        checkSide( t, t.j, t.k, t.v2, t.v3 );
        checkSide( t, t.k, t.i, t.v3, t.v1 );
      }
    }
  }

  /** called by SkecthModel
   * compute the borders of a surface.
   * a border is a chain of sides that have only one triangle
   */
  void computeBorders() 
  {
    computeSides( );
    ArrayList< SketchSide > tmp = new ArrayList<SketchSide>();
    for ( SketchSide s : mSides ) {
      if ( s.t2 == null ) { tmp.add( s ); }
    }
    mBorders.clear();
    // Log.v("DistoX", "compute borders ns " + tmp.size() );
    while ( tmp.size() > 0 ) {
      SketchBorder brd = new SketchBorder();
      SketchSide s1 = tmp.get( 0 );
      tmp.remove( s1 );
      brd.add( s1 );
      int v1 = s1.v1;
      int v2 = s1.v2;
      while ( v2 != v1 ) {
        boolean found = false;
        for ( SketchSide s2 : tmp ) {
          if ( s2.v1 == v2 || s2.v2 == v2 ) {
            tmp.remove( s2 );
            brd.add( s2 );
            v2 = ( s2.v1 == v2 )? s2.v2 : s2.v1; // the other vertex of s2 different from v2
            found = true;
            break;
          }
        }
        if ( ! found ) {
          for (SketchSide s2 : tmp ) {
            if ( s2.v1 == v1 || s2.v2 == v1 ) {
              tmp.remove( s2 );
              brd.sides.add( 0, s2 );
              v1 = ( s2.v1 == v1 )? s2.v2 : s2.v1;
              found = true;
              break;
            }
          }
        }
        if ( ! found ) {
          // Log.v("DistoX", "broken border length " + brd.sides.size() );
          break;
        }
      }
      // StringWriter sw = new StringWriter();
      // PrintWriter pw = new PrintWriter( sw );
      // for ( SketchSide s : brd.sides ) {
      //   pw.format("%d-%d ", s.v1, s.v2 );
      // }
      // Log.v("DistoX", sw.getBuffer().toString() );
      mBorders.add( brd );
    }
    Log.v("DistoX", "compute border surface nr. border " + mBorders.size() );
  }

  // --------------------------------------------------------
  // VERTICES

  /** add a vertex with a given index ( for loadTh3 )
   * @param n    vertex index
   * @reurn the new-vertex index
   */
  int addVertex( int n, float x, float y, float z ) {
    SketchVertex v = new SketchVertex( this, n, x, y, z );
    mVertices.put( n, v );
    if ( n > maxkey_vtx ) maxkey_vtx = n;
    return n;
  }

  /** add a vertex giving the 3D vector
   * @param v  input 3D vector
   */
  private int addVertex( Vector v ) { return addVertex( v.x, v.y, v.z ); }

  /** add a vertex given X,Y,Z (or return an already existing close vertex)
   * @return the vertex index
   */
  private int addVertex( float x, float y, float z )
  {
    for ( SketchVertex v : mVertices.values() ) {
      if ( Math.abs( x - v.x ) < 0.1 && Math.abs( y - v.y ) <  0.1 && Math.abs( z - v.z ) < 0.1 ) {
        return v.index;
      }
    }
    // int n = mVertices.size();
    ++ maxkey_vtx;
    SketchVertex v = new SketchVertex( this, maxkey_vtx, x, y, z );
    mVertices.put( maxkey_vtx, v );
    return maxkey_vtx;
  }

  /** get a vertex given the  index
   * @param index    vertex index
   * @return the vertex
   * used by SketchBorder
   */
  SketchVertex getVertex( int index ) 
  {
    return mVertices.get( index );
  }

  // --------------------------------------------------------
  // triangles
  final float r2_2 = FloatMath.sqrt( 0.5f ); // sqrt(2)/2

  /**
   * @return the new vertex index
   */
  // private int interpolateVertices( SketchVertex v1, SketchVertex v2, float a, float r, Sketch3dInfo info )
  // {
  //   float b = 1 - a;
  //   // float r = 1/FloatMath.sqrt( a*a + b*b );
  //   Vector v = new Vector( a*v1.x+b*v2.x - info.station1.e,
  //                          a*v1.y+b*v2.y - info.station1.s,
  //                          a*v1.z+b*v2.z - info.station1.v );
  //   float nv = (v.x*info.sin_alpha + v.y*info.cos_alpha)*info.cos_gamma + v.z*info.sin_gamma;
  //   Vector u = new Vector( nv*info.sin_alpha*info.cos_gamma, nv*info.cos_alpha*info.cos_gamma, nv*info.sin_gamma );
  //   return addVertex( info.station1.e + u.x + r*(v.x - u.x),
  //                     info.station1.s + u.y + r*(v.y - u.y),
  //                     info.station1.v + u.z + r*(v.z - u.z) );
  // }

  // from loadTh3 Therion
  // void addSide( int index, int v1, int v2 )
  // {
  //   SketchVertex w1 = mVertices.get( v1 );
  //   SketchVertex w2 = mVertices.get( v2 );
  //   if ( w1 == null || w2 == null ) {
  //     Log.e("DistoX", "ERROR side without vertex: " + v1 + " " + v2 );
  //   }
  //   mSides.put( index, new SketchSide( this, index, v1, v2 ) );
  //   if ( index > maxkey_sds ) maxkey_sds = index;
  // }

  // SketchSide getSide( int i, int j )
  // {
  //   for ( SketchSide s : mSides.values() ) {
  //     if ( s.hasVertices( i, j ) ) return s;
  //   }
  //   ++ maxkey_sds;
  //   SketchSide s1 = new SketchSide( this, maxkey_sds, i, j );
  //   mSides.put( maxkey_sds, s1 );
  //   return s1;
  // }

  // SketchTriangle addTriangle3V( Vector v1, Vector v2, Vector v3 )
  // {
  //   int ka = addVertex( t.mA );
  //   int kb = addVertex( t.mB );
  //   int kc = addVertex( t.mC );
  //   addTriangle( ka, kb, kc );
  // }

  // add triangle
  SketchTriangle addTriangle( int i, int j, int k )
  {
    return addTriangle( i, j, k, mVertices.get(i), mVertices.get(j), mVertices.get(k) );
  }

  private SketchTriangle addTriangle( int i, int j, int k, SketchVertex v1, SketchVertex v2, SketchVertex v3 )
  {
    SketchTriangle t = null;
    if ( i != j && j != k && k != i ) {
      t = new SketchTriangle( this, i, j, k, v1, v2, v3 );
      mTriangles.add( t );
    }
    return t;
  }

  private SketchTriangle addTriangle( int i, int j, int k, SketchVertex v1, SketchVertex v2, SketchVertex v3, Sketch3dInfo info )
  {
    SketchTriangle t = null;
    if ( i != j && j != k && k != i ) {
      t = new SketchTriangle( this, i, j, k, v1, v2, v3 );
      computeProjections3V( t, v1, v2, v3, info );
      mTriangles.add( t );
    }
    return t;
  }
  // private float equilaterity( int i, int j, int k )
  // {
  //   SketchVertex v1 = mVertices.get( i );
  //   // SketchVertex v2 = mVertices.get( j );
  //   SketchVertex v3 = mVertices.get( k );
  //   return v1.distance( v3 );
  // }

  // -----------------------------------------------------------

  /** The angle around the unit vector of the shot
   *              +-----------> unit
   *             / \
   *        dir2/   \ dir1
   *           v     v
   *
   *  dir2 = unit ^ dir1 The angles are in the (dir1,dir2) plane:
   *
   *          +-------> dir1
   *          |\
   *          | \ X
   *     dir2 v 
   * sin( alpha ) = X * dir1
   * cos( alpha ) = X * dir2
   */
  private int computeAngles( ArrayList<Vector> pts, Vector base, Vector unit, Vector dir1, Vector dir2, float[] angle )
  {
    int ns = pts.size();
    // Vector retn[] = new Vector[ns];
    Vector retn = new Vector();
    for ( int n=0; n<ns; ++n ) {
      Vector v = pts.get(n);
      retn.x = v.x - base.x;
      retn.y = v.y - base.y;
      retn.z = v.z - base.z;
      float p = retn.dot( unit );
      retn.x -= p * unit.x;
      retn.y -= p * unit.y;
      retn.z -= p * unit.z;
      retn.Normalized();
      angle[n] = (float)Math.atan2( retn.dot(dir1), retn.dot(dir2) );
    }
    float a = angle[0] - angle[ns-1];
    for ( int n=1; n<ns; ++n ) {
      float da = angle[n] - angle[n-1];
      a += ( da > 1.57 )? da - 3.14 : ( da < -1.57 ) ? da + 3.14 : da;
    }
    // Log.v("DistoX", "angle around " + a);

    // if (a > 0.0f) { // revert
    //   int n0 = 0;
    //   int n1 = ns - 1;
    //   while ( n0 < n1 ) {
    //     float aa = angle[n0]; angle[n0] = angle[n1]; angle[n1] = aa;
    //     Vector v0 = pts.get( n0 ); 
    //     Vector v1 = pts.get( n1 ); 
    //     pts.set( n0, v1 );
    //     pts.set( n1, v0 );
    //     ++ n0;
    //     -- n1;
    //   }
    // }
    // return 1;
    return ( a < 0.0f )? ns - 1 : 1;
  }
  
  private void addTriangles(int k00, int k10, int k11, int nt) 
  {
    if ( nt <= 1 ) {   
      addTriangle( k00, k10, k11 );
    } else {
      SketchVertex v00 = mVertices.get( k00 );
      SketchVertex v10 = mVertices.get( k10 );
      SketchVertex v11 = mVertices.get( k11 );
      Vector dv0 = new Vector( (v10.x - v00.x )/nt, (v10.y - v00.y)/nt, (v10.z - v00.z)/nt );
      Vector dv1 = new Vector( (v11.x - v00.x )/nt, (v11.y - v00.y)/nt, (v11.z - v00.z)/nt );
      for ( int n = nt-1; n > 0; --n ) {
        int k0 = addVertex( new Vector( v00.x + dv0.x * n, v00.y + dv0.y * n, v00.z + dv0.z * n ) );
        int k1 = addVertex( new Vector( v00.x + dv1.x * n, v00.y + dv1.y * n, v00.z + dv1.z * n ) );
        addTriangle( k0, k10, k11 );
        addTriangle( k0, k11, k1 );
        k10 = k0;
        k11 = k1;
      }
      addTriangle( k00, k10, k11 );
    }
  }

  /** check if two arc-segments on the (unit) sphere intersect
   * (p1,p2) first segment
   * (q1,q2) second segment
   * @param p1   first unit vector of the first pair
   * @param p2   second unit vector of the first pair
   * @param q1   first unit vector of the second pair
   * @param q2
   */
  private boolean arcIntersect( Vector p1, Vector p2, Vector q1, Vector q2 )
  {
    Vector np = p1.cross( p2 );              // "normal" to the plane (p1,p2)
    Vector nq = q1.cross( q2 );              // "normal" to the plane (q1,q2)
    Vector nn = np.cross( nq );
    nn.Normalized(); // intersection of the planes (p1,p2) (q1,q2)
    if ( p1.cross( nn ).dot( p1.cross( p2 ) ) < 0 ) nn.times( -1 );
    if ( p1.cross( nn ).dot( nn.cross( p2 ) ) < 0 ) return false;
    if ( q1.cross( nn ).dot( q1.cross( q2 ) ) < 0 ) return false;
    if ( q1.cross( nn ).dot( nn.cross( q2 ) ) < 0 ) return false;
    return true;
  }

  /** a pair of indices
   */
  private class VPair
  {
    int v1, v2;

    VPair( int x1, int x2 )
    {
      v1 = x1;
      v2 = x2;
    }
  };

  /** check if the array of index-pairs contains a given pair
   * @param n1   first index of the pair
   * @param n2   second index of the pair
   * @param vts  array of pairs
   * @param nv   number of pairs in the array
   */
  private boolean hasPair( int n1, int n2, VPair[] vts, int nv )
  {
    for ( int v=0; v<nv; ++v ) {
      if (  ( vts[v].v1 == n1 && vts[v].v2 == n2 )
         || ( vts[v].v1 == n2 && vts[v].v2 == n1 ) ) return true;
    }
    return false;
  }

  private final static float MAX_DIST  = 10f; // FIXME > 2*PI+eps 
  private final static float MAX_DIST2 =  5f; // MAX_DIST / 2

  /**
   * @param ss set of cross-sections at the station
   * @param info
   * @param splays splays at the station
   * @param center station 3D coords
   */
  // void makeJoinTriangles( SketchSectionSet ss, Sketch3dInfo info, ArrayList< SketchFixedPath > splays, Vector center )
  // {
  //   int ms = ss.size();
  //   if ( ms < 2 ) return;
  //   int NP = 0;
  //   for ( int k=0; k<ms; ++k ) {
  //     int np0 = ss.getSection(k).mLine.points.size();
  //     NP += np0;
  //     // Log.v("DistoX", "Sections " + k + " points " + np0 );
  //   }
  //     
  //   int [] off = new int[ ms+1 ];
  //   off[0] = 0;
  //   int np = 0;
  //   Vector[] pts = new Vector[ NP ];

  //   int nv = 0;
  //   int NV = 2*NP + 3*(ms-2);
  //   VPair[] vts = new VPair[ NV ]; // vertex pairs connected by a side

  //   for ( int k=0; k<ms; ++k ) {
  //     SketchSection s = ss.getSection( k );
  //     ArrayList<Vector> pts0 = s.mLine.points;
  //     int n0 = pts0.size();
  //     for ( int n=0; n<n0; ++n ) {
  //       Vector v0 = pts0.get(n);
  //       addVertex( np, v0.x, v0.y, v0.z ); // prepare the surface vertices
  //   
  //       v0.sub( center );
  //       pts[ np ] = v0.getUnitVector();
  //       // Log.v("DistoX", "P" + np + ": " + pts[np].x + " " + pts[np].y + " " + pts[np].z );
  //       ++ np;
  //     }
  //     off[ k + 1 ] = np;
  //   }
  //   // Log.v("DistoX", "Nr. sections " + ms + " NP " + NP + " np " + np + " nv " + nv );

  //   float[] dist = new float[ np * np ];
  //   for ( int m=0; m<ms; ++m ) {
  //     for ( int k = off[m]; k < off[m+1]; ++k ) {
  //       for ( int h = off[m]; h < off[m+1]; ++h ) {
  //         dist[ k*np + h ] = MAX_DIST;
  //       }
  //     }
  //     for ( int n=m+1; n<ms; ++n ) {
  //       for ( int k = off[m]; k < off[m+1]; ++k ) {
  //         int nk = off[m+1] - off[m];
  //         for ( int h = off[n]; h < off[n+1]; ++h ) {
  //           int nh = off[n+1] - off[n];
  //           boolean ok = true;
  //           for ( int m1=0; ok && m1<ms; ++m1 ) {
  //             int k1 = off[m1+1] - 1;
  //             for ( int h1=off[m1]; h1 < off[m1+1]; ++h1 ) {
  //               if ( h != h1 && h != k1 && k != h1 && h != k1 &&
  //                    arcIntersect( pts[k], pts[h], pts[k1], pts[h1] ) ) {
  //                 ok = false;
  //                 break;
  //               }
  //               k1 = h1;
  //             }
  //           }
  //           // if ( ok && arcIntersect( pts[k], pts[h], 
  //           //                          pts[off[m] + (k-off[m]+nk/4)%nk], pts[off[m] + (k-off[m]+3*nk/4)%nk] ) ) {
  //           //   ok = false;
  //           // }
  //           // if ( ok && arcIntersect( pts[k], pts[h], 
  //           //                          pts[off[n] + (h-off[n]+nh/4)%nh], pts[off[n] + (h-off[n]+3*nh/4)%nh] ) ) {
  //           //   ok = false;
  //           // }
  //           if ( ok ) {
  //             dist[ k*np + h ] = (float) Vector.arc_distance( pts[k], pts[h] );
  //             dist[ h*np + k ] = dist[ k*np + h ]; // not necessary
  //           //  Log.v("DistoX", "dist. " + k + "-" + h + " " + dist[k*np+h ] );
  //           } else {
  //             dist[ k*np + h ] = MAX_DIST;
  //             dist[ h*np + k ] = MAX_DIST;
  //           }
  //         }
  //       }
  //     }
  //   }

  //   for ( int m0=0; m0<ms; ++m0 ) {
  //     for ( int h0 = off[m0]; h0 < off[m0+1]; ++h0 ) {
  //       int k0 = (h0>off[m0])? h0 - 1 : off[m0+1] - 1;
  //       // Vector p1 = pts[ k0 ];
  //       // Vector p2 = pts[ h0 ];
  //       // for ( int m=0; m<ms; ++m ) {
  //       //   for ( int n=m+1; n<ms; ++n ) {
  //       //     for ( int k = off[m]; k < off[m+1]; ++k ) {
  //       //       if ( k == k0 ) continue;
  //       //       for ( int h = off[n]; h < off[n+1]; ++h ) { // NOTE h > k
  //       //         if ( h == h0 ) continue;
  //       //         if ( dist[ k*np+h ] < MAX_DIST2 && arcIntersect( p1, p2, pts[k], pts[h] ) ) {
  //       //           // Log.v("DistoX", "arc-intersect " + k0 + "-" + h0 + " " + k + "-" + h );
  //       //           dist[ k*np + h ] = MAX_DIST;
  //       //           dist[ h*np + k ] = MAX_DIST;
  //       //         }
  //       //       }
  //       //     }
  //       //   }
  //       // }
  //       vts[ nv ] = new VPair( k0, h0 );
  //       ++nv;
  //     }
  //   }
  //   while ( nv < NV ) {
  //     float dmin = MAX_DIST2;
  //     int kmin = -1;
  //     int hmin = -1; 
  //     for ( int m=0; m<ms; ++m ) {
  //       for ( int n=m+1; n<ms; ++n ) {
  //         for ( int k = off[m]; k < off[m+1]; ++k ) {
  //           for ( int h = off[n]; h < off[n+1]; ++h ) {
  //             if ( dist[ k*np + h ] < dmin ) {
  //               dmin = dist[ k*np + h ];
  //               kmin = k;
  //               hmin = h;
  //             }
  //           }
  //         }
  //       }
  //     }
  //     if ( kmin < 0 ) break; // NOTE hmin > kmin

  //     Vector p1 = pts[ kmin ];
  //     Vector p2 = pts[ hmin ];
  //     for ( int m=0; m<ms; ++m ) {
  //       for ( int n=m+1; n<ms; ++n ) {
  //         for ( int k = off[m]; k < off[m+1]; ++k ) {
  //           if ( k == kmin ) continue;
  //           for ( int h = off[n]; h < off[n+1]; ++h ) { // NOTE h > k
  //             if ( h == hmin ) continue;
  //             if ( dist[ k*np+h ] < MAX_DIST2 && arcIntersect( p1, p2, pts[k], pts[h] ) ) {
  //               // Log.v("DistoX", "arc-intersect " + kmin + "-" + hmin + " " + k + "-" + h );
  //               dist[ k*np + h ] = MAX_DIST;
  //               dist[ h*np + k ] = MAX_DIST;
  //             }
  //           }
  //         }
  //       }
  //     }
  //     dist[ kmin*np + hmin ] = MAX_DIST;
  //     dist[ hmin*np + kmin ] = MAX_DIST;
  //     // Log.v("DistoX", "pair " + nv + ": " + kmin + " " + hmin );
  //     vts[ nv ] = new VPair( kmin, hmin );
  //     ++nv;
  //   }
  //   // Log.v("DistoX", "Nr. vertices " + np + " Nr. sides " + nv + " Left over:");
  //   // for ( int k =0; k<off[ms]; ++k ) { // DEBUG log
  //   //   for ( int h =0; h<off[ms]; ++h ) {
  //   //     if ( dist[k*np+h] < MAX_DIST2 ) Log.v("DistoX", k + "-" + h + " " + dist[k*np+h] );
  //   //   }
  //   // }

  //   // now make triangles
  //   int nt = 0;
  //   for ( int n1 = 0; n1 < NP; ++n1 ) {
  //     for ( int n2 = n1+1; n2 < NP; ++n2 ) {
  //       if ( hasPair( n1, n2, vts, nv ) ) {
  //         for ( int n3 = n2+1; n3 < NP; ++n3 ) {
  //           if ( hasPair( n2, n3, vts, nv ) && hasPair( n3, n1, vts, nv ) ) {
  //             if ( Vector.triple_product( pts[n1], pts[n2], pts[n3] ) > 0 ) {
  //               addTriangle( n1, n2, n3 );
  //             } else {
  //               addTriangle( n1, n3, n2 );
  //             }
  //             ++ nt;
  //           }
  //         }
  //       }
  //     }
  //   }
  //   // Log.v("DistoX", "Nr. triangles " + nt );
  // }

  void makeTriangles( Sketch3dInfo info, ConvexHull hull ) 
  {
    Vector unit = info.shotUnit();    // shot unit-vector
    ArrayList< Triangle > tri = hull.mTri;
    ArrayList< Vector > pts = new ArrayList<Vector>();
    for ( Triangle t : tri ) {
      // Triangle has vertces mA mB mC and outgoing normal mN 
      int ka = addVertex( t.mA );
      int kb = addVertex( t.mB );
      int kc = addVertex( t.mC );
      addTriangle( ka, kb, kc );
    }
    computeBorders();
  }

  // void makeTriangles( SketchSectionSet ss, Sketch3dInfo info, ArrayList< SketchFixedPath > splays1,
  //                                                             ArrayList< SketchFixedPath > splays2 )
  // {
  //   int ms = ss.size();
  //   if ( ms < 2 ) return;

  //   int type = ss.mType;

  //   Vector unit = info.shotUnit();    // shot unit-vector
  //   Vector dir1 = new Vector(0,0,0);  // first orthogonal unit-vector
  //   if ( Math.abs( unit.x ) > Math.abs( unit.y ) ) {
  //     if ( Math.abs( unit.y ) > Math.abs( unit.z ) ) { // x > y > z
  //       dir1.x = unit.y;
  //       dir1.y = -unit.x;
  //     } else { // x > z > y or z > x > y
  //       dir1.x = -unit.z;
  //       dir1.z = unit.x;
  //     }
  //   } else {
  //     if ( Math.abs( unit.x ) > Math.abs( unit.z ) ) { // y > x > z
  //       dir1.x = unit.y;
  //       dir1.y = -unit.x;
  //     } else { // y > z > x  or z > y > x
  //       dir1.y = unit.z;
  //       dir1.z = -unit.y;
  //     }
  //   }
  //   dir1.Normalized();
  //   Vector dir2 = unit.cross(dir1);  // second orthogonal unit-vector

  //   Vector v0, v1, v2;
  //   SketchSection s0 = ss.getSection(0);
  //   ArrayList<Vector> pts0 = s0.mLine.points;
  //   int ms0 = pts0.size();
  //   float angle0[] = new float[ms0];
  //   int dm0 = computeAngles( pts0, s0.mBasePoint, unit, dir1, dir2, angle0 );
  //   int m0start = 0;
  //   for ( int m0=0; m0<ms0; ++m0 ) if ( angle0[m0] < angle0[m0start] ) m0start = m0;
  //   // int dm0 = ( angle0[m0start + 1 + ms0/5] < 0 )? 1 : ms0 - 1;

  //   for (int m=1; m<ms; ++m ) {
  //     SketchSection s1 = ss.getSection(m);
  //     ArrayList<Vector> pts1 = s1.mLine.points;
  //     int ns1 = pts1.size();
  //     float angle1[] = new float[ns1];
  //     int dn1 = computeAngles( pts1, s1.mBasePoint, unit, dir1, dir2, angle1 );
  //     int n1start = 0;
  //     for ( int n1=0; n1<ns1; ++n1 ) if ( angle1[n1] < angle1[n1start] ) n1start = n1;
  //     // int dn1 = ( angle1[n1start + 1 + ns1/5] < 0 )? 1 : ns1 - 1;

  //     float len = s1.mBasePoint.minus( s0.mBasePoint ).Length();
  //     int nt = 1 + (int)(len / 0.5f); // number of triangle per side
  //     // Log.v( "DistoX", "nt " + nt + " len " + len + " m0 " + m0start + "/" + ms0 + " n1 " + n1start + "/" + ns1 );

  //     int m00 = m0start;
  //     int n10 = n1start;
  //     int m01 = (m0start + dm0)%ms0;
  //     int n11 = (n1start + dn1)%ns1;

  //     boolean invert = false; // (ms0 != 1) ^ (ns1 != 1);

  //     int k00 = addVertex( pts0.get( m0start ) );  //   k00 +       + k10
  //     int k01 = addVertex( pts0.get( m01 ) );    //       |       |
  //     int k10 = addVertex( pts1.get( n1start ) );  //   k01 +       + k11
  //     int k11 = addVertex( pts1.get( n11 ) );    //       |       |

  //     int k0start = k00;
  //     int k1start = k10;

  //     boolean do0 = true;
  //     boolean do1 = true;
  //     while ( do0 || do1 ) {
  //       if ( ! do0 ) {
  //         if ( invert ) {
  //           addTriangles(k00, k10, k11, nt);   
  //         } else {
  //           addTriangles(k00, k11, k10, nt);   
  //         }
  //         k10 = k11;                 
  //         n10 = n11;
  //         if ( k10 == k1start ) break;
  //         n11 = ( n11 + dn1 ) % ns1;  
  //         k11 = addVertex( pts1.get( n11 ) );
  //       } else if ( ! do1 ) {
  //         if ( invert ) {
  //           addTriangles(k10, k01, k00, nt);
  //         } else {
  //           addTriangles(k10, k00, k01, nt);
  //         }
  //         k00 = k01;
  //         m00 = m01;
  //         if ( k00 == k0start ) break;
  //         m01 = ( m01 + dm0 ) % ms0;
  //         k01 = addVertex( pts0.get( m01 ) );
  //       } else {
  //         if ( angle1[n11] < angle0[m01] ) {
  //           if ( invert ) {
  //             addTriangles(k00, k10, k11, nt);
  //           } else {
  //             addTriangles(k00, k11, k10, nt);      //   k00 +------+ k10
  //           }                                  //       |      |
  //           k10 = k11;                         //       +      + k11
  //           n10 = n11;
  //           if ( k10 == k1start ) {
  //             do1 = false;
  //           } else {
  //             n11 = ( n11 + dn1 ) % ns1;   
  //             k11 = addVertex( pts1.get( n11 ) );
  //           }
  //         } else {
  //           if ( invert ) {
  //             addTriangles(k10, k01, k00, nt);
  //           } else {
  //             addTriangles(k10, k00, k01, nt);        //  k00 +------+ k10
  //           }
  //           k00 = k01;                         //  k01 +      +
  //           m00 = m01;
  //           if ( k00 == k0start ) {
  //             do0 = false;
  //           } else {
  //             m01 = ( m01 + dm0 ) % ms0;           //      |      |
  //             k01 = addVertex( pts0.get( m01 ) );
  //           }
  //         }
  //       } 
  //     }
  //     s0    = s1;
  //     pts0  = pts1;
  //     ms0   = ns1;
  //     dm0   = dn1;
  //     m0start = n1start;
  //     angle0 = angle1;
  //   }

  //   if ( TopoDroidApp.mSketchUsesSplays ) { // take into account splays
  //     for ( SketchFixedPath s : splays1 ) {
  //       // s.mLine.points is an array of Vector
  //       // if ( s.mLine.points.size() != 2 ) Log.v("DistoX", "splays at 1 pts " + s.mLine.points.size() );
  //       v1 = s.mLine.points.get( 0 );
  //       v2 = s.mLine.points.get( 1 );
  //       for ( SketchTriangle tri : mTriangles ) {
  //         v0 = tri.intersection( v1, v2 );
  //         if ( v0 != null ) {
  //           // Log.v("DistoX", "splay1 intersect at " + v0.x + " " + v0.y + " " + v0.z );
  //           tri.shiftVertices( v0 );
  //         }
  //       }
  //     }
  //     for ( SketchFixedPath s : splays2 ) {
  //       // if ( s.mLine.points.size() != 2 ) Log.v("DistoX", "splays at 2 pts " + s.mLine.points.size() );
  //       v1 = s.mLine.points.get( 0 );
  //       v2 = s.mLine.points.get( 1 );
  //       for ( SketchTriangle tri : mTriangles ) {
  //         v0 = tri.intersection( v1, v2 );
  //         if ( v0 != null ) {
  //           // Log.v("DistoX", "splay2 intersect at " + v0.x + " " + v0.y + " " + v0.z );
  //           tri.shiftVertices( v0 );
  //         }
  //       }
  //     } 
  //   }

  //   computeBorders();
  // }

  /** make triangles. the triangles border are oriented:
   *
   *     n1 * -- (k-1)<--(k) -- ... -- *
   *                |    /|
   *                |   / |
   *                v  /  ^
   *                | /   |
   *                |/    |
   *     n2 * -- (k-1)-->(k) -- ... -- *
   */
  void makeTriangles( ArrayList<Vector> pts, int npts )
  {
    int nn = pts.size();
    int[] idx = new int[ nn ];
    int k = 0;
    for ( Vector p : pts ) {
      idx[k] = addVertex( p );
      ++k;
    }
    nn = nn/npts - 1;
    for ( int n = 0; n < nn; ++n ) {
      int n1 = n * npts;
      int n2 = (n+1) * npts;
      k = 0;
      addTriangle( idx[n1+npts-1], idx[n2+npts-1], idx[n1+0] );
      addTriangle( idx[n1+0],      idx[n2+npts-1], idx[n2+0] );
      for ( ++k; k<npts; ++k ) {
        addTriangle( idx[n1+k-1], idx[n2+k-1], idx[n1+k] );
        addTriangle( idx[n1+k],   idx[n2+k-1], idx[n2+k] );
      }
    }
    // Log.v("DistoX", "nr. triangles [2] " + mTriangles.size() );
  }

  // select a triangle that contains the (x,y) scene point
  SketchTriangle selectTriangleAt( float x, float y, Sketch3dInfo info, SketchTriangle tri )
  {
    // Log.v("DistoX", "SketchSurface::selectTriangleAt() " + x + " " + y );
    if ( tri != null ) {
      if ( tri.contains( x, y ) > 0 ) return tri;
      // else try the sides
      // SketchTriangle tri2 = mSides.get(tri.sjk).otherTriangle( tri );
      // if ( tri2 != null && tri2.contains(x,y) > 0 ) return tri2;
      // tri2 = mSides.get(tri.ski).otherTriangle( tri );
      // if ( tri2 != null && tri2.contains(x,y) > 0 ) return tri2;
      // tri2 = mSides.get(tri.sij).otherTriangle( tri );
      // if ( tri2 != null && tri2.contains(x,y) > 0 ) return tri2;
    }

    SketchTriangle ret = null;
    float d0 = 0.0f;
    for ( SketchTriangle tri1 : mTriangles ) {
      if ( tri1.contains( x, y ) > 0 ) {
        float d = tri1.dotNormal( info.ne, info.ns, info.nv );
        if ( d > d0 ) {
          ret = tri1;
          d0 = d;
        } 
      }
    }
    return ret;
  }

  // stretch inside triangles
  void makeStretch( ArrayList< Vector > pts, ArrayList< Vector > border3d )
  {
    // Log.v("DistoX", "make stretch pts " + pts.size() + " border 3d " + border3d.size() );
    int kmax = pts.size();
    float lens[] = new float[ kmax + 1 ];
    float len_max = 0.01f;
    lens[0] = len_max;
    for ( int k=1; k<kmax; ++k ) {
      len_max += pts.get(k).distance( pts.get(k-1) );
      lens[k] = len_max;
    }
    lens[ kmax ] = len_max + 1;
    synchronized( mTriangles ) {
      mInsideVertices.clear();
      for ( SketchTriangle t : mInsideTriangles ) {
        if ( t.inside ) {
          t.inside = false;
          if ( ! mInsideVertices.contains( t.v1 ) ) mInsideVertices.add( t.v1 );
          if ( ! mInsideVertices.contains( t.v2 ) ) mInsideVertices.add( t.v2 );
          if ( ! mInsideVertices.contains( t.v3 ) ) mInsideVertices.add( t.v3 );
        }
      }
      float dist_max = 0;
      for ( SketchVertex v : mInsideVertices ) {
        // compute min-distance from v to the 3d border
        float dist = v.distance( border3d.get(0) );
        for ( Vector w : border3d ) {
          float d = v.distance( w );
          if ( d < dist ) dist = d;
        }
        if ( dist > dist_max ) dist_max = dist;
        v.dist = dist;
      }
      Vector w0 = pts.get(0);
      for ( SketchVertex v : mInsideVertices ) {
        float l = len_max * v.dist / dist_max;
        int k=0;
        while ( k < kmax && l > lens[k] ) ++k;
        if ( k == kmax ) -- k;
        // Log.v("DistoX", "vector len " + l + "/" + len_max + " shift " + k + "/" + kmax );
        Vector w = pts.get(k);
        v.x += TopoDroidSetting.mDeltaExtrude*(w.x - w0.x);
        v.y += TopoDroidSetting.mDeltaExtrude*(w.y - w0.y);
        v.z += TopoDroidSetting.mDeltaExtrude*(w.z - w0.z);
      }  
    } 
  }

  private void drawBorders( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    if ( mBorders != null ) {
      for ( SketchBorder brd : mBorders ) {
        PointF p0 = new PointF();
        Path path3 = new Path();
        for ( SketchSide s : brd.sides ) {
          SketchVertex v1 = mVertices.get( s.v1 );
          SketchVertex v2 = mVertices.get( s.v2 );
          if ( v1 != null && v2 != null ) {
            info.worldToSceneOrigin( v1, p0 );
            path3.moveTo( p0.x, p0.y );
            info.worldToSceneOrigin( v2, p0 );
            path3.lineTo( p0.x, p0.y );
          }
        }
        path3.transform( matrix );
        canvas.drawPath( path3, mPainter.borderLinePaint );
      }
    }
  }
   
  private void drawSurfaceBack( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    float radius = 5f / info.zoom_3d;
    synchronized( mTriangles ) {
      // mCorners.clear();
      Paint paint = mPainter.surfaceBackPaint;
      for ( SketchTriangle tri : mTriangles ) {
        if ( info.isForward( tri ) ) continue;
        // if ( tri.direction( info.ne, info.ns, info.nv ) < 0.0 ) continue;
        float zz = computeProjections( tri, info );
        if ( zz > -900 ) {
          Path path = new Path();
          // mCorners.put( tri.i, tri.p1 );
          // mCorners.put( tri.j, tri.p2 );
          // mCorners.put( tri.k, tri.p3 );

          path.moveTo( tri.p1.x, tri.p1.y );
          path.lineTo( tri.p2.x, tri.p2.y );
          path.lineTo( tri.p3.x, tri.p3.y );
          path.lineTo( tri.p1.x, tri.p1.y );
          path.transform( matrix );
          paint.setAlpha( 32 + (int)(164/(1+Math.abs(zz) ) ) );
          canvas.drawPath( path, paint );
        }
      }

      // paint = mPainter.backVertexPaint;
      // for ( PointF p : mCorners.values() ) {
      //   Path path = new Path();
      //   path.addCircle( p.x, p.y, 3*radius, Path.Direction.CCW );
      //   path.transform( matrix );
      //   canvas.drawPath( path, paint );
      // }
    }
  }


  private void drawSurfaceFor( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    Paint highpaint = mPainter.bluePaint;
    float radius = 5f / info.zoom_3d;
    synchronized( mTriangles ) {
      Paint paint = mPainter.surfaceForPaint;
      mCorners.clear();
      for ( SketchTriangle tri : mTriangles ) {
        if ( ! info.isForward( tri ) ) continue;
        // if ( tri.direction( info.ne, info.ns, info.nv ) > 0.0 ) continue;
        float zz = computeProjections( tri, info );
        if ( zz > -900 ) {
          Path path = new Path();
          mCorners.put( tri.i, tri.p1 );
          mCorners.put( tri.j, tri.p2 );
          mCorners.put( tri.k, tri.p3 );

          path.moveTo( tri.p1.x, tri.p1.y );
          path.lineTo( tri.p2.x, tri.p2.y );
          path.lineTo( tri.p3.x, tri.p3.y );
          path.lineTo( tri.p1.x, tri.p1.y );
          path.transform( matrix );
          paint.setAlpha( (int)(255/(1+Math.abs(zz)/4 ) ) );
          if ( tri.highlight ) {
            canvas.drawPath( path, highpaint );
          } else {
            canvas.drawPath( path, paint );
          }
        }
      }

      paint = mPainter.vertexPaint;
      for ( PointF p : mCorners.values() ) {
        Path path = new Path();
        path.addCircle( p.x, p.y, radius, Path.Direction.CCW );
        path.transform( matrix );
        canvas.drawPath( path, paint );
      }
    }
  }


  private void drawVertices( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    float radius = 5f / info.zoom_3d;
    if ( mSelectedVertex != null ) {
      synchronized( mSelectedVertex ) {
        PointF p = new PointF();
        float z = info.worldToSceneOrigin( mSelectedVertex, p );
        Path path = new Path();
        path.addCircle( p.x, p.y, 2*radius, Path.Direction.CCW );
        path.transform( matrix );
        canvas.drawPath( path, mPainter.vertexPaint );
      }
    }
  }

  // DEBUG show inside triangles blue
  private void drawInsideTriangles( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    if ( mInsideTriangles != null ) {
      synchronized( mInsideTriangles ) {
        for ( SketchTriangle t : mInsideTriangles ) {
          Path path = new Path();
          path.moveTo( t.p1.x, t.p1.y );
          path.lineTo( t.p2.x, t.p2.y );
          path.lineTo( t.p3.x, t.p3.y );
          path.lineTo( t.p1.x, t.p1.y );
          path.transform( matrix );
          canvas.drawPath( path, mPainter.insidePaint );
        }
      }
    }
  }

    // if ( mSelectedTriangle != null ) {
    //   Path path1 = new Path();
    //   SketchVertex v1 = mVertices.get( mSelectedTriangle.i );
    //   SketchVertex v2 = mVertices.get( mSelectedTriangle.j );
    //   SketchVertex v3 = mVertices.get( mSelectedTriangle.k );
    //   PointF p1 = mSelectedTriangle.p1;
    //   PointF p2 = mSelectedTriangle.p2;
    //   PointF p3 = mSelectedTriangle.p3;
    //   
    //   x1 = v1.x - info.east;
    //   y1 = v1.y - info.south;
    //   z1 = v1.z - info.vert;
    //   // project on (cos_clino*sin_azi, -cos_clino*cos_azimuth, -sin_clino)
    //   info.worldToScene( x1, y1, z1, p1 );
    //   path1.moveTo( p1.x, p1.y );
    //   x1 = v2.x - info.east;
    //   y1 = v2.y - info.south;
    //   z1 = v2.z - info.vert;
    //   info.worldToScene( x1, y1, z1, p2 );
    //   path1.lineTo( p2.x, p2.y );
    //   x1 = v3.x - info.east;
    //   y1 = v3.y - info.south;
    //   z1 = v3.z - info.vert;
    //   info.worldToScene( x1, y1, z1, p3 );
    //   path1.lineTo( p3.x, p3.y );
    //   path1.lineTo( p1.x, p1.y );
    //   path1.transform( matrix );
    //   canvas.drawPath( path1, mPainter.redPaint );
    // }
  
  synchronized void draw( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    drawSurfaceBack( canvas, matrix, info );
    drawSurfaceFor( canvas, matrix, info );
    drawVertices( canvas, matrix, info );
    // drawInsideTriangles( canvas, matrix, info );
    drawBorders( canvas, matrix, info );
  }

  // get the vertex at a certain scene point 
  // (null if no  vertex found)
  SketchVertex getVertexAt( float x, float y, float d ) // (x,y) scene point
  {
    synchronized( mTriangles ) {  // mCorners is synchronized on mTriangles
      for ( Integer key : mCorners.keySet() ) {
        PointF p = mCorners.get( key );
        Log.v("DistoX", "pt " + key + " " + p.x + " " + p.y  );
        if ( Math.abs( p.x - x ) < d && Math.abs( p.y - y ) < d ) {
          return mVertices.get( key );
        }
      }
    }
    return null;
  }

  void toTherion( PrintWriter pw, String what ) 
  { 
    // dump();
    // pw.format("%s -shot %s %s %d %d %d\n", what, st1, st2, mVertices.size(), mSides.size(), mTriangles.size() );
    pw.format("%s -shot %s %s %d %d\n", what, st1, st2, mVertices.size(), mTriangles.size() );
    pw.format("  vertex\n");
    for ( SketchVertex v : mVertices.values() ) {
      v.toTherion( pw );
    }
    pw.format("  endvertex\n");
    // pw.format("  side\n");
    // for ( SketchSide s : mSides.values() ) {
    //   s.toTherion( pw );
    // }
    // pw.format("  endside\n");
    pw.format("  triangle\n");
    for ( SketchTriangle t : mTriangles ) {
      t.toTherion( pw );
    }
    pw.format("  endtriangle\n");
    pw.format("end%s\n\n", what );
  }
}
