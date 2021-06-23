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
package com.topodroid.inport;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.R;
import com.topodroid.DistoX.MainWindow;

import java.io.InputStreamReader;

// import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;

public class ImportTroDialog extends MyDialog
                      implements View.OnClickListener
{
  private MainWindow mParent;
  private Button mBtnOK;
  private Button mBtnCancel;

  private CheckBox mCBlrud;
  private CheckBox mCBleg;
  private CheckBox mCBtrox;

  private String mFilepath;
  private InputStreamReader isr;

  public ImportTroDialog( Context context, MainWindow parent, InputStreamReader isr, String filepath )
  {
    super( context, R.string.ImportTroDialog );
    mParent = parent;
    // mApp = app;
    mFilepath = filepath;
    this.isr = isr;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout( R.layout.import_tro_dialog, R.string.title_import_tro );

    TextView tv = (TextView) findViewById( R.id.tro_path );
    tv.setText( mFilepath );

    mCBlrud = (CheckBox) findViewById( R.id.tro_lrud );
    mCBleg  = (CheckBox) findViewById( R.id.tro_leg_first );
    mCBtrox = (CheckBox) findViewById( R.id.tro_trox );
    mCBtrox.setChecked( TDSetting.mVTopoTrox );

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
      mParent.importTroFile( isr, mFilepath, mCBlrud.isChecked(), mCBleg.isChecked(), mCBtrox.isChecked() );
    // } else if ( b == mBtnCancel ) {
    }
    dismiss();
  }

}
