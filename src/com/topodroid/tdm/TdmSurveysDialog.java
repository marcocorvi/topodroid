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

import com.topodroid.ui.MyDialog;
import com.topodroid.TDX.R;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

class TdmSurveysDialog extends MyDialog
                       implements OnItemClickListener
                                , View.OnClickListener
{
  TdmViewActivity mParent;
  ArrayList<String> mSurveys;

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

    ( (Button) findViewById(R.id.button_cancel) ).setOnClickListener( this );
    ListView list = (ListView)findViewById( R.id.list );
    ArrayAdapter<String> adapter = new ArrayAdapter<>( mContext, R.layout.message );
    for ( String survey : mSurveys ) adapter.add( survey );
    list.setAdapter( adapter );
    list.setOnItemClickListener( this );
    // list.setOnItemLongClickListener( this );
    list.setDividerHeight( 2 );
  }
    
  @Override public void onClick( View v ) 
  {
    dismiss();
  }

  /** implements user tap on an item
   * @param parent    parent view
   * @param view      tapped view
   * @param pos       item position in the list
   * @param id        ...
   */
  @Override
  public void onItemClick( AdapterView<?> parent, View view, int pos, long id )
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

