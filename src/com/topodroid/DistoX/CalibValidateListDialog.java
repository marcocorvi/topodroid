/** @file CalibValidateListDialog.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid calibs list to validate a calibration
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

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
// import android.widget.Button;
import android.widget.Toast;

import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

public class CalibValidateListDialog extends Dialog
                        implements OnItemClickListener
{
  private Context mContext;
  private GMActivity mParent;
  private ArrayAdapter<String> mArrayAdapter;

  private ListView mList;
  private List<String> mCalibs;

  public CalibValidateListDialog( Context context, GMActivity parent, List<String> calibs )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mCalibs  = calibs;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calib_validate_list_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    setTitle( R.string.title_calib );
    for ( String item : mCalibs ) {
      mArrayAdapter.add( item );
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
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "CalibValidate ListDialog onItemClick() " + name );
    // TODO open calibration activity
    mParent.validateCalibration( name );
    dismiss();
  }

}
