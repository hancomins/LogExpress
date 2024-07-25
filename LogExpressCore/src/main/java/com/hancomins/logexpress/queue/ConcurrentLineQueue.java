package com.hancomins.logexpress.queue;

import com.hancomins.logexpress.Line;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentLineQueue extends AbsLineQueue {

	private int endIndex = capacity - 1;
	private final Line[] circleQueue;
	private final AtomicInteger writePos = new AtomicInteger(0);
	private final AtomicInteger readPos = new AtomicInteger(0);
	private final AtomicInteger size = new AtomicInteger(0);

	
	
	protected ConcurrentLineQueue(int capacity) {
		super(capacity);
		endIndex = capacity - 1;
		circleQueue = new Line[capacity];
	}
	
	
	

	@Override
	public void push(Line line) {
		waitFor();
		int pos = getAndUpdate(writePos, endIndex);
		circleQueue[pos] = line;
		super.push(line);
	}


	@Override
	public Line pop() {
		if(size.get() == 0) {
			return null;
		}
		int pos = getAndUpdate(readPos, endIndex);
		Line result = circleQueue[pos];
		circleQueue[pos] = null;
		size.decrementAndGet();
		return result;
	}
	
	
	public final void waitFor() {
		for (;;) {
			int current = size.get();
			if(current == capacity) continue;
			int next = current + 1;
			if (size.compareAndSet(current, next)) {
				return;
			}
		}
	}
	
	
	private int getAndUpdate(AtomicInteger integer, int capacity) {
		for (;;) {
			int current = integer.get();
			int next = current + 1;
			if (current == capacity) {
				next = 0;
			}
			if (integer.compareAndSet(current, next)) {
				return current;
			}
		}
	}
	 
	 
	
	
	

}
