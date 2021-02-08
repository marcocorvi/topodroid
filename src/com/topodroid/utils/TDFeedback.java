/* @file TDFeedback.java
 *
 * @author marco corvi
 * @date feb 2021 ( estracted from TopoDroidApp )
 *
 * @brief TopoDroid LED & vibration feedbacks
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.prefs.TDSetting;

import android.content.Context;
import android.os.Vibrator;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class TDFeedback
{
  static boolean mFeedbackOn = false;

  // Led notifcation are shown only while the display is off
  // static final int NOTIFY_LED_ID = 10101;
  //   NotificationManager manager =
  //     (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE );
  //   Notification notify_led = new Notification( ); // crash
  //   notify_led.ledARGB = Color
  //   notify_led.ledOffMS = 800;
  //   notify_led.ledOnMS  = 200;
  //   notify_led.flags = notify_led.flags | Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;
  //   manager.notify( NOTIFY_LED_ID, notify_led );
  //   manager.cancel( NOTIFY_LED_ID );
  //
  // an alternative is a vibration (but too frequent vibrations are
  // considered a bad idea)
  // manifest must have
  //   <uses-permission android:name="android.permission.VIBRATE" />
  // next
  //   Vibrator vibrator = (Vibrator)getSystemService( Context.VIBRATOR_SERVICE );
  //   vibrator.vibrate( 500 );
  // or
  //   long[] pattern = {400, 200};
  //   vibrator.vibrate( pattern, 0 ); // 0: repeat fom index 0, use -1 not to repeat
  //   vibrator.cancel();

  public static void reset()
  {
    mFeedbackOn = false;
  }

  // called only by ShotWindow
  public static void notifyFeedback( Context ctx, boolean on_off ) 
  {
    if ( TDSetting.mConnectFeedback == TDSetting.FEEDBACK_NONE ) return;

    if ( mFeedbackOn ) {
      if ( ! on_off ) { // turn off led
        mFeedbackOn = false;
      }
    } else {
      if ( on_off ) { // turn on led
        mFeedbackOn = true;
        if ( TDSetting.mConnectFeedback == TDSetting.FEEDBACK_BELL ) {
          ringTheBell( 200 );
        } else if ( TDSetting.mConnectFeedback == TDSetting.FEEDBACK_VIBRATE ) {
          vibrate( ctx, 200 );
        }
      }
    }
  }

  // LEG FEEDBACK ---------------------------------------------

  private static final int TRIPLE_SHOT_BELL_TIME = 200; // ms
  private static final int TRIPLE_SHOT_VIBRATE_TIME = 200; // ms

  public static  void legFeedback( Context ctx ) 
  {
    if ( TDSetting.mTripleShot == 1 ) {
      TDFeedback.ringTheBell( TRIPLE_SHOT_BELL_TIME );
    } else if ( TDSetting.mTripleShot == 2 ) {
      TDFeedback.vibrate( ctx, TRIPLE_SHOT_VIBRATE_TIME );
    }
  }

  // HEPTIC FEEDBACK ------------------------------------------

  private static void ringTheBell( int duration )
  {
    // Log.v("DistoXX", "bell ...");
    // ToneGenerator toneG = new ToneGenerator( AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME );
    ToneGenerator toneG = new ToneGenerator( AudioManager.STREAM_ALARM, TDSetting.mBeepVolume );
    // for ( int i=0; i<2; ++i ) {
      toneG.startTone( ToneGenerator.TONE_PROP_PROMPT, duration ); 
      // TDUtil.slowDown( duration );
    // }
  }

  private static void vibrate( Context ctx, int duration )
  {
    Vibrator vibrator = (Vibrator)ctx.getSystemService( Context.VIBRATOR_SERVICE );
    try {
      vibrator.vibrate(duration);
    } catch ( NullPointerException e ) {
      // TODO
    }
  }

}
