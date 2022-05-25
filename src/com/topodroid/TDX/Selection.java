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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

// import java.util.LinkedList;
// import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Matrix;

class Selection
{
  final static private int BSIZE = 100; // bucket size factor

  ArrayList< SelectionPoint > mPoints;
  ArrayList< SelectionBucket > mBuckets;

  /** cstr - prepare lists
   */
  Selection( )
  {
    mPoints  = new ArrayList< SelectionPoint >();
    mBuckets = new ArrayList< SelectionBucket >();
  }

  // this is clearReferencePoints
  // void removeShotsAndStations()
  // {
  //   ArrayList< SelectionPoint > toRemove = new ArrayList<>();
  //   for ( SelectionPoint sp : mPoints ) {
  //     int t = sp.type();
  //     if ( t == DrawingPath.DRAWING_PATH_FIXED
  //       || t == DrawingPath.DRAWING_PATH_SPLAY
  //       || t == DrawingPath.DRAWING_PATH_STATION ) toRemove.add( sp );
  //   }
  //   for ( SelectionPoint sp : toRemove ) {
  //     removePoint( sp );
  //   }
  // }

  /** shift selection
   * @param x    X shift [pixels]
   * @param y    Y shift
   */
  void shiftSelectionBy( float x, float y )  // synchronized by CommandManager
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

  /** scale selection
   * @param z    scale factor
   * @param m    scaling matrix
   */
  void scaleSelectionBy( float z, Matrix m ) // synchronized by CommandManager
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

