/* @file DistoXAccuracy.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey shots management
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * note: this could be made non-static and created as field of ShotWindow
 *       the other class using it, DrawingWindow, would get it from the mParent
 */
package com.topodroid.DistoX;

import java.util.List;

class DistoXAccuracy
{
  static private float mAccelerationMean = 0.0f;
  static private float mMagneticMean     = 0.0f;
  static private float mDipMean          = 0.0f;
  static private int   mCount = 0;

  static void reset()
  {
    mAccelerationMean = 0.0f;
    mMagneticMean     = 0.0f;
    mDipMean          = 0.0f;
    mCount = 0;
  }

  // reset the means with a list of blocks
  static void addBlocks( List<DBlock> blks ) 
  {
    reset();
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
  static void addBlock( DBlock blk ) 
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

  static float deltaAcc( float acc )
  {
    if ( mAccelerationMean > 0 ) return TDMath.abs( 100*(acc - mAccelerationMean)/mAccelerationMean ); 
    return 0;
  }

  static float deltaMag( float mag )
  {
    if ( mMagneticMean > 0 ) return TDMath.abs( 100*(mag - mMagneticMean)/mMagneticMean );
    return 0;
  }

  static float deltaDip( float dip ) { return TDMath.abs( dip - mDipMean ); }

  static boolean isBlockMagneticBad( DBlock blk )
  {
    if ( blk.mAcceleration < 10.0f || blk.mMagnetic < 10.0f ) return false;
    return deltaMag( blk.mMagnetic ) > TDSetting.mMagneticThr
        || deltaAcc( blk.mAcceleration ) > TDSetting.mAccelerationThr
        || deltaDip( blk.mDip ) > TDSetting.mDipThr;
  }

}
