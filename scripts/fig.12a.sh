###  
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
###  


MYJAVAPATH="../"
NRUNS=20

# Compile
cd $MYJAVAPATH
javac -nowarn lpmaker/ProduceLP.java
cd -

rm -rf mylog runfile ../topology/*.lp ../topology/pathlengths/*.txt ../resultfiles/vl2_*.txt

echo "16 6 24" >> runfile	# Format: DI DA <A guess for number of TORs supported; the better your guess, the faster the search!>
echo "16 8 39" >> runfile
echo "16 10 51 " >> runfile
echo "16 12 60" >> runfile
echo "16 14 76" >> runfile

#echo "20 6 30" >> runfile	# These are for the other curves in 12(a). Uncomment if you want to test those too.
#echo "20 8 46" >> runfile
#echo "20 10 62" >> runfile
#echo "20 12 76" >> runfile
#echo "20 14 92" >> runfile
#echo "20 16 107" >> runfile
#echo "20 18 124" >> runfile

#echo "24 6 36" >> runfile
#echo "24 8 54" >> runfile
#echo "24 10 72" >> runfile
#echo "24 12 90" >> runfile
#echo "24 14 111" >> runfile
#echo "24 16 128" >> runfile
#echo "24 18 152" >> runfile
#echo "24 20 170" >> runfile

#echo "28 6 42" >> runfile
#echo "28 8 60" >> runfile
#echo "28 10 85" >> runfile
#echo "28 12 105" >> runfile
#echo "28 14 136" >> runfile
#echo "28 16 160" >> runfile
#echo "28 18 178" >> runfile

while read line
do
	di=`echo $line | awk '{print $1}'`
	da=`echo $line | awk '{print $2}'`
	hint=`echo $line | awk '{print $3}'`
	vl2tors=`expr $da \* $di / 4`
	
	lowerbound=`expr $hint - 10`	# Again: this is guesswork. If you don't want to guess just put in large bounds to be slow but safe.
	upperbound=`expr $hint + 12`
	
	gap=9999999
	while [ $gap -gt 0 ]; do
		gap=`expr $upperbound - $lowerbound`
		percent_gap=`expr $gap \* 100 / $lowerbound`
		gap=`expr $gap / 2`
		if [ $percent_gap -lt 2 ]; then
			break;
		fi		
		tornow=`expr $lowerbound + $gap`
		
		rm -rf mylog flowtmp ../topology/*.lp ../topology/pathlengths/*.txt
		for (( i=0 ; i < $NRUNS ; i++ ))
		do
			cd $MYJAVAPATH
			java lpmaker/ProduceLP 1 14 garbage 0 $di $da $tornow 0 0 0 0 0 0 0 0 0 0 0 0 1 0
			mv my.0.lp topology/my.lp
			mv pl.0 topology/pathlengths/
			cd -

			flowVal=`./lpRun.sh ../topology/my.lp`
			rm -rf ../flowIDmap* ../linkCaps*
			flowValInt=`echo $flowVal | awk '{print int($1*10000)}'`
			echo $flowValInt >> flowtmp

			if [ $flowValInt -lt 500 ]; then # This takes care of the "Out of Memory" error which might give a 0 flowVal.
				continue;
			fi

			if [ $flowValInt -lt 9900 ]; then
				upperbound=$tornow
				break;
			fi
		done

		if [ "$upperbound" -gt $tornow ]; then
			lowerbound=$tornow
			avgflow=`cat flowtmp | awk '{if(NF>0){sum+=$1; cnt++;}} END{printf("%d", sum/cnt)}'`
		fi
	done
	echo $di $da $lowerbound $vl2tors $avgflow >> ../resultfiles/vl2_${di}.txt
done < runfile

rm -rf runfile

# PLOT!
cd ../gnuplotscripts
gnuplot 12a.plt
cd -
