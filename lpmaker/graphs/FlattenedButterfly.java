/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import java.util.*;
import java.io.*;


public class FlattenedButterfly extends Graph{
	private int k;
	private int n;
	private static int c; //concentration or # of server ports per switch
	//# of ports per switch= c+(n-1)*(k-1)
	
	public FlattenedButterfly (int k, int n, int size){
		super(size);
		this.k=k;
		this.n=n;
		this.c=k-1; //k-1 servers per switch
		populateAdjacencyList();
	}
	
	public FlattenedButterfly (int k, int n, int size, int c){
		super(size);
		this.k=k;
		this.n=n;
		this.c=c; 
		populateAdjacencyList();
	}

	private void populateAdjacencyList(){
		int numSwitches=this.noNodes;
		int j;
		for (int d=1;d<n;d++){
			for (int i=0;i<this.getNoNodes();i++){
				for(int m=0;m<k;m++){
					int tmp=(int)Math.floor(i/Math.pow(k,d-1)) % k;
					if (tmp<0)
						tmp=k+tmp;
					j=i+(m-tmp)* (int)Math.pow(k,d-1);
					if((i != j)&&(j<numSwitches))
						addBidirNeighbor(i, j);
				}
			}	
		}		
		//set weights
                setUpFixWeight(0);
                for(int t = 0; t < noNodes; t++){
                        int curr_weight = c;
                        weightEachNode[t] = curr_weight;
                        totalWeight += curr_weight;
                }

	}

	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		return (i/c);
		
	}

}
