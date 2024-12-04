package com.rakdcolon;

import java.util.*;

public class RandomizedSet implements Iterable<Integer>
{
    private final List<Integer> list;
    private final Map<Integer, Integer> map;
    private final Random rand;
    
    public RandomizedSet()
    {
        list = new ArrayList<>();
        map = new HashMap<>();
        rand = new Random();
    }
    
    public boolean add(int val)
    {
        if (map.containsKey(val)) return false;
        map.put(val, list.size());
        list.add(val);
        return true;
    }
    
    public void incrementAll(int increment)
    {
        for (int i = 0; i < list.size(); i++)
        {
            int newValue = list.get(i) + increment;
            list.set(i, newValue);
            map.put(newValue, i);
        }
    }
    
    public void remove(int val)
    {
        Integer index = map.get(val);
        if (index == null) return;
        int lastElement = list.getLast();
        list.set(index, lastElement);
        map.put(lastElement, index);
        list.removeLast();
        map.remove(val);
    }
    
    public int getRandom()
    {
        return list.get(rand.nextInt(list.size()));
    }
    
    public int size()
    {
        return list.size();
    }
    
    public boolean isEmpty()
    {
        return list.isEmpty();
    }
    
    public boolean contains(int val)
    {
        return map.containsKey(val);
    }
    
    public void clear()
    {
        list.clear();
        map.clear();
    }
    
    @Override
    public Iterator<Integer> iterator()
    {
        return list.iterator();
    }
    
    @Override
    public String toString()
    {
        return list.toString();
    }
    
    public List<Integer> getList ()
    {
        return list;
    }
    
    public void addAll (RandomizedSet potentialCells)
    {
        for (int cell : potentialCells)
        {
            add(cell);
        }
    }
}