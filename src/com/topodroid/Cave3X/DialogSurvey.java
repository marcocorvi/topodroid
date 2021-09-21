/* @file DialogSurvey.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D drawing infos dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

// import com.topodroid.utils.TDLog;
// import com.topodroid.Cave3X.R;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

class DialogSurvey extends Dialog 
                   implements View.OnClickListener
{
  // private Button mBtnOk;

  private TopoGL mApp;
  private Cave3DSurvey mSurvey;

  public DialogSurvey( TopoGL app, Cave3DSurvey survey )
  {
    super( app );
    mApp    = app;
    mSurvey = survey;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.cave3d_survey_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    TextView tv;
    tv = (TextView) findViewById( R.id.survey_legs );
    tv.setText( Integer.toString( mSurvey.getShotNr() ) );
    tv = (TextView) findViewById( R.id.survey_legs_length );
    tv.setText( Integer.toString( (int)(mSurvey.mLenShots) ) );
    tv = (TextView) findViewById( R.id.survey_splays );
    tv.setText( Integer.toString( mSurvey.getSplayNr() ) );
    tv = (TextView) findViewById( R.id.survey_splays_length );
    tv.setText( Integer.toString( (int)(mSurvey.mLenSplays) ) );

    Button btn_close = (Button) findViewById( R.id.btn_close );
    btn_close.setOnClickListener( this );

    setTitle( mSurvey.name );
  }

  @Override
  public void onClick(View view)
  {
    dismiss();
  }

}

