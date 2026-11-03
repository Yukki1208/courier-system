package com.example.courier;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 程序入口：控制台菜单。
 * 运行前请先执行 sql/courier.sql 建库建表，并修改 DBUtil 里的数据库密码。
 */
public class Main {

    private static final Scanner IN = new Scanner(System.in);
    private static final CourierDao DAO = new CourierDao();

    public static void main(String[] args) {
        System.out.println("************* 快递管理系统 *************");
        while (true) {
            System.out.println();
            System.out.println("  1. 录入快递      2. 查看全部      3. 按运单号查询");
            System.out.println("  4. 修改状态      5. 删除快递      6. 统计");
            System.out.println("  0. 退出");
            System.out.print("请选择功能：");

            String choice = IN.nextLine().trim();
            try {
                switch (choice) {
                    case "1": add(); break;
                    case "2": list(); break;
                    case "3": find(); break;
                    case "4": updateStatus(); break;
                    case "5": delete(); break;
                    case "6": count(); break;
                    case "0":
                        System.out.println("已退出系统，再见！");
                        return;
                    default:
                        System.out.println("没有这个选项，请重新输入。");
                }
            } catch (SQLException e) {
                // 任何数据库异常都在这里统一提示，程序不会崩掉
                System.out.println("数据库操作失败：" + e.getMessage());
            }
        }
    }

    /** 1. 录入快递 */
    private static void add() throws SQLException {
        Courier c = new Courier();
        System.out.print("运单号：");
        c.no = IN.nextLine().trim();
        System.out.print("寄件人：");
        c.sender = IN.nextLine().trim();
        System.out.print("寄件人电话：");
        c.senderPhone = IN.nextLine().trim();
        System.out.print("收件人：");
        c.receiver = IN.nextLine().trim();
        System.out.print("收件人电话：");
        c.receiverPhone = IN.nextLine().trim();
        System.out.print("收件地址：");
        c.address = IN.nextLine().trim();
        c.status = "待取件";

        if (c.no.isEmpty() || c.sender.isEmpty() || c.receiver.isEmpty()) {
            System.out.println("运单号、寄件人、收件人都不能为空，录入失败。");
            return;
        }
        if (DAO.findByNo(c.no) != null) {
            System.out.println("该运单号已存在，录入失败。");
            return;
        }
        DAO.add(c);
        System.out.println("录入成功，当前状态：待取件。");
    }

    /** 2. 查看全部 */
    private static void list() throws SQLException {
        List<Courier> list = DAO.findAll();
        if (list.isEmpty()) {
            System.out.println("暂无快递记录。");
            return;
        }
        System.out.println(pad("ID", 4) + pad("运单号", 12) + pad("寄件人", 10) + pad("收件人", 10)
                + pad("收件地址", 30) + pad("状态", 10) + "录入时间");
        for (Courier c : list) {
            System.out.println(pad(String.valueOf(c.id), 4) + pad(c.no, 12) + pad(c.sender, 10)
                    + pad(c.receiver, 10) + pad(c.address, 30) + pad(c.status, 10) + c.createTime);
        }
        System.out.println("共 " + list.size() + " 条记录。");
    }

    /** 3. 按运单号查询 */
    private static void find() throws SQLException {
        System.out.print("请输入运单号：");
        String no = IN.nextLine().trim();
        Courier c = DAO.findByNo(no);
        if (c == null) {
            System.out.println("没有找到运单号为 " + no + " 的快递。");
            return;
        }
        System.out.println("运单号：" + c.no);
        System.out.println("寄件人：" + c.sender + "（" + c.senderPhone + "）");
        System.out.println("收件人：" + c.receiver + "（" + c.receiverPhone + "）");
        System.out.println("收件地址：" + c.address);
        System.out.println("当前状态：" + c.status);
        System.out.println("录入时间：" + c.createTime);
    }

    /** 4. 修改状态 */
    private static void updateStatus() throws SQLException {
        System.out.print("请输入运单号：");
        String no = IN.nextLine().trim();
        if (DAO.findByNo(no) == null) {
            System.out.println("没有找到该快递。");
            return;
        }
        System.out.print("请选择新状态（1-待取件  2-运输中  3-已签收）：");
        String s = IN.nextLine().trim();
        String status;
        if ("1".equals(s)) {
            status = "待取件";
        } else if ("2".equals(s)) {
            status = "运输中";
        } else if ("3".equals(s)) {
            status = "已签收";
        } else {
            System.out.println("输入有误，修改失败。");
            return;
        }
        DAO.updateStatus(no, status);
        System.out.println("修改成功，当前状态：" + status);
    }

    /** 5. 删除快递 */
    private static void delete() throws SQLException {
        System.out.print("请输入要删除的运单号：");
        String no = IN.nextLine().trim();
        System.out.print("确认删除吗？（y/n）：");
        String ok = IN.nextLine().trim();
        if (!"y".equalsIgnoreCase(ok)) {
            System.out.println("已取消删除。");
            return;
        }
        if (DAO.delete(no)) {
            System.out.println("删除成功。");
        } else {
            System.out.println("没有找到该运单号，删除失败。");
        }
    }

    /** 6. 统计各状态数量 */
    private static void count() throws SQLException {
        Map<String, Integer> map = DAO.countByStatus();
        if (map.isEmpty()) {
            System.out.println("暂无快递数据。");
            return;
        }
        int total = 0;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            System.out.println(pad(e.getKey(), 10) + e.getValue() + " 件");
            total += e.getValue();
        }
        System.out.println("总计：" + total + " 件");
    }

    /**
     * 让中英文混排也能对齐：中文按 2 个字符宽计算，不足 width 就补空格。
     */
    private static String pad(String s, int width) {
        if (s == null) {
            s = "";
        }
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            len += s.charAt(i) > 127 ? 2 : 1;
        }
        StringBuilder sb = new StringBuilder(s);
        for (int i = len; i < width; i++) {
            sb.append(' ');
        }
        return sb.append(' ').toString(); // 列之间至少空一格
    }
}
