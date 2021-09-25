/* @file DialogSketches.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D sketch list dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

class DialogSketches extends MyDialog 
                     implements View.OnClickListener
                     // , OnItemClickListener
{
  // private Button mBtnOk;

  private TopoGL mApp;
  private GlRenderer mRenderer;
  // private List< GlSketch > sketches;

  public DialogSketches( Context context, TopoGL app, GlRenderer renderer )
  {
    super( context, R.string.DialogSketches );
    mApp      = app;
    mRenderer = renderer;
    // sketches = renderer.getSketches();
    // TDLog.v("TopoGL sketches " + sketches.size() );
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState );
    initLayout( R.layout.cave3d_sketches_dialog, R.string.ctitle_sketches );

    Button mLoad  = (Button) findViewById( R.id.btn_load );
    Button mBind  = (Button) findViewById( R.id.btn_bind );
    Button mOk    = (Button) findViewById( R.id.btn_ok );
    // Button mClose = (Button) findViewById( R.id.btn_close );
    mLoad.setOnClickListener( this );
    mBind.setOnClickListener( this );
    mOk.setOnClickListener( this );
    // mClose.setOnClickListener( this );

    SketchAdapter sketchAdapter = new SketchAdapter( mContext, mApp, R.layout.sketch_row, new ArrayList<GlSketch>() );

    // TDLog.v("sketch adapter size " + sketchAdapter.size() );

    ListView list = (ListView) findViewById( R.id.list );
    list.setAdapter( sketchAdapter );
    list.setDividerHeight( 2 );
    // list.invalidate();

    for ( GlSketch sketch : mRenderer.getSketches() ) {
      sketchAdapter.addSketch( sketch );
    }

  }

  @Override
  public void onClick(View view)
  {
    if ( view.getId() == R.id.btn_load ) {
      mApp.loadSketch();
    } else if ( view.getId() == R.id.btn_bind ) {
      GlSketch.rebindTexture();
    } else if ( view.getId() == R.id.btn_ok ) {
      mRenderer.updateSketches();
    }
    dismiss();
  }


}

