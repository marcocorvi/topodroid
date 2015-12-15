/** @file DrawingAreaDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch line attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.graphics.Paint;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.graphics.Canvas;
import android.graphics.Bitmap;

import android.util.FloatMath;

public class DrawingAreaDialog extends Dialog
                               implements View.OnClickListener
{
  private DrawingAreaPath mArea;
  private DrawingActivity mParent;
  private boolean mOrientable;

  private CheckBox mCBvisible;

  // private SeekBar  mSeekBar;
  // private ImageView mIVorientation;
  // private Bitmap mBitmap;
  // private Canvas mCanvas;
  private OrientationWidget mOrientationWidget; 

  private Button   mBtnOk;

  public DrawingAreaDialog( DrawingActivity context, DrawingAreaPath line )
  {
    super( context );
    mParent = context;
    mArea = line;
    mOrientable = DrawingBrushPaths.mAreaLib.isSymbolOrientable( mArea.mAreaType );
    // mBitmap = Bitmap.createBitmap( 40, 40, Bitmap.Config.ARGB_8888);
    // mCanvas = new Canvas( mBitmap );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_area_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( String.format( mParent.getResources().getString( R.string.title_draw_area ),
              DrawingBrushPaths.mAreaLib.getSymbolName( mArea.mAreaType ) ) );

    mOrientationWidget = new OrientationWidget( this, mOrientable, mArea.mOrientation );

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

    mCBvisible = (CheckBox) findViewById( R.id.area_visible );
    mCBvisible.setChecked( mArea.isVisible() );

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingAreaDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      mArea.setVisible( mCBvisible.isChecked() );
      if ( mOrientable ) {
        mArea.setOrientation( mOrientationWidget.mOrient );
      }
    }
    dismiss();
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

}

