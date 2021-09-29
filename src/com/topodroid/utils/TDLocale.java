/* @file TDLocale.java
 *
 * @author marco corvi
 * @date feb 2021 (extracted from TopoDroidApp)
 *
 * @brief TopoDroid locale
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDPrefActivity;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.BrushManager;

import android.os.Build;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Configuration;

import android.util.DisplayMetrics;

import java.util.Locale;

public class TDLocale
{
  public static final boolean FIXME_LOCALE = true;

  static Locale mLocale;
  static String mLocaleStr;

  public static Locale getLocale() { return mLocale; }

  public static String getLocaleCode() { return mLocale.toString().substring(0,2); }

  // called by MainWindow
  public static void /* Context */ resetLocale( )
  {
    // mLocale = (mLocaleStr.equals(TDString.EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    Resources res = TDInstance.getResources();
    DisplayMetrics dm = res.getDisplayMetrics();
    if ( android.os.Build.VERSION.SDK_INT >= 17 ) {
      Configuration conf = new Configuration( res.getConfiguration() );
      conf.setLocale( mLocale );
      // TDInstance.context = TDInstance.context.createConfigurationContext( conf );
      res.updateConfiguration( conf, dm );
    } else {
      Configuration conf = res.getConfiguration();
      conf.locale = mLocale; 
      res.updateConfiguration( conf, dm );
    }
    // return TDInstance.context;
  }

  // called by TDSetting
  public static void setLocale( String locale )
  {
    mLocaleStr = locale;
    mLocale = (mLocaleStr.equals(TDString.EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    // TDLog.v( "set locale str <" + mLocaleStr + "> " + mLocale.toString() );
    resetLocale( );
  }

}
