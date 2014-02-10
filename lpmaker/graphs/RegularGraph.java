
package lpmaker.graphs;

public class RegularGraph extends Graph{
	
	protected int degree;

	public RegularGraph(int size, int degree){
		super(size);
		this.degree = degree;
	}
	public RegularGraph(int size){
		super(size);
		this.degree = 0;
	}

	public RegularGraph(int size, int noHosts,int degree){
		super(size,noHosts);
		this.degree = degree;
	}
	
	public int getDegree(){
		return degree;
	}
}
