/* @file BleBuffer.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BLE packet buffer 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import java.util.Arrays;

public class BleBuffer
{
  public byte[] data;
  public int type;
  BleBuffer next;
  
  /** cstr
   * @param t     type
   * @param bytes array of bytes copied into the data (can be null)
   */
  public BleBuffer( int t, byte[] bytes )
  {
    type = t;
    data = (bytes != null)? Arrays.copyOfRange( bytes, 0, bytes.length ) : null;
    next = null;
  }

  // /** @return size of the data array, (-1 if data is null)
  //  */
  // public int length()
  // {
  //   return (data == null)? -1 : data.length;
  // }

}

