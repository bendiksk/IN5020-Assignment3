# You can uncomment the following lines to produce a png figure
set terminal png enhanced
set output 'plot.png'

set title "Average Clustering Coefficient"
set xlabel "cycles"
set ylabel "clustering coefficient (log)"
set key right top
set logscale y 
plot "clusteringCoefficientData-shuffle-ring-30cache.txt" title 'Random Graph c = 30' with lines, \
	"clusteringCoefficientData-shuffle-star-30cache.txt" title 'Shuffle c = 30' with lines, \
	"clusteringCoefficientData-shuffle-ring-50cache.txt" title 'Random Graph c = 50' with lines, \
	"clusteringCoefficientData-shuffle-star-50cache.txt" title 'Shuffle c = 50' with lines