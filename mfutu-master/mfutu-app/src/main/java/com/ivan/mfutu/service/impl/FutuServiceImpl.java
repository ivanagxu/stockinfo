package com.ivan.mfutu.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;

import com.futu.openapi.FTAPI;
import com.ivan.mfutu.entity.Category;
import com.ivan.mfutu.entity.FutuData;
import com.ivan.mfutu.mapper.CategoryMapper;
import com.ivan.mfutu.mapper.FutuDataMapper;
import com.ivan.mfutu.mapper.SubBasicQotMapper;
import com.ivan.mfutu.entity.SubBasicQot;
import com.ivan.mfutu.service.FutuService;
import com.ivan.mfutu.util.MyFutuUtil;

@Service
public class FutuServiceImpl implements FutuService, InitializingBean{

	@Autowired 
	FutuDataMapper futuDataMapper;
	
	@Autowired
	CategoryMapper categoryMapper;

	@Autowired
	SubBasicQotMapper subBasicQotMapper;
	
	private static MyFutuUtil futuUtil = null;
	
	public FutuServiceImpl() {
		if(futuUtil == null) {
			futuUtil = new MyFutuUtil();
			futuUtil.setFutuService(this);
			FTAPI.init(); // 初始化环境，程序启动时调用1次
			futuUtil.start();
			futuUtil.unlockTrade();
		}
	}
	
	@Override
	public void syncFutuData(int market) {
		//Database update will process in asynchronize mode
		if(FutuService.MARKET_US == market)
		{
			futuUtil.syncUSFutuData();
		}
		else {
			System.out.println("Unknow market code, please check FutuService.MARKET_XX options");
		}
	}
	
	@Override
	public void syncFutuCategory() {
		List<Category> catetories = futuDataMapper.getDistinctCategory();
		importCategory(catetories);
	}
	
	@Override
	public List<String> listPlDates() {
		return futuDataMapper.getPlDateList();
	}
	
	@Override
	public List<FutuData> listFutuDataByDate(String date) {
		return futuDataMapper.getByPlDate(date);
	}
	
	@Override
	public List<Category> listCategory() {
		return categoryMapper.getAll();
	}

	@Override
	public void importCategory(List<Category> categories) {
		for(Category category: categories) {
			if(category != null) {
				if(!StringUtils.hasLength(category.getCode()))
					continue;
				
				if(categoryMapper.getByCode(category.getCode()) == null) {
					System.out.println("Importing category, code=" + category.getCode());
					categoryMapper.insert(category);
				}
				else {
					if(StringUtils.hasLength(category.getCurrency()) && StringUtils.hasLength(category.getIndustry()) && StringUtils.hasLength(category.getLevel()) && StringUtils.hasLength(category.getMarket())) {
						System.out.println("Updating category, code=" + category.getCode());
						categoryMapper.update(category);
					}
				}
			}
		}
	}

	@Override
	public void importFutuData(List<FutuData> futuDatas, String plDate) {
		if(!StringUtils.hasLength(plDate)) {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
			plDate = sdf.format(today);
		}
		futuDataMapper.deleteByDate(plDate);
		for(FutuData data: futuDatas) {
			data.setPlDate(plDate);
			futuDataMapper.insert(data);
			System.out.println("Imported " + data);
		}
	}

	@Override
	public void syncKline(int market, String code, String begin, String end) {
		futuUtil.getKlineHistoryByDay(market, code, begin, end);
	}

	@Override
	public void getBasicQot(int market, String code) {
		futuUtil.getBasicQot(market, code);
	}

	@Override
	public List<SubBasicQot> listSubBasicQotAll() {
		return subBasicQotMapper.listAll();
	}

	@Override
	public void upsertSubBasicQot(SubBasicQot data) {
		if(subBasicQotMapper == null)
			return;
		
		if (data == null || data.getCode() == null) {
			return;
		}
		SubBasicQot exist = subBasicQotMapper.get(data.getCode());
		if (exist == null) {
			subBasicQotMapper.insert(data);
			System.out.println("Inserted SubBasicQot: " + data);
		} else {
			subBasicQotMapper.update(data);
			System.out.println("Updated SubBasicQot: " + data);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//初始化订阅全量股票基本行情（top N）
		futuUtil.initSubscribe();
	}

	/**
	 * 定时任务：定时全量查询订阅的股票基本行情并更新数据库。
	 * 可通过配置项调整间隔：`futu.subscribed.update.fixedDelay`（毫秒），
	 * 和初始延迟：`futu.subscribed.update.initialDelay`（毫秒）。
	 */
	@Scheduled(fixedDelayString = "${futu.subscribed.update.fixedDelay:60000}", initialDelayString = "${futu.subscribed.update.initialDelay:5000}")
	public void scheduledUpdateSubscribedBasicQot() {
		List<SubBasicQot> subs = listSubBasicQotAll();
		if (subs == null || subs.isEmpty()) {
			return;
		}
		for (SubBasicQot sb : subs) {
			try {
				if (sb != null && sb.getCode() != null) {
					futuUtil.getBasicQot(sb.getMarket(), sb.getCode());
					// 小延迟，避免短时间内大量请求压垮本地api
					Thread.sleep(50);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
