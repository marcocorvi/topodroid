/** @file TdmSurveysDialog
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager dialog to enter the filename of a project
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.util.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.SurveyCheckAdapter;
import com.topodroid.TDX.R;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
// import android.widget.ArrayAdapter;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout;

import java.util.List;
import java.util.ArrayList;

class TdmSurveysDialog extends MyDialog
                       implements android.view.View.OnClickListener
                                , android.widget.AdapterView.OnItemClickListener
{
  TdmViewActivity mParent;
  ArrayList<String> mSurveys;
  // private Button mBtnFewer;
  // private Button mBtnMore;
  // private Button mBtnReset;
  // private SurveyCheckAdapter mAdapter = null;

  /** cstr
   * @param ctx    context
   * @param parent parent activity
   * @param names  survey names
   */
  TdmSurveysDialog( Context ctx, TdmViewActivity parent, ArrayList<String> names )
  {
    super( ctx, null, R.string.TdmSurveysDialog );
    mParent  = parent;
    mSurveys = names;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.tdm_surveys_dialog, R.string.title_tdsurveys );

    Button mBtnFewer = (Button) findViewById( R.id.button_fewer );
    Button mBtnMore  = (Button) findViewById( R.id.button_more  );
    Button mBtnReset = (Button) findViewById( R.id.button_reset );
    mBtnFewer.setOnClickListener( this );
    mBtnMore.setOnClickListener( this );
    mBtnReset.setOnClickListener( this );

    // LinearLayout body = (LinearLayout) findViewById( R.id.body );
    // ViewGroup.LayoutParams params = body.getLayoutParams();
    // params.height += mSurveys.size() * 40;
    // body.setLayoutParams( params );

    ( (Button) findViewById(R.id.button_cancel) ).setOnClickListener( this );
    ListView list = (ListView)findViewById( R.id.list );
    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>( mContext, R.layout.message );
    for ( String survey : mSurveys ) adapter.add( survey );
    list.setAdapter( adapter );
    list.setOnItemClickListener( this );
    // mAdapter = new SurveyCheckAdapter( mContext, R.layout.select_text_row, mSurveys );
    // list.setAdapter( mAdapter );

    // list.setOnItemLongClickListener( this );
    list.setDividerHeight( 2 );
  }
    
  @Override public void onClick( View v ) 
  {
    if ( v.getId() == R.id.button_fewer ) {
      TDLog.v("stations less");
      mParent.changeStationRate( -1 );
    } else if ( v.getId() == R.id.button_more ) {
      TDLog.v("stations more");
      mParent.changeStationRate( +1 );
    } else if ( v.getId() == R.id.button_reset ) {
      TDLog.v("stations reset");
      // List< String > surveys = mAdapter.getSelectedSurveys();
      // if ( surveys.size() > 1 ) {
      //   return;
      // } else if ( surveys.size() == 0 ) {
      //   // error: no check
      //   return;
      // }
      // String name = surveys.get( 0 );
      // mParent.showOnlySurvey( name );
      mParent.resetStationRate();
      mParent.showAllSurveys();
    }
    dismiss();
  }

  /** implements user tap on an item
   * @param parent    parent view
   * @param view      tapped view
   * @param pos       item position in the list
   * @param id        ...
   */
  @Override
  public void onItemClick( android.widget.AdapterView<?> parent, View view, int pos, long id )
  {
    if ( pos >= 0 || pos < mSurveys.size() ) {
      String name = mSurveys.get( pos );
      if ( name != null ) {
        mParent.showOnlySurvey( name );
      }
    }
    dismiss();
  }
      
}

