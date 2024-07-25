package com.hancomins.LogExpress.configuration;

import com.hancomins.LogExpress.InLogger;
import com.hancomins.LogExpress.LogExpress;
import com.hancomins.LogExpress.util.Files;

import java.io.*;

import java.util.ArrayList;

/**
 * LogExpress 설정을 관리하는 클래스입니다.
 * 설정이 닫힌 후에는 변경할 수 없습니다.
 * 설정이 열려있는지 확인하기 위하여 {@link #isClosed()} 메소드를 사용합니다.
 * 변경한 설정은 {@link LogExpress#updateConfig(Configuration)} 메소드를 사용하여 적용합니다.
 * @author Beom
 */
@SuppressWarnings("unused")
public final class Configuration implements Cloneable {
	
	private final static String DEFAULT_CONFIG_FILE = "log-express.ini";
	private final static int MIN_QUEUE_SIZE = 10;
	public final static String PROPERTIES_KEY_FILE = "LogExpress.configurationFile";
	public final static int DEFAULT_QUEUE_SIZE = 128000;
	public final static int DEFAULT_WRITER_WORKER_INTERVAL = 3000;

	private boolean isDaemonThread = false;
	private volatile boolean closed = false;

	private boolean autoShutdown = false;


	private ArrayList<WriterOption> writerOptionList = new ArrayList<WriterOption>();
	private int queueSize = DEFAULT_QUEUE_SIZE;

	private WriterOption defaultOption = null;
	private String defaultMarker = "";
	private boolean isDebug = false;
	private boolean debugFileLogEnabled = false;
	private boolean debugConsoleLogEnabled = false;


	private boolean isExistCheck = false;

	private boolean nonBlockingMode = true;
	private int writerWorkerInterval = DEFAULT_WRITER_WORKER_INTERVAL;

	private String staticVariableReplacedDefaultMarker = null;


	/**
	 * 모든 WriterOption을 초기화 합니다.
	 */
	public void clearWriters() {
		if(closed) return;
		writerOptionList.clear();
	}

	/**
	 * 설정이 닫혔는지 여부를 반환합니다.
	 * 닫히 설정은 변경할 수 없습니다.
	 * @return 설정이 닫혔는지 여부
	 */
	public boolean isClosed() {
		return closed;
	}


	/**
	 * 설정을 복제합니다.
	 * 닫힌 설정이라면 새로운 설정을 생성하여 반환합니다.
	 * @return 설정 복사본
	 */
    @Override
	public Configuration clone()  {
        Configuration configuration;
        try {
            configuration = (Configuration) super.clone();
        } catch (CloneNotSupportedException e) {
            configuration = new Configuration();
        }
		configuration.closed = false;
		configuration.isDaemonThread = isDaemonThread;
		configuration.queueSize = queueSize;
		configuration.defaultMarker = defaultMarker;
		configuration.autoShutdown = autoShutdown;
		configuration.writerWorkerInterval = writerWorkerInterval;
		configuration.writerOptionList = new ArrayList<WriterOption>();
		configuration.defaultOption = getDefaultOption().clone();
		configuration.nonBlockingMode = nonBlockingMode;
		for(int i = 0, n = writerOptionList.size(); i < n; ++i) {
			WriterOption writerOption = writerOptionList.get(i);
			if(writerOption == getDefaultOption()) {
				configuration.writerOptionList.add(configuration.defaultOption);
			} else {
				configuration.writerOptionList.add(writerOption.clone());
			}

		}
		configuration.staticVariableReplacedDefaultMarker = null;
		return configuration;
	}

	/**
	 * 설정을 ini 파일로부터 초기화합니다.
	 * @param path 설정 파일 경로. ini 파일 형식을 사용합니다.
	 * @throws IOException 설정 파일을 읽는 도중 오류가 발생했을 때
	 */
	Configuration(String path) throws IOException {
		File file = new File(path);
		loadFile(file);
	}

	/**
	 * 설정을 ini 파일로부터 초기화합니다.
	 * @param file 설정 파일. ini 파일 형식을 사용합니다.
	 * @throws IOException 설정 파일을 읽는 도중 오류가 발생했을 때
	 */
	Configuration(File file) throws IOException {
		loadFile(file);
	}

	/**
	 * 설정을 Reader로부터 읽어온 문자열 데이터로부터 초기화합니다.
	 * @param reader  설정 데이터를 읽는 Reader
	 * @throws IOException 설정 데이터를 읽는 도중 오류가 발생했을 때
	 */
	Configuration(Reader reader) throws IOException {
		ConfigurationParser.parse(reader, this);
	}


