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
package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;
// import com.topodroid.Cave3X.R;

import java.util.List;

import android.app.Dialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

class DialogSurveys extends Dialog
{
  private Context mContext;
  private TopoGL  mApp;
  private List< Cave3DSurvey > mSurveys;

  private SurveyAdapter mAdapter;
  private ListView mList;

  DialogSurveys( Context ctx, TopoGL app, List< Cave3DSurvey > surveys )
  {
    super( ctx );
    mContext = ctx;
    mApp     = app;
    mSurveys = surveys;
    mAdapter = new SurveyAdapter( mContext, mApp, R.layout.survey_row, surveys );
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    setContentView(R.layout.cave3d_surveys_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( R.string.survey_list );
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

}
