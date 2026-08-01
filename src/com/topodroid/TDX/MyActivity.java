/* @file MyActivity.java
 *
 * @author marco corvi
 * @date oct 2014
 *
 * @brief TopoDroid 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;

import android.app.Activity;
import android.os.Bundle;

import android.window.OnBackInvokedDispatcher;
import android.window.OnBackInvokedCallback;

import android.view.KeyEvent;

import androidx.annotation.RequiresApi;

abstract public class MyActivity extends Activity
{
  protected Activity mActivity = null;

  // issua 169 - this should be implemente from API 33 onward
  @RequiresApi( 36 )
  private static class Api33
  {
    private static final java.util.Map< MyActivity, OnBackInvokedCallback > sCallbacks = new java.util.WeakHashMap<>();

    static void registerBack( MyActivity d )
    {
      TDLog.v("*** Item Drawer register BACK");
      OnBackInvokedCallback cb = d::onBackPressed;
      sCallbacks.put( d, cb );
      d.getOnBackInvokedDispatcher().registerOnBackInvokedCallback( OnBackInvokedDispatcher.PRIORITY_DEFAULT, cb );
    }

    static void unregisterBack( MyActivity d )
    {
      TDLog.v("*** Item Drawer unregister BACK");
      OnBackInvokedCallback cb = sCallbacks.remove( d );
      if ( cb != null ) {
        d.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback( cb );
      }
    }
  }

  @Override
  public void onBackPressed()
  {
    TDLog.v("*** my Activity BACK pressed");
    finish();
  }

// issue 170
  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    registerBackCallback();
  }

  @Override protected void onDestroy() 
  {
    unregisterBackCallback();
    super.onDestroy();
  }

  private void registerBackCallback()
  {
    if ( android.os.Build.VERSION.SDK_INT >= 33 ) {
      Api33.registerBack( this );
    }
  }
  
  private void unregisterBackCallback()
  {
    if ( android.os.Build.VERSION.SDK_INT >= 33 ) {
      Api33.unregisterBack( this );
    }
  }

  /** handle BACK key up event // alternative-169
   * @param code  key code
   * @param ev    key event
   */
  protected boolean backKeyUp( int code, KeyEvent event )
  {
    if ( TDandroid.BELOW_API_36 && code == KeyEvent.KEYCODE_BACK ) {
      if ( event.isTracking() && ! event.isCanceled() ) {
        return true;
      }
    }
    return false;
  }

  /** handle BACK key down event // alternative-169
   * @param code  key code
   * @param ev    key event
   */
  protected boolean backKeyDown( int code, KeyEvent event )
  {
    if ( TDandroid.BELOW_API_36 && code == KeyEvent.KEYCODE_BACK ) {
      onBackPressed();
      event.startTracking();
      return true;
    }
    return false;
  }
  
  
}
