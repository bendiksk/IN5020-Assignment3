set terminal png enhanced
set output 'plot3.png'

set title "In-degree distribution"
set xlabel "in-degree"
set ylabel "number of nodes"
set key right top
plot "inDegreeData-random-30.txt" title 'Random Graph c = 30' with histeps, \
	 "inDegreeData-shuffle-ring-30cache.txt" title 'Ring Graph c = 30' with histeps, \
	 "inDegreeData-random-50.txt" title 'Random Graph c = 50' with histeps, \
	 "inDegreeData-shuffle-ring-50cache.txt" title 'Ring Graph c = 50' with histeps
	