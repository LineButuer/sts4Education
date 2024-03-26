package com.icia.board.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.icia.board.dto.BoardDto;
import com.icia.board.dto.BoardFileDto;
import com.icia.board.dto.SearchDto;
import com.icia.board.service.BoardService;

import jakarta.servlet.http.HttpSession;


@Controller
@Slf4j
public class BoardController {
	@Autowired
	private BoardService boardService;
	@GetMapping("boardList")
	public String boradList(SearchDto searchDto, HttpSession session,  Model model) {
		log.info("boardList()");
		String view = boardService.getBoardList(searchDto, session, model);
		return view;
	}
	@GetMapping("writeForm")
	public String wirteForm() {
		log.info("writeForm()");
		return "writeForm";
	}
	@PostMapping("writeProc")
	public String writeProc(@RequestPart("files")/* input에서 name이 files*/ List<MultipartFile> files, BoardDto boardDto, HttpSession session
							, RedirectAttributes rttr) {
		String view = boardService.boardWrite(files, boardDto, session, rttr);
		
		return view;
	}
	@GetMapping("boardDetail")
	public String boardDetail(@RequestParam("b_num") int b_num, Model model) {
		log.info("boardDetail()");
		String view = boardService.getBoard(b_num, model);
		return view;
	}
	
	@GetMapping("download")
	public ResponseEntity<Resource> fileDownload(BoardFileDto boardFileDto, HttpSession session) throws IOException {
		log.info("fileDownload()");
		ResponseEntity<Resource> resp = boardService.fileDownload(boardFileDto, session);
		return resp;
	}
	
}
