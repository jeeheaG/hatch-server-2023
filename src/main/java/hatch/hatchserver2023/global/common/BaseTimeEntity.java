package hatch.hatchserver2023.global.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Getter //mapper 사용을 위해 필요
@MappedSuperclass // 이 클래스를 상속한 엔티티들이 이 클래스의 필드들도 자신의 컬럼으로 인식하도록 함
@EntityListeners(AuditingEntityListener.class) //Auditing 기능
public abstract class BaseTimeEntity {

    //TODO : NOT NULL 설정?
    private ZonedDateTime createdTime;

    private ZonedDateTime modifiedTime;

    @PrePersist
    public void prePersist() {
        this.createdTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        this.modifiedTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

}
