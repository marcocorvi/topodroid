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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyDialog;

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

public class RecalibrateDialog extends MyDialog
                      implements OnItemClickListener
                      , View.OnClickListener
                      // , OnItemLongClickListener
{
  private final ShotWindow mParent;
  private ArrayAdapter<String> mArrayAdapter;
  private Button mBtnCancel;
  private Button mBtnOk;

  private ListView mList;
  private TextView mCalibText;
  private long mShotId;
  private String mCalibName = null;

  public RecalibrateDialog( Context context, ShotWindow parent, long shot_id )
  {
    super( context, null, R.string.RecalibrateDialog ); 
    mParent = parent;
    mShotId = shot_id;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout( R.layout.recalibrate_dialog, R.string.title_recalibrate );

    mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnOk  = (Button) findViewById(R.id.button_ok);
    mBtnCancel = (Button) findViewById(R.id.button_cancel);

    mBtnOk.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    mCalibText = (TextView) findViewById( R.id.calib_name );

    // setTitle( R.string.title_calib );
    updateList();
    setCalibText();
  }

  private void updateList()
  {
    if ( TopoDroidApp.mDData != null && TDInstance.getDeviceA() != null ) {
      List< String > list = TopoDroidApp.mDData.selectDeviceCalibs( TDInstance.deviceAddress() );
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
    if ( b == mBtnOk ) {
      if ( mCalibName != null ) { 
        mParent.doRecalibrate( mShotId, mCalibName );
      }
    } else if ( b == mBtnCancel ) {
      /* nothing */
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
    if ( ! ( view instanceof TextView ) ) {
      TDLog.e("calib list view instance of " + view.toString() );
      return;
    }
    String name = ((TextView) view).getText().toString();
    int len = name.indexOf(" ");
    mCalibName = name.substring(0, len);
    setCalibText();
  }

  private void setCalibText()
  {
    mCalibText.setText( TDInstance.formatString( R.string.recalibrate_with, ( TDString.isNullOrEmpty( mCalibName )? "-" : mCalibName ) ) );
  }

}
