/* @file TopoDroidComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid bluetooth communication 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDStatus;
// import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDUtil;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.Lister;
import com.topodroid.TDX.ListerHandler;
// import com.topodroid.TDX.DBlock;
import com.topodroid.common.ExtendType;
import com.topodroid.common.LegType;
// import com.topodroid.dev.distox.DistoX;

import java.util.UUID;
// import java.util.List;
// import java.util.concurrent.atomic.AtomicInteger; // FIXME_ATOMIC_INT

import android.os.Bundle;
// import android.os.Handler;
import android.os.Message;

// import android.content.Context;
// import android.content.Intent;
// import android.content.IntentFilter;
// import android.content.BroadcastReceiver;

public class TopoDroidComm
{
  public static final int COMM_RFCOMM = 0;
  public static final int COMM_GATT   = 1;

  public static final String SERVICE_STRING = "00001101-0000-1000-8000-00805F9B34FB";
  public static final UUID   SERVICE_UUID = UUID.fromString( SERVICE_STRING );

  public TopoDroidApp mApp;
  public String mAddress;
  public TopoDroidProtocol mProtocol;

  public boolean mBTConnected;

  public CommThread mCommThread = null;

  public byte[] mCoeff;

  boolean mHasG = false; // whether the last received packet was G type
  long mLastShotId;   // last shot id

  // private AtomicInteger mNrReadPackets; // FIXME_ATOMIC_INT
  protected volatile int mNrReadPackets;


// -----------------------------------------------------------

  // int getNrPacketsRead() { return ( mNrReadPackets == null )? 0 : mNrReadPackets.get(); } // FIXME_ATOMIC_INT 

  /** @return the number of read packets
   */
  public int  getNrReadPackets() { return mNrReadPackets; }

  /** set the number of read packets
   * @param nr   number of read packets
   */
  public void setNrReadPackets( int nr ) { mNrReadPackets = nr; }

  // /** increment number of read packets - FIXME NON_ATOMIC_ON_VOLATILE
  //  */
  // protected void incrementNrReadPackets() { ++mNrReadPackets; }

  // void resetNrReadPackets() { mNrReadPackets = 0; }

  /** @return true if the BT is connected
   */
  public boolean isConnected() { return mBTConnected; }

  /** handle a BRIC packet
   * @param index      bric shot-index
   * @param lister     data lister
   * @param data_type bric datatype (0: normal, 1: scan )
   * @param clino_error    error between clino readings
   * @param azimuth_error  error between azimuth readings
   * @param comment        device data comment (?)
   * @return ...
   */
  public boolean handleBricPacket( long index, ListerHandler lister, int data_type, float clino_error, float azimuth_error, String comment )
  {
    // TDLog.v( "TD comm: packet DATA");
    ++mNrReadPackets; // FIXME NON_ATOMIC_ON_VOLATILE incrementNrPacketsRead();
    double d = mProtocol.mDistance;
    double b = mProtocol.mBearing;
    double c = mProtocol.mClino;
    double r = mProtocol.mRoll;
    double dip  = mProtocol.mDip;
    long status = ( d > TDSetting.mMaxShotLength )? TDStatus.OVERSHOOT : TDStatus.NORMAL;
    long time   = mProtocol.getTimeStamp(); // BRIC time of the shot [seconds]
    // TODO split the data insert in three places: one for each data packet

    TDLog.v( "TD comm: HANDLE PACKET " + index + " " + d + " " + b + " " + c + " time " + time );
    int leg = ( data_type == DataType.DATA_SCAN )? LegType.SCAN : LegType.NORMAL;
    if ( comment == null ) comment = "";
    long id = TopoDroidApp.mData.insertBricShot( TDInstance.sid, /* index, */ d, b, c, r, clino_error, azimuth_error, dip, ExtendType.EXTEND_IGNORE, leg, status, comment, TDInstance.deviceAddress(), index, time );
    // TopoDroidApp.mData.updateShotAMDR( mLastShotId, TDInstance.sid, clino_error, azimuth_error, dip, r, false );
    // if ( comment != null ) TopoDroidApp.mData.updateShotComment( mLastShotId, TDInstance.sid, comment );

    if ( id == -1L ) {
      TDLog.e("Handle BRIC packet failure");
      return false;
    }

    mLastShotId = id;
    if ( lister != null ) { // FIXME_LISTER sendMessage with mLastShotId only
      Message msg = lister.obtainMessage( Lister.LIST_UPDATE );
      Bundle bundle = new Bundle();
      bundle.putLong( Lister.BLOCK_ID, mLastShotId );
      msg.setData(bundle);
      lister.sendMessage(msg);
      if ( TDInstance.deviceType() == Device.DISTO_A3 && TDSetting.mWaitData > 10 ) {
        TDUtil.slowDown( TDSetting.mWaitData );
      }
    } else {
      TDLog.e( "TD comm: null Lister");
    }
    return true;
  }

  // /** handle zero packet
  //  * @param index      ???
  //  * @param lister     data lister
  //  * @param data_type  packet expected data type (unused)
  //  */
  // public void handleZeroPacket( long index, ListerHandler lister, int data_type )
  // {
  //   ++mNrReadPackets; // FIXME NON_ATOMIC_ON_VOLATILE incrementNrPacketsRead();
  //   TDLog.v( "TD comm: packet ZERO " + mNrReadPackets );
  //   double r = mProtocol.mRoll;
  //   long status = TDStatus.NORMAL;
  //   mLastShotId = TopoDroidApp.mData.insertDistoXShot( TDInstance.sid, index, 0, 0, 0, r, ExtendType.EXTEND_IGNORE, status, TDInstance.deviceAddress(), index );
  //   if ( lister != null ) { // FIXME_LISTER sendMessage with mLastShotId only
  //     Message msg = lister.obtainMessage( Lister.LIST_UPDATE );
  //     Bundle bundle = new Bundle();
  //     bundle.putLong( Lister.BLOCK_ID, mLastShotId );
  //     msg.setData(bundle);
  //     lister.sendMessage(msg);
  //     if ( TDInstance.deviceType() == Device.DISTO_A3 && TDSetting.mWaitData > 10 ) {
  //       TDUtil.slowDown( TDSetting.mWaitData );
  //     }
  //   } else {
  //     TDLog.e( "TD comm: null Lister");
  //   }
  // }

  /** handle regular packet
   * @param res    packet type (as returned by handlePacket / or set by Protocol )
   * @param lister data lister
   * @param data_type unused
   */
  public void handleRegularPacket( int res, ListerHandler lister, int data_type )
  {
    if ( res == DataType.PACKET_DATA ) {
      ++mNrReadPackets; // FIXME NON_ATOMIC_ON_VOLATILE incrementNrPacketsRead();
      // TDLog.v( "TD comm: packet DATA " + mNrReadPackets );
      double d = mProtocol.mDistance;
      double b = mProtocol.mBearing;
      double c = mProtocol.mClino;
      double r = mProtocol.mRoll;
      // extend is unset to start
      // long extend = TDAzimuth.computeLegExtend( b ); // ExtendType.EXTEND_UNSET; FIXME_EXTEND 
      // TDLog.Log( TDLog.LOG_COMM, "Comm D PACKET " + d + " " + b + " " + c );
      // TDLog.v( "TD comm: D PACKET " + d + " " + b + " " + c );
      // NOTE type=0 shot is DistoX-type
      long status = ( d > TDSetting.mMaxShotLength )? TDStatus.OVERSHOOT : TDStatus.NORMAL;
      mLastShotId = TopoDroidApp.mData.insertDistoXShot( TDInstance.sid, -1L, d, b, c, r, ExtendType.EXTEND_IGNORE, status, TDInstance.deviceAddress(), 0 );
      if ( lister != null ) { // FIXME_LISTER sendMessage with mLastShotId only
        Message msg = lister.obtainMessage( Lister.LIST_UPDATE );
        Bundle bundle = new Bundle();
        bundle.putLong( Lister.BLOCK_ID, mLastShotId );
        msg.setData(bundle);
        lister.sendMessage(msg);
        if ( TDInstance.deviceType() == Device.DISTO_A3 && TDSetting.mWaitData > 10 ) {
          TDUtil.slowDown( TDSetting.mWaitData );
        }
      } else {
        TDLog.e( "TD comm: null Lister");
      }
      // if ( lister != null ) {
      //   DBlock blk = new DBlock( );
      //   blk.setId( mLastShotId, TDInstance.sid );
      //   blk.mLength  = (float)d;
      //   blk.mBearing = (float)b;
      //   blk.mClino   = (float)c;
      //   blk.mRoll    = (float)r;
      //   lister.updateBlockList( blk );
      // }
    } else if ( res == DataType.PACKET_G ) {
      ++mNrReadPackets; // FIXME NON_ATOMIC_ON_VOLATILE incrementNrPacketsRead();
      TDLog.v( "TD comm: packet G " + mNrReadPackets );
      setHasG( true );
    } else if ( res == DataType.PACKET_M ) {
      ++mNrReadPackets; // FIXME NON_ATOMIC_ON_VOLATILE incrementNrPacketsRead();
      TDLog.v( "TD comm: packet M " + mNrReadPackets );
      // get G and M from mProtocol and save them to store
      // TDLog.v( "G " + mProtocol.mGX + " " + mProtocol.mGY + " " + mProtocol.mGZ + " M " + mProtocol.mMX + " " + mProtocol.mMY + " " + mProtocol.mMZ );
      if ( ! lister.hasDialog() ) {
        long c_blk = TopoDroidApp.mDData.insertGM( TDInstance.cid, mProtocol.mGX, mProtocol.mGY, mProtocol.mGZ, mProtocol.mMX, mProtocol.mMY, mProtocol.mMZ );
        if ( lister != null ) {
          Message msg = lister.obtainMessage( Lister.LIST_UPDATE );
          Bundle bundle = new Bundle();
          bundle.putLong( Lister.BLOCK_ID, c_blk );
          msg.setData(bundle);
          lister.sendMessage(msg);
        }
      } else { // AUTO-CALIB
        TDLog.v("AutoCalib CID " + TDInstance.cid + " has-G " + mHasG );
        if ( mHasG ) {
          Message msg = lister.obtainMessage( Lister.LIST_AUTO_CALIB );
          Bundle bundle = new Bundle();
          bundle.putLong( "GX", mProtocol.mGX );
          bundle.putLong( "GY", mProtocol.mGY );
          bundle.putLong( "GZ", mProtocol.mGZ );
          bundle.putLong( "MX", mProtocol.mMX );
          bundle.putLong( "MY", mProtocol.mMY );
          bundle.putLong( "MZ", mProtocol.mMZ );
          msg.setData(bundle);
          lister.sendMessage(msg);
        }
      }
      if ( ! mHasG ) {
        TDLog.e( "data without G packet " + mNrReadPackets /* getNrReadPackets() */ );
        // if ( TopoDroidApp.mActivity != null ) { // skip toast
        //   TopoDroidApp.mActivity.runOnUiThread( new Runnable() {
        //     public void run() {
        //       TDToast.makeBG("data without G: " + mNrReadPackets /* getNrReadPackets() */, TDColor.FIXED_RED );
        //     }
        //   } );
        // }
      }
      setHasG( false );
      
    } else if ( res == DataType.PACKET_REPLY ) {
      // TDLog.v( "TD comm: packet REPLY");
      // TODO handle packet reply
      //
      // byte[] addr = mProtocol.getAddress();
      // byte[] reply = mProtocol.getReply();
      // String result = String.format("%02x %02x %02x %02x at %02x%02x",
      //   reply[0], reply[1], reply[2], reply[3], addr[1], addr[0] );
      // TDLog.Log( TDLog.LOG_DISTOX, "REPLY PACKET: " + result ); 

      // if ( addr[0] == (byte)0x00 && addr[1] == (byte)0x80 ) { // 0x8000
      //   // TDLog.Log( TDLog.LOG_DISTOX, "toggle reply" );
      //   // if ( (reply[0] & CALIB_BIT) == 0 ) {
      //   //     mProtocol.sendCommand( (byte)DistoX.CALIB_ON );
      //   // } else {
      //   //     mProtocol.sendCommand( (byte)DistoX.CALIB_OFF );
      //   // }
      // } else if ( ( addr[1] & (byte)0x80) == (byte)0x80 ) { // REPLY TO READ/WRITE-CALIBs
      //   // TDLog.Log( TDLog.LOG_DISTOX, "write reply" );
      //   // mProtocol.setWrittenCalib( true );
      // } else if ( addr[0] == 0x20 && addr[1] == (byte)0xC0 ) { // C020 READ HEAD-TAIL
      //   // TDLog.Log( TDLog.LOG_DISTOX, "read head-tail reply");
      //   // mHead = (int)( reply[0] | ( (int)(reply[1]) << 8 ) );
      //   // mTail = (int)( reply[2] | ( (int)(reply[3]) << 8 ) );
      // }
    } else if ( res == DataType.PACKET_VECTOR ) {
      // vector packet do count
      ++mNrReadPackets; // FIXME NON_ATOMIC_ON_VOLATILE
      // TDLog.v( "TD comm: packet VECTOR " + mNrReadPackets );
      double acc  = mProtocol.mAcceleration;
      double mag  = mProtocol.mMagnetic;
      double dip  = mProtocol.mDip;
      double roll = mProtocol.mRoll;
      boolean backshot = mProtocol.mBackshot;
      // TDLog.Log( TDLog.LOG_COMM, "Comm V PACKET " + mLastShotId + " " + acc + " " + mag + " " + dip + " " + roll );
      if ( TDInstance.deviceType() == Device.DISTO_X310 || TDInstance.deviceType() == Device.DISTO_XBLE ) {
        TopoDroidApp.mData.updateShotAMDR( mLastShotId, TDInstance.sid, acc, mag, dip, roll, backshot );
        if ( TDSetting.mWaitData > 10 ) {
          TDUtil.slowDown( TDSetting.mWaitData );
        }
      }
    } else {
      TDLog.e("DistoX packet UNKNOWN");
    }
  }

  /** @return true if the Comm thread is null
   */
  public boolean isCommThreadNull( ) { return ( mCommThread == null ); }

  /** set the Comm thread to null
   */
  void doneCommThread() { mCommThread = null; }

  /** set whether the data has G shot
   * @param has_g  whether the data has G shot
   */
  public void setHasG( boolean has_g ) { mHasG = has_g; }

  /** cstr
   * @param app  TopoDroid app
   */
  public TopoDroidComm( TopoDroidApp app )
  {
    mApp          = app;
    mProtocol     = null;
    mAddress      = null;
    // mCommThread   = null;
    mBTConnected  = false;
    // TDLog.Log( TDLog.LOG_COMM, "TopoDroid Comm cstr");
  }

  /** dstr
   * terminate the Comm - by default cancel the Comm thread
   */
  public void terminate()
  {
    // TDLog.v("TD comm terminate");
    cancelCommThread();
  }

  /** resume work - nothing for default
   */
  public void resume()
  {
    // if ( mCommThread != null ) { mCommThread.resume(); }
  }

  /** suspend work - nothing for default
   */
  public void suspend()
  {
    // if ( mCommThread != null ) { mCommThread.suspend(); }
  }

  /** cancel the Comm thread
   */
  protected void cancelCommThread()
  {
    // TDLog.v( "TD comm cancel Comm thread");
    if ( mCommThread != null ) { // FIXME check that comm-thread is really alive
      // TDLog.Log( TDLog.LOG_COMM, "cancel Comm thread: thread is active");
      mCommThread.cancelWork();
      try {
        mCommThread.join();
      } catch ( InterruptedException e ) {
        // TDLog.e( "cancel thread interrupt " + e.getMessage() );
      } finally {
        // TDLog.Log( TDLog.LOG_COMM, "cancel Comm thread: nulling thread");
        mCommThread = null;
      }
    // } else {
      // TDLog.Log( TDLog.LOG_COMM, "cancel Comm thread: no thread");
    }
  }

  // -------------------------------------------------------- 
  // PROTOCOL

  protected void closeProtocol()
  {
    // TDLog.Log( TDLog.LOG_COMM, "VD comm close protocol");
    // if ( mProtocol != null ) mProtocol.closeIOstreams(); // FIXME somewhere closeIOStreams must be called
    mProtocol = null;
  }

  /** start the Comm thread
   * @param to_read   number of packets to read
   * @param lister    data lister
   * @param data_type expected data type (?)A
   * @param timeout   closing the thread if no data received within the timeout (UNUSED)
   * @return always false (ie, thread not started) by default
   */
  protected boolean startCommThread( int to_read, ListerHandler lister, int data_type, int timeout ) 
  {
    return false;
  }

  /** disconnect the remote device
   */
  public void disconnectRemoteDevice( )
  {
    // TDLog.Log( TDLog.LOG_COMM, "disconnect remote device ");
    cancelCommThread();
    closeProtocol();
  }

  /** send a command 
   * @param cmd   command code
   * @return ???
   * 
   * The command code is the app-code of the command.
   * This method invokes the protocol' sendCmd() which should translate the app-code to the
   * device code of the command (or whatever) and invoke the comm class to send it to the
   * device.
   */
  public boolean sendCommand( int cmd )
  {
    boolean ret = false;
    if ( mProtocol != null ) {
      // TDLog.v( "sendCommand " + cmd + " trying three times");
      for (int k=0; k<3 && ! ret; ++k ) { // try three times
        // TDLog.v( "sendCommand " + cmd + " try " + k );
        ret = mProtocol.sendCommand( (byte)cmd ); // was ret |= ...
        // TDLog.v( "sendCommand " + cmd + " " + k + "-ret " + ret );
        TDUtil.slowDown( TDSetting.mWaitCommand, "send command sleep interrupted"); // it is ok to be interrupted
      }
    }
    return ret;
  }

  /** toggle DistoX calibration mode
   * @param address   device address
   * @param type      ???
   * @return ???
   */
  public boolean toggleCalibMode( String address, int type ) { return false; }

  /** write DistoX calibration coeffs
   * @param address   device address
   * @param coeff     calib coeff array
   * @return ???
   */
  public boolean writeCoeff( String address, byte[] coeff ) { return false; }

  /** read DistoX calibration coeffs
   * @param address   device address
   * @param coeff     calib coeff array
   * @return ???
   */
  public boolean readCoeff( String address, byte[] coeff ) { return false; }

  /** read DistoX (??? bytes) memory
   * @param address   device address
   * @param addr      memory address
   * @return array of read bytes
   */
  public byte[] readMemory( String address, int addr ) 
  {
    TDLog.Error("TD Comm read memory returns null");
    return null;
  }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  /** connect to a device 
   * @param address   device address
   * @param lister    data lister
   * @param data_type ???
   * @param timeout   timeout (unused)
   * @return always false (ie, failure) by default
   */
  public boolean connectDevice( String address, ListerHandler lister, int data_type, int timeout )
  {
    TDLog.v("TD comm: generic connect device always false");
    return false;
  }

  /** disconnect from the remote device
   * @return always true, ie disconnected, by default
   */
  public boolean disconnectDevice() 
  { 
    TDLog.v("TD comm: generic disconnect device always true");
    return true;
  }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  /** download data from the remote device
   * @param address   device address
   * @param lister    data lister
   * @param data_type packet datatype, either shot or calib (or all) (not used)
   * @param timeout   timeout (unused)
   * @return always -1: number of packet received - must be overridden
   */
  public int downloadData( String address, ListerHandler lister, int data_type, int timeout )
  {
    TDLog.e("TD comm: generic download data always fails");
    return -1;
  }

  /** read a number of packets
   * @param to_read    number of packets to read
   * @param data_type  ???
   * @return ???
   */
  int readingPacket( boolean to_read, int data_type )
  {
    return mProtocol.readPacket( to_read, data_type );
  }

  /** cancel the work in progress
   */
  void cancelWork() 
  {
    if ( mProtocol != null ) mProtocol.mMaxTimeout = 0;
  }


  /** notify the application of the connection status
   * @param status   connection status
   * @note to be used when the connection status changes
   */
  public void notifyStatus( int status )
  {
    mApp.notifyListerStatus( mApp.mListerSet, status );
  }

  /** notify the lister of the connection status
   * @param lister   lister handler
   * @param status   connection status
   * @note to be used when the connection status changes
   */
  protected void notifyStatus( ListerHandler lister, int status )
  {
    mApp.notifyListerStatus( lister, status );
  }

}
