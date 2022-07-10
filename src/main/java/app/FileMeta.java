package app;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import util.Util;

import java.util.Date;

/**
 * @Author: Waterless
 * @Date: 2022/07/09/15:13
 * @Description: 实体类 使用此类完成和数据库数据表的映射关系
 * 该类的一个对象就对应数据表的一行数据
 */
@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class FileMeta {
    private String name;
    private String path;
    private Boolean isDirectory;
    private Long size;
    private Date lastModified;
    // 若包含中文名称，名称全拼
    private String pinYin;
    // 拼音首字母
    private String pinYinFirst;

    //以下三个属性需要在界面中展示，将当前属性值做处理之后展示
    //属性名要和app.fxml中保持一致
    //文件类型
    private String isDirectoryText;
    //文件大小
    private String sizeText;
    //上次修改时间
    private String lastModifiedText;
    //重载注解生成的setSize方法
    public void setSize(Long size) {
        this.size = size;
        this.sizeText = Util.parseSize(size);
    }
    //重载注解生成的setIsDirectory方法
    public void setIsDirectory(Boolean directory) {
        isDirectory = directory;
        this.isDirectoryText = Util.parseFileType( directory);
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        this.lastModifiedText = Util.parseDate(lastModified);
    }

    public FileMeta(String name, String path, Boolean isDirectory, Long size, Date lastModified) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.size = size;
        this.lastModified = lastModified;
    }
}
