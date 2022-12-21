package com.softwarevax.flume.client.configura.result;

import com.softwarevax.flume.exception.ExceptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.flume.FlumeException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GoalExceptionHandler {

    /**
     * 参数不合法异常
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = FlumeException.class)
    public ResultDto<?> flumeException(FlumeException e) {
        log.error(e.getMessage(), e);
        return ResultDto.fail(false, ExceptionEnum.getCode(e.getClass()), e.getMessage());
    }

    /**
     * 服务器内部异常
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResultDto<?> exception(Exception e) {
        log.error(e.getMessage(), e);
        return ResultDto.fail(false, ExceptionEnum.getCode(e.getClass()), "服务器内部异常");
    }
}
