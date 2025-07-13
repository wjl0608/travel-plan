package com.fawnyr.travelplanbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fawnyr.travelplanbackend.constant.UserConstant;
import com.fawnyr.travelplanbackend.exception.BusinessException;
import com.fawnyr.travelplanbackend.exception.ErrorCode;
import com.fawnyr.travelplanbackend.exception.ThrowUtils;
import com.fawnyr.travelplanbackend.manager.auth.StpKit;
import com.fawnyr.travelplanbackend.model.dto.user.UserAddRequest;
import com.fawnyr.travelplanbackend.model.dto.user.UserQueryRequest;
import com.fawnyr.travelplanbackend.model.dto.user.UserUpdateRequest;
import com.fawnyr.travelplanbackend.model.entity.User;
import com.fawnyr.travelplanbackend.model.enums.UserRoleEnum;
import com.fawnyr.travelplanbackend.model.vo.LoginUserVO;
import com.fawnyr.travelplanbackend.model.vo.UserVO;
import com.fawnyr.travelplanbackend.service.UserService;
import com.fawnyr.travelplanbackend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.fawnyr.travelplanbackend.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author wujialu
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-06-20 12:10:38
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验数据
        if(StrUtil.hasBlank(userAccount,userPassword,checkPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        if(userAccount.length()<4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        if(userPassword.length()<8 || checkPassword.length()<8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        if(!userPassword.equals(checkPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入密码不一致");
        // 2.检验用户账号是否和数据库中已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if(count>0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        // 3.密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4.插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if(!saveResult)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
        return user.getId();
    }

    /**
     * 密码加密
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword){
        // 加盐，混淆密码
        final String SALT = "Fawnyr";
        return DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
    }

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.检验数据
        if(StrUtil.hasBlank(userAccount,userPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        if(userAccount.length()<4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        if(userPassword.length()<8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        //2.加密
        String encryptPassword = getEncryptPassword(userPassword);
        //3.检验用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user==null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        //4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        //5.记录用户的登录态 注意这个过去时间和 SpringSession 一致的
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);

        return this.getLoginUserVO(user);
    }

    /**
     * 获取脱敏的登录用户信息
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if(user==null)
            return null;
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    /**
     * 获得脱敏后的用户信息
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if(user==null)
            return null;
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }

    /**
     * 获得脱敏后的用户信息列表
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if(CollUtil.isEmpty(userList))
           return new ArrayList<>();
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser==null || currentUser.getId()==null)
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        // 从数据库中查询
        Long userId = currentUser.getId();
        User user = this.getById(userId);
        if(user==null)
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        StpKit.SPACE.logout(((User) userObj).getId());
        return true;
    }


    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if(userQueryRequest==null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(id),"id",id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userRole),"userRole",userRole);
        queryWrapper.like(ObjectUtil.isNotEmpty(userName),"userName",userName);
        queryWrapper.like(ObjectUtil.isNotEmpty(userAccount),"userAccount",userAccount);
        queryWrapper.like(ObjectUtil.isNotEmpty(userProfile),"userProfile",userProfile);
        queryWrapper.orderBy(ObjectUtil.isNotEmpty(sortOrder),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    /**
     * 添加用户（管理员）
     * @param userAddRequest
     * @return
     */
    @Override
    public long addUser(UserAddRequest userAddRequest) {
        User user = new User();
        BeanUtil.copyProperties(userAddRequest,user);
        String userPassword = "12345678";
        String encryptPassword = getEncryptPassword(userPassword);
        user.setUserPassword(encryptPassword);
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return user.getId();
    }

    /**
     * 更新用户（管理员）
     * @param userUpdateRequest
     * @return
     */
    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest) {
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest,user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 分页获取用户列表（管理员）
     * @param userQueryRequest
     * @return
     */
    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current,pageSize,userPage.getTotal());
        List<UserVO> userVOList = this.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}




