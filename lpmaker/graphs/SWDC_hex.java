/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class SWDC_hex extends RegularGraph{
	public int nsvrlp=0; //# servers with lower server ports
	public int svrp=2; //server ports (lower)
	public Vector<Integer> alreadyAdded;
	
	public static int R_MAX = 10, C_MAX = 15, D_MAX = 3;
	public static int DIM = 3;			// Dimension is different for different SWDC_hexs; HexTor = 3;
	public static int NUM_RAND_CONN = 1;		// Num-rand-ports is different for different SWDC_hexs; HexTor = 1;

	public SWDC_hex(int size, int degree){
		super(size,degree);
		alreadyAdded = new Vector<Integer>();

		populateAdjacencyList(degree, degree, 0, 0, 0);
		name="swdc_hex";
	}

	public int getIndex(int r, int c, int d){
		int modr = (R_MAX + r) % R_MAX;
		int modc = (C_MAX + c) % C_MAX;
		int modd = (D_MAX + d) % D_MAX;

		int index = modd + modc*D_MAX + modr*C_MAX*D_MAX;
		return index; 
	}

/*
	// reverse of above function
	public int[] dimensions(int index){
		int[] dim = new int[3];
		dim[0] = index / (C_MAX * D_MAX);
		dim[1] = (index - dim[0]*C_MAX * D_MAX) / D_MAX;
		dim[2] = index - dim[0]*C_MAX * D_MAX - dim[1]*D_MAX;
		return dim;
	}
*/

	// Distance function
	public Integer distance(int from, int to){
		/*int[] dim_f = dimensions(from);
		int[] dim_t = dimensions(to);

		Integer dis = 0;
		int adjust = 0;
		for (int dir = 0; dir < 3; dir++){
			if (dir == 0) adjust = R_MAX;
			else if (dir == 1) adjust = C_MAX;
			else adjust = D_MAX;
			dis += Math.min(Math.abs(dim_f[dir] - dim_t[dir]), adjust - Math.abs(dim_f[dir] - dim_t[dir]));
		}
		return (int)dis;*/
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
	
	public ArrayList RandomPermutation(int size)
	{
		int screw;
		ArrayList<Integer> ls;
		do
		{
			screw=0;
			ls=new ArrayList<Integer>();
			for(int i=0; i<size; i++)
			{
				ls.add(new Integer(i));
			}
			for(int i=0; i<size; i++)
			{
				int ok=0;
				int k=0;
				int cnt=0;
				do
				{
					//choose a shift in [0,size-1-i]
					k = rand.nextInt(size-i);
					//check if we should swap i and i+k
					int r;
					if(svrtoswitch(i) != svrtoswitch(ls.get(i+k)))
					{
						ok = 1;
					}
					//System.out.println(i + " " + ls.get(i+k) + " " + i/serverport + " " + ls.get(i+k)/serverport);
					cnt++;
					if(cnt>50)
					{
						screw=1;
						ok=1;
					}
				}
				while(ok==0);
				//swap i's value and i+k's value
				int buf=ls.get(i);
				ls.set(i, ls.get(i+k));
				ls.set(i+k, buf);
			}
		}while(screw==1);
		/*for(int i=0; i<size; i++)
		  {
		  System.out.println(i + " to " + ls.get(i));
		  }*/
		return ls;
	}

	public int svrtoswitch(int i)	//i is the server index. return the switch index.
	{
		return i/2;
	}

	public void PrintGraphforMCF(String filename, int trafficmode, int transportprotocol){
		//trafficmode=1 : all to all
		//trafficmode=2 : every tor-sw send to k/2 stride
		//trafficmode=3 : every tor-sw send to k/4 stride
		//trafficmode=4 : every tor-sw send to random one
		// first (nsvrlp) switches use (svrp) server ports. other (noNodes-nsvrlp) switches use (svrp+1) server ports

		int svrp = 2;
		// int nsvrlp = this.nsvrlp;

		int r=noNodes; //# of ToR switches
		//int svrp=nsvrp; //# of server ports per switch
		//int svrs=nsvrlp * svrp + (noNodes-nsvrlp) * (svrp+1); //# of servers
		int svrs = r*2;

/*		System.out.println("CY " + this.svrp + " " + this.nsvrlp);
		System.out.println("CY " + svrtoswitch(0));
		System.out.println("CY " + svrtoswitch(10));
		System.out.println("CY " + svrtoswitch(20));
		System.out.println("CY " + svrtoswitch(30));
		System.out.println("CY " + svrtoswitch(41));
		System.out.println("CY " + svrtoswitch(42));*/

		//transportprotocol=0 TCP Tahoe;
		//transportprotocol=1 UDP;
		int nflowlet = 1;
		//ArrayList<Integer> ls = RandomPermutation();
		try{

			ArrayList<Integer> rndmap = RandomPermutation(svrs);
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);

			// Nodes
			//			out.write(noNodes + "\n");
			int numEdges = 0;
			for(int i=0; i<noNodes; i++){
				//				out.write(i + " " + i*10 + " " + i*10 + "\n");
				numEdges += adjacencyList[i].size();
			}

			// Edges
			int edgeID = 0;
			int edgeCapacity = 1;
			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					int to = adjacencyList[i].elementAt(j).intValue();
					//					out.write(edgeID + " " + i + " " + to + " " + edgeCapacity + " " + "0 0\n");
					edgeID++;
				}
			}
			int ito=0;

			if(trafficmode==1){ //alltoall
				ito=svrs*(svrs-1);
			}
			else if(trafficmode>=2){//random dst
				ito=svrs*nflowlet; //2x flows 20110416
			}

			int index=0;
			int[][] switchLevelMatrix = new int[noNodes][noNodes];

			int nCommodities = 0;
			for(int i=0; i<svrs; i++)  //for sending server i
			{
				int jto=0; //number of receiving servers
				if(trafficmode==1) //alltoall
					jto=svrs;
				else if(trafficmode>=2 && trafficmode <=4)//random dst
					jto=1; //two servers now 20110416

				for(int j=0; j<jto; j++) //to receiving server j
				{
					int kbound = 0;
					if(trafficmode==1)
						kbound = 1;
					else
						kbound = nflowlet;
					for(int k=0; k<nflowlet; k++)
					{
						if((trafficmode==1 && i != j) || (trafficmode>=2))
						{
							int to=0;
							if(trafficmode==1) //alltoall
							{
								to=j;
							}
							else if(trafficmode==2)//random dst
							{
								to=(i+svrs/2)%svrs;
							}
							else if(trafficmode==3)
							{
								to=(i+svrs/4)%svrs;
							}
							else if(trafficmode==4)
							{
								to=rndmap.get(i);
							}

							double st_time = 0; //rand.nextDouble(); // 0~1.0 s
							//st_time /= 1000.0/2; // 0~0.5ms
							st_time += 0.002; // 1.0ms ~ 1.5ms

							int fromsw=svrtoswitch(i);
							int tosw=svrtoswitch(to);

							// I'm counting only number of connections
							if (switchLevelMatrix[fromsw][tosw] == 0) nCommodities ++;
							switchLevelMatrix[fromsw][tosw] ++;
						}
					}
				}
			}

			// Commodities
			//			out.write(nCommodities + "\n");
			int commodityIndex = 0;
			for (int f = 0; f < noNodes; f ++)
				for (int t = 0; t < noNodes; t++){
					if (switchLevelMatrix[f][t] != 0) {
						//						out.write(commodityIndex + " " + f + " " + t + " " + switchLevelMatrix[f][t] + "\n");
						commodityIndex ++;
					}
				}

			//< Objective
			out.write("Maximize \n");
			out.write("obj: ");
			String objective = "";

			
			int fid=0;
			for (int f = 0; f < noNodes; f++) {
				for (int t = 0; t < noNodes; t++) {
					if(switchLevelMatrix[f][t]>0)	{ //for each flow fid with source f
						for(int j=0; j<adjacencyList[f].size(); j++) { //for each out link of f = (f,j)
							objective += "f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue() + " ";
//							if (fid != commodityIndex-1 && j != adjacencyList[f].size() - 1)
//								objective += "+ ";
//							else
//								objective += "\n";
							if(j!=adjacencyList[f].size()-1)
								objective += "+ ";
						}
						if(fid != commodityIndex-1)
							objective += "+ ";
						else
							objective += "\n";
						fid++;
					}
				}	
			}
			out.write(objective);
			//>
			
		

			//<Constraints of Type 1: Load on link <= max_load
			out.write("\n\nSUBJECT TO \n\\Type 1: Load on link <= max_load\n");

			String constraint = "";
			for(int i=0; i<noNodes; i++) {
				for(int j=0; j<adjacencyList[i].size(); j++) {
					//for each edge (i,j)
					constraint = "c1_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + ": ";
					for(int fd_=0; fd_<commodityIndex; fd_++) {
						//for each flow fd_
						constraint += "f_" + fd_ + "_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + " ";
						if(fd_!=commodityIndex-1)
							constraint += "+ ";
					}
					out.write(constraint + "<= " + edgeCapacity + "\n");
				}
			}
			//>

			//<Constraints of Type 2: Flow conservation at non-source, non-destination
			out.write("\n\\Type 2: Flow conservation at non-source, non-destination\n");
			fid=0;
			for (int f = 0; f < noNodes; f++) {
				for (int t = 0; t < noNodes; t++) {
					if(switchLevelMatrix[f][t]>0)	{  //for each flow fid
						for(int u=0; u<noNodes; u++) { //for each node u
							constraint = "";
							if(u==f)  { //src
								constraint = "c2_" + fid + "_" + u + "_1: ";
								
								for(int j=0; j<adjacencyList[u].size(); j++) { //for each out link of u = (u,j)
									constraint += "f_" + fid + "_" + u + "_" + adjacencyList[u].elementAt(j).intValue() + " ";
									if(j!=adjacencyList[u].size()-1)
										constraint += "+ ";
									else
										constraint += "<= " + switchLevelMatrix[f][t] + "\n";
								}
								constraint += "c2_" + fid + "_" + u + "_2: ";
								for(int j=0; j<adjacencyList[u].size(); j++) { //for each in link of u = (j,u)
									constraint += "f_" + fid + "_" + adjacencyList[u].elementAt(j).intValue() + "_" + u + " ";
									if(j!=adjacencyList[u].size()-1)
										constraint += "+ ";
									else
										constraint += "= 0\n";
								}
							}
							else if(u==t) { //dst
								/*for(int j=0; j<adjacencyList[u].size(); j++) { //for each in link of u = (j,u)
									constraint += "f_ " + fid + "_" + j + "_" + u + " ";
									if(j!=adjacencyList[u].size()-1)
										constraint += "+ ";
									else
										constraint += "<= " + switchLevelMatrix[f][t] + "\n";
								}*/
								/*constraint += "c2_" + fid + "_" + u + "2: ";
								for(int j=0; j<adjacencyList[u].size(); j++) { //for each out link of u = (u,j)
									constraint += "f_ " + fid + "_" + u + "_" + j + " ";
									if(j!=adjacencyList[u].size()-1)
										constraint += "+ ";
									else
										constraint += "= 0\n";
								}*/
							}
							else{ // non-src and non-dest
								constraint = "c2_" + fid + "_" + u + "_3: ";
								for(int j=0; j<adjacencyList[u].size(); j++) { //for each out link of u = (u,j)
									constraint += "f_" + fid + "_" + u + "_" + adjacencyList[u].elementAt(j).intValue() + " ";
									if(j!=adjacencyList[u].size()-1)
										constraint += "+ ";
									else
										constraint += "- ";
								}
								for(int j=0; j<adjacencyList[u].size(); j++) { //for each in link of u = (j,u)
									constraint += "f_" + fid + "_" + adjacencyList[u].elementAt(j).intValue() + "_" + u + " ";
									if(j!=adjacencyList[u].size()-1)
										constraint += "- ";
									else
										constraint += "= 0\n";
								}
							}
							out.write(constraint);
						}
						fid++;
					}
				}	
			}

			out.close();
		}catch (Exception e){
			System.err.println("PrintGraphforNS2 Error: " + e.getMessage());
		}

	}

	public void PrintGraphforNS2(String filename, int nsvrp, int trafficmode, int transportprotocol){
		//trafficmode=1 : all to all
		//trafficmode=2 : every tor-sw send to k/2 stride
		//trafficmode=3 : every tor-sw send to k/4 stride
		//trafficmode=4 : every tor-sw send to random one
		int r=noNodes; //# of ToR switches
		int svrp=nsvrp; //# of server ports per switch
		int svrs=r*svrp;


		//transportprotocol=0 TCP Tahoe;
		//transportprotocol=1 UDP;
		int nflowlet = 6;
		//ArrayList<Integer> ls = RandomPermutation();
		try{

			ArrayList<Integer> rndmap = RandomPermutation(svrs);
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			for(int i=0; i<noNodes; i++)
			{
				out.write("set S(" + i + ") [$ns node]\n");
			}

			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					//if(adjacencyList[n1.intValue()].contains(n2) == false){
					int to = adjacencyList[i].elementAt(j).intValue();
					if(i < to)
					{
						out.write("$ns duplex-link $S(" + i + ") $S(" + to  + ") 100Mb 0.01ms DropTail\n");
						out.write("$ns queue-limit $S(" + i + ") $S(" + to  + ") 200\n");
					}
				}
			}
			//svrport = 1;
			int ito=0;

			if(trafficmode==1) //alltoall
				ito=svrs*(svrs-1);
			else if(trafficmode>=2)//random dst
				ito=svrs*nflowlet; //2x flows 20110416

			for(int i=0; i<ito; i++)
			{
				if(transportprotocol==0) //TCP
				{
					out.write("set tcpsrc(" + i + ") [new Agent/TCP/Reno]\n");
					out.write("set tcp_snk(" + i + ") [new Agent/TCPSink]\n");
					out.write("$tcpsrc(" + i + ") set fid_ " + i + "\n");
				}
				else //UDP
				{
					out.write("set tcpsrc(" + i + ") [new Agent/UDP]\n");
					out.write("set tcp_snk(" + i + ") [new Agent/Null]\n");
					out.write("$tcpsrc(" + i + ") set fid_ " + i + "\n");
				}
			}

			int index = 0;
			for(int i=0; i<svrs; i++)  //for sending server i
			{
				int jto=0; //number of receiving servers
				if(trafficmode==1) //alltoall
					jto=svrs;
				else if(trafficmode>=2 && trafficmode <=4)//random dst
					jto=1; //two servers now 20110416

				for(int j=0; j<jto; j++) //to receiving server j
				{
					int kbound = 0;
					if(trafficmode==1)
						kbound = 1;
					else
						kbound = nflowlet;
					for(int k=0; k<nflowlet; k++)
					{
						if((trafficmode==1 && i != j) || (trafficmode>=2))
						{
							int to=0;
							if(trafficmode==1) //alltoall
							{
								to=j;
							}
							else if(trafficmode==2)//random dst
							{
								to=(i+svrs/2)%svrs;
							}
							else if(trafficmode==3)
							{
								to=(i+svrs/4)%svrs;
							}
							else if(trafficmode==4)
							{
								to=rndmap.get(i);
							}

							double st_time = 0; //rand.nextDouble(); // 0~1.0 s
							//st_time /= 1000.0/2; // 0~0.5ms
							st_time += 0.002; // 1.0ms ~ 1.5ms

							int fromsw=i/svrp;
							int tosw=to/svrp;
							//if(i%svrp != 0)
							//   continue;

							System.out.println("from=" + i + ", to=" + to);
							System.out.println("fromsv=" + fromsw + ", tosv=" + tosw);

							if(fromsw != tosw)
							{
								out.write("$ns attach-agent $S(" + fromsw + ") $tcpsrc(" + index + ")\n");
								out.write("$ns attach-agent $S(" + tosw + ") $tcp_snk(" + index + ")\n");
								out.write("$ns connect $tcpsrc(" + index + ") $tcp_snk(" + index + ")\n");
								if(transportprotocol==0) //For TCP, we need FTP
								{
									out.write("set ftp(" + index + ") [new Application/FTP]\n");
									out.write("$ftp(" + index + ") attach-agent $tcpsrc(" + index + ")\n");
									out.write("$ftp(" + index + ") set type_ FTP\n");
								}
								else //For UDP, we send CBR...
								{
									double snd_rate = 100.0/(double)nflowlet;
									out.write("set ftp(" + index + ") [new Application/Traffic/CBR]\n");
									out.write("$ftp(" + index + ") set packetSize_ 1000\n");
									out.write("$ftp(" + index + ") set rate_ " + snd_rate + "Mb\n");
									out.write("$ftp(" + index + ") attach-agent $tcpsrc(" + index + ")\n");
								}
								out.write("$ns at " + st_time + " \"$ftp(" + index + ") start\"\n");
							}
							index++;
						}
					}
				}
			}
			out.close();
		}catch (Exception e){
			System.err.println("PrintGraphforNS2 Error: " + e.getMessage());
		}

	}


	/*
	 * Construction of the random graph give a degree and noNodes
	 */
	private void populateAdjacencyList(int swdeg, int degree, int mode, int ex_size, int nsvrs){

		//< First, the regular part
		int nodeIndex = 0;
		for (int r = 0; r < R_MAX; r ++)	// row
			for (int c = 0; c < C_MAX; c++ )	// column
				for (int d = 0; d < D_MAX; d++ ) {	// depth
					// This is node n[r][c][d] we're talking about
					
					// Does it connect North or South? 
					int north;			// north = 1 => North. Surprising, I know!
					if ((r+c) % 2 == 0) north = 1;
					else north = 0;
					
					// connect to r-1, r+1; north? c-1 : c+1; d+1; d-1; (All modulo r.max etc.)
					addBidirNeighbor(nodeIndex, getIndex(r-1, c, d));
					if (north == 1) addBidirNeighbor(nodeIndex, getIndex(r, c-1, d));
					addBidirNeighbor(nodeIndex, getIndex(r, c, d-1));

					//int distance = r + c + d;
					//if (nodeIndex > 1) myQueue.add(new NodeAndDistance(nodeIndex, distance));

					// Increment node index
					nodeIndex ++;
				}
		//>

		modifiedFloydWarshall();
		nodeIndex = 0;
		for (int r = 0; r < R_MAX; r ++)	
			for (int c = 0; c < C_MAX; c++ )
				for (int d = 0; d < D_MAX; d++ ) {
					// Record what order this node will actually be added to the network in
					//if (nodeIndex > 1) 
					//	myQueue.add(new NodeAndDistance(nodeIndex, shortestPathLen[0][nodeIndex]));
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
/*
				// Otherwise
				detach1 = randomEdges[chosen1][i];
				else {
					int foundSwap = 0;
					while (foundSwap == 0 && detach1 != 0) {
						// Find next element in someDegreesLeft which has free port-i
						adding2 = -1
						for (int f = 1; f < someDegreeLeft.size(); f++) {
							adding2 = someDegreeLeft.elementAt(f);
							if (randomEdges[adding2][i] == -1) break;
						}
						if (adding2 == -1) System.out.println("No luck finding adding2!");

						// Pick another random, with either FREE or differently attached port-i
						while (detach2 != detach1) {
							chosen2 = pickRand(adding2);
							detach2 = randomEdges[chosen2][i];
						}
						
						// if they have free port-i connect our new guy there, else SWAP!
						if (detach2 == -1) {
							randomEdges[adding2][i] = chosen2;
							randomEdges[chosen2][i] = adding2;
						}
						else foundSwap == 1;
					}
					
					// Edge swaps!
					if (randomEdges[chosen1][i] != detach2) {
						randomEdges[detach1][i] = detach2;
						randomEdges[detach2][i] = detach1;
					}
					randomEdges[adding1][i] = chosen1;
					randomEdges[chosen1][i] = adding1;
					randomEdges[adding2][i] = chosen2;
					randomEdges[chosen2][i] = adding2;
				}*/
			}
		}
		//>

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
	}
/*
	public static void main(String[] args){

		Integer numNodes = Integer.parseInt(args[0]);
		Integer swports = Integer.parseInt(args[1]);
		
		Integer numsrv = Integer.parseInt(args[2]);

		//int size, int swdeg, int degree, int mode, int ex_size, int nsvrs){

		Graph net = new SWDC_hex(numNodes, swports, -1, 1, 0, numsrv);
		String textGraph = net.toString();

		System.out.println(textGraph);
		return;
	}*/
}
