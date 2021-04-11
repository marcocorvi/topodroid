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
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.Lister;
// import com.topodroid.DistoX.DBlock;
import com.topodroid.common.ExtendType;
import com.topodroid.common.LegType;
import com.topodroid.dev.distox.DistoX;

import android.util.Log;

import java.util.UUID;
// import java.util.List;
// import java.util.concurrent.atomic.AtomicInteger; // FIXME_ATOMIC_INT

import android.os.Bundle;
import android.os.Handler;
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

  public byte[] mCoeff;

// -----------------------------------------------------------

  // private AtomicInteger mNrPacketsRead; // FIXME_ATOMIC_INT
  protected volatile int mNrPacketsRead;

  // int getNrPacketsRead() { return ( mNrPacketsRead == null )? 0 : mNrPacketsRead.get(); } // FIXME_ATOMIC_INT 
  public int  getNrReadPackets() { return mNrPacketsRead; }
  public void setNrReadPackets( int nr ) { mNrPacketsRead = nr; }

  // void incNrReadPackets() { ++mNrPacketsRead; }
  // void resetNrReadPackets() { mNrPacketsRead = 0; }

  public boolean isConnected() { return mBTConnected; }

  // @param index      bric shot index
  // @param lister
  // @param data_type bric datatype (0: normal, 1: scan )
  public void handleBricPacket( long index, Handler lister, int data_type, float clino_error, float azimuth_error, String comment )
  {
    // Log.v( "DistoX", "TD comm: PACKET " + res + "/" + DataType.PACKET_DATA + " type " + data_type );
    // Log.v("DistoX", "TD comm: packet DATA");
    // mNrPacketsRead.incrementAndGet(); // FIXME_ATOMIC_INT
    ++mNrPacketsRead;
    double d = mProtocol.mDistance;
    double b = mProtocol.mBearing;
    double c = mProtocol.mClino;
    double r = mProtocol.mRoll;
    double dip  = mProtocol.mDip;
    long status = ( d > TDSetting.mMaxShotLength )? TDStatus.OVERSHOOT : TDStatus.NORMAL;
    // TODO split the data insert in three places: one for each data packet

    int leg = ( data_type == DataType.DATA_SCAN )? LegType.SCAN : LegType.NORMAL;
    if ( comment == null ) comment = "";
    mLastShotId = TopoDroidApp.mData.insertBricShot( TDInstance.sid, index, d, b, c, r, clino_error, azimuth_error, dip, ExtendType.EXTEND_IGNORE, leg, status, comment, TDInstance.deviceAddress() );
    // TopoDroidApp.mData.updateShotAMDR( mLastShotId, TDInstance.sid, clino_error, azimuth_error, dip, r, false );
    // if ( comment != null ) TopoDroidApp.mData.updateShotComment( mLastShotId, TDInstance.sid, comment );
    
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
      Log.v("DistoX", "TD comm: null Lister");
    }
  }

  public void handleZeroPacket( long index, Handler lister, int data_type )
  {
    // Log.v( "DistoX", "TD comm: PACKET " + res + "/" + DataType.PACKET_DATA + " type " + data_type );
    // Log.v("DistoX", "TD comm: packet DATA");
    // mNrPacketsRead.incrementAndGet(); // FIXME_ATOMIC_INT
    ++mNrPacketsRead;
    double r = mProtocol.mRoll;
    long status = TDStatus.NORMAL;
    mLastShotId = TopoDroidApp.mData.insertDistoXShot( TDInstance.sid, index, 0, 0, 0, r, ExtendType.EXTEND_IGNORE, status, TDInstance.deviceAddress() );
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
      Log.v("DistoX", "TD comm: null Lister");
    }
  }

  // @param res    packet type (as returned by handlePacket / or set by Protocol )
  // @param lister data lister
  // @param data_type unused
  public void handleRegularPacket( int res, Handler lister, int data_type )
  {
    // Log.v( "DistoX", "TD comm: PACKET " + res + "/" + DataType.PACKET_DATA + " type " + data_type );
    if ( res == DataType.PACKET_DATA ) {
      // Log.v("DistoX", "TD comm: packet DATA");
      // mNrPacketsRead.incrementAndGet(); // FIXME_ATOMIC_INT
      ++mNrPacketsRead;
      double d = mProtocol.mDistance;
      double b = mProtocol.mBearing;
      double c = mProtocol.mClino;
      double r = mProtocol.mRoll;
      // extend is unset to start
      // long extend = TDAzimuth.computeLegExtend( b ); // ExtendType.EXTEND_UNSET; FIXME_EXTEND 
      // TDLog.Log( TDLog.LOG_COMM, "Comm D PACKET " + d + " " + b + " " + c );
      // Log.v("DistoX", "TD comm: D PACKET " + d + " " + b + " " + c );
      // NOTE type=0 shot is DistoX-type
      long status = ( d > TDSetting.mMaxShotLength )? TDStatus.OVERSHOOT : TDStatus.NORMAL;
      mLastShotId = TopoDroidApp.mData.insertDistoXShot( TDInstance.sid, -1L, d, b, c, r, ExtendType.EXTEND_IGNORE, status, TDInstance.deviceAddress() );
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
        Log.v("DistoX", "TD comm: null Lister");
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
      // Log.v("DistoX", "TD comm: packet G");
      /// TDLog.Log( TDLog.LOG_COMM, "Comm G PACKET" );
      // mNrPacketsRead.incrementAndGet(); // FIXME_ATOMIC_INT
      ++mNrPacketsRead;
      setHasG( true );
    } else if ( res == DataType.PACKET_M ) {
      // Log.v("DistoX", "TD comm: packet M");
      // TDLog.Log( TDLog.LOG_COMM, "Comm M PACKET" );
      // mNrPacketsRead.incrementAndGet(); // FIXME_ATOMIC_INT
      ++mNrPacketsRead;
      // get G and M from mProtocol and save them to store
      // TDLog.Log( TDLog.LOG_COMM, "G " + mProtocol.mGX + " " + mProtocol.mGY + " " + mProtocol.mGZ + " M " + mProtocol.mMX + " " + mProtocol.mMY + " " + mProtocol.mMZ );
      long cblk = TopoDroidApp.mDData.insertGM( TDInstance.cid, mProtocol.mGX, mProtocol.mGY, mProtocol.mGZ, mProtocol.mMX, mProtocol.mMY, mProtocol.mMZ );
      if ( lister != null ) {
        Message msg = lister.obtainMessage( Lister.LIST_UPDATE );
        Bundle bundle = new Bundle();
        bundle.putLong( Lister.BLOCK_ID, cblk );
        msg.setData(bundle);
        lister.sendMessage(msg);
      }
      if ( ! mHasG ) {
        TDLog.Error( "data without G packet " + mNrPacketsRead /* getNrReadPackets() */ );
        // if ( TopoDroidApp.mActivity != null ) { // skip toast
        //   TopoDroidApp.mActivity.runOnUiThread( new Runnable() {
        //     public void run() {
        //       TDToast.makeBG("data without G: " + mNrPacketsRead /* getNrReadPackets() */, TDColor.FIXED_RED );
        //     }
        //   } );
        // }
      }
      setHasG( false );
    } else if ( res == DataType.PACKET_REPLY ) {
      // Log.v("DistoX", "TD comm: packet REPLY");
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
      // Log.v("DistoX", "TD comm: packet VECTOR");
      // vector packet do count
      // mNrPacketsRead.incrementAndGet(); // FIXME_ATOMIC_INT
      ++mNrPacketsRead;
      double acc  = mProtocol.mAcceleration;
      double mag  = mProtocol.mMagnetic;
      double dip  = mProtocol.mDip;
      double roll = mProtocol.mRoll;
      boolean backshot = mProtocol.mBackshot;
      // TDLog.Log( TDLog.LOG_COMM, "Comm V PACKET " + mLastShotId + " " + acc + " " + mag + " " + dip + " " + roll );
      if ( TDInstance.deviceType() == Device.DISTO_X310 ) {
        TopoDroidApp.mData.updateShotAMDR( mLastShotId, TDInstance.sid, acc, mag, dip, roll, backshot );
        if ( TDSetting.mWaitData > 10 ) {
          TDUtil.slowDown( TDSetting.mWaitData );
        }
      }
    } else {
      TDLog.Error("DistoX packet UNKNOWN");
    }
  }

  public CommThread mCommThread = null;

  public boolean isCommThreadNull( ) { return ( mCommThread == null ); }

  void doneCommThread() { mCommThread = null; }

  boolean mHasG = false;
  long mLastShotId;   // last shot id

  public void setHasG( boolean has_g ) { mHasG = has_g; }

  public TopoDroidComm( TopoDroidApp app )
  {
    mApp          = app;
    mProtocol     = null;
    mAddress      = null;
    // mCommThread   = null;
    mBTConnected  = false;
    // TDLog.Log( TDLog.LOG_COMM, "TopoDroid Comm cstr");
  }

  public void resume()
  {
    // if ( mCommThread != null ) { mCommThread.resume(); }
  }

  public void suspend()
  {
    // if ( mCommThread != null ) { mCommThread.suspend(); }
  }


  protected void cancelCommThread()
  {
    // TDLog.Log( TDLog.LOG_COMM, "VD comm cancel Comm thread");
    if ( mCommThread != null ) { // FIXME check that comm-thread is really alive
      // TDLog.Log( TDLog.LOG_COMM, "cancel Comm thread: thread is active");
      mCommThread.cancelWork();
      try {
        mCommThread.join();
      } catch ( InterruptedException e ) {
        // TDLog.Error( "cancel thread interrupt " + e.getMessage() );
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
    // if ( mProtocol != null ) mProtocol.closeIOstreams();
    mProtocol = null;
  }

  protected boolean startCommThread( int to_read, Handler /* ILister */ lister, int data_type ) 
  {
    return false;
  }

  public void disconnectRemoteDevice( )
  {
    // TDLog.Log( TDLog.LOG_COMM, "disconnect remote device ");
    cancelCommThread();
    closeProtocol();
  }

  public boolean sendCommand( int cmd )
  {
    // TDLog.Log( TDLog.LOG_COMM, "VD comm send cmd " + cmd );
    boolean ret = false;
    if ( mProtocol != null ) {
      for (int k=0; k<3 && ! ret; ++k ) { // try three times
        ret = mProtocol.sendCommand( (byte)cmd ); // was ret |= ...
        // TDLog.Log( TDLog.LOG_COMM, "sendCommand " + cmd + " " + k + "-ret " + ret );
        // Log.v( "DistoX", "sendCommand " + cmd + " " + k + "-ret " + ret );
        TDUtil.slowDown( TDSetting.mWaitCommand, "send command sleep interrupted"); // it is ok to be interrupted
      }
    }
    return ret;
  }

  public boolean toggleCalibMode( String address, int type ) { return false; }

  public boolean writeCoeff( String address, byte[] coeff ) { return false; }

  public boolean readCoeff( String address, byte[] coeff ) { return false; }

  public byte[] readMemory( String address, int addr ) { return null; }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  public boolean connectDevice( String address, Handler /* ILister */ lister, int data_type )
  {
    return false;
  }

  public boolean disconnectDevice() { return true; }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  /** 
   * @param lister    data lister
   * @param data_type packet datatype, either shot or calib (or all)
   * @return ialways -1: number of packet received - must be overridden
   */
  public int downloadData( String address, Handler /* ILister */ lister, int data_type )
  {
    TDLog.Error("TD comm: generic download data always fails");
    // Log.v("DistoX", "TD comm: generic download data always fails");
    return -1;
  }

  int readingPacket( boolean to_read, int data_type )
  {
    return mProtocol.readPacket( to_read, data_type );
  }

  void cancelWork() 
  {
    if ( mProtocol != null ) mProtocol.mMaxTimeout = 0;
  }

}
