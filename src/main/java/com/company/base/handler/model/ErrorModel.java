package com.company.base.handler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorModel(@JsonProperty("message") String message) {}
