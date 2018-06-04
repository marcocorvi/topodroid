/* @file DeviceA3MemoryDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX A3 device memory dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

class DeviceA3MemoryDialog extends MyDialog
                           implements View.OnClickListener
                           , IMemoryDialog
{
  private final DeviceActivity mParent;

  private Button mBtnStore;
  private Button mBtnReset;
  private Button mBtnDump;
  private Button mBtnRead;
  // private Button mBtnBack;

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETdumpfrom;
  private EditText mETdumpto;
  private EditText mETdumpfile;
  private TextView mTVshead;
  private TextView mTVstail;
  private TextView mTVrhead;
  private TextView mTVrtail;

  // List< MemoryOctet> mMemory;
  private ArrayAdapter< String > mArrayAdapter;
  private ListView mList;

  DeviceA3MemoryDialog( Context context, DeviceActivity parent )
  {
    super( context, R.string.DeviceA3MemoryDialog );
    mParent = parent;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.device_a3_memory_dialog, R.string.memoryA3 );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mTVshead = (TextView) findViewById( R.id.tv_stored_head );
    mTVstail = (TextView) findViewById( R.id.tv_stored_tail );
    mTVrhead = (TextView) findViewById( R.id.tv_read_head );
    mTVrtail = (TextView) findViewById( R.id.tv_read_tail );
    mETfrom  = (EditText) findViewById( R.id.et_from );
    mETto    = (EditText) findViewById( R.id.et_to );
    mETdumpfrom  = (EditText) findViewById( R.id.et_dumpfrom );
    mETdumpto    = (EditText) findViewById( R.id.et_dumpto );
    mETdumpfile  = (EditText) findViewById( R.id.et_dumpfile );
    mBtnStore = (Button) findViewById(R.id.button_store);
    mBtnRead  = (Button) findViewById(R.id.button_read );
    mBtnDump  = (Button) findViewById(R.id.button_dump );
    mBtnReset = (Button) findViewById(R.id.button_reset);
    // mBtnBack  = (Button) findViewById(R.id.button_back);
    mBtnStore.setOnClickListener( this );
    mBtnReset.setOnClickListener( this );
    mBtnRead.setOnClickListener( this );
    mBtnDump.setOnClickListener( this );
    // mBtnBack.setOnClickListener( this );
    
    mArrayAdapter = new ArrayAdapter<>( mParent, R.layout.message );
    mList = (ListView) findViewById(R.id.list_memory);
    mList.setAdapter( mArrayAdapter );
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    int[] ht = new int[2];
    mParent.retrieveDeviceHeadTail( ht );
    setText( mTVshead, mTVstail, ht );
  }

  public void updateList( ArrayList<MemoryOctet> memory )
  {
    mArrayAdapter.clear();
    for ( MemoryOctet m : memory ) mArrayAdapter.add( m.toString() );
    mList.invalidate();
  }

  private void setText( TextView h, TextView t, int[] ht )
  {
    h.setText( String.format(Locale.US, "%04d", ht[0] / 8 ) );
    t.setText( String.format(Locale.US, "%04d", ht[1] / 8 ) );
  }

  // int parseInt( EditText et, int scale )
  // {
  //   if ( et.getText() == null ) return 0;
  //   String str = et.getText().toString();
  //   if ( str == null || str.length() == 0 ) return 0;
  //   int ret = 0;
  //   try {
  //     ret = Integer.parseInt( str ) * scale;
  //   } catch ( NumberFormatException e ) {
  //     et.setError( mParent.getResources().getString( R.string.error_invalid_number ) );
  //     ret = -1;
  //   }
  //   return ret;
  // }

  // user inputs index, then converted to address multiplying by 8 
  @Override
  public void onClick( View view )
  {
    int[] ht = new int[2];
    String from, to, error;
    switch ( view.getId() ) {
      case R.id.button_store:
        try {
          ht[0] = Integer.parseInt( mTVrhead.getText().toString() ) * 8;
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mTVrhead.setError( error );
          return;
        }
        try {
          ht[1] = Integer.parseInt( mTVrtail.getText().toString() ) * 8;
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mTVrtail.setError( error );
          return;
        }
        mParent.storeDeviceHeadTail( ht );
        mTVshead.setText( mTVrhead.getText() );
        mTVstail.setText( mTVrtail.getText() );
        break;
      case R.id.button_dump:
        from = mETdumpfrom.getText().toString();
        to   = mETdumpto.getText().toString();
        if ( /* from == null || */ from.length() == 0 ) {
          error = mParent.getResources().getString( R.string.error_begin_required );
          mETdumpfrom.setError( error );
          return;
        }
        if ( /* to == null || */ to.length() == 0 ) {
          error = mParent.getResources().getString( R.string.error_end_required );
          mETdumpto.setError( error );
          return;
        }
        try {
          ht[0] = Integer.parseInt( from ) * 8;
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mETdumpfrom.setError( error );
          return;
        }
        try {
          ht[1] = Integer.parseInt( to ) * 8;
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mETdumpto.setError( error );
          return;
        }
        String dumpfile = null;
        if ( mETdumpfile.getText() != null ) dumpfile = mETdumpfile.getText().toString();
        mParent.readA3Memory( this, ht, dumpfile );
        break;
      case R.id.button_read:
        mParent.readDeviceHeadTail( DeviceA3Details.HeadTail, ht );
        setText( mTVrhead, mTVrtail, ht );
        mETfrom.setText( mTVstail.getText() );
        mETto.setText( mTVrtail.getText() );
        break;
      case R.id.button_reset:
        from = mETfrom.getText().toString().trim();
        to   = mETto.getText().toString().trim();
        if ( /* from == null || */ from.length() == 0 ) {
          error = mParent.getResources().getString( R.string.error_begin_required );
          mETfrom.setError( error );
          return;
        }
        if ( /* to == null || */ to.length() == 0 ) {
          error = mParent.getResources().getString( R.string.error_end_required );
          mETto.setError( error );
          return;
        }
        try {
          ht[0] = Integer.parseInt( from ) * 8;
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mETfrom.setError( error );
          return;
        }
        try {
          ht[1] = Integer.parseInt( to ) * 8;
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mETto.setError( error );
          return;
        }
        askReset( ht );
        // mParent.resetA3DeviceHeadTail( ht );
        break;
      // case R.id.button_cancel:
      //   dismiss();
      //   break;
    }
  }

  // @param ht[] head-tail addresses
  private void askReset( final int ht[] )
  {
    TopoDroidAlertDialog.makeAlert( mContext, mParent.getResources(), R.string.ask_reset,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          mParent.resetA3DeviceHeadTail( ht );
          // finish(); 
        }
      }
    );
  }


}
