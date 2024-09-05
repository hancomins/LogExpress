package com.hancomins.logexpress;

import com.hancomins.logexpress.configuration.ColorOption;
import com.hancomins.logexpress.configuration.WriterType;
import com.hancomins.logexpress.util.ANSIColor;
import com.hancomins.logexpress.util.SysTool;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

class LineCombiner {

    private final LineFormatter.FormatItem[] formatItems;
    private final ColorOption colorOption;



    LineCombiner(LineFormatter.FormatItem[] formatItems, ColorOption colorOption) {
        this.formatItems = formatItems;
        this.colorOption = colorOption;
    }

    boolean isConsistentOutputLine() {
        if(colorOption == null) {
            return true;
        }
        return colorOption.isEnabledConsole() == colorOption.isEnabledFile();
    }


    private CharSequence align(CharSequence text, int space, int align) {
        if(align == LineFormatter.LenRange.ALIGN_LEFT) {
            StringBuilder sb = new StringBuilder(text);
            for(int i = 0; i < space; i++) {
                sb.append(' ');
            }
            return sb.toString();
        } else if(align == LineFormatter.LenRange.ALIGN_RIGHT) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < space; i++) {
                sb.append(' ');
            }
            sb.append(text);
            return sb.toString();
        } else if(align == LineFormatter.LenRange.ALIGN_CENTER) {
            int left = space / 2;
            int right = space - left;
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < left; i++) {
                sb.append(' ');
            }
            sb.append(text);
            for(int i = 0; i < right; i++) {
                sb.append(' ');
            }
            return sb.toString();
        }
        return text;
    }


    private CharSequence subSequenceAndAlign(CharSequence text,int length, int align) {
        if(align == LineFormatter.LenRange.ALIGN_LEFT) {
            return text.subSequence(0, length);
        } else if(align == LineFormatter.LenRange.ALIGN_RIGHT) {
            int start = text.length() - length;
            return text.subSequence(start, text.length());
        } else if(align == LineFormatter.LenRange.ALIGN_CENTER) {
            int left = (text.length() - length) / 2;
            int right = left + length;
            return text.subSequence(left, right);
        }
        return text;
    }


    private CharSequence cutRange(CharSequence text, LineFormatter.LenRange lenRange) {
        int textLen = text.length();
        if (lenRange.min > 0 && text.length() < lenRange.min) {
            int space = lenRange.min - textLen;
            return align(text, space, lenRange.align);
        }
        if(lenRange.max > 0 && textLen > lenRange.max) {
            if(lenRange.align == LineFormatter.LenRange.ALIGN_LEFT) {
                return text.subSequence(0, lenRange.max);
            } else {
                return subSequenceAndAlign(text, lenRange.max, lenRange.align);
            }
        }
        else if(lenRange.max < 0 && textLen > Math.abs(lenRange.max)) {
            int max = Math.abs(lenRange.max);
            return text.subSequence(textLen - max, textLen);
        }
        return text;
    }


    CharSequence combine(Line line) {
        return combine(line, null);
    }




    @SuppressWarnings("ForLoopReplaceableByForEach")
    CharSequence combine(Line line, WriterType writerType) {
        StringBuilder stringBuilder = new StringBuilder();
        Level level = line.getLevel();
        boolean allowColor = colorOption != null &&
                        ((writerType == null && (colorOption.isEnabledConsole() || colorOption.isEnabledFile())) ||
                        (writerType == WriterType.Console && colorOption.isEnabledConsole()) ||
                        (writerType == WriterType.File && colorOption.isEnabledFile()));
        boolean writeColor = false;
        for(int i = 0, n = formatItems.length; i < n; ++i) {
            LineFormatter.FormatItem item = formatItems[i];
            if(!level.isLowerThan(item.level)) {
                continue;
            }
            if(!item.isMarkerAllowed(line.getMarker())) {
                continue;
            }
            if(allowColor) {
                writeColor = false;
                String colorCode = colorOption.getColorCode(level, item.type);
                if(colorCode != null) {
                    stringBuilder.append(colorCode);
                    writeColor = true;
                }
            }

            switch(item.type) {
                case Text:
                    stringBuilder.append(item.text);
                    break;
                case Caller:
                    if(item.lenRange != null)
                        stringBuilder.append(cutRange(line.getCallerFQCN(), item.lenRange));
                    else
                        stringBuilder.append(line.getCallerFQCN());
                    break;
                case CallerSimple:
                    String callerName = line.getCallerFQCN() + "";
                    int lastDot = callerName.lastIndexOf('.');
                    if(lastDot != -1) {
                        callerName = callerName.substring(lastDot + 1);
                    }
                    if(item.lenRange != null)
                        callerName = cutRange(callerName, item.lenRange).toString();
                    stringBuilder.append(callerName);
                    break;
                case Marker:
                    if(item.lenRange != null)
                        stringBuilder.append(cutRange(line.getMarker(), item.lenRange));
                    else
                        stringBuilder.append(line.getMarker());
                    break;
                case Time:
                    stringBuilder.append(item.dateFormat.format(line.getTime()));
                    break;
                case Level:
                    stringBuilder.append(line.getLevel());
                    break;
                case Thread:
                    CharSequence threadName = line.getThreadName();
                    if(item.lenRange != null)
                        threadName = cutRange(threadName, item.lenRange);
                    stringBuilder.append(threadName);
                    break;
                case Tid:
                    long tid = line.getTID();
                    stringBuilder.append(tid);
                    break;
                case Class:
                    CharSequence className = line.getStackTraceElement().getClassName();
                    if(item.lenRange != null)
                        className = cutRange(className, item.lenRange);
                    stringBuilder.append(className);
                    break;
                case ClassName:
                    String simpleClassName = line.getStackTraceElement().getClassName();
                    int idx = simpleClassName.lastIndexOf('.');
                    simpleClassName = idx < 0 ? simpleClassName : simpleClassName.substring(idx + 1);
                    if(item.lenRange != null)
                        simpleClassName = cutRange(simpleClassName, item.lenRange).toString();
                    stringBuilder.append(simpleClassName);
                    break;
                case Line:
                    int lineNumber = line.getStackTraceElement().getLineNumber();
                    stringBuilder.append(lineNumber);
                    break;
                case Method:
                    String method = line.getStackTraceElement().getMethodName();
                    if(item.lenRange != null)
                        method = cutRange(method, item.lenRange).toString();
                    stringBuilder.append(method);
                    break;
                case File:
                    String file = line.getStackTraceElement().getFileName();
                    file = file == null ? "Unknown" : file;
                    if(item.lenRange != null)
                        file = cutRange(file, item.lenRange).toString();
                    stringBuilder.append(file);
                    break;
                case Message:
                    if(item.lenRange != null)
                        stringBuilder.append(cutRange(line.getMessage(), item.lenRange));
                    else
                        stringBuilder.append(line.getMessage());
                    break;
                case Hostname:
                    if(item.lenRange != null)
                        stringBuilder.append(cutRange(SysTool.hostname(), item.lenRange));
                    else
                        stringBuilder.append(SysTool.hostname());
                    break;
                case Pid:
                    if(item.lenRange != null)
                        stringBuilder.append(cutRange(SysTool.pid() + "", item.lenRange));
                    else
                        stringBuilder.append(SysTool.pid());
                    break;
            }
            if(writeColor) {
                stringBuilder.append(ANSIColor.ANSI_RESET);
            }
        }

        stringBuilder.append("\n");
        if(line.getError() != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            PrintWriter printWriter = new PrintWriter(baos);
            line.getError().printStackTrace(printWriter);
            printWriter.flush();
            printWriter.close();
            stringBuilder.append(baos);
        }
        return stringBuilder;
    }
}
