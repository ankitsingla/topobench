
package lpmaker.graphs;
import java.util.*;
import java.io.*;

public class RandVL2CompareMod extends Graph{
	public int K;
	public int di;
	public int da;
	public int[] portMap;
	public int tors;
	public int totalports;
	public int svrT, svrC, svrA; // svrT = #servers on ToR; svrC = #servers on Core, etc.
	public int corePortsMorphed, aggPortsMorphed; // How many core and agg 10G ports are lost
	public int smallCorePorts, smallAggPorts;     // How many 1G ports on core and agg are for network

	public RandVL2CompareMod(int i, int a, int t, int sT, int sC, int sA, int cPM, int aPM, int small_on_core, int small_on_agg){
		super(i + a/2 + t);
		di = i;
		da = a;
		tors = t;
		svrT = sT;
		svrC = sC;
		svrA = sA;
		corePortsMorphed = cPM;
		aggPortsMorphed = aPM;
		smallCorePorts = small_on_core;
		smallAggPorts = small_on_agg;
		this.K = di + da/2 + t;
		populateAdjacencyList();
		name = "vl2compare";
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

		//set weights
		setUpFixWeight(0);
		for(int t = 0; t < tors; t++){
			weightEachNode[t] = svrT;
			totalWeight += svrT;
		}

		// Set degrees
		for (int i = 0; i < noNodes; i++) {
			if (i >= tors && i < (tors + num_agg_with_high)) {
				if (agg_high != 0) {
					nodeIds.add(i);
					degreesForTors.add(agg_high);
				}
				degreesNet.add(da - agg_high - aggPortsMorphed);
				weightEachNode[i] = svrA;
			}
			if (i >= (tors + num_agg_with_high) && i < (tors + di)) {
				if(agg_low != 0) {
					nodeIds.add(i);
					degreesForTors.add(agg_low);
				}
				degreesNet.add(da - agg_low - aggPortsMorphed);
				weightEachNode[i] = svrA;
			}
			if (i >= (tors + di) && i < (tors + di + num_core_with_high)) {
				nodeIds.add(i);
				degreesForTors.add(core_high);
				degreesNet.add(di - core_high - corePortsMorphed);
				weightEachNode[i] = svrC;
			}
			if (i >= (tors + di + num_core_with_high)) {
				if (core_low != 0){
					nodeIds.add(i);
				 	degreesForTors.add(core_low);
				}
				degreesNet.add(di - core_low - corePortsMorphed);
				weightEachNode[i] = svrC;
			}
		}
		
		int tot_deg_tors_added = 0, tot_net_deg = 0;
		for (int i = 0; i < degreesForTors.size(); i++) {
			tot_deg_tors_added += degreesForTors.elementAt(i);
			tot_net_deg += degreesNet.elementAt(i);
		}
		System.out.println("CHECK; TOTAL TOR DEG = " + tot_deg_tors_added + " TOTAL NET DEG = " + tot_net_deg);
		//>

		//< For each tor, find two random ports to connect to from above pool
		for (int i = 0; i < tors; i++) {
			if (nodeIds.size() == 0) break;
			if (nodeIds.size() == 1) {
				// Needs edge swaps
				int t1, t2, conn_t1a, conn_t1b, conn_t2a, conn_t2b;
				do {
					t1 = rand.nextInt(tors - 1);
					t2 = rand.nextInt(tors - 1);

					conn_t1a = adjacencyList[t1].elementAt(0).intValue();
					conn_t1b = adjacencyList[t1].elementAt(1).intValue();
					conn_t2a = adjacencyList[t2].elementAt(0).intValue();
					conn_t2b = adjacencyList[t2].elementAt(1).intValue();
					System.out.println("T1 and T2: " + t1 + " " + t2);

				} while (t1 == t2 || isNeighbor(t1, nodeIds.elementAt(0)) || isNeighbor(t2, nodeIds.elementAt(0))
					|| conn_t1a == conn_t2a || conn_t1a == conn_t2b || conn_t1b == conn_t2a || conn_t1b == conn_t2b);
				removeBidirNeighbor(t1, conn_t1a);
				removeBidirNeighbor(t2, conn_t2a);
				addBidirNeighbor(new Integer(t1), new Integer(nodeIds.elementAt(0)), 10);
				addBidirNeighbor(new Integer(t2), new Integer(nodeIds.elementAt(0)), 10);
				addBidirNeighbor(new Integer(i), new Integer(conn_t1a), 10);
				addBidirNeighbor(new Integer(i), new Integer(conn_t2a), 10);
				
				System.out.println("DEL: " + t1 + "->" + conn_t1a);
				System.out.println("DEL: " + t2 + "->" + conn_t2a);
				System.out.println("ADD: " + t1 + "->" + nodeIds.elementAt(0));
				System.out.println("ADD: " + t2 + "->" + nodeIds.elementAt(0));
				System.out.println("ADD: " + i + "->" + conn_t1a);
				System.out.println("ADD: " + i + "->" + conn_t2a);
				if (degreesForTors.elementAt(0) == 2) break;
				else {
					degreesForTors.set(0, degreesForTors.elementAt(0)-2);
					continue;
				}
			}

			int p1=i, p2;
			while (isNeighbor(i, p1)) 
				p1 = rand.nextInt(nodeIds.size());
			p2 = p1;
			while (p1 == p2 || isNeighbor(i, p2)) 
				p2 = rand.nextInt(nodeIds.size());

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

		//< Core-agg 10G random network!
		Vector<Integer> still_to_link = new Vector<Integer>();
		Vector<Integer> degrees = new Vector<Integer>();

		for(int i = tors; i < noNodes; i++){
			still_to_link.add(new Integer(i));         // Initialize with nodes with available degree
			degrees.add(degreesNet.elementAt(i - tors));      // Initialize with remaining degree
		}
		randomConstructor(still_to_link, degrees, 10);

		// Re-adjust the still_to_link and degrees vectors
		still_to_link.clear();
		degrees.clear();
		for (int nn = tors; nn < noNodes; nn++) {
			if ((nn < (tors + di)) && nn >=tors && adjacencyList[nn].size() < da - aggPortsMorphed) {
				still_to_link.add(nn);
				degrees.add(da - adjacencyList[nn].size() - aggPortsMorphed);
			}
			if ((nn >= (tors + di)) && adjacencyList[nn].size() < di - corePortsMorphed) {
				still_to_link.add(nn);
				degrees.add(di - adjacencyList[nn].size() - corePortsMorphed);
			}
		}
		System.out.println(still_to_link.toString());
		System.out.println(degrees.toString());

		// Edge swaps to fix still left ports  
		int fix_iter = 0;
		while (fix_iter < 5000 && still_to_link.size() != 0){
			fix_iter ++;
			int badNode = still_to_link.elementAt(0);
			int degFix = degrees.elementAt(0);
			int anotherBad = badNode;

			if (degFix == 1) { // Find another different bad node
				if (still_to_link.size() == 1) break;
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
		//>

		//< Interconnect of remaining 1G network ports
		still_to_link = new Vector<Integer>();
		degrees = new Vector<Integer>();

		for(int i = 0; i < tors; i++){
			if (svrT < 20) {
				still_to_link.add(new Integer(i));         // Initialize with nodes with available degree
				degrees.add(new Integer(20 - svrT));       // Initialize with remaining degree
			}
		}
		for(int i = tors; i < tors + di; i++){
			if (smallAggPorts > 0) {
				still_to_link.add(new Integer(i));         
				degrees.add(new Integer(smallAggPorts));   
			}
		}
		for(int i = tors + di; i < noNodes; i++){
			if (smallCorePorts > 0) {
				still_to_link.add(new Integer(i));         
				degrees.add(new Integer(smallCorePorts));  
			}
		}
		randomConstructor(still_to_link, degrees, 1);
		//>*/
	}

	public int svrToSwitch(int serverIndex){
		int swIndex = 0;
		int svrSoFar = 0;

		while (svrSoFar <= serverIndex){
			svrSoFar += weightEachNode[swIndex];
			swIndex++;
		}
		swIndex--;
		return swIndex;
	}
}
