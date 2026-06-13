SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`) VALUES ('admin', '123456', '管理员');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES ('worker', '123456', '张工人', 'worker');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES ('warehouse', '123456', '李仓管', 'warehouse');

-- 岗位表
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position_name` varchar(100) NOT NULL COMMENT '岗位名称',
  `position_type` varchar(20) NOT NULL DEFAULT 'indoor' COMMENT '岗位类型（indoor-室内，outdoor-室外）',
  `hourly_wage` decimal(10,2) DEFAULT '0.00' COMMENT '时薪（元/小时）',
  `heat_allowance` decimal(10,2) DEFAULT '0.00' COMMENT '高温津贴（元/天）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_position_type` (`position_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

INSERT INTO `position` (`position_name`, `position_type`, `hourly_wage`, `heat_allowance`, `remark`) VALUES
('仓库管理员', 'indoor', 25.00, 0.00, '室内办公'),
('现场搬运工', 'outdoor', 30.00, 30.00, '室外露天作业'),
('塔吊司机', 'outdoor', 35.00, 35.00, '高空室外作业'),
('钢筋工', 'outdoor', 32.00, 32.00, '室外露天作业');

-- 排班表
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `position_id` bigint(20) NOT NULL COMMENT '岗位ID',
  `position_name` varchar(100) DEFAULT NULL COMMENT '岗位名称（冗余）',
  `position_type` varchar(20) DEFAULT NULL COMMENT '岗位类型（冗余）',
  `worker_id` bigint(20) NOT NULL COMMENT '工人ID',
  `worker_name` varchar(100) DEFAULT NULL COMMENT '工人姓名（冗余）',
  `start_time` datetime DEFAULT NULL COMMENT '上班时间',
  `end_time` datetime DEFAULT NULL COMMENT '下班时间',
  `work_hours` decimal(5,2) DEFAULT '0.00' COMMENT '工时（小时）',
  `temperature` decimal(5,2) DEFAULT NULL COMMENT '当日气温（摄氏度）',
  `heat_warning` tinyint(1) DEFAULT '0' COMMENT '高温预警标识（0-否，1-是）',
  `heat_allowance` decimal(10,2) DEFAULT '0.00' COMMENT '当日高温津贴',
  `rest_frequency` int(11) DEFAULT '0' COMMENT '休息频次（次/天）',
  `schedule_status` varchar(20) DEFAULT 'pending' COMMENT '排班状态（pending-待执行，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_date` (`schedule_date`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_position_id` (`position_id`),
  KEY `idx_schedule_status` (`schedule_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 防暑物资表
DROP TABLE IF EXISTS `heat_supply`;
CREATE TABLE `heat_supply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supply_name` varchar(100) NOT NULL COMMENT '物资名称',
  `supply_code` varchar(50) NOT NULL COMMENT '物资编码',
  `supply_type` varchar(20) DEFAULT 'heat' COMMENT '物资类型（heat-防暑物资）',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位（袋、副、张等）',
  `stock_quantity` int(11) DEFAULT '0' COMMENT '库存数量',
  `description` varchar(500) DEFAULT NULL COMMENT '物资描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supply_code` (`supply_code`),
  KEY `idx_supply_type` (`supply_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='防暑物资表';

INSERT INTO `heat_supply` (`supply_name`, `supply_code`, `supply_type`, `unit`, `stock_quantity`, `description`) VALUES
('盐丸', 'SALT_001', 'heat', '袋', 500, '口服补液盐，补充电解质'),
('冰袖', 'SLEEVE_001', 'heat', '副', 200, '防晒冰袖，降温防晒'),
('饮水券', 'WATER_001', 'heat', '张', 1000, '饮用水兑换券');

-- 物资发放记录表
DROP TABLE IF EXISTS `supply_record`;
CREATE TABLE `supply_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_no` varchar(50) NOT NULL COMMENT '发放单号',
  `supply_id` bigint(20) NOT NULL COMMENT '物资ID',
  `supply_name` varchar(100) DEFAULT NULL COMMENT '物资名称（冗余）',
  `supply_code` varchar(50) DEFAULT NULL COMMENT '物资编码（冗余）',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位（冗余）',
  `quantity` int(11) DEFAULT '1' COMMENT '发放数量',
  `worker_id` bigint(20) NOT NULL COMMENT '领取工人ID',
  `worker_name` varchar(100) DEFAULT NULL COMMENT '领取工人姓名（冗余）',
  `schedule_id` bigint(20) DEFAULT NULL COMMENT '关联排班ID',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '发放人ID（仓管）',
  `warehouse_name` varchar(100) DEFAULT NULL COMMENT '发放人姓名（冗余）',
  `issue_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发放时间',
  `issue_status` varchar(20) DEFAULT 'confirmed' COMMENT '发放状态（pending-待确认，confirmed-已确认，cancelled-已取消）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_supply_id` (`supply_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_issue_time` (`issue_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资发放记录表';

-- 工时工资结算表
DROP TABLE IF EXISTS `work_hour`;
CREATE TABLE `work_hour` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `settle_month` varchar(7) NOT NULL COMMENT '结算月份（YYYY-MM）',
  `worker_id` bigint(20) NOT NULL COMMENT '工人ID',
  `worker_name` varchar(100) DEFAULT NULL COMMENT '工人姓名（冗余）',
  `normal_hours` decimal(10,2) DEFAULT '0.00' COMMENT '普通工时（小时）',
  `normal_wage` decimal(10,2) DEFAULT '0.00' COMMENT '普通工资（元）',
  `heat_days` int(11) DEFAULT '0' COMMENT '高温天数',
  `heat_hours` decimal(10,2) DEFAULT '0.00' COMMENT '高温工时（小时）',
  `heat_allowance_total` decimal(10,2) DEFAULT '0.00' COMMENT '高温津贴合计（元）',
  `total_wage` decimal(10,2) DEFAULT '0.00' COMMENT '工资合计（元）',
  `settle_status` varchar(20) DEFAULT 'pending' COMMENT '结算状态（pending-待结算，settled-已结算，paid-已发放）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_month_worker` (`settle_month`, `worker_id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_settle_month` (`settle_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工时工资结算表';

-- 高温配置表
DROP TABLE IF EXISTS `heat_config`;
CREATE TABLE `heat_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(255) NOT NULL COMMENT '配置值',
  `config_name` varchar(100) DEFAULT NULL COMMENT '配置名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高温配置表';

INSERT INTO `heat_config` (`config_key`, `config_value`, `config_name`, `description`) VALUES
('heat_threshold', '35', '高温阈值', '气温达到此温度触发高温预警（摄氏度）'),
('rest_frequency_below_37', '3', '35-37℃休息频次', '35-37℃之间每2小时休息一次的次数（次/天）'),
('rest_frequency_37_40', '4', '37-40℃休息频次', '37-40℃之间的休息频次（次/天）'),
('rest_frequency_above_40', '6', '40℃以上休息频次', '40℃以上的休息频次（次/天）'),
('work_stop_temp', '40', '停工温度', '达到此温度停止室外作业（摄氏度）');

-- 修改用户表，增加角色字段
ALTER TABLE `user` ADD COLUMN `role` varchar(20) DEFAULT 'worker' COMMENT '用户角色（admin-管理员，worker-工人，warehouse-仓管）' AFTER `nickname`;
UPDATE `user` SET `role` = 'admin' WHERE `username` = 'admin';

SET FOREIGN_KEY_CHECKS = 1;
