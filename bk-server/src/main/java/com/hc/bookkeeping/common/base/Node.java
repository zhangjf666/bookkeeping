package com.hc.bookkeeping.common.base;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/4 11:19
 */
public interface Node<T> extends Comparable<Node<T>> {
    /**
     * 获取ID
     *
     * @return ID
     */
    T getId();

    /**
     * 设置ID
     *
     * @param id ID
     */
    void setId(T id);

    /**
     * 获取父节点ID
     *
     * @return 父节点ID
     */
    T getPid();

    /**
     * 设置父节点ID
     *
     * @param pid 父节点ID
     */
    void setPid(T pid);

    /**
     * 获取节点标签名称
     *
     * @return 节点标签名称
     */
    CharSequence getName();

    /**
     * 设置节点标签名称
     *
     * @param name 节点标签名称
     */
    void setName(CharSequence name);

    /**
     * 获取权重
     *
     * @return 权重
     */
    Comparable<?> getWeight();

    /**
     * 设置权重
     *
     * @param weight 权重
     */
    void setWeight(Comparable<?> weight);

    /**
     * 比较node大小
     *
     * @param node
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes", "NullableProblems"})
    @Override
    default int compareTo(Node node) {
        final Comparable weight = this.getWeight();
        if (null != weight) {
            final Comparable weightOther = node.getWeight();
            return weight.compareTo(weightOther);
        }
        return 0;
    }
}
