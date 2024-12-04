package com.rakdcolon.classes.util;

import com.rakdcolon.Main;

import java.util.*;

/**
 * A data structure that supports insertion, deletion, and getting a random element in constant time.
 */
public class RandomizedSet implements Iterable<Integer>
{
    /**
     * The list of elements in the set.
     */
    private final List<Integer> list;
    
    /**
     * A map that maps each element to the element's index in the list.
     */
    private final Map<Integer, Integer> map;
    
    /**
     * Constructs an empty RandomizedSet with an initial capacity based on the grid size.
     */
    public RandomizedSet ()
    {
        list = new ArrayList<>(Main.SIZE * Main.SIZE);
        map = new HashMap<>(Main.SIZE * Main.SIZE);
    }
    
    /**
     * Constructs an empty RandomizedSet with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the set
     */
    public RandomizedSet (int initialCapacity)
    {
        list = new ArrayList<>(initialCapacity);
        map = new HashMap<>(initialCapacity);
    }
    
    /**
     * Adds a value to the set. If the value already exists, it does nothing.
     *
     * @param val the value to add
     */
    public void add (int val)
    {
        if (map.containsKey(val)) return;
        map.put(val, list.size());
        list.add(val);
    }
    
    /**
     * Increments all elements in the set by the specified increment.
     *
     * @param increment the value to add to each element
     */
    public void incrementAll (int increment)
    {
        for (int i = 0; i < list.size(); i++)
        {
            int newValue = list.get(i) + increment;
            list.set(i, newValue);
            map.put(newValue, i);
        }
    }
    
    /**
     * Removes a value from the set. If the value does not exist, it does nothing.
     *
     * @param val the value to remove
     */
    public void remove (int val)
    {
        Integer index = map.get(val);
        if (index == null) return;
        int lastElement = list.getLast();
        list.set(index, lastElement);
        map.put(lastElement, index);
        list.removeLast();
        map.remove(val);
    }
    
    /**
     * Returns the number of elements in the set.
     *
     * @return the number of elements in the set
     */
    public int size ()
    {
        return list.size();
    }
    
    /**
     * Checks if the set is empty.
     *
     * @return true if the set is empty, false otherwise
     */
    public boolean isEmpty ()
    {
        return list.isEmpty();
    }
    
    /**
     * Checks if the set contains the specified value.
     *
     * @param val the value to check
     *
     * @return true if the set contains the value, false otherwise
     */
    public boolean contains (int val)
    {
        return map.containsKey(val);
    }
    
    /**
     * Adds all elements from the specified set to this set.
     *
     * @param potentialCells the set of elements to add
     */
    public void addAll (RandomizedSet potentialCells)
    {
        for (int cell : potentialCells)
        {
            add(cell);
        }
    }
    
    /**
     * Removes all elements from the specified set from this set.
     *
     * @param potentialCells the set of elements to remove
     */
    public void removeAll (RandomizedSet potentialCells)
    {
        for (int cell : potentialCells)
        {
            remove(cell);
        }
    }
    
    /**
     * Checks if this randomized set is equal to another object.
     *
     * @param obj the object to compare
     *
     * @return true if the sets are equal, false otherwise
     */
    @Override
    public boolean equals (Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof RandomizedSet other)) return false;
        return list.equals(other.list);
    }
    
    /**
     * Returns an iterator over the elements in the set.
     *
     * @return an iterator over the elements in the set
     */
    @Override
    public Iterator<Integer> iterator ()
    {
        return list.iterator();
    }
    
    /**
     * Returns a string representation of the set.
     *
     * @return a string representation of the set
     */
    @Override
    public String toString ()
    {
        return list.toString();
    }
}