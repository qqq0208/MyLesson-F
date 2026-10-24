package com.mdkj.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(name = "购物车分页DTO")
@Data
public class CartPageDTO extends PageDTO {

    @Schema(description = "用户ID")
    private Long fkUserId;
    @Schema(description = "用户账号，冗余字段")
    private String username;
    @Schema(description = "课程标题，冗余字段")
    private String courseTitle;
}