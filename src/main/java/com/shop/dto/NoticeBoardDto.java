package com.shop.dto;

import com.shop.entity.NoticeBoard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NoticeBoardDto {
    private Long id;

    private String title;

    private String writer;
    private String content;
    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    public NoticeBoardDto(String title, String writer, String content, LocalDateTime regTime, LocalDateTime updateTime) {
        this.title = title;
        this.writer = writer;
        this.content = content;
        this.regTime = regTime;
        this.updateTime = updateTime;
    }

    //ModelMapper
    private static ModelMapper modelMapper = new ModelMapper();


    public NoticeBoard createNoticeBd() {
        //NoticeBoardDto -> NoticeBoard 연결
        return modelMapper.map(this, NoticeBoard.class);
    }

    public static NoticeBoardDto of(NoticeBoard noticeBoard){
        //NoticeBoard -> NoticeBoardDto 연결
        return modelMapper.map(noticeBoard, NoticeBoardDto.class);
    }

}
