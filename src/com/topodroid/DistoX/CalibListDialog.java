/* @file CalibListDialog.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid calibs list for a device
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;

import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

class CalibListDialog extends MyDialog
                             implements OnItemClickListener
                             , View.OnClickListener
                             // , OnItemLongClickListener
{
  private DeviceActivity mParent;
  private TopoDroidApp mApp;
  private ArrayAdapter<String> mArrayAdapter;
  // private ListItemAdapter mArrayAdapter;
  private Button mBtnNew;
  private Button mBtnImport;
  // private Button mBtnCancel;
  private Button mBtnReset;

  private ListView mList;

  CalibListDialog( Context context, DeviceActivity parent, TopoDroidApp app )
  {
    super( context, R.string.CalibListDialog );
    mParent = parent;
    mApp = app;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout( R.layout.calib_list_dialog, R.string.title_calib );

    mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    // mArrayAdapter = new ListItemAdapter( mContext, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnNew    = (Button) findViewById(R.id.button_new);
    mBtnImport = (Button) findViewById(R.id.button_import);
    mBtnReset  = (Button) findViewById(R.id.button_reset);
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);

    mBtnNew.setOnClickListener( this );
    mBtnImport.setOnClickListener( this );
    if ( TDLevel.overTester ) {
      mBtnReset.setOnClickListener( this );
    } else {
      mBtnReset.setVisibility( View.GONE );
    }
    // mBtnCancel.setOnClickListener( this );

    // setTitle( R.string.title_calib );
    updateList();
  }

  private void updateList()
  {
    if ( TopoDroidApp.mDData != null && TDInstance.device != null ) {
      List< String > list = TopoDroidApp.mDData.selectDeviceCalibs( TDInstance.device.mAddress );
      // mList.setAdapter( mArrayAdapter );
      mArrayAdapter.clear();
      for ( String item : list ) {
        mArrayAdapter.add( item );
      }
    }
  }
 
  // @Override
  public void onClick(View v) 
  {
    // TDLog.Log(  TDLog.LOG_INPUT, "CalibListDialog onClick() " );
    Button b = (Button) v;
    hide();
    if ( b == mBtnNew ) {
      mParent.openCalibration( null );
    } else if ( b == mBtnImport ) {
      mParent.openCalibrationImportDialog();
    } else if ( b == mBtnReset ) {
      mParent.askCalibReset( b );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

  // ---------------------------------------------------------------
  // list items click

  // @Override 
  // public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  // {
  //   CharSequence item = ((TextView) view).getText();
  //   String value = item.toString();
  //   // String[] st = value.split( " ", 3 );
  //   int from = value.indexOf('<');
  //   int to = value.lastIndexOf('>');
  //   String plot_name = value.substring( from+1, to );
  //   String plot_type = value.substring( to+2 );
  //   mParent.startPlotDialog( plot_name, plot_type ); // context of current SID
  //   dismiss();
  //   return true;
  // }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String name = item.toString();
    int len = name.indexOf(" ");
    name = name.substring(0, len);
    // TDLog.Log(  TDLog.LOG_INPUT, "CalibListDialog onItemClick() " + name );
    // TODO open calibration activity
    mParent.openCalibration( name );
    dismiss();
  }

}
