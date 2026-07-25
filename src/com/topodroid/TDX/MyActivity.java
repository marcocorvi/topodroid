/* @file MyActivity.java
 *
 * @author marco corvi
 * @date oct 2014
 *
 * @brief TopoDroid 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;

import android.app.Activity;
import android.os.Bundle;



import androidx.annotation.RequiresApi;

abstract class MyActivity extends Activity
{
  protected Activity mActivity = null;

  

  @Override
  public void onBackPressed()
  {
    TDLog.v("*** my Activity BACK pressed");
    // finish();
  }

// issue 170
  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
   
  }

  @Override
  protected void onDestroy() 
  {
    
    super.onDestroy();
  }

  
  
}
