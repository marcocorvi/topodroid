/* @file Selection.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief Selection among drawing items
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.LinkedList;
// import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

class Selection
{
  final static int BSIZE = 100; // bucket size factor

  ArrayList< SelectionPoint > mPoints;
  ArrayList< SelectionBucket > mBuckets;

  Selection( )
  {
    mPoints  = new ArrayList< SelectionPoint >();
    mBuckets = new ArrayList< SelectionBucket >();
  }

  void shiftSelectionBy( float x, float y )
  {
    for ( SelectionPoint sp : mPoints ) {
      int t = sp.type();
      if ( t == DrawingPath.DRAWING_PATH_POINT
        || t == DrawingPath.DRAWING_PATH_LINE
        || t == DrawingPath.DRAWING_PATH_AREA ) {
        sp.shiftSelectionBy(x, y);
        float x1 = sp.X();
        float y1 = sp.Y();
        if ( sp.mBucket != null ) {
          if ( ! sp.mBucket.contains( x1, y1 ) ) {
            sp.setBucket( getBucket( x1, y1 ) );
          }
        } else {
          sp.setBucket( getBucket( x1, y1 ) );
        }
      }
    }
  }

  void clearSelectionPoints()
  {
    // Log.v("DistoX", "Selection clear" );
    mPoints.clear();
    mBuckets.clear();
  }

  void clearReferencePoints()
  {
    synchronized ( mPoints ) {
      Iterator< SelectionPoint > it = mPoints.iterator();
      while( it.hasNext() ) {
        SelectionPoint sp1 = (SelectionPoint)it.next();
        if ( sp1.isReferenceType() ) {
          sp1.setBucket( null );
          it.remove( );
        }
      }
    }
  }

  void clearDrawingPoints()
  {
    synchronized ( mPoints ) {
      Iterator< SelectionPoint > it = mPoints.iterator();
      while( it.hasNext() ) {
        SelectionPoint sp1 = (SelectionPoint)it.next();
        if ( sp1.isDrawingType() ) {
          sp1.setBucket( null );
          it.remove( );
        }
      }
    }
  }

  void insertStationName( DrawingStationName st )
  {
    insertItem( st, null );
  }

  /** like insertItem, but it returns the inserted SelectionPoint
   * @param path     point-line path
   * @param point    new point on the point-line
   * @return
   */
  SelectionPoint insertPathPoint( DrawingPointLinePath path, LinePoint pt )
  {
    SelectionPoint sp = new SelectionPoint( path, pt, null );
    mPoints.add( sp );
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );

    // Log.v("DistoX", "Selection insert path point " + pt.mX + " " + pt.mY );
    // sp.mBucket.dump();
    return sp;
  }
  
  void insertLinePath( DrawingLinePath path )
  {
    for ( LinePoint p2 = path.mFirst; p2 != null; p2 = p2.mNext ) {
      insertItem( path, p2 );
    }
  }

  void insertPath( DrawingPath path )
  {
    // Log.v("DistoX", "Selection insert path" );
    // LinePoint p1;
    LinePoint p2;
    switch ( path.mType ) {
      case DrawingPath.DRAWING_PATH_FIXED:
      case DrawingPath.DRAWING_PATH_SPLAY:
        insertItem( path, null );
        break;
      case DrawingPath.DRAWING_PATH_GRID:
        // nothing
        break;
      case DrawingPath.DRAWING_PATH_STATION:
        insertItem( path, null );
        break;
      case DrawingPath.DRAWING_PATH_POINT:
        insertItem( path, null );
        break;
      case DrawingPath.DRAWING_PATH_LINE:
        DrawingLinePath lp = (DrawingLinePath)path;
        for ( p2 = lp.mFirst; p2 != null; p2 = p2.mNext ) {
          insertItem( path, p2 );
        }
        break;
      case DrawingPath.DRAWING_PATH_AREA:
        DrawingAreaPath ap = (DrawingAreaPath)path;
        for ( p2 = ap.mFirst; p2 != null; p2 = p2.mNext ) {
          insertItem( path, p2 );
        }
        break;
      default:
    }
  }

  void resetDistances()
  {
    // Log.v("DistoX", "Selection reset distances" );
    for ( SelectionPoint pt : mPoints ) {
      pt.mDistance = 0.0f;
    }
  }

  private void insertItem( DrawingPath path, LinePoint pt )
  {
    SelectionPoint sp = new SelectionPoint( path, pt, null );
    mPoints.add( sp );
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );

    // if ( pt != null ) {
    //   Log.v("DistoX", "insert item path type " + path.mType + " pt " + pt.mX + " " + pt.mY );
    // } else {
    //   Log.v("DistoX", "insert item path type " + path.mType + " null pt ");
    // }
    // sp.mBucket.dump();
    // dumpBuckets();
  }

  // FIXME this is called with dmin = 10f
  SelectionPoint getBucketNearestPoint( SelectionPoint sp, float x, float y, float dmin )
  {
    SelectionPoint spmin = null;
    float x0 = sp.X();
    float y0 = sp.Y();
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x0, y0, dmin, dmin ) ) {
        final Iterator jt = bucket.mPoints.iterator();
        while( jt.hasNext() ) {
          SelectionPoint sp2 = (SelectionPoint)jt.next();
          if ( sp == sp2 ) continue;
          float d = sp2.distance( x, y );
          if ( d < dmin ) {
            dmin = d;
            spmin = sp2;
          }
        }
      }
    }
    return spmin;
  }

  SelectionPoint getNearestPoint( SelectionPoint sp, float x, float y, float dmin )
  {
    SelectionPoint spmin = getBucketNearestPoint( sp, x, y, dmin );
    if ( spmin != null ) return spmin;

    final Iterator it = mPoints.iterator();
    while( it.hasNext() ) {
      SelectionPoint sp1 = (SelectionPoint)it.next();
      if ( sp == sp1 ) continue;
      float d = sp1.distance( x, y );
      if ( d < dmin ) {
        dmin = d;
        spmin = sp1;
      }
    }
    return spmin;
  }

  void removePoint( SelectionPoint sp )
  {
    sp.setBucket( null );
    mPoints.remove( sp ); 
  }

  void removePath( DrawingPath path )
  {
    if ( path.mType == DrawingPath.DRAWING_PATH_LINE || path.mType == DrawingPath.DRAWING_PATH_AREA ) {
      DrawingPointLinePath line = (DrawingPointLinePath)path;
      for ( LinePoint lp = line.mFirst; lp != null; lp = lp.mNext ) {
        for ( SelectionPoint sp : mPoints ) {
          if ( sp.mPoint == lp ) {
            removePoint( sp );
            break;
          }
        }
      }
    } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {  
      for ( SelectionPoint sp : mPoints ) {
        if ( sp.mItem == path ) {
          removePoint( sp );
          break;
        }
      }
    }
  }

  void removeLinePoint( DrawingPointLinePath path, LinePoint lp )
  {
    if ( path.mType != DrawingPath.DRAWING_PATH_LINE && path.mType != DrawingPath.DRAWING_PATH_AREA ) return;
    for ( SelectionPoint sp : mPoints ) {
      if ( sp.mPoint == lp ) {
        removePoint( sp );
        return;
      }
    }
  }

  void bucketSelectAt( float x, float y, float radius, SelectionSet sel, boolean legs, boolean splays, boolean stations )
  {
    // Log.v("DistoX", "bucket select at " + x + " " + y + " R " + radius + " buckets " + mBuckets.size() );
    for ( SelectionBucket bucket : mBuckets ) {
      // bucket.dump();
      if ( bucket.contains( x, y, radius, radius ) ) {
        for ( SelectionPoint sp : bucket.mPoints ) {
          if ( !legs && sp.type() == DrawingPath.DRAWING_PATH_FIXED ) continue;
          if ( !splays && sp.type() == DrawingPath.DRAWING_PATH_SPLAY ) continue;
          if ( !stations && ( sp.type() == DrawingPath.DRAWING_PATH_STATION || sp.type() == DrawingPath.DRAWING_PATH_NAME ) ) continue;
       
          sp.mDistance = sp.distance( x, y );
          if ( sp.mDistance < radius ) {
            sel.addPoint( sp );
            // sp.mBucket.dump();
          }
        }
      }
    }
  }

  void selectAt( float x, float y, float zoom, SelectionSet sel, boolean legs, boolean splays, boolean stations )
  {
    float radius = TDSetting.mCloseCutoff + TDSetting.mCloseness / zoom;
    // Log.v( "DistoX", "selection select at " + x + " " + y + " pts " + mPoints.size() + " " + legs + " " + splays + " " + stations + " radius " + radius );

    bucketSelectAt( x, y, radius, sel, legs, splays, stations );
    // Log.v("DistoX", "bucketSelect size " + sel.size() );

    if ( sel.size() > 0 ) return;

    // for ( SelectionPoint sp : mPoints ) {
    //   if ( !legs && sp.type() == DrawingPath.DRAWING_PATH_FIXED ) continue;
    //   if ( !splays && sp.type() == DrawingPath.DRAWING_PATH_SPLAY ) continue;
    //   if ( !stations && ( sp.type() == DrawingPath.DRAWING_PATH_STATION || sp.type() == DrawingPath.DRAWING_PATH_NAME ) ) continue;
    //   sp.mDistance = sp.distance(x, y);
    //   // Log.v("DistoX", "sp " + sp.name() + " distance " + sp.mDistance );
    //   if ( sp.mDistance < radius ) {
    //     sel.addPoint( sp );
    //   }
    // }
  }

  private SelectionBucket getBucket( float x, float y )
  {
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x, y ) ) return bucket;
    }
    float x0 = BSIZE * (float)Math.floor(x / BSIZE);
    float y0 = BSIZE * (float)Math.floor(y / BSIZE);
    SelectionBucket ret = new SelectionBucket( x0, y0, x0+BSIZE, y0+BSIZE );
    mBuckets.add( ret );
    return ret;
  }

  // check if the SelectionPoint is still in its bucket
  // otherwise move it to the new bucket
  void checkBucket( SelectionPoint sp ) 
  {
    if ( sp == null ) return;
    SelectionBucket sb = sp.mBucket;
    if ( sb != null && ! sb.contains( sp.X(), sp.Y() ) ) {
      // find the bucket that contains sp and assign it to sp
      sb = getBucket( sp.X(), sp.Y() );
      sp.setBucket( sb ); // this removes sp from its old bucket and add it to the new bucket
    }
  }

  // private void dumpBuckets()
  // {
  //   for ( SelectionBucket bucket : mBuckets ) {
  //     bucket.dump();
  //   }
  // }

  // private void dumpPointsTypes()
  // {
  //   int nfxd = 0;
  //   int nspl = 0;
  //   int ngrd = 0;
  //   int nsta = 0;
  //   int npnt = 0;
  //   int nlin = 0;
  //   int nare = 0;
  //   int nnam = 0;
  //   int nnrt = 0;
  //   int noth = 0;
  //   for ( SelectionPoint p : mPoints ) {
  //     switch ( p.mItem.mType ) {
  //       case DrawingPath.DRAWING_PATH_FIXED:   nfxd++; break;
  //       case DrawingPath.DRAWING_PATH_SPLAY:   nspl++; break;
  //       case DrawingPath.DRAWING_PATH_GRID:    ngrd++; break;
  //       case DrawingPath.DRAWING_PATH_STATION: nsta++; break;
  //       case DrawingPath.DRAWING_PATH_POINT:   npnt++; break;
  //       case DrawingPath.DRAWING_PATH_LINE:    nlin++; break;
  //       case DrawingPath.DRAWING_PATH_AREA:    nare++; break;
  //       case DrawingPath.DRAWING_PATH_NAME:    nnam++; break;
  //       case DrawingPath.DRAWING_PATH_NORTH:   nnrt++; break;
  //       default: noth ++;
  //     }
  //   }
  //   Log.v("DistoX", "Selection points " + mPoints.size() + " " + nfxd + " " + nspl + " " + ngrd + " " + nsta 
  //                   + " " + npnt + " " + nlin + " " + nare + " " + nnam + " " + nnrt + " " + noth  );
  // }
}
