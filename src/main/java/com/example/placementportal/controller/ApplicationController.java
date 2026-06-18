package com.example.placementportal.controller;

import com.example.placementportal.entity.JobApplication;
import com.example.placementportal.service.JobApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apply")
public class ApplicationController {

    @Autowired
    private JobApplicationService service;


    @PostMapping
    public JobApplication applyJob(@RequestBody JobApplication application) {
        return service.applyJob(application);
    }
}