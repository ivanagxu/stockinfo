package com.ivan.mfutu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.futu.openapi.FTAPI;
import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTAPI_Conn_Qot;
import com.futu.openapi.FTAPI_Conn_Trd;
import com.futu.openapi.FTSPI_Conn;
import com.futu.openapi.FTSPI_Qot;
import com.futu.openapi.FTSPI_Trd;
import com.futu.openapi.ProtoID;
import com.futu.openapi.pb.GetGlobalState;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotCommon.BasicQot;
import com.futu.openapi.pb.QotCommon.KLine;
import com.futu.openapi.pb.QotCommon.Security;
import com.futu.openapi.pb.QotGetBasicQot;
import com.futu.openapi.pb.QotRequestHistoryKL;
import com.futu.openapi.pb.QotSub;
import com.futu.openapi.pb.QotUpdateBasicQot;
import com.futu.openapi.pb.TrdCommon;
import com.futu.openapi.pb.TrdGetAccList;
import com.futu.openapi.pb.TrdGetPositionList;
import com.futu.openapi.pb.TrdUnlockTrade;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.ivan.mfutu.entity.FutuData;
import com.ivan.mfutu.entity.KlineData;
import com.ivan.mfutu.entity.SubBasicQot;
import com.ivan.mfutu.service.FutuService;


class ReqInfo {
    int protoID;
    Object syncEvent;
    GeneratedMessageV3 rsp;

    ReqInfo(int protoID, Object syncEvent) {
        this.protoID = protoID;
        this.syncEvent = syncEvent;
    }
}


public class MyFutuUtil implements FTSPI_Qot, FTSPI_Trd, FTSPI_Conn {
	FutuService futuService = null;
	
	protected Object qotLock = new Object();
    protected Object trdLock = new Object();
    
	FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();
	FTAPI_Conn_Trd trd = new FTAPI_Conn_Trd();
	
	public static final long loginAccId = 21859003L;
	public static final long USAccId = 281756460299160251L;
	public static final long HKAccId = 281756456004192955L;
	public static final long CHAccId = 281756468889094843L;
	public static final long ALLAccId = 281756473184062139L;
	
	public static final String tradePwdMd5 = "8a3dd852636daf04754ac8d07006baec";
	public static final String loginPwdMd5 = "29eb0c5e3fbca6fe836ac64e961ca267";
	
	public static final int SecurityFirm_FutuSecurities_VALUE = 1;
	
	public static final int max_sub_basic_qot_num = 10;
	
	private static boolean tradeUnlocked = false;
	
	List<FutuData> myPositionAsFutuData = new ArrayList<FutuData>();
	
	protected HashMap<Integer, ReqInfo> qotReqInfoMap = new HashMap<>();
    protected HashMap<Integer, ReqInfo> trdReqInfoMap = new HashMap<>();
    protected HashMap<String, Integer> subscribedCodes = new HashMap<>();
	
	static TrdCommon.TrdHeader makeTrdHeader(TrdCommon.TrdEnv trdEnv, long accID, TrdCommon.TrdMarket trdMarket) {
		TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder().setTrdEnv(trdEnv.getNumber()).setAccID(accID)
				.setTrdMarket(trdMarket.getNumber()).build();
		return header;
	}
	
	static QotCommon.Security makeSec(QotCommon.QotMarket market, String code) {
        QotCommon.Security sec = QotCommon.Security.newBuilder().setCode(code)
                .setMarket(market.getNumber())
                .build();
        return sec;
    }

	public MyFutuUtil() {
		qot.setClientInfo("FTAPI4J_Sample", 1); // 设置客户端信息
		qot.setConnSpi(this); // 设置连接回调
		qot.setQotSpi(this); // 设置交易回调

		trd.setClientInfo("FTAPI4J_Trade_Sample", 1); // 设置客户端信息
		trd.setConnSpi(this); // 设置连接回调
		trd.setTrdSpi(this); // 设置交易回调
	}

