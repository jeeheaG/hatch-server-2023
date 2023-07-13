package hatch.hatchserver2023.domain.user.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

public class UserResponseDto {

    @ToString
    @Getter
    @Builder
    public static class KakaoLogin { //응답dto 에도 생성자?
        private UUID uuid;
        private String nickname;
        private String email;

        public static KakaoLogin toDto(User user) {
            return KakaoLogin.builder()
                    .uuid(user.getUuid())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .build();
        }
    }


    @ToString
    @Getter
    @Builder
    // 영상 커뮤니티에서 사용자를 화면에 보이기 위해 필요한 정보
    public static class CommunityUserInfo {
        private UUID uuid;
        private String nickname;
        private String email;
        private String profileImg;

        public static CommunityUserInfo toDto(User user){
            return CommunityUserInfo.builder()
                    .uuid(user.getUuid())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .profileImg(user.getProfileImg())
                    .build();
        }

    }
}
