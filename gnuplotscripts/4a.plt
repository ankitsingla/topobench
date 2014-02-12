###  
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
###  

# Note you need gnuplot 4.4 for the pdfcairo terminal.
set terminal pdfcairo font "Gill Sans, 8" linewidth 4 rounded

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

#set log x
#set mxtics 10    # Makes logscale look good.

# Line styles: try to pick pleasing colors, rather
# than strictly primary colors or hard-to-see colors
# like gnuplot's default yellow.  Make the lines thick
# so they're easy to see in small plots in papers.
set style line 1 lt rgb "#A00000" lw 2 pt 1
set style line 2 lt rgb "#00A000" lw 2 pt 6
set style line 3 lt rgb "#5060D0" lw 2 pt 2
set style line 4 lt rgb "#F25900" lw 2 pt 9

set output "../plots/4a.pdf"
set xlabel "Number of Servers at Large Switches\n(Ratio to Expected Under Random Distribution)"
#set xlabel "Server-Ratio / Port-Ratio"
set ylabel "Normalized Throughput"

set key top right

set xrange [0.4:2]
#set xrange [0:5]
set yrange [0:1]

plot  "../resultfiles/20_40_30_10_440.txt" using (($3-$5)/(440*$3/($3*$1 + $4*$2))):7 title "3:1 Port-ratio" w lp ls 3, \
      "../resultfiles/20_40_30_15_560.txt" using (($3-$5)/(560*$3/($3*$1 + $4*$2))):7 title "2:1 Port-ratio" w lp ls 1, \
      "../resultfiles/20_40_30_20_540.txt" using (($3-$5)/(540*$3/($3*$1 + $4*$2))):7 title "3:2 Port-ratio" w lp ls 2

