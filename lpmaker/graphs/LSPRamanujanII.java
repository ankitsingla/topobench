package lpmaker.graphs;

import java.util.*;
import java.io.*;


public class LSPRamanujanII extends RegularGraph{
	// q > p primes; 
	// (k^2 - p) is divisible by q for some Integer k; 
	// p, q are congruent 1 modulo 4.

	private int p;
	private int q;	
	private int i;
	private int svr_per_sw;
	
	public LSPRamanujanII(int p, int q, int svr_per_sw){
		super(q+1,p+1);
		this.q=q;
		this.p=p;
		this.i=0;
		this.svr_per_sw = svr_per_sw;
	
		while((i*i + 1)%q != 0)
			i++;

		populateAdjacencyList();
	}
	
	private boolean isPrime(int n) {
    		for(int i=2;i<n;i++) {
       			 if(n%i==0)
            			return false;
    		}
    		return true;
	}
	
	private static class Matrix2x2{
		public int[][] m = new int[2][2];
		
		public void modulo(int q){
			m[0][0] = m[0][0] % q;
			while(m[0][0]<0)
				m[0][0]=m[0][0] + q;

			m[1][0] = m[1][0] % q;
			while(m[1][0]<0)
				m[1][0]=m[1][0] + q;

			m[0][1] = m[0][1] % q;
			while(m[0][1]<0)
				m[0][1]=m[0][1] + q;
	
			m[1][1] = m[1][1] % q;
			while(m[1][1]<0)
				m[1][1]=m[1][1] + q;
		}
		public int det(){
			return m[0][0]*m[1][1] - m[1][0]*m[0][1];
		}
		public static Matrix2x2 multiply(Matrix2x2 m1, Matrix2x2 m2){
			Matrix2x2 res = new Matrix2x2();
			res.m[0][0] = m1.m[0][0]*m2.m[0][0] + m1.m[0][1]*m2.m[1][0];
			res.m[0][1] = m1.m[0][0]*m2.m[0][1] + m1.m[0][1]*m2.m[1][1];
			res.m[1][0] = m1.m[1][0]*m2.m[0][0] + m1.m[1][1]*m2.m[1][0];
			res.m[1][1] = m1.m[1][0]*m2.m[0][1] + m1.m[1][1]*m2.m[1][1];
			return res;
		}
		public  Matrix2x2 mult(int l,int q){
                        Matrix2x2 res = new Matrix2x2();
                        res.m[0][0] = (l*m[0][0])%q;
                        res.m[0][1] = (l*m[0][1])%q;
                        res.m[1][0] = (l*m[1][0])%q;
                        res.m[1][1] = (l*m[1][1])%q;
                        return res;

                }
	}

	private Matrix2x2 createAlpha(int a0, int a1, int a2, int a3,int i, int q, int p){
		Matrix2x2 res = new Matrix2x2();
		int n=0;
		res.m[0][0] = a0 + i*a1;
		res.m[0][1] = a2 + i*a3;
		res.m[1][0] = -a2 + i*a3;
		res.m[1][1] = a0 - i*a1;
		res.modulo(q);
		return res;
		//return res.mult(q-1,q);	//	-- ANKIT CHANGED TO PREVIOUS LINE; NOT SURE WHY THIS WAS THERE!!	
	}

	
	private Vector getAlphas(){
		Vector res = new Vector();
		int solutionCount = 0;
		for (int a0 = 1; a0 < p; a0 = a0 + 2){ // a0 > 0 and odd
			for(int a1 = -p + 1; a1 < p; a1 = a1 + 2)
				for(int a2 = -p + 1; a2 < p; a2 = a2 + 2)
					for(int a3 = -p + 1; a3 < p; a3 = a3 + 2){
			//			if(solutionCount < p + 1){
							if(a0*a0 + a1*a1 + a2*a2 + a3*a3 == p){
								solutionCount ++;
								System.out.println("SOLN: (a0, a1, a2, a3) = (" + a0 + ", " + a1 + ", " + a2 + ", " + a3 + ")");
								res.add(createAlpha(a0, a1, a2, a3, this.i, this.q, this.p));
							}
			//			}
			//				else
			//					return res;
						}
					}
		if(res.size()!=p+1){
			System.out.println("Error: less then p+1 solutions"+res.size());
			System.exit (0);
		}
		return res;
	}
	
