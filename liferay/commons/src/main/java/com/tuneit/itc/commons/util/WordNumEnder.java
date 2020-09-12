package com.tuneit.itc.commons.util;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class WordNumEnder {

    public String getEnding(int number, List<String> words) {
        number = number % 100;
        if (number >= 11 && number <= 19) {
            return words.get(2);
        } else {
            number = number % 10;
            switch (number) {
                case (1):
                    return words.get(0);
                case (2):
                case (3):
                case (4):
                    return words.get(1);
                default:
                    return words.get(2);
            }
        }
    }
}
