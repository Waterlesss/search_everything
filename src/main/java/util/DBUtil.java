package util;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;


import javax.sql.DataSource;
import java.io.File;
import java.sql.*;

/**
 * @Author: Waterless
 * @Date: 2022/07/06/19:58
 * @Description: SQLite数据库的工具类，创建数据源，创建数据库的连接
 * 只向外部提供SQLite数据库的连接即可，数据源不提供(封装在工具类的内部)
 * 无论是哪种关系型数据库，操作的流程都是JDBC四步走
 */
public class DBUtil {
    //单例数据源
    private volatile static DataSource dataSource;
    
    //单例数据库连接
    private volatile static Connection connection;
    
    //获取数据源方法，使用double-check单例模式获取数据源对象
    private static DataSource getDataSource() {
        if (dataSource == null) {
            //多线程场景下，只有一个线程能进入同步代码块
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    // SQLite没有账户密码，只需要配置日期格式即可 SQLite的默认日期格式是时间戳
                    SQLiteConfig config = new SQLiteConfig();
                    config.setDateStringFormat(Util.DATE_PATTERN);
                    dataSource = new SQLiteDataSource(config);
                    // 配置数据源的URL是SQLite子类独有的方法，因此向下转型
                    ((SQLiteDataSource) dataSource).setUrl(getUrl());
                }   
            }
        }
        return  dataSource;
    }

    /**
     * 配置SQLite数据库的地址
     * mysql: jdbc:mysql://127.0.0.1:3306/数据库名称？
     *对于SQLite数据库来说，没有服务端和客户端
     * 因此只需要指定SQLite数据库的地址即可
     * @return
     */
    private static String getUrl() {
        String path = "E:\\code\\maven\\search_everything\\target";
        //File类的separator常量，不同操作系统的文件分隔符 win下是\ 类Unix 都是 /
        String url = "jdbc:sqlite://" + path + File.separator + "search_everything.db";
        System.out.println("获取数据库的连接为 : " + url);
        return url;
    }
    //多线程场景下，SQLite要求多个线程使用同一个连接
    public static Connection getConnection() throws SQLException {
//        //相当于返回 datasource.getConnection();
//        return getDataSource().getConnection();
        if (connection == null) {
            synchronized (DBUtil.class) {
                if (connection == null) {
                    connection = getDataSource().getConnection();
                }
            }
        }
        return connection;
    }

    public static void main(String[] args) throws SQLException {
        System.out.println(getConnection());
    }
    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    //重载关闭方法
    public static void close( PreparedStatement ps, ResultSet rs) throws SQLException {
        close(ps);
        if (rs != null) {
//            try {
                rs.close();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
        }
    }
}
