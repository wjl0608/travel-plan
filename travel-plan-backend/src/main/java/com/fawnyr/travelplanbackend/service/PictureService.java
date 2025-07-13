package com.fawnyr.travelplanbackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fawnyr.travelplanbackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.fawnyr.travelplanbackend.common.DeleteRequest;
import com.fawnyr.travelplanbackend.model.dto.picture.*;
import com.fawnyr.travelplanbackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.vo.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author wujialu
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-25 10:57:33
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    /**
     * 删除图片
     * @param deleteRequest
     * @param request
     * @return
     */
    boolean deletePicture(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 更新图片
     * @param pictureUpdateRequest
     * @return
     */
    boolean updatePicture(PictureUpdateRequest pictureUpdateRequest,HttpServletRequest request);

    /**
     * 图片校验
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 分页获取图片列表
     * @param pictureQueryRequest
     * @return
     */
    Page<Picture> listPictureByPage(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取查询条件
     * @param pictureQueryRequest
     * @return
     */
    Wrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 分页获取图片列表（包装类）
     * @param pictureQueryRequest
     * @return
     */
    Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest,HttpServletRequest request);

    /**
     * 分页获取图片封装
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 编辑图片
     * @param pictureEditRequest
     * @param request
     * @return
     */
    boolean editPicture(PictureEditRequest pictureEditRequest, HttpServletRequest request);

    /**
     * 审核图片
     * @param pictureReviewRequest
     * @param loginUser
     * @return
     */
    boolean reviewPicture(PictureReviewRequest pictureReviewRequest,User loginUser);

    /**
     * 设置审核状态
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    /**
     * 分页获取图片封装(有缓存)
     */
    Page<PictureVO> listPictureVOByPageWithCache(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    /**
     * 校验空间图片权限
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser,Picture picture);

    /**
     * 通过颜色搜图
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
     List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量修改图片
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
     void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建扩图任务
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return
     */
     CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

}
