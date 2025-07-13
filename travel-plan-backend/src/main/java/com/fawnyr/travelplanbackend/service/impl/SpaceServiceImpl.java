package com.fawnyr.travelplanbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fawnyr.travelplanbackend.common.DeleteRequest;
import com.fawnyr.travelplanbackend.exception.BusinessException;
import com.fawnyr.travelplanbackend.exception.ErrorCode;
import com.fawnyr.travelplanbackend.exception.ThrowUtils;
import com.fawnyr.travelplanbackend.manager.auth.SpaceUserAuthManager;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceAddRequest;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceEditRequest;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceQueryRequest;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceUpdateRequest;
import com.fawnyr.travelplanbackend.model.entity.Space;
import com.fawnyr.travelplanbackend.model.entity.SpaceUser;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.enums.SpaceLevelEnum;
import com.fawnyr.travelplanbackend.model.enums.SpaceRoleEnum;
import com.fawnyr.travelplanbackend.model.enums.SpaceTypeEnum;
import com.fawnyr.travelplanbackend.model.vo.SpaceVO;
import com.fawnyr.travelplanbackend.model.vo.UserVO;
import com.fawnyr.travelplanbackend.service.SpaceService;
import com.fawnyr.travelplanbackend.mapper.SpaceMapper;
import com.fawnyr.travelplanbackend.service.SpaceUserService;
import com.fawnyr.travelplanbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author wujialu
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-07-02 10:29:59
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserService;
    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 空间校验
     * @param space
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        //要创建
        if(add){
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间名字不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间类别不能为空");
            }
        }
        //修改数据
        if (spaceLevel != null && spaceLevelEnum==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length()>30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间名字过长");
        }
        if (spaceType != null && spaceTypeEnum==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间类别不存在");
        }
    }

    /**
     * 根据空间级别，自动填充限额
     * @param space
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if(spaceLevelEnum!=null){
            long maxCount = spaceLevelEnum.getMaxCount();
            if(space.getMaxCount()==null)
                space.setMaxCount(maxCount);
            long maxSize = spaceLevelEnum.getMaxSize();
            if(space.getMaxSize()==null)
                space.setMaxSize(maxSize);
        }
    }

    /**
     * 更新空间
     * @param spaceUpdateRequest
     * @return
     */
    @Override
    public boolean updateSpace(SpaceUpdateRequest spaceUpdateRequest) {
        // 将dto转换为实体类
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest,space);
        // 数据校验
        validSpace(space,false);
        // 根据空间级别，自动填充限额
        fillSpaceBySpaceLevel(space);
        // 判断数据库是否存在
        Long id = spaceUpdateRequest.getId();
        Space oldSpace = getById(id);
        ThrowUtils.throwIf(oldSpace==null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        // 将dto转换为实体类
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest,space);
        if(StrUtil.isBlank(spaceAddRequest.getSpaceName()))
            space.setSpaceName("默认空间");
        if(spaceAddRequest.getSpaceLevel()==null)
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        if(spaceAddRequest.getSpaceType()==null)
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        // 数据校验
        validSpace(space,true);
        // 根据空间级别，自动填充限额
        fillSpaceBySpaceLevel(space);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        //仅本人或管理员可操作
        if(space.getSpaceLevel()!=SpaceLevelEnum.COMMON.getValue() || !userService.isAdmin(loginUser))
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限创建指定权限空间");
        // 针对用户加锁
        String lock = String.valueOf(userId).intern();
        synchronized (lock){
            Long newSpaceId = transactionTemplate.execute(status -> {
                if(space.getSpaceType()==SpaceTypeEnum.PRIVATE.getValue()){
                    boolean exists = lambdaQuery().eq(Space::getUserId, userId).exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能创建一个私有空间");
                }
                //写入数据库
                boolean result = save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                if(space.getSpaceType() == SpaceTypeEnum.TEAM.getValue()){
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(space.getUserId());
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    boolean result1 = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result1, ErrorCode.OPERATION_ERROR);
                }
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    /**
     * 编辑空间
     * @param spaceEditRequest
     * @param request
     * @return
     */
    @Override
    public boolean editSpace(SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        // 将dto转换为实体类
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest,space);
        //设置编辑时间
        space.setEditTime(new Date());
        // 数据校验
        validSpace(space,false);
        // 判断数据库是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = this.getById(id);
        ThrowUtils.throwIf(oldSpace==null, ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可操作
        User loginUser = userService.getLoginUser(request);
        checkSpaceAuth(loginUser,space);
        // 操作数据库
        boolean result = this.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 删除空间
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public boolean deleteSpace(DeleteRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        // 判断是否存在
        Space oldSpace = this.getById(id);
        ThrowUtils.throwIf(oldSpace==null, ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可操作
        User loginUser = userService.getLoginUser(request);
        checkSpaceAuth(loginUser,oldSpace);
        boolean result = this.removeById(oldSpace);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 获取SpaceVO
     * @param space
     * @return
     */
    @Override
    public SpaceVO getSpaceVO(Space space) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = spaceVO.getUserId();
        if(userId!=null || userId>0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    /**
     * 根据id查询空间
     * @param id
     * @return
     */
    @Override
    public SpaceVO getSpaceVOById(long id,HttpServletRequest request) {
        Space space = this.getById(id);
        ThrowUtils.throwIf(space==null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        SpaceVO spaceVO = getSpaceVO(space);
        spaceVO.setPermissionList(permissionList);
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> listSpaceVOByPage(SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(spaceQueryRequest));
        return getSpaceVOPage(spacePage,request);

    }

    /**
     * 获取Page<SpaceVO>
     * @param spacePage
     * @param request
     * @return
     */
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public Page<Space> listSpaceByPage(SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(spaceQueryRequest));
        return spacePage;
    }

    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        if(!Objects.equals(space.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser))
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
    }

    /**
     * 获取查询条件
     * @param spaceQueryRequest
     * @return
     */
    @Override
    public Wrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

}




