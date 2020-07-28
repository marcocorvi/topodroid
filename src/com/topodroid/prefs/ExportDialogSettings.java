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

import com.topodroid.ui.MyDialog;
import com.topodroid.DistoX.TDPath;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;

import android.util.Log;

import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.SharedPreferences;

import android.widget.Button;
import android.widget.CheckBox;

import android.view.View;

public class ExportDialogSettings extends MyDialog
                   implements View.OnClickListener
{
  private Button   mBtnExport;
  private Button   mBtnImport;
  // private Button   mBtnBack;
  private CheckBox mCBfunctional;
  private int mTitle;

  public ExportDialogSettings( Context context, int title )
  {
    super( context, R.string.ExportSettings );
    mTitle = title;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.export_dialog_settings, mTitle );

    mCBfunctional = (CheckBox) findViewById( R.id.cb_functional );
    mCBfunctional.setChecked( true );

    mBtnExport = (Button) findViewById(R.id.button_export );
    mBtnImport = (Button) findViewById(R.id.button_import );
    mBtnExport.setOnClickListener( this );
    mBtnImport.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_back );
    // mBtnBack.setOnClickListener( this );
    ( (Button) findViewById(R.id.button_back ) ).setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    // Log.v("DistoX-C3D", "Selected " + mSelected );
    Button b = (Button)v;
    if ( b == mBtnExport ) {
      ( new AsyncTask< Void, Void, Boolean >() { // FIXME static or LEAK
        @Override
        protected Boolean doInBackground(Void... v)
        {
          // Log.v("DistoX", "export settings");
          return TDSetting.exportSettings();
        }
        @Override
        protected void onPostExecute( Boolean v )
        {
          if ( v ) {
            TDToast.make( String.format( mContext.getResources().getString( R.string.exported_settings ), TDPath.getSettingsPath() ) );
          } else {
            TDToast.makeWarn( R.string.export_settings_failed );
          }
        }
      }).execute();

    } else if ( b == mBtnImport ) {

      final boolean functional = mCBfunctional.isChecked();
      final boolean all = ! functional;
      ( new AsyncTask< Void, Void, Boolean >() { // FIXME static or LEAK
          TDPrefHelper hlp = new TDPrefHelper( mContext );
          SharedPreferences prefs = hlp.getSharedPrefs();
        @Override
        protected Boolean doInBackground(Void... v)
        {
          // Log.v("DistoX", "import settings - functional " + functional );
          return TDSetting.importSettings( prefs, all );
        }
        @Override
        protected void onPostExecute( Boolean v )
        {
          if ( v ) {
            TDToast.make( R.string.imported_settings );
          } else {
            TDToast.makeWarn( R.string.imported_settings_failed );
          }
        }
      }).execute();
      
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

}


