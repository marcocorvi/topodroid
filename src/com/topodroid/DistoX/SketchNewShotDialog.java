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
 *
 * WARNING 
 * The Keyboard has not been tested
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

// import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
// import android.widget.TextView;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import android.text.InputType;
import android.inputmethodservice.KeyboardView;


class SketchNewShotDialog extends MyDialog
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

  private SketchWindow mParent;
  private DataHelper     mData;
  private ShotWindow   mShots;
  private TopoDroidApp   mApp;
  String mFrom;
  boolean manual_shot;
  private DBlock mBlk;

  private MyKeyboard mKeyboard;

  SketchNewShotDialog( Context context, SketchWindow parent, TopoDroidApp app, String name )
  {
    super( context, R.string.SketchNewShotDialog );

    mParent = parent;
    mApp    = app;
    mData   = TopoDroidApp.mData;
    mShots  = mApp.mShotWindow;
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

    initLayout( R.layout.sketch_new_shot_dialog, R.string.title_sketch_shot );

    mBtnOk     = (Button) findViewById(R.id.btn_ok );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel );
    mCBsplay   = (CheckBox) findViewById(R.id.cb_splay );
    mETfrom = (EditText) findViewById(R.id.et_from );
    mETto   = (EditText) findViewById(R.id.et_to );
    mETlength  = (EditText) findViewById(R.id.et_length );
    mETazimuth = (EditText) findViewById(R.id.et_azimuth );
    mETclino   = (EditText) findViewById(R.id.et_clino );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), 
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );

    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom, flag );
      MyKeyboard.registerEditText( mKeyboard, mETto,   flag );

      MyKeyboard.registerEditText( mKeyboard, mETlength,  MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETazimuth, MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETclino,   MyKeyboard.FLAG_POINT_SIGN );

      // TODO LRUD
      // MyKeyboard.registerEditText( mKeyboard, mETleft,  MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETright, MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETup,    MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETdown,  MyKeyboard.FLAG_POINT );

    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETfrom.setInputType( TDConst.NUMBER_DECIMAL );
        mETto.setInputType( TDConst.NUMBER_DECIMAL );
      }
      mETlength.setInputType( TDConst.NUMBER_DECIMAL );
      mETazimuth.setInputType( TDConst.NUMBER_DECIMAL );
      mETclino.setInputType( TDConst.NUMBER_DECIMAL_SIGNED );
    }

    // TextView station = (TextView) findViewById(R.id.tv_station );

    if ( mShots != null ) {
      mBlk = mShots.getNextBlankLegShot( null );
      if ( mBlk != null ) {
        mFrom = mBlk.mFrom;
        mETfrom.setText( mFrom );
        mETto.setText( mBlk.mTo );
        mETlength.setText(  String.format(Locale.US, "%.2f", mBlk.mLength ) );
        mETazimuth.setText( String.format(Locale.US, "%.1f", mBlk.mBearing ) );
        mETclino.setText(   String.format(Locale.US, "%.1f", mBlk.mClino ) );
        mETlength.setEnabled( false );
        mETazimuth.setEnabled( false );
        mETclino.setEnabled( false );
      } else {
        mETfrom.setText( mFrom );
        String to = mFrom;
        List< DBlock > list = mData.selectAllShots( mApp.mSID, 0 );
        do {
            to = DistoXStationName.increment( to ); 
        } while ( DistoXStationName.listHasName( list, to ) );
        mETto.setText( to );
      }
    }
    mBtnOk.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    MyKeyboard.close( mKeyboard );

    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.

    mFrom = mETfrom.getText().toString();
    String to = mETto.getText().toString();

    Button b = (Button) v;
    boolean splay = mCBsplay.isChecked();
    if ( b == mBtnOk ) {
      ArrayList<DBlock> updateList = null;
      if ( splay ) {
        to = "";
      }
      if ( mBlk == null ) {
        float len = Float.parseFloat( mETlength.getText().toString() );
        float ber = Float.parseFloat( mETazimuth.getText().toString() );
        float cln = Float.parseFloat( mETclino.getText().toString() );
        // append a new shot FIXME null splay ?
        DBlock blk = mApp.insertManualShot( -1L, mFrom, to, len, ber, cln, 1L, null, null, null, null, null );
        updateList = new ArrayList<>();
        updateList.add( blk );
      } else {
        // set stations to mBlk
        // mBlk.setName( mFrom, to ); // FIXME
        if ( mShots != null ) {
          mShots.updateShot( mFrom, to, 1, 0, false, null, mBlk ); // null comment ?
          mBlk.setName( mFrom, to ); // reset block name/type
        }
        if ( ! splay ) {
          /* updateList = mShots.numberSplays(); // FIXME-EXTEND in may go away ... */
        } else {
          updateList = new ArrayList<>();
        }
        if ( updateList != null ) updateList.add( mBlk );
      }
      // TODO mParent update Num
      // mParent.recreateNum( mData.selectAllShots( mApp.mSID, 0 ) );
      mParent.updateNum( updateList );
    }
    mParent.setMode( SketchDef.MODE_MOVE );
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}


