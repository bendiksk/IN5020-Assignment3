set terminal png enhanced
set output 'plot4.png'

set title "In-degree distribution"
set xrange [0:100]
set xlabel "in-degree"
set ylabel "number of nodes"
set key right top
plot "inDegreeData-random-30.txt" title 'Random Graph c = 30' with histeps, \
	 "inDegreeData-shuffle-star-30cache.txt" title 'Star Graph c = 30' with histeps, \
	 "inDegreeData-random-50.txt" title 'Random Graph c = 50' with histeps, \
	 "inDegreeData-shuffle-star-50cache.txt" title 'Star Graph c = 50' with histeps
	