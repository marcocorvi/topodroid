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
 */
package com.topodroid.DistoX;

// import java.io.IOException;
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
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.Menu;
import android.view.MenuItem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.util.Log;

public class GMActivity extends Activity
                        implements OnItemClickListener
                        , ILister
                        , IEnableButtons
                        , OnClickListener
{
  private TopoDroidApp mApp;

  private CalibAlgo mCalibration = null;

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

  private static int izons[] = { 
                        R.drawable.iz_toggle,
                        R.drawable.iz_bt,
                        R.drawable.iz_download,
                        R.drawable.iz_numbers_no,
                        R.drawable.iz_compute,
                        R.drawable.iz_cover,
                        R.drawable.iz_read,
                        R.drawable.iz_write
                     };
  final static int BTN_TOGGLE   = 0;
  final static int BTN_BT       = 1;
  final static int BTN_DOWNLOAD = 2;
  final static int BTN_GROUP    = 3;
  final static int BTN_COMPUTE  = 4;
  final static int BTN_COVER    = 5;
  final static int BTN_READ     = 6;
  final static int BTN_WRITE    = 7;

  static int izonsno[] = { 
                        R.drawable.iz_toggle_no,
                        R.drawable.iz_bt_no,
                        R.drawable.iz_download_on,
                        0,
                        0,
                        R.drawable.iz_cover_no,
                        R.drawable.iz_read_no,
                        R.drawable.iz_write_no
                     };

  static int menus[] = {
                        R.string.menu_display,
                        R.string.menu_validate,
                        R.string.menu_options, 
                        R.string.menu_help
                     };

  static int help_icons[] = { 
                        R.string.help_toggle,
                        R.string.help_bluetooth,
                        R.string.help_download,
                        R.string.help_group,
                        R.string.help_compute,
                        R.string.help_cover,
                        R.string.help_read,
                        R.string.help_write
                      };
  static int help_menus[] = { 
                        R.string.help_display_calib,
                        R.string.help_validate,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  static int mNrButton1 = 0;
  private Button[]     mButton1;
  HorizontalListView   mListView;
  HorizontalButtonView mButtonView1;
  boolean mEnableWrite;
  ListView   mMenu;
  Button     mImage;
  // HOVER
  // MyMenuAdapter mMenuAdapter;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  BitmapDrawable mBMtoggle;
  BitmapDrawable mBMtoggle_no;
  BitmapDrawable mBMcover    = null;
  BitmapDrawable mBMcover_no = null;
  BitmapDrawable mBMread     = null;
  BitmapDrawable mBMread_no  = null;
  BitmapDrawable mBMwrite    = null;
  BitmapDrawable mBMwrite_no = null;
  BitmapDrawable mBMdownload;
  BitmapDrawable mBMdownload_on;
  BitmapDrawable mBMbluetooth;
  BitmapDrawable mBMbluetooth_no;

  public void setTheTitle() { }

  // -------------------------------------------------------------------
  // forward survey name to DeviceHelper

  // -------------------------------------------------------------

  /** called by CalibComputer Task
   * @return nr of iterations (neg. error)
   * @note run on an AsyncTask
   */
  int computeCalib()
  {
    long cid = mApp.mCID;
    if ( cid < 0 ) return -2;
    List<CalibCBlock> list = mApp.mDData.selectAllGMs( cid, 0 ); 
    if ( list.size() < 16 ) {
      return -1;
    }
    int ng = 0; // cb with group
    for ( CalibCBlock cb : list ) {
      if ( cb.mGroup > 0 ) ++ng;
    }
    if ( ng < 16 ) {
      return -3;
    }
    switch ( mAlgo ) {
      case CalibInfo.ALGO_NON_LINEAR:
        mCalibration = new CalibAlgoBH( 0, true );
        // FIXME set the calibration algorithm (whether non-linear or linear)
        // mCalibration.setAlgorith( mAlgo == 2 ); // CALIB_AUTO_NON_LINEAR
        break;
      case CalibInfo.ALGO_MINIMUM:
        if ( TDLevel.overTester ) {
          mCalibration = new CalibAlgoMin( 0, false );
          break;
        }
      default: // linear algo
        mCalibration = new CalibAlgoBH( 0, false );
    }

    mCalibration.Reset( list.size() );
    for ( CalibCBlock item : list ) mCalibration.AddValues( item );
    
    int iter = mCalibration.Calibrate();
    if ( iter > 0 && iter < TDSetting.mCalibMaxIt ) {
      float[] errors = mCalibration.Errors();
      for ( int k = 0; k < list.size(); ++k ) {
        CalibCBlock cb = list.get( k );
        mApp.mDData.updateGMError( cb.mId, cid, errors[k] );
        // cb.setError( errors[k] );
      }

      byte[] coeff = mCalibration.GetCoeff();
      mApp.mDData.updateCalibCoeff( cid, CalibAlgo.coeffToString( coeff ) );
      mApp.mDData.updateCalibError( cid, 
             mCalibration.Delta(),
             mCalibration.Delta2(),
             mCalibration.MaxError(),
             iter );

      // DEBUG:
      // Calibration.logCoeff( coeff );
      // coeff = Calibration.stringToCoeff( mApp.mDData.selectCalibCoeff( cid ) );
      // Calibration.logCoeff( coeff );
    }
    // Log.v( TopoDroidApp.TAG, "iteration " + iter );
    return iter;
  }

  /** validate this calibration against another calibration
   */
  void validateCalibration( String name )
  {
    String device = mApp.distoAddress();
    if ( device == null ) return;
    Long cid = mApp.mDData.getCalibCID( name, device );
    if ( cid < 0 ) {
      return;
    }
    List<CalibCBlock> list  = mApp.mDData.selectAllGMs( mApp.mCID, 0 );
    List<CalibCBlock> list1 = mApp.mDData.selectAllGMs( cid, 0 );
    // list.addAll( list1 );
    int size  = list.size();
    int size1 = list1.size();
    if ( size < 16 || size1 < 16 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      return;
    }

    String coeffStr = mApp.mDData.selectCalibCoeff( cid );
    int algo = mApp.mDData.selectCalibAlgo( cid );
    if ( algo == 0 ) algo = mApp.getCalibAlgoFromDevice();
    CalibAlgo calib1 = null;
    switch ( algo ) {
      case CalibInfo.ALGO_NON_LINEAR:
        calib1 = new CalibAlgoBH( CalibAlgo.stringToCoeff( coeffStr ), true );
        break;
      case CalibInfo.ALGO_MINIMUM:
        if ( TDLevel.overTester ) {
          calib1 = new CalibAlgoMin( CalibAlgo.stringToCoeff( coeffStr ), false );
          break;
        }
      default:
        calib1 = new CalibAlgoBH( CalibAlgo.stringToCoeff( coeffStr ), false );
    }
    // Log.v("DistoX", "Calib-1 algo " + algo );
    // calib1.dump();

    coeffStr = mApp.mDData.selectCalibCoeff( mApp.mCID );
    algo = mApp.mDData.selectCalibAlgo( mApp.mCID );
    if ( algo == 0 ) algo = mApp.getCalibAlgoFromDevice();
    CalibAlgo calib0 = null;
    switch ( algo ) {
      case CalibInfo.ALGO_NON_LINEAR:
        calib0 = new CalibAlgoBH( CalibAlgo.stringToCoeff( coeffStr ), true );
        break;
      case CalibInfo.ALGO_MINIMUM:
        if ( TDLevel.overTester ) {
          calib0 = new CalibAlgoMin( CalibAlgo.stringToCoeff( coeffStr ), false );
          break;
        }
      default:
        calib0 = new CalibAlgoBH( CalibAlgo.stringToCoeff( coeffStr ), false );
    }
    // Log.v("DistoX", "Calib-0 algo " + algo );
    // calib0.dump();

    float[] errors0 = new float[ list.size() ]; 
    float[] errors1 = new float[ list1.size() ]; 
    int ke1 = computeErrorStats( calib0, list1, errors1 );
    int ke0 = computeErrorStats( calib1, list,  errors0 );
    double ave0 = calib0.getStatError() / calib0.getStatCount();
    double std0 = Math.sqrt( calib0.getStatError2() / calib0.getStatCount() - ave0 * ave0 + 1e-8 );
    double ave1 = calib1.getStatError() / calib1.getStatCount();
    double std1 = Math.sqrt( calib1.getStatError2() / calib1.getStatCount() - ave1 * ave1 + 1e-8 );
    ave0 *= TDMath.RAD2DEG;
    std0 *= TDMath.RAD2DEG;
    ave1 *= TDMath.RAD2DEG;
    std1 *= TDMath.RAD2DEG;

    list.addAll( list1 );
    size = list.size();

    float[] errors = new float[ size ];
    double err1   = 0; // average error [radians]
    double err2   = 0;
    double errmax = 0;
    int ke = 0;
    for ( CalibCBlock b : list ) {
      Vector g = new Vector( b.gx, b.gy, b.gz );
      Vector m = new Vector( b.mx, b.my, b.mz );
      Vector v0 = calib0.computeDirection(g,m);
      Vector v1 = calib1.computeDirection(g,m);
      double err = v0.minus( v1 ).Length();
      errors[ke++] = (float) err;
      err1 += err;
      err2 += err * err;
      if ( err > errmax ) errmax = err;
    }
    err1 /= size;
    err2 = Math.sqrt( err2/size - err1 * err1 );
    err1 *= TDMath.RAD2DEG;
    err2 *= TDMath.RAD2DEG;
    errmax *= TDMath.RAD2DEG;
    new CalibValidateResultDialog( this, errors0, errors1, errors,
                                   ave0, std0, ave1, std1, err1, err2, errmax, name, mApp.myCalib ).show();
  }

  /** compute the error stats of the data of this calibration using the 
   * coeffiecients of another calibration
   * @param  calib    calibration (algorithm)
   * @param  list     calibration data
   * @param  errors   [output] errors 
   * @return number of errors in the array
   */
  private int computeErrorStats( CalibAlgo calib, List<CalibCBlock> list, float[] errors )
  {
    int ke = 0; // number of errors
    for ( int c=0; c<errors.length; ++c ) errors[c] = -1;

    calib.initErrorStats();
    long group = 0;
    int k = 0;
    int cnt = 0;
    while( k < list.size() && list.get(k).mGroup <= 0 ) ++k;
    
    for ( int j=k; j<list.size(); ++j ) {
      if ( list.get(j).mGroup > 0 ) {
        if ( list.get(j).mGroup != group ) {
          if ( cnt > 0 ) {
            Vector g[] = new Vector[cnt];
            Vector m[] = new Vector[cnt];
            float  e[] = new float[cnt];
            int i=0;
            for ( ; k<j; ++k ) {
              CalibCBlock b = list.get(k);
              if ( b.mGroup == group ) {
                g[i] = new Vector( b.gx, b.gy, b.gz );
                m[i] = new Vector( b.mx, b.my, b.mz );
                e[i] = -1;
                ++i;
              }
            }
            calib.addStatErrors( g, m, e );
            for ( int c=0; c<cnt; ++c ) errors[ ke++ ] = e[c];
          }
          group = list.get(j).mGroup;
          cnt = 1;
        } else { 
          cnt ++;
        }
      }
    } 
    if ( cnt > 0 ) {
      Vector g[] = new Vector[cnt];
      Vector m[] = new Vector[cnt];
      float  e[] = new float[cnt];
      int i=0;
      for ( ; k<list.size(); ++k ) {
        CalibCBlock b = list.get(k);
        if ( b.mGroup == group ) {
          g[i] = new Vector( b.gx, b.gy, b.gz );
          m[i] = new Vector( b.mx, b.my, b.mz );
          e[i] = -1;
          ++i;
        }
      }
      calib.addStatErrors( g, m, e );
      for ( int c=0; c<cnt; ++c ) errors[ ke++ ] = e[c];
    }
    return ke;
  }


  void handleComputeCalibResult( int job, int result )
  {
    if ( ! mApp.mGMActivityVisible ) return;
    switch ( job ) {
      case CalibComputer.CALIB_COMPUTE_CALIB:
        resetTitle( );
        // Log.v("DistoX", "compute result " + result );
        if ( result >= TDSetting.mCalibMaxIt ) {
          Toast.makeText( this, R.string.few_iter, Toast.LENGTH_SHORT ).show();
          return;
        } else if ( result > 0 ) {
          boolean saturated = mCalibration.hasSaturatedCoeff();
          if ( saturated ) {
            Toast.makeText( this, "WARNING. Saturated calibration coefficients", Toast.LENGTH_SHORT).show();
          }
          // enableWrite( ! saturated );
          enableWrite( true );
          Vector bg = mCalibration.GetBG();
          Matrix ag = mCalibration.GetAG();
          Vector bm = mCalibration.GetBM();
          Matrix am = mCalibration.GetAM();
          Vector nL = mCalibration.GetNL();
          byte[] coeff = mCalibration.GetCoeff();
          float[] errors = mCalibration.Errors();

          (new CalibCoeffDialog( this, mApp, bg, ag, bm, am, nL, errors,
                                 mCalibration.Delta(), mCalibration.Delta2(), mCalibration.MaxError(), 
                                 result, coeff /* , saturated */ ) ).show();
        } else if ( result == 0 ) {
          Toast.makeText( this, R.string.few_iter, Toast.LENGTH_SHORT ).show();
          return;
        } else if ( result == -1 ) {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          return;
        } else if ( result == -2 ) {
          return;
        } else if ( result == -3 ) {
          Toast.makeText( this, R.string.few_groups, Toast.LENGTH_SHORT ).show();
          return;
        } else {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          return;
        }
        break;
      case CalibComputer.CALIB_RESET_GROUPS:
        break;
      case CalibComputer.CALIB_COMPUTE_GROUPS:
      case CalibComputer.CALIB_RESET_AND_COMPUTE_GROUPS:
        if ( result < 0 ) {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
        } else {
          Toast.makeText( this, "Found " + result + " groups", Toast.LENGTH_SHORT ).show();
        }
        break;
      default:
    }
    updateDisplay( );

  }

  /** called by CalibComputer Task
   * @param start_id id of the GM-data to start with
   * @note run on an AsyncTask
   */
  void doResetGroups( long start_id )
  {
    // Log.v("DistoX", "Reset CID " + mApp.mCID + " from gid " + start_id );
    mApp.mDData.resetAllGMs( mApp.mCID, start_id ); // reset all groups where status=0, and id >= start_id
  }

  /** called by CalibComputer Task
   * @note run on an AsyncTask
   */
  int doComputeGroups( long start_id )
  {
    long cid = mApp.mCID;
    // Log.v("DistoX", "Compute CID " + cid + " from gid " + start_id );
    if ( cid < 0 ) return -2;
    float thr = TDMath.cosd( TDSetting.mGroupDistance );
    List<CalibCBlock> list = mApp.mDData.selectAllGMs( cid, 0 );
    if ( list.size() < 4 ) {
      return -1;
    }
    long group = 0;
    int cnt = 0;
    float b = 0.0f;
    float c = 0.0f;
    if ( start_id >= 0 ) {
      for ( CalibCBlock item : list ) {
        if ( item.mId == start_id ) {
          group = item.mGroup;
          cnt = 1;
          b = item.mBearing;
          c = item.mClino;
          break;
        }
      }
    } else {
      if ( TDSetting.mGroupBy != TDSetting.GROUP_BY_DISTANCE ) {
        group = 1;
      }
    }
    switch ( TDSetting.mGroupBy ) {
      case TDSetting.GROUP_BY_DISTANCE:
        for ( CalibCBlock item : list ) {
          if ( start_id >= 0 && item.mId <= start_id ) continue;
          if ( group == 0 || item.isFarFrom( b, c, thr ) ) {
            ++ group;
            b = item.mBearing;
            c = item.mClino;
          }
          item.setGroup( group );
          mApp.mDData.updateGMName( item.mId, item.mCalibId, Long.toString( item.mGroup ) );
          // N.B. item.calibId == cid
        }
        break;
      case TDSetting.GROUP_BY_FOUR:
        // TDLog.Log( TDLog.LOG_CALIB, "group by four");
        for ( CalibCBlock item : list ) {
          if ( start_id >= 0 && item.mId <= start_id ) continue;
          item.setGroupIfNonZero( group );
          mApp.mDData.updateGMName( item.mId, item.mCalibId, Long.toString( item.mGroup ) );
          ++ cnt;
          if ( (cnt%4) == 0 ) {
            ++group;
            // TDLog.Log( TDLog.LOG_CALIB, "cnt " + cnt + " new group " + group );
          }
        }
        break;
      case TDSetting.GROUP_BY_ONLY_16:
        for ( CalibCBlock item : list ) {
          if ( start_id >= 0 && item.mId <= start_id ) continue;
          item.setGroupIfNonZero( group );
          mApp.mDData.updateGMName( item.mId, item.mCalibId, Long.toString( item.mGroup ) );
          ++ cnt;
          if ( (cnt%4) == 0 || cnt >= 16 ) ++group;
        }
        break;
    }
    return (int)group-1;
  }

  // -----------------------------------------------------------
  // ILister interface
  @Override
  public void refreshDisplay( int nr, boolean toast )
  {
    // Log.v( TopoDroidApp.TAG, "refreshDisplay nr " + nr );
    resetTitle( );
    if ( nr >= 0 ) {
      if ( nr > 0 ) updateDisplay( );
      if ( toast ) {
        Toast.makeText( this, 
          getResources().getQuantityString(R.plurals.read_calib_data, nr/2, nr/2, nr ),
          Toast.LENGTH_SHORT ).show();
      }
    } else if ( nr < 0 ) {
      if ( toast ) {
        // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, mApp.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
      }
    }
    mButton1[BTN_DOWNLOAD].setBackgroundDrawable( mBMdownload );
    // mButton1[BTN_BT].setBackgroundDrawable( mBMbluetooth );
    // mButton1[BTN_BT].setEnabled( true );
    // mButton1[BTN_TOGGLE].setEnabled( true );
    enableButtons( true );
  }
    
  @Override
  public void updateBlockList( CalibCBlock blk ) 
  { 
    if ( blk != null ) mDataAdapter.add( blk );
  }
  
  @Override
  public void updateBlockList( DBlock blk ) { }

  @Override
  public void updateBlockList( long blk_id ) 
  {
    updateBlockList( mApp.mDData.selectGM( blk_id, mApp.mCID ) );
  }

  @Override
  public void setRefAzimuth( float azimuth, long fixed_extend ) { }

  @Override
  public void setConnectionStatus( int status )
  {
    /* nothing : GM data are downloaded only on-demand */
  }

  // --------------------------------------------------------------

  public void updateDisplay( )
  {
    // Log.v( TopoDroidApp.TAG, "update Display CID " + mApp.mCID );
    resetTitle( );
    mDataAdapter.clear();
    if ( mApp.mDData != null && mApp.mCID >= 0 ) {
      List<CalibCBlock> list = mApp.mDData.selectAllGMs( mApp.mCID, mBlkStatus );
      // Log.v( TopoDroidApp.TAG, "update Display GMs " + list.size() );
      updateGMList( list );
      setTitle( mCalibName );
    }
  }

  private void updateGMList( List<CalibCBlock> list )
  {
    int n_saturated = 0;
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_gms, Toast.LENGTH_SHORT ).show();
      return;
    }
    for ( CalibCBlock item : list ) {
      if ( item.isSaturated() ) ++ n_saturated;
      mDataAdapter.add( item );
    }
    // mList.setAdapter( mDataAdapter );
    if ( n_saturated > 0 ) {
      Toast toast = Toast.makeText( this, 
        getResources().getQuantityString( R.plurals.calib_saturated_values, n_saturated, n_saturated), Toast.LENGTH_LONG );
      toast.getView().setBackgroundColor( TDColor.BROWN );
      toast.show();
    }
  }


  // ---------------------------------------------------------------
  // list items click


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

    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    // TDLog.Log(  TDLog.LOG_INPUT, "GMActivity onItemClick() " + item.toString() );

    // if ( value.equals( getResources().getString( R.string.back_to_calib ) ) ) {
    //   setStatus( TDStatus.CALIB );
    //   updateDisplay( );
    //   return;
    // }
    mSaveCBlock   = mDataAdapter.get( pos );
    mSaveTextView = (TextView) view;
    String msg = mSaveTextView.getText().toString();
    String[] st = msg.split( " ", 3 );
    try {    
      mCIDid = Long.parseLong(st[0]);
      // String name = st[1];
      mSaveData = st[2];
      if ( mSaveCBlock.mStatus == 0 ) {
        // startGMDialog( mCIDid, st[1] );
        (new CalibGMDialog( this, this, mSaveCBlock )).show();
      } else { // FIXME TODO ask whether to undelete
        TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.calib_gm_undelete,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int btn ) {
              // TDLog.Log( TDLog.LOG_INPUT, "calib delite" );
              deleteGM( false );
            }
          }
        );
      }
    } catch ( NumberFormatException e ) {
      TDLog.Error( "error: expected a long, got: " + st[0] );
    }
  }
 
  // ---------------------------------------------------------------
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

    mNrButton1 = 5;
    if ( TDLevel.overBasic  ) mNrButton1 += 1; // COVER
    if ( TDLevel.overNormal ) mNrButton1 += 2; // READ WRITE
    Resources res = getResources();
    mButton1 = new Button[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( this, this, izons[k] );
    }
    mBMdownload     = MyButton.getButtonBackground( mApp, res, izons[BTN_DOWNLOAD] ); 
    mBMdownload_on  = MyButton.getButtonBackground( mApp, res, izonsno[BTN_DOWNLOAD] );
    mBMbluetooth    = MyButton.getButtonBackground( mApp, res, izons[BTN_BT] );
    mBMbluetooth_no = MyButton.getButtonBackground( mApp, res, izonsno[BTN_BT] );
    mBMtoggle       = MyButton.getButtonBackground( mApp, res, izons[BTN_TOGGLE] );
    mBMtoggle_no    = MyButton.getButtonBackground( mApp, res, izonsno[BTN_TOGGLE] );
    if ( TDLevel.overBasic ) {
      mBMcover        = MyButton.getButtonBackground( mApp, res, izons[BTN_COVER] );
      mBMcover_no     = MyButton.getButtonBackground( mApp, res, izonsno[BTN_COVER] );
    }
    if ( TDLevel.overNormal ) {
      mBMread         = MyButton.getButtonBackground( mApp, res, izons[BTN_READ] );
      mBMread_no      = MyButton.getButtonBackground( mApp, res, izonsno[BTN_READ] );
      mBMwrite        = MyButton.getButtonBackground( mApp, res, izons[BTN_WRITE] );
      mBMwrite_no     = MyButton.getButtonBackground( mApp, res, izonsno[BTN_WRITE] );
    }

    enableWrite( false );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mCalibName = mApp.myCalib;
    mAlgo = mApp.getCalibAlgoFromDB();
    // updateDisplay( );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );
    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter( res );
    closeMenu();
    // HOVER
    mMenu.setOnItemClickListener( this );
  }

  private void resetTitle()
  {
    setTitle( mCalibName );
    if ( mBlkStatus == 0 ) {
      setTitleColor( TDColor.NORMAL );
    } else {
      setTitleColor( TDColor.NORMAL2 );
    }
  }

  private void enableWrite( boolean enable ) 
  {
    mEnableWrite = enable;
    if ( TDLevel.overBasic ) {
      mButton1[BTN_COVER].setEnabled( enable );
      mButton1[BTN_COVER].setBackgroundDrawable( ( enable ? mBMcover : mBMcover_no ) );
    }
    if ( TDLevel.overNormal ) {
      mButton1[BTN_WRITE].setEnabled( enable );
      mButton1[BTN_WRITE].setBackgroundDrawable( ( enable ? mBMwrite : mBMwrite_no ) );
    }
  }

  @Override
  public void enableButtons( boolean enable )
  {
    boolean enable2 = enable && mEnableWrite;

    mButton1[BTN_TOGGLE].setEnabled( enable );
    mButton1[BTN_BT].setEnabled( enable );
    if ( TDLevel.overBasic ) {
      mButton1[BTN_COVER].setEnabled( enable2 );
      mButton1[BTN_COVER].setBackgroundDrawable( ( enable2 ? mBMcover : mBMcover_no ) );
    }
    if ( TDLevel.overNormal ) {
      mButton1[BTN_READ].setEnabled( enable );
      mButton1[BTN_READ].setBackgroundDrawable( ( enable ? mBMread : mBMread_no ) );
      mButton1[BTN_WRITE].setEnabled( enable2 );
      mButton1[BTN_WRITE].setBackgroundDrawable( ( enable2 ? mBMwrite : mBMwrite_no ) );
    }
    if ( enable ) {
      setTitleColor( TDColor.NORMAL );
      mButton1[BTN_TOGGLE].setBackgroundDrawable( mBMtoggle );
      mButton1[BTN_BT].setBackgroundDrawable( mBMbluetooth );
    } else {
      setTitleColor( TDColor.CONNECTED );
      mButton1[BTN_TOGGLE].setBackgroundDrawable( mBMtoggle_no );
      mButton1[BTN_BT].setBackgroundDrawable( mBMbluetooth_no );
    }
  }

  void doBluetooth( Button b )
  {
    if ( TDLevel.overAdvanced && mApp.distoType() == Device.DISTO_X310 ) {
      CutNPaste.showPopupBT( this, this, mApp, b, true );
    } else {
      mApp.resetComm();
      Toast.makeText( this, R.string.bt_reset, Toast.LENGTH_SHORT).show();
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

    if ( b == mButton1[BTN_TOGGLE] ) { // TOGGLE
      enableButtons( false );
      new CalibToggleTask( this, this, mApp ).execute();

    } else if ( b == mButton1[BTN_BT] ) { // BLUETOOTH
      doBluetooth( b );

    } else if ( b == mButton1[BTN_DOWNLOAD] ) { // DOWNLOAD
      if ( ! mApp.checkCalibrationDeviceMatch() ) {
        Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
      } else {
        enableWrite( false );
        setTitleColor( TDColor.CONNECTED );
        if ( mAlgo == CalibInfo.ALGO_AUTO ) { 
          mAlgo = mApp.getCalibAlgoFromDevice();
          if ( mAlgo < CalibInfo.ALGO_AUTO ) {
            Toast.makeText( this, R.string.device_algo_failed, Toast.LENGTH_SHORT ).show();
            mAlgo = CalibInfo.ALGO_LINEAR; 
          }
          mApp.updateCalibAlgo( mAlgo );
        }
        ListerHandler handler = new ListerHandler( this ); // FIXME LISTER
        new DataDownloadTask( mApp, handler ).execute();
        // new DataDownloadTask( mApp, this ).execute();
        mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMdownload_on );
        enableButtons( false );
      }

    } else if ( b == mButton1[BTN_GROUP] ) { // GROUP
      if ( mApp.mCID >= 0 ) {
        List< CalibCBlock > list = mApp.mDData.selectAllGMs( mApp.mCID, 0 );
        if ( list.size() >= 16 ) {
          (new GMGroupsDialog( this, this, 
            ( TDSetting.mGroupBy == TDSetting.GROUP_BY_DISTANCE )?
              getResources().getString( R.string.group_policy_distance )
            : ( TDSetting.mGroupBy == TDSetting.GROUP_BY_FOUR )?
              getResources().getString( R.string.group_policy_four )
            : /* TDSetting.GROUP_BY_ONLY_16 */
              getResources().getString( R.string.group_policy_sixteen ) 
          )).show();
          // new CalibComputer( this, -1L, CalibComputer.CALIB_COMPUTE_GROUPS ).execute();
        } else {
          resetTitle( );
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
        }
      } else {
        resetTitle( );
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
      }

    } else if ( b == mButton1[BTN_COMPUTE] ) { // COMPUTE
      if ( mApp.mCID >= 0 ) {
        setTitle( R.string.calib_compute_coeffs );
        setTitleColor( TDColor.COMPUTE );
        if ( mAlgo == CalibInfo.ALGO_AUTO ) { 
          mAlgo = ( TDSetting.mCalibAlgo != CalibInfo.ALGO_AUTO ) ? TDSetting.mCalibAlgo : CalibInfo.ALGO_LINEAR;
          mApp.updateCalibAlgo( mAlgo );
        }
        new CalibComputer( this, -1L, CalibComputer.CALIB_COMPUTE_CALIB ).execute();
      } else {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
      }

    } else if ( TDLevel.overBasic && b == mButton1[BTN_COVER] ) { // COVER
      if ( mCalibration == null ) {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT ).show();
      } else {
        List< CalibCBlock > list = mApp.mDData.selectAllGMs( mApp.mCID, 0 );
        if ( list.size() >= 16 ) {
          ( new CalibCoverageDialog( this, list, mCalibration ) ).show();
        } else {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
        }
      }

    } else if ( TDLevel.overNormal && b == mButton1[BTN_READ] ) { // READ
      enableButtons( false );
      new CalibReadTask( this, this, mApp, CalibReadTask.PARENT_GM ).execute(); // 

    } else if (TDLevel.overNormal &&  b == mButton1[BTN_WRITE] ) { // WRITE
      // if ( mEnableWrite ) {
        if ( mCalibration == null ) {
          Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
        } else {
          setTitle( R.string.calib_write_coeffs );
          setTitleColor( TDColor.CONNECTED );

          byte[] coeff = mCalibration.GetCoeff();
          if ( coeff == null ) {
            Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
          } else {
            mApp.uploadCalibCoeff( this, coeff, true, b );
          }
          resetTitle( );
        }
      // }
    // } else if ( b == mButton1[BTN_DISTO] ) { // disto
    //   Intent deviceIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DeviceActivity.class );
    //   startActivity( deviceIntent );
    }
  }

  void computeGroups( long start_id )
  {
    setTitle( R.string.calib_compute_groups );
    setTitleColor( TDColor.COMPUTE );
    new CalibComputer( this, start_id, CalibComputer.CALIB_COMPUTE_GROUPS ).execute();
  }

  void resetGroups( long start_id )
  {
    new CalibComputer( this, start_id, CalibComputer.CALIB_RESET_GROUPS ).execute();
  }

  void resetAndComputeGroups( long start_id )
  {
    // if ( ! mApp.mGMActivityVisible ) return;
    setTitle( R.string.calib_compute_groups );
    setTitleColor( TDColor.COMPUTE );
    new CalibComputer( this, start_id, CalibComputer.CALIB_RESET_AND_COMPUTE_GROUPS ).execute();
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
    mApp.mGMActivityVisible = true;
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    mApp.mGMActivityVisible = false;
    // mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
  }


  @Override
  public synchronized void onStop()
  { 
    super.onStop();
    // Log.v("DistoX", "GM Activity stop");
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    // Log.v("DistoX", "GM Activity destroy");
  }

  // ------------------------------------------------------------------

  // public int downloadDataBatch()
  // {
  //   ArrayList<ILister> listers = new ArrayList<>();
  //   listers.add( this );
  //   return mApp.downloadDataBatch( this );
  // }

  // public void makeNewCalib( String name, String date, String comment )
  // {
  //   long id = setCalibFromName( name );
  //   if ( id > 0 ) {
  //     mApp.mDData.updateCalibDayAndComment( id, date, comment );
  //     setStatus( TDStatus.GM );
  //     // updateDisplay( );
  //   }
  // }
 
  void updateGM( long value, String name )
  {
    mApp.mDData.updateGMName( mCIDid, mApp.mCID, name );
    String id = Long.toString(mCIDid);
    // CalibCBlock blk = mApp.mDData.selectGM( mCIDid, mApp.mCID );
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
    mApp.mDData.deleteGM( mApp.mCID, mCIDid, delete );
    updateDisplay( );
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
        String help_page = getResources().getString( R.string.GMActivity );
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
    mMenuAdapter = new ArrayAdapter<>(this, R.layout.menu );

    mMenuAdapter.add( res.getString( menus[0] ) );
    if ( TDLevel.overAdvanced ) mMenuAdapter.add( res.getString( menus[1] ) );
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
    int p = 0;
    if ( p++ == pos ) { // DISPLAY
      mBlkStatus = 1 - mBlkStatus;       // 0 --> 1;  1 --> 0
      updateDisplay( );
    } else if ( TDLevel.overAdvanced && p++ == pos ) { // VALIDATE
      List< String > list = mApp.mDData.selectDeviceCalibs( mApp.mDevice.mAddress );
      for ( String str : list ) {
        int len = str.indexOf(' ');
        if ( mApp.myCalib.equals( str.substring(0,len) ) ) {
          list.remove( str );
          break;
        }
      }
      if ( list.size() == 0 ) {
        Toast.makeText( this, R.string.few_calibs, Toast.LENGTH_SHORT ).show();
      } else {
        (new CalibValidateListDialog( this, this, list )).show();
      }
    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
      startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, menus.length ) ).show();
    }
  }

  // public void notifyDisconnected()
  // {
  // }

}
