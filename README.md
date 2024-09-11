# LogExpress - English manual
- https://github.com/hancomins/LogExpress/wiki/English-manual

# LogExpress
## 개요
* 가볍고 빠르고 사용 방법이 단순한 로깅 라이브러리입니다.
* JAVA6 부터 사용 가능하기 때문에 레거시 프로젝트에도 바로 적용할 수 있습니다.
* 비동기 로거입니다. non-block 알고리즘을 사용합니다. Log4j2 와 logback 에서 지원하는 AsyncAppender 와 동일한 방식입니다. 원형 큐에 로그를 쌓았다가 백그라운드 스레드가 파일로 저장합니다.

## 사용하기
### gradle 프로젝트에 추가
```gradle
dependencies {
    // LogExpress core 프로젝트.
    implementation 'io.github.hancomins:LogExpress:1.0.5'
    // SLF4j 지원. SLF4j1.7 기반
    //implementation 'io.github.hancomins:LogExpressSLF4J:1.0.5'
}
```

### 튜토리얼
* 로거 사용하거나 만들기<br/>
  ```java
	import com.hancomins.logexpress.LogExpress;
	
	// 기본 Logger 에 로그를 기록합니다. 
    // 가장 빠르고 쉽게 사용할 수 있는 방법입니다. 
	LogExpress.info("info log");
	
    // 아래 코드는 위 코드와 동일하게 동작합니다. 
	Logger baseLogger = LogExpress.newLogger();
	baseLogger.info("info log");
	baseLogger.fatal("fatal log");
	
    // 임의의 marker 를 사용하여 로그를 기록할 수 있습니다.
	// 설정에 따라서 marker별로 다른 파일에 기록할 수 있습니다. 
	Logger markerLogger = LogExpress.newLogger("debug-marker");
	markerLogger.trace("trace log");
	markerLogger.debug("debug log");
  
    // 호출자 클래스를 인자로 입력.
    // pattern 에 {caller} 를 사용할 경우 호출 클래스 이름을 출력합니다.
    Logger markerLogger = LogExpress.newLogger(this.getClass(), "debug-marker");
    markerLogger.info("info log");
  
    // 호출자 클래스 풀 패키지 경로(FQCN)를 인자로 입력.
    // pattern 에 {caller} 를 사용할 경우 호출 클래스 이름을 출력합니다.
    Logger markerLogger = LogExpress.newLogger("com.hancomins.project.Main", "marker");
    markerLogger.info("info log");
  ```
<br/><br/>

### 설정 파일
* 설정 파일의 포맷은 ini 입니다.
* 기본 파일명은 log-express.ini 입니다.
* log-express.ini 파일은 리소스의 루트경로 혹은 프로젝트의 루트경로에 위치해야합니다. 하지만, 설정 파일의 경로를 변경하고 싶다면 jvm 옵션에 '-DLogExpress.configurationFile=파일경로' 를 추가하거나 프로세스 최초 시작지점에 아래와 같은 코드를 넣어주세요. <br/>
  ```java
    public static void main(String[] args) {
        System.setProperty("logexpress.configurationFile", "파일경로/logexpress.ini");
        ... 
    }
    ```
