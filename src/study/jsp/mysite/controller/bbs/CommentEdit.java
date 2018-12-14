package study.jsp.mysite.controller.bbs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import study.jsp.helper.BaseController;
import study.jsp.helper.WebHelper;
import study.jsp.mysite.dao.MyBatisConnectionFactory;
import study.jsp.mysite.model.BbsComment;
import study.jsp.mysite.service.BbsCommentService;
import study.jsp.mysite.service.impl.BbsCommentServiceImpl;

@WebServlet("/bbs/comment_edit.do")

public class CommentEdit extends BaseController {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 3545051223160616981L;
	/** (1) 사용하고자 하는 Helper 객체 선언 */
	// --> import org.apache.logging.log4j.Logger;
	Logger logger;
	// --> import org.apache.ibatis.session.SqlSession;
	SqlSession sqlSession;
	// --> import study.jsp.helper.WebHelper;
	WebHelper web;
	// --> import study.jsp.mysite.service.BbsDocumentService;
	BbsCommentService bbsCommentService;

	
	
	@Override
	public String doRun(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/** (2) 사용하고자 하는 Helper+Service 객체 생성 */
		// --> import org.apache.logging.log4j.LogManager;
		logger = LogManager.getFormatterLogger(request.getRequestURI());
		// --> import study.jsp.mysite.service.impl.MemberServiceImpl;
		sqlSession = MyBatisConnectionFactory.getSqlSession();
		web = WebHelper.getInstance(request, response);
		bbsCommentService = new BbsCommentServiceImpl(sqlSession, logger);
		
		/**(3) 덧글 번호 받기 */
		int commentId = web.getInt("comment_id");
		if (commentId ==0) {
			sqlSession.close();
			web.redirect(null, "덧글번호가 없습니다.");
			return null;
		}

		//파라미터를 Beans로 묶기
		BbsComment comment = new BbsComment();
		comment.setId(commentId);
		
		/** 덧글 일련번호를 사용한 데이터 조회 */
		// 지금 읽고 있는 덧글이 저장될 객체
		BbsComment readComment = null;
		
		try {
			readComment = bbsCommentService.selectComment(comment);
		} catch (Exception e) {
			web.redirect(null, e.getLocalizedMessage());
			return null;
		} finally {
			sqlSession.close();
		}
		
		/** (5) 읽은 데이터를 뷰에게 전달한다. */
		request.setAttribute("comment", readComment);
		
		return "bbs/comment_edit";
	}

}
