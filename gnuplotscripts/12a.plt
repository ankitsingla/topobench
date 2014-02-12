###  
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
###  

# Note you need gnuplot 4.4 for the pdfcairo terminal.
set terminal pdfcairo font "Gill Sans, 8" linewidth 4 rounded
set size ratio 0.6

#set terminal postscript monochrome font "Helvetica, 22" linewidth 4 rounded 
#set size ratio 0.5

# Line style for axes
set style line 80 lt rgb "#808080"

# Line style for grid
set style line 81 lt 0  # dashed
set style line 81 lt rgb "#808080"  # grey

set grid back linestyle 81
set border 3 back linestyle 80 # Remove border on top and right.  These
             # borders are useless and make it harder
	                  # to see plotted lines near the border.
			      # Also, put it in grey; no need for so much emphasis on a border.
			      set xtics nomirror
			      set ytics nomirror

#set log x 2
#set mxtics 10    # Makes logscale look good.

# Line styles: try to pick pleasing colors, rather
# than strictly primary colors or hard-to-see colors
# like gnuplot's default yellow.  Make the lines thick
# so they're easy to see in small plots in papers.
#set style line 1 lt rgb "#A00000" lw 2 pt 1
#set style line 2 lt rgb "#00A000" lw 2 pt 6
#set style line 3 lt rgb "#5060D0" lw 2 pt 2
#set style line 4 lt rgb "#F25900" lw 2 pt 9

set output "../plots/12a.pdf"
set xlabel "Aggregation Switch Degree (DA)"
set ylabel "Servers at Full Throughput \n(Ratio Over VL2)"

set key bottom right

set xrange [6:20]
set yrange [1:1.45]

plot "../resultfiles/vl2_16.txt" using 2:($3/$4) title "16 Agg Switches (DI=16)" w lp lt 2 pt 5 lw 2 ps 1 lc rgb "#000000", \
     "../resultfiles/vl2_20.txt" using 2:($3/$4) title "20 Agg Switches (DI=20)" w lp lt 3 pt 3 lw 2.5 ps 1.2 lc rgb "#00A000", \
     "../resultfiles/vl2_24.txt" using 2:($3/$4) title "24 Agg Switches (DI=24)" w lp lt 1 pt 8 lw 2.5 ps 1.6 lc rgb "#5060D0", \
     "../resultfiles/vl2_28.txt" using 2:($3/$4) title "28 Agg Switches (DI=28)" w lp lt 12 pt 7 lw 2.5 ps 1.1 lc rgb "#A00000"
