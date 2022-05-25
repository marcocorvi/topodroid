/* @file TimerTask.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid timer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.AsyncTask;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

class TimerTask extends AsyncTask<String, Integer, Long >
                implements SensorEventListener
{
  final static int TYPE_MAG = Sensor.TYPE_MAGNETIC_FIELD;
  final static int TYPE_ACC = Sensor.TYPE_ACCELEROMETER; // when the device is at rest, the output of GRAVITY should be identical to that of ACCELEROMETER
  
  final static int X_AXIS = 1; // short side of phone heading right
  final static int Y_AXIS = 2; // long side of phone heading top
  final static int Z_AXIS = 3; // coming out of the screen

  private int mCntGrv;  // gravity counter
  private int mCntMag;  // magnetic counter
  private float[] mValGrv = new float[3];
  private float[] mValMag = new float[3];
  private SensorManager mSensorManager;
  private WeakReference<IBearingAndClino> mParent;
  boolean mRun;
  private int mAxis;
  private int mWait;  // secs to wait
  private int mCount; // measures to count
  private int mMagAccuracy;
  private int mAccAccuracy;
  private boolean mTakeReading = false; // whether to take readings or not

  /** cstr
   * @param parent   parent activity/dialog
   * @param axis     reference axis
   * @param wait     number of seconds to wait before taking measurements
   * @param count    number of readings to average
   */
  TimerTask( IBearingAndClino parent, int axis, int wait, int count )
  {
    mParent  = new WeakReference<IBearingAndClino>( parent );
    mRun     = true;
    mAxis    = axis;
    mWait    = wait;
    mCount   = count;
    mSensorManager = (SensorManager)TDInstance.context.getSystemService( Context.SENSOR_SERVICE );
    // TDLog.Log( TDLog.LOG_PHOTO, "Timer task axis " + axis );
    mMagAccuracy = 0;
    mAccAccuracy = 0;
  }

  /** task execution
   * @param str ...
   * @return 0 on success, neg. error (-1 no-run, -2 no sensor manager, -3 no sensors, -4 cancelled )
   */
  @Override
  protected Long doInBackground( String... str )
  {
    // TDLog.v( "timer task in bkgr");
    // TDLog.Log( TDLog.LOG_PHOTO, "Timer task in background - run " + mRun );
    int duration = 100; // ms
    ToneGenerator toneG = new ToneGenerator( AudioManager.STREAM_ALARM, TDSetting.mBeepVolume );
    mCntGrv = 0;
    mCntMag = 0;
    if ( ! mRun ) return -1L;
    if ( mSensorManager == null ) {
      TDLog.Error( "Timer task: no sensor manager" );
      return -2L;
    }
    Sensor mAcc = mSensorManager.getDefaultSensor( TYPE_ACC );
    Sensor mMag = mSensorManager.getDefaultSensor( TYPE_MAG );
    if ( mAcc == null || mMag == null ) {
      TDLog.Error( "Timer task: no sensors" );
      return -3L;
    }
    mTakeReading = false;
    mSensorManager.registerListener( this, mAcc, SensorManager.SENSOR_DELAY_NORMAL );
    mSensorManager.registerListener( this, mMag, SensorManager.SENSOR_DELAY_NORMAL );
    for ( int i=0; i<mWait && mRun; ++i ) {
      toneG.startTone( ToneGenerator.TONE_PROP_BEEP, duration ); 
      TDUtil.slowDown( 1000 - duration );
      if ( isCancelled() ) {
        TDLog.Error( "Timer task: cancelled" );
        mRun = false;
        mSensorManager.unregisterListener( this );
        return -4L;
      }
    }

    // TDLog.Log( TDLog.LOG_PHOTO, "Timer task ready - run " + mRun );
    int cnt = 3*mCount;
    mValGrv[0] = 0; mValGrv[1] = 0; mValGrv[2] = 0;
    mValMag[0] = 0; mValMag[1] = 0; mValMag[2] = 0;
    mTakeReading = true;
    while ( cnt > 0 && ( mCntGrv < mCount || mCntMag < mCount ) ) {
      toneG.startTone( ToneGenerator.TONE_PROP_BEEP, duration ); 
      -- cnt;
      TDUtil.slowDown( 100 );
    }
    mTakeReading = false;
    mSensorManager.unregisterListener( this );
    
    // TDLog.v( "timer task bkgr done");
    return 0L;
  }

  /** progress update (empty)
   */
  @Override
  protected void onProgressUpdate(Integer... progress) 
  {
  }

  /** post execution
   * @param result   execution result (unused)
   */
  @Override
  protected void onPostExecute(Long result) 
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "Timer task post exec. Acc " + mCntGrv + " Mag " + mCntMag + " run " + mRun );
    // TDLog.v( "Timer task post exec. " + result + " Acc " + mCntGrv + " Mag " + mCntMag );
    if ( result == 0 ) {
      if ( mCntGrv > 0 && mCntMag > 0 && mRun ) {
        mValGrv[0] /= mCntGrv;
        mValGrv[1] /= mCntGrv;
        mValGrv[2] /= mCntGrv;
        mValMag[0] /= mCntMag;
        mValMag[1] /= mCntMag;
        mValMag[2] /= mCntMag;
        computeBearingAndClino();
        toastAccuracy( mMagAccuracy );
        // TDLog.v( "Timer task. Acc. counts " + mCntGrv + " Mag. counts " + mCntMag );
      } else {
        mValGrv[0] = 0;
        mValGrv[1] = 0;
        mValGrv[2] = 0;
        mValMag[0] = 0;
        mValMag[1] = 0;
        mValMag[2] = 0;
        TDLog.Error( "Timer task null direction. Acc. counts " + mCntGrv + " Mag. counts " + mCntMag );
        TDToast.makeWarn( R.string.sensor_no_readings );
      }
    } else {
      TDToast.makeWarn( String.format( TDInstance.getResourceString( R.string.sensor_failure ), result.longValue() ) );
    }
  }

  /** react to a notification of a change in sensor accuracy
   * @param sensor   sensor type
   * @param accuracy sensor accuracy (0: unreliable, 1: low, 2: med, 3: high)
   * @note store the value of the accuracy in a local field
   */
  @Override
  public void onAccuracyChanged( Sensor sensor, int accuracy ) 
  {
    int type = sensor.getType();
    if ( type == TYPE_MAG ) {
      // if ( accuracy <= mMagAccuracy) return;
      mMagAccuracy = accuracy;
    } else if ( type == TYPE_ACC ) {
      // if ( accuracy <= mAccAccuracy) return;
      mAccAccuracy = accuracy;
    }
  }
 
  /** toast a warning if the accuracy is not good
   * @param accuracy   sensor accuracy
   */
  private void toastAccuracy( int accuracy )
  {
    if ( accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_HIGH ) {
      // TDToast.make( R.string.accuracy_high );
    } else if ( accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ) { 
      TDToast.makeWarn( R.string.accuracy_medium );
    } else if ( accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW ) { 
      TDToast.makeWarn( R.string.accuracy_low );
    } else {
      TDToast.makeWarn( R.string.accuracy_unreliable );
    }
  }

  /** react to a change in a sensor value
   * @param event   sensor event
   */
  @Override
  public void onSensorChanged( SensorEvent event )
  {
    if ( ! mTakeReading ) return;
    float[] value = event.values;
    switch ( event.sensor.getType() ) {
      case TYPE_MAG: // ambient magnetic field [uT]
        ++ mCntMag;
        mValMag[0] += value[0]; // X-axis of the device: rightward to the side
        mValMag[1] += value[1]; // Y-axis: forward from the top
        mValMag[2] += value[2]; // Z-axis: upward out of the screen
        break;
      case TYPE_ACC: // when the device is at rest, the output of GRAVITY should be identical to that of ACCELEROMETER
        ++ mCntGrv;
        mValGrv[0] += value[0]; // - Gx [m/s^2]
        mValGrv[1] += value[1]; // - Gy
        mValGrv[2] += value[2]; // - Gz
        break;
    }
  }

  /** compute azimuth and clino
   */
  private void computeBearingAndClino( )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "Timer task compute B & C" );
    // TDLog.v("Gravity " + mValGrv[0] + " " + mValGrv[1] + " " + mValGrv[2] + " Magnetic" + mValMag[0] + " " + mValMag[1] + " " + mValMag[2] );
    TDVector g = new TDVector( mValGrv[0], mValGrv[1], mValGrv[2] ); // -G: X-right, Y-forward, Z-upward
    TDVector m = new TDVector( mValMag[0], mValMag[1], mValMag[2] ); //  M:
    g.normalize();
    m.normalize();

    int o0 = 0;

    // TDVector e = new TDVector( 1.0f, 0.0f, 0.0f );
    TDVector w = m.cross( g ); // east // FIXME THIS
    TDVector n = g.cross( w ); // north
    w.normalize();
    n.normalize();
    float b0 = 0;
    float c0 = 0;
    switch ( mAxis ) {
      case X_AXIS:
      case -X_AXIS:
        b0 =   TDMath.atan2( -w.x, n.x );
        c0 = - TDMath.atan2( g.x, TDMath.sqrt( w.x*w.x + n.x*n.x ) );
        o0 = ((int)(TDMath.atan2d( -g.y, g.z ) + 360)) % 360;
        break;
      case Y_AXIS:
      case -Y_AXIS:
        b0 =   TDMath.atan2( -w.y, n.y );
        c0 = - TDMath.atan2( g.y, TDMath.sqrt( w.y*w.y + n.y*n.y ) );
        o0 = ((int)(TDMath.atan2d( -g.z, g.x ) + 360)) % 360;
        break;
      case Z_AXIS:
      case -Z_AXIS:
        b0 =   TDMath.atan2( -w.z, n.z );
        c0 = - TDMath.atan2( g.z, TDMath.sqrt( w.z*w.z + n.z*n.z ) );
        o0 = ((int)(TDMath.atan2d( -g.x, g.y ) + 360)) % 360;
        break;
    }
    if ( o0 < 0 ) o0 += 360;
    if ( mAxis < 0 ) { // opposite of standard android axes
      b0 += TDMath.M_PI;
      c0 = -c0;
    }
    if ( b0 < 0.0f ) b0 += TDMath.M_2PI;
    // if ( r0 < 0.0f ) r0 += TDMath.M_2PI;
    b0 = 360 - b0 * 360.0f / TDMath.M_2PI;
    c0 =   0 - c0 * 360.0f / TDMath.M_2PI;
    if ( mParent.get() != null ) mParent.get().setBearingAndClino( b0, c0, o0, mMagAccuracy, 0 ); // 0 timer does not know camera API
  }

}
