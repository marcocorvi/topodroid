/* @file CalibActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calib activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.app.Activity;
// import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.content.Context;
// import android.content.Intent;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;
import android.app.DatePickerDialog;

import android.app.Application;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class CalibActivity extends Activity
                           implements OnItemClickListener
                           , View.OnClickListener
                           , IExporter
{
  private static int izonsno[] = {
                        0, // R.drawable.iz_save_no,
                        R.drawable.iz_open_no,
                        R.drawable.iz_read_no
                        // R.drawable.iz_export0_no
                     };
  private static int izons[] = {
                        R.drawable.iz_save,
                        R.drawable.iz_open,
                        R.drawable.iz_read
                     };

  BitmapDrawable mBMopen;
  BitmapDrawable mBMopen_no;
  BitmapDrawable mBMread;
  BitmapDrawable mBMread_no;
  
  private static int menus[] = {
                        R.string.menu_export,
                        R.string.menu_delete,
                        R.string.menu_options,
                        R.string.menu_help
                     };

  private static int help_icons[] = {
                        R.string.help_save_calib,
                        R.string.help_open_calib,
                        R.string.help_coeff
                      };
  private static int help_menus[] = {
                        R.string.help_export_calib,
                        R.string.help_delete_calib,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private EditText mEditName;
  private Button mEditDate;
  private EditText mEditDevice;
  private EditText mEditComment;

  MyDateSetListener mDateListener;

  private RadioButton mCBAlgoAuto;
  private RadioButton mCBAlgoLinear;
  private RadioButton mCBAlgoNonLinear;

  private TopoDroidApp mApp;
  private boolean isSaved;

  private void setButtons( )
  {
    mButton1[1].setEnabled( isSaved );   // open
    if ( 2 < mNrButton1 ) mButton1[2].setEnabled( isSaved ); // coeff (read)
    if ( isSaved ) {
      mButton1[1].setBackgroundDrawable( mBMopen );
      if ( 2 < mNrButton1 ) {
        mButton1[2].setBackgroundDrawable( mBMread );
      }
    } else {
      mButton1[1].setBackgroundDrawable( mBMopen_no );
      if ( 2 < mNrButton1 ) {
        mButton1[2].setBackgroundDrawable( mBMread_no );
      }
    }
  }

// -------------------------------------------------------------------
  private MyButton[] mButton1;
  private int mNrButton1 = 0;
  // private Button[] mButton2;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  ListView   mMenu;
  Button     mImage;
  // HOVER
  // MyMenuAdapter mMenuAdapter;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    mApp     = (TopoDroidApp)getApplication();

    setContentView(R.layout.calib_activity);
    mEditName    = (EditText) findViewById(R.id.calib_name);
    mEditDate    = (Button) findViewById(R.id.calib_date);
    mEditDevice  = (EditText) findViewById(R.id.calib_device);
    mEditComment = (EditText) findViewById(R.id.calib_comment);

    mDateListener = new MyDateSetListener( mEditDate );
    mEditDate.setOnClickListener( this );

    mCBAlgoAuto      = (RadioButton) findViewById( R.id.calib_algo_auto );
    mCBAlgoLinear    = (RadioButton) findViewById( R.id.calib_algo_linear );
    mCBAlgoNonLinear = (RadioButton) findViewById( R.id.calib_algo_non_linear );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );
    // icons00   = ( TDSetting.mSizeButtons == 2 )? ixons : icons;
    // icons00no = ( TDSetting.mSizeButtons == 2 )? ixonsno : iconsno;

    mNrButton1 = 2 + ( TDSetting.mLevelOverNormal? 1 : 0 );
    mButton1 = new MyButton[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = new MyButton( this, this, size, izons[k], 0 );
      if ( k == 1 ) { mBMopen = mButton1[k].mBitmap; }
      else if ( k == 2 ) { mBMread = mButton1[k].mBitmap; }
    }
    mBMopen_no = MyButton.getButtonBackground( mApp, size, izonsno[1] );
    mBMread_no = MyButton.getButtonBackground( mApp, size, izonsno[2] );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    // mButtonView2 = new HorizontalButtonView( mButton2 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );

    // TDLog.Log( TDLog.LOG_CALIB, "app mCID " + mApp.mCID );
    setNameEditable( mApp.mCID >= 0 );
    if ( isSaved ) {
      CalibInfo info = mApp.getCalibInfo();
      mEditName.setText( info.name );
      // mEditName.setEditable( false );
      mEditDate.setText( info.date );
      if ( info.device != null && info.device.length() > 0 ) {
        mEditDevice.setText( info.device );
      } else if ( mApp.distoAddress() != null ) {
        mEditDevice.setText( mApp.distoAddress() );
      }
      if ( info.comment != null && info.comment.length() > 0 ) {
        mEditComment.setText( info.comment );
      } else {
        mEditComment.setHint( R.string.description );
      }
      switch ( info.algo ) {
        case 0: mCBAlgoAuto.setChecked( true ); break;
        case 1: mCBAlgoLinear.setChecked( true ); break;
        case 2: mCBAlgoNonLinear.setChecked( true ); break;
        default: mCBAlgoAuto.setChecked( true ); break;
      }
    } else {
      mEditName.setHint( R.string.name );
      mEditDate.setText( TopoDroidUtil.currentDate() );
      mEditDevice.setText( mApp.distoAddress() );
      mEditComment.setHint( R.string.description );
      mCBAlgoAuto.setChecked( true );
    }

    setButtons();

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    // mImage.setBackgroundResource( ( TDSetting.mSizeButtons == 2 )? R.drawable.ix_menu : R.drawable.ic_menu );
    MyButton.setButtonBackground( mApp, mImage, size, R.drawable.iz_menu );

    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter( getResources() );
    closeMenu();
    // HOVER
    mMenu.setOnItemClickListener( this );
  }

  // ---------------------------------------------------------------

  @Override
  public void onClick(View view)
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }

    // TDLog.Log( TDLog.LOG_INPUT, "onClick(View) " + view.toString() );
    Button b = (Button)view;

    if ( b == mImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    } else if ( b == mEditDate ) {
      String date = mEditDate.getText().toString();
      int y = TopoDroidUtil.dateParseYear( date );
      int m = TopoDroidUtil.dateParseMonth( date );
      int d = TopoDroidUtil.dateParseDay( date );
      new DatePickerDialog( this, mDateListener, y, m, d ).show();
      return;
    }

    int k = 0;
    if ( k < mNrButton1 && b == mButton1[k++] ) {
      doSave();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {
      if ( ! mApp.checkCalibrationDeviceMatch() ) {
        // FIXME use alert dialog
        Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
      }
      doOpen();
    // } else if ( k < mNrButton1 && b == mButton1[k++] ) { // export
    //   new CalibExportDialog( this, this ).show();
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {
      showCoeffs();
    // } else if ( k < mNrButton1 && b == mButton1[k++] ) {
    //   askDelete();
    }
  }

  private void showCoeffs()
  {
    byte[] coeff = Calibration.stringToCoeff( mApp.mDData.selectCalibCoeff( mApp.mCID ) );
    Matrix mG = new Matrix();
    Matrix mM = new Matrix();
    Vector vG = new Vector();
    Vector vM = new Vector();
    Vector nL = new Vector();
    Calibration.coeffToG( coeff, vG, mG );
    Calibration.coeffToM( coeff, vM, mM );
    Calibration.coeffToNL( coeff, nL );
   
    CalibResult res = new CalibResult();
    mApp.mDData.selectCalibError( mApp.mCID, res );
    (new CalibCoeffDialog( this, mApp, vG, mG, vM, mM, nL, null,
                           res.error, res.stddev, res.max_error, res.iterations, coeff )).show();
  }

  private void askDelete()
  {
    TopoDroidAlertDialog.makeAlert( this, getResources(), 
                              getResources().getString( R.string.calib_delete ) + " " + mApp.myCalib + " ?",
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          // TDLog.Log( TDLog.LOG_INPUT, "calib delite" );
          doDelete();
        }
      }
    );
  }

  private void doOpen()
  {
    Intent openIntent = new Intent( this, GMActivity.class );
    startActivity( openIntent );
  }

  private void doSave( )
  {
    String name = mEditName.getText().toString().trim();
    if ( name == null ) {
      String error = getResources().getString( R.string.error_name_required );
      mEditName.setError( error );
      return;
    }
    name = TopoDroidUtil.noSpaces( name );
    if ( name.length() == 0 ) {
      String error = getResources().getString( R.string.error_name_required );
      mEditName.setError( error );
      return;
    }

    String date = mEditDate.getText().toString();
    String device = mEditDevice.getText().toString();
    String comment = mEditComment.getText().toString();
    if ( date    != null ) { date    = date.trim(); }
    if ( device  != null ) { device  = device.trim(); }
    if ( comment != null ) { comment = comment.trim(); }

    if ( isSaved ) { // calib already saved
      mApp.mDData.updateCalibInfo( mApp.mCID, date, device, comment );
      Toast.makeText( this, R.string.calib_updated, Toast.LENGTH_SHORT ).show();
    } else { // new calib
      name = TopoDroidUtil.noSpaces( name );
      if ( name != null && name.length() > 0 ) {
        if ( mApp.hasCalibName( name ) ) { // name already exists
          // Toast.makeText( this, R.string.calib_exists, Toast.LENGTH_SHORT ).show();
          String error = getResources().getString( R.string.calib_exists );
          mEditName.setError( error );
        } else {
          mApp.setCalibFromName( name );
          mApp.mDData.updateCalibInfo( mApp.mCID, date, device, comment );
          setNameEditable( true );
          Toast.makeText( this, R.string.calib_saved, Toast.LENGTH_SHORT ).show();
        }
      } else {
        Toast.makeText( this, R.string.calib_no_name, Toast.LENGTH_SHORT ).show();
      }
    }
    int algo = 0;
    if ( mCBAlgoLinear.isChecked() ) algo = 1;
    else if ( mCBAlgoNonLinear.isChecked() ) algo = 2;
    mApp.mDData.updateCalibAlgo( mApp.mCID, algo );

    setButtons();
  }
  
  private void setNameEditable( boolean saved )
  {
    isSaved = saved;
    if ( isSaved ) {
      mEditName.setFocusable( false );
      mEditName.setClickable( false );
      mEditName.setKeyListener( null );
      mEditDevice.setFocusable( false );
      mEditDevice.setClickable( false );
      mEditDevice.setKeyListener( null );
    }
  }

  public void doDelete()
  {
    if ( mApp.mCID < 0 ) return;
    mApp.mDData.doDeleteCalib( mApp.mCID );
    mApp.setCalibFromName( null );
    finish();
  }

  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( this, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
    startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
        return onSearchRequested();
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.CalibActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------

  private void setMenuAdapter( Resources res )
  {
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( this, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );

    mMenuAdapter.add( res.getString( menus[0] ) );
    if ( TDSetting.mLevelOverBasic  ) mMenuAdapter.add( res.getString( menus[1] ) );
    mMenuAdapter.add( res.getString( menus[2] ) );
    mMenuAdapter.add( res.getString( menus[3] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    // HOVER
    // mMenuAdapter.resetBgColor();
    onMenu = false;
  }

  private void handleMenu( int pos )
  {
    closeMenu();
    // Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show();
    int p = 0;
    if ( p++ == pos ) { // EXPORT
      if ( mApp.myCalib != null ) {
        // new CalibExportDialog( this, this ).show();
        new ExportDialog( this, this, TDConst.mCalibExportTypes, R.string.title_calib_export ).show();
      }
    } else if ( TDSetting.mLevelOverBasic && p++ == pos ) { // DELETE 
      if ( mApp.myCalib != null ) {
        askDelete();
      }
    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
      startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 4 ) ).show();
    }
  }

  public void doExport( String type )
  {
    int index = TDConst.calibExportIndex( type );
    if ( index >= 0 ) doExport( index, true );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // CharSequence item = ((TextView) view).getText();
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
    }
  }

  private void doExport( int exportType, boolean warn )
  {
    if ( mApp.mCID < 0 ) {
      if ( warn ) {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
      }
    } else {
      String filename = null;
      switch ( exportType ) {
        case TDConst.DISTOX_EXPORT_CSV:
          filename = mApp.exportCalibAsCsv();
      }
      if ( warn ) { 
        if ( filename != null ) {
          Toast.makeText( this, getString(R.string.saving_) + filename, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText( this, R.string.saving_file_failed, Toast.LENGTH_SHORT).show();
        }
      }
    }
  }
}
