package student;

import game.Manager;
import game.Truck;
import game.Parcel;

import game.Board;
import game.Edge;
import game.Node;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;


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

		//allocate parcels
		for (Truck truck: t){
			truck.setUserData(new LinkedList<Parcel>());
		}
		//allocate packages
		for (Parcel parcel: b.getParcels()){
			if (count >= t.size()){
				count = 0;
			}
			((LinkedList<Parcel>)t.get(count).getUserData()).add(parcel);
			count = count + 1;
		}
		for (Truck truck: t){
			System.out.println("Truck: "+truck+", Parcels:"+truck.getUserData());
		}
		//move trucks to initial state
		for (Truck truck: t){
			LinkedList<Parcel> list = (LinkedList<Parcel>)truck.getUserData();
			if (list != null){
				Node node = list.get(0).getLocation();
				LinkedList<Node> bestPath = Dijkstra(truck.getLocation(), node);				
				if (bestPath.size() > 0){
					System.out.println("Path: "+bestPath);
				}
				System.out.println("Parcel location: "+node+", Dijkstra's destination: "+bestPath.get(bestPath.size() - 1));
				if ((bestPath != null) && (bestPath.size() > 0)){
					truck.setTravelPath(bestPath);
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void truckNotification(Truck truck, Notification notification) {
		//check notification
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
						//initial parcel
						if (truck.getLocation().equals(parcel.destination)){
							((LinkedList<Parcel>)truck.getUserData()).remove(0);
							truck.dropoffLoad();
							//further parcels
							if (((LinkedList<Parcel>)truck.getUserData()).size() != 0){
								parcel = ((LinkedList<Parcel>)truck.getUserData()).get(0);
								this.moveTruck(truck, parcel.getLocation());
							}
						}
					}
				}
			}
			break;
		}
	}
	
	public synchronized void moveTruck(Truck truck, Node node){
		if (truck.getLocation().equals(node)){
			return;
		}
		LinkedList<Node> bestPath = Dijkstra(truck.getLocation(), node);
		truck.setTravelPath(bestPath);
	}
	
	public LinkedList<Node> Dijkstra(Node first, Node last){
		HashMap<Node, Integer> length = new HashMap<Node, Integer>();
		HashMap<Node, Node> previous = new HashMap<Node, Node>();
		
		for (Node node: this.getBoard().getNodes()){
			length.put(node, Integer.MAX_VALUE);
			previous.put(node, null);
		}
		length.put(first, 0);
		GriesHeap<Node> heap = new GriesHeap<Node>();
		heap.add(first, 0);
		while ((last != heap.peek()) && (heap.size() > 0)){
			Node currentNode = heap.poll();
			for (Edge edge: currentNode.getExits()){
				Node nextNode = edge.getOther(currentNode);
				int currentDistance = length.get(currentNode) + edge.length;
				int nextDistance = length.get(nextNode);
				if (currentDistance < nextDistance){
					length.put(nextNode, currentDistance);
					previous.put(nextNode, currentNode);
					try {
						heap.add(nextNode, currentDistance);
					}
					catch(IllegalArgumentException i){
						heap.updatePriority(nextNode, currentDistance);
					}
				}
			}
		}
		LinkedList<Node> node = new LinkedList<Node>();
		Node currentNode = last;
		while (currentNode != null){
			node.push(currentNode);
			currentNode = previous.get(currentNode);
		}
		return node;
	}

}
