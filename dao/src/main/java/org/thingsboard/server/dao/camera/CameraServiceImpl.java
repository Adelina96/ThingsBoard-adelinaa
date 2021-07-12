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

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.*;

import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.EntitySearchDirection;

import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.data.tenant.profile.DefaultTenantProfileConfiguration;
import org.thingsboard.server.dao.customer.CustomerDao;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.entityview.EntityViewService;
import org.thingsboard.server.dao.event.EventService;
import org.thingsboard.server.dao.exception.DataValidationException;
import org.thingsboard.server.dao.service.DataValidator;
import org.thingsboard.server.dao.service.PaginatedRemover;
import org.thingsboard.server.dao.service.Validator;
import org.thingsboard.server.dao.tenant.TbTenantProfileCache;
import org.thingsboard.server.dao.tenant.TenantDao;
import org.thingsboard.server.dao.util.mapping.JacksonUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.thingsboard.server.common.data.CacheConstants.DEVICE_CACHE;
import static org.thingsboard.server.dao.DaoUtil.toUUIDs;
import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID;
import static org.thingsboard.server.dao.service.Validator.validateId;
import static org.thingsboard.server.dao.service.Validator.validateIds;
import static org.thingsboard.server.dao.service.Validator.validatePageLink;
import static org.thingsboard.server.dao.service.Validator.validateString;

@Service
@Slf4j
public class CameraServiceImpl extends AbstractEntityService implements CameraService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_TESTING_PROFILE_ID = "Incorrect cameraProfileId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_TESTING_ID = "Incorrect cameraId ";

    @Autowired
    private CameraDao cameraDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;


    @Autowired
    private EntityViewService entityViewService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EventService eventService;

    @Autowired
    @Lazy
    private TbTenantProfileCache tenantProfileCache;




    @CacheEvict(cacheNames = DEVICE_CACHE, key = "{#camera.tenantId, #camera.name}")
    @Override
    public Camera saveCamera(Camera camera) {
        return doSaveCamera(camera, null);
    }

    private Camera doSaveCamera(Camera camera, String accessToken) {
        log.trace("Executing saveCamera [{}]", camera);
        cameraValidator.validate(camera, Camera::getTenantId);
        System.out.println("--------Do save testisng: " + camera);
        Camera savedCamera;
        try {
            System.out.println("--------  instide try Do save testisng: " + camera);
            System.out.println("--------  instide try Do save tenantId: " + camera.getTenantId());


            savedCamera = cameraDao.save(camera.getTenantId(), camera);

            System.out.println("----::: ::: ::: "+ savedCamera);
        } catch (Exception t) {
            ConstraintViolationException e = extractConstraintViolationException(t).orElse(null);
            if (e != null && e.getConstraintName() != null && e.getConstraintName().equalsIgnoreCase("camera_name_unq_key")) {
                // remove camera from cache in case null value cached in the distributed redis.
                removeCameraFromCache(camera.getTenantId(), camera.getName());
                throw new DataValidationException("Camera with such name already exists!");
            } else {
                throw t;
            }
        }

        return savedCamera;
    }

    private void removeCameraFromCache(TenantId tenantId, String name) {
        List<Object> list = new ArrayList<>();
        list.add(tenantId);
        list.add(name);
        Cache cache = cacheManager.getCache(DEVICE_CACHE);
        cache.evict(list);
    }


    private DataValidator<Camera> cameraValidator =
            new DataValidator<Camera>() {

                @Override
                protected void validateCreate(TenantId tenantId, Camera camera) {
                    DefaultTenantProfileConfiguration profileConfiguration =
                            (DefaultTenantProfileConfiguration)tenantProfileCache.get(tenantId).getProfileData().getConfiguration();
                    //long maxCameras = profileConfiguration.getMaxCameras();
                    //validateNumberOfEntitiesPerTenant(tenantId, cameraDao, maxCameras, EntityType.DEVICE);
                }

            };




    @Override
    public PageData<Camera>findCamerasByTenantId(TenantId tenantId, PageLink pageLink) {
        log.trace("Executing findDevicesByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink);
        return cameraDao.findCamerasByTenantId(tenantId.getId(), pageLink);
    }



    @Override
    public PageData<Camera> findCamerasByTenantIdAndType(TenantId tenantId, String sensorType, PageLink pageLink) {
        log.trace("Executing findCamerassByTenantIdAndSensorType, tenantId [{}], sensorType [{}], pageLink [{}]", tenantId, sensorType, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateString(sensorType, "Incorrect type " + sensorType);
        validatePageLink(pageLink);
        return cameraDao.findCamerasByTenantIdAndType(tenantId.getId(), sensorType, pageLink);
    }




    @Override
    public Camera findCameraById(TenantId tenantId, CameraId cameraId) {
        log.trace("Executing findDeviceById [{}]", cameraId);
        validateId(cameraId, INCORRECT_TESTING_ID + cameraId);
        if (TenantId.SYS_TENANT_ID.equals(tenantId)) {
            return cameraDao.findById(tenantId, cameraId.getId());
        } else {
            return cameraDao.findCameraByTenantIdAndId(tenantId, cameraId.getId());
        }
    }

    @Override
    public CameraInfo findCameraInfoById(TenantId tenantId, CameraId deviceId) {
        log.trace("Executing findDeviceInfoById [{}]", deviceId);
        validateId(deviceId, INCORRECT_TESTING_ID + deviceId);
        return cameraDao.findCameraInfoById(tenantId, deviceId.getId());
    }


    @Override
    public void deleteCamera(TenantId tenantId, CameraId cameraId) {
        log.trace("Executing deleteDevice [{}]", cameraId);
        validateId(cameraId, INCORRECT_TESTING_ID + cameraId);

        Camera camera = cameraDao.findById(tenantId, cameraId.getId());
        try {
            List<EntityView> entityViews = entityViewService.findEntityViewsByTenantIdAndEntityIdAsync(camera.getTenantId(), cameraId).get();
            if (entityViews != null && !entityViews.isEmpty()) {
                throw new DataValidationException("Can't delete device that has entity views!");
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while finding entity views for deviceId [{}]", cameraId, e);
            throw new RuntimeException("Exception while finding entity views for deviceId [" + cameraId + "]", e);
        }

        //   DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(tenantId, deviceId);
        //   if (deviceCredentials != null) {
        //     deviceCredentialsService.deleteDeviceCredentials(tenantId, deviceCredentials);
        // }
        deleteEntityRelations(tenantId, cameraId);

        removeCameraFromCache(tenantId, camera.getName());

        cameraDao.removeById(tenantId, cameraId.getId());
    }

//    @Override
//    public void deleteDashboard(TenantId tenantId, DashboardId dashboardId) {
//        log.trace("Executing deleteDashboard [{}]", dashboardId);
//        Validator.validateId(dashboardId, INCORRECT_DASHBOARD_ID + dashboardId);
//        deleteEntityRelations(tenantId, dashboardId);
//        dashboardDao.removeById(tenantId, dashboardId.getId());
//    }















}
