/* @file DistoX310Comm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid-DistoX BlueTooth communication 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

// import java.nio.ByteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
// import java.util.ArrayList;
// import java.util.concurrent.atomic.AtomicInteger;

// import android.os.Bundle;
import android.os.Handler;
// import android.os.Message;

class DistoX310Comm extends DistoXComm
{

  DistoX310Comm( TopoDroidApp app )
  {
    super( app );
  }

  // public void resume()
  // {
  //   // if ( mCommThread != null ) { mCommThread.resume(); }
  // }

  // public void suspend()
  // {
  //   // if ( mCommThread != null ) { mCommThread.suspend(); }
  // }

  /** must be overridden to call create proper protocol
   * @param in      input
   * @param out     output
   *        device  TDInstance.device
   *        app     context
   */
  protected DistoXProtocol createProtocol( DataInputStream in, DataOutputStream out )
  {
    return (new DistoX310Protocol( in, out, TDInstance.device, mApp ));
  }

  // -------------------------------------------------------- 
  /**
   * nothing to read (only write) --> no AsyncTask
   * @param address   remote device address
   * @param what      command to send to the remote device
   * @param lister    callback handler
   */
  void setX310Laser( String address, int what, int to_read, Handler /* ILister */ lister ) // FIXME_LISTER
  {
    if ( connectSocket( address ) ) {
      switch ( what ) {
        case 0: // LASER OFF
          sendCommand( 0x37 );
          break;
        case 1: // LASER ON
          sendCommand( 0x36 );
          break;
        case 2: // MEASURE
          sendCommand( 0x38 );
          break;
        case 3: // MEASURE and DOWNLAOD
          sendCommand( 0x38 );
          break;
      }
      if ( mCommThread == null && to_read > 0 ) {
        // Log.v("DistoX", "RF comm thread start ... ");
        startCommThread( 2*to_read, lister );  // each data has two packets
        while ( mCommThread != null ) {
          TDUtil.slowDown( 100 );
        }
      // } else {
      //   Log.v("DistoX-COMM", "RF comm thread not null ");
      }
    }
    destroySocket( );
  }

  /* send the set/unset calib-mode command
   * note called within connectSocket()
   * nothing to read (only write) --> no AsyncTask
   *
   * @param turn_on   whether to turn on or off the DistoX calibration mode
   * @return true if success
   */
  // private boolean setCalibMode( boolean turn_on )
  // {
  //   return sendCommand( turn_on? 0x31 : 0x30 ); 
  // }

  /** Toggle device calibration mode
   * @param address    device address
   * @param type       device type
   * @return true if success
   */
  @Override
  boolean toggleCalibMode( String address, int type )
  {
    if ( ! checkCommThreadNull() ) {
      TDLog.Error( "toggle Calib Mode address " + address + " not null RFcomm thread" );
      return false;
    }
    boolean ret = false;
    if ( connectSocket( address ) ) {
      byte[] result = null;
      byte[] fw = mProtocol.readMemory( DeviceX310Details.mFirmwareAddress ); // read firmware
      if ( fw == null ) {
        TDLog.Error( "toggle Calib Mode X310 failed read E000" );
      } else {
        // Log.v("DistoX", "firmware " + fw[0] + " " + fw[1] );
        if ( fw[1] > 5 ) {
          mCalibMode = ! mCalibMode;
          // TDLog.Log( TDLog.LOG_COMM, "toggle Calib Mode X310 setX310CalibMode " + mCalibMode );
          ret = setCalibMode( mCalibMode );
        } else {
          result = mProtocol.readMemory( DeviceX310Details.mStatusAddress[ fw[1] ] );
          if ( result == null ) { 
            TDLog.Error( "toggle Calib Mode X310 failed read status word" ); // C044
          } else {
            ret = setCalibMode( DeviceX310Details.isNotCalibMode( result[0] ) );
          }
        }
      }
    }
    destroySocket();
    return ret;
  }

  // X310 data memory is read-only
  // public int resetX310Memory( String address, int from, int to )
  // {
  //   if ( ! checkCommThreadNull() ) return -1;
  //   int n = 0;
  //   if ( connectSocket( address ) ) {
  //     n = mProtocol.resetX310Memory( from, to );
  //   }
  //   destroySocket( );
  //   return n;
  // }

  int readX310Memory( String address, int from, int to, List< MemoryOctet > memory )
  {
    if ( ! checkCommThreadNull() ) return -1;
    int n = 0;
    if ( connectSocket( address ) ) {
      DistoX310Protocol protocol = (DistoX310Protocol)mProtocol;
      n = protocol.readX310Memory( from, to, memory );
      // FIXME ASYNC new CommandThread( mProtocol, READ_X310_MEMORY, memory ) Note...
    }
    destroySocket( );
    return n;
  }

  // ====================================================================================
  // FIRMWARE

  // int readFirmwareHardware( String address )
  // {
  //   int ret = 0;
  //   if ( connectSocket( address ) ) {
  //     ret = mProtocol.readFirmwareAddress( );
  //   }
  //   destroySocket( );
  //   return ret;
  // }
    
  int dumpFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocket( address ) ) {
      DistoX310Protocol protocol = (DistoX310Protocol)mProtocol;
      ret = protocol.dumpFirmware( filepath );
    }
    destroySocket( );
    return ret;
  }

  int uploadFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocket( address ) ) {
      // TDLog.LogFile( "Firmware upload: socket is ready " );
      DistoX310Protocol protocol = (DistoX310Protocol)mProtocol;
      ret = protocol.uploadFirmware( filepath );
    }
    destroySocket( );
    return ret;
  }

}
