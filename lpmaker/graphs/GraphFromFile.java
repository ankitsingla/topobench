/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class GraphFromFile extends Graph{
	public GraphFromFile(int size, String fileName){
		super(size);
		populateAdjacencyList(fileName);
		name="fromfile";
	}

	/*
	 * Construction of the graph give a degree and noNodes and input file
	 */

	private void populateAdjacencyList(String fName){
		// INPUT: from <list of neighbors >
		try {
			BufferedReader br = new BufferedReader(new FileReader(fName));
			String strLine = "";
			int linenum = 0;
			while ((strLine = br.readLine()) != null){
				StringTokenizer strTok = new StringTokenizer(strLine);
				int from = Integer.parseInt(strTok.nextToken());
				while (strTok.hasMoreTokens()){
					int to = Integer.parseInt(strTok.nextToken());
					if (!isNeighbor(from, to)) addBidirNeighbor(from, to);
				}
				linenum ++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add servers to switches in proportion to degree for scale-free stuff
		setUpFixWeight(0);
		for(int t = 0; t < noNodes; t++){
			int curr_weight = adjacencyList[t].size();		// easiest thing: same #servers as degree
			weightEachNode[t] = curr_weight;
                        if(t == 0){
                                weightBeforeEachNode[t] = 0;
                        }
                        weightBeforeEachNode[t+1] = curr_weight + weightBeforeEachNode[t];
			totalWeight += curr_weight;
		}
	}

	public int svrToSwitch(int i) {
		int curr_total = 0;
		for (int sw = 0; sw < noNodes; sw++) {
			int num_here = adjacencyList[sw].size();
			if (curr_total + num_here > i) return sw;
			else curr_total += num_here;
		}
		return -1;
	}
}
