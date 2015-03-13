/** @file CalibExportDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey export dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130213 created
 */
package com.topodroid.DistoX;

import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;


public class CalibExportDialog extends Dialog
                               implements View.OnClickListener
{
  private Button   mBtnCsv;
  // private Button   mBtnCancel;

  private CalibActivity mParent;

  CalibExportDialog( Context context, CalibActivity parent )
  {
    super( context );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.calib_export_dialog);
    mBtnCsv = (Button) findViewById(R.id.btn_csv );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );

    mBtnCsv.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );

    setTitle( R.string.title_calib_export );
  }

  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mBtnCsv ) {
      mParent.doExport( TopoDroidConst.DISTOX_EXPORT_CSV, true );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}


