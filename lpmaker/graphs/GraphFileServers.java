package lpmaker.graphs;

import java.util.*;
import java.io.*;

public class GraphFileServers extends Graph{
	public int totalSrv = 0;

	public GraphFileServers(int size, String fileName, boolean heterCap){
		super(size);
		populateAdjacencyList(fileName, heterCap);
		name="fromfile";
	}

	/*
	 * Construction of the graph give an input file
	 * INPUT FORMAT = each line has switchIndex #Servers sw-link1 sw-link2 ...
	 * If (heterCap) the FORMAT = switchIndex #Servers sw-link1 cap1 sw-link2 cap2
	 */
	private void populateAdjacencyList(String fName, boolean heterCap){
		Random rand = new Random(); //127 is a good number
		totalSrv = 0;	// Keep count of total number of servers

		try {
			BufferedReader br = new BufferedReader(new FileReader(fName));
			String strLine = "";
			int linenum = 0;
			while ((strLine = br.readLine()) != null){
				StringTokenizer strTok = new StringTokenizer(strLine);
				int from = Integer.parseInt(strTok.nextToken());
				int numSrv = Integer.parseInt(strTok.nextToken());
				totalSrv += numSrv;
				weightEachNode[from] = numSrv;
				int cap = 1;
				while (strTok.hasMoreTokens()){
					int to = Integer.parseInt(strTok.nextToken());
					if(heterCap) cap = Integer.parseInt(strTok.nextToken());
					if(!isNeighbor(from, to)) addBidirNeighbor(from, to, cap);
					//System.out.println(linenum + " " + to);
					
		//			temp_graph[linenum][to] = 1;
		//			temp_graph[to][linenum] = 1;
					
//					if (!adjacencyList[linenum].contains((Integer)to)) addNeighbor(linenum, to);
//					if (!adjacencyList[to].contains((Integer)linenum)) addNeighbor(to, linenum);
				}
				linenum ++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// CHECK: 
	/*	
		for (int i = 0; i < noNodes; i++){
			for (int j = 0; j < noNodes; j++){
				if (temp_graph[i][j] == 1) System.out.print(j + " ");
			}
			System.out.println();
		}*/
	}

	public int svrToSwitch(int serverIndex){
		int swIndex = 0;
		int svrSoFar = 0;
		
		while (svrSoFar <= serverIndex){
			svrSoFar += weightEachNode[swIndex];
			swIndex++;
		}
		swIndex--;
		//System.out.println(serverIndex + " is at " + swIndex);
		return swIndex;
	}
}
