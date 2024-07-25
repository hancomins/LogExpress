package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.InLogger;
import com.hancomins.logexpress.Level;
import com.hancomins.logexpress.util.SysTool;
import com.hancomins.logexpress.util.Files;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;

@SuppressWarnings("unused")
class ConfigurationParser {
	
	public static Configuration parse(File file) throws IOException {
		FileReader fileReader = new FileReader(file);
		Configuration configuration = parse(fileReader, Configuration.newConfiguration());
		fileReader.close();
		return configuration;
	}
	
	public static Configuration parse(Reader reader) throws IOException {
		return parse(reader, Configuration.newConfiguration());
	}
	
	public static void commit(Configuration configuration, File file) throws IOException {
		Files.write(file, toString(configuration).getBytes());
	}
	
	public static Configuration parse(Reader reader,Configuration configuration) throws IOException {

		LinkedHashMap<String, Properties> list = parseINI(reader);
		Iterator<Entry<String, Properties>> iter = list.entrySet().iterator();
        //noinspection WhileLoopReplaceableByForEach
        while(iter.hasNext()) {
			Entry<String, Properties> entry = iter.next();
			String key = String.valueOf(entry.getKey());

			Properties properties = entry.getValue();
			if("configuration".equals(key)) {
				String queueSize = properties.getProperty("queueSize", Configuration.DEFAULT_QUEUE_SIZE +"");
				//String consoleBufferSize = properties.getProperty("consoleBufferSize", Configuration.DEFAULT_CONSOLE_BUFFER_SIZE +"");
				String defaultMarker = properties.getProperty("defaultMarker", "");
				String writeWorkerInterval = properties.getProperty("workerInterval", Configuration.DEFAULT_WRITER_WORKER_INTERVAL +"");

				String strDebugMode = properties.getProperty("debugMode.enable","");
				String strDebugModeFile = properties.getProperty("debugMode.file","false");
				String strDebugModeConsole = properties.getProperty("debugMode.console","false");
				String strIsAutoShutdown = properties.getProperty("autoShutdown","false");
				String strIsNonBlockingQueue = properties.getProperty("nonBlockingQueue","");
				if(strIsNonBlockingQueue.isEmpty()) {
					strIsNonBlockingQueue = properties.getProperty("nonBlocking", "true");
				}
				if(strDebugMode.isEmpty()) {
					strDebugMode = properties.getProperty("debug","");
				}
				boolean fileExistCheck = "true".equalsIgnoreCase(properties.getProperty("fileExistCheck","false"));

				boolean debugMode = "true".equalsIgnoreCase(strDebugMode);
				boolean debugModeFile = "true".equalsIgnoreCase(strDebugModeFile);
				boolean debugModeConsole = "true".equalsIgnoreCase(strDebugModeConsole);
				boolean isAutoShutdown = "true".equalsIgnoreCase(strIsAutoShutdown);
				boolean isNonBlockingQueue = !"false".equalsIgnoreCase(strIsNonBlockingQueue);
				configuration.setFileExistCheck(fileExistCheck);
				configuration.setDebugMode(debugMode);
				configuration.enableConsoleLogInDebugMode(debugModeConsole);
				configuration.enableFileLogInDebugMode(debugModeFile);
				configuration.setAutoShutdown(isAutoShutdown);
				configuration.setQueueSize(parseInteger(queueSize, Configuration.DEFAULT_QUEUE_SIZE));
				configuration.setDaemonThread("true".equalsIgnoreCase(properties.getProperty("daemonThread","false")));
				configuration.setDefaultMarker(defaultMarker);
				configuration.setNonBlockingMode(isNonBlockingQueue);
				configuration.setWorkerInterval(parseInteger(writeWorkerInterval, Configuration.DEFAULT_WRITER_WORKER_INTERVAL));
			}
			else if(key.startsWith("writer/") ) {


				String defaultName = key.replaceAll("^writer/{1,}", "");
				String markers = properties.getProperty("markers", defaultName);
				String bufferSize = properties.getProperty("bufferSize", WriterOption.DEFAULT_BUFFER_SIZE + "");
				String maxSize = properties.getProperty("maxSize",  WriterOption.DEFAULT_MAXSIZE + "");
				String history = properties.getProperty("maxHistroy","");
				// 오타수정.
				if(history.isEmpty()) {
					history = properties.getProperty("maxHistory", WriterOption.DEFAULT_HISTORY + "");
				}
				String filePattern = properties.getProperty("file",  WriterOption.DEFAULT_FILE_PATTERN);
				String pattern = properties.getProperty("pattern",  WriterOption.FULL_PATTERN);
				String type = properties.getProperty("types","console");
				String level = properties.getProperty("level", "");
				String encoding = properties.getProperty("encoding",null);

				// 원래는 addedStackTraceElementsIndex 를 사용했지만, 0.10.3 버전이후로 stackTraceDepth 로 변경.
				// 호환성을 위하여 둘다 사용하도록 함.
				String addedStackTraceIndex_ = properties.getProperty("addedStackTraceElementsIndex",WriterOption.DEFAULT_ADDED_INDEX_OF_STACKTRACE_ELEMENTS + "");
				String stackTraceDepth = properties.getProperty("stackTraceDepth", addedStackTraceIndex_);

				
				if(encoding != null && !encoding.isEmpty() && !encodingTest(encoding)) {
					encoding = null;
				}
				WriterOption option = configuration.newWriterOption(defaultName);
				option.setBufferSize(parseInteger(bufferSize, WriterOption.DEFAULT_BUFFER_SIZE));
				option.setMaxSize(parseInteger(maxSize, WriterOption.DEFAULT_MAXSIZE));
				option.setHistory(parseInteger(history, WriterOption.DEFAULT_HISTORY ));
				option.setStackTraceDepth(parseInteger(stackTraceDepth,WriterOption.DEFAULT_ADDED_INDEX_OF_STACKTRACE_ELEMENTS));
				option.setLevel(Level.stringValueOf(level));
				option.setEncoding(encoding);
				option.setFile(filePattern);
				option.setLinePattern(pattern);
				
				String[] markerArray = markers.split(",");
				option.addMarker(defaultName);
                //noinspection ForLoopReplaceableByForEach
                for(int i = 0; i < markerArray.length; ++i) {
					if(markerArray[i] == null || markerArray[i].trim().isEmpty()) continue;
					option.addMarker(markerArray[i].trim());
				}
				
				String[] typeArray = type.split(",");
				option.clearWriterType();
				//noinspection ForLoopReplaceableByForEach
				for(int i = 0; i < typeArray.length; ++i) {
					String typeName =  String.valueOf(typeArray[i]).trim();
					if("console".equalsIgnoreCase(typeName))
						option.addWriterType(WriterType.Console);
					else if("file".equalsIgnoreCase(typeName))
						option.addWriterType(WriterType.File);
				}
			}
		}   
		return configuration;
	}
	

