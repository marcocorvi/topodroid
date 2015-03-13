/** @file DrawingPointDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch point attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121225 implemented erase
 * 20130825 added a field to edit the point text (for labels)
 * 20130829 added the buttons for the point orientation
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;

// import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ImageView;
import android.view.View;
// import android.graphics.drawable.Drawable;
// import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Matrix;

import android.util.FloatMath;

public class DrawingPointDialog extends Dialog
                               implements View.OnClickListener
                               , View.OnLongClickListener
{
  private DrawingPointPath mPoint;
  private DrawingActivity  mParent;
  private int mOrient;

  // private TextView mTVtype;
  private EditText mEToptions;
  private EditText mETtext;
  private RadioButton mBtnScaleXS;
  private RadioButton mBtnScaleS;
  private RadioButton mBtnScaleM;
  private RadioButton mBtnScaleL;
  private RadioButton mBtnScaleXL;

  private Button   mBtnLeft;
  private Button   mBtnRight;
  private TextView mTVorientation;
  private ImageView mIVorientation;
  // private Drawable mDrawable;
  private Bitmap mBitmap;
  private Canvas mCanvas;
 
  private Button   mBtnOk;
  // private Button   mBtnCancel;
  private Button   mBtnErase;

  public DrawingPointDialog( DrawingActivity context, DrawingPointPath point )
  {
    super( context );
    mParent = context;
    mPoint  = point;
    mOrient = (int)mPoint.mOrientation;
    // mDrawable = new Drawable();
    // mDrawable.setBounds( new Rect( -10, -10, 10, 10 ) );
    mBitmap = Bitmap.createBitmap( 40, 40, Bitmap.Config.ARGB_8888);
    mCanvas = new Canvas( mBitmap );
  }

  private void drawOrientation()
  {
    int d = 20;
    mTVorientation.setText( Integer.toString(mOrient) );
    mCanvas.drawColor( 0xff666666 );
    float c = FloatMath.cos( mOrient * TopoDroidUtil.GRAD2RAD);
    float s = FloatMath.sin( mOrient * TopoDroidUtil.GRAD2RAD);
    float c135 = FloatMath.cos( (mOrient+135) * TopoDroidUtil.GRAD2RAD);
    float s135 = FloatMath.sin( (mOrient+135) * TopoDroidUtil.GRAD2RAD);
    float c225 = FloatMath.cos( (mOrient+225) * TopoDroidUtil.GRAD2RAD);
    float s225 = FloatMath.sin( (mOrient+225) * TopoDroidUtil.GRAD2RAD);
    float x1 = d+d*s;
    float y1 = d-d*c;
    Paint paint = DrawingBrushPaths.highlightPaint;
    mCanvas.drawLine( d-d*s, d+d*c, x1, y1, paint );
    mCanvas.drawLine( x1, y1, x1+10*s135, y1-10*c135, paint );
    mCanvas.drawLine( x1, y1, x1+10*s225, y1-10*c225, paint );
    mIVorientation.setImageBitmap( mBitmap );
    mIVorientation.invalidate();
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_point_dialog);

    // mTVtype = (TextView) findViewById( R.id.point_type );
    mEToptions = (EditText) findViewById( R.id.point_options );
    mETtext    = (EditText) findViewById( R.id.point_text );


    setTitle( "POINT " + DrawingBrushPaths.mPointLib.getAnyPointName( mPoint.mPointType ) );
    if ( DrawingBrushPaths.mPointLib.pointHasText( mPoint.mPointType ) ) {
      mETtext.setText( mPoint.getText() );
    } else {
      mETtext.setEnabled( false );
    }

    mBtnLeft  = (Button) findViewById( R.id.left );
    mBtnRight = (Button) findViewById( R.id.right );
    mTVorientation = (TextView) findViewById( R.id.value );

    mIVorientation = (ImageView) findViewById( R.id.image );
    // mIVorientation.setImageDrawable( mDrawable );
    mIVorientation.setImageBitmap( mBitmap );
    drawOrientation();

    if ( DrawingBrushPaths.canRotate( mPoint.mPointType ) ) {
      mBtnLeft.setOnClickListener( this );
      mBtnRight.setOnClickListener( this );
      mBtnLeft.setOnLongClickListener( this );
      mBtnRight.setOnLongClickListener( this );
    } else {
      mBtnLeft.setEnabled( false );
      mBtnRight.setEnabled( false );
    }

    if ( mPoint.mOptions != null ) {
      mEToptions.setText( mPoint.mOptions );
    }

    mBtnScaleXS = (RadioButton) findViewById( R.id.point_scale_xs );
    mBtnScaleS  = (RadioButton) findViewById( R.id.point_scale_s  );
    mBtnScaleM  = (RadioButton) findViewById( R.id.point_scale_m  );
    mBtnScaleL  = (RadioButton) findViewById( R.id.point_scale_l  );
    mBtnScaleXL = (RadioButton) findViewById( R.id.point_scale_xl );
    switch ( mPoint.getScale() ) {
      case DrawingPointPath.SCALE_XS: mBtnScaleXS.setChecked( true ); break;
      case DrawingPointPath.SCALE_S:  mBtnScaleS.setChecked( true ); break;
      case DrawingPointPath.SCALE_M:  mBtnScaleM.setChecked( true ); break;
      case DrawingPointPath.SCALE_L:  mBtnScaleL.setChecked( true ); break;
      case DrawingPointPath.SCALE_XL: mBtnScaleXL.setChecked( true ); break;
    }

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    mBtnErase = (Button) findViewById( R.id.button_erase );
    mBtnErase.setOnClickListener( this );
  }

  public boolean onLongClick( View v ) 
  {
    Button b = (Button)v;
    if ( b == mBtnLeft ) {
      mOrient -= 10;
      if ( mOrient < 0 ) mOrient += 360;
      drawOrientation();
      return true;
    } else if ( b == mBtnRight ) {
      mOrient += 10;
      if ( mOrient >= 360 ) mOrient -= 360;
      drawOrientation();
      return true;
    }
    return false;
  } 

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingPointDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      if ( mEToptions.getText() != null ) {
        String options = mEToptions.getText().toString().trim();
        if ( options.length() > 0 ) mPoint.mOptions = options;
      }
      if ( mBtnScaleXS.isChecked() )      mPoint.setScale( DrawingPointPath.SCALE_XS );
      else if ( mBtnScaleS.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_S  );
      else if ( mBtnScaleM.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_M  );
      else if ( mBtnScaleL.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_L  );
      else if ( mBtnScaleXL.isChecked() ) mPoint.setScale( DrawingPointPath.SCALE_XL );

      if ( DrawingBrushPaths.canRotate( mPoint.mPointType ) ) {
        mPoint.setOrientation( mOrient );
      }
      if ( DrawingBrushPaths.mPointLib.pointHasText( mPoint.mPointType ) ) {
        mPoint.setText( mETtext.getText().toString().trim() );
      }
      dismiss();
    } else if ( b == mBtnErase ) {
      mParent.deletePoint( mPoint );
      dismiss();
    // } else if ( b == mBtnCancel ) {
    //   dismiss();
    } else if ( b == mBtnLeft ) {
      mOrient --;
      if ( mOrient < 0 ) mOrient += 360;
      drawOrientation();
    } else if ( b == mBtnRight ) {
      mOrient ++;
      if ( mOrient >= 360 ) mOrient -= 360;
      drawOrientation();
    } else {
      dismiss();
    }
  }

}

