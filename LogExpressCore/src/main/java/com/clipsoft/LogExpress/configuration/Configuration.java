package com.clipsoft.LogExpress.configuration;

import com.clipsoft.LogExpress.InLogger;
import com.clipsoft.LogExpress.LogExpress;
import com.clipsoft.LogExpress.util.Files;

import java.io.*;

import java.util.ArrayList;

public final class Configuration implements Cloneable {
	
	private final static String DEFAULT_CONFIG_FILE = "log-express.ini";
	private final static int MIN_QUEUE_SIZE = 10;
	public final static String PROPERTIES_KEY_FILE = "LogExpress.configurationFile";
	public final static int DEFAULT_QUEUE_SIZE = 128000;
	public final static int DEFAULT_WRITER_WORKER_INTERVAL = 3000;

	private boolean mIsDaemonThread = false;
	private volatile boolean mClosed = false;

	private boolean mAutoShutdown = false;


	private ArrayList<WriterOption> mWriterConfigureList = new ArrayList<WriterOption>();
	private int mQueueSize = DEFAULT_QUEUE_SIZE;

	private WriterOption mDefaultOption = null;
	private String mDefaultMarker = "";
	private boolean mIsDebug = false;
	private boolean mIsExistCheck = false;

	private boolean mNonBlockingMode = true;
	private int mWriterWorkerInterval = DEFAULT_WRITER_WORKER_INTERVAL;

	private String mStaticVariableReplacedDefaultMarker = null;



	
		
