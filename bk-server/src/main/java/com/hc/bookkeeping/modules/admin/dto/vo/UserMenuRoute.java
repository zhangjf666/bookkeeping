package com.hc.bookkeeping.modules.admin.dto.vo;

import cn.hutool.core.util.StrUtil;
import com.hc.bookkeeping.common.base.SimpleTree;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.modules.admin.dto.MenuDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/5 11:24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Slf4j
public class UserMenuRoute extends SimpleTree<UserMenuRoute, Long> {

    private String label;
    private String name;
    private String path;
    private Boolean hidden;
    private String redirect;
    private String component;
    private String componentName;
    private Boolean iframe;
    private Boolean alwaysShow;
    private MenuRouteMeta meta;

    public UserMenuRoute(MenuVo vo) {
        this.id = vo.getId();
        this.pid = vo.getPid();
        this.name = vo.getName();
        this.path = vo.getUrl();
        this.iframe = Constants.BOOL_TRUE.equals(vo.getIsOutside());
        this.label = vo.getName();
        this.meta = new MenuRouteMeta(vo.getName(), vo.getIcon(), true);
        this.setWeight(vo.getSort());
        this.setHidden(Constants.BOOL_TRUE.equals(vo.getIsShow()));
        // 如果不是外链
        this.setComponent(!this.iframe && vo.getPid().equals(Constants.TREE_ROOT)
                && StrUtil.isEmpty(vo.getComponent()) ? "Layout" : vo.getComponent());
        this.setComponentName(vo.getComponentName());
        log.debug("name {}, component {}", this.getName(), this.getComponent());
    }

    public UserMenuRoute(MenuDto dto) {
        this.id = dto.getId();
        this.pid = dto.getPid();
        this.name = dto.getName();
        this.path = dto.getUrl();
        this.iframe = Constants.BOOL_TRUE.equals(dto.getIsOutside());
        this.label = dto.getName();
        this.meta = new MenuRouteMeta(dto.getName(), dto.getIcon(), true);
        this.setWeight(dto.getSort());
        this.setHidden(Constants.BOOL_FALSE.equals(dto.getIsShow()));
        // 如果不是外链
        this.setComponent(!this.iframe && dto.getPid().equals(Constants.TREE_ROOT)
                && StrUtil.isEmpty(dto.getComponent()) ? "Layout" : dto.getComponent());
        this.setComponentName(dto.getComponentName());
        log.debug("name {}, component {}", this.getName(), this.getComponent());
    }
}
