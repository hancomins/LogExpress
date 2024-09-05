package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.InLogger;
import com.hancomins.logexpress.Level;
import com.hancomins.logexpress.LogExpress;
import com.hancomins.logexpress.util.Files;

import java.io.*;
import java.util.ArrayList;

/**
 * logexpress 설정을 관리하는 클래스입니다.<br>
 * 설정이 닫힌 후에는 변경할 수 없습니다.<br>
 * 설정이 열려있는지 확인하려면 {@link #isClosed()} 메서드를 사용합니다.<br>
 * 변경된 설정은 {@link LogExpress#updateConfig(Configuration)} 메서드를 통해 적용할 수 있습니다.<br>
 * This class manages the logexpress configuration.<br>
 * Once closed, the configuration cannot be changed.<br>
 * Use {@link #isClosed()} method to check if the configuration is closed.<br>
 * Apply changes using the {@link LogExpress#updateConfig(Configuration)} method.
 *
 * @author : Beom
 */
@SuppressWarnings("unused")
public final class Configuration implements Cloneable {


	private final static Level DEFAULT_LEVEL = Level.TRACE;

	private final static String DEFAULT_CONFIG_FILE = "log-express.ini";
	private final static int MIN_QUEUE_SIZE = 10;
	public final static String PROPERTIES_KEY_FILE = "logexpress.configurationFile";
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
	private Level defaultLevel = DEFAULT_LEVEL;

	private boolean nonBlockingMode = true;
	private int writerWorkerInterval = DEFAULT_WRITER_WORKER_INTERVAL;

	private String staticVariableReplacedDefaultMarker = null;
	private final ColorOption defaultColorOption = new ColorOption();


	/**
	 * 모든 WriterOption을 초기화합니다.<br>
	 * Initializes all WriterOptions.
	 */
	public void clearWriters() {
		if(this.closed) return;
		this.writerOptionList.clear();
	}

	/**
	 * 설정이 닫혔는지 여부를 반환합니다.<br>
	 * 닫힌 설정은 변경할 수 없습니다.<br>
	 * Returns whether the configuration is closed.<br>
	 * A closed configuration cannot be changed.
	 *
	 * @return 설정이 닫혔는지 여부<br>
	 *         whether the configuration is closed
	 */
	public boolean isClosed() {
		return this.closed;
	}

	/**
	 * 기본 컬러 옵션의 인스턴스를 가져옵니다.<br>
	 * Returns the instance of default color option.
	 * {@link ColorOption}
	 * @return 컬러 옵션<br>
	 */
	public ColorOption defaultColorOption() {
		return this.defaultColorOption;
	}

	/**
	 * 설정을 복제합니다.<br>
	 * 닫힌 설정이라면 새로운 설정을 생성하여 반환합니다.<br>
	 * Clones the configuration.<br>
	 * If the configuration is closed, a new configuration is created and returned.
	 *
	 * @return 설정 복사본<br>
	 *         cloned configuration
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
		configuration.isDaemonThread = this.isDaemonThread;
		configuration.queueSize = this.queueSize;
		configuration.defaultMarker = this.defaultMarker;
		configuration.autoShutdown = this.autoShutdown;
		configuration.writerWorkerInterval = this.writerWorkerInterval;
		configuration.writerOptionList = new ArrayList<WriterOption>();
		configuration.defaultOption = this.getDefaultOption().clone();
		configuration.nonBlockingMode = this.nonBlockingMode;
		for(int i = 0, n = this.writerOptionList.size(); i < n; ++i) {
			WriterOption writerOption = this.writerOptionList.get(i);
			if(writerOption == this.getDefaultOption()) {
				configuration.writerOptionList.add(configuration.defaultOption);
			} else {
				configuration.writerOptionList.add(writerOption.clone());
			}
		}
		configuration.staticVariableReplacedDefaultMarker = null;
		return configuration;
	}

	/**
	 * 기본 로그 Level을 설정합니다.<br>
	 * WriterOption에 설정된 Level이 없으면 기본 Level이 사용됩니다.<br>
	 * WriterOption의 Level 은 기본 Level보다 낮을 수 없습니다.<br>
	 * @param level 기본 Level<br>
	 * @return 설정 객체<br>
	 */
	public Configuration setDefaultLevel(Level level) {
		if(this.closed) return this;
		this.defaultLevel = level;
		return this;
	}

