/* @file DrawingProfileFlipDialog.java
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
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;

// import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.CheckBox;

class DrawingProfileFlipDialog extends MyDialog
                               implements View.OnClickListener
{
  private final DrawingWindow mParent;
  private Button mBtnOK;

  DrawingProfileFlipDialog( Context context, DrawingWindow parent )
  {
    super( context, null, R.string.DrawingProfileFlipDialog ); // null app
    mParent  = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.drawing_profile_flip_dialog, R.string.title_profile_flip );

    mBtnOK   = (Button) findViewById(R.id.button_ok );
    mBtnOK.setOnClickListener( this );   // OK-SAVE
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      CheckBox cb_shots = (CheckBox) findViewById( R.id.shots );
      CheckBox cb_scrap = (CheckBox) findViewById( R.id.scrap );
      mParent.flipProfile( cb_shots.isChecked(), cb_scrap.isChecked() );
    }
    dismiss();
  }

}
