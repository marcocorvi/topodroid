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
package com.topodroid.dev.distox2;

import com.topodroid.utils.TDLog;
import com.topodroid.packetX.MemoryOctet;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.dev.Device;
import com.topodroid.dev.distox.DistoX;
import com.topodroid.dev.distox.DistoXComm;
import com.topodroid.dev.distox.DistoXProtocol;

import android.util.Log;

// import java.nio.ByteBuffer;

import java.io.File;
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
  public DistoXProtocol createProtocol( DataInputStream in, DataOutputStream out )
  {
    return (new DistoX310Protocol( in, out, TDInstance.getDeviceA(), mApp ));
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
        case DistoX.DISTOX_OFF:
          sendCommand( (byte)DistoX.DISTOX_OFF );
          break;
        case Device.LASER_ON:
          sendCommand( (byte)DistoX.LASER_ON );
          break;
        case Device.LASER_OFF:
          sendCommand( (byte)DistoX.LASER_OFF );
          break;
        case Device.MEASURE:
          // sendCommand( (byte)DistoX.MEASURE );
          // break;
        case Device.MEASURE_DOWNLOAD:
          sendCommand( (byte)DistoX.MEASURE );
          break;
        case DistoX.CALIB_OFF:
          sendCommand( (byte)DistoX.CALIB_OFF );
          break;
        case DistoX.CALIB_ON:
          sendCommand( (byte)DistoX.CALIB_ON );
          break;
      }
      if ( mCommThread == null && to_read > 0 ) {
        // Log.v("DistoX-BLE", "DistoX310 comm: RF comm thread start ... ");
        startCommThread( 2*to_read, lister, data_type );  // each data has two packets
        while ( mCommThread != null ) {
          TDUtil.slowDown( 100 );
        }
      // } else {
      //   TDLog.Log( TDLog.LOG_BT, "DistoX310 comm: RF comm thread not null ");
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

  // @param expected device hardware
  public byte[] readFirmwareSignature( String address, int hw )
  {
    byte[] ret = null;
    int blk = ( hw == FirmwareUtils.HW_HEEB )? 8
            : ( hw == FirmwareUtils.HW_LANDOLT )? 16
            : 0;
    if ( blk == 0 ) return null;
    if ( connectSocketAny( address ) ) {
      if ( mProtocol instanceof DistoX310Protocol ) {
       TDLog.Log( TDLog.LOG_BT, "comm firmware signature hw " + hw);
        ret = ((DistoX310Protocol)mProtocol).readFirmwareBlock( blk );
      }
    }
    destroySocket( );
    return ret;
  }

  // public int dumpFirmware( String address, String filepath )
  public int dumpFirmware( String address, File file )
  {
    Log.v("DistoX-FW", "Comm dump firmware " + file.getPath() );
    int ret = 0;
    if ( connectSocketAny( address ) ) {
      if ( mProtocol instanceof DistoX310Protocol ) {
        // ret = ((DistoX310Protocol)mProtocol).dumpFirmware( filepath );
        ret = ((DistoX310Protocol)mProtocol).dumpFirmware( file );
      } else {
        ret = -1;
      }
    }
    destroySocket( );
    return ret;
  }

  final static private boolean DRY_RUN = false;

  // public int uploadFirmware( String address, String filepath )
  public int uploadFirmware( String address, File file )
  {
    Log.v("DistoX-FW", "Comm upload firmware " + file.getPath() );
    int ret = 0;
    if ( connectSocketAny( address ) ) {
      if ( mProtocol instanceof DistoX310Protocol ) {
        // TODO check that the signature of the current firmware hw-agree with that of the file firmware
        // byte[] signature = ((DistoX310Protocol)mProtocol).readFirmwareBlock( 8 ); 
        // if ( FirmwareUtil.checkSignature( signature, filepath ) ) {
        //   ret = ((DistoX310Protocol)mProtocol).uploadFirmware( filepath );
        // } else { 
        //   ret = -1;
        // }

        // FIXME DRY_RUN
        if ( DRY_RUN ) {
          ret = ((DistoX310Protocol)mProtocol).uploadFirmwareDryRun( file );
          Log.v("DistoX-FW", "Comm Firmware upoad dry run: " + ret );
        } else {
          // ret = ((DistoX310Protocol)mProtocol).uploadFirmware( filepath );
          ret = ((DistoX310Protocol)mProtocol).uploadFirmware( file );
          Log.v("DistoX-FW", "Comm Firmware upoad: " + ret );
        }
      } else {
        ret = -1;
      }
    } else {
      TDLog.Log( TDLog.LOG_BT, "Comm Firmware upoad socket failure");
    }
    destroySocket( );
    return ret;
  }

}
