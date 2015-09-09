/* @file CalibGMDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid calibration data dialog (to assign the group number)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView;

import android.text.method.KeyListener;
import android.text.InputType;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

import android.view.Window;
import android.view.WindowManager;

public class CalibGMDialog extends Dialog
                           implements View.OnClickListener
{
  private Context     mContext;
  private GMActivity  mParent;
  private CalibCBlock mBlk;

  private EditText mETbearing;
  private EditText mETclino;
  private EditText mETroll;

  private TextView mTVerror;
  
  private EditText mETname;  // group number
  private Button   mButtonOK;
  private Button   mButtonDelete;
  private CheckBox mCBregroup;
  // private Button   mButtonCancel;

  private MyKeyboard mKeyboard = null;

  /**
   * @param context   context
   * @param calib     calibration activity
   * @param group     data group
   * @param data      calibration data (as string)
   */
  CalibGMDialog( Context context, GMActivity parent, CalibCBlock blk )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mBlk     = blk;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE); 

    setContentView(R.layout.calib_gm_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mETbearing = (EditText) findViewById( R.id.gm_bearing );
    mETclino   = (EditText) findViewById( R.id.gm_clino   );
    mETroll    = (EditText) findViewById( R.id.gm_roll    );
    mTVerror   = (TextView) findViewById( R.id.gm_error );

    mETname = (EditText) findViewById(R.id.gm_name);
    
    mButtonOK     = (Button) findViewById(R.id.gm_ok );
    mButtonDelete = (Button) findViewById(R.id.gm_delete );
    mCBregroup = (CheckBox) findViewById(R.id.gm_regroup );
    mCBregroup.setChecked( false );
    // mButtonCancel = (Button) findViewById(R.id.gm_cancel );

    mETbearing.setText( String.format( "%.1f", mBlk.mBearing ) );
    mETclino.setText( String.format( "%.1f", mBlk.mClino ) );
    mETroll.setText( String.format( "%.1f", mBlk.mRoll ) );
    mTVerror.setText( String.format( "%.4f", mBlk.mError ) );

    mETname.setHint( Long.toString( mBlk.mGroup ) );
    mButtonOK.setOnClickListener( this );
    mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, -1 );
    if ( TopoDroidSetting.mKeyboard ) {
      MyKeyboard.registerEditText( mKeyboard, mETname, MyKeyboard.FLAG_SIGN );
    } else {
      mETname.setInputType( TopoDroidConst.NUMBER_SIGNED );
    }
    setEditable( mETbearing, null, false, MyKeyboard.FLAG_POINT );
    setEditable( mETclino,   null, false, MyKeyboard.FLAG_POINT );
    setEditable( mETroll,    null, false, MyKeyboard.FLAG_POINT );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "GM dialog onClick button " + b.getText().toString() );
    if ( b == mButtonOK ) {
      String name = mETname.getText().toString();
      if ( name == null || name.length() == 0 ) {
        name = mETname.getHint().toString();
      }
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
    // } else if ( b == mButtonCancel ) {
    //   /* nothing */
    }
    dismiss();
  }


  private void setEditable( EditText et, KeyListener kl, boolean editable, int flag )
  {
    if ( TopoDroidSetting.mKeyboard ) {
      et.setKeyListener( null );
      et.setClickable( true );
      et.setFocusable( editable );
      if ( editable ) {
        MyKeyboard.registerEditText( mKeyboard, et, flag );
        // et.setKeyListener( mKeyboard );
        et.setBackgroundResource( android.R.drawable.edit_text );
      } else {
        MyKeyboard.registerEditText( mKeyboard, et, flag | MyKeyboard.FLAG_NOEDIT );
        et.setBackgroundColor( 0xff999999 );
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
        et.setBackgroundColor( 0xff999999 );
      }
    }
  }

}

