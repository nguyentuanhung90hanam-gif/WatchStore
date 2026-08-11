package com.watchstore.repository;

import com.watchstore.model.Voucher;
import java.util.List;

public interface VoucherRepository {

    List<Voucher> findAll();

    Voucher findById(Integer id);

    List<Voucher> search(String keyword);

    boolean existsByCode(String code, Integer excludeId);

    boolean save(Voucher voucher);

    boolean update(Voucher voucher);

    boolean delete(Integer id);

    boolean isVoucherInUse(Integer id);

}