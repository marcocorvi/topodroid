/** @file ConnectionQueue.java
 *
 */
package com.topodroid.DistoX;

class ConnectionQueue
{
  int mSize;
  ConnectionQueueItem mHead;
  ConnectionQueueItem mLast;

  ConnectionQueue( )
  {
    mHead = null;
    mLast = null;
    mSize = 0;
  }


  ConnectionQueueItem add( byte[] data )
  {
    mLast = new ConnectionQueueItem( data, mLast );
    if ( mHead == null ) {
      mHead = mLast;
    } 
    ++ mSize;
    return mLast;
  }

  int size() { return mSize; }

  void remove( ConnectionQueueItem item )
  {
    if ( mHead == null ) return;
    if ( item == mHead ) {
      if ( mLast == mHead ) {
        mHead = null;
        mLast = null;
      } else {
        mHead = mHead.next;
        mHead.prev = null;
      }
      -- mSize;
    } else if ( item == mLast ) {
      mLast = mLast.prev;
      mLast.next = null;
      -- mSize;
    } else { 
      ConnectionQueueItem temp = mHead.next;
      while ( temp != item && temp != null ) temp = temp.next;
      if ( temp == null ) return;
      temp.prev.next = temp.next;
      temp.next.prev = temp.prev;
      -- mSize;
    }
  }

  ConnectionQueueItem peek() { return mHead; }

  boolean isEmpty() { return mSize <= 0; }

  ConnectionQueueItem find( byte id ) 
  {
    ConnectionQueueItem temp = mHead;
    while ( temp != null ) {
      if ( temp.hasId( id ) ) return temp;
      temp = temp.next;
    }
    return null;
  }

  void clear()
  {
    ConnectionQueueItem temp = mHead;
    mHead = null;
    while ( temp != null ) {
      ConnectionQueueItem next = temp.next;
      temp.prev = null;
      temp.next = null;
      temp = next;
    }
    mLast = null;
    mSize = 0;
  }


}
