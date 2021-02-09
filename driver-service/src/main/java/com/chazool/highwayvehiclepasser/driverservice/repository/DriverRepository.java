package com.chazool.highwayvehiclepasser.driverservice.repository;

import com.chazool.highwayvehiclepasser.model.driverservice.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Integer> {


    Optional<Driver> findBydLicenseNo(String dLicenseNo);

    Optional<Driver> findByEmail(String email);

}
