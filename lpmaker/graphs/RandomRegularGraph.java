package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class RandomRegularGraph extends RegularGraph{
public int nsvrlp=0; //# servers with lower server ports
public int svrp=0; //server ports (lower)
public RandomRegularGraph(int size, int degree){
	super(size,degree);
	populateAdjacencyList(degree, degree, 0, 0, 0);
	name="rand";
}

public RandomRegularGraph(int size, int swdeg, int degree, int mode, int ex_size, int nsvrs, int pod_size){
	//this is the expension mode, where "size" is the overall size after expansion, where the last "ex_size" is the expension part. 
	//when mode=1 (server expansion), we want to add more switches with the same degree to support more servers.
	//when mode=2 (capacity expansion), we want to add more switches with the same degree, without adding servers.
//if nsvrs = 0, then we use the argument "degree" to generate homogeneous RRG
	//if nsvrs != 0, then we ignore the argument "degree". Instead, we generate homogeneous RRG
	super(size);
	populateAdjacencyList2Layer(swdeg, degree, mode, ex_size, nsvrs, pod_size);
	name="rand";
}

public RandomRegularGraph(int size, int swdeg, int degree, int mode, int ex_size, int nsvrs, int pod_size, boolean outer){
	//this is the expension mode, where "size" is the overall size after expansion, where the last "ex_size" is the expension part. 
	//when mode=1 (server expansion), we want to add more switches with the same degree to support more servers.
	//when mode=2 (capacity expansion), we want to add more switches with the same degree, without adding servers.
//if nsvrs = 0, then we use the argument "degree" to generate homogeneous RRG
	//if nsvrs != 0, then we ignore the argument "degree". Instead, we generate homogeneous RRG
	super(size);
	System.out.println("IN RIGHT CONSTRUCTOR");
	populateAdjacencyList1Layer(swdeg, degree, mode, ex_size, nsvrs, pod_size);
	name="rand";
}
public RandomRegularGraph(int size, int swdeg, int degree, int mode, int ex_size, int nsvrs){
	//this is the expension mode, where "size" is the overall size after expansion, where the last "ex_size" is the expension part. 
	//when mode=1 (server expansion), we want to add more switches with the same degree to support more servers.
	//when mode=2 (capacity expansion), we want to add more switches with the same degree, without adding servers.
//if nsvrs = 0, then we use the argument "degree" to generate homogeneous RRG
	//if nsvrs != 0, then we ignore the argument "degree". Instead, we generate homogeneous RRG
	super(size);
	populateAdjacencyList(swdeg, degree, mode, ex_size, nsvrs);
	name="rand";
}

public RandomRegularGraph(int size, int swdeg, int degree, int mode, int ex_size, int nsvrs, double fail_rate){
	super(size);
	populateAdjacencyList(swdeg, degree, mode, ex_size, nsvrs);
	name="rand";
	failLinks(fail_rate);
	String textGraph = toString();

	//System.out.println(textGraph);
}

public RandomRegularGraph(int size, int swdeg, int degree, int mode, int ex_size, int nsvrs, double fail_rate, String nstage){
	super(size);
	int nstage_nm = Integer.parseInt(nstage);
	ex_size = (nstage_nm-1) * 20;
	populateAdjacencyList(swdeg, degree, mode, ex_size, nsvrs);
	name="rand";
	failLinks(fail_rate);
	String textGraph = toString();

}

public RandomRegularGraph(int size, int swdeg, int degree, int mode, int ex_size, int nsvrs, double fail_rate, int ext_nsw){
	super(size);
	populateAdjacencyListLegup(swdeg, degree, mode, ex_size, nsvrs, ext_nsw);
	name="rand";
	failLinks(fail_rate);
	String textGraph = toString();

	//System.out.println(textGraph);
}
/*public RandomRegularGraph(int size, int noHosts,int degree){
  super(size,noHosts,degree);
  populateAdjacencyList(degree);
  name="rand";
  }*/

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
public int svrToSwitch(int i)	//i is the server index. return the switch index.
{
	if(i < this.svrp * this.nsvrlp)
			return i/this.svrp;
		else
		{
			int k = (i-this.svrp*this.nsvrlp)/(this.svrp+1);
			return k + this.nsvrlp;
		}
	}

public int svrtoswitch(int i)	//i is the server index. return the switch index.
{
	if(i < this.svrp * this.nsvrlp)
			return i/this.svrp;
		else
		{
			int k = (i-this.svrp*this.nsvrlp)/(this.svrp+1);
			return k + this.nsvrlp;
		}
	}
	public void PrintGraphforFBW(String filename){
	 try{
		FileWriter fstream = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(fstream);
		System.out.println("hi");
		
		// # of switches:
		out.write(noNodes + "");
		out.write("\n");
		
		// adj matrix
		int [] OutDeg = new int[noNodes];
		for(int i=0; i<noNodes; i++)
		{
			for(int j=0; j<noNodes; j++)
			{
				OutDeg[j]=0;
			}
			for(int j=0; j<adjacencyList[i].size(); j++)
			{
				int k = adjacencyList[i].elementAt(j).intValue();
				OutDeg[k] = 1;
			}
			for(int j=0; j<noNodes; j++)
			{
				if(OutDeg[j]==1)
					out.write("[" + OutDeg[j] +  "]");
					//out.write("[" + OutDeg[j] + "," + OutDeg[j] +  "]");
				else
					out.write("[" + "]");
			}
			out.write("\n");
		}
		// r: The r: row indicates the uplink bandwidth of the rack. So if you have 24 servers, each with a 10Gb uplink, this should be 240 for that rack.
		// first (nsvrlp) switches use (svrp) server ports. other (noNodes-nsvrlp) switches use (svrp+1) server ports
		out.write("r:\n");
		for(int i=0; i<this.nsvrlp; i++)
			out.write(this.svrp + " ");
		int usvrp = this.svrp+1;
		for(int i=this.nsvrlp; i<noNodes; i++)
			out.write(usvrp + " ");
		out.write("\n");
		//You should set the c row values to be the same as the r row. This is downlink bandwidth.
		out.write("c:\n");
		for(int i=0; i<this.nsvrlp; i++)
			out.write(this.svrp + " ");
		usvrp = this.svrp+1;
		for(int i=this.nsvrlp; i<noNodes; i++)
			out.write(usvrp + " ");
		out.write("\n");
		//o1 is the number of free gigabit ports at that rack. you don't need to worry about this, but should have the correct number of nodes or else reading the file will fail.
		out.write("o1:\n");
		for(int i=0; i<noNodes; i++)
			out.write(0 + " ");
		out.write("\n");
		//o10 is the same for 10Gb ports. Again, you need n values here.
		out.write("o10:\n");
		for(int i=0; i<noNodes; i++)
			out.write(0 + " ");
		out.write("\n");
		//d is the delay. Again, you need n values here and these values won't matter for you.
		out.write("d:\n");
		for(int i=0; i<noNodes; i++)
			out.write(0 + " ");
		out.write("\n");		

		//close output file stream
		out.close();
	 }catch (Exception e){
		System.err.println("PrintGraphforFBW Error: " + e.getMessage());
	 }
	}

	public void PrintGraphforMCFLegup(String filename, int trafficmode, int transportprotocol){
		//trafficmode=1 : all to all
		//trafficmode=2 : every tor-sw send to k/2 stride
		//trafficmode=3 : every tor-sw send to k/4 stride
		//trafficmode=4 : every tor-sw send to random one
		// first (nsvrlp) switches use (svrp) server ports. other (noNodes-nsvrlp) switches use (svrp+1) server ports

		int svrp = this.svrp;
		int nsvrlp = this.nsvrlp;

		int r=noNodes; //# of ToR switches
		//int svrp=nsvrp; //# of server ports per switch
		int svrs=nsvrlp * svrp;  //# of servers

		System.out.println("CY " + this.svrp + " " + this.nsvrlp);
		System.out.println("CY " + svrtoswitch(0));
		System.out.println("CY " + svrtoswitch(10));
		System.out.println("CY " + svrtoswitch(20));
		System.out.println("CY " + svrtoswitch(30));
		System.out.println("CY " + svrtoswitch(41));
		System.out.println("CY " + svrtoswitch(42));

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


	private void populateAdjacencyListLegup(int swdeg, int degree, int mode, int ex_size, int nsvrs, int nnetsw){
		int old_size = noNodes - ex_size;

		System.out.println("WARNING: oldsize="+old_size);
		System.out.println("WARNING: ex_size="+ex_size);
		System.out.println("WARNING:  mode  ="+mode);
		if(nsvrs>0)
			System.out.println("WARNING:  heterogeneous RRG with degree " + (double)nsvrs/(double)old_size);
		else
			System.out.println("WARNING:  homogeneous RRG with degree " + degree);


		int d_s; // lower server degree
		int nlds; // number of switches with server degree
		int nhds; // number of switches with 0 server degree


		d_s = swdeg - degree;
		nlds = noNodes - nnetsw;
		nhds = nnetsw;

		this.nsvrlp=nlds;
		this.svrp=d_s;

			
		int d_n = (swdeg - d_s)/1; //higher network degree //ankitlook
		//int nhdn = nlds; //# of switches with higher network degree
		//int nldn = nhds; //# of switches with lower network degree (by 1)

		System.out.println("WARNING: " + nlds + " switches have " + d_n + " network ports, while " + nhds + " switches have " + swdeg + " network ports");
		
		//first run.
		if(true)
		{
			Vector still_to_link = new Vector(old_size);		// Nodes with some degree still available
			Vector degrees = new Vector(old_size);			// Degree currently used up

			for(int i = 0; i < old_size; i++){
				still_to_link.add(new Integer(i));			// Initialize with all nodes
				degrees.add(new Integer(0));				// Initialize with '0' used degree
			}

			int stop_sign=0;	
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
					//return;
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
					for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					{
						//if(adjacencyList[n1.intValue()].contains(n2) == false){
						if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2)
						{
							k=1;
						}


					}
					if(k==0)	// Edge doesn't already exist. Good, add it!
					{
						found = true;
						addBidirNeighbor(n1, n2);
					}
				}
				if(stop_sign==0)
				{
					/*
					 * If a clique of nodes is left in the end, this gives up
					 */
					if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()+1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()+1));
					boolean deleted_p1 = false;
					//if(((Integer)degrees.elementAt(p1)).intValue() == degree){
					if(((Integer)degrees.elementAt(p1)).intValue() == d_n && n1.intValue() < nlds || ((Integer)degrees.elementAt(p1)).intValue() == (swdeg) && n1.intValue() >= nlds){
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

					//if(((Integer)degrees.elementAt(p2_updated)).intValue() == degree){
					if(((Integer)degrees.elementAt(p2_updated)).intValue() == d_n && n2.intValue() < nlds || ((Integer)degrees.elementAt(p2_updated)).intValue() == (swdeg) && n2.intValue() >= nlds){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}

		//second run. do the expansion.
		if(mode>0 && ex_size>0)
		{	

			for(int fish = noNodes-ex_size; fish < noNodes; fish++) // for each new fish
			{

				int idegree=0; // the current degree of node i
				int iter=0; //the # of attempts
				int mom_i_found_it=0;
				//we want to break "degree/2" edges
				//suppose we choose edge (a,b) to break, then following three conditions has to meet:
				//1) (a,b) has an edge
				//2) (i,a) has no edge
				//3) (i,b) has no edge.
				while(idegree <=  (degree - 2) && iter++ < 8000)
				{
					int p1=fish;
					int p2=fish;
					while(p1 == fish || p1 == p2){
						p1 = rand.nextInt(noNodes);
					}
					while(p2 == fish || p2 == p1){
						p2 = rand.nextInt(noNodes);
					}
					//now we have p1 != fish, p2 != fish, p1 != p2

					// Check if an (p1, p2) edge already exists
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

					if(k1==1 && k2 == 0 && k3 == 0)	// Good keyboard, break it!
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
				}
				if(mom_i_found_it==0){	// Give up if can't find a proper link
					System.out.println("WARNING: Dad, this is so weird");
					//return;
				}
			}
		}


		//third run.
		if(mode>0 && ex_size>0)
		{
			Vector still_to_link = new Vector(noNodes);		// Nodes with some degree still available
			Vector degrees = new Vector(noNodes);			// Degree currently used up

			for(int i = 0; i < noNodes; i++){
				if(adjacencyList[i].size() != degree)
				{
					still_to_link.add(new Integer(i));			// Initialize with all nodes
					degrees.add(new Integer(adjacencyList[i].size()));				// Initialize with '0' used degree
				}
			}

			int stop_sign=0;	
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
					//return;
				}
				boolean found = false;

				int p1 = -1, p2 = -1;
				Integer n1,n2;

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
					for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					{
						//if(adjacencyList[n1.intValue()].contains(n2) == false){
						if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2)
						{
							k=1;
						}
					}
					if(k==0)	// Edge doesn't already exist. Good, add it!
					{
						found = true;
						addBidirNeighbor(n1, n2);
						//System.out.println("ADD: ("+ n1 + ", " + n2 + ")");
					}
				}
				if(stop_sign==0)
				{
					/*
					 * If a clique of nodes is left in the end, this gives up
					 */
					if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()+1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()+1));
					boolean deleted_p1 = false;
					if(((Integer)degrees.elementAt(p1)).intValue() == degree){
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

					if(((Integer)degrees.elementAt(p2_updated)).intValue() == degree){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}	
		/*for(int i=0; i<noNodes; i++)
								  {
								  System.out.println(adjacencyList[i].size());
								  }*/
	}

	/*
	 * Construction of the random graph give a degree and noNodes
	 */

	public void expand(int from, int to, int degree)
	{
		System.out.println("From " + from + ", To " + to);
		//second run. do the expansion.
		//if(mode>0 && ex_size>0)
		{

			//for(int fish = noNodes-ex_size; fish < noNodes; fish++) // for each new fish
			for(int fish = from; fish < to; fish++) // for each new fish
			{

				//System.out.println("Second stage. mode=" + mode + ", ex_size=" + ex_size);
				int idegree=0; // the current degree of node i
				int iter=0; //the # of attempts
				int mom_i_found_it=0;
				//we want to break "degree/2" edges
				//suppose we choose edge (a,b) to break, then following three conditions has to meet:
				//1) (a,b) has an edge
				//2) (i,a) has no edge
				//3) (i,b) has no edge.
				while(idegree <=  (degree - 2) && iter++ < 800000)
				{
					int p1=fish;
					int p2=fish;
					while(p1 == fish || p1 == p2){
						p1 = rand.nextInt(fish);
					}
					while(p2 == fish || p2 == p1){
						p2 = rand.nextInt(fish);
					}
					//now we have p1 != fish, p2 != fish, p1 != p2

					// Check if an (p1, p2) edge already exists
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

					if(k1==1 && k2 == 0 && k3 == 0)	// Good keyboard, break it!
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
				}
				if(mom_i_found_it==0){	// Give up if can't find a proper link
					System.out.println("WARNING: Dad, this is so weird");
					//return;
				}
			}
		}
	}

	private void populateAdjacencyList1Layer(int swdeg, int degree, int mode, int ex_size, int nsvrs, int pod_size){
		int old_size = noNodes - ex_size;

		System.out.println("WARNING: oldsize="+old_size);
		System.out.println("WARNING: ex_size="+ex_size);
		System.out.println("WARNING:  mode  ="+mode);
		if(nsvrs>0)
			System.out.println("WARNING:  heterogeneous RRG with degree " + (double)nsvrs/(double)old_size);
		else
			System.out.println("WARNING:  homogeneous RRG with degree " + degree);


		int d_s; // lower server degree
		int nlds; // number of switches with lower server degree
		int nhds; // number of switches with higher server degree (by 1)

		if(nsvrs==0) {
			d_s = (swdeg - degree);
			nlds = noNodes;
			nhds = 0;
		}
		else{
			d_s = nsvrs/old_size;
		   nhds = nsvrs - (old_size * d_s); 
			nlds = noNodes - nhds;
      }
		//this.nsvrlp=nlds;				// ANKIT CHANGED
		this.nsvrlp = old_size;

		this.svrp=d_s;

				
		int d_n = swdeg - d_s; //higher network degree
		int nhdn = nlds; //# of switches with higher network degree
		int nldn = nhds; //# of switches with lower network degree (by 1)

		// 0 to p-1; p to 2p-1, etc. are the pods.

		System.out.println("WARNING: " + nhdn + " switches have " + d_n + " network ports, while " + nldn + " switches have " + (d_n -1) + " network ports");
		//first run.
		if(true)
		{
			Vector still_to_link = new Vector(old_size);		// Nodes with some degree still available
			Vector degrees = new Vector(old_size);			// Degree currently used up

			for(int i = 0; i < old_size; i++){
				still_to_link.add(new Integer(i));			// Initialize with all nodes
				degrees.add(new Integer(0));				// Initialize with '0' used degree
			}

			int stop_sign=0;	
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
					//return;
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
					int same_pod = 0;
					while(p2 == p1){
						p2 = rand.nextInt(still_to_link.size());
					}

					n1 = (Integer)still_to_link.elementAt(p1);
					n2 = (Integer)still_to_link.elementAt(p2);
					
					if (n1/pod_size == n2/pod_size) continue;

					// Check if an n1-n2 edge already exists
					int k=0;
					for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					{
						//if(adjacencyList[n1.intValue()].contains(n2) == false){
						if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2)
						{
							k=1;
						}


					}
					if(k==0)	// Edge doesn't already exist. Good, add it!
					{
						found = true;
						addBidirNeighbor(n1, n2);
					}
				}
				if(stop_sign==0)
				{
					/*
					 * If a clique of nodes is left in the end, this gives up
					 */
					if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()+1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()+1));
					boolean deleted_p1 = false;
					//if(((Integer)degrees.elementAt(p1)).intValue() == degree){
					if(((Integer)degrees.elementAt(p1)).intValue() == d_n && n1.intValue() < nhdn || ((Integer)degrees.elementAt(p1)).intValue() == (d_n - 1) && n1.intValue() >= nhdn){
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

					//if(((Integer)degrees.elementAt(p2_updated)).intValue() == degree){
					if(((Integer)degrees.elementAt(p2_updated)).intValue() == d_n && n2.intValue() < nhdn || ((Integer)degrees.elementAt(p2_updated)).intValue() == (d_n - 1) && n2.intValue() >= nhdn){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}
		/*
		//second run. do the expansion.
		if(mode>0 && ex_size>0)
		{

			for(int fish = noNodes-ex_size; fish < noNodes; fish++) // for each new fish
			{

				int idegree=0; // the current degree of node i
				int iter=0; //the # of attempts
				int mom_i_found_it=0;
				//we want to break "degree/2" edges
				//suppose we choose edge (a,b) to break, then following three conditions has to meet:
				//1) (a,b) has an edge
				//2) (i,a) has no edge
				//3) (i,b) has no edge.
				while(idegree <=  (degree - 2) && iter++ < 8000)
				{
					int p1=fish;
					int p2=fish;
					while(p1 == fish || p1 == p2){
						p1 = rand.nextInt(noNodes);
					}
					while(p2 == fish || p2 == p1){
						p2 = rand.nextInt(noNodes);
					}
					//now we have p1 != fish, p2 != fish, p1 != p2

					// Check if an (p1, p2) edge already exists
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

					if(k1==1 && k2 == 0 && k3 == 0)	// Good keyboard, break it!
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
				}
				if(mom_i_found_it==0){	// Give up if can't find a proper link
					System.out.println("WARNING: Dad, this is so weird");
					//return;
				}
			}
		}


		//third run.
		if(mode>0 && ex_size>0)
		{
			Vector still_to_link = new Vector(noNodes);		// Nodes with some degree still available
			Vector degrees = new Vector(noNodes);			// Degree currently used up

			for(int i = 0; i < noNodes; i++){
				if(adjacencyList[i].size() != degree)
				{
					still_to_link.add(new Integer(i));			// Initialize with all nodes
					degrees.add(new Integer(adjacencyList[i].size()));				// Initialize with '0' used degree
				}
			}

			int stop_sign=0;	
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
					//return;
				}
				boolean found = false;

				int p1 = -1, p2 = -1;
				Integer n1,n2;

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
					for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					{
						//if(adjacencyList[n1.intValue()].contains(n2) == false){
						if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2)
						{
							k=1;
						}
					}
					if(k==0)	// Edge doesn't already exist. Good, add it!
					{
						found = true;
						addBidirNeighbor(n1, n2);
						//System.out.println("ADD: ("+ n1 + ", " + n2 + ")");
					}
				}
				if(stop_sign==0)
				{
					if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()+1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()+1));
					boolean deleted_p1 = false;
					if(((Integer)degrees.elementAt(p1)).intValue() == degree){
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

					if(((Integer)degrees.elementAt(p2_updated)).intValue() == degree){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}*/	
		/*for(int i=0; i<noNodes; i++)
								  {
								  System.out.println(adjacencyList[i].size());
								  }*/
	}
	private void populateAdjacencyList2Layer(int swdeg, int degree, int mode, int ex_size, int nsvrs, int pod_size){
		int old_size = noNodes - ex_size;

		System.out.println("WARNING: oldsize="+old_size);
		System.out.println("WARNING: ex_size="+ex_size);
		System.out.println("WARNING:  mode  ="+mode);
		if(nsvrs>0)
			System.out.println("WARNING:  heterogeneous RRG with degree " + (double)nsvrs/(double)old_size);
		else
			System.out.println("WARNING:  homogeneous RRG with degree " + degree);


		int d_s; // lower server degree
		int nlds; // number of switches with lower server degree
		int nhds; // number of switches with higher server degree (by 1)

		if(nsvrs==0) {
			d_s = (swdeg - degree);
			nlds = noNodes;
			nhds = 0;
		}
		else{
			d_s = nsvrs/old_size;
		   nhds = nsvrs - (old_size * d_s); 
			nlds = noNodes - nhds;
      }
		//this.nsvrlp=nlds;				// ANKIT CHANGED
		this.nsvrlp = old_size;

		this.svrp=d_s;

				
		int d_n = swdeg - d_s; //higher network degree
		int nhdn = nlds; //# of switches with higher network degree
		int nldn = nhds; //# of switches with lower network degree (by 1)

		// 0 to p-1; p to 2p-1, etc. are the pods.

		System.out.println("WARNING: " + nhdn + " switches have " + d_n + " network ports, while " + nldn + " switches have " + (d_n -1) + " network ports");
		//first run.
		if(true)
		{
			Vector still_to_link = new Vector(old_size);		// Nodes with some degree still available
			Vector degrees = new Vector(old_size);			// Degree currently used up

			for(int i = 0; i < old_size; i++){
				still_to_link.add(new Integer(i));			// Initialize with all nodes
				degrees.add(new Integer(0));				// Initialize with '0' used degree
			}

			int stop_sign=0;	
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
					//return;
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
					int same_pod = 0;
					while(p2 == p1){
						p2 = rand.nextInt(still_to_link.size());
					}

					n1 = (Integer)still_to_link.elementAt(p1);
					n2 = (Integer)still_to_link.elementAt(p2);
					
					if (n1/pod_size != n2/pod_size) continue;

					// Check if an n1-n2 edge already exists
					int k=0;
					for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					{
						//if(adjacencyList[n1.intValue()].contains(n2) == false){
						if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2)
						{
							k=1;
						}


					}
					if(k==0)	// Edge doesn't already exist. Good, add it!
					{
						found = true;
						addBidirNeighbor(n1, n2);
					}
				}
				if(stop_sign==0)
				{
					/*
					 * If a clique of nodes is left in the end, this gives up
					 */
					if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()+1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()+1));
					boolean deleted_p1 = false;
					//if(((Integer)degrees.elementAt(p1)).intValue() == degree){
					if(((Integer)degrees.elementAt(p1)).intValue() == d_n && n1.intValue() < nhdn || ((Integer)degrees.elementAt(p1)).intValue() == (d_n - 1) && n1.intValue() >= nhdn){
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

					//if(((Integer)degrees.elementAt(p2_updated)).intValue() == degree){
					if(((Integer)degrees.elementAt(p2_updated)).intValue() == d_n && n2.intValue() < nhdn || ((Integer)degrees.elementAt(p2_updated)).intValue() == (d_n - 1) && n2.intValue() >= nhdn){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}
		/*
		//second run. do the expansion.
		if(mode>0 && ex_size>0)
		{

			for(int fish = noNodes-ex_size; fish < noNodes; fish++) // for each new fish
			{

				int idegree=0; // the current degree of node i
				int iter=0; //the # of attempts
				int mom_i_found_it=0;
				//we want to break "degree/2" edges
				//suppose we choose edge (a,b) to break, then following three conditions has to meet:
				//1) (a,b) has an edge
				//2) (i,a) has no edge
				//3) (i,b) has no edge.
				while(idegree <=  (degree - 2) && iter++ < 8000)
				{
					int p1=fish;
					int p2=fish;
					while(p1 == fish || p1 == p2){
						p1 = rand.nextInt(noNodes);
					}
					while(p2 == fish || p2 == p1){
						p2 = rand.nextInt(noNodes);
					}
					//now we have p1 != fish, p2 != fish, p1 != p2

					// Check if an (p1, p2) edge already exists
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

					if(k1==1 && k2 == 0 && k3 == 0)	// Good keyboard, break it!
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
				}
				if(mom_i_found_it==0){	// Give up if can't find a proper link
					System.out.println("WARNING: Dad, this is so weird");
					//return;
				}
			}
		}


		//third run.
		if(mode>0 && ex_size>0)
		{
			Vector still_to_link = new Vector(noNodes);		// Nodes with some degree still available
			Vector degrees = new Vector(noNodes);			// Degree currently used up

			for(int i = 0; i < noNodes; i++){
				if(adjacencyList[i].size() != degree)
				{
					still_to_link.add(new Integer(i));			// Initialize with all nodes
					degrees.add(new Integer(adjacencyList[i].size()));				// Initialize with '0' used degree
				}
			}

			int stop_sign=0;	
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
					//return;
				}
				boolean found = false;

				int p1 = -1, p2 = -1;
				Integer n1,n2;

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
					for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					{
						//if(adjacencyList[n1.intValue()].contains(n2) == false){
						if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2)
						{
							k=1;
						}
					}
					if(k==0)	// Edge doesn't already exist. Good, add it!
					{
						found = true;
						addBidirNeighbor(n1, n2);
						//System.out.println("ADD: ("+ n1 + ", " + n2 + ")");
					}
				}
				if(stop_sign==0)
				{
					if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()+1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()+1));
					boolean deleted_p1 = false;
					if(((Integer)degrees.elementAt(p1)).intValue() == degree){
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

					if(((Integer)degrees.elementAt(p2_updated)).intValue() == degree){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}*/	
		/*for(int i=0; i<noNodes; i++)
								  {
								  System.out.println(adjacencyList[i].size());
								  }*/
	}
	private void populateAdjacencyList(int swdeg, int degree, int mode, int ex_size, int nsvrs){
		int old_size = noNodes - ex_size;

		System.out.println("UNDER USE!!!");
		System.out.println("WARNING: oldsize="+old_size);
		System.out.println("WARNING: ex_size="+ex_size);
		System.out.println("WARNING:  mode  ="+mode);
		if(nsvrs>0)
			System.out.println("WARNING:  heterogeneous RRG with degree " + (double)nsvrs/(double)old_size);
		else
			System.out.println("WARNING:  homogeneous RRG with degree " + degree);


		int d_s; // lower server degree
		int nlds; // number of switches with lower server degree
		int nhds; // number of switches with higher server degree (by 1)

		if(nsvrs==0) {
			d_s = (swdeg - degree);
			nlds = noNodes;
			nhds = 0;
		}
		else{
			d_s = nsvrs/old_size;
		   nhds = nsvrs - (old_size * d_s); 
			nlds = noNodes - nhds;
      }
		this.nsvrlp=nlds;				// ANKIT CHANGED
		//this.nsvrlp = old_size;

		this.svrp=d_s;

				
		int d_n = swdeg - d_s; //higher network degree
		int nhdn = nlds; //# of switches with higher network degree
		int nldn = nhds; //# of switches with lower network degree (by 1)

		// First nhdh are the ones with fewer servers
		//set weights
		setUpFixWeight(0);
		for(int t = 0; t < noNodes; t++){
			int curr_weight = (t < nhdn) ? d_s : (d_s + 1);
			weightEachNode[t] = curr_weight;
			totalWeight += curr_weight;
		}

		System.out.println("WARNING: " + nhdn + " switches have " + d_n + " network ports, while " + nldn + " switches have " + (d_n -1) + " network ports");
		//first run.
		if(true)
		{
			Vector still_to_link = new Vector(old_size);		// Nodes with some degree still available
			Vector degrees = new Vector(old_size);			// Degree currently used up

			for(int i = 0; i < old_size; i++){
				still_to_link.add(new Integer(i));			// Initialize with all nodes
				degrees.add(new Integer(0));				// Initialize with '0' used degree
			}

			int stop_sign=0;	
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
					//return;
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
					for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					{
						//if(adjacencyList[n1.intValue()].contains(n2) == false){
						if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2)
						{
							k=1;
						}


					}
					if(k==0)	// Edge doesn't already exist. Good, add it!
					{
						found = true;
						addBidirNeighbor(n1, n2);
					}
				}
				if(stop_sign==0)
				{
					/*
					 * If a clique of nodes is left in the end, this gives up
					 */
					if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()+1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()+1));
					boolean deleted_p1 = false;
					//if(((Integer)degrees.elementAt(p1)).intValue() == degree){
					if(((Integer)degrees.elementAt(p1)).intValue() == d_n && n1.intValue() < nhdn || ((Integer)degrees.elementAt(p1)).intValue() == (d_n - 1) && n1.intValue() >= nhdn){
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

					//if(((Integer)degrees.elementAt(p2_updated)).intValue() == degree){
					if(((Integer)degrees.elementAt(p2_updated)).intValue() == d_n && n2.intValue() < nhdn || ((Integer)degrees.elementAt(p2_updated)).intValue() == (d_n - 1) && n2.intValue() >= nhdn){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}
		/*
		//second run. do the expansion.
		if(mode>0 && ex_size>0)
		{

			for(int fish = noNodes-ex_size; fish < noNodes; fish++) // for each new fish
			{

				int idegree=0; // the current degree of node i
				int iter=0; //the # of attempts
				int mom_i_found_it=0;
				//we want to break "degree/2" edges
				//suppose we choose edge (a,b) to break, then following three conditions has to meet:
				//1) (a,b) has an edge
				//2) (i,a) has no edge
				//3) (i,b) has no edge.
				while(idegree <=  (degree - 2) && iter++ < 8000)
				{
					int p1=fish;
					int p2=fish;
					while(p1 == fish || p1 == p2){
						p1 = rand.nextInt(noNodes);
					}
					while(p2 == fish || p2 == p1){
						p2 = rand.nextInt(noNodes);
					}
					//now we have p1 != fish, p2 != fish, p1 != p2

					// Check if an (p1, p2) edge already exists
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

					if(k1==1 && k2 == 0 && k3 == 0)	// Good keyboard, break it!
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
				}
				if(mom_i_found_it==0){	// Give up if can't find a proper link
					System.out.println("WARNING: Dad, this is so weird");
					//return;
				}
			}
		}


		//third run.
		if(mode>0 && ex_size>0)
		{
			Vector still_to_link = new Vector(noNodes);		// Nodes with some degree still available
			Vector degrees = new Vector(noNodes);			// Degree currently used up

			for(int i = 0; i < noNodes; i++){
				if(adjacencyList[i].size() != degree)
				{
					still_to_link.add(new Integer(i));			// Initialize with all nodes
					degrees.add(new Integer(adjacencyList[i].size()));				// Initialize with '0' used degree
				}
			}

			int stop_sign=0;	
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){				// Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
					//return;
				}
				boolean found = false;

				int p1 = -1, p2 = -1;
				Integer n1,n2;

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
					for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					{
						//if(adjacencyList[n1.intValue()].contains(n2) == false){
						if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2)
						{
							k=1;
						}
					}
					if(k==0)	// Edge doesn't already exist. Good, add it!
					{
						found = true;
						addBidirNeighbor(n1, n2);
						//System.out.println("ADD: ("+ n1 + ", " + n2 + ")");
					}
				}
				if(stop_sign==0)
				{
					if(iteration >= MAX_ITERATION){	// Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()+1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()+1));
					boolean deleted_p1 = false;
					if(((Integer)degrees.elementAt(p1)).intValue() == degree){
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

					if(((Integer)degrees.elementAt(p2_updated)).intValue() == degree){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}*/	
		/*for(int i=0; i<noNodes; i++)
								  {
								  System.out.println(adjacencyList[i].size());
								  }*/
	}

	public static void main(String[] args){

		Integer numNodes = Integer.parseInt(args[0]);
		Integer swports = Integer.parseInt(args[1]);
		
		Integer numsrv = Integer.parseInt(args[2]);
		Integer pod_size = Integer.parseInt(args[3]);
		Integer numports1 = Integer.parseInt(args[4]);
		Integer numports2 = Integer.parseInt(args[5]);
		

		//int size, int swdeg, int degree, int mode, int ex_size, int nsvrs){

		Graph net = new RandomRegularGraph(numNodes, swports, numports1, 1, 0, 0, pod_size, true);
		String textGraph = net.toString();

		System.out.println(textGraph);
		
		net = new RandomRegularGraph(numNodes, swports, numports2, 1, 0, 0, pod_size);
		textGraph = net.toString();

		System.out.println(textGraph);
		return;
	}
}
