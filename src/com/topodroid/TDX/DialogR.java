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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.ui.MyCheckBox;
// import com.topodroid.ui.MyButton;
// import com.topodroid.ui.MyColorPicker;
import com.topodroid.ui.MyDialog;
// import com.topodroid.ui.TDLayout;
// import com.topodroid.prefs.TDSetting;
// import com.topodroid.common.ExtendType;
// import com.topodroid.common.PlotType;

import android.os.Bundle;
import android.content.Context;
import android.content.res.Resources;
// import android.content.Intent;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
// import android.widget.LinearLayout;

import android.text.TextWatcher;
import android.text.Editable;

class DialogR extends MyDialog
              implements View.OnClickListener
{
  private final MainWindow mParent;

  private Button mBTcontinue;
  private EditText mCBunderstood;
  private final String mIUnderstood;
  private boolean mContinue = false;
  // private String mRContinue;
  // private String mRQuit;

  // Dialog R
  // if this dialog is shown the TopoDroid folder in the app private space does not exists
  //
  DialogR( Context context, MainWindow parent )
  {
    super(context, null, 0 ); // null app, 0 no help resource
    mParent = parent;
    Resources res = mContext.getResources();
    mIUnderstood = res.getString( R.string.r_i_understood );
    // mRContinue   = res.getString( R.string.r_continue );
    // mRQuit       = res.getString( R.string.r_quit );
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.dialog_r, R.string.r_title );

    mBTcontinue = (Button) findViewById(R.id.r_continue);
    mBTcontinue.setOnClickListener( this );

    mCBunderstood = (EditText) findViewById( R.id.r_i_understood );

    mCBunderstood.addTextChangedListener( new TextWatcher() {
      @Override
      public void afterTextChanged( Editable e ) { }

      @Override
      public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) { }

      @Override
      public void onTextChanged( CharSequence cs, int start, int before, int cnt ) 
      {
        if ( mContinue ) { 
          if ( mCBunderstood.getText() == null || ! mIUnderstood.equals( mCBunderstood.getText().toString() ) ) {
            mContinue = false;
            mBTcontinue.setText( R.string.r_quit );
          }
        } else {
          if ( mCBunderstood.getText() != null && mIUnderstood.equals( mCBunderstood.getText().toString() ) ) {
            mContinue = true;
            mBTcontinue.setText( R.string.r_continue );
          }
        }
      }
    } );
 
    Resources res = mContext.getResources();

    ((TextView)findViewById( R.id.r_dialog )).setText( String.format( res.getString(R.string.r_dialog), mIUnderstood ) );
  }

  public void onClick( View v )
  {
    if ( v.getId() == R.id.r_continue ) {
      dismiss();
      // check again
      if ( mCBunderstood.getText() != null && mCBunderstood.getText().toString().equals( mIUnderstood ) ) {
        TDLog.v( "Dialog R accepted");
        // MainWindow.say_dialogR = false; // clear flag
        mParent.resultR( true );
        mParent.showInitDialogs( /* false */ );
      } else {
        TDLog.v( "Dialog R rejected");
        mParent.resultR( false );
      }
    }
  }

  @Override
  public void onBackPressed()
  {
    dismiss();
    mParent.resultR( false );
  }

}
        