	/**
	 * 설정을 ini 파일로부터 초기화합니다.<br>
	 * Initializes the configuration from an ini file.
	 *
	 * @param path 설정 파일 경로. ini 파일 형식을 사용합니다.<br>
	 *             path to the configuration file, in ini format
	 * @throws IOException 설정 파일을 읽는 도중 오류가 발생했을 때<br>
	 *                     if an error occurs while reading the configuration file
	 */
	Configuration(String path) throws IOException {
		File file = new File(path);
		this.loadFile(file);
	}

	/**
	 * 설정을 ini 파일로부터 초기화합니다.<br>
	 * Initializes the configuration from an ini file.
	 *
	 * @param file 설정 파일. ini 파일 형식을 사용합니다.<br>
	 *             the configuration file, in ini format
	 * @throws IOException 설정 파일을 읽는 도중 오류가 발생했을 때<br>
	 *                     if an error occurs while reading the configuration file
	 */
	Configuration(File file) throws IOException {
		this.loadFile(file);
	}

	/**
	 * 설정을 Reader로부터 읽어온 문자열 데이터로 초기화합니다.<br>
	 * Initializes the configuration from string data read from a Reader.
	 *
	 * @param reader 설정 데이터를 읽는 Reader<br>
	 *               Reader to read the configuration data
	 * @throws IOException 설정 데이터를 읽는 도중 오류가 발생했을 때<br>
	 *                     if an error occurs while reading the configuration data
	 */
	Configuration(Reader reader) throws IOException {
		ConfigurationParser.parse(reader, this);
	}

	/**
	 * 설정을 ini 파일로부터 초기화하고 생성하여 반환합니다.<br>
	 * 주의! 잘못된 설정 포맷을 사용하여도 예외가 발생하지 않으며, 기본 설정 값으로 초기화됩니다.<br>
	 * Initializes and returns the configuration from an ini file.<br>
	 * Note: If an incorrect configuration format is used, it will be initialized with default values without throwing an exception.
	 *
	 * @param file 설정 파일. ini 파일 형식을 사용합니다.<br>
	 *             the configuration file, in ini format
	 * @return 설정 객체<br>
	 *         the configuration object
	 * @throws IOException 설정 파일을 읽는 도중 오류가 발생했을 때<br>
	 *                     if an error occurs while reading the configuration file
	 */
	public static Configuration newConfiguration(File file) throws IOException {
		return new Configuration(file);
	}

	/**
	 * 설정을 Reader로부터 읽어온 문자열 데이터로 초기화하고 생성하여 반환합니다.<br>
	 * 주의! 잘못된 설정 포맷을 사용하여도 예외가 발생하지 않으며, 기본 설정 값으로 초기화됩니다.<br>
	 * Initializes and returns the configuration from string data read from a Reader.<br>
	 * Note: If an incorrect configuration format is used, it will be initialized with default values without throwing an exception.
	 *
	 * @param reader 설정 데이터를 읽는 Reader<br>
	 *               Reader to read the configuration data
	 * @return 설정 객체<br>
	 *         the configuration object
	 * @throws IOException 설정 데이터를 읽는 도중 오류가 발생했을 때<br>
	 *                     if an error occurs while reading the configuration data
	 */
	public static Configuration newConfiguration(Reader reader) throws IOException {
		return new Configuration(reader);
	}

	/**
	 * 설정을 ini 파일로부터 초기화하고 생성하여 반환합니다.<br>
	 * 주의! 잘못된 설정 포맷을 사용하여도 예외가 발생하지 않으며, 기본 설정 값으로 초기화됩니다.<br>
	 * Initializes and returns the configuration from an ini file.<br>
	 * Note: If an incorrect configuration format is used, it will be initialized with default values without throwing an exception.
	 *
	 * @param path 설정 파일 경로. ini 파일 형식을 사용합니다.<br>
	 *             path to the configuration file, in ini format
	 * @return 설정 객체<br>
	 *         the configuration object
	 * @throws IOException 설정 파일을 읽는 도중 오류가 발생했을 때<br>
	 *                     if an error occurs while reading the configuration file
	 */
	public static Configuration newConfiguration(String path) throws IOException {
		return new Configuration(path);
	}

