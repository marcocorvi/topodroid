/* @file DrawingCommandManager.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: commands manager
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120621 item editoing: getPointAt getLineAt
 * 20120726 TopoDroidApp log
 * 20121225 getAreaAt and deletePath
 * 20130108 getStationAt getShotAt
 * 20130204 using Selection class to speed up item selection
 * 20130627 SelectionException
 * 20130828 shift point path (change position of symbol point)
 * 201311   revised selection management to keep into account new point actions
 * 201312   synch bug fixes 
 * ...
 * 20140117 added date/version to export
 * 20140328 line-legs intersection
 * 20140513 export as cSurvey
 * 20140521 1-point line bug
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.PointF;
import android.graphics.Paint;
import android.graphics.Path;
// import android.graphics.Path.Direction;
import android.os.Handler;

import java.util.Iterator;
import java.util.List;
// import java.util.Locale;
import java.util.Collections;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileReader;
// import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.util.Log;

/**
 */
public class DrawingCommandManager 
{
  private static final int BORDER = 20;

  private static final float mCloseness = TopoDroidSetting.mCloseness;

  static final int DISPLAY_NONE    = 0;
  static final int DISPLAY_LEG     = 0x01;
  static final int DISPLAY_SPLAY   = 0x02;
  static final int DISPLAY_STATION = 0x04;
  static final int DISPLAY_GRID    = 0x08;
  static final int DISPLAY_ALL     = 0x0f;
  // private static final int DISPLAY_MAX     = 4;
  static int mDisplayMode = DISPLAY_ALL;

  DrawingPath                  mNorthLine;
  private List<DrawingPath>    mGridStack;
  List<DrawingPath>            mFixedStack;
  List<DrawingPath>            mCurrentStack;
  private List<DrawingPath>    mRedoStack;
  // private List<DrawingPath>    mHighlight;  // highlighted path
  private List<DrawingStationName> mStations;
  private int mMaxAreaIndex;                   // max index of areas in this plot

  private Selection mSelection;
  private SelectionSet mSelected;
  private boolean mDisplayPoints;

  private Matrix mMatrix;

