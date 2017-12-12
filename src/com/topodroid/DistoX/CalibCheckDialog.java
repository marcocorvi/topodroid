/* @file CalibCheckDialog.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid DistoX calibration-check shots dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;

import android.app.Dialog;
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
import android.view.ViewGroup.LayoutParams;

import android.graphics.Bitmap;

// import android.util.Log;

public class CalibCheckDialog extends MyDialog
                              implements OnItemClickListener
{
  private SurveyWindow mParent;
  private List< DBlock > mShots;

  // private ImageView hist0;
  private ImageView hist1;
  private ImageView hist2;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;

  public CalibCheckDialog( Context context,
                           SurveyWindow parent,
                           List< DBlock > shots )
  {
    super( context, R.string.CalibCheckDialog );
    mParent = parent;
    mShots  = shots;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.calib_check_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    // hist0 = (ImageView) findViewById( R.id.histogram0 );
    hist1 = (ImageView) findViewById( R.id.histogram1 );
    hist2 = (ImageView) findViewById( R.id.histogram2 );

    mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );
    
    setTitle( R.string.title_calib_check );
    for ( DBlock blk : mShots ) {
      mArrayAdapter.add( blk.toShortString(true) );
    }
  }


  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long idx )
  {
    CharSequence item = ((TextView) view).getText();
    String str = item.toString();
    int len = str.indexOf(" ");
    int id = Integer.parseInt( str.substring(0, len) );
    DBlock blk = null;
    float x=0, y=0, z=0; // vector sum of the data in the leg
    int n1 = 0;          // number of data in the leg
    int k1 = 0;          // index of the leg
    int k  = 0;
    for ( DBlock b : mShots ) {
      if ( b.mId == id ) {
        if ( b.type() == DBlock.BLOCK_MAIN_LEG ) {
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
        if ( b.type() == DBlock.BLOCK_MAIN_LEG ) {
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
      Vector v0 = new Vector( x, y, z ); // unit vector along the leg
      float l0 = v0.Length();            // length of leg vector
      v0.normalize();
      boolean in_leg = false;
      int n2 = 0; // number of data in the opposite leg
      int k2 = 0; // index of the opposite leg
      k = 0;
      for ( DBlock b : mShots ) {
        if ( ! in_leg ) {
          if ( b.type() == DBlock.BLOCK_MAIN_LEG && blk.mFrom.equals( b.mTo ) && blk.mTo.equals( b.mFrom ) ) {
            k2 = k;
            in_leg = true;
            ++n2;
          }
        } else {
          if ( b.type() == DBlock.BLOCK_MAIN_LEG ) {
            break;
          } else {
            ++n2;
          }
        }
        ++k;
      }
      float errors1[] = new float[n1]; // angle differences between data in leg and leg average [radians]
      for ( k = 0; k<n1; ++k ) {
        DBlock b = mShots.get( k1 + k );
        float h = b.mLength * TDMath.cosd( b.mClino );
        Vector v1 = new Vector( h * TDMath.sind( b.mBearing ), h * TDMath.cosd( b.mBearing ), b.mLength * TDMath.sind( b.mClino ) );
        v1.normalize();
        errors1[k] = (float)(Vector.arc_distance( v0, v1 ));
      }
      hist1.setImageBitmap( CalibCoeffDialog.makeHistogramBitmap( errors1, 400, 100, 40, 50, TDColor.FIXED_ORANGE ) );
      if ( n2 > 0 ) {
        float errors2[] = new float[n2*n1];
        for ( k = 0; k<n1; ++k ) {
          DBlock bb = mShots.get( k1 + k );
          float h = bb.mLength * TDMath.cosd( bb.mClino );
          Vector w1 = new Vector( h * TDMath.sind( bb.mBearing ), h * TDMath.cosd( bb.mBearing ), bb.mLength * TDMath.sind( bb.mClino ) );
          for ( int kk = 0; kk<n2; ++kk ) {
            bb = mShots.get( k2 + kk );
            h = bb.mLength * TDMath.cosd( bb.mClino );
            Vector w2 = new Vector( h * TDMath.sind( bb.mBearing ), h * TDMath.cosd( bb.mBearing ), bb.mLength * TDMath.sind( bb.mClino ) );
            w2.plusEqual( w1 );                      // W1 + W2
            w2.minusEqual( v0.times( v0.dot(w2) ) ); // (W1+W2) - V0 [ V0 * (W1+W2) ] part orthogonal to V0
            errors2[k*n2+kk] = w2.Length() / l0;     // angle difference
          }
        }
        hist2.setImageBitmap( CalibCoeffDialog.makeHistogramBitmap( errors2, 400, 100, 40, 10, TDColor.LIGHT_GRAY ) );
      } else {
        hist2.setImageBitmap( null );
      }
    } else {
      hist1.setImageBitmap( null );
      hist2.setImageBitmap( null );
    }
  }
}

