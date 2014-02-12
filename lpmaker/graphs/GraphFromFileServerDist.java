/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class GraphFromFileServerDist extends RegularGraph{
	public GraphFromFileServerDist(int size, String fileName){
		super(size);
		populateAdjacencyList(fileName);
		name="fromfile";
	}

	/*
	 * Construction of the graph give a degree and noNodes and input file
	 */

	private void populateAdjacencyList(String fName){
		Random rand = new Random();
		setUpFixWeight(0);
		
		// INPUT: from <num_servers_at_this_sw> <list_of_neighbors>
		try {
			BufferedReader br = new BufferedReader(new FileReader(fName));
			String strLine = "";
			int linenum = 0;
			while ((strLine = br.readLine()) != null){
				StringTokenizer strTok = new StringTokenizer(strLine);
				int from = Integer.parseInt(strTok.nextToken());
				int numsvr = Integer.parseInt(strTok.nextToken());
				weightEachNode[from] = numsvr;
				totalWeight += numsvr;

				while (strTok.hasMoreTokens()){
					int to = Integer.parseInt(strTok.nextToken());
					if (from < to) addBidirNeighbor(from, to);
				}
				linenum ++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
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
