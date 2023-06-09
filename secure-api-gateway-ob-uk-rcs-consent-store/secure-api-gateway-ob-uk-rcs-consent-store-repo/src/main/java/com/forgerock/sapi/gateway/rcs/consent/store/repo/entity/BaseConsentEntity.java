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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.entity;

import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

public class BaseConsentEntity<T> {

    @Id
    private String id;
    @Version
    private int version;
    private T requestObj;
    private String requestType;
    private OBVersion requestVersion;
    private String status;
    private String apiClientId;
    private String resourceOwnerId;

    @CreatedDate
    private DateTime creationDateTime;

    @LastModifiedDate
    private DateTime statusUpdatedDateTime;

    public BaseConsentEntity() {
    }

    public String getId() {
        return id;
    }

    public T getRequestObj() {
        return requestObj;
    }

    public String getRequestType() {
        return requestType;
    }

    public OBVersion getRequestVersion() {
        return requestVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRequestObj(T requestObj) {
        this.requestObj = requestObj;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setRequestVersion(OBVersion requestVersion) {
        this.requestVersion = requestVersion;
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

    public void setResourceOwnerId(String resourceOwnerId) {
        this.resourceOwnerId = resourceOwnerId;
    }

    public String getResourceOwnerId() {
        return resourceOwnerId;
    }

    public DateTime getCreationDateTime() {
        return creationDateTime;
    }

    public DateTime getStatusUpdatedDateTime() {
        return statusUpdatedDateTime;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "BaseConsentEntity{" +
                "id='" + id + '\'' +
                ", requestObj=" + requestObj +
                ", requestType='" + requestType + '\'' +
                ", requestVersion=" + requestVersion +
                ", status='" + status + '\'' +
                ", tppId='" + apiClientId + '\'' +
                ", resourceOwnerId='" + resourceOwnerId + '\'' +
                ", creationDateTime=" + creationDateTime +
                ", statusUpdatedDateTime=" + statusUpdatedDateTime +
                '}';
    }
}
