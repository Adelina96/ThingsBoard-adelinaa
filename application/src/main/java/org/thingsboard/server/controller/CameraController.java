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
package org.thingsboard.server.controller;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.rule.engine.api.msg.DeviceCredentialsUpdateNotificationMsg;
import org.thingsboard.rule.engine.api.msg.DeviceNameOrTypeUpdateMsg;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.device.DeviceSearchQuery;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgDataType;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.dao.device.claim.ClaimResponse;
import org.thingsboard.server.dao.device.claim.ClaimResult;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.security.permission.Resource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@TbCoreComponent
@RequestMapping("/api")
public class CameraController  extends BaseController {

    private static final String TESTING_ID = "cameraId";
    private static final String TESTING_NAME = "cameraName";
    private static final String TENANT_ID = "tenantId";


    @RequestMapping(value = "/camera", method = RequestMethod.POST)
    public Camera camera(@RequestBody Camera camera) {
        System.out.println("--------: " + camera);
        try {
            camera.setTenantId(getCurrentUser().getTenantId());
            checkEntity(camera.getId(), camera, Resource.TESTING);
            return cameraService.saveCamera(camera);
        } catch (ThingsboardException e) {
            e.printStackTrace();
        }
        System.out.println("e po po qenka kshu kjo");
        return camera;
    }
    @RequestMapping(value = "/camera/update", method = RequestMethod.POST)
    public Camera updatecamera(@RequestBody Camera camera) {
        System.out.println("--------:UPDATE " + camera);
        try {
            camera.setTenantId(getCurrentUser().getTenantId());
            checkEntity(camera.getId(), camera, Resource.DEVICE);
            return cameraService.saveCamera(camera);
        } catch (ThingsboardException e) {
            e.printStackTrace();
        }
        System.out.println("e po po qenka kshu kjo");
        return camera;
    }



    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/cameras", params = {"pageSize", "page"}, method = RequestMethod.GET)
    @ResponseBody
    public PageData<Camera> getTenantCameras(
            @RequestParam int pageSize,
            @RequestParam int page,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            PageLink pageLink = createPageLink(pageSize, page, sensorType, sortProperty, sortOrder);
            if (sensorType != null && sensorType.trim().length() > 0) {
                return checkNotNull(cameraService.findCamerasByTenantIdAndType(tenantId, sensorType, pageLink));
            } else {
                return checkNotNull(cameraService.findCamerasByTenantId(tenantId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }



    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/info/{cameraId}", method = RequestMethod.GET)
    @ResponseBody
    public CameraInfo getCameraInfoById(@PathVariable(TESTING_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(TESTING_ID, strDeviceId);
        try {
            CameraId deviceId = new CameraId(toUUID(strDeviceId));
            System.out.println("---------: ktu jemi te camera controller: "+ toUUID(strDeviceId));
            return checkCameraInfoId(deviceId, Operation.READ);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    //@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/{cameraId}", method = RequestMethod.GET)
    @ResponseBody
    public CameraInfo getCameraById(@PathVariable(TESTING_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(TESTING_ID, strDeviceId);
        try {
            CameraId deviceId = new CameraId(toUUID(strDeviceId));
            Camera cameraObject = checkCameraId(deviceId, Operation.READ);
            return new CameraInfo(cameraObject,"",true);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/camera/{cameraId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteCamera(@PathVariable(TESTING_ID) String strCameraId) throws ThingsboardException {
        checkParameter(TESTING_ID, strCameraId);
        try {
            CameraId cameraId = new CameraId(toUUID(strCameraId));
            Camera camera = checkCameraId(cameraId, Operation.DELETE);
            cameraService.deleteCamera(getCurrentUser().getTenantId(), cameraId);

            // tbClusterService.onDeviceDeleted(camera, null);
            //   tbClusterService.onEntityStateChange(device.getTenantId(), deviceId, ComponentLifecycleEvent.DELETED);

            logEntityAction(cameraId, camera,
                    camera.getCustomerId(),
                    ActionType.DELETED, null, strCameraId);

            // deviceStateService.onDeviceDeleted(device);
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.TESTING),
                    null,
                    null,
                    ActionType.DELETED, e, strCameraId);
            throw handleException(e);
        }
    }
























}
