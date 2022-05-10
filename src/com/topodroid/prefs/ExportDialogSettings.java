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
import com.topodroid.utils.TDFile;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

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
  private TDPrefActivity mParent;
  private SharedPreferences mPrefs;
  private Button   mBtnExport;
  private Button   mBtnImport;
  // private Button   mBtnBack;
  private CheckBox mCBfunctional;
  private int mTitle;

  /** cstr
   * @param context context
   * @param parent  parent activity
   * @param prefs   shared prefrences
   * @param title   title resource index
   */
  public ExportDialogSettings( Context context, TDPrefActivity parent, SharedPreferences prefs, int title )
  {
    super( context, R.string.ExportSettings );
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

  /** implements click listener
   * @param v tapped view
   */
  @Override
  public void onClick(View v) 
  {
    // TDLog.v( "CWD Selected " + mSelected );
    Button b = (Button)v;
    if ( b == mBtnExport ) {
      ( new AsyncTask< Void, Void, Boolean >() { // FIXME static or LEAK
        @Override
        protected Boolean doInBackground(Void... v)
        {
          // TDLog.v("export settings");
          return TDSetting.exportSettings();
        }
        @Override
        protected void onPostExecute( Boolean v )
        {
          if ( v ) {
            TDToast.make( String.format( mContext.getResources().getString( R.string.exported_settings ), TDFile.getSettingsFile().getPath() ) );
          } else {
            TDToast.makeWarn( R.string.export_settings_failed );
          }
        }
      }).execute();

    } else if ( b == mBtnImport ) {

      final boolean functional = mCBfunctional.isChecked();
      final boolean all = ! functional;
      ( new AsyncTask< Void, Void, Boolean >() { // FIXME static or LEAK
        @Override
        protected Boolean doInBackground(Void... v)
        {
          TDLog.v("import settings - functional " + functional );
          return TDSetting.importSettings( mPrefs, all );
        }
        @Override
        protected void onPostExecute( Boolean v )
        {
          if ( v ) {
            TDToast.make( R.string.imported_settings );
          } else {
            TDToast.makeWarn( R.string.imported_settings_failed );
          }
          mParent.reloadPreferences();
        }
      }).execute();
      
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

}


