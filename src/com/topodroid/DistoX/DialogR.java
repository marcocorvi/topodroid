/* @file DialogR.java
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
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyColorPicker;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.ExtendType;
import com.topodroid.common.PlotType;

import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
// import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.LinearLayout;

class DialogR extends MyDialog
              implements View.OnClickListener
{
  private MainWindow mParent;

  // private Button mBtnContinue;

  private CheckBox mCBunderstood;
  private CheckBox mCBdisclaimer;

  // Dialog R
  // if this dialog is shown the TopoDroid folder in the app private space does not exists
  //
  DialogR( Context context, MainWindow parent )
  {
    super(context, R.string.DialogR );
    mParent = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.dialog_r, R.string.r_title );

    ((Button) findViewById(R.id.r_continue)).setOnClickListener( this );

    mCBunderstood = (CheckBox) findViewById( R.id.r_i_understood );
    mCBdisclaimer = (CheckBox) findViewById( R.id.r_disclaimer );
  }

  public void onClick( View v )
  {
    if ( v.getId() == R.id.r_continue ) {
      if ( ! ( mCBunderstood.isChecked() && mCBdisclaimer.isChecked() ) ) {
        TDToast.make( R.string.r_checkboxes );
      } else {
        TDLog.v( "Dialog R accepted");
        // MainWindow.say_dialogR = false; // clear flag
        mParent.showInitDialogs( false );
        dismiss();
      }
    }
  }

  @Override
  public void onBackPressed()
  {
    dismiss();
    mParent.finish();
  }

}
        


