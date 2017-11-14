package com.netty4test.rpc.test;

import com.netty4test.rpc.RpcClient;

import java.util.Scanner;

/**
 * Created by cd_huang on 2017/11/13.
 */
public class RpcClientTest {
	public static void main(String[] args) {
		final ISayHelloService sayHelloService = RpcClient.referenceService(ISayHelloService.class);
		final IFileService fileService = RpcClient.referenceService(IFileService.class);
		RpcClient.startClient("127.0.0.1", 1111);
		for (int j = 0; j < 3; j++) {
			final int index = j;
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 10; i++) {
						String hello = sayHelloService.sayHello("World!!!" + index + "--------" + i);
						FileVO fileVO = fileService.downloadFile("C:\\netty4test.txt");
					}
				}
			}).start();
		}
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("请输入");
			String hello = sayHelloService.sayHello(scanner.next());
			System.out.println(hello);
		}
	}
}
