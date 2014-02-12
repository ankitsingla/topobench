
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import lpmaker.ProduceLP;

public class FatTreeSigcomm extends Graph{
	
	public int K;

	public static FatTreeSigcomm createFatTreeByHosts(int hosts){
		double K_ = Math.pow(hosts*4, 1.0/3.0);
		int K_int = (int)K_;
		if(K_int % 2 != 0){
			K_int++;
		}
		System.out.println("K would be "+K_+" rounding to "+K_int+ " leading to "+(K_int*K_int*K_int/4)+" hosts");
		return new FatTreeSigcomm(K_int);
	}

	public FatTreeSigcomm(int K_){
		super(K_*K_*5/4);
		this.K = K_;
		populateAdjacencyList();
		name = "fat";
	}
	public FatTreeSigcomm(int K_, double fail_rate){
		super(K_*K_*5/4);
		this.K = K_;
		System.out.println(K_ + " ^^^^^^^^^^^^^^^^^^^^ ");
		populateAdjacencyList();
		name = "fat";
		failLinks(fail_rate);
	}

	public ArrayList TrafficGenAllAll()
	{

		// Over-ridden because I need traffic only between nodes with terminals
		// Server j on node i i.e. [(noNodeswithTerminals - 1) * i + j] th server sends to jth server on all other nodes

		int noNodeswithTerminals = K*K/2;
		int numPerms = noNodeswithTerminals - 1;

		ArrayList<Integer> ls = new ArrayList<Integer>();
		for (int i = 0; i < noNodeswithTerminals; i++) {
			int target = 0;
			for (int svr = 0; svr < numPerms; svr++) {
				if (target == i) target ++;
				ls.add(numPerms * target  + svr);
				target ++;
			}
		}

		System.out.println("NUM FLOWS = " + ls.size());

		return ls;
	}
	
	public ArrayList TrafficGenPermutations()
	{
		// Over-ridden because I need traffic only between nodes with terminals
		
		int noNodeswithTerminals = K*K/2;
		int numPerms = totalWeight / noNodeswithTerminals;
		int[][] allPerms = new int[numPerms][noNodeswithTerminals];
		Random rand = new Random(ProduceLP.universalRand.nextInt(10));

		for (int currPerm = 0; currPerm < numPerms; currPerm ++)
		{
			allPerms[currPerm] = new int[noNodeswithTerminals];
			for (int i = 0; i < noNodeswithTerminals; i++)
			{
				int temprand = rand.nextInt(i + 1);
				allPerms[currPerm][i] = allPerms[currPerm][temprand];
				allPerms[currPerm][temprand] = i;
			}

			// fix cases where a node is sending to itself
			Vector<Integer> badCases = new Vector<Integer>();
			for (int i = 0; i < noNodeswithTerminals; i++)
			{
				if (allPerms[currPerm][i] == i) badCases.add(new Integer(i));
			}
			for (Integer badOne : badCases)
			{
				int temprand= -1;
				do
				{
					temprand = rand.nextInt(noNodeswithTerminals);
				}
				while (allPerms[currPerm][temprand] == temprand);

				allPerms[currPerm][badOne.intValue()] = allPerms[currPerm][temprand];
				allPerms[currPerm][temprand] = badOne.intValue();
			}
		}

		ArrayList<Integer> ls = new ArrayList<Integer>();
		for (int i = 0; i < noNodeswithTerminals; i++)
		{
			for (int currPerm = 0; currPerm < numPerms; currPerm ++)
			{
				ls.add(numPerms * allPerms[currPerm][i] + currPerm);
			}
		}
		return ls;
	}

	// +++++++++++ THIS CONSTRUCTION ROUTINE FOR VANILLA FAT TREE +++++++++++++++++++++++++++++++
	private void populateAdjacencyList(){
		//layout
		
		//0-(K*K/2) lower layer close to hosts 
		//(K*K/2) - K*K middle layer
		//K*K - 5/4*K*K core layer
		
		//connect lower to middle
		for(int pod = 0; pod < K; pod++){
			for(int i = 0; i < K/2; i++){
				for(int l = 0; l < K/2; l++){
					addBidirNeighbor(pod*K/2+i, K*K/2+pod*K/2+l);
				}
			}
		}
		
		//connect middle to core
		for(int core_type = 0; core_type < K/2; core_type++){
			for(int incore = 0; incore < K/2; incore++){
				for(int l = 0; l < K; l++){
					addBidirNeighbor(K*K+core_type*K/2+incore, K*K/2+l*K/2+core_type);
				}
			}
		}
		
		
		
		//set weights
		setUpFixWeight(0);
		//int total = 0;
		for(int pod = 0; pod < K; pod++){
			for(int i = 0; i < K/2; i++){
				// For new comparison method, ANKIT changed this to set up arbitrary numbers of terminals!
				weightEachNode[pod*K/2+i] = K/2;
				totalWeight += K/2;
				//weightEachNode[pod*K/2+i] = 5;
				//totalWeight += 5;
			}
		}
	}

	
	public int getK(){
		return K;
	}
	
	public int getNoHosts(){
		return K*K*K/4;
	}
	
	public int getNoSwitches(){
		return K*K*5/4;
	}
	
	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		return i / weightEachNode[0];
	}
}
