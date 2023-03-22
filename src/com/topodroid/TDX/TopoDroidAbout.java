/* @file TopoDroidAbout.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid about dialog
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDVersion;
import com.topodroid.help.UserManualActivity;
import com.topodroid.help.TDVersionCheck;

import android.content.Context;
// import android.content.Intent;

import android.app.Dialog;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
// import android.net.Uri;

class TopoDroidAbout extends Dialog
                     implements OnClickListener
{
  // private Button mBTok;
  // private Button mBTman;
  private Context mContext;
  // private MainWindow mParent;
  private int mSetup;

  int nextSetup() { return mSetup + 1; }

  TopoDroidAbout( Context context, MainWindow parent, int setup )
  {
    super( context );
    mContext = context;
    // mParent  = parent;
    mSetup   = setup;
    setContentView(R.layout.welcome);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( String.format( context.getResources().getString(R.string.welcome_title), TDVersion.fullString() ) );

    TextView version = (TextView) findViewById( R.id.TextView02 );
    version.setText( String.format( context.getResources().getString(R.string.welcome_db_version), TDVersion.DATABASE_VERSION, TDVersion.DEVICE_DATABASE_VERSION ) );

    ((Button)findViewById(R.id.btn_ok)).setOnClickListener( this );
    ((Button)findViewById(R.id.btn_manual)).setOnClickListener( this );
    // ((Button)findViewById(R.id.btn_version)).setOnClickListener( this );
    (new TDVersionCheck()).execute();
  }

  // @Override
  // public void onDismiss()
  // {
  //   if ( mSetup >= 0 ) mParent.doNextSetup( mSetup + 1 );
  // }

  @Override
  public void onClick( View v ) 
  {
    if ( v.getId() == R.id.btn_manual ) {
      dismiss();
      UserManualActivity.showHelpPage( mContext, null );
      return;
    // } else if ( v.getId() == R.id.btn_version ) {
    //   (new TDVersionCheck()).execute();
    //   return;  
    // } else if ( v.getId() == R.id.btn_ok ) {
    }
    dismiss();
  }
  
}
