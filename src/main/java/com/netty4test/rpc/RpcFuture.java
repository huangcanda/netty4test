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
	/**
	 * 这里用闭锁，而不用普通的锁，是由于业务线程调用write方法是异步的，
	 * 既然是异步，则无法保证调用完write后，客户端接收到结果的时间一定发生阻塞等待之后。
	 * 即普通的lock可能出现先释放锁再上锁导致死锁的情况。
	 */
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
		lock.await(60000, TimeUnit.MILLISECONDS);
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
