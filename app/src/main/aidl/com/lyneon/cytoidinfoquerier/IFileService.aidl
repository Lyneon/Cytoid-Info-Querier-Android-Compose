// IFileService.aidl
package com.lyneon.cytoidinfoquerier;

interface IFileService {
    void destroy() = 16777114;
    boolean copyFileTo(String sourceFilePath, String targetDirectoryPath) = 10000;
}