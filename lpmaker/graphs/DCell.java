
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import lpmaker.utils.*;


import java.util.*;
import java.io.*;


public class DCell extends Graph{

	public static final boolean DCELL_DEBUG = false;
	
	protected final int K;
	protected final int N;
	protected final int noHosts;
	protected final int noSwitches;

	public static DCell createDCellByHostsFixedK(int noHosts, int k){
		//iterative :)
		//TODO: maybe improve this in the future
		for(int n = 2; n < 40; n++){
			int tentative_hosts = DCell.computeNoHosts(k,n);
			if(tentative_hosts > noHosts){
				//pick closest
				int tentative_hosts_prev = DCell.computeNoHosts(k,n-1);
				if(Math.abs(noHosts-tentative_hosts) > Math.abs(noHosts-tentative_hosts_prev)){
					System.out.println("DCell: createDCellByHostsFixedK: approximating "+noHosts+" for K "+k+" with "+tentative_hosts_prev+" with n "+(n-1));
					return new DCell(k,n-1);
				}else{
					System.out.println("DCell: createDCellByHostsFixedK: approximating "+noHosts+" for K "+k+" with "+tentative_hosts+" with n "+(n));
					return new DCell(k,n);
				}
			}
		}
		System.out.println("DCell: createDCellByHostsFixedK: ERROR: unable to create network for "+noHosts+" with K "+k);
		System.exit(0);
		return null;
	}

	public DCell(int K_, int N_){
		super(computeNoSwitches(K_,N_) + computeNoHosts(K_,N_));
		noHosts = computeNoHosts(K_,N_);
		noSwitches = computeNoSwitches(K_,N_);
		this.K = K_;
		this.N = N_;

		System.out.println("DCell: K is "+K+" N is "+N+" no nodes is "+noNodes+" no switches "+noSwitches+" no hosts "+noHosts);

		setSwitches();
		populateAdjacencyList();
		verify();
	}

	public static int getGL(int K_, int N_){
		return computeNoHosts(K_-1,N_)+1;
	}

