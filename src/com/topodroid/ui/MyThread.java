/* @file MyThread.java
 *
 * @author marco corvi
 * @date Sept 2015
 *
 * @brief TopoDroid thread
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import android.os.Looper;

public class MyThread 
{

  private Runnable mRunnable;

  /** cstr
   * @param runnable  runnable code - must implement public void run() 
   */
  public MyThread( Runnable runnable ) 
  {
    mRunnable = runnable;
  }

  /** execute the runnable code in background if this thread is the main thread (or forced)
   * @param background     if true force background execution
   */
  public void start( boolean background )
  {
    if ( background || isMainThread() ) {
      (new Thread( mRunnable )).start();
    } else {
      mRunnable.run();
    }
  }

  /** @return true if the current thread is the main (UI) thread
   */
  private static boolean isMainThread()
  {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

}

