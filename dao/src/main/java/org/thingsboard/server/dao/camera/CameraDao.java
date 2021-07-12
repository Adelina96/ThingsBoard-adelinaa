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
package org.thingsboard.server.dao.camera;

import com.google.common.util.concurrent.ListenableFuture;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.Camera;
import org.thingsboard.server.common.data.CameraInfo;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.Dao;
import org.thingsboard.server.dao.TenantEntityDao;
import org.thingsboard.server.dao.model.sql.CameraEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CameraDao extends Dao<Camera>, TenantEntityDao {


    /**
     * Find camera info by id.
     *
     * @param tenantId the tenant id
     * @param cameraId the camera id
     * @return the camera info object
     */
    CameraInfo findCameraInfoById(TenantId tenantId, UUID cameraId);

    /**
     * Save or update camera object
     *
     * @param camera the camera object
     * @return saved camera object
     */
    Camera save(TenantId tenantId, Camera camera);

    /**
     * Find cameras by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of camera objects
     */
    PageData<Camera> findCamerasByTenantId(UUID tenantId, PageLink pageLink);

    /**
     * Find camera infos by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of camera info objects
     */
//    PageData<CameraInfo> findCameraInfosByTenantId(UUID tenantId, PageLink pageLink);

    /**
     * Find cameras by tenantId, type and page link.
     *
     * @param tenantId the tenantId
     * @param sensorType the type
     * @param pageLink the page link
     * @return the list of camera objects
     */
    PageData<Camera> findCamerasByTenantIdAndType(UUID tenantId, String sensorType, PageLink pageLink);

    /**
     * Find camera infos by tenantId, type and page link.
     *
     * @param tenantId the tenantId
     * @param type the type
     * @param pageLink the page link
     * @return the list of camera info objects
     */
//    PageData<CameraInfo> findCameraInfosByTenantIdAndType(UUID tenantId, String type, PageLink pageLink);


    /**
     * Find cameras by tenantId and cameras Ids.
     *
     * @param tenantId the tenantId
     * @param cameraIds the camera Ids
     * @return the list of camera objects
     */
//    ListenableFuture<List<Camera>> findCamerasByTenantIdAndIdsAsync(UUID tenantId, List<UUID> cameraIds);

    /**
     * Find cameras by tenantId, customerId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param pageLink the page link
     * @return the list of camera objects
     */
//    PageData<Camera> findCamerasByTenantIdAndCustomerId(UUID tenantId, UUID customerId, PageLink pageLink);

    /**
     * Find camera infos by tenantId, customerId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param pageLink the page link
     * @return the list of camera info objects
     */
//    PageData<CameraInfo> findCameraInfosByTenantIdAndCustomerId(UUID tenantId, UUID customerId, PageLink pageLink);

    /**
     * Find cameras by tenantId, customerId, type and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param type the type
     * @param pageLink the page link
     * @return the list of camera objects
     */
//    PageData<Camera> findCamerasByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, PageLink pageLink);

    /**
     * Find camera infos by tenantId, customerId, type and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param type the type
     * @param pageLink the page link
     * @return the list of camera info objects
     */
//    PageData<CameraInfo> findCameraInfosByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, PageLink pageLink);

    /**
     * Find camera infos by tenantId, customerId, cameraProfileId and page link.
     *
     * @param tenantId the tenantId
     * @param customerId the customerId
     * @param cameraProfileId the cameraProfileId
     * @param pageLink the page link
     * @return the list of camera info objects
     */
//    PageData<CameraInfo> findCameraInfosByTenantIdAndCustomerIdAndCameraProfileId(UUID tenantId, UUID customerId, UUID cameraProfileId, PageLink pageLink);



    /**
     * Find cameras by tenantId and camera name.
     *
     * @param tenantId the tenantId
     * @param name the camera name
     * @return the optional camera object
     */
//    Optional<Camera> findCameraByTenantIdAndName(UUID tenantId, String name);



    /**
     * Find cameras by tenantId and camera id.
     * @param tenantId the tenant Id
     * @param id the camera Id
     * @return the camera object
     */
    Camera findCameraByTenantIdAndId(TenantId tenantId, UUID id);



    /**
     * Find cameras by tenantId, profileId and page link.
     *
     * @param tenantId the tenantId
     * @param profileId the profileId
     * @param pageLink the page link
     * @return the list of camera objects
    //     */
//    PageData<Camera> findCamerasByTenantIdAndProfileId(UUID tenantId, UUID profileId, PageLink pageLink);

//
//    ListenableFuture<List<Camera>> findCamerasByTenantIdCustomerIdAndIdsAsync(UUID id, UUID id1, List<UUID> toUUIDs);
}
