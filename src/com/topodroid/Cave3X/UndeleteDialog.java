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
package com.topodroid.Cave3X;

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
import android.widget.Button;
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

  // private Button mBtnCancel;
  private Button mBtnStatus;
  private Button mBtnOk;

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

  UndeleteDialog( Context context, ShotWindow parent, DataHelper data, long sid,
                         List< DBlock > shots1, List< DBlock > shots2, List< DBlock > shots3,
                         List< PlotInfo > plots )
  {
    super( context, R.string.UndeleteDialog );
    mParent = parent;
    mData   = data;
    mSid    = sid;
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
    if ( plots.size() > 0 ) {
      mPlots  = new ArrayList< UndeleteItem >();
      for ( PlotInfo p : plots ) {
        mPlots.add( new UndeleteItem( p.id, String.format(Locale.US, "%d <%s> %s", p.id, p.name, p.getTypeString() ), UndeleteItem.UNDELETE_PLOT ) );
      }
    }
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button)v;
    if ( b == mBtnStatus ) {
      incrementStatus( );
      return;
    } else if ( b == mBtnOk ) {
      recoverData();
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

  private void recoverData()
  {
    boolean update = false;
    switch ( mStatus ) {
      case 0:
        if ( mPlots != null ) {
          for ( UndeleteItem item : mPlots ) if ( item.flag ) {
            mData.undeletePlot( item.id, mSid );
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
    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );

    setTitle( R.string.undelete_text );

    mStatus = 0;
    incrementStatus(); // calls updateList();
  }

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
