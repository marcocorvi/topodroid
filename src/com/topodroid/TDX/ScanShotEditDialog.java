/* @file ScanShotEditDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog to enter FROM-TO stations etc.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDUtil;
import com.topodroid.util.TDString;
import com.topodroid.util.TDStatus;
// import com.topodroid.util.TDColor;
// import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.types.BlockType;
import com.topodroid.prefs.TDSetting;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import java.util.regex.Pattern;

// import android.app.Dialog;
import android.os.Bundle;
// import android.widget.RadioButton;

import android.text.method.KeyListener;
import android.text.InputType;

import android.content.Context;
// import android.content.res.Resources;
import android.content.DialogInterface;
// import android.inputmethodservice.KeyboardView;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.CheckBox;;

import android.view.View;
// import android.graphics.drawable.BitmapDrawable;

class ScanShotEditDialog extends MyDialog
                     implements View.OnClickListener
{
  private final ShotWindow mParent;
  private DBlock mBlk;
  private int mPos;

  // private Pattern mPattern; // name pattern
  private TextView mETdistance; // distance | depth
  private TextView mETbearing;
  private TextView mETclino;    // clino | distance

  private EditText mETfrom;
  // private EditText mETto;
  // private EditText mETcomment;
  
  private Button mButtonOK;
  private Button mButtonBack;
  private Button mButtonDelete;
  private CheckBox mRBplan;
  private CheckBox mRBprofile;
  private CheckBox mRBxsection;

  private String shot_from;
  // private String shot_to;
  private String shot_distance;  // distance - depth
  private String shot_bearing;   // bearing
  private String shot_clino;     // clino    - distance

  /** cstr
   */
  ScanShotEditDialog( Context context, ShotWindow parent, DBlock blk, int pos )
  {
    super( context, null, R.string.ScanShotEditDialog ); // null app
    mParent   = parent;
    mBlk      = blk;
    mPos      = pos; // position in the DBlockAdapter
    shot_from = blk.mFrom;
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
      shot_distance = blk.depthString();
      shot_bearing  = blk.bearingString();
      shot_clino    = blk.distanceString();
    } else { // SurveyInfo.DATAMODE_NORMAL
      shot_distance = blk.distanceString();
      shot_bearing  = blk.bearingString();
      shot_clino    = blk.clinoString();
    }
  }

  /** update the interface
   */
  private void updateView()
  {
    mETdistance.setText( shot_distance );
    mETbearing.setText( shot_bearing );
    mETclino.setText( shot_clino );

    if ( shot_from.length() > 0 /* || shot_to.length() > 0 */ ) {
      mETfrom.setText( shot_from );
      // mETto.setText( shot_to );
    }

  }


