package d.money.web.action;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import d.money.common.utils.MD5Util;
import d.money.pojo.base.Admin;
import d.money.pojo.base.AdminExample;
import d.money.service.AdminService;
import d.money.service.impl.AdminServiceImpl;

@Controller
@RequestMapping("/user")
public class UserLoginController {

	@Autowired
	AdminService adminservice;

	private static final Logger logger = LoggerFactory
			.getLogger(AdminServiceImpl.class);

	@RequestMapping("/login")
	public String toAdminLogin(HttpServletRequest request) {
		String id = request.getParameter("username");
		String password = MD5Util.MD5(request.getParameter("password"));

		logger.debug(id);
		logger.debug(password);

		return "money/user1";
	}

	/**
	 * 跳转至注册邀请页面
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/regInvite")
	public String userRegister(HttpServletRequest request,
			HttpServletResponse response) {
		return "money/registerInvite";
	}

	/**
	 * 确认邀请码
	 * @param request
	 * @param pw
	 * @throws Exception 
	 */
	@RequestMapping("/confirmUser")
	public void confirmUser(HttpServletRequest request, PrintWriter pw) {
		String inviteCode = request.getParameter("code");
		AdminExample example = new AdminExample();
		example.createCriteria().andSigninCodeEqualTo(inviteCode);
		List<Admin> list = adminservice.selectByExample(example);
		if (list.size() > 0) {
			pw.print("success");
		} else {
			pw.print("error");
		}
	}

	/**
	 * 跳转到注册页面
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/regUser")
	public String regUser(HttpServletRequest request) {
		return "money/register";
	}

	@RequestMapping("/regsubmit")
	public String userRegSubmit() {
		return "money/registerSuccess";
	}
}
