/** @file TimerTask.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid timer
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.os.AsyncTask;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class TimerTask extends AsyncTask<String, Integer, Long >
                       implements SensorEventListener
{
  int mCntAcc;
  int mCntMag;
  float mValAcc[] = new float[3];
  float mValMag[] = new float[3];
  Context mContext; 
  IBearingAndClino mParent;
  boolean mRun;

  TimerTask( Context context, IBearingAndClino parent )
  {
    mContext = context;
    mParent  = parent;
    mRun = true;
  }

  @Override
  protected Long doInBackground( String... str )
  {
    int count = TDSetting.mTimerCount;
    int duration = 100; // ms
    ToneGenerator toneG = new ToneGenerator( AudioManager.STREAM_ALARM, TDSetting.mBeepVolume );
    long ret = 0;
    mCntAcc = 0;
    mCntMag = 0;
    for ( int i=0; i<count && mRun; ++i ) {
      toneG.startTone( ToneGenerator.TONE_PROP_BEEP, duration ); 
      try {
        Thread.sleep( 1000 - duration );
      } catch ( InterruptedException e ) {
      }
      if ( isCancelled() ) {
        mRun = false;
        break;
      }
    }
    if ( mRun ) {
      int cnt = 0;
      mValAcc[0] = 0; mValAcc[1] = 0; mValAcc[2] = 0;
      mValMag[0] = 0; mValMag[1] = 0; mValMag[2] = 0;
      SensorManager sensor_manager = (SensorManager)mContext.getSystemService( Context.SENSOR_SERVICE );
      Sensor mAcc = sensor_manager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
      Sensor mMag = sensor_manager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
      if ( mAcc != null && mMag != null ) {
        sensor_manager.registerListener( this, mAcc, SensorManager.SENSOR_DELAY_NORMAL );
        sensor_manager.registerListener( this, mMag, SensorManager.SENSOR_DELAY_NORMAL );
        while ( cnt < 100 && ( mCntAcc < 10 || mCntMag < 10 ) ) {
          toneG.startTone( ToneGenerator.TONE_PROP_BEEP, duration ); 
          try{
            ++ cnt;
            Thread.sleep( 100 );
          } catch ( InterruptedException e ) {
          }
        }    
        sensor_manager.unregisterListener( this );
      }
    }
    return ret;
  }

  @Override
  protected void onProgressUpdate(Integer... progress) 
  {
  }

  @Override
  protected void onPostExecute(Long result) 
  {
    if ( mCntAcc > 0 && mCntMag > 0 && mRun ) {
      mValAcc[0] /= mCntAcc;
      mValAcc[1] /= mCntAcc;
      mValAcc[2] /= mCntAcc;
      mValMag[0] /= mCntMag;
      mValMag[1] /= mCntMag;
      mValMag[2] /= mCntMag;
      computeBearingAndClino();
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
    Vector g = new Vector( mValAcc[0], mValAcc[1], mValAcc[2] );
    Vector m = new Vector( mValMag[0], mValMag[1], mValMag[2] );
    g.normalize();
    m.normalize();
    // Vector e = new Vector( 1.0f, 0.0f, 0.0f );
    Vector w = m.cross( g ); // west
    Vector n = g.cross( w ); // north
    w.normalize();
    n.normalize();
    float b0 = TDMath.atan2( -w.y, n.y );
    float c0 = - TDMath.atan2( g.y, TDMath.sqrt(w.y*w.y+n.y*n.y) );
    if ( b0 < 0.0f ) b0 += TDMath.M_2PI;
    // if ( r0 < 0.0f ) r0 += TDMath.M_2PI;
    b0 = 360 - b0 * 360.0f / TDMath.M_2PI;
    c0 = 0 - c0 * 360.0f / TDMath.M_2PI;
    mParent.setBearingAndClino( b0, c0 );
  }

}
