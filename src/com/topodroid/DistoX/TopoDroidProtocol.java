/* @file TopoDroidProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid TopoDroid-DistoX communication protocol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

// import java.lang.ref.WeakReference;

import java.io.IOException;

import java.util.UUID;
import java.util.List;
import java.util.Locale;
// import java.lang.reflect.Field;

// import android.os.CountDownTimer;

import android.content.Context;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothServerSocket;
// import android.bluetooth.BluetoothSocket;

class TopoDroidProtocol
{
  protected int    mDeviceType;
  protected String mDeviceAddress;
  protected PacketLogger mPacketLogger;

  protected static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );

  // protocol packet types
  static final int DISTOX_PACKET_NONE   = 0;
  static final int DISTOX_PACKET_DATA   = 1;
  static final int DISTOX_PACKET_G      = 2;
  static final int DISTOX_PACKET_M      = 3;
  static final int DISTOX_PACKET_VECTOR = 4;
  static final int DISTOX_PACKET_REPLY  = 5;

  static final int DISTOX_ERR_OK           =  0; // OK: no error
  static final int DISTOX_ERR_HEADTAIL     = -1;
  static final int DISTOX_ERR_HEADTAIL_IO  = -2;
  static final int DISTOX_ERR_HEADTAIL_EOF = -3;
  static final int DISTOX_ERR_CONNECTED    = -4;
  static final int DISTOX_ERR_OFF          = -5; // distox has turned off
  static final int DISTOX_ERR_PROTOCOL     = -6; // protocol is null

  protected int  mError; // readToRead error code
  int getErrorCode() { return mError; }

  double mDistance;
  double mBearing;
  double mClino;
  double mRoll;
  double mAcceleration; // G intensity
  double mMagnetic;     // M intensity
  double mDip;          // magnetic dip
  protected byte mRollHigh; // high byte of roll
  long mGX, mGY, mGZ;
  long mMX, mMY, mMZ;

  protected byte[] mBuffer = new byte[8]; // packet buffer
  protected byte[] mAddress;        // request-reply address
  protected byte[] mRequestBuffer;  // request buffer
  protected byte[] mReplyBuffer;    // reply data
  
  int mMaxTimeout = 8;

  // int    getType() { return mDeviceType; }
  // byte[] getAddress() { return mDeviceAddress; }

  //-----------------------------------------------------

  TopoDroidProtocol( Device device, Context context )
  {
    mDeviceType = device.mType;
    mDeviceAddress = device.mAddress;
    mPacketLogger  = new PacketLogger( context );

    // allocated buffers
    mAddress       = new byte[2];
    mReplyBuffer   = new byte[4];
    mRequestBuffer = new byte[8];
  }

  void closeIOstreams() { }

  // PACKET LOGGER ----------------------------------------------------------------
  protected void logPacket( long dir )
  {
    mPacketLogger.insertPacket(
        System.currentTimeMillis(),
        dir,
        mDeviceAddress,
        (int)(mBuffer[0] & 0x3f),
        String.format("%02x %02x %02x %02x %02x %02x %02x %02x",
          mBuffer[0], mBuffer[1], mBuffer[2], mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
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
        String.format("%02x %02x %02x", buf[0], buf[1], buf[2], mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
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
  protected int handlePacket( ) 
  {
    byte type = (byte)(mBuffer[0] & 0x3f);
    if ( TDLog.LOG_PROTO ) {
      TDLog.DoLog( "handle packet type " + type + " " + 
        String.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
        mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
    }
    if ( TDSetting.mPacketLog ) logPacket( 0L );

    // int high, low;
    switch ( type ) {
      case 0x01: // Data
        int dhh = (int)( mBuffer[0] & 0x40 );
        double d =  dhh * 1024.0 + MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        double b = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        double c = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
        // X31--ready
        mRollHigh = mBuffer[7];

        int r7 = (int)(mBuffer[7] & 0xff); if ( r7 < 0 ) r7 += 256;
        // double r = (mBuffer[7] & 0xff);
        double r = r7;

        // if ( mDeviceType == Device.DISTO_A3 || mDeviceType == Device.DISTO_X000) // FIXME VirtualDistoX
        if ( mDeviceType == Device.DISTO_A3 ) {
          mDistance = d / 1000.0;
        } else if ( mDeviceType == Device.DISTO_X310 ) {
          if ( d < 99999 ) {
            mDistance = d / 1000.0;
          } else {
            mDistance = 100 + (d-100000) / 100.0;
          }
        } else if ( mDeviceType == Device.DISTO_SAP5 ) {
          // FIXME_SAP5
        }

        mBearing  = b * 180.0 / 32768.0; // 180/0x8000;
        mClino    = c * 90.0  / 16384.0; // 90/0x4000;
        if ( c >= 32768 ) { mClino = (65536 - c) * (-90.0) / 16384.0; }
        mRoll = r * 180.0 / 128.0;

        if ( TDLog.LOG_PROTO ) {
          TDLog.DoLog( "Proto packet D " +
            String.format(Locale.US, " %7.2f %6.1f %6.1f (%6.1f)", mDistance, mBearing, mClino, mRoll ) );
        }
        // Log.v( "Proto packet D ",
        //     String.format(Locale.US, " %7.2f %6.1f %6.1f (%6.1f)", mDistance, mBearing, mClino, mRoll ) );

        return DISTOX_PACKET_DATA;
      case 0x02: // G
        mGX = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        mGY = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        mGZ = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );

        if ( mGX > TDUtil.ZERO ) mGX = mGX - TDUtil.NEG;
        if ( mGY > TDUtil.ZERO ) mGY = mGY - TDUtil.NEG;
        if ( mGZ > TDUtil.ZERO ) mGZ = mGZ - TDUtil.NEG;
        TDLog.Log( TDLog.LOG_PROTO, "Proto packet G " + String.format(" %x %x %x", mGX, mGY, mGZ ) );
        return DISTOX_PACKET_G;
      case 0x03: // M
        mMX = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        mMY = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        mMZ = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );

        if ( mMX > TDUtil.ZERO ) mMX = mMX - TDUtil.NEG;
        if ( mMY > TDUtil.ZERO ) mMY = mMY - TDUtil.NEG;
        if ( mMZ > TDUtil.ZERO ) mMZ = mMZ - TDUtil.NEG;
        TDLog.Log( TDLog.LOG_PROTO, "Proto packet M " + String.format(" %x %x %x", mMX, mMY, mMZ ) );
        return DISTOX_PACKET_M;
      case 0x04: // Vector data packet
        if ( mDeviceType == Device.DISTO_X310 ) {
          double acc = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
          double mag = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
          double dip = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
          double rh = MemoryOctet.toInt( mRollHigh, mBuffer[7] );
          mAcceleration = acc;
          mMagnetic = mag;
          mDip = dip * 90.0  / 16384.0; // 90/0x4000;
          if ( dip >= 32768 ) { mDip = (65536 - dip) * (-90.0) / 16384.0; }
          mRoll  = rh * 180.0 / 32768.0; // 180/0x8000;
          if ( TDLog.LOG_PROTO ) {
            TDLog.DoLog( "Proto packet V " +
              String.format(Locale.US, " %.2f %.2f %.2f roll %.1f", mAcceleration, mMagnetic, mDip, mRoll ) );
	  }
          // Log.v( "Proto packet V ",
          //     String.format(Locale.US, " %.2f %.2f %.2f roll %.1f", mAcceleration, mMagnetic, mDip, mRoll ) );
        }
        return DISTOX_PACKET_VECTOR;
      case 0x38:  // Reply packet
        mAddress[0] = mBuffer[1];
        mAddress[1] = mBuffer[2];
        mReplyBuffer[0] = mBuffer[3];
        mReplyBuffer[1] = mBuffer[4];
        mReplyBuffer[2] = mBuffer[5];
        mReplyBuffer[3] = mBuffer[6];
        TDLog.Log( TDLog.LOG_PROTO, "handle Packet mReplyBuffer" );
        return DISTOX_PACKET_REPLY;
      default:
        TDLog.Error( 
          "packet error. type " + type + " " + 
          String.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
          mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
      //   return DISTOX_PACKET_NONE;
    }
    return DISTOX_PACKET_NONE;
  } 

  /** swap hot bit in a data in DistoX A3 memory
   * @param addr  memory address
   */
  boolean swapHotBit( int addr ) { return false; } // only A3 - by default does not do anything

  // PACKETS I/O ------------------------------------------------------------------------

  /** try to read 8 bytes - return the number of read bytes
   * @param timeout    joining timeout
   * @param maxtimeout max number of join attempts
   * @return number of data that have been read
   * 
   * must be overridden - by default returns 0
   */
  protected int getAvailable( long timeout, int maxtimeout ) throws IOException
  {
    return 0;
  }

  // must be overridden
  int readPacket( boolean no_timeout )
  {
    return DISTOX_PACKET_NONE;
  }

  /** write a command to the out channel
   * @param cmd command code
   * @return true if success
   *
   * must be overridden - default fails
   */
  boolean sendCommand( byte cmd ) { return false; }

  /** read the number of data to download
   * @param command command to send to the DistoX
   * @param a3      whether DistoX A3
   * @return number of data to download
   *
   * must be overridden - default returns 0
   */
  int readToRead( byte[] command, boolean a3 ) { return 0; }

  /** read the memory buffer head and tail
   * @param command    head-tail command with the memory address of head-tail words
   * @param head_tail  array to store head and tail values
   * @return null on failure, string presentation on success
   */
  String readHeadTail( byte[] command, int[] head_tail ) { return null; }

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

  /** read memory at address 8000
   * @param result 4-byte array to write the read values
   * @return true if successful
   */
  boolean read8000( byte[] result ) { return false; }

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

  int readX310Memory( int start, int end, List< MemoryOctet > data ) { return 0; }

  int uploadFirmware( String filepath ) { return 0; }

  int dumpFirmware( String filepath ) { return 0; }

}
