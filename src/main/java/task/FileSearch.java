package task;

import app.FileMeta;
import util.DBUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: Waterless
 * @Date: 2022/07/12/15:39
 * @Description: 根据选择的文件夹路径和用户输入的内容
 * 从数据库中查找出指定的内容并返回
 */
public class FileSearch {
    /**
     * @param dir     用户选择的检索的文件夹路径 一定是不为空的
     * @param content 用户搜索框中的内容 - 可能为空，若为空就展示当前数据库中选择的路径下的所有内容即可
     * @return
     */
    public static List<FileMeta> search(String dir, String content) {
        //先从数据库中查内容
        List<FileMeta> result = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DBUtil.getConnection();
            // 先根据用户选择的文件夹dir查询内容
            String sql = "select name,path,size,is_directory,last_modified from file_meta " +
                    " where (path = ? or path like ?)";
            if (content != null && content.trim().length() != 0) {
                // 此时用户搜索框中的内容不为空,此处支持文件全名称，拼音全名称，以及拼音首字母的模糊查询
                sql += " and (name like ? or pinyin like ? or pinyin_first like ?)";
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1, dir);
            ps.setString(2, dir + File.separator + "%");
            // 根据搜索框的内容查询数据库，都是模糊匹配
            if (content != null && content.trim().length() != 0) {
                ps.setString(3, "%" + content + "%");
                ps.setString(4, "%" + content + "%");
                ps.setString(5, "%" + content + "%");
            }
//            System.out.println("正在从数据库中检索信息,sql为:" + ps);
            //执行sql 取得返回值，也是一个对象，存在ResultSet的这个对象中
            rs = ps.executeQuery();
            //遍历结果集
            while (rs.next()) {
                FileMeta meta = new FileMeta();
                meta.setName(rs.getString("name"));
                meta.setPath(rs.getString("path"));
                meta.setIsDirectory(rs.getBoolean("is_directory"));
                if (!meta.getIsDirectory()) {
                    // 是文件，保存大小
                    meta.setSize(rs.getLong("size"));
                }
                meta.setLastModified(new Date(rs.getTimestamp("last_modified").getTime()));
//                System.out.println("检索到文件信息 : name = " + meta.getName() + ",path = " + meta.getPath());
                result.add(meta);
            }
        } catch (SQLException e) {
            System.err.println("从数据库中搜索用户查找内容时出错，请检查SQL语句");
            e.printStackTrace();
        } finally {
            try {
                DBUtil.close(ps, rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
