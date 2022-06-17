/* @file ReductionErrorsDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration coefficients display dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;

import java.lang.ref.WeakReference;
import java.util.Locale;

import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;

import android.graphics.Bitmap;

public class ReductionErrorsDialog extends MyDialog
                       implements View.OnClickListener
{
  // private final WeakReference<DrawingActivity> mParent; 

  // private Button mButtonBack;

  private int mMultiBad;
  private int mMagneticBad;
  private boolean mExtendBad;
  private boolean mAttachedBad;

  public ReductionErrorsDialog( Context context, // DrawingWindow parent,
                    int multi_bad, int magnetic_bad, boolean extend_bad, boolean attached_bad )
  {
    super( context, null, R.string.ReductionErrorsDialog ); // null app
    // mParent = new WeakReference<GMActivity>( parent );

    mMultiBad    = multi_bad;
    mMagneticBad = magnetic_bad;
    mExtendBad   = extend_bad;
    mAttachedBad = attached_bad;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.reduction_errors_dialog, R.string.title_reduction_errors );

    TextView textMulti  = (TextView) findViewById(R.id.multi_bad);
    if ( mMultiBad == 0 ) {
      textMulti.setVisibility( View.GONE );
    }
      
    TextView textMagn   = (TextView) findViewById(R.id.magnetic_bad);
    if ( mMagneticBad == 0 ) {
      textMagn.setVisibility( View.GONE );
    }

    TextView textExtend = (TextView) findViewById(R.id.extend_bad);
    if ( ! mExtendBad ) {
      textExtend.setVisibility( View.GONE );
    }

    TextView textAttach = (TextView) findViewById(R.id.attached_bad);
    if ( ! mAttachedBad ) {
      textAttach.setVisibility( View.GONE );
    }

    Button button_back  = (Button) findViewById( R.id.button_back );
    button_back.setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    // int id = v.getId();
    dismiss();
  }

}

