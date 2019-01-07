/* @file ShotDeleteDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo dialog (to enter the name of the photo)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.widget.CheckBox;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;


class ShotDeleteDialog extends MyDialog
                              implements View.OnClickListener
{
  private final ShotWindow mParent;
  private DBlock mBlk;

  // private TextView mTVstations;
  // private TextView mTVdata;
  private Button   mButtonDelete;
  private CheckBox mCBleg;
  // private Button   mButtonCancel;

  /**
   * @param context   context
   * @param parent    parent
   * @param blk       shot block
   */
  ShotDeleteDialog( Context context, ShotWindow parent, DBlock blk )
  {
    super( context, R.string.ShotDeleteDialog );
    mParent  = parent;
    mBlk = blk;
    // TDLog.Log( TDLog.LOG_PHOTO, "PhotoSensorDialog");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log(  TDLog.LOG_PHOTO, "ShotDeleteDialog onCreate" );
    initLayout( R.layout.shot_delete_dialog, R.string.title_shot_delete );
    // getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mButtonDelete = (Button) findViewById(R.id.shot_delete );
    // mButtonCancel = (Button) findViewById(R.id.button_cancel );

    mCBleg = (CheckBox) findViewById( R.id.leg );
    mCBleg.setChecked( false );

    TextView mTVstations = (TextView) findViewById( R.id.shot_shot_stations );
    TextView mTVdata = (TextView) findViewById( R.id.shot_shot_data );
    mTVstations.setText( mBlk.Name() );
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_NORMAL ) {
      mTVdata.setText( mBlk.dataStringNormal( mContext.getResources().getString(R.string.shot_data) ) );
    } else { // TDInstance.datamode == SurveyInfo.DATAMODE_NORMAL
      mTVdata.setText( mBlk.dataStringDiving( mContext.getResources().getString(R.string.shot_data) ) );
    }

    mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );

    if ( ! mBlk.isLeg() ) mCBleg.setVisibility( View.GONE );

  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "PhotoiSensorDialog onClick() " + b.getText().toString() );
    if ( b == mButtonDelete ) {
      mParent.doDeleteShot( mBlk.mId, mBlk, TDStatus.DELETED, mCBleg.isChecked() );
    // } else if ( b == mButtonCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}

