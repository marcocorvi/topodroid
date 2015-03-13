/** @file ConnectionQueueItem.java
 *
 */
package com.topodroid.DistoX;

class ConnectionQueueItem
{
  byte[] mData;
  ConnectionQueueItem next;
  ConnectionQueueItem prev;

  // create a new ConnectionQueue
  ConnectionQueueItem( byte[] data )
  {
    mData = data;
    next = null;
    prev = null;
  }

  // create a new ConnectionQueueItem and append to "p"
  ConnectionQueueItem( byte[] data, ConnectionQueueItem p )
  {
    mData = data;
    next = null;
    prev = p;
    if ( p != null ) p.next = this;
  }

  // create a new ConnectionQueueItem and link between "p" and "n"
  ConnectionQueueItem( byte[] data, ConnectionQueueItem n, ConnectionQueueItem p )
  {
    mData = data;
    next = n;
    prev = p;
    if ( n != null ) n.prev = this;
    if ( p != null ) p.next = this;
  }

  boolean hasId( byte id ) { return id == mData[0]; }

}
