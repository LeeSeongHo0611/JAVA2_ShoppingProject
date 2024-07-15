package com.shop.service;

import com.shop.dto.NoticeBoardDto;
import com.shop.dto.NoticeBoardSearchDto;
import com.shop.entity.NoticeBoard;
import com.shop.repository.NoticeBoardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional
@RequiredArgsConstructor
public class NoticeBoardService {
    private final NoticeBoardRepository noticeBoardRepository;

    public Long saveNoticeBd(NoticeBoardDto noticeBoardDto)throws Exception {
        NoticeBoard noticeBoard = noticeBoardDto.createNoticeBd();
        noticeBoardRepository.save(noticeBoard);

        return noticeBoard.getId();
    }

    @Transactional(readOnly = true)
    public NoticeBoardDto getNoticeBdDtl(Long noticeBdId) {
        NoticeBoard noticeBoard = noticeBoardRepository.findById(noticeBdId).orElseThrow(EntityNotFoundException::new);
        return NoticeBoardDto.of(noticeBoard);
    }

    public Long updateNoticeBd(NoticeBoardDto noticeBoardDto) throws Exception{

        NoticeBoard noticeBoard = noticeBoardRepository.findById(noticeBoardDto.getId()).
                orElseThrow(EntityNotFoundException::new);
        noticeBoard.updateNoticeBd(noticeBoardDto);
        return noticeBoard.getId();
    }

    @Transactional(readOnly = true)
    public Page<NoticeBoard> getAdminNoticeBdPage(NoticeBoardSearchDto noticeBoardSearchDto, Pageable pageable){
        return noticeBoardRepository.getAdminNoticePage(noticeBoardSearchDto,pageable);
    }

    @Transactional
    public void incrementViews(Long noticeBdId) {
        NoticeBoard noticeBoard = noticeBoardRepository.findById(noticeBdId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + noticeBdId));

        // 조회수 증가
        noticeBoard.setView(noticeBoard.getView() + 1);
        noticeBoardRepository.save(noticeBoard); // 변경사항 저장
    }
}

