/* @file CalibGMDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid calibration data dialog (to assign the group number)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Locale;

// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;
// import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView;

import android.text.method.KeyListener;
// import android.text.InputType;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
// import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import android.view.Window;
// import android.view.WindowManager;

class CalibGMDialog extends MyDialog
                           implements View.OnClickListener
{
  private final GMActivity  mParent;
  private CalibCBlock mBlk;

  // private EditText mETbearing;
  // private EditText mETclino;
  // private EditText mETroll;

  // private TextView mTVerror;
  
  private EditText mETname;  // group number
  private Button   mButtonOK;
  private Button   mButtonDelete;
  private MyCheckBox mCBregroup;
  private Button   mButtonCancel;

  private MyKeyboard mKeyboard = null;

  /**
   * @param context   context
   * @param parent    parent GM activity
   * @param blk       calibration data
   */
  CalibGMDialog( Context context, GMActivity parent, CalibCBlock blk )
  {
    super( context, R.string.CalibGMDialog );
    mParent  = parent;
    mBlk     = blk;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.calib_gm_dialog, null );

    EditText eTbearing = (EditText) findViewById( R.id.gm_bearing );
    EditText eTclino   = (EditText) findViewById( R.id.gm_clino   );
    EditText eTroll    = (EditText) findViewById( R.id.gm_roll    );
    TextView tVerror   = (TextView) findViewById( R.id.gm_error );

    mETname = (EditText) findViewById(R.id.gm_name);

    LinearLayout layout2 = (LinearLayout) findViewById( R.id.layout2 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout2.setMinimumHeight( size + 20 );
    
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 10, 20, 10 );

    // mButtonOK     = new MyCheckBox( mContext, size, R.drawable.iz_save, R.drawable.iz_save ); 
    mButtonDelete = new MyCheckBox( mContext, size, R.drawable.iz_delete, R.drawable.iz_delete ); 
    mCBregroup    = new MyCheckBox( mContext, size, R.drawable.iz_numbers_ok, R.drawable.iz_numbers_no ); 
    mCBregroup.setState( false );

    layout2.addView( mCBregroup, lp );
    // layout2.addView( mButtonOK, lp );
    layout2.addView( mButtonDelete, lp );


    // LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCBregroup.getLayoutParams();
    // params.setMargins( 0, 0, 40, 0 );
    // mCBregroup.setLayoutParams( params );
    
    mButtonOK     = (Button) findViewById(R.id.gm_ok );
    mButtonCancel = (Button) findViewById(R.id.gm_cancel );
    // mButtonDelete = (Button) findViewById(R.id.gm_delete );
    // mCBregroup = (CheckBox) findViewById(R.id.gm_regroup );
    // mCBregroup.setChecked( false );

    eTbearing.setText( String.format(Locale.US, "%.1f", mBlk.mBearing ) );
    eTclino.setText( String.format(Locale.US, "%.1f", mBlk.mClino ) );
    eTroll.setText( String.format(Locale.US, "%.1f", mBlk.mRoll ) );
    tVerror.setText( String.format(Locale.US, "%.4f", mBlk.mError ) );

    mETname.setText( String.format(Locale.US, "%d", mBlk.mGroup ) );
    mButtonOK.setOnClickListener( this );
    mButtonDelete.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, -1 );
    if ( TDSetting.mKeyboard ) {
      MyKeyboard.registerEditText( mKeyboard, mETname, MyKeyboard.FLAG_SIGN );
    } else {
      mETname.setInputType( TDConst.NUMBER_SIGNED );
    }
    setEditable( eTbearing, null, false, MyKeyboard.FLAG_POINT );
    setEditable( eTclino,   null, false, MyKeyboard.FLAG_POINT );
    setEditable( eTroll,    null, false, MyKeyboard.FLAG_POINT );
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "GM dialog onClick button " + b.getText().toString() );
    if ( b == mButtonOK ) {
      MyKeyboard.close( mKeyboard );
      String name = mETname.getText().toString();
      // if ( name == null || name.length() == 0 ) {
      //   name = mETname.getHint().toString();
      // }
      if ( name == null || name.length() == 0 ) {
        mETname.setError( mParent.getResources().getString( R.string.error_group_required ) );
        return;
      } else {
        try {
          long value = Long.parseLong( name );
          mParent.updateGM( value, name );
        } catch ( NumberFormatException e ) {
          mETname.setError( mParent.getResources().getString( R.string.error_group_non_integer ) );
          return;
        }
      }
      if ( mCBregroup.isChecked() ) {
        mParent.resetAndComputeGroups( mBlk.mId );
      }
    } else if ( b == mButtonDelete ) {
      mParent.deleteGM( true );
    } else if ( b == mButtonCancel ) {
      /* nothing */
    }
    dismiss();
  }


  private void setEditable( EditText et, KeyListener kl, boolean editable, int flag )
  {
    if ( TDSetting.mKeyboard ) {
      et.setKeyListener( null );
      et.setClickable( true );
      et.setFocusable( editable );
      if ( editable ) {
        MyKeyboard.registerEditText( mKeyboard, et, flag );
        // et.setKeyListener( mKeyboard );
        et.setBackgroundResource( android.R.drawable.edit_text );
      } else {
        MyKeyboard.registerEditText( mKeyboard, et, flag | MyKeyboard.FLAG_NOEDIT );
        et.setBackgroundColor( TDColor.MID_GRAY );
      }
    } else {
      if ( editable ) {
        et.setKeyListener( kl );
        et.setBackgroundResource( android.R.drawable.edit_text );
        et.setClickable( true );
        et.setFocusable( true );
      } else {
        // et.setFocusable( false );
        // et.setClickable( false );
        et.setKeyListener( null );
        et.setBackgroundColor( TDColor.MID_GRAY );
      }
    }
  }

}

