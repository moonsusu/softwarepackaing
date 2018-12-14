package study.jsp.mysite.controller;

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
import study.jsp.helper.WebHelper;
import study.jsp.mysite.dao.MyBatisConnectionFactory;
import study.jsp.mysite.model.BbsDocument;
import study.jsp.mysite.service.BbsDocumentService;
import study.jsp.mysite.service.impl.BbsDocumentServiceImpl;

@WebServlet("/index.do")
public class Index extends BaseController {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3506399630369217895L;
	/** (1) 사용하고자 하는 Helper 객체 선언 */
	// --> import org.apache.logging.log4j.Logger;
	Logger logger;
	// --> import org.apache.ibatis.session.SqlSession;
	SqlSession sqlSession;
	// --> import study.jsp.helper.WebHelper;
	WebHelper web;
	// --> import study.jsp.mysite.service.BbsDocumentService;
	BbsDocumentService bbsDocumentService;

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
		bbsDocumentService = new BbsDocumentServiceImpl(sqlSession, logger);

		/** (3) 각 게시판 종류별로 최근 게시물을 조회한다. */
		List<BbsDocument> galleryList = null;	// 갤러리 최신 게시물
		List<BbsDocument> noticeList = null;	// 공지사항 최신 게시물
		List<BbsDocument> freeList = null;		// 자유게시판 최신 게시물
		List<BbsDocument> qnaList = null;		// 질문답변 최신 게시물
		
		try {
			galleryList = this.getDocumentList("gallery", 3);
			noticeList = this.getDocumentList("notice", 5);
			freeList = this.getDocumentList("free", 5);
			qnaList = this.getDocumentList("qna", 5);
		} catch (Exception e) {
			web.redirect(null, e.getLocalizedMessage());
		} finally {
			sqlSession.close();
		}

		/** (4) 최신 글 목록을 View에 전달 */
		request.setAttribute("galleryList", galleryList);
		request.setAttribute("noticeList", noticeList);
		request.setAttribute("freeList", freeList);
		request.setAttribute("qnaList", qnaList);

		// "/WEB-INF/views/index.jsp"파일을 View로 사용한다.
		return "index";
	}

	/**
	 * 특정 카테고리에 대한 상위 n개의 게시물 가져오기
	 * @param category - 가져올 카테고리
	 * @param listCount - 가져올 게시물 수
	 * @return
	 * @throws Exception
	 */
	private List<BbsDocument> getDocumentList(String category, int listCount) 
			throws Exception {
		List<BbsDocument> list = null;

		// 조회할 조건 생성하기
		// --> 지정된 카테고리의 0번째부터 listCount개 만큼 조회
		BbsDocument document = new BbsDocument();
		document.setCategory(category);
		document.setLimitStart(0);
		document.setListCount(listCount);
		document.setGallery(category.equals("gallery"));

		list = bbsDocumentService.selectDocumentList(document);

		return list;
	}
}
