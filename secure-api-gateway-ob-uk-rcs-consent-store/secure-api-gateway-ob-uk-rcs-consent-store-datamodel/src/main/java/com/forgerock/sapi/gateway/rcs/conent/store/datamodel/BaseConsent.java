/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.sapi.gateway.rcs.conent.store.datamodel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;


@Validated
public class BaseConsent<T> {

    @NotNull
    private String id;
    @NotNull
    @Valid
    private T requestObj;
    @NotNull
    private String requestType;
    @NotNull
    private OBVersion requestVersion;
    @NotNull
    private String status;
    @NotNull
    private String apiClientId;
    @NotNull
    private DateTime creationDateTime;
    @NotNull
    private DateTime statusUpdateDateTime;

    private String resourceOwnerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getRequestObj() {
        return requestObj;
    }

    public void setRequestObj(T requestObj) {
        this.requestObj = requestObj;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public OBVersion getRequestVersion() {
        return requestVersion;
    }

    public void setRequestVersion(OBVersion requestVersion) {
        this.requestVersion = requestVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApiClientId() {
        return apiClientId;
    }

    public void setApiClientId(String apiClientId) {
        this.apiClientId = apiClientId;
    }

    public String getResourceOwnerId() {
        return resourceOwnerId;
    }

    public void setResourceOwnerId(String resourceOwnerId) {
        this.resourceOwnerId = resourceOwnerId;
    }

    public DateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(DateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public DateTime getStatusUpdateDateTime() {
        return statusUpdateDateTime;
    }

    public void setStatusUpdateDateTime(DateTime statusUpdateDateTime) {
        this.statusUpdateDateTime = statusUpdateDateTime;
    }

    @Override
    public String toString() {
        return "BaseConsent{" +
                "id='" + id + '\'' +
                ", requestObj=" + requestObj +
                ", requestType='" + requestType + '\'' +
                ", requestVersion=" + requestVersion +
                ", status='" + status + '\'' +
                ", apiClientId='" + apiClientId + '\'' +
                ", creationDateTime=" + creationDateTime +
                ", statusUpdateDateTime=" + statusUpdateDateTime +
                ", resourceOwnerId='" + resourceOwnerId + '\'' +
                '}';
    }
}

