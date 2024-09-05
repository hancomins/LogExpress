package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.ColorOption;
import com.hancomins.logexpress.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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

	LineCombiner setColorOption(ColorOption colorOption) {
		lineCombiner.setColorOption(colorOption);
		return lineCombiner;
	}

	static LineFormatter parse(String format) {
		final int MODE_TEXT = 0;
		final int MODE_IN_TYPE = 1;


		StringBuilder itemBuffer = new StringBuilder();
		ArrayList<FormatItem> items = new ArrayList<FormatItem>();
		String[] typeNames = LinePatternItemType.getTypeNameArray();
		
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

					 try {
						 if ("time".equalsIgnoreCase(typeNames[ti]) && itemType.matches("[{][\\s]{0,}(time[:])(?i).+[\\s]{0,}(@.+)?[}]")) {
							 AtQualifier atQualifier = findAtQualifier(itemType);
							 String pattern = itemType.replaceAll("[{][\\s]{0,}(time[:]{1,})(?i)[\\s]{0,}", "").replaceAll("[\\s]{0,}(@.+)?[}]", "").trim();
							 items.add(FormatItem.newTimeType(pattern).setLevel(atQualifier.level).addMarkerQualifiers(atQualifier.markers));
							 appended = true;
							 break;
						 }
						 if ("text".equalsIgnoreCase(typeNames[ti]) && itemType.matches("[{][\\s]{0,}(text[:])(?i).+[\\s]{0,}(@.+]{4,})?[}]")) {
							 AtQualifier atQualifier = findAtQualifier(itemType);
							 String text = itemType.replaceAll("[{][\\s]{0,}(text[:]{1,})(?i)[\\s]{0,}", "").replaceAll("[\\s]{0,}(@.+)?[}]", "").trim();
							 items.add(FormatItem.newTextType(text).setLevel(atQualifier.level).addMarkerQualifiers(atQualifier.markers));
							 appended = true;
							 break;
						 } else if (itemType.matches("[{][\\s]{0,}(" + typeNames[ti] + ")(?i)[\\s]{0,}(\\[[' ']{0,}[0-9]{0,}[' ']{0,1}[:][' ']{0,}[-]{0,1}[0-9]{0,}[' ']{0,}\\]){0,1}(@.+)?[}]")) {
							 itemType = itemType.replace("{", "").replace("}", "");
							 AtQualifier atQualifier = findAtQualifier(itemType);
							 if (atQualifier.hasQualifier()) {
								 itemType = itemType.contains("@") ? itemType.substring(0, itemType.indexOf('@')) : itemType;
							 }

							 Matcher matcher = PATTERN_LEN_RANGE.matcher(itemType);
							 LenRange lenRange = null;

							 while (matcher.find()) {
								 String lenRangeStr = matcher.group();
								 itemType = itemType.replace(lenRangeStr, "");
								 lenRange = parseLenRange(lenRangeStr);
							 }

							 LinePatternItemType type = LinePatternItemType.typeNameOf(itemType.trim());
							 FormatItem item = FormatItem.newByType(type);
							 item.setLevel(atQualifier.level).addMarkerQualifiers(atQualifier.markers);
							 item.lenRange = lenRange;
							 items.add(item);
							 if (type == LinePatternItemType.Class || type == LinePatternItemType.Method || type == LinePatternItemType.ClassName || type == LinePatternItemType.Line || type == LinePatternItemType.File) {
								 needStacktrace = true;
							 }
							 if (type == LinePatternItemType.Tid || type == LinePatternItemType.Thread) {
								 needThreadInfo = true;
							 }
							 if (type == LinePatternItemType.Tid || type == LinePatternItemType.Thread) {
								 //noinspection DataFlowIssue
								 needThreadInfo = true;
							 }
							 appended = true;
							 break;
						 }
					 } catch (Exception e) {
						 InLogger.ERROR("LineFormatter.parse() error. Item: " + itemType, e);
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

	private static AtQualifier findAtQualifier(String format) {
		AtQualifier atQualifier = new AtQualifier();
		if(format.contains("@")) {
			String[] parts = format.split("@");
			if(parts.length == 2) {
				String part = parts[1];
				if(part.endsWith("}")) {
					part = part.substring(0, part.length() - 1);
				}
				String[] markerOrLevel = StringUtil.splitStringWithSeparator(part, ',', true);

				for(String item : markerOrLevel) {
					item = item.trim();
					if(item.isEmpty()) {
						continue;
					}
					// marker 가 큰띠옴표 혹은 작은따옴표로 감싸져 있으면 제거String
					if((item.startsWith("\"") && item.endsWith("\"")) || item.startsWith("'") && item.endsWith("'")) {
						item = item.substring(1, item.length() - 1);
						atQualifier.markers.add(item);
						continue;
					}
					// level 이름을 가져 온다.
					Level level = Level.stringValueOrNull(item.replaceAll("\\s", ""));
					if(level != null && atQualifier.level == null) {
						atQualifier.level = level;
					}
					else if(level == null) {
						atQualifier.markers.add(item);
					}
				}
			}
		}
		return atQualifier;

	}

	private static class AtQualifier {
		Level level = null;
		HashSet<String> markers = new HashSet<String>();
		boolean hasQualifier() {
			return level != null || !markers.isEmpty();
		}

	}
/*

	private static Level findLevelQualifier(String format) {
		Level level = Level.TRACE;
		if(format.contains("@")) {
			String[] parts = format.split("@");
			if(parts.length == 2) {
				String part = parts[1];
				if(part.endsWith("}")) {
					part = part.substring(0, part.length() - 1);
					// 띄어쓰기 제거
					part = part.trim().replaceAll("\\s", "");

				}
				level = Level.stringValueOrNull(part);
				if(level != null) {
					return level;
				}
			}
		}
		return level;
	}
*/


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
		LinePatternItemType type = LinePatternItemType.Text;
		SimpleDateFormat dateFormat = null;
		LenRange lenRange = null;
		Level level = null;
		Set<String> markerQualifiers = null;


		public FormatItem setLevel(Level level) {
			this.level = level == Level.TRACE ? null : level;
			return this;
		}

		FormatItem addMarkerQualifiers(Set<String> markerQualifiers) {
			if(markerQualifiers == null || markerQualifiers.isEmpty()) {
				return this;
			}
			if(this.markerQualifiers == null) {
				this.markerQualifiers = new HashSet<String>();
			}
			this.markerQualifiers.addAll(markerQualifiers);
			return this;
		}

		public boolean isMarkerAllowed(String marker) {
			if(markerQualifiers == null) {
				return true;
			}
			if(StringUtil.isNullOrEmpty(marker)) {
				return false;
			}
			return markerQualifiers.contains(marker);
		}


		private static FormatItem newTimeType(String pattern) {
			FormatItem item = new FormatItem();
			item.type = LinePatternItemType.Time;
			item.dateFormat = new SimpleDateFormat(pattern);
			return item;
		}
		
		private static FormatItem newByType(LinePatternItemType type) {
			FormatItem item = new FormatItem();
			item.type = type;
			return item;
		}
		
		private static FormatItem newTextType(String text) {
			FormatItem item = new FormatItem();
			item.type = LinePatternItemType.Text;
			item.text = text == null ? "" : text;
			return item;
		}
		
	}

}
