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

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

class SurveyAccuracy
{
  /** the class SurveyAccuracy contains a hashmap of Accuracy class 
   * indexed by the device MAC.
   * The Accuracy calsses are used to compute the shots accuracy.
   * Shot data balcks contribute to the Accuracy of their MAC address,
   * and the shot accuracy is computed using the Accuracy of their MAC address.
   */
  private HashMap< String, Accuracy > mAccuracies;

  /** default cstr
   */
  SurveyAccuracy()
  {
    // TDLog.v("ACCURACY default cstr");
    mAccuracies = new HashMap< String, Accuracy >();
  }

  /** cstr
   * @param blks   list of data, used to initialize the accuracy
   */
  SurveyAccuracy( List< DBlock > blks )
  {
    // TDLog.v("ACCURACY list cstr " + blks.size() );
    mAccuracies = new HashMap< String, Accuracy >();
    setBlocks( blks );
  }

  /** @return the Accuracy class of the block MAC address
   * @param blk   shot data block
   */
  private Accuracy getBlockAccuracy( DBlock blk )
  {
    if ( blk == null ) return null;
    String device = blk.getAddress();
    if ( device == null || device.isEmpty() ) return null;
    return mAccuracies.get( device );
  }

  /** @return true if a shot is above A/M/D threshold
   * @param blk    shot data 
   */
  boolean isBlockAMDBad( DBlock blk )
  {
    Accuracy accu = getBlockAccuracy( blk );
    if ( accu == null ) return false;
    return accu.isBlockAMDBad( blk );
  }

  /** @return the string with how much the data differ from the means
   * @param blk  shot data (guatanteed not null)
   */
  String getBlockExtraString( DBlock blk )
  {
    Accuracy accu = getBlockAccuracy( blk );
    if ( accu == null ) return TDString.EMPTY;
    return accu.getBlockExtraString( blk );
  }

  /** add a block to the existing means
   * @param blk  shot data
   * [ @note blocks are added when they arrive to the app ]
   */
  void addBlockAMD( DBlock blk ) 
  {
    if ( blk == null ) return;
    // if ( blk == null || blk.mAcceleration < Accuracy.THRS ) return;
    String device = blk.getAddress();
    // TDLog.v("Accu add block " + blk.mId + " device <" + device + ">" );  
    if ( device == null || device.isEmpty() ) return;
    Accuracy accu = getBlockAccuracy( blk );
    if ( accu == null ) {
      TDLog.v("ACCU new " + device );
      accu = new Accuracy( device );
      mAccuracies.put( device, accu );
    }
    accu.addBlockAMD( blk );
  }

  /** set the means with a list of shot data
   * @param blks   list of shot data
   */
  private void setBlocks( List< DBlock > blks ) 
  {
    if ( TDUtil.isEmpty( blks ) ) return;
    for ( DBlock blk : blks ) {
      addBlockAMD( blk );
    } 
  }

  // /** set the means with the shot data in a survey statistics
  //  * @param stat   survey statistics
  //  * @param n      number of data in stat vectors
  //  * @return the number of items used in the averages
  //  */
  // public int setBlocks( SurveyStat stat, int n  ) 
  // {
  //   float ma = 0;
  //   float mm = 0;
  //   int   nr = 0;
  //   for ( int k = 0; k < n; ++k ) {
  //     if ( stat.G[k] > Accuracy.THRS ) { 
  //       ma += stat.G[k];
  //       mm += stat.M[k];
  //       ++ nr;
  //     }
  //   }
  //   if ( nr == 0 ) return 0;
  //   ma /= nr;
  //   mm /= nr;
  //   for ( int k = 0; k < n; ++k ) {
  //     if ( stat.G[k] > Accuracy.THRS ) { 
  //       addBlockAcc( stat.G[k], ma );
  //       addBlockMag( stat.M[k], mm );
  //       addBlockDip( stat.D[k] );
  //     }
  //   }
  //   if ( mCountDip > 0 ) stat.averageD = mDipSum / mCountDip;
  //   stat.averageG = mAccelerationSum / mCountAcc;
  //   stat.averageM = mMagneticSum / mCountMag;
  //   return nr;
  // } 

  void debug()
  {
    TDLog.v("Survey Accuracy - size " + mAccuracies.size() );
    for ( String key : mAccuracies.keySet() ) {
      Accuracy acc = (Accuracy)(mAccuracies.get( key ));
      TDLog.v( acc.toString() );
    }
  }

}
