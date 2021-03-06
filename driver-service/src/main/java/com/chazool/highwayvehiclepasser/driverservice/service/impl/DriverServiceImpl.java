package com.chazool.highwayvehiclepasser.driverservice.service.impl;


import com.chazool.highwayvehiclepasser.driverservice.repository.DriverRepository;
import com.chazool.highwayvehiclepasser.driverservice.service.DriverService;
import com.chazool.highwayvehiclepasser.driverservice.service.EmailSenderService;
import com.chazool.highwayvehiclepasser.driverservice.thread.EmailSender;
import com.chazool.highwayvehiclepasser.driverservice.thread.PaymentCard;
import com.chazool.highwayvehiclepasser.model.driverservice.Driver;
import com.chazool.highwayvehiclepasser.model.emailservice.Email;
import com.chazool.highwayvehiclepasser.model.exception.*;
import com.chazool.highwayvehiclepasser.model.paymentservice.PaymentMethod;
import com.chazool.highwayvehiclepasser.model.responsehandle.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private EmailSenderService emailSenderService;
    @Autowired
    private RestTemplate restTemplate;


    @Override
    public Driver save(Driver driver, String authorization)
            throws InvalidEmailException, InvalidPasswordException, InvalidNameException, InvalidDrivingLicenseException {

        isValid(driver);
        if (driver.getPassword().trim().equals(null) || driver.getPassword().trim().equals("")
                || driver.getPassword().trim().length() <= 5) {
            throw new InvalidEmailException("Invalid Password");
        }
        driver.setActive(true);

        driver.setRegistrationDate(LocalDateTime.now(ZoneId.of("Asia/Colombo")).toString());

        try {
            driver = driverRepository.save(driver);
            PaymentCard paymentCard = new PaymentCard(driver, this, authorization);
            paymentCard.start();
            return driver;
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw new DuplicateEntryException("Username already exit. please Check again");
        }
    }

    @Override
    public Driver update(Driver driver)
            throws InvalidIdException, InvalidEmailException, InvalidPasswordException
            , InvalidNameException, InvalidDrivingLicenseException {

        findById(driver.getId());
        isValid(driver);
        return driverRepository.save(driver);
    }

    @Override
    public Driver delete(int id) throws InvalidIdException {

        Driver driver = findById(id);
        driver.setActive(false);
        return driverRepository.save(driver);
    }

    @Override
    public Driver findById(int id) throws InvalidIdException {
        Optional<Driver> driver = driverRepository.findById(id);
        return driver.isPresent() ? driver.get() : new Driver();

    }

    @Override
    public Driver findByDLicenseNo(String dLicenseNo) {
        Optional<Driver> driver = driverRepository.findByDrivingLicenseNo(dLicenseNo);
        return driver.isPresent() ? driver.get() : new Driver();
    }

    @Override
    public Driver findByEmail(String email) {
        Optional<Driver> driver = driverRepository.findByEmail(email);
        return driver.isPresent() ? driver.get() : new Driver();
    }

    @Override
    public Driver findByUsername(String username) {
        Optional<Driver> optionalDriver = driverRepository.findByUsername(username);
        return optionalDriver.isPresent() ? optionalDriver.get() : new Driver();
    }

    @Override
    public List<Driver> findByAll() {
        return driverRepository.findAll();
    }


    private void isValid(Driver driver) throws InvalidPasswordException, InvalidEmailException {

        if (driver.getFirstName().trim().toString().equals(null) || driver.getFirstName().trim().equals(""))
            throw new InvalidNameException("Invalid First Name");
        if (driver.getLastName().trim().toString().equals(null) || driver.getLastName().trim().equals(""))
            throw new InvalidNameException("Invalid Last Name");
        if (Pattern.compile("^(.+)@(.+)$").matcher(driver.getEmail()).matches() == false) {
            throw new InvalidEmailException("Invalid Email Address");
        }
        if (driver.getDrivingLicenseNo().trim().equals(null) || driver.getDrivingLicenseNo().trim().equals("")
                || driver.getDrivingLicenseNo().trim().length() < 9) {
            new InvalidDrivingLicenseException("Invalid Driving Licence ");
        }

    }


    @Override
    public void createCard(Driver driver, String authorization) {

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setDriver(driver.getId());

        String date = LocalDateTime.now(ZoneId.of("Asia/Colombo")).toString();

        paymentMethod.setIssueDate(date);
        paymentMethod.setBalanceAmount(new BigDecimal("0.00"));
        paymentMethod.setActive(true);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", authorization);
        JSONObject jsonObject = new JSONObject(paymentMethod);
        HttpEntity httpEntity = new HttpEntity<>(jsonObject.toString(), httpHeaders);

        ResponseEntity<Response> responseEntity = restTemplate
                .exchange("http://payment/services/payment-method", HttpMethod.POST, httpEntity, Response.class);

        Map response = (Map) responseEntity.getBody().getData();

        Email email = new Email();
        email.setEmail(driver.getEmail());
        email.setSubject("Registration");
        email.setMessage("your registration is completed \n Card No: " + response.get("id"));

        EmailSender emailSender = new EmailSender(email, emailSenderService, authorization);
        emailSender.start();

    }


}
