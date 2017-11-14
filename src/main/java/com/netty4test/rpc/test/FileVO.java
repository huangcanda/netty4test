package com.netty4test.rpc.test;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 文件信息vo
 * Created by cd_huang on 2017/4/22.
 */
public class FileVO implements Serializable {

	private String fileName;

	private byte[] fileData;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	@Override
	public String toString() {
		return "FileVO{" +
				"fileName='" + fileName + '\'' +
				", fileData=" + Arrays.toString(fileData) +
				'}';
	}
}
