package com.netty4test.rpc.test;

/**
 * 文件获取服务
 * Created by cd_huang on 2017/4/22.
 */
public interface IFileService {
	FileVO downloadFile(String filePath);
}
