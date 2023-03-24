package com.clipsoft.LogExpress.queue;

import com.clipsoft.LogExpress.Line;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentLineQueue extends AbsLineQueue {

	private int mEndIndex = mCapacity - 1;
	private volatile Line[] mCircleQueue;
	private AtomicInteger mWritePos = new AtomicInteger(0);
	private AtomicInteger mReadPos = new AtomicInteger(0);
	private AtomicInteger mSize = new AtomicInteger(0);

	
	
	protected ConcurrentLineQueue(int capacity) {
		super(capacity);
		mEndIndex = capacity - 1;
		mCircleQueue = new Line[capacity];
	}
	
	
	

	@Override
	public void push(Line line) {
		waitFor();
		int pos = getAndUpdate(mWritePos, mEndIndex);
		mCircleQueue[pos] = line;
		super.push(line);
	}


	@Override
	public Line pop() {
		if(mSize.get() == 0) {
			return null;
		}
		int pos = getAndUpdate(mReadPos, mEndIndex);
		Line result = mCircleQueue[pos];
		mCircleQueue[pos] = null;
		mSize.decrementAndGet();
		return result;
	}
	
	
	public final void waitFor() {
		for (;;) {
			int current = mSize.get();
			if(current == mCapacity) continue;
			int next = current + 1;
			if (mSize.compareAndSet(current, next)) {
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
