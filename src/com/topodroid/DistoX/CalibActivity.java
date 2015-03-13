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
 * CHANGES
 * 20120520 created
 * 20120524 added device in CalibInfo
 * 20120531 activated doDelete with askDelete first
 * 20120725 TopoDroidApp log
 * 20121124 calibration-device consistency check
 * 20131201 button bar new interface. reorganized actions
 * 20140416 setError for required EditText inputs
 */
package com.topodroid.DistoX;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

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

import android.app.Application;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
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
{
  // private static int icons00no[];
  // private static int iconsno[] = {
  //                       0, // R.drawable.ic_save_no,
  //                       R.drawable.ic_open_no,
  //                       R.drawable.ic_read_no
  //                       // R.drawable.ic_export0_no,
  //                    };
  // private static int ixonsno[] = {
  //                       0, // R.drawable.ix_save_no,
  //                       R.drawable.ix_open_no,
  //                       R.drawable.ix_read_no
  //                       // R.drawable.ix_export0_no
  //                    };
  private static int izonsno[] = {
                        0, // R.drawable.iz_save_no,
                        R.drawable.iz_open_no,
                        R.drawable.iz_read_no
                        // R.drawable.iz_export0_no
                     };
                      
  // private static int icons00[];
  // private static int icons[] = {
  //                       R.drawable.ic_save,
  //                       R.drawable.ic_open,
  //                       R.drawable.ic_read
  //                    };
  // private static int ixons[] = {
  //                       R.drawable.ix_save,
  //                       R.drawable.ix_open,
  //                       R.drawable.ix_read
  //                     };
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
  private EditText mEditDate;
  private EditText mEditDevice;
  private EditText mEditComment;

  private RadioButton mCBAlgoAuto;
  private RadioButton mCBAlgoLinear;
  private RadioButton mCBAlgoNonLinear;

  private MenuItem mMIexport;
  private MenuItem mMIdelete;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

  private TopoDroidApp mApp;
  private boolean isSaved;

  private void setButtons( )
  {
    mButton1[1].setEnabled( isSaved );   // open
    if ( 2 < mNrButton1 ) mButton1[2].setEnabled( isSaved ); // coeff (read)
    // mButton1[3].setEnabled( isSaved );   // export
    if ( isSaved ) {
      // mButton1[1].setBackgroundResource( icons00[1] );
      mButton1[1].setBackgroundDrawable( mBMopen );
      if ( 2 < mNrButton1 ) {
        // mButton1[2].setBackgroundResource( icons00[2] );
        mButton1[2].setBackgroundDrawable( mBMread );
      }
    } else {
      // mButton1[1].setBackgroundResource( icons00no[1] );
      mButton1[1].setBackgroundDrawable( mBMopen_no );
      if ( 2 < mNrButton1 ) {
        // mButton1[2].setBackgroundResource( icons00no[2] );
        mButton1[2].setBackgroundDrawable( mBMread_no );
      }
    }
  }

// -------------------------------------------------------------------
  // private Button mButtonHelp;
  private Button[] mButton1;
  private int mNrButton1 = 0;
  // private Button[] mButton2;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    mApp     = (TopoDroidApp)getApplication();

    setContentView(R.layout.calib_activity);
    mEditName    = (EditText) findViewById(R.id.calib_name);
    mEditDate    = (EditText) findViewById(R.id.calib_date);
    mEditDevice  = (EditText) findViewById(R.id.calib_device);
    mEditComment = (EditText) findViewById(R.id.calib_comment);

    mCBAlgoAuto      = (RadioButton) findViewById( R.id.calib_algo_auto );
    mCBAlgoLinear    = (RadioButton) findViewById( R.id.calib_algo_linear );
    mCBAlgoNonLinear = (RadioButton) findViewById( R.id.calib_algo_non_linear );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );
    // icons00   = ( TopoDroidSetting.mSizeButtons == 2 )? ixons : icons;
    // icons00no = ( TopoDroidSetting.mSizeButtons == 2 )? ixonsno : iconsno;

    mNrButton1 = 2 + ( TopoDroidSetting.mLevelOverNormal? 1 : 0 );
    mButton1 = new Button[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      // mButton1[k].setBackgroundResource(  icons00[k] );
      BitmapDrawable bm2 = mApp.setButtonBackground( mButton1[k], size, izons[k] );
      if ( k == 1 ) {
        mBMopen = bm2;
      } else if ( k == 2 ) {
        mBMread = bm2;
      }
    }
    mBMopen_no = mApp.setButtonBackground( null, size, izonsno[1] );
    mBMread_no = mApp.setButtonBackground( null, size, izonsno[2] );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    // mButtonView2 = new HorizontalButtonView( mButton2 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );

    // TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "app mCID " + mApp.mCID );
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
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      mEditDate.setText( sdf.format( new Date() ) );
      mEditDevice.setText( mApp.distoAddress() );
      mEditComment.setHint( R.string.description );
      mCBAlgoAuto.setChecked( true );
    }

    setButtons();

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    // mImage.setBackgroundResource( ( TopoDroidSetting.mSizeButtons == 2 )? R.drawable.ix_menu : R.drawable.ic_menu );
    mApp.setButtonBackground( mImage, size, R.drawable.iz_menu );

    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter();
    closeMenu();
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

    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "onClick(View) " + view.toString() );
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
    byte[] coeff = Calibration.stringToCoeff( mApp.mData.selectCalibCoeff( mApp.mCID ) );
    Matrix mG = new Matrix();
    Matrix mM = new Matrix();
    Vector vG = new Vector();
    Vector vM = new Vector();
    Vector nL = new Vector();
    Calibration.coeffToG( coeff, vG, mG );
    Calibration.coeffToM( coeff, vM, mM );
    Calibration.coeffToNL( coeff, nL );
   
    CalibResult res = new CalibResult();
    mApp.mData.selectCalibError( mApp.mCID, res );
    (new CalibCoeffDialog( this, mApp, vG, mG, vM, mM, nL, res.error, res.max_error, res.iterations, coeff )).show();
  }

  private void askDelete()
  {
    new TopoDroidAlertDialog( this, getResources(), 
                              getResources().getString( R.string.calib_delete ) + " " + mApp.myCalib + " ?",
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "calib delite" );
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
    name = TopoDroidApp.noSpaces( name );
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
      mApp.mData.updateCalibInfo( mApp.mCID, date, device, comment );
      Toast.makeText( this, R.string.calib_updated, Toast.LENGTH_SHORT ).show();
    } else { // new calib
      name = TopoDroidApp.noSpaces( name );
      if ( name != null && name.length() > 0 ) {
        if ( mApp.hasCalibName( name ) ) { // name already exists
          // Toast.makeText( this, R.string.calib_exists, Toast.LENGTH_SHORT ).show();
          String error = getResources().getString( R.string.calib_exists );
          mEditName.setError( error );
        } else {
          mApp.setCalibFromName( name );
          mApp.mData.updateCalibInfo( mApp.mCID, date, device, comment );
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
    mApp.mData.updateCalibAlgo( mApp.mCID, algo );

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
    mApp.mData.doDeleteCalib( mApp.mCID );
    mApp.setCalibFromName( null );
    finish();
  }

  // ---------------------------------------------------------
  /* MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIexport  = menu.add( R.string.menu_export );
    mMIdelete  = menu.add( R.string.menu_delete );
    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIexport.setIcon(  icons[3] );
    mMIdelete.setIcon(  icons[4] );
    mMIoptions.setIcon( icons[5] );
    mMIhelp.setIcon(    icons[6] );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
      startActivity( intent );
    } else if ( item == mMIexport  ) { // EXPORT
      if ( mApp.myCalib != null ) {
        new CalibExportDialog( this, this ).show();
      } else {
        // TODO Toast
      }
    } else if ( item == mMIdelete  ) { // DELETE DIALOG
      if ( mApp.myCalib != null ) {
        askDelete();
      } else {
        // TODO Toast
      }
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 4 ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  */

  private void setMenuAdapter()
  {
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    if ( TopoDroidSetting.mLevelOverBasic  ) mMenuAdapter.add( res.getString( menus[1] ) );
    mMenuAdapter.add( res.getString( menus[2] ) );
    mMenuAdapter.add( res.getString( menus[3] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // CharSequence item = ((TextView) view).getText();
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      // Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show();
      int p = 0;
      if ( p++ == pos ) { // EXPORT
        if ( mApp.myCalib != null ) {
          new CalibExportDialog( this, this ).show();
        }
      } else if ( TopoDroidSetting.mLevelOverBasic && p++ == pos ) { // DELETE 
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
  }

  void doExport( int exportType, boolean warn )
  {
    if ( mApp.mCID < 0 ) {
      if ( warn ) {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
      }
    } else {
      String filename = null;
      switch ( exportType ) {
        case TopoDroidConst.DISTOX_EXPORT_CSV:
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
