package ru.yooxa.bungee.auth;

import java.util.Iterator;
import java.util.List;

public class YIterator<T>
  extends Object {
  private List<T> list;
  private Iterator<T> iterator;

  public YIterator(List<T> list) {
    this.list = list;
    this.iterator = list.iterator();
  }
  
  public T getNext() {
    if (!this.iterator.hasNext()) {
      this.iterator = this.list.iterator();
    }
    
    return (T)this.iterator.next();
  }

  
  public int size() { return this.list.size(); }
}


