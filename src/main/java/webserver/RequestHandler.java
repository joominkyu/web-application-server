package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

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
            log.debug("request line : {}",line);

            if(line == null){
                return;
            }
            String[] tokens = line.split(" ");
            //url 부분 저장
            String url = tokens[1];

            while (!line.equals("")){
                line = br.readLine();
                log.debug("header : {}",line);
            }
            DataOutputStream dos = new DataOutputStream(out);
            //url 요청이 user/create 로 시작할경우 회원가입 시작
            if(url.startsWith("/user/create")){
                int index = url.indexOf("?");
                //쿼리스트링 부분(get 요청부분) 을 따로 저장
                String queryString = url.substring(index+1);
                //get요청을 HttpRequestUtils.parseQueryString를 이용해 user 객체에 저장
                Map<String,String> params = HttpRequestUtils.parseQueryString(queryString);
                User user = new User(params.get("userId"),params.get("password"),params.get("name"),params.get("email"));
                log.debug("user : {}",user);
            }
            // 요청한 url 로 이동
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

            response200Header(dos, body.length);
            responseBody(dos, body);
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
