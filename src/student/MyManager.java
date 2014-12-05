package student;

import java.util.HashMap;
import java.util.LinkedList;
import game.Edge;
import game.Node;
import game.Parcel;
import game.Truck;
import game.Board;
import java.util.ArrayList;


public class MyManager extends game.Manager {

	private Board b;
	ArrayList<Truck> t;

	public MyManager() {
		// TODO Auto-generated constructor stub
	}


	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		int count = 0;
		b = this.getBoard();
		t = b.getTrucks();

		//Allocate parcels to trucks
		for (Truck truck: t){
			truck.setUserData(new LinkedList<Parcel>());
		}
		
		//Allocate packages to trucks
		for (Parcel parcel: b.getParcels()){
			if (count >= t.size()){
				count = 0;
			}
			((LinkedList<Parcel>)t.get(count).getUserData()).add(parcel);
			count += 1;
		}
		
		for (Truck truck: t){
			System.out.println("Truck: "+truck+", Parcels:"+truck.getUserData());
		}
		
		//All trucks start off at the truck depot
		for (Truck truck: t){
			LinkedList<Parcel> list = (LinkedList<Parcel>)truck.getUserData();
			
			if (list != null){
				Node node = list.get(0).getLocation();
				
				LinkedList<Node> bestPath = dijkstra(truck.getLocation(), node);	
				
				if (bestPath.size() > 0){
					System.out.println("Path: " + bestPath);
				}
				
				System.out.println("Parcel location: " + node + ", Dijkstra's destination: "+ bestPath.get(bestPath.size() - 1));
				
				//Make trucks use the shortest path when traveling to pick up parcels
				if ((bestPath != null) && (bestPath.size() > 0)){
					truck.setTravelPath(bestPath);
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void truckNotification(Truck truck, Notification notification) {
		
		//Truck status
		switch(notification){
				case DROPPED_OFF_PARCEL:
					System.out.println("DROPPED_OFF_PARCEL");
					break;
				case GOING_TO_CHANGED:
					System.out.println("GOING_TO_CHANGED");
					break;
				case LOCATION_CHANGED:
					System.out.println("LOCATION_CHANGED");
					break;
				case PARCEL_AT_NODE:
					System.out.println("PARCEL_AT_NODE");			
					break;
				case PICKED_UP_PARCEL:
					System.out.println("PICKED_UP_PARCEL");
					break;
				case STATUS_CHANGED:
					System.out.println("STATUS_CHANGED");
					break;
				case TRAVELING_TO_CHANGED:
					System.out.println("TRAVELING_TO_CHANGED");
					break;
				case WAITING:
					if (((LinkedList<Parcel>)truck.getUserData()).size() == 0){
						System.out.println("WAIT");
						if (truck.getLocation().equals(b.getTruckDepot())){
							return;
						}
						else{
							System.out.println("MOVE");
							Node node = b.getTruckDepot();
							moveTruck(truck, node);
						}
					}
					else{
						if (truck.getLoad() == null){
							System.out.println("PICK UP PARCEL");
							synchronized(this){
								Parcel parcel = ((LinkedList<Parcel>)truck.getUserData()).get(0);
								if (truck.getLocation().equals(parcel.getLocation())){
									truck.pickupLoad(parcel);
									moveTruck(truck, parcel.destination);
								}
							}
						}
						else{
							System.out.println("DROP OFF PARCEL");
							synchronized(this){
								Parcel parcel = truck.getLoad();
								
								//First parcel
								if (truck.getLocation().equals(parcel.destination)){
									((LinkedList<Parcel>)truck.getUserData()).remove(0);
									truck.dropoffLoad();
									
									//We have more parcels to get
									if (((LinkedList<Parcel>)truck.getUserData()).size() != 0){
										parcel = ((LinkedList<Parcel>)truck.getUserData()).get(0);
										this.moveTruck(truck, parcel.destination);
									}
								}
							}
						}
					}
					break;
			}
	}
	
	//Each truck runs in its own thread
	public synchronized void moveTruck(Truck truck, Node node){
		if (truck.getLocation().equals(node)){
			return;
		}
		
		//Must let truck travel the shortest path
		LinkedList<Node> bestPath = dijkstra(truck.getLocation(), node);
		truck.setTravelPath(bestPath);
	}
	

	private LinkedList<Node> dijkstra(Node start, Node end){
		
		//visited cities and distances
		HashMap<Node, Double> nodeDistances = new HashMap<Node, Double>();
		
		//pairs of nodes and previously visited nodes
		HashMap<Node, Node> previousNodes = new HashMap<Node, Node>();

		//Set all nodes other than the start node to be at a large distance
		for(Node node: getNodes()){
			if(!(node.equals(start))){
				nodeDistances.put(node, Double.MAX_VALUE);
			}
		}
	
		
		//unvisited cities
		GriesHeap<Node> shortestPathHeap = new GriesHeap<Node>();
		
		//Start at truck depot Node		
		shortestPathHeap.add(start, 0.0);
		nodeDistances.put(start, 0.0);
								
		//Keep getting shortest paths until end node is reached
		while((end != shortestPathHeap.peek()) && (shortestPathHeap.size() > 0)){
			
			Node currentNode = shortestPathHeap.poll();
	
			for (Edge edge: currentNode.getExits()){
				 
				//Get neighbor node
				Node nextNode = edge.getOther(currentNode);
				
				//Update distance from current node
				double curDist = nodeDistances.get(currentNode) + edge.length;
				double neighborDist = nodeDistances.get(nextNode);
				
				//We have a new shortest path
				if(curDist < neighborDist){
					nodeDistances.put(nextNode, curDist);
					previousNodes.put(nextNode, currentNode);
					
					try {
						shortestPathHeap.add(nextNode, curDist);
					}
					catch(IllegalArgumentException i){
						shortestPathHeap.updatePriority(nextNode, curDist);
					}
				}

	
			}

			
		}

		//Get shortest path
		LinkedList<Node> shortestPath = new LinkedList<Node>();
		
		Node currentNode = end;
		
		while (currentNode != null){
			shortestPath.push(currentNode);
			currentNode = previousNodes.get(currentNode);
		}
		
		return shortestPath;
		
	}


}
