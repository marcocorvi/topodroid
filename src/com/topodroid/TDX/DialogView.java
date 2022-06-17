/* @file DialogView.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D drawing infos dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

class DialogView extends MyDialog 
                 implements View.OnClickListener
{
  private Button mBtnOk;

  private TopoGL  mTopoGl;
  private GlRenderer mRenderer;

  private Button   mButtonOK;
  private Button   mButtonCancel;
  private Button mRBtop;
  private Button mRBeast;
  private Button mRBnorth;
  private Button mRBwest;
  private Button mRBsouth;
  private CheckBox mCBzoom;

  /** cstr
   * @param context  context
   * @param topogl   3D viewer activity
   * @param renderer screen renderer
   */
  public DialogView( Context context, TopoGL topogl, GlRenderer renderer )
  {
    super( context, null, R.string.DialogView ); // null app
    mTopoGl   = topogl;
    mRenderer = renderer;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.cave3d_view_dialog, R.string.view_title );

    mButtonOK = (Button) findViewById( R.id.button_ok );
    mButtonCancel = (Button) findViewById( R.id.button_cancel );
    mButtonOK.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );

    mRBtop   = (Button) findViewById( R.id.view_top );
    mRBeast  = (Button) findViewById( R.id.view_east );
    mRBnorth = (Button) findViewById( R.id.view_north );
    mRBwest  = (Button) findViewById( R.id.view_west );
    mRBsouth = (Button) findViewById( R.id.view_south );
    mRBtop.setOnClickListener( this );
    mRBeast.setOnClickListener( this );
    mRBnorth.setOnClickListener( this );
    mRBwest.setOnClickListener( this );
    mRBsouth.setOnClickListener( this );

    mCBzoom  = (CheckBox) findViewById( R.id.view_zoom  );
    mCBzoom.setChecked( true );

  }

  /** respond to user taps
   * @param v  tapped view
   */
  @Override
  public void onClick(View v)
  {
    // TDLog.v( "View onClick()" );
    int id = v.getId();
    if ( id == R.id.button_cancel ) {
      /* nothing */
    } else {
      if ( id == R.id.view_top ) {
        mRenderer.setAngles( 180, -90 );
      } else if ( id == R.id.view_north ) {
        mRenderer.setAngles(   0, 0 );
      } else if ( id == R.id.view_west ) {
        mRenderer.setAngles( 270, 0 );
      } else if ( id == R.id.view_south ) {
        mRenderer.setAngles( 180, 0 );
      } else if ( id == R.id.view_east ) {
        mRenderer.setAngles(  90, 0 );
      }
      if ( mCBzoom.isChecked() ) mRenderer.zoomOne();
      mTopoGl.refresh();
    }
    dismiss();
  }

}

