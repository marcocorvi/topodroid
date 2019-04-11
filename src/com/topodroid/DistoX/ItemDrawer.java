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

public class ItemDrawer extends Activity
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
    // if ( BrushManager.mPointLib == null ) return;
    updateRecent( BrushManager.mPointLib.getSymbolByIndex( point ), mRecentPoint );
  }

  static void updateRecentLine( int line )
  {
    // if ( BrushManager.mLineLib == null ) return;
    updateRecent( BrushManager.mLineLib.getSymbolByIndex( line ), mRecentLine );
  }

  static void updateRecentArea( int area )
  {
    // if ( BrushManager.mAreaLib == null ) return;
    updateRecent( BrushManager.mAreaLib.getSymbolByIndex( area ), mRecentArea );
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
    ( new RecentSymbolsTask( this, data, mRecentPoint, mRecentLine, mRecentArea, NR_RECENT, RecentSymbolsTask.LOAD ) ).execute();

  }

  protected void saveRecentSymbols( DataHelper data )
  {
    ( new RecentSymbolsTask( this, data, mRecentPoint, mRecentLine, mRecentArea, NR_RECENT, RecentSymbolsTask.SAVE ) ).execute();
  }

  // ----------------------------------------------------------------------
  // SELECTION

    public void areaSelected( int k, boolean update_recent ) 
    {
      mSymbol = Symbol.AREA;
      if ( k >= 0 && k < BrushManager.mAreaLib.mSymbolNr ) {
        mCurrentArea = k;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentArea( mCurrentArea );
      }
      mLinePointStep = TDSetting.mLineType;
    }

    public void lineSelected( int k, boolean update_recent ) 
    {
      mSymbol = Symbol.LINE;
      if ( k >= 0 && k < BrushManager.mLineLib.mSymbolNr ) {
        mCurrentLine = k;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentLine( mCurrentLine );
      }
      mLinePointStep = BrushManager.mLineLib.getStyleX( mCurrentLine );
      if ( mLinePointStep != POINT_MAX ) mLinePointStep *= TDSetting.mLineType;
    }

    public void pointSelected( int p, boolean update_recent )
    {
      mSymbol = Symbol.POINT;
      if ( p >= 0 && p < BrushManager.mPointLib.mSymbolNr ) {
        mCurrentPoint = p;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentPoint( mCurrentPoint );
      }
    }

    public void setTheTitle() { }

}
