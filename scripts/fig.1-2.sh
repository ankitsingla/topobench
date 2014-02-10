#!/bin/bash

# First get the set of configurations to test
# The output of configuration format: <Number of switches> <network degree> <svrs per switch> <ASPL lower bound> <Throughput upper bound>
cd ../upperBound
./pl_based_thput_bound.sh
cd -

NRUNS=3			# Reduced for sake of running time.
MYJAVAPATH="../"

# Compile
cd $MYJAVAPATH
javac -nowarn lpmaker/ProduceLP.java
cd -

# Function reads configuration list as ARG 1, outputs result in file named `ARG 2` in ../resultfiles directory
function produceBoundResult () {
	# Clean up any old result files
	rm -rf ../resultfiles/$2
	
	while read line	# For each configuration generate and run the linear program; record results!
	do
		sw=`echo $line | awk '{print $1}'`
		swminus1=`expr $sw - 1`
		deg=`echo $line | awk '{print $2}'`
		svr_per_sw=`echo $line | awk '{print $3}'`
		avg_pl_bound=`echo $line | awk '{print $4}'`
		thput_bound=`echo $line | awk '{print $5}'`
		sw_ports=`expr $deg + $svr_per_sw`

		# This is such a poor hack to save some code-writing. But such is life :P
		tmode=0
		if [ "$svr_per_sw" -eq "$swminus1" ]; then
			tmode=1
			svr_per_sw=1
			sw_ports=`expr $deg + 1`
		fi
	
		rm -rf ../topology/*.lp ../topology/pathlengths/*.txt flowtmp_bound_test pl_bound_test
	
		for (( i=0 ; i < $NRUNS ; i++ ))
		do
			cd $MYJAVAPATH
			java lpmaker/ProduceLP 1 0 garbage $tmode $sw $sw_ports $deg 0 0 0 0 0 0 0 0 0 0 0 0 1 0 # ARG[19] = 1 => LP will be created!
			mv my.0.lp topology/my.lp
			mv pl.0 topology/pathlengths/pl_${sw}_${deg}_${i}.txt
			cd -
	
			flowVal=`./lpRun.sh ../topology/my.lp`
			rm -rf ../flowIDmap* ../linkCaps*

			# Note: Only considering switch-switch path lengths here, because that's what's comparable to the bound.
			plav=`cat ../topology/pathlengths/pl_${sw}_${deg}_${i}.txt | awk 'BEGIN{sum=0; count=0}{if($1 < 1000) {sum+=$1; count++}}END{print sum/count}'`
			echo "$plav" >> pl_bound_test
			echo "$flowVal" >> flowtmp_bound_test
		done
		
		avgflow=`cat flowtmp_bound_test | awk '{if(NF > 0 && $1 > 0){sum+=$1; cnt++;}} END{print sum/cnt}'`
		avgpl=`cat pl_bound_test | awk '{if(NF>0){sum+=$1; cnt++;}} END{print sum/cnt}'`
		bound_compare=`echo $avgflow $thput_bound | awk '{print $1/$2}'`
		avg_pl_compare=`echo $avg_pl_bound $avgpl | awk '{print $2/$1}'`	
		echo "$sw $sw_ports $deg $thput_bound $avgflow $bound_compare $avg_pl_bound $avgpl $avg_pl_compare" >> ../resultfiles/$2
	
		# Clean up all data files other than final result
		rm -rf ../topology/*.lp ../topology/pathlengths/*.txt flowtmp_bound_test pl_bound_test
	done < $1
}

# Call function to produce the result files
produceBoundResult ../upperBound/inc_sw_all.txt inc_sw_allall.txt
produceBoundResult ../upperBound/inc_sw_10.txt inc_sw_10match.txt
produceBoundResult ../upperBound/inc_sw_5.txt inc_sw_5match.txt
produceBoundResult ../upperBound/inc_deg_all.txt 40sizeBound_allall.txt
produceBoundResult ../upperBound/inc_deg_10.txt 40sizeBound_10matchings.txt
produceBoundResult ../upperBound/inc_deg_5.txt 40sizeBound_5match.txt

# Plot files using the gnuplot scripts (Final results are in ../plots)
cd ../gnuplotscripts
gnuplot 1a-40sizeComparison.plt 
gnuplot 1b-40size_pl.plt
gnuplot 2a-10DegComparison.plt
gnuplot 2b-10deg_pl.plt
cd -

rm -rf ../upperBound/inc_*txt
