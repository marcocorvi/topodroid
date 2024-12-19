/* @file DeviceXBLEProtocol.java
 *
 * @author Siwei Tian
 * @date July 2024
 *
 * @brief TopoDroid Cavway data protocol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.ListerHandler;
// import com.topodroid.dev.DataType;
import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidProtocol;
import com.topodroid.packetX.MemoryOctet;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;

import android.annotation.SuppressLint;
import android.content.Context;
// import android.os.Handler;

// import java.io.IOException;

public class CavwayProtocol extends TopoDroidProtocol
{
  private final static String TAG = "CAVWAY PROTO ";
  private final static boolean LOG = false;
  private final static int DATA_LEN = 64;

  private final CavwayComm mComm;
  private final ListerHandler mLister;

  public static final float ABSSCALE            = 10000;
  public static final float ANGLESCALE          = (float)(360.0 / 0xFFFF);
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
  public int mCheckCRC;
  public int mFwOpReturnCode;    //0: OK  1: Flash Error 2: Addr or CRC error

  public byte[] mMeasureDataPacket1;
  public byte[] mMeasureDataPacket2;

  public byte[] mFlashBytes;
  //public int mPacketType;

  private byte[] mPacketBytes;

  public long mTime = 0;
  public String mComment = "";

  /** @return the shot timestamp [s]
   */
  @Override
  public long getTimeStamp() {
    return mTime; }

  /** cstr
   * @param ctx      context
   * @param app      application
   * @param lister   data lister
   * @param device   BT device
   * @param comm     DistoX BLE comm object
   */
  public CavwayProtocol(Context ctx, TopoDroidApp app, ListerHandler lister, Device device, CavwayComm comm )
  {
    super( device, ctx );
    // if ( LOG ) TDLog.v( TAG + "cstr");
    mLister = lister;
    mComm   = comm;
    mRepliedData = new byte[4];
    mFlashBytes  = new byte[256];
    mPacketBytes = new byte[DATA_LEN];
    for ( int k=0; k<DATA_LEN; ++k ) mPacketBytes[k] = (byte)0xa5;
  }

  public int handleCavwayPacket(byte [] packetdata)
  {
    // if ( LOG ) {
    //   StringBuilder sb = new StringBuilder();
    //   for ( byte b : packetdata ) sb.append( String.format(" %02x", b ) );
    //   TDLog.v( TAG + "packet " + packetdata.length + ": " + sb.toString() );
    // }

    byte flag = packetdata[1];   //leg, err flag
    double d =  (packetdata[2] << 8) + MemoryOctet.toInt( packetdata[4], packetdata[3] );
    double b = MemoryOctet.toInt( packetdata[6], packetdata[5] );  //AZM
    double c = MemoryOctet.toInt( packetdata[8], packetdata[7] );  //INCL
    double r = MemoryOctet.toInt( packetdata[10], packetdata[9] ); //ROLL

    mTime = MemoryOctet.toLong(packetdata[20],packetdata[19],packetdata[18],packetdata[17]);
    //mTime = ((long)packetdata[20] << 24 | (long)packetdata[19] << 16 | (long)packetdata[18] << 8 | (long)packetdata[17]);
    mDistance = d / 1000.0;
    mBearing  = b * 180.0 / 32768.0; // 180/0x8000;
    mClino    = c * 90.0  / 16384.0; // 90/0x4000;
    if ( c >= 32768 ) { mClino = (65536 - c) * (-90.0) / 16384.0; }
    mRoll  = r * 180.0 / 32768.0; // 180/0x8000;

    double acc = MemoryOctet.toInt( packetdata[12], packetdata[11] );
    double mag = MemoryOctet.toInt( packetdata[14], packetdata[13] );
    double dip = MemoryOctet.toInt( packetdata[16], packetdata[15] );
    mAcceleration = acc;
    mMagnetic = mag;
    mDip = dip * 90.0  / 16384.0; // 90/0x4000;
    if ( dip >= 32768 ) { mDip = (65536 - dip) * (-90.0) / 16384.0; }

    mGX = MemoryOctet.toInt( packetdata[22], packetdata[21] );
    mGY = MemoryOctet.toInt( packetdata[24], packetdata[23] );
    mGZ = MemoryOctet.toInt( packetdata[26], packetdata[25] );
    if ( mGX > TDUtil.ZERO ) mGX = mGX - TDUtil.NEG;
    if ( mGY > TDUtil.ZERO ) mGY = mGY - TDUtil.NEG;
    if ( mGZ > TDUtil.ZERO ) mGZ = mGZ - TDUtil.NEG;

    mMX = MemoryOctet.toInt( packetdata[28], packetdata[27] );
    mMY = MemoryOctet.toInt( packetdata[30], packetdata[29] );
    mMZ = MemoryOctet.toInt( packetdata[32], packetdata[31] );
    if ( mMX > TDUtil.ZERO ) mMX = mMX - TDUtil.NEG;
    if ( mMY > TDUtil.ZERO ) mMY = mMY - TDUtil.NEG;
    if ( mMZ > TDUtil.ZERO ) mMZ = mMZ - TDUtil.NEG;

    byte[] errorbytes = new byte[9];
    for (int i = 0; i < 9; i++)
      errorbytes[i] = packetdata[45 + i];
    mComment = parseErrInfo(errorbytes);

    return ( packetdata[0] == 0x01 )? DataType.PACKET_DATA 
         : ( packetdata[0] == 0x02 )? DataType.PACKET_G    //definite a new identifier? PACKET_G not suitable
         : PACKET_NONE;
  }

  @SuppressLint("DefaultLocale")
  private String parseErrInfo(byte[] errbytes)
  {
    // if ( LOG ) {
    //   StringBuilder sb = new StringBuilder();
    //   for ( byte b : errbytes ) sb.append( String.format(" %02x", b ) );
    //   TDLog.v( TAG + "parse error info - length " + errbytes.length + sb.toString() );
    // }

    //string error_type = "";
    if ( errbytes[0] == 0xFF ) {
      return "No error";
    }
    StringBuilder res = new StringBuilder();
    int err_cnt = 0;
    if ( ( (errbytes[0] >> 7) & 0x1 ) == 0x00 ) { //absG error
      if ( err_cnt == 0 ) res.append( "Error. " );
      res.append( "G: " );
      res.append( String.format("%.4f ", MemoryOctet.toInt(errbytes[4 * err_cnt + 2] ,errbytes[4 * err_cnt + 1]) / ABSSCALE) );
      res.append( String.format("%.4f ", MemoryOctet.toInt(errbytes[4 * err_cnt + 4] ,errbytes[4 * err_cnt + 3]) / ABSSCALE) );
      err_cnt++;
      // if (err_cnt == 2) return res.toString();
    }
    if ( ( (errbytes[0] >> 6) & 0x1) == 0x00 ) {  //absM error
      if ( err_cnt == 0 ) res.append( "Error. " );
      res.append( "M: " );
      res.append( String.format("%.4f ", MemoryOctet.toInt(errbytes[4 * err_cnt + 2] ,errbytes[4 * err_cnt + 1]) / ABSSCALE) );
      res.append( String.format("%.4f ", MemoryOctet.toInt(errbytes[4 * err_cnt + 4] ,errbytes[4 * err_cnt + 3]) / ABSSCALE) );
      err_cnt++;
      if (err_cnt == 2) return res.toString();
    }

    if ( ( (errbytes[0] >> 5) & 0x1 ) == 0x00 ){  //dip error
      if ( err_cnt == 0 ) res.append( "Error. " );
      float ftmp;
      res.append( "Dip: ");
      ftmp = MemoryOctet.toInt(errbytes[4 * err_cnt + 2],errbytes[4 * err_cnt + 1]) * ANGLESCALE;
      if(ftmp > 180) ftmp -= 360f;      //The 2 bytes should be negative here
      res.append( String.format("%.2f ",ftmp) );
      ftmp = MemoryOctet.toInt(errbytes[4 * err_cnt + 4],errbytes[4 * err_cnt + 3]) * ANGLESCALE;
      if(ftmp > 180) ftmp -= 360f;      //The 2 bytes should be negative here
      res.append( String.format("%.2f ", ftmp) );
      err_cnt++;
      if (err_cnt == 2) return res.toString();
    }

    if ( ( (errbytes[0] >> 4) & 0x1 ) == 0x00 ) { //angle error
      if ( err_cnt == 0 ) res.append( "Error. " );
      float ftmp;
      res.append( "Angle: ");
      ftmp = MemoryOctet.toInt(errbytes[4 * err_cnt + 2],errbytes[4 * err_cnt + 1]) * ANGLESCALE;
      res.append( String.format("%.2f ",ftmp) );
      err_cnt++;
      // if (err_cnt == 2) return res.toString();
    }
    return res.toString();
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
    // if ( LOG ) TDLog.v( TAG + "handle packet length " + databuf.length );
    if ( databuf.length == 0 ) {
      TDLog.e( TAG + "handle packet: 0-length data");
      return PACKET_NONE;
    }
    TDLog.v( TAG + " byte[0] " + String.format("%02x", databuf[0] ) );
    if ( (databuf[0] == MemoryOctet.BYTE_PACKET_DATA || databuf[0] == MemoryOctet.BYTE_PACKET_G ) && databuf.length == DATA_LEN ) { // shot / calib data
      if ( mComm.isDownloading() ) {
        for ( int kk=0; kk<DATA_LEN; ++kk ) {
          if ( mPacketBytes[kk] != databuf[kk] ) { // new packet data: send ack depends on handling packets
            System.arraycopy( databuf, 0, mPacketBytes, 0, DATA_LEN );
            int res = handleCavwayPacket(mPacketBytes);
            if ( res != PACKET_NONE) {
              mComm.sendCommand(mPacketBytes[1] | 0x55);
              mComm.handleCavwayPacket(res, mLister, 0, mComment);
              return PACKET_MEASURE_DATA; // with ( PACKET_MEASURE_DATA | databuf[0]) shots would be distinguished from calib
            } else {
              return PACKET_ERROR; // break for loop
            }
          }
        }
      } else if ( mComm.isReadingMemory() ) {
        TDLog.v( TAG + "handle memory read");
        mComm.handleOneMemory( mPacketBytes );
      } else {
        TDLog.t( TAG + "not downloading ???");
        return PACKET_NONE;
      }
    } else { // command packet
      byte command = databuf[0];
      if ( command == MemoryOctet.BYTE_PACKET_3D || command == MemoryOctet.BYTE_PACKET_3E ) { // 0x3d or 0x3e
        if ( LOG ) TDLog.v( TAG + "handle packet command " + command );
        int addr = (databuf[2] << 8 | (databuf[1] & 0xff)) & 0xFFFF;
        int len = databuf[3];
        mRepliedData = new byte[len];
        if ( LOG ) TDLog.v( TAG + "command packet " + command + " length " + len );
        for (int i = 0; i < len; i++)
          mRepliedData[i] = databuf[i + 4];
        if ( addr == CavwayDetails.FIRMWARE_ADDRESS ) {
          mFirmVer = Integer.toString(databuf[4]) + "." + Integer.toString(databuf[5]) + "." + Integer.toString(databuf[6]);
          if ( LOG )TDLog.v( TAG + "fw v. " + mFirmVer );
          return PACKET_INFO_FIRMWARE;
        } else if ( addr == CavwayDetails.HARDWARE_ADDRESS ) {
          float HardVer = ((float) databuf[4]) / 10;
          mHardVer = Float.toString(HardVer);
          if ( LOG ) TDLog.v( TAG + "hw v. " + mHardVer );
          return PACKET_INFO_HARDWARE;
        } else if ( command == MemoryOctet.BYTE_PACKET_3D ) { // 0x3d
          if ( LOG ) TDLog.v( TAG + "reply (3D)");
          return PACKET_REPLY;
        } else if ( command == MemoryOctet.BYTE_PACKET_3E ) { // 0x3e
          if ( LOG ) TDLog.v( TAG + "write reply (3E)");
          return PACKET_WRITE_REPLY;
        // } else {
        //   return PACKET_ERROR;
        }
      } else if ( command == MemoryOctet.BYTE_PACKET_HW_CODE ) { // 0x3c: signature: hardware ver. - 0x3d 0x3e only works in App mode not in the bootloader mode.
        // 0x3a 0x3b 0x3c are commands work in bootloader mode
        if ( LOG ) TDLog.v( TAG + "hw code (3C) (signature) length " + databuf.length );
        if ( databuf.length == 3 ) { 
          mRepliedData[0] = databuf[1];
          mRepliedData[1] = databuf[2];
          return PACKET_SIGNATURE;
        }
      } else if ( databuf[0] == MemoryOctet.BYTE_PACKET_FW_WRITE ) { // 0x3b
        if ( LOG ) TDLog.v( TAG + "fw write (3B) (checksum) length " + databuf.length );
        if ( databuf.length == 6 ) {
          mFwOpReturnCode = databuf[3];
          mCheckCRC = ((databuf[5] << 8) | (databuf[4] & 0xff)) & 0xffff;
          return PACKET_FLASH_CHECKSUM;
        }
      } else if ( command == MemoryOctet.BYTE_PACKET_FW_READ && (databuf.length == 131 || databuf.length == 133)) {   // 0x3a: 3 headers + 128 payloadsda
        if ( LOG) TDLog.v( TAG + "fw read (3B) databuffer length " + databuf.length );
        if ( databuf[2] == 0x00 ) {        // firmware first packet (MTU=247)
          for ( int i=3; i<131; i++) mFlashBytes[i-3] = databuf[i]; // databuf is copied from offset 3
          return PACKET_FLASH_BYTES_1;
        } else if ( databuf[2] == 0x01 && databuf.length == 133) {   // firmware second packet, with 2 bytes CRC at the end
          for ( int i=3; i<131; i++) mFlashBytes[i+128-3] = databuf[i];
          mCheckCRC = ((databuf[132] << 8) | (databuf[131] & 0xff)) & 0xffff;
          return PACKET_FLASH_BYTES_2;
        // } else {
        //   // TDLog.t("Cavway ...");
        //   return PACKET_ERROR;
        }
      }
    }
    return PACKET_ERROR;
  }

}
