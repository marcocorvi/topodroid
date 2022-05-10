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
package com.topodroid.packetX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.TDX.R;

import java.util.List;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
// import android.content.Intent;

import android.widget.ListView;
// import android.widget.TextView;
// import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;

import android.view.View;
import android.view.View.OnClickListener;


public class PacketDialog extends MyDialog
                          implements OnClickListener
{
  // private MainWindow mParent;
  private PacketLogger mLogger;

  // private Button   mButtonRefresh;
  private Button   mButtonDayClear;
  private Button   mButtonWeekClear;
  private Button   mButtonBack;

  private CheckBox mCB0;
  private CheckBox mCBD;
  private CheckBox mCBV;
  private CheckBox mCBG;
  private CheckBox mCBM;
  private CheckBox mCBC;
  private CheckBox mCBX;

  private ListView mList;

  public PacketDialog( Context context )
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

    // mButtonRefresh   = (Button) findViewById( R.id.refresh );
    mButtonDayClear  = (Button) findViewById( R.id.clear_day );
    mButtonWeekClear = (Button) findViewById( R.id.clear_week );
    mButtonBack      = (Button) findViewById( R.id.cancel );

    // mButtonRefresh.setOnClickListener( this );
    mButtonDayClear.setOnClickListener( this );
    mButtonWeekClear.setOnClickListener( this );
    mButtonBack.setOnClickListener( this );

    mCB0 = (CheckBox) findViewById( R.id.type_zero );
    mCBD = (CheckBox) findViewById( R.id.type_d );
    mCBV = (CheckBox) findViewById( R.id.type_v );
    mCBG = (CheckBox) findViewById( R.id.type_g );
    mCBM = (CheckBox) findViewById( R.id.type_m );
    mCBC = (CheckBox) findViewById( R.id.type_c );
    mCBX = (CheckBox) findViewById( R.id.type_x );

    mCB0.setOnClickListener( this );
    mCBD.setOnClickListener( this );
    mCBV.setOnClickListener( this );
    mCBG.setOnClickListener( this );
    mCBM.setOnClickListener( this );
    mCBC.setOnClickListener( this );
    mCBX.setOnClickListener( this );

    mList = (ListView) findViewById( R.id.packet_list );
    mList.setDividerHeight( 2 );

    updateDisplay();
  }

  private void updateDisplay()
  {
    int filter = 0;
    if ( mCB0.isChecked() ) filter += 1;  // order important: as in PacketData filter
    if ( mCBD.isChecked() ) filter += 2;
    if ( mCBG.isChecked() ) filter += 4;
    if ( mCBC.isChecked() ) filter += 8;
    if ( mCBV.isChecked() ) filter += 16;
    if ( mCBC.isChecked() ) filter += 32;
    if ( mCBX.isChecked() ) filter += 64;
    long age = 28L * 24L * 3600L;  // four weeks
    List< PacketData > packets = mLogger.selectPackets( age, filter );
    PacketAdapter adapter = new PacketAdapter( mContext, R.layout.row, packets );
    // MyStringAdapter adapter = new MyStringAdapter( mContext, R.layout.message_small );
    // for ( PacketData pt : packets ) {
    //   adapter.add( pt.toString() );
    // }
    mList.setAdapter( adapter );
  }


  @Override
  public void onClick(View v) 
  {
    long id = v.getId();

    if ( id == R.id.cancel ) {
      dismiss();
      return;
    } else if ( id == R.id.clear_day ) {
      mLogger.clearDayOlder();
    } else if ( id == R.id.clear_week ) {
      mLogger.clearWeekOlder();
    // } else { // every checkbox
      /* nothing */
    } 
    updateDisplay();
  }

  @Override
  public void onBackPressed()
  {
    dismiss();
  }

}
