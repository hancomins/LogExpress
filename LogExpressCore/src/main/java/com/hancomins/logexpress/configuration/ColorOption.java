package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.InLogger;
import com.hancomins.logexpress.Level;
import com.hancomins.logexpress.LinePatternItemType;
import com.hancomins.logexpress.util.ANSIColor;

import java.util.EnumMap;


/**
 * Configuration class for managing color options for logging output.<br>
 * 로깅 출력에 대한 색상 옵션을 관리하는 구성 클래스입니다.<br>
 * This class allows enabling or disabling color output, resetting color configurations,<br>
 * and setting color codes for different logging levels and line pattern types.<br>
 * 이 클래스는 색상 출력을 활성화 또는 비활성화하고, 색상 구성을 재설정하며,<br>
 * 다양한 로깅 레벨 및 라인 패턴 유형에 대한 색상 코드를 설정할 수 있습니다.<br>
 *
 * @author beom
 */
@SuppressWarnings("unused")
public class ColorOption implements Cloneable {

    private boolean enableConsole = true;
    private boolean enableFile = false;

    private final EnumMap<Level, EnumMap<LinePatternItemType, String>> colorCodeMap = new EnumMap<Level, EnumMap<LinePatternItemType, String>>(Level.class);

    public boolean isEnabledConsole() {
        return enableConsole;
    }

    public boolean isEnabledFile() {
        return enableFile;
    }


