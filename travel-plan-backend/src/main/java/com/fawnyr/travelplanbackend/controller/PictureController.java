package com.fawnyr.travelplanbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fawnyr.travelplanbackend.annotation.AuthCheck;
import com.fawnyr.travelplanbackend.api.aliyunai.AliYunAiApi;
import com.fawnyr.travelplanbackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.fawnyr.travelplanbackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.fawnyr.travelplanbackend.api.imageSearch.ImageSearchApiFacade;
import com.fawnyr.travelplanbackend.api.imageSearch.model.ImageSearchResult;
import com.fawnyr.travelplanbackend.common.BaseResponse;
import com.fawnyr.travelplanbackend.common.DeleteRequest;
import com.fawnyr.travelplanbackend.common.ResultUtils;
import com.fawnyr.travelplanbackend.constant.UserConstant;
import com.fawnyr.travelplanbackend.exception.BusinessException;
import com.fawnyr.travelplanbackend.exception.ErrorCode;
import com.fawnyr.travelplanbackend.exception.ThrowUtils;
import com.fawnyr.travelplanbackend.manager.CosManager;
import com.fawnyr.travelplanbackend.manager.auth.SpaceUserAuthManager;
import com.fawnyr.travelplanbackend.manager.auth.StpKit;
import com.fawnyr.travelplanbackend.manager.auth.annotion.SaSpaceCheckPermission;
import com.fawnyr.travelplanbackend.manager.auth.model.SpaceUserPermissionConstant;
import com.fawnyr.travelplanbackend.model.dto.picture.*;
import com.fawnyr.travelplanbackend.model.dto.user.UserQueryRequest;
import com.fawnyr.travelplanbackend.model.entity.Picture;
import com.fawnyr.travelplanbackend.model.entity.Space;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.vo.PictureTagCategory;
import com.fawnyr.travelplanbackend.model.vo.PictureVO;
import com.fawnyr.travelplanbackend.model.vo.UserVO;
import com.fawnyr.travelplanbackend.service.PictureService;
import com.fawnyr.travelplanbackend.service.SpaceService;
import com.fawnyr.travelplanbackend.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private AliYunAiApi aliYunAiApi;
    @Resource
    private SpaceService spaceService;
    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

     /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 删除图片
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if(deleteRequest==null || deleteRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = pictureService.deletePicture(deleteRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 更新图片
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,HttpServletRequest request){
        // 检验请求数据
        if(pictureUpdateRequest==null || pictureUpdateRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = pictureService.updatePicture(pictureUpdateRequest,request);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取图片（管理员 未脱敏数据）
     * @param id
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id,HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(id<=0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture==null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    /**
     * 根据id获取图片（脱敏数据）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id,HttpServletRequest request){
        ThrowUtils.throwIf(id<=0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture==null, ErrorCode.NOT_FOUND_ERROR);
        Long spaceId = picture.getSpaceId();
        Space space = null;
        if(spaceId!=null){
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
            // User loginUser = userService.getLoginUser(request);
            //校验权限 已经使用注解鉴权
            //pictureService.checkPictureAuth(loginUser,picture);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        }
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        PictureVO pictureVO = PictureVO.objToVo(picture);
        pictureVO.setPermissionList(permissionList);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页获取图片列表
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest){
        ThrowUtils.throwIf(pictureQueryRequest==null, ErrorCode.PARAMS_ERROR);
        Page<Picture> picturePage = pictureService.listPictureByPage(pictureQueryRequest);
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（包装类）
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureQueryRequest==null, ErrorCode.PARAMS_ERROR);
        Page<PictureVO> pictureVOPage = pictureService.listPictureVOByPage(pictureQueryRequest,request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 分页获取图片列表（包装类有缓存）
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureQueryRequest==null, ErrorCode.PARAMS_ERROR);
        Page<PictureVO> pictureVOPage = pictureService.listPictureVOByPageWithCache(pictureQueryRequest,request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request){
        // 检验请求数据
        if(pictureEditRequest==null || pictureEditRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = pictureService.editPicture(pictureEditRequest,request);
        return ResultUtils.success(result);
    }

    /**
     * 获取预置标签和分类
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("特色", "美食", "游玩", "购物", "艺术体验", "拍照");
        List<String> categoryList = Arrays.asList("景点", "购物", "美食", "住宿");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request){
        // 检验请求数据
        if(pictureReviewRequest==null || pictureReviewRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = pictureService.reviewPicture(pictureReviewRequest,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 批量抓取图片
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        String url = oldPicture.getUrl()+"?imageMogr2/format/png";
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(url);
        return ResultUtils.success(resultList);
    }

    /**
     * 颜色搜图
     * @param searchPictureByColorRequest
     * @param request
     * @return
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 批量编辑图片
     * @param pictureEditByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建 AI 扩图任务
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }

}


