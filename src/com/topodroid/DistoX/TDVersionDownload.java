/* @file TDVersionDownload.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid Google lay version download
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

// import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.os.AsyncTask;
import android.content.Context;
import android.content.SharedPreferences;

class TDVersionDownload extends AsyncTask< Void, Integer, String >
{
  private final String current_pattern = "<div[^>]*?>Current\\sVersion</div><span[^>]*?>(.*?)><div[^>]*?(.*?)><span[^>]*?>(.*?)</span>";
  private final String app_pattern = "htlgb\">([^<]*)</s";
  private final String gplay_url = "https://play.google.com/store/apps/details?id=com.topodroid.DistoX&hl=en";

  private final Context mContext; // used to toast
  // private static boolean running = false; 
  private SharedPreferences mPrefs;

  TDVersionDownload( Context context, SharedPreferences prefs )
  {
    mContext = context;
    mPrefs   = prefs;
  }

  private String getAppVersion( String pattern_str, String input_str )
  {
    try {
      Pattern pattern = Pattern.compile( pattern_str );
      if ( pattern == null ) return null;
      Matcher matcher = pattern.matcher( input_str );
      if ( matcher != null && matcher.find() ) return matcher.group(1);
    } catch (PatternSyntaxException ex ) {
      ex.printStackTrace();
    }
    return null;
  }

  private String getGPlayAppVersion( String url_str )
  {
    try {
      final URL url = new URL( url_str );
      URLConnection conn = url.openConnection();
      if ( conn == null ) return null;
      conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6");
      BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
      // if ( br == null ) return null; // br always non null
      StringBuilder url_data = new StringBuilder();
      String str;
      while ( ( str = br.readLine() ) != null ) url_data.append( str );
      String version = getAppVersion( current_pattern, url_data.toString() );
      if ( version == null ) return null;
      return getAppVersion( app_pattern, version );
    } catch ( MalformedURLException e1 ) {
      TDLog.Error( "ERROR bad URL: " + e1.toString() );
    } catch ( IOException e2 ) {
      TDLog.Error( "ERROR I/O exception: " + e2.toString() );
    }
    return null;
  }

// -------------------------------------------------------------------
  @Override
  protected String doInBackground( Void... arg)
  {
    // if ( ! lock() ) return null;
    return getGPlayAppVersion( gplay_url );
  }

  @Override
  protected void onProgressUpdate( Integer... values)
  {
    super.onProgressUpdate( values );
  }

  @Override
  protected void onPostExecute( String res )
  {
    if ( res != null ) {
      // get next alert date
      long now = -1L;
      long time = TDSetting.getLongPreference( mPrefs, "VERSION_ALERT_TIME", 0 );
      boolean check = false;
      if ( time >= 0 ) {
        now  = System.currentTimeMillis();
	if ( now >= time ) {
	  now += 3600 * 24 * 30 * 1000; // 30 days
	  check = true;
	}
      }
      if ( check ) {
        // String[] this_version = TopoDroidApp.VERSION.split("\\.");
        // String[] play_version = res.split("\\.");
        String this_version = TopoDroidApp.VERSION.substring(0,3);
        String play_version = res.substring(0,3);
        // int cmp = res.compareTo( TopoDroidApp.VERSION );
        int cmp = play_version.compareTo( this_version );
        if ( cmp > 0 ) {
	  // NOTE alert dialog could make "now" -1
          TDToast.make( "Newer version on Google Play: " + res );
        // } else {
        //   TDToast.make( "Current version on Google Play: " + res );
        }
        if ( now > 0 ) {
	  TDSetting.setPreference( mPrefs, "VERSION_ALERT_TIME", now );
	}
      }
    }
    // unlock();
  }

  // private synchronized boolean lock()
  // {
  //   if ( running ) return false;
  //   running = true;
  //   return true;
  // }

  // private synchronized void unlock()
  // {
  //   if ( running ) running = false;
  // }

}
