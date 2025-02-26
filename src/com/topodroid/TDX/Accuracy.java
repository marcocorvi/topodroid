/* @file Accuracy.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid accuracy reference for a device in a survey - means of G, M, dip
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * note: this could be made non-static and created as field of ShotWindow
 *       the other class using it, DrawingWindow, would get it from the mParent
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.Locale;

class Accuracy
{
  final static float THRS  = 10.0f; // acc/mag threshold
  private final static float BND_1 = 10.0f; // small acc/mag values cutoff function
  private final static float BND_2 =  8.5f;

  private float mAccelerationSum  = 0.0f; // weighted sum acceleration value
  private float mMagneticSum      = 0.0f; // weighted sum magnetic field value
  private float mDipSum           = 0.0f; // weighted sum magnetic dip [degrees]
  private float mDipMean          = 0.0f; // weighted mean magnetic dip [degrees]
  private float mCountAcc         = 0;
  private float mCountMag         = 0;
  private float mCountDip         = 0;

  private String mDevice; // address of the device 

  Accuracy( String device ) 
  { 
    mDevice = device;
    reset();
  }

  // /** @return the number of AMD data accumulated
  //  */
  // float getCount() { return mCountDip; }

  // float getMeanAcc() { return (mCountAcc > 0) ? mAccelerationSum / mCountAcc : 0 ; }

  // float getMeanMag() { return (mCountMag > 0) ? mMagneticSum / mCountMag : 0 ; }

  // float getMeanDip() { return mDipMean; }

  /** @return true if a shot is above A/M/D threshold
   * @param blk    shot data (guaranteed not null)
   */
  boolean isBlockAMDBad( DBlock blk )
  {
    if ( blk.mAcceleration < THRS || blk.mMagnetic < THRS ) return false; // block without G,M,Dip
    return deltaMag( blk.mMagnetic )     > TDSetting.mMagneticThr
        || deltaAcc( blk.mAcceleration ) > TDSetting.mAccelerationThr
        || deltaDip( blk.mDip )          > TDSetting.mDipThr;
  }
  
  /** @return the string with how much the data differ from the means
   * @param blk  shot data (guatanteed not null)
   */
  String getBlockExtraString( DBlock blk )
  {
    return String.format(Locale.US, TDInstance.getResourceString( R.string.accuracy_amd ),
      deltaAcc( blk.mAcceleration ), 
      deltaMag( blk.mMagnetic ), 
      deltaDip( blk.mDip ) * TDSetting.mUnitAngle
    );
  }

  /** add a block to the existing means
   * @param blk  shot data (guaranteed not null)
   */
  void addBlockAMD( DBlock blk ) 
  {
    if ( mCountAcc > 0 ) {
      addBlockAcc( blk.mAcceleration, mAccelerationSum / mCountAcc );
    } else {
      mAccelerationSum = blk.mAcceleration;
      mCountAcc = 1;
    }
    if ( mCountMag > 0 ) {
      addBlockMag( blk.mMagnetic, mMagneticSum / mCountMag );
    } else {
      mMagneticSum = blk.mMagnetic;
      mCountMag = 1;
    }
    addBlockDip( blk.mDip );
    mDipMean = mDipSum / mCountDip;
  }

  // -----------------------------------------------------------------------------

  private void addBlockAcc( float acc, float mean ) 
  {
    float r = BND_1 * acc / mean - BND_2;
    if ( r >= 1.0f ) {
      mAccelerationSum += acc;
      mCountAcc += 1.0f;
    } else if ( r > 0.0f ) {
      mAccelerationSum += acc * r;
      mCountAcc += r;
    }
  }

  private void addBlockMag( float mag, float mean ) 
  {
    float r = BND_1 * mag / mean - BND_2;
    if ( r >= 1.0f ) {
      mMagneticSum += mag;
      mCountMag += 1.0f;
    } else if ( r > 0.0f ) {
      mMagneticSum += mag * r;
      mCountMag += r;
    }
  }

  private void addBlockDip( float dip ) 
  {
    mDipSum   += dip;
    mCountDip += 1.0f;
  }

  /** @return weighted average of differences of per-device value minus mean 
   * @param n   number of elements in G[]
   * @param G   values (size( nr)
   * @param GG  means (per device)
   * @param idx device index for each value (size nr)
   */
  static float computeWeightedAverage( int nr, float[] G, float[] GG, int[] idx )
  {
    float count = 0;
    float mean  = 0;
    for ( int j = 0; j < nr; ++ j ) {
      int k = idx[j];
      if ( k >= 0 && G[j] > THRS ) {
        float a = G[j];
        float m = GG[k];
        float r = BND_1 * a / m - BND_2;
        if ( r >= 1.0f ) {
          mean += (a - m);
          count += 1.0f;
        } else if ( r > 0 ) {
          mean += (a - m) * r;
          count += r;
        }
      }
    }
    if ( count > 0.0f ) {
      mean /= count;
    }
    return mean;
  }

  // /** set the means with a list of shot data
  //  * @param blks   list of shot data
  //  */
  // private void setBlocks( List< DBlock > blks ) 
  // {
  //   if ( TDUtil.isEmpty(blks) ) return;
  //   float ma = 0;
  //   float mm = 0;
  //   int   nr = 0;
  //   for ( DBlock blk : blks ) {
  //     if ( blk.mAcceleration > THRS ) { 
  //       ma += blk.mAcceleration;
  //       mm += blk.mMagnetic;
  //       ++ nr;
  //     }
  //   }
  //   if ( nr == 0 ) return;
  //   ma /= nr;
  //   mm /= nr;
  //   for ( DBlock blk : blks ) {
  //     if ( blk.mAcceleration > THRS ) { 
  //       addBlockAcc( blk.mAcceleration, ma );
  //       addBlockMag( blk.mMagnetic,     mm );
  //       addBlockDip( blk.mDip );
  //     }
  //   }
  //   if ( mCountDip > 0 ) mDipMean = mDipSum / mCountDip;
  //   ma = mAccelerationSum / mCountAcc;
  //   mm = mMagneticSum / mCountMag;
  //   // TDLog.v("ACCURACY count " + mCountDip + " means " + ma + " / " + mCountAcc + " " + mm + " / " + mCountMag + " " + mDipMean );
  // } 

  // /** compute the means with the shot data in a survey statistics
  //  * @param stat   survey statistics
  //  * @param n      number of data in stat vectors
  //  * @return the number of items that could be used in the averages
  //  * @note used by DataHelper
  //  */
  // static public int countBlocks( SurveyStat stat, int n  ) 
  // {
  //   Accuracy accu = new Accuracy( null );
  //   return accu.doCountBlocks( stat, n );
  // }

  // private int doCountBlocks( SurveyStat stat, int n  ) 
  // {
  //   float ma = 0;
  //   float mm = 0;
  //   int   nr = 0;
  //   for ( int k = 0; k < n; ++k ) {
  //     if ( stat.G[k] > THRS ) { 
  //       ma += stat.G[k];
  //       mm += stat.M[k];
  //       ++ nr;
  //     }
  //   }
  //   if ( nr > 0 ) {
  //     ma /= nr;
  //     mm /= nr;
  //     for ( int k = 0; k < n; ++k ) {
  //       if ( stat.G[k] > THRS ) { 
  //         addBlockAcc( stat.G[k], ma );
  //         addBlockMag( stat.M[k], mm );
  //         addBlockDip( stat.D[k] );
  //       }
  //     }
  //     if ( mCountDip > 0 ) stat.averageD = mDipSum / mCountDip;
  //     stat.averageG = mAccelerationSum / mCountAcc;
  //     stat.averageM = mMagneticSum / mCountMag;
  //   }
  //   return nr;
  // } 

  /** reset counters
   */
  private void reset()
  {
    mAccelerationSum  = 0.0f;
    mMagneticSum      = 0.0f;
    mDipSum           = 0.0f;
    mDipMean          = 0.0f;
    mCountAcc = 0;
    mCountMag = 0;
    mCountDip = 0;
  }

  /** @return percent difference from the mean acceleration
   * @param acc   testing acceleration
   */
  private float deltaAcc( float acc )
  {
    return ( mAccelerationSum > 0 )? TDMath.abs( 100*( acc * mCountAcc - mAccelerationSum)/mAccelerationSum ) : 0;
  }

  /** @return percent difference from the mean magnetic field
   * @param mag   testing magnetic field
   */
  private float deltaMag( float mag )
  {
    return ( mMagneticSum > 0 )? TDMath.abs( 100*( mag * mCountMag - mMagneticSum)/mMagneticSum ) : 0;
  }

  /** @return absolute difference from the mean magnetic dip [degrees]
   * @param dip   testing magnetic dip [degrees]
   */
  private float deltaDip( float dip ) { return TDMath.abs( dip - mDipSum/mCountDip ); }

}
