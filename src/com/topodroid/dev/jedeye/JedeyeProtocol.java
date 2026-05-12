/* @file JedeyeProtocol.java
 *
 * @author sebastien kister
 * @date 2026
 *
 * @brief TopoDroid JedEye BLE protocol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * JedEye uses the SAP6 17-byte shot frame (sequence byte + four little-endian
 * float32 = bearing/clino/roll/distance). All parsing is therefore inherited
 * from SapProtocol; this subclass only exists so the Device-type dispatch in
 * SapProtocol.handleRead() correctly identifies JedEye as SAP6-shaped data.
 */
package com.topodroid.dev.jedeye;

import com.topodroid.dev.Device;
import com.topodroid.dev.sap.SapProtocol;

import android.content.Context;

class JedeyeProtocol extends SapProtocol
{
  /** cstr
   * @param comm    communication class
   * @param device  BT device
   * @param context context
   */
  JedeyeProtocol( JedeyeComm comm, Device device, Context context )
  {
    super( comm, device, context );
  }
}
