package com.netty4test.rpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 等待信息
 * Created by cd_huang on 2017/11/13.
 */
public class RpcFuture {
	private final long invokeId;
	private Object responseResult;
	private CountDownLatch lock;
	private boolean isResponse =false;
	public RpcFuture(RpcRequest request) {
		this.invokeId = request.getInvokeId();
		lock =new CountDownLatch(1);
	}

	/**
	 * 线程阻塞等待获得结果（默认60秒）
	 * @return
	 * @throws Throwable
	 */
	public Object get() throws Throwable {
		lock.await(30000, TimeUnit.MILLISECONDS);
		if(!isResponse){
			throw new TimeoutException("invokeId "+invokeId+" timeout!");
		}
		if(responseResult instanceof Throwable){
			throw (Throwable) responseResult;
		}
		return responseResult;
	}

	/**
	 * 写入返回的结果并唤醒等待线程
	 * @param res
	 */
	public void setResponseResult(Object res){
		responseResult =res;
		isResponse =true;
		lock.countDown();
	}
}
