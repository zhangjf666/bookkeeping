INSERT INTO `sys_role` VALUES (11, '超级管理员', '所有权限', '1', 'system', '2020-6-10 16:45:34', 'system', '2020-6-10 16:45:34');
INSERT INTO `sys_role` VALUES (21, '普通用户', '普通权限', '2', 'system', '2020-6-10 16:46:54', 'system', '2020-6-10 16:46:54');

INSERT INTO `sys_user` VALUES (51, 1, 'admin', '$2a$10$iEpGPe7J2Zj23juaZVCi7.XR/GlzuK3IDkzd8SGPSkPn64xUNvOsG', '管理员', '0001', '1', 'admin@qq.com', NULL, '13811112222', '1', NULL, '1', 'system', '2020-6-10 16:34:38', 'system', '2020-6-10 16:34:47', NULL, '0');
INSERT INTO `sys_user` VALUES (61, 1, 'zjf', '$2a$10$dG6vuXDol6Kfiwxo2shd/u9b6x8bjCrQASjWDMV5ehTxMoOMp5O9m', '张小峰', '0002', '1', 'zjf@qq.com', NULL, '13811112222', '0', NULL, '1', 'system', '2020-6-10 16:36:51', 'system', '2020-6-10 16:36:51', NULL, '0');

INSERT INTO `sys_user_role` VALUES (1, 51, 11);
INSERT INTO `sys_user_role` VALUES (11, 61, 21);

INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('tianjiajihua','1'),
	 ('jinbi','1'),
	 ('dingjiaziyuanwei','1'),
	 ('xinzi','1'),
	 ('jiangjin','1'),
	 ('touzishouyi','1'),
	 ('qitashouru','1'),
	 ('leqi','0'),
	 ('dianqi','0'),
	 ('yinliao','0');
INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('yaopin','0'),
	 ('yinyue','0'),
	 ('qingke','0'),
	 ('caipiao','0'),
	 ('baoxian','0'),
	 ('jipiao','0'),
	 ('tongxun','0'),
	 ('lingshiyanjiu','0'),
	 ('baobei','0'),
	 ('regou','0');
INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('lingshi','0'),
	 ('hanbao','0'),
	 ('meizhuang','0'),
	 ('juanzhi','0'),
	 ('bangqiu','0'),
	 ('wanju','0'),
	 ('dianyundou','0'),
	 ('shouyinji','0'),
	 ('zhangpeng','0'),
	 ('erhuan','0');
INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('qipao','0'),
	 ('yinle1','0'),
	 ('xiyiji','0'),
	 ('kongdiao','0'),
	 ('jieri','0'),
	 ('yiliao','0'),
	 ('shipin','0'),
	 ('dengshanxie','0'),
	 ('pingbandiannao','0'),
	 ('bingxiang1','0');
INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('yingerche','0'),
	 ('Txu','0'),
	 ('naifen','0'),
	 ('MP3','0'),
	 ('qiche','0'),
	 ('zixingche','0'),
	 ('riyong','0'),
	 ('neiyi','0'),
	 ('xiefu','0'),
	 ('lipinlijin','0');
INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('canyin','0'),
	 ('xianshiqi','0'),
	 ('zhaoxiangji','0'),
	 ('shiwu','0'),
	 ('youxi','0'),
	 ('lianyiqun','0'),
	 ('gaogenxie','0'),
	 ('nvbao','0'),
	 ('yundong','0'),
	 ('kuzi','0');
INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('jiaju','0'),
	 ('naiping','0'),
	 ('tushu','0'),
	 ('shoubiao','0'),
	 ('shuma','0'),
	 ('zhiwu','0'),
	 ('wuyeshuidian','0'),
	 ('wenjiao','0'),
	 ('xingqudingxiang','0'),
	 ('qita','0');
INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('renqing','0'),
	 ('biaoqian','0'),
	 ('dianpu1','0'),
	 ('kge','0'),
	 ('gouwucheman','0'),
	 ('dianying','0'),
	 ('fangshai','0'),
	 ('chongdian','0'),
	 ('shumiao','0'),
	 ('yinger','0');
INSERT INTO bookkeeping.icon_config (code,`type`) VALUES
	 ('yuer','0'),
	 ('shuiguo','0'),
	 ('jiafang','0'),
	 ('huapen','0'),
	 ('huoche','0'),
	 ('gongjiao','0'),
	 ('gouwu','0'),
	 ('yule','0'),
	 ('jiaotong','0'),
	 ('lvxing','0');