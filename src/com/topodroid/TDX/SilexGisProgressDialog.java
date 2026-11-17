/* @file SilexGisProgressDialog.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: progress of the upload
 *
 * Cancel stops the sending of the files, or only the watching of the processing:
 * the server goes on processing an upload it has received.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;

import android.os.Bundle;
import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;

import android.view.View;

class SilexGisProgressDialog extends MyDialog
                             implements View.OnClickListener
{
  private final Runnable mOnCancel;
  private TextView    mTVstep;
  private TextView    mTVdetail;
  private ProgressBar mProgress;
  private Button      mBtnCancel;

  /** cstr
   * @param context    context
   * @param on_cancel  run when the user taps Cancel
   */
  SilexGisProgressDialog( Context context, Runnable on_cancel )
  {
    super( context, null, 0 ); // 0: no help resource
    mOnCancel = on_cancel;
  }

  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout( R.layout.silexgis_progress_dialog, R.string.silexgis_upload_title );
    setCancelable( false );            // the back key and a tap outside do not hide a running upload
    setCanceledOnTouchOutside( false );
    mTVstep   = (TextView) findViewById( R.id.silexgis_step );
    mTVdetail = (TextView) findViewById( R.id.silexgis_detail );
    mProgress = (ProgressBar) findViewById( R.id.silexgis_progress );
    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );
  }

  /** show the progress
   * @param step      step text
   * @param percent   progress [0..100], negative for none
   * @param detail    detail text (can be null)
   */
  void setProgress( String step, int percent, String detail )
  {
    if ( mTVstep == null ) return; // not created yet
    if ( step != null ) mTVstep.setText( step );
    mTVdetail.setText( ( detail == null )? "" : detail );
    mProgress.setProgress( Math.max( 0, Math.min( 100, percent ) ) );
  }

  @Override
  public void onClick( View v )
  {
    if ( v.getId() == R.id.button_cancel ) {
      mBtnCancel.setEnabled( false );
      if ( mOnCancel != null ) mOnCancel.run();
    }
  }

}
