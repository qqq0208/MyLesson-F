package com.mdkj.service;

import com.mdkj.dto.EpisodeExcelDTO;
import com.mdkj.dto.EpisodeInsertDTO;
import com.mdkj.dto.EpisodePageDTO;
import com.mdkj.dto.EpisodeUpdateDTO;
import com.mdkj.vo.EpisodeSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.service.IService;
import com.mdkj.entity.Episode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 集次表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface EpisodeService extends IService<Episode> {
    //添加 - 单条
    boolean insert(EpisodeInsertDTO dto);

    //查询 - 单条
    Episode select(Long id);

    //查询 - 简单列表
    List<EpisodeSimpleListVO> simpleList();

    //查询 - 条件分页
    PageVO<Episode> page(EpisodePageDTO dto);

    //修改 - 单条
    boolean update(EpisodeUpdateDTO dto);

    //删除
    boolean delete(Long id);
    boolean deleteBatch(List<Long> ids);

    //上传 - 视频封面
    String uploadVideoCover(MultipartFile newFile, Long id);

    //上传 - 集次视频
    String uploadVideo(MultipartFile newFile, Long id);

    //查询 - Excel数据
    List<EpisodeExcelDTO> getExcelData();

}
