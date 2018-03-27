package com.aps.env.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * <dl>
 * <dt>com.aps.env.controller.ExceptionController</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    DXC Technology </dd>
 * <dd>CreateDate: 2018/3/19</dd>
 * </dl>
 *
 * @author appleshow
 */
@RestController
public abstract class ExceptionController {
    private static final Logger LOG = LogManager.getLogger(ExceptionController.class);

    /**
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler
    @ResponseBody
    public List<String> exception(HttpServletRequest request, Exception e) {
        final List<String> errorList = new ArrayList<>();

        errorList.add(e.getMessage());
        LOG.error(e);

        return errorList;
    }
}
