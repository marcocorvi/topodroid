/** @file TDThread.java
 *
 * @author marco corvi
 * @date mar 2022
 *
 * @brief TopoDroid background thread helper class
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import android.os.Handler;
import android.os.HandlerThread;

/** usage
 * [1] create a background thread and get the HandlerThread
 * [2] get the background Handler
 * [3] post Runnable's to the Handler
 */
public class TDThread 
{
  /** get a background thread
   * @param name  thread name
   * @return the background thread
   */
  public static HandlerThread startThread( String name )
  {
    HandlerThread thread = new HandlerThread( name );
    thread.start();
    return thread;
  }

  /** get a background thread handler
   * @param thread   thread
   * @return handler
   */
  public static Handler getHandler( HandlerThread thread )
  {
    if ( thread == null ) return null;
    return new Handler( thread.getLooper() );
  }

  /** stop background thread
   * @param thread   thread
   * @return true if success, false if interrupted
   * @note thread (and its handler) cannot be used after this method - must be null-ed
   */
  public static boolean stopThread( HandlerThread thread )
  {
    if ( thread == null ) return true;
    thread.quitSafely();
    try {
      thread.join();
      return true;
    } catch ( InterruptedException e ) {
      e.printStackTrace();
      return false;
    }
  }

}
