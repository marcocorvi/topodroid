/* @file ImportDatDialog.java
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

class ImportDatDialog extends MyDialog
                      implements View.OnClickListener
{
  private MainWindow mParent;
  private Button mBtnOK;
  // private Button mBtnCancel;

  private CheckBox mCBlrud;
  private CheckBox mCBleg;
  private CheckBox mCBdiving;

  private String mFilepath;

  ImportDatDialog( Context context, MainWindow parent, String filepath )
  {
    super( context, R.string.ImportDatDialog );
    mParent = parent;
    // mApp = app;
    mFilepath = filepath;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout( R.layout.import_dat_dialog, R.string.title_import_dat );

    TextView tv = (TextView) findViewById( R.id.dat_path );
    tv.setText( mFilepath );

    mCBlrud = (CheckBox) findViewById( R.id.dat_lrud );
    mCBleg  = (CheckBox) findViewById( R.id.dat_leg_first );
    mCBdiving = (CheckBox) findViewById( R.id.dat_diving_datamode );

    mCBdiving.setChecked( TDSetting.mImportDatamode == SurveyInfo.DATAMODE_DIVING );

    mBtnOK     = (Button) findViewById(R.id.dat_ok);
    // mBtnCancel = (Button) findViewById(R.id.dat_cancel);
    mBtnOK.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );

    // setTitle( R.string.title_calib );
  }

 
  // @Override
  public void onClick(View v) 
  {
    // TDLog.Log(  TDLog.LOG_INPUT, "ImportDatDialog onClick() " );
    Button b = (Button) v;
    hide();
    if ( b == mBtnOK ) {
      int datamode = mCBdiving.isChecked()? SurveyInfo.DATAMODE_DIVING : SurveyInfo.DATAMODE_NORMAL;
      mParent.importDatFile( mFilepath, datamode, mCBlrud.isChecked(), mCBleg.isChecked() );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}
