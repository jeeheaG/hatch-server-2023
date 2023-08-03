package hatch.hatchserver2023.domain.stage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StageRequestDto {

    @ToString
    @Getter
    @Builder
    public static class SendPlaySkeleton {
        @NotNull
        private Integer playerNum;

        @NotNull
        private Skeleton skeleton;
    }

    @ToString
    @Getter
    @Builder
    public static class Skeleton {
        private Double noseX;
        private Double noseY;

        private Double rightShoulderX;
        private Double rightShoulderY;
        private Double rightElbowX;
        private Double rightElbowY;
        private Double rightWristX;
        private Double rightWristY;
        private Double rightHipX;
        private Double rightHipY;
        private Double rightKneeX;
        private Double rightKneeY;
        private Double rightAnkleX;
        private Double rightAnkleY;

        private Double leftShoulderX;
        private Double leftShoulderY;
        private Double leftElbowX;
        private Double leftElbowY;
        private Double leftWristX;
        private Double leftWristY;
        private Double leftHipX;
        private Double leftHipY;
        private Double leftKneeX;
        private Double leftKneeY;
        private Double leftAnkleX;
        private Double leftAnkleY;

        public Float[] toFloatArray() {
            List<Float> values = new ArrayList<>();
            for(Field field : this.getClass().getDeclaredFields()) { //모든 필드명 얻기
                try{
                    values.add(Float.parseFloat(field.get(this).toString())); // 이 객체의 해당 필드값 얻기
                }catch (IllegalAccessException e) {
                    values.add(null); // 값을 못 얻어오면 null 추가
                }
            }

            return values.toArray(new Float[0]);
        }

        public static Float[][] toFloatArrays(List<Skeleton> skeletons) {
            List<Float[]> arrays = new ArrayList<>();
            for(Skeleton skeleton : skeletons) {
                arrays.add(skeleton.toFloatArray());
            }

            return arrays.toArray(new Float[0][0]);
        }
    }

// 플레이 종료 api 가 필요 없게 됨
//    @ToString
//    @Getter
//    @Builder
//    public static class EndPlay {
//        @NotNull
//        private String title;
//
//        @NotNull
//        private List<StageRequestDto.Skeleton> skeletons;
//    }


//
//    @ToString
//    @Getter
//    @Builder
//    public static class Skeleton {
//        private Point nose;
//        private BodyPart right;
//        private BodyPart left;
//    }
//
//    @ToString
//    @Getter
//    @Builder
//    public static class BodyPart {
//        private Point shoulder;
//        private Point elbow;
//        private Point wrist;
//        private Point hip;
//        private Point knee;
//        private Point ankle;
//    }
//
//    @ToString
//    @Getter
//    @Builder
//    public static class Point {
//        private Double x;
//        private Double y;
//    }
}
