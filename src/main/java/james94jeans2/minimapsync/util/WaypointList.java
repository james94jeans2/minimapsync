package james94jeans2.minimapsync.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.logging.log4j.Logger;

import james94jeans2.minimapsync.Minimapsync;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class WaypointList implements java.lang.Iterable<Waypoint>
{
	private int dim;
	private LinkedList<Waypoint> list;
	private boolean isPublic;
	private String username;
	
	public WaypointList(int pDim, boolean pIsPublic)
	{
		dim = pDim;
		list = new LinkedList<Waypoint>();
		username = "";
		isPublic = pIsPublic;
	}
	
	private WaypointList(int pDim, boolean pIsPublic, LinkedList<Waypoint> pList)
	{
		dim = pDim;
		list = pList;
		username = "";
		isPublic = pIsPublic;
	}
	
	public WaypointList(int pDim, String pUsername)
	{
		dim = pDim;
		list = new LinkedList<Waypoint>();
		username = pUsername;
		isPublic = false;
	}
	
	private WaypointList(int pDim, String pUsername, LinkedList<Waypoint> pList)
	{
		dim = pDim;
		list = pList;
		username = pUsername;
		isPublic = false;
	}
	
	public void add(Waypoint pWaypoint)
	{
		list.add(pWaypoint);
	}
	
	public void addAll(WaypointList pList)
	{
		for(Waypoint tempPoint : pList)
		{
			list.add(tempPoint);
		}
	}
	
	public void clear()
	{
		list.clear();
	}
	
	public WaypointList clone()
	{
		if(isPublic)
		{
			LinkedList<Waypoint> tempList = new LinkedList<Waypoint>();
			for (Waypoint tempPoint : list)
			{
				tempList.add(tempPoint.clone());
			}
			return new WaypointList(dim, true, tempList);
		}else
		{
			LinkedList<Waypoint> tempList = new LinkedList<Waypoint>();
			for (Waypoint tempPoint : list)
			{
				tempList.add(tempPoint.clone());
			}
			return new WaypointList(dim, username, tempList);
		}
	}
	
	public boolean contains(Waypoint pWaypoint)
	{
		return list.contains(pWaypoint);
	}
	
	public Waypoint get(int index)
	{
		return list.get(index);
	}
	
	public int getDim()
	{
		return dim;
	}
	
	public WaypointList getDoublePoints(WaypointList listA)
	{
		WaypointList duplicates = new WaypointList(dim, true);
		for(Waypoint tempA : listA)
		{
			ListIterator iterator = list.listIterator();
			Waypoint tempB = null;
			while(iterator.hasNext())
			{
				tempB = (Waypoint) iterator.next();
				if(tempA.getCompareStr().equalsIgnoreCase(tempB.getCompareStr()))
				{
					duplicates.add(tempB);
				}
			}
		}
		return duplicates;
	}
	
	public WaypointList getMissingPoints(WaypointList listA)
	{
		boolean allreadyIn;
		WaypointList missing = new WaypointList(dim, isPublic);
		for(Waypoint tempA : listA)
		{
			allreadyIn = false;
			ListIterator iterator = list.listIterator();
			Waypoint tempB = null;
			while(iterator.hasNext() && !allreadyIn)
			{
				tempB = (Waypoint) iterator.next();
				if(tempA.getCompareStr().equalsIgnoreCase(tempB.getCompareStr()))
				{
					allreadyIn = true;
				}
			}
			if(!allreadyIn && !tempA.isDeath())
			{
				missing.add(tempA);
			}
		}
		return missing;
	}
	
	public WaypointList getWaypointsForName(String pName)
	{
		WaypointList tempList = new WaypointList(dim, true);
		for(Object tempObject : list)
		{
			Waypoint tempPoint = (Waypoint) tempObject;
			if(tempPoint.getName().equalsIgnoreCase(pName))
			{
				tempList.add(tempPoint);
			}
		}
		return tempList;
	}
	
	public WaypointList getWaypointsForNameWithCase(String pName)
	{
		WaypointList tempList = new WaypointList(dim, true);
		for(Object tempObject : list)
		{
			Waypoint tempPoint = (Waypoint) tempObject;
			if(tempPoint.getName().equals(pName))
			{
				tempList.add(tempPoint);
			}
		}
		return tempList;
	}
	
	public int indexOf(Waypoint pWaypoint)
	{
		return list.indexOf(pWaypoint);
	}
	
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
	
	@Override
	public Iterator<Waypoint> iterator() {
		return list.listIterator();
	}
	
	public ListIterator<Waypoint> listIterator()
	{
		return list.listIterator();
	}
	
	public boolean remove(Waypoint pWaypoint)
	{
		return list.remove(pWaypoint);
	}
	
	public boolean removeAll(WaypointList tempList) {
		boolean success = false;
		for (Waypoint p : tempList) {
			success &= list.remove(p);
		}
		return success;
	}
	
	public void setDim(int pDim)
	{
		dim = pDim;
	}
	
	public int size()
	{
		return list.size();
	}
	
	public WaypointList getPublic () {
		WaypointList returnList = new WaypointList(dim, true);
		for (Waypoint waypoint : list) {
			if (waypoint.isPublic()) {
				returnList.add(waypoint);
			}
		}
		return returnList;
	}
	
	public WaypointList getPrivate() {
		return getPublic().getMissingPoints(this);
	}
	
	public boolean isPrivate () {
		return !isPublic;
	}

	public void setPrivate(boolean priv) {
		isPublic = !priv;
	}
	
}
