/* @file PacketDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey fix point edit dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
// import android.content.Intent;

import android.widget.ListView;
// import android.widget.TextView;
// import android.widget.EditText;
import android.widget.Button;
// import android.widget.CheckBox;

import android.view.View;
import android.view.View.OnClickListener;

// import android.widget.Toast;

// import android.util.Log;


class PacketDialog extends MyDialog
                   implements OnClickListener
{
  // private MainWindow mParent;
  private PacketLogger mLogger;

  private Button   mButtonDayClear;
  private Button   mButtonWeekClear;
  private Button   mButtonBack;

  private ListView mList;

  PacketDialog( Context context )
  {
    super( context, R.string.PacketDialog );
    // mParent      = parent;
    mLogger = new PacketLogger( mContext );
  }
  
  // -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_FIXED, "Packet Dialog onCreate" );
    initLayout( R.layout.packet_dialog, R.string.title_packet_dialog );

    mButtonDayClear  = (Button) findViewById( R.id.clear_day );
    mButtonWeekClear = (Button) findViewById( R.id.clear_week );
    mButtonBack      = (Button) findViewById( R.id.cancel );

    mButtonDayClear.setOnClickListener( this );
    mButtonWeekClear.setOnClickListener( this );
    mButtonBack.setOnClickListener( this );

    mList = (ListView) findViewById( R.id.packet_list );
    mList.setDividerHeight( 2 );

    updateDisplay();
  }

  private void updateDisplay()
  {
    Long age = 28L * 24L * 3600L;  // four weeks
    List< PacketData > packets = mLogger.selectPackets( age );
    MyStringAdapter adapter = new MyStringAdapter( mContext, R.layout.message_small );
    for ( PacketData pt : packets ) {
      adapter.add( pt.toString() );
    }
    mList.setAdapter( adapter );
  }


  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;

    if ( b == mButtonDayClear ) {
      mLogger.clearDayOlder();
      updateDisplay();
      return;
    } else if ( b == mButtonWeekClear ) {
      mLogger.clearWeekOlder();
      updateDisplay();
      return;
    } 
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    dismiss();
  }

}
