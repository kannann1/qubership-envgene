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

package org.qubership.cloud.devops.commons.pojo.bom;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.qubership.cloud.devops.commons.pojo.registries.dto.RegistrySummaryDTO;

import java.util.Map;

@Data
@Builder
public class ServiceBomDTO {
    private String name;
    private String gitBranch;
    private String imageType;
    private String imageName;
    private String dockerTag;
    private String dockerRepoName;
    private RegistrySummaryDTO registry;
    private boolean isFacadeGateway;
    private Map<String, String> parameters;
}
