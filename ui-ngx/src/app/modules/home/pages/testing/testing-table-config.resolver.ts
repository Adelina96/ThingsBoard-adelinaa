/*
Copyright © 2016-2021 The Thingsboard Authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import { Injectable } from '@angular/core';

import { ActivatedRouteSnapshot, Resolve, Router } from '@angular/router';
import {
  checkBoxCell,
  DateEntityTableColumn,
  EntityTableColumn,
  EntityTableConfig,
  HeaderActionDescriptor
} from '@home/models/entity/entities-table-config.models';
import { TranslateService } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { EntityType, entityTypeResources, entityTypeTranslations } from '@shared/models/entity-type.models';
import { AddEntityDialogData, EntityAction } from '@home/models/entity/entity-component.models';
import { TestingComponent } from '@modules/home/pages/testing/testing.component';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import { select, Store } from '@ngrx/store';
import { selectAuthUser } from '@core/auth/auth.selectors';
import { map, mergeMap, take, tap } from 'rxjs/operators';
import { AppState } from '@core/core.state';
import { Authority } from '@app/shared/models/authority.enum';
import { CustomerService } from '@core/http/customer.service';
import { Customer } from '@app/shared/models/customer.model';
import { NULL_UUID } from '@shared/models/id/has-uuid';
import { BroadcastService } from '@core/services/broadcast.service';
import { TestingTableHeaderComponent } from '@modules/home/pages/testing/testing-table-header.component';
import { MatDialog } from '@angular/material/dialog';
import { DialogService } from '@core/services/dialog.service';

import {
  AddEntitiesToCustomerDialogComponent,
  AddEntitiesToCustomerDialogData
} from '../../dialogs/add-entities-to-customer-dialog.component';
import { TestingTabsComponent } from '@home/pages/testing/testing-tabs.component';
import { HomeDialogsService } from '@home/dialogs/home-dialogs.service';
import { BaseData, HasId } from '@shared/models/base-data';
import { isDefinedAndNotNull } from '@core/utils';
import { TestingInfo } from '@app/shared/models/testing.models';
import { TestingService } from '@app/core/http/testing.service';
import { TestingWizardDialogComponent } from '../../components/wizard/testing-wizard-dialog.component';

@Injectable()
export class TestingTableConfigResolver implements Resolve<EntityTableConfig<TestingInfo>> {

  private readonly config: EntityTableConfig<TestingInfo> = new EntityTableConfig<TestingInfo>();

  private customerId: string;

  constructor(private store: Store<AppState>,
              private broadcast: BroadcastService,
              private testingService: TestingService,
              private customerService: CustomerService,
              private dialogService: DialogService,
              private homeDialogs: HomeDialogsService,
              private translate: TranslateService,
              private datePipe: DatePipe,
              private router: Router,
              private dialog: MatDialog) {

    this.config.entityType = EntityType.TESTING;
    this.config.entityComponent = TestingComponent;
    this.config.entityTabsComponent = TestingTabsComponent;
    this.config.entityTranslations = entityTypeTranslations.get(EntityType.TESTING);
    this.config.entityResources = entityTypeResources.get(EntityType.TESTING);

    this.config.addDialogStyle = {width: '600px'};

    this.config.deleteEntityTitle = device => this.translate.instant('device.delete-device-title', { deviceName: device.name });
    this.config.deleteEntityContent = () => this.translate.instant('device.delete-device-text');
    this.config.deleteEntitiesTitle = count => this.translate.instant('device.delete-devices-title', {count});
    this.config.deleteEntitiesContent = () => this.translate.instant('device.delete-devices-text');

    this.config.loadEntity = id => this.testingService.getTestingInfo(id.id);
    this.config.saveEntity = testing => {
      return this.testingService.saveTesting(testing).pipe(
        tap(() => {
          this.broadcast.broadcast('deviceSaved');
        }),
        mergeMap((savedTesting) => this.testingService.getTestingInfo(savedTesting.id.id)

      ));

    };
    //this.config.onEntityAction = action => this.onDeviceAction(action);
    this.config.detailsReadonly = () => this.config.componentsData.deviceScope === 'customer_user';

    this.config.headerComponent = TestingTableHeaderComponent;

  }

  resolve(route: ActivatedRouteSnapshot): Observable<EntityTableConfig<TestingInfo>> {
    const routeParams = route.params;
   

    this.config.componentsData = {
      deviceScope: route.data.devicesType,
    };
    this.customerId = routeParams.customerId;
    return this.store.pipe(select(selectAuthUser), take(1)).pipe(
      tap((authUser) => {
        if (authUser.authority === Authority.CUSTOMER_USER) {
          this.config.componentsData.deviceScope = 'customer_user';
          this.customerId = authUser.customerId;
        }
      }),
      mergeMap(() =>
        this.customerId ? this.customerService.getCustomer(this.customerId) : of(null as Customer)
      ),
      map((parentCustomer) => {
        if (parentCustomer) {
          if (parentCustomer.additionalInfo && parentCustomer.additionalInfo.isPublic) {
            this.config.tableTitle = this.translate.instant('customer.public-devices'); //Public Devices
          } else {
            this.config.tableTitle = parentCustomer.title + ': ' + this.translate.instant('testingObj.testing');
          }
        } else {
          this.config.tableTitle = this.translate.instant('testingObj.testing');
        }
        this.config.columns = this.configureColumns(this.config.componentsData.deviceScope);
        this.configureEntityFunctions(this.config.componentsData.deviceScope);
        this.config.addActionDescriptors = this.configureAddActions(this.config.componentsData.deviceScope);
        this.config.addEnabled = this.config.componentsData.deviceScope !== 'customer_user';
        this.config.entitiesDeleteEnabled = this.config.componentsData.deviceScope === 'tenant';
        this.config.deleteEnabled = () => this.config.componentsData.deviceScope === 'tenant';
        return this.config;
      })
    );
  }

  configureColumns(deviceScope: string): Array<EntityTableColumn<TestingInfo>> {
    const columns: Array<EntityTableColumn<TestingInfo>> = [
      new DateEntityTableColumn<TestingInfo>('createdTime', 'common.created-time', this.datePipe, '150px'),
      new EntityTableColumn<TestingInfo>('name', 'device.name', '25%'),
      new EntityTableColumn<TestingInfo>('cameraType', 'Camera Type', '25%'),
      new EntityTableColumn<TestingInfo>('model', 'Model', '25%'),
      new EntityTableColumn<TestingInfo>('ip', 'Ip', '25%'),
    ];
    return columns;
  }

  configureEntityFunctions(deviceScope: string): void {
    if (deviceScope === 'tenant') {
      this.config.entitiesFetchFunction = pageLink =>
        this.testingService.getTenantDeviceInfos(pageLink);
      this.config.deleteEntity = id => this.testingService.deleteDevice(id.id);
    }
  }


  configureAddActions(deviceScope: string): Array<HeaderActionDescriptor> {
    const actions: Array<HeaderActionDescriptor> = [];
    // Add new device wizard is opened
    if (deviceScope === 'tenant') {
      actions.push(
        {
          name: this.translate.instant('testingObj.add-testing-text'),
          icon: 'add',
          isEnabled: () => true,
          onAction: ($event) => this.deviceWizard($event)
         }
      );
    }
    
    return actions;
  }


  deviceWizard($event: Event) {
    this.dialog.open<TestingWizardDialogComponent, AddEntityDialogData<BaseData<HasId>>,
      boolean>(TestingWizardDialogComponent, {
      disableClose: true,
      panelClass: ['tb-dialog', 'tb-fullscreen-dialog'],
      data: {
        entitiesTableConfig: this.config.table.entitiesTableConfig
      }
    }).afterClosed().subscribe(
      (res) => {
        if (res) {
          this.config.table.updateData();
        }
      }
    );
  }

}
