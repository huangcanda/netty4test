package com.netty4test.rpc.test;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * 文件获取服务
 * Created by cd_huang on 2017/4/22.
 */
public class FileServiceImpl implements IFileService {
	@Override
	public FileVO downloadFile(String filePath) {
		File file =new File(filePath);
		FileVO fileVO =new FileVO();
		fileVO.setFileName(file.getName());
		try{
			fileVO.setFileData(FileUtils.readFileToByteArray(file));
			return fileVO;
		}catch (Throwable e){
			e.printStackTrace();
			return null;
		}
	}
}
