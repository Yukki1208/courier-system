package com.example.courier;

/**
 * 快递单实体类，一个对象对应 courier 表中的一条记录。
 * 为了代码简短，这里直接使用 public 字段。
 */
public class Courier {

    public int id;               // 主键
    public String no;            // 运单号
    public String sender;        // 寄件人
    public String senderPhone;   // 寄件人电话
    public String receiver;      // 收件人
    public String receiverPhone; // 收件人电话
    public String address;       // 收件地址
    public String status;        // 状态：待取件 / 运输中 / 已签收
    public String createTime;    // 录入时间
}
