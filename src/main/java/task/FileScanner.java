package task;

import app.FileMeta;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: Waterless
 * @Date: 2022/07/09/15:25
 * @Description: 进行文件的扫描任务
 */

@Getter
public class FileScanner {
    //当前扫描的文件个数
    private int fileNum;
    //当前扫描的文件夹个数
    //最开始扫描的根路径没有统计，因此初始化文件夹的个数为1，表示从根目录下开始进行扫描任务
    private int dirNum = 1;
    //扫描的所有文件信息
    List<FileMeta> fileMetas = new ArrayList<>();

    /**
     * 根据传入的文件夹进行扫描任务
     * @param filePath 要扫描的文件夹
     */
    public void scan (File filePath) {
        //终止条件
        if (filePath == null) {
            return;
        }
        //先将当前目录下的file对象获取出来
        File[] files = filePath.listFiles();
        //遍历这些file对象，根据是否是文件夹 进行区别处理
        for (File file : files) {
            FileMeta meta = new FileMeta();
            if (file.isDirectory()) {
                //是文件夹继续递归扫描
                //文件夹不设置大小
                //file对象的lastModified是一个长整型，以时间戳为单位
                setCommonFile(file.getName(),file.getPath(),true,file.lastModified(),meta);
                //将当前文件夹保存到list集合中
                fileMetas.add(meta);
                dirNum++;
                scan(file);
            } else {
                //是个文件
                setCommonFile(file.getName(),file.getPath(),false,file.lastModified(),meta);
                //文件有大小，file.length()默认以字节为单位 长整型
                meta.setSize(file.length());
                //保存文件
                fileMetas.add(meta);
                fileNum++;
            }
        }
    }
    //设置文件共有属性
    private void setCommonFile(String name,String path,boolean isDir,Long lastModified,FileMeta meta) {
        meta.setName(name);
        meta.setPath(path);
        //文件夹不设置大小
        meta.setIsDirectory(isDir);
        //file对象的lastModified是一个长整型，以时间戳为单位
        meta.setLastModified(new Date(lastModified));
    }
}
