package com.breeding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.breeding.entity.Animal;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnimalMapper extends BaseMapper<Animal> {
}
