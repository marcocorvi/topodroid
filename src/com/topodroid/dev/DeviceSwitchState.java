/* @file DeviceSwitchState.java
 *
 * @author muddy mohawk
 * @date oct 2026
 *
 * @brief TopoDroid device switch state
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

import com.topodroid.util.TDLog;
import com.topodroid.TDX.TDInstance;

/** Serializes finite device operations and a single pending survey switch.
 * TODO proper name may be DeviceState
 */
public class DeviceSwitchState
{
  private int mOperations;    // number of operations: can be only 0 or 1
  private boolean mSwitching; // whether is switching
  private String mPending;    // pending (what ???)

  /** @return true if it is not switching AND nothing pending AND no operation 
   */
  synchronized public boolean beginOperation()
  {
    if ( ! TDInstance.canSwitchDevice() ) return true;
    TDLog.v("beginOperation: switching " + mSwitching + " pending " + (mPending != null) + " operations " + mOperations );
    if ( mSwitching || mPending != null || mOperations != 0 ) return false;
    ++mOperations;
    return true;
  }

  /** end the operation (decrease the operation counter from 1 to 0)
   */
  synchronized public void endOperation()
  {
    if ( ! TDInstance.canSwitchDevice() ) return;
    TDLog.v("endOperation: operations " + mOperations );
    if ( mOperations <= 0 ) throw new IllegalStateException( "No device operation to finish" );
    --mOperations;
  }

  /** set the pending string
   * @param current   current string
   * @param target    new string
   * @return true if not switching AND target id not null nor equal to current
   */
  synchronized public boolean request( String current, String target )
  {
    if ( target == null || target.equals( current ) || mSwitching ) return false;
    mPending = target;
    return true;
  }

  /** clear the pending and set switching
   * @return null if already switching OR no pending OR there is an operation 
   */
  synchronized public String takePending()
  {
    if ( mOperations != 0 || mSwitching || mPending == null ) return null;
    String target = mPending;
    mPending = null;
    mSwitching = true;
    return target;
  }

  /** notified switching finish
   */
  synchronized public void finishSwitch() { mSwitching = false; }

  /** notified to cancel pending
   */
  synchronized public void cancelPending() { mPending = null; }

  /** @return true if switching
   */
  synchronized public boolean isSwitching() { return mSwitching; }

  /** return true if there is an operation OR is switching OR there is a pending string
   */
  synchronized public boolean isBusy() 
  { 
    return mOperations != 0 || mSwitching || mPending != null;
  }
}
