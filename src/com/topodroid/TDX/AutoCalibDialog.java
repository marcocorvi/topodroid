/* @file AutoCalibDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device info dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDMath;
import com.topodroid.dev.Device;
import com.topodroid.dev.DataType;
// import com.topodroid.TDX.GMActivity;
// import com.topodroid.TDX.ListerHandler;
// import com.topodroid.TDX.TDInstance;
// import com.topodroid.TDX.TopoDroidApp;
// import com.topodroid.TDX.R;
import com.topodroid.calib.ICoeffDisplayer;
import com.topodroid.calib.CalibAlgo;
import com.topodroid.calib.CalibTransform;
import com.topodroid.calib.CBlock;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
// import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.widget.TextView;
// import android.widget.RadioButton;
import android.widget.Button;

import java.util.Locale;
import java.util.List;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

public class AutoCalibDialog extends MyDialog
                             implements View.OnClickListener
//
{
  /** pair of G-M vectors
   */
  private class GMVector
  {
    TDVector g;
    TDVector m;

    /** default cstr - zero vectors
     */
    GMVector() 
    {
      g = new TDVector();
      m = new TDVector();
    }

    /** copy cstr
     * @param v  another GM vector
     */
    GMVector( GMVector v ) 
    {
      g = new TDVector( v.g ); 
      m = new TDVector( v.m ); 
    }

    /** cstr
     * @param vg   G vector
     * @param vm   M vector
     */
    GMVector( TDVector vg, TDVector vm ) 
    {
      g = new TDVector( vg );
      m = new TDVector( vm );
    }

    /** independently normalize the G and M vectors
     */
    void normalize()
    {
      g.normalize();
      m.normalize();
    }

    /** @return the sum of this GM vector with another
     * @param v the other GM vector
     */
    GMVector plus( GMVector v )
    {
      return new GMVector( g.plus( v.g ), m.plus( v.m ) );
    }

    /** @return the difference of this GM vector and another GM vector
     * @param v the other GM vector
     */
    GMVector minus( GMVector v )
    {
      return new GMVector( g.minus( v.g ), m.minus( v.m ) );
    }

    /** @return add another GM vector this GM vector
     * @param v the other GM vector
     */
    void plusEqual( GMVector v )
    {
      g.plusEqual( v.g );
      m.plusEqual( v.m );
    }

    /** @return add a pair of G and M vectors this GM vector
     * @param vg the G vector
     * @param vm the M vector
     */
    void plusEqual( TDVector vg, TDVector vm )
    {
      g.plusEqual( vg );
      m.plusEqual( vm );
    }

    /** @return the GM vector with the G and the M vector of this GM vector multiplied by a scalar
     * @param f  scalar
     */
    GMVector times( float f )
    {
      return new GMVector( g.times( f ), m.times( f ) );
    }

    /** multiply both the G and the M vector of this GM vector by a scalar
     * @param f  scalar
     */
    void timesEqual( float f )
    {
      g.timesEqual( f );
      m.timesEqual( f );
    }

    /** optimize the G and M vectors
     * @param s  sin( alpha )
     * @param c  cos( alpha )
     * @pre s*s + c*c = 1
     */
    void opt( float s, float c )
    {
      TDVector e = TDVector.cross_product( g, m );       // E = G ^ M (east)
      e.normalize();
      TDVector nm = TDVector.cross_product( m, e );      // Mn = M ^ E
      g.plusEqual( m.times( c ).plus( nm.times( s ) ) ); // G += M * c + (M ^ E) * s
      g.normalize();
      TDVector ng = TDVector.cross_product( e, g );
      m = g.times( c ).plus( ng.times( s ) );
    }

    /** turn the G, M vectors around the X axis
     * @param s  sin( alpha )
     * @param c  cos( alpha )
     * @pre s*s + c*c = 1
     */
    void turnX( float s, float c )
    {
      float y = g.y * c - g.z * s;
      float z = g.y * s + g.z * c;
      g.y = y;
      g.z = z;
      y = m.y * c - m.z * s;
      z = m.y * s + m.z * c;
      m.y = y;
      m.z = z;
    }

    void turnVectors( GMVector r )
    {
      float s = r.g.z * g.y - r.g.y * g.z + r.m.z * m.y - r.m.y * m.z;
      float c = r.g.y * g.y + r.g.z * g.z + r.m.y * m.y + r.m.z * m.z;
      float d = TDMath.sqrt( s*s + c*c );
      turnX( s/d, c/d );
    }

    // GMVector transform( TDVector bG, TDMatrix aG, TDVector bM, TDMatrix aM )
    // {
    //   return new GMVector( bG.plus( aG.timesV( g ) ), bM.plus( aM.timesV( m ) );
    // }
  };

  /** pair of G and M A-matrices
   */
  private class GMMatrix
  {
    TDMatrix g;
    TDMatrix m;

    /** default cstr - init to zero matrices
     */
    GMMatrix( )
    {
      g = new TDMatrix( );
      m = new TDMatrix( );
    }

    /** cstr from two TDMatrices
     * @param mg   G TDMatrix
     * @param mm   M TDMatrix
     */
    GMMatrix( TDMatrix mg, TDMatrix mm ) 
    {
      g = new TDMatrix( mg );
      m = new TDMatrix( mm );
    }

    /** cstr - external product of two GMvectors
     * @param v1   left vector
     * @param v2   right vector
     */
    GMMatrix( GMVector v1, GMVector v2 )
    {
      g = new TDMatrix( v1.g, v2.g );
      m = new TDMatrix( v1.m, v2.m );
    }

    /** copy cstr
     * @param mm   GMMatrix
     */
    GMMatrix( GMMatrix mm )
    {
      g = new TDMatrix( mm.g );
      m = new TDMatrix( mm.m );
    }

    GMMatrix plus( GMMatrix mm ) 
    {
      return new GMMatrix( g.plus( mm.g ), m.plus( mm.m ) );
    }

    void plusEqual( GMMatrix mm ) 
    {
      g.plusEqual( mm.g );
      m.plusEqual( mm.m );
    }

    GMMatrix minus( GMMatrix mm ) 
    {
      return new GMMatrix( g.minus( mm.g ), m.minus( mm.m ) );
    }

    GMMatrix timesF( float f )
    {
      return new GMMatrix( g.timesF( f ), m.timesF( f ) );
    }

    GMVector timesV( GMVector v ) 
    {
      return new GMVector( g.timesV( v.g ), m.timesV( v.m ) ); 
    }

    GMMatrix timesM( GMMatrix mm ) 
    {
      return new GMMatrix( g.timesM( mm.g ), m.timesM( mm.m ) );
    }

    GMMatrix inverseMatrix()
    {
      return new GMMatrix( g.inverseMatrix(), m.inverseMatrix() );
    }
  }

    


  private Button mBTstart;
  private Button mBTwrite;
  // private Button mBTstop;
  private Button mBTback;

  // private final GMActivity   mParent;
  private final TopoDroidApp mApp;
  private CalibTransform     mParentCalib;
  private CalibTransform     mCalib;

  private GMVector mB;
  private GMMatrix mA;
  // private TDVector mNL;

  private float mAzimuth; // degrees
  private float mClino;
  private GMVector mF = null;

  private GMVector mAveI;
  private GMVector mSumI;
  private GMMatrix mSumI2;

  private GMVector mAveR;
  private GMVector mSumR;
  private GMMatrix mSumR2;

  private float mDip;  // calib data average dip (M dot G)
  private float mM2;
  private float mG2;

  private float mSa; // sin(alpha)
  private float mCa; // cos(alpha)

  // private float mAlphaD = 0.1f;
  // private float mAlphaM = 0.01f;
  // private float mAlphaG = 0.01f;
  private float beta   = TDSetting.mAutoCalBeta;  // 0.002f; 
  private float eta    = TDSetting.mAutoCalEta;   // 0.02f; 
  private float gamma  = TDSetting.mAutoCalGamma; // 0.02f; 
  private float delta  = TDSetting.mAutoCalDelta; // 0.02f; 

  private boolean mDownloading = false;

  private PrintWriter mPw = null;


  /** cstr
   * @param context   context
   * @param app       topodroid app
   * @param calib     parent' calib transform (must be non-null)
   */
  public AutoCalibDialog( Context context, /* GMActivity parent, */ TopoDroidApp app, CalibTransform calib )
  {
    super( context, null, R.string.AutoCalibDialog ); // null app
    // mParent = parent;
    mApp    = app;
    mParentCalib  = calib;

    try {
      File f = TDPath.getDumpFile( TDUtil.currentDate() );
      PrintWriter mPw = new PrintWriter( f );
    } catch ( IOException e ) {
      mPw = null;
    }
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.auto_calib_dialog, R.string.auto_calib );

    // Resources res = mParent.getResources();
    mBTstart = (Button) findViewById( R.id.button_start );
    mBTwrite = (Button) findViewById( R.id.button_write );
    // mBTstop  = (Button) findViewById( R.id.button_stop  );
    mBTback  = (Button) findViewById( R.id.button_close );

    byte[] coeff0 = new byte[ 52 ];
    if ( ! mApp.readCalibCoeff( coeff0 ) ) {
      TDLog.v("Error: could not read coeffs");
      mBTstart.setVisibility( View.GONE );
      mBTwrite.setVisibility( View.GONE );
      // mBTstop.setVisibility( View.GONE );
    } else {
      byte[] coeff = mParentCalib.GetCoeff();
      int diffs = 0;
      for ( int k = 0; k < 52; ++ k ) {
        if ( coeff[k] != coeff0[k] ) {
          diffs ++;
          TDLog.v("coeff " + k + " differ " + coeff[k] + " " + coeff0[k] );
        }
      }
      if ( diffs > 0 ) {
        TDToast.make( "Diff nr " + diffs );
      }

      mCalib = new CalibTransform( coeff0, false );

      mB = new GMVector( mCalib.GetBG(), mCalib.GetBM() );
      mA = new GMMatrix( mCalib.GetAG(), mCalib.GetAM() );
      // mNL = mCalib.GetNL();
      displayCoeffs();

      // TDLog.v("read coeffs");
      mBTstart.setOnClickListener( this );
      mBTwrite.setOnClickListener( this );
      // mBTstop.setOnClickListener( this );
    }

    mBTback.setOnClickListener( this );
    initDipM2G2();
  }

  /** check that the coefficients of the transformation do not overflow
   * @param a   GM A-matrix
   * @param b   GM B-vector
   *
  // FV = 24000.0f;
  // FM = 16384.0f;
   */
  void checkOverflow( GMMatrix a, GMVector b )
  {
    float mb = b.g.maxAbsValue() * TDUtil.FV; 
    float ma = a.g.maxAbsValue() * TDUtil.FM;
    float m = ( ma > mb )? ma : mb;
    if ( m > 32768 ) { // 32768 max short
      m = 32768 / m;
      b.g.timesEqual( m );
      a.g.timesEqual( m );
    }
    mb = b.m.maxAbsValue() * TDUtil.FV; 
    ma = a.m.maxAbsValue() * TDUtil.FM;
    m = ( ma > mb )? ma : mb;
    if ( m > 32768 ) {
      m = 32768 / m;
      b.m.timesEqual( m );
      a.m.timesEqual( m );
    }
  }

  /** update the display of coeffs
   */
  private void displayCoeffs()
  {
    TDVector mBG = mB.g;
    TDVector mBM = mB.m;
    TDMatrix mAG = mA.g;
    TDMatrix mAM = mA.m;
    // TDLog.v("BG " + mBG.x + " " + mBG.y + " " + mBG.z );
    ((TextView)findViewById( R.id.coeff_bg  )).setText( String.format(Locale.US, "bG   %8.4f %8.4f %8.4f", mBG.x,   mBG.y,   mBG.z ) );
    ((TextView)findViewById( R.id.coeff_agx )).setText( String.format(Locale.US, "aGx  %8.4f %8.4f %8.4f", mAG.x.x, mAG.x.y, mAG.x.z ) );
    ((TextView)findViewById( R.id.coeff_agy )).setText( String.format(Locale.US, "aGy  %8.4f %8.4f %8.4f", mAG.y.x, mAG.y.y, mAG.y.z ) );
    ((TextView)findViewById( R.id.coeff_agz )).setText( String.format(Locale.US, "aGz  %8.4f %8.4f %8.4f", mAG.z.x, mAG.z.y, mAG.z.z ) );

    ((TextView)findViewById( R.id.coeff_bm  )).setText( String.format(Locale.US, "bM   %8.4f %8.4f %8.4f", mBM.x,   mBM.y,   mBM.z ) );
    ((TextView)findViewById( R.id.coeff_amx )).setText( String.format(Locale.US, "aMx  %8.4f %8.4f %8.4f", mAM.x.x, mAM.x.y, mAM.x.z ) );
    ((TextView)findViewById( R.id.coeff_amy )).setText( String.format(Locale.US, "aMy  %8.4f %8.4f %8.4f", mAM.y.x, mAM.y.y, mAM.y.z ) );
    ((TextView)findViewById( R.id.coeff_amz )).setText( String.format(Locale.US, "aMz  %8.4f %8.4f %8.4f", mAM.z.x, mAM.z.y, mAM.z.z ) );
  }

  /** react to a user tap: dismiss the dialog if the tapped button is "BACK"
   * @param view tapped view
   */
  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTstart ) {
      if ( ! mDownloading ) {
        mDownloading = mApp.mDataDownloader.toggleDownload();
        // TDLog.v("AutoCalib START: downloading " + mDownloading );
        ListerHandler handler = new ListerHandler( this ); // FIXME_LISTER
        mApp.mDataDownloader.doDataDownload( handler, DataType.DATA_CALIB );
        mBTstart.setText( R.string.button_stop );
        mBTwrite.setEnabled( false );
      } else {
        mDownloading = mApp.mDataDownloader.toggleDownload();
        mApp.mDataDownloader.stopDownloadData( new ListerHandler( this ) );
        // TDLog.v("AutoCalib STOP: downloading " + mDownloading );
        mBTstart.setText( R.string.button_start );
        mBTwrite.setEnabled( true );
        if ( mPw != null ) {
          mPw.flush();
        }
      }
    } else if ( b == mBTwrite ) {
      if ( ! mDownloading ) {
        // TODO ask for confirm
        byte[] coeff = mCalib.GetCoeff();
        // TODO 
        // mApp.uploadCalibCoeff( coeff, false, mBTwrite ); // false: no MAC check
      } else {
        TDToast.make("cannot upload while downloading");
      }
    } else if ( b == mBTback ) {
      onBackPressed();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( mDownloading ) {
      mDownloading = mApp.mDataDownloader.toggleDownload();
      mApp.mDataDownloader.stopDownloadData( new ListerHandler( this ) );
      TDLog.v("AutoCalib BACK: downloading " + mDownloading );
      if ( mPw != null ) {
        mPw.flush();
      }
    }
    if ( mPw != null ) {
      mPw.close();
    }
    super.onBackPressed();
  }

  // public void displayCoeff( TDVector bg, TDMatrix ag, TDVector bm, TDMatrix am, TDVector nl )
  // {
  //   TDLog.v("set coeffs");
  //   mBG = bg;
  //   mAG = ag;
  //   mBM = bm;
  //   mAM = am;
  //   mNL = nl;
  // }
  //
  // public void enableButtons( boolean b )
  // {
  //   TDLog.v("enable buttons");
  // }
  //
  // public boolean isActivityFinishing() { return false; }

  GMVector transform( GMVector v )
  {
    return mB.plus( mA.timesV( v ) );
  }


  /** update the display of the DistoX2 info
   * @param info   DistoX2 info (null indicates a read error)
   */
  public void update( long gx, long gy, long gz, long mx, long my, long mz )
  {
    updateDipM2G2( gx, gy, gz, mx, my, mz );
    displayCoeffs();
  }

  /** compute the average dip, M2 and G2 of the calibration data
   */
  private void initDipM2G2()
  {
    List< CBlock > list = TopoDroidApp.mDData.selectAllGMs( TDInstance.cid, 0, false ); // false: skip negative-grp

    mSumI  = new GMVector();
    mSumI2 = new GMMatrix();
    mSumR  = new GMVector();
    mSumR2 = new GMMatrix();

    if ( list.size() > 1 ) {
      int   n = 0;
      mSa = 0;
      mCa = 0;
      float m = 0;
      float g = 0;
      // int cnt = 0;
      for ( CBlock b : list ) {
        // if ( cnt ++ < 6 ) TDLog.v("calib data G " + b.gx + " " + b.gy + " " + b.gz + " M " + b.mx + " " + b.my + " " + b.mz );
        TDVector gi = CalibTransform.scaledVector( b.gx, b.gy, b.gz );
        TDVector mi = CalibTransform.scaledVector( b.mx, b.my, b.mz );
        GMVector vi = new GMVector( gi, mi );
        mSumI.plusEqual( vi );
        mSumI2.plusEqual( new GMMatrix( vi, vi ) );

        GMVector vr = transform( vi );
        mSumR.plusEqual( vr );
        mSumR2.plusEqual( new GMMatrix( vr, vi ) );

        mSa += TDVector.cross_product( vr.g, vr.m ).length();
        mCa += TDVector.dot_product( vr.g, vr.m );
        m += TDVector.dot_product( vr.m, vr.m );
        g += TDVector.dot_product( vr.g, vr.g );
        n ++;
      }
      float invN = 1.0f / n;
      mAveI = mSumI.times( invN );
      mAveR = mSumR.times( invN );
      // mMatG2 = mSumG2i.minus( new TDMatrix( mSumGi, mAveGi ) ); // mInvG = mMatG2.inverseMatrix();
      // mMatM2 = mSumM2i.minus( new TDMatrix( mSumMi, mAveMi ) );

      // mSumG2.timesEqual( invN );
      // mSumM2.timesEqual( invN );

      mDip = mCa * invN;
      mM2  = m * invN;
      mG2  = g * invN;
      normalizeAlpha();
      TDLog.v("AutoCalib init D " + mDip + " M2 " + mM2 + " G2 " + mG2 );
    }
    mAzimuth = 8; // radians
    mClino   = 2; // radians
  }

  private void normalizeAlpha()
  {
    float d = TDMath.sqrt( mSa * mSa + mCa * mCa );
    mSa = mSa / d;
    mCa = mCa / d;
  }

  private boolean closeShot( GMVector r )
  {
    float c = TDMath.acosd( r.g.x / r.g.length() ); // degrees
    TDVector e = TDVector.cross_product( r.g, r.m ); // east: G^M
    TDVector n = TDVector.cross_product( e, r.g ); // nord: (G^M) ^ G
    float a = TDMath.atan2d( e.x / e.length(), n.x / n.length() ); // degrees
    
    if ( mF == null || Math.abs( a - mAzimuth ) > 5 || Math.abs( c - mClino ) > 5 ) { // degrees
      mF = r;
      mAzimuth = a;
      mClino   = c;
      return false;
    }
    return true;
  }

  private void updateDipM2G2( long g_x, long g_y, long g_z, long m_x, long m_y, long m_z )
  {
    // TDLog.v("update data G " + g_x + " " + g_y + " " + g_z + " M " + m_x + " " + m_y + " " + m_z );
    TDVector gi = CalibTransform.scaledVector( g_x, g_y, g_z );
    TDVector mi = CalibTransform.scaledVector( m_x, m_y, m_z );
    TDVector gr = mCalib.getTransformedG( gi );
    TDVector mr = mCalib.getTransformedM( mi );
    float gm = TDVector.dot_product( gr, mr );
    float mm = TDVector.dot_product( mr, mr );
    float gg = TDVector.dot_product( gr, gr );
    // TDLog.v("gm " + gm + " mm " + mm + " gg " + gg );
    boolean turned = false;
    GMVector vi = new GMVector( gi, mi );
    GMVector vr = new GMVector( gr, mr );
    GMVector vx = new GMVector( vr );
    if ( closeShot( vx ) ) {
      vx.turnVectors( mF );
      turned = true;
    } else {
      mF = vr;
    }
    vx.opt( mSa, mCa );
    float gamma1 = 1.0f - gamma;
    float beta1  = 1.0f - beta;
    float eta1   = 1.0f - eta;
    mSa = gamma1 * mSa + gamma * TDVector.cross_product( vx.g, vr.m ).length();
    mCa = gamma1 * mCa + gamma * vx.g.dot( vr.m );
    normalizeAlpha();
    if ( turned ) {
      vx.turnVectors( vr );
    }
    mAveI = mAveI.times(beta1).plus( vi.times(beta) );
    mAveR = mAveR.times(beta1).plus( vx.times(beta) );

    mSumI  = mSumI.times(gamma1).plus( vi.times(gamma) );
    mSumR  = mSumR.times(gamma1).plus( vx.times(gamma) );

    mSumR2 = mSumR2.timesF(eta1).plus( new GMMatrix( vx, vi ).timesF(eta) );
    mSumI2 = mSumI2.timesF(eta1).plus( new GMMatrix( vi, vi ).timesF(eta) );

    GMMatrix i2 = mSumI2.minus( new GMMatrix( mSumI, mAveI ) );
    GMMatrix r2 = mSumR2.minus( new GMMatrix( mAveR, mSumR ) );
    mA = r2.timesM( i2.inverseMatrix() );
    float zy = (mA.g.z.y + mA.g.y.z)/2;
    mA.g.z.y = mA.g.y.z = zy;

    mB = mAveR.minus( mA.timesV( mAveI ) );

    checkOverflow( mA, mB );

    // float ed = ( gm - mDip ) * mAlphaD;
    // float em = ( mm - mM2  ) * mAlphaM;
    // float eg = ( gg - mG2  ) * mAlphaG;
    // TDVector dm = gs.times( ed ).plus( ms.times(em) );
    // TDVector dg = ms.times( ed ).plus( gs.times(eg) );
    // mBM.minusEqual( dm );
    // mBG.minusEqual( dg );
    // mAM.minusEqual( new TDMatrix( dm, mr ) );
    // mAG.minusEqual( new TDMatrix( dg, gr ) );
    // float zy = (mAG.z.y + mAG.y.z)/2;
    // mAG.z.y = mAG.y.z = zy;
    float delta1 = 1.0f - delta;
    mDip = delta1 * mDip + delta * gm;
    mM2  = delta1 * mM2  + delta * mm;
    mG2  = delta1 * mG2  + delta * gg;
    // TDLog.v("AutoCalib update D " + mDip + " M2 " + mM2 + " G2 " + mG2 );
    if ( mPw != null ) {
      mPw.format(Locale.US, "G %d %d %d M %d %d %d", g_x, g_y, g_z, m_x, m_y, m_z );
      mPw.format(Locale.US, " gm %.2f mm %.2f gg %.2f", gm, mm, gg );
      mPw.format(Locale.US, " Dip %.2f M2 %.2f G2 %.2f\n", mDip, mM2, mG2 );
    }
  }


}
