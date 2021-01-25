/* @file DistoX310Comm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid  DistoX2 (X310) bluetooth communication 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

import com.topodroid.utils.TDLog;
import com.topodroid.packetX.MemoryOctet;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.TopoDroidApp;

import android.util.Log;

// import java.nio.ByteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

import android.os.Handler;

public class DistoX310Comm extends DistoXComm
{
  private static boolean mCalibMode = false;   //!< whether the device is in calib-mode

  public DistoX310Comm( TopoDroidApp app )
  {
    super( app );
    // TDLog.Log( TDLog.LOG_COMM, "Disto X310 Comm cstr");
  }

  /** must be overridden to call create proper protocol
   * @param in      input
   * @param out     output
   *        device  TDInstance.deviceA
   *        app     context
   */
  protected DistoXProtocol createProtocol( DataInputStream in, DataOutputStream out )
  {
    return (new DistoX310Protocol( in, out, TDInstance.deviceA, mApp ));
  }

  // -------------------------------------------------------- 
  /**
   * nothing to read (only write) --> no AsyncTask
   * @param address   remote device address
   * @param what      command to send to the remote device
   * @param lister    callback handler
   * @param data_type packet datatype 
   */
  public void setX310Laser( String address, int what, int to_read, Handler /* ILister */ lister, int data_type ) // FIXME_LISTER
  {
    if ( connectSocket( address, data_type ) ) {
      switch ( what ) {
        case Device.DISTOX_OFF:
          sendCommand( (byte)Device.DISTOX_OFF );
          break;
        case Device.LASER_ON:
          sendCommand( 0x36 );
          break;
        case Device.LASER_OFF:
          sendCommand( 0x37 );
          break;
        case Device.MEASURE:
          // sendCommand( 0x38 );
          // break;
        case Device.MEASURE_DOWNLOAD:
          sendCommand( 0x38 );
          break;
        case Device.CALIB_OFF:
          sendCommand( (byte)Device.CALIB_OFF );
          break;
        case Device.CALIB_ON:
          sendCommand( (byte)Device.CALIB_ON );
          break;
      }
      if ( mCommThread == null && to_read > 0 ) {
        // Log.v("DistoX-BLEZ", "RF comm thread start ... ");
        startCommThread( 2*to_read, lister, data_type );  // each data has two packets
        while ( mCommThread != null ) {
          TDUtil.slowDown( 100 );
        }
      // } else {
      //   Log.v("DistoX-BLEZ", "RF comm thread not null ");
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
  //   return sendCommand( turn_on? Device.CALIB_ON : Device.CALIB_OFF ); 
  // }

  /** Toggle device calibration mode
   * @param address    device address
   * @param type       device type
   * @return true if success
   */
  @Override
  public boolean toggleCalibMode( String address, int type )
  {
    if ( ! isCommThreadNull() ) {
      TDLog.Error( "toggle Calib Mode address " + address + " not null RFcomm thread" );
      return false;
    }
    boolean ret = false;
    if ( connectSocketAny( address ) ) {
      if ( mProtocol instanceof DistoX310Protocol ) {
        byte[] result = null;
        byte[] fw = mProtocol.readMemory( DeviceX310Details.FIRMWARE_ADDRESS ); // read firmware
        if ( fw == null || fw.length < 2 ) {
          TDLog.Error( "toggle Calib Mode X310 failed read E000" );
        } else {
          // TDLog.Log( TDLog.LOG_COMM, "firmware " + fw[0] + " " + fw[1] );
          if ( fw[1] >= 0 && fw[1] < DeviceX310Details.STATUS_ADDRESS.length ) {
            result = mProtocol.readMemory( DeviceX310Details.STATUS_ADDRESS[ fw[1] ] );
            if ( result == null ) { 
              mCalibMode = ! mCalibMode;
              ret = setCalibMode( mCalibMode );
              // TDLog.Log( TDLog.LOG_COMM, "toggle Calib Mode X310 " + fw[1] + " to " + mCalibMode );
            } else {
              if ( result.length >= 2 ) {
                TDLog.Log( TDLog.LOG_COMM, "toggle Calib Mode X310 " + fw[1] + " to " + mCalibMode + " res " + result[0] + " " + result[1] );
              }
              ret = setCalibMode( DeviceX310Details.isNotCalibMode( result[0] ) );
            }
          } else {
            mCalibMode = ! mCalibMode;
            ret = setCalibMode( mCalibMode );
            // TDLog.Log( TDLog.LOG_COMM, "toggle Calib Mode X310 to " + mCalibMode );
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
  //   if ( ! isCommThreadNull() ) return -1;
  //   int n = 0;
  //   if ( connectSocketAny( address ) ) {
  //     n = mProtocol.resetX310Memory( from, to );
  //   }
  //   destroySocket( );
  //   return n;
  // }

  public int readX310Memory( String address, int from, int to, List< MemoryOctet > memory )
  {
    if ( ! isCommThreadNull() ) return -1;
    int n = 0;
    if ( connectSocketAny( address ) ) {
      if ( mProtocol instanceof DistoX310Protocol ) {
        n = ((DistoX310Protocol)mProtocol).readX310Memory( from, to, memory );
        // FIXME ASYNC new CommandThread( mProtocol, READ_X310_MEMORY, memory ) Note...
      } else { 
        n= -1;
      }
    }
    destroySocket( );
    return n;
  }

  // ====================================================================================
  // FIRMWARE

  public int dumpFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocketAny( address ) ) {
      if ( mProtocol instanceof DistoX310Protocol ) {
        ret = ((DistoX310Protocol)mProtocol).dumpFirmware( filepath );
      } else {
        ret = -1;
      }
    }
    destroySocket( );
    return ret;
  }

  public int uploadFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocketAny( address ) ) {
      if ( mProtocol instanceof DistoX310Protocol ) {
        ret = ((DistoX310Protocol)mProtocol).uploadFirmware( filepath );
      } else {
        ret = -1;
      }
    }
    destroySocket( );
    return ret;
  }

}
