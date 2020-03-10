/** @file TdmEquatesDialog.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager list of equates
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.io.File;

import android.content.DialogInterface;
import android.app.Dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

class TdmEquatesDialog extends MyDialog
                      implements OnItemClickListener
                      , OnClickListener
{
  Context mContext;
  TdmEquateAdapter mTdmEquateAdapter;
  TdmConfig mConfig;
  TdmViewActivity mActivity;
  ListView mList;
  ArrayList< TdmEquate > mEquates;

  TdmEquatesDialog( Context context, TdmConfig config, TdmViewActivity activity )
  {
    super( context, R.string.TdmEquatesDialog );
    mContext = context;
    mConfig  = config;
    mActivity = activity;
    if ( mConfig != null && mConfig.mEquates != null ) {
      mEquates = mConfig.mEquates;
    } else {
      mEquates = new ArrayList< TdmEquate >();
    }
    // Log.v("TdManager", "TdmEquatesDialog equates " + mEquates.size() );
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout( R.layout.tdequates_dialog, R.string.title_equates );

    Button back = (Button)findViewById( R.id.button_back );
    back.setOnClickListener( this );

    mList = (ListView) findViewById(R.id.list);
    mList.setDividerHeight( 2 );
    mList.setOnItemClickListener( this );

    updateList();
  }

  void updateList()
  {
    if ( mEquates != null && mEquates.size() > 0 ) {
      mTdmEquateAdapter = new TdmEquateAdapter( mContext, R.layout.tdequate_adapter, mEquates );
      mList.setAdapter( mTdmEquateAdapter );
      // mList.invalidate();
    } else {
      hide();
      TDToast.make( R.string.no_equate );
      dismiss();
    }
  }

  void doRemoveEquate( TdmEquate equate )
  {
    mEquates.remove( equate );
    updateList();
    if ( mActivity != null ) mActivity.updateViewEquates();
  }

  void askRemoveEquate( final TdmEquate equate )
  {
    TopoDroidAlertDialog.makeAlert( mContext, mContext.getResources(), 
      String.format( mContext.getResources().getString( R.string.ask_remove_equate ), equate.stationsString() ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doRemoveEquate( equate );
        }
    } );
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    TdmEquateViewHolder vh = (TdmEquateViewHolder) view.getTag();
    if ( vh != null ) {
      askRemoveEquate( vh.equate );
    }
  }

  @Override
  public void onClick( View v ) 
  {
    // if ( v.getId() == R.id.button_back ) {
    // }
    dismiss();
  }
      
}
