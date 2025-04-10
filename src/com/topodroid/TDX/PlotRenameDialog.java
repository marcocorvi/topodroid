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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
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
                       // , View.OnLongClickListener
{
  private EditText mEtName;
  private EditText mEtOrigin;
  private Button   mBtnRename;
  private Button   mBtnBack;
  private Button   mBtnDelete;
  // private Button   mBtnSplit;
  private CheckBox mCBmove     = null;
  private CheckBox mCBcopy     = null;
  private Button   mBtnOutline = null; // sketch outlines
  private Button   mBtnMerge   = null;  // sketch merge outlines
  private Button   mBtnPaste   = null;  // paste into scrap
  private Button   mBtnSketch  = null; // sketch move/copy
  private Button   mBtnScrap   = null;  // scrap move/copy

  private final DrawingWindow mParent;
  private String mStation;
  private String mName;
  private boolean mScrapCopy;
  private boolean mHasOutline;

  PlotRenameDialog( Context context, DrawingWindow parent, boolean scrap_copy, boolean has_outline /*, TopoDroidApp app */ )
  {
    super( context, null, R.string.PlotRenameDialog ); // null app
    mParent = parent;
    mStation = mParent.getPlotStation();
    mName    = mParent.getPlotName();
    mScrapCopy = scrap_copy;
    mHasOutline = has_outline;
    // TDLog.v("Plot Rename Dialog: " + mName + " scrap_copy " + scrap_copy + " has_outline " + has_outline );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.v("Plot Rename on create");
    initLayout( R.layout.plot_rename_dialog, R.string.title_plot_rename );
    
    mBtnRename = (Button) findViewById(R.id.btn_rename );
    mBtnSketch = (Button) findViewById(R.id.btn_to_sketch );
    mBtnScrap  = (Button) findViewById(R.id.btn_to_scrap );
    mBtnDelete = (Button) findViewById(R.id.btn_delete );
    mBtnBack   = (Button) findViewById(R.id.btn_back );
    mCBmove    = (CheckBox) findViewById( R.id.cb_move );
    mCBcopy    = (CheckBox) findViewById( R.id.cb_copy );
    mBtnOutline = (Button) findViewById(R.id.btn_outline );
    mBtnMerge  = (Button) findViewById(R.id.btn_merge );
    mBtnPaste  = (Button) findViewById(R.id.btn_paste );

    mEtName = (EditText) findViewById( R.id.et_name );
    mEtName.setText( mName );

    mEtOrigin = (EditText) findViewById( R.id.et_station );
    mEtOrigin.setText( mStation );
    if ( ! TDLevel.overExpert ) {
      mEtOrigin.setInputType( 0 ); // 0 = not editable
    }

    mBtnRename.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );
    mBtnDelete.setOnClickListener( this );
    if ( TDLevel.overExpert && TDSetting.mPlotSplit && mParent != null && ! mParent.isAnySection() ) {
      mCBmove.setOnClickListener( this );
      mCBcopy.setOnClickListener( this );
      mBtnOutline.setOnClickListener( this );
      mBtnSketch.setOnClickListener( this );
      mBtnScrap.setOnClickListener( this );
      // mBtnSplit.setOnLongClickListener( this );
      if ( mHasOutline ) {
        mBtnMerge.setOnClickListener( this );
      } else {
        mBtnMerge.setVisibility( View.GONE );
      }
      if ( mScrapCopy ) {
        mBtnPaste.setOnClickListener( this );
      } else {
        mBtnPaste.setVisibility( View.GONE );
      }
      mCBmove.setChecked( true );
      mCBcopy.setChecked( false );
    } else {
      mCBmove.setVisibility( View.GONE );
      mCBcopy.setVisibility( View.GONE );
      mBtnSketch.setVisibility( View.GONE );
      mBtnScrap.setVisibility( View.GONE );
      mBtnOutline.setVisibility( View.GONE );
      mBtnMerge.setVisibility( View.GONE );
      mBtnPaste.setVisibility( View.GONE );
    }

  }

  @Override
  public void onClick(View v) 
  {
    // TDLog.v("Plot Rename on click ");
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    if ( v instanceof CheckBox ) {
      CheckBox cb = (CheckBox)v;
      if ( cb == mCBmove ) {
        mCBmove.setChecked( true );
        mCBcopy.setChecked( false );
      } else if ( cb == mCBcopy ) {
        mCBmove.setChecked( false );
        mCBcopy.setChecked( true );
      }
      return;
    }
    Button b = (Button) v;
    if ( b == mBtnRename ) {
      // TDLog.v("click RENAME");
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

      if ( TDLevel.overExpert ) {
        String station = mEtOrigin.getText().toString();
        if ( ! mStation.equals( station ) ) { // change origin name
          mParent.setPlotOrigin( station );
        }
      }
    } else if ( b == mBtnDelete ) {
      // TDLog.v("click DELETE");
      mParent.askDelete();
    } else if ( TDSetting.mPlotSplit && b == mBtnSketch ) {
      // TDLog.v("click SKETCH");
      if ( ! handleSketchSplit( true ) ) return;
    } else if ( TDSetting.mPlotSplit && b == mBtnScrap ) {
      // TDLog.v("click SCRAP");
      handleScrapSplit( );
    } else if ( b == mBtnOutline ) {
      // TDLog.v("click OUTLINE");
      mParent.scrapOutlineDialog();
    } else if ( b == mBtnMerge ) { // merge outline to plot
      // TDLog.v("click MERGE");
      mParent.mergePlotOutline( );
    } else if ( b == mBtnPaste ) { // merge paths to scrap
      // TDLog.v("click PASTE");
      mParent.pasteToScrap( );
    // } else if ( b == mBtnBack ) {
      /* nothing */
    }
    dismiss();
  }

  // @Override
  // public boolean onLongClick(View v) // called only on mBtnSplit
  // {
  //   handleSketchSplit( false );
  //   return true;
  // }

  /** do a sketch split action
   * @param warning unused
   * @return true on success
   */
  private boolean handleSketchSplit( boolean warning )
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

  private void handleScrapSplit( )
  {
    mParent.splitScrap(  ! mCBcopy.isChecked() ); //  not mCBcopy == remove
  }

}



