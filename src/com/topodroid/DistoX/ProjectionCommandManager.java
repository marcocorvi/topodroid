/* @file ProjectionCommandManager.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid projected profile azimuth: commands manager
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
// import android.graphics.PointF;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
// import android.graphics.Path.Direction;
import android.os.Handler;

import java.util.Iterator;
import java.util.List;
// import java.util.Locale;
import java.util.Collections;
import java.util.ArrayList;

import java.util.Locale;

import android.util.Log;

/**
 */
public class ProjectionCommandManager 
{
  private static final int BORDER = 20;

  RectF mBBox = null;

  private List<DrawingPath>    mGridStack1;
  private List<DrawingPath>    mGridStack10;
  private List<DrawingPath>    mGridStack100;

  private List<DrawingPath>        mLegsStack;
  private List<DrawingPath>        mSplaysStack;
  private List<ICanvasCommand>     mCurrentStack;
  private List<DrawingStationPath> mUserStations;  // user-inserted stations
  private List<ICanvasCommand>     mRedoStack;
  // private List<DrawingPath>     mHighlight;  // highlighted path
  private List<DrawingStationName> mStations;  // survey stations
  private int mMaxAreaIndex;                   // max index of areas in this plot

  private Selection mSelection;
  private SelectionSet mSelected;
  private boolean mDisplayPoints;

  private Matrix mMatrix;
  private float  mScale; // current zoom: value of 1 pl in scene space

  List<DrawingPath>        getLegs()         { return mLegsStack;    }
  List<DrawingPath>        getSplays()       { return mSplaysStack;  }
  List<ICanvasCommand>     getCommands()     { return mCurrentStack; }
  List<DrawingStationName> getStations()     { return mStations;     } 
  List<DrawingStationPath> getUserStations() { return mUserStations; }

  // void checkLines()
  // {
  //   synchronized( mCurrentStack ) {
  //     int size = mCurrentStack.size();
  //     for ( int i1 = 0; i1 < size; ++i1 ) {
  //       ICanvasCommand cmd1 = mCurrentStack.get( i1 );
  //       DrawingPath path1 = (DrawingPath)cmd1;
  //       if ( path1.mType != DrawingPath.DRAWING_PATH_LINE ) continue;
  //       DrawingLinePath line1 = (DrawingLinePath)path1;
  //       for ( int i2 = i1+1; i2 < size; ++i2 ) {
  //         ICanvasCommand cmd2 = mCurrentStack.get( i2 );
  //         DrawingPath path2 = (DrawingPath)cmd2;
  //         if ( path2.mType != DrawingPath.DRAWING_PATH_LINE ) continue;
  //         DrawingLinePath line2 = (DrawingLinePath)path2;
  //         if ( line1.overlap( line2 ) > 1 ) {
  //           Log.v("DistoX", "LINE OVERLAP " + i1 + "-" + i2 + " total nr. " + size );
  //           // for ( int i=0; i<size; ++i ) {
  //           //   ICanvasCommand cmd = mCurrentStack.get( i );
  //           //   DrawingPath path = (DrawingPath)cmd;
  //           //   if ( path.mType != DrawingPath.DRAWING_PATH_LINE ) continue;
  //           //   DrawingLinePath line = (DrawingLinePath)path;
  //           //   line.dump();
  //           // }
  //           Log.v("DistoX", "LINE1 ");
  //           line1.dump();
  //           Log.v("DistoX", "LINE2 ");
  //           line2.dump();
  //           throw new RuntimeException();
  //         }
  //       }
  //     }
  //   }
  // }

  private void flipXAxes( List<DrawingPath> paths )
  {
    final float z = 1/mScale;
    final Iterator i1 = paths.iterator();
    while ( i1.hasNext() ){
      final DrawingPath drawingPath = (DrawingPath) i1.next();
      drawingPath.flipXAxis( z );
    }
  }

