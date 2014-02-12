###  
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
###  

# USAGE: Input is a file containing a linear program for throughput in CPLEX format. Output is the throughput value obtained.

infile=$1

# Run LP. Replace below with whatever tool you want to use to run the LP.
gurobi_cl Method=2 Crossover=0 $infile > templog
objective=`cat templog | grep "Optimal objective " | awk '{print $3}' | awk -F "e" '{print $1*10^$2}'`

# Echo only the throughput. If unsuccessful, echo -1
if [[ -z "$objective" ]]; then
	echo "-1"
else
	echo "$objective"
fi

rm -rf templog gurobi.log
