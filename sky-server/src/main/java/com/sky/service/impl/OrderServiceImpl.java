package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种业务异常(地址簿为空,购物车为空)
        //查询当前用户的地址簿数据
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        //判断地址簿是否为空
        if (addressBook == null) {
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        //判断当前用户的购物车是否为空
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            //抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //以当前用户id向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);//设置用户id
        orders.setOrderTime(LocalDateTime.now());//设置订单时间 当前时间
        orders.setPayStatus(Orders.UN_PAID);//设置支付状态 初始为未支付
        orders.setStatus(Orders.PENDING_PAYMENT);//设置订单状态 初始为待支付
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//订单号
        orders.setPhone(addressBook.getPhone());//用户手机号
        orders.setConsignee(addressBook.getConsignee());//收货人
        orders.setAddress(addressBook.getDetail());//地址 详细地址

        orderMapper.insert(orders);

        //以当前用户id向订单明细表插入n条数据
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart sc : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();//订单明细对象
            BeanUtils.copyProperties(sc, orderDetail);
            orderDetail.setOrderId(orders.getId());//获取从插入操作中返回的订单id并赋值
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);//批量插入

        //清空当前用户的购物车数据
        shoppingCartMapper.deleteAll(userId);

        //封装成订单VO对象返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 分页查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO) {
        //为OrdersPageQueryDTO的用户id属性赋值
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //执行查询操作
        Page<Orders> p = orderMapper.page(ordersPageQueryDTO);

        long total = p.getTotal();
        List<Orders> result = p.getResult();

        List<OrderVO> records = new ArrayList<>();

        //判断是否有数据查出,有则查询各个订单对应的订单明细
        if (p != null && p.getTotal() > 0) {
            for (Orders orders : p) {
                //获取订单id
                Long orderId = orders.getId();
                //根据订单id查询对应的订单明细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                //把订单数据和订单明细数据赋值给OrdersVO对象
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetailList);

                //把OrderVO对象添加到结果集合中
                records.add(orderVO);
            }
        }

        return new PageResult(total,records);
    }

    /**
     * 查询订单详情
     *
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        //获取订单数据
        Orders orders = orderMapper.getById(id);

        //根据订单id获取订单明细表的数据
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

       //把查询的数据封装成orderVO对象返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }
}
