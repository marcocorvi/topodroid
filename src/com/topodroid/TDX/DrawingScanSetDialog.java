/* @file DrawingScanSetDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a survey shot: editing comment, extend and flag
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyColorPicker;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDStatus;
import com.topodroid.prefs.TDSetting;
import com.topodroid.types.ExtendType;
import com.topodroid.types.LegType;
// import com.topodroid.types.PlotType;

import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

// import android.graphics.Paint;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
// import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

// import android.text.InputType;
// import android.inputmethodservice.KeyboardView;

class DrawingScanSetDialog extends MyDialog
                           implements View.OnClickListener
                           , View.OnLongClickListener
                           , AdapterView.OnItemSelectedListener
{
  private Button mBtnOK;
  private Button mBtnCancel;
  private EditText mETfrom;

  private Spinner  mSPtype;
  // private CheckBox mCBgeneric;
  // private CheckBox mCBxsection;
  // private CheckBox mCBplan;
  // private CheckBox mCBprofile;

  private final DrawingWindow mParent;
  private long mIdx;
  private long mLegType;  // scan-set leg-type (between 6 and 9) 
  private String mFrom;   // scan-set station
  private int mSelectedPos;

  private MyKeyboard mKeyboard = null;

  /** cstr
   * @param context  context
   * @param parent   parent window
   * @param shot     drawing path of this dialog
   * @param flag     ...
   */
  DrawingScanSetDialog( Context context, DrawingWindow parent, DrawingPath shot )
  {
    super(context, null, R.string.DrawingScanSetDialog ); // null app
    mParent  = parent;
    mIdx     = TopoDroidApp.mData.getShotIdx( shot.mBlock.mId, TDInstance.sid );
    mLegType = shot.mBlock.getLegType();
    mFrom    = shot.mBlock.mFrom;
    TDLog.v("Scan-set dialog ID " + mIdx + " FROM " + mFrom + " leg-type " + mLegType );
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.drawing_scanset_dialog, R.string.title_scanset );

    // TextView mLabel     = (TextView) findViewById(R.id.shot_label);
    // mLabel.setText( String.format( mContext.getResources().getString(R.string.scanset_string), mFrom );
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETfrom.setText( mFrom );
    mETfrom.setOnLongClickListener( this );

    mSelectedPos = (int)(mLegType - 6);
    // TDLog.v("selected pos " + mSelectedPos );
    mSPtype = (Spinner) findViewById(R.id.shot_type );
    // ArrayAdapter adapter = new ArrayAdapter<>( this, R.layout.menu, leg_types );
    ArrayAdapter adapter = ArrayAdapter.createFromResource( mContext, R.array.scanLegType, android.R.layout.simple_spinner_item );   
    mSPtype.setAdapter( adapter );
    mSPtype.setOnItemSelectedListener( this );
    mSPtype.setSelection( mSelectedPos );

    // mCBgeneric  = (CheckBox) findViewById( R.id.shot_generic );
    // mCBxsection = (CheckBox) findViewById( R.id.shot_xsection);
    // mCBplan     = (CheckBox) findViewById( R.id.shot_plan );
    // mCBprofile  = (CheckBox) findViewById( R.id.shot_profile );
    // if ( mLegType == 7 ) { mCBxsection.setChecked( true ); }
    // else if ( mLegType == 8 ) { mCBplan.setChecked( true ); }
    // else if ( mLegType == 9 ) { mCBprofile.setChecked( true ); }
    // else { mCBgeneric.setChecked( true ); }
    // mCBgeneric.setOnClickListener( this );
    // mCBxsection.setOnClickListener( this );
    // mCBplan.setOnClickListener( this );
    // mCBprofile.setOnClickListener( this );

    // mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );
    mKeyboard= MyKeyboard.getMyKeyboard( mContext, findViewById( R.id.keyboardview ), R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );

    mBtnOK     = (Button) findViewById(R.id.btn_ok);
    mBtnCancel = (Button) findViewById(R.id.btn_cancel);

    mBtnOK.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );
    
    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom, flag );
      mKeyboard.hide();
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETfrom.setInputType( TDConst.NUMBER_DECIMAL );
      }
    }
  }

  /** react to an item selection
   * @param av    item adapter
   * @param v     item view
   * @param pos   item position
   * @param id    ?
   */
  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  {
    mSelectedPos = pos;
  }

  // if nothing is selected select the block leg-type
  @Override
  public void onNothingSelected( AdapterView av )
  {
    mSelectedPos = (int)(mLegType - 6);
    mSPtype.setSelection( mSelectedPos );
  }

  /** implements user long-taps
   * @param view   tapped view
   * @return true if the tap has been handled
   */
  @Override
  public boolean onLongClick(View view)
  {
    CutNPaste.makePopup( mContext, (EditText)view );
    return true;
  }

  // private void clearLegTypeCheckBoxes()
  // {
  //   mCBgeneric.setChecked( false );
  //   mCBxsection.setChecked( false );
  //   mCBplan.setChecked( false );
  //   mCBprofile.setChecked( false );
  // }

  /** implements user taps
   * @param v   tapped view
   */
  @Override
  public void onClick(View v)
  {
    // TDLog.v( "Drawing Shot Dialog onClick() " + view.toString() );
    CutNPaste.dismissPopup();

    // if ( v.getId() == R.id.shot_generic ) {
    //   clearLegTypeCheckBoxes();
    //   mCBgeneric.setChecked( true );
    // } else if ( v.getId() == R.id.shot_xsection ) {
    //   clearLegTypeCheckBoxes();
    //   mCBxsection.setChecked( true );
    // } else if ( v.getId() == R.id.shot_plan ) {
    //   clearLegTypeCheckBoxes();
    //   mCBplan.setChecked( true );
    // } else if ( v.getId() == R.id.shot_profile ) {
    //   clearLegTypeCheckBoxes();
    //   mCBprofile.setChecked( true );
    // } else
    if ( v.getId() == R.id.btn_cancel ) {
      dismiss();
    } else if ( v.getId() == R.id.btn_ok ) {
      MyKeyboard.close( mKeyboard );
      String from = TDString.noSpaces( mETfrom.getText().toString() ); // N.B. no spaces in station names
      if ( TDString.isNullOrEmpty( from ) ) {
        mETfrom.setError( resString( R.string.missing_station ) );
        return;
      }
      // TODO FIXME get new scan type
      long new_leg = 6L + mSelectedPos;
      TDLog.v( "old leg " + mLegType + " new leg " + new_leg );
      // long new_leg = 6;
      // if ( mCBxsection.isChecked() ) { new_leg = 7; }
      // else if ( mCBplan.isChecked() ) { new_leg = 8; }
      // else if ( mCBprofile.isChecked() ) { new_leg = 9; }
      if ( ! from.equals( mFrom ) || new_leg != mLegType ) {
        mParent.updateScanSet( mIdx, mFrom, from, mLegType, new_leg );
      }
      dismiss();
    }
  }

  /** implements a user BACK press
   */
  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}
        


