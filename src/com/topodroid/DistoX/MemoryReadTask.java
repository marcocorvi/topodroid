/* @file MemoryReadTask.java
 *
 * @author marco corvi
 * @date apr 2016
 *
 * @brief TopoDroid DistoX memory read task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.content.Context;

import android.widget.Toast;

// import android.util.Log;

class MemoryReadTask extends AsyncTask<Void, Integer, Integer>
{
  TopoDroidApp   mApp;
  IMemoryDialog  mDialog;
  int mType; // DistoX type
  String mAddress;
  int[] mHT;
  String mDumpfile = null;
  ArrayList< MemoryOctet > mMemory;

  MemoryReadTask( TopoDroidApp app, IMemoryDialog dialog, int type, String address, int[] ht, String dumpfile )
  {
    mApp      = app;
    mDialog   = dialog;
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
    if ( mType == Device.DISTO_X310 ) {
      res = mApp.readX310Memory( mAddress, mHT[0], mHT[1], mMemory );
    } else if ( mType == Device.DISTO_A3 ) {
      res = mApp.readA3Memory( mAddress, mHT[0], mHT[1], mMemory );
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
    if ( result > 0 && mDialog != null ) {
      mDialog.updateList( mMemory );
    } else {
      Toast.makeText( mApp, R.string.read_failed, Toast.LENGTH_SHORT ).show();
    }
  }

  private void writeMemoryDumpToFile( String dumpfile, ArrayList< MemoryOctet > memory )
  {
    if ( dumpfile == null ) return;
    dumpfile.trim();
    if ( dumpfile.length() == 0 ) return;
    try { 
      String dumppath = TDPath.getDumpFile( dumpfile );
      TDPath.checkPath( dumppath );
      FileWriter fw = new FileWriter( dumppath );
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
