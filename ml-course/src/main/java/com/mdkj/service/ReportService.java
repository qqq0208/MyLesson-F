package com.mdkj.service;

import com.mdkj.dto.ReportInsertDTO;
import com.mdkj.dto.ReportPageDTO;
import com.mdkj.dto.ReportUpdateDTO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.service.IService;
import com.mdkj.entity.Report;

import java.util.List;

/**
 * 举报表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface ReportService extends IService<Report> {
    boolean insert(ReportInsertDTO dto);
    Report select(Long id);
    PageVO<Report> page(ReportPageDTO dto);
    boolean update(ReportUpdateDTO dto);
    boolean delete(Long id);
    boolean deleteBatch(List<Long> ids);
    //删除 - 用户举报
    boolean deleteByUserId(Long userId);
    //删除 - 批量用户举报
    boolean deleteByUserIds(List<Long> userIds);
}
