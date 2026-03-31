package com.hc.bookkeeping.modules.bkeeping.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/bookkeepingUser")
@RequiredArgsConstructor
@Api(tags = "用户接口")
public class BookkeepingUserController {
}
