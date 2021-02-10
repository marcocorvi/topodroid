/* @file BlePoint.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth low-energy point, in the service tree
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

class BlePoint
{
  final static int POINT_SRV  = 1;
  final static int POINT_CHRT = 2;
  final static int POINT_DESC = 3;

  final static int PERM_READ  = 1;
  final static int PERM_WRITE = 2;

  final static int PROP_READ  = 1;
  final static int PROP_WRITE = 2;
  final static int PROP_NOTIF = 4;
  final static int PROP_INDIC = 8;

  final static int WRITE_DFLT = 1;
  final static int WRITE_NRSP = 2;
  final static int WRITE_SIGN = 4;

  int type;
  int perm;
  int prop;
  int wtyp;
  UUID srv_uuid;
  UUID chrt_uuid;
  UUID desc_uuid;

  BlePoint( BluetoothGattService srv )
  {
    type = POINT_SRV;
    perm = 0;
    prop = 0;
    wtyp = 0;
    srv_uuid = srv.getUuid();
    chrt_uuid = null;
    desc_uuid = null;
  }

  BlePoint( UUID srv, BluetoothGattCharacteristic chrt )
  {
    type = POINT_CHRT;
    srv_uuid  = srv;
    chrt_uuid = chrt.getUuid();
    desc_uuid = null;
    perm = 0;
    if ( BleUtils.isChrtRead(chrt) )   perm += PERM_READ;
    if ( BleUtils.isChrtWrite(chrt) )  perm += PERM_WRITE;
    prop = 0;
    int p = chrt.getProperties();
    if ( BleUtils.canChrtPRead(chrt) )  prop += PROP_READ;
    if ( BleUtils.canChrtPWrite(chrt) ) prop += PROP_WRITE;
    if ( BleUtils.isChrtPNotify( p ) )   prop += PROP_NOTIF;
    if ( BleUtils.isChrtPIndicate( p ) ) prop += PROP_INDIC;
    wtyp = 0;
    int typ = chrt.getWriteType();
    if ( BleUtils.isChrtWDflt(type) )   wtyp += WRITE_DFLT;
    if ( BleUtils.isChrtWNoResp(type) ) wtyp += WRITE_NRSP;
    if ( BleUtils.isChrtWSign(type) )   wtyp += WRITE_SIGN;
  }

  BlePoint( UUID srv, UUID chrt, BluetoothGattDescriptor desc )
  {
    type = POINT_SRV;
    srv_uuid  = srv;
    chrt_uuid = chrt;
    desc_uuid = desc.getUuid();
    perm = 0;
    prop = 0;
    wtyp = 0;
    if ( BleUtils.isDescRead(desc) )   perm += PERM_READ;
    if ( BleUtils.isDescWrite(desc) )  perm += PERM_WRITE;
  }

  boolean canPRead()     { return ( prop & PROP_READ  ) != 0; }
  boolean canPWrite()    { return ( prop & PROP_WRITE ) != 0; }
  boolean canPNotify()   { return ( prop & PROP_NOTIF ) != 0; }
  boolean canPIndicate() { return ( prop & PROP_INDIC ) != 0; }

  String propertyString() 
  {
    StringBuilder sb = new StringBuilder();
    if ( ( perm & PERM_READ ) != 0 ) sb.append("r");
    if ( ( perm & PERM_WRITE ) != 0 ) sb.append("w");
    sb.append(":");
    if ( ( prop & PROP_READ ) != 0 ) sb.append("r");
    if ( ( prop & PROP_WRITE ) != 0 ) sb.append("w");
    if ( ( prop & PROP_NOTIF ) != 0 ) sb.append("n");
    if ( ( prop & PROP_INDIC ) != 0 ) sb.append("i");
    sb.append(":");
    if ( ( wtyp & WRITE_DFLT ) != 0 ) sb.append("d");
    if ( ( wtyp & WRITE_NRSP ) != 0 ) sb.append("n");
    if ( ( wtyp & WRITE_SIGN ) != 0 ) sb.append("s");
    return sb.toString();
  }
}
