set terminal png enhanced
set output 'plot.png'

set title "In-degree distribution"
set xlabel "in-degree"
set ylabel "number of nodes"
set key right top
plot "inDegreeData-shuffle-ring-30cache.txt" title 'Basic Shuffle c = 30' with histeps, \
	"inDegreeData-shuffle-star-30cache.txt" title 'Random Graph c = 30' with histeps, \
	"inDegreeData-shuffle-ring-50cache.txt" title 'Basic Shuffle c = 50' with histeps, \
	"inDegreeData-shuffle-star-50cache.txtt" title 'Random Graph c = 50' with histeps
	