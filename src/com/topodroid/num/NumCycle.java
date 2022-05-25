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
package com.topodroid.num;

// import com.topodroid.utils.TDLog;

import java.util.ArrayList;

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
  double e, s, v;    // displacement = closure error
  double ce, cs, cv; // corrections

  /** @return the number of branches in this cycle
   */
  int size() { return branches.size(); }

  /** cstr
   * @param sz   not-used
   */
  NumCycle( int sz )
  {
    // mMax = sz;
    // mSize = 0;
    branches = new ArrayList< NumCycleBranch >();
    // nodes = new NumNode[mMax];
    // dirs  = new int[mMax];
  }

  /** add a branch to this cycle
   * @param branch   branch to add
   * @param node     ...
   */
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

  /** assumes that branches displacement-vector (e,s,v) have been computed
   *  computes the total displacement along the cycle (= loop mis-closure)
   */
  void computeError()
  {
    e = 0;
    s = 0;
    v = 0;
    // for ( int k=0; k<mSize; ++k ) {
    for ( NumCycleBranch branch : branches ) {
      NumBranch br = branch.mBranch;
      int dir      = branch.mDir;
      e += br.e * dir;
      s += br.s * dir;
      v += br.v * dir;
    }
  }

  /** get the branch direction in this cycle
   * @param br   num branch
   * @return  the direction of the branch in this cycle or 0 if the branch is not in this cycle
   */
  int getBranchDir( NumBranch br )
  {
    for ( NumCycleBranch branch : branches ) {
      if ( br == branch.mBranch ) return branch.mDir;
    }
    return 0;
  }

  /** @return true if every branch in this cycle is equal to a branch of one of the cycles
   * @param cycles    array of cycles
   */
  boolean isBranchCovered( ArrayList< NumCycle > cycles )
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

