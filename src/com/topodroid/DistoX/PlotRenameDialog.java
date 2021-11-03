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

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
import android.content.Context;

import android.widget.EditText;
// import android.widget.TextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;

class PlotRenameDialog extends MyDialog
                       implements View.OnClickListener
                       , View.OnLongClickListener
{
  private EditText mEtName;
  private EditText mEtOrigin;
  private Button   mBtnRename;
  private Button   mBtnBack;
  private Button   mBtnDelete;
  private Button   mBtnSplit;
  // private Button   mBtnMerge;
  private CheckBox mCBcopy;

  private final DrawingWindow mParent;
  private String mStation;
  private String mName;

  PlotRenameDialog( Context context, DrawingWindow parent /*, TopoDroidApp app */ )
  {
    super( context, R.string.PlotRenameDialog );
    mParent = parent;
    mStation = mParent.getPlotStation();
    mName    = mParent.getPlotName();
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
    mEtName.setText( mName );

    mEtOrigin = (EditText) findViewById( R.id.et_station );
    mEtOrigin.setText( mStation );
    // if ( ! TDLevel.overExpert ) {
      mEtOrigin.setInputType( 0 ); // 0 = not editable
    // }

    mBtnRename.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );
    mBtnDelete.setOnClickListener( this );
    if ( TDLevel.overExpert && TDSetting.mPlotSplit ) {
      // mBtnMerge.setOnClickListener( this );
      mBtnSplit.setOnClickListener( this );
      mBtnSplit.setOnLongClickListener( this );
    } else {
      mCBcopy.setVisibility( View.GONE );
      mBtnSplit.setVisibility( View.GONE );
      // mBtnMerge.setVisibility( View.GONE );
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
      if ( ! mName.equals( name ) ) {
        INewPlot maker = TopoDroidApp.mShotWindow; // FIXME
        if ( maker.hasSurveyPlot( name ) ) {
          mEtName.setError( mContext.getResources().getString( R.string.plot_duplicate_name ) );
          return;
        }
        // mParent.renamePlot( mEtName.getText().toString() );
        mParent.renamePlot( name );
      }

      // if ( TDLevel.overExpert ) {
      //   String station = mEtOrigin.getText().toString();
      //   if ( ! mStation.equals( station ) ) { // change origin name
      //     mParent.setPlotOrigin( station );
      //   }
      // }
    } else if ( b == mBtnDelete ) {
      mParent.askDelete();
    } else if ( TDSetting.mPlotSplit && b == mBtnSplit ) {
      if ( ! handleSplit( true ) ) return;
    // } else if ( b == mBtnMerge ) {
    //   mParent.mergePlot();
    // } else if ( b == mBtnBack ) {
      /* nothing */
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
    mParent.splitPlot( name, mStation, ! mCBcopy.isChecked() ); // not mCBcopy == remove
    return true;
  }

}



