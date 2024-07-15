package com.shop.entity;

import com.shop.dto.NoticeBoardDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice_bd")
@Getter
@Setter
@ToString
public class NoticeBoard extends BaseEntity {
    // 기본키 컬럼명
    @Id
    @Column(name = "notice_bd_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // 게시글 번호
    private String title; // 제목
    private String content; // 내용
    @ColumnDefault("0")
    @Column(nullable = false)
    private int view;

    public void updateNoticeBd(NoticeBoardDto noticeBoardDto){
        this.title = noticeBoardDto.getTitle();
        this.content = noticeBoardDto.getContent();
    }

}
