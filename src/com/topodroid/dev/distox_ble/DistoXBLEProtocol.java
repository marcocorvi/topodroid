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

import android.content.Context;
import android.os.Handler;

import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidProtocol;

import java.io.IOException;

public class DistoXBLEProtocol extends TopoDroidProtocol
{

  private final DistoXBLEComm mComm;
  private final Handler mLister;


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

  public static final int PACKET_NONE           = 0;
  public static final int PACKET_ERROR          = 0x80;

  public String mFirmVer;
  public String mHardVer;
  public byte[] mRepliedData;
  public int mCheckSum;

  public byte[] mMeasureDataPacket1;
  public byte[] mMeasureDataPacket2;

  public byte[] mFlashBytes;
  // public boolean   mFlashFirstPacketReiceved; // not necessary because packet type can be distinguished by databuf length
  //public int mPacketType;

  /** cstr
   * @param ctx      context
   * @param app      application
   * @param lister   data lister
   * @param device   BT device
   * @param comm     DistoX BLE comm object
   */
  public DistoXBLEProtocol(Context ctx, TopoDroidApp app, Handler lister, Device device, DistoXBLEComm comm )
  {
    super( device, ctx );
    mLister = lister;
    mComm   = comm;
    mRepliedData = new byte[4];
    mMeasureDataPacket1 = new byte[8];
    mMeasureDataPacket2 = new byte[8];
    // mFlashFirstPacketReiceved = false;
    mFlashBytes = new byte[256];
  }

  /** process a data array
   * @param databuf  input data array, 
   *        length is 16 for shot data, otherwise is a command reply
   *                   5 for flash checksum (0x3b)
   *                   3 for hw signature (0x3c)
   *        offset  0 is command: it can be 0x3a 0x3b 0x3c 0x3d 0x3e
   *        offsets 1,2 contain adddres (0x3d 0x3e), reply (0x3c)
   *        offset  3 payload length (0x3d 0x3e)
   * @return packet type
   */
  public int packetProcess( byte[] databuf )
  {
    if ( databuf.length == 0 ) return PACKET_NONE;
    if ( databuf.length == 16 ) {       //shot data
      System.arraycopy(databuf,0,mMeasureDataPacket1,0,8);
      System.arraycopy(databuf,8,mMeasureDataPacket2,0,8);
      int res1 = handlePacket(mMeasureDataPacket1);
      int res2 = handlePacket(mMeasureDataPacket2);
      if ( res1 != PACKET_NONE && res2 != PACKET_NONE ) {
        mComm.sendCommand(( mMeasureDataPacket1[0] & 0x80 ) | 0x55);
        mComm.handleRegularPacket(res1, mLister, 0);
        mComm.handleRegularPacket(res2, mLister, 0);
        return PACKET_MEASURE_DATA;
      // } else {
      //   return PACKET_ERROR;
      }
    } else { // command packet
      byte command = databuf[0];
      if ( command == 0x3d || command == 0x3e ) {
        int addr = (databuf[2] << 8 | (databuf[1] & 0xff)) & 0xFFFF;
        int len = databuf[3];
        mRepliedData = new byte[len];
        for (int i = 0; i < len; i++)
          mRepliedData[i] = databuf[i + 4];
        if (addr == DistoXBLEDetails.FIRMWARE_ADDRESS) {
          mFirmVer = Integer.toString(databuf[4]) + "." + Integer.toString(databuf[5]);
          return PACKET_INFO_FIRMWARE;
        } else if (addr == DistoXBLEDetails.HARDWARE_ADDRESS) {
          float HardVer = ((float) databuf[4]) / 10;
          mHardVer = Float.toString(HardVer);
          return PACKET_INFO_HARDWARE;
        } else if (addr == DistoXBLEDetails.STATUS_ADDRESS) {
          return PACKET_STATUS;
        } else if (command == 0x3d) {
          return PACKET_REPLY;
        } else if (command == 0x3e) {
          return PACKET_WRITE_REPLY;
        // } else {
        //   return PACKET_ERROR;
        }
      } else if ( command == 0x3c ) { // FIXME signature: hardware ver. // doe snot this do the same as 0x3d/0x3e with HARDWARE_ADDRESS ?
        if ( databuf.length == 3 ) { 
          mRepliedData[0] = databuf[1];
          mRepliedData[1] = databuf[2];
          return PACKET_SIGNATURE;
        }
      } else if ( databuf[0] == 0x3B ) {
        if ( databuf.length == 5 ) {
          mCheckSum = ((databuf[4] << 8) | (databuf[3] & 0xff)) & 0xffff;
          return PACKET_FLASH_CHECKSUM;
        }
      } else if ( command == 0x3A ) {
        if ( databuf.length == 247 ) {        // firmware first packet (MTU=247)
          // mFlashFirstPacketReiceved = true;
          for ( int i=3; i<247; i++) mFlashBytes[i-3] = databuf[i]; // FIXME only 244 bytes copied ? here databuf is copied from offset 3
          return PACKET_FLASH_BYTES_1;
        } else if ( /* mFlashFirstPacketReiceved && */ databuf.length == 12 ) {   // firmware second packet
          // mFlashFirstPacketReiceved = false;
          // SHOULD THE LOOP GO FROM 3 TO 15 ????
          for ( int i=0; i<12; i++) mFlashBytes[i+244] = databuf[i]; // 244 + 12 = 256 // here databuf is copied from offset 0
          return PACKET_FLASH_BYTES_2;
        } else {
          // TDLog.Error("XBLE ...");
          return PACKET_ERROR;
        }
      }
    return PACKET_ERROR;
  }

}
