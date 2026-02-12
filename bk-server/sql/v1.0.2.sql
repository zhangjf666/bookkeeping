--用户标签表
CREATE TABLE `user_tag` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                               `code` bigint(10) NOT NULL COMMENT '标签代码',
                               `user_id` bigint(20) NOT NULL COMMENT '用户id',
                               `name` varchar(100) NOT NULL COMMENT '标签名称',
                               `color` varchar(50) NOT NULL COMMENT '标签颜色',
                               `pinned` CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否置顶(0:不置顶,1:置顶)',
                               `create_time` datetime NOT NULL COMMENT '创建时间',
                               `update_time` datetime NOT NULL COMMENT '更新时间',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT='用户标签表';

--增加标签tag_codes列
ALTER TABLE income_expense ADD tag_codes varchar(512) NULL COMMENT '所属标签代码,逗号分隔';