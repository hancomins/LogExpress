package com.clipsoft.LogExpress.writer;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;

public class OldFileCleanerTest {

	// "number", "date","marker", "hostname", "pid","path"
	@Test
	public void defaultTest() throws IOException {
		/*
		// 사용자가 패턴을 잘못 지정하여 서로 다른 marker 를 갖고 있는 두 개 이상의 로그 파일이 충돌나는 경우는 고려하지 않는다.
		FileNamePattern pattern = FileNamePattern.parse("./test/{marker}{date:yyyy-MM-dd}{marker}{hostname}{pid}{path}{number}.log");
		
		
		String hostName = Systool.hostname();
		long pid = Systool.pid();
		Calendar calendar = Calendar.getInstance();
		Random rand = new Random(System.currentTimeMillis());
		for(int i = 0; i < 60; ++i) {
			int maxNumber = rand.nextInt(19) + 1;
			for(int j = 0; j < maxNumber; ++j) {
				int fileNumber = j;
				File file = pattern.toFile(pid, hostName, "beombeom",calendar.getTimeInMillis(),  fileNumber);
				file.getParentFile().mkdir();
				file.createNewFile();
			}
			calendar.add(Calendar.DATE, -1);
		}
		OldFileCleaner oldFileCleaner = new OldFileCleaner(pattern, pid, hostName, "beombeom");
		assertEquals(30, oldFileCleaner.clean(30));
		
		oldFileCleaner = new OldFileCleaner(pattern, pid, hostName, "beombeom");
		assertEquals(29, oldFileCleaner.clean(1));*/
		
	}
	
	@After
	public void after() {
		/*File[] list = new File("./test").listFiles();
		for(int i = 0, n = list.length; i < n; ++i) {
			
		}
		
		
		try {
			Files.delete(new File("./test").toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	

}
