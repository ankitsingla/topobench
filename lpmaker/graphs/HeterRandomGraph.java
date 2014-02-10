package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class HeterRandomGraph extends RegularGraph{
	public int nsvrlp=0; //# servers with lower server ports
	public int svrp=0; //server ports (lower)

	public int totalSwitches = 0;
	public int highPortSwitches = 0;
	public int lowPortSwitches = 0;
	public int highDeg = 0;
	public int lowDeg = 0;
	public int hNetDeg = 0;
	public int lNetDeg = 0;
	public int nsvrs = 0;
//	public double hh_frac = 0.0;
	public int degreeHH = 0;

	public HeterRandomGraph(int hportSw, int lportSw, int h, int l, int dh, int dl, int dhh){		
		super(hportSw + lportSw);
		highPortSwitches = hportSw;
		lowPortSwitches = lportSw;
		totalSwitches = hportSw + lportSw;

		highDeg = h;
		lowDeg = l;
		hNetDeg = dh;
		lNetDeg = dl;
	//	nsvrs = svrs;
		nsvrs = (h - dh)*hportSw + (l - dl)*lportSw;
		degreeHH = dhh;

		name="heter-rand";
		populateAdjacencyList();
	}
	
	private void populateAdjacencyList(){
		System.out.println("WARNING: " + highPortSwitches + " with " + highDeg + " ports, and " + lowPortSwitches + " with " + lowDeg);
		System.out.println("WARNING: Number of servers = " + nsvrs);

		/*  0 to highPortSwitches-1 are high-port ones
		 *  highPortSwitches to noNodes are low-port ones
		 */

		int highNetDegree = hNetDeg;
		int lowNetDegree = lNetDeg;

	
		// First create a random regular graph over the high degree switches using degreeHH:
		System.out.println("Building only H-H RRG with " + degreeHH + " ports!");

		Vector still_to_link = new Vector();		// Nodes with some degree still available
		Vector degrees = new Vector();			// Degree currently used up

		if (degreeHH > 0) {
			for(int i = 0; i < highPortSwitches; i++){
				still_to_link.add(new Integer(i));			// Initialize with all nodes
				degrees.add(new Integer(degreeHH));			// Initialize with all degree available
			}
			randomConstructor(still_to_link, degrees, 1);
		}

		// Now degreeHH has been used for the H-port devices;
		// Next build RRG over only low-port devices
		still_to_link = new Vector(lowPortSwitches);		// Nodes with some degree still available
		degrees = new Vector(lowPortSwitches);			// Degree currently used up

		for(int i = highPortSwitches; i < noNodes; i++){
			still_to_link.add(new Integer(i));			// Initialize with all nodes
			degrees.add(new Integer(lowNetDegree));			// Initialize with '0' used degree
		}
		randomConstructor(still_to_link, degrees, 1);

		// Last phase: Connect the remaining degree of highPortSwitches to lowPortSwitches by rewiring.
		for(int fish = 0; fish < highPortSwitches; fish++) // for each new fish
		{

			int idegree=0; // the current degree of node i
			int iter=0; //the # of attempts
			int mom_i_found_it=0;
			//we want to break "degree/2" edges
			//suppose we choose edge (a,b) to break, then following three conditions has to meet:
			//1) (a,b) has an edge
			//2) (i,a) has no edge
			//3) (i,b) has no edge.
			while(idegree <=  (highNetDegree - degreeHH - 2) && iter++ < 8000)
			{
				int p1=fish;
				int p2=fish;
				while(p1 == fish || p1 == p2){
					p1 = rand.nextInt(lowPortSwitches) + highPortSwitches;
				}
				while(p2 == fish || p2 == p1){
					p2 = rand.nextInt(lowPortSwitches) + highPortSwitches;
				}
				//now we have p1 != fish, p2 != fish, p1 != p2

				// Check if a (p1, p2) edge already exists
				int k1=0;
				for(int i=0; i<adjacencyList[p1].size(); i++)
				{
					//if(adjacencyList[n1.intValue()].contains(n2) == false){
					if(adjacencyList[p1].elementAt(i).intValue() == p2)
					{
						k1=1;
					}
				}
				// Check if (fish,p1) edge already exists
				int k2=0;
				for(int i=0; i<adjacencyList[fish].size(); i++)
				{
					//if(adjacencyList[n1.intValue()].contains(n2) == false){
					if(adjacencyList[fish].elementAt(i).intValue() == p1)
					{
						k2=1;
					}
				}
				// Check if (fish,p2) edge already exists
				int k3=0;
				for(int i=0; i<adjacencyList[fish].size(); i++)
				{
					//if(adjacencyList[n1.intValue()].contains(n2) == false){
					if(adjacencyList[fish].elementAt(i).intValue() == p2)
					{
						k3=1;
					}
				}

				if(k1 == 1 && k2 == 0 && k3 == 0)	// Good keyboard, break it!
				{
					addBidirNeighbor(fish, p1);
					addBidirNeighbor(fish, p2);
					removeBidirNeighbor(p1, p2);

					//System.out.println(adjacencyList[p1].size());
					//System.out.println(adjacencyList[p2].size());
					//System.out.println(adjacencyList[fish].size());
					//System.out.println("=========");
					idegree+=2;

					//System.out.println("ADD: ("+ fish + ", " + p1 + ")");
					//System.out.println("ADD: ("+ fish + ", " + p2 + ")");
					//System.out.println("RM : ("+  p1  + ", " + p2 + ")");

					mom_i_found_it = 1;
				}
				if(mom_i_found_it==0){	// Give up if can't find a proper link
					if (iter > 5000) System.out.println("WARNING: Dad, this is so weird");
					//return;
				}
			}
		}

		// If degreeHH was odd, we've one H-port left free. Use pairs of H switches to 
		// connect to L switches
		for(int fish = 0; fish < highPortSwitches; ) // for each new fish
		{
			
			int iter=0; //the # of attempts
			int h1=fish;
			int h2=fish+1;
			
			if (adjacencyList[h1].size() == highNetDegree || adjacencyList[h2].size() == highNetDegree) {
				fish++;
				continue;
			}

			while(iter++ < 8000)
			{
				int l1 = rand.nextInt(lowPortSwitches) + highPortSwitches;
				int internal_iter = 0;
				while(isNeighbor(h1, l1) && internal_iter++ < 1000){
					l1 = rand.nextInt(lowPortSwitches) + highPortSwitches;
				}
				int l2 = adjacencyList[l1].elementAt(rand.nextInt(adjacencyList[l1].size())).intValue();
				while((isNeighbor(h2, l2) || l2 < highPortSwitches) && internal_iter++ < 1000){
					l2 = adjacencyList[l1].elementAt(rand.nextInt(adjacencyList[l1].size())).intValue();
				}
				if (isNeighbor(h1, l1) || isNeighbor(h2, l2) || l2 < highPortSwitches) continue;

				addBidirNeighbor(h1, l1);
				addBidirNeighbor(h2, l2);
				removeBidirNeighbor(l1, l2);
				break;
			}
			fish += 2;
		}

		// Some final healing?
		// Find which nodes have remaining degree
		int stop_sign = 0;
		still_to_link = new Vector(lowPortSwitches);
                degrees = new Vector(lowPortSwitches);
		for (int i = 0; i < highPortSwitches; i++) {
			if (adjacencyList[i].size() < hNetDeg) {
				still_to_link.add(new Integer(i));
				degrees.add(new Integer(hNetDeg - adjacencyList[i].size()));
			}
		}
		for (int i = 0; i < lowPortSwitches; i++) {
			int sw_index = i + highPortSwitches;
			if (adjacencyList[sw_index].size() < lNetDeg) {
				still_to_link.add(new Integer(sw_index));
				degrees.add(new Integer(lNetDeg - adjacencyList[sw_index].size()));
			}
		}
		System.out.println("SOME DEGREE FREE: " + still_to_link.size());

		// Go through still-to-link and degrees to see if any more connections can be added.
		while(!still_to_link.isEmpty() && stop_sign==0){
			if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
				System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
				stop_sign=1;
			}
			boolean found = false;

			int p1 = -1, p2 = -1;
			Integer n1 = new Integer(0);
			Integer n2 = new Integer(0);

			int iteration = 0;
			int MAX_ITERATION = 10000;
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
				for(int i=0; i<adjacencyList[n1.intValue()].size(); i++) 
					if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2) k=1;

				if(k==0) {	// Edge doesn't already exist. Good, add it!
					found = true;
					addBidirNeighbor(n1, n2);
					System.out.println("HEAL LINK ADDED: " + n1 + "->" + n2);
				}
			}

			if(stop_sign==0) {
				// If a clique of nodes is left in the end, this gives up
				if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
					System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
					stop_sign=1;
				}
				degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()-1));
				degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()-1));
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
	
		// set up weights
		setUpFixWeight(0);
                for(int t = 0; t < highPortSwitches; t++){
                        weightEachNode[t] = highDeg - hNetDeg;
                        totalWeight += weightEachNode[t];
                }
                for(int t = highPortSwitches; t < noNodes; t++){
                        weightEachNode[t] = lowDeg - lNetDeg;
                        totalWeight += weightEachNode[t];
                }
	}

	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		if (i < highPortSwitches * (highDeg - hNetDeg)){
			return i / (highDeg - hNetDeg);
		}
		else {
			return highPortSwitches + (i - highPortSwitches * (highDeg - hNetDeg))/(lowDeg - lNetDeg);
		}
	}
}
