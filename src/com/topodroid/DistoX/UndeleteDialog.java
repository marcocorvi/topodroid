/* @file UndeleteDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid undelete survey item activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.Locale;

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

public class UndeleteDialog extends MyDialog
                            implements OnItemClickListener
                            , View.OnClickListener
{
  public long mSID;
  DataHelper mData;
  ShotWindow mParent;

  private Button mBtnCancel;
  // ArrayAdapter< String >  mArrayAdapter;
  MyStringAdapter mArrayAdapter;
  ListView mList;
  List< DistoXDBlock > mShots;
  List< PlotInfo >     mPlots;

  public UndeleteDialog( Context context, ShotWindow parent, DataHelper data, long sid,
                         List<DistoXDBlock> shots, List<PlotInfo> plots )
  {
    super( context, R.string.UndeleteDialog );
    mParent = parent;
    mData   = data;
    mSID    = sid;
    mShots  = shots;
    mPlots  = plots;
  }

  @Override
  public void onClick(View v) 
  {
    // TDLog.Log( TDLog.LOG_INPUT, "UndeleteDialog onClick()" );
    dismiss();
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
          mData.undeleteShot( id, mSID, true );
          mParent.updateDisplay();
        } else {
          mData.undeletePlot( id, mSID );
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
    // mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    mArrayAdapter = new MyStringAdapter( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.list_undelete);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );

    for ( DistoXDBlock b : mShots ) {
      mArrayAdapter.add( 
        String.format(Locale.US, "shot %d <%s> %.2f %.1f %.1f", b.mId, b.Name(), b.mLength, b.mBearing, b.mClino ) );
    }
    for ( PlotInfo p : mPlots ) {
      if ( p.type == PlotInfo.PLOT_PLAN ) {
        mArrayAdapter.add( String.format("plot %d <%s> %s", p.id, p.name, p.getTypeString() ) );
      // } else { // this is OK extended do not show up in this dialog
      //   TDLog.Log( TDLog.LOG_PLOT, " plot " + p.id + " <" + p.name + "> " +  p.getTypeString() );
      }
    }

    setTitle( R.string.undelete_text );
  }
}
