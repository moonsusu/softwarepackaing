package study.jsp.mysite.controller.bbs;

/**
 * 모든 게시판에서 공통적으로 수행되어야 하는 처리 로직에 대한 기능 구현
 * - Speaker클래스의 생성자를 외부에서 호출할 수 없도록 private으로 선언하고, getInstance() 메서드를 사용하여 스피커 인스턴스가 이미 생성되어 있는지를 검사하고 생성되지 않은 상호아이라면 생성자를 호출해 인스턴스를 생성하고, 이미 생성되어 있다면 정적 변수 speaker 변수를 참조하는 인스턴스를 반환합니다.
 */
public class BBSCommon {
	// ----------- 싱글톤 객체 생성 시작 -----------
	private static BBSCommon current = null;

	public static BBSCommon getInstance() {
		if (current == null) {
			current = new BBSCommon();
		}
		return current;
	}

	public static void freeInstance() {
		current = null;
	}

	private BBSCommon() {
		super();
	}
	// ----------- 싱글톤 객체 생성 끝 -----------

	/**
	 * 카테고리 값을 추출하여 허용된 게시판인지 판별한다.
	 * 허용된 게시판일 경우 게시판의 이름을 리턴. 그렇지 않을 경우 예외를 발생시킨다.
	 * @param category
	 * @return 게시판 이름
	 * @throws Exception
	 */
	public String getBbsName(String category) throws Exception {
		// 리턴할 게시판 이름
		String bbsName = null;

		// 카테고리값이 존재할 경우 게시판이름 판별
		if (category != null) {
			if (category.equals("notice")) {
				bbsName = "공지사항";
			} else if (category.equals("free")) {
				bbsName = "자유게시판";
			} else if (category.equals("gallery")) {
				bbsName = "갤러리";
			} else if (category.equals("qna")) {
				bbsName = "질문/답변";
			}
		}

		// 생성된 게시판 이름이 없다면 예외를 발생시킨다.
		if (bbsName == null) {
			throw new Exception("존재하지 않는 게시판 입니다.");
		}

		return bbsName;
	}
}
