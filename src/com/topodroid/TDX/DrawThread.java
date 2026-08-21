/* @file DrawThread.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid  surface (canvas) drawing thread
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.util.TDLog;
import com.topodroid.util.TDUtil;

import android.view.SurfaceHolder;


public class DrawThread extends  Thread
{
  private volatile SurfaceHolder mHolder;
  private volatile boolean mRunning;

  private final IDrawingSurface mParent;

  public DrawThread( IDrawingSurface parent, SurfaceHolder holder)
  {
    // TDLog.Log( TDLog.LOG_PLOT, "draw thread cstr");
    mParent = parent;
    mHolder = holder;
  }

  public void setHolder( SurfaceHolder holder )
  {
    // TDLog.Log( TDLog.LOG_PLOT, "draw thread set holder " + ( ( holder == null )? "null" : "non-null" ) );
    mHolder = holder;
  }

  public void setRunning( boolean run ) { mRunning = run; }

  public boolean isRunning() { return mRunning; }

  public void stopRunning() { mRunning = false; }

  @Override
  public void run() 
  {
    // TDLog.Log( TDLog.LOG_PLOT, "draw thread run");
    mRunning = true;
    while ( mRunning ) {
      if ( mHolder != null && mParent.isDrawing() ) {
        mParent.refresh( mHolder );
        TDUtil.yieldDown( 20 ); // NECESSARY
      } else {
        TDUtil.yieldDown( 40 );
      }
    }
    // TDLog.Log( TDLog.LOG_PLOT, "draw thread exit");
  }
}

