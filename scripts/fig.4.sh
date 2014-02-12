###  
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
###  


# Experiment: Vary distribution of servers across switches of two different port counts

# First list all the configurations that will be tested in a `runfile'
# runfile format: num_large_sw num_small_sw large_numports small_numports dh=high_net_degree dl=low_net_degree

# 4(a)
echo "20 40 30 10 12 8" > runfile	# 440 servers
echo "20 40 30 10 14 7" >> runfile
echo "20 40 30 10 16 6" >> runfile
echo "20 40 30 10 18 5" >> runfile
echo "20 40 30 10 20 4" >> runfile
echo "20 40 30 10 22 3" >> runfile

echo "20 40 30 15 8 12" >> runfile	# So this means 20*(30-8) + 40*(15-12) = 560 servers
echo "20 40 30 15 10 11" >> runfile
echo "20 40 30 15 12 10" >> runfile
echo "20 40 30 15 14 9" >> runfile
echo "20 40 30 15 16 8" >> runfile
echo "20 40 30 15 18 7" >> runfile

echo "20 40 30 20 9 17" >> runfile 	# 540 servers
echo "20 40 30 20 11 16" >> runfile
echo "20 40 30 20 13 15" >> runfile
echo "20 40 30 20 15 14" >> runfile
echo "20 40 30 20 17 13" >> runfile
echo "20 40 30 20 19 12" >> runfile
echo "20 40 30 20 21 11" >> runfile
echo "20 40 30 20 23 10" >> runfile
echo "20 40 30 20 25 9" >> runfile

# 4(b)
echo "20 20 30 20 9 17" >> runfile 	# 480 servers
echo "20 20 30 20 10 16" >> runfile
echo "20 20 30 20 11 15" >> runfile
echo "20 20 30 20 12 14" >> runfile
echo "20 20 30 20 13 13" >> runfile
echo "20 20 30 20 14 12" >> runfile
echo "20 20 30 20 15 11" >> runfile
echo "20 20 30 20 16 10" >> runfile
echo "20 20 30 20 17 9" >> runfile
echo "20 20 30 20 18 8" >> runfile
echo "20 20 30 20 19 7" >> runfile
echo "20 20 30 20 20 6" >> runfile
echo "20 20 30 20 21 5" >> runfile
echo "20 20 30 20 22 4" >> runfile

echo "20 30 30 20 6 20" >> runfile 	# 480 servers		-- also used for 4(c)
echo "20 30 30 20 9 18" >> runfile
echo "20 30 30 20 12 16" >> runfile
echo "20 30 30 20 15 14" >> runfile
echo "20 30 30 20 18 12" >> runfile
echo "20 30 30 20 21 10" >> runfile
echo "20 30 30 20 24 8" >> runfile

echo "20 40 30 20 9 17" >> runfile     # 540 servers
echo "20 40 30 20 11 16" >> runfile
echo "20 40 30 20 13 15" >> runfile
echo "20 40 30 20 15 14" >> runfile
echo "20 40 30 20 17 13" >> runfile
echo "20 40 30 20 19 12" >> runfile
echo "20 40 30 20 21 11" >> runfile
echo "20 40 30 20 23 10" >> runfile
echo "20 40 30 20 25 9" >> runfile

# 4(c)
echo "20 30 30 20 6 19" >> runfile      # 510 servers
echo "20 30 30 20 9 17" >> runfile
echo "20 30 30 20 12 15" >> runfile
echo "20 30 30 20 15 13" >> runfile
echo "20 30 30 20 18 11" >> runfile
echo "20 30 30 20 21 9" >> runfile
echo "20 30 30 20 24 7" >> runfile

echo "20 30 30 20 6 18" >> runfile      # 540 servers
echo "20 30 30 20 9 16" >> runfile
echo "20 30 30 20 12 14" >> runfile
echo "20 30 30 20 15 12" >> runfile
echo "20 30 30 20 18 10" >> runfile
echo "20 30 30 20 21 8" >> runfile
echo "20 30 30 20 24 6" >> runfile

NRUNS=3 	# Reduced for sake of running time.
MYJAVAPATH="../"

# Compile
cd $MYJAVAPATH
javac -nowarn lpmaker/ProduceLP.java
cd -

rm -rf ../resultfiles/*_*_*_*_*.txt	# clearing old result files!
while read line
do
	nh=`echo $line | awk '{print $1}'`
	nl=`echo $line | awk '{print $2}'`
	h=`echo $line | awk '{print $3}'`
	l=`echo $line | awk '{print $4}'`
	dh=`echo $line | awk '{print $5}'`
	dl=`echo $line | awk '{print $6}'`

	numsvrs=`echo $line | awk '{val=($1*($3-$5) + $2*($4-$6)); print val}'`

	rm -rf ../topology/*.lp ../topology/pathlengths/*.txt flowtmp_bound_test pl_bound_test
	for (( i=0 ; i < $NRUNS ; i++ ))
	do
	
		cd $MYJAVAPATH
		java lpmaker/ProduceLP 1 10 garbage 0 0 0 0 0 0 0.0 $nh $nl $h $l $dh $dl 0 0 0 1 0
		mv my.0.lp topology/my.lp
		mv pl.0 topology/pathlengths/
		cd -
	
		flowVal=`./lpRun.sh ../topology/my.lp`
		rm -rf ../flowIDmap* ../linkCaps*
		echo "$flowVal" >> flowtmp_bound_test
	done
	avgflow=`cat flowtmp_bound_test | awk '{if(NF > 0 && $1 > 0){sum+=$1; cnt++;}} END{print sum/cnt}'`
	echo $nh $nl $h $l $dh $dl $avgflow >> ../resultfiles/${nh}_${nl}_${h}_${l}_${numsvrs}.txt
	
	# Clean up all data files other than final result
	rm -rf ../topology/*.lp ../topology/pathlengths/*.txt flowtmp_bound_test pl_bound_test
done < runfile

rm -rf runfile

# Plot
cd ../gnuplotscripts
gnuplot 4a.plt
gnuplot 4b.plt
gnuplot 4c.plt
cd -