* 아래는 설정 파일 예제와 각 옵션에 대한 자세한 설명입니다. <br/>
  ```ini
  # 메인 설정
  [configuration]
  
  # LogExpress 자체 문제를 진단할 수 있는 디버그 메시지를 출력하는 옵션입니다.
  # 기본값: false
  debugMode.enable=false
  
  # debugMode.enable이 true인 경우 LogExpress 디버그 메시지를 파일에 기록합니다.
  # 로그 파일은 LogExpress의 jar 라이브러리가 위치한 경로에 생성됩니다.
  # 기본값: false
  debugMode.file=false
  
  # debugMode.enable이 true인 경우 LogExpress 디버그 메시지를 콘솔에 출력합니다.
  # 기본값: false
  debugMode.console=false
  
  # 로그가 저장될 원형 큐의 크기를 설정합니다.
  # 큐가 가득 차면, 로그 기록 메서드 호출 시 빈 공간이 생길 때까지 대기합니다.
  # 큐 크기는 최소 10이어야 합니다.
  # 기본값: 128000
  queueSize=128000
  
  # 기본적으로 non-blocking 알고리즘을 사용하는 큐에 로그를 저장합니다.
  # false로 설정하면 뮤텍스 락을 사용하는 큐를 사용합니다.
  # CAS 알고리즘이 잘 동작하지 않는 특수한 환경에서 false로 설정하세요.
  # 기본값: true
  nonBlockingQueue=true
  
  # 메인 스레드가 종료되면, 로그 큐의 내용을 모두 비운 후 로거 스레드도 종료합니다.
  # 일반적인 JVM 환경에서만 동작합니다. (메인 스레드의 id가 1인 경우)
  # 다중 스레드를 사용하는 서버 환경에서는 사용을 권장하지 않습니다.
  # 기본값: false
  autoShutdown=false
  
  # true로 설정하면 메인 스레드 종료 시 로거 스레드도 함께 종료합니다.
  # 기본값: false
  daemonThread=false
  
  # 기본 marker를 설정합니다. marker는 태그와 유사한 개념입니다.
  # defaultLogger() 메서드 호출 시 해당 marker를 가진 Logger 객체를 가져옵니다.
  # 지정하지 않으면 첫 번째 WriterOption 이름이 기본 marker가 됩니다.
  # 기본값: 없음
  defaultMarker=
  
  # 빈 로그 큐를 검사하는 간격을 밀리세컨드 단위로 설정합니다.
  # 큐가 비어있을 때 이 시간만큼 대기(wait)합니다.
  # 0 이하의 값을 설정하면 빈 로그 큐를 검사하지 않습니다.
  # autoShutdown 옵션이 true로 설정된 경우, 0 이하의 값 설정을 권장하지 않습니다.
  # 기본값: 3000
  workerInterval=3000
  
  # 항상 파일 존재 여부를 확인하고 없으면 다시 생성합니다.
  # 기본값: false
  fileExistCheck=true

  # 기본 로그 레벨을 설정합니다.
  # trace, debug, info, warn, error,fatal, off 순으로 레벨이 낮아집니다. off 레벨에서는 어떤 로그도 출력되지 않습니다.
  # 만약 writer 옵션에 레벨이 설정되지 않는다면 기본 로그 레벨로 초기화 됩니다.
  # writer 옵션에서 정한 level 이 기본 로그 레벨보다 낮을 수 없습니다.
  # 만약 writer 의 레벨이 info, 기본 레벨이 error 면 writer 레벨은 error 로 강제 조절됩니다. 
  # 기본값: trace
  level=trace
  
  # 기본 ANSI 스타일을 설정합니다.
  # 콘솔과 파일 로그에 ANSI 스타일을 적용할 수 있습니다.
  # 각각의 writer 설정 내에도 style 옵션을 사용할 수 있습니다. 
  style.console=false
  style.file=false
  
  # 색상과 폰트 스타일을 설정할 수 있습니다. 각 설정값은 세미콜론(;)으로 구분합니다.
  # 만약, 두 개 이상의 색상이 설정되어 있다면, 마지막에 설정된 색은 배경색으로 적용됩니다.
  # 색상: BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE
  # 폰트 스타일: BOLD, ITALIC, UNDERLINE, STRIKE
  
  # 모든 레벨의 출력. 라인 패턴에서 지정한 {time}에 대하여 CYAN 색상을 적용합니다.
  style.all.time=CYAN
  # 값 앞에 + 를 붙이면 기존 스타일에 추가합니다.
  style.error.time=+BOLD;RED
  # 값이 없으면 기존의 모든 스타일을 제거합니다.
  style.trace.time=
  
  # INFO 레벨의 출력. 라인 패턴에서 지정한 {level}에 대하여 GREEN 색상과 BOLD 스타일을 적용합니다. 
  style.info.level=GREEN;BOL
  # WARN 레벨의 출력. 라인 패턴에서 지정한 {level}에 대하여 YELLOW 색상과 BOLD 스타일을 적용합니다.    
  style.warn.level=YELLOW;BOLD
  # ERROR 레벨의 출력. 라인 패턴에서 지정한 {level}에 대하여 RED 색상과 BOLD 스타일을 적용합니다.
  style.error.level=RED;BOLD
  # FATAL 레벨의 출력. 라인 패턴에서 지정한 {level}에 대하여 BLACK 색상의 글자색,
  # RED 색상의 배경색, BOLD 스타일을 적용합니다.
  style.fatal.level=BLACK;RED;BOLD
    
  # 라인 패턴을 all 로 설정하면 라인 전체에 스타일이 적용됩니다.
  # 아래 예제에서는 fatal 레벨의 모든 출력에 대하여 UNDERLINE 스타일을 적용합니다.
  style.fatal.all=+UNDERLINE
  
  
  # writer는 logback의 appender와 유사합니다.
  # writer 옵션: Logger 객체는 해당 marker의 Writer를 사용합니다. 지정되지 않은 marker를 사용할 경우 기본 Writer를 사용합니다.
  # writer 섹션 이름은 항상 'writer/'로 시작해야 합니다. 그 뒤에 오는 것은 marker입니다. 이 예제에서 marker는 'api'입니다.
  [writer/api]
  
  # 같은 Writer 옵션을 공유하는 marker를 추가할 수 있습니다. 쉼표로 구분합니다.
  markers=catalina,bootstrap
  
  # 로그 레벨을 설정합니다.

  # trace, debug, info, warn, error,fatal, off 순으로 레벨이 낮아집니다. off 레벨에서는 어떤 로그도 출력되지 않습니다.
  # 기본값: info
  level=info
  
  # 버퍼 크기를 바이트 단위로 설정합니다.
  # 기본값: 1024 byte
  bufferSize=1024
  
  # 파일의 최대 크기를 MiB 단위로 설정합니다.
  # file 옵션에 {number}가 있어야 작동합니다.
  # 0 이하로 설정하거나 file 옵션에 {number}가 없으면 무제한 크기를 갖습니다.
  # 기본값: 512 MiB
  maxSize=512
  
  # 로그 파일의 최대 보관 기간을 설정합니다.
  # 설정된 기간이 지나면 로그 파일이 자동으로 삭제됩니다.
  # file 옵션에 {date::}가 있어야 작동합니다. date format에 따라 오동작할 수 있으니 주의하세요.
  # 0보다 작은 값으로 설정 시 기간 제한이 없습니다.
  # 기본값: 60일
  maxHistory=60
  
  # 파일 경로 및 패턴을 설정합니다.
  #  - {marker}: marker 이름
  #  - {hostname}: 호스트 이름
  #  - {pid}: 프로세스 ID (사용 권장하지 않음)
  #  - {date::(date format)}: 로그 기록 날짜
  #  - {number}: maxSize 옵션에 따라 파일 크기가 최대치에 도달하면, 새로운 번호의 파일로 생성됨
  file={marker}.{hostname}.{date:yyyy-MM-dd}.{number}.txt
  
  # 로그 라인 패턴을 설정합니다.
  #  - {time::(date format)}: 로그 발생 시간
  #  - {level}: 로그 레벨
  #  - {hostname}: 호스트 이름. 네트워크 환경에서 서버를 식별하는 데 사용됩니다.
  #  - {pid}: 프로세스 ID
  #  - {thread}: 스레드 이름
  #  - {tid}: 스레드 ID
  #  - {marker}: marker 이름
  #  - {message}: 로그 메시지
  # !주의! 아래 옵션을 추가하면 로그 속도가 약 0.3배로 감소하며 더 많은 메모리를 사용합니다.
  #  - {file}: 로그가 발생된 파일명
  #  - {class}: 로그가 발생된 클래스의 전체 패키지 경로
  #  - {class-name}: 로그가 발생된 클래스명 (패키지 경로 제외)
  #  - {method}: 로그가 발생된 메서드명
  #  - {line}: 로그가 발생된 라인 번호
  #  - {caller}: 로그를 호출한 클래스 (logexpress.getLogger 메서드의 인자로 정의됨)
  #  - {caller-simple}: 로그를 호출한 클래스의 패키지 경로를 제외한 이름
  #  - {text::(text)}: 텍스트를 출력합니다.  @ 을 사용하여 level 혹은 marker를 제한하여 출력할 때 사용합니다. 
  pattern={time::HH:mm:ss.SSS} [{level}] <{hostname}/PID:{pid}/{thread}:{tid}> {marker} | {caller} {caller-simple} | ({file}) {class-name}.{method}():{line} | {message}  {text::에러가 발생하였습니다. @error} 
  
  # 로그 기록 타입을 설정합니다.
  # file, console 타입을 지정할 수 있습니다.
  # 기본값: console
  types=file,console
  
  # 인코딩을 설정합니다.
  # 기본값: 비어있음. 시스템 기본 인코딩 사용.
  encoding=
  
  # 만약 Logger를 감싸는 facade나 wrapper 클래스를 만들 경우, 로그 호출 위치를 더 정확하게 명시할 수 있습니다.
  # 로그에 출력되는 클래스 및 메서드, 라인 번호가 실제 호출한 위치와 다르다면 이 값을 조정하세요.
  # 기본값: 1
  stackTraceDepth=1
  ```
