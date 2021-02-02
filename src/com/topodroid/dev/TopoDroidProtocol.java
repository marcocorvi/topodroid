/* @file TopoDroidProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid TopoDroid communication protocol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.packetX.MemoryOctet;
import com.topodroid.packetX.PacketLogger;
import com.topodroid.DistoX.TDUtil;

import android.util.Log;

// import java.lang.ref.WeakReference;

// import java.io.IOException;

import java.util.UUID;
import java.util.List;
import java.util.Locale;
// import java.lang.reflect.Field;

// import android.os.CountDownTimer;

import android.content.Context;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothServerSocket;
// import android.bluetooth.BluetoothSocket;

public class TopoDroidProtocol
{
  protected int    mDeviceType;
  protected String mDeviceAddress;
  protected PacketLogger mPacketLogger;

  protected static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );


  static final int DISTOX_ERR_OK           =  0; // OK: no error
  static final int DISTOX_ERR_HEADTAIL     = -1;
  static final int DISTOX_ERR_HEADTAIL_IO  = -2;
  static final int DISTOX_ERR_HEADTAIL_EOF = -3;
  static final int DISTOX_ERR_CONNECTED    = -4;
  static final int DISTOX_ERR_OFF          = -5; // distox has turned off
  static final int DISTOX_ERR_PROTOCOL     = -6; // protocol is null

  protected int  mError; // readToRead error code
  int getErrorCode() { return mError; }

  public double mDistance;
  public double mBearing;
  public double mClino;
  public double mRoll;
  public double mAcceleration; // G intensity
  public double mMagnetic;     // M intensity
  public double mDip;          // magnetic dip
  public byte mRollHigh; // high byte of roll
  public long mGX, mGY, mGZ;
  public long mMX, mMY, mMZ;
  public boolean mBackshot;

  // protected byte[] mBuffer;
  protected byte[] mAddress;        // request-reply address
  // protected byte[] mRequest_Buffer;  // request buffer
  // private byte[] mReplyBuffer;    // reply data
  
  int mMaxTimeout = 8;

  // int    getType() { return mDeviceType; }
  // byte[] getAddress() { return mDeviceAddress; }

  //-----------------------------------------------------

  protected TopoDroidProtocol( Device device, Context context )
  {
    // Log.v("DistoX-BLE-TP", "topodroid proto: type " + device.mType + " addr " + device.mAddress );
    mDeviceType    = device.mType;
    mDeviceAddress = device.mAddress;
    mPacketLogger  = new PacketLogger( context );

    // allocated buffers
    // mBuffer        = new byte[8];
    mAddress       = new byte[2];
    // mReplyBuffer   = new byte[4];
    // mRequest_Buffer = new byte[8];
  }

  void closeIOstreams() { }

  // PACKET LOGGER ----------------------------------------------------------------
  protected void logPacket( long dir, byte[] buf )
  {
    mPacketLogger.insertPacket(
        System.currentTimeMillis(),
        dir,
        mDeviceAddress,
        (int)(buf[0] & 0x3f),
        String.format("%02x %02x %02x %02x %02x %02x %02x %02x", buf[0], buf[1], buf[2], buf[3], buf[4], buf[5], buf[6], buf[7] ) );
  }

  protected void logPacket1( long dir, byte[] buf )
  {
    mPacketLogger.insertPacket(
        System.currentTimeMillis(),
        dir,
        mDeviceAddress,
        (int)(buf[0] & 0x3f),
        String.format("%02x", buf[0] ) );
  }

  protected void logPacket3( long dir, byte[] buf )
  {
    mPacketLogger.insertPacket(
        System.currentTimeMillis(),
        dir,
        mDeviceAddress,
        (int)(buf[0] & 0x3f),
        String.format("%02x %02x %02x", buf[0], buf[1], buf[2] ) );
  }

  protected void logPacket7( long dir, byte[] buf )
  {
    mPacketLogger.insertPacket(
        System.currentTimeMillis(),
        dir,
        mDeviceAddress,
        (int)(buf[0] & 0x3f),
        String.format("%02x %02x %02x %02x %02x %02x %02x", buf[0], buf[1], buf[2], buf[3], buf[4], buf[5], buf[6] ) );
  }

  protected void logPacket8( long dir, byte[] buf )
  {
    mPacketLogger.insertPacket(
        System.currentTimeMillis(),
        dir,
        mDeviceAddress,
        (int)(buf[0] & 0x3f),
        String.format("%02x %02x %02x %02x %02x %02x %02x %02x", buf[0], buf[1], buf[2], buf[3], buf[4], buf[5], buf[6], buf[7] ) );
  }

  // PACKETS HANDLING -----------------------------------------------------------

  /** packet dispatcher
   */
  protected int handlePacket( byte[] buffer )
  {
    if ( TDSetting.mPacketLog ) logPacket( 0L, buffer );
    byte type = (byte)(buffer[0] & 0x3f);
    // if ( TDLog.LOG_PROTO ) {
    //   TDLog.DoLog( "handle packet type " + type + " " + 
    //     String.format("%02x %02x %02x %02x %02x %02x %02x %02x", buffer[0], buffer[1], buffer[2],
    //     buffer[3], buffer[4], buffer[5], buffer[6], buffer[7] ) );
    // }
    // Log.v( "DistoX-BLE-TP", "handle packet type " + type + " " + 
    //     String.format("%02x %02x %02x %02x %02x %02x %02x %02x", buffer[0], buffer[1], buffer[2],
    //     buffer[3], buffer[4], buffer[5], buffer[6], buffer[7] ) );

    // int high, low;
    switch ( type ) {
      case 0x01: // Data
        mBackshot = false;
        int dhh = (int)( buffer[0] & 0x40 );
        double d =  dhh * 1024.0 + MemoryOctet.toInt( buffer[2], buffer[1] );
        double b = MemoryOctet.toInt( buffer[4], buffer[3] );
        double c = MemoryOctet.toInt( buffer[6], buffer[5] );
        // X31--ready
        mRollHigh = buffer[7];

        int r7 = (int)(buffer[7] & 0xff); if ( r7 < 0 ) r7 += 256;
        // double r = (buffer[7] & 0xff);
        double r = r7;

        // if ( mDeviceType == Device.DISTO_A3 || mDeviceType == Device.DISTO_X000) // FIXME VirtualDistoX
        switch ( mDeviceType ) {
          case Device.DISTO_A3:
            mDistance = d / 1000.0;
            break;
          case Device.DISTO_X310:
            if ( d < 99999 ) {
              mDistance = d / 1000.0;
            } else {
              mDistance = 100 + (d-100000) / 100.0;
            }
            break;
          case Device.DISTO_BRIC4: 
            Log.v("DistoX-BLE-TP", "did not handle packet BLE");
            break;
          case Device.DISTO_SAP5: 
            // Log.v("DistoX-BLE-TP", "handle packet SAP");
            mDistance = d / 1000.0;
            break;
          // case Device.DISTO_BLEX: // FIXME BLEX
          //   // Log.v("DistoX-BLE-TP", "did not handle packet BLE");
          //   break;
          default:
            mDistance = d / 1000.0;
            break;
        }

        mBearing  = b * 180.0 / 32768.0; // 180/0x8000;
        mClino    = c * 90.0  / 16384.0; // 90/0x4000;
        if ( c >= 32768 ) { mClino = (65536 - c) * (-90.0) / 16384.0; }
        mRoll = r * 180.0 / 128.0;

        // if ( TDLog.LOG_PROTO ) {
        //   TDLog.DoLog( "Proto packet D " +
        //     String.format(Locale.US, " %7.2f %6.1f %6.1f (%6.1f)", mDistance, mBearing, mClino, mRoll ) );
        // }
        // Log.v( "DistoX-BLE-TP", String.format(Locale.US, "Packet-D %7.2f %6.1f %6.1f (%6.1f)", mDistance, mBearing, mClino, mRoll ) );
        return DataType.PACKET_DATA;
      case 0x02: // G
        mGX = MemoryOctet.toInt( buffer[2], buffer[1] );
        mGY = MemoryOctet.toInt( buffer[4], buffer[3] );
        mGZ = MemoryOctet.toInt( buffer[6], buffer[5] );

        if ( mGX > TDUtil.ZERO ) mGX = mGX - TDUtil.NEG;
        if ( mGY > TDUtil.ZERO ) mGY = mGY - TDUtil.NEG;
        if ( mGZ > TDUtil.ZERO ) mGZ = mGZ - TDUtil.NEG;
        // TDLog.Log( TDLog.LOG_PROTO, "Proto packet G " + String.format(" %x %x %x", mGX, mGY, mGZ ) );
        return DataType.PACKET_G;
      case 0x03: // M
        mMX = MemoryOctet.toInt( buffer[2], buffer[1] );
        mMY = MemoryOctet.toInt( buffer[4], buffer[3] );
        mMZ = MemoryOctet.toInt( buffer[6], buffer[5] );

        if ( mMX > TDUtil.ZERO ) mMX = mMX - TDUtil.NEG;
        if ( mMY > TDUtil.ZERO ) mMY = mMY - TDUtil.NEG;
        if ( mMZ > TDUtil.ZERO ) mMZ = mMZ - TDUtil.NEG;
        // TDLog.Log( TDLog.LOG_PROTO, "Proto packet M " + String.format(" %x %x %x", mMX, mMY, mMZ ) );
        return DataType.PACKET_M;
      case 0x04: // Vector data packet
        if ( mDeviceType == Device.DISTO_X310 ) {
          mBackshot = ( (buffer[0] & 0x40) == 0x40 );
          double acc = MemoryOctet.toInt( buffer[2], buffer[1] );
          double mag = MemoryOctet.toInt( buffer[4], buffer[3] );
          double dip = MemoryOctet.toInt( buffer[6], buffer[5] );
          double rh = MemoryOctet.toInt( mRollHigh, buffer[7] );
          mAcceleration = acc;
          mMagnetic = mag;
          mDip = dip * 90.0  / 16384.0; // 90/0x4000;
          if ( dip >= 32768 ) { mDip = (65536 - dip) * (-90.0) / 16384.0; }
          mRoll  = rh * 180.0 / 32768.0; // 180/0x8000;
          // if ( TDLog.LOG_PROTO ) {
          //   TDLog.DoLog( "Proto packet V " +
          //     String.format(Locale.US, " %.2f %.2f %.2f roll %.1f", mAcceleration, mMagnetic, mDip, mRoll ) );
	  // }
          // Log.v( "Proto packet V ",
          //     String.format(Locale.US, " %.2f %.2f %.2f roll %.1f", mAcceleration, mMagnetic, mDip, mRoll ) );
        }
        return DataType.PACKET_VECTOR;
      case 0x38:  // Reply packet
        mAddress[0] = buffer[1];
        mAddress[1] = buffer[2];
        {
          byte[] mReplyBuffer = new byte[4];
          mReplyBuffer[0] = buffer[3];
          mReplyBuffer[1] = buffer[4];
          mReplyBuffer[2] = buffer[5];
          mReplyBuffer[3] = buffer[6];
          // TDLog.Log( TDLog.LOG_PROTO, "handle Packet mReplyBuffer" );
          // TODO
        }
        return DataType.PACKET_REPLY;
      default:
        TDLog.Error( 
          "packet error. type " + type + " " + 
          String.format("%02x %02x %02x %02x %02x %02x %02x %02x", buffer[0], buffer[1], buffer[2],
          buffer[3], buffer[4], buffer[5], buffer[6], buffer[7] ) );
      //   return DataType.PACKET_NONE;
    }
    return DataType.PACKET_NONE;
  } 


  // PACKETS I/O ------------------------------------------------------------------------

  // must be overridden
  // @param no_timeout
  // @param data_type  packet data type (to filter packet of different type)
  int readPacket( boolean no_timeout, int data_type )
  {
    Log.v("DistoX-BLE-TP", "TD proto read_packet returns NONE");
    return DataType.PACKET_NONE;
  }

  /** write a command to the out channel
   * @param cmd command code
   * @return true if success
   *
   * must be overridden - default fails
   */
  public boolean sendCommand( byte cmd ) { return false; }

  /** read the number of data to download
   * @param command command to send to the DistoX
   * @return number of data to download
   *
   * must be overridden - default returns 0
   */
  int readToRead( byte[] command ) { return 0; }

  /** read 4 bytes at a memory location
   * @param addr    memory address to read
   * @return 4-byte array with memory data
   */
  byte[] readMemory( int addr ) { return null; }

  /** read memory data between two addresses
   * @param start    start address (inclusive)
   * @param end      end address (excluded)
   * @param data     array-list of MemoryOctet to fill with read data
   * @return the number of read octets 
   */
  int readMemory( int start, int end, List< MemoryOctet > data ) { return 0; }

  /** write calibration coeffs to the DistoX
   * @param calib    array of 48 (52) bytes with the coefficients
   * @return true if successful
   */
  boolean writeCalibration( byte[] calib ) { return false; }

  /** read calibration coeffs from the DistoX
   * @param calib    array of 48 (52) bytes to store the coefficients
   * @return true if successful
   *
   * called only by DistoXComm.readCoeff (TopoDroidComm.readCoeff)
   */
  boolean readCalibration( byte[] calib ) { return false; }

}
