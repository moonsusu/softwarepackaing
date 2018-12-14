package study.jsp.mysite.controller.content;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import study.jsp.helper.BaseController;

@WebServlet("/content/introduce.do")
public class Introduce extends BaseController {
	private static final long serialVersionUID = -561468701326547961L;

	@Override
	public String doRun(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		// "/WEB-INF/views/content/introduce.jsp" 파일을 View로 사용한다.
		return "content/introduce";
	}
}
