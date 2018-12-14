package study.jsp.mysite.controller.member;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import study.jsp.helper.BaseController;
import study.jsp.helper.FileInfo;
import study.jsp.helper.RegexHelper;
import study.jsp.helper.UploadHelper;
import study.jsp.helper.WebHelper;
import study.jsp.mysite.dao.MyBatisConnectionFactory;
import study.jsp.mysite.model.Member;
import study.jsp.mysite.service.MemberService;
import study.jsp.mysite.service.impl.MemberServiceImpl;

@WebServlet("/member/edit_ok.do")
public class EditOk extends BaseController {
	private static final long serialVersionUID = 6105880445587447360L;

	/** (1) 사용하고자 하는 Helper + Service 객체 선언 */
	// --> import org.apache.logging.log4j.Logger;
	Logger logger;
	// --> import org.apache.ibatis.session.SqlSession;
	SqlSession sqlSession;
	// --> import study.jsp.helper.WebHelper;
	WebHelper web;
	// --> import study.jsp.helper.RegexHelper;
	RegexHelper regex;
	// --> import study.jsp.helper.UploadHelper;
	UploadHelper upload;
	// --> import study.jsp.mysite.service.MemberService;
	MemberService memberService;
	
	@Override
	public String doRun(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		/** (2) 사용하고자 하는 Helper+Service 객체 생성 */
		// --> import org.apache.logging.log4j.LogManager;
		logger = LogManager.getFormatterLogger(request.getRequestURI());
		// --> import study.jsp.mysite.service.impl.MemberServiceImpl;
		sqlSession = MyBatisConnectionFactory.getSqlSession();
		web = WebHelper.getInstance(request, response);
		regex = RegexHelper.getInstance();
		upload = UploadHelper.getInstance();
		// 회원가입 처리를 위한 Service객체
		// --> import study.jsp.mysite.service.impl.MemberServiceImpl;
		memberService = new MemberServiceImpl(sqlSession, logger);

		/** (3) 로그인 여부 검사 */
		Member loginInfo = (Member) web.getSession("loginInfo");
		// 로그인 중이라면 이 페이지를 동작시켜서는 안된다.
		if (loginInfo == null) {
			sqlSession.close();
			web.redirect(web.getRootPath() + "/index.do", "로그인 후에 이용 가능합니다.");
			return null;
		}

		/** (4) 파일이 포함된 POST 파라미터 받기 */
		try {
			upload.multipartRequest(request);
		} catch (Exception e) {
			sqlSession.close();
			web.redirect(null, "multipart 데이터가 아닙니다.");
			return null;
		}

		// UploadHelper에서 텍스트 형식의 파라미터를 분류한 Map을 리턴받아서 값을 추출한다.
		Map<String, String> paramMap = upload.getParamMap();
		String userPw = paramMap.get("user_pw");
		String newUserPw = paramMap.get("new_user_pw");
		String newUserPwRe = paramMap.get("new_user_pw_re");
		String name = paramMap.get("name");
		String email = paramMap.get("email");
		String tel = paramMap.get("tel");
		String birthdate = paramMap.get("birthdate");
		String gender = paramMap.get("gender");
		String postcode = paramMap.get("postcode");
		String addr1 = paramMap.get("addr1");
		String addr2 = paramMap.get("addr2");
		// 추가 - 이미지 삭제 여부에 대한 체크박스
		String imgDel = paramMap.get("img_del");

		// 전달받은 파라미터는 값의 정상여부 확인을 위해서 로그로 확인
		logger.debug("userPw=" + userPw);
		logger.debug("newUserPw=" + newUserPw);
		logger.debug("newUserPwRe=" + newUserPwRe);
		logger.debug("name=" + name);
		logger.debug("email=" + email);
		logger.debug("tel=" + tel);
		logger.debug("birthdate=" + birthdate);
		logger.debug("gender=" + gender);
		logger.debug("postcode=" + postcode);
		logger.debug("addr1=" + addr1);
		logger.debug("addr2=" + addr2);
		logger.debug("img_del=" + imgDel);

		/** (5) 입력값의 유효성 검사 (아이디 검사 수행안함) */
		// 현재 비밀번호 검사
		if (!regex.isValue(userPw)) {
			sqlSession.close();
			web.redirect(null, "현재 비밀번호를 입력하세요.");
			return null;
		}
		
		// 신규 비밀번호 검사
		// --> 신규 비밀번호가 입력된 경우는 변경으로 간주하고, 입력하지 않은 경우는
		//     변경하지 않도록 처리한다. 그러므로 입력된 경우만 검사해야 한다.
		if (regex.isValue(newUserPw)) {
			if (!regex.isEngNum(newUserPw) || newUserPw.length() > 20) {
				sqlSession.close();
				web.redirect(null, "새로운 비밀번호는 숫자와 영문의 조합으로 20자까지만 가능합니다.");
				return null;
			}
	
			// 비밀번호 확인
			if (!newUserPw.equals(newUserPwRe)) {
				sqlSession.close();
				web.redirect(null, "비밀번호 확인이 잘못되었습니다.");
				return null;
			}
		}

		// 이름 검사
		if (!regex.isValue(name)) {
			sqlSession.close();
			web.redirect(null, "이름을 입력하세요.");
			return null;
		}

		if (!regex.isKor(name)) {
			sqlSession.close();
			web.redirect(null, "이름은 한글만 입력 가능합니다.");
			return null;
		}

		if (name.length() < 2 || name.length() > 5) {
			sqlSession.close();
			web.redirect(null, "이름은 2~5글자 까지만 가능합니다.");
			return null;
		}

		// 이메일 검사
		if (!regex.isValue(email)) {
			sqlSession.close();
			web.redirect(null, "이메일을 입력하세요.");
			return null;
		}

		if (!regex.isEmail(email)) {
			sqlSession.close();
			web.redirect(null, "이메일의 형식이 잘못되었습니다.");
			return null;
		}

		// 연락처 검사
		if (!regex.isValue(tel)) {
			sqlSession.close();
			web.redirect(null, "연락처를 입력하세요.");
			return null;
		}

		if (!regex.isCellPhone(tel) && !regex.isTel(tel)) {
			sqlSession.close();
			web.redirect(null, "연락처의 형식이 잘못되었습니다.");
			return null;
		}

		// 생년월일 검사
		if (!regex.isValue(birthdate)) {
			sqlSession.close();
			web.redirect(null, "생년월일을 입력하세요.");
			return null;
		}

		// 성별검사
		if (!regex.isValue(gender)) {
			sqlSession.close();
			web.redirect(null, "성별을 입력하세요.");
			return null;
		}

		if (!gender.equals("M") && !gender.equals("F")) {
			sqlSession.close();
			web.redirect(null, "성별이 잘못되었습니다.");
			return null;
		}
		
		
		/** (6) 프로필 사진의 삭제가 요청된 경우 */
		if (regex.isValue(imgDel) && imgDel.equals("Y")) {
			// 세션에 보관되어 있는 이미지 경로를 취득
			upload.removeFile(loginInfo.getProfileImg());
		}

		/** (7) 업로드 된 파일 정보 추출 */
		// --> 이미지 수정을 원하지 않는 경우, 삭제만 원하는 경우
		//     데이터 없음
		List<FileInfo> fileList = upload.getFileList();
		// 업로드 된 프로필 사진 경로가 저장될 변수
		String profileImg = null;

		// 업로드 된 파일이 존재할 경우만 변수값을 할당한다.
		if (fileList.size() > 0) {
			// 단일 업로드이므로 0번째 항목만 가져온다.
			FileInfo info = fileList.get(0);
			profileImg = info.getFileDir() + "/" + info.getFileName();
		}

		// 파일경로를 로그로 기록
		logger.debug("profileImg=" + profileImg);

		/** (8) 전달받은 파라미터를 Beans 객체에 담는다. */
		//  아이디는 변경할 수 없으므로 제외한다.
		Member member = new Member();
		// WHERE절에 사용할 회원번호는 세션에서 취득
		member.setId(loginInfo.getId());
		member.setUserPw(userPw);
		member.setName(name);
		member.setEmail(email);
		member.setTel(tel);
		member.setBirthdate(birthdate);
		member.setGender(gender);
		member.setPostcode(postcode);
		member.setAddr1(addr1);
		member.setAddr2(addr2);
		// 변경할 신규 비밀번호
		member.setNewUserPw(newUserPw);
		
		if (profileImg != null) {
			// 이미지가 업로드 되었다면?
			// --> 이미지 교체를 위해서 업로드 된 파일의 정보를 Beans에 등록
			member.setProfileImg(profileImg);
		} else if (profileImg == null) {
			// 이미지가 업로드 되지 않았다면?
			// --> 삭제만 체크했을 경우
			if (imgDel != null && imgDel.equals("Y")) {
				// SQL에서는 공백일 경우 null로 업데이트 하도록 분기하고 있다.
				member.setProfileImg("");
			}
		}

		/** (9) Service를 통한 데이터베이스 저장 처리 */
		// 변경된 정보를 저장하기 위한 객체
		Member editInfo = null;
		try {
			memberService.selectMemberPasswordCount(member);
			memberService.updateMember(member);
			editInfo = memberService.selectMember(member);
		} catch (Exception e) {
			web.redirect(null, e.getLocalizedMessage());
			return null;
		} finally {
			sqlSession.close();
		}
		
		/** (10) 세션, 쿠키 갱신 */
		// 일단 쿠키의 썸네일 정보를 삭제한다.
		if (imgDel != null && imgDel.equals("Y")) {
			web.removeCookie("profileThumbnail");
		}
		
		// 프로필 이미지가 있을 경우 썸네일을 생성하여 쿠키에 별도로 저장
		String newProfileImg = editInfo.getProfileImg();
		if (newProfileImg != null) {
			try {
				String profileThumbnail = upload.createThumbnail(newProfileImg, 40, 40, true);
				web.setCookie("profileThumbnail", profileThumbnail, -1);
			} catch (Exception e) {
				web.redirect(null, e.getLocalizedMessage());
				return null;
			}
		}
		
		// 세션을 갱신한다.
		web.removeSession("loginInfo");
		web.setSession("loginInfo", editInfo);

		/** (11) 수정이 완료되었으므로 다시 수정페이지로 이동 */
		web.redirect(web.getRootPath() + "/member/edit.do", 
				"회원정보가 수정되었습니다.");

		
		// INSERT,UPDATE,DELETE 처리를 수행하는 action 페이지들은 자체적으로 View를
		// 갖지 않고 결과를 확인할 수 있는 다른 페이지로 강제 이동시켜야 한다. 
		// 그러므로 View의 경로를 리턴하지 않는다.(중복실행 방지)
		return null;
	}

}
