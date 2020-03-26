/* @file CalibValidateListDialog.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid calibs list to validate a calibration
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
import android.widget.ListView;
// import android.widget.Button;

import android.widget.TextView;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

class CalibValidateListDialog extends MyDialog
                              implements OnItemClickListener
                              , OnClickListener
{
  private final GMActivity mParent;
  // private ArrayAdapter<String> mArrayAdapter;

  // private ListView mList;
  private final List<String> mCalibs;
  private Button mBtnCancel;

  CalibValidateListDialog( Context context, GMActivity parent, List<String> calibs )
  {
    super( context, R.string.CalibValidateListDialog );
    mParent  = parent;
    mCalibs  = calibs;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout( R.layout.calib_validate_list_dialog, R.string.title_calib );

    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );

    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );

    ListView list = (ListView) findViewById(R.id.list);
    list.setAdapter( arrayAdapter );
    list.setOnItemClickListener( this );
    list.setDividerHeight( 2 );

    // setTitle( R.string.title_calib );
    for ( String item : mCalibs ) {
      arrayAdapter.add( item );
    }
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
    // TDLog.Log(  TDLog.LOG_INPUT, "CalibValidate ListDialog onItemClick() " + name );
    // TODO open calibration activity
    mParent.validateCalibration( name );
    dismiss();
  }

  @Override
  public void onClick( View v )
  {
    // can be only cancel
    dismiss();
  }

}
