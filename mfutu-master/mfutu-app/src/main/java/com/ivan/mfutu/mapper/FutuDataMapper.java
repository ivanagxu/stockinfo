package com.ivan.mfutu.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ivan.mfutu.entity.Category;
import com.ivan.mfutu.entity.FutuData;

@Repository
public interface FutuDataMapper {
	FutuData get(Integer id);
	List<FutuData> getByPlDate(String plDate);
	List<Category> getDistinctCategory();
	void insert(FutuData data);
	void deleteAll();
	void deleteByDate(String plDate);
	List<String> getPlDateList();
}
