package com.netty4test.rpc.test;

import com.netty4test.rpc.RpcServer;

/**
 * Created by cd_huang on 2017/11/13.
 */
public class RpcServerTest {
	public static void main(String[] args) {
		ISayHelloService sayHelloService = new SayHelloServiceImpl();
		IFileService fileService =new FileServiceImpl();
		RpcServer.exportService(ISayHelloService.class,sayHelloService);
		RpcServer.exportService(IFileService.class,fileService);
		RpcServer.startServer(1111);
	}
}
