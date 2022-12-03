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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDLocale;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDRequest;
// import com.topodroid.utils.TDLocale;
// import com.topodroid.ui.MyMenuAdapter;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.UserManualActivity;

// import java.util.regex.Pattern;
// import java.util.Locale;

import android.os.Bundle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;

// import android.widget.TextView;
// import android.widget.EditText;
import android.widget.Button;
import android.widget.ArrayAdapter;
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
import android.widget.Button;
import android.widget.ListView;
// import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.TextView.OnEditorActionListener;

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
  // private Button mBtHelp;   // TOOLBAR
  // private Button mBtClose;

  private boolean hasGps = false;

  private Button     mMenuImage;
  private ListView   mMenu;
  // // ArrayAdapter< String > mMenuAdapter;
  // MyMenuAdapter mMenuAdapter;
  boolean onMenu;

  private static final int[] izons = {
                        R.drawable.iz_gps,
                        R.drawable.iz_plus,
                        R.drawable.iz_import
                     };
  // private static final int menus[] = {
  //                    };

  private static final int[] menus = {
                          R.string.menu_close,
                          R.string.menu_help,
                          };

  private static final int[] help_menus = {
                          R.string.help_close_app,
                          R.string.help_help,
                        };


// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    TDandroid.setScreenOrientation( this );

    // mApp = (TopoDroidApp)getApplication();
    mContext = this;

    hasGps = /* TDandroid.ABOVE_API_23 && */ TDandroid.checkLocation( mContext );
    // Bundle extras = getIntent().getExtras();
    // if ( extras != null ) {
    // }

    setContentView(R.layout.fixed_activity);
    setTitle( R.string.title_fixed );

    // mBtHelp  = (Button) findViewById( R.id.button_help ); // TOOLBAR
    // mBtHelp.setOnClickListener( this );
    // mBtClose = (Button) findViewById( R.id.button_close );
    // mBtClose.setOnClickListener( this );

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    // mListView.setEmptyPlaceholder(true);
    /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    // MOBILE TOPOGRAPHER
    mNrButton1 = 2;
    if ( hasGps ) ++ mNrButton1;
    mButton1 = new Button[ mNrButton1 + 1];
    int kz = (hasGps)? 0 : 1; // index of izons
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mContext, this, izons[kz++] );
    }
    mButton1[mNrButton1] = MyButton.getButton( mContext, null, R.drawable.iz_empty );
    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    // MENU
    mMenuImage = (Button) findViewById( R.id.handle );
    mMenuImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( mContext, getResources(), R.drawable.iz_menu ) );

    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );
    setMenuAdapter( ); // in on Start()
    closeMenu();

    mList = (ListView) findViewById(R.id.fx_list);
    mList.setOnItemClickListener( this );

    refreshList();
  }

  /** refresh the list of fixed points
   */
  private void refreshList()
  {
    List< FixedInfo > fxds = TopoDroidApp.mData.selectAllFixed( TDInstance.sid, TDStatus.NORMAL );
    mFixedAdapter = new FixedAdapter( mContext, R.layout.message, fxds );
    mList.setAdapter( mFixedAdapter );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }

    // TDLog.Log( TDLog.LOG_LOC, "Location::onItemClick pos " + pos );
    // CharSequence item = ((TextView) view).getText();
    // String value = item.toString();
    // // setListPos( position  );
    // mSaveTextView = (TextView) view;
    mSaveFixed = mFixedAdapter.get(pos);
    mSavePos   = pos;
    (new FixedDialog( mContext, this, mSaveFixed )).show();
  }

  /** add a fixed point
   * @param station point station name
   * @param lng     longitude [degrees]
   * @param lat     latitude [degrees]
   * @param h_ell   ellipsoid altitude [m]
   * @param h_geo   geoid altitude [m]
   * @param comment comment
   * @param source  source type
   */
  public void addFixedPoint( String name,
                             double lng, // decimal degrees
                             double lat,
                             double h_ell,  // meters
                             double h_geo,
                             String comment,
                             long source,
                             double accur,
                             double accur_v
                           )
  {
    if ( comment == null ) comment = "";
    FixedInfo f = addLocation( name, lng, lat, h_ell, h_geo, comment, source, accur, accur_v );
    // if ( f != null ) { // always true
      mFixedAdapter.add( f );
      mList.invalidate();
    // }
  }

  /** insert a new fixed point 
   * @param station point station name
   * @param lng     longitude
   * @param lat     latitude
   * @param h_ell   ellipsoid altitude
   * @param h_geo   geoid altitude
   * @param comment comment
   * @param source  source type
   */
  private FixedInfo addLocation( String station, double lng, double lat, double h_ell, double h_geo, String comment, long source, double accur, double accur_v )
  {
    TDLog.v("FIXED new " + station + ": " + lng + " " + lat + " H " + h_ell + " " + h_geo );
    long id = TopoDroidApp.mData.insertFixed( TDInstance.sid, -1L, station, lng, lat, h_ell, h_geo, comment, 0L, source, accur, accur_v );
    return new FixedInfo( id, station, lng, lat, h_ell, h_geo, comment, source, accur, accur_v ); 
  }

  /** set the adapter of the menu pull-down list
   */
  void setMenuAdapter( )
  {
    // TDLog.v("MAIN set menu adapter");
    Resources res = getResources();
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<String >( this, R.layout.menu );

    menu_adapter.add( res.getString( menus[0] ) ); // CLOSE
    menu_adapter.add( res.getString( menus[1] ) ); // HELP

    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

  /** close the menu pull-down list
   */
  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  /** handle a tap on a menu item
   * @param pos   menu item index
   */
  private void handleMenu( int pos ) 
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) { // CLOSE 
      super.onBackPressed();
    } else if ( p == pos ) { // HELP
      doHelp();
    }
  }

  @Override
  public void onClick(View v) 
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }
    Button b = (Button) v;

    if ( b == mMenuImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        // TDLog.v("MENU-image tapped " + onMenu + " MENU visible ");
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        // TDLog.v("MENU-image tapped " + onMenu + " MENU not visible ");
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      // updateDisplay();
      return;
    }

    // if ( b == mBtHelp ) { // TOOLBAR
    //   doHelp();
    //   return;
    // } else if ( b == mBtClose ) {
    //   super.onBackPressed();
    //   return;
    // }

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
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    // intent.putExtra( "importtype", index ); // extra is not returned to the app
    startActivityForResult( Intent.createChooser(intent, getResources().getString( R.string.title_import_gps ) ), TDRequest.REQUEST_GET_GPS_IMPORT );
  }


  public boolean hasLocation( String station )
  {
    return TopoDroidApp.mData.hasFixed( TDInstance.sid, station );
  }

  /** update fixed point data
   * @param fxd     fixed point
   * @param lng     longitude [degrees]
   * @param lat     latitude [degrees]
   * @param h_ell   ellipsoid altitude [m]
   * @param h_geo   geoid altitude [m]
   */
  void updateFixedData( FixedInfo fxd, double lng, double lat, double h_ell, double h_geo ) // , double accur, double accur_v )
  {
    TDLog.v("FIXED update data " + fxd.name + ": " + lng + " " + lat + " H " + h_ell + " " + h_geo );
    TopoDroidApp.mData.updateFixedData( fxd.id, TDInstance.sid, lng, lat, h_ell, h_geo ); // , accur, accur_v );
    // mList.invalidate();
    refreshList();
  }
 
  /** update fixed point name and comment
   * @param fxd     fixed point
   * @param name    station name
   * @param comment comment
   */
  void updateFixedNameComment( FixedInfo fxd, String name, String comment )
  {
    TopoDroidApp.mData.updateFixedStationComment( fxd.id, TDInstance.sid, name, comment );
    // mList.invalidate();
    refreshList();
  }

  /** drop a fixed point
   * @param fxd     fixed point to drop (mark as deleted)
   */
  public void dropFixed( FixedInfo fxd )
  {
    TopoDroidApp.mData.updateFixedStatus( fxd.id, TDInstance.sid, TDStatus.DELETED );
    refreshList();
  }

  /** set the survey declination
   * @param decl   declination [degree]
   */
  void setDeclination( float decl )
  {
    // TDLog.v( "set survey declination " + decl );
    TopoDroidApp.mData.updateSurveyDeclination( TDInstance.sid, decl );
  }

  // private final static int LOCATION_REQUEST = 1;
  private static final int CRS_CONVERSION_REQUEST = 2; // not final ?
  private static final int CRS_INPUT_REQUEST = 3;      // not final ?
  private double mFixedHGeo = 0;
  private FixedDialog mFixedDialog = null;
  private FixedAddDialog mFixedAddDialog = null;

  /** clear converted coords in a fixed point
   * @param fxd      fixed point
   */
  void clearConvertedCoords( FixedInfo fxd ) 
  {
    TopoDroidApp.mData.updateFixedCS( fxd.id, TDInstance.sid, null, 0, 0, 0, 2L, 0 );
    fxd.clearConverted();
  }

  /** get converted coords
   * @param dialog   fixed point edit dialog
   * @param cs_to    target CS
   * @param fxd      fixed point
   */
  void tryProj4( FixedDialog dialog, String cs_to, FixedInfo fxd )
  {
    if ( cs_to == null ) return;
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "request", "CRS_CONVERSION_REQUEST" ); // Proj4 request
      intent.putExtra( "cs_from", "Long-Lat" ); // NOTE MUST USE SAME NAME AS Proj4
      intent.putExtra( "cs_to", cs_to ); 
      intent.putExtra( "longitude", fxd.lng );
      intent.putExtra( "latitude",  fxd.lat );
      // intent.putExtra( "altitude",  fxd.h_ell );
      intent.putExtra( "altitude",  fxd.h_geo ); // geoid altitude
      mFixedHGeo   = fxd.h_geo;
      mFixedDialog = dialog;
      // TDLog.Log( TDLog.LOG_LOC, "CONV. REQUEST " + fxd.lng + " " + fxd.lat + " " + fxd.h_ell );
      startActivityForResult( intent, CRS_CONVERSION_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      // mFixedHGeo  = 0;
      mFixedDialog = null;
      TDToast.makeBad( R.string.no_proj4 );
    }
  }

  /** request coordinates from Proj4
   * @param dialog   add dialog
   */
  void getProj4Coords( FixedAddDialog dialog )
  {
    TDLog.v("FIXED get Proj4 coords");
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "request", "CRS_INPUT_REQUEST" ); // Proj4 request
      mFixedAddDialog = dialog;
      // TDLog.Log( TDLog.LOG_LOC, "COORD. INPUT REQUEST " );
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
            // double h_ell = bundle.getDouble( "altitude"); // Proj4 (geoid) altitude
            double h_geo = mFixedHGeo; // use geoid altitude instead of Proj4 altitude
	    long   n_dec = bundle.containsKey( "decimals" )? bundle.getLong( "decimals" ) : 2;
	    double conv  = bundle.containsKey( "convergence" )? bundle.getDouble( "convergence" ) : 0; // degrees
            TopoDroidApp.mData.updateFixedCS(  mFixedDialog.getFixedId(), TDInstance.sid, cs, lng, lat, h_geo, n_dec, conv );
            mFixedDialog.setConvertedCoords( cs, lng, lat, h_geo, n_dec, conv );
          }
          // mFixedHGeo   = 0;
          mFixedDialog = null;
        }
      } else if ( reqCode == CRS_INPUT_REQUEST ) {
        if ( mFixedAddDialog != null ) {
          Bundle bundle = intent.getExtras();
          if ( bundle != null ) {
            mFixedAddDialog.setCoordsGeo(
              bundle.getDouble( "longitude"),
              bundle.getDouble( "latitude"),
              bundle.getDouble( "altitude")  // geoid altitude
	      // ( bundle.containsKey( "convergence" )? bundle.getDouble( "convergence" ) : 0) // degrees
            );
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
            // TDLog.v( "read " + line );

            // syntax: name, lat, lng, h_ell, h_geo
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
          TDLog.Error( "File not found " + e.getMessage() );
        } catch ( IOException e ) { 
          TDLog.Error( "IO exception " + e.getMessage() );
        }
      }
    }
  }

  /** react to a user hw key press
   * @param code key code
   * @param event key event
   * @return true if the key press has been handled
   */
  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_MENU:   // HARDWARE MENU (82)
        doHelp();
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

  /** start user-man window with the help page of this activity
   */
  private void doHelp()
  {
    String help_page = getResources().getString( R.string.FixedActivity );
    /* if ( help_page != null ) */ UserManualActivity.showHelpPage( this, help_page );
  }

  /** react to a change in the configuration
   * @param cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration cfg )
  {
    super.onConfigurationChanged( cfg );
    TDLocale.resetTheLocale();
  }

}

