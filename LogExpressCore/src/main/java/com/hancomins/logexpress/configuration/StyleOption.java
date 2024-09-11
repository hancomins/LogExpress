
package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.InLogger;
import com.hancomins.logexpress.Level;
import com.hancomins.logexpress.LinePatternItemType;

import java.util.*;

/**
 * Configuration class for managing color and style options for logging output.<br>
 * 로깅 출력에 대한 색상 및 스타일 옵션을 관리하는 구성 클래스입니다.<br>
 * This class can set ANSI color codes and font styles for various logging levels and line pattern types.<br>
 * 이 클래스는 다양한 로깅 레벨 및 라인 패턴 유형에 대한 ANSI 색상 코드 및 폰트 스타일을 설정할 수 있습니다.<br>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * styleOption.enableConsole(true)
 *            .setStyle(Level.INFO, LinePatternItemType.MESSAGE, ANSIColor.GREEN, ANSIFont.BOLD, ANSIFont.ITALIC);
 * }
 * </pre>
 *
 * @author beom
 */
@SuppressWarnings("unused")
public class StyleOption implements Cloneable {

    private static final List<ANSIStyle> AllStyles;
    private boolean enableConsole = true;
    private boolean enableFile = false;

    static {
        List<ANSIStyle> styles  = new ArrayList<ANSIStyle>();
        styles.addAll(Arrays.asList(ANSIColor.values()));
        styles.addAll(Arrays.asList(ANSIFont.values()));
        AllStyles = Collections.unmodifiableList(styles);
    }

    private final EnumMap<Level, EnumMap<LinePatternItemType, String>> ansiCodeMap = new EnumMap<Level, EnumMap<LinePatternItemType, String>>(Level.class);

    /**
     * Checks if console output is enabled.<br>
     * 콘솔 출력이 활성화되어 있는지 확인합니다.<br>
     *
     * @return true if console output is enabled, false otherwise<br>
     *         콘솔 출력이 활성화되어 있으면 true, 그렇지 않으면 false<br>
     */
    public boolean isEnabledConsole() {
        return enableConsole;
    }

    /**
     * Checks if file output is enabled.<br>
     * 파일 출력이 활성화되어 있는지 확인합니다.<br>
     *
     * @return true if file output is enabled, false otherwise<br>
     *         파일 출력이 활성화되어 있으면 true, 그렇지 않으면 false<br>
     */
    public boolean isEnabledFile() {
        return enableFile;
    }

    private boolean isChanged = false;

    /**
     * Enables or disables console output.<br>
     * 콘솔 출력을 활성화 또는 비활성화합니다.<br>
     *
     * @param enable true to enable console output, false to disable<br>
     *               콘솔 출력을 활성화하려면 true, 비활성화하려면 false<br>
     * @return the current StyleOption instance<br>
     *         현재 StyleOption 인스턴스<br>
     */
    public StyleOption enableConsole(boolean enable) {
        enableConsole = enable;
        isChanged = true;
        return this;
    }

    /**
     * Enables or disables file output.<br>
     * 파일 출력을 활성화 또는 비활성화합니다.<br>
     *
     * @param enable true to enable file output, false to disable<br>
     *               파일 출력을 활성화하려면 true, 비활성화하려면 false<br>
     * @return the current StyleOption instance<br>
     *         현재 StyleOption 인스턴스<br>
     */
    @SuppressWarnings("UnusedReturnValue")
    public StyleOption enableFile(boolean enable) {
        enableFile = enable;
        isChanged = true;
        return this;
    }

    /**
     * Resets all style configurations.<br>
     * 모든 스타일 구성을 재설정합니다.<br>
     *
     * @return the current StyleOption instance<br>
     *         현재 StyleOption 인스턴스<br>
     */
    public StyleOption reset() {
        boolean changed = !ansiCodeMap.isEmpty();
        ansiCodeMap.clear();
        return this;
    }

    /**
     * Gets the ANSI code for a specific logging level and line pattern type.<br>
     * 특정 로깅 레벨 및 라인 패턴 유형에 대한 ANSI 코드를 가져옵니다.<br>
     *
     * @param level the logging level<br>
     *              로깅 레벨<br>
     * @param linePatternType the line pattern type<br>
     *                        라인 패턴 유형<br>
     * @return the ANSI code as a string<br>
     *         문자열로 된 ANSI 코드<br>
     * @throws IllegalArgumentException if level or linePatternType is null<br>
     *                                  level 또는 linePatternType이 null인 경우<br>
     */
    public String getAnsiCode(Level level, LinePatternItemType linePatternType) {
        if(level == null) {
            throw new IllegalArgumentException("level is null");
        }
        if(linePatternType == null) {
            throw new IllegalArgumentException("linePatternType is null");
        }
        EnumMap<LinePatternItemType, String> typeMap = ansiCodeMap.get(level);
        if(typeMap == null) {
            return null;
        }
        return typeMap.get(linePatternType);
    }

