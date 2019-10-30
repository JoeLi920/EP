package com.oracle.ebp_16.controller;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oracle.ebp_16.domain.Admin;
import com.oracle.ebp_16.domain.User;
import com.oracle.ebp_16.exception.AdminException;
import com.oracle.ebp_16.service.AdminService;

@Controller
public class AdminController {

	@Resource
	AdminService adminSvc;
	
	//根据关键字获取用户
	@RequestMapping(value="admin/queryUsersByDatePage")
	public String getuserByTime(Model model,String begin,String end){
		System.out.println("begin="+begin+"    end="+end+"          1");
		String bString=null,eString=null;
		if(begin!=null){
			bString=begin.substring(0,4)+begin.substring(5,7)+begin.substring(8);
		}
		else{
			bString = "20150101";
		}
		if(end!=null){
			eString=end.substring(0,4)+end.substring(5,7)+end.substring(8);
		}
		else{
			eString = "20200101";
		}
		System.out.println("begin="+bString+"    end="+eString);
		try {
			List<User> list = adminSvc.getUserByTime(bString, eString);
			model.addAttribute("list",list);
			System.out.println("查询结果list为 "+list);
			return "admin/ShowAllUser";
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("通过time查询用户错误  admincontroller");
			return "admin/ShowAllUser";
		}
	}
	
	@RequestMapping(value="admin/queryUsersByCondition")
	public String getUserByKeys(Model model ,String name,String idCard,String telno){
		if(name.equals("")){
			name=null;
		}
		if(idCard.equals("")) {
			idCard=null;
		}
		if(telno.equals("")) {
			telno=null;
		}
		User user2 =new User(name,idCard,telno);
		System.out.println(name+"  "+idCard+"  "+telno);
		try {
			List<User> list = adminSvc.getUserByKeys(user2);
			model.addAttribute("list",list);
			System.out.println("查询结果为"+list);
			return "admin/ShowAllUser";
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("根据关键字查询用户错误  adminController\n");
			return "admin/ShowAllUser";
		}
	}
	
	//获取所有用户
	@RequestMapping(value="admin/queryUsersByDatePage.action")
	public String getAllUsers(Model model){
		List<User> list = null;
		try {
			list = adminSvc.getAllUsers();
			if(list != null){
				for(User user:list){
					System.out.println(user.getUsername()+"   "+ user.getStatus());
				}
			}
			model.addAttribute("list",list);
			return "admin/ShowAllUser";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "admin/ShowAllUser";
	}
	
	//根据uid获得user
	public User getUserByUid(String uid){
		User user = null;
		try{
			user = adminSvc.getUserByUid(uid);
			System.out.println("getUserByUid : "+user+"\n");
			return user;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//修改用户状态
	@RequestMapping(value="admin/updateUserStatusById")
	@ResponseBody
	public String changeUserStatus(String uid,String statu){
		try{
			int rowAffect = adminSvc.changeUserStatus(uid, statu);
			System.out.println("-----   一共"+rowAffect+"行受影响。现在statu为：");
			return statu;
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("changeStatus异常-------");
			return statu;
		}
	}
//	@RequestMapping(value="admin/updateUserStatusById")
//	public String changeUserStatus(String uid,Model model){
//		try{
//			User user = getUserByUid(uid);
//			if(user!=null){
//				int status = user.getStatus();
//				if(status==1) {
//					status = 0;
//				} else if(status==0) {
//					status=1;
//				}
//				model.addAttribute("status",status);
//				int rowAffect = adminSvc.changeUserStatus(uid, status+"");
//				System.out.println("-----   一共"+rowAffect+"行受影响。");
//				return "admin/ajaxChangeStatusSucc";
//			}
//			System.out.println("user is null");
//			throw new Exception("user is null");
//		}catch(Exception e){
//			e.printStackTrace();
//			return "admin/ShowAllUser";
//		}
//	}
	
	
	@RequestMapping(value="/login.action")
	public String login(@Valid @ModelAttribute("admin")Admin admin,Errors errors,HttpSession session,HttpServletRequest request){
		if(errors.hasFieldErrors())
			return "adminlogin";
		
		Admin session_admin;
		try {
			session_admin = adminSvc.getByName(admin.getUsername());
			session.setAttribute("session_admin", session_admin);
			if(session_admin==null){
				errors.rejectValue("username", "empty.admin.username","该用户不存在");
				return "adminlogin";
			}else if(session_admin !=null && !admin.getPassword().equals(session_admin.getPassword()))
			{
				errors.rejectValue("password", "empty.admin.password","密码错误");
				return "adminlogin";
			}
			else if(session_admin !=null && admin.getPassword().equals(session_admin.getPassword()) && 
					admin.getUsername().equals(session_admin.getUsername())){
				String url=(String) request.getServletContext().getAttribute("url");
				String URL="";
				if(url != null){
					int num = url.indexOf(".");
					URL += url.substring(1, num);
					if(URL.equals("admin/ShowAllOrder"))
						return "redirect:/admin/queryOrderByDate.action";
					if(URL.equals("admin/ShowAllProduct"))
						return "redirect:/admin/showProduct.action";
					if(URL.equals("admin/ShowAllUser"))
						return "redirect:/admin/queryUsersByDatePage.action";
					return URL;
				}
				return "admin/index";		
			}
		} catch (AdminException e) {
			e.printStackTrace();
			System.out.println("admin login 有问题");
		}
		return "adminlogin";
	}
	
	@RequestMapping(value="admin/adminlogout.action")
	public String adminLogout(HttpSession session,HttpServletRequest request){
		Admin admin = null;
		try {
			admin = (Admin) session.getAttribute("session_admin");
		    request.getServletContext().setAttribute("url",null);
			System.out.println(admin);
			session.setAttribute("session_admin", null);
			System.out.println((Admin)session.getAttribute("session_admin"));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("管理员登出失败");
		}
		return "redirect:/adminlogin.jsp";
	}
}