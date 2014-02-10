/* The main class takes as inputs various parameters and constructs the desired graph type with
   its specified parameters. The Graph class has methods to print the linear program with the 
   specified graph type and the specified traffic matrix. You may add your own traffic matrices
   by following the example of All-to-all etc. in that file (Graph.java).
 */


package lpmaker;

import java.security.SecureRandom;
import java.util.*;
import java.io.*;

import lpmaker.graphs.*;

public class ProduceLP {

	// Make it so that there's only one source of randomness in all, so I can control it if need be.
	public static Random universalRand;

	public static int switches = 80; 	// # of switch
	public static int switchports = 8; 	// # of ports per switch
	public static int serverports = 1; 	// # of ports per switch that used to connect to servers
	public static int extended_switches = 0; // # switches added in expansion
	public static int nsvrs = 0;	// # of servers we want to support (this, if non-zero, overrides the server ports arguments.)

	public static void main(String args[]) throws IOException{
		long randSeed = Long.parseLong(args[20]);	// In case I want to control the randomness for replicability etc.

		if (randSeed == 0) universalRand = new Random();
		else { 
			System.out.println("NEWSFLASH: USING YOUR RANDOM SEED -----------------------------------------------");
			universalRand = new Random(randSeed);
		}

		int NUMRUNS = Integer.parseInt(args[0]);
		int createLP = Integer.parseInt(args[19]);	// If 1, lp is created; otherwise, only the path lengths are analyzed

		switches = Integer.parseInt(args[4]);
		switchports = Integer.parseInt(args[5]);
		serverports = switchports - Integer.parseInt(args[6]);	// Pay attention to the arguments here!
		extended_switches = Integer.parseInt(args[7]);		
		nsvrs = Integer.parseInt(args[8]);
		double fail_rate = Double.parseDouble(args[9]);

		int trafficMode = Integer.parseInt(args[3]); 	// 0: Server-level random permutation. 1: All-to-all
		//if (switches == serverports + 1) trafficMode = 1;

		int graphtype = Integer.parseInt(args[1]);

		int runs = 0;	// This and NUMRUNS used to be useful; not anymore.

		if(graphtype==0) // Vanilla Random Regular Graph
		{	
			System.out.println("sw:" + switches);
			System.out.println("switchports:" + switchports);
			System.out.println("svr port:" + serverports);
			System.out.println("nsvrs" + nsvrs);
			Graph mynet = new RandomRegularGraph(switches,switchports, switchports - serverports, 1, extended_switches, nsvrs, fail_rate);

			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==1)	// Fat tree
		{
			System.out.println("FAT-SIZE = " + switchports);
			Graph mynet = new FatTreeSigcomm(switchports, fail_rate); 
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==2) //Dragonfly
		{
			int a = Integer.parseInt(args[4]); //# of switches in each group
			int p = Integer.parseInt(args[5]); //# of serverport per switch
			int h = Integer.parseInt(args[6]); //# of intergroup ports per switch
			int z = Integer.parseInt(args[7]); //the number of connection between a pair of groups
			// a = 2p = 2h    recommended value
			// z = 1, 2, or 3?
			// number of groups g = a*h/z+1
			// number of switches = a*(g+1) = a * (a*h/z+2)
			// # ports per switch = a+h+p-1
			// z < a to avoid multigraph
			// z has to be a divisor of (a*h)
			// if a=2p=2h and h is an even number, z can be set by 1, 2, or 4.

			// if z=1, a=2, p=2, h=2

			Graph mynet = new Dragonfly(a, p, h, z); 
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==5) // SWDC_hex
		{	
			System.out.println("Start SWDC_hex Construction");
			Graph mynet = new SWDC_hex(switches,switchports-serverports);
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==6) // SWDC_2torus
		{	
			int gridSize = Integer.parseInt(args[7]);
			System.out.println("Start SWDC_2torus Construction");
			Graph mynet = new SWDC_2torus(switches,switchports, serverports, gridSize);
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==7) // SWDC_ring
		{	
			System.out.println("Start SWDC_ring Construction");
			Graph mynet = new SWDC_ring(switches,switchports);
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==9) // Jellyfish Heterogeneous
		{	
			int nh = Integer.parseInt(args[10]);
			int nl = Integer.parseInt(args[11]);
			int h  = Integer.parseInt(args[12]);
			int l  = Integer.parseInt(args[13]);
			int dh = Integer.parseInt(args[14]);          	// high network degree     
			int dl = Integer.parseInt(args[15]);		// low network degree

			System.out.println("HETER: " + nh + " " + h + "-port; " + nl + " " + l + "-port; ");

			int netH = dh;
			int netL = dl;

			// Vary fhh, such that d_hh varies in a meaningful range
			double min_frac = (nh * netH - nl * netL) / (double)nh;
			int min_dhh = (nh * netH - nl * netL) / nh;
			if (min_dhh < min_frac) min_dhh += 1;

			if (min_dhh < 0) min_dhh = 0;
			if (dh < 10) min_dhh = 3;
			else if (dh < 13) min_dhh = 6;
			else if (dh < 17) min_dhh = 7;
			else min_dhh = 8;
			int range = netH - min_dhh;
			int increment = 1;

			int curr_dhh = min_dhh;
			while (curr_dhh < netH && curr_dhh < nh - 1) {
				Graph mynet = new HeterRandomGraph(nh, nl, h, l, dh, dl, curr_dhh);
				if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + curr_dhh + ".lp", trafficMode);
				mynet.printPathLengths("pl." + curr_dhh);
				curr_dhh += increment;
			}
		}
		else if(graphtype==10) // Jellyfish Heterogeneous Server distribution
		{	
			int nh = Integer.parseInt(args[10]);
			int nl = Integer.parseInt(args[11]);
			int h  = Integer.parseInt(args[12]);
			int l  = Integer.parseInt(args[13]);
			int dh = Integer.parseInt(args[14]); 
			int dl = Integer.parseInt(args[15]);

			Graph mynet = new HeterServerGraph(nh, nl, h, l, dh, dl);
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==13)	// VL2
		{
			int aggports = Integer.parseInt(args[10]);
			int aggsw = Integer.parseInt(args[11]);

			Graph mynet = new VL2(aggsw, aggports); 
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if (graphtype == 14){ // VL2 Compare
			int aggsw = Integer.parseInt(args[4]);		// di
			int aggports = Integer.parseInt(args[5]);	// da
			int tors = Integer.parseInt(args[6]);		// > da * di / 4

			Graph mynet = new RandVL2Compare(aggsw, aggports, tors);
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==15) // Hypercube
		{
			Graph mynet = new Hypercube((int)(Math.log(switches)/Math.log(2)), serverports); 
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if (graphtype == 16) // Butterfly 
		{
			Graph mynet = new FlattenedButterfly(1+serverports,1+((switchports-serverports)/serverports),switches);
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==17) // LSPIIRamanujam
		{
			serverports = Integer.parseInt(args[6]);
			Graph mynet = new LSPRamanujanII(switchports, switches, serverports); 
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		else if(graphtype==18) // Jellyfish Heterogeneous Linespeeds
		{	
			int nh = Integer.parseInt(args[10]);
			int nl = Integer.parseInt(args[11]);
			int h1 = Integer.parseInt(args[12]);
			int h2 = Integer.parseInt(args[13]);
			int l  = Integer.parseInt(args[14]);
			int dh = Integer.parseInt(args[15]);          	// high network degree i.e. out of h2, how many are network ports     
			int dl = Integer.parseInt(args[16]);		// low network degree i.e. out of 'l' how many are network ports
			int capH = Integer.parseInt(args[17]);		// Higher line-rate i.e. either 10G or 40G
			int capL = Integer.parseInt(args[18]);		// Lower line-rate i.e. either 1G or 10G

			int netH1 = h1;	
			int netH2 = dh;
			int netL = dl;

			// Vary conn_nl_nl in a meaningful range
			double min_frac = (nl * netL - nh * netH2) / (double)nl;
			int min_nl_nl = (nl * netL - nh * netH2) / nl;
			if (min_nl_nl < min_frac) min_nl_nl += 1;

			if (min_nl_nl < 0) min_nl_nl = 0;
			int range = netL - min_nl_nl;
			int increment = 1;

			int curr_nl_nl = min_nl_nl;
			while (curr_nl_nl < netL) {
				Graph mynet = new HeterLineSpeeds(nh, nl, h1, h2, l, dh, dl, capH, capL, curr_nl_nl);
				if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + curr_nl_nl + ".lp", trafficMode);
				mynet.printPathLengths("pl." + curr_nl_nl);
				curr_nl_nl += increment;
			}
		}
		else if (graphtype == 20){ // Read graph from file, print linear program for both that graph, and equivalent random graph for comparison; graph file has lines of type "from <list of neighbors>"
			String graph_file = args[2];

			Graph mynet = new GraphFromFile(switches, graph_file);
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);

			int[] degreeDist = new int[mynet.noNodes];
			for (int sw = 0; sw < mynet.noNodes; sw ++) {
				degreeDist[sw] = mynet.adjacencyList[sw].size();
			}
			Graph randCompare = new RandDegreeDist(switches, degreeDist);
			if (createLP == 1) randCompare.PrintGraphforMCFFairCondensed("randCompare." + runs + ".lp", trafficMode);

			mynet.printPathLengths("pl." + runs);
			randCompare.printPathLengths("pl_randCompare." + runs);
		}

		else if (graphtype == 21){ // Read graph from file, print linear program for both that graph, and equivalent random graph for comparison; graph file has lines of type "from #servers-here <list of neighbors>"
			String graph_file = args[2];

			Graph mynet = new GraphFromFileServerDist(switches, graph_file);

			int[] degreeDist = new int[mynet.noNodes];
			int[] svrdist = new int[mynet.noNodes];

			for (int sw = 0; sw < mynet.noNodes; sw ++) {
				degreeDist[sw] = mynet.adjacencyList[sw].size();
				svrdist[sw] = mynet.weightEachNode[sw];
			}

			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			Graph randCompare = new RandDegreeDist(switches, degreeDist, svrdist);
			if (createLP == 1) randCompare.PrintGraphforMCFFairCondensed("randCompare." + runs + ".lp", trafficMode);

			mynet.printPathLengths("pl." + runs);
			randCompare.printPathLengths("pl_randCompare." + runs);
		}
		else if (graphtype == 22){ // VL2 Compare Modified Switch Design
			int aggsw = Integer.parseInt(args[4]);		// di
			int aggports = Integer.parseInt(args[5]);	// da
			int tors = Integer.parseInt(args[6]);		// > da * di / 4

			int svrT = Integer.parseInt(args[7]);
			int svrC = Integer.parseInt(args[8]);
			int svrA = Integer.parseInt(args[9]);
			int corePortsMorphed = Integer.parseInt(args[10]);
			int aggPortsMorphed = Integer.parseInt(args[11]);
			int smallCorePorts = Integer.parseInt(args[12]);
			int smallAggPorts = Integer.parseInt(args[13]);

			Graph mynet = new RandVL2CompareMod(aggsw, aggports, tors, svrT, svrC, svrA, corePortsMorphed, aggPortsMorphed, smallCorePorts, smallAggPorts);
			if (createLP == 1) mynet.PrintGraphforMCFFairCondensed("my." + runs + ".lp", trafficMode);
			mynet.printPathLengths("pl." + runs);
		}
		System.out.println("Done Constructing Graph");
	}
}