	public static int computeNoHosts(int K_,int N_){
		int no_h = N_;
		
		for(int i = 1; i <= K_; i++){
			no_h = (no_h+1)*no_h;
		}
		
		return no_h;
	}

	
	public static int computeNoSwitches(int K_,int N_){
		int no_s = computeNoHosts(K_,N_) / N_;
		return no_s;
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
	
	
	public int convertDCellAddressToNo(Vector<Integer> address){
		int uid = address.get(address.size()-1).intValue();
		int tj = N;
		for(int j = address.size()-2; j >= 0; j--){
			uid += address.get(j).intValue()*tj;
			tj *= (tj+1);
		}
		
		return uid;
	}



	public int convertDCell1AddressToSwitchNo(Vector<Integer> address){
		int uid = address.get(address.size()-1).intValue();
		int gl = 1;
		
		for(int j = address.size()-2; j >= 0; j--){
			int new_gl = getGL(address.size()-j-1,N);
			gl = gl * new_gl;
			uid += address.get(j).intValue()*gl;
		}
		
		return uid;
	}

	public Vector<Integer> convertUidToDCellAddress(int uid, int l){
		Vector<Integer> address = new Vector(l+1);
		for(int j = l-1; j >= 0; j--){
			int tj_1 = computeNoHosts(j,N);
			address.add(new Integer(uid/tj_1));
			uid = uid % tj_1;
		}
		
		address.add(new Integer(uid));
		
		return address;
	}
	
	private void buildDCells(Vector<Integer> prefix, int n, int l){
		if(l == 0){
			int switch_id = convertDCell1AddressToSwitchNo(prefix);
			if(DCELL_DEBUG){
				System.out.print("switch id "+switch_id+" ");
				System.out.print("Prefix: ");
					for(int p = 0; p < prefix.size(); p++)
						System.out.print(prefix.get(p).intValue()+",");
				System.out.println("");
			}
			for(int i = 0; i < n; i++){
				Vector<Integer> addr1 = (Vector<Integer>)prefix.clone();
				addr1.add(new Integer(i));
				int graph_id1 = convertDCellAddressToNo(addr1)+noSwitches;
				//switch i
				addBidirNeighbor(graph_id1, switch_id);
			}
			return;
		}
		
		for(int i = 0; i < getGL(l,n); i++){
			Vector pi = (Vector)prefix.clone();
			pi.add(new Integer(i));
			buildDCells(pi,n,l-1);
		}
		
		for(int i = 0; i < computeNoHosts(l-1,n); i++){
			for(int j = i+1; j < getGL(l,n); j++){
				int uid_1 = j-1;
				int uid_2 = i;
				Vector<Integer> id1 = convertUidToDCellAddress(uid_1, l-1);
				Vector<Integer> id2 = convertUidToDCellAddress(uid_2, l-1);
				
				Vector<Integer> addr1 = (Vector<Integer>)prefix.clone();
				addr1.add(new Integer(i));
				addr1.addAll(id1);
				
				Vector<Integer> addr2 = (Vector<Integer>)prefix.clone();
				addr2.add(new Integer(j));
				addr2.addAll(id2);
				
				int graph_id1 = convertDCellAddressToNo(addr1)+noSwitches;
				int graph_id2 = convertDCellAddressToNo(addr2)+noSwitches;

				//debug
				if(DCELL_DEBUG){
					System.out.println("Linking the following for l "+l+" n "+n);
					System.out.print("Prefix: ");
					for(int p = 0; p < prefix.size(); p++)
						System.out.print(prefix.get(p).intValue());
					System.out.println("\ni "+i);
					System.out.println("uid_1 "+uid_1);
					System.out.print("Leading to addr1:");
					for(int p = 0; p < addr1.size(); p++)
						System.out.print(""+(addr1.get(p).intValue()));
					System.out.println("\nj-1 "+(j-1));
					System.out.println("uid_2 "+uid_2);
					System.out.print("Leading to addr2:");
					for(int p = 0; p < addr2.size(); p++)
						System.out.print(""+(addr2.get(p).intValue()));
					System.out.println("");
					System.out.println("this leads to link "+graph_id1+" <-> "+graph_id2);
				}

				addBidirNeighbor(graph_id1, graph_id2);	
			}
		}
	}
	
	private void populateAdjacencyList(){
		buildDCells(new Vector(), N, K);
	}

	private void verify(){
		for(int i = 0; i < noSwitches; i++){
			if(adjacencyList[i].size() != N){
				System.out.println("DCell not created properly, switch "+i+" connected to "+adjacencyList[i].size()+" nodes, expected "+N);
				try{
					writeDotFile(new File("../dot_files/graph.dot"));
				}
				catch(Exception e){
					System.out.println("ERROR: unable to write dot file"+e);
					System.exit(0);
				}
				System.exit(0);
			}
		}
		for(int i = noSwitches; i < noHosts+noSwitches; i++){
			if(adjacencyList[i].size() != K+1){
				System.out.println("DCell not created properly, host "+i+" connected to "+adjacencyList[i].size()+" nodes, expected "+(K+1));
				try{
					writeDotFile(new File("../dot_files/graph.dot"));
				}
				catch(Exception e){
					System.out.println("ERROR: unable to write dot file"+e);
					System.exit(0);
				}
				System.exit(0);
			}
		}		
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
				Vector<Integer> sw = convertUidToDCellAddress(i,K-1);
	      		output.write(i+" [shape=box, label=\"sw "+i+" <"+sw.toString()+">\"];\n");
	      	}
	      	else{
	      		Vector<Integer> host = convertUidToDCellAddress(i-noSwitches,K);
	      		output.write(i+" [shape=circle, label=\"h "+i+" "+host.toString()+"\"];\n");
	      	}
	      }
	      output.write("}\n");
	    }
	    finally {
	      output.close();
	    }
  	}

}
