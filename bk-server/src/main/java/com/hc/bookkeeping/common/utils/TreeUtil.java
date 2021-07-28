package com.hc.bookkeeping.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.hc.bookkeeping.common.base.LazyTree;
import com.hc.bookkeeping.common.base.Tree;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description: 树工具类
 * @Date: 2020/8/5 13:48
 */
public class TreeUtil {

    /**
     * 树构建
     *
     * @param <T>            转换的实体 为数据源里的对象类型
     * @param <E>            ID类型
     * @param list           源数据集合
     * @param parentId       最顶层父id值 一般为 0 之类
     * @return List
     */
    public static <T extends Tree<T, E>, E> List<T> build(Collection<T> list, E parentId) {
        List<T> finalTreeList = CollUtil.newArrayList();
        for (T node : list) {
            if (parentId.equals(node.getPid())) {
                finalTreeList.add(node);
                innerBuild(list, node, 0, null);
            }
        }
        // 内层每层已经排过了 这是最外层排序
        finalTreeList = finalTreeList.stream().sorted().collect(Collectors.toList());
        return finalTreeList;
    }

    /**
     * 递归构建树
     *
     * @param treeNodes  数据集合
     * @param parentNode 当前节点
     * @param deep       已递归深度
     * @param maxDeep    最大递归深度 可能为null即不限制
     */
    private static <T extends Tree<T, E>, E> void innerBuild(Collection<T> treeNodes, T parentNode, int deep, Integer maxDeep) {
        if (CollUtil.isEmpty(treeNodes)) {
            return;
        }
        //maxDeep 可能为空
        if (maxDeep != null && deep >= maxDeep) {
            return;
        }

        if(parentNode.getChildren() == null){
            parentNode.setChildren(CollUtil.newArrayList());
        }
        // 每层排序 TreeNodeMap 实现了Comparable接口
        treeNodes = treeNodes.stream().sorted().collect(Collectors.toList());
        for (T childNode : treeNodes) {
            if (parentNode.getId().equals(childNode.getPid())) {
                List<T> children = parentNode.getChildren();
                parentNode.setChildren(children);
                children.add(childNode);
				childNode.setPid(parentNode.getId());
                innerBuild(treeNodes, childNode, deep + 1, maxDeep);
            }
        }
        if(parentNode instanceof LazyTree){
            ((LazyTree)parentNode).setHasChildren(parentNode.getChildren().size() > 0);
        }
    }

    /**
     * 获取ID对应的节点，如果有多个ID相同的节点，只返回第一个。<br>
     * 此方法只查找此节点及子节点，采用广度优先遍历。
     *
     * @param <T> ID类型
     * @param node 节点
     * @param id ID
     * @return 节点
     */
    public static <T extends Tree<T, E>, E> T getNode(T node, E id) {
        if (ObjectUtil.equal(id, node.getId())) {
            return node;
        }

        final List<T> children = node.getChildren();
        if(null == children) {
            return null;
        }

        // 查找子节点
        T childNode;
        for (T child : children) {
            childNode = getNode(child, id);
            if (null != childNode) {
                return childNode;
            }
        }

        // 未找到节点
        return null;
    }
}
