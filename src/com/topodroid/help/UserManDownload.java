/* @file UserManDownload.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid calibration coefficient computation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
// import com.topodroid.TDX.TDInstance;
// import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import android.os.AsyncTask;
// import android.content.Context;

public class UserManDownload extends AsyncTask< String, Integer, Integer >
{
  // private final Context mContext; // unused 
  private String  mUrl;
  private static boolean running = false; 

  /** cstr
   * @param url   URL from which to download the man pages
   */
  public UserManDownload( /* Context context, */ String url )
  {
    // mContext = context;
    mUrl     = url;
  }

  // -------------------------------------------------------------------
  /** execute tha man pages download in background
   * @param statuses ...
   */
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    int ret = 0;
    URL url;
    try {
      url = new URL( mUrl );
      URLConnection connection = url.openConnection();
      HttpURLConnection http = (HttpURLConnection) connection;
      int response = http.getResponseCode();
      if ( response == HttpURLConnection.HTTP_OK ) {
        byte[] buffer = new byte[4096];
        InputStream in = http.getInputStream();
        ZipInputStream zin = new ZipInputStream( in );
        ZipEntry ze = null;
        if ( TDFile.checkManDir() ) {
          while ( ( ze = zin.getNextEntry() ) != null ) {
            String name = ze.getName();
            if ( ! ze.isDirectory() ) { // normal file
              // TDLog.Log( TDLog.LOG_PREFS, "Zip entry \"" + name + "\"" );
              int pos = name.lastIndexOf('/');
	      if ( pos > 0 ) name = name.substring(pos+1);
	      if ( ! name.startsWith("README") ) {
	        FileOutputStream fos = TDFile.getManFileOutputStream( name );
	        // FileOutputStream fos = TDInstance.context.openFileOutput( TDPath.getManFileName( name ), Context.MODE_PRIVATE );
                // int size = 0;
                int c;
                while ( ( c = zin.read( buffer ) ) != -1 ) {
                  fos.write(buffer, 0, c);
                  // size += c;
                }
                fos.close();
                // if ( file.getPath().endsWith("png") ) TDLog.v( "file " + file.getPath() + " " + size );
	      }
            } else { // ze directory: not really an error
              // TDLog.Log( TDLog.LOG_PREFS, "Zip dir entry \"" + name + "\"" );
	    }
	  }
          ret = 1;
        } else {
          TDLog.Error("ERROR could not mkdirs" );
        }
      } else {
        TDLog.Error("HTTP error : " + response );
      } 
      http.disconnect();
    } catch ( MalformedURLException e1 ) {
      TDLog.Error( "ERROR bad URL: " + e1.toString() );
    } catch ( IOException e2 ) {
      TDLog.Error( "ERROR I/O exception: " + e2.toString() );
    }
    return ret;
  }

  /** update the interface during the downloading 
   * @param values ...
   * only forward to the superclass
   */
  @Override
  protected void onProgressUpdate( Integer... values)
  {
    super.onProgressUpdate( values );
  }

  /** update the interface at the end of the download: display a toast
   * @param res   download result
   */
  @Override
  protected void onPostExecute( Integer res )
  {
    if ( res != null ) {
      int r = res.intValue();
      if ( r == 1 ) { // success
	TDToast.make( R.string.user_man_ok );
      } else { // failed
	TDToast.makeBad( R.string.user_man_fail );
      }
    }
    unlock();
  }

  /** lock the task: prevent double download
   */
  private synchronized boolean lock()
  {
    if ( running ) return false;
    running = true;
    return true;
  }

  /** unlock the task: allow another download
   */
  private synchronized void unlock()
  {
    if ( running ) running = false;
  }

}
