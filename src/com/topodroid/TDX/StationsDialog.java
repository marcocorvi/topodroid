/* @file StationsDialog.java
 *
 * @author marco corvi
 * @date jul 2020
 *
 * @brief TopoDroid stations dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;

import android.os.Bundle;
import android.content.Context;

import android.widget.Button;
import android.widget.ListView;

import android.view.View;
import android.view.View.OnClickListener;

// import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import java.io.IOException;

class StationsDialog extends MyDialog
                     implements OnClickListener
                     // , OnItemClickListener
                     // , OnItemLongClickListener
{
  private final SurveyWindow mParent;
  private ListView mList;
  // private Button   mButtonCancel;
  // private Button   mButtonOK;
  // private Button   mButtonImport;

  private ArrayList< StationMap > mStations;
  private StationAdapter mAdapter;

  /** cstr
   * @param context   context
   * @param parent    parent window
   * @param audios    audio list
   * @param shots     data list
   */
  StationsDialog( Context context, SurveyWindow parent, Set< String > stations )
  {
    super( context, null, R.string.StationsDialog ); // null app
    mParent   = parent;
    mStations = new ArrayList< StationMap >();
    for ( String name : stations ) {
      mStations.add( new StationMap( name ) );
      // TDLog.v("Stations dialog: name " + name );
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout(R.layout.stations_dialog, R.string.title_stations );

    mList = (ListView) findViewById( R.id.list );
    // mList.setOnItemClickListener( this );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    mAdapter = new StationAdapter( mContext, mParent, R.layout.two_columns, mStations );
    mList.setAdapter( mAdapter );
    
    ( (Button) findViewById(R.id.button_cancel ) ).setOnClickListener( this );
    ( (Button) findViewById(R.id.button_ok ) ).setOnClickListener( this );
    ( (Button) findViewById(R.id.button_import ) ).setOnClickListener( this );
  }

  public void onClick( View v ) 
  {
    if ( v.getId() == R.id.button_ok ) {
      TDLog.v("Button OK");
      mAdapter.setTargetNames();
      for ( StationMap st : mStations ) TDLog.v( st.mFrom + " -> " + st.mTo );
    } else if ( v.getId() == R.id.button_import ) {
      TDLog.v("Button Import");
    } else {
      TDLog.v("Button Cancel");
    }
    dismiss();
  }


}

