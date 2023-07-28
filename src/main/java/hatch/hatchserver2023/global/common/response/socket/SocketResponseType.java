package hatch.hatchserver2023.global.common.response.socket;

public enum SocketResponseType {
    TALK_MESSAGE( "TALK_MESSAGE", "라이브톡 메세지 전송"),
    TALK_REACTION( "TALK_REACTION", "라이브톡 반응 전송"),

    USER_COUNT( "USER_COUNT", "스테이지 인원수"),

    CATCH_START( "CATCH_START", "스테이지 캐치 시작"),
    CATCH_END( "CATCH_END", "스테이지 캐치 끝"),
    PLAY_START( "PLAY_START", "스테이지 플레이 시작"),
//    PLAY_END( "PLAY_END", "스테이지 플레이 끝"),
    MVP_START( "MVP_START", "스테이지 MVP세리머니 시작"),
//    MVP_END( "MVP_END", "스테이지 MVP세리머니 끝"),

    STAGE_ROUTINE_STOP( "STAGE_ROUTINE_STOP", "스테이지 진행 멈춤"),
    ;

    private final String type;
    private final String message;

    SocketResponseType(String type, String message){
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
