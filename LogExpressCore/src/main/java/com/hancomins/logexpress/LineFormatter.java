package com.hancomins.logexpress;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [{level}] {caller} {thread} {pid} {hostname} {method} {class} {class-name} {line} {time:hh:mm:ss.SSS} {message}
 * 
 * @author beom
 */


class LineFormatter {

	private final static Pattern PATTERN_LEN_RANGE = Pattern.compile("\\[[' ']{0,}[0-9]{0,}[' ']{0,1}[:][' ']{0,}[-]{0,1}[0-9]{0,}[' ']{0,}\\]");
	
	private boolean needStacktrace = false;
	private boolean needThreadInfo = false;
	private FormatItem[] formatItems = null;
	private LineCombiner lineCombiner;
	
	public boolean needThreadInfo() {
		return needThreadInfo;
	}
	
	
	public boolean needStacktrace() {
		return needStacktrace;
	}

	public LineCombiner getLineCombiner() {
		return lineCombiner;
	}

	static LineFormatter parse(String format) {
		final int MODE_TEXT = 0;
		final int MODE_IN_TYPE = 1;

		StringBuilder itemBuffer = new StringBuilder();
		ArrayList<FormatItem> items = new ArrayList<FormatItem>();
		String[] typeNames = ItemType.getTypeNameArray();
		
		boolean needStacktrace = false;
		boolean needThreadInfo = false;
		int mode = 0;
		for(int i = 0, n = format.length(); i < n; ++i) {
			char ch = format.charAt(i);
			if(ch == '{') {
				if(itemBuffer.length() > 0) {
					items.add(FormatItem.newTextType(itemBuffer.toString()));
					itemBuffer.setLength(0);
				}
				mode = MODE_IN_TYPE;
				itemBuffer.append(ch);
			}


			else if(ch == '}' && mode == MODE_IN_TYPE) {
				itemBuffer.append(ch);
				String itemType = itemBuffer.toString();

				boolean appended = false; 
 				for(int ti = 0; ti < typeNames.length; ++ti) {
					if("time".equalsIgnoreCase(typeNames[ti]) && itemType.matches("[{][\\s]{0,}(time[:])(?i).+[\\s]{0,}[}]")) {
						String pattern = itemType.replaceAll("[{][\\s]{0,}(time[:]{1,})(?i)[\\s]{0,}", "").replaceAll("[\\s]{0,}[}]", "").trim();
						items.add(FormatItem.newTimeType(pattern));
						appended = true;
						break;
					}
					else if(itemType.matches("[{][\\s]{0,}(" + typeNames[ti]  +  ")(?i)[\\s]{0,}(\\[[' ']{0,}[0-9]{0,}[' ']{0,1}[:][' ']{0,}[-]{0,1}[0-9]{0,}[' ']{0,}\\]){0,1}[}]")) {
						itemType = itemType.replace("{", "").replace("}", "");

						Matcher matcher = PATTERN_LEN_RANGE.matcher(itemType);
						LenRange lenRange = null;
						while(matcher.find()) {
							String lenRangeStr = matcher.group();
							itemType = itemType.replace(lenRangeStr, "");
							lenRange = parseLenRange(lenRangeStr);
						}

						ItemType type = ItemType.typeNameOf(itemType.trim());
						FormatItem item = FormatItem.newByType(type);
						item.lenRange = lenRange;
						items.add(item);
						if(type == ItemType.Class || type == ItemType.Method || type == ItemType.ClassName || type == ItemType.Line || type == ItemType.File) {
							needStacktrace = true;
						}
						if(type == ItemType.Tid || type == ItemType.Thread) {
							needThreadInfo = true;
						}
						if(type == ItemType.Tid || type == ItemType.Thread) {
                            //noinspection DataFlowIssue
                            needThreadInfo = true;
						}
						appended = true;
						break;
					}
				}
				if(!appended) {
					items.add(FormatItem.newTextType(itemType));
				}
				mode = MODE_TEXT; 
				itemBuffer.setLength(0);
			}
			else if(mode == MODE_IN_TYPE) {
				itemBuffer.append(ch);
			}
			else {
				itemBuffer.append(ch);
			}
		}
		
		if(itemBuffer.length() > 0) {
			items.add(FormatItem.newTextType(itemBuffer.toString()));
			itemBuffer.setLength(0);
		}
		
		LineFormatter formatter = new LineFormatter();
		formatter.formatItems = items.toArray(new FormatItem[0]);
		formatter.needStacktrace = needStacktrace;
		formatter.needThreadInfo = needThreadInfo;
		formatter.lineCombiner = new LineCombiner(formatter.formatItems);
		return formatter;
	}

