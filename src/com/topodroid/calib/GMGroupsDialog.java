/* @file GMGroupsDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calibration data dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;
import com.topodroid.TDX.GMActivity;

// import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;

import android.widget.TextView;
// import android.widget.CheckBox;
import android.widget.Button;
import android.widget.RadioButton;

import android.view.View;
import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;

public class GMGroupsDialog extends MyDialog
                     implements OnClickListener
{
  private final GMActivity mParent;

  private Button mBtnReset;
  private Button mBtnOK;
  // private Button mBtnCancel;

  private RadioButton mRBtd;
  private RadioButton mRBpt;

  // private String mPolicy;

  public GMGroupsDialog( Context context, GMActivity parent /* , String policy */ )
  {
    super( context, R.string.GMGroupsDialog );
    mParent  = parent;
    // mPolicy  = policy;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.gm_groups_dialog, R.string.group_title );
    
    mBtnOK     = (Button) findViewById( R.id.group_ok );
    mBtnReset  = (Button) findViewById( R.id.group_reset );
    mBtnOK.setOnClickListener( this );
    mBtnReset.setOnClickListener( this );
    // mBtnCancel = (Button) findViewById( R.id.group_cancel );
    // mBtnCancel.setOnClickListener( this );
    ( (Button) findViewById( R.id.group_cancel ) ).setOnClickListener( this );
    
    TextView policy = (TextView) findViewById( R.id.group_policy );
    // if ( TDLevel.overExpert ) {
      mRBtd = (RadioButton) findViewById( R.id.gm_policy_td );
      mRBpt = (RadioButton) findViewById( R.id.gm_policy_pt );
      if ( TDSetting.mGroupBy == TDSetting.GROUP_BY_ONLY_16 ) {
        mRBpt.setChecked( true );
      } else {
        mRBtd.setChecked( true );
      }
    // } else { 
    //   policy.setText( mPolicy );
    //   findViewById( R.id.gm_policies ).setVisibility( View.GONE );
    // }
  }
    
  @Override
  public void onClick( View v ) 
  {
    Button b = (Button)v;
    if ( b == mBtnOK ) {
      int policy = TDSetting.mGroupBy;
      // if ( TDLevel.overExpert ) {
        policy = mRBpt.isChecked() ? TDSetting.GROUP_BY_ONLY_16 : TDSetting.GROUP_BY_FOUR ;
      // }
      mParent.computeGroups( -1L, policy );
    } else if ( b == mBtnReset ) {
      mParent.resetGroups( -1L );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }
}
