package com.fawnyr.travelplanbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fawnyr.travelplanbackend.common.DeleteRequest;
import com.fawnyr.travelplanbackend.exception.BusinessException;
import com.fawnyr.travelplanbackend.exception.ErrorCode;
import com.fawnyr.travelplanbackend.exception.ThrowUtils;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserAddRequest;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserEditRequest;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.fawnyr.travelplanbackend.model.entity.Space;
import com.fawnyr.travelplanbackend.model.entity.SpaceUser;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.enums.SpaceLevelEnum;
import com.fawnyr.travelplanbackend.model.enums.SpaceRoleEnum;
import com.fawnyr.travelplanbackend.model.enums.SpaceTypeEnum;
import com.fawnyr.travelplanbackend.model.vo.SpaceUserVO;
import com.fawnyr.travelplanbackend.model.vo.SpaceVO;
import com.fawnyr.travelplanbackend.model.vo.UserVO;
import com.fawnyr.travelplanbackend.service.SpaceService;
import com.fawnyr.travelplanbackend.service.SpaceUserService;
import com.fawnyr.travelplanbackend.mapper.SpaceUserMapper;
import com.fawnyr.travelplanbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author wujialu
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-07-05 13:56:37
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private SpaceService spaceService;
    /**
     * 空间角色校验
     * @param spaceUser
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        //要创建
        if(add){
            if (spaceId == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间id不能为空");
            }
            if (userId == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户id不能为空");
            }
            if (StrUtil.isBlank(spaceRole)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户角色不能为空");
            }
        }
        //修改数据
        if (StrUtil.isBlank(spaceRole) && spaceRoleEnum==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户角色不存在");
        }
    }

    /**
     * 创建空间角色
     * @param spaceUserAddRequest
     * @return
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest,spaceUser);
        if(StrUtil.isBlank(spaceUser.getSpaceRole()))
            spaceUser.setSpaceRole(SpaceRoleEnum.VIEWER.getValue());
        validSpaceUser(spaceUser,true);
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return spaceUser.getUserId();
    }

    /**
     * 删除空间成员
     * @param deleteRequest
     * @return
     */
    @Override
    public boolean deleteSpaceUser(DeleteRequest deleteRequest) {
        //判断是否存在
        Long id = deleteRequest.getId();
        SpaceUser oldSpaceUser = this.getById(id);
        ThrowUtils.throwIf(oldSpaceUser==null,ErrorCode.PARAMS_ERROR,"空间用户不存在");
        //移除数据
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 获取查询条件
     * @param spaceUserQueryRequest
     * @return
     */
    @Override
    public Wrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

    /**
     * 获取单个封装类
     * @param spaceUser
     * @return
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser) {
        // 对象转封装类
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtils.copyProperties(spaceUser,spaceUserVO);
        // 查询关联用户信息
        Long userId = spaceUserVO.getUserId();
        if(userId!=null && userId>0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 查询关联空间信息
        Long spaceId = spaceUserVO.getSpaceId();
        if(spaceId!=null && spaceId>0){
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    /**
     * 获取封装类列表
     * @param spaceUserList
     * @return
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if(CollUtil.isEmpty(spaceUserList))
            return Collections.emptyList();
        // 对象列表->封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).toList();
        // 1.收集需要关联查询的用户ID和空间ID
        Set<Long> userIdList = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdList = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        // 2.批量查询用户和空间
        Map<Long, List<User>> UserListMap = userService.listByIds(userIdList).stream().collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceListMap = spaceService.listByIds(spaceIdList).stream().collect(Collectors.groupingBy(Space::getId));
        // 3.填充SpaceUserVO
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            User user = null;
            if(UserListMap.containsKey(userId)){
                user = UserListMap.get(userId).get(0);
            }
            spaceUserVO.setUser(userService.getUserVO(user));
            Space space = null;
            if(spaceListMap.containsKey(spaceId)){
                space = spaceListMap.get(spaceId).get(0);
            }
            spaceUserVO.setSpace(spaceService.getSpaceVO(space));
        });
        return spaceUserVOList;
    }

    /**
     * 编辑空间用户
     * @param spaceUserEditRequest
     * @return
     */
    @Override
    public boolean editSpaceUser(SpaceUserEditRequest spaceUserEditRequest) {
        // 将dto转换为实体类
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserEditRequest,spaceUser);
        // 数据校验
        validSpaceUser(spaceUser,false);
        // 判断数据库是否存在
        long id = spaceUserEditRequest.getId();
        SpaceUser oldSpaceUser = this.getById(id);
        ThrowUtils.throwIf(oldSpaceUser==null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = this.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

}




