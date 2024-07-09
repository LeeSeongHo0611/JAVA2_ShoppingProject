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
        System.out.println("@@@@@@@@@@@22");
        NoticeBoard noticeBoard = noticeBoardRepository.findById(noticeBdId).orElseThrow(EntityNotFoundException::new);
        System.out.println("###############3");
        return NoticeBoardDto.of(noticeBoard);
    }

    public Long updateNoticeBd(NoticeBoardDto noticeBoardDto) throws Exception{
        System.out.println("수정 3 "+noticeBoardDto.getId());
        NoticeBoard noticeBoard = noticeBoardRepository.findById(noticeBoardDto.getId()).
                orElseThrow(EntityNotFoundException::new);
        System.out.println("수정 4 "+noticeBoardDto.getId()   );
        noticeBoard.updateNoticeBd(noticeBoardDto);
        System.out.println("수정 5 ");

        return noticeBoard.getId();
    }

    @Transactional(readOnly = true)
    public Page<NoticeBoard> getAdminNoticeBdPage(NoticeBoardSearchDto noticeBoardSearchDto, Pageable pageable){
        return noticeBoardRepository.getAdminNoticePage(noticeBoardSearchDto,pageable);
    }
}

