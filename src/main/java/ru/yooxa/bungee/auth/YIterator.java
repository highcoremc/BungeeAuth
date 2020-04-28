package ru.yooxa.bungee.auth;

import java.util.Iterator;
import java.util.List;

public class YIterator<T>
{
  private final List<T> list;
  private Iterator<T> iterator;

  public YIterator(List<T> list) {
    this.list = list;
    this.iterator = list.iterator();
  }
  
  public T getNext() {
    if (!this.iterator.hasNext()) {
      this.iterator = this.list.iterator();
    }
    
    return this.iterator.next();
  }

  public int size() { return this.list.size(); }
}


