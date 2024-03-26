package com.icia.board.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.icia.board.dto.BoardDto;
import com.icia.board.dto.BoardFileDto;
import com.icia.board.dto.SearchDto;

@Mapper
public interface BoardDao {
	// 게시글 목록 가져오는 메소드
	List<BoardDto> selectBoardList(SearchDto searchDto);
	
	// 전체 게시물 개수 구하는 메소드
	int selectBoardCnt(SearchDto searchDto);

	// 게시물 저장 메소드
	void insertBoard(BoardDto boardDto);
	
	// 파일 정보 저장 메소드
	void insertFile(BoardFileDto boardFileDto);
}
