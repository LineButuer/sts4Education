package com.icia.board.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
	
}