  void shiftDrawing( float x, float y )
  {
    // if ( mStations != null ) {
    //   synchronized( mStations ) {
    //     for ( DrawingStationName st : mStations ) {
    //       st.shiftBy( x, y );
    //     }
    //   }
    // }
    if ( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.shiftPathBy( x, y );
        }
      }
    }
    if ( mSelection != null ) {
      synchronized( mSelection ) {
        mSelection.shiftSelectionBy( x, y );
      }
    }
  }

  public DrawingCommandManager()
  {
    mNorthLine    = null;
    mGridStack    = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mFixedStack   = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mCurrentStack = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mRedoStack    = Collections.synchronizedList(new ArrayList<DrawingPath>());
    // mHighlight    = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mStations     = Collections.synchronizedList(new ArrayList<DrawingStationName>());
    mMatrix = new Matrix(); // identity
    mSelection = new Selection();
    mSelected  = new SelectionSet();
    mMaxAreaIndex = 0;
  }

  int getNextAreaIndex()
  {
    ++mMaxAreaIndex;
    return mMaxAreaIndex;
  }

  List< DrawingPath > getIntersectionShot( LinePoint p1, LinePoint p2 )
  {
    List< DrawingPath > ret = new ArrayList< DrawingPath >();
    for ( DrawingPath p : mFixedStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
        if ( p.intersect( p1.mX, p1.mY, p2.mX, p2.mY, null ) ) {
          // Log.v( TopoDroidApp.TAG, "intersect " + p.mBlock.toString(false) );
          // if ( ret != null ) return null;
          ret.add( p );
        }
      }
    }
    return ret;
  }

  void setDisplayPoints( boolean display ) { mDisplayPoints = display; }

  boolean isSelectable() { return mSelection != null; }

  void clearReferences()
  {
    synchronized( mGridStack ) {
      mNorthLine = null;
      mGridStack.clear();
    }
    synchronized( mFixedStack ) {
      mFixedStack.clear();
    }
    synchronized( mStations ) {
      mStations.clear();
    }
    clearSelected();
    synchronized( mSelection ) {
      // Log.v("DistoX", "clear selection");
      mSelection.clearPoints();
    }
  }

  void clearDrawing()
  {
    mNorthLine = null;
    mGridStack.clear();
    mFixedStack.clear();
    mStations.clear();
    mSelection.clearPoints();
    clearSketchItems();
  }

  void clearSketchItems()
  {
    mCurrentStack.clear();
    mRedoStack.clear();
    mSelected.clear();
    mDisplayPoints = false;
  }

  // public void clearHighlight()
  // {
  //   for ( DrawingPath p : mHighlight ) {
  //     if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
  //       p.mPaint = DrawingBrushPaths.fixedShotPaint;
  //     } else {
  //       p.mPaint = DrawingBrushPaths.fixedSplayPaint;
  //     }
  //   }
  //   mHighlight.clear();
  // }

  // public DistoXDBlock setHighlight( int plot_type, float x, float y )
  // {
  //   clearHighlight();
  //   if ( plot_type != PlotInfo.PLOT_PLAN && plot_type != PlotInfo.PLOT_EXTENDED ) return null;
  //   boolean legs   = (mDisplayMode & DISPLAY_LEG) != 0;
  //   boolean splays = (mDisplayMode & DISPLAY_SPLAY) != 0;
  //   for ( DrawingPath p : mFixedStack ) {
  //     if (    ( p.mType == DrawingPath.DRAWING_PATH_FIXED && legs )
  //          || ( p.mType == DrawingPath.DRAWING_PATH_SPLAY && splays ) ) {
  //       if ( p.isCloseTo( x, y ) ) {
  //         p.mPaint = DrawingBrushPaths.highlightPaint;
  //         mHighlight.add( p );
  //       }
  //     }
  //   }
  //   if ( mHighlight.size() == 1 ) {
  //     return mHighlight.get(0).mBlock;
  //   }
  //   return null;
  // }

  public void setTransform( float dx, float dy, float s )
  {
    mMatrix = new Matrix();
    mMatrix.postTranslate( dx, dy );
    mMatrix.postScale( s, s );
  }

  // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

  /** 
   * @return result code:
   *    0  no erasing
   *    1  point erased
   *    2  line complete erase
   *    3  line start erase
   *    4  line end erase 
   *    5  line split
   *    6  area complete erase
   *    7  area point erase
   */
  int eraseAt( float x, float y, float zoom ) 
  {
    SelectionSet sel = new SelectionSet();
    mSelection.selectAt( x, y, zoom, sel, false, false, false );
    int ret = 0;
    if ( sel.size() > 0 ) {
      synchronized( mCurrentStack ) {
        for ( SelectionPoint pt : sel.mPoints ) {
          DrawingPath path = pt.mItem;
          if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath line = (DrawingLinePath)path;
            // ArrayList< LinePoint > points = line.mPoints;
            // int size = points.size();
            LinePoint first = line.mFirst;
            LinePoint last  = line.mLast;
            int size = line.size();
            // if ( size <= 2 || ( size == 3 && pt.mPoint == points.get(1) ) ) 
            if ( size <= 2 || ( size == 3 && pt.mPoint == first.mNext ) ) 
            {
              ret = 2;
              mCurrentStack.remove( path );
              synchronized( mSelection ) {
                mSelection.removePath( path );
              }
            } 
            // else if ( pt.mPoint == points.get(1) ) 
            else if ( pt.mPoint == first.mNext ) 
            {
              ret = 3;
              // LinePoint lp = points.get(0);
              LinePoint lp = first;
              doRemoveLinePoint( line, lp, null );
              doRemoveLinePoint( line, pt.mPoint, pt );
              synchronized( mSelection ) {
                mSelection.removeLinePoint( line, lp ); // index = 0
                mSelection.mPoints.remove( pt );        // index = 1
              }
              line.retracePath();
            } 
            // else if ( pt.mPoint == points.get(size-2) ) 
            else if ( pt.mPoint == last.mPrev ) 
            {
              ret = 4;
              // LinePoint lp = points.get(size-1);
              LinePoint lp = last;
              doRemoveLinePoint( line, lp, null );
              doRemoveLinePoint( line, pt.mPoint, pt );
              synchronized( mSelection ) {
                mSelection.removeLinePoint( line, lp ); // size -1
                mSelection.mPoints.remove( pt );        // size -2
              }
              line.retracePath();
            } else {
              ret = 5;
              doSplitLine( line, pt.mPoint );
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath)path;
            // if ( area.mPoints.size() <= 3 ) 
            if ( area.size() <= 3 ) 
            {
              ret = 6;
              mCurrentStack.remove( path );
              synchronized( mSelection ) {
                mSelection.removePath( path );
              }
            } else {
              ret = 7;
              doRemoveLinePoint( area, pt.mPoint, pt );
              area.retracePath();
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            ret = 1;
            // DrawingPointPath point = (DrawingPointPath)path;
            mCurrentStack.remove( path );
            synchronized( mSelection ) {
              mSelection.removePath( path );
            }
          }
        }
      }
    }
    return ret;
  }

  // called from synchronized( CurrentStack ) context
  // called only by eraseAt
  private void doSplitLine( DrawingLinePath line, LinePoint lp )
  {
    DrawingLinePath line1 = new DrawingLinePath( line.mLineType );
    DrawingLinePath line2 = new DrawingLinePath( line.mLineType );
    if ( line.splitAt( lp, line1, line2, true ) ) {
      mCurrentStack.remove( line );
      if ( line1.size() > 1 ) mCurrentStack.add( line1 );
      if ( line2.size() > 1 ) mCurrentStack.add( line2 );
      synchronized( mSelection ) {
        mSelection.removePath( line ); 
        if ( line1.size() > 1 ) mSelection.insertLinePath( line1 );
        if ( line2.size() > 1 ) mSelection.insertLinePath( line2 );
      }
    }
  }

  void splitLine( DrawingLinePath line, LinePoint lp )
  {
    if ( lp == null ) {
      return;
    }
    // if ( lp == line.mPoints.get(0) ) 
    if ( lp == line.mFirst )
    {
      return; // cannot split at first point
    }
    // int size = line.mPoints.size();
    int size = line.size();
    if ( size == 2 ) {
      return;
    }
    // if ( lp == line.mPoints.get(size-1) ) 
    if ( lp == line.mLast ) 
    {
      return; // cannot split at last point
    }
    clearSelected();

    DrawingLinePath line1 = new DrawingLinePath( line.mLineType );
    DrawingLinePath line2 = new DrawingLinePath( line.mLineType );
    if ( line.splitAt( lp, line1, line2, false ) ) {
      synchronized( mCurrentStack ) {
        mCurrentStack.remove( line );
        mCurrentStack.add( line1 );
        mCurrentStack.add( line2 );
      }
      synchronized( mSelection ) {
        mSelection.removePath( line ); 
        mSelection.insertLinePath( line1 );
        mSelection.insertLinePath( line2 );
      }
    }
  }

  // called from synchronized( mCurrentStack )
  private void doRemoveLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp )
  {
    // int size = line.mPoints.size();
    // for ( int k=0; k<size; ++k ) {
    //   LinePoint lp = line.mPoints.get( k );
    //   if ( lp == point ) {
    //     line.mPoints.remove( k );
    //     mSelection.mPoints.remove( sp );
    //     return;
    //   }
    // }

    //line.mPoints.remove( point );
    line.remove( point );
    synchronized( mSelection ) {
      mSelection.removePoint( sp );
    }
  }

  boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp )
  {
    if ( point == null ) return false;
    // int size = line.mPoints.size();
    int size = line.size();
    if ( size <= 2 ) return false;
    synchronized( mSelection ) {
      clearSelected();
    }
    // for ( int k=0; k<size; ++k ) 
    for ( LinePoint lp = line.mFirst; lp != null; lp = lp.mNext ) 
    {
      // LinePoint lp = line.mPoints.get( k );
      if ( lp == point ) {
        synchronized( mCurrentStack ) {
          // line.mPoints.remove( k );
          line.remove( point );
          synchronized( mSelection ) {
            mSelection.removePoint( sp );
          }
          return true;
        }
      }
    }
    return false;
  }

  void deletePath( DrawingPath path )
  {
    synchronized( mCurrentStack ) {
      mCurrentStack.remove( path );
    }
    synchronized( mSelection ) {
      mSelection.removePath( path );
      clearSelected();
    }
  }

  void sharpenLine( DrawingLinePath line, boolean reduce ) 
  {
    if ( reduce ) {
      synchronized( mSelection ) {
        mSelection.removePath( line );
        clearSelected();
      }
    }
    synchronized( mCurrentStack ) {
      line.makeSharp( reduce );
    }
    if ( reduce ) {
      synchronized( mSelection ) {
        mSelection.insertPath( line );
      }
    }
  }

  // ooooooooooooooooooooooooooooooooooooooooooooooooooooo

  public void setDisplayMode( int mode ) { mDisplayMode = mode; }
  public int getDisplayMode( ) { return mDisplayMode; }

  // void setBounds( float x1, float x2, float y1, float y2 )
  // {
  //   mSelection = new Selection();
  // }
  //   try {
  //     mSelection = new Selection( x1, x2, y1, y2, 5.0f );
  //     mSelected  = new SelectionSet();
  //   } catch ( SelectionException e ) {
  //     TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "oversize: unable to select " );
  //     mSelection = null;
  //   }
  // } 

  void resetFixedPaint( Paint paint )
  {
    if( mFixedStack != null ) { 
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath path = (DrawingPath) i.next();
          path.setPaint( paint );
        }
      }
    }
  }

  /** add a fixed path (called by DrawingSurface::addFixedPath)
   * @param path       path
   * @param selectable whether the path is selectable
   */
  public void addFixedPath( DrawingPath path, boolean selectable )
  {
    mFixedStack.add( path );
    if ( selectable ) {
      synchronized( mSelection ) {
        if ( path.mBlock != null ) {
          // Log.v( "DistoX", "selection add fixed path " + path.mBlock.mFrom + " " + path.mBlock.mTo );
        }
        mSelection.insertPath( path );
      }
    }
  }  
  
  public void setNorth( DrawingPath path )
  {
    mNorthLine = path;
  }
  
  public void addGrid( DrawingPath path )
  {
    mGridStack.add( path );
  }
 
  // called by DrawingSurface::addDrawingStation
  public void addStation( DrawingStationName st, boolean selectable )
  {
    // Log.v("PTDistoX", "add station " + st.mName + " scene " + st.cx + " " + st.cy
    //                + " num " + st.mStation.e + " " + st.mStation.s );

    mStations.add( st );
    if ( selectable ) {
      synchronized( mSelection ) {
        // Log.v( "DistoX", "selection add station " + st.mName );
        mSelection.insertStationName( st );
      }
    }
  }

  // void setScaleBar( float x0, float y0 ) 
  // {
  //   if ( mCurrentStack.size() > 0 ) return;
  //   DrawingLinePath scale_bar = new DrawingLinePath( DrawingBrushPaths.mLineLib.mLineSectionIndex );
  //   scale_bar.addStartPoint( x0 - 50, y0 );
  //   scale_bar.addPoint( x0 + 50, y0 );  // 5 meters
  //   synchronized( mCurrentStack ) {
  //     mCurrentStack.add( scale_bar );
  //   }
  // }

  void addCommand( DrawingPath path )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "addCommand stack size  " + mCurrentStack.size() );
    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "addCommand path " + path.toString() );
    mRedoStack.clear();

    if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
      DrawingAreaPath area = (DrawingAreaPath)path;
      if ( area.mAreaCnt > mMaxAreaIndex ) {
        mMaxAreaIndex = area.mAreaCnt;
      }
    }

    // if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
    //   DrawingLinePath line = (DrawingLinePath)path;
    //   LinePoint lp = line.mFirst;
    //   Log.v("PTDistoX", "add path. size " + line.size() + " start " + lp.mX + " " + lp.mY );
    // }
    
    synchronized( mCurrentStack ) {
      mCurrentStack.add( path );
    }
    synchronized( mSelection ) {
      mSelection.insertPath( path );
    }
  }

  // called by DrawingSurface.getBitmap()
  public Bitmap getBitmap()
  {
    RectF bounds = new RectF();
    RectF b = new RectF();
    if( mFixedStack != null ) { 
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.mPath.computeBounds( b, true );
          bounds.union( b );
        }
      }
    }
    if( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.mPath.computeBounds( b, true );
          bounds.union( b );
        }
      }
    }
    // TopoDroidLog.Log(  TopoDroidLog.LOG_PLOT, "getBitmap Bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
    float scale = TopoDroidSetting.mBitmapScale;

    int width  = (int)((bounds.right - bounds.left + 2 * BORDER) );
    int height = (int)((bounds.bottom - bounds.top + 2 * BORDER) );
    int max = (int)( 8 * 1024 * 1024 / (scale * scale) );  // 16 MB 2 B/pixel
    while ( width*height > max ) {
      scale /= 2;
      max *= 4;
    }
    width  = (int)((bounds.right - bounds.left + 2 * BORDER) * scale );
    height = (int)((bounds.bottom - bounds.top + 2 * BORDER) * scale );
    // Log.v( "DistoX", "PNG scale " + scale + " " + TopoDroidSetting.mBitmapScale );
   
    Bitmap bitmap = null;
    while ( bitmap == null && scale > 0.01 ) {
      try {
        // bitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
        bitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.RGB_565);
      } catch ( OutOfMemoryError e ) {
        scale /= 2;
        width  = (int)((bounds.right - bounds.left + 2 * BORDER) * scale );
        height = (int)((bounds.bottom - bounds.top + 2 * BORDER) * scale );
      }
    }
    if ( bitmap == null ) return null;
    Canvas c = new Canvas (bitmap);
    c.drawColor(0, PorterDuff.Mode.CLEAR);
    // commandManager.executeAll(c,previewDoneHandler);
    // previewPath.draw(c);
    c.drawBitmap (bitmap, 0, 0, null);

    Matrix mat = new Matrix();
    mat.postTranslate( BORDER - bounds.left, BORDER - bounds.top );
    mat.postScale( scale, scale );
    if ( mGridStack != null ) {
      synchronized( mGridStack ) {
        final Iterator i = mGridStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( c, mat );
        }
        if ( mNorthLine != null ) mNorthLine.draw( c, mat );
      }
    }
    if ( mFixedStack != null ) {
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( c, mat );
        }
      }
    }
    if( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( c, mat );
        }
      }
    }
    return bitmap;
  }

  public void undo ()
  {
    final int length = currentStackLength();
    if ( length > 0) {
      final DrawingPath undoCommand = mCurrentStack.get(  length - 1  );
      synchronized( mSelection ) {
        mSelection.removePath( undoCommand );
      }
      synchronized( mCurrentStack ) {
        mCurrentStack.remove( length - 1 );
      }
      undoCommand.undo();
      mRedoStack.add( undoCommand );
    }
  }

  public int currentStackLength()
  {
    final int length = mCurrentStack.toArray().length;
    return length;
  }

  DrawingLinePath getLineToContinue( LinePoint lp, int type )
  {
    DrawingLinePath ret = null;
    synchronized( mCurrentStack ) {
      final Iterator i = mCurrentStack.iterator();
      while ( i.hasNext() ){
        final DrawingPath drawingPath = (DrawingPath) i.next();
        if ( drawingPath.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath linePath = (DrawingLinePath)drawingPath;
          if ( linePath.mLineType == type ) {
            if ( linePath.mFirst.distance( lp ) < 20 || linePath.mLast.distance( lp ) < 20 ) {
              if ( ret != null ) return null;
              ret = linePath;
            }
          }
        }
      }
    }
    if ( ret != null ) mSelection.removePath( ret );
    return ret;
  }
        
  /** add the points of the first line to the second line
   */
  void addLineToLine( DrawingLinePath line, DrawingLinePath line0 )
  {
    synchronized( mCurrentStack ) {
      line0.append( line );
      mSelection.insertPath( line0 );
    }
  }


  public void executeAll( Canvas canvas, float zoom, Handler doneHandler)
  {
    boolean legs   = (mDisplayMode & DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DISPLAY_SPLAY ) != 0;
    boolean stations = (mDisplayMode & DISPLAY_STATION ) != 0;

    if( mGridStack != null && ( (mDisplayMode & DISPLAY_GRID) != 0 ) ) {
      synchronized( mGridStack ) {
        final Iterator i = mGridStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( canvas, mMatrix );
          //doneHandler.sendEmptyMessage(1);
        }
        if ( mNorthLine != null ) mNorthLine.draw( canvas, mMatrix );
      }
    }

    if ( mFixedStack != null && (legs || splays) ) {
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath path = (DrawingPath) i.next();
          if ( legs && path.mType == DrawingPath.DRAWING_PATH_FIXED ) {
            path.draw( canvas, mMatrix );
          } else if ( splays && path.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
            path.draw( canvas, mMatrix );
          }
          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
 
    if ( mStations != null && stations ) {  
      synchronized( mStations ) {
        for ( DrawingStationName st : mStations ) {
          st.draw( canvas, mMatrix );
        }
      }
    }

    if ( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( canvas, mMatrix );

          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
    if ( mDisplayPoints ) {
      synchronized( mSelection ) {
        float radius = TopoDroidSetting.mDotRadius/zoom;
        for ( SelectionPoint pt : mSelection.mPoints ) { // FIXME SELECTION
          float x, y;
          if ( pt.mPoint != null ) { // line-point
            x = pt.mPoint.mX;
            y = pt.mPoint.mY;
          } else {  
            x = pt.mItem.cx;
            y = pt.mItem.cy;
          }
          Path path = new Path();
          path.addCircle( x, y, radius, Path.Direction.CCW );
          path.transform( mMatrix );
          canvas.drawPath( path, DrawingBrushPaths.highlightPaint2 );
        }
      }
      synchronized( mSelected ) {
        if ( mSelected.mPoints.size() > 0 ) { // FIXME SELECTIOM
          float radius = 4*TopoDroidSetting.mDotRadius/zoom;
          Path path;
          SelectionPoint sp = mSelected.mHotItem;
          if ( sp != null ) {
            float x, y;
            LinePoint lp = sp.mPoint;
            if ( lp != null ) { // line-point
              x = lp.mX;
              y = lp.mY;
            } else {
              x = sp.mItem.cx;
              y = sp.mItem.cy;
            }
            path = new Path();
            path.addCircle( x, y, radius, Path.Direction.CCW );
            path.transform( mMatrix );
            canvas.drawPath( path, DrawingBrushPaths.highlightPaint2 );
            if ( lp != null && lp.has_cp ) {
              path = new Path();
              path.moveTo( lp.mX1, lp.mY1 );
              path.lineTo( lp.mX2, lp.mY2 );
              path.lineTo( x, y );
              path.addCircle( lp.mX1, lp.mY1, radius/2, Path.Direction.CCW );
              path.addCircle( lp.mX2, lp.mY2, radius/2, Path.Direction.CCW );
              path.transform( mMatrix );
              canvas.drawPath( path, DrawingBrushPaths.highlightPaint3 );
            }
          }
          radius = radius/3; // 2/zoom;
          for ( SelectionPoint pt : mSelected.mPoints ) {
            float x, y;
            if ( pt.mPoint != null ) { // line-point
              x = pt.mPoint.mX;
              y = pt.mPoint.mY;
            } else {
              x = pt.mItem.cx;
              y = pt.mItem.cy;
            }
            path = new Path();
            path.addCircle( x, y, radius, Path.Direction.CCW );
            path.transform( mMatrix );
            canvas.drawPath( path, DrawingBrushPaths.highlightPaint );
          }
        }
      } 
    }
  }

  boolean hasStationName( String name )
  {
    synchronized( mCurrentStack ) {
      final Iterator i = mCurrentStack.iterator();
      while ( i.hasNext() ){
        final DrawingPath p = (DrawingPath) i.next();
        if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
          DrawingStationPath sp = (DrawingStationPath)p;
          if ( sp.mName.equals( name ) ) return true;
        }
      }
    }
    return false;
  }

  public boolean hasMoreRedo()
  {
    return  mRedoStack.toArray().length > 0;
  }

  public boolean hasMoreUndo()
  {
    return  mCurrentStack.toArray().length > 0;
  }

  public void redo()
  {
    final int length = mRedoStack.toArray().length;
    if ( length > 0) {
      final DrawingPath redoCommand = mRedoStack.get(  length - 1  );
      mRedoStack.remove( length - 1 );
      synchronized( mCurrentStack ) {
        mCurrentStack.add( redoCommand );
      }
      synchronized( mSelection ) {
        mSelection.insertPath( redoCommand );
      }
    }
  }

  public SelectionSet getItemsAt( float x, float y, float zoom )
  {
    boolean legs   = (mDisplayMode & DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DISPLAY_SPLAY ) != 0;
    boolean stations = (mDisplayMode & DISPLAY_STATION ) != 0;
    synchronized ( mSelected ) {
      mSelected.clear();
      // Log.v( "DistoX", "getItemAt " + x + " " + y + " zoom " + zoom + " selection pts " + mSelection.mPoints.size() );
      mSelection.selectAt( x, y, zoom, mSelected, legs, splays, stations );
      if ( mSelected.mPoints.size() > 0 ) {
        mSelected.nextHotItem();
      }
    }
    return mSelected;
  }

  void splitHotItem()
  { 
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return;
    if ( sp.type() != DrawingPath.DRAWING_PATH_LINE && sp.type() != DrawingPath.DRAWING_PATH_AREA ) return;
    LinePoint lp = sp.mPoint;
    if ( lp == null ) return;
    float x = lp.mX;
    float y = lp.mY;
    DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
    LinePoint p1 = line.insertPointAfter( x, y, lp );
    SelectionPoint sp1 = null;
    synchronized( mSelection ) {
      sp1 = mSelection.insertPathPoint( line, p1 );
    }
    if ( sp1 != null ) {
      synchronized( mSelected ) {
        mSelected.mPoints.add( sp1 );
      }
    }
  }

  private float project( LinePoint q, LinePoint p0, LinePoint p1 )
  {
    float x01 = p1.mX - p0.mX;
    float y01 = p1.mY - p0.mY;
    return ((q.mX-p0.mX)*x01 + (q.mY-p0.mY)*y01) / ( x01*x01 + y01*y01 );
  }
    
  private float distance( LinePoint q, LinePoint p0, LinePoint p1 )
  {
    float x01 = p1.mX - p0.mX;
    float y01 = p1.mY - p0.mY;
    return (float)( Math.abs((q.mX-p0.mX)*y01 - (q.mY-p0.mY)*x01) / Math.sqrt( x01*x01 + y01*y01 ) );
  }
    
      
  boolean moveHotItemToNearestPoint()
  {
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return false;
    float x = 0.0f;
    float y = 0.0f;
    if ( sp.type() == DrawingPath.DRAWING_PATH_POINT ) {
      x = sp.mItem.cx;
      y = sp.mItem.cy;
    } else if ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) {
      x = sp.mPoint.mX;
      y = sp.mPoint.mY;
    } else {
      return false;
    }
    SelectionPoint spmin = mSelection.getNearestPoint( sp, x, y, 10f );

    if ( spmin != null ) {
      if ( spmin.type() == DrawingPath.DRAWING_PATH_LINE || spmin.type() == DrawingPath.DRAWING_PATH_AREA ) {
        x = spmin.mPoint.mX - x;
        y = spmin.mPoint.mY - y;
      } else {
        x = spmin.mItem.cx - x;
        y = spmin.mItem.cy - y;
      }
      sp.shiftBy( x, y );
    }
    return true;
  }
  
  // return error codes
  //  -1   no selected point
  //  -2   selected point not on area border
  //  -3   no close line
  //  +1   only a point: nothing to follow
  //
  int snapHotItemToNearestLine()
  {
    SelectionPoint sp = mSelected.mHotItem;

    // no selected point or selected point not on area border:
    if ( sp == null ) return -1;
    if ( sp.type() != DrawingPath.DRAWING_PATH_AREA ) return -2;

    DrawingPath item = sp.mItem;
    DrawingAreaPath area = (DrawingAreaPath)item;
    // int k0 = 0;
    LinePoint q0 = sp.mPoint;

    // ArrayList< LinePoint > pts0 = area.mPoints;
    // int size0 = pts0.size();
    // for ( ; k0 < size0; ++k0 ) {
    //   if ( pts0.get(k0) == q0 ) break;
    // }
    // if ( k0 == size0 ) return false;
    // // area border: ... --> q2 --> q0 --> q1 --> ...
    // int k1 = (k0+1)%size0;
    // int k2 = (k0+size0-1)%size0;
    // LinePoint q1 = area.mPoints.get( k1 ); // next point on the area border
    // LinePoint q2 = area.mPoints.get( k2 ); // prev point on the area border
    LinePoint q1 = area.next( q0 );
    LinePoint q2 = area.prev( q0 );

    float x = q0.mX;
    float y = q0.mY;
    float thr = 10f;
    float dmin = thr; // require a minimum distance
    DrawingPointLinePath lmin = null;
    boolean min_is_area = false;
    // int kk0 = -1;

    // find drawing path with minimal distance from (x,y)
    LinePoint pp0 = null;

    for ( DrawingPath p : mCurrentStack ) {
      if ( p == item ) continue;
      if ( p.mType != DrawingPath.DRAWING_PATH_LINE &&
           p.mType != DrawingPath.DRAWING_PATH_AREA ) continue;
      DrawingPointLinePath lp = (DrawingPointLinePath)p;
      // ArrayList< LinePoint > pts = lp.mPoints;
      // int size = pts.size();
      // for ( int k=0; k<size; ++k ) 
      int ks = lp.size();
      for ( LinePoint pt = lp.mFirst; pt != null && ks > 0; pt = pt.mNext )
      {
        -- ks;
        // float d = pts.get(k).distance( x, y );
        float d = pt.distance( x, y );
        if ( d < dmin ) {
          dmin = d;
          // kk0 = k;
          pp0  = pt;
          lmin = lp;
          min_is_area = ( p.mType == DrawingPath.DRAWING_PATH_AREA );
        }
      }
    }
    if ( lmin == null ) return -3;
    int cmax = area.size() + 1;
    
    if ( TopoDroidLog.LOG_DEBUG ) { // ===== FIRST SET OF LOGS
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap to line");
      for ( LinePoint pt = lmin.mFirst; pt!=null; pt=pt.mNext ) TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, pt.mX + " " + pt.mY );
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap area");
      for ( LinePoint pt = area.mFirst; pt!=null; pt=pt.mNext ) TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, pt.mX + " " + pt.mY );
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap qq0= " + q0.mX + " " + q0.mY + " to pp0= " + pp0.mX + " " + pp0.mY );
    }

    int ret = 0; // return code

    // ArrayList< LinePoint > pts1 = lmin.mPoints;
    // LinePoint pp0 = pts1.get( kk0 );
    // int size1 = pts1.size();

    // // try to follow p1 on the line:
    // int kk1 = ( kk0+1 < size1 )? kk0 + 1 : (min_is_area)? 0 : -1; // index of next point
    // int kk2 = ( kk0 > 0 )? kk0 - 1 : (min_is_area)? size1-1 : -1; // index of prev point
    // int delta1 = 0; 
    // int delta2 = 0;
    // int kk10 = kk0;
    // int kk20 = kk0;

    LinePoint pp1 = lmin.next( pp0 );
    LinePoint pp2 = lmin.prev( pp0 );

    LinePoint pp10 = null; // current point forward
    LinePoint pp20 = null; // current point backward
    // LinePoint pp1  = null; // next point forward
    // LinePoint pp2  = null; // prev point backwrad
    LinePoint qq10 = null;
    LinePoint qq20 = null;
    LinePoint qq1 = null;
    LinePoint qq2 = null;
    boolean reverse = false;
    int step = 1;
    // if ( kk1 >= 0 ) 
    if ( pp1 != null ) { 
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap pp1 " + pp1.mX + " " + pp1.mY + " FOLLOW LINE FORWARD" );
      // pp1  = pts1.get( kk1 );
      // pp10 = pts1.get( kk0 );
      pp10 = pp0;
      // if ( kk2 >= 0 ) 
      if ( pp2 != null ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap pp2 " + pp2.mX + " " + pp2.mY );
        // pp2  = pts1.get( kk2 ); 
        // pp20 = pts1.get( kk0 ); 
        pp20 = pp0;
      }
      if ( pp1.distance( q1 ) < pp1.distance( q2 ) ) {
        qq1  = q1; // follow border forward
        qq10 = q0;
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap qq1 " + qq1.mX + " " + qq1.mY + " follow border forward" );
        // delta1 = 1;
        // if ( kk2 >= 0 ) 
        if ( pp2 != null ) {
          qq2  = q2;
          qq20 = q0;
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap qq2 " + qq2.mX + " " + qq2.mY );
          // delta2 = size0-1;
        }
      } else {
        // int k = k1; k1 = k2; k2 = k;
        reverse = true;
        qq1  = q2; // follow border backward
        qq10 = q0;
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap reverse qq1 " + qq1.mX + " " + qq1.mY + " follow border backward" );
        // delta1 = size0-1;
        // if ( kk2 >= 0 ) 
        if ( pp2 != null ) {
          qq2 = q1;
          qq20 = q0;
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap qq2 " + qq2.mX + " " + qq2.mY + " follow forward");
          // delta2 = 1;
        }
      }
    } else // if ( kk2 >= 0 ) 
           if ( pp2 != null ) { // pp10 is null
      // pp2  = pts1.get( kk2 ); 
      // pp20 = pts1.get( kk0 ); 
      pp20 = pp0;
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap pp1 null pp2 " + pp2.mX + " " + pp2.mY + " FOLLOW LINE BACKWARD" );
      if ( pp2.distance( q2 ) < pp2.distance( q1 ) ) {
        qq2 = q2;
        qq20 = q0;
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap qq2 " + qq2.mX + " " + qq2.mY + " follow border backward" );
        // delta2 = size0-1;
      } else {
        // int k = k1; k1 = k2; k2 = k;
        reverse = true;
        qq2 = q1;
        qq20 = q0;
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap reverse qq2 " + qq2.mX + " " + qq2.mY + " follow border forward" );
        // delta2 = 1;
      }
    } else {  // pp10 and pp20 are null: nothing to follow
      // copy pp0 to q0
      q0.mX = pp0.mX;
      q0.mY = pp0.mY;
      ret = 1;
    }

    if ( qq1 != null ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "qq1 not null " + qq1.mX + " " + qq1.mY + " reverse " + reverse );
      // follow line pp10 --> pp1 --> ... using step 1
      // with border qq10 --> qq1 --> ... using step delta1

      for (int c=0; c<cmax; ++c) { // try to move qq1 forward
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap at qq1 " + qq1.mX + " " + qq1.mY );
        float s = project( qq1, pp10, pp1 );
        while ( s > 1.0 ) {
          // kk1 = ( kk1+1 < size1 )? kk1 + 1 : (min_is_area)? 0 : -1;
          // if ( kk1 < 0 || kk1 == kk0 ) break;
          pp10 = pp1;
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap follow pp10 " + pp10.mX + " " + pp10.mY );
          // pp1  = pts1.get( kk1 );
          pp1  = lmin.next( pp1 );
          if ( pp1 == null ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap end of line pp1 null, pp10 " + pp10.mX + " " + pp10.mY );
            break;
          }
          if ( pp1 == pp0 ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap pp1 == pp0, pp10 " + pp10.mX + " " + pp10.mY );
            break;
          }
          s = project( qq1, pp10, pp1 );
        }
        if ( pp1 == null ) break;
        float d1 = distance( qq1, pp10, pp1 );
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "distance d1 " + d1 + " s " + s );

        if ( s < 0.0f ) break;
        if ( d1 > thr || d1 < 0.001f ) break; 
        qq10 = qq1;
        // k1 = (k1+delta1)%size0;
        // if ( k1 == k0 ) break;
        // qq1 = pts0.get( k1 );
        qq1 = (reverse)? area.prev(qq1) : area.next( qq1 );
        if ( qq1 == q0 ) break;
      }
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap qq1 null" );
      qq10 = q0; // FIXME
    }
    if ( qq10 != null && pp10 != null ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "QQ10 " + qq10.mX + " " + qq10.mY + " PP10 " + pp10.mX + " " + pp10.mY );
    }

    if ( qq2 != null ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "qq2 not null: " + qq2.mX + " " + qq2.mY + " reverse " + reverse );
      // follow line pp20 --> pp2 --> ... using step size1-1
      // with border qq20 --> qq2 --> ... using step delta2
      for (int c=0; c < cmax; ++c) { // try to move qq2 backward
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap at qq2 " + qq2.mX + " " + qq2.mY );
        float s = project( qq2, pp20, pp2 );
        while ( s > 1.0 ) {
          // kk2 = ( kk2 > 0 )? kk2 - 1 : (min_is_area)? size1-1 : -1;
          // if ( kk2 < 0 || kk2 == kk0 ) break;
          pp20 = pp2;
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap s>1, follow pp20 " + pp20.mX + " " + pp20.mY );
          // pp2 = pts1.get( kk2 );
          pp2 = lmin.prev( pp2 );
          if ( pp2 == null ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap end of line pp2 null, pp20 " + pp20.mX + " " + pp20.mY );
            break;
          }
          if ( pp2 == pp0 ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap pp2 == pp0, pp20 " + pp20.mX + " " + pp20.mY );
            break;
          }
          s = project( qq2, pp20, pp2 );
        }
        if ( pp2 == null ) break;
        float d2 = distance( qq2, pp20, pp2 );
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "distance qq2-P_line " + d2 + " s " + s );

        if ( s < 0.0f ) break;
        if ( d2 > thr || d2 < 0.001f ) break; 
        qq20 = qq2;
        // k2 = (k2+delta2)%size0;
        // if ( k2 == k0 ) break;
        // qq2 = pts0.get( k2 );
        qq2 = (reverse)? area.next(qq2) : area.prev( qq2 );
        if ( qq2 == q0 ) break;
      }
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap qq2 null");
      qq20 = q0; // FIXME
    }
    if ( qq20 != null && pp20 != null ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "QQ20 " + qq20.mX + " " + qq20.mY + " PP20 " + pp20.mX + " " + pp20.mY );
    }

    // if ( reverse ) { int k=k1; k1=k2; k2=k; }
    // k2 and k1 are kept
    // ArrayList< LinePoint > pts2 = new ArrayList< LinePoint >();
    // LinePoint prev = null;
    // for (int k=k1; k!=k2; k=(k+1)%size0 ) {
    //   prev = new LinePoint(pts0.get(k), prev);
    //   pts2.add( prev );
    // }
    // prev = new LinePoint(pts0.get(k2), prev );
    // pts2.add( prev );

    // if ( reverse ) {
    //   for ( int k = (kk1+size1-1)%size1; k != kk2; k = (k+size1-1)%size1 ) {
    //     prev = new LinePoint( pts1.get(k), prev );
    //     pts2.add( prev );
    //   }
    // } else {
    //   for ( int k = (kk2+1)%size1; k != kk1; k=(k+1)%size1 ) {
    //     prev = new LinePoint( pts1.get(k), prev );
    //     pts2.add( prev );
    //   }
    // }
    
    if ( qq20 == qq10 || (reverse && pp10 == null) || (!reverse && pp20 == null) ) {
      // should not happen, anyways copy pp0 to q0
      q0.mX = pp0.mX;
      q0.mY = pp0.mY;
      ret = 2;
    }

    synchronized( mCurrentStack ) {
      if ( ret == 0 ) { 
        synchronized( mSelection ) {
          mSelection.removePath( area );
        }
        // next-prev refer to the point list along the area path.
        LinePoint next = qq10.mNext; // unlink qq20 -> ... -> qq10
        LinePoint prev = qq20.mPrev;
        if ( reverse ) {             // unlink qq10 -> ... -> qq20
          next = qq20.mNext;
          prev = qq10.mPrev;
        } 

        if ( prev == null ) {
          area.mFirst = null; // ( reverse )? qq10 : qq20;
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap setting area FIRST null ");
        } else {
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap start prev " + prev.mX + " " + prev.mY );
          LinePoint q = prev;
          while ( prev != null && prev != next ) {
            q = prev;
            prev = q.mPrev;
          }
          area.mFirst = q;
          if ( q.mPrev != null ) { // make sure first has no prev
            q.mPrev.mNext = null;
            q.mPrev = null;
          }
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap setting area FIRST " + area.mFirst.mX + " " + area.mFirst.mY );
        }

        if ( next == null ) {
          area.mLast = null; // ( reverse )? qq20 : qq10;
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap setting area LAST null ");
        } else {
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap start next " + next.mX + " " + next.mY );
          LinePoint q = next;
          while ( next != null && next != prev ) {
            q = next;
            next = q.mNext;
          }
          area.mLast = q;
          if ( q.mNext != null ) {
            q.mNext.mPrev = null;
            q.mNext = null;
          }
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap setting area LAST " + area.mLast.mX + " " + area.mLast.mY );
        }

        next = (reverse)? qq20 : qq10;

        // insert points pp20 - ... - pp10 (included)
        if ( reverse ) {
          LinePoint q = qq10.mPrev;
          LinePoint p = pp10;
          if ( q != null ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap attach at " + q.mX + " " + q.mY );
          } else {
            TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap restart area ");
          }
          q = new LinePoint( p.mX, p.mY, q );
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap first new point " + q.mX + " " + q.mY );
          if ( p != pp20 ) {
            p = p.mPrev;
            if ( area.mFirst == null ) area.mFirst = q;
            for ( ; p != null && p != pp20; p = p.mPrev ) {
              if ( p.has_cp && p != pp10 ) {
                LinePoint pp = p.mNext;
                q = new LinePoint( pp.mX2, pp.mY2, pp.mX1, pp.mY1, p.mX, p.mY, q );
              } else {
                q = new LinePoint( p.mX, p.mY, q );
              }
              TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap new point " + q.mX + " " + q.mY );
            }
            if ( p != null ) { // FIXME add last point
              if ( p.has_cp ) {
                LinePoint pp = p.mNext;
                q = new LinePoint( pp.mX2, pp.mY2, pp.mX1, pp.mY1, p.mX, p.mY, q );
              } else {
                q = new LinePoint( p.mX, p.mY, q );
              }
              TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap last new point " + q.mX + " " + q.mY );
            }
          }
          q.mNext = next;
          if ( next != null ) {
            next.mPrev  = q;
            next.has_cp = false; // enforce straight segment
          }
          if ( area.mLast == null ) area.mLast = q;

        } else { // not reverse

          LinePoint q = qq20.mPrev;
          LinePoint p = pp20;
          if ( q != null ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap attach at " + q.mX + " " + q.mY );
          } else {
            TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap restart area ");
          }
          q = new LinePoint( p.mX, p.mY, q );
          TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap first new point " + q.mX + " " + q.mY );
          if ( p != pp10 ) {
            p = p.mNext;
            if ( area.mFirst == null ) area.mFirst = q;
            for ( ; p != null && p != pp10; p = p.mNext ) {
              q = new LinePoint( p, q );
              TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap new point " + q.mX + " " + q.mY );
            }
            // if ( p != null ) { // FIXME not add "last" point
            //   q = new LinePoint( p, q );
            //   TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap last new point " + q.mX + " " + q.mY );
            // }
          }
          q.mNext = next;
          if ( next != null ) {
            next.mPrev  = q;
            next.has_cp = false;
          }
          if ( area.mLast == null ) area.mLast = q;
        }
        area.recount(); 
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "snap new size " + area.size() );
      }

      // area.mPoints = pts2;
      area.retracePath();
      
      if ( ret == 0 ) {
        synchronized( mSelection ) {
          mSelection.insertPath( area );
        }
      }
      clearSelected();
    }
    return ret;
  }

  SelectionPoint hotItem()
  {
    return mSelected.mHotItem;
  }

  void shiftHotItem( float dx, float dy )
  {
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

    void clearSelected()
    {
      synchronized( mSelected ) {
        mSelected.clear();
      }
    }

  public void exportTherion( int type, BufferedWriter out, String scrap_name, String proj_name )
  {
    try { 
      out.write("encoding utf-8");
      out.newLine();
      out.newLine();
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("# %s created by TopoDroid v. %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );
      // TODO write current palette
      pw.format("#P ");
      DrawingBrushPaths.mPointLib.writePalette( pw );
      pw.format("\n#L ");
      DrawingBrushPaths.mLineLib.writePalette( pw );
      pw.format("\n#A ");
      DrawingBrushPaths.mAreaLib.writePalette( pw );
      pw.format("\n");

      if ( type == PlotInfo.PLOT_SECTION || type == PlotInfo.PLOT_H_SECTION ) {
        float toTherion = TopoDroidConst.TO_THERION;
        if ( mNorthLine != null ) {
          pw.format("scrap %s -projection %s -scale [%.0f %.0f %.0f %.0f 0 5 0 0 m]", scrap_name, proj_name, 
            mNorthLine.x1*toTherion, -mNorthLine.y1*toTherion, mNorthLine.x2*toTherion, -mNorthLine.y2*toTherion );
        } else {
          // float x1 = 0 * toTherion;
          // float x2 = 5 * toTherion;
          // final Iterator i = mCurrentStack.iterator();
          // while ( i.hasNext() ) {
          //   final DrawingPath p = (DrawingPath) i.next();
          //   if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
          //     DrawingLinePath lp = (DrawingLinePath)p;
          //     if ( lp.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
          //       x1 = lp.mFirst.mX * toTherion;
          //       x2 = lp.mLast.mX  * toTherion;
          //       break;
          //     }
          //   }
          // }
          float dx = 100 * toTherion;
          pw.format("scrap %s -projection %s -scale [0 0 %.0f 0 0 0 5 0 m]", scrap_name, proj_name, dx );
        }
      } else {
        pw.format("scrap %s -projection %s -scale [0 0 1 0 0.0 0.0 1 0.0 m]", scrap_name, proj_name );
      }
      out.write( sw.getBuffer().toString() );
      out.newLine();
      // out.newLine();
      // for ( DrawingStationName st : mStations ) {
      //   out.write( st.toTherion() );
      //   out.newLine();
      // }
      out.newLine();
      float xmin=10000f, xmax=-10000f, 
            ymin=10000f, ymax=-10000f,
            umin=10000f, umax=-10000f,
            vmin=10000f, vmax=-10000f;
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ) {
          final DrawingPath p = (DrawingPath) i.next();
          if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
            DrawingPointPath pp = (DrawingPointPath)p;
            out.write( pp.toTherion() );
            out.newLine();
          } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
            if ( ! TopoDroidSetting.mAutoStations ) {
              DrawingStationPath st = (DrawingStationPath)p;
              // Log.v( TopoDroidApp.TAG, "save station to Therion " + st.mName );
              out.write( st.toTherion() );
              out.newLine();
            }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath lp = (DrawingLinePath)p;
            // TopoDroidLog.Log(  TopoDroidLoLogOG_PLOT, "exportTherion line " + lp.lineType() + "/" + DrawingBrushPaths.mLineLib.mLineWallIndex );
            // ArrayList< LinePoint > pts = lp.mPoints;
            // if ( pts.size() > 1 )
            if ( lp.size() > 1 ) {
              if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
                // for ( LinePoint pt : pts ) 
                for ( LinePoint pt = lp.mFirst; pt != null; pt = pt.mNext )
                {
                  if ( pt.mX < xmin ) xmin = pt.mX;
                  if ( pt.mX > xmax ) xmax = pt.mX;
                  if ( pt.mY < ymin ) ymin = pt.mY;
                  if ( pt.mY > ymax ) ymax = pt.mY;
                  float u = pt.mX + pt.mY;
                  float v = pt.mX - pt.mY;
                  if ( u < umin ) umin = u;
                  if ( u > umax ) umax = u;
                  if ( v < vmin ) vmin = v;
                  if ( v > vmax ) vmax = v;
                }
              }
              out.write( lp.toTherion() );
              out.newLine();
            }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath ap = (DrawingAreaPath)p;
            // TopoDroidLog.Log(  TopoDroidLog.LOG_PLOT, "exportTherion area " + ap.areaType() );
            out.write( ap.toTherion() );
            out.newLine();
          }
        }
      }
      out.newLine();

      if ( TopoDroidSetting.mAutoStations ) {
        TopoDroidLog.Log(  TopoDroidLog.LOG_PLOT, "exportTherion auto-stations: nr. " + mStations.size() );
        TopoDroidLog.Log(  TopoDroidLog.LOG_PLOT, "bbox " + xmin + ".." + xmax + " " + ymin + ".." + ymax );
        for ( DrawingStationName st : mStations ) {
          // TopoDroidLog.Log(  TopoDroidLog.LOG_PLOT, "stations " + st.cx + " " + st.cy );
          // FIXME if station is in the convex hull of the lines
          if ( xmin > st.cx || xmax < st.cx ) continue;
          if ( ymin > st.cy || ymax < st.cy ) continue;
          float u = st.cx + st.cy;
          float v = st.cx - st.cy;
          if ( umin > u || umax < u ) continue;
          if ( vmin > v || vmax < v ) continue;
          // TopoDroidLog.Log(  TopoDroidLog.LOG_PLOT, "writing" );
          out.write( st.toTherion() );
          out.newLine();
        }
        out.newLine();
        out.newLine();
      } else {
        TopoDroidLog.Log(  TopoDroidLog.LOG_PLOT, "exportTherion NO auto-stations: nr. " + mStations.size() );
      }

      out.write("endscrap");
      out.newLine();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  void exportAsCsx( PrintWriter pw )
  {
    synchronized( mCurrentStack ) {
      pw.format("    <layers>\n");

      // LAYER 0: images and sketches
      pw.format("      <layer name=\"Base\" type=\"0\">\n");
      pw.format("         <items>\n");
      pw.format("         </items>\n");
      pw.format("      </layer>\n");

      // LAYER 1: soil areas
      pw.format("      <layer name=\"Soil\" type=\"1\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath ap = (DrawingAreaPath)p;
          if ( DrawingBrushPaths.getAreaCsxLayer( ap.mAreaType ) != 1 ) continue;
          ap.toCsurvey( pw );
        }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 2: 
      pw.format("      <layer name=\"Water and floor morphologies\" type=\"2\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath lp = (DrawingLinePath)p;
          if ( DrawingBrushPaths.getLineCsxLayer( lp.mLineType ) != 2 ) continue;
          lp.toCsurvey( pw );
        } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath ap = (DrawingAreaPath)p;
          if ( DrawingBrushPaths.getAreaCsxLayer( ap.mAreaType ) != 2 ) continue;
          ap.toCsurvey( pw );
        } 
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 3
      pw.format("      <layer name=\"Rocks and concretions\" type=\"3\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
	if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
	  DrawingLinePath lp = (DrawingLinePath)p;
	  if ( DrawingBrushPaths.getLineCsxLayer( lp.mLineType ) != 2 ) continue;
	  lp.toCsurvey( pw );
        }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 4
      pw.format("      <layer name=\"Ceiling morphologies\" type=\"4\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath lp = (DrawingLinePath)p;
          if ( DrawingBrushPaths.getLineCsxLayer( lp.mLineType ) != 4 ) continue;
          lp.toCsurvey( pw );
        }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 5:
      pw.format("      <layer name=\"Borders\" type=\"5\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath lp = (DrawingLinePath)p;
          if ( DrawingBrushPaths.getLineCsxLayer( lp.mLineType ) != 5 ) continue;
          lp.toCsurvey( pw );
        }
        // if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
        //   // linetype: 0 line, 1 spline, 2 bezier
        //   pw.format("          <item layer=\"5\" name=\"\" type=\"4\" category=\"1\" linetype=\"0\" mergemode=\"0\">\n");
        //   pw.format("            <pen type=\"1\" />\n");
        //   pw.format("            <points data=\"");
        //   ArrayList< LinePoint > pts = lp.mPoints;
        //   boolean b = true;
        //   for ( LinePoint pt : pts ) {
        //     float x = DrawingActivity.sceneToWorldX( pt.mX );
        //     float y = DrawingActivity.sceneToWorldY( pt.mY );
        //     pw.format(Locale.ENGLISH, "%.2f %.2f ", x, y );
        //     if ( b ) { pw.format("B "); b = false; }
        //   }
        //   pw.format("\" />\n");
        //   pw.format("          </item>\n");
        // }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 6: signs and texts
      pw.format("      <layer name=\"Signs\" type=\"6\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
          DrawingPointPath pp = (DrawingPointPath)p;
          if ( DrawingBrushPaths.getPointCsxLayer( pp.mPointType ) != 6 ) continue;
          pp.toCsurvey( pw );
        }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");
      pw.format("    </layers>\n");
    }
  }
}
