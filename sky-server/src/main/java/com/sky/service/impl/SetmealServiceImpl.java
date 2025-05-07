package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐以及套餐-菜品关系表
     * @param setmealDTO
     */
    public void saveWithDishes(SetmealDTO setmealDTO) {
        //插入setmeal表（setmeal类）
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        //获取setmeal主键
        Long setmealId = setmeal.getId();

        //插入setmeal_dish表(setmealDish)
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 删除套餐
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            if(id != null) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteBySetmealIds(ids);

    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        //查询setmeal表
        Setmeal setmeal = setmealMapper.getById(id);
        BeanUtils.copyProperties(setmeal, setmealVO);
        //查询setmeal_dish表
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        //返回
        return setmealVO;
    }

    /**
     * 更新套餐
     * @param setmealDTO
     */
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //更新setmeal表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        //获取套餐id
        Long setmealId = setmeal.getId();
        //setmealDish—根据setmealId删除相关数据
        setmealDishMapper.deleteBysetmealId(setmealId);
        //setmealDish——重新插入新数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 起售停售套餐
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        if (status == StatusConstant.ENABLE){
            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            setmealDishes.forEach(setmealDish -> {
                Long dishId = setmealDish.getDishId();
                Dish dish = dishMapper.getById(dishId);
                if(dish.getStatus() == StatusConstant.DISABLE){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
