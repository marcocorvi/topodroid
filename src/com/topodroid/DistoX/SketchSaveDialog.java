/** @file SketchSaveDialog.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid sketch save dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130323 created
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


public class SketchSaveDialog extends Dialog
                            implements View.OnClickListener
{
  private Button   mBtnTh3;
  private Button   mBtnDxf;
  // private Button   mBtnCancel;

  private SketchActivity mParent;

  SketchSaveDialog( Context context, SketchActivity parent )
  {
    super( context );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.sketch_save_dialog);
    mBtnTh3 = (Button) findViewById(R.id.btn_th3 );
    mBtnDxf = (Button) findViewById(R.id.btn_dxf );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );

    mBtnTh3.setOnClickListener( this );
    mBtnDxf.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );

    setTitle( R.string.title_plot_save );
  }

  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mBtnTh3 ) {
      mParent.doSaveTh3( false );
    } else if ( b == mBtnDxf ) {
      mParent.doSaveDxf();
    // } else {
      // setResult( RESULT_CANCELED );
    }
    dismiss();
  }

}


