package com.breeding.service.ai;

import com.breeding.common.LoginUser;
import com.breeding.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 数据访问中间层
 * 负责在AI查询前进行权限拦截，只返回当前用户有权限查看的数据
 */
@Component
public class AIDataAccessLayer {

    @Autowired
    private AnimalService animalService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private EventService eventService;

    @Autowired
    private SymptomService symptomService;

    /**
     * 根据当前用户权限，获取AI可分析的上下文数据摘要
     */
    public Map<String, Object> getAccessibleContextData() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> permissions = loginUser.getPermissions();
        
        Map<String, Object> contextData = new HashMap<>();
        List<String> accessedModules = new ArrayList<>();

        // 1. 动物数据访问权限校验
        if (hasPermission(permissions, "animal:view", "system:*", "ROLE_admin", "ROLE_owner", "ROLE_vet")) {
            // 获取简要的动物统计数据供AI分析
            contextData.put("animal_total", animalService.count());
            accessedModules.add("animal");
        }

        // 2. 库存数据访问权限校验
        if (hasPermission(permissions, "inventory:view", "system:*", "ROLE_admin")) {
            contextData.put("inventory_total", inventoryService.count());
            accessedModules.add("inventory");
        }

        // 3. 事件数据访问权限校验
        if (hasPermission(permissions, "dashboard:view", "report:view", "system:*", "ROLE_admin", "ROLE_owner")) {
            contextData.put("event_total", eventService.count());
            accessedModules.add("event");
        }

        // 4. 疾病数据访问权限校验
        if (hasPermission(permissions, "disease:view", "disease:add", "diagnosis:add", "treatment:add", "system:*", "ROLE_admin", "ROLE_owner", "ROLE_vet")) {
            contextData.put("symptom_total", symptomService.count());
            accessedModules.add("disease");
        }

        contextData.put("accessedModules", String.join(",", accessedModules));
        return contextData;
    }

    private boolean hasPermission(List<String> userPerms, String... requiredPerms) {
        for (String perm : requiredPerms) {
            if (userPerms.contains(perm)) {
                return true;
            }
        }
        return false;
    }
}
