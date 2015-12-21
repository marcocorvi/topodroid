/* @file CalibValidateResultDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration validation results
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

// import android.util.Log;

public class CalibValidateResultDialog extends Dialog
{
  private Context mContext;

  private String avestd0;
  private String avestd1;
  private String std;
  private String err1;
  private String err2;
  private String errmax;
  private String title;

  public CalibValidateResultDialog( Context context, double a0, double s0, double a1, double s1,
                                                     double e1, double e2, double em, String n1, String n2 )
  {
    super( context );
    mContext = context;

    avestd0 = String.format( mContext.getResources().getString( R.string.calib_ave_std ), a0, s0 );
    avestd1 = String.format( mContext.getResources().getString( R.string.calib_ave_std ), a1, s1 );
    err1  = String.format( mContext.getResources().getString( R.string.calib_error ), e1 );
    err2 = String.format( mContext.getResources().getString( R.string.calib_stddev ), e2 );
    errmax = String.format( mContext.getResources().getString( R.string.calib_max_error ), em );
    title = String.format( mContext.getResources().getString( R.string.calib_validation ), n1, n2 );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.calib_validate_result_dialog);

    ((TextView)findViewById(R.id.avestd0)).setText( avestd0 );
    ((TextView)findViewById(R.id.avestd1)).setText( avestd1 );
    ((TextView)findViewById(R.id.error1)).setText( err1 );
    ((TextView)findViewById(R.id.error2)).setText( err2 );
    ((TextView)findViewById(R.id.error_max)).setText( errmax );

    setTitle( title );
  }

}

