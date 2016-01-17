/** @file DrawingProfileFlipDialog.java
 *
 * @author marco corvi
 * @date jan 2015
 *
 * @brief TopoDroid current station dialog
 *
 * displays the stack of saved stations and allows to push 
 * a station on it or pop one from it
 *
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.CheckBox;

import android.widget.Toast;

import android.util.Log;

public class DrawingProfileFlipDialog extends Dialog
                           implements View.OnClickListener
{
  private Context mContext;
  private DrawingActivity mParent;
  private CheckBox mCBshots;

  private Button mBtnOK;

  public DrawingProfileFlipDialog( Context context, DrawingActivity parent )
  {
    super( context );
    mContext = context;
    mParent  = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_profile_flip_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mCBshots = (CheckBox) findViewById( R.id.shots );

    mBtnOK      = (Button) findViewById(R.id.button_ok );
    mBtnOK.setOnClickListener( this );   // OK-SAVE

    setTitle( R.string.title_profile_flip );
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      mParent.flipProfile( mCBshots.isChecked() );
    }
    dismiss();
  }

}
