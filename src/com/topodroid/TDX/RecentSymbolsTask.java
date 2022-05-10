/* @file RecentSymbolsTask.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid save recet symbols to db
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.content.Context;

class RecentSymbolsTask extends AsyncTask<Void, Integer, Boolean>
{
  private DataHelper mData;
  private ItemDrawer mDrawer;
  private int mWhat;
  static final int LOAD = 0; // actions
  static final int SAVE = 1;

  /** cstr
   * @param context    context (unused)
   * @param drawer     sketch/drawing window
   * @param data       database helper class
   * @param what       task action, either LOAD or SAVE
   */
  RecentSymbolsTask( Context context, ItemDrawer drawer, DataHelper data, int what )
  {
    mDrawer  = drawer;
    mData    = data;
    mWhat    = what;
  }

  /** execute the task
   * @return true 
   */
  @Override
  protected Boolean doInBackground(Void... v)
  {
    if ( mWhat == LOAD ) {
      loadRecentSymbols();
    } else if ( mWhat == SAVE ) {
      saveRecentSymbols();
    } else {
      return false;
    }
    return true;
  }

  /** save the recent symbol tools
   */
  private void saveRecentSymbols()
  {
    // TDLog.v( "save recent tools");
    boolean first = false;
    if ( ItemDrawer.mRecentPoint[0] != null ) {
      StringBuilder points = new StringBuilder( );
      // first = false;
      for ( int k=ItemDrawer.NR_RECENT-1; k>=0; --k ) {
        Symbol symbol = ItemDrawer.mRecentPoint[k];
        if ( symbol != null ) {
          if ( first ) {
            points.append(" ").append( symbol.getThName());
          } else {
            first = true;
            points.append( symbol.getThName() );
          }
        }
      }
      mData.setValue( "recent_points", points.toString() );
    }

    if ( ItemDrawer.mRecentLine[0] != null ) {
      StringBuilder lines = new StringBuilder( );
      first = false;
      for ( int k=ItemDrawer.NR_RECENT-1; k>=0; --k ) {
        Symbol symbol = ItemDrawer.mRecentLine[k];
        if ( symbol != null ) {
          if ( first ) {
            lines.append(" ").append(symbol.getThName());
          } else {
            first = true;
            lines.append( symbol.getThName() );
          }
        }
      }
      mData.setValue( "recent_lines", lines.toString() );
    }

    if ( ItemDrawer.mRecentArea[0] != null ) { 
      StringBuilder areas = new StringBuilder( );
      first = false;
      for ( int k=ItemDrawer.NR_RECENT-1; k>=0; --k ) {
        Symbol symbol = ItemDrawer.mRecentArea[k];
        if ( symbol != null ) {
          if ( first ) {
            areas.append(" ").append(symbol.getThName());
          } else {
            first = true;
            areas.append( symbol.getThName() );
          }
        }
      }
      mData.setValue( "recent_areas", areas.toString() );
    }
  }

  /** load the recent symbol tools
   * @note skip "section" point
   */
  private void loadRecentSymbols()
  {
    // TDLog.v( "load recent tools");
    BrushManager.setRecentPoints( ItemDrawer.mRecentPoint );
    BrushManager.setRecentLines(  ItemDrawer.mRecentLine );
    BrushManager.setRecentAreas(  ItemDrawer.mRecentArea );

    String names = mData.getValue( "recent_points" );
    if ( names != null ) {
      String[] points = names.split(" ");
      for ( String point : points ) {
        if ( point.equals("section" ) ) continue;
        ItemDrawer.updateRecent( BrushManager.getPointByThName( point ), ItemDrawer.mRecentPoint, ItemDrawer.mRecentPointAge );
      }
    }
    names = mData.getValue( "recent_lines" );
    if ( names != null ) {
      String[] lines = names.split(" ");
      for ( String line : lines ) {
        ItemDrawer.updateRecent( BrushManager.getLineByThName( line ), ItemDrawer.mRecentLine, ItemDrawer.mRecentLineAge );
      }
    }
    names = mData.getValue( "recent_areas" );
    if ( names != null ) {
      String[] areas = names.split(" ");
      for ( String area : areas ) {
        ItemDrawer.updateRecent( BrushManager.getAreaByThName( area ), ItemDrawer.mRecentArea, ItemDrawer.mRecentAreaAge );
      }
    }
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  /** react to the execution outcome
   * @param result   result of the execution
   */
  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( mWhat == LOAD && mDrawer != null ) {
      mDrawer.onRecentSymbolsLoaded();
    }
  }
}
