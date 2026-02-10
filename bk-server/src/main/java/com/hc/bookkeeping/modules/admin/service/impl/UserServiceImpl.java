package com.hc.bookkeeping.modules.admin.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.exception.DataNotExsitException;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.RoleDto;
import com.hc.bookkeeping.modules.admin.dto.UserDto;
import com.hc.bookkeeping.modules.admin.dto.UserQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Role;
import com.hc.bookkeeping.modules.admin.entity.User;
import com.hc.bookkeeping.modules.admin.entity.UserRole;
import com.hc.bookkeeping.modules.admin.mapper.UserMapper;
import com.hc.bookkeeping.modules.admin.mapper.UserRoleMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.UserMapstruct;
import com.hc.bookkeeping.modules.admin.service.RoleService;
import com.hc.bookkeeping.modules.admin.service.UserService;
import com.hc.bookkeeping.modules.bkeeping.entity.AccountBook;
import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import com.hc.bookkeeping.modules.bkeeping.entity.UserConfig;
import com.hc.bookkeeping.modules.bkeeping.mapper.AccountBookMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.ClassifyMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserConfigMapper;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import com.hc.bookkeeping.modules.security.dto.RegisterUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapstruct, UserDto, UserMapper, User> implements UserService {
    private static String DEFAULT_PASSWORD = "123456";

    private final UserRoleMapper userRoleMapper;
    
    private final RoleService roleService;
    
    private final UserConfigMapper userConfigMapper;

    private final AccountBookMapper accountBookMapper;

    private final ClassifyMapper classifyMapper;

    @Override
    public UserDto findByUsername(String username) {
        User user = baseMapper.findByUsername(username);
        if(user == null){
            throw new DataNotExsitException();
        }
        return baseMapstruct.toDto(user);
    }

    @Override
    public Page<UserDto> queryPage(UserQueryDto queryDto, Page page) {
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), QueryUtil.bulid(queryDto)));
        rpage.setRecord(baseMapstruct.toDto(rpage.getRecord()));
        return rpage;
    }

    @Override
    public List<UserDto> query(UserQueryDto queryDto) {
        return baseMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserDto queryById(Serializable id) {
        UserDto dto = super.queryById(id);
        dto.setRoles(roleService.getRoleByUserId((Long) id));
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserDto create(UserDto dto) {
        User user = baseMapstruct.toEntity(dto);
        user.setPassword(new BCryptPasswordEncoder().encode(DEFAULT_PASSWORD));
        baseMapper.insert(user);
        for (RoleDto roleDto:dto.getRoles()) {
            UserRole ur = new UserRole(user.getId(),roleDto.getId());
            userRoleMapper.insert(ur);
        }
        return baseMapstruct.toDto(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(UserDto dto) {
        baseMapper.updateById(baseMapstruct.toEntity(dto));
        if(dto.getRoles() != null){
            userRoleMapper.delete(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId,dto.getId()));
            for (RoleDto roleDto:dto.getRoles()) {
                UserRole ur = new UserRole(dto.getId(),roleDto.getId());
                userRoleMapper.insert(ur);
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteById(Long id) {
        baseMapper.deleteById(id);
        userRoleMapper.delete(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, id));
        return true;
    }

    @Override
    public boolean checkExist(String userName) {
        return count(Wrappers.<User>lambdaQuery().eq(User::getUsername, userName.trim())) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean registerUser(RegisterUserDto dto) {
        Role normalRole = roleService.getOne(Wrappers.<Role>lambdaQuery().eq(Role::getName,"普通用户"));
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickName(RandomUtil.randomString(12));
        user.setPassword(new BCryptPasswordEncoder().encode(dto.getPassword()));
        baseMapper.insert(user);
        //用户角色
        UserRole userRole = new UserRole(user.getId(), normalRole.getId());
        userRoleMapper.insert(userRole);
        //账本
        createAccountBook(user);
        //用户配置
        createUserConfig(user);
        //用户栏目
        createDefaultClassify(user);
        return true;
    }

    /**
     * 创建账本
     * @param user
     */
    private void createAccountBook(User user){
        AccountBook ab = new AccountBook();
        ab.setName("默认账本");
        ab.setImage("red");
        ab.setUserId(user.getId());
        ab.setDescription("默认账本");
        ab.setIsDefault(BoolEnum.True);
        accountBookMapper.insert(ab);
    }

    /**
     * 创建用户配置
     * @param user 用户
     */
    private void createUserConfig(User user){
        UserConfig uc = new UserConfig();
        uc.setUserId(user.getId());
        uc.setName("is_credit_card");
        uc.setValue("0");
        uc.setDescription("流水记录时默认不选中信用卡");
        userConfigMapper.insert(uc);
        uc = new UserConfig();
        uc.setUserId(user.getId());
        uc.setName("show_expense_limit");
        uc.setValue("1");
        uc.setDescription("支出限额显示模式(1:不显示,2:显示月限额,3:显示年限额)");
        uc = new UserConfig();
        uc.setUserId(user.getId());
        uc.setName("default_monthly_expense_limit");
        uc.setValue("0");
        uc.setDescription("默认每月支出限额");
        uc = new UserConfig();
        uc.setUserId(user.getId());
        uc.setName("default_yearly_expense_limit");
        uc.setValue("0");
        uc.setDescription("默认每年支出限额");
        userConfigMapper.insert(uc);
    }

    /**
     * 新用户创建默认分类
     * @param user 用户
     */
    private void createDefaultClassify(User user){
        //大类,支出
        //餐饮
        Classify classify = new Classify();
        classify.setName("餐饮");
        classify.setSort(0);
        classify.setImage("canyin");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //零食烟酒
        classify = new Classify();
        classify.setName("零食烟酒");
        classify.setSort(1);
        classify.setImage("lingshiyanjiu");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //零食烟酒--小类
        Classify classify1 = new Classify();
        classify1.setName("零食");
        classify1.setSort(1);
        classify1.setImage("lingshi");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("饮料");
        classify1.setSort(1);
        classify1.setImage("yinliao");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("水果");
        classify1.setSort(1);
        classify1.setImage("shuiguo");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //购物
        classify = new Classify();
        classify.setName("购物");
        classify.setSort(2);
        classify.setImage("gouwu");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //购物-小类
        classify1 = new Classify();
        classify1.setName("数码");
        classify1.setSort(2);
        classify1.setImage("shuma");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("日用");
        classify1.setSort(2);
        classify1.setImage("riyong");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("玩具");
        classify1.setSort(2);
        classify1.setImage("wanju");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("电器");
        classify1.setSort(2);
        classify1.setImage("dianqi");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("美妆");
        classify1.setSort(2);
        classify1.setImage("meizhuang");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("运动");
        classify1.setSort(2);
        classify1.setImage("yundong");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("鞋服");
        classify1.setSort(2);
        classify1.setImage("xiefu");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("饰品");
        classify1.setSort(2);
        classify1.setImage("shipin");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //住房
        classify = new Classify();
        classify.setName("住房");
        classify.setSort(3);
        classify.setImage("shouye");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //住房-小类
        classify1 = new Classify();
        classify1.setName("家纺");
        classify1.setSort(3);
        classify1.setImage("jiafang");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("家具");
        classify1.setSort(3);
        classify1.setImage("jiaju");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("物业水电");
        classify1.setSort(3);
        classify1.setImage("wuyeshuidian");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //交通
        classify = new Classify();
        classify.setName("交通");
        classify.setSort(4);
        classify.setImage("jiaotong");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //交通-小类
        classify1 = new Classify();
        classify1.setName("公交出租");
        classify1.setSort(4);
        classify1.setImage("gongjiao");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("火车");
        classify1.setSort(4);
        classify1.setImage("huoche");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("机票");
        classify1.setSort(4);
        classify1.setImage("jipiao");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //汽车
        classify1 = new Classify();
        classify1.setName("汽车");
        classify1.setSort(5);
        classify1.setImage("qiche");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classifyMapper.insert(classify1);
        //娱乐
        classify = new Classify();
        classify.setName("娱乐");
        classify.setSort(5);
        classify.setImage("yule");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //娱乐-小类
        classify1 = new Classify();
        classify1.setName("游戏");
        classify1.setSort(4);
        classify1.setImage("youxi");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("电影");
        classify1.setSort(4);
        classify1.setImage("dianying");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("K歌");
        classify1.setSort(4);
        classify1.setImage("kge");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //文教
        classify = new Classify();
        classify.setName("文教");
        classify.setSort(6);
        classify.setImage("wenjiao");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //通讯
        classify = new Classify();
        classify.setName("通讯");
        classify.setSort(6);
        classify.setImage("tongxun");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //育儿
        classify = new Classify();
        classify.setName("育儿");
        classify.setSort(7);
        classify.setImage("yuer");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //人情
        classify = new Classify();
        classify.setName("人情");
        classify.setSort(8);
        classify.setImage("renqing");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //人情-小类
        classify1 = new Classify();
        classify1.setName("礼品礼金");
        classify1.setSort(8);
        classify1.setImage("lipinlijin");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("请客");
        classify1.setSort(8);
        classify1.setImage("qingke");
        classify1.setType(BillType.Expense);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //医疗
        classify = new Classify();
        classify.setName("医疗");
        classify.setSort(9);
        classify.setImage("yiliao");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //旅行
        classify = new Classify();
        classify.setName("旅行");
        classify.setSort(10);
        classify.setImage("lvxing");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //其他
        classify = new Classify();
        classify.setName("其他");
        classify.setSort(11);
        classify.setImage("qita");
        classify.setType(BillType.Expense);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);

        //大类 收入
        //薪资
        classify = new Classify();
        classify.setName("薪资");
        classify.setSort(12);
        classify.setImage("xinzi");
        classify.setType(BillType.Income);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //奖金
        classify = new Classify();
        classify.setName("奖金");
        classify.setSort(13);
        classify.setImage("jiangjin");
        classify.setType(BillType.Income);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //投资收益
        classify = new Classify();
        classify.setName("投资收益");
        classify.setSort(14);
        classify.setImage("touzishouyi");
        classify.setType(BillType.Income);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //其他收入
        classify = new Classify();
        classify.setName("其他收入");
        classify.setSort(15);
        classify.setImage("qitashouru");
        classify.setType(BillType.Income);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
    }
}
