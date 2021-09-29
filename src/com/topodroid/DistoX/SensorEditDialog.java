/* @file SensorEditDialog.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief TopoDroid sensor measurement edit dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;


// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;

class SensorEditDialog extends MyDialog
                       implements View.OnClickListener
{
  private final SensorListActivity mParent;
  private SensorInfo mSensor;

  // private TextView mTVtype;
  // private TextView mTVtitle;
  // private TextView mTVshotname;
  // private TextView mTVvalue;

  private EditText mETcomment;  // sensor comment
  private Button   mButtonOK;
  private Button   mButtonDelete;
  // private Button   mButtonCancel;

  /**
   * @param context   context
   */
  SensorEditDialog( Context context, SensorListActivity parent, SensorInfo sensor )
  {
    super( context, R.string.SensorEditDialog );
    mParent = parent;
    mSensor  = sensor;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_PHOTO, "onCreate" );
    initLayout( R.layout.sensor_edit_dialog, R.string.title_sensor_edit );

    TextView tVtitle      = (TextView) findViewById( R.id.sensor_title );
    TextView tVtype       = (TextView) findViewById( R.id.sensor_type );
    TextView tVvalue      = (TextView) findViewById( R.id.sensor_value );
    TextView tVshotname   = (TextView) findViewById( R.id.sensor_shotname );
    mETcomment    = (EditText) findViewById( R.id.sensor_comment );
    mButtonOK     = (Button) findViewById( R.id.sensor_ok );
    mButtonDelete = (Button) findViewById( R.id.sensor_delete );
    // mButtonCancel = (Button) findViewById( R.id.sensor_cancel );

    tVtitle.setText( mSensor.mTitle );
    tVtype.setText( mSensor.mType );
    tVvalue.setText( mSensor.mValue );
    tVshotname.setText( mSensor.mShotName );
    // public String mSensor.mDate;
    if ( mSensor.mComment != null ) {
      mETcomment.setText( mSensor.mComment );
    }

    mButtonOK.setOnClickListener( this );
    mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "SensorEditDialog onClick() " + b.getText().toString() );

    if ( b == mButtonOK ) {
      String comment = mETcomment.getText().toString();
      // if ( comment == null ) comment = "";
      mParent.updateSensor( mSensor, comment );
    } else if ( b == mButtonDelete ) {
      mParent.dropSensor( mSensor );
    }
    dismiss();
  }

}

