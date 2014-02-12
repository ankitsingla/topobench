/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class RandDegreeDist extends Graph{
	public RandDegreeDist(int size, int[] degrees){
		super(size);
		populateAdjacencyList(size, degrees);
		name="randDegreeDist";
	}

	public RandDegreeDist(int size, int[] degrees, int[] svrdist){
		super(size);
		populateAdjacencyList(size, degrees, svrdist);
		name="randDegreeDist";
	}

	/*
	 * Construction of a random graph give a degree distribution
	 */

	private void populateAdjacencyList(int size, int[] degrees){

		Vector<Integer> nodes = new Vector<Integer>();
		Vector<Integer> degs = new Vector<Integer>();
		for (int i = 0; i < size; i++){
			nodes.add(new Integer(i));
			degs.add(new Integer(degrees[i]));
		}
		randomConstructorPerms(nodes, degs, 1);

		// add servers to switches in proportion to degree for now
		setUpFixWeight(0);
		for(int t = 0; t < noNodes; t++){
			int curr_weight = adjacencyList[t].size();		// easiest thing: same #servers as degree
			weightEachNode[t] = curr_weight;
			totalWeight += curr_weight;
		}
	}

	private void populateAdjacencyList(int size, int[] degrees, int[] svrdist){

		Vector<Integer> nodes = new Vector<Integer>();
		Vector<Integer> degs = new Vector<Integer>();
		for (int i = 0; i < size; i++){
			nodes.add(new Integer(i));
			degs.add(new Integer(degrees[i]));
		}
		randomConstructorPerms(nodes, degs, 1);

		setUpFixWeight(0);
		for(int t = 0; t < noNodes; t++){
			int curr_weight = svrdist[t];		
			weightEachNode[t] = curr_weight;
			totalWeight += curr_weight;
		}
	}

	public int svrToSwitch(int i) {
		int curr_total = 0;
		for (int sw = 0; sw < noNodes; sw++) {
			int num_here = weightEachNode[sw];
			if (curr_total + num_here > i) return sw;
			else curr_total += num_here;
		}
		return -1;
	}
}
