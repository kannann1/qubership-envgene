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

package org.qubership.cloud.devops.commons.pojo.profile.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationProfile implements Serializable {

    private static final long serialVersionUID = -3488224997958951786L;
    private String name;
    private String version;
    private String sd;
    @JsonManagedReference
    private List<ServiceProfile> services;
    @JsonBackReference
    private Profile profile;

    @Override
    public String toString() {
        return "ApplicationProfile{" +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", sd='" + sd + '\'' +
                ", services=" + services +
                '}';
    }
}
