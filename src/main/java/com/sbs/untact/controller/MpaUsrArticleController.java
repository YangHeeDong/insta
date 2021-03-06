package com.sbs.untact.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sbs.untact.dto.Article;
import com.sbs.untact.dto.Board;
import com.sbs.untact.dto.Reply;
import com.sbs.untact.dto.ResultData;
import com.sbs.untact.dto.Rq;
import com.sbs.untact.service.ArticleService;
import com.sbs.untact.service.ReplyService;
import com.sbs.untact.util.Util;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MpaUsrArticleController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private ReplyService replyService;

    @RequestMapping("/mpaUsr/article/detail")
    public String showDetail(HttpServletRequest req, int id) {
        Article article = articleService.getForPrintArticleById(id);
        List<Reply> replies = replyService.getForPrintRepliesByRelTypeCodeAndRelId("article", article.getId());
        
        if (article == null) {
            return Util.msgAndBack(req, id + "번 게시물이 존재하지 않습니다.");
        }

        Board board = articleService.getBoardById(article.getBoardId());
        
        
        req.setAttribute("replies", replies);
        req.setAttribute("article", article);
        req.setAttribute("board", board);

        return "mpaUsr/article/detail";
    }

    @RequestMapping("/mpaUsr/article/write")
    public String showWrite(HttpServletRequest req, @RequestParam(defaultValue = "1") int boardId) {
        Board board = articleService.getBoardById(boardId);

        if (board == null) {
            return Util.msgAndBack(req, boardId + "번 게시판이 존재하지 않습니다.");
        }

        req.setAttribute("board", board);

        return "mpaUsr/article/write";
    }

    @RequestMapping("/mpaUsr/article/doWrite")
    public String doWrite(HttpServletRequest req, int boardId, String title, String body) {
        if (Util.isEmpty(title)) {
            return Util.msgAndBack(req, "제목을 입력해주세요.");
        }

        if (Util.isEmpty(body)) {
            return Util.msgAndBack(req, "내용을 입력해주세요.");
        }

        int memberId = ((Rq)req.getAttribute("rq")).getLoginedMemberId();
        ResultData writeArticleRd = articleService.writeArticle(boardId, memberId, title, body);

        if (writeArticleRd.isFail()) {
            return Util.msgAndBack(req, writeArticleRd.getMsg());
        }

        String replaceUri = "detail?id=" + writeArticleRd.getBody().get("id");
        return Util.msgAndReplace(req, writeArticleRd.getMsg(), replaceUri);
    }
    
    @RequestMapping("/mpaUsr/article/showModify")
    public String ShowModify(HttpServletRequest req, Integer id, String redirectUri) {

        if (Util.isEmpty(id)) {
            return Util.msgAndBack(req, "게시물 번호가 입력되지 않았습니다.");
        }
        
        Article article = articleService.getArticleById(id);
        
        if(Util.isEmpty(article)) {
        	return Util.msgAndBack(req, "존재하지 않는 게시물 입니다.");
        }
        
        if(article.getMemberId() != ((Rq)req.getAttribute("rq")).getLoginedMemberId()) {
        	return Util.msgAndBack(req, "권한이 없습니다!");
        }
        
        req.setAttribute("article", article);
        
        return "/mpaUsr/article/showModify";
    }
    

    @RequestMapping("/mpaUsr/article/doModify")
    public String doModify(HttpServletRequest req, Integer id, String title, String body,String redirectUri) {

        if (Util.isEmpty(id)) {
            return Util.msgAndBack(req, "번호를 입력해주세요.");
        }

        if (Util.isEmpty(title)) {
            return Util.msgAndBack(req, "제목을 입력해주세요.");
        }

        if (Util.isEmpty(body)) {
        	return Util.msgAndBack(req, "내용을 입력해주세요.");
        }

        Article article = articleService.getArticleById(id);

        if(Util.isEmpty(article)) {
        	return Util.msgAndBack(req, "존재하지 않는 게시물 입니다.");
        }
        
        if(article.getMemberId() != ((Rq)req.getAttribute("rq")).getLoginedMemberId()) {
        	return Util.msgAndBack(req, "권한이 없습니다!");
        }
        
        ResultData rd =articleService.modifyArticle(id, title, body);
        
        return Util.msgAndReplace(req, rd.getMsg(), redirectUri);
    }

    @RequestMapping("/mpaUsr/article/doDelete")
    public String doDelete(HttpServletRequest req, Integer id) {
        if (Util.isEmpty(id)) {
            return Util.msgAndBack(req, "id를 입력해주세요.");
        }

        ResultData rd = articleService.deleteArticleById(id);

        if (rd.isFail()) {
            return Util.msgAndBack(req, rd.getMsg());
        }

        String redirectUri = "../article/list?boardId=" + rd.getBody().get("boardId");

        return Util.msgAndReplace(req, rd.getMsg(), redirectUri);
    }

    @RequestMapping("/mpaUsr/article/list")
    public String showList(HttpServletRequest req, @RequestParam(defaultValue = "1") int boardId, String searchKeywordType, String searchKeyword,
                           @RequestParam(defaultValue = "1") int page) {
        Board board = articleService.getBoardById(boardId);

        if (Util.isEmpty(searchKeywordType)) {
            searchKeywordType = "titleAndBody";
        }

        if (board == null) {
            return Util.msgAndBack(req, boardId + "번 게시판이 존재하지 않습니다.");
        }

        req.setAttribute("board", board);

        int totalItemsCount = articleService.getArticleTotalCount(boardId, searchKeywordType, searchKeyword);

        if (searchKeyword == null || searchKeyword.trim().length() == 0) {

        }

        req.setAttribute("totalItemsCount", totalItemsCount);

        // 한 페이지에 보여줄 수 있는 게시물 최대 개수
        int itemsCountInAPage = 20;
        // 총 페이지 수
        int totalPage = (int) Math.ceil(totalItemsCount / (double) itemsCountInAPage);

        // 현재 페이지(임시)
        req.setAttribute("page", page);
        req.setAttribute("totalPage", totalPage);

        List<Article> articles = articleService.getForPrintArticles(boardId, searchKeywordType, searchKeyword, itemsCountInAPage, page);

        req.setAttribute("articles", articles);

        return "mpaUsr/article/list";
    }

    @RequestMapping("/mpaUsr/article/getArticle")
    @ResponseBody
    public ResultData getArticle(Integer id) {
        if (Util.isEmpty(id)) {
            return new ResultData("F-1", "번호를 입력해주세요.");
        }

        Article article = articleService.getArticleById(id);

        if (article == null) {
            return new ResultData("F-1", id + "번 글은 존재하지 않습니다.", "id", id);
        }

        return new ResultData("S-1", article.getId() + "번 글 입니다.", "article", article);
    }
}