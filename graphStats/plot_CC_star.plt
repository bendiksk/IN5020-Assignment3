# You can uncomment the following lines to produce a png figure
set terminal png enhanced
set output 'plot2.png'

set title "Average Clustering Coefficient"
set xlabel "cycles"
set ylabel "clustering coefficient (log)"
set key right top
set logscale y 
plot "clusteringCoefficientData-random-30.txt" title 'Random graph c = 30' with lines, \
	"clusteringCoefficientData-shuffle-star-30cache.txt" title 'Shuffle-star c = 30' with lines, \
	"clusteringCoefficientData-random-50.txt" title 'Random Graph c = 50' with lines, \
	"clusteringCoefficientData-shuffle-star-50cache.txt" title 'Shuffle-star c = 50' with lines