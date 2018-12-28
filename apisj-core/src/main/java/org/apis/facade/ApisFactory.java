/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.apis.facade;

import org.apis.config.DefaultConfig;
import org.apis.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;


/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Component
public class ApisFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");

    public static Apis createEthereum() {
        return createEthereum((Class) null);
    }

    public static Apis createEthereum(Class userSpringConfig) {
        return userSpringConfig == null ? createEthereum(new Class[] {DefaultConfig.class}) :
                createEthereum(DefaultConfig.class, userSpringConfig);
    }

    /**
     * @deprecated The config parameter is not used anymore. The configuration is passed
     * via 'systemProperties' bean either from the DefaultConfig or from supplied userSpringConfig
     * @param config  Not used
     * @param userSpringConfig   User Spring configuration class
     * @return  Fully initialized Apis instance
     */
    public static Apis createEthereum(SystemProperties config, Class userSpringConfig) {

        return userSpringConfig == null ? createEthereum(new Class[] {DefaultConfig.class}) :
                createEthereum(DefaultConfig.class, userSpringConfig);
    }

    public static Apis createEthereum(Class ... springConfigs) {
        logger.info("Starting APIS...");
        ApplicationContext context = new AnnotationConfigApplicationContext(springConfigs);

        return context.getBean(Apis.class);
    }
}
