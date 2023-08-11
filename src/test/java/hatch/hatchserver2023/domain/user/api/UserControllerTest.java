package hatch.hatchserver2023.domain.user.api;


import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.config.restdocs.RestDocsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class) // Controller 단위 테스트
@MockBean(JpaMetamodelMappingContext.class) // jpaAuditingHandler 에러 해결
@WithMockUser //401 에러 해결
@AutoConfigureRestDocs // rest docs 자동 설정
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("User Controller Unit Test")
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RestDocumentationResultHandler docs;

    @MockBean
    UserUtilService userUtilService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setup(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))  // rest docs 설정 주입
                .alwaysDo(MockMvcResultHandlers.print()) // andDo(print()) 코드 포함
                .alwaysDo(docs) // pretty 패턴과 문서 디렉토리 명 정해준것 적용
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지
                .build();

        user1 = User.builder()
                .id(991L)
                .uuid(UUID.randomUUID())
                .email("user1@gmail.com")
                .nickname("user1")
                .instagramAccount("인스타 계정1")
                .twitterAccount("트위터 계정1")
                .kakaoAccountNumber(1L)
                .introduce("user1 입니다")
                .profileImg("프로필 이미지 경로")
                .build();

        user2 = User.builder()
                .id(992L)
                .uuid(UUID.randomUUID())
                .email("user2@gmail.com")
                .nickname("user2")
                .instagramAccount("인스타 계정2")
                .twitterAccount("트위터 계정2")
                .kakaoAccountNumber(2L)
                .introduce("user2 입니다 :)")
                .profileImg("프로필 이미지 경로 2")
                .build();

        user3 = User.builder()
                .id(993L)
                .uuid(UUID.randomUUID())
                .email("user3@gmail.com")
                .nickname("user3")
                .instagramAccount("인스타 계정3")
                .twitterAccount("트위터 계정3")
                .kakaoAccountNumber(3L)
                .introduce("user3 입니다 XD")
                .profileImg("프로필 이미지 경로 3")
                .build();
    }


    @Test
    @DisplayName("Get Profile")
    void getProfile() throws Exception {
        //given
        boolean isMe = false;
        given(userUtilService.findOneByUuid(user1.getUuid()))
                .willReturn(user1);
        given(userUtilService.countFollower(user1))
                .willReturn(2);
        given(userUtilService.countFollowing(user1))
                .willReturn(1);

        //when
        StatusCode code = UserStatusCode.GET_PROFILE_SUCCESS;

        MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                .get("/api/v1/users/profile/{userId}", user1.getUuid())
                .header("headerXAccessToken", "headerXAccessToken")
                .header("headerXRefreshToken", "headerXRefreshToken");

        //then
        ResultActions resultActions = mockMvc.perform(requestGet);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.userId").value(user1.getUuid().toString()))
                .andExpect(jsonPath("$.data.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("$.data.introduce").value(user1.getIntroduce()))
                .andExpect(jsonPath("$.data.twitterId").value(user1.getTwitterAccount()))
                .andExpect(jsonPath("$.data.isMe").value(isMe))
        ;


        resultActions
                .andDo( //rest docs 문서 작성 시작
                        docs.document(
                                pathParameters(
                                        parameterWithName("userId").description("사용자 UUID")
                                ),
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다. \n\n로그인한 사용자가 자신의 프로필을 조회하는지 여부 isMe를 판단하기 위해 받습니다.").optional(),
                                        headerWithName("headerXRefreshToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.").optional()
                                ),
                                responseFields( // response 필드 정보 입력
                                        beneathPath("data"),
                                        fieldWithPath("userId").type(JsonFieldType.STRING).description("사용자 식별자 UUID"),
                                        fieldWithPath("isMe").type(JsonFieldType.BOOLEAN).description("로그인한 사용자가 자신의 프로필을 확인하는지 여부"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 경로"),
                                        fieldWithPath("introduce").type(JsonFieldType.STRING).description("자기소개"),
                                        fieldWithPath("instagramId").type(JsonFieldType.STRING).description("인스타그램 계정"),
                                        fieldWithPath("twitterId").type(JsonFieldType.STRING).description("트위터 계정"),
                                        fieldWithPath("followingCount").type(JsonFieldType.NUMBER).description("팔로잉 수"),
                                        fieldWithPath("followerCount").type(JsonFieldType.NUMBER).description("팔로워 수"),
                                        fieldWithPath("createdAt").type("DateTime").description("생성 시각"),
                                        fieldWithPath("modifiedAt").type("DateTime").description("수정 시각")
                                )
                        )
                )
        ;
    }


    }
