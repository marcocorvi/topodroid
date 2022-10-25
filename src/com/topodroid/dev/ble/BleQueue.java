/* @file BleQueue.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BLE packet-buffer queue
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

// import com.topodroid.dev.ble.BleUtils;

import com.topodroid.utils.TDLog;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class BleQueue
{
  final Lock mLock = new ReentrantLock(); // mutex
  final Condition notEmpty = mLock.newCondition(); // condition variable

  BleBuffer mHead = null;
  BleBuffer mTail = null;
  public int size = 0;

  /** thread-safe putter
   * @param type   buffer type
   * @param bytes  buffer data (can be null)
   */
  public void put( int type, byte[] bytes )
  {
    // TDLog.v( "BLE queue put " + BleUtils.bytesToHexString( bytes ) );
    BleBuffer buffer = new BleBuffer( type, bytes );
    mLock.lock();
    try {
      if ( mTail == null ) {
        mHead = buffer;
        mTail = mHead;
        notEmpty.signal();
      } else {
        mTail.next = buffer;
      }
      ++ size;
    } finally {
      mLock.unlock();
    }
  }

  /** thread-safe getter
   * @return buffer
   */
  public BleBuffer get()
  {
    mLock.lock();
    try {
      while ( mHead == null ) {
        try {
          notEmpty.await();
        } catch ( InterruptedException e ) {
          TDLog.v("INTERRUPT " + e.getMessage() );
        }
      }
      BleBuffer ret = mHead;
      mHead = mHead.next;
      if ( mHead == null ) mTail = null;
      // ret.next = null;
      -- size;
      return ret;
    } finally {
      mLock.unlock();
    }
  }

  // /** flush queue buffers - UNUSED
  //  */
  // public void flush()
  // {
  //   mLock.lock();
  //   try {
  //     mHead = null;
  //     mTail = null;
  //     size  = 0;
  //   } finally {
  //     mLock.unlock();
  //   }
  // }

} 
