package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class HeterServerGraph extends RegularGraph{
	public int nsvrlp=0; //# servers with lower server ports
	public int svrp=0; //server ports (lower)

	public int totalSwitches = 0;
	public int highPortSwitches = 0;
	public int lowPortSwitches = 0;
	public int highDeg = 0;
	public int lowDeg = 0;
	public int highNetDegree = 0;
	public int lowNetDegree = 0;

	public int nsvrs = 0;
//	public double hh_frac = 0.0;
	public int degreeHH = 0;

	public HeterServerGraph(int hportSw, int lportSw, int h, int l, int dh, int dl){		
		super(hportSw + lportSw);
		highPortSwitches = hportSw;
		lowPortSwitches = lportSw;
		totalSwitches = hportSw + lportSw;

		highDeg = h;
		lowDeg = l;
		highNetDegree = dh;
		lowNetDegree = dl;
		nsvrs = (h - dh)*highPortSwitches + (l - dl)*lowPortSwitches;

		name="heter-srv";
		populateAdjacencyList();
	}
	
	private void populateAdjacencyList(){
		System.out.println("WARNING: " + highPortSwitches + " with " + highDeg + " ports, and " + lowPortSwitches + " with " + lowDeg);
		System.out.println("WARNING: Number of servers = " + nsvrs);

		/*  0 to highPortSwitches-1 are high-port ones
		 *  highPortSwitches to noNodes are low-port ones
		 */

		setUpFixWeight(0);
		for (int s = 0; s < highPortSwitches; s ++) {
			weightEachNode[s] = highDeg - highNetDegree;
			totalWeight += weightEachNode[s];
		}
		for (int s = highPortSwitches; s < noNodes; s ++) {
			weightEachNode[s] = lowDeg - lowNetDegree;
			totalWeight += weightEachNode[s];
		}

		Vector<Integer> still_to_link = new Vector();                   // Nodes with degree available
		Vector<Integer> degrees = new Vector();                         // Degree available
		for(int i = 0; i < highPortSwitches; i++){
			still_to_link.add(new Integer(i));              // Initialize with all nodes
			degrees.add(new Integer(highNetDegree));      // Initialize with h1 degree
		}
		for(int i = highPortSwitches; i < noNodes; i++){
			still_to_link.add(new Integer(i));               
			degrees.add(new Integer(lowNetDegree));      
		}
		randomConstructor(still_to_link, degrees, 1);
		//>
	}

	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		
		int ret = 0;
		if (i >= (highDeg - highNetDegree)*highPortSwitches) {
			ret = highPortSwitches + (i - (highDeg - highNetDegree) * highPortSwitches) / (lowDeg - lowNetDegree);
		}
		else ret = i / (highDeg - highNetDegree);

		return ret;
	}
}
