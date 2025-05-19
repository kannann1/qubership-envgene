/*
 * Copyright 2024-2025 NetCracker Technology Corporation
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

package org.qubership.cloud.devops.cli.service.implementation;

import org.qubership.cloud.devops.cli.pojo.dto.input.InputData;
import org.qubership.cloud.devops.commons.pojo.tenants.dto.TenantDTO;
import org.qubership.cloud.devops.commons.pojo.tenants.model.Tenant;
import org.qubership.cloud.devops.commons.service.interfaces.TenantConfigurationService;
import org.qubership.cloud.devops.commons.utils.convert.PojoConverterUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TenantConfigurationServiceCliImpl implements TenantConfigurationService {

    private final InputData inputData;
    @Inject
    public TenantConfigurationServiceCliImpl(InputData inputData) {
        this.inputData = inputData;
    }

    @Override
    public Tenant getTenantByName(String tenantName) {
        TenantDTO tenantDTO = inputData.getTenantDTO();
        return PojoConverterUtils.convertDTOToTenant(tenantDTO);
    }

}
