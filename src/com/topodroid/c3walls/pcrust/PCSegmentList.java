/** @file PCSegmentList.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief 3D segment list
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.pcrust;

class PCSegmentList
{
  PCSegment head;
  int size;

  PCSegmentList( )
  {
    head = null;
    size = 0;
  }

  PCSegmentList( PCSegment s )
  {
    head = s;
    size = 1;
  }
/*
  void mergeIn( PCSegmentList ll ) 
  {
    PCSegment ss = ll.head;
    while ( ss.next != null ) ss = ss.next;
    ss.next = head;
    head    = ll.head;
    size += ll.size;
    ll.head = null;
    ll.size = 0;
  }

  void add( PCSegment sgm )
  {
    sgm.next = head;
    head = sgm;
    ++ size;
  }
*/
 
  // insert a segment keeping the list ordered by increasing s
  void insert( PCSegment sgm ) 
  {
    if ( head == null ) {
      sgm.next = null;
      head = sgm;
    } else if ( head.s() > sgm.s() ) {
      sgm.next = head;
      head = sgm;
    } else {
      PCSegment s2 = head;
      while ( s2.next != null && s2.next.s() < sgm.s() ) s2 = s2.next;
      sgm.next = s2.next;
      s2.next  = sgm;
    }
    ++ size;
  }

  double centerZ()
  {
    double ret = 0;
    for ( PCSegment s = head; s != null; s = s.next ) {
      ret += s.v1.z;
      ret += s.v2.z;
    }
    return ret/( 2 * size );
  }

  double minZ()
  {
    double ret = 0;
    for ( PCSegment s = head; s != null; s = s.next ) {
      if ( s.v1.z < ret ) ret = s.v1.z;
      if ( s.v2.z < ret ) ret = s.v2.z;
    }
    return ret;
  }

  double maxZ()
  {
    double ret = 0;
    for ( PCSegment s = head; s != null; s = s.next ) {
      if ( s.v1.z > ret ) ret = s.v1.z;
      if ( s.v2.z > ret ) ret = s.v2.z;
    }
    return ret;
  }

}
