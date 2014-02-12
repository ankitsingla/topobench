/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class Hypercube extends RegularGraph{
	public int n;
	public int svrp=0; //server port
	public Hypercube(int n, int svrport){
		super((int)Math.pow(2, n),n);
		this.n=n;
                this.svrp=svrport;
		populateAdjacencyList();
	}
	
	private void populateAdjacencyList(){
		System.out.println("#Switch: " + (int)Math.pow(2, n) + " #nwport: " + this.n + " #svrport:" + this.svrp + " #svrs:" + (int)Math.pow(2, n) * this.svrp);

		for (int i=0;i<this.getNoNodes();i++)
			for (int j=i+1;j<this.getNoNodes();j++){
				if(hammingDistance(i,j)==1)
					addBidirNeighbor(i, j);
			}
                //set weights
                setUpFixWeight(0);
                for(int t = 0; t < noNodes; t++){
                        weightEachNode[t] = this.svrp;
                        totalWeight += this.svrp;
                }
	
	}

	private int hammingDistance(int a, int b){
		int counter=0,xor;
		xor=a^b;
		while(xor!=0){
			xor &= xor - 1;
			counter++;
		}
		return counter;
	}
	
	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		return i/this.svrp;
		
	}
}
