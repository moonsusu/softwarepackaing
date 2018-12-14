package study.jsp.mysite.controller.bbs;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import study.jsp.helper.BaseController;
import study.jsp.helper.RegexHelper;
import study.jsp.helper.UploadHelper;
import study.jsp.helper.WebHelper;
import study.jsp.mysite.dao.MyBatisConnectionFactory;
import study.jsp.mysite.model.BbsDocument;
import study.jsp.mysite.model.BbsFile;
import study.jsp.mysite.service.BbsDocumentService;
import study.jsp.mysite.service.BbsFileService;
import study.jsp.mysite.service.impl.BbsDocumentServiceImpl;
import study.jsp.mysite.service.impl.BbsFileServiceImpl;

@WebServlet("/bbs/document_edit.do")
public class DocumentEdit extends BaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6526965398920503122L;
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

		/** (3) 게시판 카테고리 값을 받아서 View에 전달 */
		String category = web.getString("category");
		request.setAttribute("category", category);

		/** (4) 존재하는 게시판인지 판별하기 */
		try {
			String bbsName = bbs.getBbsName(category);
			request.setAttribute("bbsName", bbsName);
		} catch (Exception e) {
			sqlSession.close();
			web.redirect(null, e.getLocalizedMessage());
			return null;
		}

		/** (5) 글 번호 파라미터 받기 */
		int documentId = web.getInt("document_id");
		if (documentId == 0) {
			sqlSession.close();
			web.redirect(null, "글 번호가 지정되지 않았습니다.");
			return null;
		}

		// 파라미터를 Beans로 묶기
		BbsDocument document = new BbsDocument();
		document.setId(documentId);
		document.setCategory(category);

		BbsFile file = new BbsFile();
		file.setBbsDocumentId(documentId);

		/** (6) 게시물 일련번호를 사용한 데이터 조회 */
		// 지금 읽고 있는 게시물이 저장될 객체
		BbsDocument readDocument = null;
		// 첨부파일 정보가 저장될 객체
		List<BbsFile> fileList = null;

		try {
			readDocument = bbsDocumentService.selectDocument(document);
			fileList = bbsFileService.selectFileList(file);
		} catch (Exception e) {
			web.redirect(null, e.getLocalizedMessage());
			return null;
		} finally {
			sqlSession.close();
		}

		/** (7) 읽은 데이터를 View에게 전달한다. */
		request.setAttribute("readDocument", readDocument);
		request.setAttribute("fileList", fileList);

		return "bbs/document_edit";
	}

}
