/* @file MainWindow.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid simple thread-based async-task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.util;

import com.topodroid.TDX.TDandroid;

import android.app.Activity;

import android.os.Looper;
import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** N.B. for the moment AsyncTask is used only by the TimerTask
 */
public class AsyncTask<T>
{
  protected boolean mIsCancelled = false;
  protected final Activity mActivity;

  public AsyncTask( Activity activity )
  {
    mActivity = activity;
  }

  // public void execute() 
  // {
  //   ExecutorService executor = Executors.newSingleThreadExecutor();
  //   Handler handler = new Handler( Looper.getMainLooper() );
  //   executor.execute( new Runnable() {
  //     @Override
  //     public void run() {
  //       T ret = doInBackground();
  //       handler.post( new Runnable() {
  //         @Override
  //         public void run() {
  //           onPostExecute( ret );
  //         }
  //       } );
  //     }
  //   } );
  // }

  public void execute() 
  { 
    mIsCancelled = false;
    (new Thread() {
      public void run() {
        final T ret = doInBackground();
		TDandroid.runOnMainThread( new Runnable() {
		  public void run() {
            onPostExecute( ret );
		  }
		} );
      };
    } ).start();
  } 
    
  public void cancel( boolean what ) 
  {
    mIsCancelled = true;
  }

  protected T doInBackground() { return null; }

  protected void onPostExecute( T t ) { }

}
