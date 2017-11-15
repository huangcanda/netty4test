package com.netty4test.rpc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求类
 * Created by cd_huang on 2017/11/13.
 */
public class RpcRequest implements Serializable{
	private static final AtomicLong INVOKE_ID = new AtomicLong(0);
	private final long invokeId;
	public RpcRequest() {
		invokeId = INVOKE_ID.getAndIncrement();
	}
	private Class<?> interfaceClass;
	private String methodName;
	private Class[] parameterTypeClass;
	private Object[] arguments;

	public long getInvokeId() {
		return invokeId;
	}

	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class[] getParameterTypeClass() {
		return parameterTypeClass;
	}

	public void setParameterTypeClass(Class[] parameterTypeClass) {
		this.parameterTypeClass = parameterTypeClass;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		return "RpcRequest{" +
				"invokeId=" + invokeId +
				", interfaceClass=" + interfaceClass +
				", methodName='" + methodName + '\'' +
				", parameterTypeClass=" + Arrays.toString(parameterTypeClass) +
				", arguments=" + Arrays.toString(arguments) +
				'}';
	}
}
