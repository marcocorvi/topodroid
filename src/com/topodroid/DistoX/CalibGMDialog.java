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
 * 20130730 bug fix: use hint when no text entered in the edit box
 */
package com.topodroid.DistoX;

// import java.Thread;


import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;


public class CalibGMDialog extends Dialog
                           implements View.OnClickListener
{
  private GMActivity mParent;
  private String mGroup;
  private String mData;

  private TextView mTextView;
  private EditText mEditText;  // group number
  private Button   mButtonOK;
  private Button   mButtonDelete;
  // private Button   mButtonCancel;

  /**
   * @param context   context
   * @param calib     calibration activity
   * @param group     data group
   * @param data      calibration data (as string)
   */
  CalibGMDialog( Context context, GMActivity parent, String group, String data )
  {
    super( context );
    mParent = parent;
    mGroup = group;
    mData  = data;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calib_gm_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mTextView = (TextView) findViewById(R.id.gm_text);
    mEditText = (EditText) findViewById(R.id.gm_name);
    // mEditText.setOnKeyListener(
    //   new OnKeyListener() {
    //     public boolean onKey(View v, int keyCode, KeyEvent event) {
    //       // If the event is a key-down event on the "enter" button
    //       if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
    //         (keyCode == KeyEvent.KEYCODE_ENTER)) {
    //         // Perform action on key press
    //         // Toast.makeText(HelloFormStuff.this, edittext.getText(), Toast.LENGTH_SHORT).show();
    //         return true;
    //       }
    //       return false;
    //     }
    //   }
    // );
    mButtonOK     = (Button) findViewById(R.id.gm_ok );
    mButtonDelete = (Button) findViewById(R.id.gm_delete );
    // mButtonCancel = (Button) findViewById(R.id.gm_cancel );

    mTextView.setText( mData );
    mEditText.setHint( mGroup );
    mButtonOK.setOnClickListener( this );
    mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "GM dialog onClick button " + b.getText().toString() );
    if ( b == mButtonOK ) {
      String name = mEditText.getText().toString();
      if ( name == null || name.length() == 0 ) {
        name = mEditText.getHint().toString();
      }
      if ( name == null || name.length() == 0 ) {
        mEditText.setError( mParent.getResources().getString( R.string.error_group_required ) );
        return;
      } else {
        try {
          long value = Long.parseLong( name );
          mParent.updateGM( value, name );
        } catch ( NumberFormatException e ) {
          mEditText.setError( mParent.getResources().getString( R.string.error_group_non_integer ) );
          return;
        }
      }
    } else if ( b == mButtonDelete ) {
      mParent.deleteGM( true );
    // } else if ( b == mButtonCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}

