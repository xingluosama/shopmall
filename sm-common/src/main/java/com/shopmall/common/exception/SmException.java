package com.shopmall.common.exception;

import com.shopmall.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SmException extends RuntimeException {

    private ExceptionEnum exceptionEnum;
}
