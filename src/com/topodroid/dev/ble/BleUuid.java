/** @fle BleUuid.java
 *
 * adevrtised service UUID's
 */
package com.topodroid.dev.ble;

import com.topodroid.dev.bric.BricConst;
import com.topodroid.dev.sap.SapConst;
import com.topodroid.dev.distox_ble.DistoXBLEConst;
import com.topodroid.dev.cavway.CavwayConst;

import java.util.UUID;

public class BleUuid
{
  // service UUID strings
  private static final UUID[] mUuids = { 
    BricConst.MEAS_SRV_UUID,         // BRIC4 BRIC5
    BricConst.CTRL_SRV_UUID,
    SapConst.SAP5_SERVICE_UUID, // SAP5
    SapConst.SAP6_SERVICE_UUID, // SAP6 DiscoX
    DistoXBLEConst.DISTOXBLE_SERVICE_UUID,
    CavwayConst.CAVWAY_SERVICE_UUID
  };

}


