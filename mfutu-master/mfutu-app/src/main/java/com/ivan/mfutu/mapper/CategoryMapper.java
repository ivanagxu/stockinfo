package com.ivan.mfutu.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ivan.mfutu.entity.Category;

@Repository
public interface CategoryMapper {
	public List<Category> getAll();
	public Category getByCode(String code);
	public void update(Category category);
	public void insert(Category category);
}
