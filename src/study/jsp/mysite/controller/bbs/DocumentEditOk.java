package study.jsp.mysite.controller.bbs;

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
import study.jsp.mysite.model.BbsDocument;
import study.jsp.mysite.model.BbsFile;
import study.jsp.mysite.model.Member;
import study.jsp.mysite.service.BbsDocumentService;
import study.jsp.mysite.service.BbsFileService;
import study.jsp.mysite.service.impl.BbsDocumentServiceImpl;
import study.jsp.mysite.service.impl.BbsFileServiceImpl;

@WebServlet("/bbs/document_edit_ok.do")
public class DocumentEditOk extends BaseController {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7863498410156757740L;
	/** (1) 사용하고자 하는 Helper 객체 선언 */
	// --> import org.apache.logging.log4j.Logger;
	Logger logger;
	// --> import org.apache.ibatis.session.SqlSession;
	SqlSession sqlSession;
	// --> import study.jsp.helper.WebHelper;
	WebHelper web;
	BBSCommon bbs;
	// --> import study.jsp.helper.UploadHelper;
	UploadHelper upload;
	// --> import study.jsp.helper.RegexHelper;
	RegexHelper regex;
	// --> import study.jsp.mysite.service.BbsDocumentService;
	BbsDocumentService bbsDocumentService;
	// --> import study.jsp.mysite.service.BbsFileService;
	BbsFileService bbsFileService;

