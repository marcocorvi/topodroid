/* @file ShotPasteDialog.java
 *
 * @author marco corvi
 * @date may 2026
 *
 * @brief TopoDroid shot buffer paste dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDUtil;
import com.topodroid.util.TDAnalytics;
// import com.topodroid.util.TDStatus;
import com.topodroid.ui.MyDialog;
import com.topodroid.types.LegType;
import com.topodroid.prefs.TDSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

import android.view.View;
// import android.view.View.OnClickListener;

// FIXME TODO: make a separate ShotPasteDialog

class ShotPasteDialog extends MyDialog
                     implements View.OnClickListener
                     // , OnItemClickListener
{
  private final static int MAX_STATUS = 5;

  private long mSid;
  private final DataHelper mData;
  private final ShotWindow mParent;
  private DBlockBuffer mDBlockBuffer = null;
  private CheckBox     mCBbufferSortByID;

  // private Button mBtnCancel;
  private Button mBtnStatus;

  private boolean mCanRecover = false;

  // FLAGS
  // private final int RECOVER_SHOTS_DELETED   = 1;
  // private final int RECOVER_SHOTS_OVERSHOOT = 2;
  // private final int RECOVER_SHOTS_CHECK     = 4;
  // private final int RECOVER_SHOTS_BLUNDER   = 8;
  // private final int RECOVER_PLOTS_DELETED   =16;


  /** cstr
   * @param context   context
   * @param parent    parent window
   * @param data      data database
   * @param sid       survey ID
   * @param shots1    deleted shots
   * @param shots2    overshoot-ed shots
   * @param shots3    check shots
   * @param shots4    blunder shots
   * @param plots     deleted plots (plan-profile pairs)
   * @param buffer    data-block buffer (copy/cut and paste)
   * @note the choice of two separate lists of plots: plans and profiles is brittle
   */
  ShotPasteDialog( Context context, ShotWindow parent, DataHelper data, long sid, DBlockBuffer buffer )
  {
    super( context, null, R.string.ShotPasteDialog ); // null app
    mParent = parent;
    mData   = data;
    mSid    = sid;
    mDBlockBuffer = buffer;
    assert( buffer != null && buffer.size() > 0 );
  }

  /** implements user taps
   * @param v   tapped view
   */
  @Override
  public void onClick(View v) 
  {
    // TDLog.v( "ShotPasteDialog onClick() " + v.getId() );
    TDLog.v("BUFFER append: size " + mDBlockBuffer.size() );
    if ( v.getId() == R.id.button_buffer_copy ) {
      appendBuffer( );
    } else if ( v.getId() == R.id.button_buffer_move ) {
      appendBuffer( );
      mDBlockBuffer.clear();
    } else if ( v.getId() == R.id.button_buffer_empty ) {
      mDBlockBuffer.clear();
    }
    dismiss();
  }

  /** move the data from the buffer to the survey - the buffer is not cleared 
   */
  private void appendBuffer( )
  {
    TopoDroidApp.updateAnalytic( TDAnalytics.MULTIPASTE );
    // TDLog.v("Append buffer " + mDBlockBuffer.size() );
    if ( mDBlockBuffer == null || mDBlockBuffer.size() == 0 ) return;
    if ( mCBbufferSortByID.isChecked() ) mDBlockBuffer.sort();
    long bid = -1L;
    for ( DBlock blk : mDBlockBuffer.getBuffer() ) {
      if ( bid == -1L ) {
        bid = mData.insertDBlockShot( mSid, blk );
      } else {
        mData.insertDBlockShot( mSid, blk );
      }
    }
    if ( bid >= 0 ) mParent.renumberShotsFrom( bid ); // 20251205
    mParent.updateDisplay(); // this recomputes DistoX accuracy
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    initLayout(R.layout.shot_paste_dialog, R.string.shot_paste_text );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );

    mCBbufferSortByID = (CheckBox) findViewById( R.id.buffer_sorted );
    int buffer_size = mDBlockBuffer.size();
    // TDLog.v("non-null buffer: size " + buffer_size );
    TextView text_buffer = (TextView)findViewById( R.id.text_buffer );
    text_buffer.setText( String.format( resString( R.string.buffer_size ), buffer_size ) );
    ((Button) findViewById( R.id.button_buffer_copy )).setOnClickListener( this );
    ((Button) findViewById( R.id.button_buffer_move )).setOnClickListener( this );
    ((Button) findViewById( R.id.button_buffer_empty )).setOnClickListener( this );
  }

}
