package com.ivan.test.futu;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.futu.openapi.pb.QotCommon;
import com.ivan.mfutu.MFutuApplication;
import com.ivan.mfutu.entity.Category;
import com.ivan.mfutu.entity.FutuData;
import com.ivan.mfutu.mapper.CategoryMapper;
import com.ivan.mfutu.mapper.FutuDataMapper;
import com.ivan.mfutu.service.FutuService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MFutuApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class MyFutuTest {
	@Autowired
	FutuDataMapper futuDataMapper;
	@Autowired
	CategoryMapper categoryMapper;
	@Autowired
	FutuService futuService;

	@Test
	public void md5Test() {
		System.out.println(DigestUtils.md5DigestAsHex("要加密的字符串".getBytes()));
	}

	@Test
	public void testGetFutuData() {
		FutuData fd = futuDataMapper.get(16);
		System.out.println(fd);
	}

	@Test
	public void testSyncFutuData() throws InterruptedException {
		futuService.syncFutuData(FutuService.MARKET_US);
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException exc) {

		}
	}

	@Test
	public void testSyncCategory() {
		futuService.syncFutuCategory();
	}

	@Test
	public void testDateFormat() {
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		String plToday = sdf.format(today);
		System.out.println(plToday);
	}
	
	@Test
	public void testGetKLine() {
		futuService.syncKline(QotCommon.QotMarket.QotMarket_US_Security_VALUE , "ZM", "2020-02-01", "2020-03-03");
		
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException exc) {
			exc.printStackTrace();
		}
	}

	@Test
	public void testSyncAll() {
		futuService.syncFutuData(FutuService.MARKET_US);
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException exc) {

		}
		futuService.syncFutuCategory();
	}

	@Test
	public void testListFutuDataByDate() {
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String plToday = sdf.format(today);
		List<FutuData> data = futuService.listFutuDataByDate(plToday);
		Assert.notNull(data);
		for (FutuData dt : data) {
			System.out.println("" + dt + ", Industry=" + dt.getIndustry() + ", subIndustry=" + dt.getSubIndustry()
					+ ", level=" + dt.getLevel() + ", percentage=" + dt.getPercentage());
		}
	}

	@Test
	public void testGetPlDateList() {
		List<String> dates = futuDataMapper.getPlDateList();
		for (String dt : dates) {
			System.out.println(dt);
		}
	}

	@Test
	public void testListCategory() {
		List<Category> categories = futuService.listCategory();
		for (Category c : categories) {
			System.out.println(c.getCode());
		}
	}

	@Test
	public void testGetCategory() {
		Category category = categoryMapper.getByCode("BABA");
		System.out.println(category.getName() + "," + category.getSubIndustry());
		Assert.isTrue(StringUtils.hasLength(category.getSubIndustry()));
	}
}
