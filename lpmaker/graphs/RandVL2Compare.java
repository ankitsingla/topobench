package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

class Pair {
	public int l;
	public int r;

	Pair(int l, int r){
		this.l = l;
		this.r = r;
	}
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof Pair)) return false;
		Pair pairo = (Pair) o;
		return ((this.l == pairo.l) && (this.r == pairo.r));
	}
}

public class RandVL2Compare extends Graph{
	
	public int K;
	public int di;
	public int da;
	public int[] portMap;
	public int tors;
	public int totalports;
	public int[] portsLeft;

	public RandVL2Compare(int i, int a, int t){
		super(i + a/2 + t);
		di = i;
		da = a;
		tors = t;
		this.K = di + da/2 + t;
		portsLeft = new int[di + da/2];	// keeps track of how many network ports (excl. ToRs) are on each switch
		for (int p = 0; p < portsLeft.length; p++) portsLeft[p] = degreeOfSwitch(tors + p);
		populateAdjacencyList();
		name = "vl2compare";
	}

	private int degreeOfSwitch(int i) { // returns degree given switch index i
		int degAvailable = 2;				// for Tor
		if (i >= tors) degAvailable = da;		// for agg
		if (i >= tors + di) degAvailable = di;		// for core

		return degAvailable;
	}

	private int portToSwitch(int portNum) {	// converts global port-index to switch index
		int swindex = 0;

		int portsSoFar = 0;
		for (int curr_sw = 0; curr_sw < noNodes; curr_sw++){
			portsSoFar += portsLeft[curr_sw];
			if (portsSoFar > portNum) return (curr_sw + tors);
		}

		return (swindex + tors);
	}

	private int lastPortonSwitch(int sw) { // returns the global port index of last port on this switch
		int portret = -1;
		int sw_index_shifted = sw - tors;

		int portsSoFar = 0;
		for (int curr_sw = 0; curr_sw <= sw_index_shifted; curr_sw++){
			portsSoFar += portsLeft[curr_sw];
		}

		portret = portsSoFar - 1;

		return portret;
	}
	
	private void populateAdjacencyList(){
		// 0 to t - 1 ToRs
		// t to [t + di - 1] Agg
		// [t + di] to [t + di + da/2 - 1] core
		
		//< Figure out how many ToRs go to core, and how many to agg
		// Because of the way VL2 is configured, it turns out that tors/3 must be at core, rest at agg
		
		int core_low, core_high, num_core_with_low, num_core_with_high;
		core_low = (4 * tors) / (3 * da);	// Some core switches have this many tor-ports
		core_high = core_low + 1;		// Rest have 1 additional

		num_core_with_high = (2 * tors) / 3 - (core_low * da / 2);	// Core sw with core_high tor ports
		num_core_with_low = da / 2 - num_core_with_high;	// Core sw with core_low tor ports

		int tors_on_agg = 2 * tors - (num_core_with_high * core_high + num_core_with_low * core_low);	// These many tor ports left for agg
		int agg_low, agg_high, num_agg_with_low, num_agg_with_high;
		agg_low = tors_on_agg / di ;		// Some agg switches have this many tor ports
		agg_high = agg_low + 1;			// Rest have 1 additional

		num_agg_with_high = tors_on_agg - (agg_low * di);	// agg sw with agg_high tor ports
		num_agg_with_low = di - num_agg_with_high;		// agg sw with agg_low tor ports
	
		// If there are some core switches with fewer tor-ports and some more, then move tor ports from agg to core (so agg sw are more "balanced")
		if (num_core_with_high != 0 && num_agg_with_high != 0 && num_core_with_low > num_agg_with_high) {
			num_core_with_high += num_agg_with_high;
			num_core_with_low -= num_agg_with_high;
			num_agg_with_low += num_agg_with_high;
			num_agg_with_high = 0;
		}
		
		System.out.println(num_agg_with_high + " AGG have " + agg_high + ";" + num_agg_with_low + " AGG have " + agg_low);
		System.out.println(num_core_with_high + " core have " + core_high + ";" + num_core_with_low + " core have " + core_low);

		Vector<Integer> nodeIds = new Vector<Integer>();
		Vector<Integer> degreesForTors = new Vector<Integer>();
		Vector<Integer> degreesNet = new Vector<Integer>();

		for (int i = 0; i < noNodes; i++) {
			if (i >= tors && i < (tors + num_agg_with_high)) {
				if (agg_high != 0) {
					nodeIds.add(i);
					degreesForTors.add(agg_high);
				}
				degreesNet.add(da - agg_high);
			}
			if (i >= (tors + num_agg_with_high) && i < (tors + di)) {
				if(agg_low != 0) {
					nodeIds.add(i);
					degreesForTors.add(agg_low);
				}
				degreesNet.add(da - agg_low);
			}
			if (i >= (tors + di) && i < (tors + di + num_core_with_high)) {
				nodeIds.add(i);
				degreesForTors.add(core_high);
				degreesNet.add(di - core_high);
			}
			if (i >= (tors + di + num_core_with_high)) {
				if (core_low != 0){
					nodeIds.add(i);
				 	degreesForTors.add(core_low);
				}
				degreesNet.add(di - core_low);
			}
		}
		int tot_deg_tors_added = 0;
		for (int i = 0; i < degreesForTors.size(); i++) tot_deg_tors_added += degreesForTors.elementAt(i);
		System.out.println("CHECK; TOTAL TOR DEG = " + tot_deg_tors_added);
		//>

		//< For each tor, find two random ports to connect to from above pool
		for (int i = 0; i < tors; i++) {
			if (nodeIds.size() == 0) break;

			int p1, p2;
			p1 = rand.nextInt(nodeIds.size());
			p2 = p1;
			while (p1 == p2 && nodeIds.size() > 1) p2 = rand.nextInt(nodeIds.size());

			addBidirNeighbor(new Integer(i), nodeIds.elementAt(p1), 10);
			addBidirNeighbor(new Integer(i), nodeIds.elementAt(p2), 10);
			
			int curr_val = degreesForTors.elementAt(p1);
			degreesForTors.set(p1, curr_val - 1);
			curr_val = degreesForTors.elementAt(p2);
			degreesForTors.set(p2, curr_val - 1);
				
			int was_deleted = 0;
			if (degreesForTors.elementAt(p1) == 0) {
				if (nodeIds.size() == 1) break;
				degreesForTors.remove(p1);
				nodeIds.remove(p1);
				was_deleted = 1;
			}
			if (p2 > p1 && was_deleted == 1) p2--;
			if (degreesForTors.elementAt(p2) == 0) {
				degreesForTors.remove(p2);
				nodeIds.remove(p2);
			}
		}
		//>

		//< Core-agg random network!
		Vector<Integer> still_to_link = new Vector<Integer>();
		Vector<Integer> degrees = new Vector<Integer>();

		for(int i = tors; i < noNodes; i++){
			still_to_link.add(new Integer(i));         // Initialize with nodes with available degree
			degrees.add(degreesNet.elementAt(i - tors));      // Initialize with remaining degree

			System.out.println("NET DEG of " + i + " = " + degreesNet.elementAt(i - tors));
		}

		int stop_sign=0;
		while(!still_to_link.isEmpty() && stop_sign==0){
			if(still_to_link.size() == 1){                          // Ignores this case of 1 node left out
				System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0));
				stop_sign=1;
			}
			boolean found = false;

