
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class Legup extends Graph{
	
	public int nToR;
	public int budget;
	public int nAggSw;
	public int nCoreSw;
	public double agility;
	public int flexibility;
	public int reliability;
	public int nSvrs;

	public int[][] AggPorts; // [Type: 0=Total, 1=Up, 2=Down, 3=Free, 4=usedDown][Switch index]
	public int[][] CorePorts; // [Type: 0=Total, 1=usedDown][Switch Index]
	public int[][] ToRPorts; // [Type: 0=Total, 1=Up, 2=usedUp][SwitchIndex]
	public int totalToRupports;

	public Legup(int stage, String filename) throws IOException{
		//Readfromfile
		BufferedReader br = new BufferedReader(new FileReader(filename));
                String strLine = "";
                int linenum = 0;
		int aggSwIndex = 0;
		int coreSwIndex = 0;
		totalToRupports = 0;
		int state = 0; //0: do nothing. >0: in the correct iteration
			       //1: parse the first line of a statement
			       //2: parse the 2 line of the aggregations switch properties
			       //3: parse the 3 line of the aggregations switch properties
                while ((strLine = br.readLine()) != null){
			String[] strTok = strLine.split(" ");

			//System.out.println(strLine);
			String st = "" + stage;

                	if(strTok[0].equals("ITERATION") && strTok[1].equals(st)) 
			{
				
				state = 1;
				aggSwIndex = 0;
				coreSwIndex = 0;
			}
			
			if(state == 1) //we are in the correct stage
			{
				if(strTok[0].equals("TOTAL") && strTok[1].equals("ToRs"))
					nToR = Integer.parseInt(strTok[2]);
				if(strTok[0].equals("BUDGET") )
					budget = Integer.parseInt(strTok[1]);
				if(strTok[0].equals("aggr") && strTok[1].equals("switches") )
				{
					nAggSw = Integer.parseInt(strTok[2]);
					AggPorts = new int[5][];
					for (int i=0; i < 5; i++) {
						AggPorts[i] = new int[nAggSw];
						for(int j=0; j<nAggSw; j++)
							AggPorts[i][j]=0;
					}
					nCoreSw = Integer.parseInt(strTok[5]);
					CorePorts = new int[2][];
					for (int i=0; i < 2; i++){
						CorePorts[i] = new int[nCoreSw];
						for(int j=0; j<nCoreSw; j++)
							CorePorts[i][j]=0;
					}
					ToRPorts = new int[3][];
					for (int i=0; i < 3; i++){
						ToRPorts[i] = new int[nToR];
					}
					for(int j=0; j<nToR; j++)
					{
						ToRPorts[0][j]=24;
						ToRPorts[1][j]=0;
						ToRPorts[2][j]=0;
					}

				}
				if(strTok[0].equals("AGGR") && strTok[1].equals("SWITCH"))
				{
					AggPorts[0][aggSwIndex] = Integer.parseInt(strTok[2]);
					state = 2;
				}
				if(strTok[0].equals("CORE") && strTok[1].equals("SWITCH"))
				{
					CorePorts[0][coreSwIndex] = Integer.parseInt(strTok[2]);	
					coreSwIndex++;
				}
				if(strTok[0].equals("Metrics"))
				{
					agility = Double.parseDouble(strTok[1]);	
					flexibility = Integer.parseInt(strTok[2]);	
					reliability = Integer.parseInt(strTok[3]);	
					break;
				}
			}
			if(state == 2)
			{
				if(strTok[0].equals("upports"))
				{
					AggPorts[1][aggSwIndex] = Integer.parseInt(strTok[2]);
					state = 3;
				}
			}
			if(state == 3)
			{
				if(strTok[0].equals("downports"))
				{
					AggPorts[2][aggSwIndex] = Integer.parseInt(strTok[2]);
					totalToRupports+= AggPorts[2][aggSwIndex];
					state = 4;
				}
			}
			if(state == 4)
			{
				if(strTok[0].equals("freeports"))
				{
					AggPorts[3][aggSwIndex] = Integer.parseInt(strTok[2]);
					state = 1;
					aggSwIndex++;
				}
			}

		}
		br.close();


		// Sort by port count
		for (int i =0; i < AggPorts[0].length; i++) {
			for (int j =0; j < i; j++) {
				if (AggPorts[0][i] > AggPorts[0][j]) {
					for (int k = 0; k < 4; k++){
						int temp = AggPorts[k][i];
						AggPorts[k][i] = AggPorts[k][j];
						AggPorts[k][j] = temp;
					}
				}
			}
		}

				
		for (int i =0; i < CorePorts[0].length; i++) {
			for (int j =0; j < i; j++) {
				if (CorePorts[0][i] > CorePorts[0][j]) {
					int temp = CorePorts[0][i];
					CorePorts[0][i] = CorePorts[0][j];
					CorePorts[0][j] = temp;
				}
			}
		}
		

		noNodes = nAggSw + nToR + nCoreSw;
                System.out.println("Total Number of Switches (noNodes) = " + noNodes);
		nSvrs = nToR * 24;
		System.out.println("Number of Servers = " + nSvrs);
		localQ = new int[noNodes];
                creationBuffer = new int[noNodes];
                allocateAdjacencyList();
                setUpFixWeight(DEFAULT_NO_HOSTS);
                setUpAllSwitches();


		//print aggregation level for debugging
		/*for(int type=0; type<4; type++)
		{
			for(int aggindex=0; aggindex<nAggSw; aggindex++)
				System.out.println(AggPorts[type][aggindex] + ",");
			System.out.println("");
		}*/

		populateAdjacencyList();


		//public int[][] AggPorts; // [Type: 0=Total, 1=Up, 2=Down, 3=Free, 4=usedDown][Switch index]
		//public int[][] CorePorts; // [Type: 0=Total, 1=usedDown][Switch Index]
		int totalp=0;
		
		for (int i =0; i < AggPorts[0].length; i++) {
			totalp+=AggPorts[0][i];
		}
		for (int i =0; i < CorePorts[0].length; i++) {
			totalp+=CorePorts[0][i];
		}
		System.out.println("Total Agg and Core Switch Port = " + totalp);
		//name = "fat";
	}
	
	
	public ArrayList RandomPermutation()
	{
		int size = nSvrs;
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
		return i / (48 / 2); //ToR switch has 48 ports, a half of which are attached to the servers
	}

	public void PrintGraphforMCF(String filename, int trafficmode, int transportprotocol){
		//trafficmode=1 : all to all
		//trafficmode=2 : every tor-sw send to k/2 stride
		//trafficmode=3 : every tor-sw send to k/4 stride
		//trafficmode=4 : every tor-sw send to random one
		
		

		int svrp=24; //# of server ports per ToR switch
		int svrs=nSvrs;


		//transportprotocol=0 TCP Tahoe;
		//transportprotocol=1 UDP;
		int nflowlet = 1;
		//ArrayList<Integer> ls = RandomPermutation();
		try{

			ArrayList<Integer> rndmap = RandomPermutation();
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

							int fromsw=i/svrp;
							int tosw=to/svrp;

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
					out.write(constraint + "<= " + adjacencyList[i].elementAt(j).linkcapacity + "\n");
				}
			}
			//>

			System.out.println("HERE");
			
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
			System.err.println("PrintGraphforMCF Error: " + e.getMessage());
			e.printStackTrace();
		}

	}



	private int switchindexlookup(int type, int index)
	{
		if(type==0) // ToR
			return index;
		if(type==1) // Agg
			return (nToR + index);
		if(type==2) // Core
			return (nToR + nAggSw + index);
		else
			System.out.println("not this type of switch");
		return -1;
	}
	private int nodeIndextoswitchtype(int idx)
	{
		if(idx<nToR)
			return 0; //ToR
		else if(idx>=nToR && idx < nToR + nAggSw)
			return 1; //Agg
		else if(idx>nToR + nAggSw)
			return 2; //Core
		else
			System.out.println("not this type of switch!!");
		return -1;
	}

	private void populateAdjacencyList(){
		//layout
		
				// Switch Labelling
		// [0, (nToR-1)]: ToR Layer Switches
		// [nToR, (nToR + nAggSw - 1)]: Agg Layer Switches
		// [nToR+nAggSw, (nToR+nAggSw+nCoreSw)]: Core Layer Switches

		//public int nToR;
		//public int nAggSw;
		//public int nCoreSw;

		//public int[][] ToRPorts; // [Type: 0=Total, 1=Up][SwitchIndex]
		//public int[][] AggPorts; // [Type: 0=Total, 1=Up, 2=Down, 3=Free, 4=usedDown][Switch index]
		//public int[][] CorePorts; // [Type: 0=Total, 1=usedDown][Switch Index]

		//Derive the number of up-ports for ToRs. the first x ToRs have more up-ports.

		int HighPortCountforToR = (int) Math.ceil((double)totalToRupports / (double)nToR);
		int LowPortCountforToR = (int) Math.floor((double)totalToRupports / (double)nToR);

		int nHighPortCountToR=totalToRupports - LowPortCountforToR * nToR;
		int nLowPortCountToR= nToR - nHighPortCountToR;

		if(true)
		{
			System.out.println("CY: Creating Legup Topology...");
			System.out.println("CY: nToR= " + nToR + " , nAgg= " + nAggSw + " , nCore=" + nCoreSw);
			System.out.println("CY: First " + nHighPortCountToR + " ToR Switches have " + HighPortCountforToR + " uplinks each");
			System.out.println("CY: The remaining " + nLowPortCountToR + " ToR Switches have " + LowPortCountforToR + " uplink each");
			for(int i=0; i<nAggSw; i++)
				System.out.println("CY: AggSw " + i + " has " + AggPorts[0][i] + "/" + AggPorts[1][i] + "/" + AggPorts[2][i] + "/" + AggPorts[3][i] + "/" + AggPorts[4][i]);
		}
		for(int i=0; i<nToR; i++)
		{
			if(i<nHighPortCountToR)
				ToRPorts[1][i] = HighPortCountforToR;
			else
				ToRPorts[1][i] = LowPortCountforToR;
		}

		//Connect Agg to Core
		for (int agg = 0; agg < nAggSw; agg++) {
			int coreSwIndexWeUse=0;
			for (int aggPort = 0; aggPort < AggPorts[1][agg]; aggPort++) {
				for (int cooffset = 0; cooffset < nCoreSw; cooffset ++) {
					if (CorePorts[1][coreSwIndexWeUse] < CorePorts[0][coreSwIndexWeUse]) {
						addBidirNeighbor(switchindexlookup(1,agg), switchindexlookup(2,coreSwIndexWeUse));
						CorePorts[1][coreSwIndexWeUse]++;
						coreSwIndexWeUse = (coreSwIndexWeUse+1)%nCoreSw;
						break;
					}
					coreSwIndexWeUse = (coreSwIndexWeUse+1)%nCoreSw;
				}
				
			}
		} 
		
		

		//Connect ToR to Agg
		/*for (int mytor = 0; mytor < nToR; mytor++) {
			int aggSwIndexWeUse=0;
			for (int tport = 0; tport < ToRPorts[1][mytor]; tport++) {
				for (int cooffset = 0; cooffset < nAggSw; cooffset ++) {
					if (AggPorts[4][aggSwIndexWeUse] < AggPorts[2][aggSwIndexWeUse]) {
						addBidirNeighbor(switchindexlookup(0,mytor), switchindexlookup(1,aggSwIndexWeUse));
						AggPorts[4][aggSwIndexWeUse]++;

						//int x = AggPorts[2][aggSwIndexWeUse] - AggPorts[4][aggSwIndexWeUse];
						//System.out.println("ZZ ToR " + mytor + " Connect to Agg Sw " + aggSwIndexWeUse + " which have " + x + " remaining port now");

						aggSwIndexWeUse = (aggSwIndexWeUse+1)%nAggSw;
						break;
					}
					aggSwIndexWeUse = (aggSwIndexWeUse+1)%nAggSw;
				}
			}
		}*/
		for (int agg = 0; agg < nAggSw; agg++) {
			//int aggSwIndexWeUse=0;
			int ToRSwIndexWeUse=0;
			//for (int tport = 0; tport < ToRPorts[1][mytor]; tport++) {
			for( int tport = 0; tport < AggPorts[2][agg]; tport++) {
				for (int cooffset = 0; cooffset < nToR; cooffset ++) {
					if (ToRPorts[2][ToRSwIndexWeUse] < ToRPorts[1][ToRSwIndexWeUse]) {
						addBidirNeighbor(switchindexlookup(1,agg), switchindexlookup(0,ToRSwIndexWeUse));
						ToRPorts[2][ToRSwIndexWeUse]++;

						//int x = AggPorts[2][aggSwIndexWeUse] - AggPorts[4][aggSwIndexWeUse];
						//System.out.println("ZZ ToR " + mytor + " Connect to Agg Sw " + aggSwIndexWeUse + " which have " + x + " remaining port now")
						//aggSwIndexWeUse = (aggSwIndexWeUse+1)%nAggSw;
						ToRSwIndexWeUse = (ToRSwIndexWeUse+1)%nToR;
						break;
					}
					//aggSwIndexWeUse = (aggSwIndexWeUse+1)%nAggSw;
					ToRSwIndexWeUse = (ToRSwIndexWeUse+1)%nToR;
				}
			}
		}
	}
}
