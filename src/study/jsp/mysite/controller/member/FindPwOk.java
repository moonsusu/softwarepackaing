package study.jsp.mysite.controller.member;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import study.jsp.helper.BaseController;
import study.jsp.helper.MailHelper;
import study.jsp.helper.Util;
import study.jsp.helper.WebHelper;
import study.jsp.mysite.dao.MyBatisConnectionFactory;
import study.jsp.mysite.model.Member;
import study.jsp.mysite.service.MemberService;
import study.jsp.mysite.service.impl.MemberServiceImpl;

@WebServlet("/member/find_pw_ok.do")
public class FindPwOk extends BaseController {
	private static final long serialVersionUID = -8254336411293536082L;

	/** (1) 사용하고자 하는 Helper 객체 선언 */
	// --> import org.apache.logging.log4j.Logger;
	Logger logger;
	// --> import org.apache.ibatis.session.SqlSession;
	SqlSession sqlSession;
	// --> import study.jsp.helper.WebHelper;
	WebHelper web;
	// --> import study.jsp.helper.MailHelper;
	MailHelper mail;
	// --> import study.jsp.helper.Util;
	Util util;
	// --> import study.jsp.mysite.service.MemberService;
	MemberService memberService;
	
	@Override
	public String doRun(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/** (2) 사용하고자 하는 Helper+Service 객체 생성 */
		// --> import org.apache.logging.log4j.LogManager;
		logger = LogManager.getFormatterLogger(request.getRequestURI());
		// --> import study.jsp.mysite.service.impl.MemberServiceImpl;
		sqlSession = MyBatisConnectionFactory.getSqlSession();
		web = WebHelper.getInstance(request, response);
		mail = MailHelper.getInstance();
		util = Util.getInstance();
		// --> import study.jsp.mysite.service.impl.MemberServiceImpl;
		memberService = new MemberServiceImpl(sqlSession, logger);

		/** (3) 로그인 여부 검사 */
		// 로그인 중이라면 이 페이지를 이용할 수 없다.
		if (web.getSession("loginInfo") != null) {
			sqlSession.close();
			web.redirect(web.getRootPath() + "/index.do", "이미 로그인 중입니다.");
			return null;
		}
		
		/** (4) 파라미터 받기 */
		// 입력된 메일 주소를 받는다.
		String email = web.getString("email");

		logger.debug("email=" + email);
		
		if (email == null) {
			sqlSession.close();
			web.redirect(null, "이메일 주소를 입력하세요.");
			return null;
		}
		
		/** (5) 임시 비밀번호 생성하기 */
		String newPassword = util.getRandomPassword();
		
		/** (6) 입력값을 JavaBeans에 저장하기 */
		Member member = new Member();
		member.setEmail(email);
		member.setUserPw(newPassword);
		
		/** (7) Service를 통한 비밀번호 갱신 */		
		try {
			memberService.updateMemberPasswordByEmail(member);
		} catch (Exception e) {
			web.redirect(null, e.getLocalizedMessage());
			return null;
		} finally {
			sqlSession.close();
		}
		
		/** (8) 발급된 비밀번호를 메일로 발송하기 */
		String sender = "webmaster@mysite.com";
		String subject = "MySite 비밀번호 변경 안내 입니다.";
		String content = "회원님의 새로운 비밀번호는 <strong>" + newPassword + "</strong>입니다.";
		
		try {
			// 사용자가 입력한 메일주소를 수신자로 설정하여 메일 발송하기
			mail.sendMail(sender, email, subject, content);
		} catch (MessagingException e) {
			web.redirect(null, "메일 발송에 실패했습니다. 관리자에게 문의 바랍니다.");
			return null;
		}
		
		/** (9) 결과 페이지로 이동 */
		// 여기서는 이전 페이지로 이동함
		web.redirect(null, "새로운 비밀번호가 메일로 발송되었습니다.");
		return null;
	}

}
