/* @file Path11Dialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid dialog to ask the user to move to Path11
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
// import android.content.Intent;

import android.app.Dialog;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

class Path11Dialog extends Dialog
                   implements OnClickListener
{
  private Context mContext;
  private TopoDroidApp mApp;

  Path11Dialog( Context context, TopoDroidApp app )
  {
    super( context );
    mContext = context;
    mApp     = app;
    setContentView(R.layout.path11);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( R.string.path11_title );

    ((Button)findViewById(R.id.btn_ok)).setOnClickListener( this );
  }

  @Override
  public void onClick( View v ) 
  {
    if ( v.getId() == R.id.btn_ok ) {
      if ( ((CheckBox)findViewById(R.id.btn_no_again)).isChecked() ) {
        mApp.setPath11NoAgain();
      }
    }
    dismiss();
  }
  
}
