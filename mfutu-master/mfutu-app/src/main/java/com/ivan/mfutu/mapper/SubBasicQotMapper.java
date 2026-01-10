package com.ivan.mfutu.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ivan.mfutu.entity.SubBasicQot;

@Repository
public interface SubBasicQotMapper {
    SubBasicQot get(String code);
    List<SubBasicQot> listAll();
    void insert(SubBasicQot data);
    int update(SubBasicQot data);
    void delete(String code);
    void deleteAll();
}