### 환경 변수 및 프로퍼티 적용

- 문자열로 입력 가능한 모든 설정 값에 환경 변수 또는 프로퍼티 값을 포함할 수 있습니다.
- 환경 변수는 `%환경 변수 키%` 형식으로 정의되며, 프로퍼티는 `${프로퍼티 키}` 형식으로 설정됩니다. 만약 해당 키에 대응하는 값이 없다면, 설정 문자열이 그대로 사용됩니다.<br/>
  ```ini
     # 사용 예 - System.properties에 임의로 정의된 Log.dir 값과 시스템 환경 변수 USER에 해당하는 값
     file=${Log.dir}/%USER%/log.out
  ```

- 기본 프로퍼티 이름 
  - ${LogExpress.path}:     LogExpress 라이브러리가 설치된 디렉터리 경로입니다. 이는 애플리케이션의 루트 경로가 될 수 있습니다.
  - ${LogExpress.hostname}: 애플리케이션이 실행 중인 서버의 호스트 이름입니다. 네트워크 환경에서 서버를 식별하는 데 사용됩니다.
  - ${LogExpress.pid}:      현재 실행 중인 프로세스의 ID입니다. 시스템에서 실행 중인 프로세스를 고유하게 식별하는 데 사용됩니다. 

### 코드상에서 동적으로 설정 변경
* 코드상에서 동적으로 설정 변경이 가능합니다. 로그 큐와 Worker 를 모두 종료시키고 새로 생성된 객체로 안전하게 교체(exchange)합니다. <br/>
  ```java
    // 기존 설정을 복제합니다.
    Configuration config = LogExpress.cloneConfiguration();
    // 빈 설정을 생성합니다.
    // Configuration config  = Configuration.newConfiguration();
    // 기본 위치의 설정 파일을 읽어옵니다.
    //Configuration config  = Configuration.fromDefaultConfigurationFile();
    // 경로를 지정하여 설정 파일을 읽어옵니다.
    //Configuration config  = Configuration.newConfiguration("../conf/log.ini");
    // 경로를 지정하여 설정 파일을 읽어옵니다.						
    config.clearWriters();
    WriterOption option = config.newWriterOption("api");
    option.setFile("./log.{hostname}.{marker}.{date:yyyy-MM-dd}.{number}.txt");
    config.setDefaultMarker("api");
    config.setQueueSize(100000000);
    // 변경된 설정을 업데이트합니다.
    LogExpress.updateConfig(config);
  ```
