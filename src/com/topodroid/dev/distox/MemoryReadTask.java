/* @file MemoryReadTask.java
 *
 * @author marco corvi
 * @date apr 2016
 *
 * @brief TopoDroid DistoX memory read task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox;

import com.topodroid.dev.Device;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;

import com.topodroid.packetX.MemoryOctet;
// import com.topodroid.dev.Device;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.TDPath;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;

import java.lang.ref.WeakReference;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

// import java.util.List;
import java.util.ArrayList;

// import android.app.Activity;
import android.os.AsyncTask;
// import android.content.Context;

public class MemoryReadTask extends AsyncTask<Void, Integer, Integer>
{
  private final WeakReference<TopoDroidApp> mApp;
  private WeakReference<IMemoryDialog>  mDialog;
  private int mType; // DistoX type
  private String mAddress;
  private int[] mHT;
  private String mDumpfile = null;
  private ArrayList< MemoryOctet > mMemory;

  public MemoryReadTask( TopoDroidApp app, IMemoryDialog dialog, int type, String address, int[] ht, String dumpfile )
  {
    mApp      = new WeakReference<TopoDroidApp>( app );
    mDialog   = new WeakReference<IMemoryDialog>( dialog );
    mType     = type;
    mAddress  = address;
    mHT       = ht;
    mDumpfile = dumpfile;
    mMemory   = new ArrayList<>();
  }

  @Override
  protected Integer doInBackground(Void... v)
  {
    int res = 0;
    if ( mApp.get() != null ) {
      if ( mType == Device.DISTO_X310 ) {
        res = mApp.get().readX310Memory( mAddress, mHT[0], mHT[1], mMemory );
      } else if ( mType == Device.DISTO_A3 ) {
        res = mApp.get().readA3Memory( mAddress, mHT[0], mHT[1], mMemory );
      }
    }
    if ( res > 0 ) {
      writeMemoryDumpToFile( mDumpfile, mMemory );
    }
    return res;
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Integer result )
  {
    if ( result > 0 && mDialog.get() != null ) {
      mDialog.get().updateList( mMemory );
    } else {
      TDToast.makeBad( R.string.read_failed );
    }
  }

  private void writeMemoryDumpToFile( String dumpfile, ArrayList< MemoryOctet > memory )
  {
    if ( dumpfile == null ) return;
    dumpfile = dumpfile.trim();
    if ( dumpfile.length() == 0 ) return;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "dump memory to file " + dumppath );
      // String dumppath = TDPath.getDumpFile( dumpfile );
      // TDPath.checkPath( dumppath );
      // FileWriter fw = new FileWriter( dumppath );
      File file = TDPath.getDumpFile( dumpfile );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );
      for ( MemoryOctet m : memory ) {
        m.printHexString( pw );
        pw.format(" " + m.toString() + "\n");
      }
      fw.flush();
      fw.close();
    } catch ( IOException e ) {
    }
  }
}
