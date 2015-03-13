/** @file SketchNewShotDialog.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid sketch save dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130326 created
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;


public class SketchNewShotDialog extends Dialog
                                 implements View.OnClickListener
{
  private Button   mBtnOk;
  // private Button   mBtnCancel;
  private CheckBox mCBsplay;
  private EditText mETfrom;
  private EditText mETto;
  private EditText mETlength;
  private EditText mETazimuth;
  private EditText mETclino;

  private SketchActivity mParent;
  private DataHelper     mData;
  private ShotActivity   mShots;
  private TopoDroidApp   mApp;
  String mFrom;
  boolean manual_shot;
  DistoXDBlock mBlk;

  SketchNewShotDialog( Context context, SketchActivity parent, TopoDroidApp app, String name )
  {
    super( context );
    mParent = parent;
    mApp    = app;
    mData   = app.mData;
    mShots  = app.mShotActivity;
    mFrom   = name;
    mBlk    = null;
    if ( mFrom == null || mFrom.length() == 0 ) {
      mFrom = mData.getLastStationName( mApp.mSID );
    }
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.sketch_new_shot_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mBtnOk     = (Button) findViewById(R.id.btn_ok );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel );
    mCBsplay   = (CheckBox) findViewById(R.id.cb_splay );
    mETfrom = (EditText) findViewById(R.id.et_from );
    mETto   = (EditText) findViewById(R.id.et_to );
    mETlength  = (EditText) findViewById(R.id.et_length );
    mETazimuth = (EditText) findViewById(R.id.et_azimuth );
    mETclino   = (EditText) findViewById(R.id.et_clino );

    // TextView station = (TextView) findViewById(R.id.tv_station );

    mBlk = mShots.getNextBlankLegShot( null );
    if ( mBlk != null ) {
      mFrom = mBlk.mFrom;
      mETfrom.setText( mFrom );
      mETto.setText( mBlk.mTo );
      mETlength.setText(  Float.toString( mBlk.mLength ) );
      mETazimuth.setText( Float.toString( mBlk.mBearing ) );
      mETclino.setText(   Float.toString( mBlk.mClino ) );
      mETlength.setEnabled( false );
      mETazimuth.setEnabled( false );
      mETclino.setEnabled( false );
    } else {
      mETfrom.setText( mFrom );
      String to = mFrom;
      List< DistoXDBlock > list = mData.selectAllShots( mApp.mSID, 0 );
      do {
          to = DistoXStationName.increment( to ); 
      } while ( DistoXStationName.listHasName( list, to ) );
      mETto.setText( to );
    }
    mBtnOk.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );

    setTitle( R.string.title_sketch_shot );
  }

  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.

    mFrom = mETfrom.getText().toString();
    String to = mETto.getText().toString();

    Button b = (Button) v;
    boolean splay = mCBsplay.isChecked();
    if ( b == mBtnOk ) {
      ArrayList<DistoXDBlock> updateList = null;
      if ( splay ) {
        to = "";
      }
      if ( mBlk == null ) {
        float len = Float.parseFloat( mETlength.getText().toString() );
        float ber = Float.parseFloat( mETazimuth.getText().toString() );
        float cln = Float.parseFloat( mETclino.getText().toString() );
        // append a new shot FIXME null splay ?
        DistoXDBlock blk = mApp.insertManualShot( -1L, mFrom, to, len, ber, cln, 1L, null, null, null, null, null );
        updateList = new ArrayList<DistoXDBlock>();
        updateList.add( blk );
      } else {
        // set stations to mBlk
        // mBlk.setName( mFrom, to ); // FIXME
        mShots.updateShot( mFrom, to, 1, 0, false, null, mBlk ); // null comment ?
        mBlk.setName( mFrom, to ); // reset block name/type
        if ( ! splay ) {
          updateList = mShots.numberSplays();
        } else {
          updateList = new ArrayList<DistoXDBlock>();
        }
        updateList.add( mBlk );
      }
      // TODO mParent update Num
      // mParent.recreateNum( mData.selectAllShots( mApp.mSID, 0 ) );
      mParent.updateNum( updateList );
    }
    mParent.setMode( SketchDef.MODE_MOVE );
    dismiss();
  }

}