    boolean isNotChanged() {
        return !isChanged;
    }

    protected void resetChanged() {
        isChanged = false;
    }

    /**
     * Sets the style for a specific logging level and line pattern type.<br>
     * 특정 로깅 레벨 및 라인 패턴 유형에 대한 스타일을 설정합니다.<br>
     * If more than one ANSI color is set, the first is the font color and the second is the background color.<br>
     * ANSIColor 를 2개 이상 설정할 경우, 첫번째는 폰트 색상, 두번째는 배경 색상으로 설정됩니다.<br>
     *
     * @param level the logging level<br>
     *              로깅 레벨<br>
     * @param linePatternType the line pattern type<br>
     *                        라인 패턴 유형<br>
     * @param styles ANSI styles<br>
     *               ANSI 스타일<br>
     * @return the current StyleOption instance<br>
     *         현재 StyleOption 인스턴스<br>
     * @throws IllegalArgumentException if level, linePatternType, or styles are null<br>
     *                                  level, linePatternType 또는 styles가 null인 경우<br>
     */
    public StyleOption setStyle(Level level, LinePatternItemType linePatternType, ANSIStyle... styles) {
        return setStyle(false, level, linePatternType, styles);
    }

    /**
     * Add the style for a specific logging level and line pattern type.<br>
     * 특정 로깅 레벨 및 라인 패턴 유형에 대한 스타일을 추가합니다.<br>
     * If more than one ANSI color is set, the first is the font color and the second is the background color.<br>
     * ANSIColor 를 2개 이상 설정할 경우, 첫번째는 폰트 색상, 두번째는 배경 색상으로 설정됩니다.<br>
     *
     * @param level the logging level<br>
     *              로깅 레벨<br>
     * @param linePatternType the line pattern type<br>
     *                        라인 패턴 유형<br>
     * @param styles ANSI styles<br>
     *               ANSI 스타일<br>
     * @return the current StyleOption instance<br>
     *         현재 StyleOption 인스턴스<br>
     * @throws IllegalArgumentException if level, linePatternType, or styles are null<br>
     *                                  level, linePatternType 또는 styles가 null인 경우<br>
     */
    public StyleOption addStyle(Level level, LinePatternItemType linePatternType, ANSIStyle... styles) {
        return setStyle(true, level, linePatternType, styles);
    }

    private ANSIStyle[] removeNull(ANSIStyle... styles) {
        if(styles == null) {
            return new ANSIStyle[0];
        }
        List<ANSIStyle> styleList = new ArrayList<ANSIStyle>();
        for(ANSIStyle style : styles) {
            if(style == null) {
                continue;
            }
            styleList.add(style);
        }
        return styleList.toArray(new ANSIStyle[0]);
    }

    private StyleOption setStyle(boolean isAppend, Level level, LinePatternItemType linePatternType, ANSIStyle... styles) {
        styles = removeNull(styles);
        if(level == null) {
            throw new IllegalArgumentException("level is null");
        }
        if(linePatternType == null) {
            throw new IllegalArgumentException("linePatternType is null");
        }

        EnumMap<LinePatternItemType, String> typeMap = ansiCodeMap.get(level);
        if(typeMap == null) {
            typeMap = new EnumMap<LinePatternItemType, String>(LinePatternItemType.class);
            ansiCodeMap.put(level, typeMap);
        }
        if(!isAppend) {
            typeMap.remove(linePatternType);
        }

        if(styles.length == 0) {
            return this;
        }


        ANSIStyle[] mergeStyles = styles;

        if(isAppend) {
            String alreadyCode = typeMap.get(linePatternType);
            if (alreadyCode != null) {
                String alreadyStylesNames = codeToStyleNames(alreadyCode);
                ANSIStyle[] alreadyStyles = parseStyleNames(alreadyStylesNames);
                ANSIStyle[] newStyles = new ANSIStyle[alreadyStyles.length + mergeStyles.length];
                System.arraycopy(alreadyStyles, 0, newStyles, 0, alreadyStyles.length);
                System.arraycopy(mergeStyles, 0, newStyles, alreadyStyles.length, mergeStyles.length);
                mergeStyles = newStyles;
            }
        }



        String code = combineCode(mergeStyles);
        if(code == null) {
            InLogger.ERROR("(StyleOption) Invalid ANSI styles");
        }
        typeMap.put(linePatternType, code);
        return this;
    }

