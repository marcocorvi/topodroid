/* @file ItemDrawer.java
 *
 * @author marco corvi
 * @date oct 2014
 *
 * @brief TopoDroid label adder interfare
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.SymbolType;
import com.topodroid.common.PointScale;

import android.app.Activity;

abstract class ItemDrawer extends Activity
{
  static final int POINT_MAX = 32678;

  protected Activity mActivity = null;

  int mCurrentPoint;
  int mCurrentLine;
  int mCurrentArea;
  protected int mPointScale;
  protected int mLinePointStep = 1;

  protected int mSymbol = SymbolType.LINE; // kind of symbol being drawn

  // -----------------------------------------------------------
  static final int NR_RECENT = 6; // max is 6
  static Symbol[] mRecentPoint = { null, null, null, null, null, null };
  static Symbol[] mRecentLine  = { null, null, null, null, null, null };
  static Symbol[] mRecentArea  = { null, null, null, null, null, null };
  static int[] mRecentPointAge = { 6, 5, 4, 3, 2, 1 };
  static int[] mRecentLineAge  = { 6, 5, 4, 3, 2, 1 };
  static int[] mRecentAreaAge  = { 6, 5, 4, 3, 2, 1 };
  static Symbol[] mRecentTools = mRecentLine;
  static float mRecentDimX;
  static float mRecentDimY;

  void setPointScale( int scale )
  {
    if ( scale >= PointScale.SCALE_XS && scale <= PointScale.SCALE_XL ) mPointScale = scale;
  }

  int getPointScale() { return mPointScale; }

  // --------------------------------------------------------------
  // MOST RECENT SYMBOLS
  // recent symbols are stored with their filenames
  //
  // update of the "recent" arrays is done either with symbol index, or with symbol itself
  // load and save is done using a string of symbol filenames (separated by space)

  /** update the array of recent points
   * @param point  index of the point in the point library
   */
  static void updateRecentPoint( int point )
  {
    updateRecent( BrushManager.getPointByIndex( point ), mRecentPoint, mRecentPointAge );
  }

  /** update the array of recent lines
   * @param line  index of the line in the line library
   */
  static void updateRecentLine( int line )
  {
    updateRecent( BrushManager.getLineByIndex( line ), mRecentLine, mRecentLineAge );
  }

  /** update the array of recent areas
   * @param area  index of the area in the area library
   */
  static void updateRecentArea( int area )
  {
    updateRecent( BrushManager.getAreaByIndex( area ), mRecentArea, mRecentAreaAge );
  }

  /** update the array of recent points
   * @param point  point symbol
   */
  static void updateRecentPoint( Symbol point ) { updateRecent( point, mRecentPoint, mRecentPointAge ); }

  /** update the array of recent lines
   * @param line  line symbol
   */
  static void updateRecentLine( Symbol line ) { updateRecent( line, mRecentLine, mRecentLineAge ); }

  /** update the array of recent areas
   * @param area  area symbol
   */
  static void updateRecentArea( Symbol area ) { updateRecent( area, mRecentArea, mRecentAreaAge ); }

  /** update a set of recent symbols
   * @param symbol    symbol to update
   * @param symbols   set of recent symbols
   * @param ages      set of recent symbols ages
   * @note used by RecentSymbolTask
   */
  static void updateRecent( Symbol symbol, Symbol[] symbols, int[] ages )
  {
    if ( symbol == null ) return;
    int kmin = 0;
    for ( int k=0; k<NR_RECENT; ++k ) {
      if ( symbol == symbols[k] ) {
        updateAge( k, ages );
        return;
      } else if ( ages[k] < ages[kmin] ) {
        kmin = k;
      }
    }
    symbols[kmin] = symbol;
    updateAge( kmin, ages );
  }

  /** update a recent symbol age
   * @param kk   index of the recent symbol
   * @param ages      set of recent symbols ages
   */
  static void updateAge( int kk, int[] ages )
  {
    // TDLog.v("AGE kk " + kk );
    int amax = ages[kk];
    for ( int k=0; k<NR_RECENT; ++k ) {
      if ( k != kk && ages[kk] < ages[k] ) { 
        if ( amax < ages[k] ) amax = ages[k];
        -- ages[k];
      }
    }
    ages[kk] = amax;
    // TDLog.v("AGE " + ages[0] + " " + ages[1] + " " + ages[2] + " " + ages[3] + " " + ages[4] + " " + ages[5] );
  }

  /** load the recent symbols sets from the database
   * @param db   database helper
   * @note recent symbols are stored with their th_names
   */
  protected void loadRecentSymbols( DataHelper db )
  {
    ( new RecentSymbolsTask( this, this, db, /* mRecentPoint, mRecentLine, mRecentArea, NR_RECENT, */ RecentSymbolsTask.LOAD ) ).execute();

  }

  /** save the recent symbols sets to the database
   * @param db   database helper
   */
  protected void saveRecentSymbols( DataHelper db )
  {
    ( new RecentSymbolsTask( this, this, db, /* mRecentPoint, mRecentLine, mRecentArea, NR_RECENT, */ RecentSymbolsTask.SAVE ) ).execute();
  }

  // ----------------------------------------------------------------------
  // TOOL SELECTION

  /** react to the selection of an area symbol
   * @param p      symbol index
   * @param update_recent whether to update the recemt symbols set
   */
  public void areaSelected( int k, boolean update_recent ) 
  {
    // TDLog.v("Item drawer point selected: " + k + " update " + update_recent );
    mSymbol = SymbolType.AREA;
    if ( k >= 0 && k < BrushManager.getAreaLibSize() ) {
      mCurrentArea = k;
      if ( TDSetting.mWithLevels > 0 ) {
        if ( ! DrawingLevel.isVisible( BrushManager.getAreaLevel( k ) ) ) {
          mCurrentArea = 0; // BrushManager.mAreaLib.mAreaUserIndex;
        }
      }
    }
    setTheTitle();
    if ( update_recent ) {
      updateRecentArea( mCurrentArea );
      setBtnRecent( SymbolType.AREA );
    }
    mLinePointStep = TDSetting.mLineType;
  }

  /** react to the selection of a line symbol
   * @param p      symbol index
   * @param update_recent whether to update the recemt symbols set
   */
  public void lineSelected( int k, boolean update_recent ) 
  {
    // TDLog.v("Item drawer line selected " + k + " update recent " + update_recent );
    mSymbol = SymbolType.LINE;
    if ( k >= 0 && k < BrushManager.getLineLibSize() ) {
      mCurrentLine = k;
      if ( TDSetting.mWithLevels > 0 ) {
        if ( ! DrawingLevel.isVisible( BrushManager.getLineLevel( k ) ) ) {
          // TDLog.v("Item drawer line selected " + k + " is not visible");
          mCurrentLine = 0; // BrushManager.mLineLib.mLineUserIndex;
        }
      }
    }
    setTheTitle();
    if ( update_recent ) {
      // TDLog.v("Item drawer update recent: current line " + mCurrentLine );
      updateRecentLine( mCurrentLine );
      setBtnRecent( SymbolType.LINE );
    }
    mLinePointStep = BrushManager.getLineStyleX( mCurrentLine );
    if ( mLinePointStep != POINT_MAX ) mLinePointStep *= TDSetting.mLineType;
  }

  /** react to the selection of a point symbol
   * @param p      symbol index
   * @param update_recent whether to update the recemt symbols set
   */
  public void pointSelected( int p, boolean update_recent )
  {
    mSymbol = SymbolType.POINT;
    // TDLog.v("Item drawer point selected: " + p + " update " + update_recent );
    if ( p >= 0 && p < BrushManager.getPointLibSize() ) {
      mCurrentPoint = p;
      if ( TDSetting.mWithLevels > 0 ) {
        if ( ! DrawingLevel.isVisible( BrushManager.getPointLevel( p ) ) ) {
          mCurrentPoint = 0; // BrushManager.mPointLib.mPointUserIndex;
        }
      }
    }
    setTheTitle();
    if ( update_recent ) {
      updateRecentPoint( mCurrentPoint );
      setBtnRecent( SymbolType.POINT );
    }
  }

  /** set the recent symbols button - empty, to be overridden
   * @param symbol   symbol index
   */
  public void setBtnRecent( int symbol ) { }

  /** set the window tithe - empty, to be overridden
   */
  public void setTheTitle() { }

  // public boolean setCurrentPoint( int k, boolean update_recent ) { }
  // public boolean setCurrentLine( int k, boolean update_recent ) { }
  // public boolean setCurrentArea( int k, boolean update_recent ) { }

  /** react to a notified that recent symbols are loaded
   */
  public void onRecentSymbolsLoaded() { } 

  /** @return whether the point of the given index is "section" and is forbidden
   * @param i   point index
   * @note overridden by DrawingWindow
   */
  public boolean forbidPointSection( int i ) { return false; }

  /** @return whether the line of the given index is "section" and is forbidden
   * @param j   line index
   * @note overridden by DrawingWindow
   */
  public boolean forbidLineSection( int j ) { return false; }

}