  void flipXAxis(float z)
  {
    synchronized( mGridStack1 ) {
      flipXAxes( mGridStack1 );
      flipXAxes( mGridStack10 );
      flipXAxes( mGridStack100 );
    }
    synchronized( mLegsStack )   { flipXAxes( mLegsStack ); }
    synchronized( mSplaysStack ) { flipXAxes( mSplaysStack ); }
 
    synchronized( mStations ) {
      for ( DrawingStationName st : mStations ) {
        st.flipXAxis(z);
      }
    }
    if ( mCurrentStack != null ) {
      Selection selection = new Selection();
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final ICanvasCommand cmd = (ICanvasCommand) i.next();
          if ( cmd.commandType() == 0 ) {
            cmd.flipXAxis(z);
            DrawingPath path = (DrawingPath)cmd;
            if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
              DrawingLinePath line = (DrawingLinePath)path;
              line.flipReversed();
            }
            selection.insertPath( path );
          }
        }
      }
      mSelection = selection;
    }
  }

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
          final ICanvasCommand cmd = (ICanvasCommand) i.next();
          cmd.shiftPathBy( x, y );
        }
      }
    }
    if ( mSelection != null ) {
      synchronized( mSelection ) {
        mSelection.shiftSelectionBy( x, y );
      }
    }
  }

  public ProjectionCommandManager()
  {
    // mBBox = new RectF();
    mGridStack1   = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mGridStack10  = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mGridStack100 = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mLegsStack   = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mSplaysStack   = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mCurrentStack = Collections.synchronizedList(new ArrayList<ICanvasCommand>());
    mUserStations = Collections.synchronizedList( new ArrayList<DrawingStationPath>());
    mRedoStack    = Collections.synchronizedList(new ArrayList<ICanvasCommand>());
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
    for ( DrawingPath p : mLegsStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
        if ( p.intersect( p1.x, p1.y, p2.x, p2.y, null ) ) {
          ret.add( p );
        }
      }
    }
    return ret;
  }

  DrawingStationName getStationAt( float x, float y, float size ) // x,y canvas coords
  {
    // Log.v("DistoX", "get station at " + x + " " + y );
    for ( DrawingStationName st : mStations ) {
      // Log.v("DistoX", "station at " + st.cx + " " + st.cy );
      if ( Math.abs( x - st.cx ) < size && Math.abs( y - st.cy ) < size ) return st;
    }
    return null;
  }

  void setDisplayPoints( boolean display ) { mDisplayPoints = display; }

  boolean isSelectable() { return mSelection != null; }

  void clearReferences()
  {
    synchronized( mGridStack1 ) {
      mGridStack1.clear();
      mGridStack10.clear();
      mGridStack100.clear();
    }
    synchronized( mLegsStack ) {
      mLegsStack.clear();
    }
    synchronized( mSplaysStack ) {
      mSplaysStack.clear();
    }
    synchronized( mStations ) {
      mStations.clear();
    }
  }

  void clearDrawing()
  {
    mGridStack1.clear();
    mGridStack10.clear();
    mGridStack100.clear();
    mLegsStack.clear();
    mSplaysStack.clear();
    mStations.clear();
  }

  public void setTransform( float dx, float dy, float s )
  {
    mMatrix = new Matrix();
    mMatrix.postTranslate( dx, dy );
    mMatrix.postScale( s, s );
    mScale  = 1 / s;
    // mBBox.left   = - dx;      // scene coords
    // mBBox.right  = mScale * TopoDroidApp.mDisplayWidth - dx; 
    // mBBox.top    = - dy;
    // mBBox.bottom = mScale * TopoDroidApp.mDisplayHeight - dy;
  }

  // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

  // FIXME LEGS_SPLAYS
  // void resetFixedPaint( Paint paint )
  // {
  //   if( mLegsStack != null ) { 
  //     synchronized( mLegsStack ) {
  //       final Iterator i = mLegsStack.iterator();
  //       while ( i.hasNext() ){
  //         final DrawingPath path = (DrawingPath) i.next();
  //         if ( path.mBlock == null || ( ! path.mBlock.mMultiBad ) ) {
  //           path.setPaint( paint );
  //         }
  //       }
  //     }
  //   }
  //   if( mSplaysStack != null ) { 
  //     synchronized( mSplaysStack ) {
  //       final Iterator i = mSplaysStack.iterator();
  //       while ( i.hasNext() ){
  //         final DrawingPath path = (DrawingPath) i.next();
  //         if ( path.mBlock == null || ( ! path.mBlock.mMultiBad ) ) {
  //           path.setPaint( paint );
  //         }
  //       }
  //     }
  //   }
  // }

  public void addLegPath( DrawingPath path )
  { 
    if ( mLegsStack == null ) return;
    synchronized( mLegsStack ) {
      mLegsStack.add( path );
    }
  }  

  public void addSplayPath( DrawingPath path )
  {
    if ( mSplaysStack == null ) return;
    synchronized( mSplaysStack ) {
      mSplaysStack.add( path );
    }
  }  
  
  public void addGrid( DrawingPath path, int k ) 
  { 
    if ( mGridStack1 == null ) return;
    synchronized( mGridStack1 ) {
      switch (k) {
        case 1:   mGridStack1.add( path );   break;
        case 10:  mGridStack10.add( path );  break;
        case 100: mGridStack100.add( path ); break;
      }
    }
  }

  public void executeAll( Canvas canvas )
  {
    if ( canvas == null ) {
      TDLog.Error( "drawing executeAll null canvas");
      return;
    }

    if ( mGridStack1 != null ) {
      synchronized( mGridStack1 ) {
        if ( mScale < 1 ) {
          final Iterator i1 = mGridStack1.iterator();
          while ( i1.hasNext() ){
            final DrawingPath drawingPath = (DrawingPath) i1.next();
            drawingPath.draw( canvas, mMatrix, mScale, mBBox );
          }
        }
        if ( mScale < 10 ) {
          final Iterator i10 = mGridStack10.iterator();
          while ( i10.hasNext() ){
            final DrawingPath drawingPath = (DrawingPath) i10.next();
            drawingPath.draw( canvas, mMatrix, mScale, mBBox );
          }
        }
        final Iterator i100 = mGridStack100.iterator();
        while ( i100.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i100.next();
          drawingPath.draw( canvas, mMatrix, mScale, mBBox );
        }
      }
    }

    if ( mLegsStack != null ) {
      synchronized( mLegsStack ) {
        for ( DrawingPath path : mLegsStack ) {
          path.draw( canvas, mMatrix, mScale, mBBox );
        }
      }
    }

    if ( mSplaysStack != null ) {
      synchronized( mSplaysStack ) {
        for ( DrawingPath path : mSplaysStack ) {
          path.draw( canvas, mMatrix, mScale, mBBox );
        }
      }
    }
 
    if ( mStations != null ) {  
      synchronized( mStations ) {
        for ( DrawingStationName st : mStations ) {
          st.draw( canvas, mMatrix, mScale, mBBox );
        }
      }
    }
  }

  // RectF computeBBox() 
  // {
  //   float xmin=1000000f, xmax=-1000000f, 
  //         ymin=1000000f, ymax=-1000000f;
  //   synchronized( mCurrentStack ) {
  //     final Iterator i = mCurrentStack.iterator();
  //     while ( i.hasNext() ) {
  //       final ICanvasCommand cmd = (ICanvasCommand) i.next();
  //       if ( cmd.commandType() != 0 ) continue;
  //       DrawingPath p = (DrawingPath) cmd;
  //       // RectF bbox = p.mBBox;
  //       if ( p.left   < xmin ) xmin = p.left;
  //       if ( p.right  > xmax ) xmax = p.right;
  //       if ( p.top    < ymin ) ymin = p.top;
  //       if ( p.bottom > ymax ) ymax = p.bottom;
  //     }
  //   }
  //   return new RectF( xmin, ymin, xmax, ymax ); // left top right bottom
  // }

  // called by DrawingSurface::addDrawingStationName
  public void addStation( DrawingStationName st )
  {
    synchronized( mStations ) {
      mStations.add( st );
    }
  }

}
