package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Service
@Slf4j
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

    @Autowired
    private WebSocketServer webSocketServer;

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
        // TODO 由于个人小程序无法接入微信支付,所以暂时直接对订单状态和支付状态直接进行修改并更新数据库的数据
        //直接调用支付成功的功能模块对订单的相关状态进行修改
        paySuccess(ordersPaymentDTO.getOrderNumber());
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

//        return vo;
        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        Long orderId = ordersDB.getId();

        // TODO 由于未接入微信支付,所以对订单状态和订单的支付状态进行判断,只有待付款和未支付的状态才能更新订单的相关状态
        if (ordersDB.getStatus().equals(Orders.PENDING_PAYMENT) && ordersDB.getPayStatus().equals(Orders.UN_PAID)) {
            // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
            Orders orders = Orders.builder()
                    .id(orderId)
                    .status(Orders.TO_BE_CONFIRMED)
                    .payStatus(Orders.PAID)
                    .checkoutTime(LocalDateTime.now())
                    .build();

            orderMapper.update(orders);

            //当用户支付成功后就为管理端页面推送来单提醒
            //通过websocket向客户端浏览器推送消息 消息封装在一个Map集合中 里面的key有 type orderId content
            Map map = new HashMap<>();
            map.put("type", 1);//1表示来单提醒 2表示用户催单
            map.put("orderId", orderId);
            map.put("content", "订单号：" + outTradeNo);

            //把map转成json字符串
            String json = JSON.toJSONString(map);
            //把json字符串推送给所有与websocket连接的客户端浏览器
            webSocketServer.sendToAllClient(json);
        }

    }

    /**
     * 分页查询历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO) {
        //为OrdersPageQueryDTO的用户id属性赋值
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
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
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);

                //把OrderVO对象添加到结果集合中
                records.add(orderVO);
            }
        }

        return new PageResult(total, records);
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
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        //校验订单是否存在 不存在则抛出异常
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断订单状态,除了待支付和待接单状态下用户可以自己取消,其他情况都抛出异常提示需要联系商家
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //新建一个订单对象,仅修改与状态相关的数据,方便订单表的更新操作
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        //如果订单处于待接单状态下取消,需要调用微信支付退款(省略,这里仅需把订单支付状态直接修改即可)
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            orders.setPayStatus(Orders.REFUND);
        }

        //更新订单状态,取消原因,取消时间
        orders.setStatus(Orders.CANCELLED);//设置订单状态为已取消
        orders.setCancelReason("用户取消");//设置订单的取消原因
        orders.setCancelTime(LocalDateTime.now());//设置订单取消时间
        orderMapper.update(orders);

    }

    /**
     * 再来一单
     *
     * @param orderId
     */
    @Transactional
    @Override
    public void repetition(Long orderId) {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //查询订单明细表
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
        //创建购物车集合
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        //遍历订单明细集合
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            //把订单明细表的属性拷贝到购物车对象中
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);//为当前购物车对象的用户id赋值
            shoppingCart.setCreateTime(LocalDateTime.now());//设置创建时间

            shoppingCartList.add(shoppingCart);//把购物车对象加入到购物车集合中
        }

        //批量插入到数据库中
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 订单搜索 分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //进行查询操作
        Page<Orders> p = orderMapper.page(ordersPageQueryDTO);

        //调用方法把订单对象集合转成订单VO对象集合
        List<OrderVO> orderVOList = getOrderVOList(p);

        //封装成PageResult对象返回
        return new PageResult(p.getTotal(), orderVOList);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        //根据订单状态查询各个订单的数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        //把数据封装成订单数字统计VO对象返回
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {

        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        //订单只有处于待接单的状态下才能进行拒单操作,否则抛出业务异常
        if (ordersDB == null || !(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED))) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //创建订单对象
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();

        //判断订单是否已经付款,付款了需要进行退款(由于未接入微信支付,所以直接修改支付状态即可)
        if (ordersDB.getPayStatus().equals(Orders.PAID)) {
            orders.setPayStatus(Orders.REFUND);
        }

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        //从数据库中查出数据,获取订单状态进行比对
        Orders ordersDB = orderMapper.getById(id);
        //只有订单状态为待派送的订单才能进行派送订单操作
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //创建一个订单对象
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        orderMapper.update(orders);

    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
//        if (payStatus == 1) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
//        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


    /**
     * 完成订单
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        //查询对应订单
        Orders ordersDB = orderMapper.getById(id);
        //判断订单的状态是否是派送中,不是则抛出业务异常
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //创建订单对象
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        //进行非空校验且订单状态处于待接单才能进行催单
        if (ordersDB == null && (ordersDB.getStatus() != Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //当用户点击催单后就为管理端页面推送催单提醒
        //通过websocket向客户端浏览器推送消息 消息封装在一个Map集合中 里面的key有 type orderId content
        Map map = new HashMap<>();
        map.put("type", 2);//1表示来单提醒 2表示用户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + ordersDB.getNumber());

        //把map转成json字符串
        String json = JSON.toJSONString(map);
        //把json字符串推送给所有与websocket连接的客户端浏览器
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 把集合中的订单对象转换成订单VO对象,并且为菜品信息字段赋值
     *
     * @param p
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Orders> p) {
        List<OrderVO> orderVOList = new ArrayList<>();

        //获取查询结果
        List<Orders> result = p.getResult();

        //作非空校验
        if (!CollectionUtils.isEmpty(result)) {
            for (Orders orders : p) {

                //把订单数据封装成VO对象
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDishes(getOrderDishes(orders.getId()));

                orderVOList.add(orderVO);

            }
        }
        return orderVOList;
    }

    /**
     * 把订单明细表的菜品数据、菜品数量以字符串的形式拼接起来
     *
     * @param orderId
     * @return
     */
    private String getOrderDishes(Long orderId) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
        List<String> orderDishList = orderDetailList.stream().map(orderDetail -> {
            String orderDishes = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
            log.info("orderDishes = " + orderDishes);
            return orderDishes;
        }).collect(Collectors.toList());//把stream流的数据收集到List集合中
        return String.join(" ", orderDishList);
    }


}