	private Vector getPLElements(){
		Vector res = new Vector();
		for (int j=0;j<q+1;j++)
			res.add(j);
		return res;
	}
	
	private void populateAdjacencyList(){
		//set weights	-- ASSUME UNIFORM SERVER DISTRIBUTION; EACH SW HAS svr_per_sw servers.
		setUpFixWeight(0);
		for(int t = 0; t < noNodes; t++){
			int curr_weight = svr_per_sw;
			weightEachNode[t] = curr_weight;
			totalWeight += curr_weight;
		}
		
		Vector nodes = getPLElements();
		Vector alphas = getAlphas();
		for(int a = 0; a < alphas.size(); a++){
			Matrix2x2 alpha =(Matrix2x2)alphas.elementAt(a);
			System.out.println("alpha.m[0][0]="+alpha.m[0][0]+" alpha.m[0][1]="+alpha.m[0][1]+" alpha.m[1][0]="+alpha.m[1][0]+" alpha.m[1][1]="+alpha.m[0][0]);
		}
		int no_links = 0;

		/* ANKIT: My interpretation of "linear fractional way" is as follows:
		   For each of the (p + 1) generators (i.e. matrices here), call the elements a, b, c, d
		   Then nbrs(INF-node) = {a/c} for each matrix. In the group sense a/c = a * c-inverse
		   For other PL-elements x, nbrs(x) = (ax + b) / (cx + d) -- again '/' in group sense

		   Also note that we're using node-id 'q' for the INF element.
		*/

		// ANKIT: This loop deals with the INF element of the Projective Line
		for(int a = 0; a < alphas.size(); a++){
			Matrix2x2 alpha =(Matrix2x2)alphas.elementAt(a);
			int t=q;
			if (alpha.m[1][0]!=0)		
				t=(alpha.m[0][0]* inverse(alpha.m[1][0]))%q;
			else {
				//System.out.println("THIS HAPPENS A!");		// This and the other "HAPPENS" sum to the lost degrees.
				continue;
			}
			System.out.println("a=" + alpha.m[0][0] + "; c=" + alpha.m[1][0] + "; c-inv=" + inverse(alpha.m[1][0]) + "; ac-inv=" + ((t+q)%q));

			//while (t<0)
			//	t+=q; -- ANKIT: Unnecessary; inverse is positive/zero, t is positive or zero.
			
			if ((t!=q)&&(isAdj(t,q)==false))
				addBidirNeighbor(q, t);	
		}

		// These do the rest of the connections
		for(int j = 0; j < noNodes-1; j++){
			for(int a = 0; a < alphas.size(); a++){
				Matrix2x2 alpha =(Matrix2x2)alphas.elementAt(a);
				int n,t1,t2;
				t1=(alpha.m[0][0]*j + alpha.m[0][1]) % q; // ANKIT: Not sure it matters: I added the %q here and in line below
				t2=(alpha.m[1][0]*j + alpha.m[1][1]) % q;
				if (t2==0){ // The group inverse of 0 is INF, i.e. node 'q'
					//if((t1!=0)&&(adjacencyList[j].size()<p+1)&& (isAdj(j,q)==false)&&(adjacencyList[q].size()<p+1))
					// ANKIT COMMENTED OUT PREV LINE: PARALLEL ALLOWED + j to q edges are fine!
						if (isAdj(j,q)==false) addBidirNeighbor(j, q); // ANKIT: alright disable parallel again
					continue;
				}
				n = (t1* inverse(t2))%q;
				//while(n<0)
				//	n+=q;		// ANKIT: unnecessary
			
				//if((n!=j)&&(adjacencyList[j].size()<p+1)&&(isAdj(j,n)==false)&&(adjacencyList[n].size()<p+1))
				if((n!=j) && (isAdj(j,n)==false))
					addBidirNeighbor(j, n);
				//else
					//System.out.println("THIS HAPPENS B!");
			}
		}
		
		//print the degree for debug
		int max_degree = 0, min_degree = 999999, sum_degree = 0;;
		for(int i = 0; i < noNodes; i++){
			int degree = adjacencyList[i].size();
			int degreeMult = 0;
			for (int l = 0; l < adjacencyList[i].size(); l++)
				degreeMult += adjacencyList[i].elementAt(l).linkcapacity;

			System.out.println("node "+i+" degree "+degree + " degreeMult " + degreeMult);
			if(degree > max_degree)
				max_degree = degree;
			if(degree < min_degree)
				min_degree = degree;
			sum_degree += degree;
		}
		
		System.out.println("avg degree "+((float)sum_degree/(float)noNodes)+" max degree "+max_degree+" min degree "+min_degree);
		
		for(int a = 0; a < alphas.size(); a++){
			Matrix2x2 m = (Matrix2x2)alphas.elementAt(a);
			System.out.println("alpha["+a+"]="+m+ " det "+m.det()+" det%q "+(m.det()%q));
		}

		// Fix the selp-loops etc.
		if(true)
		{
			Vector still_to_link = new Vector();            // Nodes with some degree still available
			Vector degrees = new Vector();                  // Degree currently used up

			for(int i = 0; i < noNodes; i++){
				int degree_used = adjacencyList[i].size();
				if (degree_used < this.degree) {
					still_to_link.add(new Integer(i));                      // Initialize with nodes with available degree
					degrees.add(new Integer(this.degree - degree_used));      // Initialize with remaining degree
				}
			}

			int stop_sign=0;
			while(!still_to_link.isEmpty() && stop_sign==0){
				if(still_to_link.size() == 1){                          // Ignores this case of 1 node left out
					System.out.println("WARNING: Remaining just one node to link with degree "+degrees.elementAt(0)+" out of "+degree);
					stop_sign=1;
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
					if(k==0)        // Edge doesn't already exist. Good, add it!
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
					if(iteration >= MAX_ITERATION){ // Give up if can't find a pair to link
						System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
						stop_sign=1;
						//return;
					}
					degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue() - 1));
					degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue() - 1));
					boolean deleted_p1 = false;
					if(((Integer)degrees.elementAt(p1)).intValue() == 0){
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

					if(((Integer)degrees.elementAt(p2_updated)).intValue() == 0){
						// Degree exhausted => No longer available to connect
						still_to_link.remove(p2_updated);
						degrees.remove(p2_updated);
					}
				}
			}
		}
		
		//print the degree for debug -- AGAIN (i.e. after random fixing)
		max_degree = 0; min_degree = 999999; sum_degree = 0;
		for(int i = 0; i < noNodes; i++){
			int degree = adjacencyList[i].size();
			int degreeMult = 0;
			for (int l = 0; l < adjacencyList[i].size(); l++)
				degreeMult += adjacencyList[i].elementAt(l).linkcapacity;

			System.out.println("node "+i+" degree "+degree + " degreeMult " + degreeMult);
			if(degree > max_degree)
				max_degree = degree;
			if(degree < min_degree)
				min_degree = degree;
			sum_degree += degree;
		}
		
		System.out.println("avg degree "+((float)sum_degree/(float)noNodes)+" max degree "+max_degree+" min degree "+min_degree);
	}

	public boolean isAdj(int i,int j){
		for(int l=0; l<adjacencyList[i].size(); l++)
			if(adjacencyList[i].elementAt(l).intValue()==j)
				return true;
		return false;	
	}
	public int inverse(int i){
		if (i==this.q)
			return 0;
		if(i==0)
			return q;
		for (int j=1;j<this.q;j++){
			if ((j*i)%q==1)
				return j;
		}
		return -1;			
	}
	
	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		return i/svr_per_sw;
		
	}
}
