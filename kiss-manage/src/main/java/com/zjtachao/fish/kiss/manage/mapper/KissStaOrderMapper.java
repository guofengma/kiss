/**************************************************************************
 * Copyright (c) 2016-2017 Zhejiang TaChao Network Technology Co.,Ltd.
 * All rights reserved.
 *
 * 项目名称：浙江踏潮-基础架构
 * 版权说明：本软件属浙江踏潮网络科技有限公司所有，在未获得浙江踏潮网络科技有限公司正式授权
 *           情况下，任何企业和个人，不能获取、阅读、安装、传播本软件涉及的任何受知
 *           识产权保护的内容。                            
 ***************************************************************************/
package com.zjtachao.fish.kiss.manage.mapper;

import com.zjtachao.fish.kiss.common.bean.domain.KissStaOrder;
import com.zjtachao.fish.kiss.common.bean.so.KissStaOrderSo;

import java.util.List;

/**
 * 订单统计
 *
 * @author <a href="mailto:dh@zjtachao.com">duhao</a>
 * @since 2.0
 */
public interface KissStaOrderMapper {

    /**
     * 查询订单列表
     * @param so
     * @return
     */
    public List<KissStaOrder> queryStaOrderList(KissStaOrderSo so);

    /**
     * 添加order
     * @param order
     */
    public void addStaOrder(KissStaOrder order);

    /**
     * 删除order
     * @param order
     */
    public void deleteStaOrder(KissStaOrder order);

    /**
     * 查询总数
     * @return
     */
    public KissStaOrder queryStaOrderTotal(KissStaOrderSo so);


}
