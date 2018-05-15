/** @file VirtualDistoX.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid virtual DistoX
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.LinkedList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;

// import android.content.Intent;
import android.content.Context;
// import android.content.ComponentName;
// import android.content.ServiceConnection;
// import android.app.Service;
// import android.os.IBinder;
// import android.os.Binder;

import android.content.SharedPreferences;
// import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

// import android.util.Log;

class VirtualDistoX 
{
  private boolean mBound = false; // single client at once

  private SharedPreferences mSharedPrefs; // to store calibration coeffs
  private Vector bG = null; // calibration coeffs
  private Vector bM = null;
  private Matrix aG = null;
  private Matrix aM = null;

  LinkedList< MemoryOctet > mData = null; // synch collection
  private byte mSeqByte = (byte)0x00;

  private void addOctet( MemoryOctet octet )
  {
    octet.data[0] = (byte)( octet.data[0] | mSeqByte );
    mData.add( octet );
    // Log.v("DistoX", "VD put octet " + String.format("%02x %02x", octet.data[0], mSeqByte ) );
    mSeqByte = (byte)( ( mSeqByte == 0 )? 0x80 : 0x00 );
  }

  // -------------------------------------------------------------

  // starting by startService()
  void startServer( Context ctx )
  {
    // Log.v("DistoX", "VD server: start ");
    mSharedPrefs = PreferenceManager.getDefaultSharedPreferences( ctx );
    loadCalibration();
    mData = new LinkedList< MemoryOctet >();
    startServiceThread();
  }

  void stopServer( Context ctx )
  {
    // Log.v("DistoX", "VD server: stop ");
    stopServiceThread();
    saveCalibration();
  }

  void bindServer()
  {
    // Log.v("DistoX", "VD server: bind. bound: " + mBound );
    if ( ! mBound ) {
      mBound = true;
      startIOThread();
    }
  }

  boolean unbindServer()
  {
    // Log.v("DistoX", "VD server: unbind. bound: " + mBound );
    mBound = false;
    stopIOThreads();
    return true;
  }

  // -------------------------------------------------------------
  // data streams
  
  private PipedOutputStream  mServerToClient   = null;
  private PipedInputStream   mClientToServer   = null;
  private PipedOutputStream  mServerFromClient = null;
  private PipedInputStream   mClientFromServer = null;
  

  DataInputStream  getInputStream()
  { 
    // Log.v("DistoX", "VD server: get input stream from " + ((mClientFromServer==null)?"null":"non-null") );
    if ( mClientFromServer != null ) return new DataInputStream( mClientFromServer );
    return null;
  }

  DataOutputStream getOutputStream()
  { 
    // Log.v("DistoX", "VD server: get output stream from " + ((mServerFromClient==null)?"null":"non-null") );
    if ( mServerFromClient != null ) return new DataOutputStream( mServerFromClient );
    return null;
  } 

  // TODO a thread handling I/O
  volatile private boolean mIOWaitAck = false;
  volatile private boolean mCmdDone   = false;
  volatile private boolean mDataDone  = false;
  private Thread mCmdThread  = null;
  private Thread mDataThread = null;

  private void startIOThread()
  {
    // Log.v("DistoX", "VD server: start I/O thread");
    mServerToClient = new PipedOutputStream();
    mClientToServer = new PipedInputStream();
    mClientFromServer = new PipedInputStream();
    mServerFromClient = new PipedOutputStream();
    try { mClientFromServer.connect( mServerToClient ); } catch ( IOException e ) { }
    try { mServerFromClient.connect( mClientToServer ); } catch ( IOException e ) { }

    mCmdThread = new Thread() {
      public void run()
      {
        PipedInputStream dis  = mClientToServer;
        MemoryOctet octet;
        mCmdDone = false;
        while ( ! mCmdDone ) {
          try {
            byte b = (byte)dis.read(); // Byte();
            // Log.v("DistoX", "VD I/O on wait ack. recv " + String.format("0x%02x", b) );
            switch ( b & 0x3f ) {
              case 0x30: // calib off
                mCalibMode = false;
                break;
              case 0x31: // calib on
                mCalibMode = true;
                break;
              case 0x36: // laser on
              case 0x37: // laser off
                break;
              case 0x38:
                // read two more bytes (address)
                octet = new MemoryOctet( ++mCount );
                octet.data[0] = b;
                octet.data[1] = (byte)dis.read(); // Byte();
                octet.data[2] = (byte)dis.read(); // Byte();
                for ( int k = 3; k<8; ++k ) octet.data[k] = 0;
                mData.add( octet );
                break;
              case 0x39: // data
                // read six more bytes
                octet = new MemoryOctet( ++mCount );
                octet.data[0] = b;
                for ( int k = 1; k<7; ++k ) octet.data[k] = (byte)dis.read(); // Byte();
                mData.add( octet );
                break;
              case 0x3a: // firmware dump FIXME
                // octet = new MemoryOctet( ++mCount );
                // octet.data[0] = b;
                // octet.data[1] = (byte)dis.read(); // Byte();
                // octet.data[2] = (byte)dis.read(); // Byte();
                break;
              case 0x3b: // firmware upload
                for ( int k = 1; k<259; ++k ) b = (byte)dis.read(); // Byte();
                break;
              default: // ack (1 byte)
                // Log.v("DistoX", "VD I/O ack recv " + String.format("0x%02x", b) );
                // ack = ( b & 0x80 ) | 0x55;
                mData.poll(); // pollFirst(); // remove head of queue
                mIOWaitAck = false;
            }
          } catch ( IOException e ) { 
            TDLog.Error("VD I/O read error " + e.getMessage() );
          }
        }
      }
    };

    mDataThread = new Thread() {
      public void run()
      {
        PipedOutputStream dos = mServerToClient;
        MemoryOctet octet;
        int n_read = 0;
        mDataDone = false;
        while ( ! mDataDone ) {
          if ( mIOWaitAck ) {
            try { Thread.sleep( 100 ); } catch ( InterruptedException e ) { }
          } else {
            octet = mData.peek(); // peekFirst(); // get head of queue
            if ( octet != null ) {
              try {
                // Log.v("DistoX", "VD I/O write octet " + String.format("%02x %02x %02x %02x %02x %02x %02x %02x",
                //   octet.data[0], octet.data[1], octet.data[2], octet.data[3], octet.data[4], octet.data[5],
                //   octet.data[6], octet.data[7] ) );
                dos.write( octet.data, 0, 8 );
                dos.flush();
                if ( ( octet.data[0] & 0x7f ) < 0x0f ) { // only DATA G M and VECTOR
                  mIOWaitAck = true;
                }
              } catch ( IOException e ) {
                TDLog.Error("VD I/O write error " + e.getMessage() );
                stopIOThreads();
              }
            } else {
              // Log.v("DistoX", "VD I/O no octet to write: sleep");
              try { Thread.sleep( 500 ); } catch ( InterruptedException e ) { }
            }
          }
        }
      }
    };

    mIOWaitAck = false;
    mCmdThread.start();
    mDataThread.start();
  }

  private void stopIOThreads()
  {
    mCmdDone    = true;
    mDataDone   = true;
    mCmdThread  = null;
    mDataThread = null;
    if ( mServerToClient != null ) {
      try { mServerToClient.close();   } catch ( IOException e ) { }
      mServerToClient   = null;
    }
    if ( mClientToServer != null ) {
      try { mClientToServer.close();   } catch ( IOException e ) { }
      mClientToServer   = null;
    }
    if ( mClientFromServer != null ) {
      try { mClientFromServer.close(); } catch ( IOException e ) { }
      mClientFromServer = null;
    }
    if ( mServerFromClient != null ) {
      try { mServerFromClient.close(); } catch ( IOException e ) { }
      mServerFromClient = null;
    }
  }

  // -------------------------------------------------------
  // service thread

  private Thread mServiceThread = null;
  private int mCount;                  // octet index
  private boolean mCalibMode  = false; // whether to use raw data (calibration)
  private boolean mSilentMode = false;

  final static private float G_MAX = 32768;
  final static private float M_MAX = 32768;

  private boolean mServiceThreadDone;
  class ServiceThread extends Thread
  {
    public void run()
    {
      float   distance;
      Vector  dataG = new Vector();
      Vector  dataM = new Vector();
      mCount = 0;
      mServiceThreadDone = false;
      while ( ! mServiceThreadDone ) {
        // Log.v("DistoX", "VD service thread running. count " + mCount );
        distance = data_available( dataG, dataM );
        if ( distance >= 0 ) {
          if ( ! mSilentMode ) {
            if ( mCalibMode ) {
              addOctet( rawData( dataG, G_MAX, (byte)0x02 ) ); // raw G-octet
              addOctet( rawData( dataM, M_MAX, (byte)0x03 ) ); // raw M-octet
            } else {
              addOctet( cookedData( distance, dataG, dataM ) );
            }
          }
        } else {
          try { Thread.sleep( 3000 ); } catch ( InterruptedException e ) { }
        }
      }
    }
  };

  private void startServiceThread()
  {
    // Log.v("DistoX", "VD server: start service thread" );
    mServiceThread = new ServiceThread();
    mServiceThread.start();
  }

  private void stopServiceThread()
  {
    // Log.v("DistoX", "VD server: stop service thread" );
    if ( mServiceThread == null ) return;
    mServiceThreadDone = true;
    try {
      mServiceThread.join( 2000 );
    } catch ( InterruptedException e ) { }
    mServiceThread = null;
  }


    
  // get data from hardware: 
  //     update G and M [arbitrary units]
  //     return distance[mm] (negative if fail)
  private static int mSplay = 0;
  private static float d0, gx, gy, gz, mx, my, mz;

  protected float data_available( Vector G, Vector M ) 
  { 
    // try { Thread.sleep( 500 ); } catch ( InterruptedException e ) { }
    if ( mData.size() > 3 && mSplay == 0 ) return -1.0f;
    G.x = (float)(Math.random()*2-1) * TopoDroidUtil.FV / 2;
    G.y = (float)(Math.random()*2-1) * TopoDroidUtil.FV / 2;
    G.z = (float)(Math.random()*2-1) * TopoDroidUtil.FV / 2;
    M.x = (float)(Math.random()*2-1) * TopoDroidUtil.FV / 2;
    M.y = (float)(Math.random()*2-1) * TopoDroidUtil.FV / 2;
    M.z = (float)(Math.random()*2-1) * TopoDroidUtil.FV / 2;
    float dist = 2000 + 6000 * (float)Math.random();
    ++ mSplay;
    if ( mSplay == 6 ) {
      gx = G.x; gy = G.y; gz = G.z;
      mx = M.x; my = M.y; mz = M.z;
      d0 = dist;
    } else if ( mSplay > 6 ) {
      G.x = gx + 0.005f * G.x;
      G.y = gy + 0.005f * G.y;
      G.z = gz + 0.005f * G.z;
      M.x = mx + 0.005f * M.x;
      M.y = my + 0.005f * M.y;
      M.z = mz + 0.005f * M.z;
      dist = d0 + (dist - 5000)/60; // +/- 50 mm
      if ( mSplay > 7 ) mSplay = 0;
    }
    return dist;
  }

  private MemoryOctet rawData( Vector v, float max, byte type )
  {
    MemoryOctet ret = new MemoryOctet( ++mCount );
    ret.data[0] = type;
    int ix = (int)(( v.x > 0 )? (v.x/max)*0x8000 : 0x10000 + (v.x/max)*0x8000);
    int iy = (int)(( v.y > 0 )? (v.y/max)*0x8000 : 0x10000 + (v.y/max)*0x8000);
    int iz = (int)(( v.z > 0 )? (v.z/max)*0x8000 : 0x10000 + (v.z/max)*0x8000);
    ret.data[1] = (byte)(ix & 0xff);
    ret.data[2] = (byte)((ix>>8) & 0xff);
    ret.data[3] = (byte)(iy & 0xff);
    ret.data[4] = (byte)((iy>>8) & 0xff);
    ret.data[5] = (byte)(iz & 0xff);
    ret.data[6] = (byte)((iz>>8) & 0xff);
    ret.data[7] = (byte)0;
    return ret;
  }

  // raw data
  //     gx = g[2]<<8 | g[1] etc.
  // distance in mm
  private MemoryOctet cookedData( float distance, Vector g, Vector m )
  {
    MemoryOctet ret = new MemoryOctet( ++mCount );
    Vector vg = new Vector( bG.x + aG.x.x * g.x + aG.x.y * g.y + aG.x.z * g.z,
                            bG.y + aG.y.x * g.x + aG.y.y * g.y + aG.y.z * g.z,
                            bG.z + aG.z.x * g.x + aG.z.y * g.y + aG.z.z * g.z );
    vg.normalize();
    Vector vm = new Vector( bM.x + aM.x.x * m.x + aM.x.y * m.y + aM.x.z * m.z,
                            bM.y + aM.y.x * m.x + aM.y.y * m.y + aM.y.z * m.z,
                            bM.z + aM.z.x * m.x + aM.z.y * m.y + aM.z.z * m.z );
    vm.normalize();
    Vector ve = vg.cross( vm );
    ve.normalize();
    Vector vn = ve.cross( vg );

    float xg = vg.x; // (1,0,0) * vg
    float clino = (float)( Math.acos( xg ) ) * TDMath.RAD2DEG;
    if ( clino > 90 ) clino -= 180;
    Vector vh = new Vector( 1-xg*vg.x, 0-xg*vg.y, 0-xg*vg.z ); // X - (X*G) G = proj of X on E-N plane
    float azimuth = (float)( Math.atan2( vh.dot(ve), vh.dot(vn) ) ) * TDMath.RAD2DEG;
    if ( azimuth < 0 ) azimuth += 360;
    int ia = (int)(azimuth * 0x8000 / 180.0f);
    int ic = ( clino >= 0 )? (int)(clino * 0x4000 / 90.0f) : (int)(0x10000 + clino * 0x4000 / 90.0f);

    // distance = (b[0] & 0x40)<<10 | b[2]<<8 | b[1] // distance mm
    // compass  = b[4]<<8 | b[3]
    // clino    = b[5]<<8 | b[4]
    int id = (int)distance;
    ret.data[0] = (byte)((((id>>16) & 0x01)<<6) | 0x01); // data octet
    ret.data[1] = (byte)(id & 0xff);
    ret.data[2] = (byte)((id>>8) & 0xff);
    ret.data[3] = (byte)(ia & 0xff);
    ret.data[4] = (byte)((ia>>8) & 0xff);
    ret.data[5] = (byte)(ic & 0xff);
    ret.data[6] = (byte)((ic>>8) & 0xff);
    ret.data[7] = 0; // TODO roll
    return ret;
  }

  // -------------------------------------------------------
  // DistoX protocol

  private float toFloat( byte bh, byte bl ) // bh high-byte, bk low-byte
  {
    int i = MemoryOctet.toInt( bh, bl );
    return ( i < 0x8000 )? 0x10000 - i : i;
  }

  // x and y between -0x8000 and 0x8000
  private void fromFloat( byte[] b, float x, float y )
  {
    int ix = ( x >= 0 )? (int)(x) : 0x10000 - (int)(-x); 
    int iy = ( y >= 0 )? (int)(y) : 0x10000 - (int)(-y); 
    b[3] = (byte)(ix & 0xff);
    b[4] = (byte)((ix>>8) & 0xff);
    b[5] = (byte)(iy & 0xff);
    b[6] = (byte)((iy>>8) & 0xff);
  }

  private void writeMemory( byte[] b )
  {
    int addr = (int)(b[1]) | ((int)(b[2]) << 8 );
    switch ( addr ) {
      case 0x8010: // bG.x bG.y
        bG.x = toFloat( b[4], b[3] );
        bG.y = toFloat( b[6], b[5] );
        break;
      case 0x8014: // bG.z Ag.x.x
        bG.z = toFloat( b[4], b[3] );
        aG.x.x = toFloat( b[6], b[5] );
        break;
      case 0x8018: // aG.x.y aG.x.z
        aG.x.y = toFloat( b[4], b[3] );
        aG.x.z = toFloat( b[6], b[5] );
        break;
      case 0x801c: // aG.y.x aG.y.y
        aG.y.x = toFloat( b[4], b[3] );
        aG.y.y = toFloat( b[6], b[5] );
        break;
      case 0x8020: // aG.y.z aG.z.x
        aG.y.z = toFloat( b[4], b[3] );
        aG.z.x = toFloat( b[6], b[5] );
        break;
      case 0x8024: // aG.z.y aG.z.z
        aG.z.y = toFloat( b[4], b[3] );
        aG.z.z = toFloat( b[6], b[5] );
        break;

      case 0x8028: 
        bM.x = toFloat( b[4], b[3] );
        bM.y = toFloat( b[6], b[5] );
        break;
      case 0x802c:
        bM.z = toFloat( b[4], b[3] );
        aM.x.x = toFloat( b[6], b[5] );
        break;
      case 0x8030:
        aM.x.y = toFloat( b[4], b[3] );
        aM.x.z = toFloat( b[6], b[5] );
        break;
      case 0x8034:
        aM.y.x = toFloat( b[4], b[3] );
        aM.y.y = toFloat( b[6], b[5] );
        break;
      case 0x8038:
        aM.y.z = toFloat( b[4], b[3] );
        aM.z.x = toFloat( b[6], b[5] );
        break;
      case 0x803c:
        aM.z.y = toFloat( b[4], b[3] );
        aM.z.z = toFloat( b[6], b[5] );
        break;
    }
  }

  private void readMemory( byte[] b )
  {
    int addr = (int)(b[1]) | ((int)(b[2]) << 8 );
    switch ( addr ) {
      case 0x8010:
        fromFloat( b, bG.x, bG.y ); 
        break;
      case 0x8014:
        fromFloat( b, bG.z, aG.x.x );
        break;
      case 0x8018:
        fromFloat( b, aG.x.y, aG.x.z );
        break;
      case 0x801c:
        fromFloat( b, aG.y.x, aG.y.y );
        break;
      case 0x8020:
        fromFloat( b, aG.y.z, aG.z.x );
        break;
      case 0x8024:
        fromFloat( b, aG.z.x, aG.z.z );
        break;

      case 0x8028:
        fromFloat( b, bM.x, bM.y ); 
        break;
      case 0x802c:
        fromFloat( b, bM.z, aM.x.x );
        break;
      case 0x8030:
        fromFloat( b, aM.x.y, aM.x.z );
        break;
      case 0x8034:
        fromFloat( b, aM.y.x, aM.y.y );
        break;
      case 0x8038:
        fromFloat( b, aM.y.z, aM.z.x );
        break;
      case 0x803c: 
        fromFloat( b, aM.z.x, aM.z.z );
        break;
    }
  }

  boolean onSend( byte[] b ) // DistoX commands protocol
  {
    switch ( b[0] ) {
      case 0x39: // memory write
        writeMemory( b );
        break;
      case 0x38: // memory read
        readMemory( b );
        break;
      case 0x30: // calib stop
        mCalibMode = false;
        break;
      case 0x31: // calib start
        mCalibMode = true;
        break;
      case 0x32: // silent stop
        mSilentMode = false;
        break;
      case 0x33: // silent start
        mSilentMode = true;
        break;
    }
    return false;
  }

  boolean onRead( byte[] octet, int timeout ) // DistoX data polling
  {
    int time = 0;
    while ( timeout > 0 && time < timeout ) {
      if ( mData == null ) break;
      if ( mData.size() == 0 ) try { Thread.sleep(100); } catch (InterruptedException e ) { }
      time += 100;
    }
    if ( mData == null || mData.size() == 0 ) return false;
    MemoryOctet tmp = (MemoryOctet)( mData.poll() ); // pollFirst() );
    for ( int k=0; k<8; ++k ) octet[k] = tmp.data[k];
    return true;
  }

  // -------------------------------------------------------
  // calib coeffs storage
  // WARNING vector by default is (0,0,0)
  //         matrix by default is zero

  private float loadFloat( String key )
  {
    return mSharedPrefs.getFloat( key, 0.0f );
  }

  private void saveFloat( String key, float val )
  {
    SharedPreferences.Editor editor = mSharedPrefs.edit();
    editor.putFloat( key, val );
    editor.apply(); // was editor.commit();
  }

  private Vector loadVector( String key )
  { 
    return new Vector( loadFloat( key + "x" ), 
                       loadFloat( key + "y" ),
                       loadFloat( key + "z" ) );
  }

  private Matrix loadMatrix( String key )
  { 
    return new Matrix( loadVector( key + "x" ), 
                       loadVector( key + "y" ),
                       loadVector( key + "z" ) );
  }

  private void saveVector( String key, Vector v )
  { 
    if ( v == null ) return;
    saveFloat( key + "x", v.x );
    saveFloat( key + "y", v.y );
    saveFloat( key + "z", v.z );
  }

  private void saveMatrix( String key, Matrix m )
  { 
    if ( m == null ) return;
    saveVector( key + "x", m.x ); 
    saveVector( key + "y", m.y );
    saveVector( key + "z", m.z );
  }

  private void loadCalibration()
  {
    bG = loadVector( "bG" );
    bM = loadVector( "bM" );
    aG = loadMatrix( "aG" ); aG = new Matrix( Matrix.one );
    aM = loadMatrix( "aM" ); aM = new Matrix( Matrix.one );
  }
  
  private void saveCalibration()
  {
    saveVector( "bG", bG );
    saveVector( "bM", bM );
    saveMatrix( "aG", aG );
    saveMatrix( "aM", aM );
  }

}

