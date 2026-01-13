package com.ivan.mfutu.service;

import java.util.List;

import com.ivan.mfutu.entity.Category;
import com.ivan.mfutu.entity.FutuData;
import com.ivan.mfutu.entity.SubBasicQot;

public interface FutuService {
	public static final int MARKET_US = 1;
	public static final int MARKET_HK = 2;
	public static final int MARKET_CN = 3;
	
	List<FutuData> listFutuDataByDate(String date);
	void importFutuData(List<FutuData> futuDatas, String plDate);
	void importCategory(List<Category> categories);
	void syncFutuData(int market);
	void syncFutuCategory();
	List<Category> listCategory();
	List<String> listPlDates();
	void syncKline(int market, String code, String begin, String end);
	void getBasicQot(int market, String code);

	// SubBasicQot operations
	List<SubBasicQot> listSubBasicQotAll();
	List<SubBasicQot> listSubBasicQotByMarket(int market);
	void upsertSubBasicQot(SubBasicQot data);
}
