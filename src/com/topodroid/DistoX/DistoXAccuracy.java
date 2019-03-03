/* @file DistoXAccuracy.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey shots management
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * note: this could be made non-static and created as field of ShotWindow
 *       the other class using it, DrawingWindow, would get it from the mParent
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.Locale;

class DistoXAccuracy
{
  private float mAccelerationMean = 0.0f;
  private float mMagneticMean     = 0.0f;
  private float mDipMean          = 0.0f;
  private int   mCount = 0;

  DistoXAccuracy()
  {
    reset();
  }

  DistoXAccuracy( List<DBlock> blks )
  {
    reset();
    setBlocks( blks );
  }

  private void reset()
  {
    mAccelerationMean = 0.0f;
    mMagneticMean     = 0.0f;
    mDipMean          = 0.0f;
    mCount = 0;
  }

  // reset the means with a list of blocks
  private void setBlocks( List<DBlock> blks ) 
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

  // add a block to the existing means
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

  private float deltaAcc( float acc )
  {
    if ( mAccelerationMean > 0 ) return TDMath.abs( 100*(acc - mAccelerationMean)/mAccelerationMean ); 
    return 0;
  }

  private float deltaMag( float mag )
  {
    if ( mMagneticMean > 0 ) return TDMath.abs( 100*(mag - mMagneticMean)/mMagneticMean );
    return 0;
  }

  private float deltaDip( float dip ) { return TDMath.abs( dip - mDipMean ); }

  boolean isBlockAMDBad( DBlock blk )
  {
    if ( blk == null || blk.mAcceleration < 10.0f || blk.mMagnetic < 10.0f ) return false;
    return deltaMag( blk.mMagnetic ) > TDSetting.mMagneticThr
        || deltaAcc( blk.mAcceleration ) > TDSetting.mAccelerationThr
        || deltaDip( blk.mDip ) > TDSetting.mDipThr;
  }
  
  String getBlockExtraString( DBlock blk )
  {
    if ( blk == null ) return TDString.EMPTY;
    return String.format(Locale.US, "A %.1f  M %.1f  D %.1f", 
      deltaAcc( blk.mAcceleration ), 
      deltaMag( blk.mMagnetic ), 
      deltaDip( blk.mDip ) * TDSetting.mUnitAngle
    );
  }


}
