package com.tuneit.itc.feedback;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.tuneit.itc.commons.model.JsonConvertible;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackPortletConfig implements JsonConvertible {
    private String emailFrom;
    private List<String> emailsTo;

    @Override
    public String toJsonString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
