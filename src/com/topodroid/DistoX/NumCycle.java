/** @file NumCycle.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction cycle of the survey net
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created 
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

// import android.util.Log;

public class NumCycle 
{
  int mMax;
  int mSize;
  NumBranch[] branches;
  // NumNode[]   nodes;
  int[]       dirs; // branch direction in the cycle
  float e, s, v;    // displacement = closure error
  float ce, cs, cv; // corrections

  NumCycle( int sz )
  {
    mMax = sz;
    mSize = 0;
    branches = new NumBranch[mMax];
    // nodes = new NumNode[mMax];
    dirs  = new int[mMax];
  }

  void addBranch( NumBranch branch, NumNode node )
  {
    if ( mSize < mMax ) {
      branches[mSize] = branch;
      // nodes[mSize] = node;
      dirs[mSize] = ( node == branch.n2 )? 1 : -1;
      // TopoDroidLog.Log( TopoDroidLog.LOG_NUM, "C add branch dir " + dirs[mSize] );
      // branch.dump();
      ++mSize;
    }
  }

  // NumNode getNode( int k ) 
  // {
  //   if ( k >= 0 && k < mSize ) return nodes[k];
  //   return null;
  // }

  // assumes that branches errors have been computed
  void computeError()
  {
    e = 0.0f;
    s = 0.0f;
    v = 0.0f;
    for ( int k=0; k<mSize; ++k ) {
      NumBranch br = branches[k];
      int dir = dirs[k];
      e += br.e * dir;
      s += br.s * dir;
      v += br.v * dir;
    }
    // NumNode nd = null;
    // for ( NumBranch br : branches ) {
    //   if ( nd == br.n2 ) {
    //     e -= br.e;
    //     s -= br.s;
    //     v -= br.v;
    //     nd = br.n1;
    //   } else {
    //     e += br.e;
    //     s += br.s;
    //     v += br.v;
    //     nd = br.n2;
    //   }
    // }
  }

  int branchIndex( NumBranch br )
  {
    for (int k = 0; k<mSize; ++k ) {
      if ( br == branches[k] ) return k;
    }
    return mSize;
  }

  boolean isBranchCovered( ArrayList<NumCycle> cycles )
  {
    for (int k=0; k<mSize; ++k ) {
      NumBranch br = branches[k];
      boolean found = false;
      for ( NumCycle cy : cycles ) {
        for ( int k1 = 0; k1 < cy.mSize; ++k1 ) {
          if ( br == cy.branches[k1] ) {
            found = true;
            break;
          }
        }
        if ( found ) break;
      }
      if ( ! found ) return false;
    }
    return true;
  }

}

