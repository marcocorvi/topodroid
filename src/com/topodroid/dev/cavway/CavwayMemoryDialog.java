/* @file DeviceCavwayMemoryDialog.java
 *
 * @author Siwei Tian
 * @date july 2024
 *
 * @brief TopoDroid Cavway XBLE memory dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

import com.topodroid.ui.MyDialog;
import com.topodroid.packetX.CavwayData;
import com.topodroid.TDX.DeviceActivity;
import com.topodroid.TDX.R;
// import com.topodroid.dev.distox.IMemoryDialog;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;
import android.content.Context;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;
// import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

public class CavwayMemoryDialog extends MyDialog
        implements View.OnClickListener
                 // , IMemoryDialog
{
  // private Button mBtnDump;
  // private Button mBtnBack;

  private EditText mETnumber;
  private EditText mETdumpfile;

  // List< MemoryOctet> mMemory;
  private ArrayAdapter< String > mArrayAdapter;
  private ListView mList;

  private final DeviceActivity mParent;

  /** cstr 
   * @param context  context
   * @param parent   device activity
   */
  public CavwayMemoryDialog( Context context, DeviceActivity parent )
  {
    super( context, null, R.string.DeviceCavwayMemoryDialog ); // null app FIXME DeviceCavwayMemoryDialog dialog help page
    mParent = parent;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.device_cavway_memory_dialog, R.string.memoryCavway );

    mETnumber    = (EditText) findViewById( R.id.et_number );
    mETdumpfile  = (EditText) findViewById( R.id.et_dumpfile );

    Button btnDump = (Button) findViewById(R.id.button_dump );
    btnDump.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_cancel);
    // mBtnBack.setOnClickListener( this );

    mArrayAdapter = new ArrayAdapter<>( mParent, R.layout.message );
    mList = (ListView) findViewById(R.id.list_memory);
    mList.setAdapter( mArrayAdapter );
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // int[] ht = new int[2];
    // mParent.retrieveDeviceHeadTail( ht );
    // setText( mTVshead, mTVstail, ht );
  }

  public void updateList( ArrayList< CavwayData > memory )
  {
    mArrayAdapter.clear();
    for ( CavwayData m : memory ) mArrayAdapter.add( m.toString() );
    mList.invalidate();
  }

  public void appendToList( CavwayData data )
  {
    if ( data != null ) {
      mArrayAdapter.add( data.toString() );
      mList.invalidate();
    }
  }



  @Override
  public void onClick( View view )
  {
    int nr = 0;
    String number, error;
    if ( view.getId() == R.id.button_dump ) {
      number = mETnumber.getText().toString();
      if ( /* number == null || */ number.length() == 0 ) {
        error = mParent.getResources().getString( R.string.error_number_required );
        mETnumber.setError( error );
        return;
      }
      try {
        nr = Integer.parseInt( number );
      } catch ( NumberFormatException e ) {
        error = mParent.getResources().getString( R.string.error_invalid_number );
        mETnumber.setError( error );
        return;
      }
      nr = CavwayDetails.boundNumber( nr );
      String file = null;
      if ( mETdumpfile.getText() != null ) file = mETdumpfile.getText().toString();
      mParent.readCavwayX1Memory( this, nr, file );
      // } else if ( view.getId() == R.id.button_cancel ) {
      //   dismiss();
      //   break;
    }
  }

  /** set the value in the NUMBER (ie, FROM) field
   * @param index   value
   * @note same as in IMemoryDialog
   */
  public void setIndex( int index ) 
  {
    mETnumber.setText( String.format( Locale.US, "%d", index ) );
  }

}

