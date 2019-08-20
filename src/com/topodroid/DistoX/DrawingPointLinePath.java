/* @file DrawingPointLinePath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: line of points
 *
 * The area border (line) path id DrawingPath.mPath
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
// import android.graphics.Matrix;


// import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

// used by Log.e
// import android.util.Log;

/**
 */
class DrawingPointLinePath extends DrawingPath
                           implements IDrawingLink
{
  private boolean mVisible; // visible line
  private boolean mClosed;
  // ArrayList< LinePoint > mPoints;      // points (scene coordinates)
  private LinePoint mPrevPoint = null; // previous point while constructing the line
  LinePoint mFirst;
  LinePoint mLast;
  private int mSize;  // number of points

  float mDx, mDy; // unit vector in the direction of this line

  int size() { return mSize; }

  // FIXME-COPYPATH
  // @Override
  // DrawingPath copyPath()
  // {
  //   DrawingPointLinePath ret = new DrawingPointLinePath( mType, mVisible, mClosed );
  //   copyTo( ret );
  //   return ret;
  // }
  //
  // @Override
  // protected void copyTo( DrawingPath p )
  // {
  //   DrawingPointLinePath path = (DrawingPointLinePath)p;
  //   super.copyTo( path );
  //   path.append( this );
  // }

  /* DEBUG
   * counts how many points this line overlaps with another line
   */
  int overlap( DrawingPointLinePath other ) 
  {
    int ret = 0;
    for (LinePoint l1 = mFirst; l1 != null; l1=l1.mNext ) {
      for ( LinePoint l2 = other.mFirst; l2 != null; l2 = l2.mNext ) {
        if ( Math.abs( l1.x - l2.x ) < 0.001f && Math.abs( l1.y - l2.y ) < 0.001f ) {
          ++ ret;
          break;
        }
      }
    }
    return ret;
  }

  // @implements IDrawingLink
  public float getLinkX( ) { return mLast.x; }
  public float getLinkY( ) { return mLast.y; }

  @Override
  float getX() { return (left+right)/2; }

  @Override
  float getY() { return (top+bottom)/2; }
  // void dump() // DEBUG
  // {
  //   int k=0;
  //   for (LinePoint l1 = mFirst; l1 != null; l1=l1.mNext ) {
  //     // if ( k < 2 || k > mSize-2 ) 
  //     {
  //       Log.v("DistoX", k + ": " + l1.x + " " + l1.y );
  //     }
  //     ++k;
  //   }
  // }

  @Override
  void shiftBy( float dx, float dy )
  {
    left   += dx;
    right  += dx;
    top    += dy;
    bottom += dy;
    x1 += dx;
    y1 += dy;
    x2 += dx;
    y2 += dy;
    cx += dx;
    cy += dy;
    for ( LinePoint lp = mFirst; lp != null; lp = lp.mNext ) lp.shiftBy( dx, dy );
    retracePath();
  }

  void moveFirstTo( float x, float y )
  {
    mFirst.x = x;
    mFirst.y = y;
    retracePath();
  }
    
  void moveLastTo( float x, float y )
  {
    mLast.x = x;
    mLast.y = y;
    retracePath();
  }

  // from ICanvasCommand
  @Override
  public void flipXAxis( float z )
  {
    super.flipXAxis(z);
    for ( LinePoint lp = mFirst; lp != null; lp = lp.mNext ) lp.flipXAxis(z);
    retracePath();
  }

  void recount() // throws Exception
  {
    if ( mFirst == null ) {
      mSize = 0;
      mLast = null;
      return;
    }
    mSize = 1;
    for ( LinePoint p = mFirst.mNext; p != null; p = p.mNext ) {
      if ( p == mFirst ) break;
      // Log.v( "DistoX", "[>] " + p.x + " " + p.y );
      ++ mSize;
    }
    // CHECK;
    // int size = 1;
    // for ( LinePoint p = mLast.mPrev; p != null; p = p.mPrev ) {
    //   if ( p == mLast ) break;
    //   // Log.v( "DistoX", "[<] " + p.x + " " + p.y );
    //   ++ size;
    // }
    // if ( size != mSize ) {
    //   TDLog.Error( "recount size mismatch " + mSize + " " + size );
    //   // throw new Exception("size mismatch");
    // }
  }

  void landscapeToPortrait()
  {
    for ( LinePoint lp = mFirst; lp != null; lp = lp.mNext ) lp.landscapeToPortrait();
    retracePath();
  }


  DrawingPointLinePath( int path_type, boolean visible, boolean closed )
  {
    super( path_type, null ); // DrawingPath.DRAWING_PATH_AREA );
    // mPoints  = new ArrayList<>();
    clear();
    mVisible = visible;
    mClosed  = closed;
    mFirst   = null;
    mLast    = null;
    mSize    = 0;
    mDx = mDy = 0;
  }

  void setClosed( boolean closed ) { mClosed = closed; }
  boolean isClosed() { return mClosed; }
  boolean isPathClosed() 
  {
    if ( mSize < 2 ) return false;
    float dx = (mFirst.x - mLast.x)/3;
    float dy = (mFirst.y - mLast.y)/3;
    return ( dx*dx + dy*dy < 1.0e-7 );
  }

  void setVisible( boolean visible ) { mVisible = visible; }
  boolean isVisible() { return mVisible; }

  void computeUnitNormal() { mDx = mDy = 0; }

  /** unlink a line_point
   */
  void remove( LinePoint lp )
  {
    if ( lp == mFirst ) {
      mFirst = lp.mNext;
    } else if ( lp.mPrev != null ) {
      lp.mPrev.mNext = lp.mNext;
    }
    if ( lp == mLast ) {
      mLast = lp.mPrev;
    } else if ( lp.mNext != null ) {
      lp.mNext.mPrev = lp.mPrev;
    }
    lp.mNext = null;
    lp.mPrev = null;
    -- mSize;
    computeUnitNormal();
  }

  LinePoint next( LinePoint lp )
  {
    if ( lp == null ) return null;
    return lp.mNext;
  }

  LinePoint prev( LinePoint lp )
  {
    if ( lp == null ) return null;
    return lp.mPrev;
  }

  void makeSharp( )
  {
    // FIXME this was here: retracePath();
    for ( LinePoint lp = mFirst; lp != null; lp = lp.mNext ) {
      lp.has_cp = false;
    }
    retracePath();
  }

  // @param decimation   log-decimation (must be >= 1)
  //                     1: keep one point every 2
  //                     2: keep one point every 4
  // @param min_size     2: line, 3: area
  void makeReduce( int decimation, int min_size )
  {
    while ( decimation > 0 ) {
      if ( mSize > min_size ) {
        int size = 1;  // keep first point 
        LinePoint prev = mFirst;
        LinePoint pt = prev.mNext;
        while ( pt != mLast && pt != null ) {
          LinePoint next = pt.mNext; // pt.mNext != null because pt < mLast
          prev.mNext = next;
          next.mPrev = prev; 
          ++ size;
          prev = next;
          pt = prev.mNext;
        }
        if ( pt == mLast ) ++ size; // for the mLast point
        mSize = size;     
      }    
      -- decimation;
    }
    retracePath();
  }

  void makeRock()
  {
    if ( mSize > 2 ) {
      int size = 1;
      LinePoint prev = mFirst;
      LinePoint pt = mFirst.mNext;
      while ( pt != mLast ) {
        LinePoint next = pt.mNext; // pt.mNext != null because pt < mLast
        float x1 = pt.x - prev.x;
        float y1 = pt.y - prev.y;
        float x2 = next.x - pt.x;
        float y2 = next.y - pt.y;
        float cos = (x1*x2 + y1*y2)/(float)Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2));
        if ( cos >= TDSetting.mReduceCosine ) {
          prev.mNext = next;
          next.mPrev = prev; 
        } else {
          ++ size;
          prev = pt;
        }
        pt = next;
      }
      ++ size; // for the mLast point
      mSize = size;     
    }    
    retracePath();
  }

  void makeClose( )
  {
    if ( mSize > 2 ) {
      float dx = (mFirst.x - mLast.x)/3;
      float dy = (mFirst.y - mLast.y)/3;
      if ( dx*dx + dy*dy > 1.0e-7 ) {
        if ( mLast.has_cp ) {
          mLast.x1 += dx;
          mLast.y1 += dy;
          mLast.x2 += dx*2;
          mLast.y2 += dy*2;
        }
        mLast.x = mFirst.x;
        mLast.y = mFirst.y;
        retracePath();
      }
    }    
  }

  private void clear()
  {
    mFirst = null;
    mLast  = null;
    mPath = new Path();
    mSize = 0;
    left   = 0;
    right  = 0;
    top    = 0;
    bottom = 0;
    mDx = 0;
    mDy = 0;
  }


  void makeStraight( )
  {
    // if ( mPoints.size() < 2 ) return;
    // LinePoint first = mPoints.get( 0 );
    // LinePoint last  = mPoints.get( mPoints.size() - 1 );
    if ( mSize < 2 ) return;
    LinePoint first = mFirst;
    LinePoint last  = mLast;

    clear();
    addStartPoint( first.x, first.y );
    addPoint( last.x, last.y );
    computeUnitNormal();
    // Log.v( TopoDroidApp.TAG, "make straight final size " + mPoints.size() );
  }
    
  void addStartPoint( float x, float y ) 
  {
    // mPrevPoint = new LinePoint(x,y, null);
    // mPoints.add( mPrevPoint );
    mLast = new LinePoint(x,y, null);
    ++ mSize;
    mFirst = mLast;
    mPath.moveTo( x, y );
    left = right  = x;
    top  = bottom = y;
  }

  void addPoint( float x, float y ) 
  {
    if ( Float.isNaN(x) || Float.isNaN(y) ) return;
    if ( mFirst == null ) {
      addStartPoint( x, y );
    } else {
      // mPrevPoint = new LinePoint(x,y,mPrevPoint);
      // mPoints.add( mPrevPoint );
      mLast = new LinePoint(x, y, mLast);
      ++ mSize;
      mPath.lineTo( x, y );
      if ( x < left ) { left = x; } else if ( x > right  ) { right  = x; }
      if ( y < top  ) { top  = y; } else if ( y > bottom ) { bottom = y; }
    }
  }

  void addPoint3( float x1, float y1, float x2, float y2, float x, float y ) 
  {
    if ( Float.isNaN(x) || Float.isNaN(y) ) return;
    if ( mFirst == null ) {
      addStartPoint( x, y );
    } else {
      if ( Float.isNaN( x1 ) || Float.isNaN( y1 ) || Float.isNaN( x2 ) || Float.isNaN( y2 ) ) {
        mLast = new LinePoint(x, y, mLast);
        ++ mSize;
        mPath.lineTo( x, y );
      } else {
        // mPrevPoint = new LinePoint( x1,y1, x2,y2, x,y, mPrevPoint );
        // mPoints.add( mPrevPoint );
        mLast = new LinePoint( x1,y1, x2,y2, x,y, mLast );
        ++mSize;
        mPath.cubicTo( x1,y1, x2,y2, x,y );
      }
      if ( x < left ) { left = x; } else if ( x > right  ) { right  = x; }
      if ( y < top  ) { top  = y; } else if ( y > bottom ) { bottom = y; }
    }
  }

  // ----------------------------------------------
  void addStartPointNoPath( float x, float y ) 
  {
    mFirst = mLast = new LinePoint(x,y, null);
    ++ mSize;
    left = right  = x;
    top  = bottom = y;
  }

  void addPointNoPath( float x, float y ) 
  {
    mLast = new LinePoint(x, y, mLast);
    ++ mSize;
    if ( x < left ) { left = x; } else if ( x > right  ) { right  = x; }
    if ( y < top  ) { top  = y; } else if ( y > bottom ) { bottom = y; }
  }

  void addPoint3NoPath( float x1, float y1, float x2, float y2, float x, float y ) 
  {
    mLast = new LinePoint( x1,y1, x2,y2, x,y, mLast );
    ++mSize;
    if ( x < left ) { left = x; } else if ( x > right  ) { right  = x; }
    if ( y < top  ) { top  = y; } else if ( y > bottom ) { bottom = y; }
  }
  // ----------------------------------------------

  void append( DrawingPointLinePath line )
  {
    if ( line.mSize ==  0 ) return;
    LinePoint lp = line.mFirst;
    addPoint( lp.x, lp.y );
    for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
      if ( lp.has_cp ) {
        addPoint3( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
      } else {
        addPoint( lp.x, lp.y );
      }
    }
    // computeUnitNormal();
  }

  void resetPath( ArrayList<LinePoint> pts )
  {
    clear();
    // if ( pts == null ) return;
    int size = pts.size();
    if ( size <= 1 ) return;
    LinePoint lp = pts.get(0);
    addStartPoint( lp.x, lp.y );
    for ( int k=1; k<size; ++k ) {
      lp = pts.get(k);
      if ( lp.has_cp ) {
        addPoint3( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
      } else {
        addPoint( lp.x, lp.y );
      }
    }
    computeUnitNormal();
  }
     

  LinePoint insertPointAfter( float x, float y, LinePoint lp )
  {
    if ( Float.isNaN(x) || Float.isNaN(y) ) return null;
    // int index = mPoints.indexOf(lp);
    // if ( index < mPoints.size() ) ++index; // insert before next point
    LinePoint next = lp.mNext;
    LinePoint pp = new LinePoint(x, y, lp );
    ++mSize;
    pp.mNext = next;
    if ( next != null ) next.mPrev = pp;
    // mPoints.add(index, pp);
    retracePath();
    return pp;
  }

  void recomputeSize()
  {
    mSize = 0;
    for ( LinePoint lp = mFirst; lp != null; lp = lp.mNext ) ++ mSize;
  }

  void retracePath()
  {
    // int size = mPoints.size();
    // if ( size == 0 ) return;
    // mPath = new Path();
    // LinePoint lp = mPoints.get(0);
    // mPath.moveTo( lp.x, lp.y );
    // for ( int k=1; k<size; ++k ) {
    //   lp = mPoints.get(k);
    //   if ( lp.has_cp ) {
    //     mPath.cubicTo( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
    //   } else {
    //     mPath.lineTo( lp.x, lp.y );
    //   }
    // }
    if ( mSize == 0 ) return;
    mPath = new Path();
    LinePoint lp  = mFirst;
    left = right  = lp.x;
    top  = bottom = lp.y;
    mPath.moveTo( lp.x, lp.y );
    for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
      if ( lp.has_cp ) {
        mPath.cubicTo( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
      } else {
        mPath.lineTo( lp.x, lp.y );
      }
      if ( lp.x < left ) { left = lp.x; } else if ( lp.x > right  ) { right  = lp.x; }
      if ( lp.y < top  ) { top  = lp.y; } else if ( lp.y > bottom ) { bottom = lp.y; }
    }
    if ( mClosed ) mPath.close();
    computeUnitNormal();
  }

  void reversePath()
  {
    if ( mSize == 0 ) return;
    LinePoint lf = mFirst;
    LinePoint ll = mLast;
    clear();
    // mPath = new Path();
    // mFirst = null;
    // mLast  = null;
    LinePoint lp = ll;
    addStartPoint( lp.x, lp.y );
    LinePoint prev = lp.mPrev;
    while ( prev != null ) {
      if ( lp.has_cp ) {
        addPoint3( lp.x2, lp.y2, lp.x1, lp.y1, prev.x, prev.y );
      } else {
        addPoint( prev.x, prev.y );
      }
      lp = prev;
      prev = prev.mPrev;
    }
    if ( mClosed ) mPath.close();
    computeUnitNormal(); // FIXME 
  }

  @Override
  float distanceToPoint( float x, float y )
  {
    if ( Float.isNaN(x) || Float.isNaN(y) ) return Float.NaN;
    float dist = 10000000f; // FIXME
    // for ( LinePoint pt : mPoints ) 
    for ( LinePoint pt=mFirst; pt != null; pt = pt.mNext ) 
    {
      float dx = x - pt.x;
      float dy = y - pt.y;
      float d2 = dx*dx + dy*dy;
      if ( d2 < dist ) dist = d2;
    }
    return (dist > 0 )? TDMath.sqrt(dist) : 0;
  }

  void closePath() 
  {
    mPath.close();
    // Log.v( TopoDroidApp.TAG, "area close path" );
  }

  // ArrayList< LinePoint > getPoints() { return mPoints; }

  // int size() { return mPoints.size(); }

  // @Override
  // void draw( Canvas canvas )
  // {
  //   super.draw( canvas );
  //   // Path path = new Path();
  //   // path.addCircle( 0, 0, 1, Path.Direction.CCW );
  //   // for ( LinePoint lp : mPoints ) {
  //   //   Path path1 = new Path( path );
  //   //   path1.offset( lp.x, lp.y );
  //   //   canvas.drawPath( path1, BrushManager.highlightPaint );
  //   // }
  // }

  // @Override
  // void draw( Canvas canvas, Matrix matrix )
  // {
  //   super.draw( canvas, matrix );
  //   // Path path = new Path();
  //   // path.addCircle( 0, 0, 1, Path.Direction.CCW );
  //   // for ( LinePoint lp : mPoints ) {
  //   //   Path path1 = new Path( path );
  //   //   path1.offset( lp.x, lp.y );
  //   //   path1.transform( matrix );
  //   //   canvas.drawPath( path1, BrushManager.highlightPaint );
  //   // }
  // }

  protected void toTherionPoints( PrintWriter pw, boolean close )
  {
    // for ( LinePoint pt : mPoints ) 
    LinePoint pt = mFirst; 
    pt.toTherion( pw );
    float x0 = DrawingUtil.sceneToWorldX( pt );
    float y0 = DrawingUtil.sceneToWorldY( pt );
    float delta = 0;
    // Log.v("DistoX", "X " + x0 + " Y " + y0 );
    // if ( mLineType == BrushManager.mLineLib.mLineSectionIndex && size() > 2 ) pt = pt.mNext; // skip first point (tick)
    for ( pt = pt.mNext; pt != null; pt = pt.mNext ) {
      float x3 = DrawingUtil.sceneToWorldX( pt );
      float y3 = DrawingUtil.sceneToWorldY( pt );
      if ( pt.has_cp ) { 
        pt.toTherion( pw );
	delta = 0;
        // Log.v("DistoX", "X " + x3 + " Y " + y3 + " with CP " );
      } else {
	delta += TDMath.sqrt( (x3-x0)*(x3-x0) + (y3-y0)*(y3-y0) );
        // Log.v("DistoX", "X " + x3 + " Y " + y3 + " Delta " + delta );
	if ( delta > TDSetting.mBezierStep ) {
          pt.toTherion( pw );
	  delta = 0;
	}
      }
      x0 = x3;
      y0 = y3;
    }
    if ( close ) { // insert start point again if closed
      float dx = mLast.x - mFirst.x;
      float dy = mLast.y - mFirst.y;
      if ( dx*dx + dy*dy > 1.0e-7 ) {
        mFirst.toTherion( pw );
      }
    }
  }
}