    public ColorOption enableConsole(boolean enable) {
        enableConsole = enable;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ColorOption enableFile(boolean enable) {
        enableFile = enable;
        return this;
    }


    /**
     * Resets all color configurations.<br>
     * 모든 색상 구성을 재설정합니다.<br>
     *
     * @return the current ColorOption instance<br>
     *         현재 ColorOption 인스턴스<br>
     */
    public ColorOption reset() {
        colorCodeMap.clear();
        return this;
    }

    /**
     * Gets the color code for a specific logging level and line pattern type.<br>
     * 특정 로깅 레벨 및 라인 패턴 유형에 대한 색상 코드를 가져옵니다.<br>
     *
     * @param level the logging level<br>
     *              로깅 레벨<br>
     * @param linePatternType the line pattern type<br>
     *                        라인 패턴 유형<br>
     * @return the color code as a string. Null if no value<br>
     *         문자열로 된 색상 코드. 값이 없으면 null<br>
     * @throws IllegalArgumentException if level or linePatternType is null<br>
     *                                  level 또는 linePatternType이 null인 경우<br>
     */
    public String getColorCode(Level level, LinePatternItemType linePatternType) {
        if(level == null) {
            throw new IllegalArgumentException("level is null");
        }
        if(linePatternType == null) {
            throw new IllegalArgumentException("linePatternType is null");
        }
        EnumMap<LinePatternItemType, String> typeMap = colorCodeMap.get(level);
        if(typeMap == null) {
            return null;
        }
        return typeMap.get(linePatternType);
    }





    /**
     * Sets the color code for a specific logging level and line pattern type.<br>
     * 특정 로깅 레벨 및 라인 패턴 유형에 대한 색상 코드를 설정합니다.<br>
     *
     * @param level the logging level<br>
     *              로깅 레벨<br>
     * @param linePatternType the line pattern type<br>
     *                        라인 패턴 유형<br>
     * @param color the ANSI color. WHITE, BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN<br>
     *              ANSI 색상. WHITE, BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN<br><br>
     * @param background the ANSI background color (optional)<br>
     *                   ANSI 배경 색상 (선택 사항)<br>
     * @return the current ColorOption instance<br>
     *         현재 ColorOption 인스턴스<br>
     * @throws IllegalArgumentException if level, linePatternType, or color is null<br>
     *                                  level, linePatternType 또는 color가 null인 경우<br>
     */
    public ColorOption putColorCode(Level level, LinePatternItemType linePatternType, ANSIColor color, ANSIColor background) {
        if(level == null) {
            throw new IllegalArgumentException("level is null");
        }
        if(linePatternType == null) {
            throw new IllegalArgumentException("linePatternType is null");
        }
        if(color == null) {
            throw new IllegalArgumentException("color is null");
        }

        EnumMap<LinePatternItemType, String> typeMap = colorCodeMap.get(level);
        if(typeMap == null) {
            typeMap = new EnumMap<LinePatternItemType, String>(LinePatternItemType.class);
            colorCodeMap.put(level, typeMap);
        }

        typeMap.put(linePatternType, background == null ? color.getANSICode() : ANSIColor.combineCode(color, background));
        return this;
    }

    /**
     * Sets the color code for a specific logging level and line pattern type using string values.<br>
     * 문자열 값을 사용하여 특정 로깅 레벨 및 라인 패턴 유형에 대한 색상 코드를 설정합니다.<br>
     *
     * @param level the logging level as a string<br>
     *              문자열로 된 로깅 레벨 TRACE, DEBUG, INFO, WARN, ERROR, FATAL, ALL<br>
     * @param linePatternType the line pattern type as a string<br>
     *                        문자열로 된 라인 패턴 유형 <br/>
     * @param color the color name as a string.  WHITE, BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN<br>
     *              문자열로 된 색상 이름.  WHITE, BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN<br><br>
     * @return the current ColorOption instance<br>
     *         현재 ColorOption 인스턴스<br>
     * @throws IllegalArgumentException if level, linePatternType, or color is null<br>
     *                                  level, linePatternType 또는 color가 null인 경우<br>
     */
    @SuppressWarnings("UnusedReturnValue")
    public ColorOption putColorCode(String level, String linePatternType, String color) {

        if(level == null) {
            throw new IllegalArgumentException("level is null");
        }
        if(linePatternType == null) {
            throw new IllegalArgumentException("linePatternType is null");
        }
        if(color == null) {
            throw new IllegalArgumentException("color is null");
        }
        color = color.trim();
        Level levelEnum = Level.stringValueOrNull(level);
        if(levelEnum == null && !"all".equalsIgnoreCase(level)) {
            InLogger.ERROR("(ColorOption) Invalid Level : " + level);
            return this;
        }
        LinePatternItemType patternTypeEnum = LinePatternItemType.typeNameOf(linePatternType);
        if(patternTypeEnum == null) {
            InLogger.ERROR("(ColorOption) Invalid Line pattern Type : " + linePatternType);
            return this;
        }

        String[] colorCodes = color.split(";");
        String colorName = colorCodes.length > 0 ? colorCodes[0] : "WHITE";
        String backgroundColorName = colorCodes.length > 1 ? colorCodes[1] : null;
        ANSIColor colorEnum = ANSIColor.fromString(colorName);
        if(colorEnum == null) {
            InLogger.ERROR("(ColorOption) Invalid Color : " + colorName);
            return this;
        }
        ANSIColor backgroundColorEnum = backgroundColorName != null ? ANSIColor.fromString(backgroundColorName) : null;
        if(backgroundColorName != null && !backgroundColorName.isEmpty() && backgroundColorEnum == null) {
            InLogger.ERROR("(ColorOption) Invalid Background Color : " + backgroundColorName);
            return this;
        }

        if("all".equalsIgnoreCase(level)) {
            for(Level levelItem : Level.values()) {
                putColorCode(levelItem, patternTypeEnum, colorEnum, backgroundColorEnum);
            }
            return this;
        }


        return putColorCode(levelEnum, patternTypeEnum, colorEnum, backgroundColorEnum);

    }


    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public ColorOption clone() {
        ColorOption clone = new ColorOption();
        clone.enableConsole = enableConsole;
        clone.enableFile = enableFile;
        for(Level level : colorCodeMap.keySet()) {
            EnumMap<LinePatternItemType, String> typeMap = colorCodeMap.get(level);
            EnumMap<LinePatternItemType, String> cloneTypeMap = new EnumMap<LinePatternItemType, String>(LinePatternItemType.class);
            for(LinePatternItemType type : typeMap.keySet()) {
                cloneTypeMap.put(type, typeMap.get(type));
            }
            clone.colorCodeMap.put(level, cloneTypeMap);
        }
        return clone;
    }





}
