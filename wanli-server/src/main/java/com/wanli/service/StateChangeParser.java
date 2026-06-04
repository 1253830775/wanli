package com.wanli.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.dto.StateChangeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StateChangeParser {

    private static final Logger log = LoggerFactory.getLogger(StateChangeParser.class);
    private static final Pattern STATE_CHANGE_PATTERN =
        Pattern.compile("【状态变更】\\s*(\\{[^】]*\\})");

    private final ObjectMapper objectMapper;

    public StateChangeParser() {
        this.objectMapper = new ObjectMapper();
    }

    public StateChangeDTO parse(String narrative) {
        if (narrative == null || narrative.isBlank()) return null;
        Matcher matcher = STATE_CHANGE_PATTERN.matcher(narrative);
        if (!matcher.find()) return null;
        String json = matcher.group(1);
        try {
            return objectMapper.readValue(json, StateChangeDTO.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse state change JSON: {}", json, e);
            return null;
        }
    }

    public String stripStateChangeBlock(String narrative) {
        if (narrative == null) return null;
        return STATE_CHANGE_PATTERN.matcher(narrative).replaceAll("").trim();
    }
}
