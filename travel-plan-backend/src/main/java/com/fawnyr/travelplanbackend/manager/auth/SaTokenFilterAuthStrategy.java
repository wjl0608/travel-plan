package com.fawnyr.travelplanbackend.manager.auth;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.router.SaRouter;
import com.fawnyr.travelplanbackend.exception.ErrorCode;
import com.fawnyr.travelplanbackend.exception.ThrowUtils;
import com.fawnyr.travelplanbackend.manager.auth.model.SpaceUserPermissionConstant;
import com.fawnyr.travelplanbackend.model.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fawnyr.travelplanbackend.constant.UserConstant.USER_LOGIN_STATE;

public class SaTokenFilterAuthStrategy {
    SaFilterAuthStrategy getSaFilterAuthStrategy() {
        // 登录校验：拦截所有路由，排除登录接口
// 权限校验：根据路由划分模块，不同模块校验不同权限
        return obj -> {
            // 登录校验：拦截所有路由，排除登录接口
            if (SaRouter.match("/api/user/login").isHit) {
                return;
            }
            SaRouter.match("/**", r -> StpKit.SPACE.checkLogin());
            Long userId = ((User) StpKit.SPACE.getSession().get(USER_LOGIN_STATE)).getId();
            SpaceUserAuthContext spaceUserAuthContext = (SpaceUserAuthContext) SaTokenContextHolder.get(Objects.nonNull(userId) ? userId.toString() : "");
            // 权限校验：根据路由划分模块，不同模块校验不同权限
            SaRouter.match("/api/picture/**", r -> {
                String requestPath = SaHolder.getRequest().getRequestPath();
                List<Boolean> hasElement = new ArrayList<>();
                if (requestPath.contains("edit")) {
                    hasElement.add(StpKit.SPACE.hasElement(spaceUserAuthContext.getPermissionList(), SpaceUserPermissionConstant.PICTURE_EDIT));
                }
                if (requestPath.contains("delete")) {
                    hasElement.add(StpKit.SPACE.hasElement(spaceUserAuthContext.getPermissionList(), SpaceUserPermissionConstant.PICTURE_DELETE));
                }
                if (requestPath.contains("upload")) {
                    hasElement.add(StpKit.SPACE.hasElement(spaceUserAuthContext.getPermissionList(), SpaceUserPermissionConstant.PICTURE_UPLOAD));
                }
                if (requestPath.contains("admin")) {
                    hasElement.add(StpKit.SPACE.hasElement(spaceUserAuthContext.getPermissionList(), SpaceUserPermissionConstant.SPACE_USER_MANAGE));
                }
                ThrowUtils.throwIf(hasElement.contains(false), ErrorCode.NO_AUTH_ERROR, "Sa-Token 无权限");
            });
            SaRouter.match("/api/spaceUser/**", r -> {
                boolean hasElement = StpKit.SPACE.hasElement(spaceUserAuthContext.getPermissionList(), SpaceUserPermissionConstant.SPACE_USER_MANAGE);
                ThrowUtils.throwIf(!hasElement, ErrorCode.NO_AUTH_ERROR, "Sa-Token 无权限");

            });
            SaRouter.match("/api/space/**", r -> {
                String requestPath = SaHolder.getRequest().getRequestPath();
                List<Boolean> hasElement = new ArrayList<>();
                if (requestPath.contains("manage")) {
                    hasElement.add(StpKit.SPACE.hasElement(spaceUserAuthContext.getPermissionList(), SpaceUserPermissionConstant.SPACE_USER_MANAGE));
                }
                if (requestPath.contains("delete")) {
                    hasElement.add(StpKit.SPACE.hasElement(spaceUserAuthContext.getPermissionList(), SpaceUserPermissionConstant.PICTURE_DELETE));
                }
                ThrowUtils.throwIf(hasElement.contains(false), ErrorCode.NO_AUTH_ERROR, "Sa-Token 无权限");
            });
        };
    }


}
