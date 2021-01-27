/* @file BleUtils.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth low-energy utility functions and constants
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothProfile;
// import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCharacteristic;
// import android.bluetooth.BluetoothGattCallback;
// import android.bluetooth.BluetoothGattService;

import java.util.UUID;
import java.util.Arrays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class BleUtils
{
  // ------------------------------------------------------------------------------
  // UTIILS

  final static String STANDARD_UUID =  "-0000-1000-8000-00805f9b34fb";

  final static UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  
  static String uuidToString( UUID uuid )
  {
    String ret = uuid.toString();
    if ( ret.endsWith( STANDARD_UUID ) ) return ret.substring(0,8);
    return ret;
  }

  static String uuidToShortString( UUID uuid )
  {
    String ret = uuid.toString().substring(0,8);
    if ( ret.startsWith("0000") ) return ret.substring(4,8);
    return ret;
  }

  static UUID toUuid( String str ) 
  { 
    if ( str.length() <= 4 ) {
      return UUID.fromString( ( "0000" + str + STANDARD_UUID) );
    } else if ( str.length() <= 8 ) {
      return UUID.fromString( (str + STANDARD_UUID) );
    }
    return UUID.fromString( str );
  }

  static String deviceToString( BluetoothDevice device )
  {
    String name = device.getName();
    if ( name == null ) name = "--";
    return name + " " + device.getAddress();
  }

  // ------------------------------------------------------------------------------
  // DEBUG
  static String chrtPropString( BluetoothGattCharacteristic chrt )
  {
    int prop = chrt.getProperties();
    String B = isChrtPBcast(prop)? "B" : ".";
    String I = isChrtPIndicate(prop)? "I" : ".";
    String N = isChrtPNotify(prop)? "N" : ".";

    String r = isChrtPRead(prop)? "R" : ".";
    String w = isChrtPWrite(prop)? "W" : ".";
    String s = isChrtPWriteSign(prop)? "Ws" : ".";
    String n = isChrtPWriteNoResp(prop)? "Wn" : ".";

    String wtype = "";
    if ( canChrtPWrite( chrt ) ) {
      int type = chrt.getWriteType();
      wtype = (isChrtWDflt(type)? "d" : "-") + (isChrtWNoResp(type)? "n" : "-") + (isChrtWSign(type)? "s" : "-");
    }
    return " props(" + prop + ") " + B + I + N + " " + r + w + s + n + " w-type " + wtype;
  } 

  static String chrtPermString( BluetoothGattCharacteristic chrt )
  {
    int perm = chrt.getPermissions();
    boolean r = isChrtRead(perm)  || isChrtReadEnc(perm);
    boolean w = isChrtWrite(perm) || isChrtWriteEnc(perm) || isChrtWriteSign(perm);
    return " perms(" + perm + ") " + (r? "R" : ".") + (w? "W" : ".");
  } 

  static String descPermString( BluetoothGattDescriptor desc )
  {
    int perm = desc.getPermissions();
    boolean r = isDescRead(perm)  || isDescReadEnc(perm);
    boolean w = isDescWrite(perm) || isDescWriteEnc(perm) || isDescWriteSign(perm);
    return " perms(" + perm + ") " + (r? "R" : ".") + (w? "W" : ".");
  } 

  static String bytesToString( byte[] bytes )
  {
    StringBuilder sb = new StringBuilder();
    for ( byte b : bytes ) sb.append( (int)b ).append(" ");
    return sb.toString();
  }

  // ------------------------------------------------------------------------------
  static boolean isChrtPBcast( int prop )       { return (prop & BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0; }
  static boolean isChrtPIndicate( int prop )    { return (prop & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0; }
  static boolean isChrtPNotify( int prop )      { return (prop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0; }
  static boolean isChrtPRead( int prop )        { return (prop & BluetoothGattCharacteristic.PROPERTY_READ) != 0; }
  static boolean isChrtPWrite( int prop )       { return (prop & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0; }
  static boolean isChrtPWriteSign( int prop )   { return (prop & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0; }
  static boolean isChrtPWriteNoResp( int prop ) { return (prop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0; }

  static boolean canChrtPNotify( BluetoothGattCharacteristic chrt )
  { 
    int prop = chrt.getProperties();
    return ( isChrtPIndicate( prop ) || isChrtPNotify( prop ) );
  }

  static byte[] getChrtPNotify( BluetoothGattCharacteristic chrt )
  { 
    int prop = chrt.getProperties();
    if ( isChrtPIndicate( prop ) ) return BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
    if ( isChrtPNotify( prop ) ) return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    return null;
  }

  static boolean canChrtPRead( BluetoothGattCharacteristic chrt )
  { 
    return isChrtPRead( chrt.getProperties() );
  }

  static boolean canChrtPWrite( BluetoothGattCharacteristic chrt )
  { 
    int prop = chrt.getProperties();
    return isChrtPWrite(prop) || isChrtPWriteSign(prop) || isChrtPWriteNoResp(prop);
  }

  static int getChrtWriteType( BluetoothGattCharacteristic chrt )
  {
    int prop = chrt.getProperties();
    if (  isChrtPWrite(prop) ) return BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
    if ( isChrtPWriteNoResp(prop) ) return BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
    return -1;
  }

  static boolean isChrtWDflt( int type )         { return (type & BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) != 0; }
  static boolean isChrtWNoResp( int type )       { return (type & BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) != 0; }
  static boolean isChrtWSign( int type )         { return (type & BluetoothGattCharacteristic.WRITE_TYPE_SIGNED) != 0; }

  static boolean isChrtRead( int perm )          { return (perm & BluetoothGattCharacteristic.PERMISSION_READ) != 0; }
  static boolean isChrtWrite( int perm )         { return (perm & BluetoothGattCharacteristic.PERMISSION_WRITE) != 0; }
  static boolean isChrtReadWrite( int perm )     { return isChrtRead( perm ) && isChrtWrite( perm ); }
  static boolean isChrtReadEnc( int perm )       { return (perm & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) != 0; }
  static boolean isChrtWriteEnc( int perm )      { return (perm & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) != 0; }
  static boolean isChrtWriteSign( int perm )     { return (perm & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED) != 0; }
  static boolean isChrtReadEncMitm( int perm )   { return (perm & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) != 0; }
  static boolean isChrtWriteEncMitm( int perm )  { return (perm & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM) != 0; }
  static boolean isChrtWriteSignMitm( int perm ) { return (perm & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM) != 0; }

  static boolean isChrtRead( BluetoothGattCharacteristic chrt )
  { 
    int perm = chrt.getPermissions();
    return isChrtRead( perm ) || isChrtReadEnc( perm ) || isChrtReadEncMitm( perm );
  }

  static boolean isChrtWrite( BluetoothGattCharacteristic chrt )
  { 
    int perm = chrt.getPermissions();
    return isChrtWrite( perm ) || isChrtWriteSign( perm ) || isChrtWriteSignMitm( perm ) || isChrtWriteEnc( perm ) || isChrtWriteEncMitm( perm );
  }

  static boolean isDescRead( int perm )          { return (perm & BluetoothGattDescriptor.PERMISSION_READ) != 0; }
  static boolean isDescWrite( int perm )         { return (perm & BluetoothGattDescriptor.PERMISSION_WRITE) != 0; }
  static boolean isDescReadWrite( int perm )     { return isDescRead( perm ) && isDescWrite( perm ); }
  static boolean isDescReadEnc( int perm )       { return (perm & BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED) != 0; }
  static boolean isDescWriteEnc( int perm )      { return (perm & BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED) != 0; }
  static boolean isDescWriteSign( int perm )     { return (perm & BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED) != 0; }
  static boolean isDescReadEncMitm( int perm )   { return (perm & BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM) != 0; }
  static boolean isDescWriteEncMitm( int perm )  { return (perm & BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM) != 0; }
  static boolean isDescWriteSignMitm( int perm ) { return (perm & BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM) != 0; }

  static boolean isDescRead( BluetoothGattDescriptor desc )
  { 
    int perm = desc.getPermissions();
    return isDescRead( perm ) || isDescReadEnc( perm ) || isDescReadEncMitm( perm );
  }

  static boolean isDescWrite( BluetoothGattDescriptor desc )
  { 
    int perm = desc.getPermissions();
    return isDescWrite( perm ) || isDescWriteSign( perm ) || isDescWriteSignMitm( perm ) || isDescWriteEnc( perm ) || isDescWriteEncMitm( perm );
  }

  // ---------------------------------------------------------------------------------
  static float getFloat( byte[] bytes, int offset )
  {
    if ( offset+4 > bytes.length ) return 0;
    return ByteBuffer.wrap( bytes ).order(ByteOrder.LITTLE_ENDIAN).getFloat( offset );
  }

  static int getInt( byte[] bytes, int offset )
  {
    if ( offset+4 > bytes.length ) return 0;
    return ByteBuffer.wrap( bytes ).order(ByteOrder.LITTLE_ENDIAN).getInt( offset );
  }

  static short getShort( byte[] bytes, int offset )
  {
    if ( offset+2 > bytes.length ) return 0;
    return ByteBuffer.wrap( bytes ).order(ByteOrder.LITTLE_ENDIAN).getShort( offset );
  }

  static int getChar( byte[] bytes, int offset )
  {
    if ( offset >= bytes.length ) return 0;
    int i = bytes[offset];
    return ( i < 0 )? i+256 : i;
  }
}
