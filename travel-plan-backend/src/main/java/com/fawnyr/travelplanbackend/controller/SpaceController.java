package com.fawnyr.travelplanbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fawnyr.travelplanbackend.annotation.AuthCheck;
import com.fawnyr.travelplanbackend.common.BaseResponse;
import com.fawnyr.travelplanbackend.common.DeleteRequest;
import com.fawnyr.travelplanbackend.common.ResultUtils;
import com.fawnyr.travelplanbackend.constant.UserConstant;
import com.fawnyr.travelplanbackend.exception.BusinessException;
import com.fawnyr.travelplanbackend.exception.ErrorCode;
import com.fawnyr.travelplanbackend.exception.ThrowUtils;
import com.fawnyr.travelplanbackend.model.dto.space.*;
import com.fawnyr.travelplanbackend.model.entity.Space;
import com.fawnyr.travelplanbackend.model.entity.Space;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.enums.SpaceLevelEnum;
import com.fawnyr.travelplanbackend.model.vo.SpaceVO;
import com.fawnyr.travelplanbackend.model.vo.SpaceVO;
import com.fawnyr.travelplanbackend.service.SpaceService;
import com.fawnyr.travelplanbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    /**
     * 用户创建空间
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request){
        // 检验请求数据
        if(spaceAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long spaceId = spaceService.addSpace(spaceAddRequest,request);
        return ResultUtils.success(spaceId);
    }
    /**
     * 管理员更新空间
     * @param spaceUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest){
        // 检验请求数据
        if(spaceUpdateRequest==null || spaceUpdateRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = spaceService.updateSpace(spaceUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 编辑空间
     * @param spaceEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request){
        // 检验请求数据
        if(spaceEditRequest==null || spaceEditRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = spaceService.editSpace(spaceEditRequest,request);
        return ResultUtils.success(result);
    }

    /**
     * 删除空间
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if(deleteRequest==null || deleteRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = spaceService.deleteSpace(deleteRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取空间（管理员 未脱敏数据）
     * @param id
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(id<=0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space==null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }

    /**
     * 根据id获取图片（脱敏数据）
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request){
        ThrowUtils.throwIf(id<=0, ErrorCode.PARAMS_ERROR);
        SpaceVO spaceVO = spaceService.getSpaceVOById(id,request);
        return ResultUtils.success(spaceVO);
    }

    /**
     * 分页获取图片列表
     * @param spaceQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest){
        ThrowUtils.throwIf(spaceQueryRequest==null, ErrorCode.PARAMS_ERROR);
        Page<Space> spacePage = spaceService.listSpaceByPage(spaceQueryRequest);
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表（包装类）
     * @param spaceQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(spaceQueryRequest==null, ErrorCode.PARAMS_ERROR);
        Page<SpaceVO> spaceVOPage = spaceService.listSpaceVOByPage(spaceQueryRequest,request);
        return ResultUtils.success(spaceVOPage);
    }

    /**
     * 查询所有空间级别，供前端展示
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )).collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }



}
