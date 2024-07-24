package com.hancomins.LogExpress.configuration;

import com.hancomins.LogExpress.InLogger;
import com.hancomins.LogExpress.LogExpress;
import com.hancomins.LogExpress.util.Files;

import java.io.*;

import java.util.ArrayList;

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


	private ArrayList<WriterOption> writerConfigureList = new ArrayList<WriterOption>();
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



	
		
	public void clearWriters() {
		if(closed) return;
		writerConfigureList.clear();
	}

	public boolean isClosed() {
		return closed;
	}
	
	

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
		configuration.writerConfigureList = new ArrayList<WriterOption>();
		configuration.defaultOption = getDefaultOption().clone();
		configuration.nonBlockingMode = nonBlockingMode;
		for(int i = 0, n = writerConfigureList.size(); i < n; ++i) {
			WriterOption writerOption = writerConfigureList.get(i);
			if(writerOption == getDefaultOption()) {
				configuration.writerConfigureList.add(configuration.defaultOption);
			} else {
				configuration.writerConfigureList.add(writerOption.clone());
			}

		}
		configuration.staticVariableReplacedDefaultMarker = null;
		return configuration;
	}
	
	Configuration(String path) throws IOException {
		File file = new File(path);
		loadFile(file);
	}
	
	Configuration(File file) throws IOException {
		loadFile(file);
	}
	
	Configuration(Reader reader) throws IOException {
		ConfigurationParser.parse(reader, this);
	}
	
	
	public static Configuration newConfiguration(File file) throws IOException {
		return new Configuration(file);
	}
	
	public static Configuration newConfiguration(Reader reader) throws IOException {
		return new Configuration(reader);
	}


	
	public static Configuration newConfiguration(String path) throws IOException {
		return new Configuration(path);
	}
	
	public static Configuration newConfiguration() throws IOException {
		return new Configuration();
	}
	
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

	public void setDaemonThread(boolean mIsDaemonThread) {
		this.isDaemonThread = mIsDaemonThread;
	}

	public boolean isDaemonThread() {
		return isDaemonThread;
	}
	
	
	

	public void setDefaultMarker(String defaultMarker) {
		if(closed) return;
		defaultMarker = defaultMarker;
	}

	public void setAutoShutdown(boolean enable) {
		if(closed) return;
		autoShutdown = enable;
	}

	public void setNonBlockingMode(boolean enable) {
		if(closed) return;
		nonBlockingMode = enable;
	}

	public boolean isNonBlockingQueue() {
		return nonBlockingMode;
	}

	public boolean isAutoShutdown() {
		return autoShutdown;
	}
	
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
		for(int i = 0, n = writerConfigureList.size(); i < n; ++i) {
			WriterOption configure = writerConfigureList.get(i);
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
	
	
	
	public WriterOption getDefaultWriterOption() {
		if(defaultMarker == null || defaultMarker.isEmpty()) {
			if(!writerConfigureList.isEmpty()) return writerConfigureList.get(0);
			else {
				return null;
			}
		}
		WriterOption configure = getWriterOption(defaultMarker);
		if(configure != null) return configure;
		else if(!writerConfigureList.isEmpty()) return writerConfigureList.get(0);
		return null;
	}
	
	public void setWorkerInterval(int interval) {
		if(closed) return;
		writerWorkerInterval = interval <= 0 ? Integer.MAX_VALUE : interval;
	}
	
	public int getWorkerInterval() {
		return writerWorkerInterval;
	}
	
	
	public WriterOption getWriterOption(String marker) {
		int idx = getIndexOfWriterConfigure(marker);
		if(idx > -1) {
			return writerConfigureList.get(idx);
		}
		return null;
	}
	
	
	public WriterOption newWriterOption(String marker) {
		int idx = getIndexOfWriterConfigure(marker);
		if(idx > -1) {
            return writerConfigureList.get(idx);
		}
		WriterOption option = new WriterOption();
		option.addMarker(marker);
		writerConfigureList.add(option);
		return option;
	}
	


	
	public WriterOption[] getWriterOptions() {
		return writerConfigureList.toArray(new WriterOption[0]);
	}
	
	
	public void setQueueSize(int size) {
		if(closed) return;
		if(size < 10) {
			size = MIN_QUEUE_SIZE;
		}
		queueSize = size;
		
		
	}
	
	public int getQueueSize() {
		return queueSize;
	}
	
	public WriterOption getDefaultOption() {
		return defaultOption;
	}
	
	public void setDebugMode(boolean debug) {
		if(closed) return;
		isDebug = debug;
	}

	public void enableFileLogInDebugMode(boolean enable) {
		if(closed) return;
		debugFileLogEnabled = enable;
	}

	public void enableConsoleLogInDebugMode(boolean enable) {
		if(closed) return;
		debugConsoleLogEnabled = enable;
	}

	
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

	
	public synchronized Configuration close() {
		if(closed) return this;
		closed = true;
		// 설정값이 아무것도 없을 때.
		if(writerConfigureList.isEmpty()) {
			if(defaultMarker == null || defaultMarker.isEmpty()) {
				defaultMarker = "default";
			}
			WriterOption option = new WriterOption();
			option.addMarker(defaultMarker);
			defaultOption = option;
			writerConfigureList.add(option);
			//TODO 시스템 로그로 경고 출력해야함.
		} else if(defaultMarker == null || defaultMarker.isEmpty()) {
			defaultOption = writerConfigureList.get(0);
			defaultOption.addMarker(defaultMarker);
			String[] markers = defaultOption.getMarkers();
			if(markers!= null && markers.length > 0 ) {
				defaultMarker = defaultOption.getMarkers()[0];
			}  else {
				defaultMarker = "default";
			}
			//TODO 시스템 로그로 상황 알려줘야함.
		} else {
			int index =  getIndexOfWriterConfigure(defaultMarker);
			if(index < 0) {
				defaultOption = writerConfigureList.get(0);
				defaultOption.addMarker(defaultMarker);
			} else {
				defaultOption = writerConfigureList.get(index);
			}
		}

        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, n = writerConfigureList.size(); i < n; ++i) {
			WriterOption option = writerConfigureList.get(i);
			option.close();
		}
		return this;
	}
	
	
	@Override
	public String toString() {
		return ConfigurationParser.toString(this);
	}

}
