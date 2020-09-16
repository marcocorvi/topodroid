/* @file SelectionPoint.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief a point in the selection set
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

import android.graphics.Matrix;

public class SelectionPoint
{
  // scene coord (x, y )
  // for DrawingStationName (x,y) = st.(x,y)
  // for DrawingPath        (x,y) = (cx,cy)
  // for DrawingStationPath (x,y) = path.(xpos,ypos)
  // for DrawingPointPath   (x,y) = path.(xpos,ypos)
  // for DrawingLinePath    (x.y) = midpoint between each two line points
  // for DrawingAreaPath    (x,y) = midpoint between each two border points

  public DrawingPath mItem;
  public LinePoint   mPoint;

  SelectionBucket mBucket = null;

  private float mDistance; // distance from input (X,Y)
  private int mMin;        // whether to shift the point (0) or a CP (1 or 2)

  SelectionRange mRange;

  // range is from lp1 to lp2 - see DrawingCommandManager.setRangeAt()
  void setRangeTypeAndPoints( int type, LinePoint lp1, LinePoint lp2, float d1, float d2 )
  {
    mRange = new SelectionRange( type, lp1, lp2, d1, d2 );
  }

  void setDistance( float d ) { mDistance = d; }
  float getDistance() { return mDistance; }

  int type() { return mItem.mType; }
  // DRAWING_PATH_FIXED   = 0; // leg
  // DRAWING_PATH_SPLAY   = 1; // splay
  // DRAWING_PATH_GRID    = 2; // grid
  // DRAWING_PATH_STATION = 3; // station point
  // DRAWING_PATH_POINT   = 4; // drawing point
  // DRAWING_PATH_LINE    = 5;
  // DRAWING_PATH_AREA    = 6;
  // DRAWING_PATH_NAME    = 7; // station name
  // DRAWING_PATH_NORTH   = 8

  boolean isReferenceType() { return DrawingPath.isReferenceType( mItem.mType ); }

  boolean isDrawingType() { return DrawingPath.isDrawingType( mItem.mType ); }

  SelectionPoint( DrawingPath it, LinePoint pt, SelectionBucket bucket )
  {
    mItem = it;
    mPoint = pt;
    mDistance = 0.0f;
    mMin = 0;
    setBucket( bucket );
  }

  String name()
  {
    if ( mItem.mBlock != null ) {
      return mItem.mBlock.mFrom + " " + mItem.mBlock.mTo + " " + X() + " " + Y();
    } else {
      return X() + " " + Y();
    }
  }


  float X() { return ( mPoint != null )? mPoint.x : mItem.cx; }
  float Y() { return ( mPoint != null )? mPoint.y : mItem.cy; }

  // distance from a scene point (xx, yy)
  // as side effect set mDistance to the result
  float distance( float xx, float yy )
  {
    mMin = 0; // index of the point that has achived the min distance
    if ( mPoint != null ) {
      float d = mPoint.distance( xx, yy );
      if ( mPoint.has_cp ) {
        float d1 = mPoint.distanceCP1( xx, yy );
        float d2 = mPoint.distanceCP2( xx, yy );
        if ( d <= d1 ) {
          if ( d <= d2 ) { // d < d1 and d < d2
            mDistance = d;
            // return d;
          } else {         // d2 < d < d1
            mMin = 2;
            mDistance = d2;
            // return d2;
          }
        } else {           // d1 < d and d1 < d2
          if ( d1 < d2 ) {
            mMin = 1;
            mDistance = d1;
            // return d1;
          } else {         // d2 < d1 < d
            mMin = 2;
            mDistance = d2;
            // return d2;
          }
        }
      } else {
        mDistance = d;
        // return d;
      }
    } else {
      mDistance = mItem.distanceToPoint( xx, yy );
      // return mItem.distanceToPoint( xx, yy );
    }
    return mDistance;
  }

  boolean rotateBy( float dy )
  {
    return ( mItem.mType == DrawingPath.DRAWING_PATH_POINT ) && mItem.rotateBy( dy );
  }


  // void shiftBy( float dx, float dy, float range )
  void shiftBy( float dx, float dy )
  {
    if ( mPoint != null ) {
      DrawingPointLinePath item = (DrawingPointLinePath)mItem;
      switch ( mMin ) {
        case 1 : mPoint.shiftCP1By( dx, dy ); break;
        case 2 : mPoint.shiftCP2By( dx, dy ); break;
        default: 
          mPoint.shiftBy( dx, dy );
          // if ( range > 0f ) {
          //   float d0 = 0;
          //   for ( LinePoint lp = mPoint.mNext; lp != null; lp = lp.mNext ) {
          //     d0 += mPoint.distance( lp );
          //     if ( d0 > range ) break;
          //     float d = 2*d0/(0.1f+range);  // window fct = 1 / ( 1 + (2d/r)^4 )
          //     d = d*d;
          //     d = 1/(1+d*d);
          //     lp.shiftBy( d*dx, d*dy );
          //   }
          //   d0 = 0;
          //   for ( LinePoint lp = mPoint.mPrev; lp != null; lp = lp.mPrev ) {
          //     d0 += mPoint.distance( lp );
          //     if ( d0 > range ) break;
          //     float d = 2*d0/(0.1f+range);
          //     d = d*d;
          //     d = 1/(1+d*d);
          //     lp.shiftBy( d*dx, d*dy );
          //   }
          // }
          if ( mRange != null ) {
            if ( mRange.isItem() ) { // FIXME item-shift could be a for on item points - but the point should not have been shifted already
              for ( LinePoint lp = mPoint.mPrev; lp != null; lp=lp.mPrev ) {
                lp.shiftBy( dx, dy );
              }
              for ( LinePoint lp = mPoint.mNext; lp != null; lp=lp.mNext ) {
                lp.shiftBy( dx, dy );
              }
            } else { // isSoft() || isHard()
              LinePoint lp2 = mRange.end();
              if ( lp2 != null ) {
                if ( mRange.isSoft() ) {
                  float d0 = 0;
                  LinePoint lp0 = mPoint;
                  float dd = 2.0f/( 0.1f + mRange.endDistance() );
                  for ( LinePoint lp = mPoint.mNext; lp != lp2 && lp != null; lp=lp.mNext ) {
                    d0 += lp0.distance( lp );
                    float d = dd * d0;
                    d = d*d;
                    d = 1/(1+d*d);
                    lp.shiftBy( d*dx, d*dy );
                    lp0 = lp;
                  }
                  mRange.setEndDistance( d0 );
                } else { // if ( mRange.isHard() || mRange.isItem() )
                  for ( LinePoint lp = mPoint.mNext; lp != lp2 && lp != null; lp=lp.mNext ) {
                    lp.shiftBy( dx, dy );
                  }
                  lp2.shiftBy( dx, dy );
                }
              }
              LinePoint lp1 = mRange.start();
              if ( lp1 != null ) {
                if ( mRange.isSoft() ) {
                  float d0 = 0;
                  float dd = 2.0f/( 0.1f + mRange.startDistance() );
	          LinePoint lp0 = mPoint;
                  for ( LinePoint lp = mPoint.mPrev; lp != lp1 && lp != null; lp=lp.mPrev ) {
                    d0 += lp0.distance( lp );
                    float d = dd * d0;
                    d = d*d;
                    d = 1/(1+d*d); // 1/(1 + d^4)
                    lp.shiftBy( d*dx, d*dy );
                    lp0 = lp;
                  }
                  mRange.setStartDistance( d0 );
                } else { // if ( mRange.isHard() || mRange.isItem() )
                  for ( LinePoint lp = mPoint.mPrev; lp != lp1 && lp != null; lp=lp.mPrev ) {
                    lp.shiftBy( dx, dy );
                  }
                  lp1.shiftBy( dx, dy );
                }
              }
            }
          }
          break;
      }
      item.retracePath();
    } else if ( mItem.mType == DrawingPath.DRAWING_PATH_POINT ) {
      mItem.shiftBy( dx, dy );
    }
  }

  void shiftSelectionBy( float dx, float dy )
  {
    if ( mPoint != null ) {
      // switch ( mMin ) {
      //  case 1 : mPoint.shiftCP1By( dx, dy ); break;
      //  case 2 : mPoint.shiftCP2By( dx, dy ); break;
      //  default: mPoint.shiftBy( dx, dy ); break;
      // }
      mPoint.shiftBy( dx, dy ); 
      DrawingPointLinePath item = (DrawingPointLinePath)mItem;
      item.retracePath();
    } else if ( mItem.mType == DrawingPath.DRAWING_PATH_POINT ) {
      mItem.shiftBy( dx, dy );
    }
  }

  void scaleSelectionBy( float z, Matrix m )
  {
    if ( mPoint != null ) {
      // switch ( mMin ) {
      //  case 1 : mPoint.shiftCP1By( dx, dy ); break;
      //  case 2 : mPoint.shiftCP2By( dx, dy ); break;
      //  default: mPoint.shiftBy( dx, dy ); break;
      // }
      mPoint.scaleBy( z, m );
      DrawingPointLinePath item = (DrawingPointLinePath)mItem;
      item.retracePath();
    } else if ( mItem.mType == DrawingPath.DRAWING_PATH_POINT ) {
      mItem.scaleBy( z, m );
    }
  }

  void affineTransformSelectionBy( float[] mm, Matrix m )
  {
    if ( mPoint != null ) {
      // switch ( mMin ) {
      //  case 1 : mPoint.shiftCP1By( dx, dy ); break;
      //  case 2 : mPoint.shiftCP2By( dx, dy ); break;
      //  default: mPoint.shiftBy( dx, dy ); break;
      // }
      mPoint.affineTransformBy( mm, m );
      DrawingPointLinePath item = (DrawingPointLinePath)mItem;
      item.retracePath();
    } else if ( mItem.mType == DrawingPath.DRAWING_PATH_POINT ) {
      mItem.affineTransformBy( mm, m );
    }
  }
 
  void setBucket( SelectionBucket bucket )
  {
    if ( mBucket == bucket ) return;
    if ( mBucket != null ) mBucket.removePoint( this );
    mBucket = bucket;
    if ( mBucket != null ) mBucket.addPoint( this );
  }

}
