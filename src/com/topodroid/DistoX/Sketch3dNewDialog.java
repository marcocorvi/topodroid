/* @file Sketch3dNewDialog.java
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


// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
// import android.widget.RadioButton;
// import android.widget.RadioGroup;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;

// import android.widget.Toast;

class Sketch3dNewDialog extends MyDialog
                               implements View.OnClickListener
{
  private TopoDroidApp mApp;
  private INewPlot mMaker;

  private EditText mEditName;
  private EditText mEditStart;
  private EditText mEditNext;

  private Button   mBtnOK;
  // private Button   mBtnCancel;

  Sketch3dNewDialog( Context context, INewPlot maker, TopoDroidApp app )
  {
    super( context, R.string.Sketch3dNewDialog );
    mApp    = app;
    mMaker  = maker;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout( R.layout.sketch3d_new_dialog, R.string.title_sketch_new );

    mEditName  = (EditText) findViewById(R.id.edit_sketch3d_name);
    mEditStart = (EditText) findViewById(R.id.edit_sketch3d_start);
    mEditNext  = (EditText) findViewById(R.id.edit_sketch3d_next);

    // mEditName.setHint( R.string.scrap_name );
    // mEditStart.setHint( R.string.station_base );
    // mEditNext.setHint( R.string.station_next );

    mBtnOK = (Button) findViewById(R.id.button_ok);
    mBtnOK.setOnClickListener( this );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);
    // mBtnCancel.setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    String error;
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      String name  = mEditName.getText().toString();
      String start = mEditStart.getText().toString();
      String next  = mEditNext.getText().toString();

      name = TopoDroidUtil.noSpaces( name );
      if ( name == null || name.length() == 0 ) {
        error = mContext.getResources().getString( R.string.error_name_required );
        mEditName.setError( error );
        return;
      } else {
        start = TopoDroidUtil.noSpaces( start );
        if ( start == null || start.length() == 0 ) {
          error = mContext.getResources().getString( R.string.error_start_required );
          mEditStart.setError( error );
          return;
        } else {
          // if ( next != null ) {
            next = TopoDroidUtil.noSpaces( next );
            if ( next.length() > 0 ) {
              if ( ! TopoDroidApp.mData.hasShot( mApp.mSID, start, next ) ) {
                error = mContext.getResources().getString( R.string.no_shot_between_stations );
                mEditNext.setError( error );
                return;
              }
            } else {
              next = null;
            }
          // }
          mMaker.makeNewSketch3d( name, start, next );
        }
      }
    }
    dismiss();
  }
}

