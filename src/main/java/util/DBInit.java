package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @Author: Waterless
 * @Date: 2022/07/06/20:12
 * @Description: 界面初始化时创建文件信息数据表
 */
public class DBInit {
    /**
     * 从resources路径下读取init.sql文件，加载到程序中
     * 文件IO
     * @return
     */
    public static List<String> readSQL() {
        List<String> ret = new ArrayList<>();
        try {
    //从init.sql文件中获取内容，需要拿到文件的输入流
            InputStream in = DBInit.class.getClassLoader().
                    getResourceAsStream("init.sql");
    //输入流用Scanner，输出流使用PrintStream
            //从文件获取输入流
            Scanner scan = new Scanner(in);
            //自定义分隔符 用 ; 作为分隔符
            scan.useDelimiter(";");
            //nextLine默认碰到换行分隔  next按照自定义的分隔符分隔
            while (scan.hasNext()) {
                String str = scan.next();
//                if ("".equals(str) || "\n".equals(str)) {
//                    continue;
//                }
                if (str.contains("--")) {
                   str = str.replaceAll("--","");
                }
                ret.add(str);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        System.out.println("读取到的sql内容为");
//        System.out.println(ret);
        return  ret;
    }

    /**
     * 在界面初始化的时候先初始化数据库，创建数据表
     */
    public static void init() {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DBUtil.getConnection();
            // 获取要执行的sql语句
            List<String> sqls = readSQL();
            // 这里咱采用了普通的Statement接口，没有用PrepareStatement
            statement = connection.createStatement();
            for (String sql : sqls) {
                System.out.println("执行SQL操作 : " + sql);
                statement.executeUpdate(sql);
            }
        }catch (SQLException e) {
            System.err.println("数据库初始化失败");
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement);
        }
    }

//    public static void main(String[] args) {
//        //采用类加载器的方式创建资源文件
//        //JVM在加载类的时候用到的ClassLoader类
//        //处理相对路径的通用写法
////        InputStream in = DBInit.class.getClassLoader()//获取到编译后的classes目录
////                .getResourceAsStream("init.sql");
////        //所谓的类加载器简单理解就是告诉JVM从哪个文件夹去执行class文件
////        System.out.println(in);
////        readSQL();
//        init();
//    }
}
