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
import com.topodroid.util.TDRequest;

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
      // TDLog.v("*** Item Drawer register BACK");
      OnBackInvokedCallback cb = d::onBackPressed;
      sCallbacks.put( d, cb );
      d.getOnBackInvokedDispatcher().registerOnBackInvokedCallback( OnBackInvokedDispatcher.PRIORITY_DEFAULT, cb );
    }

    static void unregisterBack( MyActivity d )
    {
      // TDLog.v("*** Item Drawer unregister BACK");
      OnBackInvokedCallback cb = sCallbacks.remove( d );
      if ( cb != null ) {
        d.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback( cb );
      }
    }
  }

  @Override
  public void onBackPressed()
  {
    // TDLog.v("*** my Activity BACK pressed");
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

  /** a runtime-permission request waiting for the user's answer
   */
  private static class PendingRequest
  {
    Runnable action = null; // action to run if the permission is granted
    Runnable denied = null; // action to run if the permission is not granted (can be null)
  }

  private final PendingRequest mCameraRequest = new PendingRequest();
  private final PendingRequest mAudioRequest  = new PendingRequest();

  // --- CAMERA

  /** run an action that needs the CAMERA permission, requesting the permission first if necessary
   * @param action   action to run once the permission is granted
   */
  void runWithCameraPermission( Runnable action )
  {
    runWithCameraPermission( action, null );
  }

  /** run an action that needs the CAMERA permission, requesting the permission first if necessary
   * @param action     action to run once the permission is granted
   * @param on_denied  action to run if the permission is not granted (can be null)
   */
  protected void runWithCameraPermission( Runnable action, Runnable on_denied )
  {
    runWithPermission( TDandroid.hasCameraPermission( this ), android.Manifest.permission.CAMERA,
                       TDRequest.REQUEST_CAMERA_PERMISSION, mCameraRequest, action, on_denied );
  }

  // --- AUDIO

  /** run an action that needs the RECORD_AUDIO permission, requesting the permission first if necessary
   * @param action   action to run once the permission is granted
   */
  void runWithAudioPermission( Runnable action )
  {
    runWithAudioPermission( action, null );
  }

  /** run an action that needs the RECORD_AUDIO permission, requesting the permission first if necessary
   * @param action     action to run once the permission is granted
   * @param on_denied  action to run if the permission is not granted (can be null)
   */
  protected void runWithAudioPermission( Runnable action, Runnable on_denied )
  {
    runWithPermission( TDandroid.hasAudioPermission( this ), android.Manifest.permission.RECORD_AUDIO,
                       TDRequest.REQUEST_AUDIO_PERMISSION, mAudioRequest, action, on_denied );
  }

  // --- common

  /** run an action that needs a runtime permission, requesting the permission first if necessary
   * @param granted     whether the permission is already granted (checked live by the caller)
   * @param permission  permission name
   * @param req_code    request code
   * @param pending     where to keep the actions waiting for the answer
   * @param action      action to run once the permission is granted
   * @param on_denied   action to run if the permission is not granted (can be null)
   */
  private void runWithPermission( boolean granted, String permission, int req_code, PendingRequest pending,
                                  Runnable action, Runnable on_denied )
  {
    if ( granted ) {
      action.run();
    } else {
      pending.action = action;
      pending.denied = on_denied;
      requestPermissions( new String[] { permission }, req_code );
    }
  }

  /** handle the answer to a request of a single runtime permission
   * @param permission  permission name
   * @param pending     the actions waiting for the answer
   * @param results     grant results
   * @param res_setting message when the permission is permanently denied (the app settings are opened)
   * @param res_denied  message when the permission is denied
   */
  private void handlePermissionResult( String permission, PendingRequest pending, int[] results, int res_setting, int res_denied )
  {
    Runnable action = pending.action;
    Runnable denied = pending.denied;
    pending.action = null;
    pending.denied = null;

    if ( results.length > 0 && results[0] == android.content.pm.PackageManager.PERMISSION_GRANTED ) {
      if ( action != null ) action.run();
      return;
    }
    // not granted (an empty result means the request was interrupted: nothing to tell the user)
    if ( denied != null ) denied.run();
    if ( results.length > 0 ) {
      if ( ! shouldShowRequestPermissionRationale( permission ) ) {
        // permanently denied: the system will no longer show the prompt
        TDToast.makeWarn( res_setting );
        TDandroid.openAppSettings( this );
      } else {
        TDToast.makeWarn( res_denied );
      }
    }
  }

  /** handle the result of a runtime permission request
   * @param reqCode  request code
   * @param perms    requested permissions
   * @param results  grant results
   */
  @Override
  public void onRequestPermissionsResult( int reqCode, String[] perms, int[] results )
  {
    super.onRequestPermissionsResult( reqCode, perms, results );
    if ( reqCode == TDRequest.REQUEST_CAMERA_PERMISSION ) {
      handlePermissionResult( android.Manifest.permission.CAMERA, mCameraRequest, results,
                              R.string.camera_permission_setting, R.string.camera_no_permission );
    } else if ( reqCode == TDRequest.REQUEST_AUDIO_PERMISSION ) {
      handlePermissionResult( android.Manifest.permission.RECORD_AUDIO, mAudioRequest, results,
                              R.string.audio_permission_setting, R.string.audio_no_permission );
    }
  }
  
  
}
