/* @file PlotRenameDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey Rename / Delete dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
// import android.view.ViewGroup.LayoutParams;


class PlotRenameDialog extends MyDialog
                       implements View.OnClickListener
                       , View.OnLongClickListener
{
  private EditText mEtName;
  private EditText mEtStation;
  private Button   mBtnRename;
  private Button   mBtnBack;
  private Button   mBtnDelete;
  private Button   mBtnSplit;
  // private Button   mBtnMerge;
  private CheckBox mCBcopy;

  private final DrawingWindow mParent;
  // private final TopoDroidApp mApp;

  PlotRenameDialog( Context context, DrawingWindow parent /*, TopoDroidApp app */ )
  {
    super( context, R.string.PlotRenameDialog );
    mParent = parent;
    // mApp    = app;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.plot_rename_dialog, R.string.title_plot_rename );
    
    mBtnRename = (Button) findViewById(R.id.btn_rename );
    mBtnSplit  = (Button) findViewById(R.id.btn_split );
    // mBtnMerge  = (Button) findViewById(R.id.btn_merge );
    mBtnDelete = (Button) findViewById(R.id.btn_delete );
    mBtnBack   = (Button) findViewById(R.id.btn_back );
    mCBcopy    = (CheckBox) findViewById( R.id.cb_copy );

    mEtName = (EditText) findViewById( R.id.et_name );
    mEtStation = (EditText) findViewById( R.id.et_station );
    mEtName.setText( mParent.getPlotName( ) );
    mEtStation.setText( mParent.getPlotStation( ) );

    mBtnRename.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );
    mBtnDelete.setOnClickListener( this );
    if ( TDLevel.overExpert && TDSetting.mPlotSplit ) {
      // mBtnMerge.setOnClickListener( this );
      mBtnSplit.setOnClickListener( this );
      mBtnSplit.setOnLongClickListener( this );
      // mEtStation.setInputType( android.text.InputType.TYPE_NULL );
    } else {
      mCBcopy.setVisibility( View.GONE );
      mBtnSplit.setVisibility( View.GONE );
      // mBtnMerge.setVisibility( View.GONE );
      // mEtStation.setInputType( android.text.InputType.TYPE_NULL );
    }
  }

  @Override
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;

    if ( b == mBtnRename ) {
      String name = mEtName.getText().toString();
      INewPlot maker = TopoDroidApp.mShotWindow; // FIXME
      if ( maker.hasSurveyPlot( name ) ) {
        mEtName.setError( mContext.getResources().getString( R.string.plot_duplicate_name ) );
        return;
      }
      mParent.renamePlot( mEtName.getText().toString() );
    } else if ( b == mBtnBack ) {
      /* nothing */
    } else if ( b == mBtnDelete ) {
      mParent.askDelete();
    } else if ( TDSetting.mPlotSplit && b == mBtnSplit ) {
      if ( ! handleSplit( true ) ) return;
    // } else if ( b == mBtnMerge ) {
    //   mParent.mergePlot();
    }
    dismiss();
  }

  @Override
  public boolean onLongClick(View v) // called only on mBtnSplit
  {
    handleSplit( false );
    return true;
  }

  private boolean handleSplit( boolean warning )
  {
    INewPlot maker = TopoDroidApp.mShotWindow; // FIXME
    String name = mEtName.getText().toString();
    if ( maker.hasSurveyPlot( name ) ) {
      mEtName.setError( mContext.getResources().getString( R.string.plot_duplicate_name ) );
      return false;
    }
    String station = mEtStation.getText().toString();
    // if ( warning && ! maker.hasSurveyStation( station ) ) {
    //   mEtStation.setError( mContext.getResources().getString( R.string.error_station_non_existing ) );
    //   return false;
    // }
    mParent.splitPlot( name, station, ! mCBcopy.isChecked() ); // not mCBcopy == remove
    return true;
  }

}



