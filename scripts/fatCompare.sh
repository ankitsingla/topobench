#!/bin/bash

NRUNS=3         # Reduced for sake of running time.
MYJAVAPATH="../"

# Compile
cd $MYJAVAPATH
javac -nowarn lpmaker/ProduceLP.java
cd -

rm -rf ../resultfiles/result_fat.txt

#for port in 6 8 10 12 14 16	# Can run for larger sizes; for sake of demo run time, we'll run for only three sizes below
for port in 6 8 10
do
	switches=`expr $port \* $port \* 5 / 4`
	numsvr=`expr $port \* $port \* $port / 4`

	rm -rf flowtmp_fat pl_fat

	for (( i=0 ; i < $NRUNS ; i++ ))
	do
		# We checked -- the fat-tree does give throughput = 1 each time, as expected. So need to run the LP for it!
		cd $MYJAVAPATH
		java lpmaker/ProduceLP 1 0 garbage 0 $switches $port 0 0 $numsvr 0.0 0 0 0 0 0 0 0 0 0 1 0
		mv my.0.lp topology/my.lp
		mv pl.0 topology/pathlengths/
		cd -
	
		flowVal=`./lpRun.sh ../topology/my.lp`
		rm -rf ../flowIDmap* ../linkCaps* flowIDmap* linkCaps*
		echo "$flowVal" >> flowtmp_fat
	done
	
	avgflow=`cat flowtmp_fat | awk '{if(NF>0){sum+=$1; cnt++;}} END{print sum/cnt}'`
	echo "$switches $numsvr $port 1 $avgflow" >> ../resultfiles/result_fat.txt
done

# PLOT!
cd ../gnuplotscripts
gnuplot fatCompare.plt
cd -
