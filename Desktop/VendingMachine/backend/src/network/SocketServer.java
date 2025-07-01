// network/SocketServer.java
package network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 간단한 TCP 소켓 서버
 * - 클라이언트가 보낸 메시지를 콘솔에 출력
 * - 한 번 연결된 클라이언트의 모든 메시지를 출력 후 연결 종료
 */
public class SocketServer {

    public static void main(String[] args) {
        int port = 12345; // 포트 번호 설정

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("📡 서버 시작! 포트: " + port);

            while (true) {
                // 클라이언트 연결 수락
                Socket client = serverSocket.accept();
                System.out.println(" 클라이언트 연결됨: " + client.getInetAddress());

                // 클라이언트로부터 데이터 수신
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    // 수신된 Base64 문자열 출력
                    System.out.println("수신: " + line);
                }

                // 클라이언트 연결 종료
                client.close();
                System.out.println("클라이언트 연결 종료");
            }

        } catch (IOException e) {
            // 예외 발생 시 스택 트레이스 출력
            e.printStackTrace();
        }
    }
}
