package com.oracle.ebp_16.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oracle.ebp_16.domain.ShoppingCart;
import com.oracle.ebp_16.domain.ShoppingCartItem;
import com.oracle.ebp_16.domain.User;
import com.oracle.ebp_16.service.ProductService;
import com.oracle.ebp_16.service.ShoppingCartItemService;
import com.oracle.ebp_16.service.ShoppingCartService;
import com.oracle.ebp_16.util.Constant;

@Controller
public class ShoppingCartController {

	@Resource
	ShoppingCartService shoppingCartService;
	@Resource
	ProductService productService;
	@Resource
	ShoppingCartItemService shoppingCartItemService;
	
	@RequestMapping(value = "/addShoppingCart")
	@ResponseBody
	public String addShoppingCart(String uid, String pid, String buynum) {
		Integer uidiInteger = null;
		Integer pidiInteger = null;
		Integer mountInteger = null;
		if (uid != null || !uid.trim().equals("")) {
			uidiInteger = Integer.parseInt(uid);
		}
		if (pid != null || !pid.trim().equals("")) {
			pidiInteger = Integer.parseInt(pid);
		}
		if (buynum != null || !buynum.trim().equals("")) {
			mountInteger = Integer.parseInt(buynum);
		}
		if (uidiInteger != null && pidiInteger != null && mountInteger != null) {
			ShoppingCart shoppingCart = new ShoppingCart(-1, uidiInteger,
					pidiInteger, mountInteger, 1);
			ShoppingCart exist = shoppingCartService.queryShopCartByUPid(
					uidiInteger, pidiInteger);
			int row = 0;
			if (exist == null) {
				row = shoppingCartService.addShoppingCart(shoppingCart);
			} else if (exist != null) {
				row = shoppingCartService.addShoppingIfExist(uidiInteger,pidiInteger,mountInteger);
			}
			int urow = productService.updateProduct(pidiInteger, mountInteger);
			System.out.println("update product row:"+urow);
			System.out.println("ShoppingCartController:addShoppingCart:" + row);
			return row + "";
		}
		System.out.println("ShoppingCartController:addShoppingCart:" + 0);
		return 0 + "";
	}

	
	
	
	
	
	
	@RequestMapping(value = "/my/deleteById")
	@ResponseBody
	public String deleteById(String scid,HttpSession session) {
		int row = 0;
		if (scid != null)  {
			System.out.println("deleteById:"+scid);
			row = shoppingCartService
					.deleteShoppingCart(Integer.parseInt(scid));
		}
		User user = (User)session.getAttribute(Constant.SESSION_USER);
		List<ShoppingCartItem> list = shoppingCartItemService.listAllItems(user.getUid());
		session.setAttribute(Constant.SESSION_SHOPPINGCART_ITEMS, list);
		System.out.println("row:"+row);
		return row + "";
	}
	
	
	
	
	
	
	
	
	
	@RequestMapping(value="/my/myCar.action")
	public String listAllItems(HttpSession session){
		System.out.println("my myCar.action");
		User user = (User)session.getAttribute("user");
		List<ShoppingCartItem> list = null;
		int uid;
		if (user != null) {
			uid = user.getUid();
			list = shoppingCartItemService.listAllItems(uid);
			for (ShoppingCartItem shoppingCartItem : list) {
				System.out.println("listAllItems:"+shoppingCartItem);
			}
		}
		else{
			System.out.println("失败    ==== ");
			return "/my/myCar";
		}
//		Double sumMoney = 0.0;
//		Integer summount = 0;
		if (list.size() != 0) {
			for (ShoppingCartItem shoppingCartItem : list) {
				System.out.println(shoppingCartItem);
			}
		}
//			for (ShoppingCartItem shoppingCartItem : list) {
//				summount+=shoppingCartItem.getMount();
//				sumMoney+=shoppingCartItem.getSumprice();
//		 	}
//		session.setAttribute("sumMoney", sumMoney);
//		session.setAttribute("summount", summount);
		
		session.setAttribute(Constant.SESSION_SHOPPINGCART_ITEMS, list);
		return "/my/myCar";
	}
}
