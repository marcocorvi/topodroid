/* @file BricBuffer.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BRIC4 packet buffer 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import java.util.Arrays;

class BricBuffer
{
  byte[] data;
  int type;
  BricBuffer next;
  
  BricBuffer( int t, byte[] bytes )
  {
    data = Arrays.copyOfRange( bytes, 0, bytes.length );
    type = t;
    next = null;
  }
}

