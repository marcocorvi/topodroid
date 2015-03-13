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
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20120725 TopoDroidApp log
 * 20121210 symbol area lib
 * 20121225 added "visible" border attribute
 * 201312   method to make the path sharp, straight (section line) and insert point
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import android.util.FloatMath;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

// used by Log.e
import android.util.Log;

/**
 */
public class DrawingPointLinePath extends DrawingPath
{
  boolean mVisible; // visible line
  boolean mClosed;
  // ArrayList< LinePoint > mPoints;      // points (scene coordinates)
  private LinePoint mPrevPoint = null; // previous point while constructing the line
  LinePoint mFirst;
  LinePoint mLast;
  private int mSize;  // number of points

  int size() { return mSize; }

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
      // Log.v( "DistoX", "[>] " + p.mX + " " + p.mY );
      if ( ++ mSize > 100 ) break;;
    }
    // CHECK;
    int size = 1;
    for ( LinePoint p = mLast.mPrev; p != null; p = p.mPrev ) {
      if ( p == mLast ) break;
      // Log.v( "DistoX", "[<] " + p.mX + " " + p.mY );
      if ( ++ size > 100 ) break;;
    }
    if ( size != mSize ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "recount size mismatch " + mSize + " " + size );
      // throw new Exception("size mismatch");
    }
  }

  public DrawingPointLinePath( int path_type, boolean visible, boolean closed )
  {
    super( path_type ); // DrawingPath.DRAWING_PATH_AREA );
    // mPoints  = new ArrayList< LinePoint >();
    clear();
    mVisible = visible;
    mClosed  = closed;
    mFirst   = null;
    mLast    = null;
    mSize    = 0;
  }

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

  void makeSharp( boolean reduce )
  {
    // for ( LinePoint lp : mPoints ) {
    //   lp.has_cp = false;
    // }
    // if ( reduce ) {
    //   int size = mPoints.size();
    //   if ( size > 2 ) {
    //     ArrayList pts = new ArrayList< LinePoint >();
    //     LinePoint prev = mPoints.get( 0 );
    //     pts.add( prev );
    //     LinePoint next = mPoints.get( 1 );
    //     for ( int k = 2; k < size; ++k ) {
    //       LinePoint pt = next;
    //       next = mPoints.get(k);
    //       float x1 = pt.mX - prev.mX;
    //       float y1 = pt.mY - prev.mY;
    //       float x2 = next.mX - pt.mX;
    //       float y2 = next.mY - pt.mY;
    //       float cos = (x1*x2 + y1*y2)/(float)(Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2)));
    //       if ( cos < 0.7 ) pts.add( pt );
    //     }
    //     pts.add( next );
    //     mPoints = pts;
    //     retracePath();
    //   }
    // }
    // FIXME this was here: retracePath();
    for ( LinePoint lp = mFirst; lp != null; lp = lp.mNext ) {
      lp.has_cp = false;
    }
    if ( reduce && mSize > 2 ) {
      int size = 1;
      LinePoint prev = mFirst;
      LinePoint pt = mFirst.mNext;
      while ( pt != mLast ) {
        LinePoint next = pt.mNext; // pt.mNext != null because pt < mLast
        float x1 = pt.mX - prev.mX;
        float y1 = pt.mY - prev.mY;
        float x2 = next.mX - pt.mX;
        float y2 = next.mY - pt.mY;
        float cos = (x1*x2 + y1*y2)/(float)(Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2)));
        if ( cos >= 0.7 ) {
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
      retracePath();
    }    
  }

  private void clear()
  {
    // mPoints.clear();
    mFirst = null;
    mLast  = null;
    mPath = new Path();
  }


  // with_arrow: put a arrow-tick before the first point
  void makeStraight( boolean with_arrow )
  {
    // Log.v( TopoDroidApp.TAG, "make straight with arrow " + with_arrow + " size " + mPoints.size() );
    // if ( mPoints.size() < 2 ) return;
    // LinePoint first = mPoints.get( 0 );
    // LinePoint last  = mPoints.get( mPoints.size() - 1 );
    if ( mSize < 2 ) return;
    LinePoint first = mFirst;
    LinePoint last  = mLast;

    clear();
    if ( with_arrow ) {
      float dy =   first.mX - last.mX;
      float dx = - first.mY + last.mY;
      float d = dx*dx + dy*dy;
      if ( d > 0.00001f ) {
        d = TopoDroidSetting.mArrowLength * TopoDroidSetting.mUnit / FloatMath.sqrt( d );
        dx *= d;
        dy *= d;
        addStartPoint( first.mX+dx, first.mY+dy );
        addPoint( first.mX, first.mY );
      } else {
        addStartPoint( first.mX, first.mY );
      }
    } else {
      addStartPoint( first.mX, first.mY );
    }
    addPoint( last.mX, last.mY );
    // Log.v( TopoDroidApp.TAG, "make straight final size " + mPoints.size() );
  }
    
  public void addStartPoint( float x, float y ) 
  {
    // mPrevPoint = new LinePoint(x,y, null);
    // mPoints.add( mPrevPoint );
    mLast = new LinePoint(x,y, null);
    ++ mSize;
    mFirst = mLast;
    mPath.moveTo( x, y );
  }

  public void addPoint( float x, float y ) 
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
    }
  }

  public void addPoint3( float x1, float y1, float x2, float y2, float x, float y ) 
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
    }
  }

  void append( DrawingPointLinePath line )
  {
    if ( line.mSize ==  0 ) return;
    LinePoint lp = line.mFirst;
    addPoint( lp.mX, lp.mY );
    for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
      if ( lp.has_cp ) {
        addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        addPoint( lp.mX, lp.mY );
      }
    }
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

  void retracePath()
  {
    // int size = mPoints.size();
    // if ( size == 0 ) return;
    // mPath = new Path();
    // LinePoint lp = mPoints.get(0);
    // mPath.moveTo( lp.mX, lp.mY );
    // for ( int k=1; k<size; ++k ) {
    //   lp = mPoints.get(k);
    //   if ( lp.has_cp ) {
    //     mPath.cubicTo( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
    //   } else {
    //     mPath.lineTo( lp.mX, lp.mY );
    //   }
    // }
    if ( mSize == 0 ) return;
    mPath = new Path();
    LinePoint lp = mFirst;
    mPath.moveTo( lp.mX, lp.mY );
    for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
      if ( lp.has_cp ) {
        mPath.cubicTo( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        mPath.lineTo( lp.mX, lp.mY );
      }
    }
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
    addStartPoint( lp.mX, lp.mY );
    LinePoint prev = lp.mPrev;
    while ( prev != null ) {
      if ( lp.has_cp ) {
        addPoint3( lp.mX2, lp.mY2, lp.mX1, lp.mY1, prev.mX, prev.mY );
      } else {
        addPoint( prev.mX, prev.mY );
      }
      lp = prev;
      prev = prev.mPrev;
    }
  }

  float distance( float x, float y )
  {
    if ( Float.isNaN(x) || Float.isNaN(y) ) return Float.NaN;
    float dist = 1000f; // FIXME
    // for ( LinePoint pt : mPoints ) 
    for ( LinePoint pt=mFirst; pt != null; pt = pt.mNext ) 
    {
      double dx = x - pt.mX;
      double dy = y - pt.mY;
      float d = (float)( Math.sqrt( dx*dx + dy*dy ) );
      if ( d < dist ) dist = d;
    }
    return dist;
  }

  public void close() 
  {
    mPath.close();
    // Log.v( TopoDroidApp.TAG, "area close path" );
  }

  // public ArrayList< LinePoint > getPoints() { return mPoints; }

  // public int size() { return mPoints.size(); }

  // @Override
  // public void draw( Canvas canvas )
  // {
  //   super.draw( canvas );
  //   // Path path = new Path();
  //   // path.addCircle( 0, 0, 1, Path.Direction.CCW );
  //   // for ( LinePoint lp : mPoints ) {
  //   //   Path path1 = new Path( path );
  //   //   path1.offset( lp.mX, lp.mY );
  //   //   canvas.drawPath( path1, DrawingBrushPaths.highlightPaint );
  //   // }
  // }

  // @Override
  // public void draw( Canvas canvas, Matrix matrix )
  // {
  //   super.draw( canvas, matrix );
  //   // Path path = new Path();
  //   // path.addCircle( 0, 0, 1, Path.Direction.CCW );
  //   // for ( LinePoint lp : mPoints ) {
  //   //   Path path1 = new Path( path );
  //   //   path1.offset( lp.mX, lp.mY );
  //   //   path1.transform( matrix );
  //   //   canvas.drawPath( path1, DrawingBrushPaths.highlightPaint );
  //   // }
  // }
}

