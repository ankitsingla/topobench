
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class VL2 extends Graph{
	
	public int K;
	public int di;		// di = aggswitches
	public int da;		// da = aggports

	public VL2(int i, int a){
		super(i + a/2 + (a*i)/4);
		di = i;
		da = a;
		this.K = di + da/2 + (da*di)/4;
		populateAdjacencyList();
		name = "vl2";
	}
	
	private void populateAdjacencyList(){
		// 0-da*di/4 ToRs
		// da*di/4 - [da*di/4 + di] Agg
		// [da*di/4 + di] - [da*di/4 + di + da/2] core
		
		//connect Tors to Agg
		int numtors = (da * di)/4;
		int curr_index = 0;
		for(int tor = 0; tor < numtors; tor++){
			addBidirNeighbor(tor, (da*di)/4 + curr_index%(di), 10);
			addBidirNeighbor(tor, (da*di)/4 + (++curr_index)%(di), 10);
		}
		
		//connect agg to core
		for(int agg = 0; agg < di; agg++){
			for(int core = 0; core < da/2; core++){
					addBidirNeighbor((da*di)/4 + agg, (da*di)/4 + di + core , 10);
				}
			}
		
		//set weights
		setUpFixWeight(0);
		for(int tor = 0; tor < numtors; tor++){
				weightEachNode[tor] = 20;
				totalWeight += 20;
		}
	}

	public int svrToSwitch(int i){ //i is the server index. return the switch index.
		return i / 20;
	}
}