  /** affine transform selection
   * @param mm   affine transform matrix as array
   * @param m    affine transform matrix
   */
  void affineTransformSelectionBy( float[] mm, Matrix m ) // synchronized by CommandManager
  {
    for ( SelectionPoint sp : mPoints ) {
      int t = sp.type();
      if ( t == DrawingPath.DRAWING_PATH_POINT
        || t == DrawingPath.DRAWING_PATH_LINE
        || t == DrawingPath.DRAWING_PATH_AREA ) {
        sp.affineTransformSelectionBy( mm, m );
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

  /** clear the selection - remove all points
   */
  void clearSelectionPoints() // synchronized by CommandManager
  {
    mPoints.clear();
    mBuckets.clear();
  }

  /** clear the reference points
   */
  void clearReferencePoints() // synchronized by CommandManager
  {        
    // int cnt = 0;
    Iterator< SelectionPoint > it = mPoints.iterator();
    while( it.hasNext() ) {
      SelectionPoint sp1 = (SelectionPoint)it.next();
      if ( sp1.isReferenceType() ) {
        sp1.setBucket( null );
        it.remove( );
        // ++ cnt;
      }
    }
    // TDLog.v("selection clear reference points " + cnt );
  }

  // void clearDrawingPoints()
  // {
  //   synchronized ( TDPath.mSelectionLock ) {
  //     Iterator< SelectionPoint > it = mPoints.iterator();
  //     while( it.hasNext() ) {
  //       SelectionPoint sp1 = (SelectionPoint)it.next();
  //       if ( sp1.isDrawingType() ) {
  //         sp1.setBucket( null );
  //         it.remove( );
  //       }
  //     }
  //   }
  // }

  /** insert the station name point
   * @param st   station name item
   */
  void insertStationName( DrawingStationName st )
  {
    insertItem( st, null );
  }

  /** like insertItem, but it returns the inserted SelectionPoint
   * @param path     point-line path
   * @param pt       new point on the point-line
   * @return newly created selection point
   */
  SelectionPoint insertPathPoint( DrawingPointLinePath path, LinePoint pt ) // synchronized by CommandManager
  {
    SelectionPoint sp = new SelectionPoint( path, pt, null );
    mPoints.add( sp );
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );

    // TDLog.v( "Selection insert path point " + pt.x + " " + pt.y );
    // sp.mBucket.dump();
    return sp;
  }
  
  /** insert the points of a line
   * @param path   line item
   */
  void insertLinePath( DrawingLinePath path ) // synchronized by CommandManager
  {
    for ( LinePoint p2 = path.first(); p2 != null; p2 = p2.mNext ) {
      insertItem( path, p2 );
    }
  }

  /** insert the points of a path
   * @param path   item
   */
  void insertPath( DrawingPath path ) // synchronized by CommandManager
  {
    // TDLog.v( "Selection insert path" );
    // LinePoint p1;
    LinePoint p2;
    switch ( path.mType ) {
      case DrawingPath.DRAWING_PATH_GRID:
        // nothing
        break;
      case DrawingPath.DRAWING_PATH_FIXED:
      case DrawingPath.DRAWING_PATH_SPLAY:
        // insertItem( path, null );
        // break;
      case DrawingPath.DRAWING_PATH_STATION:
        // insertItem( path, null );
        // break;
      case DrawingPath.DRAWING_PATH_POINT:
        insertItem( path, null );
        break;
      case DrawingPath.DRAWING_PATH_LINE:
        DrawingLinePath lp = (DrawingLinePath)path;
        for ( p2 = lp.first(); p2 != null; p2 = p2.mNext ) {
          insertItem( path, p2 );
        }
        break;
      case DrawingPath.DRAWING_PATH_AREA:
        DrawingAreaPath ap = (DrawingAreaPath)path;
        for ( p2 = ap.first(); p2 != null; p2 = p2.mNext ) {
          insertItem( path, p2 );
        }
        break;
      default:
    }
  }

  /** reset to 0 the distance field of the points in the selection
   */
  void resetDistances()
  {
    // TDLog.v( "Selection reset distances" );
    for ( SelectionPoint pt : mPoints ) {
      pt.setDistance( 0.0f );
    }
  }

  /** re-bucket a point
   * @param sp   point
   */
  void rebucket( SelectionPoint sp ) // synchronized by CommandManager
  {
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );
  }

  /** insert an item
   * @param path  item
   * @param pt    line point
   */
  private void insertItem( DrawingPath path, LinePoint pt )
  {
    try {
      SelectionPoint sp = new SelectionPoint( path, pt, null ); // OOM Exception
      mPoints.add( sp );
      sp.setBucket( getBucket( sp.X(), sp.Y() ) );
    } catch ( OutOfMemoryError e ) { // unrecoverable error
      TDLog.Error("OOM " + e.getMessage() );
    }

    // if ( pt != null ) {
    //   TDLog.v("SELECT insert item path type " + path.mType + " pt " + pt.x + " " + pt.y );
    // } else {
    //   TDLog.v("SELECT insert item path type " + path.mType + " null pt ");
    // }
    // sp.mBucket.dump();
    // dumpBuckets();
  }

  /** find the closest point of the bucket containing a given point, provided it is close enough
   * @param sp   selection point
   * @param x    X coord
   * @param y    Y coord
   * @param dmin maximum acceptable distance
   * @return minimum distance point or null
   * @note this is called with dmin = 10f
   */
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

  /** get the nearest endpoint of a line having the same type as that of sp
   * @param sp   selection point (hot item)
   * @param x    point X coord
   * @param y    point Y coord
   * @param dmin maximum acceptable point-distance
   * @param linetype type of the line
   * @return minimum distance point or null
   * @note this is called with dmin = 10f
   */
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
	  // if ( pt2 != line2.first() && pt2 != line2.last() ) continue;
	  if ( line2.isNotEndpoint( pt2 ) ) continue;

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

  /** @return the selection point of a line corresponding to a line point
   * @param line   line item
   * @param pt     line point
   */
  private SelectionPoint getBucketLinePoint( DrawingLinePath line, LinePoint pt )
  {
    float dmin = 1f;
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( pt.x, pt.y, dmin, dmin ) ) {
        for ( SelectionPoint sp2 : bucket.mPoints ) {
	  if ( sp2.type() != DrawingPath.DRAWING_PATH_LINE ) continue;
          if ( (DrawingLinePath)sp2.mItem != line ) continue;
	  if ( sp2.mPoint == pt ) return sp2;
        }
      }
    }
    return null;
  }

  /** @return the selection point closest to (x,y) within a maximum distance
   * @param sp    selection point for the initial search bucket
   * @param x     X coord
   * @param y     Y coord
   * @param dmin  maximum acceptable distance
   */
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

  /** @return the selection point, of a line endpoint, closest to (x,y) within a maximum distance
   * @param sp    selection point for the initial search bucket
   * @param x     X coord
   * @param y     Y coord
   * @param dmin  maximum acceptable distance
   * @param linetype line type
   */
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
      // if ( pt1 != line1.first() && pt1 != line1.last() ) continue;
      if ( line1.isNotEndpoint( pt1 ) ) continue;

      float d = sp1.distance( x, y );
      if ( d < dmin ) {
        dmin = d;
        spmin = sp1;
      }
    }
    return spmin;
  }

  /** remove a line point
   * @param line   line item
   * @param pt     line point
   */
  void removeLineLastPoint( DrawingLinePath line, LinePoint pt )
  {
    SelectionPoint sp = getBucketLinePoint( line, pt );
    if ( sp != null ) removePoint( sp );
  }

  /** remove a point
   * @param sp  point to remove
   */
  void removePoint( SelectionPoint sp ) // synchronized by CommandManager
  {
    sp.setBucket( null );
    mPoints.remove( sp ); 
  }

  /** remove the points of a splay item
   * @param path   item
   * @note for incremental update
   */
  void removeSplayPath( DrawingPath path ) // synchronized by CommandManager
  {
    for ( SelectionPoint sp : mPoints ) {
      if ( sp.mItem == path ) {
        removePoint( sp );
        break;
      }
    }
  }

  /** remove the points of an item
   * @param path   item
   */
  void removePath( DrawingPath path ) // synchronized by CommandManager
  {
    if ( path.mType == DrawingPath.DRAWING_PATH_LINE || path.mType == DrawingPath.DRAWING_PATH_AREA ) {
      DrawingPointLinePath line = (DrawingPointLinePath)path;
      for ( LinePoint lp = line.first(); lp != null; lp = lp.mNext ) {
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

  /** remove a point-line point
   * @param path   point-line item
   * @param lp     line point
   */
  void removeLinePoint( DrawingPointLinePath path, LinePoint lp ) // synchronized by CommandManager
  {
    if ( path.mType != DrawingPath.DRAWING_PATH_LINE && path.mType != DrawingPath.DRAWING_PATH_AREA ) return;
    for ( SelectionPoint sp : mPoints ) {
      if ( sp.mPoint == lp ) {
        removePoint( sp );
        return;
      }
    }
  }

  /** @return the selection point of a line point
   * @param lp     line point
   */
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

  /** @return the selection point ...
   * @param item    ...
   * @param x       X coord
   * @param y       Y coord
   * @param radius  selection radius
   */
  SelectionPoint selectOnItemAt( DrawingPath item, float x, float y, float radius ) // synchronized by CommandManager
  {
    return bucketSelectOnItemAt( item, x, y, radius );
  }

  /** @return the selection point ...
   * @param item    ...
   * @param x       X coord
   * @param y       Y coord
   * @param radius  selection radius
   */
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

  /** select by type
   * @param x        X coord
   * @param y        Y coord
   * @param radius   selection radius
   * @param mode     ...
   * @param sel      selection set (to be filled)
   * @param legs     whether to include legs
   * @param splays   whether to include splays
   * @param stations whether to include stations
   * @param station_splay ...
   */
  private void bucketSelectAt(float x,float y,float radius,int mode,SelectionSet sel,boolean legs,boolean splays,boolean stations, DrawingStationSplay station_splay )
  {
    // TDLog.v( "bucket select at " + x + " " + y + " R " + radius + " buckets " + mBuckets.size() );
    if ( mode == Drawing.FILTER_ALL ) {
      for ( SelectionBucket bucket : mBuckets ) {
        if ( bucket.contains( x, y, radius, radius ) ) {
          for ( SelectionPoint sp : bucket.mPoints ) {
            int type = sp.type();
            if ( !legs && type == DrawingPath.DRAWING_PATH_FIXED ) continue;
            // if ( !splays && type == DrawingPath.DRAWING_PATH_SPLAY ) continue;
            if ( !stations && (    type == DrawingPath.DRAWING_PATH_STATION 
                                || type == DrawingPath.DRAWING_PATH_NAME ) ) continue;
            if ( type == DrawingPath.DRAWING_PATH_SPLAY ) {
              if ( station_splay == null ) {
                if ( ! splays ) continue;
              } else {
                if ( splays ) {
                  if ( station_splay.isStationOFF( sp.mItem ) ) continue;
	        } else {
                  if ( ! station_splay.isStationON( sp.mItem ) ) continue;
	        }
              }
	    } else if ( type == DrawingPath.DRAWING_PATH_POINT
	             || type == DrawingPath.DRAWING_PATH_LINE 
	             || type == DrawingPath.DRAWING_PATH_AREA ) {
              if ( ! DrawingLevel.isLevelVisible( sp.mItem ) ) continue;
            }
              
            if ( sp.distance( x, y ) < radius ) {
              // TDLog.v( "pt " + sp.mPoint.x + " " + sp.mPoint.y + " dist " + sp.getDistance() );
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
      // TDLog.v("pt " + sp.mPoint.x + " " + sp.mPoint.y + " type " + type );
      for ( SelectionBucket bucket : mBuckets ) {
        if ( bucket.contains( x, y, radius, radius ) ) {
          for ( SelectionPoint sp : bucket.mPoints ) {
            if ( sp.type() == type && DrawingLevel.isLevelVisible( sp.mItem ) ) {
              if ( sp.distance( x, y ) < radius ) sel.addPoint( sp );
            }
          }
        }
      }
    }
  }
 
  /** select by type
   * @param sel    selection set (to be filled)
   * @param x      X coord
   * @param y      Y coord
   * @param radius selection radius
   * @param type   selectable items type
   */
  void selectAt( SelectionSet sel, float x, float y, float radius, int type )
  {
    bucketSelectAt( x, y, radius, type, sel );
  }

  /** select by type
   * @param x      X coord
   * @param y      Y coord
   * @param radius selection radius
   * @param type   selectable items type
   * @param sel    selection set (to be filled)
   */
  private void bucketSelectAt(float x,float y,float radius,int type, SelectionSet sel )
  {
    // TDLog.v( "bucket select at " + x + " " + y + " R " + radius + " buckets " + mBuckets.size() );
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x, y, radius, radius ) ) {
        for ( SelectionPoint sp : bucket.mPoints ) {
          if ( sp.type() == type && DrawingLevel.isLevelVisible( sp.mItem ) && sp.distance( x, y ) < radius ) {
            // TDLog.v( "pt " + sp.mPoint.x + " " + sp.mPoint.y + " dist " + sp.getDistance() );
            sel.addPoint( sp );
          }
        }
      }
    } 
  }
  
  /** select by type
   * @param sel      selection set (to be filled)
   * @param x        X coord
   * @param y        Y coord
   * @param radius   selection radius
   * @param mode     ...
   * @param legs     whether to include legs
   * @param splays   whether to include splays
   * @param stations whether to include stations
   * @param station_splay ...
   */
  void selectAt( SelectionSet sel, float x, float y, float radius, int mode, boolean legs, boolean splays, boolean stations, DrawingStationSplay station_splay )
  {
    bucketSelectAt( x, y, radius, mode, sel, legs, splays, stations, station_splay );
  }

  /** @return the bucket containing (x,y)
   * @param x   X coord
   * @param y   Y coord
   */
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

  /** check if the SelectionPoint is still in its bucket - otherwise move it to the new bucket
   * @param sp   selection point
   */
  void checkBucket( SelectionPoint sp ) 
  {
    if ( sp == null ) return;
    if ( sp.mRange != null ) {
      // if ( sp.mRange.start() != null || sp.mRange.end() != null ) {
        rebucketLinePath( (DrawingPointLinePath)sp.mItem );
      // }
    } else {
      SelectionBucket sb = sp.mBucket;
      if ( sb != null && ! sb.contains( sp.X(), sp.Y() ) ) {
        // find the bucket that contains sp and assign it to sp
        sb = getBucket( sp.X(), sp.Y() );
        sp.setBucket( sb ); // this removes sp from its old bucket and add it to the new bucket
      }
    }
  }

  /** re-bucket a point-line
   * @param line   point-line item
   */
  private void rebucketLinePath(DrawingPointLinePath line)
  {
    for ( LinePoint lp = line.first(); lp != null; lp = lp.mNext ) {
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
  // {a
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
  //   TDLog.v("SELECT Selection points " + mPoints.size() + " " + nfxd + " " + nspl + " " + ngrd + " " + nsta 
  //                   + " " + npnt + " " + nlin + " " + nare + " " + nnam + " " + nnrt + " " + noth  );
  // }
}
