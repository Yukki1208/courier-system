-- ============================================================
-- 快递管理系统 建库脚本
-- 使用方法：在 MySQL 命令行或 Navicat 中执行本文件
--          mysql -u root -p < sql/courier.sql
-- ============================================================

DROP DATABASE IF EXISTS courier_db;
CREATE DATABASE courier_db DEFAULT CHARACTER SET utf8mb4;
USE courier_db;

-- 快递单表：一条记录就是一件快递
CREATE TABLE courier (
  id             INT          PRIMARY KEY AUTO_INCREMENT COMMENT '主键，自增',
  no             VARCHAR(20)  NOT NULL UNIQUE               COMMENT '运单号',
  sender         VARCHAR(20)  NOT NULL                      COMMENT '寄件人',
  sender_phone   VARCHAR(20)                                COMMENT '寄件人电话',
  receiver       VARCHAR(20)  NOT NULL                      COMMENT '收件人',
  receiver_phone VARCHAR(20)                                COMMENT '收件人电话',
  address        VARCHAR(100)                               COMMENT '收件地址',
  status         VARCHAR(10)  NOT NULL DEFAULT '待取件'      COMMENT '状态：待取件/运输中/已签收',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间'
);

-- 初始化几条测试数据，方便直接演示
INSERT INTO courier (no, sender, sender_phone, receiver, receiver_phone, address, status) VALUES
('SF1001', '张三', '13800000001', '李四', '13900000001', '北京市海淀区中关村大街1号',   '待取件'),
('SF1002', '王五', '13800000002', '赵六', '13900000002', '上海市浦东新区世纪大道100号', '运输中'),
('YT2001', '孙七', '13800000003', '周八', '13900000003', '广州市天河区体育西路8号',     '已签收');
