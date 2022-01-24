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

// import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDPrefActivity;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.BrushManager;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Configuration;

import android.util.DisplayMetrics;

import java.util.Locale;

public class TDLocale
{
  public static final boolean FIXME_LOCALE = false; // 6.0.33 true;

  static Locale mLocale = null;
  static String mLocaleStr = null;

  /** @return the current locale
   */
  public static Locale getLocale() { return mLocale; }

  /** @return the locale country ISO code
   */
  public static String getLocaleCode() { return mLocale.toString().substring(0,2); }

  /** reset the locale
   * @note called by MainWindow
   */
  public static void resetTheLocale( )
  {
    // if ( ! TDSetting.isFlagLocale() ) return;
    TDLog.v( "LOCALE reset <" + ((mLocaleStr == null)? "null" : mLocaleStr) + "> " + ((mLocale == null)? "null" : mLocale.toString() ) );

    // mLocale = (mLocaleStr.equals(TDString.EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    Resources res = TDInstance.getResources();
    DisplayMetrics dm = res.getDisplayMetrics();
    // if ( TDandroid.ABOVE_API_16 ) { // minSdkVersion is 18
      Configuration conf = new Configuration( res.getConfiguration() );
      conf.setLocale( mLocale );
      // TDInstance.context = TDInstance.context.createConfigurationContext( conf );
      res.updateConfiguration( conf, dm );
    // } else {
    //   Configuration conf = res.getConfiguration();
    //   conf.locale = mLocale; 
    //   res.updateConfiguration( conf, dm );
    // }
  }

  /** set the locale
   * @param locale   country ISO code
   * @note called by TDSetting
   */
  public static void setTheLocale( String locale )
  {
    mLocaleStr = locale;
    mLocale = (mLocaleStr.equals(TDString.EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    TDLog.v( "LOCALE set <" + mLocaleStr + "> " + mLocale.toString() );

    // mLocale = (mLocaleStr.equals(TDString.EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    Resources res = TDInstance.getResources();
    DisplayMetrics dm = res.getDisplayMetrics();
    // if ( TDandroid.ABOVE_API_16 ) { // minSdkVersion is 18
      Configuration conf = new Configuration( res.getConfiguration() );
      conf.setLocale( mLocale );
      // TDInstance.context = TDInstance.context.createConfigurationContext( conf );
      res.updateConfiguration( conf, dm );
    // } else {
    //   Configuration conf = res.getConfiguration();
    //   conf.locale = mLocale; 
    //   res.updateConfiguration( conf, dm );
    // }
    // TDSetting.clearFlagLocale();
  }

}
