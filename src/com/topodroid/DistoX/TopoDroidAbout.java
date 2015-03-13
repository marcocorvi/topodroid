/** @file TopoDroidAbout.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid about dialog
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
import android.content.ActivityNotFoundException;

import android.app.Dialog;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.net.Uri;

import android.widget.Toast;

class TopoDroidAbout extends Dialog
                     // implements OnClickListener
{
  // private Button mBTok;
  // private Button mBTman;
  // private Button mBTsymbol;
  private Context mContext;

  TopoDroidAbout( Context context )
  {
    super( context );
    mContext = context;
    setContentView(R.layout.welcome);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( String.format( context.getResources().getString(R.string.welcome_title), TopoDroidApp.VERSION ) );

    // mBTman    = (Button)findViewById(R.id.btn_man);
    // mBTsymbol = (Button)findViewById(R.id.btn_symbol);
    // mBTok     = (Button)findViewById(R.id.btn_ok);

    // mBTman.setOnClickListener( this );
    // mBTsymbol.setOnClickListener( this );
    // mBTok.setOnClickListener( this );
  }
  
  // @Override
  // public void onClick(View v) 
  // {
  //   switch (v.getId()){
  //     // case R.id.btn_man:
  //     //   try {
  //     //     // TopoDroidHelp.show( this, R.string.help_topodroid );
  //     //     Intent pdf = new Intent( Intent.ACTION_VIEW, Uri.parse( TopoDroidApp.mManual ) );
  //     //     mContext.startActivity( pdf );
  //     //   } catch ( ActivityNotFoundException e ) {
  //     //     Toast.makeText( mContext, "No pdf viewer app", Toast.LENGTH_SHORT ).show();
  //     //   }
  //     //   break; 
  //     case R.id.btn_symbol:
  //       int cnt = TopoDroidApp.symbolsSync();
  //       Toast.makeText( mContext, "Installed " + cnt + "symbols", Toast.LENGTH_SHORT ).show();
  //       break;
  //   }
  //   dismiss();
  // }
}
