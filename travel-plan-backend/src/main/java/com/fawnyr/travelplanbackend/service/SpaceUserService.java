package com.fawnyr.travelplanbackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fawnyr.travelplanbackend.common.DeleteRequest;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceQueryRequest;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserAddRequest;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserEditRequest;
import com.fawnyr.travelplanbackend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.fawnyr.travelplanbackend.model.entity.Space;
import com.fawnyr.travelplanbackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fawnyr.travelplanbackend.model.vo.SpaceUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author wujialu
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-07-05 13:56:37
*/
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 空间角色校验
     * @param spaceUser
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);


    /**
     * 创建空间角色
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 删除空间
     * @param deleteRequest
     * @return
     */
    boolean deleteSpaceUser(DeleteRequest deleteRequest);

    /**
     * 获取查询条件
     * @param spaceUserQueryRequest
     * @return
     */
    Wrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取单个封装类
     * @param spaceUser
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser);

    /**
     * 获取封装类列表
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 编辑空间用户
     * @param spaceUserEditRequest
     * @return
     */
    boolean editSpaceUser(SpaceUserEditRequest spaceUserEditRequest);
}
