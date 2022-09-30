/** @file TdmInfoDialog.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager surveys infos
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.ui.MyDialog;
import com.topodroid.TDX.TopoDroidApp;
// import com.topodroid.TDX.TopoDroidAlertDialog;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

import java.util.ArrayList;
// import java.io.File;

// import android.content.DialogInterface;
// import android.app.Dialog;

import android.view.View;
import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.ListView;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

import android.content.Context;
// import android.content.Intent;
import android.os.Bundle;

class TdmInfoDialog extends MyDialog
                    implements OnClickListener
{
  // Context mContext;
  TopoDroidApp mApp;
  TdmConfig mConfig;
  ListView mList;
  TdmInfoAdapter mTdmInfoAdapter;

  TdmInfoDialog( Context context, TopoDroidApp app, TdmConfig config )
  {
    super( context, null, R.string.TdmInfoDialog ); // null app
    // mContext = context;
    mApp     = app;
    mConfig  = config;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout( R.layout.tdminfo_dialog, R.string.title_infos );

    Button back = (Button)findViewById( R.id.button_back );
    back.setOnClickListener( this );

    mList = (ListView) findViewById(R.id.list);
    mList.setDividerHeight( 2 );

    updateList();
  }

  void updateList()
  {
    ArrayList< TdmInput > surveys = mConfig.getInputs();
    if ( surveys != null && surveys.size() > 0 ) {
      mTdmInfoAdapter = new TdmInfoAdapter( mContext, mApp, R.layout.tdinfo_adapter, surveys );
      mList.setAdapter( mTdmInfoAdapter );
      mList.invalidate();
    } else {
      hide();
      TDToast.make( R.string.no_surveys );
      dismiss();
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
