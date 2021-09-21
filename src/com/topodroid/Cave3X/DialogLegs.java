/* @file DialogLegs.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D legs visibility dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

// import com.topodroid.utils.TDLog;
// import com.topodroid.Cave3X.R;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

class DialogLegs extends Dialog 
                  implements View.OnClickListener
{
  private Context mContext;

  private CheckBox mCBsurface;
  private CheckBox mCBduplicate;
  private CheckBox mCBcommented;

  public DialogLegs( Context context )
  {
    super( context );
    mContext  = context;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.cave3d_legs_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    Button buttonOK     = (Button) findViewById( R.id.button_ok );
    Button buttonCancel = (Button) findViewById( R.id.button_cancel );
    buttonOK.setOnClickListener( this );
    buttonCancel.setOnClickListener( this );

    mCBsurface   = (CheckBox) findViewById( R.id.surface );
    mCBduplicate = (CheckBox) findViewById( R.id.duplicate );
    mCBcommented = (CheckBox) findViewById( R.id.commented );

    mCBsurface.setChecked(   GlModel.showLegsSurface );
    mCBduplicate.setChecked( GlModel.showLegsDuplicate );
    mCBcommented.setChecked( GlModel.showLegsCommented );

    setTitle( R.string.ctitle_legs );
  }

  @Override
  public void onClick(View v)
  {
    // TDLog.v( "Legs onClick()" );
    if ( v.getId() == R.id.button_ok ) {
      GlModel.showLegsSurface   = mCBsurface.isChecked();
      GlModel.showLegsDuplicate = mCBduplicate.isChecked();
      GlModel.showLegsCommented = mCBcommented.isChecked();
    }
    dismiss();
  }
}

