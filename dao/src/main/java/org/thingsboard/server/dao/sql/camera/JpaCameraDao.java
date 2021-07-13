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
package org.thingsboard.server.dao.sql.camera;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.camera.CameraDao;
import org.thingsboard.server.dao.model.sql.CameraEntity;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;
import java.util.UUID;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
@Component
public class JpaCameraDao extends JpaAbstractSearchTextDao<CameraEntity, Camera> implements CameraDao {

    @Autowired
    private CameraRepository cameraRepository;

    @Override
    protected Class<CameraEntity> getEntityClass() {
        return CameraEntity.class;
    }

    @Override
    protected CrudRepository<CameraEntity, UUID> getCrudRepository() {
        return cameraRepository;
    }



    @Override
    public Long countByTenantId(TenantId tenantId) {
        return cameraRepository.countByTenantId(tenantId.getId());
    }


    @Override
    public CameraInfo findCameraInfoById(TenantId tenantId, UUID cameraId) {
        System.out.println(" "+ cameraId);
        //CameraInfo ti = DaoUtil.getData(cameraRepository.findCameraInfoById(cameraId));
        return DaoUtil.getData(cameraRepository.findCameraInfoById(cameraId));
    }

    @Override
    public PageData<Camera>findCamerasByTenantId(UUID tenantId, PageLink pageLink) {
        if (StringUtils.isEmpty(pageLink.getTextSearch())) {
            return DaoUtil.toPageData(
                    cameraRepository.findByTenantId(
                            tenantId,
                            DaoUtil.toPageable(pageLink)));
        } else {
            return DaoUtil.toPageData(
                    cameraRepository.findByTenantId(
                            tenantId,
                            DaoUtil.toPageable(pageLink)));
        }
    }

    @Override
    public PageData<Camera> findCamerasByTenantIdAndType(UUID tenantId, String sensorType, PageLink pageLink) {
        return DaoUtil.toPageData(
                cameraRepository.findByTenantIdAndType(
                        tenantId,
                        sensorType,

                        DaoUtil.toPageable(pageLink)));
    }


    @Override
    public Camera findCameraByTenantIdAndId(TenantId tenantId, UUID id) {
        return DaoUtil.getData(cameraRepository.findByTenantIdAndId(tenantId.getId(), id));
    }



}
