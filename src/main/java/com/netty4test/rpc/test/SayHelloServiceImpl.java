package com.netty4test.rpc.test;

/**
 * Created by cd_huang on 2017/4/22.
 */
public class SayHelloServiceImpl implements ISayHelloService {
	@Override
	public String sayHello(String name) {
		return "Hello " + name;
	}
}