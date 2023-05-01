// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.athenz.client.zts.bindings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yahoo.security.Pkcs10Csr;
import com.yahoo.security.Pkcs10CsrUtils;
import com.yahoo.vespa.athenz.api.AthenzIdentity;

/**
 * Used for serializing request to ZTS
 *
 * @author mortent
 * @author bjorncs
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstanceRegisterInformation {
    @JsonProperty("provider")
    private final String provider;
    @JsonProperty("domain")
    private final String domain;
    @JsonProperty("service")
    private final String service;
    @JsonProperty("attestationData")
    private final String attestationData;
    @JsonProperty("csr")
    private final String csr;

    public InstanceRegisterInformation(AthenzIdentity providerIdentity,
                                       AthenzIdentity instanceIdentity,
                                       String attestationData,
                                       Pkcs10Csr csr) {
        this.provider = providerIdentity.getFullName();
        this.domain = instanceIdentity.getDomain().getName();
        this.service = instanceIdentity.getName();
        this.attestationData = attestationData;
        this.csr = Pkcs10CsrUtils.toPem(csr);
    }
}
