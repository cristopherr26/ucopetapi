package com.uco.ucopetapi.repository.sale.impl;

import com.uco.ucopetapi.domain.sale.ItemSaleDomain;
import com.uco.ucopetapi.repository.sale.ItemSaleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ItemSaleRepositoryImpl implements ItemSaleRepository {

    private final ItemSaleJpaRepository jpaRepository;

    public ItemSaleRepositoryImpl(ItemSaleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ItemSaleDomain save(ItemSaleDomain itemSaleDomain) {
        return jpaRepository.save(itemSaleDomain);
    }

    @Override
    public Optional<ItemSaleDomain> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ItemSaleDomain> findBySaleOrderId(UUID saleOrderId) {
        return jpaRepository.findBySaleOrderId(saleOrderId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
