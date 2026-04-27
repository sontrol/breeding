package com.breeding.controller;

import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.service.InvalidDataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invalid-data")
public class InvalidDataController {

    private final InvalidDataService invalidDataService;

    public InvalidDataController(InvalidDataService invalidDataService) {
        this.invalidDataService = invalidDataService;
    }

    @PutMapping("/restore")
    @PreAuthorize("hasAuthority('system:invalid:restore')")
    public Result<Boolean> restore(@RequestParam String dataType, @RequestParam Long dataId) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return invalidDataService.restore(dataType, dataId, loginUser.getUser().getId()) ? Result.success() : Result.error("恢复失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
