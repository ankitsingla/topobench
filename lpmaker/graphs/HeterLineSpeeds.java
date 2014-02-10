package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class HeterLineSpeeds extends RegularGraph{
	public int totalSwitches = 0;
	public int highPortSwitches = 0;
	public int lowPortSwitches = 0;
	public int highSpeedDegree = 0;
	public int highDeg = 0;
	public int lowDeg = 0;
	public int hNetDeg = 0;
	public int lNetDeg = 0;
	public int nsvrs = 0;
	public int degreeLL = 0;
	public int capH = 0;
	public int capL = 0;

	public HeterLineSpeeds(int hportSw, int lportSw, int h1, int h2, int l, int dh, int dl, int cH, int cL, int dll){
		super(hportSw + lportSw);
		highPortSwitches = hportSw;
		lowPortSwitches = lportSw;
		totalSwitches = hportSw + lportSw;

		highSpeedDegree = h1;
		highDeg = h2;
		lowDeg = l;
		hNetDeg = dh;
		lNetDeg = dl;
		nsvrs = (h2 - dh)*hportSw + (l - dl)*lportSw;
		capH = cH;
		capL = cL;
		degreeLL = dll;

		name="heter-linespeed";
		populateAdjacencyList();
	}

	public String summarizeLinks(){
		int countLL = 0, countHH_low = 0, countHL = 0, countHH_high = 0;
		for (int i = 0; i < noNodes; i++) {
			for (Link l : adjacencyList[i]) {
				if (i > l.intValue()) continue;
				if (l.linkcapacity == capH) {
					assert(i < highPortSwitches && l.intValue() < highPortSwitches);
					countHH_high ++;
				}
				else {
					assert(l.linkcapacity == capL);
					if (i < highPortSwitches && l.intValue() < highPortSwitches) countHH_low ++;
					if (i < highPortSwitches && l.intValue() >= highPortSwitches) countHL ++;
					if (i >= highPortSwitches && l.intValue() >= highPortSwitches) countLL ++;
				}
			}
		}
		return ("LinkSummary: " + countLL + " " + countHL + " " + countHH_high + " " + countHH_low);
	}
	
	private void populateAdjacencyList(){
		/*  0 to highPortSwitches-1 are high-port ones
		 *  highPortSwitches to noNodes are low-port ones
		 */

		int highNetDegree = hNetDeg;
		int lowNetDegree = lNetDeg;

	
		//< First create a random regular graph over the high linespeed ports with degree h1
		System.out.println("Building only H-H RRG with " + highSpeedDegree + " ports!");
		Vector<Integer> still_to_link = new Vector();			// Nodes with degree available
		Vector<Integer> degrees = new Vector();				// Degree available
		for(int i = 0; i < highPortSwitches; i++){
			still_to_link.add(new Integer(i));		// Initialize with all nodes
			degrees.add(new Integer(highSpeedDegree));	// Initialize with h1 degree
		}
		randomConstructor(still_to_link, degrees, capH);
		//>

		//< Next connect the nl_nl connections in a random graph with degree degreeLL
		if (degreeLL > 0) {
			still_to_link = new Vector();	
			degrees = new Vector();			
			for(int i = highPortSwitches; i < noNodes; i++){
				still_to_link.add(new Integer(i));
				degrees.add(new Integer(degreeLL));	
			}
			randomConstructor(still_to_link, degrees, capL);
		}
		//>

		//< Finally, connect the (nh X dh) to [nl X (lNetDeg-dll)]
		// First step: connect the nh X dh ports amongst themselves. Then, we'll add each nl switch with edge swaps!
		still_to_link = new Vector();	
		degrees = new Vector();		
		for(int i = 0; i < highPortSwitches; i++){
			still_to_link.add(new Integer(i));
			degrees.add(new Integer(hNetDeg));	
		}
		randomConstructor(still_to_link, degrees, capL);
		
		// Now add each nl switch with edge swaps!
		for (int i = highPortSwitches; i < noNodes; i++) {
			for (int j = 0; j < (lNetDeg - degreeLL - 1); j = j + 2) {
				// pick random L-L edge between the H-H switches
				int rand1 = -1, rand2 = -1, randNbrIndex = -1, numIters = 0;
				Link randLink;
				do {
					numIters++;
					if (numIters > 2000) break;
					rand1 = rand.nextInt(highPortSwitches);
					randNbrIndex = rand.nextInt(adjacencyList[rand1].size());
					rand2 = adjacencyList[rand1].elementAt(randNbrIndex).intValue();
					randLink = adjacencyList[rand1].elementAt(randNbrIndex);
				}
				while (isNeighbor(rand1, i) || isNeighbor(rand2, i) || randLink.linkcapacity == capH || rand2 >= highPortSwitches);
				if (numIters > 2000) continue;

				removeBidirNeighbor(rand1, rand2);
				addBidirNeighbor(i, rand1, capL);
				addBidirNeighbor(i, rand2, capL);
			}
		}

		// If (lNetDeg - degreeLL) is odd, the L-switches still have 1 degree remaining; pair and conquer!
		if ((lNetDeg - degreeLL) % 2 != 0) {
			for (int i = highPortSwitches; i < noNodes - 1; i = i + 2) {
				// pick random L-L edge between the H-H switches
				int rand1 = -1, rand2 = -1, randNbrIndex = -1, numIters = 0;
				Link randLink;
				do {
					numIters++;
					if (numIters > 2000) break;
					rand1 = rand.nextInt(highPortSwitches);
					randNbrIndex = rand.nextInt(adjacencyList[rand1].size());
					rand2 = adjacencyList[rand1].elementAt(randNbrIndex).intValue();
					randLink = adjacencyList[rand1].elementAt(randNbrIndex);
				}
				while (isNeighbor(rand1, i) || isNeighbor(rand2, i+1) || randLink.linkcapacity == capH || rand2 >= highPortSwitches);
				if (numIters > 2000) continue;

				removeBidirNeighbor(rand1, rand2);
				addBidirNeighbor(i, rand1, capL);
				addBidirNeighbor(i+1, rand2, capL);
			}
		}
		//>
		
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
