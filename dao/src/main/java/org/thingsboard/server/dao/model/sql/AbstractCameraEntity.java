/**
 * Copyright © 2016-2021 The Thingsboard Authors
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
package org.thingsboard.server.dao.model.sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.Camera;
import org.thingsboard.server.common.data.device.data.DeviceData;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.util.mapping.JacksonUtil;
import org.thingsboard.server.dao.util.mapping.JsonBinaryType;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@MappedSuperclass
public abstract class AbstractCameraEntity<T extends Camera> extends BaseSqlEntity<T> implements SearchTextEntity<T> {

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "costumer_id", columnDefinition = "uuid")
    private UUID customerId;


    @Column(name = "name")
    private String name;

    @Column(name = "camera_type")
    private String cameraType;

    @Column(name = "model")
    private String model;

    @Column(name = "ip")
    private String ip;

    @Type(type = "json")
    @Column(name = "additional_info")
    private JsonNode additionalInfo;



    public AbstractCameraEntity() {
        super();

    }

    public AbstractCameraEntity(Camera camera) {
        if (camera.getId() != null) {
            this.setUuid(camera.getUuidId());
        }
        this.setCreatedTime(camera.getCreatedTime());
        if (camera.getTenantId() != null) {
            this.tenantId = camera.getTenantId().getId();
        }
        if (camera.getCustomerId() != null) {
            this.customerId = camera.getCustomerId().getId();
        }

        this.name = camera.getName();
        this.cameraType = camera.getCameraType();
        this.model =camera.getModel();
        this.ip = camera.getIp();
        this.additionalInfo = camera.getAdditionalInfo();
    }

    public AbstractCameraEntity(CameraEntity deviceEntity) {
        this.setId(deviceEntity.getId());
        this.setCreatedTime(deviceEntity.getCreatedTime());
        this.tenantId = deviceEntity.getTenantId();
        this.customerId = deviceEntity.getCustomerId();
        this.name = deviceEntity.getName();
        this.cameraType = deviceEntity.getCameraType();
        this.model = deviceEntity.getModel();
        this.ip = deviceEntity.getIp();

        this.additionalInfo = deviceEntity.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }



    protected Camera toCamera() {
        Camera camera = new Camera(new CameraId(getUuid()));
        camera.setCreatedTime(createdTime);
        if (tenantId != null) {
            camera.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            camera.setCustomerId(new CustomerId(customerId));
        }
        camera.setName(name);
        camera.setCameraType(cameraType);
        camera.setModel(model);
        camera.setIp(ip);
        camera.setAdditionalInfo(additionalInfo);
        return camera;
    }

}
