/* @file SketchModeDialog.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: new-sketch3d dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

// import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Toast;

class SketchModeDialog extends MyDialog
                              implements View.OnClickListener
{
  private SketchModel mModel;

  private RadioButton mRBsingle;
  private RadioButton mRBngbh;
  private RadioButton mRBall;
  private RadioButton mRBnone;

  private CheckBox mCBsplays;
  private CheckBox mCBforesurface;

  private Button   mBtnOK;
  // private Button   mBtnCancel;

  SketchModeDialog( Context context, SketchModel model )
  {
    super( context, R.string.SketchModeDialog );
    mModel = model;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.sketch_mode_dialog, R.string.title_sketch_refs );
    
    mRBsingle  = (RadioButton) findViewById(R.id.sketch_mode_single);
    mRBngbh    = (RadioButton) findViewById(R.id.sketch_mode_ngbh);
    mRBall     = (RadioButton) findViewById(R.id.sketch_mode_all);
    mRBnone    = (RadioButton) findViewById(R.id.sketch_mode_none);

    switch ( mModel.mDisplayMode ) {
      case SketchDef.DISPLAY_NGBH: 
        mRBngbh.setChecked( true );
        break;
      case SketchDef.DISPLAY_SINGLE:
        mRBsingle.setChecked( true );
        break;
      case SketchDef.DISPLAY_ALL:
        mRBall.setChecked( true );
        break;
      case SketchDef.DISPLAY_NONE:
        mRBnone.setChecked( true );
        break;
    }

    mCBsplays      = (CheckBox) findViewById( R.id.sketch_mode_splays );
    mCBforesurface = (CheckBox) findViewById( R.id.sketch_mode_foresurface );

    mCBsplays.setChecked( mModel.mDisplaySplays );
    mCBforesurface.setChecked( mModel.mDisplayForeSurface );

    mBtnOK = (Button) findViewById(R.id.button_ok);
    mBtnOK.setOnClickListener( this );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);
    // mBtnCancel.setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      if ( mRBsingle.isChecked() ) {
        mModel.mDisplayMode = SketchDef.DISPLAY_SINGLE;
      } else if ( mRBngbh.isChecked() ) {
        mModel.mDisplayMode = SketchDef.DISPLAY_NGBH;
      } else if ( mRBall.isChecked() ) {
        mModel.mDisplayMode = SketchDef.DISPLAY_ALL;
      } else if ( mRBnone.isChecked() ) {
        mModel.mDisplayMode = SketchDef.DISPLAY_NONE;
      }

      mModel.mDisplaySplays = mCBsplays.isChecked();
      mModel.mDisplayForeSurface = mCBforesurface.isChecked();
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

  // @Override
  // public void onBackPressed ()
  // {
  //   if ( mRBsingle.isChecked() ) {
  //     mModel.mDisplayMode = SketchDef.DISPLAY_SINGLE;
  //   } else if ( mRBngbh.isChecked() ) {
  //     mModel.mDisplayMode = SketchDef.DISPLAY_NGBH;
  //   } else if ( mRBall.isChecked() ) {
  //     mModel.mDisplayMode = SketchDef.DISPLAY_ALL;
  //   }
  //   cancel();
  // }

}

