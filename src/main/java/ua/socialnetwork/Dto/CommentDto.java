package ua.socialnetwork.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto{

    private int post_id;

    private String text;

    private Boolean reaction;

    private int comment_id;
}
