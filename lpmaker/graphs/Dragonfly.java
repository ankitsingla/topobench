
package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class Dragonfly extends Graph{

	int nsvrp;	
	int K_=0;
	int K=0;
	int K_int=0;
	
	public Dragonfly(int a, int p, int h, int z){
		super(a*(a*h/z+1));
		 // a = 2p = 2h    recommended value
		// z = 1         z is the number of connection between a pair of groups
		nsvrp = p;
		populateAdjacencyList(a, p, h, z, a*h/z + 1);
		name = "dragonfly";
	}
	//public Dragonfly(int a, int z){
	//	Dragonfly(a, a/2, a/2, z);
	//}
	/*
	public ArrayList RandomPermutation(int size, int serverport)
	{
		int screw;
		Random rand = new Random();
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
					if(i/serverport != ls.get(i+k)/serverport)
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
		}while(screw==1);*/
		/*for(int i=0; i<size; i++)
		  {
		  System.out.println(i + " to " + ls.get(i));
		  }*/
/*		return ls;
	}
	public void PrintGraphforMCF(String filename, int trafficmode, int transportprotocol){
		//trafficmode=1 : all to all
		//trafficmode=2 : every tor-sw send to k/2 stride
		//trafficmode=3 : every tor-sw send to k/4 stride
		//trafficmode=4 : every tor-sw send to random one
		int r=noNodes; //# of ToR switches
		int svrp=nsvrp; //# of server ports per switch
		int svrs=r*svrp;


		//transportprotocol=0 TCP Tahoe;
		//transportprotocol=1 UDP;
		int nflowlet = 1;
		//ArrayList<Integer> ls = RandomPermutation();
		Random rand = new Random();
		try{

			ArrayList<Integer> rndmap = RandomPermutation(svrs, svrp);
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);

			// Nodes
			//			out.write(noNodes + "\n");
			int numEdges = 0;
			for(int i=0; i<noNodes; i++){
				//				out.write(i + " " + i*10 + " " + i*10 + "\n");
				numEdges += adjacencyList[i].size();
			}

			System.out.println("CY");

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
					out.write(constraint + "<= " + edgeCapacity + "\n");
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
							else if(u==t) { //dst 			*/
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
/*							}
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
*/

	private void populateAdjacencyList(int a, int p, int h, int z, int g){
		//layout
		//We have noNodes switches. 
		//0 - (a-1) swtiches in the first group
		//a - (2a-1) swtiches in the second group
		
		//connect intra-group links
		System.out.println("D " + a + " " + p + " " + h + " " + z + " " + g);
		for(int group = 0; group < g; group++){ //group id
			for(int i = 0; i < a; i++){ // switches internal index in group
				for(int j = 0; j < a; j++){
					if(i < j)
					{
						//member 1 = g*a+i
						//member 2 = g*a+j
						System.out.println("DEB " + group*a+i + " " + group*a+j);
						addBidirNeighbor(group*a+i, group*a+j);
					}
				}
			}
		}
		
		Integer[] usedinterport = new Integer[g];
		for(int i=0; i<g; i++){
			usedinterport[i] = 0;
		}

		//connect inter-group links
		for(int groupa = 0; groupa < g; groupa++){ //groupa id
			for(int groupb = 0; groupb < g; groupb++){ //groupb id
				if(groupa < groupb)
				{
					for(int nc=0; nc<z; nc++){
						Integer ma = groupa*a+(usedinterport[groupa]%a);
						Integer mb = groupb*a+(usedinterport[groupb]%a);
							//member 1 = g*a+i
							//member 2 = g*a+j
						addBidirNeighbor(ma, mb);
						usedinterport[groupa]++;
						usedinterport[groupb]++;
					}
				}
			}
		}
	}
}
