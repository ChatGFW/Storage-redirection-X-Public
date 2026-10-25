package org.srx.monitor;

import java.util.ArrayDeque;

// 有限队列缓存短暂断连期间的事件，溢出计入丢失总数。
public final class MonitorEventBuffer<T> {
  private final ArrayDeque<T> events = new ArrayDeque<>();
  private final int capacity;
  private long lost;

  public MonitorEventBuffer(int capacity) {
    this.capacity = capacity;
  }

  public synchronized boolean offer(T event) {
    if (events.size() == capacity) {
      lost++;
      return false;
    }
    events.addLast(event);
    return true;
  }

  public synchronized T peek() {
    return events.peekFirst();
  }

  public synchronized void delivered(T event) {
    if (events.peekFirst() == event)
      events.removeFirst();
  }

  public synchronized long lost() {
    return lost;
  }

  public synchronized void clear() {
    events.clear();
  }
}
