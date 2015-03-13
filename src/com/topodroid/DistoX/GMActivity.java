/* @file GMActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calibration data activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 angle units
 * 20120715 per-category preferences
 * 20121124 check device-calibration consistency before download and write
 * 20131201 button bar new interface. reorganized actions
 * 20140609 enableWrite: two state write button
 */
package com.topodroid.DistoX;

// import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

// import java.lang.Long;
// import java.lang.reflect.Method;
// import java.lang.reflect.InvocationTargetException;

import android.app.Application;
import android.app.Activity;
// import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.AsyncTask;
// import android.os.Handler;
// import android.os.Message;
// import android.os.Parcelable;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.DialogInterface;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.Menu;
import android.view.MenuItem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.util.Log;

public class GMActivity extends Activity
                        implements OnItemClickListener, ILister, IEnableButtons
                        , OnClickListener
{
  private TopoDroidApp mApp;

  private String mSaveData;                // saved GM text representation
  private TextView mSaveTextView;          // view of the saved GM
  private CalibCBlock mSaveCBlock = null;  // data of the saved GM
  private long mCIDid = -1;     // id of the GM
  private int mBlkStatus = 0;   // min display Group (can be either 1 [only active] or 0 [all])
  private int mAlgo;            // calibration algorithm

  private ListView mList;                  // display list

  private MenuItem mMIoptions;
  private MenuItem mMIdisplay;
  private MenuItem mMIhelp;

  private CalibCBlockAdapter mDataAdapter;  // adapter for the list of GM's

  private String mCalibName;
  // private ConnHandler mHandler;

  static int CALIB_COMPUTE_CALIB  = 0;
  static int CALIB_COMPUTE_GROUPS = 1;
  static int CALIB_RESET_GROUPS   = 2;

  // static int icons00[];
  // static int icons[] = { 
  //                       R.drawable.ic_download,
  //                       R.drawable.ic_toggle,
  //                       R.drawable.ic_group,
  //                       R.drawable.ic_cover,
  //                       R.drawable.ic_compute,
  //                       R.drawable.ic_read,
  //                       R.drawable.ic_write
  //                    };
  // static int ixons[] = { 
  //                       R.drawable.ix_download,
  //                       R.drawable.ix_toggle,
  //                       R.drawable.ix_group,
  //                       R.drawable.ix_cover,
  //                       R.drawable.ix_compute,
  //                       R.drawable.ix_read,
  //                       R.drawable.ix_write
  //                    };
  static int izons[] = { 
                        R.drawable.iz_download,
                        R.drawable.iz_toggle,
                        R.drawable.iz_group,
                        R.drawable.iz_cover,
                        R.drawable.iz_compute,
                        R.drawable.iz_read,
                        R.drawable.iz_write
                     };

  // static int icons00no[];
  // static int iconsno[] = { 
  //                       0,
  //                       R.drawable.ic_toggle_no,
  //                       0,
  //                       0,
  //                       0,
  //                       R.drawable.ic_read_no,
  //                       R.drawable.ic_write_no
  //                    };
  // static int ixonsno[] = { 
  //                       0,
  //                       R.drawable.ix_toggle_no,
  //                       0,
  //                       0,
  //                       0,
  //                       R.drawable.ix_read_no,
  //                       R.drawable.ix_write_no
  //                    };
  static int izonsno[] = { 
                        0,
                        R.drawable.iz_toggle_no,
                        0,
                        0,
                        0,
                        R.drawable.iz_read_no,
                        R.drawable.iz_write_no
                     };


  BitmapDrawable mBMtoggle;
  BitmapDrawable mBMtoggle_no;
  BitmapDrawable mBMread;
  BitmapDrawable mBMread_no;
  BitmapDrawable mBMwrite;
  BitmapDrawable mBMwrite_no;

  static int menus[] = {
                        R.string.menu_options,  // 8
                        R.string.menu_display,
                        R.string.menu_help
                     };

  static int help_icons[] = { 
                        R.string.help_download,
                        R.string.help_toggle,
                        R.string.help_group,
                        R.string.help_cover,
                        R.string.help_compute,
                        R.string.help_read,
                        R.string.help_write
                      };
  static int help_menus[] = { 
                        R.string.help_prefs,
                        R.string.help_display_calib,
                        R.string.help_help
                      };
  // -------------------------------------------------------------------
  // forward survey name to DataHelper

  // -------------------------------------------------------------

  /** called by CalibComputer Task
   * @return nr of iterations (neg. error)
   */
  int computeCalib()
  {
    long cid = mApp.mCID;
    if ( cid < 0 ) return -2;
    List<CalibCBlock> list = mApp.mData.selectAllGMs( cid, 0 ); 
    if ( list.size() < 16 ) {
      return -1;
    }
    Calibration calibration = mApp.mCalibration;
    // FIXME set the calibration algorithm (whether non-linear or linear)
    calibration.setAlgorith( mAlgo == 2 ); // CALIB_AUTO_NON_LINEAR

    calibration.Reset( list.size() );
    for ( CalibCBlock item : list ) {
      calibration.AddValues( item );
    }
    int iter = calibration.Calibrate();
    if ( iter > 0 ) {
      float[] errors = calibration.Errors();
      float max_error = 0.0f;
      int k = 0;
      for ( CalibCBlock cb : list ) {
        mApp.mData.updateGMError( cb.mId, cid, errors[k] );
        // cb.setError( errors[k] );
        if ( errors[k] > max_error ) max_error = errors[k];
        ++k;
      }
      calibration.mMaxError = max_error;

      byte[] coeff = calibration.GetCoeff();
      mApp.mData.updateCalibCoeff( cid, Calibration.coeffToString( coeff ) );
      mApp.mData.updateCalibError( cid, 
             calibration.Delta(),
             calibration.mMaxError * TopoDroidUtil.RAD2GRAD,
             iter );

      // DEBUG:
      // Calibration.logCoeff( coeff );
      // coeff = Calibration.stringToCoeff( mApp.mData.selectCalibCoeff( cid ) );
      // Calibration.logCoeff( coeff );
    }
    // Log.v( TopoDroidApp.TAG, "iteration " + iter );
    return iter;
  }

  void handleComputeCalibResult( int result )
  {
    resetTitle( );
    // ( result == -2 ) not handled
    if ( result == -1 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    } else if ( result > 0 ) {
      enableWrite( true );
      Calibration calibration = mApp.mCalibration;
      Vector bg = calibration.GetBG();
      Matrix ag = calibration.GetAG();
      Vector bm = calibration.GetBM();
      Matrix am = calibration.GetAM();
      Vector nL = calibration.GetNL();
      byte[] coeff = calibration.GetCoeff();

      float error = calibration.mMaxError * TopoDroidUtil.RAD2GRAD;
      (new CalibCoeffDialog( this, mApp, bg, ag, bm, am, nL, calibration.Delta(), error, result, coeff ) ).show();
    } else {
      // Toast.makeText( mApp.getApplicationContext(), R.string.few_data, Toast.LENGTH_SHORT ).show();
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    }
    updateDisplay( );
  }

  void doResetGroups( )
  {
    mApp.mData.resetAllGMs( mApp.mCID ); // reset all groups where status=0
  }

  void doComputeGroups( )
  {
    long cid = mApp.mCID;
    if ( cid < 0 ) return;
    float thr = (float)Math.cos( TopoDroidSetting.mGroupDistance * TopoDroidUtil.GRAD2RAD);
    List<CalibCBlock> list = mApp.mData.selectAllGMs( cid, 0 );
    if ( list.size() < 4 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    }
    long group = 0;
    int cnt = 0;
    float b = 0.0f;
    float c = 0.0f;
    switch ( TopoDroidSetting.mGroupBy ) {
      case TopoDroidSetting.GROUP_BY_DISTANCE:
        for ( CalibCBlock item : list ) {
          if ( group == 0 || item.isFarFrom( b, c, thr ) ) {
            ++ group;
            b = item.mBearing;
            c = item.mClino;
          }
          item.setGroup( group );
          mApp.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
          // N.B. item.calibId == cid
        }
        break;
      case TopoDroidSetting.GROUP_BY_FOUR:
        // TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "group by four");
        group = 1;
        for ( CalibCBlock item : list ) {
          item.setGroup( group );
          mApp.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
          ++ cnt;
          if ( (cnt%4) == 0 ) {
            ++group;
            // TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "cnt " + cnt + " new group " + group );
          }
        }
        break;
      case TopoDroidSetting.GROUP_BY_ONLY_16:
        group = 1;
        for ( CalibCBlock item : list ) {
          item.setGroup( group );
          mApp.mData.updateGMName( item.mId, item.mCalibId, Long.toString(group) );
          ++ cnt;
          if ( (cnt%4) == 0 || cnt >= 16 ) ++group;
        }
        break;
    }
  }

  @Override
  public void refreshDisplay( int nr, boolean toast )
  {
    // Log.v( TopoDroidApp.TAG, "refreshDisplay nr " + nr );
    resetTitle( );
    if ( nr >= 0 ) {
      if ( nr > 0 ) updateDisplay( );
      if ( toast ) {
        Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_SHORT ).show();
      }
    } else if ( nr < 0 ) {
      if ( toast ) {
        // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, mApp.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
      }
    }
  }
    
  public void updateBlockList( DistoXDBlock blk )
  {
  }

  public void updateDisplay( )
  {
    // Log.v( TopoDroidApp.TAG, "updateDisplay CID " + mApp.mCID );
    resetTitle( );
    mDataAdapter.clear();
    DataHelper data = mApp.mData;
    if ( data != null && mApp.mCID >= 0 ) {
      List<CalibCBlock> list = data.selectAllGMs( mApp.mCID, mBlkStatus );
      // Log.v( TopoDroidApp.TAG, "updateDisplay GMs " + list.size() );
      updateGMList( list );
      setTitle( mCalibName );
    }
  }

  private void updateGMList( List<CalibCBlock> list )
  {
    int nr_saturated_values = 0;
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_gms, Toast.LENGTH_SHORT ).show();
      return;
    }
    for ( CalibCBlock item : list ) {
      if ( item.isSaturated() ) ++ nr_saturated_values;
      mDataAdapter.add( item );
    }
    // mList.setAdapter( mDataAdapter );
    if ( nr_saturated_values > 0 ) {
      Toast.makeText( this, 
        String.format( getResources().getString( R.string.calib_saturated_values ), nr_saturated_values ),
        Toast.LENGTH_LONG ).show();
    }
  }


  // ---------------------------------------------------------------
  // list items click


  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // OPTIONS
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
        startActivity( intent );
      } else if ( p++ == pos ) { // DISPLAY
        mBlkStatus = 1 - mBlkStatus;       // 0 --> 1;  1 --> 0
        updateDisplay( );
      } else if ( p++ == pos ) { // HELP
        (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 3 ) ).show();
      }
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }

    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "GMActivity onItemClick() " + item.toString() );

    // if ( value.equals( getResources().getString( R.string.back_to_calib ) ) ) {
    //   setStatus( STATUS_CALIB );
    //   updateDisplay( );
    //   return;
    // }
    mSaveCBlock   = mDataAdapter.get( pos );
    mSaveTextView = (TextView) view;
    String msg = mSaveTextView.getText().toString();
    String[] st = msg.split( " ", 3 );
    try {    
      mCIDid    = Long.parseLong(st[0]);
      // String name = st[1];
      mSaveData = st[2];
      if ( mSaveCBlock.mStatus == 0 ) {
        startGMDialog( st[1] );
      } else { // FIXME TODO ask whether to undelete
        new TopoDroidAlertDialog( this, getResources(),
                          getResources().getString( R.string.calib_gm_undelete ),
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int btn ) {
              // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "calib delite" );
              deleteGM( false );
            }
          }
        );
      }
    } catch ( NumberFormatException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "error: expected a long, got: " + st[0] );
    }
  }
 
  private void startGMDialog( String name  )
  {
     // String msg = mSaveTextView.getText().toString();
     // String[] st = msg.split( " ", 3 );
     // // TopoDroidLog.Log(  TopoDroidLog.LOG_CALIB, "TextItem: (" + st[0] + ") (" + st[1] + ") (" + st[2] + ")" );
     // mCIDid    = Long.parseLong(st[0]);
     // String name = st[1];
     // mSaveData = st[2];
     int end = name.length() - 1;
     name = name.substring(1,end);
     (new CalibGMDialog( this, this, name, mSaveData )).show();
  }

  // ---------------------------------------------------------------

  private Button[] mButton1;
  private int mNrButton1 = 7;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  boolean mEnableWrite;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.gm_activity);
    mApp = (TopoDroidApp) getApplication();

    mDataAdapter  = new CalibCBlockAdapter( this, R.layout.row, new ArrayList<CalibCBlock>() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // mHandler = new ConnHandler( mApp, this );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );
    // icons00   = ( TopoDroidSetting.mSizeButtons == 2 )? ixons : icons;
    // icons00no = ( TopoDroidSetting.mSizeButtons == 2 )? ixonsno : iconsno;

    mButton1 = new Button[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      // mButton1[k].setBackgroundResource( icons00[k] );
      BitmapDrawable bm2 = mApp.setButtonBackground( mButton1[k], size, izons[k] );
      if ( k == 1 ) {
        mBMtoggle = bm2;
      } else if ( k == 5 ) {
        mBMread = bm2;
      } else if ( k == 6 ) {
        mBMwrite = bm2;
      }
    }
    mBMtoggle_no = mApp.setButtonBackground( null, size, izonsno[1] );
    mBMread_no = mApp.setButtonBackground( null, size, izonsno[5] );
    mBMwrite_no = mApp.setButtonBackground( null, size, izonsno[6] );

    enableWrite( false );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mCalibName = mApp.myCalib;
    mAlgo = mApp.getCalibAlgoFromDB();
    // updateDisplay( );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    // mImage.setBackgroundResource( ( TopoDroidSetting.mSizeButtons == 2 )? R.drawable.ix_menu : R.drawable.ic_menu );
    mApp.setButtonBackground( mImage, size, R.drawable.iz_menu );
    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter();
    closeMenu();
    mMenu.setOnItemClickListener( this );
  }

  private void resetTitle()
  {
    setTitle( mCalibName );
    if ( mBlkStatus == 0 ) {
      setTitleColor( TopoDroidConst.COLOR_NORMAL );
    } else {
      setTitleColor( TopoDroidConst.COLOR_NORMAL2 );
    }
  }

  private void enableWrite( boolean enable ) 
  {
    mEnableWrite = enable;
    mButton1[6].setEnabled( enable );
    if ( enable ) {
      // mButton1[6].setBackgroundResource( icons00[6] );
      mButton1[6].setBackgroundDrawable( mBMwrite );
    } else {
      // mButton1[6].setBackgroundResource( icons00no[6] );
      mButton1[6].setBackgroundDrawable( mBMwrite_no );
    }
  }

  @Override
  public void enableButtons( boolean enable )
  {
    mButton1[1].setEnabled( enable );
    mButton1[5].setEnabled( enable );
    mButton1[6].setEnabled( enable && mEnableWrite );
    if ( enable ) {
      setTitleColor( TopoDroidConst.COLOR_CONNECTED );
      // mButton1[1].setBackgroundResource( icons00[1] );
      // mButton1[5].setBackgroundResource( icons00[5] );
      mButton1[1].setBackgroundDrawable( mBMtoggle );
      mButton1[5].setBackgroundDrawable( mBMread );
    } else {
      setTitleColor( TopoDroidConst.COLOR_CONNECTED );
      // mButton1[1].setBackgroundResource( icons00no[1] );
      // mButton1[5].setBackgroundResource( icons00no[5] );
      mButton1[1].setBackgroundDrawable( mBMtoggle_no );
      mButton1[5].setBackgroundDrawable( mBMread_no );
    }
    if ( enable && mEnableWrite ) {
      // mButton1[6].setBackgroundResource( icons00[6] );
      mButton1[6].setBackgroundDrawable( mBMwrite );
    } else {
      // mButton1[6].setBackgroundResource( icons00no[6] );
      mButton1[6].setBackgroundDrawable( mBMwrite_no );
    }
  }

    public void onClick(View view)
    {
      if ( onMenu ) {
        closeMenu();
        return;
      }

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

      if ( b == mButton1[0] ) { // download
        if ( ! mApp.checkCalibrationDeviceMatch() ) {
          Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
        } else {
          enableWrite( false );
          setTitleColor( TopoDroidConst.COLOR_CONNECTED );
          if ( mAlgo == 0 ) { // CALIB_ALGO_AUTO
            mAlgo = mApp.getCalibAlgoFromDevice();
            if ( mAlgo < 0 ) { // CALIB_ALGO_AUTO
              Toast.makeText( this, R.string.device_algo_failed, Toast.LENGTH_SHORT ).show();
              mAlgo = 1; // CALIB_ALGO_LINEAR
            }
            mApp.updateCalibAlgo( mAlgo );
          }
          ArrayList<ILister> listers = new ArrayList<ILister>();
          listers.add( this );
          new DistoXRefresh( mApp, listers ).execute();
        }
      } else if ( b == mButton1[1] ) { // toggle
        enableButtons( false );
        new CalibToggleTask( this, this, mApp ).execute();
      } else if ( b == mButton1[2] ) { // group
        if ( mApp.mCID >= 0 ) {
          List< CalibCBlock > list = mApp.mData.selectAllGMs( mApp.mCID, 0 );
          if ( list.size() >= 16 ) {
            setTitle( R.string.calib_compute_groups );
            setTitleColor( TopoDroidConst.COLOR_COMPUTE );
            (new GMGroupsDialog( this, this, 
              ( TopoDroidSetting.mGroupBy == TopoDroidSetting.GROUP_BY_DISTANCE )?
                getResources().getString( R.string.group_policy_distance )
              : ( TopoDroidSetting.mGroupBy == TopoDroidSetting.GROUP_BY_FOUR )?
                getResources().getString( R.string.group_policy_four )
              : /* TopoDroidSetting.GROUP_BY_ONLY_16 */
                getResources().getString( R.string.group_policy_sixteen ) 
            )).show();
            // new CalibComputer( this, CALIB_COMPUTE_GROUPS ).execute();
          } else {
            resetTitle( );
            Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          }
        } else {
          resetTitle( );
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[3] ) { // cover
        Calibration calib = mApp.mCalibration;
        if ( calib != null ) {
          List< CalibCBlock > list = mApp.mData.selectAllGMs( mApp.mCID, 0 );
          if ( list.size() >= 16 ) {
            ( new CalibCoverage( this, list, calib ) ).show();
          } else {
            Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          }
        } else {
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[4] ) { // compute
        if ( mApp.mCID >= 0 ) {
          setTitle( R.string.calib_compute_coeffs );
          setTitleColor( TopoDroidConst.COLOR_COMPUTE );
          if ( mAlgo == 0 ) { // CALIB_ALGO_AUTO
            mAlgo = ( TopoDroidSetting.mCalibAlgo != 0 ) ? TopoDroidSetting.mCalibAlgo : 1; // CALIB_AUTO_LINEAR
            mApp.updateCalibAlgo( mAlgo );
          }
          new CalibComputer( this, CALIB_COMPUTE_CALIB ).execute();
        } else {
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[5] ) { // read
        enableButtons( false );
        new CalibReadTask( this, this, mApp ).execute();

      } else if ( b == mButton1[6] ) { // write
        // if ( mEnableWrite ) {
          if ( mApp.mCalibration == null ) {
            Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
          } else {
            setTitle( R.string.calib_write_coeffs );
            setTitleColor( TopoDroidConst.COLOR_CONNECTED );

            byte[] coeff = mApp.mCalibration.GetCoeff();
            if ( coeff == null ) {
              Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
            } else {
              mApp.uploadCalibCoeff( this, coeff );
            }
            resetTitle( );
          }
        // }
      // } else if ( b == mButton1[7] ) { // disto
      //   Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
      //   startActivity( deviceIntent );
      }
    }

  void computeGroups()
  {
    new CalibComputer( this, CALIB_COMPUTE_GROUPS ).execute();
  }

  void resetGroups()
  {
    new CalibComputer( this, CALIB_RESET_GROUPS ).execute();
  }

  // ------------------------------------------------------------------
  // LIFECYCLE
  //
  // onCreate --> onStart --> onResume
  //          --> onSaveInstanceState --> onPause --> onStop | drawing | --> onStart --> onResume
  //          --> onSaveInstanceState --> onPause [ off/on ] --> onResume
  //          --> onPause --> onStop --> onDestroy

  @Override
  public void onStart()
  {
    super.onStart();
    // setBTMenus( mApp.mBTAdapter.isEnabled() );
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // if ( mApp.mComm != null ) { mApp.mComm.resume(); }
    // Log.v( TopoDroidApp.TAG, "onResume ");
    updateDisplay( );
    // mApp.registerConnListener( mHandler );
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    // mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
  }


  // @Override
  // public synchronized void onStop()
  // { 
  //   super.onStop();
  // }

  // @Override
  // public synchronized void onDestroy() 
  // {
  //   super.onDestroy();
  // }

  // ------------------------------------------------------------------

  // public int downloadData()
  // {
  //   ArrayList<ILister> listers = new ArrayList<ILister>();
  //   listers.add( this );
  //   return mApp.downloadData( this );
  // }

  // public void makeNewCalib( String name, String date, String comment )
  // {
  //   long id = setCalibFromName( name );
  //   if ( id > 0 ) {
  //     mApp.mData.updateCalibDayAndComment( id, date, comment );
  //     setStatus( STATUS_GM );
  //     // updateDisplay( );
  //   }
  // }
 
  void updateGM( long value, String name )
  {
    mApp.mData.updateGMName( mCIDid, mApp.mCID, name );
    String id = (new Long(mCIDid)).toString();
    // CalibCBlock blk = mApp.mData.selectGM( mCIDid, mApp.mCID );
    mSaveCBlock.setGroup( value );

    // if ( mApp.mListRefresh ) {
    //   mDataAdapter.notifyDataSetChanged();
    // } else {
      mSaveTextView.setText( id + " <" + name + "> " + mSaveData );
      mSaveTextView.setTextColor( mSaveCBlock.color() );
      // mSaveTextView.invalidate();
      // updateDisplay( ); // FIXME
    // }
  }

  void deleteGM( boolean delete )
  {
    mApp.mData.deleteGM( mApp.mCID, mCIDid, delete );
    updateDisplay( );
  }

  // ---------------------------------------------------------
  /* MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIoptions = menu.add( R.string.menu_options );
    mMIdisplay = menu.add( R.string.menu_display );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIoptions.setIcon( R.drawable.ic_pref );
    mMIdisplay.setIcon( R.drawable.ic_logs );
    mMIhelp.setIcon( R.drawable.ic_help );

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
    } else if ( item == mMIdisplay  ) { // DISPLAY
      mBlkStatus = 1 - mBlkStatus;       // 0 --> 1;  1 --> 0
      updateDisplay( );
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 3 ) ).show();
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
    mMenuAdapter.add( res.getString( menus[1] ) );
    mMenuAdapter.add( res.getString( menus[2] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  public void setConnectionStatus( boolean connected )
  {
    /* nothing : GM data are downloaded only on-demand */
  }

  public void notifyDisconnected()
  {
  }

}
