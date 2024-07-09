package com.shop.repository;

import com.shop.dto.NoticeBoardDto;
import com.shop.dto.NoticeBoardSearchDto;
import com.shop.entity.NoticeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeBoardRepositoryCustom {
    Page<NoticeBoard> getAdminNoticePage(NoticeBoardSearchDto noticeBoardSearchDto, Pageable pageable);

}
