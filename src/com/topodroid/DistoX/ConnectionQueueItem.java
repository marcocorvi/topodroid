/* @file ConnectionQueueItem.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid cosurveying connection queue item
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

/* ---- IF_COSURVEY
 
class ConnectionQueueItem
{
  byte[] mData;
  ConnectionQueueItem next;
  ConnectionQueueItem prev;

  // create a new ConnectionQueue
  // ConnectionQueueItem( byte[] data ) // UNUSED
  // {
  //   mData = data;
  //   next = null;
  //   prev = null;
  // }

  // create a new ConnectionQueueItem and append to "p"
  ConnectionQueueItem( byte[] data, ConnectionQueueItem p )
  {
    mData = data;
    next = null;
    prev = p;
    if ( p != null ) p.next = this;
  }

  // create a new ConnectionQueueItem and link between "p" and "n"
  // ConnectionQueueItem( byte[] data, ConnectionQueueItem n, ConnectionQueueItem p ) // UNUSED
  // {
  //   mData = data;
  //   next = n;
  //   prev = p;
  //   if ( n != null ) n.prev = this;
  //   if ( p != null ) p.next = this;
  // }

  boolean hasId( byte id ) { return id == mData[0]; }

}

*/