* 자세한 설정 변경 방법은 Configuration 클래스와 public 메서드의 주석을 참고하세요.

### 로그 라인 패턴 출력을 레벨 및 마커에 따라 제한하기

* 로그 라인 패턴 마지막에 @ 를 사용하여 레벨 및 마커를 제한할 수 있습니다. @ 뒤에는 레벨 또는 마커 이름을 입력합니다.
* 레벨은 TRACE, DEUBG, INFO, WARN, ERROR, FATAL, OFF 중 하나를 입력합니다. 만약 ERROR 레벨 이상의 로그만 출력하고 싶다면, @error 를 입력하세요.
* 여러개의 레벨 또는 마커를 입력할 수 있습니다. 쉼표로 구분합니다. 레벨은 대소문자를 구분하지 않지만, 마커 이름은 대소문자 및 공백을 구분합니다.
* 만약 마커의 이름이 레벨 이름과 같다면, 중복을 피하기 위해 마커 이름 앞 뒤에 따옴표(Qutation) 또는 작은 따옴표(Single Qutation)를 사용하세요.
* 마커 이름 내부에 따옴표 혹은 작은 따옴표를 사용하려면, 역슬래시(\)를 사용하여 이스케이프 처리하세요.
* 로그 레벨이 중복되어 있을 경우, 가장 먼저 나오는 레벨을 제외하고 나머지 레벨은 무시됩니다.
* 사용 예시
  ```properties
    # 레벨이 INFO 이상인 로그(INFO, WARN, ERROR, FATAL)만 메시지를 출력합니다.
    pattern={time::HH:mm:ss.SSS} [{level}] {message@info}
    # 마커 레벨이 ERROR 이상 이면서 마커 이름이 'error' 또는 'fatal' 또는 'api' 인 로그에 대해서만 메서드 이름을 출력합니다.
    pattern={time::HH:mm:ss.SSS} [{level}] {method@error,'fatal','error',api}()  {message} {text::에러가 발생 하였습니다. @error} 
       
  ```
