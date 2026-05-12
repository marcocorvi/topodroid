/* @file JedeyeComm.java
 *
 * @author sebastien kister
 * @date 2026
 *
 * @brief TopoDroid JedEye BLE communication
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * JedEye is wire-compatible with the SAP6 shot frame, so the BLE plumbing
 * is inherited from SapComm. Only the GATT UUIDs differ.
 */
package com.topodroid.dev.jedeye;

import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.dev.Device;
import com.topodroid.dev.sap.SapComm;

import android.content.Context;
import android.bluetooth.BluetoothDevice;

public class JedeyeComm extends SapComm
{
  /** cstr
   * @param app        application
   * @param address    JedEye address
   * @param bt_device  BT (JedEye) device
   */
  public JedeyeComm( TopoDroidApp app, String address, BluetoothDevice bt_device )
  {
    super( app, address, bt_device, 6 ); // bootstrap as SAP6, then swap UUIDs
    mServiceUuid   = JedeyeConst.JEDEYE_SERVICE_UUID;
    mChrtReadUuid  = JedeyeConst.JEDEYE_CHRT_READ_UUID;
    mChrtWriteUuid = JedeyeConst.JEDEYE_CHRT_WRITE_UUID;
  }

  /** create the protocol
   * @param device   BT device
   */
  @Override
  protected void createProtocol( Device device )
  {
    mSapProto = new JedeyeProtocol( this, device, mContext );
  }

  /** SapComm.assertDevice is widened to protected so we can recognise JedEye
   * here without polluting the SAP package with JedEye knowledge.
   * @param device  bluetooth device
   * @return true if the device is JedEye (correct UUIDs are in place)
   */
  @Override
  protected boolean assertDevice( Device device )
  {
    if ( device.isJedeye() ) {
      assert( mServiceUuid   == JedeyeConst.JEDEYE_SERVICE_UUID );
      assert( mChrtReadUuid  == JedeyeConst.JEDEYE_CHRT_READ_UUID );
      assert( mChrtWriteUuid == JedeyeConst.JEDEYE_CHRT_WRITE_UUID );
      return true;
    }
    return super.assertDevice( device );
  }
}
