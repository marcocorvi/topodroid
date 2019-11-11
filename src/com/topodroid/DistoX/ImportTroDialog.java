/* @file ImportTroDialog.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid VisualTopo import options dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;

class ImportTroDialog extends MyDialog
                      implements View.OnClickListener
{
  private MainWindow mParent;
  private Button mBtnOK;
  private Button mBtnCancel;

  private CheckBox mCBlrud;
  private CheckBox mCBleg;

  private String mFilepath;

  ImportTroDialog( Context context, MainWindow parent, String filepath )
  {
    super( context, R.string.ImportTroDialog );
    mParent = parent;
    // mApp = app;
    mFilepath = filepath;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout( R.layout.import_tro_dialog, R.string.title_import_tro );

    TextView tv = (TextView) findViewById( R.id.tro_path );
    tv.setText( mFilepath );

    mCBlrud = (CheckBox) findViewById( R.id.tro_lrud );
    mCBleg  = (CheckBox) findViewById( R.id.tro_leg_first );

    mBtnOK     = (Button) findViewById(R.id.tro_ok);
    mBtnCancel = (Button) findViewById(R.id.tro_cancel);
    mBtnOK.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    // setTitle( R.string.title_calib );
  }

 
  // @Override
  public void onClick(View v) 
  {
    // TDLog.Log(  TDLog.LOG_INPUT, "ImportTroDialog onClick() " );
    Button b = (Button) v;
    hide();
    if ( b == mBtnOK ) {
      mParent.importTroFile( mFilepath, mCBlrud.isChecked(), mCBleg.isChecked() );
    // } else if ( b == mBtnCancel ) {
    }
    dismiss();
  }

}
