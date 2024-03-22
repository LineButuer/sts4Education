package com.icia.board.service;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.icia.board.dao.MemberDao;
import com.icia.board.dto.MemberDto;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailService {
	
	// 메일 전송 객체
	@Autowired
	private JavaMailSender emailSender;
	
	// Thymeleaf 사용을 위한 객체의 의존성 주입
	@Autowired
	private SpringTemplateEngine templateEngine;
	
	@Autowired
	private MemberDao memberDao;
	
	private String authNum; // 인증코드 저장 변수
	
	//인증코드 생성 메소드
	public void createCode() {
		log.info("createCode()");
		Random random = new Random();
		StringBuffer key = new StringBuffer();
		
		for(int i =0; i<8; i++) {
			int index = random.nextInt(3);
			
			switch (index) {
				case 0: 
					key.append((char)(random.nextInt(26)+97));
					break;
				case 1:
					key.append((char)(random.nextInt(26)+65)); // 65소문자 a의 값
					break;
				case 2:
					key.append((char)(random.nextInt(10)+48));//숫자
					break;
			}
		}
		authNum = key.toString();
	}
	
	// 메일 양식 생성 메소드
	public MimeMessage createEmailForm(String email) throws MessagingException, UnsupportedEncodingException {
		log.info("createEmailForm();");
		
		String setFrom = "sorkrntdlek@naver.com";// mail setting에 설정한 메일 주소
		String title = "인증 코드 전송";// 메일 제목
		
		MimeMessage message = emailSender.createMimeMessage();
		message.addRecipients(MimeMessage.RecipientType.TO, email); // 받는 사람
		message.setSubject(title);// 제목 설정
		message.setFrom(setFrom);//보내는 사람
		message.setText(setContext(),"utf-8","html");
		
		
		return message;
	}	
	
	// Thymeleaf를 이용한 context(HTML, 메일 화면)를 설정하는 메소드
	private String setContext() {
		createCode(); // 인증 코드 생성
		Context context = new Context();
		context.setVariable("code", authNum);
		return templateEngine.process("mailForm", context);
	}
	
	// 메일 전송 메소드
	public String sendEmail(MemberDto member, HttpSession session) {
		log.info("sendEmail()");
		
		MimeMessage emailForm = null;
		String res = null;
		String email = null; // DB에서 가져온 메일 주소 저장
		
		try {
			email=memberDao.selectEmail(member.getM_id());
			if(email.equals(member.getM_email())) {
				emailForm = createEmailForm(email);
				
				emailSender.send(emailForm);//메일 전송
				
				res = "ok";
				// 인증 코드 확인을 위해 세션에 저장.
				session.setAttribute("authNum", authNum);
				session.setAttribute("m_id", member.getM_id());
			}
			else {
				res="fail";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			res="fail";
		}
		
		return res;
		
	}

	public String codeAuth(String v_code, HttpSession session) {
		// TODO Auto-generated method stub
		log.info("codeAuth()");
		String authNum = (String) session.getAttribute("authNum");
		String res = null;
		
		if(v_code.equals(authNum)) {
			res = "ok";
			
		}else {
			res="fail";
		}
		
		session.removeAttribute(res);
		
		return res;
	}

}
