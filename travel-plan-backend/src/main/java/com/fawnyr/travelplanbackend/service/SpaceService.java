package com.fawnyr.travelplanbackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fawnyr.travelplanbackend.common.DeleteRequest;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceAddRequest;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceEditRequest;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceQueryRequest;
import com.fawnyr.travelplanbackend.model.dto.space.SpaceUpdateRequest;
import com.fawnyr.travelplanbackend.model.dto.space.analyze.SpaceSizeAnalyzeRequest;
import com.fawnyr.travelplanbackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.vo.SpaceVO;
import com.fawnyr.travelplanbackend.model.vo.SpaceVO;
import com.fawnyr.travelplanbackend.model.vo.space.analyze.SpaceSizeAnalyzeResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author wujialu
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-07-02 10:29:59
*/
public interface SpaceService extends IService<Space> {

    /**
     * 空间校验
     * @param space
     */
    void validSpace(Space space,boolean add);

    /**
     * 根据空间级别，自动填充限额
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 更新空间
     * @param spaceUpdateRequest
     * @return
     */
    boolean updateSpace(SpaceUpdateRequest spaceUpdateRequest);

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param request
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request);

    /**
     * 编辑空间
     * @param spaceEditRequest
     * @param request
     * @return
     */
    boolean editSpace(SpaceEditRequest spaceEditRequest, HttpServletRequest request);

    /**
     * 删除空间
     * @param deleteRequest
     * @param request
     * @return
     */
    boolean deleteSpace(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 获取SpaceVO
     * @param space
     * @return
     */
    SpaceVO getSpaceVO(Space space);
    /**
     * 根据id查询空间
     * @param id
     * @return
     */
    SpaceVO getSpaceVOById(long id,HttpServletRequest request);

    /**
     * 分页获取
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    Page<SpaceVO> listSpaceVOByPage(SpaceQueryRequest spaceQueryRequest, HttpServletRequest request);

    /**
     * 获取查询条件
     * @param spaceQueryRequest
     * @return
     */
    Wrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取Page<SpaceVO>
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取Page<Space>
     * @param spaceQueryRequest
     * @return
     */
    Page<Space> listSpaceByPage(SpaceQueryRequest spaceQueryRequest);

    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);


}