	public static String toString(Configuration configuration) {
		
		StringBuilder strignBuilder = new StringBuilder();
		
		String lb = SysTool.isWindows() ? "\r\n" : "\n";

		int workerInterval = configuration.getWorkerInterval();
		workerInterval = workerInterval == Integer.MAX_VALUE ? -1 : workerInterval;
		strignBuilder.append("[configuration]").append(lb);
		strignBuilder.append("debugMode.enable").append('=').append(InLogger.isEnabled()).append(lb);
		strignBuilder.append("debugMode.file").append('=').append(InLogger.isFileEnabled()).append(lb);
		strignBuilder.append("debugMode.console").append('=').append(InLogger.isConsoleEnabled()).append(lb);
		strignBuilder.append("daemonThread").append('=').append(configuration.isDaemonThread()).append(lb);
		strignBuilder.append("autoShutdown").append('=').append(configuration.isAutoShutdown()).append(lb);
		strignBuilder.append("queueSize").append('=').append(configuration.getQueueSize()).append(lb);
		strignBuilder.append("nonBlockingQueue").append('=').append(configuration.isNonBlockingQueue()).append(lb);

		strignBuilder.append("defaultMarker").append('=').append(configuration.getDefaultMarker()).append(lb);
		strignBuilder.append("workerInterval").append('=').append(workerInterval).append(lb);
		strignBuilder.append("fileExistCheck").append('=').append(configuration.isFileExistCheck()).append(lb);
		
		WriterOption[] writerOptions = configuration.getWriterOptions();
		//noinspection ForLoopReplaceableByForEach
		for(int i = 0; i < writerOptions.length; ++i) {
			WriterOption option = writerOptions[i];
			String[] markers = option.getMarkers();
			if(markers.length == 0) continue;
			String marker = markers[0];
			strignBuilder.append("[writer/").append(marker).append("]").append(lb);
			strignBuilder.append("markers").append('=');
			for(int m = 1, mn = markers.length; m < mn; ++m) {
				strignBuilder.append(markers[m]);
				if(m + 1 != mn) {
					strignBuilder.append(",");
				}
			}
			strignBuilder.append(lb);
			strignBuilder.append("types").append('=');
			WriterType[] types = option.getWriterTypes();
			for(int t = 0, tn = types.length; t < tn; ++t) {
				strignBuilder.append(types[t].toString());
				if(t + 1 != tn) {
					strignBuilder.append(",");
				}
			}
			strignBuilder.append(lb);
			strignBuilder.append("bufferSize").append('=').append(option.getBufferSize()).append(lb);
			int maxSize = option.getMaxSize();
			int history = option.getHistory();
			maxSize = maxSize == Integer.MAX_VALUE ? -1 : maxSize;
			history = history == Integer.MAX_VALUE ? -1 : history;
			strignBuilder.append("maxSize").append('=').append(maxSize).append(lb);
			strignBuilder.append("maxHistory").append('=').append(history).append(lb);
			strignBuilder.append("file").append('=').append(option.getFile()).append(lb);
			strignBuilder.append("pattern").append('=').append(option.getPattern()).append(lb);
			strignBuilder.append("level").append('=').append(option.getLevel()).append(lb);
			String encoding = option.getEncoding(); 
			strignBuilder.append("encoding").append('=').append(encoding == null ? ""  : encoding).append(lb);
			strignBuilder.append("addedStackTraceElementsIndex").append('=').append(option.getStackTraceDepth()).append(lb);
			
		}
		
		return strignBuilder.toString();
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean encodingTest(String encoding) {
		byte[] buffer = "\n".getBytes();
		
		try {
			new String(buffer, Charset.forName(encoding));
		} catch (Exception e) {
			System.err.println("logexpress Error: `" + encoding + "` is an unsupported encoding.");
			StringBuilder availableCharsets = new StringBuilder().append("available charsets: ");
			for(String key : Charset.availableCharsets().keySet()) {
				availableCharsets.append(key);
				availableCharsets.append(", ");
			}
			System.err.println(availableCharsets.substring(0, availableCharsets.length() - 2));
			return false;
		}
		return true; 
	}
	
	
	static private int parseInteger(String value, int def) {
		try {
            return Integer.parseInt(value);
		} catch (Exception e) {
			return def;
		}
	}
	
	
	private static LinkedHashMap<String, Properties> parseINI(Reader reader) throws IOException  {
	   final LinkedHashMap<String, Properties> result = new LinkedHashMap<String, Properties>();
		new Properties() {
			 private static final long serialVersionUID = 1L;
			private Properties section;
			 
	        @Override
	        public Object put(Object key, Object value) {
	            String header = (((String) key) + " " + value).trim();
	            if (header.startsWith("[") && header.endsWith("]"))
	                return result.put(header.substring(1, header.length() - 1),  section = new Properties());
	            else
					try {
						return section.put(key, value);
					} catch (Exception e) {
						ConfigurationIniParsingException error;
						if(e instanceof NullPointerException) {
							error = new ConfigurationIniParsingException("Unable parse configuration data in 'ini' format.");
						} else {
							error = new ConfigurationIniParsingException("Unable parse configuration data in 'ini' format.",e);
						}
						InLogger.ERROR(error);
						return "";
					}
	        }
	        
	    }.load(reader);
	    return result;
	}

	public static class ConfigurationIniParsingException extends Exception {
		public ConfigurationIniParsingException(String message) {
			super(message);
		}

		public ConfigurationIniParsingException(String message, Throwable cause) {
			super(message, cause);
		}
	}



	

}
