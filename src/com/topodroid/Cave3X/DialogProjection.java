/* @file DialogProjection.java
 *
 * @author marco corvi
 * @date mar 2018
 *
 * @brief Cave3D projection parameters
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.Cave3X;

// import com.topodroid.Cave3X.R;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.SeekBar;

class DialogProjection extends Dialog 
                    implements View.OnClickListener
{
  private GlRenderer mRenderer;
  private Context mContext;

  private SeekBar mFocal; // focal / size
  // private SeekBar mNear;
  // private SeekBar mFar;

  public DialogProjection( Context context, GlRenderer renderer )
  {
    super( context );
    mContext  = context;
    mRenderer = renderer;
  }

  private void setProgress( SeekBar sb, float val, float min, float max )
  {
    sb.setProgress( (int)( 1000*(val-min)/(max-min) ) );
  }
 
  private float getProgress( SeekBar sb, float min, float max )
  {
    return min + sb.getProgress()/1000.0f * ( max - min );
  }

  private void setProgressLog( SeekBar sb, float val, float min, float max )
  {
    sb.setProgress( (int)(1000*(Math.log(val)-Math.log(min))/( Math.log(max)-Math.log(min) ) ) );
  }
 
  private float getProgressLog( SeekBar sb, float min, float max )
  {
    return (float)Math.exp( Math.log(min) + sb.getProgress()/1000.0f * ( Math.log(max) - Math.log(min) ) );
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.cave3d_projection_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mFocal = (SeekBar) findViewById( R.id.focal );
    // mNear  = (SeekBar) findViewById( R.id.near );
    // mFar   = (SeekBar) findViewById( R.id.far );

    TextView tvFocal = (TextView) findViewById( R.id.tv_focal );
    // TextView tvNear  = (TextView) findViewById( R.id.tv_near  );
    // TextView tvFar   = (TextView) findViewById( R.id.tv_far   );

    if ( mRenderer.projectionMode == GlRenderer.PROJ_PERSPECTIVE ) {
      setTitle( R.string.proj_perspective );
      setProgressLog( mFocal, GlRenderer.FOCAL,  GlRenderer.FOCAL_MIN,  GlRenderer.FOCAL_MAX );
      // setProgress( mNear,  GlRenderer.NEAR_P, GlRenderer.NEAR_P_MIN, GlRenderer.NEAR_P_MAX );
      // setProgress( mFar,   GlRenderer.FAR_P,  GlRenderer.FAR_P_MIN,  GlRenderer.FAR_P_MAX );
      tvFocal.setText( R.string.proj_focal );
    } else {
      setTitle( R.string.proj_orthographic );
      setProgress( mFocal, GlRenderer.SIDE,   GlRenderer.SIDE_MIN,   GlRenderer.SIDE_MAX );
      // setProgress( mNear,  GlRenderer.NEAR_O, GlRenderer.NEAR_O_MIN, GlRenderer.NEAR_O_MAX );
      // setProgress( mFar,   GlRenderer.FAR_O,  GlRenderer.FAR_O_MIN,  GlRenderer.FAR_O_MAX );
      tvFocal.setText( R.string.proj_side );
    }
    // tvNear.setText( R.string.proj_near );
    // tvFar.setText( R.string.proj_far );

    Button btn = (Button) findViewById( R.id.btn_ok );
    btn.setOnClickListener( this );
    btn = (Button) findViewById( R.id.btn_cancel );
    btn.setOnClickListener( this );

  }

  @Override
  public void onClick(View view)
  {
    if ( view.getId() == R.id.btn_ok ) {
      if ( mRenderer.projectionMode == GlRenderer.PROJ_PERSPECTIVE ) {
        GlRenderer.FOCAL  = getProgressLog( mFocal, GlRenderer.FOCAL_MIN,  GlRenderer.FOCAL_MAX );
        // GlRenderer.NEAR_P = getProgress( mNear,  GlRenderer.NEAR_P_MIN, GlRenderer.NEAR_P_MAX );
        // GlRenderer.FAR_P  = getProgress( mFar,   GlRenderer.FAR_P_MIN,  GlRenderer.FAR_P_MAX );
        // if ( GlRenderer.FAR_P < GlRenderer.NEAR_P + 1 ) GlRenderer.FAR_P = GlRenderer.NEAR_P + 1;
        mRenderer.makePerspectiveMatrix();
      } else {
        GlRenderer.SIDE   = getProgress( mFocal, GlRenderer.SIDE_MIN,   GlRenderer.SIDE_MAX );
        // GlRenderer.NEAR_O = getProgress( mNear,  GlRenderer.NEAR_O_MIN, GlRenderer.NEAR_O_MAX );
        // GlRenderer.FAR_O  = getProgress( mFar,   GlRenderer.FAR_O_MIN,  GlRenderer.FAR_O_MAX );
        // if ( GlRenderer.FAR_O < GlRenderer.NEAR_O + 1 ) GlRenderer.FAR_O = GlRenderer.NEAR_O + 1;
        mRenderer.makeOrthographicMatrix();
      }
    }
    dismiss();
  }

}

