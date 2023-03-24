package com.clipsoft.LogExpress;


public class MessageFormatter implements CharSequence {

    private final String message;
    private String formattedMessage;
    private final Object[] args;

    private MessageFormatter(String message, Object[] args) {
        this.message = message;
        this.args = args;
    }

    public static MessageFormatter newFormat(String message, Object... args) {
        MessageFormatter formatter = new MessageFormatter(message, args);
        formatter.formattedMessage = formatter.format();
        return formatter;
    }




    private String format() {
        if (args == null || args.length == 0) {
            return message;
        }

        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int start = 0;
        int end = 0;
        while (true) {
            start = message.indexOf('{', end);
            if (start == -1) {
                sb.append(message.substring(end));
                break;
            }
            sb.append(message.substring(end, start));
            end = message.indexOf('}', start);
            if (end == -1) {
                sb.append(message.substring(start));
                break;
            }
            String argIndexStr = message.substring(start + 1, end);
            if (argIndexStr.length() == 0) {
                if(argIndex < args.length) {
                    sb.append(args[argIndex++]);
                } else {
                    sb.append(message.substring(start, end + 1));
                }
            } else {
                int index = Integer.parseInt(argIndexStr);
                if (index < args.length && index >= 0) {
                    sb.append(args[index]);
                } else {
                    sb.append(message.substring(start, end + 1));
                }
            }
            end++;
        }
        return sb.toString();
    }


    @Override
    public String toString() {
        if (formattedMessage == null) {
            formattedMessage = format();
        }
        return formattedMessage;
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);

    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start,end);
    }
}
