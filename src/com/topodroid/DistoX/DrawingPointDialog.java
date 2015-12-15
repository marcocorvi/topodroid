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
 */
package com.topodroid.DistoX;

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
import android.view.ViewGroup.LayoutParams;

// import android.widget.SeekBar;
// import android.widget.SeekBar.OnSeekBarChangeListener;
// import android.graphics.Bitmap;
// import android.graphics.Canvas;

import android.graphics.Paint;

public class DrawingPointDialog extends Dialog
                               implements View.OnClickListener
                               // , View.OnLongClickListener
{
  private DrawingPointPath mPoint;
  private DrawingActivity  mParent;
  private boolean mOrientable;

  // private TextView mTVtype;
  private EditText mEToptions;
  private EditText mETtext;
  private RadioButton mBtnScaleXS;
  private RadioButton mBtnScaleS;
  private RadioButton mBtnScaleM;
  private RadioButton mBtnScaleL;
  private RadioButton mBtnScaleXL;

  private OrientationWidget mOrientationWidget;
  // private SeekBar  mSeekBar;
  // private ImageView mIVorientation;
  // private Bitmap mBitmap;
  // private Canvas mCanvas;
 
  private Button   mBtnOk;

  public DrawingPointDialog( DrawingActivity context, DrawingPointPath point )
  {
    super( context );
    mParent = context;
    mPoint  = point;
    mOrientable = DrawingBrushPaths.mPointLib.isSymbolOrientable( mPoint.mPointType );
    // mBitmap = Bitmap.createBitmap( 40, 40, Bitmap.Config.ARGB_8888);
    // mCanvas = new Canvas( mBitmap );
  }

  // private void drawOrientation()
  // {
  //   if ( ! mOrientable ) return;
  //   int d = 20;
  //   // mTVorientation.setText( Integer.toString(mOrient) );
  //   mCanvas.drawColor( 0xff000000 );
  //   float c = TDMath.cosd( mOrient );
  //   float s = TDMath.sind( mOrient );
  //   float c135 = TDMath.cosd( mOrient+135 );
  //   float s135 = TDMath.sind( mOrient+135 );
  //   float c225 = TDMath.cosd( mOrient+225 );
  //   float s225 = TDMath.sind( mOrient+225 );
  //   float x1 = d+d*s;
  //   float y1 = d-d*c;
  //   Paint paint = DrawingBrushPaths.fixedBluePaint;
  //   mCanvas.drawLine( d-d*s, d+d*c, x1, y1, paint );
  //   mCanvas.drawLine( x1, y1, x1+10*s135, y1-10*c135, paint );
  //   mCanvas.drawLine( x1, y1, x1+10*s225, y1-10*c225, paint );
  //   mIVorientation.setImageBitmap( mBitmap );
  //   mIVorientation.invalidate();
  // }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_point_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    // mTVtype = (TextView) findViewById( R.id.point_type );
    mEToptions = (EditText) findViewById( R.id.point_options );
    mETtext    = (EditText) findViewById( R.id.point_text );


    setTitle( "POINT " + DrawingBrushPaths.mPointLib.getSymbolName( mPoint.mPointType ) );
    if ( DrawingBrushPaths.mPointLib.pointHasText( mPoint.mPointType ) ) {
      mETtext.setText( mPoint.getText() );
    } else {
      mETtext.setEnabled( false );
    }

    mOrientationWidget = new OrientationWidget( this, mOrientable, mPoint.mOrientation );

    // mSeekBar  = (SeekBar) findViewById( R.id.seekbar );
    // mIVorientation = (ImageView) findViewById( R.id.image );
    // if ( mOrientable ) {
    //   mIVorientation.setImageBitmap( mBitmap );
    //   drawOrientation();
    //   mSeekBar.setProgress( (mOrient+180)%360 );
    //   mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
    //     public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
    //       if ( fromUser ) {
    //         mOrient = 180 + progress;
    //         if ( mOrient >= 360 ) mOrient -= 360;
    //         drawOrientation();
    //       }
    //     }
    //     public void onStartTrackingTouch(SeekBar seekbar) { }
    //     public void onStopTrackingTouch(SeekBar seekbar) { }
    //   } );
    //   mSeekBar.setMax( 360 );
    // } else {
    //   mIVorientation.setVisibility( View.GONE );
    //   mSeekBar.setVisibility( View.GONE );
    // }

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

      if ( mOrientable ) {
        mPoint.setOrientation( mOrientationWidget.mOrient );
      }
      if ( DrawingBrushPaths.mPointLib.pointHasText( mPoint.mPointType ) ) {
        mPoint.setText( mETtext.getText().toString().trim() );
      }
    }
    dismiss();
  }

}

