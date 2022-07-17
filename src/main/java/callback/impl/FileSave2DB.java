package callback.impl;

import app.FileMeta;
import callback.FileScanCallBack;
import org.w3c.dom.ls.LSInput;
import sun.awt.AWTAccessor;
import util.DBUtil;
import util.PinyinUtil;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: Waterless
 * @Date: 2022/07/10/16:10
 * @Description: 文件信息保存到数据库的回调子类
 */
public class FileSave2DB implements FileScanCallBack {
    @Override
    public void callback(File dir) {

        //列举出当前dir下的所有文件对象
        File[] files = dir.listFiles();
        //边界条件
        if (files != null && files.length != 0) {
            //1.先将当前dir下的所有文件信息保存到内存中 缓存中的信息一定是从os中读取到的最新数据
            List<FileMeta> locals = new ArrayList<>();
            //遍历文件
            for (File file : files) {
                FileMeta meta = new FileMeta();
                if (file.isDirectory()) {
                    //文件夹
                    setCommonFile(file.getName(),file.getParent(),true,file.lastModified(),meta);
                } else {
                    //文件
                    setCommonFile(file.getName(),file.getParent(),false,file.lastModified(),meta);
                    meta.setSize(file.length());
                }
                locals.add(meta);

            }
            //2.从数据库中查询出当前路径下的所有文件信息
            List<FileMeta> dbFiles = query(dir);
            //数据库有的，本地没有 删除
            //遍历dbFiles 本地不存在 删除
            //3. 对比 本地有 数据库没有的，做插入
            //遍历locals 若数据库不存在该FileMeta 就做插入
//            for (FileMeta meta : locals) {
//                if (!dbFiles.contains(meta)) {
//                    save(meta);
//                }
//            }
//            // 数据库有的 本地没有 做删除
//            //遍历dbFiles 本地不存在 做删除
            for (FileMeta meta : dbFiles) {
                if (!locals.contains(meta)) {
                    delete(meta);
                }
            }
            for (FileMeta meta : locals) {
                if (!dbFiles.contains(meta)) {
                    save(meta);
                }
            }
        }
        //若files = null || files.length == 0 说明该文件夹下就没有文件或dir就不是文件夹 啥也不干
    }

    //删除数据库中指定记录
    private void delete(FileMeta meta) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DBUtil.getConnection();
            //如何通过一个sql删除文件和文件夹
            //此时删除的是文件本身
            String sql = "delete from file_meta where" +
                    " (name = ? and path = ?)";
            if (meta.getIsDirectory()) {
                //还需要删除文件夹内部的子文件和子文件夹
                sql += " or path = ?"; //删除子文件夹的第一级目录
                sql += " or path like ?";//删除的是多级子目录
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1,meta.getName());
            ps.setString(2,meta.getPath());
            if (meta.getIsDirectory()) {
                ps.setString(3,meta.getPath() + File.separator + meta.getName());
                ps.setString(4,meta.getPath() + File.separator + meta.getName() +
                        File.separator + "%");
            }
//            System.out.println("执行删除操作 SQL为：" + ps);
            int rows = ps.executeUpdate();
//            if (meta.getIsDirectory()) {
//                System.out.println("删除文件夹 "+ meta.getName()+" 成功，共删除" + rows +"个文件");
//            } else {
//                System.out.println("删除文件 " + meta.getName() + "成功");
//            }
        } catch (SQLException e) {
            System.err.println("文件删除出错，请检查SQL语句");
            e.printStackTrace();
        } finally {
            DBUtil.close(ps);
        }
    }

    //将指定的文件对象信息保存到数据库中
    private void save(FileMeta meta) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DBUtil.getConnection();
            String sql = "insert into file_meta values(?,?,?,?,?,?,?)";
            ps = connection.prepareStatement(sql);
            String fileName = meta.getName();
            ps.setString(1,fileName);
            ps.setString(2,meta.getPath());
            ps.setBoolean(3,meta.getIsDirectory());
            if (!meta.getIsDirectory()) {
                //只有是文件的时候才设置size值
                ps.setLong(4,meta.getSize());
            }
            ps.setTimestamp(5,new Timestamp(meta.getLastModified().getTime()));
            //到底是否需要存入拼音 要看文件名是否包含中文
            //需要判断文件名是否是包含中文的
            if (PinyinUtil.containsChinese(fileName)) {
                String[] pinyins = PinyinUtil.getPinyinByFileName(fileName);
                //全拼
                ps.setString(6,pinyins[0]);
                //首字母
                ps.setString(7,pinyins[1]);
            }
//            System.out.println("执行文件的保存操作,SQL为: " + ps);
            int rows = ps.executeUpdate();
//            System.out.println("成功保存 " + rows + "行文件信息");
        } catch (SQLException e) {
            System.err.println("保存文件信息出错，请检查SQL语句");
            e.printStackTrace();
        } finally {
            DBUtil.close(ps);
        }
    }

    //查询数据库中指定路径下的所有文件信息
    private List<FileMeta> query(File dir) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FileMeta> dbFiles = new ArrayList<>();
        try {
            connection = DBUtil.getConnection();
            //查询指定路径下的所有文件
            String sql = "select name,path,is_directory,size,last_modified from file_meta" +
                    //sql拼接时 加 空格
                    " where path = ?";
            ps =connection.prepareStatement(sql);
            ps.setString(1,dir.getPath());
            rs = ps.executeQuery();
//            System.out.println("查询指定路径的SQL为：" + ps);
            while (rs.next()) {
                FileMeta meta = new FileMeta();
                meta.setName(rs.getString("name"));
                meta.setPath(rs.getString("path"));
                meta.setIsDirectory(rs.getBoolean("is_directory"));
                //获取时间戳 转为DATE类型
                meta.setLastModified(new Date(rs.getTimestamp("last_modified").getTime()));
                //只有是文件时 才设置size大小，若是文件夹，不设置size大小
                //此处有个bug 数据库文件夹的大小为null 但是调用rs.getLong方法 若返回值为null
                //会返回0 而不是null
                if (!meta.getIsDirectory()) {
                    //文件
                    meta.setSize(rs.getLong("size"));
                }
                dbFiles.add(meta);
            }

        } catch (SQLException e) {
            System.out.println("查询数据库指定路径下的文件出错，请检查SQL语句");
            e.printStackTrace();
        } finally {
            try {
                DBUtil.close(ps,rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return dbFiles;
    }

    //设置文件共有属性
    private void setCommonFile(String name, String path, boolean isDir, Long lastModified, FileMeta meta) {
        meta.setName(name);
        meta.setPath(path);
        //文件夹不设置大小
        meta.setIsDirectory(isDir);
        //file对象的lastModified是一个长整型，以时间戳为单位
        meta.setLastModified(new Date(lastModified));
    }
}
