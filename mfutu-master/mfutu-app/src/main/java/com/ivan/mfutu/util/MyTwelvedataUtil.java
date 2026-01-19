package com.ivan.mfutu.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.ivan.mfutu.entity.SubBasicQot;
import com.ivan.mfutu.service.FutuService;

public class MyTwelvedataUtil {
	public static final String apiKey = "0b083fbbd1d04f05bcb60a9b476f6671";

	private FutuService futuService;

	public void setFutuService(FutuService service) {
		this.futuService = service;
	}

	/**
	 * Update prices for all subscribed symbols from Twelve Data and upsert to DB
	 * via FutuService.
	 */
	public void updateAllPrices() {
		if (futuService == null)
			return;
		try {
			List<SubBasicQot> subs = futuService.listSubBasicQotAll();
			if (subs == null || subs.isEmpty())
				return;
			for (SubBasicQot sb : subs) {
				try {
					if (sb == null || sb.getCode() == null || sb.getMarket() != 2)
						continue;
					// Delegate to getBasicQot which now fetches quote and upserts
					getBasicQot(sb.getMarket(), sb.getCode());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getBasicQot(int market, String code) {
		SubBasicQot sb = new SubBasicQot();
		sb.setCode(code.toUpperCase());
		sb.setMarket(market);
		HttpURLConnection conn = null;
		try {
			String qs = "symbol=" + URLEncoder.encode(code, "UTF-8") + "&apikey=" + URLEncoder.encode(apiKey, "UTF-8");
			String urlStr = "https://api.twelvedata.com/quote?" + qs;
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoInput(true);
			int codeResp = conn.getResponseCode();
			InputStream is = (codeResp >= 200 && codeResp < 300) ? conn.getInputStream() : conn.getErrorStream();
			if (is == null)
				return;
			StringBuilder sbBody = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
				String line;
				while ((line = br.readLine()) != null) {
					sbBody.append(line);
				}
			}
			String body = sbBody.toString();
			// Parse response JSON using fastjson
			JSONObject jo = null;
			try {
				jo = JSON.parseObject(body);
			} catch (Throwable _ex) {
				jo = null;
			}
			if (jo == null)
				return;
			if (jo.containsKey("name")) {
				sb.setName(jo.getString("name"));
			}
			Float curPrice = null;
			if (jo.containsKey("price"))
				curPrice = jo.getFloat("price");
			else if (jo.containsKey("close"))
				curPrice = jo.getFloat("close");
			Float prevClose = null;
			if (jo.containsKey("previous_close"))
				prevClose = jo.getFloat("previous_close");
			Float pct = null;
			if (jo.containsKey("percent_change"))
				pct = jo.getFloat("percent_change");
			else if (jo.containsKey("percentChange"))
				pct = jo.getFloat("percentChange");
			if (curPrice != null) {
				sb.setCurPrice(curPrice);
				if (prevClose != null)
					sb.setLastClosePrice(prevClose);
				if (pct != null) {
					sb.setIncreaseRate((float) (Math.round(pct * 100.0) / 100.0));
				} else {
					try {
						Float last = sb.getLastClosePrice();
						float inc = 0f;
						if (last != null && Math.abs(last) > 1e-9) {
							inc = 100f * ((curPrice - last) / last);
						}
						sb.setIncreaseRate((float) (Math.round(inc * 100.0) / 100.0));
					} catch (Throwable t) {
						sb.setIncreaseRate(0f);
					}
				}
				sb.setUpdateTime(new Date());
				futuService.upsertSubBasicQot(sb);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}
}