### 로그 라인 패턴 요소별 길이 제한
* 로그 메시지의 각 요소에 대해 표시할 텍스트 길이를 제한할 수 있습니다.
* `[최소길이:최대길이]` 형식으로 대괄호 안에 길이를 지정합니다.
* 텍스트의 길이가 최소 길이보다 짧을 경우, 공백을 추가하여 정렬을 설정할 수 있습니다. 여는 대괄호 바로 뒤에 공백이 있으면 우측 정렬, 양쪽에 공백이 있으면 중앙 정렬이 됩니다. 기본 설정은 좌측 정렬입니다.
* 최소 길이 또는 최대 길이만 설정할 수도 있습니다.
* 최대 길이에서 마이너스(-) 값을 사용하면 텍스트의 끝에서부터 해당 길이만큼만 출력합니다.<br/>
  ```ini
  # 메시지의 길이를 최소 5자, 최대 5자로 설정합니다.
  pattern={time::HH:mm:ss.SSS} {message[5:5]}
  # 메시지의 길이를 최소 10자로 설정하고, 10자 미만일 경우 중앙 정렬합니다.
  pattern={time::HH:mm:ss.SSS} {message[ 10: ]}
  # 메시지를 끝에서부터 5자만 출력합니다.
  pattern={time::HH:mm:ss.SSS} {message[:-5]}

  ```
* 출력 예시
  ```java
  LogExpress.info("1234567890");
  
  // 설정: 
  // pattern=[{level}] .{message[ 5:5 ]}.
  // 출력:
  // [INFO] .34567.
  //
  // 설정: 
  // pattern=[{level}] .{message[ 5:5]}.
  // 출력:
  // [INFO] .67890.
  // 
  // 설정:
  // pattern=[{level}] .{message[ 15:]}.
  // 출력:
  // [INFO] .     1234567890.
  //
  // 설정:
  // pattern=[{level}] .{message[ 15: ]}.
  // 출력:
  // [INFO] .  1234567890   .
  ```
  
### 로거와 프로세스 종료
* 비동기 로거인 LogExpress는 메인 스레드가 종료되어도 로거 스레드는 종료되지 않습니다. 로거 스레드가 존재하는한 프로세스도 종료되지 않을 것입니다. 만약 로거 스레드와 프로세스를 우아하게 종료하고싶다면 LogExpress.shutdown() 을 사용하세요.
  ```java
  ShutdownFuture future = LogExpress.shutdown();
  // 여러개의 이벤트 등록가능
  future.addOnEndCallback(new Runnable() {
	@Override
	public void run() {
		System.out.println("끝1");
	}
   });

  future.addOnEndCallback(new Runnable() {
	@Override
	public void run() {
		System.out.println("끝2");
	}
  });
  // 로거 스레드가 끝날때까지 대기
  furture.await();
  ```
* 설정파일 log-express.ini 에서 autoShutdown 의 값을 true로 변경하여 로거를 자동으로 종료되도록 할 수 있습니다. 하지만 다중 스레드를 이용하는 서버 운영 환경에서는 사용을 권장하지 않습니다.


### 메모리를 절약하고 빠르게 사용하기 위한 팁
* debug 로그 레벨 이상은 isDebug() 를 사용하여 로그 레벨을 검사하세요.
  ```java
  if(LogExpress.isDebugEnabled()) {
     LogExpress.debug("소켓 문제가 발생하였습니다.", exception);
  }
  ```
