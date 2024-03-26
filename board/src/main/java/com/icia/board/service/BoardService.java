package com.icia.board.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.icia.board.dto.BoardFileDto;
import com.icia.board.dto.MemberDto;
import com.icia.board.dto.ReplyDto;
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
			if(!files.get(0).isEmpty()) { // 업로드할 파일이 있다면 업로드를 해라
				fileUpload(files, session, boardDto.getB_num());
			}
			
			
			// 작성자의 point 수정
			MemberDto memberDto = (MemberDto) session.getAttribute("member"); // 여기서 member는 DTO가 아니라 session임 memberService에 가면 있음.
			int point = memberDto.getM_point() + 10;
			if(point >100) {
				point = 100;
			}
			
			memberDto.setM_point(point);
			memberDao.updateMemberPoint(memberDto);
			
			// 세션에 새 정보를 저장
			memberDto = memberDao.selectMember(memberDto.getM_id());
			session.setAttribute("memberDto", memberDto);
			
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

	private void fileUpload(List<MultipartFile> files, HttpSession session, int b_num) throws Exception {
		
		// 파일 저장 실패 시 데이터베이스 롤백 작업이 이루어지도록 예외를 throws 할 것. 
		log.info("fileUpload()");
		
		// 파일 저장 위치 처리(session에서 저장 경로를 구함)
		String realPath = session.getServletContext().getRealPath("/");
		log.info("realPath : {}", realPath);
		
		realPath += "upload/"; // 파일 업로드용 폴더
		
		File folder = new File(realPath);
		if(folder.isDirectory() == false) {
			folder.mkdir();// 폴더 생성 메소드
		}
	
		for(MultipartFile mf : files) {
			// 파일명 추출
			String oriname = mf.getOriginalFilename();
			
			BoardFileDto bfd = new BoardFileDto();
			bfd.setBf_oriname(oriname);
			bfd.setBf_bnum(b_num);
			String sysname = System.currentTimeMillis()+oriname.substring(oriname.lastIndexOf("."));
			
			// 확장자 : 파일을 구분하기 위한 식별 체계. (예. image.jpg)
			bfd.setBf_sysname(sysname);
			
			//파일 저장
			File file = new File(realPath + sysname);
			mf.transferTo(file);
			
			// 파일 정보 저장
			boardDao.insertFile(bfd);
		}
	}

	public String getBoard(int b_num, Model model) {
		log.info("getBoard()");
		
		// 게시글 번호(b_num)로 게시물 가져오기		
		BoardDto boardDto = boardDao.selectBoard(b_num);
		model.addAttribute("board", boardDto);
		
		//파일 목록 가져오기
		List<BoardFileDto> bfList = boardDao.selectFileList(b_num);
		model.addAttribute("bfList", bfList);
		
		
		//댓글 목록가져오기
		List<ReplyDto> rList = boardDao.selectReplyList(b_num);
		model.addAttribute("rList", rList);
	
		return "boardDetail";
	}

	public ReplyDto replyInsert(ReplyDto reply) {
		log.info("replyInsert()");
		
		TransactionStatus status = manager.getTransaction(definition);
		
		try {
			boardDao.insertReply(reply);
			reply = boardDao.selectReply(reply.getR_num());
			manager.commit(status);
		} catch (Exception e) {
			e.printStackTrace();
			manager.rollback(status);
			reply=null;
		} 
			
		
		
		return reply;
	}

	public ResponseEntity<Resource> fileDownload(BoardFileDto boardFileDto, HttpSession session) throws IOException {
		log.info("fileDownload()");
		String realPath = session.getServletContext().getRealPath("/");
		realPath += "upload/"+boardFileDto.getBf_sysname();
				
		// 실제 하드디스크에 저장된파일과 연동하는 객체를 생성.
		InputStreamResource fResource = new InputStreamResource(new FileInputStream(realPath));
		
		// 파일명이 한글일 경우 인코딩 처리가 필요함(UTF-8로 인코딩 해야됨)
		String fileName = URLEncoder.encode(boardFileDto.getBf_oriname(), "UTF-8");
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM) //octet은 8을 의미함 
				.cacheControl(CacheControl.noCache())
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName)
				.body(fResource); 
	}
	
}// class end