	public void clearWriters() {
		if(mClosed) return;
		mWriterConfigureList.clear();
		
	}
	
	
	@Override
	public Configuration clone()  {
		Configuration configuration = new Configuration();
		InLogger.reset();
		configuration.mClosed = false;
		configuration.mIsDaemonThread = mIsDaemonThread;
		configuration.mQueueSize = mQueueSize;
		configuration.mDefaultMarker = mDefaultMarker;
		configuration.mAutoShutdown = mAutoShutdown;
		configuration.mWriterWorkerInterval = mWriterWorkerInterval;
		configuration.mWriterConfigureList = new ArrayList<WriterOption>();
		configuration.mDefaultOption = getDefaultOption().clone();
		configuration.mNonBlockingMode = mNonBlockingMode;
		for(int i = 0, n = mWriterConfigureList.size(); i < n; ++i) {
			WriterOption writerOption = mWriterConfigureList.get(i);
			if(writerOption == getDefaultOption()) {
				configuration.mWriterConfigureList.add(configuration.mDefaultOption);
			} else {
				configuration.mWriterConfigureList.add(writerOption.clone());
			}

		}
		configuration.mStaticVariableReplacedDefaultMarker = null;
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
					InLogger.INFO("" + configPath + "` read Success...");
				}
				return configuration;
			} catch (IOException e) {
				InLogger.ERROR("" + configPath + "` read Fail...", e);
			}
		}
		return configuration;
	}

	
	private Configuration() {}


	protected boolean loadFromResources()  {

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
		this.mIsDaemonThread = mIsDaemonThread;
	}

	public boolean isDaemonThread() {
		return mIsDaemonThread;
	}
	
	
	

	public void setDefaultMarker(String defaultMarker) {
		if(mClosed) return;
		mDefaultMarker = defaultMarker;
	}

	public void setAutoShutdown(boolean enable) {
		if(mClosed) return;
		mAutoShutdown = enable;
	}

	public void setNonBlockingMode(boolean enable) {
		if(mClosed) return;
		mNonBlockingMode = enable;
	}

	public boolean isNonBlockingQueue() {
		return mNonBlockingMode;
	}

	public boolean isAutoShutdown() {
		return mAutoShutdown;
	}
	
	public String getDefaultMarker() {
		if(!mClosed) {
			return mDefaultMarker;
		}
		if(mStaticVariableReplacedDefaultMarker == null) {
			mStaticVariableReplacedDefaultMarker = StaticVariableReplacer.replace(mDefaultMarker);
		}
		return mStaticVariableReplacedDefaultMarker;

	}
	
	
	private int getIndexOfWriterConfigure(String marker) {
		for(int i = 0, n = mWriterConfigureList.size(); i < n; ++i) {
			WriterOption configure = mWriterConfigureList.get(i);
			String[] markers = configure.getMarkers();
			for(int mi = 0; mi < markers.length; ++mi) {
				if(markers[mi].equals(marker)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	
	
	public WriterOption getDefaultWriterOption() {
		if(mDefaultMarker == null || mDefaultMarker.isEmpty()) {
			if(!mWriterConfigureList.isEmpty()) return mWriterConfigureList.get(0);
			else {
				return null;
			}
		}
		WriterOption configure = getWriterOption(mDefaultMarker);
		if(configure != null) return configure;
		else if(!mWriterConfigureList.isEmpty()) return mWriterConfigureList.get(0);
		return null;
	}
	
	public void setWorkerInterval(int interval) {
		if(mClosed) return;
		mWriterWorkerInterval = interval <= 0 ? Integer.MAX_VALUE : interval;
	}
	
	public int getWorkerInterval() {
		return mWriterWorkerInterval;
	}
	
	
	public WriterOption getWriterOption(String marker) {
		int idx = getIndexOfWriterConfigure(marker);
		if(idx > -1) {
			return mWriterConfigureList.get(idx);
		}
		return null;
	}
	
	
	public WriterOption newWriterOption(String marker) {
		int idx = getIndexOfWriterConfigure(marker);
		if(idx > -1) {
			WriterOption configure = mWriterConfigureList.get(idx);
			return configure;
		}
		WriterOption option = new WriterOption();
		option.addMarker(marker);
		mWriterConfigureList.add(option);
		return option;
	}
	


	
	public WriterOption[] getWriterOptions() {
		return mWriterConfigureList.toArray(new WriterOption[mWriterConfigureList.size()]);
	}
	
	
	public void setQueueSize(int size) {
		if(mClosed) return;
		if(size < 10) {
			size = MIN_QUEUE_SIZE;
		}
		mQueueSize = size;
		
		
	}
	
	public int getQueueSize() {
		return mQueueSize;
	}
	
	public WriterOption getDefaultOption() {
		return mDefaultOption;
	}
	
	public void setDebugMode(boolean debug) {
		if(mClosed) return;
		mIsDebug = debug;
		if(debug) {
			InLogger.enable();
		} else if(InLogger.isEnabled()) {
			InLogger.disable();
		}
	}

	public void enableFileLogInDebugMode(boolean enable) {
		if(mClosed) return;
		InLogger.enableFile(enable);
	}

	public void enableConsoleLogInDebugMode(boolean enable) {
		if(mClosed) return;
		InLogger.enableConsole(enable);
	}

	
	public void setFileExistCheck(boolean enable) {
		if(mClosed) return;
		mIsExistCheck = enable;
	}
	
	public boolean isFileExistCheck() {
		return mIsExistCheck;
	}

	public boolean isDebug() {
		return mIsDebug;
	}

	
	public synchronized Configuration close() {
		if(mClosed) return this;
		mClosed = true;
		// 설정값이 아무것도 없을 때.
		if(mWriterConfigureList.isEmpty()) {
			if(mDefaultMarker == null || mDefaultMarker.isEmpty()) {
				mDefaultMarker = "default";
			}
			WriterOption option = new WriterOption();
			option.addMarker(mDefaultMarker);
			mDefaultOption = option;
			mWriterConfigureList.add(option);
			//TODO 시스템 로그로 경고 출력해야함.
		} else if(mDefaultMarker == null || mDefaultMarker.isEmpty()) {
			mDefaultOption = mWriterConfigureList.get(0);
			mDefaultOption.addMarker(mDefaultMarker);
			String[] markers = mDefaultOption.getMarkers();
			if(markers!= null && markers.length > 0 ) {
				mDefaultMarker = mDefaultOption.getMarkers()[0];
			}  else {
				mDefaultMarker = "default";	
			}
			//TODO 시스템 로그로 상황 알려줘야함.
		} else {
			int index =  getIndexOfWriterConfigure(mDefaultMarker);
			if(index < 0) {
				mDefaultOption = mWriterConfigureList.get(0);
				mDefaultOption.addMarker(mDefaultMarker);	
			} else {
				mDefaultOption = mWriterConfigureList.get(index);
			}
		}
		
		for(int i = 0, n = mWriterConfigureList.size(); i < n; ++i) {
			WriterOption option = mWriterConfigureList.get(i);
			option.close();
		}
		return this;
	}
	
	
	@Override
	public String toString() {
		return ConfigurationParser.toString(this);
	}

}
