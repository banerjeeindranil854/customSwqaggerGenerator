/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mtn.aggregator.models.response;

import lombok.Data;
import org.springframework.hateoas.Link;

@Data
public class HateoasContainer {
    Link self;
}