			int p1 = -1, p2 = -1;
			Integer n1 = new Integer(0);
			Integer n2 = new Integer(0);

			int iteration = 0;
			int MAX_ITERATION = 1000;
			while(!found && iteration++ < MAX_ITERATION && stop_sign == 0){ // try until a node-pair to connect is found
				p1 = rand.nextInt(still_to_link.size());
				p2 = p1;
				while(p2 == p1){
					p2 = rand.nextInt(still_to_link.size());
				}

				n1 = (Integer)still_to_link.elementAt(p1);
				n2 = (Integer)still_to_link.elementAt(p2);

				// Check if an n1-n2 edge already exists
				int k=0;
				for (int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					if (adjacencyList[n1.intValue()].elementAt(i).intValue() == n2) k=1;

				if (k==0) {// Edge doesn't already exist. Good, add it!
					found = true;
					addBidirNeighbor(n1, n2, 10);
				}
			}


			if(stop_sign==0){
				/*
				 * If a clique of nodes is left in the end, this gives up
				 */
				if(iteration >= MAX_ITERATION) { // Give up if can't find a pair to link
					System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
					stop_sign=1;
					//return;
				}
				degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue() - 1));
				degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue() - 1));
				boolean deleted_p1 = false;
				if(((Integer)degrees.elementAt(p1)).intValue() == 0){
					// Degree exhausted => No longer available to connect
					still_to_link.remove(p1);
					degrees.remove(p1);
					deleted_p1 = true;
				}

				// Just adjusting the vector index here, nothing related to high-level function
				int p2_updated;
				if(deleted_p1 && p1 < p2)
					p2_updated = p2-1;
				else
					p2_updated = p2;

				if(((Integer)degrees.elementAt(p2_updated)).intValue() == 0){
					// Degree exhausted => No longer available to connect
					still_to_link.remove(p2_updated);
					degrees.remove(p2_updated);
				}
			}
		}
		//>

		// Re-adjust the still_to_link and degrees vectors
		still_to_link.clear();
		degrees.clear();
		for (int nn = 0; nn < noNodes; nn++) {
			if ((nn < tors) && adjacencyList[nn].size() < 2) {
				still_to_link.add(nn);
				degrees.add(2 - adjacencyList[nn].size());
			}
			if ((nn < (tors + di)) && nn >=tors && adjacencyList[nn].size() < da) {
				still_to_link.add(nn);
				degrees.add(da - adjacencyList[nn].size());
			}
			if ((nn >= (tors + di)) && adjacencyList[nn].size() < di) {
				still_to_link.add(nn);
				degrees.add(di - adjacencyList[nn].size());
			}
		}
		System.out.println(still_to_link.toString());
		System.out.println(degrees.toString());

		//< Edge swaps to fix still left ports	
		int fix_iter = 0;
		while (fix_iter < 5000 && still_to_link.size() != 0){
			fix_iter ++;
			int badNode = still_to_link.elementAt(0);
			int degFix = degrees.elementAt(0);
			int anotherBad = badNode;
			
			if (degFix == 1) { // Find another different bad node
				anotherBad = still_to_link.elementAt(1);
			}
			
			// Locate edge to break
			int randNode1 = badNode;
			int randNode2 = badNode;
			while (randNode1 == badNode || randNode1 == anotherBad || randNode2 == badNode || 
				randNode2 == anotherBad || isNeighbor(badNode, randNode1) || isNeighbor(anotherBad, randNode2)){
				randNode1 = rand.nextInt(noNodes - tors) + tors;
				do randNode2 = adjacencyList[randNode1].elementAt(rand.nextInt(adjacencyList[randNode1].size())).intValue();
				while (randNode2 < tors);
			}
			
			// Swap
			removeBidirNeighbor(randNode1, randNode2);
			addBidirNeighbor(badNode, randNode1, 10);
			addBidirNeighbor(anotherBad, randNode2, 10);
			fix_iter = 0;
			
			// Fix still_to_link and degrees
			if (degFix == 1) {
				degrees.set(0, degFix - 1);
				degrees.set(1, degrees.elementAt(1) - 1);				
			}
			else degrees.set(0, degFix - 2); 

			if (degrees.elementAt(0) == 0) {
				still_to_link.remove(0);
				degrees.remove(0);
			}
			if (still_to_link.size() == 0) break;
			if (degrees.elementAt(0) == 0) {
				still_to_link.remove(0);
				degrees.remove(0);
			}

			if (still_to_link.size() < 2) continue;
			if (degrees.elementAt(1) == 0) {
				still_to_link.remove(1);
				degrees.remove(1);
			}
		}

		System.out.println("FIX-MESS!!!! STILL_LINK_VEC_SIZE = " + still_to_link.size() + " #############################################");
		//>
		
		//set weights
		setUpFixWeight(0);
		for(int t = 0; t < tors; t++){
			weightEachNode[t] = 20;
			totalWeight += 20;
		}
		
		for(int i = tors; i < noNodes; i++){
			System.out.println("BUILT DEG of " + i + " = " + adjacencyList[i].size());
		}

	}

	public int svrToSwitch(int i){ //i is the server index. return the switch index.
		return i / 20;
	}

	private ArrayList<Integer> notConnectedPorts(){
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int i = 0; i < portMap.length; i++)
			if (portMap[i] == -1) ret.add(new Integer(i));
		return ret;
	}

	private boolean mrSwappy(){// return value = true if swapped something
		boolean ret = false;
	
		// First identify the parallel edges
		ArrayList<Pair> multEdges = new ArrayList<Pair>();
		for (int i = tors; i < noNodes; i++) {		// For non-Tor switches
			for (int j = 0; j < adjacencyList[i].size(); j++) {
				int linkTo = adjacencyList[i].elementAt(j).intValue();
				if (adjacencyList[i].elementAt(j).linkcapacity > 10 && (i < linkTo)) {
					if (!multEdges.contains(new Pair(i, linkTo))) multEdges.add(new Pair(i, linkTo));
				}
			}
		}
		
		// Pick random edges, if "eligible" swap
		int numMultEdges = multEdges.size();
		System.out.println("MULT_EDGES = " + numMultEdges);
		boolean swapped = true;
		Pair p1, p2;
		int iter2 = 0;
		while (swapped && iter2 < 1000 && multEdges.size() > 0) {
			numMultEdges = multEdges.size();
			p1 = multEdges.get(rand.nextInt(numMultEdges));
			int iter = 0;
			do {
				// Find a random edge
				int n1, n2;
				do {
					n1 = rand.nextInt(portsLeft.length) + tors;
					n2 = adjacencyList[n1].elementAt(rand.nextInt(adjacencyList[n1].size())).intValue();
					p2 = (n1 < n2) ? new Pair(n1, n2) : new Pair(n2, n1);
				} while (multEdges.contains(p2));

				iter ++;
			} while (iter < 10000 && (p1.l == p2.l || p1.r == p2.r || p1.l == p2.r || p1.r == p2.l || isNeighbor(p1.l, p2.r) || isNeighbor(p1.r, p2.l)));
			
			if (iter < 10000) {
				// Found two good edges, SWAP!
				assert(removeBidirNeighbor(p1.l, p1.r, 10));
				assert(removeBidirNeighbor(p2.l, p2.r, 10));
				addBidirNeighbor(p1.l, p2.r, 10);
				addBidirNeighbor(p1.r, p2.l, 10);
				swapped = true;
				ret = true;
				iter2 = 0;
				multEdges.remove(p1);
				multEdges.remove(p2);
				multEdges.remove(new Pair(p2.r, p2.l));
				System.out.println("SWAP: " + p1.l + "->" + p1.r + " AND " + p2.l + "->" + p2.r);
			}
			else iter2 ++;
		}
		return ret;
	}
}
