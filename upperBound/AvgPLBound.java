/* *******************************************************
 * Released under the MIT License (MIT) --- see ../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

// This implements the Moore-bound based average path length lower bound.

import java.util.*;

public class AvgPLBound {
	public static void main(String[] args){
		int N = Integer.parseInt(args[0]); // numNodes
		int d = Integer.parseInt(args[1]); // network degree
		int svrs_per_sw = Integer.parseInt(args[2]); // servers per switch

		int R = N - 1;
		int k = -1;
		while (R >= 0) {
			k++;
			R = R - d * (int)Math.pow(d - 1, k);

			if (R < 0){
				R += d * (int)Math.pow(d - 1, k);
				k--;
				break;
			}
			if (R == 0) {
				break;
			}
		}
		k += 2;
		assert (R >= 0);

		int my_sum = k * R;
		for (int i = 1; i < k; i++){
			my_sum += i * d * (int)Math.pow(d - 1, i - 1);
		}

		double avg_pl_bound = my_sum / (double)(N - 1);
		avg_pl_bound = ((int)(avg_pl_bound * 1000000))/1000000.0;
		double bw_bound= d / (avg_pl_bound * svrs_per_sw);
		bw_bound = ((int)(bw_bound * 1000000))/1000000.0;
		
		System.out.println(N + " " + d + " " + svrs_per_sw + " " + avg_pl_bound + " " + bw_bound);
		return;
	}

}
