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
import android.content.DialogInterface;

import android.widget.Button;
import android.widget.ListView;

import android.view.View;
import android.view.View.OnClickListener;

import android.util.Pair;

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
      int nr_maps = mStations.size();
      mAdapter.setTargetNames();
      for ( StationMap st : mStations ) TDLog.v( st.mFrom + " -> " + st.mTo );
      ArrayList< Pair<StationMap,StationMap> > conflicting_maps = new ArrayList<>();
      for ( int k=0; k<nr_maps; ++k ) {
        StationMap sm1 = mStations.get( k );
        for ( int h=k+1; h<nr_maps; ++h ) {
          StationMap sm2 = mStations.get( h );
          if ( sm1.mTo.equals( sm2.mTo ) ) conflicting_maps.add( new Pair<StationMap,StationMap>( sm1, sm2 ) );
        }
      }
      TDLog.v("Button OK. Maps " + nr_maps + " conflicts " + conflicting_maps.size() );
      if (  conflicting_maps.size() > 0 ) {
        TopoDroidAlertDialog.makeAlert( mContext, mContext.getResources(), R.string.station_name_conflict, R.string.button_ok, R.string.button_no,
          new DialogInterface.OnClickListener() { @Override public void onClick( DialogInterface dialog, int btn ) 
          { 
            mParent.remapStationNames( mStations ); 
            dismiss();
          } }, null );
      } else {
        mParent.remapStationNames( mStations ); 
        dismiss();
      }
    } else if ( v.getId() == R.id.button_import ) {
      TDLog.v("Button Import: TODO");
      // read a file and update names map
    } else {
      TDLog.v("Button Cancel");
      dismiss();
    }
  }


}

