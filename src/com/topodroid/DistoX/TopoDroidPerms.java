/* @file TopoDroidPerms.java
 *
 * @author marco corvi
 * @date may 2018
 *
 * @brief TopoDroid permission dialog
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDVersion;


import android.content.Context;
// import android.content.Intent;

// import android.app.Dialog;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;
// import android.net.Uri;

class TopoDroidPerms extends MyDialog
                     implements OnClickListener
{
  // private Button mBTok;
  // private Context mContext; // INHERITED

  TopoDroidPerms( Context context, int check_perms )
  {
    super( context, R.string.TopoDroidPerms );
    // mContext = context;
    initLayout(R.layout.topodroid_perms,
      String.format( context.getResources().getString(R.string.welcome_title), TDVersion.string() ) );

    StringBuilder sb = new StringBuilder();
    if ( check_perms < 0 ) {
      sb.append( context.getResources().getString( R.string.perms_mandatory ));
      sb.append( "\nWRITE_EXTERNAL_STORAGE" );
      sb.append( "\nBLUETOOTH" );
      sb.append( "\nBLUETOOTH_ADMIN" );
    } else if ( check_perms > 0 ) {
      sb.append( context.getResources().getString( R.string.perms_optional ) );
      if ( ( check_perms & 1 ) == 1 ) sb.append( context.getResources().getString( R.string.perm_location ) );
      if ( ( check_perms & 2 ) == 2 ) sb.append( context.getResources().getString( R.string.perm_camera ) );
      if ( ( check_perms & 4 ) == 4 ) sb.append( context.getResources().getString( R.string.perm_microphone ) );
    }
    TextView tv = (TextView)findViewById( R.id.text_perms );
    tv.setText( sb.toString() );

    Button btn_ok = (Button)findViewById(R.id.btn_ok);
    btn_ok.setOnClickListener( this );
  }

  @Override
  public void onClick( View v )
  {
    dismiss();
  }
  
}
