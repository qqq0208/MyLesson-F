package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.mdkj.component.MyRedis;
import com.mdkj.constant.ML;
import com.mdkj.dto.ArticleInsertDTO;
import com.mdkj.dto.ArticlePageDTO;
import com.mdkj.dto.ArticleUpdateDTO;
import com.mdkj.entity.Notice;
import com.mdkj.exception.ServiceException;
import com.mdkj.result.ResultCode;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.entity.Article;
import com.mdkj.mapper.ArticleMapper;
import com.mdkj.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mdkj.entity.table.ArticleTableDef.ARTICLE;
import static com.mdkj.entity.table.NoticeTableDef.NOTICE;

/**
 * 新闻表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private MyRedis myRedis;

    @Override
    public boolean insert(ArticleInsertDTO dto) {
        String title = dto.getTitle();

        // 标题查重
        // select count(*) from article where title = ?
        if (QueryChain.of(mapper).where(ARTICLE.TITLE.eq(title)).exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }

        // 组装实体类
        Article article = BeanUtil.copyProperties(dto, Article.class);
        article.setCreated(LocalDateTime.now());
        article.setUpdated(LocalDateTime.now());
        // insert into article (idx, title, content, created, updated) values (?, ?, ?, ?, ?)
        if (mapper.insert(article) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        //清除缓存
        myRedis.deleteByPrefix( ML.Redis.TOP_ARTICLE_KEY_PREFIX);
        return true;
    }

    @Override
    public Article select(Long id) {
        // select * from article where id = ?
        Article article = mapper.selectOneWithRelationsById(id);
        if (ObjectUtil.isNull(article)) {
            throw new ServiceException(ResultCode.ARTICLE_NOT_FOUND, id + "号新闻数据不存在");
        }
        return article;
    }

    @Override
    public PageVO<Article> page(ArticlePageDTO dto) {
        QueryChain<Article> queryChain = QueryChain.of(mapper).orderBy(ARTICLE.IDX.asc(), ARTICLE.ID.desc());

        // title 条件
        String title = dto.getTitle();
        if (ObjectUtil.isNotNull(title)) {
            queryChain.where(ARTICLE.TITLE.like(title));
        }

        // DB分页并转为VO
        Page<Article> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<Article> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(ArticleUpdateDTO dto) {
        Long id = dto.getId();
        String title = dto.getTitle();

        // 检查通知是否存在
        this.existsById(id);

        // 标题查重
        // select count(*) from article where title = ? and id <> ?
        if (QueryChain.of(mapper).where(ARTICLE.TITLE.eq(title)).and(ARTICLE.ID.ne(id)).exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }

        // 组装实体类
        Article article = BeanUtil.copyProperties(dto, Article.class);
        article.setUpdated(LocalDateTime.now());

        // update article set title = ?, content = ?, updated = ? where id = ?
        if (!UpdateChain.of(article).where(ARTICLE.ID.eq(id)).update()) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        //清除缓存
        myRedis.deleteByPrefix( ML.Redis.TOP_ARTICLE_KEY_PREFIX);
        return true;
    }

    @Override
    public boolean delete(Long id) {

        // 检查新闻是否存在
        this.existsById(id);

        // delete from article where id = ?
        if (mapper.deleteById(id) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        //清除缓存
        myRedis.deleteByPrefix( ML.Redis.TOP_ARTICLE_KEY_PREFIX);
        return true;
    }

    @Override
    public boolean deleteBatch(List<Long> ids) {

        // 检查新闻是否存在
        // select count(*) from article where id in (?, ?, ?)
        if (QueryChain.of(mapper).where(ARTICLE.ID.in(ids)).count() < ids.size()) {
            throw new ServiceException(ResultCode.ARTICLE_NOT_FOUND, "至少一个新闻数据不存在");
        }

        // delete from article where id in (?)
        if (mapper.deleteBatchByIds(ids) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        //清除缓存
        myRedis.deleteByPrefix( ML.Redis.TOP_ARTICLE_KEY_PREFIX);
        return true;
    }

    @Override
    public List<Article> top(Long n) {
        //定义缓存的Key
        String redisKey = ML.Redis.TOP_ARTICLE_KEY_PREFIX + n;
        //查询缓存 如果存在 直接返回
        if(myRedis.exists(redisKey)){
            return JSONUtil.toList(myRedis.get(redisKey), Article.class);
        }
        //查询DB
        List<Article> articleList = QueryChain.of(mapper)
                .orderBy(ARTICLE.IDX.asc(), ARTICLE.ID.desc())
                .limit(n)
                .list();
        //将结果存入缓存
        myRedis.setEx(redisKey,JSONUtil.toJsonStr(articleList),3, TimeUnit.HOURS);
        //返回数据
        return articleList;
    }

    /**
     * 按主键检查新闻是否存在，如果不存在则直接抛出异常
     *
     * @param id 新闻主键
     */
    private void existsById(Long id) {
        // select count(*) from article where id = ?
        if (!QueryChain.of(mapper).where(ARTICLE.ID.eq(id)).exists()) {
            throw new ServiceException(ResultCode.ARTICLE_NOT_FOUND, id + "号新闻数据不存在");
        }
    }
}
