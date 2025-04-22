package com.sky.service;


import com.sky.dto.DishDTO;


public interface DishService {

    /**
     * 更新菜品以及口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
