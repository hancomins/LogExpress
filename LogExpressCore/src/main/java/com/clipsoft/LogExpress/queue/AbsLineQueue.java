package com.clipsoft.LogExpress.queue;

import com.clipsoft.LogExpress.Line;

public abstract class AbsLineQueue {

	protected final int mCapacity;


	private OnPushLineListener mPushLineListener = new OnPushLineListener() {
		@Override
		public void onPushLine() {
		}
	};

	public int getCapacity() {
		return mCapacity;
	}

	public AbsLineQueue(int capacity) {
		mCapacity = capacity;
	}


	public void setPushLineEvent(OnPushLineListener pushLineListener) {
		mPushLineListener = pushLineListener;
	}

	
	
	public void push(Line line) {
		mPushLineListener.onPushLine();
	}

	public abstract Line pop();

	

	

}
