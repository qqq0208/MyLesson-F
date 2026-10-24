package com.mdkj.dao;

import com.mdkj.es.CourseDoc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CourseRepository extends ElasticsearchRepository<CourseDoc,Long> {

    //搜索es的课程
    Page<CourseDoc> searchByTitleOrAuthorOrderByIdx(String title, String author, Pageable pageable);
}