	public void start(String apiHost, int apiPort) {
		qot.initConnect(apiHost, apiPort, false);
		trd.initConnect(apiHost, apiPort, false);
	}

	public void onInitConnect(FTAPI_Conn client, long errCode, String desc) {
		System.out.printf("Qot onInitConnect: ret=%d desc=%s connID=%d\n", errCode, desc, client.getConnectID());
		if (errCode != 0) // 连接失败
			return;
		
		//初始化订阅
		//initSubscribe();
	}
	
	public void initSubscribe() {
		List<SubBasicQot> existingSubs = futuService.listSubBasicQotAll();
		if (existingSubs != null && !existingSubs.isEmpty()) {
			List<Security> secList = new ArrayList<Security >();
			for (SubBasicQot sb : existingSubs) {
				subscribedCodes.put(sb.getCode(), sb.getMarket());
				secList.add(QotCommon.Security.newBuilder().setCode(sb.getCode())
						.setMarket(sb.getMarket())
						.build());

				if(secList.size() >= max_sub_basic_qot_num)
					break;
			}
			QotSub.C2S c2s = QotSub.C2S.newBuilder().addAllSecurityList(secList)
			.addAllSecurityList(secList)
			.addSubTypeList(QotCommon.SubType.SubType_RT_VALUE)
			.addSubTypeList(QotCommon.SubType.SubType_KL_Day_VALUE)
			.addSubTypeList(QotCommon.SubType.SubType_Basic_VALUE)
			.setIsSubOrUnSub(true)
			.setIsRegOrUnRegPush(true)
			.setIsFirstPush(true)
			.build();
        	QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
        	qot.sub(req);
		}
	}

	// 断线后会调用此回调
	public void onDisconnect(FTAPI_Conn client, long errCode) {
		System.out.printf("Qot onDisConnect: %d\n", errCode);
		qot.close(); // 释放底层资源
	}

	//订阅回调
	public void onReply_Sub(FTAPI_Conn client, int nSerialNo, QotSub.Response rsp) {
		System.out.printf("Reply: Sub: %d  %s\n", nSerialNo, rsp);
	}

