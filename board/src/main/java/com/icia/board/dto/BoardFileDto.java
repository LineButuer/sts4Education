package com.icia.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardFileDto {

	private int bf_num;// 보드파일 기본키
	private int bf_bnum;// 게시글 번호
	private String bf_oriname;
	private String bf_sysname;
	
}
