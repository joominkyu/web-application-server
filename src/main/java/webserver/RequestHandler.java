package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // url 요청을 읽음
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();
//            log.debug("request line : {}",line);

            if(line == null){
                return;
            }
            String[] tokens = line.split(" ");
            int contentLength = 0;
            //url 부분 저장
            String url = tokens[1];
            boolean logined = false;
            while (!line.equals("")){
                line = br.readLine();
//                log.debug("header : {}",line);
                //본문의 길이 구하기
                if(line.contains("Content-Length")){
                    contentLength = getContentLength(line);
                }
                if(line.contains("Cookie")){
                    logined = isLogin(line);
                }
            }
            DataOutputStream dos = new DataOutputStream(out);
            //url 요청이 user/create 로 시작할경우 회원가입 시작
            if("/user/create".equals(url)) { //POST 요청일 경우(GET 이면 뒤에 ?xxx=yyy&xx=yy등이 붙음)
                String queryString;
                queryString = IOUtils.readData(br, contentLength);
                //post 요청을 HttpRequestUtils.parseQueryString 을 이용해 user 객체에 저장
                Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);
                response302Header(dos,"/index.html");
                //회원 가입이 아닌 url 의 경우
            }else if("/user/login".equals(url)) { //로그인
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = DataBase.findUserById(params.get("userId"));
                if (user == null) {
                    responseResource(out, "/user/login_failed.html");
                    return;
                }
                if (user.getPassword().equals(params.get("password"))) {
                    response302LoginSuccessHeader(dos);
                } else {
                    responseResource(out, "/user/login_failed.html");
                }
            }else if("/user/list".equals(url)){
                if(!logined){
                    responseResource(out,"/user/login.html");
                    return;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for(User user:users){
                    sb.append("<tr>");
                    sb.append("<td>").append(user.getUserId()).append("</td>");
                    sb.append("<td>").append(user.getName()).append("</td>");
                    sb.append("<td>").append(user.getEmail()).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                byte[] body = sb.toString().getBytes();
                response200Header(dos, body.length);
                responseBody(dos,body);
            }else{
                responseResource(out,url);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 요청 후 redirect
     * @param dos
     * @param url 보낼 url
     */
    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect OK \r\n");
            dos.writeBytes("Location: " + url + "  \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * post-header 본문 내용 길이구하기
     * @param line 본문 길이의 정보 ex) Context-Length : 23
     * @return number
     */
    private int getContentLength(String line){
        String[] headerTokens =line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
        response200Header(dos, body.length);
        responseBody(dos,body);
    }

    private void response302LoginSuccessHeader(DataOutputStream dos){
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String line){
        String[] headerTokens = line.split(":");
        Map<String,String> cookie = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookie.get("logined");
        if (value == null){
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
