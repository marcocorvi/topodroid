/** @file MobileTopographerDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid MobileTopographer pointlist files dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.ArrayList;

// import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.content.IntentFilter;
import android.content.Context;


public class MobileTopographerDialog extends Dialog
                          implements OnItemClickListener
                          , OnClickListener
{ 
  static final String POINTLISTS = "/sdcard/MobileTopographer/pointlists";

  private Context mContext;
  private DistoXLocation mParent;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  private Button mBtnOk;
  private TextView mTVname;
  private TextView mTVlat;
  private TextView mTVlng;
  private TextView mTValt;

  private double lat, lng, alt, asl;
  private boolean isSet;

  public MobileTopographerDialog( Context context, DistoXLocation parent )
  {
    super( context );
    mContext = context;
    mParent  = parent;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.mobile_topographer_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mTVname = (TextView) findViewById( R.id.tv_name );
    mTVlat = (TextView) findViewById( R.id.tv_lat );
    mTVlng = (TextView) findViewById( R.id.tv_lng );
    mTValt = (TextView) findViewById( R.id.tv_alt );

    mBtnOk = (Button) findViewById( R.id.btn_ok );
    mBtnOk.setOnClickListener( this );

    // mBtnCancel = (Button)findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    // setTitleColor( 0x006d6df6 );
    setTitle( "Mobile Topographer" );
    isSet = false;

    boolean ok = false;
    File dir = new File( POINTLISTS );
    File[] files = dir.listFiles();
    ArrayList<String> names = new ArrayList<String>();
    if ( files != null ) {
      for ( File f : files ) {
        if ( ! f.isDirectory() ) {
          mArrayAdapter.add( f.getName() );
        }
      }
      if ( mArrayAdapter.getCount() > 0 ) {
        mList.setAdapter( mArrayAdapter );
        ok = true;
      }
    }
    if ( ! ok ) {
      Toast.makeText( mContext, R.string.MT_points_none, Toast.LENGTH_SHORT ).show();
      dismiss();
    }
  }

  @Override
  public void onClick( View v ) 
  {
    if ( (Button)v == mBtnOk ) {
      if ( isSet ) {
        mParent.setPosition( lng, lat, alt, asl );
        dismiss();
      }
    } else {
      dismiss();
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    String item = ((TextView) view).getText().toString();
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "MobileTopographerDialog onItemClick() " + item.toString() );

    try {
      FileReader fr = new FileReader( POINTLISTS + "/" + item );
      BufferedReader br = new BufferedReader( fr );
      String line = br.readLine();
      if ( line != null ) {
        String[] vals = line.split(",");
        int len = vals.length;
        if ( len >= 5 ) {
          String aslstr = vals[len-1].trim();
          String altstr = vals[len-2].trim();
          String lngstr = vals[len-3].trim();
          String latstr = vals[len-4].trim();
          int k=0;
          String name = vals[k];
          while ( ++k < len-4 ) {
             name = name + "," + vals[k];
          }
          asl = Double.parseDouble( aslstr );
          alt = Double.parseDouble( altstr );
          lng = Double.parseDouble( lngstr );
          lat = Double.parseDouble( latstr );
          mTVlat.setText( latstr );
          mTVlng.setText( lngstr );
          mTValt.setText( altstr );
          mTVname.setText( name );
          isSet = true;
        }
      }
    } catch ( IOException e ) { 
    } catch ( NumberFormatException e ) {
    }
  }
}

