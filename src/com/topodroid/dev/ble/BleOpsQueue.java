/* @file BleOpsQueue.java
 *
 * @author siwei tian
 * @date july 2024
 *
 * @brief TopoDroid BLE operation queue
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BleOpsQueue
{
  private final static boolean LOG = false;

  private ConcurrentLinkedQueue< BleOperation > mOps;
  private BleOperation mPendingOp = null;

  public BleOpsQueue()
  {
    mOps = new ConcurrentLinkedQueue< BleOperation >();
  }

  public void clear( boolean after_clear_pending)
  {
    if ( after_clear_pending ) clearPending();
    mOps.clear();
    if ( ! after_clear_pending ) clearPending();
  }

  /** clear the pending op and do the next if the queue is not empty
   */
  public void clearPending()
  {
    mPendingOp = null;
    // if ( ! mOps.isEmpty() || mPendingCommands > 0 ) doNextOp();
    if ( ! mOps.isEmpty() ) {
      doNextOp();
    } else {
      if ( LOG ) TDLog.v( "OpsQueue \"clear pending\": no more ops" );
    }
  }

  /** add a BLE op to the queue
   * @param op   BLE op
   * @return the length of the ops queue
   */
  public int enqueueOp( BleOperation op )
  {
    mOps.add( op );
    // printOps(); // DEBUG
    if ( mPendingOp == null ) {
      doNextOp();
    }
    return mOps.size();
  }

  /** do the next op on the queue
   * @note access by BricChrtChanged
   */
  public void doNextOp()
  {
    if ( mPendingOp != null ) {
      if ( LOG ) TDLog.v( "OpsQueue next op with pending " + mPendingOp.name() + " not null, ops " + mOps.size() );
      return;
    }
    mPendingOp = mOps.poll();
    if ( mPendingOp != null ) {
      if ( LOG ) TDLog.v( "OpsQueue polled, ops " + mOps.size() + " exec " + mPendingOp.name() );
      mPendingOp.execute();
    } else {
      if ( LOG ) TDLog.v( "OpsQueue do next op - no op");
    }
    // else if ( mPendingCommands > 0 ) {
    //   enqueueShot( this );
    //   -- mPendingCommands;
    // }
  }

}
