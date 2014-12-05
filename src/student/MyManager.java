package student;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import game.Manager;
import game.Node;
import game.Parcel;
import game.Truck;

public class MyManager extends Manager {

	public void run() {
		Set<Parcel> parcels = getParcels();

		for(Parcel p: parcels){
			
		}
	}

	public void truckNotification(Truck t, Notification message) {
		// TODO Auto-generated method stub

	}

	private List<Node> dijkstra (Node start, Node end){
		
		//unvisited cities
		GriesHeap<Node> unvisited = new GriesHeap<Node>();
		
		//visited cities and distances
		HashMap<Node, Double> visited = new HashMap<Node, Double>();
		
		//pairs of nodes and previously visited nodes
		HashMap<Node, Node> previousNodes = new HashMap<Node, Node>();
		
		
		//Start at truck depot Node		
		unvisited.add(start, 0);
		visited.put(start, 0.0);
		
		//Set all nodes other than the start node to be at a large distance
		for(Node node: getNodes()){
			if(!(node == start)){
				unvisited.add(node, Double.MAX_VALUE);
			}
		}
		
		Node prevNode = start;
		double distancetoNode = 0.0;
		
		
		//Keep getting shortest paths until end node is reached
		while((prevNode != end) && (unvisited.size() > 0)){
			
			for(Node neighborNode: prevNode.getNeighbors().keySet()){
				 
				//Update distance from current node
				double neighborDist = distancetoNode + prevNode.getNeighbors().get(prevNode);
				
				//We haven't visited this node yet
				if(!previousNodes.containsKey(neighborNode)){
					previousNodes.put(neighborNode, prevNode);
					
					visited.put(neighborNode, neighborDist);
					
					unvisited.updatePriority(prevNode, neighborDist);
					
				}
				
				//We have visited this node, so must update distances accordingly as algorithm progresses
				else if(neighborDist < visited.get(neighborNode)){
					visited.remove(neighborNode);
					visited.put(neighborNode, neighborDist);
					previousNodes.remove(neighborNode);
					previousNodes.put(neighborNode, prevNode);
				}
	
			}
			
			//Update shortest path distances
			prevNode = unvisited.poll();
			distancetoNode = visited.get(prevNode);
			
		}
		
		
		/**Get shortest path**/
		
		LinkedList<Node> shortestPath = new LinkedList<Node>();
		
		Node lastNode = end;
		
		while(previousNodes.containsKey(lastNode)){
			shortestPath.addFirst(lastNode);
			lastNode = previousNodes.get(lastNode);
		}
		
		return shortestPath;
		
	}

}
