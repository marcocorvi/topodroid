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
 * CHANGES
 * 20120521 using INewPlot interface for the maker
 */
package com.topodroid.DistoX;


import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

// import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Toast;

public class SketchModeDialog extends Dialog
                              implements View.OnClickListener
{
  private Context mContext;
  private SketchModel mParent;

  private RadioButton mRBsingle;
  private RadioButton mRBngbh;
  private RadioButton mRBall;

  private Button   mBtnOK;
  // private Button   mBtnCancel;

  public SketchModeDialog( Context context, SketchModel parent )
  {
    super( context );
    mContext = context;
    mParent  = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sketch_mode_dialog);
    mRBsingle  = (RadioButton) findViewById(R.id.sketch_mode_single);
    mRBngbh    = (RadioButton) findViewById(R.id.sketch_mode_ngbh);
    mRBall     = (RadioButton) findViewById(R.id.sketch_mode_all);

    switch ( mParent.mDisplayMode ) {
      case SketchDef.DISPLAY_NGBH: 
        mRBngbh.setChecked( true );
        break;
      case SketchDef.DISPLAY_SINGLE:
        mRBsingle.setChecked( true );
        break;
      case SketchDef.DISPLAY_ALL:
        mRBall.setChecked( true );
        break;
    }

    mBtnOK = (Button) findViewById(R.id.button_ok);
    mBtnOK.setOnClickListener( this );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);
    // mBtnCancel.setOnClickListener( this );

    setTitle( mContext.getResources().getString( R.string.title_sketch_refs ) );
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      if ( mRBsingle.isChecked() ) {
        mParent.mDisplayMode = SketchDef.DISPLAY_SINGLE;
      } else if ( mRBngbh.isChecked() ) {
        mParent.mDisplayMode = SketchDef.DISPLAY_NGBH;
      } else if ( mRBall.isChecked() ) {
        mParent.mDisplayMode = SketchDef.DISPLAY_ALL;
      }
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

  // @Override
  // public void onBackPressed ()
  // {
  //   if ( mRBsingle.isChecked() ) {
  //     mParent.mDisplayMode = SketchDef.DISPLAY_SINGLE;
  //   } else if ( mRBngbh.isChecked() ) {
  //     mParent.mDisplayMode = SketchDef.DISPLAY_NGBH;
  //   } else if ( mRBall.isChecked() ) {
  //     mParent.mDisplayMode = SketchDef.DISPLAY_ALL;
  //   }
  //   cancel();
  // }

}

