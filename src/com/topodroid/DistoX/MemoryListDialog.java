/* @file MemoryListDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid list distox memory
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20131204 cstr
 */
package com.topodroid.DistoX;

import java.util.List;
// import java.io.StringWriter;
// import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

public class MemoryListDialog extends Dialog
                              // implements View.OnClickListener
                              // , OnItemClickListener
{
  public long mSID;
  DeviceActivity mParent;
  Context mContext;

  // private Button mBtnCancel;

  List< MemoryOctet> mMemory;
  ArrayAdapter< String > mArrayAdapter;
  ListView mList;

  public MemoryListDialog( Context context, DeviceActivity parent, List<MemoryOctet> memory )
  {
    super( context );
    mContext = context;
    mParent = parent;
    mMemory = memory;
  }

  // @Override
  // public void onClick(View v) 
  // {
  //   Button b = (Button)v;
  //   if ( b == mBtnCancel ) {
  //     dismiss();
  //   }
  //   dismiss();
  // }

  // @Override
  // public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  // {
  // }

  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    setContentView(R.layout.memory_list_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mArrayAdapter = new ArrayAdapter<String>( mParent, R.layout.message );
    mList = (ListView) findViewById(R.id.list_memory);
    mList.setAdapter( mArrayAdapter );
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // TODO fill the list
    for ( MemoryOctet m : mMemory ) {
      mArrayAdapter.add( m.toString() );
    }
    // mBtnCancel = (Button) findViewById(R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    setTitle( mContext.getResources().getString( R.string.memory_data ) );
  }
}

