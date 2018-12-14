package study.jsp.mysite.controller.bbs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import study.jsp.helper.BaseController;
import study.jsp.helper.RegexHelper;
import study.jsp.helper.WebHelper;
import study.jsp.mysite.dao.MyBatisConnectionFactory;
import study.jsp.mysite.model.BbsComment;
import study.jsp.mysite.model.Member;
import study.jsp.mysite.service.BbsCommentService;
import study.jsp.mysite.service.impl.BbsCommentServiceImpl;

@WebServlet("/bbs/comment_insert.do")
public class CommentInsert extends BaseController {


	private static final long serialVersionUID = 8673166887122637294L;
	/** (1) 사용하고자 하는 Helper 객체 선언 */
	// --> import org.apache.logging.log4j.Logger;
	Logger logger;
	// --> import org.apache.ibatis.session.SqlSession;
	SqlSession sqlSession;
	// --> import study.jsp.helper.WebHelper;
	WebHelper web;
	// --> import study.jsp.helper.RegexHelper;
	RegexHelper regex;
	// --> import study.jsp.mysite.service.BbsCommentService;
	BbsCommentService bbsCommentService;
	
	@Override
	public String doRun(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		/** (2) 페이지 형식 지정 + 사용하고자 하는 Helper+Service 객체 생성 */
		// 페이지 형식을 JSON으로 설정한다.
		response.setContentType("application/json");
		
		logger = LogManager.getFormatterLogger(request.getRequestURI());
		sqlSession = MyBatisConnectionFactory.getSqlSession();
		web = WebHelper.getInstance(request, response);
		regex = RegexHelper.getInstance();
		// --> import study.jsp.mysite.service.impl.BbsCommentServiceImpl;
		bbsCommentService = new BbsCommentServiceImpl(sqlSession, logger);
		
		/** (3) 파라미터 받기 */
		int bbsDocumentId = web.getInt("document_id");
		String writerName = web.getString("writer_name");
		String writerPw = web.getString("writer_pw");
		String email = web.getString("email");
		String content = web.getString("content");
		// 작성자 아이피 주소 가져오기
		String ipAddress = web.getClientIP();
		// 회원 일련번호 --> 비 로그인인 경우 0
		int memberId = 0;

		// 로그인 한 경우, 입력하지 않은 이름, 비밀번호, 이메일을 세션정보로 대체
		Member loginInfo = (Member) web.getSession("loginInfo");
		if (loginInfo != null) {
			writerName = loginInfo.getName();
			email = loginInfo.getEmail();
			writerPw = loginInfo.getUserPw();
			memberId = loginInfo.getId();
		}

		// 전달된 파라미터는 로그로 확인한다.
		logger.debug("bbs_document_id=" + bbsDocumentId);
		logger.debug("writer_name=" + writerName);
		logger.debug("writer_pw=" + writerPw);
		logger.debug("email=" + email);
		logger.debug("content=" + content);
		logger.debug("ipAddress=" + ipAddress);
		logger.debug("memberId=" + memberId);
		
		/** (4) 입력 받은 파라미터에 대한 유효성 검사 */
		// 덧글이 소속될 게시물의 일련번호
		if (bbsDocumentId == 0) {
			sqlSession.close();
			web.printJsonRt("게시물 일련번호가 없습니다.");
			return null;
		}
		
		// 이름 + 비밀번호
		if (!regex.isValue(writerName)) {
			sqlSession.close();
			web.printJsonRt("작성자 이름를 입력하세요.");
			return null;
		}

		if (!regex.isValue(writerPw)) {
			sqlSession.close();
			web.printJsonRt("비밀번호를 입력하세요.");
			return null;
		}

		// 이메일
		if (!regex.isValue(email)) {
			sqlSession.close();
			web.printJsonRt("이메일을 입력하세요.");
			return null;
		}

		if (!regex.isEmail(email)) {
			sqlSession.close();
			web.printJsonRt("이메일의 형식이 잘못되었습니다.");
			return null;
		}

		// 내용 검사
		if (!regex.isValue(content)) {
			sqlSession.close();
			web.printJsonRt("내용을 입력하세요.");
			return null;
		}

		/** (5) 입력 받은 파라미터를 Beans로 묶기 */
		BbsComment comment = new BbsComment();
		comment.setBbsDocumentId(bbsDocumentId);
		comment.setWriterName(writerName);
		comment.setWriterPw(writerPw);
		comment.setEmail(email);
		comment.setContent(content);
		comment.setIpAddress(ipAddress);
		comment.setMemberId(memberId);
		logger.debug("comment >> " + comment.toString());
		
		/** (6) Service를 통한 덧글 저장 */
		// 작성 결과를 저장할 객체
		BbsComment item = null;
		try {
			bbsCommentService.insertComment(comment);
			item = bbsCommentService.selectComment(comment);
		} catch (Exception e) {
			web.printJsonRt(e.getLocalizedMessage());
			return null;
		} finally {
			sqlSession.close();
		}
		
		/** (7) 처리 결과를 JSON으로 출력하기 */
		// 줄바꿈이나 HTML특수문자에 대한 처리
		item.setWriterName(web.convertHtmlTag(item.getWriterName()));
		item.setEmail(web.convertHtmlTag(item.getEmail()));
		item.setContent(web.convertHtmlTag(item.getContent()));
		
		// --> import java.util.HashMap;
		// --> import java.util.Map;
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("rt", "OK");
		data.put("item", item);
		
		// --> import com.fasterxml.jackson.databind.ObjectMapper;
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getWriter(), data);
		
		return null;
	}

}
