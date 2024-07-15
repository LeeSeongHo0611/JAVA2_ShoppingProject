package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.dto.NoticeBoardDto;
import com.shop.dto.NoticeBoardSearchDto;
import com.shop.entity.NoticeBoard;
import com.shop.entity.QNoticeBoard;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public class NoticeBoardRepositoryCustomImpl implements NoticeBoardRepositoryCustom {
    private JPAQueryFactory queryFactory;

    public NoticeBoardRepositoryCustomImpl (EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }
    private BooleanExpression regDtsAfter(String searchDateType){
        LocalDateTime dateTime = LocalDateTime.now(); //
        if(StringUtils.equals("all",searchDateType) || searchDateType == null){
            return null;
        }else if(StringUtils.equals("1d",searchDateType)){
            dateTime = dateTime.minusDays(1);
        }else if(StringUtils.equals("1w",searchDateType)){
            dateTime = dateTime.minusWeeks(1);
        }else if(StringUtils.equals("1m",searchDateType)){
            dateTime = dateTime.minusMonths(1);
        }else if(StringUtils.equals("6m",searchDateType)){
            dateTime = dateTime.minusMonths(6);
        }
        return QNoticeBoard.noticeBoard.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery){
        if (StringUtils.equals("title",searchBy)){
            return QNoticeBoard.noticeBoard.title.like("%"+searchQuery+"%");
        }
        else if (StringUtils.equals("createdBy",searchBy)){
            return QNoticeBoard.noticeBoard.createdBy.like("%"+searchQuery+"%");
        }
        return null;
    }
    @Override
    public Page<NoticeBoard> getAdminNoticePage(NoticeBoardSearchDto noticeBoardSearchDto, Pageable pageable) {
        QueryResults<NoticeBoard> results = queryFactory.selectFrom(QNoticeBoard.noticeBoard).
                where(regDtsAfter(noticeBoardSearchDto.getSearchDateType()),
                        searchByLike(noticeBoardSearchDto.getSearchBy(),noticeBoardSearchDto.getSearchQuery()))
                .orderBy(QNoticeBoard.noticeBoard.id.desc())
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetchResults();
        List<NoticeBoard> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable,total);
    }
}
