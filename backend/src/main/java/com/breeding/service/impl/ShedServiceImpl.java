package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.common.BusinessException;
import com.breeding.entity.Animal;
import com.breeding.entity.Shed;
import com.breeding.mapper.AnimalMapper;
import com.breeding.mapper.ShedMapper;
import com.breeding.service.ShedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShedServiceImpl extends ServiceImpl<ShedMapper, Shed> implements ShedService {

    @Autowired
    private AnimalMapper animalMapper;

    @Override
    public Page<Shed> getShedPage(int pageNum, int pageSize, String name) {
        Page<Shed> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Shed> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Shed::getName, name);
        }
        wrapper.orderByDesc(Shed::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean deleteWithCheck(Long id) {
        long animalCount = animalMapper.selectCount(new LambdaQueryWrapper<Animal>()
                .eq(Animal::getShedId, id)
                .eq(Animal::getDeleted, 0));
        if (animalCount > 0) {
            throw new BusinessException("该栏舍下还有 " + animalCount + " 只动物，请清空后再删除");
        }
        return this.removeById(id);
    }
}
