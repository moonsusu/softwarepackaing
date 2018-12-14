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
import study.jsp.mysite.model.Member;
import study.jsp.mysite.service.BbsCommentService;
import study.jsp.mysite.service.impl.BbsCommentServiceImpl;

@WebServlet("/bbs/comment_delete.do")

public class CommentDelete extends BaseController{


	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7842320021608197366L;
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
	public String doRun(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		/** (2) 사용하고자 하는 Helper+Service 객체 생성 */
		// --> import org.apache.logging.log4j.LogManager;
		logger = LogManager.getFormatterLogger(request.getRequestURI());
		// --> import study.jsp.mysite.service.impl.MemberServiceImpl;
		sqlSession = MyBatisConnectionFactory.getSqlSession();
		web = WebHelper.getInstance(request, response);
		// --> import study.jsp.mysite.service.impl.BbsDocumentServiceImpl;
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
		
		//로그인 한 경우 현재 회원의 일련번호를 추가한다. (비로그인 시 0으로 설정됨)
		Member loginInfo = (Member) web.getSession("loginInfo");
		if (loginInfo != null) {
			comment.setMemberId(loginInfo.getId());
		}

		/** (4) 게시물 일련번호를 사용한 데이터 조회 */
		//회원번호가 일치하는 게시물 수 조회하기
		int commentCount = 0;
		try {
			commentCount = bbsCommentService.selectCommentCountByMemberId(comment);
		} catch (Exception e) {
			web.redirect(null, e.getLocalizedMessage());
			return null;
		} finally {
			sqlSession.close();
		}
		
		/**(5) 자신의 글에 대한 요청인지에 대한 여부를 view에 전달 */
		boolean myComment = commentCount > 0;
		logger.debug("myComment = " + myComment);
		request.setAttribute("myComment", myComment);
		// 상태유지를 위하여 게시글 일련번호를 "View"에 전달한다.
		request.setAttribute("commentId", commentId);

		return "bbs/comment_delete";
		
	}
	

}
