package com.hancomins.logexpress.writer;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class OldFileCleaner {
	
	private FileNamePattern fileNamePattern = null;
	private long pid = -1;
	private String hostname = null;
	private String marker = null;
	
	OldFileCleaner(FileNamePattern pattern, long pid, String hostname, String marker) {
		fileNamePattern = pattern;
		this.pid = pid;
		this.hostname = hostname;
		this.marker = marker;
	}
	
	/**
	 * 현재 날짜를 기준으로 오래된 파일부터 제거합니다. 
	 * @param history 기준 날짜. history 이후 파일들을 제거합니다. 0을 입력하면 모두 제거합니다.
	 * @return 제거된 날짜수
	 */
	@SuppressWarnings("UnusedReturnValue")
    public int clean(int history)  {
		if(history < 1) return 0;
		final int TRY_COUNT = 3;
		int removeDays = 0;
		int lossDaysCount = 0;
		for(int day = history;;++day) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(CurrentTimeMillisGetter.currentTimeMillis());
			calendar.add(Calendar.DATE, -day);
			int lossNumberCount = 0;
			boolean success = false; 
			for(int i = 0;;++i) {
				try {
					File file = fileNamePattern.toFile(pid, hostname, marker, calendar.getTimeInMillis(), i);
					if(file.exists()) {
						success = true;
						try {
							file.delete();
						} catch (Exception e) {
							//TODO 파일 삭제 실패 메시지 출력해야함.
						}
					} else {
						if(++lossNumberCount > TRY_COUNT) {
							break;
							
						}
					}
				} catch (IOException ignored) {
					if(++lossNumberCount > TRY_COUNT) {
						break;
					}
				}
			}
			if(!success) {
				if(++lossDaysCount > TRY_COUNT) {
					break;
				}
				continue;
			}
			removeDays++;
		}
		return removeDays;
	}
	

}
