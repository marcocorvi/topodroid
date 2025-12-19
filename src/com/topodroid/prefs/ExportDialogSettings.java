/* @file ExportDialogSettings.java
 *
 * @author marco corvi
 * @date jul 2020
 *
 * @brief TopoDroid settings export/import dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.prefs;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.TDX.R;

import android.os.Bundle;
// import android.os.AsyncTask;
import android.content.Context;
import android.content.SharedPreferences;

import android.widget.Button;
import android.widget.CheckBox;

import android.view.View;

public class ExportDialogSettings extends MyDialog
                   implements View.OnClickListener
{
  private TDPrefActivity mParent;
  private SharedPreferences mPrefs;
  private Button   mBtnExport;
  private Button   mBtnImport;
  // private Button   mBtnBack;
  private CheckBox mCBfunctional;
  private int mTitle;
  int mFlag = 0;

  /** cstr
   * @param context context
   * @param parent  parent activity
   * @param prefs   shared preferences
   * @param title   title resource index
   */
  public ExportDialogSettings( Context context, TDPrefActivity parent, SharedPreferences prefs, int title )
  {
    super( context, null, R.string.ExportSettings ); // null app
    mParent = parent;
    mPrefs  = prefs;
    mTitle  = title;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.export_dialog_settings, mTitle );

    // mCBfunctional = (CheckBox) findViewById( R.id.cb_functional );
    // mCBfunctional.setChecked( true );

    mBtnExport = (Button) findViewById(R.id.button_export );
    mBtnImport = (Button) findViewById(R.id.button_import );
    mBtnExport.setOnClickListener( this );
    mBtnImport.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_back );
    // mBtnBack.setOnClickListener( this );
    ( (Button) findViewById(R.id.button_back ) ).setOnClickListener( this );
  }

  /** implements click listener
   * @param v tapped view
   */
  @Override
  public void onClick(View v) 
  {
    // TDLog.v( "CWD Selected " + mSelected );
    mFlag = 0;
    if ( ((CheckBox) findViewById( R.id.cb_gen )).isChecked() ) mFlag |=    1;
    if ( ((CheckBox) findViewById( R.id.cb_ui  )).isChecked() ) mFlag |=    2;
    if ( ((CheckBox) findViewById( R.id.cb_data)).isChecked() ) mFlag |=    4;
    if ( ((CheckBox) findViewById( R.id.cb_plot)).isChecked() ) mFlag |=    8;
    if ( ((CheckBox) findViewById( R.id.cb_ui  )).isChecked() ) mFlag |=   16;
    if ( ((CheckBox) findViewById( R.id.cb_io  )).isChecked() ) mFlag |=   32;
    if ( ((CheckBox) findViewById( R.id.cb_bt  )).isChecked() ) mFlag |=   64;
    if ( ((CheckBox) findViewById( R.id.cb_cal )).isChecked() ) mFlag |=  128;
    if ( ((CheckBox) findViewById( R.id.cb_xt  )).isChecked() ) mFlag |=  256;

    Button b = (Button)v;
    if ( b == mBtnExport ) {
      mParent.exportSettings( mPrefs, mFlag );
    } else if ( b == mBtnImport ) {
      mParent.importSettings( mPrefs, mFlag );
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

}


