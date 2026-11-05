package com.example.courier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库工具类：加载 MySQL 驱动、获取数据库连接。
 * 只需要修改下面的 URL / USER / PASSWORD 就能连上自己的数据库。
 */
public class DBUtil {

    /** 数据库地址，courier_db 是库名；几个参数缺一不可 */
    private static final String URL =
            "jdbc:mysql://localhost:3306/courier_db"
            + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
            // MySQL 8 默认 caching_sha2_password，不加这个参数在服务重启后会报
            // “Public Key Retrieval is not allowed”
            + "&allowPublicKeyRetrieval=true";

    /** 数据库用户名 */
    private static final String USER = "root";

    /** 数据库密码，请改成自己电脑上的密码（这里留占位符，不要提交真实密码） */
    private static final String PASSWORD = "Akun@1234";

    // 静态代码块：类加载时注册一次驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("找不到 MySQL 驱动，请检查 mysql-connector-j 依赖是否添加", e);
        }
    }

    /** 获取一个数据库连接 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
