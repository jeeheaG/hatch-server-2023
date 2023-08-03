package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.api.StageSocketResponser;
import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.dto.AISimilarityRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageResponseDto;
import hatch.hatchserver2023.domain.stage.repository.MusicRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StageService {

    private final StageRoutineService stageRoutineService;

    @Autowired
    MusicRepository musicRepository;

    private final RedisDao redisDao;

    private final StageSocketResponser stageSocketResponser;

    // 환경변수 주입
    @Value("${AI_SERVER_URL}")
    private String AI_SERVER_URL;

    public StageService(RedisDao redisDao, StageRoutineService stageRoutineService, StageSocketResponser stageSocketResponser) {
        this.redisDao = redisDao;
        this.stageRoutineService = stageRoutineService;
        this.stageSocketResponser = stageSocketResponser;
    }

    // TODO : 플레이 마무리 로직에서 참고하여 사용할 것
    /**
     * 스테이지에서 댄스 정확도 계산
     *
     * @input music_title, sequence
     * @return similarity
     */
    // TODO: 어떤 사용자인지도 필요한가?
//    public Float calculateSimilarity(String musicTitle, Float[][] sequence) {
    public Float calculateSimilarity(String musicTitle, List<StageRequestDto.Skeleton> skeletons) {
        // 곡명으로 음악 찾기
        Music music = musicRepository.findByTitle(musicTitle);

        // ai 서버로 요청할 안무 두 개
        AISimilarityRequestDto requestDto = AISimilarityRequestDto.builder()
                .seq1(music.getAnswer())
                .seq2(StageRequestDto.Skeleton.toFloatArrays(skeletons))
                .build();

        // ai 서버로 계산 요청
        WebClient client = WebClient.create(AI_SERVER_URL);

        ResponseEntity<StageResponseDto.GetSimilarity> response = client.post()
                .uri("/api/similarity")
                .bodyValue(requestDto)
                .retrieve()
                .toEntity(StageResponseDto.GetSimilarity.class)
                .block();

         return response.getBody().getSimilarity();
    }

    /**
     * 스테이지 입장 로직
     * @param user
     * @return
     */
    public int addStageUser(User user) {
        log.info("[SERVICE] addAndGetStageUserCount");

//        if(isExistUser(user)){
//            throw new StageException(StageStatusCode.ALREADY_ENTERED_USER);
//        }

        int increasedCount = addStageData(user);

        stageSocketResponser.userCount(increasedCount);

        runStageRoutine(increasedCount);

        return increasedCount;
    }

    private int addStageData(User user) {
        // 인원수 increase
        int increasedCount = stageRoutineService.getStageUserCount() + 1;
        redisDao.setValues(StageRoutineService.STAGE_ENTER_USER_COUNT, String.valueOf(increasedCount));
        log.info("[SERVICE] increasedCount : {}", increasedCount);

        // redis 입장 목록에 입장한 사용자 PK 추가
        redisDao.setValuesSet(StageRoutineService.STAGE_ENTER_USER_LIST, user.getId().toString());
        return increasedCount;
    }

    private void runStageRoutine(int increasedCount) {
        String stageStatus = getStageStatus();
        switch (stageStatus) {
            case StageRoutineService.STAGE_STATUS_WAIT:
                log.info("stage status : wait ");
                if (increasedCount >= 3) {
                    log.info("stage user count >= 3");
                    stageRoutineService.startRoutine();
                }
                break;

            case StageRoutineService.STAGE_STATUS_CATCH:
                log.info("stage status : catch ");
                break;

            case StageRoutineService.STAGE_STATUS_MVP:
                log.info("stage status : mvp ");
                break;
        }
    }

    private boolean isExistUser(User user) {
        return redisDao.isSetDataExist(StageRoutineService.STAGE_ENTER_USER_LIST, user.getId().toString());
    }

    /**
     * 스테이지 상태 확인 로직
     * @return
     */
    public String getStageStatus() {
        log.info("[SERVICE] getStageStatus");
        String stageStatus = stageRoutineService.getStageStatus();
        return (stageStatus==null) ? StageRoutineService.STAGE_STATUS_WAIT : stageStatus;
        //TODO : 상태에 따라 진행중인 정보 같이 보내줘야 함
    }

    /**
     * 스테이지 참여자 고유값 목록 확인 로직
     * @return
     */
    public List<Long> getStageEnterUserIds() {
        log.info("[SERVICE] getStageEnterUserProfiles");
        Set<String> userIdSet = redisDao.getValuesSet(StageRoutineService.STAGE_ENTER_USER_LIST);
        List<String> userIds = new ArrayList<>(userIdSet);
        return userIds.stream().map(Long::parseLong).collect(Collectors.toList());
    }


    /**
     * 스테이지 캐치 등록 로직
     * @param user
     */
    public void registerCatch(User user) {
        log.info("[SERVICE] registerCatch");

        if(!stageRoutineService.getStageStatus().equals(StageRoutineService.STAGE_STATUS_CATCH)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_CATCH);
        }

        final long now = System.currentTimeMillis();
        log.info("registerCatch user id : {}, nickname : {}, time : {}", user.getId(), user.getNickname(), now);
        redisDao.setValuesZSet(StageRoutineService.STAGE_CATCH_USER_LIST, user.getId().toString(), (int) now);
//        redisDao.setValuesHash(StageRoutineService.STAGE_CATCH_USER_LIST, (int) now, user);
    }

    /**
     * 스테이지 퇴장 로직 (임시)
     * @param user
     */
    public void deleteStageUser(User user) {
        log.info("[SERVICE] deleteStageUser");

        int count = stageRoutineService.getStageUserCount();
        log.info("[SERVICE] count : {}", count);

        if(count == 0){
            throw new StageException(StageStatusCode.STAGE_ALREADY_EMPTY);
        }

        int decreasedCount = deleteStageData(user, count);

        tempCheckStageEmpty();

        stageSocketResponser.userCount(decreasedCount);
    }

    private int deleteStageData(User user, int count) {
        // 인원수 decrease
        int decreasedCount = count -1;
        redisDao.setValues(StageRoutineService.STAGE_ENTER_USER_COUNT, String.valueOf(decreasedCount));
        log.info("[SERVICE] decreasedCount : {}", decreasedCount);

        // redis 입장 목록에서 입장한 사용자 PK 제거
        redisDao.removeValuesSet(StageRoutineService.STAGE_ENTER_USER_LIST, user.getId().toString());
        return decreasedCount;
    }

    /**
     * 개발편의상 개발한 임시 로직 (한 사용자 여러번 count+1 가능한 환경 유지)
     * 스테이지 exit 시 사용자 목록이 비어있으면 사용자수 0으로 변경
     */
    private void tempCheckStageEmpty() {
        Long size = redisDao.getSetSize(StageRoutineService.STAGE_ENTER_USER_LIST);
        log.info("tempCheckStageEmpty STAGE_ENTER_USER_LIST set size : {}", size);
        if(size==0) {
            log.info("tempCheckStageEmpty set STAGE_ENTER_USER_COUNT = 0");
            redisDao.setValues(StageRoutineService.STAGE_ENTER_USER_COUNT, "0");
        }
    }

}
