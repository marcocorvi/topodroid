/* @file DialogSurveys.java
 *
 * @author marco corvi
 * @date jul 2020
 *
 * @brief list surveys
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

import java.util.List;

// import android.app.Dialog;
import android.os.Bundle;
// import android.content.Intent;
import android.content.Context;

// import android.widget.ArrayAdapter;
// import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Button;

import android.view.View;
import android.view.View.OnClickListener;

class DialogSurveys extends MyDialog
                    implements OnClickListener
{
  private TopoGL  mApp;
  private List< Cave3DSurvey > mSurveys;

  private SurveyAdapter mAdapter;
  private ListView mList;

  DialogSurveys( Context context, TopoGL app, List< Cave3DSurvey > surveys )
  {
    super( context, R.string.DialogSurveys );
    mApp     = app;
    mSurveys = surveys;
    mAdapter = new SurveyAdapter( mContext, mApp, R.layout.survey_row, surveys );
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.cave3d_surveys_dialog, R.string.survey_list );

    ((Button)findViewById( R.id.button_ok)).setOnClickListener( this );
    ((Button)findViewById( R.id.button_cancel)).setOnClickListener( this );

    mList = (ListView) findViewById( R.id.surveys_list );
    mList.setDividerHeight( 2 );

    mList.setAdapter( mAdapter );
  }

  @Override
  public void onBackPressed()
  {
    // mApp.toast( "surveys dialog done" );
    mApp.hideOrShow( mSurveys );
    dismiss();
  }

  @Override
  public void onClick( View v )
  {
    if ( v.getId() == R.id.button_ok ) {
      mApp.hideOrShow( mSurveys );
    } // else only button_cancel
    dismiss();
  }

}
