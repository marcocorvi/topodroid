package com.topodroid.dev.distox_ble;

import com.topodroid.dev.distox_ble.DistoXBLEQueue;
import com.topodroid.utils.TDLog;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class DistoXBLEQueue
{
    final Lock mLock = new ReentrantLock();
    final Condition notEmpty = mLock.newCondition();

    DistoXBLEBuffer mHead = null;
    DistoXBLEBuffer mTail = null;
    int size = 0;

    void put( byte[] bytes )
    {
        // TDLog.v( "BRIC queue put " + BleUtils.bytesToHexString( bytes ) );
        DistoXBLEBuffer buffer = new DistoXBLEBuffer( bytes );
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

    DistoXBLEBuffer get()
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
            DistoXBLEBuffer ret = mHead;
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
