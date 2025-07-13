package com.fawnyr.travelplanbackend.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.fawnyr.travelplanbackend.manager.auth.SpaceUserAuthManager;
import com.fawnyr.travelplanbackend.manager.auth.model.SpaceUserPermissionConstant;
import com.fawnyr.travelplanbackend.model.entity.Picture;
import com.fawnyr.travelplanbackend.model.entity.Space;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.enums.SpaceTypeEnum;
import com.fawnyr.travelplanbackend.service.PictureService;
import com.fawnyr.travelplanbackend.service.SpaceService;
import com.fawnyr.travelplanbackend.service.SpaceUserService;
import com.fawnyr.travelplanbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 建立连接前校验
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if(request instanceof ServletServerHttpRequest){
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 从请求中获取参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if(StrUtil.isBlank(pictureId)){
                log.error("缺少图片信息，拒绝握手");
                return false;
            }
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("用户未登录，拒绝握手");
                return false;
            }
            // 检验用户是否有编辑当前图片的权限
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if(spaceId!=null){
                space = spaceService.getById(spaceId);
                if (ObjUtil.isEmpty(loginUser)) {
                    log.error("图片所在空间不存在，拒绝握手");
                    return false;
                }
                if(space.getSpaceType()!= SpaceTypeEnum.TEAM.getValue()){
                    log.error("图片所在空间不是团队空间，拒绝握手");
                    return false;
                }
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if(!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)){
                log.error("用户没有编辑权限，拒绝握手");
                return false;
            }
            //设置用户登录信息等属性到websocket会话中
            attributes.put("user",loginUser);
            attributes.put("userId",loginUser.getId());
            attributes.put("pictureId",Long.valueOf(pictureId));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
