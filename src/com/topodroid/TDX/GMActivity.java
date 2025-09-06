/* @file GMActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calibration data activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDLocale;
import com.topodroid.utils.TDUtil;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
import com.topodroid.dev.Device;
import com.topodroid.dev.DataType;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.distox2.DeviceX310TakeShot;
import com.topodroid.calib.CBlock;
import com.topodroid.calib.CBlockAdapter;
import com.topodroid.calib.CalibComputer;
import com.topodroid.calib.CalibCoverage;
import com.topodroid.calib.CalibInfo;
import com.topodroid.calib.CalibToggleTask;
import com.topodroid.calib.CalibReadTask;
import com.topodroid.calib.CalibCoeffDialog;
import com.topodroid.calib.CalibGMDialog;
import com.topodroid.calib.CalibCoverageDialog;
import com.topodroid.calib.CalibValidateListDialog;
import com.topodroid.calib.CalibValidateResultDialog;
import com.topodroid.calib.CalibTransform;
import com.topodroid.calib.CalibAlgo;
import com.topodroid.calib.CalibAlgoBH;
// import com.topodroid.calib.CalibAlgoMin;
import com.topodroid.calib.ICoeffDisplayer;
import com.topodroid.calib.GMGroupsDialog;
import com.topodroid.calib.GMSearchDialog;


import java.util.List;
// import java.util.LinkedList;
import java.util.ArrayList;
// import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;

// import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.DialogInterface;

import android.widget.ArrayAdapter;
// import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.LinearLayout;

import android.graphics.drawable.BitmapDrawable;

public class GMActivity extends Activity
                        implements ILister
                        , ICoeffDisplayer
                        , OnClickListener
                        , OnLongClickListener
                        , OnItemClickListener // only for the menus
{
  private TopoDroidApp mApp;
  private DeviceHelper mApp_mDData;
  private TDVector mBG1, mBM1, mNL1, mBG2, mBM2, mNL2;
  private TDMatrix mAG1, mAM1, mAG2, mAM2;

  public static boolean mGMActivityVisible = false;

  private CalibAlgo mCalibration  = null;
  private CalibAlgo mCalibration2 = null;
  private boolean   mTwoSensors   = false; // whether there is a second set of sensors

  private CBlock mSaveCBlock = null;  // data of the saved GM
  // private long mGMid = -1;     // id of the GM = mSaveCBlock.mId
  private int mBlkStatus = 0;   // min display Group (can be either 1 [only active] or 0 [all])
  private int mAlgo;            // calibration algorithm

  private ListView mList;                  // display list

  private CBlockAdapter mDataAdapter;  // adapter for the list of GM's

  private String mCalibName;
  // private ConnHandler mHandler;

  static final private int[] izons = {
                        R.drawable.iz_toggle,
                        R.drawable.iz_bt,
                        R.drawable.iz_download,
                        R.drawable.iz_numbers_no,
                        R.drawable.iz_compute,
                        R.drawable.iz_cover,
                        R.drawable.iz_read,
                        R.drawable.iz_write,
                        R.drawable.iz_search
                        // R.drawable.iz_empty // EMPTY
                     };
  final static private int BTN_TOGGLE    = 0;
  final static private int BTN_BLUETOOTH = 1;
  final static private int BTN_DOWNLOAD  = 2;
  final static private int BTN_GROUP     = 3;
  final static private int BTN_COMPUTE   = 4;
  final static private int BTN_COVER     = 5;
  final static private int BTN_READ      = 6;
  final static private int BTN_WRITE     = 7;
  final static private int BTN_SEARCH    = 8;

  static final private int[] izonsno = {
                        R.drawable.iz_toggle_no,
                        R.drawable.iz_bt_no,
                        R.drawable.iz_download_on,
                        0,
                        0,
                        0, // R.drawable.iz_cover_no,
                        R.drawable.iz_read_no,
                        R.drawable.iz_write_no,
                        R.drawable.iz_search
                     };

  static final private int[] menus = {
                        R.string.menu_show_deleted,
                        R.string.menu_validate,
                        R.string.menu_options, 
                        R.string.menu_help,
			R.string.menu_hide_deleted     // alternate 0
                        // , R.string.menu_calib_auto // AUTO-CALIB
                     };

  static final private int[] help_icons = {
                        R.string.help_toggle,
                        R.string.help_bluetooth,
                        R.string.help_download,
                        R.string.help_group,
                        R.string.help_compute,
                        R.string.help_cover,
                        R.string.help_read,
                        R.string.help_write,
                        R.string.help_search
                      };
  static final private int[] help_menus = {
                        R.string.help_display_calib,
                        R.string.help_validate,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  static final private int HELP_PAGE = R.string.GMActivity;

  static private int mNrButton1 = 0;
  private Button[]     mButton1;
  private MyHorizontalListView   mListView;
  private MyHorizontalButtonView mButtonView1;
  private boolean mEnableWrite;
  private ListView   mMenu;
  private Button     mImage;
  private boolean onMenu;

  private BitmapDrawable mBMtoggle;
  private BitmapDrawable mBMtoggle_no;
  private BitmapDrawable mBMcover    = null;
  // private BitmapDrawable mBMcover_no = null;
  private BitmapDrawable mBMread     = null;
  private BitmapDrawable mBMread_no  = null;
  private BitmapDrawable mBMwrite    = null;
  private BitmapDrawable mBMwrite_no = null;
  private BitmapDrawable mBMdownload;
  private BitmapDrawable mBMdownload_on;
  private BitmapDrawable mBMdownload_wait;
  private BitmapDrawable mBMbluetooth;
  private BitmapDrawable mBMbluetooth_no;

  private float mDeltaDirAve = 0;
  private float mDeltaDirStd = 0;
  private float mDeltaDirMax = 0;

  /** set the window title (empty method)
   */
  public void setTheTitle() { }

  /** @return the name (ILister interface)
   */
  public String name() { return "ShotWindow"; }

  // -------------------------------------------------------------------
  // forward survey name to DeviceHelper

  // -------------------------------------------------------------

  public float getDeltaDirAve() { return mDeltaDirAve; }
  public float getDeltaDirStd() { return mDeltaDirStd; }
  public float getDeltaDirMax() { return mDeltaDirMax; }

  // /** @return the calibration transform
  //  */
  // public CalibTransform getCalibration() { return mCalibration; }

  /** get the calibration algorithm
   * @return the index of the calibration algorithm
   */
  public int getAlgo() { return mAlgo; }

  // /** set whether there is a second set of sensors
  //  * @param b    whether there is a second set of sensors
  //  */
  // public void setSecondSensors( boolean b ) { mTwoSensors = b; }

  /** set the calibration algorithm
   * @param algo   index of the calibration algorithm
   */
  public void setAlgo( int algo ) { mAlgo = algo; }

  /** compute the calibration map
   * @return nr of iterations (neg. error)
   * @note run on an AsyncTask
   *       called by CalibComputer Task
   */
  public int computeCalib()
  {
    long cid = TDInstance.cid;
    if ( cid < 0 ) return -2;
    List< CBlock > list = mApp_mDData.selectAllGMs( cid, 0, false ); // include: normal status (0) but no negative-grp (false)
    if ( list.size() < 16 ) {
      return -1;
    }
    int ng = 0; // cb with group
    for ( CBlock cb : list ) {
      if ( cb.mGroup > 0 ) ++ng;
    }
    if ( ng < 16 ) {
      return -3;
    }
    // int off_group = CalibCoverage.evalShotDeviations( list, TDSetting.mGroupDistance );
    // if ( off_group > 0 ) {
    //   updateGMList( list );
    //   TDToast.makeBad( String.format( getResources().getString( R.string.calib_offgroup_data ), off_group ) );
    // }
    return doComputeCalib( list );
  }

  /** computation of the calibration map
   * @param list     list of calib data
   * @return nr of iterations (neg. error)
   */
  private int doComputeCalib( List< CBlock > list )
  {    
    long cid = TDInstance.cid;
    int algo = mTwoSensors ? CalibInfo.ALGO_LINEAR : mAlgo;
    switch ( algo ) {
      case CalibInfo.ALGO_AUTO:
        { // 2024-01-15 added
          int dev_algo = mApp.getCalibAlgoFromDevice();
          // TDLog.v("GM algo --> " + algo );
          if ( dev_algo == CalibInfo.ALGO_NON_LINEAR ) {
            mCalibration = new CalibAlgoBH( 0, true );
          } else {
            mCalibration = new CalibAlgoBH( 0, false );
          }
        }
        break;
      // app.setGMdownload( true ); // GM_DOWNLOAD
      case CalibInfo.ALGO_NON_LINEAR:
        mCalibration = new CalibAlgoBH( 0, true );
        // FIXME set the calibration algorithm (whether non-linear or linear)
        // mCalibration.setAlgorithm( algo == 2 ); // CALIB_AUTO_NON_LINEAR
        break;
      // case CalibInfo.ALGO_MINIMUM:
      //   if ( TDLevel.overTester ) {
      //     mCalibration = new CalibAlgoMin( 0, false );
      //     break;
      //   }
      default: // linear algo
        mCalibration = new CalibAlgoBH( 0, false );
    }

    mCalibration.Reset( list.size() );
    for ( CBlock item : list ) mCalibration.AddValues( item, false );
    // TDLog.v("Calib Data " + list.size() + " ok " + ng );

    int iter = mCalibration.Calibrate();
    // TDLog.v("Calib-1 Iter " + iter + " Two-sensors " + mTwoSensors );

    if ( mTwoSensors && iter > 0 && iter < TDSetting.mCalibMaxIt ) {
      mCalibration2 = new CalibAlgoBH( 0, false );
      mCalibration2.Reset( list.size() );
      for ( CBlock item : list ) mCalibration2.AddValues( item, true );
      int iter2 = mCalibration2.Calibrate();
      if ( iter2 > iter ) iter = iter2;
      // TDLog.v("Calib-2 Iter " + iter2 );
    }

    if ( iter > 0 && iter < TDSetting.mCalibMaxIt ) {
      mCalibration.rollDifference();  // FIXME ROLL_DIFFERENCE
      float[] errors = mCalibration.Errors();
      float deltaBH = mCalibration.DeltaBH();
      float delta   = mCalibration.Delta();
      float delta2  = mCalibration.Delta2();
      float maxErr  = mCalibration.MaxError();
      float dip     = mCalibration.Dip();
      float roll    = mCalibration.Roll();
      byte[] coeff  = mCalibration.GetCoeff(); // TWO_SENSORS this is 52 bytes, for the first sensor-set

      // TWO_SENSORS 
      // so far errors and other results are the "average/max" of the two calibrations 
      // calib coeffs are stored in a string which contains both sets if there are two sets
      if ( mTwoSensors && mCalibration2 != null ) {
        // TDLog.v("Calib delta  " + delta + " " + mCalibration2.Delta() );
        // TDLog.v("Calib stddev " + delta2 + " " + mCalibration2.Delta2() );
        // TDLog.v("Calib maxerr " + maxErr + " " + mCalibration2.MaxError() );
        // TDLog.v("Calib dip    " + dip + " " + mCalibration2.Dip() );
        mCalibration2.rollDifference();  // FIXME ROLL_DIFFERENCE

        // TWO_SENSORS calib data errors are the average of the errors on the two sensor-sets
        float[] errors2 = mCalibration2.Errors();
        for ( int k = 0; k < list.size(); ++k ) errors[k] = ( errors[k] + errors2[k] ) / 2;
        // TODO could use discrepancies ?
        byte[] coeff2 = mCalibration2.GetCoeff(); // 52 bytes for the second sensor-set
        if ( mCalibration2.DeltaBH() > deltaBH ) deltaBH = mCalibration2.DeltaBH();
        if ( mCalibration2.Delta()   > delta   ) delta   = mCalibration2.Delta();
        if ( mCalibration2.Delta2()  > delta2  ) delta2  = mCalibration2.Delta2();
        if ( mCalibration2.MaxError() > maxErr ) maxErr  = mCalibration2.MaxError();
        dip  = ( dip + mCalibration2.Dip() )/2;
        roll = ( roll + mCalibration2.Roll() )/2;
        mApp_mDData.updateCalibCoeff( cid, CalibAlgo.coeffToString( coeff, coeff2 ) );
        // compare the two calibrations on the data
        int nk = mCalibration.getDataNumber();
        assert( nk == mCalibration2.getDataNumber() );
        float a1 = 0;
        float a2 = 0;
        float am = 0;
        for ( int k = 0; k < nk; ++ k ) {
          TDVector v1 = mCalibration.getDirection( k ); 
          TDVector v2 = mCalibration2.getDirection( k ); 
          float v1_v2 = TDVector.dot_product( v1, v2 );
          if ( v1_v2 < 1 ) {
            float a = TDMath.acosd( v1_v2 );
            a1 += a;
            a2 += a * a;
            if ( a > am ) am = a;
          }
        }
        a1 /= nk;
        a2 = TDMath.sqrt(a2/nk - a1*a1);
        TDLog.v("Delta orientation average " + a1 + " stddev " + a2 + " max " + am + " N " + nk );
        mDeltaDirAve = a1;
        mDeltaDirStd = a2;
        mDeltaDirMax = am;
      } else {
        // TDLog.v("Calib delta  " + delta );
        // TDLog.v("Calib stddev " + delta2 );
        // TDLog.v("Calib maxerr " + maxErr );
        // TDLog.v("Calib dip    " + dip );
        mApp_mDData.updateCalibCoeff( cid, CalibAlgo.coeffToString( coeff, null ) );
      }
      for ( int k = 0; k < list.size(); ++k ) {
        CBlock cb = list.get( k );
        mApp_mDData.updateGMError( cb.mId, cid, errors[k] );
        // cb.setError( errors[k] );
      }

      // FIXME TWO_SENSORS should keep separated the two calibs results ?
      //                   this means seven new columnsto the calibs table
      mApp_mDData.updateCalibError( cid, deltaBH, delta, delta2, maxErr, dip, roll, iter );

      // DEBUG:
      // Calibration.logCoeff( coeff );
      // coeff = Calibration.stringToCoeff( mApp.mDData.selectCalibCoeff( cid ), 1 ); // ???
      // Calibration.logCoeff( coeff );
    }
    // TDLog.v( "iteration " + iter );
    return iter;
  }

  /** validate this calibration against another calibration
   * @param name   name of the other calibration
   * *-0 vars are computed on list0 using calib1, ie, the other calib on the data of this calib
   * *-1 vars are computed on list1 using calib0, iw, this calib on the data of the other calib
   * FIXME this must be upgraded to TWO_SENSORS
   */
  public void validateCalibration( String name )
  {
    String address = TDInstance.deviceAddress();
    if ( address == null ) return;
    long cid = mApp_mDData.getCalibCID( name, address );
    if ( cid < 0 ) {
      return;
    }
    List< CBlock > list0 = mApp_mDData.selectAllGMs( TDInstance.cid, 0, false ); // include: normal status (0) but no negative-grp (false)
    List< CBlock > list1 = mApp_mDData.selectAllGMs( cid, 0, false );
    int size0 = list0.size();
    int size1 = list1.size();
    if ( size0 < 16 || size1 < 16 ) {
      TDToast.makeBad( R.string.few_data );
      return;
    }

    String coeffStr = mApp_mDData.selectCalibCoeff( cid );
    int algo = mApp_mDData.selectCalibAlgo( cid );
    if ( algo == 0 ) algo = mApp.getCalibAlgoFromDevice();
    CalibAlgo calib1 = null;
    switch ( algo ) {
      case CalibInfo.ALGO_NON_LINEAR:
        calib1 = new CalibAlgoBH( CalibAlgo.stringToCoeff( coeffStr, 1 ), true ); // TODO TWO_SENSORS
        break;
      // case CalibInfo.ALGO_MINIMUM:
      //   if ( TDLevel.overTester ) {
      //     calib1 = new CalibAlgoMin( CalibAlgo.stringToCoeff( coeffStr, 1 ), false );
      //     break;
      //   }
      default:
        calib1 = new CalibAlgoBH( CalibAlgo.stringToCoeff( coeffStr, 1 ), false );
    }
    // TDLog.v( "Calib-1 algo " + algo );
    // calib1.dump();

    coeffStr = mApp_mDData.selectCalibCoeff( TDInstance.cid );
    algo = mApp_mDData.selectCalibAlgo( TDInstance.cid );
    if ( algo == 0 ) algo = mApp.getCalibAlgoFromDevice();
    CalibAlgo calib0 = null;
    switch ( algo ) {
      case CalibInfo.ALGO_NON_LINEAR:
        calib0 = new CalibAlgoBH( CalibAlgo.stringToCoeff( coeffStr, 1 ), true );
        break;
      // case CalibInfo.ALGO_MINIMUM:
      //   if ( TDLevel.overTester ) {
      //     calib0 = new CalibAlgoMin( CalibAlgo.stringToCoeff( coeffStr, 1 ), false );
      //     break;
      //   }
      default:
        calib0 = new CalibAlgoBH( CalibAlgo.stringToCoeff( coeffStr, 1 ), false );
    }
    // TDLog.v( "Calib-0 algo " + algo );
    // calib0.dump();

    float[] errors1 = new float[ size1 ]; 
    int ke1 = computeErrorStats( calib0, list1, errors1 );
    double ave1 = calib0.getStatError() / calib0.getStatCount();
    double std1 = Math.sqrt( calib0.getStatError2() / calib0.getStatCount() - ave1 * ave1 + 1e-8 );
    ave1 *= TDMath.RAD2DEG;
    std1 *= TDMath.RAD2DEG;

    float[] errors0 = new float[ size0 ]; 
    int ke0 = computeErrorStats( calib1, list0, errors0 );
    double ave0 = calib1.getStatError() / calib1.getStatCount();
    double std0 = Math.sqrt( calib1.getStatError2() / calib1.getStatCount() - ave0 * ave0 + 1e-8 );
    ave0 *= TDMath.RAD2DEG;
    std0 *= TDMath.RAD2DEG;

    list0.addAll( list1 );
    size0 = list0.size(); // size0 += size1;

    float[] errors = new float[ size0 ];
    double err1   = 0; // average error [radians]
    double err2   = 0;
    double errmax = 0;
    int ke = 0;
    for ( CBlock b : list0 ) {
      TDVector g = new TDVector( b.gx, b.gy, b.gz );
      TDVector m = new TDVector( b.mx, b.my, b.mz );
      TDVector v0 = calib0.computeDirection(g,m);
      TDVector v1 = calib1.computeDirection(g,m);
      double err = v0.minus( v1 ).length();
      errors[ke++] = (float) err;
      err1 += err;
      err2 += err * err;
      if ( err > errmax ) errmax = err;
      // b.computeBearingAndClino(); 
      // TDLog.v("COMPARE " + String.format("%.5f A %.2f C %.2f K %d", err, b.mBearing, b.mClino, ke ) );
    }
    err1 /= size0;
    err2 = Math.sqrt( err2/size0 - err1 * err1 );
    err1 *= TDMath.RAD2DEG;
    err2 *= TDMath.RAD2DEG;
    errmax *= TDMath.RAD2DEG;
    new CalibValidateResultDialog( this, errors0, errors1, errors,
                                   ave0, std0, ave1, std1, err1, err2, errmax, name, TDInstance.calib ).show();
  }

  /** compute the error stats of the data of this calibration using the coefficients of another calibration
   * @param  calib    calibration (algorithm)
   * @param  list     calibration data
   * @param  errors   [output] errors 
   * @return number of errors in the array
   */
  private int computeErrorStats( CalibAlgo calib, List< CBlock > list, float[] errors )
  {
    int ke = 0; // number of errors
    for ( int c=0; c<errors.length; ++c ) errors[c] = -1;

    calib.initErrorStats();
    long group = 0;
    int k = 0;
    int cnt = 0;
    while( k < list.size() && list.get(k).mGroup <= 0 ) ++k;
    
    for ( int j=k; j<list.size(); ++j ) {
      long jgroup = list.get(j).mGroup;
      if ( jgroup > 0 ) {
        // TDLog.v(" J " + j + "/" + list.size() + " group " + jgroup + " prev " + group );
        if ( jgroup != group ) {
          if ( cnt > 1 ) { // 2019.09.23 at least two data in the group
            // TDLog.v("Setting group " + group + " cnt " + cnt + " from " + k + " to " + j);
            TDVector[] g = new TDVector[cnt];
            TDVector[] m = new TDVector[cnt];
            float[]  e = new float[cnt];
            int i=0;
            for ( ; k<j; ++k ) {
              CBlock b = list.get(k);
              if ( b.mGroup == group ) {
                g[i] = new TDVector( b.gx, b.gy, b.gz );
                m[i] = new TDVector( b.mx, b.my, b.mz );
                e[i] = -1;
                ++i;
              }
            }
            calib.addStatErrors( g, m, e );
            for ( int c=0; c<cnt; ++c ) errors[ ke++ ] = e[c];
          }
          group = list.get(j).mGroup;
          cnt = 1;
          k = j;
        } else { 
          cnt ++;
        }
      }
    } 
    if ( cnt > 1 ) { // 2019.09.23 at least two data in the group
      TDVector[] g = new TDVector[cnt];
      TDVector[] m = new TDVector[cnt];
      float[]  e = new float[cnt];
      int i=0;
      for ( ; k<list.size(); ++k ) {
        CBlock b = list.get(k);
        if ( b.mGroup == group ) {
          g[i] = new TDVector( b.gx, b.gy, b.gz );
          m[i] = new TDVector( b.mx, b.my, b.mz );
          e[i] = -1;
          ++i;
        }
      }
      calib.addStatErrors( g, m, e );
      for ( int c=0; c<cnt; ++c ) errors[ ke++ ] = e[c];
    }
    return ke;
  }

  /** handle the result of a computation
   * @param job     computation task 
   * @param result  result of the computation
   */
  public void handleComputeCalibResult( int job, int result )
  {
    if ( ! mGMActivityVisible ) return;
    switch ( job ) {
      case CalibComputer.CALIB_COMPUTE_CALIB:
        resetTitle( );
        // TDLog.v( "handle compute result " + result );
        if ( result >= TDSetting.mCalibMaxIt ) {
          TDToast.makeBad( R.string.few_iter );
          return;
        } else if ( result > 0 ) {
          boolean saturated = mCalibration.hasSaturatedCoeff();
          if ( saturated ) {
            TDToast.makeBad( R.string.saturated_coeffs );
          }
          // enableWrite( ! saturated );
          enableWrite( true );
          byte[]  coeffs = null;
          byte[]  coeff1 = mCalibration.GetCoeff();
          float[] errors = mCalibration.Errors();

          if ( mCalibration2 != null ) {
            byte[] coeff2 = mCalibration2.GetCoeff();
            coeffs = new byte[104];
            System.arraycopy( coeff1, 0, coeffs, 0, 52 );
            System.arraycopy( coeff2, 0, coeffs, 52, 52 );
            float[] errors2 = mCalibration2.Errors();
            for ( int k = 0; k < errors.length; ++k ) errors[k] = ( errors[k] + errors2[k] )/2;
          } else {
            coeffs = new byte[52];
            System.arraycopy( coeff1, 0, coeffs, 0, 52 );
          }

          (new CalibCoeffDialog( this, this, errors, coeffs,
                                 mCalibration.DeltaBH(), mCalibration.Delta(), mCalibration.Delta2(), mCalibration.MaxError(), 
                                 result, mCalibration.Dip(), mCalibration.Roll() // FIXME ROLL_DIFFERENCE
                                 /* , saturated */ ) ).show();
        } else if ( result == 0 ) {
          TDToast.makeBad( R.string.few_iter );
          return;
        } else if ( result == -1 ) {
          TDToast.makeBad( R.string.few_data );
          return;
        } else if ( result == -2 ) {
          return;
        } else if ( result == -3 ) {
          TDToast.makeBad( R.string.few_groups );
          return;
        } else {
          TDToast.makeBad( R.string.few_data );
          return;
        }
        break;
      case CalibComputer.CALIB_RESET_GROUPS:
        break;
      case CalibComputer.CALIB_COMPUTE_GROUPS:
      case CalibComputer.CALIB_RESET_AND_COMPUTE_GROUPS:
        if ( result < 0 ) {
          TDToast.makeBad( R.string.few_data );
        } else {
          // TDToast.make( String.format( getResources().getString( R.string.found_groups ), result ) );
          TDToast.make( getResources().getQuantityString(R.plurals.found_calib_groups, result, result ) );
        }
        break;
      default:
    }
    updateDisplay( );

  }

  /** reset the group numbers
   * @param start_id id of the GM-data to start with
   * @note run on an AsyncTask
   *       called by CalibComputer Task
   */
  public void doResetGroups( long start_id )
  {
    // TDLog.v( "Reset CID " + TDInstance.cid + " from gid " + start_id );
    mApp_mDData.resetAllGMs( TDInstance.cid, start_id ); // reset all groups where status=0, and id >= start_id
  }

  /** assign group numbers to the calibration data
   * @param start_id  data id from which to start
   * @param policy    index of the group policy
   * @note run on an AsyncTask
   *       called by CalibComputer Task
   */
  public int doComputeGroups( long start_id, int policy )
  {
    long cid = TDInstance.cid;
    // TDLog.v( "Compute CID " + cid + " from gid " + start_id );
    if ( cid < 0 ) return -2;
    float thr = TDMath.cosd( TDSetting.mGroupDistance );
    List< CBlock > list = mApp_mDData.selectAllGMs( cid, 0, true ); // include: normal status (0) but also negative-grp (true)
    if ( list.size() < 4 ) {
      return -1;
    }
    long group = 0;
    int cnt = 0;
    float b = 0.0f;
    float c = 0.0f;
    if ( start_id >= 0 ) {
      for ( CBlock item : list ) {
        if ( item.mId == start_id ) {
          group = item.mGroup;
          cnt = 1;
          b = item.mBearing;
          c = item.mClino;
          break;
        }
      }
    } else {
      // if ( TDSetting.mGroupBy != TDSetting.GROUP_BY_DISTANCE ) { // DEPRECATED
      //   group = 1;
      // }
      group = 1;
    }
    switch ( policy ) {
      case TDSetting.GROUP_BY_DISTANCE: // DEPRECATED
      //   for ( CBlock item : list ) {
      //     if ( start_id >= 0 && item.mId <= start_id ) continue;
      //     if ( group == 0 || item.isFarFrom( b, c, thr ) ) {
      //       ++ group;
      //       b = item.mBearing;
      //       c = item.mClino;
      //     }
      //     item.setGroup( group );
      //     mApp_mDData.updateGMName( item.mId, item.mCalibId, Long.toString( item.mGroup ) );
      //     // N.B. item.calibId == cid
      //   }
        TDToast.makeBad( "GROUP BY DISTANCE IS NO LONGER SUPPORTED" );
        break;
      case TDSetting.GROUP_BY_FOUR:
        // TDLog.Log( TDLog.LOG_CALIB, "group by four");
        for ( CBlock item : list ) {
          if ( start_id >= 0 && item.mId <= start_id ) continue;
          item.setGroupIfNonZero( group );
          mApp_mDData.updateGMName( item.mId, item.mCalibId, Long.toString( item.mGroup ) );
          ++ cnt;
          if ( (cnt%4) == 0 ) {
            ++group;
            // TDLog.Log( TDLog.LOG_CALIB, "cnt " + cnt + " new group " + group );
          }
        }
        break;
      case TDSetting.GROUP_BY_ONLY_16:
        for ( CBlock item : list ) {
          if ( start_id >= 0 && item.mId <= start_id ) continue;
          item.setGroupIfNonZero( group );
          mApp_mDData.updateGMName( item.mId, item.mCalibId, Long.toString( item.mGroup ) );
          ++ cnt;
          if ( (cnt%4) == 0 || cnt >= 16 ) ++group;
        }
        break;
    }
    return (int)group-1;
  }

  // -----------------------------------------------------------
  // ILister interface

  /** refresh the display
   * @param nr    calibration result
   * @param toast whether to toast a message
   */
  @Override
  public void refreshDisplay( int nr, boolean toast )
  {
    // TDLog.v( "refreshDisplay nr " + nr );
    resetTitle( );
    if ( nr >= 0 ) {
      if ( TDInstance.isDeviceXBLE() ) nr *= 2; // GM_DOWNLOAD
      if ( nr > 0 ) updateDisplay( );
      if ( toast ) {
        TDToast.make( getResources().getQuantityString(R.plurals.read_calib_data, nr/2, nr/2, nr ) );
      }
    } else { // ( nr < 0 )
      if ( toast ) {
        // TDToast.makeBad( this, getString(R.string.read_fail_with_code) + nr );
        TDToast.makeBad( mApp.DistoXConnectionError[ -nr ] );
      }
    }
    TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload );
    // TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], mBMbluetooth );
    // mButton1[BTN_BLUETOOTH].setEnabled( true );
    // mButton1[BTN_TOGGLE].setEnabled( true );
    enableButtons( true );
  }

  /** update the list of calibration data adding a new data
   * @param blk    new calibration data
   */
  private void updateCBlockList( CBlock blk ) 
  { 
    if ( blk == null ) return;
    mDataAdapter.add( blk );
  }
  
  /** update the list of calibration data adding a new data
   * @param blk_id    ID of the new calibration data
   */
  @Override
  public void updateBlockList( long blk_id ) 
  {
    updateCBlockList( mApp_mDData.selectGM( blk_id, TDInstance.cid ) );
  }

  /** set the reference azimuth for survey data - empty method
   * @param azimuth      azimuth
   * @param fixed_extend whether the extend is kept fixed
   */
  @Override
  public void setRefAzimuth( float azimuth, long fixed_extend ) { }

  /** set the bluetooth connection status
   * @param status   connection status
   */
  @Override
  public void setConnectionStatus( int status )
  {
    // TDLog.v("GM set connection status: " + ConnectionState.statusStr[ status ] ); 
    switch ( status ) {
      case ConnectionState.CONN_CONNECTED:
        TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload_on );
        TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], (TDInstance.isDeviceBric() ? mBMbluetooth : mBMbluetooth_no ) );
        enableButtons( false );
        break;
      case ConnectionState.CONN_WAITING:
        TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload_wait );
        TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], (TDInstance.isDeviceBric() ? mBMbluetooth : mBMbluetooth_no ) );
        enableButtons( false );
        break;
      default:
        TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload );
        TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], mBMbluetooth );
        enableButtons( true );
    }
  }

  // --------------------------------------------------------------

  /** update the display
   */
  private void updateDisplay( )
  {
    // TDLog.v( "update Display CID " + TDInstance.cid );
    resetTitle( );
    mDataAdapter.clear();
    if ( mApp_mDData != null && TDInstance.cid >= 0 ) {
      List< CBlock > list = mApp_mDData.selectAllGMs( TDInstance.cid, mBlkStatus, true ); // include: status (mBlkStatus) but also negative-grp (true)
      // TDLog.v( "update Display GMs " + list.size() );
      updateGMList( list );
      setTitle( mCalibName );
    }
  }

  /** update the list of calibration data
   * @param list   list of calibration data
   */
  private void updateGMList( List< CBlock > list )
  {
    int n_saturated = 0;
    if ( TDUtil.isEmpty(list) ) {
      TDToast.makeBad( R.string.no_gms );
      return;
    }
    // float thr = TDMath.cosd( TDSetting.mGroupDistance );
    float thr = TDSetting.mGroupDistance;
    long group = 0;
    int i0 = 0;
    CBlock ref = null;
    int sz = list.size();
    for ( int i1 = 0; i1 < sz; ++i1 ) {
      CBlock b1 = list.get( i1 );
      if ( b1.isSaturated() ) ++ n_saturated;

      if ( b1.mGroup > 0 ) {
        if ( b1.mGroup != group ) {
          group = b1.mGroup;
	  ref   = b1;
          i0    = i1;
          b1.computeBearingAndClino();
          for (int i2 = i1+1; i2 < sz; ++i2 ) {
            CBlock b2 = list.get( i2 );
            if ( b2.mGroup == 0 ) continue;
            if ( b2.mGroup != group ) break;
            b2.computeBearingAndClino();
          }
        }
        float compass = b1.mBearing * TDMath.DEG2RAD;
        float clino   = b1.mClino   * TDMath.DEG2RAD;
        float h1 = TDMath.cos( clino );
        float z1 = TDMath.sin( clino );
        float n1 = h1 * TDMath.cos( compass );
        float e1 = h1 * TDMath.sin( compass );
        int cnt = 0;
        float dev = 0;
        for (int i2=i0; i2<sz; ++ i2 ) {
          if ( i2 == i1 ) continue;
          CBlock b2 = list.get( i2 );
          if ( b2.mGroup == 0 ) continue;
          if ( b2.mGroup != group ) break;
          compass = b2.mBearing * TDMath.DEG2RAD;
          clino   = b2.mClino   * TDMath.DEG2RAD;
          h1 = TDMath.cos( clino );
          dev += TDMath.acosd( z1 * TDMath.sin( clino ) + n1 * h1 * TDMath.cos( compass ) + e1 * h1 * TDMath.sin( compass ) );
          ++ cnt;
        }
        b1.setFarness( cnt > 0 && dev/cnt > thr );
      } else {
        b1.computeBearingAndClino();
        b1.setFarness( false );
      } 
          
      // } else if ( ref != null ) {
      //   item.computeFarness( ref, thr );
      // }	
      mDataAdapter.add( b1 );
    }
    // mList.setAdapter( mDataAdapter );
    if ( n_saturated > 0 ) {
      TDToast.makeBad( getResources().getQuantityString( R.plurals.calib_saturated_values, n_saturated, n_saturated) );
    }
  }


  // ---------------------------------------------------------------
  // list items click

  /** implements item click listener
   * @param parent container
   * @param view   clicked view
   * @param pos    index of the clicked view
   * @param id     ...
   */ 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( closeMenu() ) return;
    // nothing else
  }

  /** handle a user tap on the data values
   * @param pos   item position
   * @param cblk  data block
   */
  public void itemDataClick( int pos, CBlock cblk )
  {
    // TDLog.v("ITEM DATA click pos " + pos + " id " + cblk.mId );
    mSaveCBlock = cblk;
    // mGMid = cblk.mId;
    if ( mSaveCBlock.mStatus == 0 ) {
      (new CalibGMDialog( this, this, mSaveCBlock )).show();
    } else { // FIXME TODO ask whether to undelete
      TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.calib_gm_undelete,
        new DialogInterface.OnClickListener() {
          @Override public void onClick( DialogInterface dialog, int btn ) {
            // TDLog.Log( TDLog.LOG_INPUT, "calib delete" );
            deleteGM( false );
          }
        }
      );
    }
  }
 
  // ---------------------------------------------------------------
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

    TDandroid.setScreenOrientation( this );

    setContentView(R.layout.gm_activity);
    mApp = (TopoDroidApp) getApplication();
    mApp_mDData = TopoDroidApp.mDData;
    mTwoSensors = TDInstance.isDeviceTwoSensors();

    mDataAdapter  = new CBlockAdapter( this, this, R.layout.cblock_row, new ArrayList< CBlock >() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // mHandler = new ConnHandler( mApp, this );
    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlaceholder(true);
    /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    mNrButton1 = 5;
    if ( TDLevel.overBasic  )   mNrButton1 += 1; // COVER
    if ( TDLevel.overNormal )   mNrButton1 += 2; // READ WRITE
    if ( TDLevel.overAdvanced ) mNrButton1 += 1; // SEARCH
    Resources res = getResources();
    mButton1 = new Button[ mNrButton1+1 ];
    for ( int k=0; k < mNrButton1; ++k ) { // add also EMPTY button
      mButton1[k] = MyButton.getButton( this, this, izons[k] );
    }
    mButton1[mNrButton1] = MyButton.getButton( this, null, R.drawable.iz_empty );
    if ( TDLevel.overAdvanced ) {
      if ( TDInstance.deviceType() == Device.DISTO_X310 ) { // FIXME add other devices that support remote comtrol
        mButton1[ BTN_BLUETOOTH ].setOnLongClickListener( this );
      }
      mButton1[ BTN_SEARCH ].setOnLongClickListener( this );
    }

    mBMdownload      = MyButton.getButtonBackground( mApp, res, izons[BTN_DOWNLOAD] ); 
    mBMdownload_on   = MyButton.getButtonBackground( mApp, res, izonsno[BTN_DOWNLOAD] );
    mBMdownload_wait = MyButton.getButtonBackground( mApp, res, R.drawable.iz_download_wait );
    mBMbluetooth     = MyButton.getButtonBackground( mApp, res, izons[BTN_BLUETOOTH] );
    mBMbluetooth_no  = MyButton.getButtonBackground( mApp, res, izonsno[BTN_BLUETOOTH] );
    mBMtoggle        = MyButton.getButtonBackground( mApp, res, izons[BTN_TOGGLE] );
    mBMtoggle_no     = MyButton.getButtonBackground( mApp, res, izonsno[BTN_TOGGLE] );
    if ( TDLevel.overBasic ) {
      mBMcover        = MyButton.getButtonBackground( mApp, res, izons[BTN_COVER] );
      // mBMcover_no     = MyButton.getButtonBackground( mApp, res, izonsno[BTN_COVER] );
    }
    if ( TDLevel.overNormal ) {
      mBMread         = MyButton.getButtonBackground( mApp, res, izons[BTN_READ] );
      mBMread_no      = MyButton.getButtonBackground( mApp, res, izonsno[BTN_READ] );
      mBMwrite        = MyButton.getButtonBackground( mApp, res, izons[BTN_WRITE] );
      mBMwrite_no     = MyButton.getButtonBackground( mApp, res, izonsno[BTN_WRITE] );
    }

    enableWrite( false );

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mCalibName = TDInstance.calib;
    mAlgo = mApp.getCalibAlgoFromDB();
    // updateDisplay( );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );
    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );
  }

  /** reset the window title
   */
  private void resetTitle()
  {
    setTitle( mCalibName );
    if ( mBlkStatus == 0 ) {
      setTitleColor( TDColor.TITLE_NORMAL );
    } else {
      setTitleColor( TDColor.TITLE_NORMAL2 );
    }
  }

  /** enable or disable the button to write the calibration coefficients
   * @param enable   whether to enable or disable
   */
  private void enableWrite( boolean enable ) 
  {
    mEnableWrite = enable;
    // if ( TDLevel.overBasic ) {
    //   mButton1[BTN_COVER].setEnabled( enable );
    //   TDandroid.setButtonBackground( mButton1[BTN_COVER], ( enable ? mBMcover : mBMcover_no ) );
    // }
    if ( TDLevel.overNormal ) {
      mButton1[BTN_WRITE].setEnabled( enable );
      TDandroid.setButtonBackground( mButton1[BTN_WRITE], ( enable ? mBMwrite : mBMwrite_no ) );
    }
  }

  /** check if the activity is finishing - from interface ICoeffDisplayer
   * @return true if this activity is finishing
   */
  public boolean isActivityFinishing() { return this.isFinishing(); }

  /** display calibration coeffs
   * @param coeffs   calibration coeff array (either 52 or 104 bytes)
   */
  public void displayCoeff( byte[] coeffs )
  { 
    (new CalibCoeffDialog( this, coeffs ) ).show();
  }

  /** enable or disable the buttons
   * @param enable whether to enable
   */
  public void enableButtons( boolean enable )
  {
    boolean enable2 = enable && mEnableWrite;

    mButton1[BTN_TOGGLE].setEnabled( enable );
    mButton1[BTN_BLUETOOTH].setEnabled( enable );
    // if ( TDLevel.overBasic ) {
    //   mButton1[BTN_COVER].setEnabled( enable2 );
    //   TDandroid.setButtonBackground( mButton1[BTN_COVER], ( enable2 ? mBMcover : mBMcover_no ) );
    // }
    if ( TDLevel.overNormal ) {
      mButton1[BTN_READ].setEnabled( enable );
      TDandroid.setButtonBackground( mButton1[BTN_READ], ( enable ? mBMread : mBMread_no ) );
      mButton1[BTN_WRITE].setEnabled( enable2 );
      TDandroid.setButtonBackground( mButton1[BTN_WRITE], ( enable2 ? mBMwrite : mBMwrite_no ) );
    }
    if ( enable ) {
      setTitleColor( TDColor.TITLE_NORMAL );
      TDandroid.setButtonBackground( mButton1[BTN_TOGGLE], mBMtoggle );
      TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], mBMbluetooth );
    } else {
      setTitleColor( TDColor.CONNECTED );
      TDandroid.setButtonBackground( mButton1[BTN_TOGGLE], mBMtoggle_no );
      TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], mBMbluetooth_no );
    }
  }

  /** bluetooth button action
   * @param b   bluetooth button
   */
  private void doBluetooth( Button b )
  {
    if ( TDLevel.overAdvanced && TDInstance.hasDeviceRemoteControl() ) {
      CutNPaste.showPopupBT( this, this, mApp, b, true, false );
    } else {
      mApp.resetComm();
      TDToast.make( R.string.bt_reset );
    }
  }

  /** enable or disable the bluetooth button
   * @param enable whether to enable
   * @note no GMActivity for BLE devices
   */
  public void enableBluetoothButton( boolean enable )
  {
    // if ( diving ) return;
    // if ( TDInstance.isBleDevice() ) enable = true; // always enabled for BLE
    TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], enable ? mBMbluetooth : mBMbluetooth_no );
    mButton1[BTN_BLUETOOTH].setEnabled( enable );
  }

  /** implements the long tap listener
   * @param view tapped view
   * @return true if the long-tap has been consumed
   */
  @Override 
  public boolean onLongClick( View view )
  {
    if ( closeMenu() ) return true;
    CutNPaste.dismissPopupBT();

    Button b = (Button)view;
    if ( b == mButton1[ BTN_BLUETOOTH ] ) {
      // TDLog.v( "BT button long click");
      // enableBluetoothButton(false);
      if ( TDInstance.isDeviceX310() ) {
        new DeviceX310TakeShot( this, (TDSetting.mCalibShotDownload ? new ListerHandler(this) : null), mApp, 1, DataType.DATA_CALIB ).execute();
      // } else if ( TDInstance.isDeviceXBLE() ) { // FIXME
      //   new DeviceX310TakeShot( this, (TDSetting.mCalibShotDownload ? new ListerHandler(this) : null), mApp, 1, DataType.DATA_CALIB ).execute();
      // } else if ( TDInstance.isDeviceCavway() ) { // FIXME
      //   return false;
      } else {
        return false;
      }
      return true;
    } else if ( b == mButton1[ BTN_SEARCH ] ) {
      clearSearchResult();
      return true;
    }
    return false;
  }

  /** implements the tap listener
   * @param view tapped view
   */
  @Override
  public void onClick(View view)
  {
    if ( closeMenu() ) return;
    if ( CutNPaste.dismissPopupBT() ) return;

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
      new CalibToggleTask( this, mApp ).execute();

    } else if ( b == mButton1[BTN_BLUETOOTH] ) { // BLUETOOTH
      doBluetooth( b );

    } else if ( b == mButton1[BTN_DOWNLOAD] ) { // DOWNLOAD
      if ( ! mApp.checkCalibrationDeviceMatch() ) {
        TDToast.makeBad( R.string.calib_device_mismatch );
      } else {
        enableWrite( false );
        setTitleColor( TDColor.CONNECTED );
        ListerHandler handler = new ListerHandler( this ); // FIXME_LISTER
        // new DataDownloadTask( mApp, handler, this, DataType.DATA_CALIB ).execute();

        boolean downloading = mApp.mDataDownloader.toggleDownloading();
        // TDLog.v("GM downloading " + downloading );
        // mApp.notifyListerStatus( handler, downloading ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED );
        mApp.mDataDownloader.doDataDownload( handler, DataType.DATA_CALIB );

        // TDLog.v("GM set button DOWNLOAD on");
        // TDandroid.setButtonBackground( mButton1[ BTN_DOWNLOAD ], mBMdownload_on );
        // enableButtons( false );
      }

    } else if ( b == mButton1[BTN_GROUP] ) { // GROUP
      if ( TDInstance.cid >= 0 ) {
        List< CBlock > list = mApp_mDData.selectAllGMs( TDInstance.cid, 0, true ); // include: normal status (0) but also negative-grp (true)
        if ( list.size() >= 16 ) {
          (new GMGroupsDialog( this, this )).show();
            // // ( TDSetting.mGroupBy == TDSetting.GROUP_BY_DISTANCE )?  getResources().getString( R.string.group_policy_distance ) :
            // ( TDSetting.mGroupBy == TDSetting.GROUP_BY_DISTANCE )?  "Group by distance is no longer supported" : // deprecated
            // ( TDSetting.mGroupBy == TDSetting.GROUP_BY_FOUR )?  getResources().getString( R.string.group_policy_four ) :
            //   getResources().getString( R.string.group_policy_sixteen ) // TDSetting.GROUP_BY_ONLY_16
          // new CalibComputer( this, -1L, CalibComputer.CALIB_COMPUTE_GROUPS ).execute();
        } else {
          resetTitle( );
          TDToast.makeBad( R.string.few_data );
        }
      } else {
        resetTitle( );
        TDToast.makeBad( R.string.no_calibration );
      }

    } else if ( b == mButton1[BTN_COMPUTE] ) { // COMPUTE
      if ( TDInstance.cid >= 0 ) {
        setTitle( R.string.calib_compute_coeffs );
        setTitleColor( TDColor.COMPUTE );
        // 2024-01-15 commented
        // if ( mAlgo == CalibInfo.ALGO_AUTO ) { 
        //   mAlgo = ( TDSetting.mCalibAlgo != CalibInfo.ALGO_AUTO ) ? TDSetting.mCalibAlgo : CalibInfo.ALGO_LINEAR;
        //   mApp.updateCalibAlgo( mAlgo );
        // }
        new CalibComputer( this, -1L, 0, CalibComputer.CALIB_COMPUTE_CALIB ).execute();
      } else {
        TDToast.makeBad( R.string.no_calibration );
      }

    } else if ( TDLevel.overBasic && b == mButton1[BTN_COVER] ) { // COVER
      // if ( mCalibration == null ) {
      //   TDToast.makeBad( R.string.no_calibration );
      // } else {
        List< CBlock > list = mApp_mDData.selectAllGMs( TDInstance.cid, 0, false ); // include: normal status (0) but no negative-grp (false)
        if ( list.size() >= 16 ) {
          ( new CalibCoverageDialog( this, list, mCalibration ) ).show();
        } else {
          TDToast.makeBad( R.string.few_data );
        }
      // }

    } else if ( TDLevel.overNormal && b == mButton1[BTN_READ] ) { // READ
      enableButtons( false );
      new CalibReadTask( this, mApp, CalibReadTask.PARENT_GM, TDInstance.isDeviceTwoSensors() ).execute(); 

    } else if (TDLevel.overNormal &&  b == mButton1[BTN_WRITE] ) { // WRITE
      // if ( mEnableWrite ) {
        if ( mCalibration == null ) {
          TDToast.makeBad( R.string.no_calibration );
        } else {
          byte[] coeff = mCalibration.GetCoeff();
          if ( coeff == null ) {
            TDToast.makeBad( R.string.no_calibration );
          } else {
            // TDLog.v("GM write calib: two-sensors " + mTwoSensors );
            setTitle( R.string.calib_write_coeffs );
            setTitleColor( TDColor.CONNECTED );
            float delta = mCalibration.Delta();
            if ( mTwoSensors ) {
              if ( mCalibration2.Delta() > delta ) delta = mCalibration2.Delta();
              byte[] coeff12 = new byte[104];
              System.arraycopy( coeff, 0, coeff12, 0, 52 );
              coeff = mCalibration2.GetCoeff();
              System.arraycopy( coeff, 0, coeff12, 52, 52 );
              uploadCoefficients( delta, coeff12, true, b ); // 20250123 simplified 
            } else {
              uploadCoefficients( delta, coeff, true, b );
            }
            resetTitle( );
          }
        }
    } else if (TDLevel.overAdvanced && b == mButton1[BTN_SEARCH] ) { // SEARCH
      (new GMSearchDialog( this, this )).show();
      // }
    // } else if ( b == mButton1[BTN_DISTO] ) { // disto
    //   Intent deviceIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DeviceActivity.class );
    //   startActivity( deviceIntent );
    }
  }


  /** highlight the next search result
   * @note called by long-tap or from the search dialog
   */
  public void clearSearchResult()
  { 
    if ( mDataAdapter.clearSearchResult() ) {
      // mList.invalidate( );
    }
  }

  /** search the calibration data
   */
  public void searchData( float error )
  {
    if ( mDataAdapter.searchData( error ) ) {
      // mList.invalidate( );
      // TDLog.v("SEARCH children " + mList.getChildCount() );
    } else {
      TDToast.make( R.string.no_search_result );
    }
  }

  // 20250123 TWO_SENSORS simplified with a single call to mApp.uploadCalibCoeff
  /** upload the calibration coeffs
   * @param delta   maximum calibration data angle error
   * @param coeff   calibration coefficients
   * @param mode    ...
   * @param b       ...
   */
  public void uploadCoefficients( float delta, final byte[] coeff, final boolean mode, final Button b )
  {
    // TDLog.v("GM upload coeff, length " + coeff.length );
    String warning = null;
    // if ( warning == null ) { // check coverage
      List< CBlock > list = mApp_mDData.selectAllGMs( TDInstance.cid, 0, false ); // include: normal status (0) but no negative-grp (false)
      CalibCoverage coverage = new CalibCoverage( );
      float cover_value = coverage.evalCoverage( list, null );
      if ( cover_value < 95 ) warning = String.format( getResources().getString( R.string.coverage_warning ), 95 );
    // }
    if ( warning == null ) {
      if ( delta > 0.5f ) warning = String.format( getResources().getString(R.string.delta_warning), 0.5 ); 
    }

    if ( warning != null ) {
      TopoDroidAlertDialog.makeAlert( this, getResources(), warning,
        new DialogInterface.OnClickListener() {
          @Override public void onClick( DialogInterface d, int btn ) {
            mApp.uploadCalibCoeff( coeff, mode, b );
          }
        } );
    } else {
      mApp.uploadCalibCoeff( coeff, mode, b );
    }
  }

  // ----------------------------------------------------------

  /** compute the groups of the calibration data
   * @param start_id   ID of the starting data
   * @param policy     group policy
   */
  public void computeGroups( long start_id, int policy )
  {
    setTitle( R.string.calib_compute_groups );
    setTitleColor( TDColor.COMPUTE );
    new CalibComputer( this, start_id, policy, CalibComputer.CALIB_COMPUTE_GROUPS ).execute();
  }

  /** reset the groups of the calibration data
   * @param start_id   ID of the starting data
   */
  public void resetGroups( long start_id )
  {
    new CalibComputer( this, start_id, 0, CalibComputer.CALIB_RESET_GROUPS ).execute();
  }

  /** reset and compute the groups of the calibration data
   * @param start_id   ID of the starting data
   * @param policy     group policy
   */
  public void resetAndComputeGroups( long start_id, int policy )
  {
    // if ( ! mGMActivityVisible ) return;
    setTitle( R.string.calib_compute_groups );
    setTitleColor( TDColor.COMPUTE );
    new CalibComputer( this, start_id, policy, CalibComputer.CALIB_RESET_AND_COMPUTE_GROUPS ).execute();
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
    // TDLog.v("GM Activity on Start " );
    // setBTMenus( DeviceUtil.isAdapterEnabled() );
    TDLocale.resetTheLocale();
    setMenuAdapter( getResources() );
    onMenu = true;
    closeMenu();
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // TDLog.v("GM Activity on Resume " );
    // if ( mApp.mComm != null ) { mApp.mComm.resume(); }
    // TDLog.v( "on resume ");
    updateDisplay( );
    // mApp.registerConnListener( mHandler );
    mGMActivityVisible = true;
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    mGMActivityVisible = false;
    // mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
  }


  @Override
  public synchronized void onStop()
  { 
    super.onStop();
    // TDLog.v( "GM Activity stop");
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    // TDLog.v( "GM Activity destroy");
  }

  // ------------------------------------------------------------------

  // public int downloadDataBatch()
  // {
  //   ArrayList< ILister > listers = new ArrayList<>();
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
 
  /** update a calibration data
   * @param value data group, as integer
   * @param name  data group, as string
   */
  public void updateGM( long value, String name )
  {
    if ( mSaveCBlock == null ) {
      TDLog.e( "GM update: null saved cblock" );
      return;
    }
    mApp_mDData.updateGMName( mSaveCBlock.mId, TDInstance.cid, name );
    mSaveCBlock.setGroup( value );
    // TDLog.v("GM activity update " + name + " id " + mSaveCBlock.mId );
    mDataAdapter.updateView( mSaveCBlock );
  }

  /** mark a calibration data "deleted"
   * @param delete whether to mark the date "deleted" or "normal"
   */
  public void deleteGM( boolean delete )
  {
    if ( mSaveCBlock == null ) return;
    mApp_mDData.deleteGM( TDInstance.cid, mSaveCBlock.mId, delete );
    updateDisplay( );
  }

  /** handle key DOWN event
   * @param code   key-event code
   * @param event  key event
   * @return true if event has been consumed
   */
  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWARE MENU (82)
        UserManualActivity.showHelpPage( this, getResources().getString( HELP_PAGE ));
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.e( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------

  /** create the menu adapter
   * @param res  resources
   */
  private void setMenuAdapter( Resources res )
  {
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>(this, R.layout.menu );

    menu_adapter.add( res.getString( (mBlkStatus == 0)? menus[0] : menus[4] ) );
    if ( TDLevel.overAdvanced ) menu_adapter.add( res.getString( menus[1] ) );
    menu_adapter.add( res.getString( menus[2] ) );
    menu_adapter.add( res.getString( menus[3] ) );
    // if ( TDLevel.overTester ) menu_adapter.add( res.getString( menus[5] ) ); // AUTO-CALIB

    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

  /** close menu drop-down
   */
  private boolean closeMenu()
  {
    if ( onMenu ) {
      mMenu.setVisibility( View.GONE );
      onMenu = false;
      return true;
    }
    return false;
  }

  /** handle menu tap
   * @param pos   index of submenu
   */
  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) { // DISPLAY
      mBlkStatus = 1 - mBlkStatus;       // 0 --> 1;  1 --> 0
      setMenuAdapter( getResources() );
      updateDisplay( );
    } else if ( TDLevel.overAdvanced && p++ == pos ) { // VALIDATE
      List< String > list = mApp_mDData.selectDeviceCalibs( TDInstance.deviceAddress() );
      for ( String str : list ) {
        int len = str.indexOf(' ');
        if ( TDInstance.calib.equals( str.substring(0,len) ) ) {
          list.remove( str );
          break;
        }
      }
      if ( TDUtil.isEmpty(list) ) {
        TDToast.makeBad( R.string.few_calibs );
      } else {
        (new CalibValidateListDialog( this, this, list )).show();
      }
    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( this, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_CALIB );
      startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    // } else if ( TDLevel.overTester && p++ == pos ) { // AUTO-CALIB
    //   if ( mCalibration != null ) {
    //     mApp.resetComm();
    //     (new AutoCalibDialog( this, /* this, */ mApp, mCalibration )).show();
    //   } else {
    //     TDToast.makeLong( R.string.autocalib_not_supported );
    //   }
    }
  }

  /** react to a change in the configuration
   * @param new_cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    TDLocale.resetTheLocale();
  }

  // public void notifyDisconnected()
  // {
  // }

}
