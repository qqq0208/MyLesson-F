package com.mdkj.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(name = "订单明细分页DTO")
@Data
public class OrderDetailPageDTO extends PageDTO {

	@Schema(description = "订单ID")
	@NotNull(message = "订单ID不能为空")
	private Long orderId;

	@Schema(description = "课程标题")
	private String courseTitle;
}