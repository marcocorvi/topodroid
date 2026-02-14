/* @file Timer.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid simple timer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

public class Timer
{
  private Thread mThread;

  /** construct the timer and start the countdown; when it expires execurte the runnable
   * @param msec    countdown [msec]
   * @param runnable function-clsss to run at the end
   * @note the runnabel is not executed if the countdiwn is interrupted
   */
  public Timer( final int msec, Runnable runnable )
  {
    mThread = new Thread() {
      @Override public void run() {
        try {
          sleep( msec );
          runnable.run();
        } catch ( InterruptedException e ) { }
      }
    };
    mThread.start();
  }

  /** cancel the timer - interrupt the countdown
   */
  public void cancel()
  {
    mThread.interrupt();
  }
}
