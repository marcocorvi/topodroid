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

import com.topodroid.util.TDLog;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BleOpsQueue
{
  private final static boolean LOG = false;

  private ConcurrentLinkedQueue< BleOperation > mOps;
  private BleOperation mPendingOp = null;
  private boolean mDiscarded = false; // MM

  public BleOpsQueue()
  {
    mOps = new ConcurrentLinkedQueue< BleOperation >();
  }

  /** clear the queue
   * @param after_clear_pending whether to clear the queue after a "clear-pending" or before it
   */
  public synchronized void clear( boolean after_clear_pending)
  {
    if ( after_clear_pending ) clearPending();
    mOps.clear();
    if ( ! after_clear_pending ) clearPending();
  }

  /** @return true if the queue is empty and there is no pending op
   */
  public synchronized boolean isIdle() // MM
  {
    return  mPendingOp == null && mOps.isEmpty();
  }

  /** clear the queue and the (possible) pending op 
   * shutdown must discards ops without exec the next command
   */
  public synchronized void discard() // MM
  {
    mDiscarded = true;
    mOps.clear();
    mPendingOp = null;
  }

  /** clear the pending op and do the next if the queue is not empty
   */
  public synchronized void clearPending()
  {
    if ( mDiscarded ) return; // MM
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
   * @note if the queue is discarded the op will not be taken
   */
  public synchronized int enqueueOp( BleOperation op )
  {
    if ( mDiscarded ) return 0; // MM
    mOps.add( op );
    // printOps(); // DEBUG
    if ( mPendingOp == null ) {
      doNextOp();
    }
    return mOps.size();
  }

  /** do the next op on the queue
   * @note is the queue is discarded no op is executed
   * @note access by BricChrtChanged
   */
  public synchronized void doNextOp()
  {
    if ( mDiscarded ) return; // MM
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