	/**
	 * "[최소길이:최대길이]" 문자열을 파싱하여 LenRange의 객체를 반환하는 메서드.
	 * 최소길이가 없을수 있고, 최대길이가 없을수도 있다.
	 * 최대길이가 없다면 최대길이만 있다.
	 * 만약 최소길이 앞에 공백이 있다면 isRightCut은 true가 된다.
	 */
	static LenRange parseLenRange(String lenRangeStr) {
 		lenRangeStr = lenRangeStr.replace("[", "").replace("]", "").replace('\t', ' ');
		int sepIdx = lenRangeStr.indexOf(':');
		LenRange lenRange = new LenRange();
		String left = lenRangeStr.substring(0, sepIdx);
		String right = lenRangeStr.substring(sepIdx + 1);
		if(!left.isEmpty() && left.charAt(0) == ' ') {
			lenRange.align = LenRange.ALIGN_RIGHT;
			left = left.trim();
		}
		if(!right.isEmpty()  &&  right.charAt(right.length() - 1) == ' ') {
			lenRange.align = lenRange.align == LenRange.ALIGN_RIGHT ? LenRange.ALIGN_CENTER : LenRange.ALIGN_LEFT;
			right = right.trim();
		}
		if(!left.isEmpty()) {
			lenRange.min = Integer.parseInt(left);
		}
		if(!right.isEmpty()) {
			lenRange.max = Integer.parseInt(right);
		}
		return lenRange;
	}



	
	static enum ItemType{
		Level, 
		Thread,
		Tid,
		Method,
		File,

		Class,
		ClassName, 
		Line,
		Time,
		Text,
		Message,
		Hostname,
		Marker,
		Pid,
		Caller,
		CallerSimple;

		
		private ItemType() {
			
		}
		
		
		
		
		public static ItemType typeNameOf(String type) {
			ItemType[] types = values();
            //noinspection ForLoopReplaceableByForEach
            for(int i = 0; i < types.length; ++i) {
				if(types[i].name().equalsIgnoreCase(type)) {
					return types[i];
				}
			}
			if(type.equalsIgnoreCase("class-name")) {
				return ClassName;
			}
			if(type.equalsIgnoreCase("caller-simple")) {
				return CallerSimple;
			}
			
			return null;
		}
		
		private static String[] getTypeNameArray() {
			return new String[] {"marker", "level", "thread","tid", "method", "class", "class-name","file", "line", "text", "message", "time", "hostname", "pid","caller","caller-simple"};
		}
	}


	@SuppressWarnings("unused")
    static class LenRange {
		int min = 0;
		int max = 0;

		static int ALIGN_LEFT = 0;
		static int ALIGN_CENTER = 1;
		static int ALIGN_RIGHT = 2;


		int align = ALIGN_LEFT;

		LenRange() {

		}

		LenRange(int minLen, int maxLen) {
			this.min = minLen;
			this.max = maxLen;
		}
	}
	
	
	
	static class FormatItem {
		
		String text = "";
		ItemType type = ItemType.Text;
		SimpleDateFormat dateFormat = null;
		LenRange lenRange = null;




		private static FormatItem newTimeType(String pattern) {
			FormatItem item = new FormatItem();
			item.type = ItemType.Time;
			item.dateFormat = new SimpleDateFormat(pattern);
			return item;
		}
		
		private static FormatItem newByType(ItemType type) { 
			FormatItem item = new FormatItem();
			item.type = type;
			return item;
		}
		
		private static FormatItem newTextType(String text) {
			FormatItem item = new FormatItem();
			item.type = ItemType.Text;
			item.text = text == null ? "" : text;
			return item;
		}
		
	}

}
