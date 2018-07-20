/* @file FixedImportDialog.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief TopoDroid MobileTopographer pointlist files dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

// import java.util.ArrayList;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
// import android.view.ViewGroup.LayoutParams;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
// import android.widget.TextView;
import android.widget.EditText;
// import android.widget.Toast;

import android.content.Context;
import android.content.Intent;

import android.inputmethodservice.KeyboardView;

import android.net.Uri;

// import android.util.Log;

class FixedImportDialog extends MyDialog
                               implements OnItemClickListener
                                        , OnClickListener
                                        , OnLongClickListener
{ 
  static final private String POINTLISTS = Environment.getExternalStorageDirectory().getPath() + "/MobileTopographer/pointlists";
  static final private String POINTLISTS_PRO = Environment.getExternalStorageDirectory().getPath() + "/MobileTopographerPro/pointlists";

  private final FixedActivity mParent;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  private Button mBtnOk;
  private Button mBtnView;
  private TextView mTVlat;
  private TextView mTVlng;
  private TextView mTVhell;
  private TextView mTVhgeo;
  private EditText mETstation;
  private EditText mETcomment;

  private double mLat, mLng, mHEll, mHGeo;
  private boolean isSet;

  private MyKeyboard mKeyboard;

  FixedImportDialog( Context context, FixedActivity parent )
  {
    super( context, R.string.FixedImportDialog );
    mParent  = parent;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.fixed_import_dialog, R.string.title_fixed_import );

    mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );
    readPoints();

    mETstation = (EditText) findViewById( R.id.station );
    mETcomment = (EditText) findViewById( R.id.comment );

    mETstation.setOnLongClickListener( this );

    mTVlat  = (TextView) findViewById( R.id.tv_lat );
    mTVlng  = (TextView) findViewById( R.id.tv_lng );
    mTVhell = (TextView) findViewById( R.id.tv_alt );
    mTVhgeo = (TextView) findViewById( R.id.tv_asl );

    mBtnOk   = (Button) findViewById( R.id.btn_ok );
    mBtnView = (Button) findViewById( R.id.btn_view );
    mBtnOk.setOnClickListener( this );
    mBtnView.setOnClickListener( this );

    // mBtnCancel = (Button)findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      if ( TDSetting.mStationNames == 1 ) {
        MyKeyboard.registerEditText( mKeyboard, mETstation, MyKeyboard.FLAG_POINT );
      } else {
        MyKeyboard.registerEditText( mKeyboard, mETstation, MyKeyboard.FLAG_POINT_LCASE_2ND );
      }
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETstation.setInputType( TDConst.NUMBER_DECIMAL );
      }
    }

    isSet = false;
  }

  private boolean readPoints() // UNUSED return
  {
    mArrayAdapter.clear();

    File dir = new File( POINTLISTS );
    if ( ! dir.exists() ) dir = new File( POINTLISTS_PRO );
    if ( ! dir.exists() ) return false;

    File[] files = dir.listFiles();
    if ( files == null || files.length == 0 ) return false;
    // Log.v("DistoX", "number of files " + files.length );

    boolean ret = false;
    for ( File f : files ) {
      // Log.v("DistoX", "file " + f.getName() + " is dir " + f.isDirectory() );
      if ( ! f.isDirectory() ) {
        ret = readPointFile( dir, f.getName() ) || ret; // N.B. read file before oring with ret
      }
    }
    if ( ret ) {
      mList.setAdapter( mArrayAdapter );
    } else {
      TDToast.make( mContext, R.string.MT_points_none );
      dismiss();
    }
    return ret;
  }

  private boolean readPointFile( File dir, String filename )
  {
    // Log.v("DistoX", "reading file " + filename );
    boolean ret = false;
    try {
      // TDLog.Log( TDLog.LOG_IO, "read GPS points file " + filename );
      File file = new File( dir, filename );
      FileReader fr = new FileReader( file );
      BufferedReader br = new BufferedReader( fr );
      for ( ; ; ) {
        String line = br.readLine();
        if ( line == null ) break;
        // Log.v("DistoX", "read " + line );

        String[] vals = line.split(",");
        int len = vals.length;
        if ( len >= 4 ) {
          ret = true;
          // StringBuilder sb = new StringBuilder();
          // sb.append( vals[len-3].trim() );
          // sb.append( " " );
          // sb.append( vals[len-4].trim() );
          // sb.append( " " );
          // sb.append( vals[len-2].trim() );
          // sb.append( " " );
          // sb.append( vals[len-1].trim() );
		  
          // int k=0;
          // String name = vals[k];
          // while ( ++k < len-4 ) {
          //    name = name + "," + vals[k];
          // }
          String sb = vals[ len - 3 ].trim() +
              " " +
              vals[ len - 4 ].trim() +
              " " +
              vals[ len - 2 ].trim() +
              " " +
              vals[ len - 1 ].trim();
          mArrayAdapter.add( sb );
		  // mArrayAdapter.add( sb.toString() );
        }
      }
    } catch ( IOException e ) { 
    } catch ( NumberFormatException e ) {
    }
    return ret;
  }

  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  @Override
  public void onClick( View v ) 
  {
    if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );

    Button b = (Button)v;

    boolean do_toast = false;
    if ( b == mBtnOk ) {
      String station = mETstation.getText().toString();
      if ( /* station == null || */ station.length() == 0 ) {
        mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
        return;
      }
      String comment = mETcomment.getText().toString();
      // if ( comment == null ) comment = "";
      if ( isSet ) {
        mParent.addFixedPoint( station, mLng, mLat, mHEll, mHGeo, comment, FixedInfo.SRC_MOBILE_TOP );
      dismiss();
      } else {
        do_toast = true;
      }
    } else if ( b == mBtnView ) {
      if ( isSet ) {
        Uri uri = Uri.parse( "geo:" + mLat + "," + mLng + "?q=" + mLat + "," + mLng );
        mContext.startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
      } else {
        do_toast = true;
      }
    } else {
      dismiss();
    }
    if ( do_toast ) {
      TDToast.make( mContext, R.string.no_location_data );
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    String item = ((TextView) view).getText().toString();
    // TDLog.Log(  TDLog.LOG_INPUT, "FixedImportDialog onItemClick() " + item.toString() );
    String[] vals = item.split(" ");
    if ( vals.length == 4 ) {
      String lngstr = vals[0].trim();
      String latstr = vals[1].trim();
      String altstr = vals[2].trim();
      String aslstr = vals[3].trim();
      mHGeo = Double.parseDouble( aslstr );
      mHEll = Double.parseDouble( altstr );
      mLng  = Double.parseDouble( lngstr );
      mLat  = Double.parseDouble( latstr );
      mTVlat.setText( latstr );
      mTVlng.setText( lngstr );
      mTVhell.setText( altstr );
      mTVhgeo.setText( aslstr );
      isSet = true;
    }
  }
}

