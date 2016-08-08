
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

        //set weights
        setUpFixWeight(0);
        for(int t = 0; t < a * g ; t++){
            //int curr_weight = c;
            int curr_weight = 1;
            weightEachNode[t] = curr_weight;
            if(t == 0){
                weightBeforeEachNode[t] = 0;
            }
            weightBeforeEachNode[t+1] = curr_weight + weightBeforeEachNode[t];
            totalWeight += curr_weight;

        }

    }

	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		return Math.round(i);

	}

}
