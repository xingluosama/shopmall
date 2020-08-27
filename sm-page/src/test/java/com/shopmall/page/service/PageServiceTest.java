package com.shopmall.page.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

//@SpringBootTest
//@RunWith(SpringRunner.class)
public class PageServiceTest {

    @Autowired
    private PageService pageService;

    @Test
    public void createHtml() {
        pageService.createHtml(141L);
    }
}