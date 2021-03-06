package com.sbs.untact.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sbs.untact.dao.ArticleDao;
import com.sbs.untact.dto.Article;
import com.sbs.untact.dto.Board;
import com.sbs.untact.dto.ResultData;

@Service
public class ArticleService {
	@Autowired
	private ArticleDao articleDao;

	public ResultData modifyArticle(int id, String title, String body) {
		Article article = getArticleById(id);

		if ( isEmpty(article)) {
			return new ResultData("F-1", "존재하지 않는 게시물 번호입니다.", "id", id);
		}

		articleDao.modifyArticle(id, title, body);

		return new ResultData("S-1", "게시물이 수정되었습니다.", "id", id);
	}


	public ResultData deleteArticleById(int id) {
		Article article = getArticleById(id);

		if ( isEmpty(article)) {
			return new ResultData("F-1", "존재하지 않는 게시물 번호입니다.", "id", id);
		}
		
		articleDao.deleteArticleById(id);

		return new ResultData("S-1", id + "번 게시물이 삭제되었습니다.", "id", id,"boardId",article.getBoardId());
	}

	public ResultData writeArticle(int boardId,int memberId, String title, String body) {
		 // 가짜 데이터
		articleDao.writeArticle(boardId, memberId, title, body);
		int id = articleDao.getArticldLastInsertId();

		return new ResultData("S-1", "게시물이 작성되었습니다.", "id", id);
	}

	public Article getArticleById(int id) {
		return articleDao.getArticleById(id);
	}
	
	private boolean isEmpty(Article article) {
		if(article == null) {
			return true;
		}
		else if(article.isDelStatus()) {
			return true;
		}
		return false;
	}


	public Board getBoardById(int boardId) {
		
		return articleDao.getBoardById(boardId);
	}


	public int getArticleTotalCount(int boardId,String searchKeywordType, String searchKeyword) {
		return articleDao.getArticleTotalCount(boardId,searchKeywordType, searchKeyword);
	}


	public List<Article> getForPrintArticles(int boardId,String searchKeywordType, String searchKeyword, int itemsInAPage, int page) {
		int limitFrom = (page-1)*itemsInAPage;
		int limitTake = itemsInAPage;
		
		return articleDao.getForPrintArticles(boardId,searchKeywordType,searchKeyword, limitFrom,limitTake);
	}


	public Article getForPrintArticleById(int id) {
		return articleDao.getForPrintArticleById(id);
	}
}