	/**
	 * 기본 설정 값을 사용하여 설정 객체를 생성하여 반환합니다.<br>
	 * Creates and returns the configuration object with default settings.
	 *
	 * @return 설정 객체<br>
	 *         the configuration object
	 */
	public static Configuration newConfiguration() {
		return new Configuration();
	}

	/**
	 * 기본 설정 파일을 사용하여 설정 객체를 생성하여 반환합니다.<br>
	 * 주의! 설정 파일이 없거나 읽을 수 없을 때는 기본 설정 값으로 초기화됩니다.<br>
	 * Creates and returns the configuration object using the default configuration file.<br>
	 * Note: If the configuration file is missing or unreadable, it initializes with default values.
	 *
	 * @return 설정 객체<br>
	 *         the configuration object
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

	Configuration() {}

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
		fileReader.close();
		bais.close();
	}

	/**
	 * 로그 Writer 스레드가 데몬 스레드인지 여부를 설정합니다.<br>
	 * Sets whether the log Writer thread is a daemon thread.
	 *
	 * @param isDaemonThread 데몬 스레드 여부<br>
	 *                       whether it is a daemon thread
	 */
	public void setDaemonThread(boolean isDaemonThread) {
		this.isDaemonThread = isDaemonThread;
	}

	/**
	 * 로그 Writer 스레드를 데몬 스레드로 설정했는지 여부를 반환합니다.<br>
	 * Returns whether the log Writer thread is set as a daemon thread.
	 *
	 * @return 데몬 스레드 설정 여부<br>
	 *         whether the daemon thread is set
	 */
	public boolean isDaemonThread() {
		return this.isDaemonThread;
	}

	/**
	 * 기본 마커 이름을 설정합니다.<br>
	 * Sets the default marker name.
	 *
	 * @param defaultMarker 기본 마커 이름<br>
	 *                      default marker name
	 */
	public void setDefaultMarker(String defaultMarker) {
		if(this.closed) return;
		this.defaultMarker = defaultMarker;
	}

	/**
	 * tid 1번을 갖는 main 스레드가 종료되면 LogExpress를 종료할지를 설정합니다.<br>
	 * 기본 값은 false입니다.<br>
	 * Sets whether to shut down logexpress when the main thread with tid 1 terminates.<br>
	 * The default value is false.
	 *
	 * @param enable 종료 여부<br>
	 *               whether to enable shutdown
	 */
	public void setAutoShutdown(boolean enable) {
		if(this.closed) return;
		this.autoShutdown = enable;
	}

	/**
	 * non-blocking 큐를 사용할지 여부를 설정합니다.<br>
	 * 극히 일부 환경에서 문제 발생 시 false로 설정하여 synchronized 된 큐를 사용하여 문제를 해결할 수 있습니다.<br>
	 * 기본 값은 true입니다.<br>
	 * Sets whether to use a non-blocking queue.<br>
	 * In rare cases, setting it to false can solve issues by using a synchronized queue.<br>
	 * The default value is true.
	 *
	 * @param enable non-blocking 큐 사용 여부<br>
	 *               whether to use non-blocking queue
	 */
	public void setNonBlockingMode(boolean enable) {
		if(this.closed) return;
		this.nonBlockingMode = enable;
	}

	/**
	 * 설정된 non-blocking 큐 사용 여부를 반환합니다.<br>
	 * Returns whether the non-blocking queue is enabled.
	 *
	 * @return non-blocking 큐 사용 여부<br>
	 *         whether the non-blocking queue is enabled
	 */
	public boolean isNonBlockingQueue() {
		return this.nonBlockingMode;
	}

