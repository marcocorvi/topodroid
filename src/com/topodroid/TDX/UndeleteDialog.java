/* @file UndeleteDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid undelete survey item activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDStatus;
import com.topodroid.ui.MyDialog;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.View;
// import android.view.View.OnClickListener;

class UndeleteDialog extends MyDialog
                     implements View.OnClickListener
                     // , OnItemClickListener
{

  private long mSid;
  private final DataHelper mData;
  private final ShotWindow mParent;
  private DBlockBuffer mDBlockBuffer = null;
  private CheckBox     mCBbufferUnsort;

  // private Button mBtnCancel;
  private Button mBtnStatus;

  private UndeleteAdapter mArrayAdapter0 = null;
  private UndeleteAdapter mArrayAdapter1 = null;
  private UndeleteAdapter mArrayAdapter2 = null;
  private UndeleteAdapter mArrayAdapter3 = null;

  private ListView mList;
  private ArrayList< UndeleteItem > mPlots  = null;
  private ArrayList< UndeleteItem > mShots1 = null; // deleted
  private ArrayList< UndeleteItem > mShots2 = null; // overshoot
  private ArrayList< UndeleteItem > mShots3 = null; // check

  private int mStatus;

  /** cstr
   * @param context   context
   * @param parent    parent window
   * @param data      data database
   * @param sid       survey ID
   * @param shots1    ... 
   * @param shots2    ... 
   * @param shots3    ... 
   * @param plots     deleted plots (plan-profile pairs)
   * @param buffer    data-block buffer (copy/cut and paste)
   * @note the choice of two separate lists of plots: plans and profiles is brittle
   */
  UndeleteDialog( Context context, ShotWindow parent, DataHelper data, long sid,
                         List< DBlock > shots1, List< DBlock > shots2, List< DBlock > shots3,
                         List< PlotInfo > plots, DBlockBuffer buffer )
  {
    super( context, R.string.UndeleteDialog );
    mParent = parent;
    mData   = data;
    mSid    = sid;
    mDBlockBuffer = buffer;
    if ( shots1.size() > 0 ) {
      mShots1 = new ArrayList< UndeleteItem >();
      for ( DBlock b : shots1 ) {
        mShots1.add( new UndeleteItem( b.mId, String.format(Locale.US, "%d <%s> %.2f %.1f %.1f", b.mId, b.Name(), b.mLength, b.mBearing, b.mClino ), UndeleteItem.UNDELETE_SHOT ) );
      }
    }
    if ( shots2.size() > 0 ) {
      mShots2 = new ArrayList< UndeleteItem >();
      for ( DBlock b : shots2 ) {
        mShots2.add( new UndeleteItem( b.mId, String.format(Locale.US, "%d %.2f %.1f %.1f", b.mId, b.mLength, b.mBearing, b.mClino ), UndeleteItem.UNDELETE_OVERSHOOT ) );
      }
    }
    if ( shots3.size() > 0 ) {
      mShots3 = new ArrayList< UndeleteItem >();
      for ( DBlock b : shots3 ) {
        mShots3.add( new UndeleteItem( b.mId, String.format(Locale.US, "%d %.2f %.1f %.1f", b.mId, b.mLength, b.mBearing, b.mClino ), UndeleteItem.UNDELETE_CALIB_CHECK ) );
      }
    }
    int np = plots.size();
    if ( np > 0 && (np%2) == 0 ) {
      mPlots  = new ArrayList< UndeleteItem >();
      for ( int k=0; k<np; k+=2 ) {
        PlotInfo plan = plots.get(k);
        PlotInfo profile = plots.get(k+1);
        String name = plan.name;
        // N.B. this assumes that the list of plots contains plan-profile pairs - could assert
        int len = name.length() - 1;
        name = name.substring( 0, len );
        if ( ! name.equals( profile.name.substring( 0, len) ) ) {
          TDLog.Error("UNDELETE plan-profile name mismatch: " + plan.name + " " + profile.name ); 
          break;
        }
        mPlots.add( new UndeleteItem( plan.id, profile.id, String.format(Locale.US, "%d-%d <%s>", plan.id, profile.id, name ), UndeleteItem.UNDELETE_PLOT ) );
      }
    }
  }

  /** implements user taps
   * @param v   tapped view
   */
  @Override
  public void onClick(View v) 
  {
    // TDLog.v( "UndeleteDialog onClick() " + v.getId() );
    if ( v.getId() == R.id.button_status ) {
      incrementStatus( );
      return;
    } else if ( v.getId() == R.id.button_ok ) {
      recoverData();
    } else if ( mDBlockBuffer != null ) { 
      if ( v.getId() == R.id.button_buffer_copy ) {
        appendBuffer( );
      } else if ( v.getId() == R.id.button_buffer_move ) {
        appendBuffer( );
        mDBlockBuffer.clear();
      }
    } else {
      // TDLog.Log( TDLog.LOG_INPUT, "UndeleteDialog onClick()" );
    }
    dismiss();
  }

  // @Override
  // public void onItemClick(AdapterView<?> parent, View view, int pos, long index)
  // {
  //   switch ( mStatus ) {
  //     case 0:
  //       mPlots.get( pos ).flipFlag();
  //       break;
  //     case 1:
  //       mShots1.get( pos ).flipFlag();
  //       break;
  //     case 2:
  //       mShots2.get( pos ).flipFlag();
  //       break;
  //     case 3:
  //       mShots3.get( pos ).flipFlag();
  //       break;
  //   }
  //   updateList();
  // }

  /** move the data from the buffer to the survey - the buffer is not cleared 
   */
  private void appendBuffer( )
  {
    // TDLog.v("Append buffer " + mDBlockBuffer.size() );
    if ( mDBlockBuffer == null || mDBlockBuffer.size() == 0 ) return;
    if ( ! mCBbufferUnsort.isChecked() ) mDBlockBuffer.sort();
    for ( DBlock blk : mDBlockBuffer.getBuffer() ) {
      mData.insertDBlockShot( mSid, blk );
    }
    mParent.updateDisplay(); // this recomputes DistoX accuracy
  }

  /** recover a shot or a plot
   */
  private void recoverData()
  {
    boolean update = false;
    switch ( mStatus ) {
      case 0:
        if ( mPlots != null ) {
          for ( UndeleteItem item : mPlots ) if ( item.flag ) {
            mData.undeletePlot( item.id, mSid );
            mData.undeletePlot( item.id2, mSid );
          }
        }
        break;
      case 1:
        if ( mShots1 != null ) {
          for ( UndeleteItem item : mShots1 ) if ( item.flag ) {
            update = true;
            mData.undeleteShot( item.id, mSid );
          }
        }
        break;
      case 2:
        if ( mShots2 != null ) {
          for ( UndeleteItem item : mShots2 ) if ( item.flag ) {
            update = true;
            mData.undeleteShot( item.id, mSid );
          }
        }
        break;
      case 3:
        if ( mShots3 != null ) {
          for ( UndeleteItem item : mShots3 ) if ( item.flag ) {
            update = true;
            mData.undeleteShot( item.id, mSid );
          }
        }
        break;
    }
    if ( update ) {
      mParent.updateDisplay(); // this recomputes DistoX accuracy
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    setContentView(R.layout.undelete_dialog);
    if ( mPlots != null )  mArrayAdapter0 = new UndeleteAdapter( mContext, this, R.layout.undelete_row, mPlots );
    if ( mShots1 != null ) mArrayAdapter1 = new UndeleteAdapter( mContext, this, R.layout.undelete_row, mShots1 );
    if ( mShots2 != null ) mArrayAdapter2 = new UndeleteAdapter( mContext, this, R.layout.undelete_row, mShots2 );
    if ( mShots3 != null ) mArrayAdapter3 = new UndeleteAdapter( mContext, this, R.layout.undelete_row, mShots3 );


    mList = (ListView) findViewById(R.id.list_undelete);
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );

    mBtnStatus = (Button) findViewById( R.id.button_status );
    mBtnStatus.setOnClickListener( this );
    ((Button) findViewById( R.id.button_ok )).setOnClickListener( this );

    mCBbufferUnsort = (CheckBox) findViewById( R.id.buffer_unsort );
    LinearLayout layout_buffer = (LinearLayout)findViewById( R.id.buffer );
    if ( mDBlockBuffer != null ) {
      int buffer_size = mDBlockBuffer.size();
      // TDLog.v("non-null buffer: size " + buffer_size );
      if ( buffer_size > 0 ) {
        TextView text_buffer = (TextView)findViewById( R.id.text_buffer );
        text_buffer.setText( String.format( mContext.getResources().getString( R.string.buffer_size ), buffer_size ) );
        ((Button) findViewById( R.id.button_buffer_copy )).setOnClickListener( this );
        ((Button) findViewById( R.id.button_buffer_move )).setOnClickListener( this );
      } else {
        layout_buffer.setVisibility( View.GONE );
      }
      setTitle( R.string.undelete_paste_text );
    } else {
      // TDLog.v("null buffer" );
      layout_buffer.setVisibility( View.GONE );
      setTitle( R.string.undelete_text );
    }

    mStatus = 0;
    incrementStatus(); // calls updateList();
  }

  /** update the list of undeletable items: switch adapter according to the status
   */
  private void updateList()
  {
    switch ( mStatus ) {
      case 0:
        mBtnStatus.setText( R.string.undelete_plot );
        mList.setAdapter( mArrayAdapter0 );
        break;
      case 1:
        mBtnStatus.setText( R.string.undelete_shot );
        mList.setAdapter( mArrayAdapter1 );
        break;
      case 2:
        mBtnStatus.setText( R.string.undelete_overshoot );
        mList.setAdapter( mArrayAdapter2 );
        break;
      case 3:
        mBtnStatus.setText( R.string.undelete_check );
        mList.setAdapter( mArrayAdapter3 );
        break;
    }
    // mList.invalidate( );
  }

  /** switch status cyclically
   */
  private void incrementStatus( )
  {
    for ( int k=0; k<4; ++k) {
      mStatus = (mStatus + 1)%4;
      if ( mStatus == 0 ) {
        if ( mPlots != null ) break;
      } else if ( mStatus == 1 ) {
        if ( mShots1 != null ) break;
      } else if ( mStatus == 2 ) {
        if ( mShots2 != null ) break;
      } else if ( mStatus == 3 ) {
        if ( mShots3 != null ) break;
      }
    }
    // TDLog.v( "Undelete status " + mStatus );
    updateList();
  }

}
