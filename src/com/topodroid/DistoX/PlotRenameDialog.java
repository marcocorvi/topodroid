/** @file PlotRenameDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey Rename dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;


public class PlotRenameDialog extends MyDialog
                              implements View.OnClickListener
{
  private EditText mEtName;
  private Button   mBtnOK;
  private Button   mBtnDelete;

  private DrawingActivity mParent;
  private TopoDroidApp mApp;

  PlotRenameDialog( Context context, DrawingActivity parent, TopoDroidApp app )
  {
    super( context, R.string.PlotRenameDialog );
    mParent = parent;
    mApp    = app;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.plot_rename_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
    mBtnOK  = (Button) findViewById(R.id.btn_ok );
    mBtnDelete = (Button) findViewById(R.id.btn_delete );

    mBtnOK.setOnClickListener( this );
    mBtnDelete.setOnClickListener( this );

    mEtName = (EditText) findViewById( R.id.et_name );
    mEtName.setText( mParent.getPlotName( ) );

    setTitle( R.string.title_plot_rename );
  }

  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      mParent.renamePlot( mEtName.getText().toString() );
    } else if ( b == mBtnDelete ) {
      mParent.askDelete();
    }
    dismiss();
  }

}



