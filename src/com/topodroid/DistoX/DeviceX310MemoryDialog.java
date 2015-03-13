/** @file DeviceX310MemoryDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device memory dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 201312   created to distinguish from A3 memory dialog
 * 20140416 setError for required EditText inputs
 * 20140719 save dump to file
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


class DeviceX310MemoryDialog extends Dialog
                             implements View.OnClickListener
{
  private Button mBtnDump;
  // private Button mBtnBack;

  private EditText mETdumpfrom;
  private EditText mETdumpto;
  private EditText mETdumpfile;

  DeviceActivity mParent;

  DeviceX310MemoryDialog( Context context, DeviceActivity parent )
  {
    super( context );
    mParent = parent;
  }

  int index2addr( int index )
  {
    int addr = 0;
    while ( index >= 56 ) {
      index -= 56;
      addr += 0x400;
    }
    addr += 18 * index;
    return addr;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_x310_memory_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mETdumpfrom  = (EditText) findViewById( R.id.et_dumpfrom );
    mETdumpto    = (EditText) findViewById( R.id.et_dumpto );
    mETdumpfile  = (EditText) findViewById( R.id.et_dumpfile );

    mBtnDump = (Button) findViewById(R.id.button_dump );
    mBtnDump.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_cancel);
    // mBtnBack.setOnClickListener( this );
    
    setTitle( mParent.getResources().getString( R.string.memoryX310 ) );

    // int[] ht = new int[2];
    // mParent.retrieveDeviceHeadTail( ht );
    // setText( mTVshead, mTVstail, ht );
  }

  static final int MAX_ADDRESS_X310 = 1064;

  @Override
  public void onClick( View view )
  {
    int[] ht = new int[2];
    String from, to, error;
    switch ( view.getId() ) {
      case R.id.button_dump:
        from = mETdumpfrom.getText().toString();
        to   = mETdumpto.getText().toString();
        if ( from == null || from.length() == 0 ) {
          error = mParent.getResources().getString( R.string.error_begin_required );
          mETdumpfrom.setError( error );
          return;
        }
        if ( to == null || to.length() == 0 ) {
          error = mParent.getResources().getString( R.string.error_end_required );
          mETdumpto.setError( error );
          return;
        }
        try {
          ht[0] = Integer.parseInt( from );
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mETdumpfrom.setError( error );
          return;
        }
        try {
          ht[1] = Integer.parseInt( to );
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mETdumpto.setError( error );
          return;
        }
        if ( ht[0] < 0 ) ht[0] = 0;
        if ( ht[1] > MAX_ADDRESS_X310 )  ht[1] = MAX_ADDRESS_X310;
        if ( ht[0] < ht[1] ) {
          String file = null;
          if ( mETdumpfile.getText() != null ) file = mETdumpfile.getText().toString();
          mParent.readX310Memory( ht, file );
        }
        break;
      // case R.id.button_cancel:
      //   dismiss();
      //   break;
    }
  }

  // void askReset( final int ht[] )
  // {
  //   new TopoDroidAlertDialog( mParent, mParent.getResources(),
  //                     mParent.getResources().getString( R.string.ask_reset ),
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         mParent.resetX310DeviceHeadTail( ht );
  //         // finish(); 
  //       }
  //     }
  //   );
  // }

}
