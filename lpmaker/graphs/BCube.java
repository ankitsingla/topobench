
package lpmaker.graphs;

import lpmaker.utils.*;


import java.util.*;
import java.io.*;


public class BCube extends Graph{
	
	protected final int K;
	protected final int N;
	protected final int noHosts;
	protected final int noSwitches;


	public static BCube createBCubeByHosts(int noHosts){
		int k_ = 0;
		int n_ = noHosts;
		while(n_ > 10 && k_ < 3){
			k_ += 1;
			n_ = (int)Math.pow(noHosts, 1.0/(double)(k_+1));
		}
		
		System.out.println("BCube: desired "+noHosts+" approximating to "+(int)Math.pow(n_,k_+1)+" using K "+k_+" n "+n_);
		return new BCube(k_,n_);
	}


	public static BCube createBCubeByHostsFixedK(int noHosts, int K_){
		int n_ = (int)Math.pow(noHosts, 1.0/(double)(K_+1));
		
		System.out.println("BCube(fixed K): desired "+noHosts+" approximating to "+(int)Math.pow(n_,K_+1)+" using K "+K_+" n "+n_);
		return new BCube(K_,n_);
	}

	public BCube(int K_, int N_){
		super((int)Math.pow(N_,K_+1) + computeNoSwitches(K_,N_));
		noHosts = (int)Math.pow(N_,K_+1);
		noSwitches = computeNoSwitches(K_,N_);
		this.K = K_;
		this.N = N_;

		System.out.println("BCube: K is "+K+" N is "+N+" no nodes is "+noNodes+" no switches "+noSwitches+" no hosts "+noHosts);

		setSwitches();
		populateAdjacencyList();
	}
	
	public static int computeNoSwitches(int K_,int N_){
		
		//simplification :) can be replaced by 
		//(K+1) * N^K
		int noSwitches = 1;
		for(int i = 1; i < K_+1; i++){
			noSwitches = noSwitches*N_ + (int)Math.pow(N_,i);
		}
		return noSwitches;
	}


	public int getK(){
		return K;
	}
	
	public int getN(){
		return N;
	}

	public int getNoSwitches(){
		return noSwitches;
	}

	public int getNoHosts(){
		return noHosts;
	}

	private void setSwitches(){
		int i = 0; 
		for(i = 0; i < noSwitches; i++){
			isSwitch[i] = true;
			weightEachNode[i] = 0;
		}
		for(; i < noNodes; i++){
			isSwitch[i] = false;
			weightEachNode[i] = 1;
		}
	}
	
	/*
	public int mapBCubeHostIDtoNodeID(int[] a){
		
	}
	*/

	public int mapBCubeSwitchIDtoNodeID(int[] s){
		int val = 0;
		int mul = (int)Math.pow(N,K); 
		//System.out.print("s:");
		for(int i = 0; i < K+1; i++){
			val += s[i] * mul;
			mul /= N;
			//System.out.print(s[i] + " ");
		}
		//System.out.println("switch id is "+val);
		return val;
	}
	
	public int[] convertToBaseK(int serverNum){
		int[] a = new int[K+1];
		int divisor = (int)Math.pow(N,K);
		int reminder = serverNum;
		for(int i = 0; i < K+1; i++){
			a[i] = reminder/divisor;
			reminder = reminder - (divisor*a[i]);
			divisor = divisor / N;
		}
		return a;
	}

	public int convertBaseKtoInt(int[] a){
		int multiplier = (int)Math.pow(N,K);
		int id = 0;
		for(int i = 0; i < K+1; i++){
			id += a[i]*multiplier;
			multiplier = multiplier / N;
		}
		return id;
	}
		
	private void populateAdjacencyList(){
		//switches are first from 0-noSwitches then come hosts
		
		for(int i = 0; i < noHosts; i++){
			int[] a = convertToBaseK(i);
			//connect to k switches
			for(int l =0; l < K+1; l++){
				int[] s = a.clone();
				int temp = s[0];
				for(int t = K-l; t > 0; t--){
					s[t] = s[t-1];
				}
				s[0] = l;
				
				int switchID = mapBCubeSwitchIDtoNodeID(s);
				
				addBidirNeighbor(noSwitches+i,switchID);
			}
		}
		
		//sanity check
		for(int i = 0; i < noSwitches; i++){
			if(adjacencyList[i].size() != N){
				System.out.println("BCube: seems not created correctly, switch is linked to different no of hosts "+adjacencyList[i].size());
				System.exit(0);
			}
		}

	}

	
	public String toString(){
		String s = "";
		for(int i = 0; i < noSwitches; i++){
			int[] sw = convertToBaseK(i);
			for(int j = 0; j < adjacencyList[i].size(); j++){
				int[] host = convertToBaseK(adjacencyList[i].elementAt(j).intValue()-noSwitches);
				s += "\"sw"+Printing.toStringArray(sw)+"\""+"->"+"\"h"+Printing.toStringArray(host)+"\""+"\n";
			}
		}
		s += "\n";
		return s;
	}


	public void writeDotFile(File aFile) throws FileNotFoundException, IOException{
	    Writer output = new BufferedWriter(new FileWriter(aFile));
	    try {
	      output.write("digraph prof {\nsize=\"50,50\";\n ratio = fill;\nnode [style=filled];\n\n");
	      output.write(this.toString());
	      //weight labels
	      output.write("\n");
	      for(int i = 0; i < noNodes; i++){
	      	if(isSwitch[i]){
	      		int[] sw = convertToBaseK(i);
	      		output.write("\"sw"+Printing.toStringArray(sw)+"\" [shape=box , label=\"<"+Printing.toStringArray(sw)+">("+i+")\"];\n");
	      	}
	      	else{
	      		int[] host = convertToBaseK(i-noSwitches);
	      		output.write("\"h"+Printing.toStringArray(host)+"\" [shape=circle , label=\""+Printing.toStringArray(host)+"("+i+")\"];\n");
	      	}
	      }
	      output.write("}\n");
	    }
	    finally {
	      output.close();
	    }
  	}
	
}
