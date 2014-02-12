###  
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
###  


javac AvgPLBound.java
rm -rf inc_*.txt

# Increasing number of switches; degree fixed at 10
sw=15
while (( $sw < 200 ))
do
	demDeg=`expr $sw - 1`
	java -ea AvgPLBound $sw 10 $demDeg >> inc_sw_all.txt	# The third arg for java is the number of servers at each switch
	java -ea AvgPLBound $sw 10 5 >> inc_sw_5.txt
	java -ea AvgPLBound $sw 10 10 >> inc_sw_10.txt
	sw=`expr $sw + 10`
done

# Increasing degree; number of switches fixed at 40
sw=40
deg=3
while (( $deg < 33 ))
do
	demDeg=`expr $sw - 1`
	java -ea AvgPLBound $sw $deg $demDeg >> inc_deg_all.txt
	java -ea AvgPLBound $sw $deg 5 >> inc_deg_5.txt
	java -ea AvgPLBound $sw $deg 10 >> inc_deg_10.txt
	deg=`expr $deg + 2`
done

rm -rf AvgPLBound.class
