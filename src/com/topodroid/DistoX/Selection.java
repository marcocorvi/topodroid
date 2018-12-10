/* @file Selection.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief Selection among drawing items
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.LinkedList;
// import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;

// import android.util.Log;

import android.graphics.Matrix;

class Selection
{
  final static private int BSIZE = 100; // bucket size factor

  ArrayList< SelectionPoint > mPoints;
  ArrayList< SelectionBucket > mBuckets;

  Selection( )
  {
    mPoints  = new ArrayList<>();
    mBuckets = new ArrayList<>();
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

  void scaleSelectionBy( float z, Matrix m )
  {
    for ( SelectionPoint sp : mPoints ) {
      int t = sp.type();
      if ( t == DrawingPath.DRAWING_PATH_POINT
        || t == DrawingPath.DRAWING_PATH_LINE
        || t == DrawingPath.DRAWING_PATH_AREA ) {
        sp.scaleSelectionBy( z, m );
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

  synchronized void clearReferencePoints()
  {
    Iterator< SelectionPoint > it = mPoints.iterator();
    while( it.hasNext() ) {
      SelectionPoint sp1 = (SelectionPoint)it.next();
      if ( sp1.isReferenceType() ) {
        sp1.setBucket( null );
        it.remove( );
      }
    }
  }

  synchronized void clearDrawingPoints()
  {
    Iterator< SelectionPoint > it = mPoints.iterator();
    while( it.hasNext() ) {
      SelectionPoint sp1 = (SelectionPoint)it.next();
      if ( sp1.isDrawingType() ) {
        sp1.setBucket( null );
        it.remove( );
      }
    }
  }

  void insertStationName( DrawingStationName st )
  {
    insertItem( st, null );
  }

  /** like insertItem, but it returns the inserted SelectionPoint
   * @param path     point-line path
   * @param pt       new point on the point-line
   * @return newly cretaed selection point
   */
  SelectionPoint insertPathPoint( DrawingPointLinePath path, LinePoint pt )
  {
    SelectionPoint sp = new SelectionPoint( path, pt, null );
    mPoints.add( sp );
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );

    // Log.v("DistoX", "Selection insert path point " + pt.x + " " + pt.y );
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
      pt.setDistance( 0.0f );
    }
  }

  void rebucket( SelectionPoint sp )
  {
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );
  }

  private void insertItem( DrawingPath path, LinePoint pt )
  {
    SelectionPoint sp = new SelectionPoint( path, pt, null );
    mPoints.add( sp );
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );

    // if ( pt != null ) {
    //   Log.v("DistoX", "insert item path type " + path.mType + " pt " + pt.x + " " + pt.y );
    // } else {
    //   Log.v("DistoX", "insert item path type " + path.mType + " null pt ");
    // }
    // sp.mBucket.dump();
    // dumpBuckets();
  }

  // FIXME this is called with dmin = 10f
  private SelectionPoint getBucketNearestPoint(SelectionPoint sp,float x,float y,float dmin)
  {
    SelectionPoint spmin = null;
    float x0 = sp.X();
    float y0 = sp.Y();
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x0, y0, dmin, dmin ) ) {
        for ( SelectionPoint sp2 : bucket.mPoints ) {
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

  // get the nearest endpoint of a line having the same type as that of sp
  // @param sp   selection point (hot item)
  // @param x,y  point coords
  // @param dmin maximum acceptable point-distance
  // @param linetype type of the line
  // FIXME this is called with dmin = 10f
  private SelectionPoint getBucketNearestLineEndPoint(SelectionPoint sp, float x, float y, float dmin, int linetype )
  {
    if ( sp.type() != DrawingPath.DRAWING_PATH_LINE ) return null;
    DrawingLinePath line1 = (DrawingLinePath)sp.mItem;
    SelectionPoint spmin = null;
    float x0 = sp.X();
    float y0 = sp.Y();
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x0, y0, dmin, dmin ) ) {
        for ( SelectionPoint sp2 : bucket.mPoints ) {
          if ( sp == sp2 ) continue;
	  if ( sp2.type() != DrawingPath.DRAWING_PATH_LINE ) continue;
          DrawingLinePath line2 = (DrawingLinePath)sp2.mItem;
	  if ( line2 == line1 ) continue;
	  if ( line2.mLineType != linetype ) continue;
	  LinePoint pt2 = sp2.mPoint;
	  if ( pt2 != line2.mFirst && pt2 != line2.mLast ) continue;

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

    for ( SelectionPoint sp1 : mPoints ) {
      if ( sp == sp1 ) continue;
      float d = sp1.distance( x, y );
      if ( d < dmin ) {
        dmin = d;
        spmin = sp1;
      }
    }
    return spmin;
  }

  SelectionPoint getNearestLineEndPoint( SelectionPoint sp, float x, float y, float dmin, int linetype )
  {
    SelectionPoint spmin = getBucketNearestLineEndPoint( sp, x, y, dmin, linetype );
    if ( spmin != null ) return spmin;

    for ( SelectionPoint sp1 : mPoints ) {
      if ( sp == sp1 ) continue;
      if ( sp1.type() != DrawingPath.DRAWING_PATH_LINE ) continue;
      DrawingLinePath line1 = (DrawingLinePath)sp1.mItem;
      if ( line1.mLineType != linetype ) continue;
      LinePoint pt1 = sp1.mPoint;
      if ( pt1 != line1.mFirst && pt1 != line1.mLast ) continue;
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

  SelectionPoint getSelectionPoint( LinePoint lp )
  {
    // for ( SelectionPoint sp : mPoints ) {
    //   if ( sp.mPoint == lp ) return sp;
    // }
    // return null;
    // FIXED use buckets
    float x0 = lp.x;
    float y0 = lp.y;
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x0, y0, 10f, 10f ) ) {
        for ( SelectionPoint sp : bucket.mPoints ) {
          if ( lp == sp.mPoint ) return sp;
        }
      }
    }
    return null;
  }


  private SelectionPoint bucketSelectOnItemAt(DrawingPath item,float x,float y,float radius)
  {  
    float min_distance = radius;
    SelectionPoint ret = null;
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x, y, radius, radius ) ) {
        for ( SelectionPoint sp : bucket.mPoints ) {
          if ( sp.mItem == item ) {
            float d = sp.distance( x, y );
            if ( d < min_distance ) {
              min_distance = d;
              ret = sp;
            }
          }
        }
      }
    }
    return ret;
  }

  private boolean containsStation( DrawingPath p, ArrayList<String> stations ) 
  {
    if ( stations == null ) return false;
    DBlock blk = p.mBlock;
    if ( blk == null ) return false;
    String station = blk.mFrom;
    if ( station == null || station.length() == 0 ) return false;
    return stations.contains( station );
  }

  private void bucketSelectAt(float x,float y,float radius,int mode,SelectionSet sel,boolean legs,boolean splays,boolean stations,
		              ArrayList<String> splays_on, ArrayList<String> splays_off )
  {
    // Log.v("DistoX", "bucket select at " + x + " " + y + " R " + radius + " buckets " + mBuckets.size() );
    if ( mode == Drawing.FILTER_ALL ) {
      for ( SelectionBucket bucket : mBuckets ) {
        if ( bucket.contains( x, y, radius, radius ) ) {
          for ( SelectionPoint sp : bucket.mPoints ) {
            if ( !legs && sp.type() == DrawingPath.DRAWING_PATH_FIXED ) continue;
            // if ( !splays && sp.type() == DrawingPath.DRAWING_PATH_SPLAY ) continue;
            if ( !stations && (    sp.type() == DrawingPath.DRAWING_PATH_STATION 
                                || sp.type() == DrawingPath.DRAWING_PATH_NAME ) ) continue;
            if ( sp.type() == DrawingPath.DRAWING_PATH_SPLAY ) {
	      if ( splays ) {
                if ( containsStation( sp.mItem, splays_off ) ) continue;
	      } else {
                if ( ! containsStation( sp.mItem, splays_on ) ) continue;
	      }
	    }
            if ( sp.distance( x, y ) < radius ) {
              // Log.v("DistoX", "pt " + sp.mPoint.x + " " + sp.mPoint.y + " dist " + sp.getDistance() );
              sel.addPoint( sp );
            }
          }
        }
      }
    } else if ( mode == Drawing.FILTER_SHOT ) {
      if ( ! (legs || splays) ) return;
      for ( SelectionBucket bucket : mBuckets ) {
        if ( bucket.contains( x, y, radius, radius ) ) {
          for ( SelectionPoint sp : bucket.mPoints ) {
            if ( ( legs && sp.type() == DrawingPath.DRAWING_PATH_FIXED ) ||
                 ( splays && sp.type() == DrawingPath.DRAWING_PATH_SPLAY ) ) {
              if ( sp.distance( x, y ) < radius ) sel.addPoint( sp );
            }
          }
        }
      }
    } else if ( mode == Drawing.FILTER_STATION ) {
      if ( ! stations ) return;
      for ( SelectionBucket bucket : mBuckets ) {
        if ( bucket.contains( x, y, radius, radius ) ) {
          for ( SelectionPoint sp : bucket.mPoints ) {
            if (    sp.type() == DrawingPath.DRAWING_PATH_STATION 
                 || sp.type() == DrawingPath.DRAWING_PATH_NAME ) {
              if ( sp.distance( x, y ) < radius ) sel.addPoint( sp );
            }
          }
        }
      }
    } else {
      int type = -0;
      if ( mode == Drawing.FILTER_POINT ) { type = DrawingPath.DRAWING_PATH_POINT; }
      else if ( mode == Drawing.FILTER_LINE ) { type = DrawingPath.DRAWING_PATH_LINE; }
      else if ( mode == Drawing.FILTER_AREA ) { type = DrawingPath.DRAWING_PATH_AREA; }
      for ( SelectionBucket bucket : mBuckets ) {
        if ( bucket.contains( x, y, radius, radius ) ) {
          for ( SelectionPoint sp : bucket.mPoints ) {
            if ( sp.type() == type ) {
              if ( sp.distance( x, y ) < radius ) sel.addPoint( sp );
            }
          }
        }
      }
    }
  }
 
  // select by type
  private void bucketSelectAt(float x,float y,float radius,int type, SelectionSet sel )
  {
    // Log.v("DistoX", "bucket select at " + x + " " + y + " R " + radius + " buckets " + mBuckets.size() );
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x, y, radius, radius ) ) {
        for ( SelectionPoint sp : bucket.mPoints ) {
          if ( sp.type() == type && sp.distance( x, y ) < radius ) {
            // Log.v("DistoX", "pt " + sp.mPoint.x + " " + sp.mPoint.y + " dist " + sp.getDistance() );
            sel.addPoint( sp );
          }
        }
      }
    } 
  }
  
  SelectionPoint  selectOnItemAt( DrawingPath item, float x, float y, float radius )
  {
    return bucketSelectOnItemAt( item, x, y, radius );
  }

  void selectAt( SelectionSet sel, float x, float y, float radius, int mode, boolean legs, boolean splays, boolean stations,
		 ArrayList<String> splays_on, ArrayList<String> splays_off )
  {
    bucketSelectAt( x, y, radius, mode, sel, legs, splays, stations, splays_on, splays_off );
  }

  void selectAt( SelectionSet sel, float x, float y, float radius, int type )
  {
    bucketSelectAt( x, y, radius, type, sel );
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
    if ( sp.mLP1 != null || sp.mLP2 != null ) {
      rebucketLinePath( (DrawingPointLinePath)sp.mItem );
    } else {
      SelectionBucket sb = sp.mBucket;
      if ( sb != null && ! sb.contains( sp.X(), sp.Y() ) ) {
        // find the bucket that contains sp and assign it to sp
        sb = getBucket( sp.X(), sp.Y() );
        sp.setBucket( sb ); // this removes sp from its old bucket and add it to the new bucket
      }
    }
  }

  private void rebucketLinePath(DrawingPointLinePath line)
  {
    for ( LinePoint lp = line.mFirst; lp != null; lp = lp.mNext ) {
      for ( SelectionPoint sp : mPoints ) {
        if ( sp.mPoint == lp ) {
          SelectionBucket sb = sp.mBucket;
          if ( sb != null && ! sb.contains( sp.X(), sp.Y() ) ) {
            // find the bucket that contains sp and assign it to sp
            sb = getBucket( sp.X(), sp.Y() );
            sp.setBucket( sb ); // this removes sp from its old bucket and add it to the new bucket
          }
          break;
        }
      }
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
