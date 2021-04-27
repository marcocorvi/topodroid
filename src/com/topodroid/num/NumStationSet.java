/* @file NumStationSet.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey stations container (RB-tree)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.common.PlotType;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

// import android.util.Log;

class NumStationSet
{
// -------------------------------------------------------------
  static final private boolean BLACK = true;
  static final private boolean RED = false;


  static private int compare( String s1, String s2 )
  { 
    int l1 = s1.length();
    int l2 = s2.length();
    int kk = Math.min(l1, l2);
    for ( int k=0; k < kk; ++k ) {
      if ( s1.charAt(k) < s2.charAt(k) ) return -1;
      if ( s1.charAt(k) > s2.charAt(k) ) return +1;
    }
    if ( l1 < l2 ) return -1;
    if ( l1 > l2 ) return +1;
    return 0;
  }

  // stations are put on a list 
  // but they have references for the parent and the left/right children (that form an RB tree)
  //
  private class NumStationNode
  {
    NumStationNode parent;
    NumStationNode left;
    NumStationNode right;
    boolean color;
    NumStation value;

    NumStationNode( NumStation v )
    {
      parent = null;
      left = null;
      right = null;
      color = RED;
      value = v;
    }

    // void dump( int indent )
    // {
    //   for ( int k = indent; k > 0; --k ) {
    //     System.out.print( "  ");
    //   }
    //   System.out.println( toString() );
    //   if ( left  != null ) { System.out.print("L: "); left.dump( indent+1 ); }
    //   if ( right != null ) { System.out.print("R: "); right.dump( indent+1 ); }
    // }

    // public String toString() { return value + ( color ? "b" : "r"); }

    // boolean checkRB()
    // {
    //   if ( ! color ) {
    //     return ( left == null ) || ( left.color && left.checkRB() ) &&
    //            ( right == null ) || ( right.color && right.checkRB() );
    //   } else {
    //     return ( left == null ) || ( left.checkRB() ) &&
    //            ( right == null ) || ( right.checkRB() );
    //   }
    // }

    // int pathLength()
    // {
    //   int left_length = ( left == null )? 0 : ( left.pathLength() + ( left.color? 1 : 0 ));
    //   int right_length = ( right == null )? 0 : ( right.pathLength() + ( right.color? 1 : 0 ));
    //   return ( left_length > right_length )? left_length : right_length;
    // }

    // boolean checkPath()
    // {
    //   int left_length = ( left == null )? 0 : ( left.pathLength() + ( left.color? 1 : 0 ));
    //   int right_length = ( right == null )? 0 : ( right.pathLength() + ( right.color? 1 : 0 ));
    //   if ( left_length != right_length ) return false;
    //   return ( left == null || left.checkPath() ) && ( right == null || right.checkPath() );
    // }

    NumStation get( String name )
    {
      int c = compare( value.name, name );
      // Log.v("DistoX", value.name + " get " + name + " " + c + " left " + ((left==null)? "null" : left.value.name) +
      //          " right " + ((right==null)? "null" : right.value.name ) );

      if ( c == 0 ) return value;
      if ( c < 0 ) return ( left == null )? null : left.get( name );
      return ( right == null )? null : right.get( name );
    }


    // initialize the subtree at this node with a distance value "p" (supposedly large)
    void initShortPathDist( ArrayList<NumShortpath> paths, float p ) 
    { 
      // value.mShortpathDist = new NumShortpath( 0, p, 0 );
      paths.add( new NumShortpath( value, 0, p, 0 ) );
      if ( left  != null ) left.initShortPathDist( paths, p );
      if ( right != null ) right.initShortPathDist( paths, p );
    }

    // used to reset the flag "has 3D Coords" to stations in a station set
    void reset3DCoords( )
    {
      value.clearHas3DCoords( );
      if ( left  != null ) left.reset3DCoords( );
      if ( right != null ) right.reset3DCoords( );
    }

    void setAzimuths()
    {
      value.setAzimuths();
      if ( left  != null ) left.setAzimuths();
      if ( right != null ) right.setAzimuths();
    }

    /* traverse the tree and put on the stack all the NuMStation that have parent st
     * @param st    hiding station
     * @param dh    variation of "hidden" field
     * @param stack changed stations [output]
     */
    void updateHidden( NumStation st, int dh, Stack<NumStation> stack )
    {
      if ( value.mParent == st ) {
	// Log.v("DistoXX", "hide station " + value.name );
        value.mHidden += dh;
        stack.push( value );
      // } else {
      // Log.v("DistoXX", "show station " + value.name );
      }
      if ( left != null ) left.updateHidden( st, dh, stack );
      if ( right != null ) right.updateHidden( st, dh, stack );
    }

  }

  private ArrayList< NumStation > mStations;
  private NumStationNode mRoot; // tree root

  NumStationSet() 
  { 
    mRoot = null;
    mStations = new ArrayList<>();
  }

  NumStation getClosestStation( long type, double x, double y ) 
  {
    NumStation ret = null;
    double dist2 = 16000000; // max 100 m 
    if ( type == PlotType.PLOT_PLAN ) {
      for ( NumStation st : mStations ) {
        double d2 = (st.e-x)*(st.e-x) + (st.s-y)*(st.s-y);
        if ( d2 < dist2 ) {
          dist2 = d2;
          ret = st;
        }
      }
    } else if ( PlotType.isProfile( type ) ) {
      for ( NumStation st : mStations ) {
        double d2 = (st.h-x)*(st.h-x) + (st.v-y)*(st.v-y);
        if ( d2 < dist2 ) {
          dist2 = d2;
          ret = st;
        }
      }
    }
    return ret;
  }
  
  // initialize the tree with a distance value "p" (supposedly large)
  void initShortestPath( ArrayList<NumShortpath> paths, float p ) 
  {
    if ( mRoot == null ) return;
    mRoot.initShortPathDist( paths, p );
  }

  // reset the flag "has 3D Coords" in the stations of this set
  void reset3DCoords( ) 
  {
    if ( mRoot != null ) mRoot.reset3DCoords( );
  }

  void setAzimuths( )
  {
    if ( mRoot != null ) mRoot.setAzimuths();
  }

  void updateHidden( NumStation st, int dh, Stack<NumStation> stack )
  {
    if ( mRoot != null ) mRoot.updateHidden( st, dh, stack );
  }

  int size() { return mStations.size(); }

  List< NumStation > getStations() { return mStations; }

  boolean addStation( NumStation v )
  {
    // Log.v("DistoX", "add station " + v.name + " root " + ((mRoot != null)? mRoot.value.name : "null") );
    boolean ret = true;
    NumStationNode n = new NumStationNode( v );
    if ( mRoot == null ) {
      mRoot = n;
      mRoot.color = BLACK;
    } else {
      for ( NumStationNode n0 = mRoot; ; ) {
        int c = compare( n0.value.name, v.name );
        if ( c < 0 ) {
          if ( n0.left == null ) {
            n0.left = n;
            n.parent = n0;
            break;
          } else {
            n0 = n0.left;
          }
        } else if ( c > 0 
                  //  || ( c == 0 && TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) 
                  ) {
          if ( n0.right == null ) {
            n0.right = n;
            n.parent = n0;
            break;
          } else {
            n0 = n0.right;
          }
        } else { 
          // Log.v("DistoX", "Double insertion of station " + v.name );
          ret = false;
          break;
        }
      }
      // rebalance
      if ( ret ) insert_case1( n );
    }
    if ( ret ) mStations.add( v );
    // Log.v("DistoX", "added station " + v.name + " root " + mRoot.value.name );
    return ret;
  }

  NumStation getStation( String name ) 
  {
    // Log.v("DistoX", "stations set size " + size() );
    return ( mRoot == null )? null : mRoot.get( name );
  }

  // -----------------------------------------------------
  // private helper methods
  //
  private void insert_case1( NumStationNode n ) 
  {
    if ( n.parent == null ) {
      n.color = BLACK;
    } else {
      insert_case2( n );
    }
  }

  // n.parent != null
  private void insert_case2( NumStationNode n ) 
  {
    if ( n.parent.color ) return; // isBlack( n.parent )
    insert_case3( n );
  }

  // n.parent != null && n.parent RED
  private void insert_case3( NumStationNode n )
  {
    NumStationNode u = uncle( n );
    if ( /* u != null && */ isRed( u ) ) {
      n.parent.color = BLACK;
      u.color = BLACK;
      NumStationNode g = grandparent( n );
      g.color = RED;
      insert_case1( g );
    } else {
      insert_case4( n );
    }
  }

  // n.parent == g.left && n = n.parent.right ( n.parent RED )
  // or symmetric
  private void insert_case4( NumStationNode n )
  {
    NumStationNode p = n.parent;
    NumStationNode g = p.parent;
    if ( isRight( n ) && isLeft( p ) ) { // rotate_left( n, p, g );
      n.parent = g;
      if ( g != null ) {
        g.left = n;
      } else {
        mRoot = n;
      }
      if ( n.left != null ) n.left.parent = p;
      p.right = n.left;
      p.parent = n;
      n.left  = p;

      n = n.left; // continue with n.left
    } else if ( isLeft( n ) && isRight( p ) ) { // rotate_right( n, p, g );
      n.parent = g;
      if ( g != null ) {
        g.right = n;
      } else {
        mRoot = n;
      }
      if ( n.right != null ) n.right.parent = p;
      p.left  = n.right;
      p.parent = n;
      n.right = p;

      n = n.right; // continue with n.right
    }
    insert_case5( n );
  }

  // n.parent RED but n.uncle BLACK
  private void insert_case5( NumStationNode n )
  {
    NumStationNode g = grandparent( n );
    NumStationNode p = n.parent;
    p.color = BLACK;
    g.color = RED;
    NumStationNode gp = g.parent;
    if ( gp != null ) {
      if ( g == gp.left ) {
	gp.left = p;
      } else {
	gp.right = p;
      }
    } else {
      mRoot = p;
    }
    p.parent = gp;
    if ( isLeft( n ) ) { // rotate_right( n.parent, g, g.parent );
      // assert( p == g.left );
      g.left = p.right;
      if ( p.right != null ) p.right.parent = g;
      p.right = g;
      g.parent = p;
    } else { // rotate_left( n.parent, g, g.parent );
      // assert( p == g.right );
      g.right = p.left;
      if ( p.left != null ) p.left.parent = g;
      p.left = g;
      g.parent = p;
    }
  }

  private NumStationNode grandparent( NumStationNode n )
  {
    return ( n != null && n.parent != null )?  n.parent.parent : null;
  }

  private NumStationNode uncle( NumStationNode n ) 
  {
    NumStationNode p = n.parent;
    if ( p == null ) return null;
    NumStationNode g = p.parent;
    if ( g == null ) return null;
    return ( p == g.left )? g.right : g.left;
  }

  private boolean isBlack( NumStationNode n ) { return ( n == null ) || n.color; }
  private boolean isRed( NumStationNode n ) { return ( n != null ) && (! n.color ); }

  // prereq. n.parent != null
  private boolean isLeft( NumStationNode n ) { return ( n == n.parent.left ); }
  private boolean isRight( NumStationNode n ) { return ( n == n.parent.right ); }

  // boolean check()
  // {
  //   if ( isRed(mRoot) ) return false;
  //   if ( mRoot != null && ! mRoot.checkRB() ) return false;
  //   if ( mRoot != null && ! mRoot.checkPath() ) return false;
  //   return true;
  // }
}
