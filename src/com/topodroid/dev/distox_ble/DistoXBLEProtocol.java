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

  public static final int PACKET_MEASURE_DATA   = 0x20;

  public static final int PACKET_NONE           = 0;
  public static final int PACKET_ERROR          = 0x80;

  public String mFirmVer;
  public String mHardVer;
  public byte[] mRepliedData;
  public int mCheckSum;

  public byte[] mMeasureDataPacket1;
  public byte[] mMeasureDataPacket2;
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
  }

  /** process a data array
   * @param databuf  input data array
   * @return packet type
   */
  public int packetProcess( byte[] databuf )
  {
    if ( databuf.length == 8 ) {
      if(databuf[0] != 0x38 && databuf[0] != 0x39) return PACKET_ERROR;
      int addr = (databuf[2] << 8 | (databuf[1] & 0xff)) & 0xFFFF;
      mRepliedData[0] = databuf[3];
      mRepliedData[1] = databuf[4];
      mRepliedData[2] = databuf[5];
      mRepliedData[3] = databuf[6];
      if ( addr == DistoXBLEDetails.FIRMWARE_ADDRESS ) {
        mFirmVer = Integer.toString(databuf[3]) + "." + Integer.toString(databuf[4]);
        return PACKET_INFO_FIRMWARE;
      } else if ( addr == DistoXBLEDetails.HARDWARE_ADDRESS ) {
        float HardVer = ((float)databuf[3]) / 10;
        mHardVer = Float.toString(HardVer);
        return PACKET_INFO_HARDWARE;
      } else if ( addr == DistoXBLEDetails.STATUS_ADDRESS ) {
        return PACKET_STATUS;
      } else if ( databuf[0] == 0x38 ) {
        return PACKET_REPLY;
      } else if ( databuf[0] == 0x39 ) {
        return PACKET_WRITE_REPLY;
      // } else {
      //   return PACKET_ERROR;
      }
    } else if ( databuf.length == 16 ) {       //shot data
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
    } else if ( databuf.length == 5 ) {
      if ( databuf[0] == 0x3B ) {
        mCheckSum = ((databuf[4] << 8) | (databuf[3] & 0xff)) & 0xffff;
        return PACKET_FLASH_CHECKSUM;
      }
    }
    return PACKET_ERROR;
  }

}
