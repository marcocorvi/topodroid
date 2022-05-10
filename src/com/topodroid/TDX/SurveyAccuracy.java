/* @file SurveyAccuracy.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid accuracy reference for a survey - means of G, M, dip
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * note: this could be made non-static and created as field of ShotWindow
 *       the other class using it, DrawingWindow, would get it from the mParent
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDString;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.Locale;

class SurveyAccuracy
{
  private float mAccelerationMean = 0.0f; // mean acceleration value
  private float mMagneticMean     = 0.0f; // mean magnetic field value
  private float mDipMean          = 0.0f; // mean magnetic dip [degrees]
  private int   mCount = 0;               // nr. of contributions to the means

  /** default cstr
   */
  SurveyAccuracy()
  {
    reset(); // not necessary
  }

  /** cstr
   * @param blks   list of data, used to initialize the accuracy
   */
  SurveyAccuracy( List< DBlock > blks )
  {
    reset();
    setBlocks( blks );
  }

  /** @return true if a shot is above A/M/D threshold
   * @param blk    shot data
   */
  boolean isBlockAMDBad( DBlock blk )
  {
    if ( blk == null || blk.mAcceleration < 10.0f || blk.mMagnetic < 10.0f ) return false; // block witout G,M,Dip
    return deltaMag( blk.mMagnetic )     > TDSetting.mMagneticThr
        || deltaAcc( blk.mAcceleration ) > TDSetting.mAccelerationThr
        || deltaDip( blk.mDip )          > TDSetting.mDipThr;
  }
  
  /** @return the string with how much the data differ from the means
   * @param blk  shot data
   */
  String getBlockExtraString( DBlock blk )
  {
    if ( blk == null ) return TDString.EMPTY;
    return String.format(Locale.US, "A %.1f  M %.1f  D %.1f", 
      deltaAcc( blk.mAcceleration ), 
      deltaMag( blk.mMagnetic ), 
      deltaDip( blk.mDip ) * TDSetting.mUnitAngle
    );
  }


  /** add a block to the existing means
   * @param blk  shot data
   */
  void addBlockAMD( DBlock blk ) 
  {
    if ( blk == null || blk.mAcceleration < 10.0 ) return;
    mAccelerationMean = mAccelerationMean * mCount + blk.mAcceleration;
    mMagneticMean     = mMagneticMean * mCount     + blk.mMagnetic;
    mDipMean          = mDipMean * mCount          + blk.mDip;
    ++ mCount;
    if ( mCount > 1 ) {
      mAccelerationMean /= mCount;
      mMagneticMean     /= mCount;
      mDipMean          /= mCount;
    }
  }

  // remove a block to the existing means (not used)
  // void removeBlockAMD( DBlock blk ) 
  // {
  //   if ( blk == null || blk.mAcceleration < 10.0 ) return;
  //   mAccelerationMean = mAccelerationMean * mCount - blk.mAcceleration;
  //   mMagneticMean     = mMagneticMean * mCount     - blk.mMagnetic;
  //   mDipMean          = mDipMean * mCount          - blk.mDip;
  //   -- mCount;
  //   if ( mCount > 1 ) {
  //     mAccelerationMean /= mCount;
  //     mMagneticMean     /= mCount;
  //     mDipMean          /= mCount;
  //   }
  // }

  // -----------------------------------------------------------------------------

  /** reset the means with a list of shot data
   * @param blks   list of shot data
   */
  private void setBlocks( List< DBlock > blks ) 
  {
    if ( blks == null || blks.size() == 0 ) return;
    for ( DBlock blk : blks ) {
      if ( blk.mAcceleration > 10.0 ) { 
        mAccelerationMean += blk.mAcceleration;
        mMagneticMean     += blk.mMagnetic;
        mDipMean          += blk.mDip;
        ++ mCount;
      }
    }
    if ( mCount > 1 ) {
      mAccelerationMean /= mCount;
      mMagneticMean     /= mCount;
      mDipMean          /= mCount;
    }
  } 

  /** reset counters
   */
  private void reset()
  {
    mAccelerationMean = 0.0f;
    mMagneticMean     = 0.0f;
    mDipMean          = 0.0f;
    mCount = 0;
  }

  /** @return percent difference from the mean acceleration
   * @param acc   testing acceleration
   */
  private float deltaAcc( float acc )
  {
    return ( mAccelerationMean > 0 )? TDMath.abs( 100*(acc - mAccelerationMean)/mAccelerationMean ) : 0;
  }

  /** @return percent difference from the mean magnetic field
   * @param mag   testing magnetic field
   */
  private float deltaMag( float mag )
  {
    return ( mMagneticMean > 0 )? TDMath.abs( 100*(mag - mMagneticMean)/mMagneticMean ) : 0;
  }

  /** @return absolute difference from the mean magnetic dip [degrees]
   * @param mag   testing magnetic dip [degrees]
   */
  private float deltaDip( float dip ) { return TDMath.abs( dip - mDipMean ); }
}