	/**
	 * 설정을 ini 파일로부터 초기화하고 생성하여 반환합니다.
	 * 주의!! 잘못된 설정 포맷을 사용하여도 예외가 발생하지 않으며, 기본 설정 값으로 초기화됩니다.
	 * @param file 설정 파일. ini 파일 형식을 사용합니다.
	 * @return 설정 객체
	 * @throws IOException 설정 파일을 읽는 도중 오류가 발생했을 때
	 */
	public static Configuration newConfiguration(File file) throws IOException {
		return new Configuration(file);
	}

	/**
	 * 설정을 Reader로부터 읽어온 문자열 데이터로부터 초기화하고 생성하여 반환합니다.
	 * 주의!! 잘못된 설정 포맷을 사용하여도 예외가 발생하지 않으며, 기본 설정 값으로 초기화됩니다.
	 * @param reader 설정 데이터를 읽는 Reader
	 * @return 설정 객체
	 * @throws IOException 설정 데이터를 읽는 도중 오류가 발생했을 때
	 */
	
	public static Configuration newConfiguration(Reader reader) throws IOException {
		return new Configuration(reader);
	}


	/**
	 * 설정을 ini 파일로부터 초기화하고 생성하여 반환합니다.
	 * 주의!! 잘못된 설정 포맷을 사용하여도 예외가 발생하지 않으며, 기본 설정 값으로 초기화됩니다.
	 * @param path 설정 파일 경로. ini 파일 형식을 사용합니다.
	 * @return 설정 객체
	 * @throws IOException 설정 파일을 읽는 도중 오류가 발생했을 때
	 */
	public static Configuration newConfiguration(String path) throws IOException {
		return new Configuration(path);
	}

	/**
	 * 기본 설정 값을 사용하여 설정 객체를 생성하여 반환합니다.
	 * @return 설정 객체
	 */
	public static Configuration newConfiguration() {
		return new Configuration();
	}


	/**
	 * 기본 설정 파일을 사용하여 설정 객체를 생성하여 반환합니다.
	 * 주의! 설정 파일이 없거나 읽을 수 없을 때는 기본 설정 값으로 초기화됩니다.
	 * @return 설정 객체
	 */
	public static Configuration fromDefaultConfigurationFile() {
		Configuration configuration = new Configuration();
		String configPath = System.getProperty(PROPERTIES_KEY_FILE);
		 if(configPath != null) {
			 configPath = configPath.replace("/", File.separator);
		 }
		 if(configPath == null ||  !new File(configPath).canRead()) {
			 configPath = DEFAULT_CONFIG_FILE;
		 }

		 if(configuration.loadFromResources()) {
		 	 return configuration;
		 }

		File configFile = new File(configPath);
		if(configFile.isFile() && configFile.canRead()) {
			try {
				configuration.loadFile(configFile);
				if(InLogger.isEnabled()) {
					InLogger.INFO(configPath + "` read Success...");
				}
				return configuration;
			} catch (IOException e) {
				InLogger.ERROR(configPath + "` read Fail...", e);
			}
		}
		return configuration;
	}

	
	private Configuration() {}



