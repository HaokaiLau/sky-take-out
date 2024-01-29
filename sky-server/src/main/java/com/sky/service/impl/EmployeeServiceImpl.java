package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

//    @Autowired
//    private HttpServletRequest httpServletRequest;
//
//    @Autowired
//    private JwtProperties jwtProperties;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端传来的密码使用spring框架提供的DigestUtils工具类的md5DigestAsHex方法进行md5加密
        //用经过md5加密后的密文与数据库中的密码进行比对(数据库的密码也要用md5加密)
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //把EmployeeDTO对象里的数据给到Employee对象
        //使用BeanUtils工具类里的方法进行属性拷贝
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置帐号的状态,默认给的是启用状态
        employee.setStatus(StatusConstant.ENABLE);

        //设置帐号的默认密码,并且使用md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置当前记录的创建时间和最后一次更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置记录的创建者和修改者,即获取当前登录用户的id

        //原始方法,通过HttpServletRequest对象获取头部的token再通过解析令牌获取当前用户id
//        //通过请求来获取请求头的信息 token
//        String token = httpServletRequest.getHeader(jwtProperties.getAdminTokenName());
//        //解析jwt令牌中的内容
//        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
//        //通过get方法根据key获取value
//        Long id = (Long) claims.get(JwtClaimsConstant.EMP_ID);

        //新方法,通过ThreadLocal提供的set get方法对数据进行存取
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

}
