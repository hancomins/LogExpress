package com.clipsoft.LogExpress;

import com.clipsoft.LogExpress.configuration.Configuration;
import com.clipsoft.LogExpress.configuration.WriterOption;
import com.clipsoft.LogExpress.configuration.WriterType;
import com.clipsoft.LogExpress.writer.CurrentTimeMillisGetter;
import org.junit.Test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class LogExpressTest {


	@Test
	public void updateConfigTest() {
		Configuration configuration = LogExpress.cloneConfiguration();
		LogExpress.updateConfig(configuration);
	}


	
	@Test
	public void showLineDateTest() {
		Configuration configuration = LogExpress.cloneConfiguration();


		configuration.clearWriters();
		WriterOption option = configuration.newWriterOption("api");
		option.clearWriterType();
		option.addWriterType(WriterType.Console);
		option.setEncoding("UTF-16");
		LogExpress.updateConfig(configuration);
		
		LogExpress.info("Hello World!!");
		
		LogExpress.defaultLogger().info("안녕하세요!!");
		
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	// 수동테스트
	/**
	 * 테스트 방법: 로그가 쌓이는 중에 실시간으로 시스템 날짜를 올려서 새로운 파일이 만들어지는지 확인합니다. 
	 */
	@Test
	public void newDatetest() throws InterruptedException {
		Configuration configuration = LogExpress.cloneConfiguration();
		System.out.println(configuration);
		configuration.clearWriters();
		configuration.setFileExistCheck(true);
		WriterOption option = configuration.newWriterOption("api");
		option.setMaxSize(1);
		String tmpDir = System.getProperty("java.io.tmpdir");
		option.setFile(tmpDir + File.separator + "{marker}.{date:yyyy-MM-dd}.{number}.txt");
		option.addWriterType(WriterType.File);
		option.addWriterType(WriterType.Console);
		option.setMaxSize(1);
		option.setHistory(5);
		configuration.setDebugMode(false);
		LogExpress.updateConfig(configuration);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		for(int i = 0, n = 1000; i < n; ++i) {
			LogExpress.defaultLogger().info( CurrentTimeMillisGetter.currentTimeMillis() + "  " +format.format(new Date(CurrentTimeMillisGetter.currentTimeMillis())) + "(" + i + ")");
			Thread.sleep(1);
			if(i % 100 == 0) {
				final int finalI = i;
				Thread.sleep(100);
				CurrentTimeMillisGetter.setGetTimeMillisDelegator(new CurrentTimeMillisGetter.TimeMillisDelegator() {
					@Override
					public long getCurrentTimeMillis() {
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.DATE, finalI / 100);
						return calendar.getTime().getTime();
					}
				});
			}
		}
		Thread.sleep(1000);
	}


	@Test
	public void unlimitIntervalTest() throws InterruptedException, IOException {
		final File testFile = new File("test.log");
		testFile.delete();
		testFile.createNewFile();
		Configuration configuration = LogExpress.cloneConfiguration();
		configuration.setWorkerInterval(-1);
		WriterOption option = configuration.getWriterOptions()[0];
		option.clearWriterType();
		option.addWriterType(WriterType.File);
		option.setFile(testFile.getAbsolutePath());
		LogExpress.updateConfig(configuration);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileInputStream fileInputStream = new FileInputStream(testFile);
					InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String line = null;
					while(testFile.exists()) {
						line = bufferedReader.readLine();
						if(line != null) {
							System.out.println(line);
						}
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}

					fileInputStream.close();
					inputStreamReader.close();
					bufferedReader.close();

				}  catch (IOException e) {
				}


			}
		}).start();


		Logger LOG = LogExpress.defaultLogger();
		System.out.println("출력시작");
		LOG.info("Hello World!! 1");
		Thread.sleep(1000);
		LOG.info("Hello World!! 2");
		Thread.sleep(1000);
		LOG.info("Hello World!! 3");
		Thread.sleep(1000);

		for(int i = 0, n = 1000; i < n; ++i) {
			LOG.info(i + "");
		}
		Thread.sleep(1000);
		testFile.delete();
	}


	@Test
	public void shutdownTest() throws InterruptedException, IOException {
		File file = new File("test.log");
		Configuration configuration = LogExpress.cloneConfiguration();
		int testCase = 10240000;
		long callLogTime = 0;
		long endFileWriteTime = 0;
		configuration.setQueueSize(testCase);
		configuration.setWorkerInterval(-1);
		WriterOption option = configuration.newWriterOption("test");
		option.setFile(file.getAbsolutePath());
		option.setLinePattern("{level} {hostname} {pid} {message}");
		option.clearWriterType();
		option.addWriterType(WriterType.File);
		LogExpress.updateConfig(configuration);

		long startTime = System.currentTimeMillis();
		Logger logger = LogExpress.newLogger();
		for(int i = 0; i < testCase; ++i) {
			logger.info(i + "");
		}
		callLogTime = System.currentTimeMillis() - startTime;
		ShutdownFuture future = LogExpress.shutdown();
		System.out.println("endendendend");
		System.out.println("endendendend");
		System.out.println("endendendend");
		System.out.println("endendendend");
		System.out.println("endendendend");
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		future.setOnEndCallback(new Runnable() {
			@Override
			public void run() {
				atomicInteger.addAndGet(10);
				System.out.println("==================끝1====================");
			}
		});

		future.setOnEndCallback(new Runnable() {
			@Override
			public void run() {
				atomicInteger.addAndGet(22);
				System.out.println("==================끝2=====================");
			}
		});
		future.await();
		endFileWriteTime = System.currentTimeMillis() - startTime;

		System.out.println("callLogTime : " + callLogTime);
		System.out.println("endFileWriteTime : " + endFileWriteTime);


		assertEquals(32, atomicInteger.get());
		assertEquals(future.isEnd(), true);
		String logs = readFileToString(file);
		assertEquals(testCase + 1, countLine(logs));



		file.delete();
	}





	// java6 기반으로 파일을 읽어서 String 으로 변환하는 메서드.
	public static String readFileToString(File file) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append("\n");
		}
		bufferedReader.close();
		inputStreamReader.close();
		fileInputStream.close();
		return stringBuilder.toString();
	}

	// String 라인의 개수를 구하는 메서드. BufferReader 를 사용함.
	public static int countLine(String string) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new StringReader(string));
		int count = 0;
		while(bufferedReader.readLine() != null) {
			++count;
		}
		bufferedReader.close();
		return count;
	}


}