// -------------------------------------------------------------------
  // @Override
  // public void onRestoreInstanceState( Bundle icicle )
  // {
  //   // FIXME DIALOG mKeyboard.hide();
  // }
 
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    // boolean photoCheck = TDandroid.checkCamera( mContext );

    initLayout( R.layout.scan_shot_dialog, R.string.title_scan_shot );

    mETdistance = (TextView) findViewById(R.id.shot_distance);
    mETbearing  = (TextView) findViewById(R.id.shot_bearing);
    mETclino    = (TextView) findViewById(R.id.shot_clino);

    mETfrom    = (EditText) findViewById(R.id.shot_from );
    // mETto      = (EditText) findViewById(R.id.shot_to );

    updateView();
    // mKeyboard = MyKeyboard.getMyKeyboard( mContext, findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    // if ( TDSetting.mKeyboard ) {
    //   int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
    //   if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
    //   MyKeyboard.registerEditText( mKeyboard, mETfrom,  flag );
    //   // MyKeyboard.registerEditText( mKeyboard, mETto,    flag );
    // } else {
    //   mKeyboard.hide();
    //   if ( TDSetting.mStationNames == 1 ) {
    //     mETfrom.setInputType( InputType.TYPE_CLASS_NUMBER );
    //     // mETto.setInputType( InputType.TYPE_CLASS_NUMBER );
    //   }
    // }

    mButtonOK = (Button)findViewById( R.id.btn_ok );
    mButtonBack = (Button)findViewById( R.id.btn_back );
    mButtonDelete = (Button)findViewById( R.id.btn_delete );
    mButtonOK.setOnClickListener( this );
    mButtonBack.setOnClickListener( this );
    mButtonDelete.setOnClickListener( this );

    mRBplan     = (CheckBox)findViewById( R.id.btn_plan );
    mRBprofile  = (CheckBox)findViewById( R.id.btn_profile );
    mRBxsection = (CheckBox)findViewById( R.id.btn_xsection );
    mRBplan.setOnClickListener( this );
    mRBprofile.setOnClickListener( this );
    mRBxsection.setOnClickListener( this );

    switch ( mBlk.getBlockType() ) {
      case BlockType.XSCAN: 
        mRBxsection.setChecked( true );
        break;
      case BlockType.HSCAN:
        mRBplan.setChecked( true );
        break;
      case BlockType.VSCAN:
        mRBprofile.setChecked( true );
        break;
    }

    mParent.mOnOpenDialog = false;
  }

  /** save the changes to the DBlock
   */
  private boolean saveDBlock()
  {
    shot_from = TDUtil.toStationFromName( mETfrom.getText().toString() ); // NOSPACES this replaces all specials
    if ( ! TDUtil.isStationName( shot_from ) ) {
      mETfrom.setError( resString( R.string.bad_station_name ) );
      return false;
    }
    int splay_class = BlockType.SCAN;
    if ( mRBplan.isChecked() ) { splay_class = BlockType.HSCAN; }
    else if ( mRBprofile.isChecked() ) { splay_class = BlockType.VSCAN; }
    else if ( mRBxsection.isChecked() ) { splay_class = BlockType.XSCAN; }
    
    // TDLog.v(" FROM " + shot_from + " " + mBlk.mFrom + " Block type " + splay_class + " " + mBlk.mBlockType );
    if ( ! shot_from.equals( mBlk.mFrom ) || splay_class != mBlk.getBlockType() ) mParent.updateScanSet( mBlk, shot_from, mPos, splay_class );
    return true;
  }

  // /** display the CutCopyPaste popup for the given edit text
  //  * @param v   edit text view
  //  * 
  //  * This is used for the stations edit texts.
  //  * FIXME The popup remains open when the user change focus to the other station.
  //  */
  // @Override
  // public boolean onLongClick(View v) 
  // {
  //   CutNPaste.dismissPopup();
  //   CutNPaste.makePopup( mContext, (EditText)v );
  //   return true;
  // }
    

  @Override
  public void onClick(View v) 
  {
    // CutNPaste.dismissPopup();
    // MyKeyboard.close( mKeyboard );

    Button b = (Button) v;
    // if ( b == mButtonBack ) {
    //   CutNPaste.dismissPopup();
    // }
    if ( b == mRBplan ) {
      mRBprofile.setChecked( false );
      mRBxsection.setChecked( false );
      return;
    } else if ( b == mRBprofile ) {
      mRBplan.setChecked( false );
      mRBxsection.setChecked( false );
      return;
    } else if ( b == mRBxsection ) {
      mRBplan.setChecked( false );
      mRBprofile.setChecked( false );
      return;
    } else if ( b == mButtonOK ) { // OK and SAVE close the keyboard
      // if ( sameString( mETfrom, mETto ) ) {
      //   mETto.setError( resString( R.string.equal_station_names ) );
      //   return;
      // }
      if ( ! saveDBlock() ) {
        TDLog.e("OK failed to save block");
        return;
      }
    } else if ( b == mButtonDelete ) {
      mParent.deleteScanShot( mBlk );
    } 
    dismiss();
    // onBackPressed();
  }

  // @Override
  // public void onBackPressed()
  // {
  //   // if ( CutNPaste.dismissPopup() ) return;
  //   // if ( MyKeyboard.close( mKeyboard ) ) return;
  //   super.onBackPressed();
  // }

}

