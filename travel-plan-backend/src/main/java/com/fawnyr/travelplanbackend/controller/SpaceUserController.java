package com.fawnyr.travelplanbackend.controller;

import com.fawnyr.travelplanbackend.common.BaseResponse;
import com.fawnyr.travelplanbackend.common.DeleteRequest;
import com.fawnyr.travelplanbackend.common.ResultUtils;
import com.fawnyr.travelplanbackend.exception.BusinessException;
import com.fawnyr.travelplanbackend.exception.ErrorCode;
import com.fawnyr.travelplanbackend.exception.ThrowUtils;
import com.fawnyr.travelplanbackend.manager.auth.annotion.SaSpaceCheckPermission;
import com.fawnyr.travelplanbackend.manager.auth.model.SpaceUserPermissionConstant;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceQueryRequest;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserAddRequest;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserEditRequest;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.fawnyr.travelplanbackend.model.entity.SpaceUser;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.vo.SpaceUserVO;
import com.fawnyr.travelplanbackend.service.SpaceUserService;
import com.fawnyr.travelplanbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spaceUser")
@Slf4j
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 添加成员到空间
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        long id = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }

    /**
     * 删除空间成员
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if(deleteRequest==null || deleteRequest.getId()<=0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean result = spaceUserService.deleteSpaceUser(deleteRequest);
        return ResultUtils.success(result);
    }


    /**
     * 查询某个成员在某个空间的信息
     */
    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,HttpServletRequest request){
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        if(spaceId==null || userId==null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = spaceUserService.getBaseMapper().selectOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询成员信息列表
     */
    @PostMapping("/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 编辑成员信息（设置权限）
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserEditRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = spaceUserService.editSpaceUser(spaceUserEditRequest);
        return ResultUtils.success(result);
    }

    /**
     * 查询我加入的团队空间列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }
}
