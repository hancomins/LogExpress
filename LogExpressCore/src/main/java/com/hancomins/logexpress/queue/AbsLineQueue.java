package com.hancomins.logexpress.queue;

import com.hancomins.logexpress.Line;

public abstract class AbsLineQueue {

	protected final int capacity;


	private OnPushLineListener pushLineListener = new OnPushLineListener() {
		@Override
		public void onPushLine() {
		}
	};

	@SuppressWarnings("unused")
    public int getCapacity() {
		return capacity;
	}

	public AbsLineQueue(int capacity) {
		this.capacity = capacity;
	}


	public void setPushLineEvent(OnPushLineListener pushLineListener) {
		this.pushLineListener = pushLineListener;
	}

	
	
	public void push(Line line) {
		pushLineListener.onPushLine();
	}

	public abstract Line pop();

	

	

}
