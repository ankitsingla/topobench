/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class SWDC_2torus extends RegularGraph{
	public int serverports=0; //server ports
	public int gridSize=0;
	
	public Vector<Integer> alreadyAdded;
	public int R_MAX, C_MAX;
	public static int DIM = 2;			// Dimension is different for different SWDC_2toruss; HexTor = 3;
	public static int NUM_RAND_CONN = 2;		// Num-rand-ports is different for different SWDC_2toruss; HexTor = 1;

	public SWDC_2torus(int size, int degree, int serverp, int gridS){
		super(size,degree);
		R_MAX = gridS;
		C_MAX = gridS;
		serverports = serverp;
		alreadyAdded = new Vector<Integer>();
		
		populateAdjacencyList(degree, degree, 0, 0, 0);
		name="swdc_2torus";
	}

	public int getIndex(int r, int c){
		int modr = (R_MAX + r) % R_MAX;
		int modc = (C_MAX + c) % C_MAX;

		int index = modc + modr*C_MAX;
		return index; 
	}

	// reverse of above function
	public int[] dimensions(int index){
		int[] dim = new int[2];
		dim[0] = index / C_MAX;
		dim[1] = index - dim[0]*C_MAX;
		return dim;
	}

	// Distance function
	public Integer distance(int from, int to){
		if(from==to)
			return Integer.MAX_VALUE;
		return shortestPathLen[from][to];
	}

	// Picks a random index based on its distance in small world fashion
	// param = 1 => I'm picking for the second node of the pair to connect
	public int pickRand(int from){
		int mypick = -1;

		// Store all existing nodes' distance from new node
		Vector<Double> distStore = new Vector<Double>();
		double inverse_distance_sum = 0, inverted_decayed = 0;

//		System.out.println("ALREADY-ADDED HAS : " + alreadyAdded.size());

		for (Integer num = 0; num < noNodes; num++) {
			//if (from == num) continue;
			Integer dis;
			dis = distance(from, alreadyAdded.elementAt(num));
			
			inverted_decayed = 1.0/Math.pow(dis, DIM);
			inverse_distance_sum += inverted_decayed;

			distStore.add(inverted_decayed);
		}

		// Now find the correct guy
		double curr_range_sum = 0;
		double mydouble = rand.nextDouble() * inverse_distance_sum; // uniformly distributed rand

		double sum_so_far = 0;
		for (Integer num = 0; num < alreadyAdded.size(); num++) {
			//if (from == num) continue;
			sum_so_far += distStore.elementAt(num);
			if (mydouble <= sum_so_far) {
				mypick = alreadyAdded.elementAt(num);
				break;
			}
		}
		
		if (mypick == -1) {
			System.out.println("ERROR in finding the random connectee!! Help!");
		}
		return mypick;
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

	/*
	 * Construction of the random graph give a degree and noNodes
	 */
	private void populateAdjacencyList(int swdeg, int degree, int mode, int ex_size, int nsvrs){

		//< First, the regular part
		int nodeIndex = 0;
		for (int r = 0; r < R_MAX; r ++)	// row
			for (int c = 0; c < C_MAX; c++) {	// column
				// This is node n[r][c] we're talking about

				// connect to r-1, r+1; c-1 : c+1; (All modulo r.max etc.)
				addBidirNeighbor(nodeIndex, getIndex(r-1, c));
				addBidirNeighbor(nodeIndex, getIndex(r, c-1));

				// Increment node index
				nodeIndex ++;
			}
		//>

		modifiedFloydWarshall();
		nodeIndex = 0;
		for (int r = 0; r < R_MAX; r ++)	
			for (int c = 0; c < C_MAX; c++ ){
					alreadyAdded.add(nodeIndex);
					nodeIndex ++;
				}

		/* 
		 * Now the random stuff:
		 * For each port i on new device, pick a random node
		 * if !(port i is occupied), just connect
		 * if yes, pick another one with occupied port i and the next new device
		 * I DO NOT avoid parallel edges
		 */
		int randPorts = NUM_RAND_CONN;

		// randomEdges[i][j] gives index of node to which random port j of node i is connected

		//< Initialize
		int [][] randomEdges = new int[noNodes][randPorts]; //i, j: node to which r-port j of node i is connected
		Vector<Integer> someDegreeLeft = new Vector<Integer>(); // list of nodes with some free port(s)
		for (int j = 0; j < noNodes; j++){
			for (int i = 0; i < randPorts; i++){
				randomEdges[j][i] = -1;
			}
			someDegreeLeft.add(j);
		}
		//>
		
		//< Loop over all nodes
		while (someDegreeLeft.size() > 0){
			int adding1 = someDegreeLeft.elementAt(0);
			//int adding2 = -1, chosen2 = -1, detach1 = -1, detach2 = -1;
			for (int i = 0; i < randPorts; i++) {
				if (randomEdges[adding1][i] != -1) continue;
				
				// Pink random dest
				int chosen1 = -1;
				int detach = Integer.MAX_VALUE;

				// If random dest has port-i free, connect! Done!
				while (detach != -1) {
					chosen1 = pickRand(adding1);
					detach = randomEdges[chosen1][i];
					if (detach != -1) continue;
					
					randomEdges[chosen1][i] = adding1;
					randomEdges[adding1][i] = chosen1;

					//System.out.println("{" + adding1 + ", " + chosen1 + "}");
					// delete from list?
					int noremain_adding = 1, noremain_chosen = 1;
					for (int p = 0; p < randPorts; p ++){
						if (randomEdges[adding1][p] == -1) noremain_adding = 0;
						if (randomEdges[chosen1][p] == -1) noremain_chosen = 0;
					}

					if (noremain_adding == 1) someDegreeLeft.remove((Integer)adding1);
					if (noremain_chosen == 1) someDegreeLeft.remove((Integer)chosen1);
					break;
				}
			}
		}

		// Add to adjacencyList now
		int to;
		for (int i = 0; i < noNodes; i ++) 
			for (int j = 0; j < randPorts; j++) {
				to = randomEdges[i][j];
				//System.out.println("[" + to + ", " + i + "]");
				if (to < i) {
					addBidirNeighbor(i, to);
					//System.out.println("(" + i + ", " + to + ")");
				}
			}

		modifiedFloydWarshall();

		setUpFixWeight(0);
		for(int t = 0; t < noNodes; t++){
			int curr_weight = serverports;     
			weightEachNode[t] = curr_weight;
			totalWeight += curr_weight;
		}
	}
/*
	public static void main(String[] args){

		Integer numNodes = Integer.parseInt(args[0]);
		Integer swports = Integer.parseInt(args[1]);
		
		Integer numsrv = Integer.parseInt(args[2]);

		//int size, int swdeg, int degree, int mode, int ex_size, int nsvrs){

		Graph net = new SWDC_2torus(numNodes, swports, -1, 1, 0, numsrv);
		String textGraph = net.toString();

		System.out.println(textGraph);
		return;
	}*/
}
