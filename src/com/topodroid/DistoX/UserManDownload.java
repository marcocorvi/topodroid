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
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

// import android.widget.Toast;
import android.os.AsyncTask;
import android.content.Context;


class UserManDownload extends AsyncTask< String, Integer, Integer >
{
  private final Context mContext; // used to toast
  private String  mUrl;
  // private static UserManDownload running = null; // WARNING do not place context in a static field
  private static boolean running = false; 

  UserManDownload( Context context, String url )
  {
    mContext = context;
    mUrl     = url;
  }

// -------------------------------------------------------------------
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
        byte buffer[] = new byte[4096];
        InputStream in = http.getInputStream();
        ZipInputStream zin = new ZipInputStream( in );
        ZipEntry ze = null;
	File dir = new File( TDPath.getManPath() );
	if ( ! dir.exists() ) { dir.mkdirs(); }

        while ( ( ze = zin.getNextEntry() ) != null ) {
          String name = ze.getName();
          if ( ze.isDirectory() ) { // not really an error: just neglect
            // TDLog.Error( "Zip dir entry \"" + name + "\"" );
          } else {
            // TDLog.Error( "Zip entry \"" + name + "\"" );
            int pos = name.lastIndexOf('/');
	    if ( pos > 0 ) name = name.substring(pos+1);
	    if ( ! name.startsWith("README") ) {
	      FileOutputStream fos = new FileOutputStream( TDPath.getManFile( name ) );
              int c;
              while ( ( c = zin.read( buffer ) ) != -1 ) {
                fos.write(buffer, 0, c);
              }
              fos.close();
	    }
	  }
	}
      }
      http.disconnect();
      ret = 1;
    } catch ( MalformedURLException e1 ) {
      TDLog.Error( "ERROR bad URL: " + e1.toString() );
    } catch ( IOException e2 ) {
      TDLog.Error( "ERROR I/O exception: " + e2.toString() );
    }
    return ret;
  }

  @Override
  protected void onProgressUpdate( Integer... values)
  {
    super.onProgressUpdate( values );
  }

  @Override
  protected void onPostExecute( Integer res )
  {
    if ( res != null ) {
      int r = res.intValue();
      if ( r == 1 ) { // success
	TDToast.make( mContext, R.string.user_man_ok );
      } else { // failed
	TDToast.make( mContext, R.string.user_man_fail );
      }
    }
    unlock();
  }

  private synchronized boolean lock()
  {
    // if ( running != null ) return false;
    // running = this;
    if ( running ) return false;
    running = true;
    return true;
  }

  private synchronized void unlock()
  {
    // if ( running == this ) running = null;
    if ( running ) running = false;
  }

}
