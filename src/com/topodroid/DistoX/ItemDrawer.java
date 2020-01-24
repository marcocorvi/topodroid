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

import android.app.Activity;

// import android.util.Log;

abstract class ItemDrawer extends Activity
{
  static final int POINT_MAX = 32678;

  protected Activity mActivity = null;

  int mCurrentPoint;
  int mCurrentLine;
  int mCurrentArea;
  protected int mPointScale;
  protected int mLinePointStep = 1;

  protected int mSymbol = Symbol.LINE; // kind of symbol being drawn

  // -----------------------------------------------------------
  static Symbol[] mRecentPoint = { null, null, null, null, null, null };
  static Symbol[] mRecentLine  = { null, null, null, null, null, null };
  static Symbol[] mRecentArea  = { null, null, null, null, null, null };
  static final int NR_RECENT = 6; // max is 6
  static Symbol[] mRecentTools = mRecentLine;
  static float mRecentDimX;
  static float mRecentDimY;

  void setPointScale( int scale )
  {
    if ( scale >= DrawingPointPath.SCALE_XS && scale <= DrawingPointPath.SCALE_XL ) mPointScale = scale;
  }

  int getPointScale() { return mPointScale; }

  // --------------------------------------------------------------
  // MOST RECENT SYMBOLS
  // recent symbols are stored with their filenames
  //
  // update of the "recent" arrays is done either with symbol index, or with symbol itself
  // load and save is done using a string of symbol filenames (separated by space)

  static void updateRecentPoint( int point )
  {
    updateRecent( BrushManager.getPointByIndex( point ), mRecentPoint );
  }

  static void updateRecentLine( int line )
  {
    updateRecent( BrushManager.getLineByIndex( line ), mRecentLine );
  }

  static void updateRecentArea( int area )
  {
    updateRecent( BrushManager.getAreaByIndex( area ), mRecentArea );
  }

  static void updateRecentPoint( Symbol point ) { updateRecent( point, mRecentPoint ); }

  static void updateRecentLine( Symbol line ) { updateRecent( line, mRecentLine ); }

  static void updateRecentArea( Symbol area ) { updateRecent( area, mRecentArea ); }

  // used by RecentSymbolTask
  static void updateRecent( Symbol symbol, Symbol[] symbols )
  {
    if ( symbol == null ) return;
    for ( int k=0; k<NR_RECENT; ++k ) {
      if ( symbol == symbols[k] ) {
        for ( ; k > 0; --k ) symbols[k] = symbols[k-1];
        symbols[0] = symbol;
        break;
      }
    }
    if ( symbols[0] != symbol ) {
      for ( int k = NR_RECENT-1; k > 0; --k ) symbols[k] = symbols[k-1];
      symbols[0] = symbol;
    }
  }

  // recent symbols are stored with their th_names
  //
  protected void loadRecentSymbols( DataHelper data )
  {
    ( new RecentSymbolsTask( this, this, data, mRecentPoint, mRecentLine, mRecentArea, NR_RECENT, RecentSymbolsTask.LOAD ) ).execute();

  }

  protected void saveRecentSymbols( DataHelper data )
  {
    ( new RecentSymbolsTask( this, this, data, mRecentPoint, mRecentLine, mRecentArea, NR_RECENT, RecentSymbolsTask.SAVE ) ).execute();
  }

  // ----------------------------------------------------------------------
  // TOOL SELECTION

  public void areaSelected( int k, boolean update_recent ) 
  {
    mSymbol = Symbol.AREA;
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
      setBtnRecent();
    }
    mLinePointStep = TDSetting.mLineType;
  }

  public void lineSelected( int k, boolean update_recent ) 
  {
    mSymbol = Symbol.LINE;
    if ( k >= 0 && k < BrushManager.getLineLibSize() ) {
      mCurrentLine = k;
      if ( TDSetting.mWithLevels > 0 ) {
        if ( ! DrawingLevel.isVisible( BrushManager.getLineLevel( k ) ) ) {
          mCurrentLine = 0; // BrushManager.mLineLib.mLineUserIndex;
        }
      }
    }
    setTheTitle();
    if ( update_recent ) {
      updateRecentLine( mCurrentLine );
      setBtnRecent();
    }
    mLinePointStep = BrushManager.getLineStyleX( mCurrentLine );
    if ( mLinePointStep != POINT_MAX ) mLinePointStep *= TDSetting.mLineType;
  }

  public void pointSelected( int p, boolean update_recent )
  {
    mSymbol = Symbol.POINT;
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
      setBtnRecent();
    }
  }

  public void setBtnRecent() { }
  public void setTheTitle() { }

  public void setPoint( int k, boolean update_recent ) { }
  public void setLine( int k, boolean update_recent ) { }
  public void setArea( int k, boolean update_recent ) { }

  // notified that recet symbols are loaded
  public void onRecentSymbolsLoaded() { } 
}
