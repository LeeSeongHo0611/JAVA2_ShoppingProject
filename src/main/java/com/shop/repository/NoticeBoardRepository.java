package com.shop.repository;

import com.shop.entity.NoticeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface NoticeBoardRepository extends JpaRepository<NoticeBoard, Long>,
        QuerydslPredicateExecutor<NoticeBoard>, NoticeBoardRepositoryCustom {

    // select * from NoticeBd where title = ?(String title)
    List<NoticeBoard> findByTitle(String title); // 제목
}