	boolean loadFromResources()  {

		InputStream inputStream = LogExpress.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE);
		if(inputStream == null) return false;
		try {
			ConfigurationParser.parse(new InputStreamReader(inputStream), this);
			inputStream.close();
			if(InLogger.isEnabled()) {
				InLogger.INFO("read '" + DEFAULT_CONFIG_FILE + "' from resources, Success...");
			}
			return true;
		} catch (IOException e) {
			InLogger.ERROR("read '" +  DEFAULT_CONFIG_FILE + "' from resources, Fail...", e);
			return false;
		}
	}
	
	
	private void loadFile(File configFile) throws IOException {
		 byte[] buffer = Files.readAllBytes(configFile);
		 ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		 FileReader fileReader = new FileReader(configFile);
		 ConfigurationParser.parse(fileReader, this);
		 fileReader.close();;
		 bais.close();
	}

	/**
	 * 로그 Writer 스레드가 데몬 스레드인지 여부를 설정합니다.
	 * @param isDaemonThread 데몬 스레드 여부
	 */
	public void setDaemonThread(boolean isDaemonThread) {
		this.isDaemonThread = isDaemonThread;
	}

	/**
	 * 로그 Writer 스레드를 데몬 스레드로 설정했는지 여부를 반환합니다.
	 * @return 데몬 스레드 설정 여부
	 */
	public boolean isDaemonThread() {
		return isDaemonThread;
	}


	/**
	 * 기본 마커 이름을 설정합니다.
	 * @param defaultMarker 기본 마커 이름
	 */
	public void setDefaultMarker(String defaultMarker) {
		if(closed) return;
		this.defaultMarker = defaultMarker;
	}

	/**
	 * tid 1번을 갖는 main 스레드가 종료되면 LogExpress를 종료할지를 설정합니다.
	 * 기본 값은 false입니다.
	 * @param enable 종료 여부
	 */
	public void setAutoShutdown(boolean enable) {
		if(closed) return;
		autoShutdown = enable;
	}

	/**
	 * non blocking 되는 큐를 사용할지 여부를 설정합니다.
	 * 극히 일부 환경에서 문제 발생시 false로 설정하여 synchronized 된 큐를 사용하여 문제를 해결할 수 있습니다.
	 * 기본 값은 true입니다.
	 * @param enable non blocking 큐 사용 여부
	 */
	public void setNonBlockingMode(boolean enable) {
		if(closed) return;
		nonBlockingMode = enable;
	}

	/**
	 * 설정한 non blocking 큐 사용 여부를 반환합니다.
	 * @return non blocking 큐 사용 여부
	 */
	public boolean isNonBlockingQueue() {
		return nonBlockingMode;
	}

	/**
	 * 설정한 tid 1번을 갖는 main 스레드가 종료되면 LogExpress를 종료할지 여부를 반환합니다.
	 * @return 종료 여부
	 */
	public boolean isAutoShutdown() {
		return autoShutdown;
	}

	/**
	 * 설정된 기본 마커 이름을 반환합니다.
	 * @return 기본 마커 이름
	 */
	public String getDefaultMarker() {
		if(!closed) {
			return defaultMarker;
		}
		if(staticVariableReplacedDefaultMarker == null) {
			staticVariableReplacedDefaultMarker = StaticVariableReplacer.replace(defaultMarker);
		}
		return staticVariableReplacedDefaultMarker;

	}
	
	
	private int getIndexOfWriterConfigure(String marker) {
		for(int i = 0, n = writerOptionList.size(); i < n; ++i) {
			WriterOption configure = writerOptionList.get(i);
			String[] markers = configure.getMarkers();
            //noinspection ForLoopReplaceableByForEach
            for(int mi = 0; mi < markers.length; ++mi) {
				if(markers[mi].equals(marker)) {
					return i;
				}
			}
		}
		return -1;
	}


	/**
	 * 설정된 기본 WriterOption을 반환합니다. 설정된 기본 WriterOption 이 없다면 null 을 반환합니다.
	 * @return 설정된 기본 WriterOption
	 */
	public WriterOption getDefaultWriterOption() {
		if(defaultMarker == null || defaultMarker.isEmpty()) {
			if(!writerOptionList.isEmpty()) return writerOptionList.get(0);
			else {
				return null;
			}
		}
		WriterOption configure = getWriterOption(defaultMarker);
		if(configure != null) return configure;
		else if(!writerOptionList.isEmpty()) return writerOptionList.get(0);
		return null;
	}

	/**
	 * WriterWorker 스레드의 주기를 설정합니다.
	 * WriterWorker 스레드가 주기적으로 큐를 확인하여 로그를 기록합니다.
	 * 주기가 0이하이면 큐가 비어있을시 WriterWorker 스레드는 항상 대기합니다.
	 * {@link #setAutoShutdown(boolean)} 메서드를 사용한다면 반드시 주기를 1000ms 이상으로 설정해야 합니다.
	 * 기본 값은 3000ms 입니다.
	 * @param interval ms 단위의 주기(interval)
	 */
	public void setWorkerInterval(int interval) {
		if(closed) return;
		writerWorkerInterval = interval <= 0 ? Integer.MAX_VALUE : interval;
	}

	/**
	 * 설정된 WriterWorker 스레드의 주기를 반환합니다.
	 * @return ms 단위의 주기(interval)
	 */
	public int getWorkerInterval() {
		return writerWorkerInterval;
	}

	/**
	 * marker 에 해당하는 WriterOption을 찾아서 반환합니다.
	 * 찾을 수 없다면 null을 반환합니다.
	 * @param marker 마커 이름
	 * @return 인자로 주어진 마커 이름에 해당하는 WriterOption. 없다면 null
	 */
	public WriterOption getWriterOption(String marker) {
		int idx = getIndexOfWriterConfigure(marker);
		if(idx > -1) {
			return writerOptionList.get(idx);
		}
		return null;
	}


	/**
	 * marker에 해당하는 WriterOption을 생성하여 반환합니다.
	 * 만약, 이미 설정된 마커 이름에 해당하는 WriterOption객체가 있다면 이미 설정된 객체를 반환합니다.
	 * @param marker 마커 이름
	 * @return 생성된 WriterOption 객체
	 */
	public WriterOption newWriterOption(String marker) {
		int idx = getIndexOfWriterConfigure(marker);
		if(idx > -1) {
            return writerOptionList.get(idx);
		}
		WriterOption option = new WriterOption();
		option.addMarker(marker);
		writerOptionList.add(option);
		return option;
	}


	/**
	 * 설정된 모든 WriterOption을 반환합니다.
	 * @return 설정된 WriterOption 배열
	 */
	public WriterOption[] getWriterOptions() {
		return writerOptionList.toArray(new WriterOption[0]);
	}


	/**
	 * 로그 큐 사이즈를 설정합니다. 만약, 큐가 가득차면 로그는 동기 방식으로 처리됩니다.
	 * 큐 사이즈가 10보다 작으면 10으로 설정됩니다.
	 * 기본 값은 128000입니다.
	 * @param size 큐 사이즈
	 */
	public void setQueueSize(int size) {
		if(closed) return;
		if(size < 10) {
			size = MIN_QUEUE_SIZE;
		}
		queueSize = size;
	}

	/**
	 * 설정된 로그 큐 사이즈를 반환합니다.
	 * @return 큐 사이즈
	 */
	public int getQueueSize() {
		return queueSize;
	}


	/**
	 * 설정된 기본 WriterOption을 반환합니다. 없다면 null 을 반환합니다.
	 * @return 설정된 기본 WriterOption
	 */
	public WriterOption getDefaultOption() {
		return defaultOption;
	}


	/**
	 * 디버그 모드 사용 여부를 설정합니다.
	 * 디버그 모드에서 LogExpress 내부의 로그가 출력됩니다.
	 * 설정 문제나 예측이 어려운 문제를 확인할 때 사용할 수 있습니다.
	 * 기본 값은 false입니다.
	 * @param debug 디버그 모드 사용 여부
	 */
	public void setDebugMode(boolean debug) {
		if(closed) return;
		isDebug = debug;
	}

	/**
	 * 디버그 모드에서 파일 로그를 사용할지 여부를 설정합니다.
	 * 기본 값은 false입니다.
	 * @param enable 파일 로그 사용 여부
	 */
	public void enableFileLogInDebugMode(boolean enable) {
		if(closed) return;
		debugFileLogEnabled = enable;
	}

	/**
	 * 디버그 모드에서 콘솔 로그를 사용할지 여부를 설정합니다.
	 * 기본 값은 false입니다.
	 * @param enable 콘솔 로그 사용 여부
	 */
	public void enableConsoleLogInDebugMode(boolean enable) {
		if(closed) return;
		debugConsoleLogEnabled = enable;
	}

	/**
	 * 로그 파일
	 * @param enable 로그 파일 사용 여부
	 */
	public void setFileExistCheck(boolean enable) {
		if(closed) return;
		isExistCheck = enable;
	}
	
	public boolean isFileExistCheck() {
		return isExistCheck;
	}

	public boolean isDebug() {
		return isDebug;
	}


	public boolean isDebugFileLogEnabled() {
		return debugFileLogEnabled;
	}


	public boolean isDebugConsoleLogEnabled() {
		return debugConsoleLogEnabled;
	}


	/**
	 * 설정을 닫습니다. 설정이 닫힌 후에는 변경할 수 없습니다.
	 * {@link  LogExpress#updateConfig(Configuration)} 메서드를 호출허였을 close() 메서드가 호출되어 설정이 닫힙니다.
	 * @return 설정 객체
	 */
	public synchronized Configuration close() {
		if(closed) return this;
		closed = true;
		// 설정값이 아무것도 없을 때.
		if(writerOptionList.isEmpty()) {
			if(defaultMarker == null || defaultMarker.isEmpty()) {
				defaultMarker = "default";
			}
			WriterOption option = new WriterOption();
			option.addMarker(defaultMarker);
			defaultOption = option;
			writerOptionList.add(option);
			InLogger.WARN("WriterOption is not defined in the configuration. Initializing WriterOption with default values.", true);
		} else if(defaultMarker == null || defaultMarker.isEmpty()) {
			defaultOption = writerOptionList.get(0);
			defaultOption.addMarker(defaultMarker);
			String[] markers = defaultOption.getMarkers();
			if(markers!= null && markers.length > 0 ) {
				defaultMarker = defaultOption.getMarkers()[0];
			}  else {
				defaultMarker = "default";
				InLogger.WARN("The default marker is not defined. Using 'default' as the default marker name.", true);
			}

		} else {
			int index =  getIndexOfWriterConfigure(defaultMarker);
			if(index < 0) {
				defaultOption = writerOptionList.get(0);
				defaultOption.addMarker(defaultMarker);
			} else {
				defaultOption = writerOptionList.get(index);
			}
		}

        for(int i = 0, n = writerOptionList.size(); i < n; ++i) {
			WriterOption option = writerOptionList.get(i);
			option.close();
		}
		return this;
	}

	/**
	 * 설정을 문자열로 변환합니다.
	 * ini 파일 형식으로 변환됩니다.
	 * @return 설정 문자열
	 */
	@Override
	public String toString() {
		return ConfigurationParser.toString(this);
	};

}
