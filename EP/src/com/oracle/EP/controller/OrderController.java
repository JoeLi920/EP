package com.oracle.ebp_16.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.oracle.ebp_16.domain.Order;
import com.oracle.ebp_16.domain.OrderList;
import com.oracle.ebp_16.domain.Orders;
import com.oracle.ebp_16.domain.PageBean;
import com.oracle.ebp_16.domain.SelectOrdersBean;
import com.oracle.ebp_16.domain.ShoppingCartItem;
import com.oracle.ebp_16.domain.User;
import com.oracle.ebp_16.exception.OrderException;
import com.oracle.ebp_16.service.OrderListService;
import com.oracle.ebp_16.service.OrderService;
import com.oracle.ebp_16.service.ProductService;
import com.oracle.ebp_16.service.ShoppingCartService;
import com.oracle.ebp_16.service.UserService;
import com.oracle.ebp_16.util.Constant;


@Controller
//@RequestMapping(value="/user")
public class OrderController {
	@Resource
	OrderService orderService;
	
	@Resource
	OrderListService orderListService;
	@Resource
	ProductService productService;
	@Resource
	UserService userService;
	@Resource
	ShoppingCartService shoppingCartService;
	
	@RequestMapping(value = "/my/checkMoney")
	@ResponseBody
	public String saveOrder(String[] num_val,String[] check_val, String sumPrice, String address, String phone, String recvname,
			HttpSession session) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String tsstr = "";
		tsstr = dateFormat.format(timestamp);
		// -------------------------------
		Order order = null;
		User user = null;
		Double amount = 0.0;
		amount = Double.parseDouble(sumPrice);
		String check = "";
		if (check_val == null) {
			System.out.println("check_val == null");
			return "0";
		} else if (check_val.length == 1) {
			check = check_val[0];
			System.out.println(check_val.getClass().getSimpleName());
		}
		if (num_val.length == 0) {
			return "0";
		}
		if (amount.doubleValue() == 0.0) {
			return "0";
		}
		user = (User) session.getAttribute(Constant.SESSION_USER);
		Integer uid = user.getUid();
		List<ShoppingCartItem> list = (List<ShoppingCartItem>) session
				.getAttribute(Constant.SESSION_SHOPPINGCART_ITEMS);
		List<ShoppingCartItem> list2 = new LinkedList<ShoppingCartItem>();
		for (ShoppingCartItem shoppingCartItem : list) {
			if (!check.trim().equals("")) {
				if (shoppingCartItem.getScid() == Integer.parseInt(check)) {
					list2.add(shoppingCartItem);
				}
			} else {
				for (String check1 : check_val) {
					if (shoppingCartItem.getScid() == Integer.parseInt(check1)) {
						list2.add(shoppingCartItem);
					}
				}
			}
		}
		if (list2.size() != 0) {
			System.out.println("�� ordercontroller ����ȡ���Ĺ��ﳵ��");
			for (ShoppingCartItem shoppingCartItem : list2) {
				System.out.println(shoppingCartItem);
			}
		} else {
			System.out.println("���ﳵ��ʲô��û��,��������");
			return "0";
		}
		System.out.println("\nuser is " + user + "\n");
		// ��֤���붩�� id ������
		Integer oid = orderService.queryMaxId();
		if (oid == null || oid == 0) {
			oid = 1;
		} else {
			oid = oid + 1;
		}
		if (amount == 0.0) {
			System.out.println("���˽��Ϊ0��ʧ��");
			return "0";
		}
		// ����ܶ�����˻�������ʧ��
		if (amount > user.getBalance()) {
			System.out.println("�ܶ�����˻���ʧ��");
			return "0";
		}
		// �����Ʒ���С�ڹ�������������ʧ��
		for (ShoppingCartItem shoppingCartItem : list2) {
			if (Integer.parseInt(productService.queryBanlanceById(shoppingCartItem.getPid())) < shoppingCartItem
					.getMount()) {
				System.out.println("���С�ڹ���������ʧ��");
				return "0";
			}
		}
		int row = 0;
		if (uid != null && amount != null && address != null && phone != null && recvname != null) {
			order = new Order(oid, uid, tsstr, amount, address, phone, recvname);
			row = orderService.saveOrder(order);
			session.setAttribute(Constant.SESSION_ORDER, order);
		} else {
			System.out.println(uid == null ? "uid is null\n"
					: "" + amount == null ? "amount is null\n"
							: "" + address == null ? "address is null\n"
									: "" + phone == null ? "phone is null\n"
											: "" + recvname == null ? "recvname is null\n" : "");
			System.out.println("���붩��ʧ��,���Ժ�����");
			return "0";
		}
		System.out.println("this order:" + order + "\n");
		// -----------------------------------
		int orow = 0;
		for (int i= 0;i<list2.size();i++) {
			ShoppingCartItem shoppingCartItem = list2.get(i);
			String num =num_val[i];
			Integer olid = orderListService.queryMaxId();
			if (olid == null || oid == 0) {
				olid = 1;
			} else {
				olid = olid + 1;
			}
			Integer status = 1;//Integer.parseInt(num);shoppingCartItem.getMount()
			Integer nnum=Integer.parseInt(num);
			orow += orderListService.saveOrderListItem(new OrderList(olid,shoppingCartItem.getDescs(),shoppingCartItem.getPrice(),nnum,shoppingCartItem.getSumprice(),oid,status));
			// ���¿��
			productService.updateProduct(shoppingCartItem.getPid(), nnum);
			// ������ﳵ
			shoppingCartService.deleteShoppingCart(shoppingCartItem.getScid());
			// �۳��˻����
			userService.subAccount(user.getUsername(), shoppingCartItem.getSumprice());
		}
		System.out.println("save " + orow + "orderlistitems");
		List<OrderList> list3 = orderListService.listAllOrderlistItems(oid);
		if (list3.size() != 0) {
			for (OrderList orderList : list3) {
				System.out.println("\n" + orderList + "\n");
			}
		}
		session.setAttribute(Constant.SESSION_USER, user);
		session.setAttribute(Constant.SESSION_ORDERLIST_ITEMS, list3);
		return row + "";
	}
	
	
	
	
	/*@RequestMapping(value="/user/ShowOrders.action")*/
	@RequestMapping(value="/my/ShowOrders.action")
	public String getOrders( HttpSession session,Model model){//,Boolean ajax){
		try{
			User user = (User) session.getAttribute("user");
			System.out.println();
			System.out.println("�û���ѯ���ж���");
			if(user==null) {
				System.out.println("�û�δ��¼ ��uid");
				return "my/myOrder";
			}
			String uid =user.getUid()+"" ;
			System.out.println("�����û���uidΪ"+uid);
			List<Order> orders= orderService.getOrders(uid);
			if(orders != null){
				System.out.println(orders);
				for(Order order:orders){
					System.out.println(order.getUid());
				}
			}
			model.addAttribute("orders",orders);
		}catch(OrderException e){
			e.printStackTrace();
		}
		//return (ajax==null||!ajax)?"ListLeagues":"AjaxListLeagues";
		return "my/myOrder";
	}
	
	/*
	 *����Ա����
	 */
	@RequestMapping(value="/admin/queryOrderByDate.action")
	public ModelAndView getAllOrders(Integer currentPage,Integer pageSize,Integer recordCount){
		if (currentPage==null || currentPage<=0) {
			currentPage=1;
		}
		if(pageSize==null){
//			pageSize=15;
			pageSize=8;
		}
		if(recordCount==null){
			recordCount=0;
		}
		PageBean<Orders> list=orderService.getAllOrders(currentPage, pageSize, recordCount);
		ModelAndView mv=new ModelAndView();
		mv.addObject("pageBean",list);
		System.out.println(list);
		mv.setViewName("admin/ShowAllOrder");
		return mv;
		
//		List<Orders> list = orderService.getAllOrders();
//		try {
//			if(list!=null){
//				System.out.println("��ȡ���ж����ɹ���"+list.indexOf(0));
//			}
//			model.addAttribute("list",list);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("����ԱgetAllOrders failed.");
//		}
//		return "admin/ShowAllOrder";
	}
	@RequestMapping(value="/admin/queryOrderByDate")
	public ModelAndView getOrdersByTime(Integer currentPage,Integer pageSize,Integer recordCount,String begin,String end){
		System.out.println("ǰ̨�����Ĳ�����startdate="+begin+"    enddate="+end+"          1");
		String bString=null,eString=null;
		if(begin!=null && !begin.equals("")){
			bString=begin.substring(0,4)+begin.substring(5,7)+begin.substring(8);
		}else{
			bString = "20150101";
		}
		if(end!=null && !end.equals("")){
			eString=end.substring(0,4)+end.substring(5,7)+end.substring(8);
		}else{
			eString = "20200101";
		}
		System.out.println("�������ݿ�Ĳ�����begin="+bString+"    end="+eString);
		
		if (currentPage==null || currentPage<=0) {
			currentPage=1;
		}
		if(pageSize==null){
//			pageSize=15;
			pageSize=8;
		}
		if(recordCount==null){
			recordCount=0;
		}
		PageBean<Orders> list=orderService.getOrdersPagingByTime(currentPage, pageSize, recordCount, bString, eString);
		ModelAndView mv=new ModelAndView();
		mv.addObject("pageBean",list);
		mv.setViewName("admin/ShowAllOrder");
		return mv;
//		try {
//			List<Orders> list = orderService.getOrdersByTime(bString, eString);
//			model.addAttribute("list",list);
//			System.out.println("��ѯ���listΪ "+list);
//			return "admin/ShowAllOrder";
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("ͨ��time��ѯ�������� ordercontroller"+"\nbegin is "+begin +"end is"+ end);
//			return "admin/ShowAllOrder";
//		}
	}
	
	//���ݹؼ��ַ�ҳ��ѯ����
	@RequestMapping(value="/admin/getOrdersByKeys")
	public ModelAndView getOrdersByKeys(Integer currentPage,Integer pageSize,Integer recordCount,String sname,String sidCard,String soid){
		System.out.println("sname  is "+sname + ", sidCard is "+sidCard+", soid is "+soid);
		if(sname==null || sname.equals("")){
			sname=null;
		}
		if(sidCard==null || sidCard.equals("")) {
			sidCard=null;
		}
		if(soid==null || soid.equals("")){
			soid=null;
		}
		SelectOrdersBean sob = new SelectOrdersBean(sname,sidCard,soid);
		System.out.println("�˴β�ѯ��sob ��"+sob);
		
		if (currentPage==null || currentPage<=0) {
			currentPage=1;
		}
		if(pageSize==null){
//			pageSize=15;
			pageSize=8;
		}
		if(recordCount==null){
			recordCount=0;
		}
		PageBean<Orders> list=orderService.getOrdersPagingByKeys(currentPage, pageSize, recordCount, sob);
		ModelAndView mv=new ModelAndView();
		mv.addObject("pageBean",list);
		mv.setViewName("admin/ShowAllOrder");
		return mv;
//		try {
//			List<Orders> list = orderService.getOrdersByKeys(sob,index,pageSize);
//			int count = orderService.getOrdersCountByKeys(sob);
//			model.addAttribute("list",list);
//			System.out.println("��ѯ���Ϊ"+list);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("���ݹؼ��ֲ�ѯ��������  orderController\n"+"sname  is "+sname + ", sidCard is "+sidCard+", soid is "+soid);
//		}
//		return "admin/ShowAllOrder";
	}
	
}