	//基本行情推送回调
	public void onPush_UpdateBasicQuote(FTAPI_Conn client, QotUpdateBasicQot.Response rsp) {
		System.out.printf("Push: UpdateBasicQot: %s\n", rsp);
		if (rsp == null || futuService == null) {
			return;
		}
		try {
			QotUpdateBasicQot.S2C s2c = rsp.getS2C();
			if (s2c == null) return;
			List<BasicQot> list = s2c.getBasicQotListList();
			updateQotFromS2c(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onReply_GetGlobalState(FTAPI_Conn client, int nSerialNo, GetGlobalState.Response rsp) {
		System.out.printf("Reply: GetGlobalState: %d  %s\n", nSerialNo, rsp);
	}

	@Override
	public void onReply_GetPositionList(FTAPI_Conn client, int nSerialNo, TrdGetPositionList.Response rsp) {
        if (rsp.getRetType() != 0) {
            System.out.printf("TrdGetPositionList failed: %s\n", rsp.getRetMsg());
        }
        else {
            try {
            	TrdGetPositionList.S2C s2c = rsp.getS2C();
                String json =  JsonFormat.printer().print(rsp);
                System.out.printf("Receive TrdGetPositionList: %s\n", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
		System.out.printf("Reply: TrdGetPositionList: %d\n", nSerialNo);
		/*
		if(rsp.getErrCode() == 0) {
			myPositionAsFutuData.clear();
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
			String plToday = sdf.format(today);
			
			TrdGetPositionList.S2C s2c = rsp.getS2C();
			List<Position> positions = s2c.getPositionListList();
			
			System.out.printf("Reply: TrdGetPositionList，positions num: %d\n", positions.size());
			
			FutuData data = null;
			for(int i = 0; i < positions.size(); i++) {
				data = new FutuData();
				data.setCode(positions.get(i).getCode());
				data.setCurrency("USD");
				data.setId(null);
				data.setIndustry("NA");
				data.setMarket("US");
				data.setMyAvgPrice((float)positions.get(i).getCostPrice());
				data.setName(positions.get(i).getName());
				data.setPercentage(0f);
				data.setPrice((float)positions.get(i).getPrice());
				data.setQuantity((int)positions.get(i).getQty());
				data.setTags("");
				data.setUpdateTime(null);
				data.setValue((float)positions.get(i).getVal());
				data.setPlValue((float)positions.get(i).getPlVal());
				data.setPlRatio((float)positions.get(i).getPlRatio());
				myPositionAsFutuData.add(data);
			}
			System.out.println("Start to import futu data of " + plToday);
			futuService.importFutuData(myPositionAsFutuData, plToday);
		}
		*/
	}
	@Override
    public void onReply_UnlockTrade(FTAPI_Conn client, int nSerialNo, TrdUnlockTrade.Response rsp) {
        System.out.printf("Reply: TrdUnlockTrade: %d  %s\n", nSerialNo, rsp.toString());
        tradeUnlocked = true;
    }
	@Override
    public void onReply_GetAccList(FTAPI_Conn client, int nSerialNo, TrdGetAccList.Response rsp) {
        System.out.printf("Reply: TrdGetAccList: %d  %s\n", nSerialNo, rsp.toString());
    }
	@Override
    public void onReply_RequestHistoryKL(FTAPI_Conn client, int nSerialNo, QotRequestHistoryKL.Response rsp) {
        System.out.printf("Reply: QotRequestHistoryKL: %d  %s\n", nSerialNo, rsp.toString());
        if(rsp.getErrCode() == 0) {
        	QotRequestHistoryKL.S2C s2c = rsp.getS2C();
        	List<KLine> lines = s2c.getKlListList();
        	List<KlineData> myKlines = new ArrayList<KlineData>();
        	KlineData dt;
        	for(KLine line : lines) {
        		dt = new KlineData();
        		dt.setId(null);
        		dt.setChangeRate(line.getChangeRate());
        		dt.setClose(line.getClosePrice());
        		dt.setCode("");
        	}
        }
    }
	
	@Override
    public void onReply_GetBasicQot(FTAPI_Conn client, int nSerialNo, QotGetBasicQot.Response rsp) {
		
		if (rsp == null || futuService == null) {
			return;
		}
		try {
			QotGetBasicQot.S2C s2c = rsp.getS2C();
			if (s2c == null) return;
			List<BasicQot> list = s2c.getBasicQotListList();
			updateQotFromS2c(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.printf("Reply: GetBasicQot: %d\n", nSerialNo);
    }
	
	public void updateQotFromS2c(List<BasicQot>  list) {
		try {
			if (list == null || list.isEmpty()) return;
			for (BasicQot b : list) {
				if (b == null || b.getSecurity() == null) continue;
				SubBasicQot sb = new SubBasicQot();
				sb.setCode(b.getSecurity().getCode());
				// security.market is an int enum value
				sb.setMarket(b.getSecurity().getMarket());
				sb.setName(b.getName());
				// set numeric fields, cast to float
				sb.setCurPrice((float) b.getCurPrice());
				sb.setLastClosePrice((float) b.getLastClosePrice());
				// calculate increase rate = (cur - lastClose) / lastClose
				try {
					double cur = b.getCurPrice();
					double last = b.getLastClosePrice();
					float inc = 0f;
					if (!Double.isNaN(cur) && !Double.isNaN(last) && Math.abs(last) > 1e-9) {
						inc = 100 * (float) ((cur - last) / last);
					}
					sb.setIncreaseRate((float)(Math.round(inc * 100.00) / 100.00));
				} catch (Throwable t) {
					sb.setIncreaseRate(0f);
				}
				// set update time to now (protobuf may include time but use current time to be safe)
				sb.setUpdateTime(new Date());
				futuService.upsertSubBasicQot(sb);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getKlineHistoryByDay(int market, String code, String beginTime, String endTime) {
		QotCommon.Security sec = QotCommon.Security.newBuilder()
                .setMarket(market)
                .setCode(code)
                .build();
		
		QotSub.C2S cc2s = QotSub.C2S.newBuilder()
                .addSecurityList(sec)
                .addSubTypeList(QotCommon.SubType.SubType_Basic_VALUE)
                .setIsSubOrUnSub(true)
                .build();
        QotSub.Request req = QotSub.Request.newBuilder().setC2S(cc2s).build();
        int seqNo = qot.sub(req);
        System.out.printf("Send QotSub: %d\n", seqNo);
		
        QotRequestHistoryKL.C2S c2s = QotRequestHistoryKL.C2S.newBuilder()
                .setRehabType(QotCommon.RehabType.RehabType_Forward_VALUE)
                .setKlType(QotCommon.KLType.KLType_Day_VALUE)
                .setSecurity(sec)
                .setBeginTime(beginTime)
                .setEndTime(endTime)
                .setMaxAckKLNum(2)
            .build();
        QotRequestHistoryKL.Request req2 = QotRequestHistoryKL.Request.newBuilder().setC2S(c2s).build();
        seqNo = qot.requestHistoryKL(req2);
        System.out.printf("Send QotRequestHistoryKL: %d\n", seqNo);
	}
	
	private void getRepoList(long accId, int market) {
		//getBasicQot();
//		try {
//			TrdGetPositionList.Response rsp = getPositionListSync(accId, TrdCommon.TrdMarket.TrdMarket_US,
//					TrdCommon.TrdEnv.TrdEnv_Real,
//					null,
//					null,
//					null,
//					false);
//			String json =  JsonFormat.printer().print(rsp);
//            System.out.printf("Receive TrdGetPositionList: %s\n", json);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (InvalidProtocolBufferException e) {
//			e.printStackTrace();
//		}
		
		
		TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                .setAccID(accId)
                .setTrdEnv(TrdCommon.TrdEnv.TrdEnv_Real_VALUE)
                .setTrdMarket(market)
                .build();
        TrdGetPositionList.C2S c2s = TrdGetPositionList.C2S.newBuilder()
                .setHeader(header)
            .build();
        TrdGetPositionList.Request req = TrdGetPositionList.Request.newBuilder().setC2S(c2s).build();
        int seqNo = trd.getPositionList(req);
        System.out.printf("Send TrdGetPositionList: %d\n", seqNo);
		
		
		//TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder().setAccID(accId)
		//		.setTrdEnv(TrdCommon.TrdEnv.TrdEnv_Real_VALUE).setTrdMarket(TrdCommon.TrdMarket.TrdMarket_US_VALUE)
		//		.build();
		//TrdGetPositionList.C2S c2s = TrdGetPositionList.C2S.newBuilder().setHeader(header).build();
		//TrdGetPositionList.Request req = TrdGetPositionList.Request.newBuilder().setC2S(c2s).build();
		//int seqNo = trd.getPositionList(req);
		//System.out.printf("Send TrdGetPositionList: %d\n", seqNo);
	}
	
	private void getTradeAccList() {
		TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder().setUserID(loginAccId)
                .build();
        TrdGetAccList.Request req = TrdGetAccList.Request.newBuilder().setC2S(c2s).build();
        int seqNo = trd.getAccList(req);
        System.out.printf("Send TrdGetAccList: %d\n", seqNo);
	}
	
	public void setFutuService(FutuService service) {
		this.futuService = service;
	}
	
	public void unlockTrade() {
		if(!tradeUnlocked) {
	        TrdUnlockTrade.C2S c2s = TrdUnlockTrade.C2S.newBuilder()
	                .setUnlock(true)
	                .setPwdMD5(tradePwdMd5)
	                .setSecurityFirm(TrdCommon.SecurityFirm.SecurityFirm_FutuSecurities.getNumber())
	                .build();
	        TrdUnlockTrade.Request req = TrdUnlockTrade.Request.newBuilder().setC2S(c2s).build();
	        int seqNo = trd.unlockTrade(req);
	        System.out.printf("Send TrdUnlockTrade: %d\n", seqNo);
		}
	}
	
	public void syncUSFutuData() {
		unlockTrade();
		getRepoList(MyFutuUtil.ALLAccId, TrdCommon.TrdMarket.TrdMarket_HK_VALUE);
	}
	
	QotSub.Response subSync(List<QotCommon.Security> secList, List<QotCommon.SubType> subTypeList, boolean isSub,
			boolean isRegPush) throws InterruptedException {
		ReqInfo reqInfo = null;
		Object syncEvent = new Object();

		synchronized (syncEvent) {
			synchronized (qotLock) {
				QotSub.C2S c2s = QotSub.C2S.newBuilder().addAllSecurityList(secList)
						.addAllSubTypeList(
								subTypeList.stream().mapToInt((QotCommon.SubType subType) -> subType.getNumber())
										.boxed().collect(Collectors.toList()))
						.setIsSubOrUnSub(isSub).setIsRegOrUnRegPush(isRegPush).build();
				QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
				int sn = qot.sub(req);
				if (sn == 0)
					return null;
				reqInfo = new ReqInfo(ProtoID.QOT_SUB, syncEvent);
				qotReqInfoMap.put(sn, reqInfo);
			}
			syncEvent.wait();
			return (QotSub.Response) reqInfo.rsp;
		}
	}
	
	//测试获取基本行情，需要先订阅才能获取
    public void getBasicQot(int market, String code) {
        Integer subscribedCodeMarket = subscribedCodes.get(code);
        if(subscribedCodeMarket == null && subscribedCodes.keySet().size() < max_sub_basic_qot_num) {
        	subscribedCodes.put(code, market);
    		
    		QotCommon.Security sec1 = QotCommon.Security.newBuilder().setCode(code)
                    .setMarket(market)
                    .build();
            QotSub.C2S c2s = QotSub.C2S.newBuilder().addSecurityList(sec1)
                    .addSubTypeList(QotCommon.SubType.SubType_RT_VALUE)
                    .addSubTypeList(QotCommon.SubType.SubType_KL_Day_VALUE)
                    .addSubTypeList(QotCommon.SubType.SubType_Basic_VALUE)
                    .setIsSubOrUnSub(true)
                    .setIsRegOrUnRegPush(true)
                    .setIsFirstPush(true)
                    .build();
            QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
            qot.sub(req);
        }
        else {
        	QotCommon.Security sec1 = QotCommon.Security.newBuilder().setCode(code)
                    .setMarket(market)
                    .build();
            QotGetBasicQot.C2S c2s = QotGetBasicQot.C2S.newBuilder().addSecurityList(sec1).build();
            QotGetBasicQot.Request req = QotGetBasicQot.Request.newBuilder().setC2S(c2s).build();
            qot.getBasicQot(req);
        }
    }


	public static void main(String[] args) throws InterruptedException {
		FTAPI.init(); // 初始化环境，程序启动时调用1次
		MyFutuUtil futuApp = new MyFutuUtil();
		futuApp.start("127.0.0.1", 11111);
		futuApp.getTradeAccList();
		//futuApp.unlockTrade();
		Thread.sleep(3000L);
		//futuApp.getRepoList(USAccId, TrdCommon.TrdMarket.TrdMarket_US_VALUE);

		while (true) {
			try {
				Thread.sleep(1000 * 60);
			} catch (InterruptedException exc) {

			}
		}
	}
}
