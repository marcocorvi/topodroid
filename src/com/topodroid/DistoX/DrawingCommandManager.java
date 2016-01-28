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
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PointF;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
import java.io.FileOutputStream;
import java.io.DataOutputStream;
// import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;

import java.util.Locale;

import android.util.Log;

/**
 */
public class DrawingCommandManager 
{
  private static final int BORDER = 20;

  private static final float mCloseness = TDSetting.mCloseness;

  static int mDisplayMode = DisplayMode.DISPLAY_ALL;
  RectF mBBox;

  private DrawingPath mNorthLine;
  DrawingPath mFirstReference;
  DrawingPath mSecondReference;

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

  DrawingPath              getNorth()        { return mNorthLine;    }
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

  private void flipXAxis( List<DrawingPath> paths )
  {
    final Iterator i1 = paths.iterator();
    while ( i1.hasNext() ){
      final DrawingPath drawingPath = (DrawingPath) i1.next();
      drawingPath.flipXAxis();
    }
  }

  void flipXAxis()
  {
    synchronized( mGridStack1 ) {
      flipXAxis( mGridStack1 );
      if ( mNorthLine != null ) mNorthLine.flipXAxis();
      flipXAxis( mGridStack10 );
      flipXAxis( mGridStack100 );
    }
    synchronized( mLegsStack )   { flipXAxis( mLegsStack ); }
    synchronized( mSplaysStack ) { flipXAxis( mSplaysStack ); }
 
    synchronized( mStations ) {
      for ( DrawingStationName st : mStations ) {
        st.flipXAxis();
      }
    }
    if ( mCurrentStack != null ) {
      Selection selection = new Selection();
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final ICanvasCommand cmd = (ICanvasCommand) i.next();
          if ( cmd.commandType() == 0 ) {
            cmd.flipXAxis();
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

  public DrawingCommandManager()
  {
    mBBox = new RectF();
    mNorthLine       = null;
    mFirstReference  = null;
    mSecondReference = null;
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
        if ( p.intersect( p1.mX, p1.mY, p2.mX, p2.mY, null ) ) {
          ret.add( p );
        }
      }
    }
    return ret;
  }

  DrawingStationName getStationAt( float x, float y ) // x,y canvas coords
  {
    // Log.v("DistoX", "get station at " + x + " " + y );
    for ( DrawingStationName st : mStations ) {
      // Log.v("DistoX", "station at " + st.cx + " " + st.cy );
      if ( Math.abs( x - st.cx ) < TDSetting.mCloseness
        && Math.abs( y - st.cy ) < TDSetting.mCloseness ) return st;
    }
    return null;
  }

  void setDisplayPoints( boolean display ) { mDisplayPoints = display; }

  boolean isSelectable() { return mSelection != null; }

  void clearReferences()
  {
    synchronized( mGridStack1 ) {
      mNorthLine = null;
      mFirstReference = null;
      mSecondReference = null;
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
    clearSelected();
    synchronized( mSelection ) {
      // Log.v("DistoX", "clear selection");
      mSelection.clearReferencePoints();
    }
  }

  void clearDrawing()
  {
    mNorthLine = null;
    mFirstReference = null;
    mSecondReference = null;
    mGridStack1.clear();
    mGridStack10.clear();
    mGridStack100.clear();
    mLegsStack.clear();
    mSplaysStack.clear();
    mStations.clear();
    mSelection.clearSelectionPoints();
    clearSketchItems();
  }

  void setFirstReference( DrawingPath path ) { synchronized( mGridStack1 ) { mFirstReference = path; } }

  void setSecondReference( DrawingPath path ) { synchronized( mGridStack1 ) { mSecondReference = path; } }

  void clearSketchItems()
  {
    mCurrentStack.clear();
    mUserStations.clear();
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
  //   boolean legs   = (mDisplayMode & DisplayMode.DISPLAY_LEG) != 0;
  //   boolean splays = (mDisplayMode & DisplayMode.DISPLAY_SPLAY) != 0;
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
    mScale  = 1 / s;
    mBBox.left   = - dx;      // scene coords
    mBBox.right  = mScale * TopoDroidApp.mDisplayWidth - dx; 
    mBBox.top    = - dy;
    mBBox.bottom = mScale * TopoDroidApp.mDisplayHeight - dy;

    // FIXME 
    // TUNING this is to see how many buckets are on the canvas and how many points they contain
    //
    // if ( mSelection != null ) {
    //   int cnt = 0;
    //   float pts = 0;
    //   for ( SelectionBucket bucket : mSelection.mBuckets ) {
    //     if ( bucket.intersects( mBBox ) ) { ++ cnt; pts += bucket.size(); }
    //   }
    //   pts /= cnt;
    //   Log.v("DistoX", "visible buckets " + cnt + " avg pts/bucket " + pts );
    // }
  }

  // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

  void addEraseCommand( EraseCommand cmd )
  {
    mCurrentStack.add( cmd );
  }

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
   *
   * x    X scene
   * y    Y scene
   * zoom canvas display zoom
   */
  int eraseAt( float x, float y, float zoom, EraseCommand eraseCmd ) 
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
            if ( size <= 2 || ( size == 3 && pt.mPoint == first.mNext ) ) // 2-point line OR erase midpoint of a 3-point line 
            {
              ret = 2; 
              eraseCmd.addAction( EraseAction.ERASE_REMOVE, path );
              mCurrentStack.remove( path );
              synchronized( mSelection ) {
                mSelection.removePath( path );
              }
            } 
            else if ( pt.mPoint == first.mNext ) // erase second point of the multi-point line
            {
              ret = 3;
              eraseCmd.addAction( EraseAction.ERASE_MODIFY, path );
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
            else if ( pt.mPoint == last.mPrev ) // erase second-to-last of multi-point line
            {
              ret = 4;
              eraseCmd.addAction( EraseAction.ERASE_MODIFY, path );
              // LinePoint lp = points.get(size-1);
              LinePoint lp = last;
              doRemoveLinePoint( line, lp, null );
              doRemoveLinePoint( line, pt.mPoint, pt );
              synchronized( mSelection ) {
                mSelection.removeLinePoint( line, lp ); // size -1
                mSelection.mPoints.remove( pt );        // size -2
              }
              line.retracePath();
            } else { // erase a point in the middle of multi-point line
              ret = 5;
              doSplitLine( line, pt.mPoint, eraseCmd );
              break; // IMPORTANT break the for-loop
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath)path;
            if ( area.size() <= 3 ) {
              ret = 6;
              eraseCmd.addAction( EraseAction.ERASE_REMOVE, path );
              mCurrentStack.remove( path );
              synchronized( mSelection ) {
                mSelection.removePath( path );
              }
            } else {
              ret = 7;
              eraseCmd.addAction( EraseAction.ERASE_MODIFY, path );
              doRemoveLinePoint( area, pt.mPoint, pt );
              area.retracePath();
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            ret = 1;
            eraseCmd.addAction( EraseAction.ERASE_REMOVE, path );
            mCurrentStack.remove( path );
            synchronized( mSelection ) {
              mSelection.removePath( path );
            }
          }
        }
      }
    }
    // checkLines();
    return ret;
  }

  // called from synchronized( CurrentStack ) context
  // called only by eraseAt
  private void doSplitLine( DrawingLinePath line, LinePoint lp, EraseCommand eraseCmd )
  {
    DrawingLinePath line1 = new DrawingLinePath( line.mLineType );
    DrawingLinePath line2 = new DrawingLinePath( line.mLineType );
    if ( line.splitAt( lp, line1, line2, true ) ) {
      // Log.v("DistoX", "split " + line.size() + " ==> " + line1.size() + " " + line2.size() );
      // synchronized( mCurrentStack ) // not neceessary: called in synchronized context
      {
        eraseCmd.addAction( EraseAction.ERASE_REMOVE, line );
        mCurrentStack.remove( line );
        if ( line1.size() > 1 ) {
          eraseCmd.addAction( EraseAction.ERASE_INSERT, line1 );
          mCurrentStack.add( line1 );
        }
        if ( line2.size() > 1 ) {
          eraseCmd.addAction( EraseAction.ERASE_INSERT, line2 );
          mCurrentStack.add( line2 );
        }
      }
      synchronized( mSelection ) {
        mSelection.removePath( line ); 
        if ( line1.size() > 1 ) mSelection.insertLinePath( line1 );
        if ( line2.size() > 1 ) mSelection.insertLinePath( line2 );
      }
    } else {
      // FIXME 
      // TDLog.Error( "FAILED splitAt " + lp.mX + " " + lp.mY );
      // line.dump();
    }
    // checkLines();
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
    // checkLines();
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
    if ( sp != null ) { // sp can be null 
      synchronized( mSelection ) {
        mSelection.removePoint( sp );
      }
    }
    // checkLines();
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
          // checkLines();
          return true;
        }
      }
    }
    // checkLines();
    return false;
  }

  void deletePath( DrawingPath path, EraseCommand eraseCmd )
  {
    synchronized( mCurrentStack ) {
      mCurrentStack.remove( path );
    }
    synchronized( mSelection ) {
      mSelection.removePath( path );
      clearSelected();
    }
    // checkLines();
    eraseCmd.addAction( EraseAction.ERASE_REMOVE, path );
  }

  void sharpenLine( DrawingLinePath line ) 
  {
    synchronized( mCurrentStack ) {
      line.makeSharp( );
    }
    // checkLines();
  }

  void reduceLine( DrawingLinePath line ) 
  {
    synchronized( mSelection ) {
      mSelection.removePath( line );
      clearSelected();
    }
    synchronized( mCurrentStack ) {
      line.makeReduce( );
    }
    synchronized( mSelection ) {
      mSelection.insertPath( line );
    }
    // checkLines();
  }

  void closeLine( DrawingLinePath line ) 
  {
    synchronized( mCurrentStack ) {
      line.makeClose( );
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
  //     TDLog.Error( "oversize: unable to select " );
  //     mSelection = null;
  //   }
  // } 

  // FIXME LEGS_SPLAYS
  void resetFixedPaint( Paint paint )
  {
    if( mLegsStack != null ) { 
      synchronized( mLegsStack ) {
        final Iterator i = mLegsStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath path = (DrawingPath) i.next();
          if ( path.mBlock == null || ! path.mBlock.mMultiBad ) {
            path.setPaint( paint );
          }
        }
      }
    }
    if( mSplaysStack != null ) { 
      synchronized( mSplaysStack ) {
        final Iterator i = mSplaysStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath path = (DrawingPath) i.next();
          if ( path.mBlock == null || ! path.mBlock.mMultiBad ) {
            path.setPaint( paint );
          }
        }
      }
    }
  }

  public void addLegPath( DrawingPath path, boolean selectable )
  { 
    if ( mLegsStack == null ) return;
    synchronized( mLegsStack ) {
      mLegsStack.add( path );
      if ( selectable ) {
        synchronized( mSelection ) {
          if ( path.mBlock != null ) {
            // Log.v( "DistoX", "selection add fixed path " + path.mBlock.mFrom + " " + path.mBlock.mTo );
          }
          mSelection.insertPath( path );
        }
      }
    }
  }  

  public void addSplayPath( DrawingPath path, boolean selectable )
  {
    if ( mSplaysStack == null ) return;
    synchronized( mSplaysStack ) {
      mSplaysStack.add( path );
      if ( selectable ) {
        synchronized( mSelection ) {
          if ( path.mBlock != null ) {
            // Log.v( "DistoX", "selection add fixed path " + path.mBlock.mFrom + " " + path.mBlock.mTo );
          }
          mSelection.insertPath( path );
        }
      }
    }
  }  
  
  public void setNorth( DrawingPath path )
  {
    mNorthLine = path;
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

  DrawingStationPath getUserStation( String name )
  {
    for ( DrawingStationPath p : mUserStations ) if ( p.mName.equals( name ) ) return p;
    return null;
  }

  void removeUserStation( DrawingStationPath path )
  {
    synchronized( mUserStations ) {
      mUserStations.remove( path );
    }
  }

  // boolean hasUserStation( String name )
  // {
  //   for ( DrawingStationPath p : mUserStations ) if ( p.mName.equals( name ) ) return true;
  //   return false;
  // }
  

  void addUserStation( DrawingStationPath path )
  {
    synchronized( mUserStations ) {
      mUserStations.add( (DrawingStationPath)path );
    }
  }

  void addCommand( DrawingPath path )
  {
    // TDLog.Log( TDLog.LOG_PLOT, "addCommand stack size  " + mCurrentStack.size() );
    // TDLog.Log( TDLog.LOG_PLOT, "addCommand path " + path.toString() );
    // Log.v("DistoX", "add command type " + path.mType + " " + path.mBBox.left + " " + path.mBBox.top + " " 
    //        + mBBox.left + " " + mBBox.top + " " + mBBox.right + " " + mBBox.bottom );

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
    
    // checkLines();
  }

  // called by DrawingSurface.getBitmap()
  public Bitmap getBitmap()
  {
    RectF bounds = new RectF();
    RectF b = new RectF();
    if( mSplaysStack != null ) { 
      synchronized( mSplaysStack ) {
        final Iterator i = mSplaysStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.mPath.computeBounds( b, true );
          bounds.union( b );
        }
      }
    }
    if( mLegsStack != null ) { 
      synchronized( mLegsStack ) {
        final Iterator i = mLegsStack.iterator();
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
          final ICanvasCommand cmd = (ICanvasCommand) i.next();
          cmd.computeBounds( b, true );
          bounds.union( b );
        }
      }
    }
    // TDLog.Log(  TDLog.LOG_PLOT, "getBitmap Bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
    float scale = TDSetting.mBitmapScale;

    int width  = (int)((bounds.right - bounds.left + 2 * BORDER) );
    int height = (int)((bounds.bottom - bounds.top + 2 * BORDER) );
    int max = (int)( 8 * 1024 * 1024 / (scale * scale) );  // 16 MB 2 B/pixel
    while ( width*height > max ) {
      scale /= 2;
      max *= 4;
    }
    width  = (int)((bounds.right - bounds.left + 2 * BORDER) * scale );
    height = (int)((bounds.bottom - bounds.top + 2 * BORDER) * scale );
    // Log.v( "DistoX", "PNG scale " + scale + " " + TDSetting.mBitmapScale );
   
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
    // c.drawColor(TDSetting.mBitmapBgcolor, PorterDuff.Mode.CLEAR);
    c.drawColor( TDSetting.mBitmapBgcolor );

    // commandManager.execute All(c,previewDoneHandler);
    // previewPath.draw(c);
    c.drawBitmap (bitmap, 0, 0, null);

    Matrix mat = new Matrix();
    float sca = 1 / scale;
    mat.postTranslate( BORDER - bounds.left, BORDER - bounds.top );
    mat.postScale( scale, scale );
    if ( mGridStack1 != null ) {
      synchronized( mGridStack1 ) {
        final Iterator i1 = mGridStack1.iterator();
        while ( i1.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i1.next();
          drawingPath.draw( c, mat, sca, null );
        }
        final Iterator i10 = mGridStack10.iterator();
        while ( i10.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i10.next();
          drawingPath.draw( c, mat, sca, null );
        }
        final Iterator i100 = mGridStack100.iterator();
        while ( i100.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i100.next();
          drawingPath.draw( c, mat, sca, null );
        }
        if ( mNorthLine != null ) mNorthLine.draw( c, mat, sca, null );
      }
    }
    if ( mSplaysStack != null ) {
      synchronized( mSplaysStack ) {
        final Iterator i = mSplaysStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( c, mat, sca, null );
        }
      }
    }
    if ( mLegsStack != null ) {
      synchronized( mLegsStack ) {
        final Iterator i = mLegsStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( c, mat, sca, null );
        }
      }
    }
 
    if ( mStations != null ) {  
      synchronized( mStations ) {
        for ( DrawingStationName st : mStations ) {
          st.draw( c, mat, sca, null );
        }
      }
    }

    if( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final ICanvasCommand cmd = (ICanvasCommand) i.next();
          cmd.draw( c, mat, sca, null );
        }
      }
    }
    // checkLines();
    return bitmap;
  }

  static final String actionName[] = { "remove", "insert", "modify" };

  public void undo ()
  {

    final int length = currentStackLength();
    if ( length > 0) {
      final ICanvasCommand cmd = mCurrentStack.get(  length - 1  );

      synchronized( mCurrentStack ) {
        mCurrentStack.remove( length - 1 );
        // cmd.undoCommand();
      }
      mRedoStack.add( cmd );

      if ( cmd.commandType() == 0 ) {
        synchronized( mSelection ) {
          mSelection.removePath( (DrawingPath)cmd );
        }
      } else { // EraseCommand
        EraseCommand eraseCmd = (EraseCommand)cmd;
        int na = eraseCmd.mActions.size(); 
        while ( na > 0 ) {
          --na;
          EraseAction action = eraseCmd.mActions.get( na );
          DrawingPath path = action.mPath;
          // Log.v("DistoX", "UNDO " + actionName[action.mType] + " path " + path.toString() );
          if ( action.mInitialType == EraseAction.ERASE_INSERT ) {
            synchronized( mCurrentStack ) {
              mCurrentStack.remove( path );
            }
            synchronized( mSelection ) {
              mSelection.removePath( path );
            }
          } else if ( action.mType == EraseAction.ERASE_REMOVE ) {
            synchronized( mCurrentStack ) {
              action.restorePoints( true ); // true: use old points
              mCurrentStack.add( path );
            }
            synchronized( mSelection ) {
              mSelection.insertPath( path );
            }
          } else if ( action.mType == EraseAction.ERASE_MODIFY ) {
            synchronized( mCurrentStack ) {
              action.restorePoints( true );
            }
            synchronized( mSelection ) {
              mSelection.removePath( path );
              mSelection.insertPath( path );
            }
          }
        }
      }
    }
    // checkLines();
  }

  public int currentStackLength()
  {
    final int length = mCurrentStack.toArray().length;
    return length;
  }

  DrawingLinePath getLineToContinue( LinePoint lp, int type )
  {
    String group = DrawingBrushPaths.mLineLib.getLineGroup( type );
    if ( group == null ) return null;

    DrawingLinePath ret = null;
    synchronized( mCurrentStack ) {
      final Iterator i = mCurrentStack.iterator();
      while ( i.hasNext() ){
        ICanvasCommand cmd = (ICanvasCommand) i.next();
        if ( cmd.commandType() != 0 ) continue; // FIXME EraseCommand

        final DrawingPath drawingPath = (DrawingPath)cmd;
        if ( drawingPath.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath linePath = (DrawingLinePath)drawingPath;
          // if ( linePath.mLineType == type ) 
          if ( group.equals( DrawingBrushPaths.mLineLib.getLineGroup( linePath.mLineType ) ) )
          {
            if ( linePath.mFirst.distance( lp ) < 20 || linePath.mLast.distance( lp ) < 20 ) {
              if ( ret != null ) return null; // ambiguity
              ret = linePath;
            }
          }
        }
      }
    }
    // if ( ret != null ) mSelection.removePath( ret ); // FIXME do not remove continuation line
    // checkLines();
    return ret;
  }
        
  /** add the points of the first line to the second line
   */
  void addLineToLine( DrawingLinePath line, DrawingLinePath line0 )
  {
    synchronized( mSelection ) {
      mSelection.removePath( line0 );
    }
    synchronized( mCurrentStack ) {
      boolean reverse = line0.mFirst.distance( line.mFirst ) < line0.mLast.distance( line.mFirst );
      if ( reverse ) line0.reversePath();
      line0.append( line );
      if ( reverse ) {
        line0.reversePath();
        line0.computeUnitNormal();
      }
    }
    synchronized( mSelection ) {
      mSelection.insertPath( line0 );
    }
    // checkLines();
  }

  private boolean showStationSplays( DrawingPath p, ArrayList<String> splay_stations ) 
  {
    DistoXDBlock blk = p.mBlock;
    if ( blk == null ) return false;
    String station = blk.mFrom;
    if ( station == null || station.length() == 0 ) return false;
    return splay_stations.contains( station );
  }

  public void executeAll( Canvas canvas, float zoom, Handler doneHandler, ArrayList<String> splay_stations )
  {
    if ( canvas == null ) {
      TDLog.Error( "drawing executeAll null canvas");
      return;
    }

    boolean legs   = (mDisplayMode & DisplayMode.DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DisplayMode.DISPLAY_SPLAY ) != 0;
    boolean stations = (mDisplayMode & DisplayMode.DISPLAY_STATION ) != 0;

    if( mGridStack1 != null && ( (mDisplayMode & DisplayMode.DISPLAY_GRID) != 0 ) ) {
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
        if ( mNorthLine != null ) mNorthLine.draw( canvas, mMatrix, mScale, mBBox );
      }
    }

    if ( mLegsStack != null && legs ) {
      synchronized( mLegsStack ) {
        final Iterator i = mLegsStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath path = (DrawingPath) i.next();
          path.draw( canvas, mMatrix, mScale, mBBox );
        }
      }
    }

    if ( mSplaysStack != null && ( splays || splay_stations.size() > 0 ) ) {
      synchronized( mSplaysStack ) {
        final Iterator i = mSplaysStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath path = (DrawingPath) i.next();
          if ( splays || showStationSplays( path, splay_stations ) ) {
            path.draw( canvas, mMatrix, mScale, mBBox );
          }
          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
 
    if ( mStations != null && stations ) {  
      synchronized( mStations ) {
        for ( DrawingStationName st : mStations ) {
          st.draw( canvas, mMatrix, mScale, mBBox );
        }
      }
    }

    if ( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final ICanvasCommand cmd = (ICanvasCommand) i.next();
          if ( cmd.commandType() == 0 ) {
            cmd.draw( canvas, mMatrix, mScale, mBBox );
          }
          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
    if ( ! TDSetting.mAutoStations ) {
      synchronized( mUserStations ) {
        for ( DrawingStationPath p : mUserStations ) {
          p.draw( canvas, mMatrix, mScale, mBBox );
        }
      }
    }

    if ( mDisplayPoints ) {
      synchronized( mSelection ) {
        float radius = TDSetting.mDotRadius/zoom;
        final Iterator i = mSelection.mBuckets.iterator();
        while ( i.hasNext() ) {
          final SelectionBucket bucket = (SelectionBucket) i.next();
          if ( bucket.intersects( mBBox ) ) {
            for ( SelectionPoint pt : bucket.mPoints ) { 
              int type = pt.type();
              if ( ( type == DrawingPath.DRAWING_PATH_FIXED && ! legs ) 
                || ( type == DrawingPath.DRAWING_PATH_SPLAY && ! splays )
                || ( type == DrawingPath.DRAWING_PATH_NAME  && ! stations ) ) continue;
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
        }
        // for ( SelectionPoint pt : mSelection.mPoints ) { // FIXME SELECTION
        //   float x, y;
        //   if ( pt.mPoint != null ) { // line-point
        //     x = pt.mPoint.mX;
        //     y = pt.mPoint.mY;
        //   } else {  
        //     x = pt.mItem.cx;
        //     y = pt.mItem.cy;
        //   }
        //   Path path = new Path();
        //   path.addCircle( x, y, radius, Path.Direction.CCW );
        //   path.transform( mMatrix );
        //   canvas.drawPath( path, DrawingBrushPaths.highlightPaint2 );
        // }

      }
      synchronized( mSelected ) {
        if ( mSelected.mPoints.size() > 0 ) { // FIXME SELECTIOM
          float radius = 4*TDSetting.mDotRadius/zoom;
          Path path;
          SelectionPoint sp = mSelected.mHotItem;
          if ( sp != null ) {
            float x, y;
            LinePoint lp = sp.mPoint;
            DrawingPath item = sp.mItem;
            if ( lp != null ) { // line-point
              x = lp.mX;
              y = lp.mY;
            } else {
              x = item.cx;
              y = item.cy;
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
            if ( item.mType == DrawingPath.DRAWING_PATH_LINE ) {
              DrawingLinePath line = (DrawingLinePath) item;
              lp = line.mFirst;
              path = new Path();
              path.moveTo( lp.mX, lp.mY );
              path.lineTo( lp.mX+line.mDx*10, lp.mY+line.mDy*10 );
              path.transform( mMatrix );
              canvas.drawPath( path, DrawingBrushPaths.fixedYellowPaint );
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
    synchronized( mGridStack1 ) {
      if ( mFirstReference != null )  mFirstReference.draw( canvas, mMatrix, mScale, mBBox );
      if ( mSecondReference != null ) mSecondReference.draw( canvas, mMatrix, mScale, mBBox );
    }
  }

  // boolean hasStationName( String name )
  // {
  //   if ( name == null ) return false;
  //   synchronized( mCurrentStack ) {
  //     final Iterator i = mCurrentStack.iterator();
  //     while ( i.hasNext() ){
  //       final ICanvasCommand cmd = (ICanvasCommand) i.next();
  //       if ( cmd.commandType() == 0 ) {
  //         DrawingPath p = (DrawingPath) cmd;
  //         if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
  //           DrawingStationPath sp = (DrawingStationPath)p;
  //           if ( name.equals( sp.mName ) ) return true;
  //         }
  //       }
  //     }
  //   }
  //   return false;
  // }

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
      final ICanvasCommand cmd = mRedoStack.get(  length - 1  );
      mRedoStack.remove( length - 1 );

      if ( cmd.commandType() == 0 ) {
        DrawingPath redoCommand = (DrawingPath)cmd;
        synchronized( mCurrentStack ) {
          mCurrentStack.add( redoCommand );
        }
        synchronized( mSelection ) {
          mSelection.insertPath( redoCommand );
        }
      } else {
        EraseCommand eraseCmd = (EraseCommand) cmd;
        for ( EraseAction action : eraseCmd.mActions ) {
          DrawingPath path = action.mPath;
          // Log.v("DistoX", "REDO " + actionName[action.mType] + " path " + path.mType );
          if ( action.mInitialType == EraseAction.ERASE_INSERT ) {
            synchronized( mCurrentStack ) {
              mCurrentStack.add( path );
            }
            synchronized( mSelection ) {
              mSelection.insertPath( path );
            }
          } else if ( action.mType == EraseAction.ERASE_REMOVE ) {
            synchronized( mCurrentStack ) {
              mCurrentStack.remove( path );
            }
            synchronized( mSelection ) {
              mSelection.removePath( path );
            }
          } else if ( action.mType == EraseAction.ERASE_MODIFY ) {
            synchronized( mSelection ) {
              mSelection.removePath( path );
            }
            synchronized( mCurrentStack ) {
              action.restorePoints( false ); // false: use new points
            }
            synchronized( mSelection ) {
              mSelection.insertPath( path );
            }
          }
        }
        synchronized( mCurrentStack ) {
          mCurrentStack.add( cmd );
        }
      }
    }
    // checkLines();
  }

  public SelectionSet getItemsAt( float x, float y, float zoom )
  {
    boolean legs   = (mDisplayMode & DisplayMode.DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DisplayMode.DISPLAY_SPLAY ) != 0;
    boolean stations = (mDisplayMode & DisplayMode.DISPLAY_STATION ) != 0;
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
    return TDMath.abs( (q.mX-p0.mX)*y01 - (q.mY-p0.mY)*x01 ) / TDMath.sqrt( x01*x01 + y01*y01 );
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

    for ( ICanvasCommand cmd : mCurrentStack ) {
      if ( cmd.commandType() != 0 ) continue;
      DrawingPath p = (DrawingPath)cmd;
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
    
    // if ( TDLog.LOG_DEBUG ) { // ===== FIRST SET OF LOGS
    //   TDLog.Debug( "snap to line");
    //   for ( LinePoint pt = lmin.mFirst; pt!=null; pt=pt.mNext ) TDLog.Debug( pt.mX + " " + pt.mY );
    //   TDLog.Debug( "snap area");
    //   for ( LinePoint pt = area.mFirst; pt!=null; pt=pt.mNext ) TDLog.Debug( pt.mX + " " + pt.mY );
    //   TDLog.Debug( "snap qq0= " + q0.mX + " " + q0.mY + " to pp0= " + pp0.mX + " " + pp0.mY );
    // }

    int ret = 0; // return code

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
      // TDLog.Debug( "snap pp1 " + pp1.mX + " " + pp1.mY + " FOLLOW LINE FORWARD" );
      // pp1  = pts1.get( kk1 );
      // pp10 = pts1.get( kk0 );
      pp10 = pp0;
      // if ( kk2 >= 0 ) 
      if ( pp2 != null ) {
        // TDLog.Debug( "snap pp2 " + pp2.mX + " " + pp2.mY );
        // pp2  = pts1.get( kk2 ); 
        // pp20 = pts1.get( kk0 ); 
        pp20 = pp0;
      }
      if ( pp1.distance( q1 ) < pp1.distance( q2 ) ) {
        qq1  = q1; // follow border forward
        qq10 = q0;
        // TDLog.Debug( "snap qq1 " + qq1.mX + " " + qq1.mY + " follow border forward" );
        if ( pp2 != null ) {
          qq2  = q2;
          qq20 = q0;
          // TDLog.Debug( "snap qq2 " + qq2.mX + " " + qq2.mY );
        }
      } else {
        reverse = true;
        qq1  = q2; // follow border backward
        qq10 = q0;
        // TDLog.Debug( "snap reverse qq1 " + qq1.mX + " " + qq1.mY + " follow border backward" );
        if ( pp2 != null ) {
          qq2 = q1;
          qq20 = q0;
          // TDLog.Debug( "snap qq2 " + qq2.mX + " " + qq2.mY + " follow forward");
        }
      }
    } else if ( pp2 != null ) { // pp10 is null
      // pp2  = pts1.get( kk2 ); 
      // pp20 = pts1.get( kk0 ); 
      pp20 = pp0;
      // TDLog.Debug( "snap pp1 null pp2 " + pp2.mX + " " + pp2.mY + " FOLLOW LINE BACKWARD" );
      if ( pp2.distance( q2 ) < pp2.distance( q1 ) ) {
        qq2 = q2;
        qq20 = q0;
        // TDLog.Debug( "snap qq2 " + qq2.mX + " " + qq2.mY + " follow border backward" );
      } else {
        reverse = true;
        qq2 = q1;
        qq20 = q0;
        // TDLog.Debug( "snap reverse qq2 " + qq2.mX + " " + qq2.mY + " follow border forward" );
      }
    } else {  // pp10 and pp20 are null: nothing to follow
      // copy pp0 to q0
      q0.mX = pp0.mX;
      q0.mY = pp0.mY;
      ret = 1;
    }

    if ( qq1 != null ) {
      // TDLog.Debug( "qq1 not null " + qq1.mX + " " + qq1.mY + " reverse " + reverse );
      // follow line pp10 --> pp1 --> ... using step 1
      // with border qq10 --> qq1 --> ... using step delta1

      for (int c=0; c<cmax; ++c) { // try to move qq1 forward
        TDLog.Debug( "snap at qq1 " + qq1.mX + " " + qq1.mY );
        float s = project( qq1, pp10, pp1 );
        while ( s > 1.0 ) {
          pp10 = pp1;
          // TDLog.Debug( "snap follow pp10 " + pp10.mX + " " + pp10.mY );
          pp1  = lmin.next( pp1 );
          if ( pp1 == null ) {
            // TDLog.Debug( "snap end of line pp1 null, pp10 " + pp10.mX + " " + pp10.mY );
            break;
          }
          if ( pp1 == pp0 ) {
            // TDLog.Debug( "snap pp1 == pp0, pp10 " + pp10.mX + " " + pp10.mY );
            break;
          }
          s = project( qq1, pp10, pp1 );
        }
        if ( pp1 == null ) break;
        float d1 = distance( qq1, pp10, pp1 );
        // TDLog.Debug( "distance d1 " + d1 + " s " + s );

        if ( s < 0.0f ) break;
        if ( d1 > thr || d1 < 0.001f ) break; 
        qq10 = qq1;
        qq1 = (reverse)? area.prev(qq1) : area.next( qq1 );
        if ( qq1 == q0 ) break;
      }
    } else {
      // TDLog.Debug( "snap qq1 null" );
      qq10 = q0; // FIXME
    }
    // if ( qq10 != null && pp10 != null ) {
    //   TDLog.Debug( "QQ10 " + qq10.mX + " " + qq10.mY + " PP10 " + pp10.mX + " " + pp10.mY );
    // }

    if ( qq2 != null ) {
      // TDLog.Debug( "qq2 not null: " + qq2.mX + " " + qq2.mY + " reverse " + reverse );
      // follow line pp20 --> pp2 --> ... using step size1-1
      // with border qq20 --> qq2 --> ... using step delta2
      for (int c=0; c < cmax; ++c) { // try to move qq2 backward
        // TDLog.Debug( "snap at qq2 " + qq2.mX + " " + qq2.mY );
        float s = project( qq2, pp20, pp2 );
        while ( s > 1.0 ) {
          pp20 = pp2;
          // TDLog.Debug( "snap s>1, follow pp20 " + pp20.mX + " " + pp20.mY );
          pp2 = lmin.prev( pp2 );
          if ( pp2 == null ) {
            // TDLog.Debug( "snap end of line pp2 null, pp20 " + pp20.mX + " " + pp20.mY );
            break;
          }
          if ( pp2 == pp0 ) {
            // TDLog.Debug( "snap pp2 == pp0, pp20 " + pp20.mX + " " + pp20.mY );
            break;
          }
          s = project( qq2, pp20, pp2 );
        }
        if ( pp2 == null ) break;
        float d2 = distance( qq2, pp20, pp2 );
        // TDLog.Debug( "distance qq2-P_line " + d2 + " s " + s );

        if ( s < 0.0f ) break;
        if ( d2 > thr || d2 < 0.001f ) break; 
        qq20 = qq2;
        qq2 = (reverse)? area.next(qq2) : area.prev( qq2 );
        if ( qq2 == q0 ) break;
      }
    } else {
      // TDLog.Debug( "snap qq2 null");
      qq20 = q0; // FIXME
    }
    // if ( qq20 != null && pp20 != null ) {
    //   TDLog.Debug( "QQ20 " + qq20.mX + " " + qq20.mY + " PP20 " + pp20.mX + " " + pp20.mY );
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
          // TDLog.Debug( "snap setting area FIRST null ");
        } else {
          // TDLog.Debug( "snap start prev " + prev.mX + " " + prev.mY );
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
          // TDLog.Debug( "snap setting area FIRST " + area.mFirst.mX + " " + area.mFirst.mY );
        }

        if ( next == null ) {
          area.mLast = null; // ( reverse )? qq20 : qq10;
          // TDLog.Debug( "snap setting area LAST null ");
        } else {
          // TDLog.Debug( "snap start next " + next.mX + " " + next.mY );
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
          // TDLog.Debug( "snap setting area LAST " + area.mLast.mX + " " + area.mLast.mY );
        }

        next = (reverse)? qq20 : qq10;

        // insert points pp20 - ... - pp10 (included)
        if ( reverse ) {
          LinePoint q = qq10.mPrev;
          LinePoint p = pp10;
          if ( q != null ) {
            // TDLog.Debug( "snap attach at " + q.mX + " " + q.mY );
          } else {
            // TDLog.Debug( "snap restart area ");
          }
          q = new LinePoint( p.mX, p.mY, q );
          // TDLog.Debug( "snap first new point " + q.mX + " " + q.mY );
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
              // TDLog.Debug( "snap new point " + q.mX + " " + q.mY );
            }
            if ( p != null ) { // FIXME add last point
              if ( p.has_cp ) {
                LinePoint pp = p.mNext;
                q = new LinePoint( pp.mX2, pp.mY2, pp.mX1, pp.mY1, p.mX, p.mY, q );
              } else {
                q = new LinePoint( p.mX, p.mY, q );
              }
              // TDLog.Debug( "snap last new point " + q.mX + " " + q.mY );
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
            // TDLog.Debug( "snap attach at " + q.mX + " " + q.mY );
          } else {
            // TDLog.Debug( "snap restart area ");
          }
          q = new LinePoint( p.mX, p.mY, q );
          // TDLog.Debug( "snap first new point " + q.mX + " " + q.mY );
          if ( p != pp10 ) {
            p = p.mNext;
            if ( area.mFirst == null ) area.mFirst = q;
            for ( ; p != null && p != pp10; p = p.mNext ) {
              q = new LinePoint( p, q );
              // TDLog.Debug( "snap new point " + q.mX + " " + q.mY );
            }
            // if ( p != null ) { // FIXME not add "last" point
            //   q = new LinePoint( p, q );
            //   TDLog.Debug( "snap last new point " + q.mX + " " + q.mY );
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
        // TDLog.Debug( "snap new size " + area.size() );
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
    // checkLines();
    return ret;
  }

  SelectionPoint hotItem() { return mSelected.mHotItem; }
  void shiftHotItem( float dx, float dy ) 
  { 
    synchronized( mSelection ) {
      SelectionPoint sp = mSelected.shiftHotItem( dx, dy );
      mSelection.checkBucket( sp );
    }
  }
  SelectionPoint nextHotItem() { return mSelected.nextHotItem(); }
  SelectionPoint prevHotItem() { return mSelected.prevHotItem(); }
  void clearSelected() { synchronized( mSelected ) { mSelected.clear(); } }

  // used by flipProfile
  // void rebuildSelection()
  // {
  //   Selection selection = new Selection();
  //   synchronized ( mCurrentStack ) {
  //     final Iterator i = mCurrentStack.iterator();
  //     while ( i.hasNext() ) {
  //       final ICanvasCommand cmd = (ICanvasCommand) i.next();
  //       if ( cmd.commandType() != 0 ) continue;
  //       DrawingPath path = (DrawingPath) cmd;
  //       selection.insertPath( path );
  //       // switch ( path.mType ) {
  //       //   case DrawingPath.DRAWING_PATH_POINT:
  //       //   case DrawingPath.DRAWING_PATH_LINE;
  //       //   case DrawingPath.DRAWING_PATH_AREA:
  //       //     selection.insertPath( path );
  //       //     break;
  //       // }
  //     }
  //   }
  //   mSelection = selection;
  // }

  RectF computeBBox() 
  {
    float xmin=1000000f, xmax=-1000000f, 
          ymin=1000000f, ymax=-1000000f;
    synchronized( mCurrentStack ) {
      final Iterator i = mCurrentStack.iterator();
      while ( i.hasNext() ) {
        final ICanvasCommand cmd = (ICanvasCommand) i.next();
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;
        RectF bbox = p.mBBox;
        if ( bbox.left   < xmin ) xmin = bbox.left;
        if ( bbox.right  > xmax ) xmax = bbox.right;
        if ( bbox.top    < ymin ) ymin = bbox.top;
        if ( bbox.bottom > ymax ) ymax = bbox.bottom;
      }
    }
    return new RectF( xmin, ymin, xmax, ymax ); // left top right bottom
  }

  public void exportTherion( int type, BufferedWriter out, String scrap_name, String proj_name )
  {
    RectF bbox = computeBBox();
    DrawingIO.exportTherion( type, out, scrap_name, proj_name, bbox, mNorthLine, mCurrentStack, mUserStations, mStations );
  }
   
  public void exportDataStream( int type, DataOutputStream dos, String scrap_name )
  {
    RectF bbox = computeBBox();
    DrawingIO.exportDataStream( type, dos, scrap_name, bbox, mNorthLine, mCurrentStack, mUserStations, mStations );
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
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;
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
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;

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
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;

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
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;

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
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;

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
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;

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
 
  // called by DrawingSurface::addDrawingStationName
  public void addStation( DrawingStationName st, boolean selectable )
  {
    Log.v("DistoX", "add station " + st.mName + " scene " + st.cx + " " + st.cy + " XSection " + st.mXSectionType );
    //                + " num " + st.mStation.e + " " + st.mStation.s );
    synchronized( mStations ) {
      mStations.add( st );
      if ( selectable ) {
        synchronized( mSelection ) {
          // Log.v( "DistoX", "selection add station " + st.mName );
          mSelection.insertStationName( st );
        }
      }
    }
  }

  // this is not efficient: the station names should be stored in a tree (key = name) for log-time search
  // type = type of the plot
  void setStationXSections( List<PlotInfo> xsections, long type )
  {
    for ( DrawingStationName st : mStations ) {
      String name = st.mName;
      // Log.v( "DistoX", "Station <" + name + ">" );
      for ( PlotInfo plot : xsections ) {
        if ( name.equals( plot.start ) ) {
          st.setXSection( plot.azimuth, plot.clino, type );
          break;
        }
      }
    }
  }

  float computeSectionArea()
  {
    float ret = 0;
    for ( ICanvasCommand icc : mCurrentStack ) {
      if ( icc.commandType() != 0 ) continue;
      DrawingPath p = (DrawingPath)icc;
      if ( p.mType != DrawingPath.DRAWING_PATH_LINE ) continue;
      DrawingLinePath lp = (DrawingLinePath)p;
      if ( lp.mLineType != DrawingBrushPaths.mLineLib.mLineWallIndex ) continue;
      LinePoint pt = lp.mFirst;
      while ( pt != lp.mLast ) {
        LinePoint pn = pt.mNext;
        ret += pt.mY * pn.mX - pt.mX * pn.mY;
        pt = pn;
      }
    }
    return ret / 2;
  }
}
