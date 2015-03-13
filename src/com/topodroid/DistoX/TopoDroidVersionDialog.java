/** @file TopoDroidVersionDialog.java
 *
 * @author marco corvi
 * @date may 2014
 *
 * @brief TopoDroid version-update dialog
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 * 20120521 created
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.content.Intent;

import android.app.Dialog;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Toast;

class TopoDroidVersionDialog extends Dialog
                     implements OnClickListener
{
  private Button mBTok;
  private Button mBTcancel;
  private Context mContext;
  TopoDroidApp mApp;

  TopoDroidVersionDialog( Context context, TopoDroidApp app )
  {
    super( context );
    mContext = context;
    mApp = app;
    setContentView(R.layout.topodroid_version_dialog );
    setTitle( context.getResources().getString(R.string.version_title) );

    mBTcancel = (Button)findViewById(R.id.version_cancel);
    mBTok     = (Button)findViewById(R.id.version_ok);

    mBTcancel.setOnClickListener( this );
    mBTok.setOnClickListener( this );
  }
  
  @Override
  public void onClick(View v) 
  {
    switch (v.getId()){
      case R.id.version_ok:
        mApp.installSymbols( true ); // overwrite symbols
        break;
    }
    dismiss();
  }
}