	@Override
	public String doRun(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/** (2) 사용하고자 하는 Helper+Service 객체 생성 */
		// --> import org.apache.logging.log4j.LogManager;
		logger = LogManager.getFormatterLogger(request.getRequestURI());
		// --> import study.jsp.mysite.service.impl.MemberServiceImpl;
		sqlSession = MyBatisConnectionFactory.getSqlSession();
		web = WebHelper.getInstance(request, response);
		bbs = BBSCommon.getInstance();
		upload = UploadHelper.getInstance();
		regex = RegexHelper.getInstance();
		// --> import study.jsp.mysite.service.impl.BbsDocumentServiceImpl;
		bbsDocumentService = new BbsDocumentServiceImpl(sqlSession, logger);
		// --> import study.jsp.mysite.service.impl.BbsFileServiceImpl;
		bbsFileService = new BbsFileServiceImpl(sqlSession, logger);

		/** (3) 파일이 포함된 POST 파라미터 받기 */
		try {
			upload.multipartRequest(request);
		} catch (Exception e) {
			sqlSession.close();
			web.redirect(null, "multipart 데이터가 아닙니다.");
			return null;
		}

		/** (4) UploadHelper에서 텍스트 형식의 값을 추출 */
		Map<String, String> paramMap = upload.getParamMap();

		// 글 번호 가져오기 --> Map에서 가져오는 값은 모두 String이므로 형변환 필요
		int documentId = 0;
		try {
			documentId = Integer.parseInt(paramMap.get("document_id"));
		} catch (NumberFormatException e) {
			sqlSession.close();
			web.redirect(null, "글 번호가 올바르지 않습니다.");
			return null;
		}

		String category = paramMap.get("category");
		String writerName = paramMap.get("writer_name");
		String writerPw = paramMap.get("writer_pw");
		String email = paramMap.get("email");
		String subject = paramMap.get("subject");
		String content = paramMap.get("content");
		// 작성자 아이피 주소 가져오기
		String ipAddress = web.getClientIP();
		// 회원 일련번호 --> 비 로그인인 경우 0
		int memberId = 0;

		/** (5) 게시판 카테고리 값을 받아서 View에 전달 */
		// 파일이 첨부된 경우 WebHelper를 사용할 수 없다.
		// String category = web.getString("category");
		request.setAttribute("category", category);

		/** (6) 존재하는 게시판인지 판별하기 */
		try {
			String bbsName = bbs.getBbsName(category);
			request.setAttribute("bbsName", bbsName);
		} catch (Exception e) {
			sqlSession.close();
			web.redirect(null, e.getLocalizedMessage());
			return null;
		}

		/** (7) 로그인 한 경우 자신의 글이라면 입력하지 않은 정보를 세션 데이터로 대체한다. */
		// 소유권 검사 정보
		boolean myDocument = false;
		
		Member loginInfo = (Member) web.getSession("loginInfo");
		if (loginInfo != null) {
			try {
				// 소유권 판정을 위하여 사용하는 임시 객체
				BbsDocument temp = new BbsDocument();
				temp.setCategory(category);
				temp.setId(documentId);
				temp.setMemberId(loginInfo.getId());

				if (bbsDocumentService.selectDocumentCountByMemberId(temp) > 0) {
					// 소유권을 의미하는 변수 변경
					myDocument = true;
					// 입력되지 않은 정보들 갱신
					writerName = loginInfo.getName();
					email = loginInfo.getEmail();
					writerPw = loginInfo.getUserPw();
					memberId = loginInfo.getId();
				}
			} catch (Exception e) {
				sqlSession.close();
				web.redirect(null, e.getLocalizedMessage());
				return null;
			}
		}

		// 전달된 파라미터는 로그로 확인한다.
		logger.debug("documentId=" + documentId);
		logger.debug("category=" + category);
		logger.debug("writer_name=" + writerName);
		logger.debug("writer_pw=" + writerPw);
		logger.debug("email=" + email);
		logger.debug("subject=" + subject);
		logger.debug("content=" + content);
		logger.debug("ipAddress=" + ipAddress);
		logger.debug("memberId=" + memberId);

		/** (8) 입력 받은 파라미터에 대한 유효성 검사 */
		// 이름 + 비밀번호
		if (!regex.isValue(writerName)) {
			sqlSession.close();
			web.redirect(null, "작성자 이름를 입력하세요.");
			return null;
		}

		if (!regex.isValue(writerPw)) {
			sqlSession.close();
			web.redirect(null, "비밀번호를 입력하세요.");
			return null;
		}

		// 이메일
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

		// 제목 및 내용 검사
		if (!regex.isValue(subject)) {
			sqlSession.close();
			web.redirect(null, "글 제목을 입력하세요.");
			return null;
		}

		if (!regex.isValue(content)) {
			sqlSession.close();
			web.redirect(null, "내용을 입력하세요.");
			return null;
		}

		/** (9) 입력 받은 파라미터를 Beans로 묶기 */
		BbsDocument document = new BbsDocument();
		// UPDATE문의 WHERE절에서 사용해야 하므로 글 번호 추가
		// --> 글 번호는 숫자로 변환해서 처리해야 한다.
		document.setId(documentId);
		document.setCategory(category);
		document.setWriterName(writerName);
		document.setWriterPw(writerPw);
		document.setEmail(email);
		document.setSubject(subject);
		document.setContent(content);
		document.setIpAddress(ipAddress);
		document.setMemberId(memberId);
		logger.debug(document.toString());

		/** (10) 게시물 변경을 위한 Service 기능을 호출 */
		try {
			// 자신의 글이 아니라면 비밀번호 검사를 먼저 수행한다.
			if (!myDocument) {
				bbsDocumentService.selectDocumentCountByPw(document);
			}
			bbsDocumentService.updateDocument(document);
		} catch (Exception e) {
			sqlSession.close();
			web.redirect(null, e.getLocalizedMessage());
			return null;
		}

		/** (11) 삭제를 선택한 첨부파일에 대한 처리 */
		// 삭제할 파일 목록에 대한 체크결과 --> 체크박스의 선택값을 paramMap에서 추출
		String delFile = paramMap.get("del_file");

		if (delFile != null) {
			// 콤마 단위로 잘라서 배열로 변환
			String[] delFileList = delFile.split(",");
	
			for (int i = 0; i < delFileList.length; i++) {
				try {
					// 체크박스에 의해서 전달된 id값으로 개별 파일에 대한 Beans 생성
					BbsFile file = new BbsFile();
					file.setId(Integer.parseInt(delFileList[i]));
	
					// 개별 파일에 대한 정보를 조회하여 실제 파일을 삭제한다.
					BbsFile item = bbsFileService.selectFile(file);
					upload.removeFile(item.getFileDir() + "/" + item.getFileName());
	
					// DB에서 파일정보 삭제처리
					bbsFileService.deleteFile(file);
				} catch (Exception e) {
					sqlSession.close();
					web.redirect(null, e.getLocalizedMessage());
					return null;
				}
			}
		}

		/** (12) 추가적으로 업로드 된 파일 정보 처리 */
		// 업로드 된 파일 목록
		List<FileInfo> fileInfoList = upload.getFileList();

		// 업로드 된 파일의 수 만큼 반복 처리 한다.
		for (int i = 0; i < fileInfoList.size(); i++) {
			// 업로드 된 정보 하나 추출
			// --> 업로드 된 정보를 데이터베이스에 저장하기 위한 형태로 가공해야 한다.
			FileInfo info = fileInfoList.get(i);

			// DB에 저장하기 위한 항목 하나 생성
			BbsFile file = new BbsFile();

			// 데이터 복사
			file.setOriginName(info.getOrginName());
			file.setFileDir(info.getFileDir());
			file.setFileName(info.getFileName());
			file.setContentType(info.getContentType());
			file.setFileSize(info.getFileSize());

			// 어느 게시물에 속한 파일인지 인식해야 하므로 글 번호 추가
			file.setBbsDocumentId(documentId);

			// 복사된 데이터를 DB에 저장
			try {
				bbsFileService.insertFile(file);
			} catch (Exception e) {
				sqlSession.close();
				web.redirect(null, e.getLocalizedMessage());
				return null;
			}
		}
		
		/** (13) 모든 절차가 종료되었으므로 DB접속 해제 후 페이지 이동 */
		sqlSession.close();
		
		String url = "%s/bbs/document_read.do?category=%s&document_id=%d";
		url = String.format(url, web.getRootPath(), category, documentId);
		web.redirect(url, null);

		return null;
	}

}
