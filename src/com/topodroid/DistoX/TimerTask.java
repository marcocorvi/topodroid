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
package com.topodroid.DistoX;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.AsyncTask;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

// import android.widget.Toast;

// import android.util.Log;

class TimerTask extends AsyncTask<String, Integer, Long >
                implements SensorEventListener
{
  final static int X_AXIS = 1; // short side of phone heading right
  final static int Y_AXIS = 2; // long side of phone heading top
  final static int Z_AXIS = 3; // coming out of the screen

  private int mCntAcc;
  private int mCntMag;
  private float mValAcc[] = new float[3];
  private float mValMag[] = new float[3];
  private SensorManager mSensorManager;
  private WeakReference<IBearingAndClino> mParent;
  boolean mRun;
  private int mAxis;
  private int mWait;  // secs to wait
  private int mCount; // measures to count

  TimerTask( IBearingAndClino parent, int axis, int wait, int count )
  {
    mParent  = new WeakReference<IBearingAndClino>( parent );
    mRun     = true;
    mAxis    = axis;
    mWait    = wait;
    mCount   = count;
    mSensorManager = (SensorManager)TDInstance.context.getSystemService( Context.SENSOR_SERVICE );
    // Log.v("DistoX", "timer task axis " + axis );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    // Log.v("DistoX", "timer task in bkgr");
    int duration = 100; // ms
    ToneGenerator toneG = new ToneGenerator( AudioManager.STREAM_ALARM, TDSetting.mBeepVolume );
    long ret = 0;
    mCntAcc = 0;
    mCntMag = 0;
    for ( int i=0; i<mWait && mRun; ++i ) {
      toneG.startTone( ToneGenerator.TONE_PROP_BEEP, duration ); 
      TopoDroidUtil.slowDown( 1000 - duration );
      if ( isCancelled() ) {
        mRun = false;
        break;
      }
    }
    if ( mRun ) {
      int cnt = 3*mCount;
      mValAcc[0] = 0; mValAcc[1] = 0; mValAcc[2] = 0;
      mValMag[0] = 0; mValMag[1] = 0; mValMag[2] = 0;
      if ( mSensorManager != null ) {
        Sensor mAcc = mSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        Sensor mMag = mSensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
        if ( mAcc != null && mMag != null ) {
          mSensorManager.registerListener( this, mAcc, SensorManager.SENSOR_DELAY_NORMAL );
          mSensorManager.registerListener( this, mMag, SensorManager.SENSOR_DELAY_NORMAL );
          while ( cnt > 0 && ( mCntAcc < mCount || mCntMag < mCount ) ) {
            toneG.startTone( ToneGenerator.TONE_PROP_BEEP, duration ); 
            -- cnt;
            TopoDroidUtil.slowDown( 100 );
          }    
          mSensorManager.unregisterListener( this );
        }
      // } else {
      //   // FAILED
      }
    }
    // Log.v("DistoX", "timer task bkgr done");
    return ret;
  }

  @Override
  protected void onProgressUpdate(Integer... progress) 
  {
  }

  @Override
  protected void onPostExecute(Long result) 
  {
    // Log.v("DistoX", "timer task post exec. Acc " + mCntAcc + " Mag " + mCntMag );
    if ( mCntAcc > 0 && mCntMag > 0 && mRun ) {
      mValAcc[0] /= mCntAcc;
      mValAcc[1] /= mCntAcc;
      mValAcc[2] /= mCntAcc;
      mValMag[0] /= mCntMag;
      mValMag[1] /= mCntMag;
      mValMag[2] /= mCntMag;
      computeBearingAndClino();
    // } else {
    //   TDToast.make(R.string.insufficient_data );
    }
  }

  @Override
  public void onAccuracyChanged( Sensor sensor, int accuracy ) { }

  @Override
  public void onSensorChanged( SensorEvent event )
  {
    float[] value = event.values;
    switch ( event.sensor.getType() ) {
      case Sensor.TYPE_MAGNETIC_FIELD:
        ++ mCntMag;
        mValMag[0] += value[0];
        mValMag[1] += value[1];
        mValMag[2] += value[2];
        break;
      case Sensor.TYPE_ACCELEROMETER:
        ++ mCntAcc;
        mValAcc[0] += value[0];
        mValAcc[1] += value[1];
        mValAcc[2] += value[2];
        break;
    }
  }

  private void computeBearingAndClino( )
  {
    // Log.v("DistoX", "Timer Task compute B & C ");
    Vector g = new Vector( mValAcc[0], mValAcc[1], mValAcc[2] );
    Vector m = new Vector( mValMag[0], mValMag[1], mValMag[2] );
    g.normalize();

    int o0 = 0;

    m.normalize();
    // Vector e = new Vector( 1.0f, 0.0f, 0.0f );
    Vector w = m.cross( g ); // west
    Vector n = g.cross( w ); // north
    w.normalize();
    n.normalize();
    float b0 = 0;
    float c0 = 0;
    switch ( mAxis ) {
      case X_AXIS:
      case -X_AXIS:
        b0 = TDMath.atan2( -w.x, n.x );
        c0 = - TDMath.atan2( g.x, TDMath.sqrt(w.x*w.x+n.x*n.x) );
        o0 = ((int)(TDMath.atan2d( -g.y, g.z ) + 360)) % 360;
        break;
      case Y_AXIS:
      case -Y_AXIS:
        b0 = TDMath.atan2( -w.y, n.y );
        c0 = - TDMath.atan2( g.y, TDMath.sqrt(w.y*w.y+n.y*n.y) );
        o0 = ((int)(TDMath.atan2d( -g.z, g.x ) + 360)) % 360;
        break;
      case Z_AXIS:
      case -Z_AXIS:
        b0 = TDMath.atan2( -w.z, n.z );
        c0 = - TDMath.atan2( g.z, TDMath.sqrt(w.z*w.z+n.z*n.z) );
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
    c0 = 0 - c0 * 360.0f / TDMath.M_2PI;
    if ( mParent.get() != null ) mParent.get().setBearingAndClino( b0, c0, o0 );
  }

}
