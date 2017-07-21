/** @file SelectionPoint.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief a point in the selection set
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

class SelectionPoint
{
  // scene coord (x, y )
  // for DrawingStationName (x,y) = st.(x,y)
  // for DrawingPath        (x,y) = (cx,cy)
  // for DrawingStationPath (x,y) = path.(xpos,ypos)
  // for DrawingPointPath   (x,y) = path.(xpos,ypos)
  // for DrawingLinePath    (x.y) = midpoint between each two line points
  // for DrawingAreaPath    (x,y) = midpoint between each two border points

  DrawingPath mItem;
  LinePoint   mPoint;
  SelectionBucket mBucket = null;

  private float mDistance; // distance from input (X,Y)
  private int mMin;        // whether to shift the point (0) or a CP (1 or 2)

  private int mRangeType; // range shift type
  LinePoint mLP1 = null;  // range shift endpoints
  LinePoint mLP2 = null;
  private float mD1;      // range shift endpoint distances
  private float mD2;
  
  void setRangeTypeAndPoints( int type, LinePoint lp1, LinePoint lp2, float d1, float d2 )
  {
    mRangeType = type;
    mLP1 = lp1;
    mLP2 = lp2;
    mD1  = d1;
    mD2  = d2;
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
    if ( mItem.mType == DrawingPath.DRAWING_PATH_POINT ) {
      return mItem.rotateBy( dy );
    }
    return false;
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
          if ( mLP2 != null ) {
            if ( mRangeType == 1 ) {
              float d0 = 0;
              LinePoint lp0 = mPoint;
              for ( LinePoint lp = mPoint.mNext; lp != mLP2 && lp != null; lp=lp.mNext ) {
                d0 += lp0.distance( lp );
                float d = 2*d0/(0.1f+mD2);
                d = d*d;
                d = 1/(1+d*d);
                lp.shiftBy( d*dx, d*dy );
                lp0 = lp;
              }
            } else {
              for ( LinePoint lp = mPoint.mNext; lp != mLP2 && lp != null; lp=lp.mNext ) {
                lp.shiftBy( dx, dy );
              }
              mLP2.shiftBy( dx, dy );
            }
          }
          if ( mLP1 != null ) {
            if ( mRangeType == 1 ) {
              float d0 = 0;
	      LinePoint lp0 = mPoint;
              for ( LinePoint lp = mPoint.mPrev; lp != mLP1 && lp != null; lp=lp.mPrev ) {
                d0 += lp0.distance( lp );
                float d = 2*d0/(0.1f+mD1);
                d = d*d;
                d = 1/(1+d*d);
                lp.shiftBy( d*dx, d*dy );
                lp0 = lp;
              }
            } else {
              for ( LinePoint lp = mPoint.mPrev; lp != mLP1 && lp != null; lp=lp.mPrev ) {
                lp.shiftBy( dx, dy );
              }
              mLP1.shiftBy( dx, dy );
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
 
  void setBucket( SelectionBucket bucket )
  {
    if ( mBucket == bucket ) return;
    if ( mBucket != null ) mBucket.removePoint( this );
    mBucket = bucket;
    if ( mBucket != null ) mBucket.addPoint( this );
  }

}
