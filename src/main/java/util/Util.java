package util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: Waterless
 * @Date: 2022/07/06/20:01
 * @Description: 通用工具类
 */
public class Util {
    public static final  String DATE_PATTERN = "yyy-MM-dd HH:mm:ss";

    //根据传入的文件大小 返回不同的单位
    //单位：B KB MB GB
    public static String parseSize(Long size) {
        String[] unit = {"B","KB","MB","GB"};
        int flag = 0;
        while (size > 1024) {
            size /= 1024;
            flag++;
        }
        return  size +unit[flag];
    }

    public static String parseFileType(Boolean directory) {
        return directory ? "文件夹" : "文件";
    }

    public static String parseDate(Date lastModified) {
        return new SimpleDateFormat(DATE_PATTERN).format(lastModified);
    }
}
