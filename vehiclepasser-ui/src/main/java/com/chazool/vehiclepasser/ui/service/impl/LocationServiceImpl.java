package com.chazool.vehiclepasser.ui.service.impl;

import com.chazool.highwayvehiclepasser.model.transactionservice.Location;
import com.chazool.highwayvehiclepasser.model.transactionservice.Terminal;
import com.chazool.vehiclepasser.ui.config.AccessToken;
import com.chazool.vehiclepasser.ui.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<Location> findAllLocations() {

        ResponseEntity<Location[]> locations = restTemplate.exchange(
                "http://transsaction/services/locations/"
                , HttpMethod.GET
                , AccessToken.getHttpEntity()
                , Location[].class);
        return Arrays.asList(locations.getBody());
    }

    @Override
    public List<Terminal> findTerminal(int location, int terminalType) {

        ResponseEntity<Terminal[]> terminals = restTemplate.exchange(
                "http://transsaction/services/terminal?location=" + location + "&terminalType=" + terminalType
                , HttpMethod.GET
                , AccessToken.getHttpEntity()
                , Terminal[].class);
        return Arrays.asList(terminals.getBody());
    }




    @Override
    public Location findLocationById(int id) {
        Location location = restTemplate.getForObject("http://transsaction/services/locations/" + id, Location.class);
        return location;

    }

    @Override
    public Terminal findTerminalById(int id) {
        Terminal terminal = restTemplate.getForObject("http://transsaction/services/terminal/" + id, Terminal.class);
        return terminal;
    }
}
