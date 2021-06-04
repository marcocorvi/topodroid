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

import android.util.Log;

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

  void clearSelectionPoints() // synchronized by CommandManager
  {
    mPoints.clear();
    mBuckets.clear();
  }

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
    // Log.v("DistoX-HIDE", "selection clear reference points " + cnt );
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

  void insertStationName( DrawingStationName st )
  {
    insertItem( st, null );
  }

  /** like insertItem, but it returns the inserted SelectionPoint
   * @param path     point-line path
   * @param pt       new point on the point-line
   * @return newly cretaed selection point
   */
  SelectionPoint insertPathPoint( DrawingPointLinePath path, LinePoint pt ) // synchrinized by CommandManager
  {
    SelectionPoint sp = new SelectionPoint( path, pt, null );
    mPoints.add( sp );
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );

    // Log.v("DistoX", "Selection insert path point " + pt.x + " " + pt.y );
    // sp.mBucket.dump();
    return sp;
  }
  
  void insertLinePath( DrawingLinePath path ) // synchronized by CommandManager
  {
    for ( LinePoint p2 = path.first(); p2 != null; p2 = p2.mNext ) {
      insertItem( path, p2 );
    }
  }

  void insertPath( DrawingPath path ) // synchronized by CommandManager
  {
    // Log.v("DistoX", "Selection insert path" );
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

  void resetDistances()
  {
    // Log.v("DistoX", "Selection reset distances" );
    for ( SelectionPoint pt : mPoints ) {
      pt.setDistance( 0.0f );
    }
  }

  void rebucket( SelectionPoint sp ) // synchronized by CommandManager
  {
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );
  }

  private void insertItem( DrawingPath path, LinePoint pt )
  {
    SelectionPoint sp = new SelectionPoint( path, pt, null ); // OOM Exception
    mPoints.add( sp );
    sp.setBucket( getBucket( sp.X(), sp.Y() ) );

    // if ( pt != null ) {
    //   Log.v("DistoX-SELECT", "insert item path type " + path.mType + " pt " + pt.x + " " + pt.y );
    // } else {
    //   Log.v("DistoX-SELECT", "insert item path type " + path.mType + " null pt ");
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

  void removeLineLastPoint( DrawingLinePath line, LinePoint pt )
  {
    SelectionPoint sp = getBucketLinePoint( line, pt );
    if ( sp != null ) removePoint( sp );
  }

  void removePoint( SelectionPoint sp ) // synchronized by CommandManager
  {
    sp.setBucket( null );
    mPoints.remove( sp ); 
  }

  // for incremental update
  void removeSplayPath( DrawingPath path ) // synchronized by CommandManager
  {
    for ( SelectionPoint sp : mPoints ) {
      if ( sp.mItem == path ) {
        removePoint( sp );
        break;
      }
    }
  }

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

  void removeLinePoint( DrawingPointLinePath path, LinePoint lp ) // snchronized by CommandManager
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


  SelectionPoint selectOnItemAt( DrawingPath item, float x, float y, float radius ) // synchronized by CommandManager
  {
    return bucketSelectOnItemAt( item, x, y, radius );
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

  private void bucketSelectAt(float x,float y,float radius,int mode,SelectionSet sel,boolean legs,boolean splays,boolean stations, DrawingStationSplay station_splay )
  {
    // Log.v("DistoX", "bucket select at " + x + " " + y + " R " + radius + " buckets " + mBuckets.size() );
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
      // Log.v("DistoX-SELECT", "pt " + sp.mPoint.x + " " + sp.mPoint.y + " type " + type );
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
 
  // select by type
  void selectAt( SelectionSet sel, float x, float y, float radius, int type )
  {
    bucketSelectAt( x, y, radius, type, sel );
  }

  private void bucketSelectAt(float x,float y,float radius,int type, SelectionSet sel )
  {
    // Log.v("DistoX", "bucket select at " + x + " " + y + " R " + radius + " buckets " + mBuckets.size() );
    for ( SelectionBucket bucket : mBuckets ) {
      if ( bucket.contains( x, y, radius, radius ) ) {
        for ( SelectionPoint sp : bucket.mPoints ) {
          if ( sp.type() == type && DrawingLevel.isLevelVisible( sp.mItem ) && sp.distance( x, y ) < radius ) {
            // Log.v("DistoX", "pt " + sp.mPoint.x + " " + sp.mPoint.y + " dist " + sp.getDistance() );
            sel.addPoint( sp );
          }
        }
      }
    } 
  }
  
  void selectAt( SelectionSet sel, float x, float y, float radius, int mode, boolean legs, boolean splays, boolean stations, DrawingStationSplay station_splay )
  {
    bucketSelectAt( x, y, radius, mode, sel, legs, splays, stations, station_splay );
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
  //   Log.v("DistoX-SELECT", "Selection points " + mPoints.size() + " " + nfxd + " " + nspl + " " + ngrd + " " + nsta 
  //                   + " " + npnt + " " + nlin + " " + nare + " " + nnam + " " + nnrt + " " + noth  );
  // }
}
