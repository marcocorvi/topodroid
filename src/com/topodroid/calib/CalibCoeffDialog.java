/* @file CalibCoeffDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration coefficients display dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDColor;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;
import com.topodroid.TDX.GMActivity;

import java.lang.ref.WeakReference;
import java.util.Locale;

import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;

import android.graphics.Bitmap;

public class CalibCoeffDialog extends MyDialog
                       implements View.OnClickListener
{
  private final WeakReference<GMActivity> mParent; 

  private static final int WIDTH  = 200;
  private static final int HEIGHT = 100;
  // private ImageView mImage; // error histogram
  private Bitmap mBitmap = null;

  private Button mButtonWrite;
  private Button mButtonSecond;
  // private Button mButtonBack;
  private boolean mSecondState = false; // dialog start with first coeff 

  private String bg0;
  private String agx;
  private String agy;
  private String agz;
  private String bm0;
  private String amx;
  private String amy;
  private String amz;
  private String nlx;
  private String delta0;
  private String delta02;
  private String error0;
  private String iter0;
  private String dip0;
  private String roll0; // FIXME ROLL_DIFFERENCE
  private final byte[] mCoeff; // caibration coeffs, 52 or 104 bytes
  private final float[] mErrors;
  private float   mDelta;
  private boolean mTwoSensors = false;
  private boolean mWithResult = false;
  // private boolean mSaturated;

  private TDVector mBG1, mBM1, mNL1, mBG2, mBM2, mNL2;
  private TDMatrix mAG1, mAM1, mAG2, mAM2;

  // TWO_SENSORS coeff2 is null if there is only one sensor set
  /** cstr
   * @param context    context
   * @param coeffs     coefficients byte array 
   */
  public CalibCoeffDialog( Context context, byte[] coeffs )
  {
    super( context, null, R.string.CalibCoeffDialog ); // null app
    mParent = null;
    mCoeff  = null;
    mErrors = null;
    mWithResult = false;
    makeVectorsAndMatricex( coeffs );

    setCoeff( mBG1, mAG1, mBM1, mAM1, mNL1 );

    // mSaturated = saturated;
  }

 /** cstr
   * @param context    context
   * @param parent     parent G-M activity
   * @param errors     calib data residual errors [radians]
   * @param coeff      calibration coefficients, 52 bytes for one sensor-pair, 104 bytes for two sensor pairs
   * @param delta_bh   B. Heeb delta
   * @param delta      average error [degrees]
   * @param delta2     error std-dev [degrees]
   * @param error      max error [degrees]
   * @param iter       number of iterations
   * @param dip        M dip (topodroid dip)
   * @param roll       roll ???
   */
  public CalibCoeffDialog( Context context, GMActivity parent, float[] errors, byte[] coeff,
                    float delta_bh, float delta, float delta2, float error, long iter, float dip, float roll /*, boolean saturated */ )
  {
    super( context, null, R.string.CalibCoeffDialog ); // null app
    mParent = new WeakReference<GMActivity>( parent );
    mCoeff  = coeff;
    mErrors = errors;
    mWithResult = true;

    makeVectorsAndMatricex( coeff );

    setCoeff( mBG1, mAG1, mBM1, mAM1, mNL1 );
    setResult( delta_bh, delta, delta2, error, iter, dip, roll );

    if ( errors != null ) {
      mBitmap = makeHistogramBitmap( errors, WIDTH, HEIGHT, 20, 5, TDColor.BLUE );
    }
  }
 
  /** set the strings for the result text fields
   * @param delta_bh   B. Heeb delta
   * @param delta      average error [degrees]
   * @param delta2     error std-dev [degrees]
   * @param error      max error [degrees]
   * @param iter       number of iterations
   * @param dip        M dip (topodroid dip)
   * @param roll       roll ???
   */
  private void setResult( float delta_bh, float delta, float delta2, float error, long iter, float dip, float roll )
  {
    mDelta = delta;
    delta0  = String.format( mContext.getResources().getString( R.string.calib_error ), delta, delta_bh );
    delta02 = String.format( mContext.getResources().getString( R.string.calib_stddev ), delta2 );
    error0  = String.format( mContext.getResources().getString( R.string.calib_max_error ), error );
    iter0   = String.format( mContext.getResources().getString( R.string.calib_iter ), iter );
    dip0    = String.format( mContext.getResources().getString( R.string.calib_dip ), dip );
    roll0   = String.format( mContext.getResources().getString( R.string.calib_roll ), roll );
  }
   

  /** create a bitmap with the histogram of the errors
   * @param error  errors [radians]
   * @param width  bitmap width
   * @param height bitmap height
   * @param bin    histogram bin
   * @param step   histogram step (?)
   * @param col    ???
   * @return histogram bitmap
   * @note used also by CalibCheckDialog
   */
  static Bitmap makeHistogramBitmap( float[] error, int width, int height, int bin, int step, int col )
  {
    Bitmap bitmap = Bitmap.createBitmap( width+20, height+20, Bitmap.Config.ARGB_8888 );
    int ww = bitmap.getWidth();
    int hh = bitmap.getHeight();
    for ( int j=0; j<hh; ++j ) {
      for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, j, 0 );
    }
    int[] hist = new int[bin];
    for ( int k=0; k<bin; ++k ) hist[k] = 0;
    if ( error != null ) {
      for ( int k=0; k < error.length; ++ k ) {
        int i = (int)( error[k]*10*TDMath.RAD2DEG );
        if ( i < bin && i >= 0 ) ++ hist[i];
      }
    }

    int red = 0xffffffff;
    int top = red;
    int joff = hh-10;
    int ioff = 10;
    int dx   = ww / bin;
    if ( dx*20 >= ww ) dx --;
    int x, y;
    for ( int k=0; k<bin; ++ k ) {
      int h = step * hist[k];
      if ( h > joff ) {
        h = joff;
        top = col;
      } else {
        top = red;
      }
      x  = ioff + dx * k;
      for ( y=joff-h; y <= joff; ++y ) bitmap.setPixel( x, y, red );
      int x2 = x  + dx-1;
      for ( ++x; x < x2; ++ x ) {
        y = joff-h;
        bitmap.setPixel( x, y, red );
        for ( ++y; y < joff; ++y ) bitmap.setPixel( x, y, col );
        bitmap.setPixel( x, y, top );
      }
      for ( y=joff-h; y <= joff; ++y ) bitmap.setPixel( x, y, red );
    }
    for ( y = 0; y < hh; ++y ) bitmap.setPixel( ioff, y, red );
    for ( x = 0; x < ww; ++x ) bitmap.setPixel( x, joff, red );
    for ( int k = 5; k <= bin; k+=5 ) {
      x  = ioff + dx * k;
      int yy = hh - ( ((k%10) == 0 )? 0 : 5 );
      for ( y = joff; y < yy; ++y ) bitmap.setPixel( x, y, red );
    }
    if ( 5  <= bin ) {
      x  = ioff + dx * 5;
      for ( y = 0; y < joff; ++y ) bitmap.setPixel( x, y, TDColor.FIXED_YELLOW );
    }
    if ( 10  <= bin ) {
      x  = ioff + dx * 10;
      for ( y = 0; y < joff; ++y ) bitmap.setPixel( x, y, TDColor.FULL_RED );
    }
    for ( int k = 10; ; k += 10 ) {
      y = joff - step * k;
      if ( y < 0 ) break;
      for ( x = 5; x < ioff; ++x ) bitmap.setPixel( x, y, red );
    }
      
    // TDLog.v( "fill image done");
    return bitmap;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.calib_coeff_dialog, R.string.title_coeff );

    TextView textBG  = (TextView) findViewById(R.id.coeff_bg);
    TextView textAGx = (TextView) findViewById(R.id.coeff_agx);
    TextView textAGy = (TextView) findViewById(R.id.coeff_agy);
    TextView textAGz = (TextView) findViewById(R.id.coeff_agz);

    TextView textBM  = (TextView) findViewById(R.id.coeff_bm);
    TextView textAMx = (TextView) findViewById(R.id.coeff_amx);
    TextView textAMy = (TextView) findViewById(R.id.coeff_amy);
    TextView textAMz = (TextView) findViewById(R.id.coeff_amz);

    TextView textNL = (TextView) findViewById(R.id.coeff_nl);

    ImageView image        = (ImageView) findViewById( R.id.histogram );
    TextView textDelta    = (TextView) findViewById(R.id.coeff_delta);
    TextView textDelta2   = (TextView) findViewById(R.id.coeff_delta2);
    TextView textMaxError = (TextView) findViewById(R.id.coeff_max_error);
    TextView textIter     = (TextView) findViewById(R.id.coeff_iter);
    TextView textDip      = (TextView) findViewById(R.id.coeff_dip);
    TextView textRoll     = (TextView) findViewById(R.id.coeff_roll);
    mButtonWrite   = (Button) findViewById( R.id.button_coeff_write );
    mButtonSecond  = (Button) findViewById( R.id.button_coeff_second );
    Button button_back  = (Button) findViewById( R.id.button_coeff_back );
    button_back.setOnClickListener( this );

    textBG.setText( bg0 );
    textAGx.setText( agx );
    textAGy.setText( agy );
    textAGz.setText( agz );
    textBM.setText( bm0 );
    textAMx.setText( amx );
    textAMy.setText( amy );
    textNL.setText( nlx );
    textAMz.setText( amz );

    if ( mBitmap != null ) {
      image.setImageBitmap( mBitmap );
    } else {
      image.setVisibility( View.GONE );
    }

    if ( mWithResult ) {
      textDelta.setText( delta0 );
      textDelta2.setText( delta02 );
      textMaxError.setText( error0 );
      textIter.setText( iter0 );
      textDip.setText( dip0 );
      textRoll.setText( roll0 ); // FIXME ROLL_DIFFERENCE
    } else {
      textDelta.setVisibility( View.GONE );
      textDelta2.setVisibility( View.GONE );
      textMaxError.setVisibility( View.GONE );
      textIter.setVisibility( View.GONE );
      textDip.setVisibility( View.GONE );
      textRoll.setVisibility( View.GONE ); // FIXME ROLL_DIFFERENCE
    }

    if ( mParent != null ) {
      mButtonWrite.setOnClickListener( this );
      // if ( mSaturated ) {
      //   mButtonWrite.setEnabled( false );
      // } else {
        mButtonWrite.setEnabled( mCoeff != null );
      // }
    } else {
      mButtonWrite.setVisibility( View.GONE );
    }

    if ( mTwoSensors ) {
      mButtonSecond.setOnClickListener( this );
    } else {
      mButtonSecond.setVisibility( View.GONE );
    }
  }

  @Override
  public void onClick(View v) 
  {
    int id = v.getId();
    if ( id == R.id.button_coeff_write ) { 
      if ( mTwoSensors ) {
        // TODO uploadCoefficients for TWO_SENSORS
      } else {
        GMActivity parent = mParent.get();
        if ( parent != null ) {
          if ( mCoeff != null ) parent.uploadCoefficients( mDelta, mCoeff, true, mButtonWrite, false );
        } else {
          TDLog.e("Calib Coeff Dialog null parent");
        }
      }
    } else if ( id == R.id.button_coeff_second ) {
      if ( mTwoSensors ) {
        if ( mSecondState ) {
          mSecondState = false;
          mButtonSecond.setText(  R.string.arrow_right );
          setCoeff( mBG1, mAG1, mBM1, mAM1, mNL1 );
        } else {
          mSecondState = true;
          mButtonSecond.setText(  R.string.arrow_left );
          setCoeff( mBG2, mAG2, mBM2, mAM2, mNL2 );
        }
      }
    } else { // id == R.id.coeff_back
      dismiss();
    }
  }

  /** set the display of the coeffs
   * @param bg   G vector
   * @param ag   G matrix
   * @param bm   M vector
   * @param am   M matrix
   * @param nl   non-linear vector
   */
  private void setCoeff( TDVector bg, TDMatrix ag, TDVector bm, TDMatrix am, TDVector nl )
  {
    bg0 = String.format(Locale.US, "bG   %8.4f %8.4f %8.4f", bg.x, bg.y, bg.z );
    agx = String.format(Locale.US, "aGx  %8.4f %8.4f %8.4f", ag.x.x, ag.x.y, ag.x.z );
    agy = String.format(Locale.US, "aGy  %8.4f %8.4f %8.4f", ag.y.x, ag.y.y, ag.y.z );
    agz = String.format(Locale.US, "aGz  %8.4f %8.4f %8.4f", ag.z.x, ag.z.y, ag.z.z );

    bm0 = String.format(Locale.US, "bM   %8.4f %8.4f %8.4f", bm.x, bm.y, bm.z );
    amx = String.format(Locale.US, "aMx  %8.4f %8.4f %8.4f", am.x.x, am.x.y, am.x.z );
    amy = String.format(Locale.US, "aMy  %8.4f %8.4f %8.4f", am.y.x, am.y.y, am.y.z );
    amz = String.format(Locale.US, "aMz  %8.4f %8.4f %8.4f", am.z.x, am.z.y, am.z.z );

    if ( nl != null ) {
      nlx = String.format(Locale.US, "nL   %8.4f %8.4f %8.4f", nl.x, nl.y, nl.z );
    } else {
      nlx = TDString.EMPTY; // new String(TDString.EMPTY);
    }
  }

  private void makeVectorsAndMatricex( byte[] coeff )
  {
    mTwoSensors = (coeff.length == 104);
    mAG1 = new TDMatrix(); // cannot move these to CalibCoeffDialog cstr because the dialog is used also without coeffs
    mAM1 = new TDMatrix();
    mBG1 = new TDVector();
    mBM1 = new TDVector();
    mNL1 = new TDVector();
    CalibAlgo.coeffToG( coeff, mBG1, mAG1 ); // FIXME using first sensor set to compute G-M vector/matrix for display 
    CalibAlgo.coeffToM( coeff, mBM1, mAM1 );
    CalibAlgo.coeffToNL( coeff, mNL1 );
    if ( coeff.length == 104) {
      mTwoSensors = true;
      mAG2 = new TDMatrix(); // cannot move these to CalibCoeffDialog cstr because the dialog is used also without coeffs
      mAM2 = new TDMatrix();
      mBG2 = new TDVector();
      mBM2 = new TDVector();
      mNL2 = new TDVector();
      byte[] coeff2 = new byte[52];
      System.arraycopy( coeff, 52, coeff2, 0, 52 );
      CalibAlgo.coeffToG( coeff2, mBG2, mAG2 ); // FIXME using first sensor set to compute G-M vector/matrix for display 
      CalibAlgo.coeffToM( coeff2, mBM2, mAM2 );
      CalibAlgo.coeffToNL( coeff2, mNL2 );
    } else {
      mTwoSensors = false;
    }
  }

}

