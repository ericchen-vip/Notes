package cn.eccto.study.springframework.tutorials.conditional.serivce.impl;

import cn.eccto.study.springframework.tutorials.conditional.serivce.LocaleService;

/**
 * description
 *
 * @author EricChen 2019/11/15 20:59
 */
public class LocaleCanadaServiceImpl implements LocaleService {

    @Override
    public void sayHello() {
        System.out.println("hello");
    }
}
