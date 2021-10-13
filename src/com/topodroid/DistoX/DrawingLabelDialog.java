/* @file DrawingLabelDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for the text of a label-point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.LinearLayout;


class DrawingLabelDialog extends MyDialog
                         implements View.OnClickListener
{
  private EditText mLabel;

  private final ILabelAdder mActivity;
  private final float mX;
  private final float mY;

  // GUI widgets
  private CheckBox mCBbase  = null;  // canvas levels
  private CheckBox mCBfloor = null;
  private CheckBox mCBfill  = null;
  private CheckBox mCBceil  = null;
  private CheckBox mCBarti  = null;
  // private CheckBox mCBform  = null;
  // private CheckBox mCBwater = null;
  // private CheckBox mCBtext  = null;

  DrawingLabelDialog( Context context, ILabelAdder activity, float x, float y )
  {
    super(context, R.string.DrawingLabelDialog );
    mActivity = activity;
    mX = x; 
    mY = y;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.drawing_label_dialog, R.string.label_title );

    mLabel     = (EditText) findViewById(R.id.label_text);

    ((Button) findViewById(R.id.label_ok)).setOnClickListener( this );
    ((Button) findViewById(R.id.label_cancel)).setOnClickListener( this );

    mLabel.setTextSize( TDSetting.mTextSize );

    if ( TDSetting.mWithLevels > 1 ) {
      setCBlayers();
    } else {
      LinearLayout ll = (LinearLayout) findViewById( R.id.layer_layout );
      ll.setVisibility( View.GONE );
    }
  }

  private void setCBlayers()
  {
    mCBbase  = (CheckBox) findViewById( R.id.cb_layer_base  );
    mCBfloor = (CheckBox) findViewById( R.id.cb_layer_floor );
    mCBfill  = (CheckBox) findViewById( R.id.cb_layer_fill  );
    mCBceil  = (CheckBox) findViewById( R.id.cb_layer_ceil  );
    mCBarti  = (CheckBox) findViewById( R.id.cb_layer_arti  );
    // mCBform  = (CheckBox) findViewById( R.id.cb_layer_form  );
    // mCBwater = (CheckBox) findViewById( R.id.cb_layer_water );
    // mCBtext  = (CheckBox) findViewById( R.id.cb_layer_text  );
    mCBbase .setChecked( true );
    mCBfloor.setChecked( false);
    mCBfill .setChecked( false);
    mCBceil .setChecked( false);
    mCBarti .setChecked( true );
    // mCBform .setChecked( true );
    // mCBwater.setChecked( true );
    // mCBtext .setChecked( true );
  }

  private int getLevel()
  {
    if ( TDSetting.mWithLevels < 2 ) return DrawingLevel.LEVEL_DEFAULT;
    int level = 0;
    if ( mCBbase .isChecked() ) level |= DrawingLevel.LEVEL_BASE;
    if ( mCBfloor.isChecked() ) level |= DrawingLevel.LEVEL_FLOOR;
    if ( mCBfill .isChecked() ) level |= DrawingLevel.LEVEL_FILL;
    if ( mCBceil .isChecked() ) level |= DrawingLevel.LEVEL_CEIL;
    if ( mCBarti .isChecked() ) level |= DrawingLevel.LEVEL_ARTI;
    // if ( mCBform .isChecked() ) level |= DrawingLevel.LEVEL_FORM;
    // if ( mCBwater.isChecked() ) level |= DrawingLevel.LEVEL_WATER;
    // if ( mCBtext .isChecked() ) level |= DrawingLevel.LEVEL_TEXT;
    return level;
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingLabelDialog onClick() " + view.toString() );
    if (view.getId() == R.id.label_ok ) {
      mActivity.addLabel( mLabel.getText().toString(), mX, mY, getLevel() );
    // } else if ( view.getId() == R.id.label_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }
}
        

