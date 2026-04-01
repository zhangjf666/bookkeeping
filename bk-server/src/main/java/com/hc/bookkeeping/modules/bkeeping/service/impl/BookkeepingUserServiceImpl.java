package com.hc.bookkeeping.modules.bkeeping.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.common.utils.SpringSecurityUtil;
import com.hc.bookkeeping.config.properties.SystemProperties;
import com.hc.bookkeeping.modules.admin.entity.Role;
import com.hc.bookkeeping.modules.admin.entity.User;
import com.hc.bookkeeping.modules.admin.entity.UserRole;
import com.hc.bookkeeping.modules.admin.mapper.UserRoleMapper;
import com.hc.bookkeeping.modules.admin.service.RoleService;
import com.hc.bookkeeping.modules.bkeeping.constants.Constants;
import com.hc.bookkeeping.modules.bkeeping.constants.ExpenseLimitShowType;
import com.hc.bookkeeping.modules.bkeeping.dto.AvatarUploadResult;
import com.hc.bookkeeping.modules.bkeeping.dto.BookkeepingUserDto;
import com.hc.bookkeeping.modules.bkeeping.dto.ChangePasswordDto;
import com.hc.bookkeeping.modules.bkeeping.entity.AccountBook;
import com.hc.bookkeeping.modules.bkeeping.entity.BookkeepingUser;
import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import com.hc.bookkeeping.modules.bkeeping.entity.UserConfig;
import com.hc.bookkeeping.modules.bkeeping.mapper.AccountBookMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.BookkeepingUserMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.ClassifyMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserConfigMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.BookkeepingUserMapstruct;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import com.hc.bookkeeping.modules.bkeeping.service.BookkeepingUserService;
import com.hc.bookkeeping.modules.security.dto.RegisterUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * <p>
 * 记账用户表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-08-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookkeepingUserServiceImpl extends BaseServiceImpl<BookkeepingUserMapstruct, BookkeepingUserDto, BookkeepingUserMapper, BookkeepingUser> implements BookkeepingUserService {

    private final PasswordEncoder passwordEncoder;

    private final SystemProperties systemProperties;

    private final UserRoleMapper userRoleMapper;

    private final RoleService roleService;

    private final UserConfigMapper userConfigMapper;

    private final AccountBookMapper accountBookMapper;

    private final ClassifyMapper classifyMapper;

    private static final String UPLOAD_AVATAR_PATH = Constants.UPLOAD_PATH + "avatar/";

    @Override
    public BookkeepingUserDto getCurrentUser() {
        Long userId = SpringSecurityUtil.getCurrentUserId();
        return queryById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookkeepingUserDto updateCurrentUser(BookkeepingUserDto userDto) throws IOException {
        Long userId = SpringSecurityUtil.getCurrentUserId();
        
        // 获取数据库中现有数据
        BookkeepingUser existingUser = baseMapper.selectById(userId);
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        String oldAvatar = existingUser.getAvatar();
        String newAvatar = userDto.getAvatar();

        // 如果头像有变化，处理头像文件
        if (newAvatar != null && !newAvatar.equals(oldAvatar)) {
            // 获取临时文件并移动到正式目录
            String tempFilePath = systemProperties.getUploadTempPath() + newAvatar;
            File tempFile = new File(tempFilePath);
            
            if (tempFile.exists()) {
                // 创建头像目录
                File avatarDir = new File(systemProperties.getUploadPath() + "avatar/");
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs();
                }

                // 生成正式文件名
                String fileExtension = getFileExtension(newAvatar);
                String finalFileName = "avatar_" + userId + "_" + System.currentTimeMillis() + fileExtension;
                String finalPath = systemProperties.getUploadPath() + "avatar/" + finalFileName;

                // 移动文件
                Path tempPath = Paths.get(tempFilePath);
                Path targetPath = Paths.get(finalPath);
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                // 设置新头像路径
                userDto.setAvatar(UPLOAD_AVATAR_PATH + finalFileName);

                // 删除旧头像文件
                if (oldAvatar != null && !oldAvatar.isEmpty()) {
                    String oldFileName = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                    File oldFile = new File(systemProperties.getUploadPath() + "avatar/" + oldFileName);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
            }
        } else {
            // 如果头像没有变化，保持原有头像
            userDto.setAvatar(oldAvatar);
        }

        // 更新用户信息
        userDto.setId(userId);
        userDto.setUsername(existingUser.getUsername());
        BookkeepingUser user = baseMapstruct.toEntity(userDto);
        // 不更新密码字段跟用户名字段
        user.setPassword(null);
        user.setUsername(null);
        baseMapper.updateById(user);
        return userDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDto changePasswordDto) {
        Long userId = SpringSecurityUtil.getCurrentUserId();
        BookkeepingUser user = baseMapper.selectById(userId);
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证当前密码
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("当前密码错误");
        }

        // 验证新密码和确认密码是否一致
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        baseMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AvatarUploadResult uploadAvatar(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new BusinessException("只支持JPG和PNG格式的图片");
        }

        // 验证文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过2MB");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        // 生成临时文件名
        String tempFileName = "tmp_" + System.currentTimeMillis() + fileExtension;

        // 创建临时目录
        File tempDir = new File(systemProperties.getUploadTempPath());
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 保存临时文件
        File tempFile = new File(systemProperties.getUploadTempPath() + tempFileName);
        file.transferTo(tempFile);
        AvatarUploadResult result = new AvatarUploadResult();
        result.setFileName(tempFileName);
        result.setUrl(Constants.UPLOAD_TMP_PATH + tempFileName);
        return result;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".jpg";
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) {
            return ".jpg";
        }
        return fileName.substring(lastDot);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean registerUser(RegisterUserDto dto) {
        Role normalRole = roleService.getOne(Wrappers.<Role>lambdaQuery().eq(Role::getName,"普通用户"));
        BookkeepingUser user = new BookkeepingUser();
        user.setUsername(dto.getUsername());
        user.setNickName(RandomUtil.randomString(12));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
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
    private void createAccountBook(BookkeepingUser user){
        AccountBook ab = new AccountBook();
        ab.setName("默认账本");
        ab.setImage("book");
        ab.setUserId(user.getId());
        ab.setDescription("默认账本");
        ab.setIsDefault(BoolEnum.YES);
        accountBookMapper.insert(ab);
    }

    /**
     * 创建用户配置
     * @param user 用户
     */
    private void createUserConfig(BookkeepingUser user){
        UserConfig uc = new UserConfig();
        uc.setUserId(user.getId());
        uc.setName("is_credit_card");
        uc.setValue(BoolEnum.NO.getValue());
        uc.setDescription("记录收支时默认选中信用卡");
        userConfigMapper.insert(uc);
        uc = new UserConfig();
        uc.setUserId(user.getId());
        uc.setName("show_expense_limit");
        uc.setValue(ExpenseLimitShowType.NOT.getCode());
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
    private void createDefaultClassify(BookkeepingUser user){
        //大类,支出
        //餐饮
        Classify classify = new Classify();
        classify.setName("餐饮");
        classify.setSort(0);
        classify.setImage("canyin");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //零食烟酒
        classify = new Classify();
        classify.setName("零食烟酒");
        classify.setSort(1);
        classify.setImage("lingshiyanjiu");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //零食烟酒--小类
        Classify classify1 = new Classify();
        classify1.setName("零食");
        classify1.setSort(1);
        classify1.setImage("lingshi");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("饮料");
        classify1.setSort(1);
        classify1.setImage("yinliao");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("水果");
        classify1.setSort(1);
        classify1.setImage("shuiguo");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //购物
        classify = new Classify();
        classify.setName("购物");
        classify.setSort(2);
        classify.setImage("gouwu");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //购物-小类
        classify1 = new Classify();
        classify1.setName("数码");
        classify1.setSort(2);
        classify1.setImage("shuma");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("日用");
        classify1.setSort(2);
        classify1.setImage("riyong");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("玩具");
        classify1.setSort(2);
        classify1.setImage("wanju");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("电器");
        classify1.setSort(2);
        classify1.setImage("dianqi");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("美妆");
        classify1.setSort(2);
        classify1.setImage("meizhuang");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("运动");
        classify1.setSort(2);
        classify1.setImage("yundong");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("鞋服");
        classify1.setSort(2);
        classify1.setImage("xiefu");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("饰品");
        classify1.setSort(2);
        classify1.setImage("shipin");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //住房
        classify = new Classify();
        classify.setName("住房");
        classify.setSort(3);
        classify.setImage("shouye");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //住房-小类
        classify1 = new Classify();
        classify1.setName("家纺");
        classify1.setSort(3);
        classify1.setImage("jiafang");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("家具");
        classify1.setSort(3);
        classify1.setImage("jiaju");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("物业水电");
        classify1.setSort(3);
        classify1.setImage("wuyeshuidian");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //交通
        classify = new Classify();
        classify.setName("交通");
        classify.setSort(4);
        classify.setImage("jiaotong");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //交通-小类
        classify1 = new Classify();
        classify1.setName("公交出租");
        classify1.setSort(4);
        classify1.setImage("gongjiao");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("火车");
        classify1.setSort(4);
        classify1.setImage("huoche");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("机票");
        classify1.setSort(4);
        classify1.setImage("jipiao");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //汽车
        classify1 = new Classify();
        classify1.setName("汽车");
        classify1.setSort(5);
        classify1.setImage("qiche");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classifyMapper.insert(classify1);
        //娱乐
        classify = new Classify();
        classify.setName("娱乐");
        classify.setSort(5);
        classify.setImage("yule");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //娱乐-小类
        classify1 = new Classify();
        classify1.setName("游戏");
        classify1.setSort(4);
        classify1.setImage("youxi");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("电影");
        classify1.setSort(4);
        classify1.setImage("dianying");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("K歌");
        classify1.setSort(4);
        classify1.setImage("kge");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //文教
        classify = new Classify();
        classify.setName("文教");
        classify.setSort(6);
        classify.setImage("wenjiao");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //通讯
        classify = new Classify();
        classify.setName("通讯");
        classify.setSort(6);
        classify.setImage("tongxun");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //育儿
        classify = new Classify();
        classify.setName("育儿");
        classify.setSort(7);
        classify.setImage("yuer");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //人情
        classify = new Classify();
        classify.setName("人情");
        classify.setSort(8);
        classify.setImage("renqing");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //人情-小类
        classify1 = new Classify();
        classify1.setName("礼品礼金");
        classify1.setSort(8);
        classify1.setImage("lipinlijin");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        classify1 = new Classify();
        classify1.setName("请客");
        classify1.setSort(8);
        classify1.setImage("qingke");
        classify1.setType(BillType.EXPENSE);
        classify1.setUserId(user.getId());
        classify1.setPid(classify.getId());
        classifyMapper.insert(classify1);
        //医疗
        classify = new Classify();
        classify.setName("医疗");
        classify.setSort(9);
        classify.setImage("yiliao");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //旅行
        classify = new Classify();
        classify.setName("旅行");
        classify.setSort(10);
        classify.setImage("lvxing");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //其他
        classify = new Classify();
        classify.setName("其他");
        classify.setSort(11);
        classify.setImage("qita");
        classify.setType(BillType.EXPENSE);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);

        //大类 收入
        //薪资
        classify = new Classify();
        classify.setName("薪资");
        classify.setSort(12);
        classify.setImage("xinzi");
        classify.setType(BillType.INCOME);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //奖金
        classify = new Classify();
        classify.setName("奖金");
        classify.setSort(13);
        classify.setImage("jiangjin");
        classify.setType(BillType.INCOME);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //投资收益
        classify = new Classify();
        classify.setName("投资收益");
        classify.setSort(14);
        classify.setImage("touzishouyi");
        classify.setType(BillType.INCOME);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
        //其他收入
        classify = new Classify();
        classify.setName("其他收入");
        classify.setSort(15);
        classify.setImage("qitashouru");
        classify.setType(BillType.INCOME);
        classify.setUserId(user.getId());
        classifyMapper.insert(classify);
    }
}
