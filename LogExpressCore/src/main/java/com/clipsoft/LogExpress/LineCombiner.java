package com.clipsoft.LogExpress;

import com.clipsoft.LogExpress.util.Systool;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class LineCombiner {

    private final LineFormatter.FormatItem[] formatItems;

    LineCombiner(LineFormatter.FormatItem[] formatItems) {
        this.formatItems = formatItems;
    }

    private CharSequence align(CharSequence text,int space, int align) {
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

    private CharSequence cutRange(CharSequence text, LineFormatter.LenRange lenRange) {
        int textLen = text.length();
        if (lenRange.min > 0 && text.length() < lenRange.min) {
            int space = lenRange.min - textLen;
            return align(text, space, lenRange.align);
        }
        if(lenRange.max > 0 && textLen > lenRange.max) {
            return text.subSequence(0, lenRange.max);
        }
        else if(lenRange.max < 0 && textLen > Math.abs(lenRange.max)) {
            int max = Math.abs(lenRange.max);
            return text.subSequence(textLen - max, textLen);
        }
        return text;

    }

    public CharSequence combine(Line line) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0, n = formatItems.length; i < n; ++i) {
            LineFormatter.FormatItem item = formatItems[i];
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
                    simpleClassName = idx < 0 ? simpleClassName : simpleClassName.substring(idx + 1, simpleClassName.length());
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
                        stringBuilder.append(cutRange(Systool.hostname(), item.lenRange));
                    else
                        stringBuilder.append(Systool.hostname());
                    break;
                case Pid:
                    if(item.lenRange != null)
                        stringBuilder.append(cutRange(Systool.pid() + "", item.lenRange));
                    else
                        stringBuilder.append(Systool.pid());
                    break;
            }
        }
        stringBuilder.append("\n");
        if(line.getError() != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            PrintWriter printWriter = new PrintWriter(baos);
            line.getError().printStackTrace(printWriter);
            printWriter.flush();
            printWriter.close();
            stringBuilder.append(new String(baos.toByteArray()));
        }
        return stringBuilder;
    }
}
