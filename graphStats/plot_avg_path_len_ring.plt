# You can uncomment the following lines to produce a png figure
set terminal png enhanced
set output 'plot5.png'

set title "Average Path Length"
set xlabel "cycles"
set ylabel "average path length (log)"
set key right top
set logscale y 
plot "shortestPathData-random-30.txt" title 'Random graph c = 30' with lines, \
	 "shortestPathData-shuffle-ring-30cache.txt" title 'Shuffle Ring c = 30' with lines, \
	 "shortestPathData-random-50.txt" title 'Random Graph c = 50' with lines, \
	 "shortestPathData-shuffle-ring-50cache.txt" title 'Shuffle Ring c = 50' with lines