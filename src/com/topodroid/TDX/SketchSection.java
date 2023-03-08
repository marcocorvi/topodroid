/* @file SketchSection.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketching: section
 * 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.math.Point2D; // float X-Y

// import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;


public class SketchSection extends SketchPath
{
  public static final int SECTION_NONE       = -1;
  public static final int SECTION_LEG        = 0;
  public static final int SECTION_VERTICAL   = 1;
  public static final int SECTION_HORIZONTAL = 2;

  private int mId;  // section number ID
  private int nMaxLineId = 0;
  SketchPoint mP1;
  SketchPoint mP2;
  TDVector    mC;   // section center
  TDVector    mN;   // projection normal
  TDVector    mH;   // X unit rightward
  TDVector    mS;   // projection Y unit downward
  // 
  float mAlpha = 0; // projection rotation angle [degrees]
  float mMaxAlpha = 0;
  float mMinAlpha = 0;
  boolean mCanRotate = false;
  TDVector    mNp;  // section-plane normal
  TDVector    mSp;  // section-plane Y
  
  ArrayList< SketchLinePath > mLines = null; // lines, in the plane of the section
  private List< SketchFixedPath > mSectionGrid = null;
  private int mType = SECTION_NONE; // whether the section is vertical (meaningfol only for x-sections)

  /** cstr
   * @param id    section ID (0 for the leg-section)
   * @param c     section center
   * @param h     section H (X rightward) vector
   * @param s     section S (Y downward) vector
   * @param n     section N (Z entering) vector
   * @param type  section type
   * @note H,S,N is a right triple
   */
  public SketchSection( int id, TDVector c, TDVector h, TDVector s, TDVector n, int type )
  {
    super( SketchPath.SKETCH_PATH_SECTION, null ); // null Paint
    mId = id;
    nMaxLineId = 0;
    mP1 = null;
    mP2 = null;
    mC = c;
    mH = h;
    mS = s;
    mN = n;
    mLines = new ArrayList< SketchLinePath >();
    mNp = mN;
    mSp = mS;
    mType = type;
    TDLog.v("SKETCH legviews C " + mC.x + " " + mC.y + " " + mC.z );
  }

  /** cstr for X-sections
   * @param p1       first base point
   * @param p2       second base point
   * @param vertical whether the x-section is vertical or horizontal
   * @note the section center is the midpoint of the two base points
   *   - the H axis is always horizontal (from P1 to P2)
   *   - if the x-section is vertical 
   *       - the H axis is horizontal in the vertical plane containing P1 and P2 (from P1 to P2)
   *       - the S axis is vertical downward (negative Z-world)
   *       - the N axis is H ^ S
   *   - if the x-section is horizontal 
   *       - the H axis is in the horizontal plane containing the projections of P1 and P2 (from P1 to P2)
   *       - the N axis is vertical downward (negative Z-world)
   *       - the S axis is N ^ H
   */
  public SketchSection( int id, SketchPoint p1, SketchPoint p2, boolean vertical )
  {
    super( SketchPath.SKETCH_PATH_SECTION, null ); // null Paint
    mId = id;
    nMaxLineId = 0;
    mP1 = p1;
    mP2 = p2;
    if ( p1 != null && p2 != null ) {
      mC  = new TDVector( (p1.x+p2.x)/2, (p1.y+p2.y)/2, (p1.z+p2.z)/2 );
      float dx = p2.x - p1.x; // East
      float dy = p2.y - p1.y; // North
      float dz = p2.z - p1.z; // Upward
      TDLog.v("SKETCH section C " + mC.x + " " + mC.y + " " + mC.z );
      // TDLog.v("SKETCH section (vert " + vertical + ") " + dx + " " + dy + " " + dz );
      float dh = TDMath.sqrt( dx*dx + dy*dy );
      if ( vertical ) {
        mS = new TDVector(      0,     0, -1 );
        mH = new TDVector(  dx/dh, dy/dh,  0 );
        mN = new TDVector( -dy/dh, dx/dh,  0 ); // H ^ S
        mType = SECTION_VERTICAL;
      } else {
        mN = new TDVector(      0,     0, -1 ); // downward
        mH = new TDVector(  dx/dh,  dy/dh, 0 );
        mS = new TDVector(  dy/dh, -dx/dh, 0 ); // N ^ H
        mType = SECTION_HORIZONTAL;
      }
    } else {
      mC = new TDVector();
      mH = new TDVector( 1, 0, 0 );
      mS = new TDVector( 0, 1, 0 );
      mN = new TDVector( 0, 0, 1 );
    }
    mLines = new ArrayList< SketchLinePath >();
    mNp = mN;
    mSp = mS;
  }

  /** make the sections grid
   * @param size   grid (half) size 
   */
  void makeSectionGrid( int size )
  {
    mSectionGrid = Collections.synchronizedList(new ArrayList< SketchFixedPath >());
    Paint paint = BrushManager.fixedGridPaint;
    for ( int i=-size; i<=size; ++i ) {
      TDVector v1 = new TDVector( mC.x + i*mH.x - size*mS.x, mC.y + i*mH.y - size*mS.y, mC.z + i*mH.z - size*mS.z );
      TDVector v2 = new TDVector( mC.x + i*mH.x + size*mS.x, mC.y + i*mH.y + size*mS.y, mC.z + i*mH.z + size*mS.z );
      SketchFixedPath path = new SketchFixedPath( SketchPath.SKETCH_PATH_GRID, paint, v1, v2 );
      mSectionGrid.add( path );
    }
    for ( int j=-size; j<=size; ++j ) {
      TDVector v1 = new TDVector( mC.x - size*mH.x + size*mS.x, mC.y - size*mH.y + j*mS.y, mC.z - size*mH.z + j*mS.z );
      TDVector v2 = new TDVector( mC.x + size*mH.x + size*mS.x, mC.y + size*mH.y + j*mS.y, mC.z + size*mH.z + j*mS.z );
      SketchFixedPath path = new SketchFixedPath( SketchPath.SKETCH_PATH_GRID, paint, v1, v2 );
      mSectionGrid.add( path );
    }
  }

  /** set the projection rotation-angle min-max
   * @param theta   leg clino [degree]
   */
  void setZeroAlpha( float theta )
  {
    mAlpha    = theta;
    mMaxAlpha = theta + 30; if ( mMaxAlpha >  90 ) mMaxAlpha =  90;
    mMinAlpha = theta - 30; if ( mMinAlpha < -90 ) mMinAlpha = -90;
    mCanRotate = true;
  }

  /** set the projection rotation-angle
   * @param alpha rotation angle [degrees]
   */
  private void setAlpha( float alpha )
  {
    mAlpha = alpha;
    if ( mAlpha > mMaxAlpha ) { mAlpha = mMaxAlpha; }
    else if ( mAlpha < mMinAlpha ) { mAlpha = mMinAlpha; }

    float c = TDMath.cosd( mAlpha );
    float s = TDMath.sind( mAlpha );
    // TDLog.v("Section set alpha " + mAlpha + " [" + + mMinAlpha + ", " + mMaxAlpha + "] c " + c + " s " + s );
    mN = new TDVector( c*mNp.x - s*mSp.x, c*mNp.y - s*mSp.y, c*mNp.z - s*mSp.z );
    mS = new TDVector( c*mSp.x + s*mNp.x, c*mSp.y + s*mNp.y, c*mSp.z + s*mNp.z );
  }

  float getRotation( ) { return mAlpha; }

  /** change the projection angle - only leg-view
   * @param delta angle change [degree]
   */
  boolean changeAlpha( int delta )
  { 
    if ( ! mCanRotate ) return false;
    setAlpha( mAlpha + delta );
    return true;
  }

  /** @return the section ID
   */
  int getId() { return mId; }

  /** @return the section type
   */
  int getType() { return mType; }

  /** increase the max line ID and return it
   * @return the max line-ID
   */
  int getNextLineId() { return ++nMaxLineId; }

  /** @return the world 3D vector of a canvas point
   * @param xw    H world coord
   * @param yw    S world coord
   */
  TDVector toTDVector( float xw, float yw )
  {
    TDVector w = new TDVector( xw*mH.x + yw*mS.x, xw*mH.y + yw*mS.y, xw*mH.z + yw*mS.z );
    float g = ( w.dot( mNp ) ) / TDMath.cosd( mAlpha );
    return mC.plus( new TDVector( w.x - g * mN.x, w.y - g * mN.y, w.z - g * mN.z ) );
  }

  /** delete a line
   * @param line   line to delete
   */
  void deleteLine( SketchLinePath line ) { if ( mLines != null ) mLines.remove( line ); }

  /** append a line
   * @param line   line to append
   */
  void appendLine( SketchLinePath line ) { if ( mLines != null ) mLines.add( line ); }

  @Override
  public int size() { return (mLines == null)? 0 : mLines.size(); }

  /** clear the section
   */
  void clear() { if ( mLines != null) mLines.clear(); }

  /** @return true if this section has the specified base points
   * @param p1    first section base-point
   * @param p2    second section base-point
   */
  boolean hasBase( SketchPoint p1, SketchPoint p2 )
  {
    if ( p1 == mP1 && p2 == mP2 ) return true;
    if ( p2 == mP1 && p1 == mP2 ) return true;
    return false;
  }

  public void drawGrid( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y )
  {
    if ( mSectionGrid == null ) return;
    for ( SketchPath line : mSectionGrid ) line.draw( canvas, mm, C, X, Y );
  }

  public void draw( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y )
  {
    if ( mLines == null ) return;
    for ( SketchPath line : mLines ) line.draw( canvas, mm, C, X, Y );
  }

  public void drawPoints( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float r )
  {
    if ( mLines == null ) return;
    for ( SketchLinePath line : mLines ) line.drawPoints( canvas, mm, C, X, Y, r );
  }

  void addEraseCommand( EraseCommand cmd ) 
  { 
    TDLog.v("TODO Add erase command ");
  }
  
  /** erase at a position, in the current scrap
   * @param c   world point
   * @param eraseCmd  erase command
   * @param size  eraser size
   *
   */
  void eraseAt( TDVector v, EraseCommand eraseCmd, float size ) 
  {
    // dataCheck( "V in plane", ( Math.abs( v.minus(mC).dot(mN) ) < 0.001 ) );
    if ( mLines == null ) return;
    for ( SketchLinePath line : mLines ) {
      // line.checkInPlane( mC, mN );
      line.eraseAt( v, eraseCmd, size );
    }
  }

  public void undo () { TDLog.v("TODO undo"); }

  public void redo () { TDLog.v("TODO redo"); }

  boolean hasMoreRedo() { return false; }

  boolean hasMoreUndo() { return false; }

  boolean setRangeAt( float x, float y, float zoom, float size ) { TDLog.v("TODO set range at"); return false; }


  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  @Override
  public void toDataStream( DataOutputStream dos ) throws IOException 
  {
    TDLog.v("WRITE section " + mId + " type " + mType + " max line-ID " + nMaxLineId + " lines " + mLines.size() );
    mC.dump("  C");
    mH.dump("  H");
    mS.dump("  S");
    mN.dump("  N");
    // TDLog.Error( "ERROR Sketch Section toDataStream ");
    dos.write( 'X' );
    dos.writeInt( mId );
    dos.writeInt( mType );
    if ( mId > 0 ) {
      toDataStream( dos, mP1 );
      toDataStream( dos, mP2 );
    } else {
      // dos.writeFloat( mAlpha );
    }
    toDataStream( dos, mC );
    toDataStream( dos, mN );
    toDataStream( dos, mH );
    toDataStream( dos, mS );
    dos.writeInt( mLines.size() );
    for ( SketchLinePath line : mLines ) {
      line.toDataStream( dos );
    }
  }

  /** read from a stream
   * @param cmd  command manager - use to add line to this section
   * @param dis  input stream
   * @param version file version
   * @return section id
   * @note the command manager must be opened on this section
   */
  @Override
  public int fromDataStream( SketchCommandManager cmd, DataInputStream dis, int version ) throws IOException
  {
    float alpha = 0;
    dataCheck( "SECTION", ( dis.read() == 'X' ) );
    mId = dis.readInt();
    mType = dis.readInt();
    nMaxLineId = 0;
    Paint paint = SketchSurface.getSectionLinePaint( mId ); // line-paint
    if ( mId > 0 ) {
      TDVector v1 = tdVectorFromDataStream( dis );
      mP1 = new SketchPoint( v1, null );
      TDVector v2 = tdVectorFromDataStream( dis );
      mP2 = new SketchPoint( v2, null );
    } else {
      mP1 = null;
      mP2 = null;
      // alpha = dis.readFloat();
    }
    mC = tdVectorFromDataStream( dis );
    mN = tdVectorFromDataStream( dis );
    mH = tdVectorFromDataStream( dis );
    mS = tdVectorFromDataStream( dis );
    if ( mId > 0 ) {
      cmd.addSection( this );
      cmd.openSection( this );
    } else {
      // setAlpha( alpha );
    }
    int nln = dis.readInt();
    for ( int k=0; k<nln; ++k ) {
      SketchLinePath line = new SketchLinePath( -1, mId, paint ); // line ID initialized to -1, is read from stream
      line.fromDataStream( cmd, dis, version );
      if ( line.size() > 1 ) { 
        if ( line.getId() > nMaxLineId ) nMaxLineId = line.getId();
        cmd.addLine( mId, line );
      } else {
        TDLog.Error("Sketch line with " + line.size() + " points" );
      }
    }
    TDLog.v("READ section " + mId + " max line id " + nMaxLineId + " lines " + nln );
    return mId;
  }

}
