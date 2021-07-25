/* @file FixedActivity.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief TopoDroid survey fix point edit dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDRequest;
// import com.topodroid.utils.TDLocale;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.UserManualActivity;

import android.util.Log;

// import java.util.regex.Pattern;
// import java.util.Locale;

// import android.widget.ArrayAdapter;

import android.os.Bundle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;

// import android.widget.TextView;
// import android.widget.EditText;
import android.widget.Button;
import android.view.View;

import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
// import android.os.Bundle;

// import android.content.Context;

// import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
// import android.view.ViewGroup.LayoutParams;

// import android.widget.TextView;
// import android.widget.EditText;
// import android.widget.Button;
// import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.TextView.OnEditorActionListener;

// import android.view.View;
import android.widget.ListView;
// import android.inputmethodservice.KeyboardView;

// import android.location.Location;
// import android.location.LocationListener;
import android.location.LocationManager;

public class FixedActivity extends Activity
                           implements View.OnClickListener
                           , OnItemClickListener
{
  private Context mContext;
  // private TopoDroidApp mApp; // unused
  private ListView mList;
  private FixedAdapter mFixedAdapter;

  private FixedInfo mSaveFixed;
  private int mSavePos;

  private Button[] mButton1;
  private int mNrButton1 = 0;
  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;


  private boolean hasGps = false;

  // ListView   mMenu;
  // Button     mImage;
  // // ArrayAdapter< String > mMenuAdapter;
  // MyMenuAdapter mMenuAdapter;
  // boolean onMenu;

  private static final int[] izons = {
                        R.drawable.iz_gps,
                        R.drawable.iz_plus,
                        R.drawable.iz_import
                     };
  // private static final int menus[] = {
  //                    };

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    TDandroid.setScreenOrientation( this );

    // mApp = (TopoDroidApp)getApplication();
    mContext = this;

    hasGps = TDandroid.checkLocation( mContext );
    // Bundle extras = getIntent().getExtras();
    // if ( extras != null ) {
    // }

    setContentView(R.layout.fixed_activity);
    setTitle( R.string.title_fixed );

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    // mListView.setEmptyPlacholder(true);
    /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    // MOBILE TOPOGRAPHER
    mNrButton1 = 2;
    if ( hasGps ) ++ mNrButton1;
    mButton1 = new Button[ mNrButton1 ];
    int kz = (hasGps)? 0 : 1; // index of izons
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( this, this, izons[kz++] );
    }
    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    // NO MENU
    // mImage = (Button) findViewById( R.id.handle );
    // mImage.setOnClickListener( this );
    // mImage.setBackground( MyButton.getButtonBackground( mApp, getResources(), R.drawable.iz_menu ) );
    // mMenu = (ListView) findViewById( R.id.menu );
    // setMenuAdapter();
    // closeMenu();
    // mMenu.setOnItemClickListener( this );

    mList = (ListView) findViewById(R.id.fx_list);
    mList.setOnItemClickListener( this );

    refreshList();
  }

  private void refreshList()
  {
    List< FixedInfo > fxds = TopoDroidApp.mData.selectAllFixed( TDInstance.sid, TDStatus.NORMAL );
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
                             String comment,
                             long source
                           )
  {
    if ( comment == null ) comment = "";
    FixedInfo f = addLocation( name, lng, lat, alt, asl, comment, source );
    // if ( f != null ) { // always true
      mFixedAdapter.add( f );
      mList.invalidate();
    // }
  }

  private FixedInfo addLocation( String station, double lng, double lat, double h_ell, double h_geo,
                                 String comment, long source )
  {
    // Log.v("DistoX-FIXED", "location " + station + ": " + lng + " " + lat );
    long id = TopoDroidApp.mData.insertFixed( TDInstance.sid, -1L, station, lng, lat, h_ell, h_geo, comment, 0L, source );
    return new FixedInfo( id, station, lng, lat, h_ell, h_geo, comment, source ); 
  }


  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;

    int k = 0;
    if ( hasGps && /* k < mNrButton1 && */ b == mButton1[k++] ) { // GPS
      final LocationManager lm = (LocationManager)getSystemService( Context.LOCATION_SERVICE );
      if ( lm != null ) {
        if ( ! lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
          TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString( R.string.ask_gps_service ),
            new DialogInterface.OnClickListener( ) { 
              @Override public void onClick( DialogInterface dialog, int btn ) { 
                startActivity( new Intent( android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS ) );
              }
            }
          );
        } else {
          new FixedGpsDialog( mContext, this ).show();
        }
      } else {
        TDLog.Error("No location manager" );
      }
    } else if ( /* k < mNrButton1 && */ b == mButton1[k++] ) { // ADD
      new FixedAddDialog( mContext, this ).show();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) { // IMPORT MOBILE TOPOGRAPHER
      // get the file with MediaStore
      selectImportFromProvider();

      // FixedImportDialog dialog = new FixedImportDialog( mContext, this );
      // if ( dialog.getNrPoints() > 0 ) {
      //   dialog.show();
      // } else {
      //   TDToast.makeBad( R.string.MT_points_none );
      // }
    }
    // refreshList();
  }

  private void selectImportFromProvider( ) // GPS IMPORT
  {
    Intent intent = new Intent( Intent.ACTION_OPEN_DOCUMENT );
    intent.setType( "application/octet-stream" );
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    // intent.putExtra( "importtype", index ); // extra is not returned to the app
    startActivityForResult( Intent.createChooser(intent, getResources().getString( R.string.title_import_gps ) ), TDRequest.REQUEST_GET_GPS_IMPORT );
  }


  public boolean hasLocation( String station )
  {
    return TopoDroidApp.mData.hasFixed( TDInstance.sid, station );
  }

  void updateFixedData( FixedInfo fxd, double lng, double lat, double alt, double asl )
  {
    TopoDroidApp.mData.updateFixedData( fxd.id, TDInstance.sid, lng, lat, alt, asl );
    // mList.invalidate();
    refreshList();
  }
 
  void updateFixedNameComment( FixedInfo fxd, String name, String comment )
  {
    TopoDroidApp.mData.updateFixedStationComment( fxd.id, TDInstance.sid, name, comment );
    // mList.invalidate();
    refreshList();
  }

  public void dropFixed( FixedInfo fxd )
  {
    TopoDroidApp.mData.updateFixedStatus( fxd.id, TDInstance.sid, TDStatus.DELETED );
    refreshList();
  }

  void setDeclination( float decl )
  {
    // Log.v( "DistoX", "set declination " + decl );
    TopoDroidApp.mData.updateSurveyDeclination( TDInstance.sid, decl );
  }

  // private final static int LOCATION_REQUEST = 1;
  private static final int CRS_CONVERSION_REQUEST = 2; // not final ?
  private static final int CRS_INPUT_REQUEST = 3;      // not final ?
  private FixedDialog mFixedDialog = null;
  private FixedAddDialog mFixedAddDialog = null;

  void clearConvertedCoords( FixedInfo fxd ) 
  {
    TopoDroidApp.mData.updateFixedCS( fxd.id, TDInstance.sid, null, 0, 0, 0, 2L );
    fxd.clearConverted();
  }

  void tryProj4( FixedDialog dialog, String cs_to, FixedInfo fxd )
  {
    if ( cs_to == null ) return;
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      // Intent intent = new Intent( Intent.ACTION_DEFAULT, "com.topodroid.Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "request", "CRS_CONVERSION_REQUEST" ); // Proj4 request
      intent.putExtra( "cs_from", "Long-Lat" ); // NOTE MUST USE SAME NAME AS Proj4
      intent.putExtra( "cs_to", cs_to ); 
      intent.putExtra( "longitude", fxd.lng );
      intent.putExtra( "latitude",  fxd.lat );
      // intent.putExtra( "altitude",  fxd.alt );
      intent.putExtra( "altitude",  fxd.asl ); // geoid altitude

      mFixedDialog = dialog;
      TDLog.Log( TDLog.LOG_LOC, "CONV. REQUEST " + fxd.lng + " " + fxd.lat + " " + fxd.alt );
      startActivityForResult( intent, CRS_CONVERSION_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      mFixedDialog = null;
      TDToast.makeBad( R.string.no_proj4 );
    }
  }

  void getProj4Coords( FixedAddDialog dialog )
  {
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      // Intent intent = new Intent( Intent.ACTION_DEFAULT, "com.topodroid.Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "request", "CRS_INPUT_REQUEST" ); // Proj4 request
      mFixedAddDialog = dialog;
      TDLog.Log( TDLog.LOG_LOC, "COORD. INPUT REQUEST " );
      startActivityForResult( intent, CRS_INPUT_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      mFixedAddDialog = null;
      TDToast.makeBad( R.string.no_proj4 );
    }
  }

  public void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    // TDLocale.resetLocale(); // OK-LOCALE
    if ( resCode == RESULT_OK ) {
      if ( reqCode == CRS_CONVERSION_REQUEST ) {
        if ( mFixedDialog != null ) {
          Bundle bundle = intent.getExtras();
          if ( bundle != null ) {
            String cs  = bundle.getString( "cs_to" );
            double lng = bundle.getDouble( "longitude");
            double lat = bundle.getDouble( "latitude");
            double alt = bundle.getDouble( "altitude"); // geoid altitude
	    long   n_dec = bundle.containsKey( "decimals" )? bundle.getLong( "decimals" ) : 2;
            TopoDroidApp.mData.updateFixedCS(  mFixedDialog.getFixedId(), TDInstance.sid, cs, lng, lat, alt, n_dec );
            mFixedDialog.setConvertedCoords( cs, lng, lat, alt, n_dec );
          }
          mFixedDialog = null;
        }
      } else if ( reqCode == CRS_INPUT_REQUEST ) {
        if ( mFixedAddDialog != null ) {
          Bundle bundle = intent.getExtras();
          if ( bundle != null ) {
            mFixedAddDialog.setCoordsGeo(
              bundle.getDouble( "longitude"),
              bundle.getDouble( "latitude"),
              bundle.getDouble( "altitude") ); // geoid altitude
          }
          mFixedAddDialog = null;
        }
      } else if ( reqCode == TDRequest.REQUEST_GET_GPS_IMPORT ) {
        Uri uri = intent.getData();
        ArrayList< String > gps_points = new ArrayList<>();
        try {
          boolean ok = true;
          InputStreamReader isr = new InputStreamReader( this.getContentResolver().openInputStream( uri ) );
          BufferedReader br = new BufferedReader( isr );
          while ( ok ) {
            String line = br.readLine();
            if ( line == null ) break;
            // Log.v("DistoX", "read " + line );

            // syntax: name, lat, lng, alt, asl
            // units:        dec.degree meters
            String[] vals = line.split(",");
            int len = vals.length;
            if ( len != 5 ) {
              ok = false;
              break;
            }
            gps_points.add( line.trim() ); // add the whole line - needed for item click processing
          }
          isr.close();
          if ( ! ok ) {
            TDToast.makeBad( R.string.MT_bad_file );
          } else if ( gps_points.size() > 0 ) {
            (new FixedImportDialog( mContext, this, gps_points )).show();
          } else {
            TDToast.makeBad( R.string.MT_points_none );
          }
        // } catch ( NumberFormatException e ) {
        } catch ( FileNotFoundException e ) {
          Log.v("DistoX", "File not found " + e.getMessage() );
        } catch ( IOException e ) { 
          Log.v("DistoX", "IO exception " + e.getMessage() );
        }
      }
    }
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.FixedActivity );
        /* if ( help_page != null ) */ UserManualActivity.showHelpPage( this, help_page );
        return true;
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

}

