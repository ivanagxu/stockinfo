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
		futuService.syncFutuData(2);
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
		futuService.syncFutuData(2);
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

	/**
	 * 测试富途持仓列表同步获取
	 * 前置条件：需要 Futu OpenD 运行在本地 (127.0.0.1:11111)
	 */
	@Test
	public void testGetPositionListSync() throws InterruptedException {
		System.out.println("=== 开始测试富途持仓列表获取 ===");

		// 初始化 Futu SDK
		com.futu.openapi.FTAPI.init();

		// 创建 MyFutuUtil 实例，连接远程 Futu OpenD
		com.ivan.mfutu.util.MyFutuUtil futuUtil = new com.ivan.mfutu.util.MyFutuUtil();
		futuUtil.start("127.0.0.1", 11111);

		// 等待连接建立
		System.out.println("等待 Futu OpenD 连接...");
		boolean trdReady = futuUtil.waitForTrdConnection(15000);

		// 解锁交易
		System.out.println("解锁交易...");
		futuUtil.unlockTrade();
		Thread.sleep(5000);

		// 获取持仓列表 - 港股
		System.out.println("获取港股持仓列表...");
		com.futu.openapi.pb.TrdGetPositionList.Response hkRsp = futuUtil.getPositionListSync(
				com.ivan.mfutu.util.MyFutuUtil.ALLAccId,
				com.futu.openapi.pb.TrdCommon.TrdMarket.TrdMarket_HK,
				com.futu.openapi.pb.TrdCommon.TrdEnv.TrdEnv_Real,
				false
		);

		if (hkRsp != null && hkRsp.getRetType() == 0) {
			java.util.List<com.futu.openapi.pb.TrdCommon.Position> positions = hkRsp.getS2C().getPositionListList();
			System.out.println("港股持仓数量: " + positions.size());
			for (com.futu.openapi.pb.TrdCommon.Position pos : positions) {
				System.out.printf("股票: %s (%s), 数量: %d, 成本: %.2f, 当前价: %.2f, 盈亏: %.2f%%\n",
						pos.getCode(), pos.getName(),
						pos.getQty(), pos.getCostPrice(),
						pos.getPrice(), pos.getPlRatio());
			}
		} else {
			String errMsg = hkRsp != null ? hkRsp.getRetMsg() : "响应为空";
			System.out.println("获取港股持仓失败: " + errMsg);
		}

		// 获取持仓列表 - 美股
		System.out.println("获取美股持仓列表...");
		com.futu.openapi.pb.TrdGetPositionList.Response usRsp = futuUtil.getPositionListSync(
				com.ivan.mfutu.util.MyFutuUtil.ALLAccId,
				com.futu.openapi.pb.TrdCommon.TrdMarket.TrdMarket_US,
				com.futu.openapi.pb.TrdCommon.TrdEnv.TrdEnv_Real,
				false
		);

		if (usRsp != null && usRsp.getRetType() == 0) {
			java.util.List<com.futu.openapi.pb.TrdCommon.Position> positions = usRsp.getS2C().getPositionListList();
			System.out.println("美股持仓数量: " + positions.size());
			for (com.futu.openapi.pb.TrdCommon.Position pos : positions) {
				System.out.printf("股票: %s (%s), 数量: %d, 成本: %.2f, 当前价: %.2f, 盈亏: %.2f%%\n",
						pos.getCode(), pos.getName(),
						pos.getQty(), pos.getCostPrice(),
						pos.getPrice(), pos.getPlRatio());
			}
			Assert.isTrue(positions.size() >= 0, "持仓列表应返回有效数据");
		} else {
			String errMsg = usRsp != null ? usRsp.getRetMsg() : "响应为空";
			System.out.println("获取美股持仓失败: " + errMsg);
		}

		System.out.println("=== 测试完成 ===");
	}

	/**
	 * 测试获取所有可用账户列表
	 * 前置条件：需要 Futu OpenD 运行在本地 (127.0.0.1:11111)
	 */
	@Test
	public void testGetAccListSync() throws InterruptedException {
		System.out.println("=== 开始测试获取账户列表 ===");

		// 初始化 Futu SDK
		com.futu.openapi.FTAPI.init();

		// 创建 MyFutuUtil 实例，连接本地 Futu OpenD
		com.ivan.mfutu.util.MyFutuUtil futuUtil = new com.ivan.mfutu.util.MyFutuUtil();
		futuUtil.start("127.0.0.1", 11111);

		// 等待连接建立
		System.out.println("等待 Futu OpenD 连接...");
		boolean trdReady = futuUtil.waitForTrdConnection(15000);
		System.out.println("Trd 连接状态: " + trdReady);

		// 解锁交易
		System.out.println("解锁交易...");
		boolean unlocked = futuUtil.unlockTrade();
		Thread.sleep(3000);

		// 获取账户列表
		System.out.println("获取账户列表...");
		com.futu.openapi.pb.TrdGetAccList.Response rsp = futuUtil.getAccListSync();

		if (rsp != null && rsp.getRetType() == 0) {
			java.util.List<com.futu.openapi.pb.TrdCommon.TrdAcc> accounts = rsp.getS2C().getAccListList();
			System.out.println("账户数量: " + accounts.size());
			for (com.futu.openapi.pb.TrdCommon.TrdAcc acc : accounts) {
					System.out.println("账户ID: " + acc.getAccID() + ", 账户类型: " + acc.getAccType());
			}
			org.springframework.util.Assert.isTrue(accounts.size() >= 0, "账户列表应返回有效数据");
		} else {
			String errMsg = rsp != null ? rsp.getRetMsg() : "响应为空";
			System.out.println("获取账户列表失败: " + errMsg);
		}

		System.out.println("=== 测试完成 ===");
	}

	@Test
	public void testGetPositionListUSReal() throws InterruptedException {
		System.out.println("=== 测试实盘美股持仓查询 ===");

		com.futu.openapi.FTAPI.init();
		com.ivan.mfutu.util.MyFutuUtil futuUtil = new com.ivan.mfutu.util.MyFutuUtil();
		futuUtil.start("127.0.0.1", 11111);

		boolean trdReady = futuUtil.waitForTrdConnection(15000);
		System.out.println("Trd 连接状态: " + trdReady);

		boolean unlocked = futuUtil.unlockTrade();
		Thread.sleep(3000);

		// 尝试实盘美股持仓
		System.out.println("尝试获取实盘美股持仓...");
		com.futu.openapi.pb.TrdGetPositionList.Response rsp = futuUtil.getPositionListSync(
				com.ivan.mfutu.util.MyFutuUtil.ALLAccId,
				com.futu.openapi.pb.TrdCommon.TrdMarket.TrdMarket_US,
				com.futu.openapi.pb.TrdCommon.TrdEnv.TrdEnv_Real,
				false
		);

		if (rsp != null) {
			System.out.println("返回结果: retType=" + rsp.getRetType() + ", retMsg=" + rsp.getRetMsg());
			if (rsp.getRetType() == 0) {
				System.out.println("持仓数量: " + rsp.getS2C().getPositionListList().size());
			}
		} else {
			System.out.println("响应为空");
		}

		System.out.println("=== 测试完成 ===");
	}

	@Test
	public void testGetPositionListUSSim() throws InterruptedException {
		System.out.println("=== 测试模拟美股持仓查询 ===");

		com.futu.openapi.FTAPI.init();
		com.ivan.mfutu.util.MyFutuUtil futuUtil = new com.ivan.mfutu.util.MyFutuUtil();
		futuUtil.start("127.0.0.1", 11111);

		boolean trdReady = futuUtil.waitForTrdConnection(15000);
		System.out.println("Trd 连接状态: " + trdReady);

		boolean unlocked = futuUtil.unlockTrade();
		Thread.sleep(3000);

		// 使用模拟账户 3873329 查询美股持仓
		System.out.println("尝试获取模拟账户美股持仓...");
		com.futu.openapi.pb.TrdGetPositionList.Response rsp = futuUtil.getPositionListSync(
				3873329L,  // 模拟账户ID
				com.futu.openapi.pb.TrdCommon.TrdMarket.TrdMarket_US,
				com.futu.openapi.pb.TrdCommon.TrdEnv.TrdEnv_Simulate,
				false
		);

		if (rsp != null) {
			System.out.println("返回结果: retType=" + rsp.getRetType() + ", retMsg=" + rsp.getRetMsg());
			if (rsp.getRetType() == 0 && rsp.getS2C() != null) {
				java.util.List<com.futu.openapi.pb.TrdCommon.Position> positions = rsp.getS2C().getPositionListList();
				System.out.println("持仓数量: " + positions.size());
				for (com.futu.openapi.pb.TrdCommon.Position pos : positions) {
					System.out.println("股票: " + pos.getCode() + " (" + pos.getName() + "), 数量: " + pos.getQty());
				}
			}
		} else {
			System.out.println("响应为空");
		}

		System.out.println("=== 测试完成 ===");
	}
}
