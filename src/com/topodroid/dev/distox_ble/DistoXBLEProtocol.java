/* @file DeviceXBLEProtocol.java
 *
 * @author siwei tian
 * @date aug 2022
 *
 * @brief TopoDroid DistoX XBLE data protocol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox_ble;

import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.ListerHandler;
// import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidProtocol;
import com.topodroid.packetX.MemoryOctet;
import com.topodroid.utils.TDLog;

import android.content.Context;
// import android.os.Handler;

// import java.io.IOException;

public class DistoXBLEProtocol extends TopoDroidProtocol
{

  private final DistoXBLEComm mComm;
  private final ListerHandler mLister;


  public static final int PACKET_REPLY          = 0x10;
  public static final int PACKET_INFO_SERIALNUM = 0x11;
  public static final int PACKET_INFO_FIRMWARE  = 0x12;
  public static final int PACKET_INFO_HARDWARE  = 0x13;
  public static final int PACKET_STATUS         = 0x14;
  public static final int PACKET_WRITE_REPLY    = 0x15;
  public static final int PACKET_COEFF          = 0x16;
  public static final int PACKET_FLASH_CHECKSUM = 0x17;
  public static final int PACKET_INFO_SHOTDATA  = 3;
  public static final int PACKET_INFO_CALIDATA  = 4;
  public static final int PACKET_FLASH_BYTES_1  = 0x18;
  public static final int PACKET_FLASH_BYTES_2  = 0x19;
  public static final int PACKET_SIGNATURE      = 0x1A;

  public static final int PACKET_MEASURE_DATA   = 0x20;
  // public static final int PACKET_SHOT_DATA   = 0x21; // PACKET_MEASURE_DATA | 0x01
  // public static final int PACKET_CALIB_DATA  = 0x22; // PACKET_MEASURE_DATA | 0x02

  public static final int PACKET_NONE           = 0;
  public static final int PACKET_ERROR          = 0x80;

  public String mFirmVer;
  public String mHardVer;
  public byte[] mRepliedData;
  public int mCheckSum;

  public byte[] mMeasureDataPacket1;
  public byte[] mMeasureDataPacket2;

  public byte[] mFlashBytes;
  //public int mPacketType;

  /** cstr
   * @param ctx      context
   * @param app      application
   * @param lister   data lister
   * @param device   BT device
   * @param comm     DistoX BLE comm object
   */
  public DistoXBLEProtocol(Context ctx, TopoDroidApp app, ListerHandler lister, Device device, DistoXBLEComm comm )
  {
    super( device, ctx );
    mLister = lister;
    mComm   = comm;
    mRepliedData = new byte[4];
    mMeasureDataPacket1 = new byte[8];
    mMeasureDataPacket2 = new byte[8];
    mFlashBytes = new byte[256];
  }

  /** process a data array
   * @param databuf  input data array, 
   *        length is 16 for shot data, otherwise is a command reply
   *                   5 for flash checksum (0x3b)
   *                   3 for hw signature (0x3c)
   *        offset  0 is command: it can be 0x3a 0x3b 0x3c 0x3d 0x3e
   *        offsets 1,2 contain address (0x3d 0x3e), reply (0x3c)
   *        offset  3 payload length (0x3d 0x3e)
   * @return packet type
   */
  public int packetProcess( byte[] databuf )
  {
    if ( databuf.length == 0 ) {
      TDLog.v("XBLE proto 0-length data");
      return PACKET_NONE;
    }
    if ( (databuf[0] == MemoryOctet.BYTE_PACKET_DATA || databuf[0] == MemoryOctet.BYTE_PACKET_G ) && databuf.length == 17 ) { // shot / calib data
      if ( mComm.isDownloading() ) {
        System.arraycopy( databuf, 1, mMeasureDataPacket1, 0, 8);
        System.arraycopy( databuf, 9, mMeasureDataPacket2, 0, 8);
        int res1 = handlePacket(mMeasureDataPacket1);
        int res2 = handlePacket(mMeasureDataPacket2);
        // TDLog.v("XBLE proto 17-length data - type " + databuf[0] + " res " + res1 + " " + res2 );
        if ( res1 != PACKET_NONE && res2 != PACKET_NONE ) {
          mComm.sendCommand(( mMeasureDataPacket1[0] & 0x80 ) | 0x55);
          mComm.handleRegularPacket(res1, mLister, 0);
          mComm.handleRegularPacket(res2, mLister, 0);
          return PACKET_MEASURE_DATA; // with ( PACKET_MEASURE_DATA | databuf[0]) shots would be distinguished from calib
        // } else {
        //   return PACKET_ERROR;
        }
      } else {
        TDLog.Error("XBLE not downloading");
        return PACKET_NONE;
      }
    } else { // command packet
      byte command = databuf[0];
      if ( command == MemoryOctet.BYTE_PACKET_3D || command == MemoryOctet.BYTE_PACKET_3E ) { // 0x3d or 0x3e
        int addr = (databuf[2] << 8 | (databuf[1] & 0xff)) & 0xFFFF;
        int len = databuf[3];
        mRepliedData = new byte[len];
        // TDLog.v("XBLE command packet " + command + " length " + len );
        for (int i = 0; i < len; i++)
          mRepliedData[i] = databuf[i + 4];
        if (addr == DistoXBLEDetails.FIRMWARE_ADDRESS) {
          mFirmVer = Integer.toString(databuf[4]) + "." + Integer.toString(databuf[5]) + "." + Integer.toString(databuf[6]);
          TDLog.v("XBLE fw " + mFirmVer );
          return PACKET_INFO_FIRMWARE;
        } else if (addr == DistoXBLEDetails.HARDWARE_ADDRESS) {
          float HardVer = ((float) databuf[4]) / 10;
          mHardVer = Float.toString(HardVer);
          return PACKET_INFO_HARDWARE;
        } else if (addr == DistoXBLEDetails.STATUS_ADDRESS) {
          return PACKET_STATUS;
        } else if ( command == MemoryOctet.BYTE_PACKET_3D ) { // 0x3d
          return PACKET_REPLY;
        } else if ( command == MemoryOctet.BYTE_PACKET_3E ) { // 0x3e
          return PACKET_WRITE_REPLY;
        // } else {
        //   return PACKET_ERROR;
        }
      } else if ( command == MemoryOctet.BYTE_PACKET_HW_CODE ) { // 0x3c: signature: hardware ver. - 0x3d 0x3e only works in App mode not in the bootloader mode.
        // 0x3a 0x3b 0x3c are commands work in bootloader mode
        // TDLog.v("XBLE command packet " + command + " (signature) length " + databuf.length );
        if ( databuf.length == 3 ) { 
          mRepliedData[0] = databuf[1];
          mRepliedData[1] = databuf[2];
          return PACKET_SIGNATURE;
        }
      } else if ( databuf[0] == MemoryOctet.BYTE_PACKET_FW_WRITE ) { // 0x3b
        // TDLog.v("XBLE command packet " + command + " (checksum) length " + databuf.length );
        if ( databuf.length == 5 ) {
          mCheckSum = ((databuf[4] << 8) | (databuf[3] & 0xff)) & 0xffff;
          return PACKET_FLASH_CHECKSUM;
        }
      } else if ( command == MemoryOctet.BYTE_PACKET_FW_READ && databuf.length == 131) {   // 0x3a: 3 headers + 128 payloadsda
        // TDLog.v("XBLE command packet " + command + " (firmware) length " + databuf.length );
        if ( databuf[2] == 0x00 ) {        // firmware first packet (MTU=247)
          for ( int i=3; i<131; i++) mFlashBytes[i-3] = databuf[i]; // databuf is copied from offset 3
          return PACKET_FLASH_BYTES_1;
        } else if ( databuf[2] == 0x01 ) {   // firmware second packet
          for ( int i=3; i<131; i++) mFlashBytes[i+128-3] = databuf[i]; 
          return PACKET_FLASH_BYTES_2;
        // } else {
        //   // TDLog.Error("XBLE ...");
        //   return PACKET_ERROR;
        }
      }
    }
    return PACKET_ERROR;
  }

}
