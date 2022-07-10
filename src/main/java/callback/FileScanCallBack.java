package callback;

import java.io.File;

/**
 * @Author: Waterless
 * @Date: 2022/07/10/15:44
 * @Description: 文件信息扫描的回调接口
 * 一种程序设计思想 将两个互相独立的功能拆分为不同的方法，解耦
 * 但是这两个方法又是互相配合完成同一个功能
 */
public interface FileScanCallBack {
    /**
     * 文件扫描的回调接口
     * 扫描文件时由具体的子类决定将当前目录下的文件信息持久化到哪个终端
     * 可以是数据库，可以通过网络传输
     * @param dir
     */
    void callback(File dir);
}

