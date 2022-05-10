/* @file TglPerms.java
 *
 * @author marco corvi
 * @date may 2018
 *
 * @brief Cave3D permission dialog
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

import android.content.Context;
// import android.content.Intent;

// import android.app.Dialog;
// import android.widget.Button;
// import android.widget.TextView;
// import android.view.View;
// import android.view.View.OnClickListener;

// import android.view.ViewGroup.LayoutParams;
// import android.net.Uri;

import android.widget.Toast;

class TglPerms // extends Dialog
               // implements OnClickListener
{
  // private Button mBTok;
  // private Context mContext; // INHERITED

  // TglPerms( Context context, int check_perms )
  static void toast( Context context, int check_perms )
  {
    // super( context );
    // mContext = context;
    // TDLog.v("TopoGL-PERM dialog " + check_perms );

    // setContentView( R.layout.cave3d_perms );
    // setTitle( String.format( context.getResources().getString(R.string.cwelcome_title), TopoGL.VERSION ) );

    StringBuilder sb = new StringBuilder();
    if ( check_perms < 0 ) {
      sb.append( context.getResources().getString( R.string.cperms_mandatory ));
      sb.append( "\nWRITE_EXTERNAL_STORAGE" );
      Toast.makeText( context, sb.toString(), Toast.LENGTH_LONG ).show();
    // } else if ( check_perms > 0 ) {
    //   sb.append( context.getResources().getString( R.string.cperms_optional ) );
    //   sb.append( "\nACCESS_FILE_LOCATION" );
    }

    // TextView tv = (TextView)findViewById( R.id.text_perms );
    // tv.setText( sb.toString() );

    // Button btn_ok = (Button)findViewById(R.id.btn_ok);
    // btn_ok.setOnClickListener( this );
  }

  // @Override
  // public void onClick( View v )
  // {
  //   dismiss();
  // }
  
}
