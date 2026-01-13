package com.ivan.mfutu.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;

import org.springframework.ui.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ivan.mfutu.entity.SubBasicQot;
import com.ivan.mfutu.service.FutuService;

@Controller
@RequestMapping("/subBasicQot")
public class SubBasicQotController {

    @Autowired
    private FutuService futuService;

    /**
     * View page for SubBasicQot display
     * @return view name
     */
    @GetMapping("")
    public String view(Model model,@RequestParam(required = false) Integer market) {
    	if (market == null) {
            // 根据当前时间决定market：白天查港股（market=1），夜间查美股（market=2）
            LocalTime now = LocalTime.now();
            if (now.isAfter(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(17, 0))) {
                market = 1; // 港股
            } else {
                market = 2; // 美股
            }
        }
    	List<SubBasicQot> raw = futuService.listSubBasicQotByMarket(market);
        List<Map<String, Object>> viewList = new ArrayList<>();
        Date now = new Date();
        if (raw != null) {
            for (SubBasicQot s : raw) {
                Map<String, Object> m = new HashMap<>();
                m.put("code", s.getCode() == null ? "N/A" : s.getCode());
                m.put("name", s.getName() == null ? "N/A" : s.getName());
                Integer mk = s.getMarket();
                String mkName = "N/A";
                if (mk != null) {
                    if (mk == 1) mkName = "港股";
                    else if (mk == 2) mkName = "美股";
                    else if (mk == 3) mkName = "A股";
                }
                m.put("market", mkName);

                float cur = s.getCurPrice() == null ? 0f : s.getCurPrice();
                float lastClose = s.getLastClosePrice() == null ? 0f : s.getLastClosePrice();
                float inc = s.getIncreaseRate() == null ? 0f : s.getIncreaseRate();
                m.put("curPrice", String.format("%.2f", cur));
                m.put("lastClosePrice", String.format("%.2f", lastClose));
                m.put("increaseRate", String.format((inc >= 0 ? "+" : "") + "%.2f%%", inc));

                String relative = "N/A";
                Date ut = s.getUpdateTime();
                if (ut != null) {
                    // Database stores updateTime in UTC (zone 0). Adjust to +8 before comparing to server local time.
                    long adjustedMillis = ut.getTime() - 8L * 60 * 60 * 1000;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - adjustedMillis);
                    if (seconds < 0) relative = "刚刚";
                    else if (seconds < 60) relative = seconds + "秒前";
                    else if (seconds < 3600) relative = (seconds / 60) + "分钟前";
                    else if (seconds < 86400) relative = (seconds / 3600) + "小时前";
                    else relative = (seconds / 86400) + "天前";
                }
                m.put("relativeTime", relative);

                viewList.add(m);
            }
        }

        model.addAttribute("qotList", viewList);
        model.addAttribute("lastUpdate", new Date());
        return "subBasicQot";
    }

    /**
     * Get all SubBasicQot data as JSON API
     * @return List of SubBasicQot objects as JSON
     */
    @GetMapping("/api/list")
    @ResponseBody
    public List<SubBasicQot> listAll(@RequestParam(required = false) Integer market) {
        if (market == null) {
            // 根据当前时间决定market：白天查港股（market=1），夜间查美股（market=2）
            LocalTime now = LocalTime.now();
            if (now.isAfter(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(17, 0))) {
                market = 1; // 港股
            } else {
                market = 2; // 美股
            }
        }
        return futuService.listSubBasicQotByMarket(market);
    }
}