    /**
     * Sets the style for a specific logging level and line pattern type using string values.<br>
     * 문자열 값을 사용하여 특정 로깅 레벨 및 라인 패턴 유형에 대한 스타일을 설정합니다.<br>
     *
     * @param level the logging level as a string<br>
     *              문자열로 된 로깅 레벨<br>
     * @param linePatternType the line pattern type as a string<br>
     *                        문자열로 된 라인 패턴 유형<br>
     * @param styles the styles as a string<br>
     *               문자열로 된 스타일<br>
     *               만약 가장 앞에 '+'가 붙으면 기존 스타일에 추가합니다.<br>
     *               각 스타일은 세미콜론(;)으로 구분됩니다.<br>
     *               # 스타일 옵션<br>
     *               - ANSI 색상 코드 : BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE<br>
     *               - ANSI 폰트 스타일 : BOLD, ITALIC, UNDERLINE, STRIKE<br>
     * @return the current StyleOption instance<br>
     *         현재 StyleOption 인스턴스<br>
     * @throws IllegalArgumentException if level, linePatternType, or styles are null<br>
     *                                  level, linePatternType 또는 styles가 null인 경우<br>
     */
    @SuppressWarnings("UnusedReturnValue")
    public StyleOption setStyle(String level, String linePatternType, String styles) {
        if(level == null) {
            throw new IllegalArgumentException("level is null");
        }
        if(linePatternType == null) {
            throw new IllegalArgumentException("linePatternType is null");
        }
        if(styles == null) {
            throw new IllegalArgumentException("style is null");
        }
        styles = styles.trim();
        boolean isAppend = false;
        if(styles.startsWith("+")) {
            isAppend = true;
            styles = styles.substring(1);
        }
        Level levelEnum = Level.stringValueOrNull(level);
        if(levelEnum == null && !"all".equalsIgnoreCase(level)) {
            InLogger.ERROR("(StyleOption) Invalid Level : " + level);
            return this;
        }
        LinePatternItemType patternTypeEnum = LinePatternItemType.typeNameOf(linePatternType);
        boolean isAllPattern = false;

        if(patternTypeEnum == null) {
            if("all".equalsIgnoreCase(linePatternType)) {
                isAllPattern = true;
            } else {
                InLogger.ERROR("(StyleOption) Invalid Line pattern Type : " + linePatternType);
                return this;
            }
        }
        boolean isAllLevel = "all".equalsIgnoreCase(level);


        if(isAllLevel && !isAllPattern) {
            for(Level levelItem : Level.values()) {
                setStyle(isAppend, levelItem, patternTypeEnum, parseStyleNames(styles));
            }
            return this;
        } else if(!isAllLevel && isAllPattern) {
            for(LinePatternItemType linePattern : LinePatternItemType.values()) {
                setStyle(isAppend, levelEnum, linePattern, parseStyleNames(styles));
            }
            return this;
        } else if(isAllLevel) {
            for(Level levelItem : Level.values()) {
                for(LinePatternItemType linePattern : LinePatternItemType.values()) {
                    setStyle(isAppend, levelItem, linePattern, parseStyleNames(styles));
                }
            }
            return this;
        }


        this.isChanged = true;
        return setStyle(isAppend, levelEnum, patternTypeEnum, parseStyleNames(styles));
    }

    void copy(StyleOption other) {
        enableConsole = other.enableConsole;
        enableFile = other.enableFile;
        ansiCodeMap.clear();
        for(Level level : other.ansiCodeMap.keySet()) {
            EnumMap<LinePatternItemType, String> typeMap = other.ansiCodeMap.get(level);
            EnumMap<LinePatternItemType, String> cloneTypeMap = new EnumMap<LinePatternItemType, String>(LinePatternItemType.class);
            for(LinePatternItemType type : typeMap.keySet()) {
                cloneTypeMap.put(type, typeMap.get(type));
            }
            ansiCodeMap.put(level, cloneTypeMap);
        }
        isChanged = other.isChanged;
    }

