package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Entity.PromoCode;
import com.example.GlobalTrackerGeo.Repository.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromoCodeService {

    @Autowired
    private PromoCodeRepository promoCodeRepository;


    public Map<String, Object> getPromoCodes(int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Order.asc("status"), Sort.Order.desc("expiredDate")));

        Page<PromoCode> promoCodes = promoCodeRepository.findAll(pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("promoCodes", promoCodes.getContent());
        response.put("total", promoCodeRepository.count());

        return response;
    }
}
