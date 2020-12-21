/* @file CalibCheckDialog.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid DistoX calibration-check shots dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.math.TDVector;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;

import java.util.List;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.View;
import android.view.View.OnClickListener;

// import android.graphics.Bitmap;

import android.util.Log;

class CalibCheckDialog extends MyDialog
                       implements OnItemClickListener
                                , OnClickListener
{
  // private SurveyWindow mParent;
  private List< DBlock > mShots;

  // private ImageView hist0;
  private ImageView hist1;
  private ImageView hist2;
  private Button    mBtnClose;

  CalibCheckDialog( Context context, SurveyWindow parent, List< DBlock > shots )
  {
    super( context, R.string.CalibCheckDialog );
    // mParent = parent;
    mShots  = shots;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.calib_check_dialog, R.string.title_calib_check );
    
    // hist0 = (ImageView) findViewById( R.id.histogram0 );
    hist1 = (ImageView) findViewById( R.id.histogram1 );
    hist2 = (ImageView) findViewById( R.id.histogram2 );

    mBtnClose = (Button) findViewById( R.id.button_close );
    mBtnClose.setOnClickListener( this );

    ArrayAdapter<String> array_adapter = new ArrayAdapter<>( mContext, R.layout.message );

    ListView list = (ListView) findViewById(R.id.list);
    list.setAdapter( array_adapter );
    list.setOnItemClickListener( this );
    list.setDividerHeight( 2 );
    
    // setTitle( R.string.title_calib_check );
    // NOTE no calib ceck for DATAMODE_DIVING
    for ( DBlock blk : mShots ) {
      array_adapter.add( blk.toShortStringNormal(true) );
    }
  }

  @Override
  public void onClick( View v )
  {
    dismiss(); // only mBtnClose
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long idx )
  {
    if ( ! ( view instanceof TextView ) ) {
      TDLog.Error("calib check view instance of " + view.toString() );
      return;
    }
    String str = ((TextView) view).getText().toString();
    int len = str.indexOf(" ");
    int id = Integer.parseInt( str.substring(0, len) );
    // Log.v("DistoX", "check item " + id );
    DBlock blk = null;
    float x=0, y=0, z=0; // vector sum of the data in the leg
    int n1 = 0;          // number of data in the leg
    int k1 = 0;          // index of the leg
    int k  = 0;
    for ( DBlock b : mShots ) {
      if ( b.mId == id ) {
        if ( b.isMainLeg() ) {
          k1 = k;
          blk = b;
          float h = b.mLength * TDMath.cosd( b.mClino );
          z += b.mLength * TDMath.sind( b.mClino );
          x += h * TDMath.sind( b.mBearing );
          y += h * TDMath.cosd( b.mBearing );
          ++n1;
        } else {
          // do not do anything
          break;
        }
      } else if ( blk != null ) {
        if ( b.isMainLeg() ) { // next leg set
          break;
        } else {
          float h = b.mLength * TDMath.cosd( b.mClino );
          z += b.mLength * TDMath.sind( b.mClino );
          x += h * TDMath.sind( b.mBearing );
          y += h * TDMath.cosd( b.mBearing );
          ++n1;
        }
      }
      ++k;
    }

    if ( blk != null ) {
      // Log.v("DistoX", "check block " + blk.mFrom + " " + blk.mTo + " n1 " + n1 );
      String b0 = blk.mFrom + "-" + blk.mTo;
      String b2 = null;
      TDVector v0 = new TDVector( x, y, z ); // unit vector along the leg
      float l0 = v0.Length();            // length of leg vector
      v0.normalize();
      boolean in_leg = false;
      int n2 = 0; // number of data in the opposite leg
      int k2 = 0; // index of the opposite leg
      k = 0;
      for ( DBlock b : mShots ) {
        if ( ! in_leg ) {
          if ( b.isMainLeg() ) {
            // FIXME only the first backshot is considered
	    if ( blk.mFrom.equals( b.mTo ) && blk.mTo.equals( b.mFrom ) ) {
              b2 = b.mFrom + "-" + b.mTo;
              k2 = k;
              n2 = 1;
              in_leg = true;
            }
          }
        } else {
          if ( b.isMainLeg() ) {
            break;
          } else {
            ++n2;
          }
        }
        ++k;
      }

      float[] errors1 = new float[n1]; // angle differences between data in leg and leg average [radians]
      for ( k = 0; k<n1; ++k ) {
        DBlock b = mShots.get( k1 + k );
        float h = b.mLength * TDMath.cosd( b.mClino );
        TDVector v1 = new TDVector( h * TDMath.sind( b.mBearing ), h * TDMath.cosd( b.mBearing ), b.mLength * TDMath.sind( b.mClino ) );
        v1.normalize();
        errors1[k] = (float)(TDVector.arc_distance( v0, v1 ));
      }
      hist1.setImageBitmap( CalibCoeffDialog.makeHistogramBitmap( errors1, 400, 100, 40, 50, TDColor.FIXED_ORANGE ) );

      if ( n2 > 0 ) {
	// Log.v("DistoX", "found opposite block: n2 " + n2 );
        float[] errors2 = new float[n2*n1];
        for ( k = 0; k<n1; ++k ) {
          DBlock bb = mShots.get( k1 + k );
          float h = bb.mLength * TDMath.cosd( bb.mClino );
          TDVector w1 = new TDVector( h * TDMath.sind( bb.mBearing ), h * TDMath.cosd( bb.mBearing ), bb.mLength * TDMath.sind( bb.mClino ) );
          for ( int kk = 0; kk<n2; ++kk ) {
            bb = mShots.get( k2 + kk );
            h = bb.mLength * TDMath.cosd( bb.mClino );
            TDVector w2 = new TDVector( h * TDMath.sind( bb.mBearing ), h * TDMath.cosd( bb.mBearing ), bb.mLength * TDMath.sind( bb.mClino ) );
            w2.plusEqual( w1 );                      // W1 + W2
            w2.minusEqual( v0.times( v0.dot(w2) ) ); // (W1+W2) - V0 [ V0 * (W1+W2) ] part orthogonal to V0
            errors2[k*n2+kk] = w2.Length() / l0;     // angle difference
          }
        }
        hist2.setImageBitmap( CalibCoeffDialog.makeHistogramBitmap( errors2, 400, 100, 40, 10, TDColor.LIGHT_GRAY ) );
        setTitle( b0 + " " + b2 );
      } else {
	Log.v("DistoX", "search a triangle" );
	// k2 = n2 = 0;
        int n3 = 0;
        int k3 = 0; // index of the opposite leg
        String station = null;
	k = 0;
	in_leg = false;
	int sign2 = 0;
	int sign3 = 0;
	DBlock blk2 = null;
	// boolean reversed = false; // triangle is 1-3-2
        for ( DBlock b : mShots ) {
          if ( b == blk ) { ++k; continue; }
          if ( ! in_leg ) {
            if ( b.isMainLeg() ) {
              // FIXME only the first triangle trial is considered
              if ( b.mFrom.equals( blk.mFrom ) && ! b.mTo.equals( blk.mTo ) ) {
                k2 = k;
                n2 = 1;
                sign2 = -1;
                blk2 = b;
                station = b.mTo;
                // reversed = true;
                in_leg = true;
              } else if ( b.mFrom.equals( blk.mTo ) && ! b.mTo.equals( blk.mFrom ) ) {
                k2 = k;
                n2 = 1;
                sign2 = 1;
                blk2 = b;
                station = b.mTo;
                in_leg = true;
              } else if ( b.mTo.equals( blk.mFrom ) && ! b.mFrom.equals( blk.mTo ) ) {
                k2 = k;
                n2 = 1;
                sign2 = 1;
                blk2 = b;
                station = b.mFrom;
                // reversed = true;
                in_leg = true;
              } else if ( b.mTo.equals( blk.mTo   ) && ! b.mFrom.equals( blk.mFrom ) ) {
                k2 = k;
                n2 = 1;
                sign2 = -1;
                blk2 = b;
                station = b.mFrom;
                in_leg = true;
              }
            }
          } else {
            if ( b.isMainLeg() ) {
              break;
            } else {
              ++n2;
            }
          }
          ++k;
	}
        String b3 = null;
	if ( station != null ) {
          // Log.v("DistoX", "found block2 " + blk2.mFrom + " " + blk2.mTo + " sign2 " + sign2 + " n2 " + n2 );
          b2 = blk2.mFrom + "-" + blk2.mTo;
          
	  k = 0;
	  in_leg = false;
          for ( DBlock b : mShots ) {
	    if ( b == blk || b == blk2 ) { ++k; continue; }
            if ( ! in_leg ) {
              if ( b.isMainLeg() ) {
	        if ( b.mFrom.equals( blk.mFrom ) && b.mTo.equals( station ) ) {
                  b3 = b.mFrom + "-" + b.mTo;
                  k3 = k;
	          n3 = 1;
		  sign3 = -1;
		  in_leg = true;
		} else if ( b.mFrom.equals( blk.mTo ) && b.mTo.equals( station ) ) {
                  b3 = b.mFrom + "-" + b.mTo;
                  k3 = k;
	          n3 = 1;
		  sign3 = 1;
		  in_leg = true;
	        } else if ( b.mTo.equals( blk.mFrom ) && b.mFrom.equals( station ) ) {
                  b3 = b.mFrom + "-" + b.mTo;
                  k3 = k;
	          n3 = 1;
		  sign3 = 1;
		  in_leg = true;
	        } else if ( b.mTo.equals( blk.mTo   ) && b.mFrom.equals( station ) ) {
                  b3 = b.mFrom + "-" + b.mTo;
                  k3 = k;
	          n3 = 1;
		  sign3 = -1;
		  in_leg = true;
		}
	      }
	    } else {
              if ( b.isMainLeg() ) {
	        break;
              } else {
                ++n3;
              }
	    }
	    ++k;
	  }
	}
        if ( n2 > 0 && n3 > 0 ) {
          // Log.v("DistoX", "found block3 k1 " + k1 + " k2 " + k2 + " k3 " + k3 );
          float[] errors3 = new float[n3*n2*n1];
          for ( int h1 = 0; h1<n1; ++h1 ) {
            DBlock bb = mShots.get( k1 + h1 );
            float h = bb.mLength * TDMath.cosd( bb.mClino );
            TDVector w1 = new TDVector( h * TDMath.sind( bb.mBearing ), h * TDMath.cosd( bb.mBearing ), bb.mLength * TDMath.sind( bb.mClino ) );
	    float l1 = w1.Length();
            for ( int h2 = 0; h2<n2; ++h2 ) {
              bb = mShots.get( k2 + h2 );
              h = sign2 * bb.mLength * TDMath.cosd( bb.mClino );
              TDVector w2 = new TDVector( h * TDMath.sind( bb.mBearing ), h * TDMath.cosd( bb.mBearing ), bb.mLength * TDMath.sind( bb.mClino ) );
	      float l2 = w2.Length();
              w2.plusEqual( w1 );                      // W1 + W2
	      for ( int h3 = 0; h3<n3; ++h3 ) {
                bb = mShots.get( k3 + h3 );
                h = sign3 * bb.mLength * TDMath.cosd( bb.mClino );
                TDVector w3 = new TDVector( h * TDMath.sind( bb.mBearing ), h * TDMath.cosd( bb.mBearing ), bb.mLength * TDMath.sind( bb.mClino ) );
	        float l3 = w3.Length();
                w3.plusEqual( w2 );                    // W1 + W2 + W3
	    	// misclosure (percent of length), the factor 1 / (10*RAD2DEG) acoounts for the same in makeHistogramBitmap()
		// 52 = 3*sqrt(3) * 100 / 10
                errors3[(h1*n2+h2)*n3+h3] = 52 * w3.Length() / ( (l1+l2+l3)*TDMath.RAD2DEG);
		// Log.v("DistoX", "error " + errors3[(h1*n2+h2)*n3+h3] + " " + l1 + " " + l2 + " " + l3 );
	      }
	    }
          }
          hist2.setImageBitmap( CalibCoeffDialog.makeHistogramBitmap( errors3, 400, 100, 40, 10, TDColor.MID_GRAY ) );
          setTitle( b0 + " " + b2 + " " + b3 );
        } else {
          hist2.setImageBitmap( null );
	}
      }
    } else {
      hist1.setImageBitmap( null );
      hist2.setImageBitmap( null );
    }
  }
}

