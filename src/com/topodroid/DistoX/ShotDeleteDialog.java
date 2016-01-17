/* @file ShotDeleteDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo dialog (to enter the name of the photo)
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.Button;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;


public class ShotDeleteDialog extends Dialog
                              implements View.OnClickListener
{
  private Context mContext;
  private ShotActivity mParent;
  private DistoXDBlock mBlk;

  private TextView mTVstations;
  private TextView mTVdata;
  private Button   mButtonDelete;
  // private Button   mButtonCancel;

  /**
   * @param context   context
   * @param parent    parent
   * @param blk       shot block
   */
  ShotDeleteDialog( Context context, ShotActivity parent, DistoXDBlock blk )
  {
    super( context );
    mContext = context;
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
    setContentView(R.layout.shot_delete_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mButtonDelete = (Button) findViewById(R.id.shot_delete );
    // mButtonCancel = (Button) findViewById(R.id.button_cancel );

    setTitle( R.string.title_shot_delete );

    mTVstations = (TextView) findViewById( R.id.shot_shot_stations );
    mTVdata = (TextView) findViewById( R.id.shot_shot_data );
    mTVstations.setText( mBlk.Name() );
    mTVdata.setText( mBlk.dataString( mContext.getResources().getString(R.string.shot_data) ) );

    mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "PhotoiSensorDialog onClick() " + b.getText().toString() );
    if ( b == mButtonDelete ) {
      mParent.doDeleteShot( mBlk.mId );
    // } else if ( b == mButtonCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}

