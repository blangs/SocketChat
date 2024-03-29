package Examplezz14_NIO10_TCPNonBlockingChannel_채팅서버1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ClientExample extends Application {
	SocketChannel socketChannel;

	public static void main(String[] args) {
		launch(args);
	}

	void startClient() {
		// 연결 시작 코드
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socketChannel = SocketChannel.open();
					socketChannel.configureBlocking(true);
					socketChannel.connect(new InetSocketAddress("localhost", 5001));

					Platform.runLater(() -> {
						try {
							displayText("[연결 완료: " + socketChannel.getRemoteAddress() + "]");
							btnConn.setText("stop");
							btnSend.setDisable(false);
						} catch (IOException e) {
						}
					});

				} catch (Exception e) {
					Platform.runLater(() -> displayText("[서버 통신 안됨]"));
					if (socketChannel.isOpen()) {
						stopClient();
					}
					return;

				}
				receive(); // 서버에서 보낸 데이터 받기
			}
		};
		thread.start();
	}

	void stopClient() {
		// 연결 끊기 코드
		try {
			Platform.runLater(() -> {
				displayText("[연결끊음]");
				btnConn.setText("start");
				btnSend.setDisable(true);
			});
			if (socketChannel != null && socketChannel.isOpen()) {
				socketChannel.close();
			}
		} catch (Exception e) {
		}
	}

	void receive() {
		// 데이터 받기 코드
		while (true) {
			try {
				ByteBuffer byteBuffer = ByteBuffer.allocate(100); // 넌다이렉트 버퍼 생성

				// 서버가 비정상적으로 종료했을 경우 IOException 발생
				int readByteCount = socketChannel.read(byteBuffer); // 데이터를 받으면 배열에 저장 후 개수리턴

				// 서버가 정상적으로 Socket의 close()를 호출했을 경우
				if (readByteCount == -1) {
					throw new IOException();
				}

				byteBuffer.flip(); // 포지션 0위치
				Charset charset = Charset.forName("UTF-8");
				String data = charset.decode(byteBuffer).toString(); // 문자열로 변환

				Platform.runLater(() -> displayText("[받기 완료] " + data));

			} catch (Exception e) {
				Platform.runLater(() -> displayText("[서버통신 안됨]"));
				stopClient();
				break;
			}
		}
	}

	void send(String data) {
		// 데이터 전송 코드
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					Charset charset = Charset.forName("UTF-8");
					ByteBuffer byteBuffer = charset.encode(data);
					socketChannel.write(byteBuffer);
					Platform.runLater(() -> displayText("[보내기 완료]"));

				} catch (Exception e) {
					Platform.runLater(() -> displayText("[서버통신 안됨]"));
					stopClient();
				}
			}
		};
		thread.start();
	}

	////////////////////////////////////////
	///// UI 생성코드

	TextArea txtDisplay;
	javafx.scene.control.TextField txtInput;
	Button btnConn, btnSend;

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		BorderPane root = new BorderPane();
		root.setPrefSize(500, 300);

		txtDisplay = new TextArea();
		txtDisplay.setEditable(false);
		BorderPane.setMargin(txtDisplay, new Insets(0, 0, 2, 0));
		root.setCenter(txtDisplay);

		BorderPane bottom = new BorderPane();
		txtInput = new javafx.scene.control.TextField();
		txtInput.setPrefSize(60, 30);
		root.setCenter(txtDisplay);

		btnConn = new Button("start");
		btnConn.setPrefSize(60, 30);
		btnConn.setOnAction(e -> {
			if (btnConn.getText().equals("start")) {
				startClient();
			} else if (btnConn.getText().equals("stop")) {
				stopClient();
			}
		});

		btnSend = new Button("send");
		btnSend.setPrefSize(60, 30);
		btnSend.setDisable(true);
		btnSend.setOnAction(e -> send(txtInput.getText()));

		bottom.setCenter(txtInput);
		bottom.setLeft(btnConn);
		bottom.setRight(btnSend);
		root.setBottom(bottom);

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Client");
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
	}

	void displayText(String text) {
		txtDisplay.appendText(text + "\n");
	}

}
