/* @file NumCycle.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction cycle of the survey net
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

// import android.util.Log;

class NumCycle
{
  class NumCycleBranch
  {
    NumBranch mBranch;
    // NumNode   mNode;
    int       mDir;    // branch direction in the cycle

    NumCycleBranch( NumBranch br, NumNode nd, int dir )
    {
      mBranch = br;
      // mNode = nd;
      mDir = dir;
    }
  }

  // private int mMax;
  // int mSize;
  private ArrayList< NumCycleBranch > branches;
  // private ArrayList< NumNode >   nodes;
  // int[]       dirs; // branch direction in the cycle
  float e, s, v;    // displacement = closure error
  float ce, cs, cv; // corrections

  int size() { return branches.size(); }

  NumCycle( int sz )
  {
    // mMax = sz;
    // mSize = 0;
    branches = new ArrayList< NumCycleBranch >();
    // nodes = new NumNode[mMax];
    // dirs  = new int[mMax];
  }

  void addBranch( NumBranch branch, NumNode node )
  {
    branches.add( new NumCycleBranch( branch, node, ( (node == branch.n2)? 1 : -1 ) ) );
    // if ( mSize < mMax ) {
    //   branches[mSize] = branch;
    //   // nodes[mSize] = node;
    //   dirs[mSize] = ( node == branch.n2 )? 1 : -1;
    //   // TDLog.Log( TDLog.LOG_NUM, "C add branch dir " + dirs[mSize] );
    //   // branch.dump();
    //   ++mSize;
    // }
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
    // for ( int k=0; k<mSize; ++k ) {
    for ( NumCycleBranch branch : branches ) {
      NumBranch br = branch.mBranch;
      int dir      = branch.mDir;
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

  // @param br   num branch
  // @return  the index of the branch or -1 if the branch is not in this cycle
  // int getBranchIndex( NumBranch br )
  // {
  //   int sz = branches.size();
  //   for (int k = 0; k<sz; ++k ) {
  //     if ( br == branches.get(k).mBranch ) return k;
  //   }
  //   return -1;
  // }

  // @param br   num branch
  // @return  the direction of the branch in this cycle or 0 if the branch is not in this cycle
  int getBranchDir( NumBranch br )
  {
    for ( NumCycleBranch branch : branches ) {
      if ( br == branch.mBranch ) return branch.mDir;
    }
    return 0;
  }

  // return true if every branch in this cycle is equal to a branch of one of the cycles
  boolean isBranchCovered( ArrayList<NumCycle> cycles )
  {
    for ( NumCycleBranch branch : branches ) {
      NumBranch br = branch.mBranch;
      boolean found = false;
      for ( NumCycle cy : cycles ) {
        if ( cy == this ) continue;
        for ( NumCycleBranch branch1 : cy.branches ) {
          if ( br == branch1.mBranch ) {
            found = true;
            break;
          }
        }
        if ( found ) break;
      }
      if ( ! found ) return false; // the branch has not been found among the cycles
    }
    return true;
  }

}