    /**
     * Creates a clone of the current StyleOption instance.<br>
     * 현재 StyleOption 인스턴스의 복제본을 생성합니다.<br>
     *
     * @return a clone of the current StyleOption instance<br>
     *         현재 StyleOption 인스턴스의 복제본<br>
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public StyleOption clone() {
        StyleOption clone = new StyleOption();
        clone.copy(this);
        return clone;
    }

    private static ANSIStyle[] parseStyleNames(String style) {
        String[] styleNames = style.split(";");
        List<ANSIStyle> styles = new ArrayList<ANSIStyle>();
        for(String name: styleNames) {
            if(name == null) {
                continue;
            }
            name = name.trim();
            if(name.isEmpty()) {
                continue;
            }
            ANSIStyle styleEnum = ANSIColor.fromString(name);
            if(styleEnum != null) {
                styles.add(styleEnum);
                continue;
            }
            ANSIFont fontEnum = ANSIFont.fromString(name);
            if(fontEnum != null) {
                styles.add(fontEnum);
            }
        }
        return styles.toArray(new ANSIStyle[0]);
    }

    /**
     * Converts ANSI code to style names.<br>
     * ANSI 코드를 스타일 이름으로 변환합니다.<br>
     * If the ANSI code is null or empty, an empty string is returned.<br>
     * ANSI 코드가 null 또는 빈 경우 빈 문자열이 반환됩니다.<br>
     * If the ANSI code is not valid, an empty string is returned.<br>
     * ANSI 코드가 유효하지 않은 경우 빈 문자열이 반환됩니다.<br>
     * If the ANSI code is valid, the style names are returned as a string separated by semicolons.<br>
     * ANSI 코드가 유효한 경우, 스타일 이름이 세미콜론으로 구분된 문자열로 반환됩니다.<br>
     * The style names are in the order of ANSI color codes and font styles.<br>
     * 스타일 이름은 ANSI 색상 코드 및 폰트 스타일 순서대로 정렬됩니다.<br>
     * If the ANSI code contains multiple ANSI color codes, the first is the font color and the second is the background color.<br>
     * ANSI 코드에 여러 ANSI 색상 코드가 포함되어 있는 경우, 첫번째는 폰트 색상, 두번째는 배경 색상입니다.<br>
     * If the ANSI code contains ANSI font styles, they are added to the end of the color codes.<br>
     * ANSI 코드에 ANSI 폰트 스타일이 포함되어 있는 경우, 색상 코드 뒤에 추가됩니다.<br>
     * If the ANSI code contains invalid ANSI color codes or font styles, they are ignored.<br>
     * ANSI 코드에 유효하지 않은 ANSI 색상 코드 또는 폰트 스타일이 포함되어 있는 경우 무시됩니다.<br>
     * @param ansiCode
     * @return
     */
    public static String codeToStyleNames(String ansiCode) {
        if(ansiCode == null || ansiCode.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        ansiCode = ansiCode.replace("\u001B[", "").replace("\u001b[", "");
        String[] colors = ansiCode.split(";");

        for(String c : colors) {
            if(c == null || c.isEmpty()) {
                continue;
            }
            if(c.endsWith("m") || c.endsWith("M")) {
                c = c.substring(0, c.length() - 1);
            }

            for(ANSIStyle style : AllStyles) {
                if(c.equals(style.getCode()) || c.equals(style.getRevertCode())) {
                    sb.append(style.name()).append(";");
                    break;
                }
            }
        }
        if(sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private static String combineCode(ANSIStyle... styles) {
        if(styles == null || styles.length == 0) {
            return null;
        }
        List<ANSIColor> colors = new ArrayList<ANSIColor>();
        List<ANSIFont> fonts = new ArrayList<ANSIFont>();
        for(ANSIStyle style : styles) {
            if(style.getType() == ANSIStyle.StyleType.COLOR) {
                colors.add((ANSIColor) style);
            } else if(style.getType() == ANSIStyle.StyleType.FONT) {
                fonts.add((ANSIFont) style);
            }
        }
        if(colors.isEmpty() && fonts.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\u001B[");
        int colorSize = colors.size();
        int fontColorIndex = colorSize - 2;
        int backgroundColorIndex = colorSize - 1;
        if(fontColorIndex == -1) {
            fontColorIndex = 0;
            backgroundColorIndex = -1;
        }
        if(fontColorIndex >= 0) {
            sb.append(colors.get(fontColorIndex).getCode());
        }
        if(backgroundColorIndex >= 0) {
            sb.append(";").append(colors.get(backgroundColorIndex).getRevertCode());
        }
        if(fontColorIndex >= 0 && !fonts.isEmpty()) {
            sb.append(";");
        }
        if(!fonts.isEmpty()) {
            for(ANSIFont font : fonts) {
                sb.append(font.getCode()).append(";");
            }
            sb.setLength(sb.length() - 1);
        }
        sb.append("m");

        return sb.toString();
    }
}
