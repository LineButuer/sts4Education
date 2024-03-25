package com.icia.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.icia.board.dao.BoardDao;
import com.icia.board.dao.MemberDao;
import com.icia.board.dto.BoardDto;
import com.icia.board.dto.SearchDto;
import com.icia.board.util.PagingUtil;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BoardService {
	@Autowired
	private BoardDao boardDao;
	@Autowired
	private MemberDao memberDao; // 회원 point 정보 변경에 사용
	
	// transaction 관련
	@Autowired
	private PlatformTransactionManager manager;
	@Autowired
	private TransactionDefinition definition;
	
	private int lcnt =10; // 한 화면(페이지)에 보여질 게시글 개수
	
	public String getBoardList(SearchDto searchDto, HttpSession session, Model model) {
		log.info("getBoardList()");
		String view = "boardList";
		// DB에서 게시글 목록 가져오기
		int num = searchDto.getPageNum();
		if(searchDto.getListCnt()==0) {
			searchDto.setListCnt(lcnt); // 모곩 개수 값 설정 (초기 10개)
		}
		searchDto.setPageNum((num-1)*searchDto.getListCnt());
		List<BoardDto> boardList = boardDao.selectBoardList(searchDto);
		model.addAttribute("boardList", boardList);
		
		// 페이징 처리
		searchDto.setPageNum(num);
		String pageHtml = getPaging(searchDto);
		model.addAttribute("pageHtml", pageHtml);
		
		// 페이지 관련 내용 세션에 저장
		if(searchDto.getColname() !=null) {
			session.setAttribute("searchDto", searchDto);
			
		}else {
			session.removeAttribute("searchDto"); // 검색을 안한 목록을 위해 삭제
		}
		// 별개로 페이지 번호도 저장
		session.setAttribute("pageNum", num);
		
		
		return view;
	}

	private String getPaging(SearchDto searchDto) {
		log.info("getPaging()");
		String pageHtml = null;
		
		// 전체 게시글 개수
		int maxNum = boardDao.selectBoardCnt(searchDto);
		
		int pageCnt=3; // 한 페이지당 보여질 번호 개수
		String listName ="boardList?"; //게시판 성격에 따라 달리 이름을 지어주면됨.
		if(searchDto.getColname()!=null) {
			// 검색 기능을 사용한 경우
			listName += "colname="+searchDto.getColname()+"&keyword="+searchDto.getKeyword()+"&";
			
			//<a href='/boardList?colname=b_title&keyword=3&pageNum=...'>
			
		}
		PagingUtil paging = new PagingUtil(maxNum, searchDto.getPageNum(), searchDto.getListCnt(), pageCnt, listName);
		pageHtml=paging.makePaging();
			
		return pageHtml;
	}
	
	// 게시글, 파일 저장 및 회원 정보(point)
	public String boardWrite(List<MultipartFile> files, BoardDto boardDto, HttpSession session, RedirectAttributes rttr) {
		log.info("boardWrite()");
		
		//트랜젝션 상태 처리 객체
		TransactionStatus status = manager.getTransaction(definition);
		
		String view = null;
		String msg = null;
		
		try {
			// 게시글 저장
			boardDao.insertBoard(boardDto);
			log.info("b_num:{}", boardDto.getB_num());
			// 파일 저장
			
			// 작성자의 point 수정
			
			// 정상적으로 처리되면 commit을 수행됨
			manager.commit(status);
			view="redirect:boardList?pageNum=1";
			msg="작성 성공";
		} catch (Exception e) {
			e.printStackTrace();
			// 예외사항이 나오면 rollback을 수행
			manager.rollback(status);
			view="redirect:writeForm";
			msg="작성 실패";
		}
		
		rttr.addFlashAttribute("msg", msg);
		return view;
	}
	
}
