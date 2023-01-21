/* @file TDVersionCheck.java
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

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
// import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

import java.io.InputStreamReader;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
// import android.content.Context;

public class TDVersionCheck extends AsyncTask< Void, Integer, Integer >
{
  // private final Context mContext; // unused 
  private static final String  mUrl = "https://raw.githubusercontent.com/marcocorvi/speleoapks/main/tdversion.txt";
  private static boolean running = false; 

  /** cstr
   */
  public TDVersionCheck( /* Context context, */ )
  {
    // mContext = context;
  }

  // -------------------------------------------------------------------
  /** execute tha man pages download in background
   * @param args ...
   */
  @Override
  protected Integer doInBackground( Void... args )
  {
    if ( ! lock() ) return 0;
    try {
      int  ret = 0;
      URL url = new URL( mUrl );
      URLConnection connection = url.openConnection();
      HttpURLConnection http = (HttpURLConnection) connection;
      int response = http.getResponseCode();
      if ( response == HttpURLConnection.HTTP_OK ) {
        InputStreamReader isr = new InputStreamReader( http.getInputStream() );
        int version = 0;
        int ch;
        try {
          while ( (ch = isr.read() ) != -1 ) { 
            // TDLog.v("version char " + ch );
            if ( ch < '0' ) break;
            version = 10 * version + (int)( ch - '0' );
          }
        } catch ( IOException e ) {
          TDLog.Error( e.getMessage() );
        }
        // TDLog.v( "last version " + version + " current " + TDVersion.VERSION_CODE );
        isr.close();
        ret = version;
      } else {
        // TDLog.Error("HTTP error : " + response );
        ret = 0;
      } 
      http.disconnect();
      return ret;
    } catch ( MalformedURLException e1 ) {
      // TDLog.Error( "ERROR bad URL: " + e1.toString() );
    } catch ( IOException e2 ) {
      // TDLog.Error( "ERROR I/O exception: " + e2.toString() );
    }
    return 0;
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
      if ( res <= 0 ) {
	// TDToast.make( R.string.version_check_failed );
      } else {
        if ( res < TDVersion.VERSION_CODE ) { // web-version smaller than current
	  // TDToast.make( R.string.version_check_latest );
        } else {
	  TDToast.make( TDInstance.formatString(R.string.version_check_newer, res ) );
        }
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

