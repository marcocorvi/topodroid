/* @file BricQueue.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BRIC4 packet-buffer queue
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

class BricQueue
{
  final Lock mLock = new ReentrantLock();
  final Condition notEmpty = mLock.newCondition();

  BricBuffer mHead = null;
  BricBuffer mTail = null;
  int size = 0;

  void put( int type, byte[] bytes )
  {
    BricBuffer buffer = new BricBuffer( type, bytes );
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

  BricBuffer get()
  {
    mLock.lock();
    try {
      while ( mHead == null ) {
        try {
          notEmpty.await();
        } catch ( InterruptedException e ) { }
      }
      BricBuffer ret = mHead;
      mHead = mHead.next;
      if ( mHead == null ) mTail = null;
      // ret.next = null;
      -- size;
      return ret;
    } finally {
      mLock.unlock();
    }
  }

} 
