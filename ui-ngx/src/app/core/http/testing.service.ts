///
/// Copyright © 2016-2021 The Thingsboard Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Injectable } from '@angular/core';
import { defaultHttpOptionsFromConfig, RequestConfig } from './http-utils';
import { Observable, ReplaySubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { PageLink } from '@shared/models/page/page-link';
import { PageData } from '@shared/models/page/page-data';
import {
  ClaimRequest,
  ClaimResult,
  Testing,
  TestingCredentials,
  TestingInfo,
  DeviceSearchQuery
} from '@app/shared/models/testing.models';
import { EntitySubtype } from '@app/shared/models/entity-type.models';
import { AuthService } from '@core/auth/auth.service';
import { TestingRoutingModule } from '@app/modules/home/pages/testing/testing-routing.module';

@Injectable({
  providedIn: 'root'
})
export class TestingService {

  testingInfo:TestingInfo;
  constructor(
    private http: HttpClient
  ) {
   }
  

  //ktu beji ndryshimet

  public getTenantDeviceInfos(pageLink: PageLink, type: string = '',
                              config?: RequestConfig): Observable<PageData<TestingInfo>> {
    return this.http.get<PageData<TestingInfo>>(`/api/tenant/cameras${pageLink.toQuery()}&type=${type}`,
      defaultHttpOptionsFromConfig(config));
  }

  public getTenantDeviceInfos_(pageLink: PageLink,
  config?: RequestConfig): Observable<PageData<TestingInfo>> {
return this.http.get<PageData<TestingInfo>>(`/api/tenant/deviceInfos${pageLink.toQuery()}`,
defaultHttpOptionsFromConfig(config));
}
  public getTesting(testingId: string, config?: RequestConfig): Observable<Testing> {
    return this.http.get<Testing>(`/api/camera/${testingId}`, defaultHttpOptionsFromConfig(config));
  }

  public getTestings(testingIds: Array<string>, config?: RequestConfig): Observable<Array<Testing>> {
    return this.http.get<Array<Testing>>(`/api/cameras?camerasIds=${testingIds.join(',')}`, defaultHttpOptionsFromConfig(config));
  }

  public getTestingInfo(testingId: string, config?: RequestConfig): Observable<TestingInfo> {
    return  this.http.get<TestingInfo>(`/api/camera/${testingId}`, defaultHttpOptionsFromConfig(config));

    //return this.http.get<TestingInfo>(`/api/testing/info/${testingId}`, defaultHttpOptionsFromConfig(config));
  }

  public saveTesting(testing: Testing, config?: RequestConfig): Observable<Testing> {
    return this.http.post<Testing>('/api/camera', testing, defaultHttpOptionsFromConfig(config));
  }

  public deleteDevice(deviceId: string, config?: RequestConfig) {
    return this.http.delete(`/api/camera/${deviceId}`, defaultHttpOptionsFromConfig(config));
  }

  public getDeviceTypes(config?: RequestConfig): Observable<Array<EntitySubtype>> {
    return this.http.get<Array<EntitySubtype>>('/api/device/types', defaultHttpOptionsFromConfig(config));
  }

}
