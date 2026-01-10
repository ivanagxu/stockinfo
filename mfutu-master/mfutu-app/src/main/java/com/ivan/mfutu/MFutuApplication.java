package com.ivan.mfutu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.ivan.mfutu.entity.Category;
import com.ivan.mfutu.entity.FutuData;
import com.ivan.mfutu.service.FutuService;

@SpringBootApplication
@RestController
@MapperScan("com.ivan.mfutu.mapper")
@EnableScheduling
public class MFutuApplication {

	@Autowired
	FutuService service;
	
	private static BlockingQueue<String> resultQueue = new LinkedBlockingQueue<String>();
	
	public static void main(String[] args) {
		SpringApplication.run(MFutuApplication.class, args);
	}
	private void addReturnMessage(Model model) {
		if(resultQueue.size() > 0) {
			List<String> msgs = new ArrayList<String>();
			while(resultQueue.size() > 0) {
				try {
					msgs.add(resultQueue.poll(10, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			model.addAttribute("resultMessages", msgs);
		}
	}
	
	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@GetMapping("/sayHello")
	public ModelAndView sayHello(Model model, @RequestParam(value = "name", defaultValue = "World") String name) {
		model.addAttribute("message", String.format("Hello %s!", name));
		return new ModelAndView("hello", "msgModel", model);
		//return String.format("Hello %s!", name);
	}
	@GetMapping("/home")
	public ModelAndView index(Model model, @RequestParam(value = "plDate", defaultValue = "1900-01-01") String plDate) {
		List<String> plDateList = service.listPlDates();
		if("1900-01-01".equals(plDate) && plDateList.size() > 0) {
			plDate = plDateList.get(plDateList.size() - 1);
		}
		model.addAttribute("plDate", plDate);
		List<FutuData> datas = service.listFutuDataByDate(plDate);
		model.addAttribute("futuDatas", datas);
		addReturnMessage(model);
		return new ModelAndView("home", "data", model);
	}
	
	@GetMapping("/manage")
	public ModelAndView manage(Model model, @RequestParam(value = "plDate", defaultValue = "2021-01-18") String plDate) {
		List<Category> categories = service.listCategory();
		model.addAttribute("plDate", plDate);
		model.addAttribute("categories", categories);
		addReturnMessage(model);
		return new ModelAndView("manage", "data", model);
	}
	@GetMapping("/sync")
	public ModelAndView sync(Model model, int market) {
		service.syncFutuData(market);
		resultQueue.add("Sync futu data success");
		addReturnMessage(model);
		List<Category> categories = service.listCategory();
		model.addAttribute("categories", categories);
		return new ModelAndView("manage", "data", model);
	}
	@GetMapping("/getBasicQot")
	public ModelAndView getBasicQot(Model model, int market, String code) {
		service.getBasicQot(market, code);
		resultQueue.add("Get BasicQot(" + code + ") success");
		addReturnMessage(model);
		List<Category> categories = service.listCategory();
		model.addAttribute("categories", categories);
		return new ModelAndView("manage", "data", model);
	}
	@PostMapping("/updateCategory")
	@ResponseBody
	public Map<String, Object> updateCategory(@RequestBody List<Category> categories) {
		service.importCategory(categories);
		resultQueue.add("Update category success");
		
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("count", categories.size());
        return map;
	}
}
