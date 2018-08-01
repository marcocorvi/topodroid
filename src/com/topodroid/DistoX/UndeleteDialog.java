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
package com.topodroid.DistoX;

import java.util.List;
import java.util.Locale;

// import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

// import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.Toast;

import android.view.View;
// import android.view.View.OnClickListener;

// import android.util.Log;

class UndeleteDialog extends MyDialog
                            implements OnItemClickListener
                            , View.OnClickListener
{
  private long mSid;
  private final DataHelper mData;
  private final ShotWindow mParent;

  private Button mBtnCancel;
  private Button mBtnStatus;

  // ArrayAdapter< String >  mArrayAdapter;
  private MyStringAdapter mArrayAdapter;
  private ListView mList;
  private List< DBlock > mShots1; // deleted
  private List< DBlock > mShots2; // overshoot
  private List< DBlock > mShots3; // check
  private List< PlotInfo >     mPlots;

  private int mStatus;

  UndeleteDialog( Context context, ShotWindow parent, DataHelper data, long sid,
                         List<DBlock> shots1, List<DBlock> shots2, List<DBlock> shots3,
                         List<PlotInfo> plots )
  {
    super( context, R.string.UndeleteDialog );
    mParent = parent;
    mData   = data;
    mSid    = sid;
    mShots1 = shots1;
    mShots2 = shots2;
    mShots3 = shots3;
    mPlots  = plots;
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button)v;
    if ( b == mBtnStatus ) {
      incrementStatus( );
    } else {
      // TDLog.Log( TDLog.LOG_INPUT, "UndeleteDialog onClick()" );
      dismiss();
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long index)
  {
    CharSequence item = ((TextView) view).getText();
    // TDLog.Log( TDLog.LOG_INPUT, "UndeleteDialog onItemClick() " + item.toString() );

    String[] value = item.toString().split( " " );
    
    if ( value.length >= 2 ) {
      long id = Long.parseLong( value[1] );
      try {
        if ( value[0].equals( "shot" ) ) {
          mData.undeleteShot( id, mSid, true );
          mParent.updateDisplay();
        } else {
          mData.undeletePlot( id, mSid );
        }
      } catch ( NumberFormatException e ) {
        TDLog.Error( "undelete parse error: item " + item.toString() );
      }
    }
    dismiss();
  }

  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    setContentView(R.layout.undelete_dialog);
    // mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    mArrayAdapter = new MyStringAdapter( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.list_undelete);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );
    mBtnStatus = (Button) findViewById( R.id.button_status );
    mBtnStatus.setOnClickListener( this );

    mStatus = 0;
    incrementStatus();
    setTitle( R.string.undelete_text );
  }

  private void incrementStatus( )
  {
    mArrayAdapter.clear();
    switch ( mStatus ) {
      case 0:
        if ( mShots1 != null && mShots1.size() > 0 ) { mStatus = 1; break; }
      case 1:
        if ( mShots2 != null && mShots2.size() > 0 ) { mStatus = 2; break; }
      case 2:
        if ( mShots3 != null && mShots3.size() > 0 ) { mStatus = 3; break; }
      case 3:
        if ( mPlots  != null && mPlots.size() > 0  ) { mStatus = 0; break; }
      default:
        if ( mShots1 != null && mShots1.size() > 0 ) { mStatus = 1; break; }
        if ( mShots2 != null && mShots2.size() > 0 ) { mStatus = 2; break; }
        if ( mShots3 != null && mShots3.size() > 0 ) { mStatus = 3; break; }
    }
    // Log.v("DistoX", "Undelete status " + mStatus );
    switch ( mStatus ) {
      case 0:
        if ( mPlots != null ) for ( PlotInfo p : mPlots ) {
          if ( p.type == PlotInfo.PLOT_PLAN ) {
            mArrayAdapter.add( String.format(Locale.US, "plot %d <%s> %s", p.id, p.name, p.getTypeString() ) );
          // } else { // this is OK extended do not show up in this dialog
          //   TDLog.Log( TDLog.LOG_PLOT, " plot " + p.id + " <" + p.name + "> " +  p.getTypeString() );
          }
        }
        mBtnStatus.setText( R.string.undelete_plot );
        break;
      case TDStatus.DELETED:
        if ( mShots1 != null ) for ( DBlock b : mShots1 ) {
          mArrayAdapter.add( 
            String.format(Locale.US, "shot %d <%s> %.2f %.1f %.1f", b.mId, b.Name(), b.mLength, b.mBearing, b.mClino ) );
        }
        mBtnStatus.setText( R.string.undelete_shot );
        break;
      case TDStatus.OVERSHOOT:
        if ( mShots2 != null ) for ( DBlock b : mShots2 ) {
          mArrayAdapter.add( 
            String.format(Locale.US, "shot %d %.2f %.1f %.1f", b.mId, b.mLength, b.mBearing, b.mClino ) );
        }
        mBtnStatus.setText( R.string.undelete_overshoot );
        break;
      case TDStatus.CHECK:
        if ( mShots3 != null ) for ( DBlock b : mShots3 ) {
          mArrayAdapter.add( 
            String.format(Locale.US, "shot %d %.2f %.1f %.1f", b.mId, b.mLength, b.mBearing, b.mClino ) );
        }
        mBtnStatus.setText( R.string.undelete_check );
        break;
    }
    mList.setAdapter( mArrayAdapter );
    // mList.invalidate( );
  }

}
