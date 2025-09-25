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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
// import com.topodroid.utils.TDFile;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

// import java.io.File;
// import java.io.FileReader;
// import java.io.BufferedReader;
// import java.io.IOException;

import java.util.ArrayList;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;
// import android.os.Environment;

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

import android.content.Context;
import android.content.Intent;

// import android.inputmethodservice.KeyboardView;

import android.net.Uri;

class FixedImportDialog extends MyDialog
                        implements OnItemClickListener
                        , OnClickListener
                        , OnLongClickListener
{ 
  // static final private String POINTLISTS = Environment.getExternalStorageDirectory().getPath() + "/MobileTopographer/pointlists";
  // static final private String POINTLISTS_PRO = Environment.getExternalStorageDirectory().getPath() + "/MobileTopographerPro/pointlists";

  private final FixedActivity mParent;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  private Button mBtnOk;
  private Button mBtnView;
  private TextView mTVlat;
  private TextView mTVlng;
  // private TextView mTVh_ell;
  private TextView mTVh_geo;
  private EditText mETstation;
  private EditText mETcomment;

  private double mLat, mLng, mHEll, mHGeo;
  private boolean isSet;

  private MyKeyboard mKeyboard;
  private int mNrPoints;

  /** cstr
   * @param context     context
   * @param parent      parent window
   * @param gps_points  list of GPS points
   */
  FixedImportDialog( Context context, FixedActivity parent, ArrayList<String> gps_points )
  {
    super( context, null, R.string.FixedImportDialog ); // null app
    mParent  = parent;
    mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    mNrPoints = gps_points.size();
    for ( String pt : gps_points ) mArrayAdapter.add( pt );
  }

  /** @return number of GPS points
   */
  int getNrPoints() { return mNrPoints; }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.fixed_import_dialog, R.string.title_fixed_import );

    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );
    mList.setAdapter( mArrayAdapter );

    mETstation = (EditText) findViewById( R.id.station );
    mETcomment = (EditText) findViewById( R.id.comment );

    mETstation.setOnLongClickListener( this );

    mTVlat  = (TextView) findViewById( R.id.tv_lat );
    mTVlng  = (TextView) findViewById( R.id.tv_lng );
    // mTVh_ell = (TextView) findViewById( R.id.tv_h_ell );
    mTVh_geo = (TextView) findViewById( R.id.tv_h_geo );

    mBtnOk   = (Button) findViewById( R.id.btn_ok );
    mBtnView = (Button) findViewById( R.id.btn_view );
    mBtnOk.setOnClickListener( this );
    mBtnView.setOnClickListener( this );

    // mBtnCancel = (Button)findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    // mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );
    mKeyboard = MyKeyboard.getMyKeyboard( mContext, findViewById( R.id.keyboardview ), R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );

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

/*
  // @return number of read points
  private int readPoints() 
  {
    mArrayAdapter.clear();

    String dirname = POINTLISTS;
    File dir = TDFile.getGpsPointFile( dirname );
    if ( ! dir.exists() ) {
      dirname = POINTLISTS_PRO;
      dir = TDFile.getGpsPointFile( dirname );
    }
    if ( ! dir.exists() ) return 0;

    File[] files = dir.listFiles();
    if ( files == null || files.length == 0 ) return 0;
    // TDLog.v( "number of files " + files.length );

    int ret = 0;
    for ( File f : files ) {
      // TDLog.v( "file " + f.getName() + " is dir " + f.isDirectory() );
      if ( ! f.isDirectory() ) {
        ret += readGpsPointFile( dirname, f.getName() ); // N.B. read file before or-ing with ret
      }
    }
    return ret;
  }

  private int readGpsPointFile( String dirname, String filename )
  {
    // TDLog.v( "reading file " + filename );
    int ret = 0;
    try {
      // TDLog.Log( TDLog.LOG_IO, "read GPS points file " + filename );
      // File file = TDFile.getFile( dirname, filename );
      FileReader fr = TDFile.getGpsPointFileReader( dirname, filename );
      BufferedReader br = new BufferedReader( fr );
      for ( ; ; ) {
        String line = br.readLine();
        if ( line == null ) break;
        // TDLog.v( "read " + line );

        String[] vals = line.split(",");
        int len = vals.length;
        if ( len >= 4 ) {
          mArrayAdapter.add( line.trim() ); // add the whole line - needed for item click processing
          ret ++;
        }
      }
      fr.close();
    } catch ( IOException e ) { 
    } catch ( NumberFormatException e ) {
    }
    return ret;
  }
*/

  /** implements user long-taps
   * @param v   tapped view
   * @return true if tap has been handled
   */
  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  /** implements user taps
   * @param v   tapped view
   */
  @Override
  public void onClick( View v ) 
  {
    if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );

    Button b = (Button)v;

    boolean do_toast = false;
    if ( b == mBtnOk ) {
      String station = mETstation.getText().toString();
      if ( station.length() == 0 ) { // if ( station == null || station.length() == 0 )
        mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
        return;
      }
      // station = TDUtil.toStationFromName( station );
      if ( ! TDUtil.isStationName( station ) ) {
        mETstation.setError( mContext.getResources().getString( R.string.bad_station_name ) );
        return;
      }
      // mETstation.setText( station );
      
      if ( mParent.hasFixed( station ) ) {
        mETstation.setError( mContext.getResources().getString( R.string.error_station_fixed ) );
        return;
      }
      String comment = mETcomment.getText().toString();
      // if ( comment == null ) comment = "";
      if ( isSet ) {
        mParent.addFixedPoint( station, mLng, mLat, mHEll, mHGeo, comment, FixedInfo.SRC_MOBILE_TOP, -1, -1 ); // FIXME ACCURACY
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
      TDToast.makeBad( R.string.no_location_data );
    }
  }

  /** implements user item taps
   * @param parent     parent view
   * @param view       tapped view
   * @param position   item position in the list
   * @param id         ...
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    if ( ! ( view instanceof TextView ) ) {
      TDLog.e("fixed import view instance of " + view.toString() );
      return;
    }
    String item = ((TextView) view).getText().toString();
    String[] vals = item.split(",");
    int len = vals.length;
    if ( len >= 4 ) {
      try {
        String lng_str = vals[len-3].trim(); // 20230118 local var "lng_str"
        String lat_str = vals[len-4].trim();
        String h_ell_str = vals[len-2].trim();
        String h_geo_str = vals[len-1].trim();
        mLng  = Double.parseDouble( lng_str );
        mLat  = Double.parseDouble( lat_str );
        mHEll = Double.parseDouble( h_ell_str );
        mHGeo = Double.parseDouble( h_geo_str );
        mTVlat.setText( lat_str );
        mTVlng.setText( lng_str );
        // mTVh_ell.setText( h_ell_str );
        mTVh_geo.setText( h_geo_str );
        isSet = true;
      } catch ( NumberFormatException e ) {
        TDLog.e("Non-number input");
      }
    }
  }
}

