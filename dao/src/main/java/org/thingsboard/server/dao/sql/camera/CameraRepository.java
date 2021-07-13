package org.thingsboard.server.dao.sql.camera;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dao.model.sql.CameraEntity;
import org.thingsboard.server.dao.model.sql.CameraInfoEntity;
import java.util.UUID;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */

public interface CameraRepository extends PagingAndSortingRepository<CameraEntity, UUID> {




    @Query("SELECT new org.thingsboard.server.dao.model.sql.CameraInfoEntity(d, c.title, c.additionalInfo) " +
            "FROM CameraEntity d " +
            "LEFT JOIN CustomerEntity c on c.id = d.customerId " +
            "WHERE d.id = :cameraId")
    CameraInfoEntity findCameraInfoById(@Param("cameraId") UUID cameraId);


    @Query("SELECT d FROM CameraEntity d WHERE d.tenantId = :tenantId")
    Page<CameraEntity>findByTenantId(@Param("tenantId") UUID tenantId,
                                     Pageable pageable);





    Long countByTenantId(UUID tenantId);


    @Query("SELECT d FROM CameraEntity d WHERE d.tenantId = :tenantId " +
            "AND d.cameraType = :cameraType ")
    Page<CameraEntity> findByTenantIdAndType(@Param("tenantId") UUID tenantId,
                                             @Param("cameraType") String type,
                                             Pageable pageable);




    CameraEntity findByTenantIdAndId(UUID tenantId, UUID id);



}


