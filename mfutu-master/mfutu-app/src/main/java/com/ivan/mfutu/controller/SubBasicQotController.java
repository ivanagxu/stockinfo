package com.ivan.mfutu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String view() {
        return "subBasicQot";
    }

    /**
     * Get all SubBasicQot data as JSON API
     * @return List of SubBasicQot objects as JSON
     */
    @GetMapping("/api/list")
    @ResponseBody
    public List<SubBasicQot> listAll() {
        return futuService.listSubBasicQotAll();
    }
}
