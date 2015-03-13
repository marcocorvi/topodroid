/** @file PlotSaveDialog.java
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


public class PlotSaveDialog extends Dialog
                            implements View.OnClickListener
{
  private Button   mBtnTh2;
  private Button   mBtnPng;
  private Button   mBtnDxf;
  private Button   mBtnCsx;
  private Button   mBtnSvg;
  // private Button   mBtnCancel;

  private DrawingActivity mParent;

  PlotSaveDialog( Context context, DrawingActivity parent )
  {
    super( context );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.plot_save_dialog);
    mBtnTh2 = (Button) findViewById(R.id.btn_th2 );
    mBtnPng = (Button) findViewById(R.id.btn_png );
    mBtnDxf = (Button) findViewById(R.id.btn_dxf );
    mBtnCsx = (Button) findViewById(R.id.btn_csx );
    mBtnSvg = (Button) findViewById(R.id.btn_svg );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );

    mBtnTh2.setOnClickListener( this );
    mBtnPng.setOnClickListener( this );
    mBtnDxf.setOnClickListener( this );
    if ( mParent.isSection() ) {
      mBtnCsx.setVisibility( View.GONE );
    } else {
      mBtnCsx.setOnClickListener( this );
    }
    mBtnSvg.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );

    setTitle( R.string.title_plot_save );
  }

  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mBtnTh2 ) {
      mParent.saveTh2();
    } else if ( b == mBtnPng ) {
      mParent.savePng();
    } else if ( b == mBtnDxf ) {
      mParent.saveWithExt( "dxf" );
    } else if ( b == mBtnCsx ) {
      mParent.saveCsx();
    } else if ( b == mBtnSvg ) {
      mParent.saveWithExt( "svg" );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}


