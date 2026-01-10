package com.ivan.test.futu;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.ivan.mfutu.MFutuApplication;
import com.ivan.mfutu.entity.SubBasicQot;
import com.ivan.mfutu.mapper.SubBasicQotMapper;
import com.ivan.mfutu.service.FutuService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MFutuApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class SubBasicQotTest {

    @Autowired
    FutuService futuService;

    @Autowired
    SubBasicQotMapper subBasicQotMapper;

    @Test
    public void testListAll() {
        List<SubBasicQot> list = futuService.listSubBasicQotAll();
        Assert.notNull(list);
    }

    @Test
    public void testUpsert() {
        String code = "TEST_SUB_BASIC_1";
        // ensure clean
        try { subBasicQotMapper.delete(code); } catch (Exception e) {}

        SubBasicQot s = new SubBasicQot();
        s.setCode(code);
        s.setMarket(1);
        s.setName("Test Name");
        s.setCurPrice(12.34f);
        s.setLastClosePrice(11.11f);
        s.setIncreaseRate(1.12f);

        futuService.upsertSubBasicQot(s);

        SubBasicQot got = subBasicQotMapper.get(code);
        Assert.notNull(got);
        Assert.isTrue(got.getCurPrice().equals(12.34f));

        // update
        s.setCurPrice(20.0f);
        futuService.upsertSubBasicQot(s);
        SubBasicQot got2 = subBasicQotMapper.get(code);
        Assert.isTrue(got2.getCurPrice().equals(20.0f));
    }
}
