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

package org.qubership.cloud.devops.cli.pojo.dto.input;


import org.qubership.cloud.devops.cli.pojo.dto.sd.SolutionBomDTO;
import org.qubership.cloud.devops.commons.pojo.applications.dto.ApplicationLinkDTO;
import org.qubership.cloud.devops.commons.pojo.clouds.dto.CloudDTO;
import org.qubership.cloud.devops.commons.pojo.consumer.ConsumerDTO;
import org.qubership.cloud.devops.commons.pojo.credentials.dto.CredentialDTO;
import org.qubership.cloud.devops.commons.pojo.namespaces.dto.NamespaceDTO;
import org.qubership.cloud.devops.commons.pojo.registries.dto.RegistryDTO;
import org.qubership.cloud.devops.commons.pojo.tenants.dto.TenantDTO;
import org.qubership.cloud.devops.commons.pojo.profile.dto.ProfileFullDto;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@ApplicationScoped
public class InputData {

    private TenantDTO tenantDTO;
    private CloudDTO cloudDTO;
    @Builder.Default
    private Map<String, Object> compositeStructureMap = Collections.emptyMap();
    @Builder.Default
    private Map<String, ConsumerDTO> consumerDTOMap = Collections.emptyMap();
    @Builder.Default
    private Map<String, NamespaceDTO> namespaceDTOMap = Collections.emptyMap();
    @Builder.Default
    private Map<String, CredentialDTO> credentialDTOMap = Collections.emptyMap();
    @Builder.Default
    private Map<String, ProfileFullDto> profileFullDtoMap = Collections.emptyMap();
    @Builder.Default
    private Map<String, ApplicationLinkDTO> applicationLinkDTOMap = Collections.emptyMap();
    @Builder.Default
    private Map<String, RegistryDTO> registryDTOMap = Collections.emptyMap();
    private SolutionBomDTO solutionBomDTO;
}
