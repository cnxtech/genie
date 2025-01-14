/*
 *
 *  Copyright 2019 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.web.dtos

import spock.lang.Specification

/**
 * Specifications for {@link GenieWebHostInfo}.
 *
 * @author tgianos
 */
class GenieWebHostInfoSpec extends Specification {

    def "can create and execute POJO methods"() {
        def hostname = UUID.randomUUID().toString()
        def rpcPort = 33_352

        when:
        def info = new GenieWebHostInfo(hostname, rpcPort)

        then:
        info.getHostname() == hostname
        info.getRpcPort() == rpcPort

        when:
        def info2 = new GenieWebHostInfo(hostname, rpcPort)
        def info3 = new GenieWebHostInfo(hostname + UUID.randomUUID().toString(), rpcPort + 1)

        then:
        info == info2
        info != info3
        info.hashCode() == info2.hashCode()
        info2.hashCode() != info3.hashCode()
        info.toString() == info2.toString()
        info.toString() != info3.toString()
    }
}
