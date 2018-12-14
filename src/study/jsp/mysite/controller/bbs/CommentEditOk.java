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

@WebServlet("/bbs/comment_edit_ok.do")

public class CommentEditOk extends BaseController{

	

	private static final long serialVersionUID = 3364534999111331506L;
	/** (1) 사용하고자 하는 Helper 객체 선언 */
	// --> import org.apache.logging.log4j.Logger;
	Logger logger;
	// --> import org.apache.ibatis.session.SqlSession;
	SqlSession sqlSession;
	// --> import study.jsp.helper.WebHelper;
	WebHelper web;
	// --> import study.jsp.helper.RegexHelper;
	RegexHelper regex;
	// --> import study.jsp.mysite.service.BbsDocumentService;
	BbsCommentService bbsCommentService;
	

	@Override
	public String doRun(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
	/** (2) 사용하고자 하는 Helper+Service 객체 생성 */
		// 페이지 형식을 JSON으로 설정한다.
		response.setContentType("application/json");
		
		// --> import org.apache.logging.log4j.LogManager;
		logger = LogManager.getFormatterLogger(request.getRequestURI());
		// --> import study.jsp.mysite.service.impl.MemberServiceImpl;
		sqlSession = MyBatisConnectionFactory.getSqlSession();
		web = WebHelper.getInstance(request, response);
		regex = RegexHelper.getInstance();
		// --> import study.jsp.mysite.service.impl.BbsDocumentServiceImpl;
		bbsCommentService = new BbsCommentServiceImpl(sqlSession, logger);
		
		/** (3) 파라미터 받기 */
		int commentId = web.getInt("comment_id");
		String writerName = web.getString("writer_name");
		String writerPw = web.getString("writer_pw");
		String email = web.getString("email");
		String content = web.getString("content");
		//작성자 아이피 주소 가져오기
		String ipAddress = web.getClientIP();
		//회원 일련번호 --> 비 로그인인 경우 0
		int memberId = 0;
		
		//전달된 파라미터는 로그로 확인한다.
		logger.debug("comment_id=" + commentId);
		logger.debug("writer_name=" + writerName);
		logger.debug("writer_pw=" + writerPw);
		logger.debug("email=" + email);
		logger.debug("content=" + content);
		logger.debug("ipAddress=" + ipAddress);
		logger.debug("memberId=" + memberId);
	System.out.println("여기까찌1");	
		/** (4) 로그인한 경우 자신의 글이라면 입력하지 않은 정보를 세션 데이터로 대체한다. */
		// 소유권 검사 정보
				boolean myComment= false;
				
				Member loginInfo = (Member) web.getSession("loginInfo");
				if (loginInfo != null) {
					try {
						// 소유권 판정을 위하여 사용하는 임시 객체
						BbsComment temp = new BbsComment();
						temp.setId(commentId);
						temp.setMemberId(loginInfo.getId());

						if (bbsCommentService.selectCommentCountByMemberId(temp) > 0) {
							// 소유권을 의미하는 변수 변경
							myComment = true;
							// 입력되지 않은 정보들 갱신
							writerName = loginInfo.getName();
							email = loginInfo.getEmail();
							writerPw = loginInfo.getUserPw();
							memberId = loginInfo.getId();
						}
					} catch (Exception e) {
						sqlSession.close();
						web.printJsonRt(e.getLocalizedMessage());
						return null;
					}
				}
				System.out.println("여기까찌2");	
				
				//전달된 파라미터는 로그로 확인한다.
				logger.debug("commentId=" + commentId);
				logger.debug("writer_name=" + writerName);
				logger.debug("writer_pw=" + writerPw);
				logger.debug("email=" + email);
				logger.debug("content=" + content);
				logger.debug("ipAddress=" + ipAddress);
				logger.debug("memberId=" + memberId);
				
				/** (5) 입력 받은 파라미터에 대한 유효성 검사 */
				if (commentId == 0) {
					sqlSession.close();
					web.printJsonRt("덧글번호가 없습니다.");
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


				if (!regex.isValue(content)) {
					sqlSession.close();
					web.printJsonRt("내용을 입력하세요.");
					return null;
				}
				
				/** (6) 입력 받은 파라미터를 Beans로 묶기 */
				BbsComment comment= new BbsComment();
				// UPDATE문의 WHERE절에서 사용해야 하므로 글 번호 추가
				// --> 글 번호는 숫자로 변환해서 처리해야 한다.
				comment.setId(commentId);
				comment.setWriterName(writerName);
				comment.setWriterPw(writerPw);
				comment.setEmail(email);
				comment.setContent(content);
				comment.setIpAddress(ipAddress);
				comment.setMemberId(memberId);
				logger.debug(comment.toString());		
				
				/** (7) 게시물 변경을 위한 Service 기능을 호출 */
				BbsComment item = null;
				try {
					// 자신의 글이 아니라면 비밀번호 검사를 먼저 수행한다.
					if (!myComment) {
						bbsCommentService.selectCommentCountByPw(comment);
					}
					bbsCommentService.updateComment(comment);
					//변경된 결과를 조회
					item = bbsCommentService.selectComment(comment);
				} catch (Exception e) {
					sqlSession.close();
					web.printJsonRt(e.getLocalizedMessage());
					return null;
				} finally {
					sqlSession.clearCache();
				}
				
				/** (8) 처리 결과를 JSON으로 출력하기 */
				//줄바꿈이나 HTML특수문자에 대한 처리
				item.setWriterName(web.convertHtmlTag(item.getWriterName()));
				item.setEmail(web.convertHtmlTag(item.getEmail()));
				item.setContent(web.convertHtmlTag(item.getContent()));
				
				//해쉬맵 맵 임포트하기
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("rt","OK");
				data.put("item", item);
				
				//-->오브젝트 매퍼 임포트
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(response.getWriter(), data);
				
		return null;
	}

	
	
}
