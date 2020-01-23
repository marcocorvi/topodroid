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
package com.topodroid.DistoX;

// import android.util.Log;

// import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.content.Context;

class RecentSymbolsTask extends AsyncTask<Void, Integer, Boolean>
{
  private DataHelper mData;
  private ItemDrawer mDrawer;
  // private WeakReference<Context> mContext;
  private Symbol[] mRecentPoint;
  private Symbol[] mRecentLine;
  private Symbol[] mRecentArea;
  private int mNr;
  private int mWhat;
  static final int LOAD = 0;
  static final int SAVE = 1;

  RecentSymbolsTask( Context context, ItemDrawer drawer, DataHelper data, Symbol[] points, Symbol[] lines, Symbol[] areas, int nr, int what )
  {
    // mContext = new WeakReference<Context>( context );
    mDrawer  = drawer;
    mData    = data;
    mRecentPoint = points;
    mRecentLine  = lines;
    mRecentArea  = areas;
    mNr = nr;
    mWhat = what;
  }

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

  private void saveRecentSymbols()
  {
    // Log.v("DistoX", "save recent tools");
    boolean first = false;
    if ( mRecentPoint[0] != null ) {
      StringBuilder points = new StringBuilder( );
      // first = false;
      for ( int k=mNr-1; k>=0; --k ) {
        if ( mRecentPoint[k] != null ) {
          if ( first ) {
            points.append(" ").append(mRecentPoint[k].mThName);
          } else {
            first = true;
            points.append( mRecentPoint[k].mThName );
          }
        }
      }
      mData.setValue( "recent_points", points.toString() );
    }

    if ( mRecentLine[0] != null ) {
      StringBuilder lines = new StringBuilder( );
      first = false;
      for ( int k=mNr-1; k>=0; --k ) {
        if ( mRecentLine[k] != null ) {
          if ( first ) {
            lines.append(" ").append(mRecentLine[k].mThName);
          } else {
            first = true;
            lines.append( mRecentLine[k].mThName );
          }
        }
      }
      mData.setValue( "recent_lines", lines.toString() );
    }

    if ( mRecentArea[0] != null ) { 
      StringBuilder areas = new StringBuilder( );
      first = false;
      for ( int k=mNr-1; k>=0; --k ) {
        if ( mRecentArea[k] != null ) {
          if ( first ) {
            areas.append(" ").append(mRecentArea[k].mThName);
          } else {
            first = true;
            areas.append( mRecentArea[k].mThName );
          }
        }
      }
      mData.setValue( "recent_areas", areas.toString() );
    }
  }

  private void loadRecentSymbols()
  {
    // Log.v("DistoX", "load recent tools");
    BrushManager.setRecentPoints( mRecentPoint );
    BrushManager.setRecentLines( mRecentLine );
    BrushManager.setRecentAreas( mRecentArea );

    String names = mData.getValue( "recent_points" );
    if ( names != null ) {
      String[] points = names.split(" ");
      for ( String point : points ) {
        ItemDrawer.updateRecent( BrushManager.getPointByThName( point ), mRecentPoint );
      }
    }
    names = mData.getValue( "recent_lines" );
    if ( names != null ) {
      String[] lines = names.split(" ");
      for ( String line : lines ) {
        ItemDrawer.updateRecent( BrushManager.getLineByThName( line ), mRecentLine );
      }
    }
    names = mData.getValue( "recent_areas" );
    if ( names != null ) {
      String[] areas = names.split(" ");
      for ( String area : areas ) {
        ItemDrawer.updateRecent( BrushManager.getAreaByThName( area ), mRecentArea );
      }
    }
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( mWhat == LOAD && mDrawer != null ) {
      mDrawer.onRecentSymbolsLoaded();
    }
  }
}
