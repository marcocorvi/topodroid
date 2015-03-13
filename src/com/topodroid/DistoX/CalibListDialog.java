/** @file CalibListDialog.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid calibs list for a device
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;

import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

public class CalibListDialog extends Dialog
                        implements OnItemClickListener
                                // , OnItemLongClickListener
                                , View.OnClickListener
{
  private Context mContext;
  private DeviceActivity mParent;
  private TopoDroidApp mApp;
  private ArrayAdapter<String> mArrayAdapter;
  // private ListItemAdapter mArrayAdapter;
  private Button mBtnNew;
  // private Button mBtnCancel;

  private ListView mList;

  public CalibListDialog( Context context, DeviceActivity parent, TopoDroidApp app )
  {
    super( context );
    mContext = context;
    mParent = parent;
    mApp = app;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calib_list_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    // mArrayAdapter = new ListItemAdapter( mContext, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnNew     = (Button) findViewById(R.id.button_new);
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);

    mBtnNew.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );

    setTitle( R.string.title_calib );
    updateList();
  }

  private void updateList()
  {
    if ( mApp.mData != null && mApp.mDevice != null ) {
      List< String > list = mApp.mData.selectDeviceCalibs( mApp.mDevice.mAddress );
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
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "CalibListDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnNew ) {
      hide();
      mParent.openCalibration( null );
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
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "CalibListDialog onItemClick() " + name );
    // TODO open calibration activity
    mParent.openCalibration( name );
    dismiss();
  }

}
