package com.example.springgateway;

import org.junit.Test;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public class FunctionTest {

    @Test
    public void contextLoads() {
        IntFunction test01 = new IntFunction() {
            @Override
            public Object apply(int value) {
                return null;
            }
        };
        test01.apply(123);
    }

    @Test
    public void rewriteTest() {
        String path = "/xxapi/get";
        String newPath = path.replaceAll("/xxapi/(?<segment>.*)","/${segment}");
        // input >     /xxapi/get
        // out   >     /get
        System.out.println(newPath);
    }

    @Test
    public void predicateTest() {
        // Predicate
        // 条件测试接口
        Predicate predicate;

    }





}
