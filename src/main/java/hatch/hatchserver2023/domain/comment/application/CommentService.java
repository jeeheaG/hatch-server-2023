package hatch.hatchserver2023.domain.comment.application;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.comment.domain.Comment;
import hatch.hatchserver2023.domain.video.VideoCacheUtil;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.comment.repository.CommentRepository;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.common.response.exception.VideoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final VideoCacheUtil videoCacheUtil;

    public CommentService(CommentRepository commentRepository, VideoRepository videoRepository, VideoCacheUtil videoCacheUtil){
        this.commentRepository = commentRepository;
        this.videoRepository = videoRepository;
        this.videoCacheUtil = videoCacheUtil;
    }


    /**
     * 댓글 등록
     *
     * @param content
     * @param videoId
     * @param user
     * @return comment
     */
    public Comment createComment(String content, UUID videoId, User user){

        Video video = getVideo(videoId);

        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .video(video)
                .build();

        commentRepository.save(comment);

        // redis 에 댓글 수 저장 (증가)
        videoCacheUtil.increaseCommentCount(video);

        return comment;
    }


    /**
     * 댓글 삭제
     *
     * @param commentId
     * @param user
     */
    public void deleteComment(UUID commentId, User user){

        Comment comment = commentRepository.findByUuid(commentId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.COMMENT_NOT_FOUND));

        //자신이 작성한 댓글이 아닌 다른 댓글을 삭제하려고 하면 에러 발생
        if(!comment.getUser().getUuid().equals(user.getUuid())){
            throw new VideoException(VideoStatusCode.NOT_YOUR_COMMENT);
        }

        commentRepository.delete(comment);

        // redis 에 댓글 수 저장 (감소)
        videoCacheUtil.decreaseCommentCount(comment.getVideo());
    }


    /**
     * 비디오의 댓글 목록 조회
     *
     * @param videoId
     * @return commentList
     */
    public List<Comment> getCommentList(UUID videoId) {

        Video video = getVideo(videoId);

        List<Comment> commentList = commentRepository.findAllByVideo(video);

        return commentList;
    }


    // videoUuid로 Video 객체를 DB에서 찾아오는 함수
    private Video getVideo(UUID videoId) {
        return videoRepository.findByUuid(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));
    }
}
