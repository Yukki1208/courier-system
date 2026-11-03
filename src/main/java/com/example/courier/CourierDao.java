package com.example.courier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据访问层：所有 SQL 语句都集中在这一个类里（增、删、改、查）。
 * 使用 PreparedStatement 传参，可以防止 SQL 注入。
 */
public class CourierDao {

    /** 录入快递（状态默认“待取件”） */
    public void add(Courier c) throws SQLException {
        String sql = "INSERT INTO courier(no, sender, sender_phone, receiver, receiver_phone, address, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.no);
            ps.setString(2, c.sender);
            ps.setString(3, c.senderPhone);
            ps.setString(4, c.receiver);
            ps.setString(5, c.receiverPhone);
            ps.setString(6, c.address);
            ps.setString(7, c.status);
            ps.executeUpdate();
        }
    }

    /** 查询全部快递，新录入的排在前面 */
    public List<Courier> findAll() throws SQLException {
        String sql = "SELECT * FROM courier ORDER BY id DESC";
        List<Courier> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(toCourier(rs));
            }
        }
        return list;
    }

    /** 按运单号查询，查不到返回 null */
    public Courier findByNo(String no) throws SQLException {
        String sql = "SELECT * FROM courier WHERE no = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, no);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? toCourier(rs) : null;
            }
        }
    }

    /** 修改快递状态，返回是否修改成功 */
    public boolean updateStatus(String no, String status) throws SQLException {
        String sql = "UPDATE courier SET status = ? WHERE no = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, no);
            return ps.executeUpdate() > 0;
        }
    }

    /** 按运单号删除，返回是否删除成功 */
    public boolean delete(String no) throws SQLException {
        String sql = "DELETE FROM courier WHERE no = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, no);
            return ps.executeUpdate() > 0;
        }
    }

    /** 统计每种状态各有多少件 */
    public Map<String, Integer> countByStatus() throws SQLException {
        String sql = "SELECT status, COUNT(*) AS num FROM courier GROUP BY status ORDER BY num DESC";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("num"));
            }
        }
        return map;
    }

    /** 把结果集的一行封装成 Courier 对象 */
    private Courier toCourier(ResultSet rs) throws SQLException {
        Courier c = new Courier();
        c.id = rs.getInt("id");
        c.no = rs.getString("no");
        c.sender = rs.getString("sender");
        c.senderPhone = rs.getString("sender_phone");
        c.receiver = rs.getString("receiver");
        c.receiverPhone = rs.getString("receiver_phone");
        c.address = rs.getString("address");
        c.status = rs.getString("status");
        c.createTime = rs.getString("create_time");
        return c;
    }
}
