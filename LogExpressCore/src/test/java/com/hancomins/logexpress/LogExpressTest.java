package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.Configuration;
import com.hancomins.logexpress.configuration.WriterOption;
import com.hancomins.logexpress.configuration.WriterType;
import com.hancomins.logexpress.writer.CurrentTimeMillisGetter;
import com.hancomins.logexpress.writer.FileWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class LogExpressTest {


	@Test
	public void updateConfigTest() {
		Configuration configuration = LogExpress.cloneConfiguration();
		LogExpress.updateConfig(configuration);
	}

	private long dateStringToLong(String dateString) {
		String dateTimeString = dateString;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        try {
            date = formatter.parse(dateTimeString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date.getTime();
	}

	private void deleteAllFile(File file) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for(File f : files) {
				deleteAllFile(f);
			}
		}
		file.delete();
	}

	@Test
	public void duplicatedWriteOverNightTest() throws InterruptedException, IOException {
		final AtomicLong currentTime = new AtomicLong(dateStringToLong("2024-01-02 23:59:00"));

		CurrentTimeMillisGetter.setGetTimeMillisDelegator(new CurrentTimeMillisGetter.TimeMillisDelegator() {
			@Override
			public long getCurrentTimeMillis() {

				return currentTime.incrementAndGet();
			}
		});
		Thread.sleep(1000);

		File testLogDir = new File("testLog");
		deleteAllFile(testLogDir);
		testLogDir.mkdirs();
		LogExpress.shutdown().await();

		Configuration configuration = LogExpress.cloneConfiguration();
		WriterOption testWriter = configuration.newWriterOption("test");
		testWriter.setFile(testLogDir.getAbsolutePath() + File.separator + "{date:yyyy-MM-dd}.log");
		testWriter.clearWriterType();
		testWriter.addWriterType(WriterType.File);
		testWriter.setLinePattern("{time::hh:mm:ss:SSS}\t{level} {message}");
		LogExpress.updateConfig(configuration);


		configuration = LogExpress.cloneConfiguration();
		WriterOption withCaller = configuration.newWriterOption("test2");
		withCaller.setFile(testLogDir.getAbsolutePath() + File.separator + "{date:yyyy-MM-dd}.log");
		withCaller.clearWriterType();
		withCaller.addWriterType(WriterType.File);
		withCaller.setLinePattern("{time::hh:mm:ss:SSS}\t{level} <{caller-simple}> {message}");
		LogExpress.updateConfig(configuration);

		Logger logger = LogExpress.newLogger("test");
		logger = LogExpress.newLogger( String.class,"test2");

		int testCase = 240000;
		final CountDownLatch countDownLatch = new CountDownLatch(testCase);
		final Logger loggerA = LogExpress.newLogger("test");
		final Logger loggerB = LogExpress.newLogger( String.class,"test2");
		ExecutorService executors = Executors.newFixedThreadPool(32);
		for(int i = 0; i < testCase;++i) {
			final int finalI = i;
			executors.submit(new Runnable() {
				@Override
				public void run() {
					final Random random = new Random(System.nanoTime());
					Logger logger;
					if(random.nextBoolean()) {
						logger = loggerA;
					} else {
						logger = loggerB;
					}
					logger.info("Hello World!!" + finalI);
					countDownLatch.countDown();
				}
			});
		}


		countDownLatch.await();
		Thread.sleep(100);
		assertEquals(1, FileWriter.getOpenFileCount());
		LogExpress.shutdown().await();

		assertEquals(0, FileWriter.getOpenFileCount());
		String[] list = testLogDir.list();
		assertEquals(2, list.length);
		for(String fileName : list) {
			System.out.println(fileName);
		}

		assertTrue(list[0].endsWith("2024-01-02.log"));
		assertTrue(list[1].endsWith("2024-01-03.log"));
		deleteAllFile(testLogDir);


	}



	@Test
	public void duplicatedWriteTest() throws InterruptedException, IOException {
		File file = new File("testx.log");
		file.delete();
		LogExpress.shutdown().await();

		Configuration configuration = LogExpress.cloneConfiguration();
		WriterOption testWriter = configuration.newWriterOption("test");
		testWriter.setFile(file.getAbsolutePath());
		testWriter.clearWriterType();
		testWriter.addWriterType(WriterType.File);
		testWriter.setLinePattern("{level} {message}");
		LogExpress.updateConfig(configuration);


		configuration = LogExpress.cloneConfiguration();
		WriterOption withCaller = configuration.newWriterOption("test2");
		withCaller.setFile(file.getAbsolutePath());
		withCaller.clearWriterType();
		withCaller.addWriterType(WriterType.File);
		withCaller.setLinePattern("{level} <{caller-simple}> {message}");
		LogExpress.updateConfig(configuration);





		Logger logger = LogExpress.newLogger("test");
		logger.info("Hello World!!");
		logger.fatal("Hello World!!");
		logger.fatal("{0}{0}{0}{0}{0}{0}", "5");

		logger = LogExpress.newLogger( String.class,"test2");
		logger.info("Hello World!!");
		logger.fatal("Hello World!!");
		logger.fatal("{0}{0}{0}{0}{0}{0}", "5");

		logger = LogExpress.newLogger( LogExpressTest.class,"test");

		assertEquals(1,FileWriter.getOpenFileCount());

		ShutdownFuture future = LogExpress.shutdown();

		future.await();


		assertEquals(0,FileWriter.getOpenFileCount());

		String logs = readFileToString(file);
		String endData = "INFO Hello World!!\n" +
				"FATAL Hello World!!\n" +
				"FATAL 555555\n" +
				"INFO <String> Hello World!!\n" +
				"FATAL <String> Hello World!!\n" +
				"FATAL <String> 555555\n" +
				"INFO LOGExpress shutdown called.\n";

		//assertEquals(logs, endData);


		assertTrue(logs.endsWith(endData));

		file.delete();

		assertFalse(file.exists());


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
		final File testFile = new File("unlimitIntervalTest.log");
		testFile.delete();
		testFile.createNewFile();
		Configuration configuration = LogExpress.cloneConfiguration();
		configuration.setWorkerInterval(-1);
		WriterOption option = configuration.getWriterOptions()[0];
		option.clearWriterType();
		option.addWriterType(WriterType.File);
		option.setFile(testFile.getAbsolutePath());
		LogExpress.updateConfig(configuration);
		final CountDownLatch countDownLatch = new CountDownLatch(1);

		final AtomicReference<AssertionError> throwableAtomicReference = new AtomicReference<AssertionError>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileInputStream fileInputStream = new FileInputStream(testFile);
					InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String line = null;
					boolean countStart = false;
					long count = 0;
					while(testFile.exists()) {
						line = bufferedReader.readLine();
						if(line != null) {
							if(line.endsWith("0;") && !countStart) {
								countStart = true;

							}
							else if(countStart) {
								++count;
								try {
									assertTrue(line.endsWith(count + ";"));
								} catch (AssertionError e) {
									System.out.println("count : " + count);
									System.out.println("line : " + line);
									throwableAtomicReference.set(e);
									countDownLatch.countDown();
									throw e;
								}


							}
							System.out.println(line);
							if(line.endsWith("999;")) {
								break;
							}
						}
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
						}
					}

					try {
						fileInputStream.close();
						inputStreamReader.close();
						bufferedReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					countDownLatch.countDown();

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


		countDownLatch.await();

		if(throwableAtomicReference.get() != null) {
			throw throwableAtomicReference.get();
		}
		LogExpress.shutdown().await();
		testFile.delete();

		assertEquals(0, FileWriter.getOpenFileCount());

		assertFalse(testFile.exists());
	}

	@Before
	public void before() throws InterruptedException {
		LogExpress.shutdown().await();
	}



	@Test
	public void shutdownTest() throws InterruptedException, IOException {
		File file = new File("shutdownTest.log");
		file.delete();
		Configuration configuration = LogExpress.cloneConfiguration();
		int testCase = 1000000;
		configuration.setQueueSize(testCase / 10);
		long callLogTime = 0;
		long endFileWriteTime = 0;
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
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		future.addOnEndCallback(new Runnable() {
			@Override
			public void run() {
				atomicInteger.addAndGet(10);
				System.out.println("==================끝1====================" + atomicInteger.get());
			}
		});

		future.addOnEndCallback(new Runnable() {
			@Override
			public void run() {
				atomicInteger.addAndGet(22);
				System.out.println("==================끝2=====================" + atomicInteger.get());
			}
		});
		future.await();
		endFileWriteTime = System.currentTimeMillis() - startTime;

		System.out.println("callLogTime : " + callLogTime);
		System.out.println("endFileWriteTime : " + endFileWriteTime);

		assertEquals(32, atomicInteger.get());
        assertTrue(future.isEnd());
		String logs = readFileToString(file);
		assertEquals(testCase + 1, countLine(logs));

		assertTrue(LogExpress.isShutdown());

		LogExpress.info("Hello World!!");


		assertFalse(LogExpress.isShutdown());


		assertTrue(file.delete());
	}





	// java6 기반으로 파일을 읽어서 String 으로 변환하는 메서드.
	public static String readFileToString(File file) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 1024 * 1024 * 32);
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