	/**
	 * 설정된 tid 1번을 갖는 main 스레드가 종료되면 LogExpress를 종료할지 여부를 반환합니다.<br>
	 * Returns whether logexpress should shut down when the main thread with tid 1 terminates.
	 *
	 * @return 종료 여부<br>
	 *         whether to shut down
	 */
	public boolean isAutoShutdown() {
		return this.autoShutdown;
	}

	/**
	 * 설정된 기본 마커 이름을 반환합니다.<br>
	 * Returns the default marker name.
	 *
	 * @return 기본 마커 이름<br>
	 *         default marker name
	 */
	public String getDefaultMarker() {
		if(!this.closed) {
			return this.defaultMarker;
		}
		if(this.staticVariableReplacedDefaultMarker == null) {
			this.staticVariableReplacedDefaultMarker = StaticVariableReplacer.replace(this.defaultMarker);
		}
		return this.staticVariableReplacedDefaultMarker;
	}

	@SuppressWarnings("ForLoopReplaceableByForEach")
    private int getIndexOfWriterConfigure(String marker) {
		for(int i = 0, n = this.writerOptionList.size(); i < n; ++i) {
			WriterOption configure = this.writerOptionList.get(i);
			String[] markers = configure.getMarkers();
			for(int mi = 0; mi < markers.length; ++mi) {
				if(markers[mi].equals(marker)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 설정된 기본 WriterOption을 반환합니다. 설정된 기본 WriterOption이 없다면 null을 반환합니다.<br>
	 * Returns the configured default WriterOption. If not configured, returns null.
	 *
	 * @return 설정된 기본 WriterOption<br>
	 *         default WriterOption
	 */
	public WriterOption getDefaultWriterOption() {
		if(this.defaultMarker == null || this.defaultMarker.isEmpty()) {
			if(!this.writerOptionList.isEmpty()) return this.writerOptionList.get(0);
			else {
				return null;
			}
		}
		WriterOption configure = this.getWriterOption(this.defaultMarker);
		if(configure != null) return configure;
		else if(!this.writerOptionList.isEmpty()) return this.writerOptionList.get(0);
		return null;
	}

	public Level getDefaultLevel() {
		return this.defaultLevel;
	}

	/**
	 * WriterWorker 스레드의 주기를 설정합니다.<br>
	 * WriterWorker 스레드가 주기적으로 큐를 확인하여 로그를 기록합니다.<br>
	 * 주기가 0 이하이면 큐가 비어있을 시 WriterWorker 스레드는 항상 대기합니다.<br>
	 * {@link #setAutoShutdown(boolean)} 메서드를 사용한다면 반드시 주기를 1000ms 이상으로 설정해야 합니다.<br>
	 * 기본 값은 3000ms입니다.<br>
	 * Sets the interval for the WriterWorker thread.<br>
	 * The WriterWorker thread periodically checks the queue to log messages.<br>
	 * If the interval is 0 or less, the WriterWorker thread always waits if the queue is empty.<br>
	 * When using the {@link #setAutoShutdown(boolean)} method, the interval must be set to 1000ms or more.<br>
	 * The default value is 3000ms.
	 *
	 * @param interval ms 단위의 주기<br>
	 *                 interval in milliseconds
	 */
	public void setWorkerInterval(int interval) {
		if(this.closed) return;
		this.writerWorkerInterval = interval <= 0 ? Integer.MAX_VALUE : interval;
	}

	/**
	 * 설정된 WriterWorker 스레드의 주기를 반환합니다.<br>
	 * Returns the configured interval for the WriterWorker thread.
	 *
	 * @return ms 단위의 주기<br>
	 *         interval in milliseconds
	 */
	public int getWorkerInterval() {
		return this.writerWorkerInterval;
	}

	/**
	 * 마커에 해당하는 WriterOption을 찾아서 반환합니다.<br>
	 * 찾을 수 없다면 null을 반환합니다.<br>
	 * Finds and returns the WriterOption for the given marker.<br>
	 * Returns null if not found.
	 *
	 * @param marker 마커 이름<br>
	 *               marker name
	 * @return 인자로 주어진 마커 이름에 해당하는 WriterOption. 없다면 null<br>
	 *         WriterOption for the given marker name. Null if not found.
	 */
	public WriterOption getWriterOption(String marker) {
		int idx = this.getIndexOfWriterConfigure(marker);
		if(idx > -1) {
			return this.writerOptionList.get(idx);
		}
		return null;
	}

	/**
	 * 마커에 해당하는 WriterOption을 생성하여 반환합니다.<br>
	 * 만약, 이미 설정된 마커 이름에 해당하는 WriterOption 객체가 있다면 이미 설정된 객체를 반환합니다.<br>
	 * Creates and returns a WriterOption for the given marker.<br>
	 * If a WriterOption object with the given marker name is already configured, returns the existing object.
	 *
	 * @param marker 마커 이름<br>
	 *               marker name
	 * @return 생성된 WriterOption 객체<br>
	 *         created WriterOption object
	 */
	public WriterOption newWriterOption(String marker) {
		int idx = this.getIndexOfWriterConfigure(marker);
		if(idx > -1) {
			return this.writerOptionList.get(idx);
		}
		WriterOption option = new WriterOption();
		option.setColorOption(this.defaultColorOption.clone());
		option.setLevel(this.defaultLevel);
		option.addMarker(marker);
		this.writerOptionList.add(option);
		return option;
	}

	/**
	 * 설정된 모든 WriterOption을 반환합니다.<br>
	 * Returns all configured WriterOptions.
	 *
	 * @return 설정된 WriterOption 배열<br>
	 *         array of configured WriterOptions
	 */
	public WriterOption[] getWriterOptions() {
		return this.writerOptionList.toArray(new WriterOption[0]);
	}

	/**
	 * 로그 큐 사이즈를 설정합니다. 만약 큐가 가득 차면 로그는 동기 방식으로 처리됩니다.<br>
	 * 큐 사이즈가 10보다 작으면 10으로 설정됩니다.<br>
	 * 기본 값은 128000입니다.<br>
	 * Sets the log queue size. If the queue is full, logs are processed synchronously.<br>
	 * If the queue size is less than 10, it is set to 10.<br>
	 * The default value is 128000.
	 *
	 * @param size 큐 사이즈<br>
	 *             queue size
	 */
	public void setQueueSize(int size) {
		if(this.closed) return;
		if(size < 10) {
			size = MIN_QUEUE_SIZE;
		}
		this.queueSize = size;
	}

	/**
	 * 설정된 로그 큐 사이즈를 반환합니다.<br>
	 * Returns the configured log queue size.
	 *
	 * @return 큐 사이즈<br>
	 *         queue size
	 */
	public int getQueueSize() {
		return this.queueSize;
	}

	/**
	 * 설정된 기본 WriterOption을 반환합니다. 없다면 null을 반환합니다.<br>
	 * Returns the configured default WriterOption. Null if not available.
	 *
	 * @return 설정된 기본 WriterOption<br>
	 *         configured default WriterOption
	 *
	 * @deprecated Deprecated. Use {@link #getDefaultWriterOption()} instead.
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
	public WriterOption getDefaultOption() {
		return this.defaultOption;
	}

	/**
	 * 디버그 모드 사용 여부를 설정합니다.<br>
	 * 디버그 모드에서 logexpress 내부의 로그가 출력됩니다.<br>
	 * 설정 문제나 예측이 어려운 문제를 확인할 때 사용할 수 있습니다.<br>
	 * 기본 값은 false입니다.<br>
	 * Sets whether to enable debug mode.<br>
	 * In debug mode, internal logs of logexpress are printed.<br>
	 * This can be useful for diagnosing configuration issues or unpredictable problems.<br>
	 * The default value is false.
	 *
	 * @param debug 디버그 모드 사용 여부<br>
	 *              whether to enable debug mode
	 */
	public void setDebugMode(boolean debug) {
		if(this.closed) return;
		this.isDebug = debug;
	}

	/**
	 * 디버그 모드에서 파일 로그를 사용할지 여부를 설정합니다.<br>
	 * 기본 값은 false입니다.<br>
	 * Sets whether to enable file logging in debug mode.<br>
	 * The default value is false.
	 *
	 * @param enable 파일 로그 사용 여부<br>
	 *               whether to enable file logging
	 */
	public void enableFileLogInDebugMode(boolean enable) {
		if(this.closed) return;
		this.debugFileLogEnabled = enable;
	}

	/**
	 * 디버그 모드에서 콘솔 로그를 사용할지 여부를 설정합니다.<br>
	 * 기본 값은 false입니다.<br>
	 * Sets whether to enable console logging in debug mode.<br>
	 * The default value is false.
	 *
	 * @param enable 콘솔 로그 사용 여부<br>
	 *               whether to enable console logging
	 */
	public void enableConsoleLogInDebugMode(boolean enable) {
		if(this.closed) return;
		this.debugConsoleLogEnabled = enable;
	}

	/**
	 * 로그 파일 존재 여부 확인을 설정합니다.<br>
	 * Sets whether to check the existence of log files.
	 *
	 * @param enable 로그 파일 사용 여부<br>
	 *               whether to enable log file check
	 */
	public void setFileExistCheck(boolean enable) {
		if(this.closed) return;
		this.isExistCheck = enable;
	}

	public boolean isFileExistCheck() {
		return this.isExistCheck;
	}

	public boolean isDebug() {
		return this.isDebug;
	}

	public boolean isDebugFileLogEnabled() {
		return this.debugFileLogEnabled;
	}

	public boolean isDebugConsoleLogEnabled() {
		return this.debugConsoleLogEnabled;
	}

	/**
	 * 설정을 닫습니다. 설정이 닫힌 후에는 변경할 수 없습니다.<br>
	 * {@link  LogExpress#updateConfig(Configuration)} 메서드를 호출하였을 때 close() 메서드가 호출되어 설정이 닫힙니다.<br>
	 * Closes the configuration. After closing, the configuration cannot be changed.<br>
	 * The close() method is called when the {@link LogExpress#updateConfig(Configuration)} method is invoked.
	 *
	 * @return 설정 객체<br>
	 *         the configuration object
	 */
	public synchronized Configuration close() {
		if(this.closed) return this;
		this.closed = true;
		// 설정값이 아무것도 없을 때.
		if(this.writerOptionList.isEmpty()) {
			if(this.defaultMarker == null || this.defaultMarker.isEmpty()) {
				this.defaultMarker = "default";
			}
			WriterOption option = new WriterOption();
			option.addMarker(this.defaultMarker);
			this.defaultOption = option;
			this.writerOptionList.add(option);
			InLogger.WARN("WriterOption is not defined in the configuration. Initializing WriterOption with default values.", true);
		} else if(this.defaultMarker == null || this.defaultMarker.isEmpty()) {
			this.defaultOption = this.writerOptionList.get(0);
			this.defaultOption.addMarker(this.defaultMarker);
			String[] markers = this.defaultOption.getMarkers();
			if(markers != null && markers.length > 0 ) {
				this.defaultMarker = this.defaultOption.getMarkers()[0];
			}  else {
				this.defaultMarker = "default";
				InLogger.WARN("The default marker is not defined. Using 'default' as the default marker name.", true);
			}
		} else {
			int index = this.getIndexOfWriterConfigure(this.defaultMarker);
			if(index < 0) {
				this.defaultOption = this.writerOptionList.get(0);
				this.defaultOption.addMarker(this.defaultMarker);
			} else {
				this.defaultOption = this.writerOptionList.get(index);
			}
		}

        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, n = this.writerOptionList.size(); i < n; ++i) {
			WriterOption option = this.writerOptionList.get(i);
			option.close();
		}
		return this;
	}

	/**
	 * 설정을 문자열로 변환합니다.<br>
	 * ini 파일 형식으로 변환됩니다.<br>
	 * Converts the configuration to a string.<br>
	 * It is converted to ini file format.
	 *
	 * @return 설정 문자열<br>
	 *         configuration string
	 */
	@Override
	public String toString() {
		return ConfigurationParser.toString(this);
	};
}
