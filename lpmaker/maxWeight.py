import networkx as nx
import sys

def sortdict(d):
	s = d.items()
        s.sort()
        print '{' + ', '.join(map(lambda t: ': '.join(map(repr, t)), s)) + '}'


G=nx.read_weighted_edgelist(sys.argv[1], nodetype=int)
#nx.write_weighted_edgelist(G, 'test.weighted.edgelist')
#for n in G.nodes_iter(data=False):
#	print nx.all_neighbors(G,n)
#print sortdict(nx.max_weight_matching(G, maxcardinality=True))

orig_stdout = sys.stdout
f = file(sys.argv[2], 'w')
sys.stdout = f


dict = nx.max_weight_matching(G, maxcardinality=True)
#print list(G.nodes_iter(data=False))
for x in range (0, len(G)):
	if dict.get(x) is not None: 
		print x ,dict.get(x)


sys.stdout = orig_stdout
f.close()

#print G.nodes()
