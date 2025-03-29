// IFileService.aidl
package com.lyneon.cytoidinfoquerier;

interface IFileService {
    void destroy() = 16777114;
    boolean copyFileTo(String sourceFilePath, String targetDirectoryPath) = 10000;
    String readFile(String filePath) = 10001;
    String[] listFiles(String directoryPath) = 10002;
    byte[] readBytes(String filePath) = 10003;
    boolean exists(String path) = 10004;
    boolean isFile(String path) = 10005;
    boolean isDirectory(String path) = 10006;
}