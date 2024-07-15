import edu.princeton.cs.algs4.CC;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedGraph;
import edu.princeton.cs.algs4.KruskalMST;
import edu.princeton.cs.algs4.MaxPQ;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.StdOut;

public class Clustering {

    // representation of connected component
    private final CC cc;

    // cluster size
    private final int clusterSize;

    // number of locations
    private final int nLocations;


    // run the clustering algorithm and create the clusters
    public Clustering(Point2D[] locations, int k) {

        inrange(locations);
        int localength = locations.length;
        nLocations = localength;
        clusterSize = k;
        inrange2(k);
        // construct graph
        int nvertices = nLocations;
        EdgeWeightedGraph graph = new EdgeWeightedGraph(nvertices);
        for (int i = 0; i < nvertices; i++) {
            for (int j = i + 1; j < nvertices; j++) {
                double weight = locations[i].distanceSquaredTo(locations[j]);
                graph.addEdge(new Edge(i, j, weight));
            }
        }

        int nEdges = localength - k; // number of edges in the cluster
        EdgeWeightedGraph clusterGraph = new EdgeWeightedGraph(nvertices);

        // form cluster of k connected components
        for (Edge edge : getClusters(graph, nEdges))
            clusterGraph.addEdge(edge);

        cc = new CC(clusterGraph);
    }

    // null validate objects
    private void inrange(Object object) {
        if (object == null)
            throw new IllegalArgumentException("the input should not be null");
    }


    // validate dimension of index
    private void inrange2(int k) {
        if (k < 1 || k > nLocations)
            throw new IllegalArgumentException("the number od clusters is invalid");
    }


    // returns a collection of edges representing cluster
    private Iterable<Edge> getClusters(EdgeWeightedGraph g, int n) {
        // Compute the minimum spanning tree of the graph
        KruskalMST mst = new KruskalMST(g);


        //! these edges are already in increasing
        // order so no need to use pq???????
        MaxPQ<Edge> pq = new MaxPQ<>();
        for (Edge edge : mst.edges()) {
            pq.insert(edge);
            if (pq.size() > n)
                pq.delMax();
        }
        return pq;
    }

    // return the cluster of the ith point
    public int clusterOf(int i) {
        if (i < 0 || i > nLocations)
            throw new IllegalArgumentException("invalid cluster index");
        return cc.id(i);
    }

    // use the clusters to reduce the dimensions of an input
    public int[] reduceDimensions(int[] input) {
        inrange(input);
        if (input.length != nLocations)
            throw new IllegalArgumentException("invalid input length ");
        int[] output = new int[clusterSize];
        for (int i = 0; i < input.length; i++) {
            int cluster = clusterOf(i);
            output[cluster] += input[i];
        }
        return output;
    }


    // unit testing (required)
    public static void main(String[] args) {

        // Example usage:
        Point2D[] locations = {
                new Point2D(0, 0), new Point2D(1, 1),
                new Point2D(2, 2)
        };
        int k = 2;
        Clustering clustering = new Clustering(locations, k);
        int[] input = { 1, 2, 3 };
        int[] reducedDimensions = clustering.reduceDimensions(input);
        for (int dimension : reducedDimensions) {
            System.out.print(dimension + " ");
        }
        StdOut.println();
        StdOut.println(clustering.clusterOf(1));

    }
}
