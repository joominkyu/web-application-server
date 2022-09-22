# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* connection.getInputStream 로 url 요청을 읽는다.
* BufferedReader 로 해당 요청을 읽은뒤 br.readLine()로 요청을 확인
* GET index.html HTTP/1.1 식을 나누어 원하는 index.html 만 얻는다.(token[1])
* Files.readAllBytes(new File("./webapp" + tokens[1]).toPath()) 로 원하는 곳으로 보내준다.

### 요구사항 2 - get 방식으로 회원가입
* url 을 따로 저장후 get 요청부분만 따로 땐다.
* get 요청을 HttpRequestUtils.parseQueryString 를 사용해 user 객체에 저장한다.

### 요구사항 3 - post 방식으로 회원가입
* post 요청이므로 request-header 에있는 본문에 내용이 들어간다.
* Content-Length 의 길이를 구한다
* bufferedReader 로 본문내용을 구한다.
* HttpRequestUtils.parseQueryString 를 사용해 user 객체에 저장한다.

### 요구사항 4 - redirect 방식으로 이동
* 그냥 url 경로를 붙여주면 요청을 계속하므로 요청후 302 응답이 왔을때 redirect 시켜줌
* dos.writeBytes("HTTP/1.1 302 Redirect OK \r\n");
* dos.writeBytes("Location: " + url + "  \r\n");
* dos.writeBytes("\r\n");
* 정확하게 입력하지 않을경우 redirect 안됨..

### 요구사항 5 - cookie
* DataOutputStream 의 writeBytes를 사용해 Cookie 값 주기.
* 이부분은 이해가 좀 더 필요하다...

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 