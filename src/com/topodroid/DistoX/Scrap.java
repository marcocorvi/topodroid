/* @file Scrap.java
 *
 * @author marco corvi
 * @date oct 2019
 *
 * @brief TopoDroid drawing commands scrap
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.ui.TDGreenDot;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.PointF;

public class Scrap
{
  final List< ICanvasCommand >   mCurrentStack;
  List< DrawingStationPath >     mUserStations;  // user-inserted stations
  private List< ICanvasCommand > mRedoStack;
  private Selection mSelection;
  private SelectionSet mSelected;
  private int mMultiselectionType = -1;  // current multiselection type (DRAWING_PATH_POINT / LINE / AREA
  private List< DrawingPath > mMultiselected;
  boolean isMultiselection = false; 
  private int mMaxAreaIndex;             // max index of areas in this scrap
  public String mPlotName;              // name of the plot this scrap belongs to
  public int mScrapIdx;
  private RectF mBBox;   // this scrap bbox

  Scrap( int idx, String plot_name ) 
  {
    mCurrentStack = Collections.synchronizedList(new ArrayList< ICanvasCommand >());
    mUserStations = Collections.synchronizedList(new ArrayList< DrawingStationPath >());
    mRedoStack    = Collections.synchronizedList(new ArrayList< ICanvasCommand >());
    mSelection    = new Selection();
    mSelected     = new SelectionSet();
    mMultiselected = new ArrayList< DrawingPath >();
    mScrapIdx     = idx;
    mBBox = null;
    mMaxAreaIndex = 0;
    mPlotName = plot_name;
  }

  boolean hasPlotName( String plot_name ) { return mPlotName != null && mPlotName.equals( plot_name ); }

  // ----------------------------------------------------------
  // "merge" b1 into b0
  static void union( RectF b0, RectF b1 )
  {
    if ( b0.left   > b1.left   ) b0.left   = b1.left;
    if ( b0.right  < b1.right  ) b0.right  = b1.right;
    if ( b0.top    > b1.top    ) b0.top    = b1.top;
    if ( b0.bottom < b1.bottom ) b0.bottom = b1.bottom;
  }

  int getNextAreaIndex() 
  {
    ++mMaxAreaIndex;
    return mMaxAreaIndex;
  }

  void clearSketchItems()
  {
    synchronized( TDPath.mSelectionLock ) { mSelection.clearSelectionPoints(); }
    synchronized( TDPath.mCommandsLock ) { mCurrentStack.clear(); }
    synchronized( TDPath.mStationsLock ) { mUserStations.clear(); }
    mRedoStack.clear();
    syncClearSelected();
  }

  // SELECTION -------------------------------------------
  void syncClearSelected()
  { 
    synchronized( TDPath.mSelectionLock ) { clearSelected(); }
  }

  void clearSelected()
  {
    mSelected.clear();
    // PATH_MULTISELECT
    mMultiselected.clear();
    mMultiselectionType  = -1;
    isMultiselection = false;
  }

  boolean isSelectable() { return mSelection != null; }

  // FIXME-HIDE UNUSED
  // void clearShotsAndStations()
  // {
  //   TDLog.v("HIDE scrap clear shots and stations");
  //   mSelection.clearReferencePoints();
  //   clearSelected();
  // }

  // FIXME-HIDE UNUSED
  // void deleteSplay( SelectionPoint sp )
  // {
  //   TDLog.v("HIDE scrap clear splay");
  //   mSelection.removePoint( sp );
  //   clearSelected();
  // }
  
  // used to insert leg-path and splay-path FIXME-HIDE UNUSED
  // void insertPathInSelection( DrawingPath path )
  // {
  //   TDLog.v("HIDE scrap insert path" );
  //   mSelection.insertPath( path );
  // }

  // end SELECTION -------------------------------------------
  // UNDO/REDO -----------------------------------------------
  boolean hasMoreRedo() { return  mRedoStack.toArray().length > 0; }
  boolean hasMoreUndo() { return  mCurrentStack.size() > 0; }

  void undo ()
  {
    final int length = mCurrentStack.size();
    if ( length > 0) {
      final ICanvasCommand cmd;
      synchronized( TDPath.mCommandsLock ) {
        cmd = mCurrentStack.get(  length - 1  );
        mCurrentStack.remove( length - 1 );
        // cmd.undoCommand();
      }
      mRedoStack.add( cmd );

      if ( cmd.commandType() == 0 ) {
        synchronized( TDPath.mSelectionLock ) {
          mSelection.removePath( (DrawingPath)cmd );
        }
      } else { // EraseCommand
        EraseCommand eraseCmd = (EraseCommand)cmd;
        int na = eraseCmd.mActions.size(); 
        while ( na > 0 ) {
          --na;
          EraseAction action = eraseCmd.mActions.get( na );
          DrawingPath path = action.mPath;
          // TDLog.v( "UNDO " + actionName[action.mType] + " path " + path.toString() );
          if ( action.mInitialType == EraseAction.ERASE_INSERT ) {
            synchronized( TDPath.mCommandsLock ) {
              mCurrentStack.remove( path );
            }
            synchronized( TDPath.mSelectionLock ) {
              mSelection.removePath( path );
            }
          } else if ( action.mType == EraseAction.ERASE_REMOVE ) {
            synchronized( TDPath.mCommandsLock ) {
              action.restorePoints( true ); // true: use old points
              mCurrentStack.add( path );
            }
            synchronized( TDPath.mSelectionLock ) {
              mSelection.insertPath( path );
            }
          } else if ( action.mType == EraseAction.ERASE_MODIFY ) { // undo modify
            synchronized( TDPath.mSelectionLock ) {
              mSelection.removePath( path );
            }
            synchronized( TDPath.mCommandsLock ) {
              action.restorePoints( true );
            }
            synchronized( TDPath.mSelectionLock ) {
              mSelection.insertPath( path );
            }
          }
        }
      }
    }
    // checkLines();
  }
  // end UNDO/REDO -----------------------------------------------

  // ADD etc. ----------------------------------------------------
  SelectionSet getItemsAt( float x, float y, float radius, int mode, 
		           boolean legs, boolean splays, boolean stations, DrawingStationSplay station_splay,
			   Selection selection_fixed // FIXME-HIDE
                         )
  {
    // synchronized ( TDPath.mSelectedLock ) {
    synchronized ( TDPath.mSelectionLock ) {
      mSelected.clear();
      // FIXME_LATEST latests splays are not considered in the selection
      mSelection.selectAt( mSelected, x, y, radius, mode, legs, splays, stations, station_splay ); 
      selection_fixed.selectAt( mSelected, x, y, radius, mode, legs, splays, stations, station_splay ); // FIXME-HIDE
      // FIXME-HIDE if ( mSelected.mPoints.size() > 0 ) {
        // TDLog.v( "seleceted " + mSelected.mPoints.size() + " points " );
        mSelected.nextHotItem();
      // }
    }
    return mSelected;
  }

  void addItemAt( float x, float y, float radius )
  {
    synchronized ( TDPath.mSelectionLock ) {
      mSelected.clear();
      mSelection.selectAt( mSelected, x, y, radius, mMultiselectionType );
      for ( SelectionPoint sp : mSelected.mPoints ) {
        addMultiselection( sp.mItem );
      }
    }
  }

  void addCommand( ArrayList< DrawingPath > paths )
  {
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() == 0 ) paths.add( (DrawingPath) cmd );
      }
    }
  }

  // FIXME-HIDE UNUSED
  // void addStationToSelection( DrawingStationName st )
  // {
  //   TDLog.v("HIDE scrap add station");
  //   mSelection.insertStationName( st );
  // }

  // PATH ACTIONS ------------------------------------------------

  private void doDeletePath( DrawingPath path )
  {
    synchronized( TDPath.mCommandsLock ) {
      mCurrentStack.remove( path );
    }
    synchronized( TDPath.mSelectionLock ) {
      mSelection.removePath( path );
      clearSelected();
    }
  }

  void deletePath( DrawingPath path, EraseCommand eraseCmd ) // called by DrawingSurface
  {
    doDeletePath( path );
    // checkLines();
    if ( eraseCmd != null ) eraseCmd.addAction( EraseAction.ERASE_REMOVE, path );
  }

  // deleting a section line automatically deletes the associated section point(s)
  void deleteSectionLine( DrawingPath line, String scrap, EraseCommand cmd )
  {
    synchronized( TDPath.mCommandsLock ) {
      int index = BrushManager.getPointSectionIndex();
      if ( index >= 0 ) {
        ArrayList< DrawingPath > todo = new ArrayList<>();
        for ( ICanvasCommand c : mCurrentStack ) {
          if ( c.commandType() == 0 ) {
            DrawingPath p = (DrawingPath)c;
            if ( p.isPoint() ) { // p instanceof DrawingPointPath
              DrawingPointPath pt = (DrawingPointPath)p;
              if ( pt.mPointType == index && scrap.equals( TDUtil.replacePrefix( TDInstance.survey, pt.getOption( TDString.OPTION_SCRAP ) ) ) ) {
                todo.add(p);
              }
            }
          }
        }
        for ( DrawingPath pp : todo ) deletePath( pp, cmd );
      }
      deletePath( line, cmd );
    }
  }

  void deleteSectionPoint( String scrap_name, EraseCommand cmd )
  {
    int index = BrushManager.getPointSectionIndex();
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand icc : mCurrentStack ) { // FIXME reverse_iterator
        if ( icc.commandType() == 0 ) { // DrawingPath
          DrawingPath path = (DrawingPath)icc;
          if ( path.isPoint() ) { // path  instanceof DrawingPointPath
            DrawingPointPath dpp = (DrawingPointPath) path;
            if ( dpp.mPointType == index ) {
              // FIXME GET_OPTION
              if ( scrap_name.equals( TDUtil.replacePrefix( TDInstance.survey, dpp.getOption( TDString.OPTION_SCRAP ) ) ) ) {
                deletePath( path, cmd );
                return; // true;
              }
              // String vals[] = dpp.mOptions.split(" ");
              // int len = vals.length;
              // for ( int k = 0; k < len; ++k ) {
              //   if ( scrap_name.equals( vals[k] ) ) {
              //     deletePath( path, cmd );
              //     return;
              //   }
              // }
            }
          }
        }
      }
    }
    // return false;
  }

  // LINE ACTIONS ------------------------------------------------

  void splitPointHotItem()
  { 
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return;
    if ( sp.type() != DrawingPath.DRAWING_PATH_LINE && sp.type() != DrawingPath.DRAWING_PATH_AREA ) return;
    LinePoint lp = sp.mPoint;
    if ( lp == null ) return;
    float x = lp.x;
    float y = lp.y;
    DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
    LinePoint p1 = line.insertPointAfter( x, y, lp );
    SelectionPoint sp1 = null;
    synchronized( TDPath.mSelectionLock ) {
      sp1 = mSelection.insertPathPoint( line, p1 );
    }
    if ( sp1 != null ) {
      // synchronized( TDPath.mSelectedLock ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelected.mPoints.add( sp1 );
      }
    }
  }

  void splitLine( DrawingLinePath line, LinePoint lp )
  {
    if ( lp == null ) return;
    if ( lp == line.mFirst || lp == line.mLast ) return; // cannot split at first and last point
    int size = line.size();
    if ( size == 2 ) return;
    syncClearSelected();

    boolean splitted = false;
    DrawingLinePath line1 = new DrawingLinePath( line.mLineType, mScrapIdx );
    DrawingLinePath line2 = new DrawingLinePath( line.mLineType, mScrapIdx );
    line1.setOptions( line.getOptions() );
    line2.setOptions( line.getOptions() );

    try {
      splitted = line.splitAt( lp, line1, line2, false );
    } catch ( OutOfMemoryError e ) {
      TDLog.Error("OOM " + e.getMessage() );
    }
    if ( splitted ) {
      synchronized( TDPath.mCommandsLock ) {
        mCurrentStack.remove( line );
        mCurrentStack.add( line1 );
        mCurrentStack.add( line2 );
      }
      synchronized( TDPath.mSelectionLock ) {
        mSelection.removePath( line ); 
        mSelection.insertLinePath( line1 );
        mSelection.insertLinePath( line2 );
      }
    } else {
      TDLog.Error("FAILED split line");
    }
    // checkLines();
  }

  // called from synchronized( TDPath.mCommandsLock )
  private void doRemoveLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp )
  { 
    line.remove( point );
    if ( sp != null ) { // sp can be null 
      synchronized( TDPath.mSelectionLock ) {
        mSelection.removePoint( sp );
      }
    }
    // checkLines();
  }

  boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp )
  {
    if ( point == null ) return false;
    int size = line.size();
    if ( size <= 2 ) return false;
    syncClearSelected();
    for ( LinePoint lp = line.mFirst; lp != null; lp = lp.mNext ) 
    {
      if ( lp == point ) {
        synchronized( TDPath.mCommandsLock ) {
          line.remove( point );
	}
        synchronized( TDPath.mSelectionLock ) {
          mSelection.removePoint( sp );
        }
        // checkLines();
        return true;
      }
    }
    // checkLines();
    return false;
  }

  boolean removeLinePointFromSelection( DrawingLinePath line, LinePoint point )
  {
    if ( point == null ) return false;
    int size = line.size();
    if ( size <= 2 ) return false;
    syncClearSelected();
    synchronized( TDPath.mCommandsLock ) {
      line.remove( point );
    }
    synchronized( TDPath.mSelectionLock ) {
      mSelection.removeLineLastPoint( line, point );
    }
    return true;
  }

  /* Split the line at the point lp
   * The erase command is updated with the removal of the original line and the insert
   * of the two new pieces
   // called from synchronized( CurrentStack ) context
   // called only by eraseAt
   */
  private void doSplitLine( DrawingLinePath line, LinePoint lp, EraseCommand eraseCmd )
  {
    DrawingLinePath line1 = new DrawingLinePath( line.mLineType, mScrapIdx );
    DrawingLinePath line2 = new DrawingLinePath( line.mLineType, mScrapIdx );
    line1.setOptions( line.getOptions() );
    line2.setOptions( line.getOptions() );

    boolean splitted = false;
    try {
      splitted = line.splitAt( lp, line1, line2, true );
    } catch ( OutOfMemoryError e ) {
      TDLog.Error("OOM " + e.getMessage() );
    }
    if ( splitted ) {
      // TDLog.v( "split " + line.size() + " ==> " + line1.size() + " " + line2.size() );
      // synchronized( TDPath.mCommandsLock ) // not neceessary: called in synchronized context
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
      synchronized( TDPath.mSelectionLock ) {
        mSelection.removePath( line ); 
        if ( line1.size() > 1 ) mSelection.insertLinePath( line1 );
        if ( line2.size() > 1 ) mSelection.insertLinePath( line2 );
      }
    } else {
      TDLog.Error( "FAILED do split line " + lp.x + " " + lp.y );
    }
    // checkLines();
  }

  void sharpenPointLine( DrawingPointLinePath line ) 
  {
    synchronized( TDPath.mCommandsLock ) {
      line.makeSharp( );
    }
    // checkLines();
  }

  // @param decimation   log-decimation 
  void reducePointLine( DrawingPointLinePath line, int decimation ) 
  {
    if ( decimation <= 0 ) return;
    synchronized( TDPath.mSelectionLock ) {
      mSelection.removePath( line );
      clearSelected();
    }
    synchronized( TDPath.mCommandsLock ) {
      int min_size = ( line.isArea()? 3 : 2 );
      line.makeReduce( decimation, min_size );
    }
    synchronized( TDPath.mSelectionLock ) {
      mSelection.insertPath( line );
    }
    // checkLines();
  }


  void rockPointLine( DrawingPointLinePath line ) 
  {
    synchronized( TDPath.mSelectionLock ) {
      mSelection.removePath( line );
      clearSelected();
    }
    synchronized( TDPath.mCommandsLock ) {
      line.makeRock( );
    }
    synchronized( TDPath.mSelectionLock ) {
      mSelection.insertPath( line );
    }
    // checkLines();
  }

  void closePointLine( DrawingPointLinePath line )
  {
    synchronized( TDPath.mCommandsLock ) {
      SelectionPoint sp = mSelection.getSelectionPoint( line.mLast );
      line.makeClose( );
      // re-bucket last line point
      synchronized ( TDPath.mSelectionLock ) {
        mSelection.rebucket( sp );
      }
    }
  }

  // ERASE ACTIONS -----------------------------------------------
  /** 
   * return result code:
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
   *
   * N.B. mSelection cannot be null here
   */
  void eraseAt( float x, float y, float zoom, EraseCommand eraseCmd, int erase_mode, float erase_size ) 
  {
    SelectionSet sel = new SelectionSet();
    float erase_radius = TDSetting.mCloseCutoff + erase_size / zoom;
    synchronized ( TDPath.mSelectionLock ) {
      mSelection.selectAt( sel, x, y, erase_radius, Drawing.FILTER_ALL, false, false, false, null );
    }
    // int ret = 0;
    if ( sel.size() > 0 ) {
      synchronized( TDPath.mCommandsLock ) {
        for ( SelectionPoint pt : sel.mPoints ) {
          DrawingPath path = pt.mItem;
          if ( path.isLine() ) { // path  instanceof DrawingLinePath
            if ( erase_mode == Drawing.FILTER_ALL || erase_mode == Drawing.FILTER_LINE ) {
              DrawingLinePath line = (DrawingLinePath)path;
	      if ( BrushManager.isLineSection( line.mLineType ) ) {
		// do not erase section lines 2018-06-22
		// deleting a section line should call DrawingWindow.deleteLine()
		// deleteSectionLine( line );
                continue;
              }
              LinePoint first = line.mFirst;
              LinePoint last  = line.mLast;
              int size = line.size();
              if ( size <= 2 || ( size == 3 && pt.mPoint == first.mNext ) ) // 2-point line OR erase midpoint of a 3-point line 
              {
                // TDLog.Log( TDLog.LOG_PLOT, remove_line );
                // ret = 2; 
                eraseCmd.addAction( EraseAction.ERASE_REMOVE, path );
                mCurrentStack.remove( path );
                synchronized ( TDPath.mSelectionLock ) {
                  mSelection.removePath( path );
                }
              } 
              else if ( pt.mPoint == first ) // erase first point of the multi-point line (2016-05-14)
              {
                // TDLog.Log( TDLog.LOG_PLOT, remove_line_first );
                // ret = 3;
                eraseCmd.addAction( EraseAction.ERASE_MODIFY, path );
                // LinePoint lp = points.get(0);
                // LinePoint lp = first;
                doRemoveLinePoint( line, pt.mPoint, pt );
                synchronized ( TDPath.mSelectionLock ) {
                  mSelection.removeLinePoint( line, first ); // index = 0
                  // mSelection.mPoints.remove( pt );        // index = 1
                }
                line.retracePath();
              }
              else if ( pt.mPoint == first.mNext ) // erase second point of the multi-point line
              {
                // TDLog.Log( TDLog.LOG_PLOT, remove_line_second );
                // ret = 3;
                eraseCmd.addAction( EraseAction.ERASE_MODIFY, path );
                // LinePoint lp = points.get(0);
                // LinePoint lp = first;
                doRemoveLinePoint( line, first, null );
                doRemoveLinePoint( line, pt.mPoint, pt );
                synchronized ( TDPath.mSelectionLock ) {
                  mSelection.removeLinePoint( line, first ); // index = 0
                  mSelection.mPoints.remove( pt );        // index = 1
                }
                line.retracePath();
              } 
              else if ( pt.mPoint == last.mPrev ) // erase second-to-last of multi-point line
              {
                // TDLog.Log( TDLog.LOG_PLOT, remove_line_last );
                // ret = 4;
                eraseCmd.addAction( EraseAction.ERASE_MODIFY, path );
                // LinePoint lp = points.get(size-1);
                // LinePoint lp = last;
                doRemoveLinePoint( line, last, null );
                doRemoveLinePoint( line, pt.mPoint, pt );
                synchronized ( TDPath.mSelectionLock ) {
                  mSelection.removeLinePoint( line, last ); // size -1
                  mSelection.mPoints.remove( pt );        // size -2
                }
                line.retracePath();
              }
              else if ( pt.mPoint == last ) // erase last of multi-point line
              {
                // TDLog.Log( TDLog.LOG_PLOT, remove_line_last );
                // ret = 4;
                eraseCmd.addAction( EraseAction.ERASE_MODIFY, path );
                // LinePoint lp = points.get(size-1);
                // LinePoint lp = last;
                doRemoveLinePoint( line, pt.mPoint, pt );
                synchronized ( TDPath.mSelectionLock ) {
                  mSelection.removeLinePoint( line, last ); // size -1
                  // mSelection.mPoints.remove( pt );        // size -2
                }
                line.retracePath();
              } else { // erase a point in the middle of multi-point line
                // TDLog.Log( TDLog.LOG_PLOT, remove_line_middle );
                // ret = 5;
                doSplitLine( line, pt.mPoint, eraseCmd );
                break; // IMPORTANT break the for-loop
              }
            }
          } else if ( path.isArea() ) { // path  instanceof DrawingAreaPath
            if ( erase_mode == Drawing.FILTER_ALL || erase_mode == Drawing.FILTER_AREA ) {
              DrawingAreaPath area = (DrawingAreaPath)path;
              if ( area.size() <= 3 ) {
                // TDLog.Log( TDLog.LOG_PLOT, remove_area );
                // ret = 6;
                eraseCmd.addAction( EraseAction.ERASE_REMOVE, path );
                mCurrentStack.remove( path );
                synchronized ( TDPath.mSelectionLock ) {
                  mSelection.removePath( path );
                }
              } else {
                // TDLog.Log( TDLog.LOG_PLOT, remove_area_point );
                // ret = 7;
                eraseCmd.addAction( EraseAction.ERASE_MODIFY, path );
                doRemoveLinePoint( area, pt.mPoint, pt );
                area.retracePath();
              }
            }
          } else if ( path.isPoint() ) { // path  instanceof DrawingPointPath
            if ( erase_mode == Drawing.FILTER_ALL || erase_mode == Drawing.FILTER_POINT ) {
              // ret = 1;
              eraseCmd.addAction( EraseAction.ERASE_REMOVE, path );
              mCurrentStack.remove( path );
              synchronized ( TDPath.mSelectionLock ) {
                mSelection.removePath( path );
              }
            }
          }
        }
      }
    }
    // checkLines();
    // return ret;
  }

  void addEraseCommand( EraseCommand cmd ) 
  { 
    synchronized( TDPath.mCommandsLock ) {
      mCurrentStack.add( cmd ); 
    }
  }

  List< DrawingPath > splitPlot( ArrayList< PointF > border, boolean remove ) 
  {
    ArrayList< DrawingPath > paths = new ArrayList<>();
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand c : mCurrentStack ) {
        if ( c.commandType() == 0 ) {
          DrawingPath p = (DrawingPath)c;
          if ( DrawingLevel.isLevelVisible( p ) && isInside( p.getX(), p.getY(), border ) ) {
            paths.add(p);
          }
        }
      }
      if ( remove ) {
        for ( DrawingPath pp : paths ) {
          mCurrentStack.remove( pp );
	  synchronized ( TDPath.mSelectionLock ) { 
            mSelection.removePath( pp );
	  }
        }
      }
    }
    return paths;
  }

  // USER STATION ---------------------------------------------------------
  void addUserStationsToList( ArrayList< DrawingStationPath > ret )
  {
    synchronized( TDPath.mStationsLock ) {
      // for ( DrawingStationPath st : mUserStations ) ret.add( st );
      ret.addAll( mUserStations );
    }
  }

  boolean hasUserStations() { return mUserStations.size() > 0; }

  DrawingStationPath getUserStation( String name )
  {
    if ( name != null ) {
      for ( DrawingStationPath sp : mUserStations ) if ( name.equals( sp.name() ) ) return sp;
    }
    return null;
  }

  void removeUserStation( DrawingStationPath path )
  {
    // TDLog.v( "remove user station " + path.mName );
    synchronized( TDPath.mStationsLock ) {
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
    // TDLog.v( "add user station " + path.mName );
    synchronized( TDPath.mStationsLock ) {
      mUserStations.add( path );
    }
  }
  // end USER STATION ---------------------------------------------------------

  void addCommand( DrawingPath path ) 
  {
    // TDLog.Log( TDLog.LOG_PLOT, "addCommand stack size  " + mCurrentStack.size() );
    // TDLog.v( "add command type " + path.mType + " " + path.left + " " + path.top + " " 
    //        + mBBox.left + " " + mBBox.top + " " + mBBox.right + " " + mBBox.bottom );

    mRedoStack.clear();

    if ( path.isArea() ) { // path instanceof DrawingAreaPath
      DrawingAreaPath area = (DrawingAreaPath)path;
      if ( area.mAreaCnt > mMaxAreaIndex ) {
        mMaxAreaIndex = area.mAreaCnt;
      }
    }

    // if ( path.isLine() ) { // path instanceof DrawingLinePath
    //   DrawingLinePath line = (DrawingLinePath)path;
    //   LinePoint lp = line.mFirst;
    //   TDLog.v("CMD add path. size " + line.size() + " start " + lp.x + " " + lp.y );
    // }
    
    synchronized( TDPath.mCommandsLock ) {
      mCurrentStack.add( path );
    }
    if ( path.mType != DrawingPath.DRAWING_PATH_NORTH ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelection.insertPath( path );
      }
    }
    
    // checkLines();
  }

  /** compute the bitmap bounding box, union of the bounding boxes of the sketch items
   * @param bound   output bounding box
   */
  void getBitmapBounds( RectF bounds )
  {
    RectF b = new RectF();
    if( mCurrentStack != null ){
      synchronized( TDPath.mCommandsLock ) {
        for ( ICanvasCommand cmd : mCurrentStack ) {
          cmd.computeBounds( b, true );
          // bounds.union( b );
          union( bounds, b );
        }
      }
    }
    // TDLog.v( "bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
  }

  /** draw on the canvas
   * @param c     canvas
   * @param mat   transform matrix
   * @param scale rescaling factor
   */
  void draw( Canvas c, Matrix mat, float scale )
  {
    if( mCurrentStack != null ){
      synchronized( TDPath.mCommandsLock ) {
        if ( TDSetting.mWithLevels == 0 ) { // treat no-levels case by itself
          for ( ICanvasCommand cmd : mCurrentStack ) {
            if ( cmd.commandType() == 0 ) {
              cmd.draw( c, mat, scale, null );
            }
          }
        } else {
          for ( ICanvasCommand cmd : mCurrentStack ) {
            if ( cmd.commandType() == 0 ) {
              if ( DrawingLevel.isLevelVisible( (DrawingPath)cmd ) ) {
                cmd.draw( c, mat, scale, null );
              }
            }
          }
        }
      }
    }
  }

  /** fine the line to continue
   * @param lp     line point
   * @param type   line type
   * @param zoom   display zoom
   * @param size   ...
   * @return the line to continue or null
   * @note line points are scene-coords
   *       continuation is checked in canvas-coords: canvas = offset + scene * zoom
   */
  DrawingLinePath getLineToContinue( LinePoint lp, int type, float zoom, float size )
  {
    String group = BrushManager.getLineGroup( type );
    if ( group == null ) return null;

    float delta = 2 * size / zoom;

    DrawingLinePath ret = null;
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue; // FIXME EraseCommand

        final DrawingPath path = (DrawingPath)cmd;
        if ( path.isLine() ) { // path instanceof DrawingLinePath
          DrawingLinePath line = (DrawingLinePath)path;
          // if ( line.mLineType == type ) 
          if ( group.equals( BrushManager.getLineGroup( line.mLineType ) ) )
          {
            if ( line.mFirst.distance( lp ) < delta || line.mLast.distance( lp ) < delta ) {
              if ( ret != null ) return null; // ambiguity
              ret = line;
            }
          }
        }
      }
    }
    // if ( ret != null ) mSelection.removePath( ret ); // FIXME do not remove continuation line
    // checkLines();
    return ret;
  }
        
  /** @return true if the line has been modified
   * @param line  line to modify
   * @param line2 modification
   * @param zoom  current zoom
   * @param size  selection size
   */
  boolean modifyLine( DrawingLinePath line, DrawingLinePath line2, float zoom, float size )
  {
    LinePoint lp1 = line.mFirst; 
    if ( lp1 == null ) {
      // TDLog.v( "modify line no start point");
      return false; // sanity check
    }
    if ( line2 == null || line2.size() < 3 ) {
      // TDLog.v( "modify line line2 null or short");
      return false;
    }
    float delta = size / zoom;
    // TDLog.v( "modify line: delta " + delta );
    LinePoint first = line2.mFirst;
    LinePoint last  = line2.mLast;
    for ( ; lp1 != null; lp1 = lp1.mNext ) {
      if ( lp1.distance( first ) < delta ) {
        LinePoint lp2 = null;
        LinePoint lp1n = lp1.mNext;
        if ( lp1n != null ) {
          lp2 = line.mLast;
          // int toDrop = 0; // number of points to drop
          for ( ; lp2 != lp1 && lp2 != null; lp2 = lp2.mPrev ) { // FIXME 20190512 check lp2 != null
            if ( lp2.distance( last ) < delta ) {
              lp2 = lp2.mNext; // backup one point
              break;
            }
            // ++ toDrop;
          }
          if ( lp2 == lp1 ) { // if loop ended because arrived to the initial point lp1
            lp2 = null;
          }
        } 
        // int old_size = line.size();
        // line.mSize += line2.mSize - toDrop; // better recount points
        synchronized( TDPath.mSelectionLock ) {
          mSelection.removePath( line );
        }
        synchronized( TDPath.mCommandsLock ) {
          // line.replacePortion( lp1, lp2, line2 );
          lp1.mNext = first.mNext;
          first.mPrev = lp1;
          last.mNext = lp2;
          if ( lp2 != null ) lp2.mPrev = last;
          line.recomputeSize();
          line.retracePath();
          // TDLog.v("size old " + old_size + " drop " + toDrop + " line2 " + line2.size() + " new " + line.size() );
        }
        synchronized( TDPath.mSelectionLock ) {
          mSelection.insertPath( line );
        }
        return true;
      } 
    }
    return false;
  }

  /** add the points of the first line to the second line
   */
  void addLineToLine( DrawingLinePath line1, DrawingLinePath line0 )
  {
    // TDLog.v( "add line to line" );
    DrawingLinePath line = new DrawingLinePath( line0.mLineType, mScrapIdx );
    boolean added = false;
    try {
      boolean prepend = line0.mFirst.distance( line1.mFirst ) < line0.mLast.distance( line1.mFirst );
      if ( prepend ) {
        line.appendReversedLinePoints( line1 );
        line.appendLinePoints( line0 );
      } else {
        line.appendLinePoints( line0 );
        line.appendLinePoints( line1 );
      }
      added = true;
    } catch ( OutOfMemoryError e ) {
      TDLog.Error("OOM " + e.getMessage() );
    }
    if ( added ) {
      synchronized( TDPath.mCommandsLock ) {
        mCurrentStack.remove( line0 );
        mCurrentStack.add( line );
      }
      synchronized( TDPath.mSelectionLock ) {
        mSelection.removePath( line0 );
        mSelection.insertPath( line );
      }
    } else {
      TDLog.Error( "FAILED add line to line ");
    }

    /*
    synchronized( TDPath.mSelectionLock ) {
      mSelection.removePath( line0 );
    }
    synchronized( TDPath.mCommandsLock ) {
      boolean reverse = line0.mFirst.distance( line1.mFirst ) < line0.mLast.distance( line1.mFirst );
      if ( reverse ) line0.reversePath();
      line0.append( line1 );
      if ( reverse ) {
        line0.reversePath();
        line0.computeUnitNormal();
      }
    }
    synchronized( TDPath.mSelectionLock ) {
      mSelection.insertPath( line0 );
    }
    */
    // checkLines();
  }

  // COMMAND STACK ---------------------------------------------
  public void addCommandsToList( ArrayList< DrawingPath > ret ) 
  {
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() == 0 ) {
          ret.add( (DrawingPath)cmd ); // FIXME copy path? ret.add( ((DrawingPath)cmd).clone() );
	}
      }
    }
  }

  /* Check if any line overlaps another of the same type
   * In case of overlap the overlapped line is removed
   */
  void checkLines()
  {
    synchronized( TDPath.mCommandsLock ) {
      int size = mCurrentStack.size();
      for ( int i1 = 0; i1 < size; ++i1 ) {
        ICanvasCommand cmd1 = mCurrentStack.get( i1 );
        if ( cmd1.commandType() != 0 ) continue;
        DrawingPath path1 = (DrawingPath)cmd1;
        if ( ! path1.isLine() ) continue; // !(path1 instanceof DrawingLinePath)
        DrawingLinePath line1 = (DrawingLinePath)path1;
        for ( int i2 = 0; i2 < size; ++i2 ) {
          if ( i2 == i1 ) continue;
          ICanvasCommand cmd2 = mCurrentStack.get( i2 );
          if ( cmd2.commandType() != 0 ) continue;
          DrawingPath path2 = (DrawingPath)cmd2;
          if ( ! path2.isLine() ) continue; // !(path2 instanceof DrawingLinePath)
          DrawingLinePath line2 = (DrawingLinePath)path2;
          // if every point in line2 overlaps a point in line1 
          if ( line1.overlap( line1 ) == line2.size() ) {
            TDLog.Error("LINE OVERLAP " + i1 + "-" + i2 + " total nr. " + size );
            // for ( int i=0; i<size; ++i ) {
            //   ICanvasCommand cmd = mCurrentStack.get( i );
            //   if ( cmd.commandType() != 0 ) continue;
            //   DrawingPath path = (DrawingPath)cmd;
            //   if ( ! path.isLine() ) continue; // !(path instanceof DrawingLinePath)
            //   DrawingLinePath line = (DrawingLinePath)path;
            //   line.dump();
            // }
            // TDLog.v( "LINE1 ");
            // line1.dump();
            // TDLog.v( "LINE2 ");
            // line2.dump();
            doDeletePath( line2 );
            -- size;
            -- i2;
            // throw new RuntimeException();
            if ( i2 < i1 ) --i1;
          }
        }
      }
    }
  }

  /** flip horizontally
   * @param z   ???
   */
  void flipXAxis( float z )
  {
    if ( mCurrentStack != null ) {
      Selection selection = new Selection();
      synchronized( TDPath.mCommandsLock ) {
        for ( ICanvasCommand cmd : mCurrentStack ) {
          if ( cmd.commandType() == 0 ) {
            cmd.flipXAxis(z);
            DrawingPath path = (DrawingPath)cmd;
            if ( path.isLine() ) { // path instanceof DrawingLinePath
              ((DrawingLinePath)path).flipReversed();
            }
            synchronized ( TDPath.mSelectionLock ) {
              selection.insertPath( path );
	    }
          }
        }
      }
      mSelection = selection;
    }
    synchronized( TDPath.mStationsLock ) {
      for ( DrawingStationPath p : mUserStations ) {
        p.flipXAxis(z);
      }
    }
  }

  /** shift the sketch
   * @param x   X shift
   * @param y   Y shift
   */
  void shiftDrawing( float x, float y )
  {
    if ( mCurrentStack != null ) {
      synchronized( TDPath.mCommandsLock ) {
        for ( ICanvasCommand cmd : mCurrentStack ) {
          cmd.shiftPathBy( x, y );
        }
      }
    }
    if ( mSelection != null ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelection.shiftSelectionBy( x, y );
      }
    }
  }

  void scaleDrawing( float z, Matrix m )
  {
    if ( mCurrentStack != null ){
      synchronized( TDPath.mCommandsLock ) {
        for ( ICanvasCommand cmd : mCurrentStack ) {
          cmd.scalePathBy( z, m );
        }
      }
    }
    if ( mSelection != null ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelection.scaleSelectionBy( z, m );
      }
    }
  }

  void affineTransformDrawing( float[] mm, Matrix m )
  {
    if ( mCurrentStack != null ){
      synchronized( TDPath.mCommandsLock ) {
        for ( ICanvasCommand cmd : mCurrentStack ) {
          cmd.affineTransformPathBy( mm, m );
        }
      }
    }
    if ( mSelection != null ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelection.affineTransformSelectionBy( mm, m );
      }
    }
  }

  /** insert points in the range of the selected point
   */
  void insertPointsHotItem()
  {
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return;
    if ( sp.type() != DrawingPath.DRAWING_PATH_LINE && sp.type() != DrawingPath.DRAWING_PATH_AREA ) return;
    if ( sp.mRange == null ) return;
    LinePoint lp1 = sp.mRange.start();
    if ( lp1 == null ) return;
    LinePoint lp2 = sp.mRange.end();
    DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;

    // lp0 if the point after lp1 - lp is inserted as midpoint between lp1 and lp0
    for ( LinePoint lp0 = lp1.mNext; lp1 != lp2 && lp0 != null; lp0 = lp0.mNext ) {
      float x = (lp1.x + lp0.x)/2;
      float y = (lp1.y + lp0.y)/2;
      LinePoint lp = line.insertPointAfter( x, y, lp1 ); 
      SelectionPoint sp1 = mSelection.insertPathPoint( line, lp );
      lp1 = lp0;
    } 
    syncClearSelected();
  }
      
  boolean moveHotItemToNearestPoint( float dmin )
  {
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return false;
    float x = 0.0f;
    float y = 0.0f;
    if ( sp.type() == DrawingPath.DRAWING_PATH_POINT ) {
      x = sp.mItem.cx;
      y = sp.mItem.cy;
    } else if ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) {
      x = sp.mPoint.x;
      y = sp.mPoint.y;
    } else {
      return false;
    }
    SelectionPoint spmin = mSelection.getNearestPoint( sp, x, y, dmin );

    if ( spmin != null ) {
      if ( spmin.type() == DrawingPath.DRAWING_PATH_LINE || spmin.type() == DrawingPath.DRAWING_PATH_AREA ) {
        x = spmin.mPoint.x - x;
        y = spmin.mPoint.y - y;
      } else {
        x = spmin.mItem.cx - x;
        y = spmin.mItem.cy - y;
      }
      // sp.shiftBy( x, y, 0f );
      sp.shiftBy( x, y );
    }
    return true;
  }

  boolean appendHotItemToNearestLine()
  {
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return false;
    if ( sp.type() != DrawingPath.DRAWING_PATH_LINE ) return false;
    if ( BrushManager.isLineSection( sp.type() ) ) return false; // NOT for "section" lines
    LinePoint pt1 = sp.mPoint;
    DrawingLinePath line1 = (DrawingLinePath)sp.mItem;
    if ( pt1 != line1.mFirst && pt1 != line1.mLast ) return false;

    int linetype = line1.mLineType;

    float x = 0.0f;
    float y = 0.0f;
    x = sp.mPoint.x;
    y = sp.mPoint.y;
    
    SelectionPoint spmin = mSelection.getNearestLineEndPoint( sp, x, y, 10f, linetype );
    if ( spmin == null ) return false;

    LinePoint pt2 = spmin.mPoint; // MERGE this line with "linemin"
    DrawingLinePath line2 = (DrawingLinePath)spmin.mItem;

    //
    DrawingLinePath line = new DrawingLinePath( line2.mLineType, mScrapIdx );
    boolean appended = false;
    try {
      boolean reversed1 = ( pt1 == line1.mLast );
      boolean reversed2 = ( pt2 == line2.mFirst );
      // TDLog.v( "Line1 reversed " + reversed1 + " Line2 reversed " + reversed2 );
      if ( reversed2 ) {
        if ( reversed1 ) {
          line.appendReversedLinePoints( line1 );
        } else {
          line.appendLinePoints( line1 );
        }
        line.appendLinePoints( line2 );
      } else { 
        line.appendLinePoints( line2 );
        if ( reversed1 ) {
          line.appendReversedLinePoints( line1 );
        } else {
          line.appendLinePoints( line1 );
        }
      }
      appended = true;
    } catch ( OutOfMemoryError e ) {
      TDLog.Error("OOM " + e.getMessage() );
    }
    if ( appended ) {
      synchronized( TDPath.mCommandsLock ) {
        mCurrentStack.remove( line1 );
        mCurrentStack.remove( line2 );
        mCurrentStack.add( line );
      }
      synchronized ( TDPath.mSelectionLock ) {
        mSelection.removePath( line2 );
        mSelection.removePath( line1 );
        mSelection.insertPath( line );
        mSelected.clear();
      }
    } else {
      TDLog.Error( "FAILED append hot item to nearest line");
    }
    /*
    synchronized ( TDPath.mSelectionLock ) {
      mSelection.removePath( line2 );
      mSelection.removePath( line1 );
    }

    boolean reverse1 = ( pt1 == line1.mLast );
    boolean reverse2 = ( pt2 == line2.mFirst );
    synchronized( TDPath.mCommandsLock ) {
      if ( reverse2 ) line2.reversePath();
      if ( reverse1 ) line1.reversePath();
      LinePoint pt = line1.mFirst; // append to end
      while ( pt != null ) {
        if ( pt.has_cp ) {
          line2.addPoint3( pt.x1, pt.y1, pt.x2, pt.y2, pt.x, pt.y );
        } else {
          line2.addPoint( pt.x, pt.y );
        }
        pt = pt.mNext;
      }
      if ( reverse1 ) line1.reversePath();
      mCurrentStack.remove( line1 );
      if ( reverse2 ) {
        line2.reversePath();
        line2.retracePath();
      }
    }
    synchronized ( TDPath.mSelectionLock ) {
      mSelection.insertPath( line2 );
      mSelected.clear();
    }
    */
    return true;
  }

  class NearbySplay
  {
    final float dx, dy;
    final float d; // distance from point
    final LinePoint pt; // point
    float llen, rlen;

    NearbySplay( float xx, float yy, float dd, LinePoint lp )
    {
      dx = xx;
      dy = yy;
      d  = dd;
      pt = lp;
    }
  }
  
  // return 0 ok
  //       -1 no hot item
  //       -2 not line
  //       -3 no splay
  int snapHotItemToNearestSplays( float dthr, DrawingStationSplay station_splay, List< DrawingSplayPath > splays_paths, boolean splays, boolean latest )
  {
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return -1;
    if ( sp.type() != DrawingPath.DRAWING_PATH_LINE ) return -2;

    DrawingPath item = sp.mItem;
    DrawingLinePath line = (DrawingLinePath)item;

    // nearby splays are the splays that get close enough (dthr) to the line
    ArrayList< NearbySplay > nearby_splays = new ArrayList<>();
    for ( DrawingSplayPath fxd : splays_paths ) {
      if ( station_splay == null ) {
        if ( ! splays ) continue;
      } else {
        if ( splays ) {
          if ( station_splay.isStationOFF( fxd ) ) continue;
        } else {
          if ( ! ( station_splay.isStationON( fxd ) || ( latest && fxd.isBlockRecent() ) ) ) continue;
        }
      }
      float x = fxd.x2;
      float y = fxd.y2;
      float dmin = dthr;
      LinePoint lpmin = null;
      for ( LinePoint lp2 = line.mFirst; lp2 != null; lp2=lp2.mNext ) {
        float d = lp2.distance( x, y );
        if ( d < dmin ) {
          dmin = d;
          lpmin = lp2;
        } else if ( lpmin != null ) { // if distances increase after a good min, break
          nearby_splays.add( new NearbySplay( fxd.x2 - lpmin.x, fxd.y2 - lpmin.y, dmin, lpmin ) );
          break;
        }
      }
    }
    // TDLog.v( "Nearby splays " + nearby_splays.size() + " line size " + line.size() );
    int ks = nearby_splays.size();
    if ( ks == 0 ) return -3;
    // check that two nearby splays do not have the same linepoint
    for ( int k1 = 0; k1 < ks; ) {
      NearbySplay nbs1 = nearby_splays.get( k1 );
      int dk1 = 1; // increment of k1
      int k2 = k1+1;
      while ( k2<ks ) {
        NearbySplay nbs2 = nearby_splays.get( k2 );
        if ( nbs1.pt == nbs2.pt ) {
          ks --;
          if ( nbs1.d <= nbs2.d ) {
            nearby_splays.remove( k2 );
          } else {
            nearby_splays.remove( k1 );
            dk1 = 0;
            break;
          }
        } else {
          k2 ++;
        }
      }
      k1 += dk1;
    }
    // TDLog.v( "Nearby splays " + nearby_splays.size() + " / " + ks );

    // compute distances between consecutive line points
    // and order nearby_splays following the line path
    int k = 0; // partition of unity
    float len = 0.001f;
    LinePoint lp1 = line.mFirst;
    int size = line.size();
    float[] dist = new float[ size ];
    int k0 = 0;
    for ( LinePoint lp2 = line.mFirst; lp2 != null; lp2 = lp2.mNext ) {
      dist[k0] = lp1.distance( lp2 );
      len += dist[k0];
      ++k0;

      int kk = k;
      for ( ; kk<ks; ++kk ) {
        if ( lp2 == nearby_splays.get(kk).pt ) {
          if ( kk != k ) { // swap nearby_splays k <--> kk
            NearbySplay nbs = nearby_splays.remove( kk );
            nearby_splays.add( k, nbs );
          }
          nearby_splays.get(k).llen = len;
          if ( k > 0 ) nearby_splays.get( k-1 ).rlen = len;
          len = 0;
          ++ k;
          break;
        }
      }
      lp1 = lp2; // lp1 = previous point
    }
    len += 0.001f;
    nearby_splays.get( k-1 ).rlen = len;

    //   |----------*--------*-----
    //      llen   sp1 rlen
    //                 llen sp2 rlen

    k0 = 0;
    int kl = -1;
    int kr = 0;
    len = 0;
    LinePoint lp2 = line.mFirst;
    NearbySplay spr = null; // right splay
    for ( NearbySplay spl : nearby_splays ) { // left splay
      while ( lp2 != spl.pt /* && lp2 != null && k0 < size */ ) { // N.B. lp2 must be non-null and k0 must be < size
        len += dist[k0];
        float dx = len/spl.llen * spl.dx;
        float dy = len/spl.llen * spl.dy;
        if ( spr != null ) {
          dx += (1 - len/spr.rlen) * spr.dx;
          dy += (1 - len/spr.rlen) * spr.dy;
        }
        lp2.shiftBy( dx, dy );
        lp2 = lp2.mNext;
        ++ k0;
      }
      // if ( lp2 == spl.pt ) { // this must be true
        lp2.shiftBy( spl.dx, spl.dy );
        lp2 = lp2.mNext;
      // }
      spr = spl;
      // if ( k0 >= size ) break;
      ++ k0;
      len = 0;
    }
    if ( spr != null ) { // always true
      while ( lp2 != null /* && k0 < size */ ) { // N.B. k0 must be < size
        len += dist[k0];
        float dx = (1 - len/spr.rlen) * spr.dx;
        float dy = (1 - len/spr.rlen) * spr.dy;
        lp2.shiftBy( dx, dy );
        lp2 = lp2.mNext;
        ++ k0;
      }
    }
    line.retracePath();

    return 0;
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
    LinePoint q0 = sp.mPoint;
    LinePoint q1 = area.next( q0 ); // next point on the area border
    LinePoint q2 = area.prev( q0 ); // previous point on the border
    // area border: ... --> q2 --> q0 --> q1 --> ...

    float x = q0.x;
    float y = q0.y;
    float thr = 10f;
    float dmin = thr; // require a minimum distance
    DrawingPointLinePath lmin = null;
    boolean min_is_area = false;
    // int kk0 = -1;

    // find drawing path with minimal distance from (x,y)
    LinePoint pp0 = null;

    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath)cmd;
        if ( p == item ) continue;
        if ( ! p.isLineOrArea() ) continue; // !(p instanceof DrawingPointLinePath)
        DrawingPointLinePath lp = (DrawingPointLinePath)p;
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
            min_is_area = p.isArea();
            // min_is_area = (p instanceof DrawingAreaPath);
          }
        }
      }
    }
    if ( lmin == null ) return -3;
    int cmax = area.size() + 1;
    
    // if ( TDLog.LOG_DEBUG ) { // ===== FIRST SET OF LOGS
    //   TDLog.Debug( "snap to line");
    //   for ( LinePoint pt = lmin.mFirst; pt!=null; pt=pt.mNext ) TDLog.Debug( pt.x + " " + pt.y );
    //   TDLog.Debug( "snap area");
    //   for ( LinePoint pt = area.mFirst; pt!=null; pt=pt.mNext ) TDLog.Debug( pt.x + " " + pt.y );
    //   TDLog.Debug( "snap qq0= " + q0.x + " " + q0.y + " to pp0= " + pp0.x + " " + pp0.y );
    // }

    int ret = 0; // return code

    LinePoint pp1 = lmin.next( pp0 );
    LinePoint pp2 = lmin.prev( pp0 );
    //
    // lmin: ... ---> pp2 ---> pp0 ---> pp1 --->
    // area: ...      q2 ----> q0 ----> q1 ...
    //                qq2 ------------> qq1          FORWARD
    //
    // area: ...      q1 <---- q0 <---- q2 ...
    //                qq2 ------------> qq1          REVERSE

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
      // TDLog.Debug( "snap pp1 " + pp1.x + " " + pp1.y + " FOLLOW LINE FORWARD" );
      // pp1  = pts1.get( kk1 );
      // pp10 = pts1.get( kk0 );
      pp10 = pp0;
      // if ( kk2 >= 0 ) 
      if ( pp2 != null ) {
        // TDLog.Debug( "snap pp2 " + pp2.x + " " + pp2.y );
        // pp2  = pts1.get( kk2 ); 
        // pp20 = pts1.get( kk0 ); 
        pp20 = pp0;
      }
      if ( pp1.distance( q1 ) < pp1.distance( q2 ) ) {
        qq1  = q1; // follow border forward
        qq10 = q0;
        // TDLog.Debug( "snap qq1 " + qq1.x + " " + qq1.y + " follow border forward" );
        if ( pp2 != null ) {
          qq2  = q2;
          qq20 = q0;
          // TDLog.Debug( "snap qq2 " + qq2.x + " " + qq2.y );
        }
      } else {
        reverse = true;
        qq1  = q2; // follow border backward
        qq10 = q0;
        // TDLog.Debug( "snap reverse qq1 " + qq1.x + " " + qq1.y + " follow border backward" );
        if ( pp2 != null ) {
          qq2 = q1;
          qq20 = q0;
          // TDLog.Debug( "snap qq2 " + qq2.x + " " + qq2.y + " follow forward");
        }
      }
    } else if ( pp2 != null ) { // pp10 is null
      // pp2  = pts1.get( kk2 ); 
      // pp20 = pts1.get( kk0 ); 
      pp20 = pp0;
      // TDLog.Debug( "snap pp1 null pp2 " + pp2.x + " " + pp2.y + " FOLLOW LINE BACKWARD" );
      if ( pp2.distance( q2 ) < pp2.distance( q1 ) ) {
        qq2 = q2;
        qq20 = q0;
        // TDLog.Debug( "snap qq2 " + qq2.x + " " + qq2.y + " follow border backward" );
      } else {
        reverse = true;
        qq2 = q1;
        qq20 = q0;
        // TDLog.Debug( "snap reverse qq2 " + qq2.x + " " + qq2.y + " follow border forward" );
      }
    } else {  // pp10 and pp20 are null: nothing to follow
      // copy pp0 to q0
      q0.x = pp0.x;
      q0.y = pp0.y;
      ret = 1;
    }

    if ( qq1 != null ) {
      // TDLog.Debug( "qq1 not null " + qq1.x + " " + qq1.y + " reverse " + reverse );
      // follow line pp10 --> pp1 --> ... using step 1
      // with border qq10 --> qq1 --> ... using step delta1
      //
      // lmin: ... ---> pp2 ---> pp0 ---> pp1 --->
      //                         pp10 --> pp1 --->
      // area: ...      q2 ----> q0 ----> q1 ...
      //                qq2 ------------> qq1          FORWARD

      for (int c=0; c<cmax; ++c) { // try to move qq1 forward
        // TDLog.Debug( "snap at qq1 " + qq1.x + " " + qq1.y );
        float s = qq1.orthoProject( pp10, pp1 );
        while ( s > 1.0 ) {
          pp10 = pp1;
          // TDLog.Debug( "snap follow pp10 " + pp10.x + " " + pp10.y );
          pp1  = lmin.next( pp1 );
          if ( pp1 == null ) {
            // TDLog.Debug( "snap end of line pp1 null, pp10 " + pp10.x + " " + pp10.y );
            break;
          }
          if ( pp1 == pp0 ) {
            // TDLog.Debug( "snap pp1 == pp0, pp10 " + pp10.x + " " + pp10.y );
            break;
          }
          s = qq1.orthoProject( pp10, pp1 );
        }
        if ( pp1 == null ) break;
        float d1 = qq1.orthoDistance( pp10, pp1 );
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
    //   TDLog.Debug( "QQ10 " + qq10.x + " " + qq10.y + " PP10 " + pp10.x + " " + pp10.y );
    // }

    if ( qq2 != null ) {
      // TDLog.Debug( "qq2 not null: " + qq2.x + " " + qq2.y + " reverse " + reverse );
      // follow line pp20 --> pp2 --> ... using step size1-1
      // with border qq20 --> qq2 --> ... using step delta2
      for (int c=0; c < cmax; ++c) { // try to move qq2 backward
        // TDLog.Debug( "snap at qq2 " + qq2.x + " " + qq2.y );
        float s = qq2.orthoProject( pp20, pp2 );
        while ( s > 1.0 ) {
          pp20 = pp2;
          // TDLog.Debug( "snap s>1, follow pp20 " + pp20.x + " " + pp20.y );
          pp2 = lmin.prev( pp2 );
          if ( pp2 == null ) {
            // TDLog.Debug( "snap end of line pp2 null, pp20 " + pp20.x + " " + pp20.y );
            break;
          }
          if ( pp2 == pp0 ) {
            // TDLog.Debug( "snap pp2 == pp0, pp20 " + pp20.x + " " + pp20.y );
            break;
          }
          s = qq2.orthoProject( pp20, pp2 );
        }
        if ( pp2 == null ) break;
        float d2 = qq2.orthoDistance( pp20, pp2 );
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
    //   TDLog.Debug( "QQ20 " + qq20.x + " " + qq20.y + " PP20 " + pp20.x + " " + pp20.y );
    // }

    if ( qq20 == qq10 || (reverse && pp10 == null) || (!reverse && pp20 == null) ) {
      // should not happen, anyways copy pp0 to q0
      q0.x = pp0.x;
      q0.y = pp0.y;
      ret = 2;
    }

    synchronized( TDPath.mCommandsLock ) {
      if ( ret == 0 ) { 
        synchronized( TDPath.mSelectionLock ) {
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
          // TDLog.Debug( "snap start prev " + prev.x + " " + prev.y );
          LinePoint q = prev;
          while ( prev != null && prev != next ) {
            q = prev;
            prev = q.mPrev;
          }
          area.mFirst = q;
          if ( q.mPrev != null ) { // make sure first has no prev
            q.mPrev.mNext = null;
          }
          q.mPrev = null;
          // TDLog.Debug( "snap setting area FIRST " + area.mFirst.x + " " + area.mFirst.y );
        }

        if ( next == null ) {
          area.mLast = null; // ( reverse )? qq20 : qq10;
          // TDLog.Debug( "snap setting area LAST null ");
        } else {
          // TDLog.Debug( "snap start next " + next.x + " " + next.y );
          LinePoint q = next;
          while ( next != null && next != prev ) {
            q = next;
            next = q.mNext;
          }
          area.mLast = q;
          if ( q.mNext != null ) {
            q.mNext.mPrev = null;
          }
          q.mNext = null;
          // TDLog.Debug( "snap setting area LAST " + area.mLast.x + " " + area.mLast.y );
        }

        next = (reverse)? qq20 : qq10; // where to close the snapped portion
        prev = (reverse)? qq10 : qq20; // where to start the snapped portion
        // it can be qq10.next == qq20 (forward)
        if ( next.mNext == prev ) {
          for ( LinePoint qc = prev; qc != null && qc != next; ) {
            LinePoint qn = qc.mNext;
            if ( qn != null ) qn.mPrev = null;
            qc.mNext = null;
            qc = qn;
          }
          area.mFirst = next;
          area.mLast  = prev;
          if ( area.mFirst != null ) { // always true: area.mFirst == next != null
            area.mFirst.mNext = area.mLast;
            area.mFirst.mPrev = null;
            if ( area.mLast != null ) // always true [?]
            {
              area.mLast.mPrev = area.mFirst;
              area.mLast.mNext = null;
            } else { 
              area.mLast = area.mFirst;
            }
          } else { 
            area.mFirst = area.mLast;
            if ( area.mFirst != null ) area.mFirst.mNext = area.mFirst.mPrev = null;
          }
        }

        // insert points pp20 - ... - pp10 (included)
        if ( reverse ) {
          LinePoint q = qq10.mPrev;
          LinePoint p = pp10;
          // if ( q != null ) {
          //   // TDLog.Debug( "snap attach at " + q.x + " " + q.y );
          // } else {
          //   // TDLog.Debug( "snap restart area ");
          // }
          q = new LinePoint( p.x, p.y, q );
          // TDLog.Debug( "snap first new point " + q.x + " " + q.y );
          if ( p != pp20 ) {
            p = p.mPrev;
            if ( area.mFirst == null ) area.mFirst = q;
            for ( ; p != null && p != pp20; p = p.mPrev ) {
              if ( p.has_cp && p != pp10 ) {
                LinePoint pp = p.mNext;
                q = new LinePoint( pp.x2, pp.y2, pp.x1, pp.y1, p.x, p.y, q );
              } else {
                q = new LinePoint( p.x, p.y, q );
              }
              // TDLog.Debug( "snap new point " + q.x + " " + q.y );
            }
            if ( p != null ) { // FIXME add last point
              if ( p.has_cp ) {
                LinePoint pp = p.mNext;
                q = new LinePoint( pp.x2, pp.y2, pp.x1, pp.y1, p.x, p.y, q );
              } else {
                q = new LinePoint( p.x, p.y, q );
              }
              // TDLog.Debug( "snap last new point " + q.x + " " + q.y );
            }
          }
          q.mNext = next;
          if ( next != null ) { // always true [?]
            next.mPrev  = q;
            next.has_cp = false; // enforce straight segment
          }
          if ( area.mLast == null ) area.mLast = q;

        } else { // not reverse

          LinePoint q = qq20.mPrev;
          LinePoint p = pp20;
          // if ( q != null ) {
          //   // TDLog.Debug( "snap attach at " + q.x + " " + q.y );
          // } else {
          //   // TDLog.Debug( "snap restart area ");
          // }
          q = new LinePoint( p.x, p.y, q );
          // TDLog.Debug( "snap first new point " + q.x + " " + q.y );
          if ( p != pp10 ) {
            p = p.mNext;
            if ( area.mFirst == null ) area.mFirst = q;
            for ( ; p != null && p != pp10; p = p.mNext ) {
              q = new LinePoint( p, q );
              // TDLog.Debug( "snap new point " + q.x + " " + q.y );
            }
            // if ( p != null ) { // FIXME not add "last" point
            //   q = new LinePoint( p, q );
            //   TDLog.Debug( "snap last new point " + q.x + " " + q.y );
            // }
          }
          q.mNext = next;
          if ( next != null ) { // always true [?]
            next.mPrev  = q;
            next.has_cp = false;
          }
          if ( area.mLast == null ) area.mLast = q;
        }


        // if ( area.mLast == area.mFirst ) { // avoid circular closed border
        //   area.mLast = area.mLast.mPrev;
        //   area.mLast.mNext = null;
        //   area.mFirst.mPrev = null;
        // }

        area.recount(); 
        // TDLog.Debug( "snap new size " + area.size() );
      }

      // area.mPoints = pts2;
      area.retracePath();
      
      if ( ret == 0 ) {
        synchronized( TDPath.mSelectionLock ) {
          mSelection.insertPath( area );
        }
      }
      syncClearSelected();
    }
    // checkLines();
    return ret;
  }

  SelectionPoint hotItem() { return mSelected.mHotItem; }

  boolean hasSelected() { return mSelected.mPoints.size() > 0; }

  void rotateHotItem( float dy )
  { 
    synchronized( TDPath.mSelectionLock ) {
      mSelected.rotateHotItem( dy );
    }
  }

  // void shiftHotItem( float dx, float dy, float range ) 
  void shiftHotItem( float dx, float dy, List< DrawingOutlinePath > xsections )
  { 
    synchronized( TDPath.mSelectionLock ) {
      // SelectionPoint sp = mSelected.shiftHotItem( dx, dy, range );
      SelectionPoint sp = mSelected.shiftHotItem( dx, dy );
      if ( sp != null ) {
        DrawingPath path = sp.mItem;
        if ( path.isPoint() ) { // path instanceof DrawingPointPath
          DrawingPointPath pt = (DrawingPointPath)path;
          if ( BrushManager.isPointSection( pt.mPointType )  ) {
            String scrap_name = TDUtil.replacePrefix( TDInstance.survey, pt.getOption( TDString.OPTION_SCRAP ) );
            if ( scrap_name != null ) {
              // TDLog.v("Shift " + scrap_name + " X-section " + xsections.size() );
              synchronized( TDPath.mXSectionsLock ) {
                for ( DrawingOutlinePath xsection : xsections ) {
                  if ( xsection.isScrapName( scrap_name ) ) {
                    xsection.mPath.shiftBy( dx, dy );
                    // break;
                  }
                }
              }
            }
          }
        }
        mSelection.checkBucket( sp );
      }
    }
  }

  SelectionPoint nextHotItem() { return mSelected.nextHotItem(); }
  SelectionPoint prevHotItem() { return mSelected.prevHotItem(); }

  // compute the bounding box for a scrap index or the global one
  // @param scrap   scrap index (-1 for global bbox)
  RectF computeBBox( )
  {
    float xmin=1000000f, xmax=-1000000f, 
          ymin=1000000f, ymax=-1000000f;
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;
        // RectF bbox = p.mBBox;
        if ( p.left   < xmin ) xmin = p.left;
        if ( p.right  > xmax ) xmax = p.right;
        if ( p.top    < ymin ) ymin = p.top;
        if ( p.bottom > ymax ) ymax = p.bottom;
      }
    }
    mBBox = new RectF( xmin, ymin, xmax, ymax ); // left top right bottom
    return mBBox;
  }

  // get the bounding box: must have been previously computed with computeBBox()
  // this is done by the command manager getBoundingBox()
  RectF getBBox() 
  {
    return mBBox;
  }

  DrawingAudioPath getAudioPoint( long bid )
  {
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() == 0 ) {
          DrawingPath path = (DrawingPath)cmd;
          if ( path.isPoint() ) { // path instanceof DrawingPointPath
            DrawingPointPath pt = (DrawingPointPath)path;
            if ( pt instanceof DrawingAudioPath ) { // BrushManager.isPointAudio( pt.mPointType )
              DrawingAudioPath audio = (DrawingAudioPath)pt;
              if ( audio.mId == bid ) return audio;
            }
          }
        }
      }
    }
    return null;
  }

  float computeSectionArea()
  {
    float ret = 0;
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand icc : mCurrentStack ) {
        if ( icc.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath)icc;
        if ( ! p.isLine() ) continue; // !(p instanceof DrawingLinePath)
        DrawingLinePath lp = (DrawingLinePath)p;
        if ( ! BrushManager.isLineWall( lp.mLineType ) ) continue;
        LinePoint pt = lp.mFirst;
        while ( pt != lp.mLast ) {
          LinePoint pn = pt.mNext;
          ret += pt.y * pn.x - pt.x * pn.y;
          pt = pn;
        }
      }
    }
    return ret / 2;
  }

  void linkSections( List< DrawingStationName > stations )
  {
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack ) {
        if ( cmd.commandType() != 0 ) continue; 
        DrawingPath p = (DrawingPath)cmd;
        if ( ! p.isPoint() ) continue; // !(p instanceof DrawingPointPath)
        DrawingPointPath pt = (DrawingPointPath)p;
        if ( ! BrushManager.isPointSection( pt.mPointType ) ) continue;
        // get the line/station
        String scrap = TDUtil.replacePrefix( TDInstance.survey, p.getOption( TDString.OPTION_SCRAP ) );
        if ( scrap != null ) {
          // TDLog.v( "section point scrap " + scrap );
          int pos = scrap.lastIndexOf( "-xx" );
          if ( pos > 0 ) {
            String id = scrap.substring(pos+1); // line id
            if ( /* id != null && */ id.length() > 0 ) { // id always not null [?]
              for ( ICanvasCommand cmd2 : mCurrentStack ) {
                if ( cmd2.commandType() != 0 ) continue; 
                DrawingPath p2 = (DrawingPath)cmd2;
                if ( ! p2.isLine() ) continue; // !(p2 instanceof DrawingLinePath)
                DrawingLinePath ln = (DrawingLinePath)p2;
                if ( ! BrushManager.isLineSection( ln.mLineType ) ) continue;
                if ( id.equals( ln.getOption("-id") ) ) {
                  pt.setLink( ln );
                  break;
                }
              }
            }
          } else {
            pos = scrap.lastIndexOf( "-xs-" );
            if ( pos < 0 ) pos = scrap.lastIndexOf( "-xh-" );
            if ( pos > 0 ) {
              String name = scrap.substring(pos+4);
              if ( /* name != null && */ name.length() > 0 ) { // name always not null [?]
                // TDLog.v( "section station " + name );
                for ( DrawingStationName st : stations ) {
                  if ( name.equals( st.getName() ) ) {
                    pt.setLink( st );
                    break;
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  public void redo()
  {
    final int length = mRedoStack.toArray().length;
    if ( length > 0) {
      final ICanvasCommand cmd = mRedoStack.get(  length - 1  );
      mRedoStack.remove( length - 1 );

      if ( cmd.commandType() == 0 ) {
        DrawingPath redoCommand = (DrawingPath)cmd;
        synchronized( TDPath.mCommandsLock ) {
          mCurrentStack.add( redoCommand );
        }
        synchronized( TDPath.mSelectionLock ) {
          mSelection.insertPath( redoCommand );
        }
      } else {
        EraseCommand eraseCmd = (EraseCommand) cmd;
        for ( EraseAction action : eraseCmd.mActions ) {
          DrawingPath path = action.mPath;
          // TDLog.v( "REDO " + actionName[action.mType] + " path " + path.mType );
          if ( action.mInitialType == EraseAction.ERASE_INSERT ) {
            synchronized( TDPath.mCommandsLock ) {
              mCurrentStack.add( path );
            }
            synchronized( TDPath.mSelectionLock ) {
              mSelection.insertPath( path );
            }
          } else if ( action.mType == EraseAction.ERASE_REMOVE ) {
            synchronized( TDPath.mCommandsLock ) {
              mCurrentStack.remove( path );
            }
            synchronized( TDPath.mSelectionLock ) {
              mSelection.removePath( path );
            }
          } else if ( action.mType == EraseAction.ERASE_MODIFY ) {
            synchronized( TDPath.mSelectionLock ) {
              mSelection.removePath( path );
            }
            synchronized( TDPath.mCommandsLock ) {
              action.restorePoints( false ); // false: use new points
            }
            synchronized( TDPath.mSelectionLock ) {
              mSelection.insertPath( path );
            }
          }
        }
        synchronized( TDPath.mCommandsLock ) {
          mCurrentStack.add( cmd );
        }
      }
    }
    // checkLines();
  }

  // --------------------------------------------------------------

  boolean setRangeAt( float x, float y, float zoom, int type, float size )
  {
    SelectionPoint sp1 = mSelected.mHotItem;
    if ( sp1 == null ) {
      // TDLog.v( "set range at: hotItem is null" );
      return false;
    }
    DrawingPath item = sp1.mItem;
    if ( ! item.isLineOrArea() ) { // !(item instanceof DrawingPointLinePath)
      // TDLog.v( "set range at: item not line/area" );
      // mSelected.clear();
      return false;
    }

    if ( SelectionRange.isItem( type ) ) {
      DrawingPointLinePath path = (DrawingPointLinePath)item;
      sp1.setRangeTypeAndPoints( type, path.first(), path.last(), 0, 0 );
      return true;
    }

    float radius = TDSetting.mCloseCutoff + size / zoom;
    SelectionPoint sp2 = null;
    // synchronized ( TDPath.mSelectedLock ) {
    synchronized ( TDPath.mSelectionLock ) {
      sp2 = mSelection.selectOnItemAt( item, x, y, 4*radius );
    }
    if ( sp2 == null ) {
      // TDLog.v( "set range at: select on Item return null");
      mSelected.clear();
      return false;
    }
    
    // range is sp1 -- sp2
    LinePoint lp1 = sp1.mPoint;
    LinePoint lp2 = sp2.mPoint;
    int cnt = 0;
    LinePoint lp = lp1;
    for ( ; lp != null; lp=lp.mNext ) { ++cnt; if ( lp == lp2 ) break; }
    if ( lp == null ) {
      cnt = 0;
      for ( lp=lp1; lp != null; lp=lp.mPrev ) { ++cnt; if ( lp == lp2 ) break; }
      if ( lp == null ) { // error
        // TDLog.v( "set range at: error lp==null");
        return false;
      }
      lp = lp1; lp1 = lp2; lp2 = lp; // swap lp1 <--> lp2
    } 
    LinePoint lp0 = lp1;
    float d1 = 0;
    float d2 = 0;
    int c1 = 0;
    int c2 = 0;
    for ( int c = cnt/2; c > 0; --c ) {
      ++ c1;
      lp = lp0.mNext; 
      d1 += lp0.distance( lp );
      lp0 = lp;
    }
    LinePoint lp4 = lp0;
    for ( LinePoint lp3 = lp0.mNext; lp3 != null; lp3=lp3.mNext) {
      ++ c2;
      d2 += lp4.distance( lp3 );
      if ( lp3 == lp2 ) break;
      lp4 = lp3;
    }
    // TDLog.v( "set range d1 " + d1 + " d2 " + d2 + " C " + cnt + " " + c1 + " " + c2 );
     
    // now make the range sp1 -- sp2 and the hotItem the midpoint
    SelectionPoint sp = mSelection.getSelectionPoint( lp0 ); 
    sp.setRangeTypeAndPoints( type, lp1, lp2, d1, d2 );

    mSelected.clear();
    mSelected.addPoint( sp );
    mSelected.mHotItem = sp;

    return true;
  }

  // AREA ACTIONS ----------------------------------------------------

  void shiftAreaShaders( float dx, float dy, float s, boolean landscape )
  {
    synchronized ( TDPath.mCommandsLock ) {
      for ( ICanvasCommand c : mCurrentStack ) {
        if ( c.commandType() == 0 ) {
          DrawingPath path = (DrawingPath)c;
          path.mLandscape = landscape;
          if ( path.isArea() ) { // path instanceof DrawingAreaPath
            DrawingAreaPath area = (DrawingAreaPath)path;
            area.shiftShaderBy( dx, dy, s );
          }
        }
      }
    }
  }

  // DRAW ACTIONS --------------------------------------------------------

  /** draw the outline
   * @param canvas   canvas
   * @param mat      transform matrix
   * @param bbox     clipping rectangle
   */
  void drawOutline( Canvas canvas, Matrix mat, RectF bbox )
  {
    if ( mCurrentStack == null ) return;
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack  ) {
        if ( cmd.commandType() == 0 ) {
          DrawingPath path = (DrawingPath)cmd;
          if ( path.isLine() ) { // path instanceof DrawingLinePath
            if ( ((DrawingLinePath)path).hasOutline() ) cmd.draw( canvas, mat, bbox );
          }
        }
      }
    }
  }

  /** draw with a grey outline
   * @param canvas   canvas
   * @param mat      transform matrix
   * @param bbox     clipping rectangle
   */
  void drawGreyOutline( Canvas canvas, Matrix mat, RectF bbox )
  {
    if ( mCurrentStack == null ) return;
    synchronized( TDPath.mCommandsLock ) {
      for ( ICanvasCommand cmd : mCurrentStack  ) {
        if ( cmd.commandType() == 0 ) {
          DrawingPath path = (DrawingPath)cmd;
          if ( path.isLine() ) { // path instanceof DrawingLinePath
            DrawingLinePath line = (DrawingLinePath)path;
            if ( line.hasOutline() ) line.drawWithPaint( canvas, mat, bbox, BrushManager.fixedGrid100Paint );
          }
        }
      }
    }
  }

  /** draw all sketch items
   * @param canvas   canvas
   * @param mat      transform matrix
   * @param scale    rescaling factor
   * @param bbox     clipping rectangle
   */
  void drawAll( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( mCurrentStack == null ) return;
    synchronized( TDPath.mCommandsLock ) {
      if ( TDSetting.mWithLevels == 0 ) { // treat no-levels case by itself
        for ( ICanvasCommand cmd : mCurrentStack  ) {
          if ( cmd.commandType() == 0 ) {
            DrawingPath path = (DrawingPath)cmd;
            cmd.draw( canvas, matrix, scale, bbox );
            if ( path.isLine() ) { // path instanceof DrawingLinePath
              DrawingLinePath line = (DrawingLinePath)path;
              if ( BrushManager.isLineSection( line.mLineType ) ) { // add direction-tick to section-lines
                LinePoint lp = line.mFirst;
                Path path1 = new Path();
                path1.moveTo( lp.x, lp.y );
                path1.lineTo( lp.x+line.mDx*TDSetting.mArrowLength, lp.y+line.mDy*TDSetting.mArrowLength );
                path1.transform( matrix );
                canvas.drawPath( path1, BrushManager.mSectionPaint );
              }
            }
          }
        }
      } else {
        for ( ICanvasCommand cmd : mCurrentStack  ) {
          if ( cmd.commandType() == 0 ) {
            DrawingPath path = (DrawingPath)cmd;
            if ( DrawingLevel.isLevelVisible( (DrawingPath)cmd ) ) {
              cmd.draw( canvas, matrix, scale, bbox );
              if ( path.isLine() ) { // path instanceof DrawingLinePath
                DrawingLinePath line = (DrawingLinePath)path;
                if ( BrushManager.isLineSection( line.mLineType ) ) { // add direction-tick to section-lines
                  LinePoint lp = line.mFirst;
                  Path path1 = new Path();
                  path1.moveTo( lp.x, lp.y );
                  path1.lineTo( lp.x+line.mDx*TDSetting.mArrowLength, lp.y+line.mDy*TDSetting.mArrowLength );
                  path1.transform( matrix );
                  canvas.drawPath( path1, BrushManager.mSectionPaint );
                }
              }
            }
          }
        }
      }
    }
  }

  /** draw the user stations
   * @param canvas   canvas
   * @param mat      transform matrix
   * @param bbox     clipping rectangle
   */
  void drawUserStations( Canvas canvas, Matrix matrix, RectF bbox )
  {
    synchronized( TDPath.mStationsLock ) {
      for ( DrawingStationPath p : mUserStations ) p.draw( canvas, matrix, bbox );
    }
  }

  // called under TDPath.mSelectionLock
  void displayPoints( Canvas canvas, Matrix matrix, RectF bbox, float dot_radius,
                      boolean spoints, boolean slines, boolean sareas, boolean splays, boolean legs_sshots, boolean sstations
                      /* , DrawingStationSplay station_splay */ )
  {
    if ( TDSetting.mWithLevels == 0 ) { // treat no-levels case by itself
      for ( SelectionBucket bucket: mSelection.mBuckets ) {
        if ( bucket.intersects( bbox ) && bucket.mPoints != null ) { // SAFETY CHECK
          for ( SelectionPoint pt : bucket.mPoints ) { 
            int type = pt.type();
            if ( type == DrawingPath.DRAWING_PATH_POINT ) {
              if ( ! spoints ) continue;
            } else if ( type == DrawingPath.DRAWING_PATH_LINE ) {
              if ( ! slines ) continue;
            } else if ( type == DrawingPath.DRAWING_PATH_AREA ) {
              if ( ! sareas ) continue;
            } else if ( ! DrawingPath.isDrawingType( type ) ) { // FIXME-HIDE should not happen
              continue;
            }
            // else if ( type == DrawingPath.DRAWING_PATH_FIXED ) {
            //   TDLog.v("HIDE scrap display fixed 1");
            //   if ( ! legs_sshots ) continue;
            // } else if ( type == DrawingPath.DRAWING_PATH_NAME ) {
            //   TDLog.v("HIDE scrap display station name 1");
            //   if ( ! sstations ) continue;
            // // else if ( type == DrawingPath.DRAWING_PATH_SPLAY && ! (splays && sshots) )
            // } else if ( type == DrawingPath.DRAWING_PATH_SPLAY ) {
            //   TDLog.v("HIDE scrap display splay 1");
            //   // FIXME_LATEST latest splays
            //   if ( splays ) {
            //     if ( station_splay.isStationOFF( pt.mItem ) ) continue;
            //   } else {
            //     if ( ! station_splay.isStationON( pt.mItem ) ) continue;
            //   }
            // } 
            TDGreenDot.draw( canvas, matrix, pt, dot_radius );
          }
        }
      }
    } else {
      for ( SelectionBucket bucket: mSelection.mBuckets ) {
        if ( bucket.intersects( bbox ) && bucket.mPoints != null ) { // SAFETY CHECK
          for ( SelectionPoint pt : bucket.mPoints ) { 
            int type = pt.type();
            if ( type == DrawingPath.DRAWING_PATH_POINT ) {
              if ( ! spoints || ! DrawingLevel.isLevelVisible( pt.mItem ) ) continue;
            } else if ( type == DrawingPath.DRAWING_PATH_LINE ) {
              if ( ! slines || ! DrawingLevel.isLevelVisible( pt.mItem ) ) continue;
            } else if ( type == DrawingPath.DRAWING_PATH_AREA ) {
              if ( ! sareas || ! DrawingLevel.isLevelVisible( pt.mItem ) ) continue;
            } else if ( ! DrawingPath.isDrawingType( type ) ) { // FIXME-HIDE should not happen
              continue;
            } 
            // else if ( type == DrawingPath.DRAWING_PATH_FIXED ) {
            //   TDLog.v("HIDE scrap display fixed 2");
            //   if ( ! legs_sshots ) continue;
            // } else if ( type == DrawingPath.DRAWING_PATH_NAME ) {
            //   TDLog.v("HIDE scrap display station name 2");
            //   if ( ! (sstations) ) continue;
            // // else if ( type == DrawingPath.DRAWING_PATH_SPLAY && ! (splays && sshots) )
            // } else if ( type == DrawingPath.DRAWING_PATH_SPLAY ) {
            //   TDLog.v("HIDE scrap display splay 2");
            //   // FIXME_LATEST latest splays
            //   if ( splays ) {
            //     if ( station_splay.isStationOFF( pt.mItem ) ) continue;
            //   } else {
            //     if ( ! station_splay.isStationON( pt.mItem ) ) continue;
            //   }
            // } 
            TDGreenDot.draw( canvas, matrix, pt, dot_radius );
          }
        }
      }
    }
  }

  /** draw the selection (???)
   * @param canvas   canvas
   * @param matrix   transform matrix
   * @param scale    used only to draw the "extend" control
   * @param is_extended ???
   * @note called under TDPath.mSelectionLock
   */
  void drawSelection( Canvas canvas, Matrix matrix, float zoom, float scale, boolean is_extended )
  {
    // PATH_SELECTION
    if ( isMultiselection ) {
      Path path = new Path();
      if ( mMultiselectionType == DrawingPath.DRAWING_PATH_POINT ) {
        float radius = 4*TDSetting.mDotRadius/zoom;
        for ( DrawingPath item : mMultiselected ) {
          float x = item.cx;
          float y = item.cy;
          path.addCircle( x, y, radius, Path.Direction.CCW );
        }
      } else { // if ( mMultiselectionType == DrawingPath.DRAWING_PATH_LINE || mMultiselectionType == DrawingPath.DRAWING_PATH_LINE ) 
        for ( DrawingPath item : mMultiselected ) {
          DrawingPointLinePath line = (DrawingPointLinePath) item;
          LinePoint lp = line.mFirst;
          path.moveTo( lp.x, lp.y );
          for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
            if ( lp.has_cp ) {
              path.cubicTo( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
            } else {
              path.lineTo( lp.x, lp.y );
            }
          }
        }
      }
      path.transform( matrix );
      canvas.drawPath( path, BrushManager.fixedYellowPaint );
      // end PATH_SELECTION
    } else if ( mSelected.mPoints.size() > 0 ) { // FIXME SELECTION
      float radius = 4*TDSetting.mDotRadius/zoom;
      Path path;
      SelectionPoint sp = mSelected.mHotItem;
      if ( sp != null ) {
        float x, y;
        LinePoint lp = sp.mPoint;
        DrawingPath item = sp.mItem;
        LinePoint lp1 = (sp.mRange == null)? null : sp.mRange.start();
        LinePoint lp2 = (sp.mRange == null)? null : sp.mRange.end();
    
        if ( lp != null ) { // line-point
          x = lp.x;
          y = lp.y;
        } else {
          x = item.cx;
          y = item.cy;
        }
        path = new Path();
        path.addCircle( x, y, radius, Path.Direction.CCW );
        path.transform( matrix );
        canvas.drawPath( path, BrushManager.highlightPaint2 );
        if ( lp != null && lp.has_cp ) {
          path = new Path();
          path.moveTo( lp.x1, lp.y1 );
          path.lineTo( lp.x2, lp.y2 );
          path.lineTo( x, y );
          path.addCircle( lp.x1, lp.y1, radius/2, Path.Direction.CCW );
          path.addCircle( lp.x2, lp.y2, radius/2, Path.Direction.CCW );
          path.transform( matrix );
          canvas.drawPath( path, BrushManager.highlightPaint3 );
        }
        if ( item.isLine() ) { // item instanceof DrawingLinePath
          Paint paint = BrushManager.fixedYellowPaint;
          DrawingLinePath line = (DrawingLinePath) item;
          lp = line.mFirst;
          if ( lp != null ) { // FIXME NULL_PTR ??? lp == null ? added test 2020-01-22
            LinePoint lpn = lp1;
            if ( lp == lp1 ) {
              paint = BrushManager.fixedOrangePaint;
              lpn = lp2;
            }
            path = new Path();
            path.moveTo( lp.x+line.mDx*10, lp.y+line.mDy*10 );  
            path.lineTo( lp.x, lp.y );
            for ( lp = lp.mNext; lp != lpn && lp != null; lp = lp.mNext ) {
              if ( lp.has_cp ) {
                path.cubicTo( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
              } else {
                path.lineTo( lp.x, lp.y );
              }
            }
            path.transform( matrix );
            canvas.drawPath( path, paint );
            if ( lp != null && lp != lp2 ) {
              path = new Path();
              path.moveTo( lp.x, lp.y );
              for ( lp = lp.mNext; lp != lp2 && lp != null; lp = lp.mNext ) {
                if ( lp.has_cp ) {
                  path.cubicTo( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
                } else {
                  path.lineTo( lp.x, lp.y );
                }
              }
              path.transform( matrix );
              canvas.drawPath( path, BrushManager.fixedOrangePaint );
            }
            if ( lp != null && lp.mNext != null ) {
              path = new Path();
              path.moveTo( lp.x, lp.y );
              for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
                if ( lp.has_cp ) {
                  path.cubicTo( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
                } else {
                  path.lineTo( lp.x, lp.y );
                }
              }
              path.transform( matrix );
              canvas.drawPath( path, BrushManager.fixedYellowPaint );
            }
          }
        } else if ( TDLevel.overExpert && is_extended && item.mType == DrawingPath.DRAWING_PATH_FIXED ) {
          path = new Path();
          float w = scale * TopoDroidApp.mDisplayWidth / 8; // TDSetting.mMinShift
          switch ( item.getBlockExtend() ) {
            case -1:
              path.moveTo( x, y ); 
              path.lineTo( x+w, y );
              break;
            case 1:
              path.moveTo( x-w, y ); 
              path.lineTo( x, y );
              break;
    	default:
              path.moveTo( x-w, y ); 
              path.lineTo( x+w, y );
          }
          path.transform( matrix );
          canvas.drawPath( path, BrushManager.fixedYellowPaint );
        }
      }
      radius = radius/3; // 2/zoom;
      for ( SelectionPoint pt : mSelected.mPoints ) {
        // float x, y;
        path = new Path();
        if ( pt.mPoint != null ) { // line-point
          path.addCircle( pt.mPoint.x, pt.mPoint.y, radius, Path.Direction.CCW );
        } else {
          path.addCircle( pt.mItem.cx, pt.mItem.cy, radius, Path.Direction.CCW );
        }
        path.transform( matrix );
        canvas.drawPath( path, BrushManager.highlightPaint );
      }
    }
  }

  // PATH MULTISELECT -----------------------------------------------
  // boolean isMultiselection() { return isMultiselection; }
  int getMultiselectionType() { return mMultiselectionType; }

  void resetMultiselection()
  {
    // TDLog.v( "reset Multi Selection" );
    mMultiselectionType  = -1;
    isMultiselection = false;
    synchronized( TDPath.mSelectionLock ) { mMultiselected.clear(); }
  }

  void startMultiselection()
  {
    // resetMultiselection();
    if ( isMultiselection ) return; // false;
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return; // false;
    DrawingPath path = sp.mItem;
    if ( path == null ) return; // false;
    int type = path.mType;
    if ( type < DrawingPath.DRAWING_PATH_POINT || type > DrawingPath.DRAWING_PATH_AREA ) return; // false;
    mMultiselectionType   = type;
    isMultiselection = true;
    addMultiselection( path );
    // TDLog.v( "start Multi Selection " + mMultiselectionType + " " + mMultiselected.size() );
    // return true;
  }

  private void addMultiselection( DrawingPath path )
  {
    if ( path.mType == mMultiselectionType ) {
      synchronized( TDPath.mSelectionLock ) { mMultiselected.add( path ); }
    }
    // TDLog.v( "add Multi Selection " + mMultiselectionType + " " + mMultiselected.size() );
  }

  void deleteMultiselection()
  {
    // if ( ! isMultiselection ) return;
    mMultiselectionType  = -1;
    isMultiselection = false;
    synchronized ( TDPath.mSelectionLock ) {
      for ( DrawingPath path : mMultiselected ) mSelection.removePath( path );
      synchronized( TDPath.mCommandsLock ) {
        for ( DrawingPath path : mMultiselected ) mCurrentStack.remove( path );
      }
      mMultiselected.clear();
    }
  }

  void decimateMultiselection()
  {
    // if ( ! isMultiselection ) return;
    synchronized ( TDPath.mSelectionLock ) {
      for ( DrawingPath path : mMultiselected ) mSelection.removePath( path );
      synchronized( TDPath.mCommandsLock ) {
        for ( DrawingPath path : mMultiselected ) {
          DrawingPointLinePath line = (DrawingPointLinePath)path;
          int min_size = ( path.isArea()? 3 : 2 );
          line.makeReduce( 1, min_size );
        }
      }
      for ( DrawingPath path : mMultiselected ) mSelection.insertPath( path );
    }
  }

  void joinMultiselection( float dmin )
  {
    // if ( ! isMultiselection ) return;
    synchronized ( TDPath.mSelectionLock ) {
      synchronized( TDPath.mCommandsLock ) {
	int k0 = mMultiselected.size();
        for ( int k1=0; k1<k0; ++k1 ) {
          DrawingPointLinePath l1 = (DrawingPointLinePath)( mMultiselected.get(k1) );
	  LinePoint lp0 = null;
	  LinePoint lp9 = null;
	  float d0 = dmin;
          float d9 = dmin;
          for ( int k2=k1+1; k2<k0; ++k2 ) {
            DrawingPointLinePath l2 = (DrawingPointLinePath)( mMultiselected.get(k2) );
            float d1 = l1.mFirst.distance( l2.mFirst ); // distance from first
            float d2 = l1.mFirst.distance( l2.mLast );  // distance from last
	    if ( d1 < d2 ) {
              if ( d1 < d0 ) { d0 = d1; lp0 = l2.mFirst; }
	    } else {
              if ( d2 < d0 ) { d0 = d2; lp0 = l2.mLast; }
            }
            d1 = l1.mLast.distance( l2.mFirst );
            d2 = l1.mLast.distance( l2.mLast );
	    if ( d1 < d2 ) {
              if ( d1 < d9 ) { d9 = d1; lp9 = l2.mFirst; }
	    } else {
              if ( d2 < d9 ) { d9 = d2; lp9 = l2.mLast; }
            }
	  }
	  boolean retrace = false;
	  if ( lp0 != null ) {
            l1.mFirst.shiftBy( lp0.x - l1.mFirst.x, lp0.y - l1.mFirst.y );
	    retrace = true;
	  }
	  if ( lp9 != null ) {
            l1.mLast.shiftBy( lp9.x - l1.mLast.x, lp9.y - l1.mLast.y );
	    retrace = true;
	  }
	  if ( retrace ) {
            l1.retracePath();
	  }
        }
      }
    }
  }
  // end PATH_MULTISELECT ACTIONS ------------------------------------------------

  static private boolean isInside( float x, float y, ArrayList< PointF > b )
  {
    int n = b.size();
    PointF p = b.get( n-1 );
    float x1 = x - p.x;
    float y1 = y - p.y;
    float z1 = x1*x1 + y1*y1;
    if ( z1 > 0 ) { z1 = (float)Math.sqrt(z1); x1 /= z1; y1 /= z1; }
    double angle = 0;
    for ( PointF q : b ) {
      float x2 = x - q.x;
      float y2 = y - q.y;
      float z2 = x2*x2 + y2*y2;
      if ( z2 > 0 ) { z2 = (float)Math.sqrt(z2); x2 /= z2; y2 /= z2; }
      angle += Math.asin( x2*y1 - y2*x1 );
      x1 = x2;
      y1 = y2;
    }
    return Math.abs( angle ) > 3.28; 
  }

  DrawingSpecialPath getDrawingSpecialPath( int type )
  {
    for ( ICanvasCommand cmd : mCurrentStack ) {
      if ( cmd instanceof DrawingSpecialPath ) {
        DrawingSpecialPath special = (DrawingSpecialPath)cmd;
        if ( special.isType( type ) ) return special;
      }
    }
    return null;
  }

  void shiftXSections( float x, float y )
  {
    for ( ICanvasCommand cmd : mCurrentStack ) {
      if ( cmd instanceof DrawingLinePath ) {
        DrawingLinePath line = (DrawingLinePath)cmd;
        if ( BrushManager.isLineSection( line.mLineType ) ) line.shiftBy( x, y );
      } else if ( cmd instanceof DrawingPointPath ) {
        DrawingPointPath point = (DrawingPointPath)cmd;
        if ( BrushManager.isPointSection( point.mPointType ) ) point.shiftBy( x, y );
      }
    }
  }

}
