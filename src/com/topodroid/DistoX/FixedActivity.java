/* @file FixedActivity.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief TopoDroid survey fix point edit dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.regex.Pattern;
import java.util.Locale;

import android.widget.ArrayAdapter;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
import android.inputmethodservice.KeyboardView;

import android.net.Uri;

// import android.widget.Toast;

import android.util.Log;


import java.util.Iterator;

import java.io.File;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import android.content.Context;

import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
// import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.inputmethodservice.KeyboardView;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.location.GpsSatellite;
// import android.location.GpsStatus.Listener;


import android.util.Log;


public class FixedActivity extends Activity
                           implements View.OnClickListener
                                    , OnItemClickListener
{
  private Context mContext;
  TopoDroidApp mApp;
  private ListView mList;
  private FixedAdapter mFixedAdapter;

  private FixedInfo mSaveFixed;
  private int mSavePos;

  private Button[] mButton1;
  private int mNrButton1 = 0;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;

  // ListView   mMenu;
  // Button     mImage;
  // // ArrayAdapter< String > mMenuAdapter;
  // MyMenuAdapter mMenuAdapter;
  // boolean onMenu;

  private static int izons[] = { 
                        R.drawable.iz_gps,
                        R.drawable.iz_plus,
                        R.drawable.iz_import
                     };
  // private static int menus[] = {
  //                    };

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    mApp = (TopoDroidApp)getApplication();
    mContext = this;

    // Bundle extras = getIntent().getExtras();
    // if ( extras != null ) {
    // }

    setContentView(R.layout.fixed_activity);
    setTitle( R.string.title_fixed );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );
    mNrButton1 = 3;
    mButton1 = new Button[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( this, this, izons[k] );
    }
    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    // NO MENU
    // mImage = (Button) findViewById( R.id.handle );
    // mImage.setOnClickListener( this );
    // mImage.setBackgroundDrawable( MyButton.getButtonBackground( getResources(), R.drawable.iz_menu ) );
    // mMenu = (ListView) findViewById( R.id.menu );
    // setMenuAdapter();
    // closeMenu();
    // mMenu.setOnItemClickListener( this );

    mList = (ListView) findViewById(R.id.fx_list);
    mList.setOnItemClickListener( this );

    refreshList();
  }

  public void refreshList()
  {
    List< FixedInfo > fxds = mApp.mData.selectAllFixed( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
    mFixedAdapter = new FixedAdapter( mContext, R.layout.message, fxds );
    mList.setAdapter( mFixedAdapter );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TDLog.Log( TDLog.LOG_LOC, "Location::onItemClick pos " + pos );
    // CharSequence item = ((TextView) view).getText();
    // String value = item.toString();
    // // setListPos( position  );
    // mSaveTextView = (TextView) view;
    mSaveFixed = mFixedAdapter.get(pos);
    mSavePos   = pos;
    (new FixedDialog( mContext, this, mSaveFixed )).show();
  }

  public void addFixedPoint( String name,
                             double lng, // decimal degrees
                             double lat,
                             double alt,  // meters
                             double asl,
                             String comment
                           )
  {
    if ( comment == null ) comment = "";
    FixedInfo f = addLocation( name, lng, lat, alt, asl, comment );
    if ( f != null ) {
      mFixedAdapter.add( f );
      mList.invalidate();
    }
  }

  private FixedInfo addLocation( String station, double lng, double lat, double h_ell, double h_geo, String comment )
  {
    long id = mApp.mData.insertFixed( mApp.mSID, -1L, station, lng, lat, h_ell, h_geo, comment, 0L );
    return new FixedInfo( id, station, lng, lat, h_ell, h_geo, comment ); 
  }


  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;

    int k = 0;
    if ( b == mButton1[k++] ) { // GPS
      new FixedGpsDialog( mContext, this ).show();
    } else if ( b == mButton1[k++] ) { // ADD
      new FixedAddDialog( mContext, this ).show();
    } else if ( b == mButton1[k++] ) { // IMPORT MOBILE TOPOGRAPHER
      new FixedImportDialog( mContext, this ).show();
    }
    // refreshList();
  }


  public boolean hasLocation( String station )
  {
    return mApp.mData.hasFixed( mApp.mSID, station );
  }
 
  void updateFixedNameComment( FixedInfo fxd, String name, String comment )
  {
    mApp.mData.updateFixedStationComment( fxd.id, mApp.mSID, name, comment );
    // mList.invalidate();
    refreshList();
  }

  public void dropFixed( FixedInfo fxd )
  {
    mApp.mData.updateFixedStatus( fxd.id, mApp.mSID, TopoDroidApp.STATUS_DELETED );
    refreshList();
  }

  void setDeclination( float decl )
  {
    mApp.mData.updateSurveyDeclination( mApp.mSID, decl, true );
  }

  // private final static int LOCATION_REQUEST = 1;
  private static int CRS_CONVERSION_REQUEST = 2; // not final ?
  private FixedDialog mFixedDialog = null;

  void tryProj4( FixedDialog dialog, String cs_to, FixedInfo fxd )
  {
    if ( cs_to == null ) return;
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      // Intent intent = new Intent( Intent.ACTION_DEFAULT, "com.topodroid.Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "cs_from", "Long-Lat" ); // NOTE MUST USE SAME NAME AS Proj4
      intent.putExtra( "cs_to", cs_to ); 
      intent.putExtra( "longitude", fxd.lng );
      intent.putExtra( "latitude",  fxd.lat );
      intent.putExtra( "altitude",  fxd.alt );

      mFixedDialog = dialog;
      TDLog.Log( TDLog.LOG_LOC, "CONV. REQUEST " + fxd.lng + " " + fxd.lat + " " + fxd.alt );
      startActivityForResult( intent, CRS_CONVERSION_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      mFixedDialog = null;
      Toast.makeText( mContext, R.string.no_proj4, Toast.LENGTH_SHORT).show();
    }
  }

  public void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    // mApp.resetLocale();
    if ( resCode == RESULT_OK ) {
      if ( reqCode == CRS_CONVERSION_REQUEST ) {
        if ( mFixedDialog != null ) {
          Bundle bundle = intent.getExtras();
          String cs = bundle.getString( "cs_to" );
          // String title = String.format(Locale.US, "%.2f %.2f %.2f",
          //    bundle.getDouble( "longitude"),
          //    bundle.getDouble( "latitude"),
          //    bundle.getDouble( "altitude") );
          // TDLog.Log( TDLog.LOG_LOC, "CONV. RESULT " + title );
          // mFixedDialog.setTitle( title );
          // mFixedDialog.setCSto( cs );
          double lng = bundle.getDouble( "longitude");
          double lat = bundle.getDouble( "latitude");
          double alt = bundle.getDouble( "altitude");
          mApp.mData.updateFixedCS(  mFixedDialog.getFixedId(), mApp.mSID, cs, lng, lat, alt );
          mFixedDialog.setConvertedCoords( cs, lng, lat, alt );
          mFixedDialog = null;
        }
      }
    }
  }

  // @Override
  // public boolean onSearchRequested()
  // {
  //   // TDLog.Error( "search requested" );
  //   Intent intent = new Intent( this, TopoDroidPreferences.class );
  //   intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
  //   startActivity( intent );
  //   return true;
  // }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.FixedActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      // case KeyEvent.KEYCODE_SEARCH:
        // return onSearchRequested();
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

}

