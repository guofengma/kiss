/**************************************************************************
 * Copyright (c) 2016-2017 Zhejiang TaChao Network Technology Co.,Ltd.
 * All rights reserved.
 *
 * 项目名称：浙江踏潮-基础架构
 * 版权说明：本软件属浙江踏潮网络科技有限公司所有，在未获得浙江踏潮网络科技有限公司正式授权
 *           情况下，任何企业和个人，不能获取、阅读、安装、传播本软件涉及的任何受知
 *           识产权保护的内容。                            
 ***************************************************************************/
package com.zjtachao.fish.kiss.manage.filter;

import com.alibaba.fastjson.JSON;
import com.zjtachao.fish.kiss.common.bean.ro.KissBaseUserRo;
import com.zjtachao.fish.kiss.common.bean.ro.KissBaseUserUpdateRo;
import com.zjtachao.fish.kiss.common.constant.KissCommonConstant;
import com.zjtachao.fish.kiss.common.context.KissCommonContext;
import com.zjtachao.fish.water.common.data.WaterRedis;
import com.zjtachao.fish.water.common.tool.WaterDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Map;

/**
 * 用户登录状态拦截
 *
 * @author <a href="mailto:dh@zjtachao.com">duhao</a>
 * @since 2.0
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class KissLoginStateFilter implements ContainerRequestFilter {

    /** 日志 **/
    public static final Logger logger = LoggerFactory.getLogger(KissLoginStateFilter.class);

    /** Redis **/
    @Autowired
    public WaterRedis waterRedis;

    /**
     *
     * @param requestContext
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        try{
            String url = requestContext.getUriInfo().getBaseUri().toString();
            String msg = null;
            if(url.contains("/manage")){
                boolean flag = false;
                Map<String , Cookie> cookieMap = requestContext.getCookies();
                if(null != cookieMap && cookieMap.containsKey(KissCommonConstant.KISS_COMMON_USER_LOGIN_COOKIE_KEY)){
                    Cookie loginCookie = cookieMap.get(KissCommonConstant.KISS_COMMON_USER_LOGIN_COOKIE_KEY);
                    if(null != loginCookie && null != loginCookie.getValue() && !"".equals(loginCookie.getValue())){
                        String userJson = waterRedis.queryString(KissCommonConstant.KISS_COMMON_USER_LOGIN_CACHE_PREFIX+loginCookie.getValue());
                        Long expireTime = waterRedis.ttl(KissCommonConstant.KISS_COMMON_USER_LOGIN_CACHE_PREFIX+loginCookie.getValue());
                        if(null != userJson && !"".equals(userJson)){
                            KissBaseUserRo userRo = JSON.parseObject(userJson , KissBaseUserRo.class);
                            if(null != userRo){
                                //判断是否权限变更
                                boolean allowLogin = true;
                                String updateJson = waterRedis.get(KissCommonConstant.KISS_COMMON_USER_SITE_NOTIFY_PREFIX+userRo.getUserCode());
                                if(null != updateJson && !"".equals(updateJson)){
                                    KissBaseUserUpdateRo userUpdateRo = JSON.parseObject(updateJson , KissBaseUserUpdateRo.class);
                                    if(null != userUpdateRo && null != userUpdateRo.getRoleDate()
                                            && WaterDateUtil.compareDate(userUpdateRo.getRoleDate() , userRo.getUserLoginTime()) >= 0){
                                        allowLogin = false;
                                    }
                                }

                                if(allowLogin) {
                                    flag = true;
                                    //延期登录
                                    if(null != userRo.getUserLoginType()){
                                        if(userRo.getUserLoginType().equals(KissCommonContext.LoginTypeContext.PROGRAM.getCode())){
                                            if((KissCommonConstant.KISS_COMMON_USER_PROGRAM_LOGIN_EXPIRE_TIME - expireTime)
                                                    > KissCommonConstant.KISS_COMMON_USER_PROGRAM_LOGIN_EXPIRE_TIME / 3){
                                                waterRedis.expireTime(KissCommonConstant.KISS_COMMON_USER_LOGIN_CACHE_PREFIX+loginCookie.getValue()
                                                        ,KissCommonConstant.KISS_COMMON_USER_PROGRAM_LOGIN_EXPIRE_TIME/60);
                                            }
                                        }
                                    }else if(null != userRo.getUserLoginType()){
                                        if(userRo.getUserLoginType().equals(KissCommonContext.LoginTypeContext.MOBILE.getCode())){
                                            if((KissCommonConstant.KISS_COMMON_USER_NORMAL_LOGIN_EXPIRE_TIME - expireTime)
                                                    > KissCommonConstant.KISS_COMMON_USER_NORMAL_LOGIN_EXPIRE_TIME / 3){
                                                waterRedis.expireTime(KissCommonConstant.KISS_COMMON_USER_NORMAL_LOGIN_EXPIRE_TIME+loginCookie.getValue()
                                                        ,KissCommonConstant.KISS_COMMON_USER_NORMAL_LOGIN_EXPIRE_TIME/60);
                                            }
                                        }
                                    }
                                }else {
                                    msg = "role change";
                                }

                            }
                        }
                    }
                }


                if(!flag){
                    if(null == msg){
                        msg = "no login";
                    }
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("{\"code\":1,\"msg\":\""+msg+"\"}").build());
                }
            }
        }catch (Exception ex){
            logger.error("验证用户是否登录出错！" , ex);
        }

    }
}
