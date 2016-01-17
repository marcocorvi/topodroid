/** @file DrawingAreaDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch line attributes editing dialog
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class DrawingAreaDialog extends Dialog
                               implements View.OnClickListener
{
  private DrawingAreaPath mArea;
  private DrawingActivity mParent;
  private boolean mOrientable;

  private CheckBox mCBvisible;

  private OrientationWidget mOrientationWidget; 

  private Button   mBtnOk;

  public DrawingAreaDialog( DrawingActivity context, DrawingAreaPath line )
  {
    super( context );
    mParent = context;
    mArea = line;
    mOrientable = DrawingBrushPaths.mAreaLib.isSymbolOrientable( mArea.mAreaType );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_area_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( String.format( mParent.getResources().getString( R.string.title_draw_area ),
              DrawingBrushPaths.mAreaLib.getSymbolName( mArea.mAreaType ) ) );

    mOrientationWidget = new OrientationWidget( this, mOrientable, mArea.mOrientation );

    mCBvisible = (CheckBox) findViewById( R.id.area_visible );
    mCBvisible.setChecked( mArea.isVisible() );

    // NOTE area do not have options

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingAreaDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      mArea.setVisible( mCBvisible.isChecked() );
      if ( mOrientable ) {
        mArea.setOrientation( mOrientationWidget.mOrient );
      }
    }
    dismiss();
  }

}

