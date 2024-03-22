package com.icia.board.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.icia.board.dto.MemberDto;
import com.icia.board.service.MailService;
import com.icia.board.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@Slf4j
public class BoardRestController {
	@Autowired
	private MemberService mServ;
	
	@Autowired
	private MailService mailService;
	
	@GetMapping("idCheck")
	public String idCheck(@RequestParam("mid") String mid) {
		log.info("idCheck()");
		String res = mServ.idCheck(mid);
		return res;
	}
	
	@PostMapping("mailConfirm")
	public String mailConfirm(MemberDto memberDto, HttpSession sesstion) {
		log.info("mailConfirm()");
		String res = mailService.sendEmail(memberDto, sesstion);
		return res;
	}
	
	@PostMapping("codeAuth")
	public String coeAuth(@RequestParam("v_code") String v_code, HttpSession session) {
		log.info("codeAuth()");
		String res = mailService.codeAuth(v_code, session);
		
		return res;
	}
	
	
}



