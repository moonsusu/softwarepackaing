package study.jsp.mysite.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import study.jsp.helper.BaseController;
import study.jsp.helper.WebHelper;

@WebServlet("/Hello")
public class Hello extends BaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7792946491755718270L;

	@Override
	public String doRun(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/** 파라미터 받기 */
		// --> import study.jsp.helper.WebHelper;
		WebHelper web = WebHelper.getInstance(request, response);
		String msg = web.getString("msg");
		
		// logger 객체는 부모클래스로부터 상속받는다.
		logger.debug("Hello 클래스의 doRun 메서드 실행됨");
		
		// View에 데이터 전달
		request.setAttribute("msg", msg);
		
		String view = "hello";
		return view;
	}
}
