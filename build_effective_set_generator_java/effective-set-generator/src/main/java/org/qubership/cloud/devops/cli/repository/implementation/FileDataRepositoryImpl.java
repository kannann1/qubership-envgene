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

package org.qubership.cloud.devops.cli.repository.implementation;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.json.JSONArray;
import org.json.JSONObject;
import org.qubership.cloud.devops.cli.constants.GenericConstants;
import org.qubership.cloud.devops.cli.exceptions.constants.ExceptionMessage;
import org.qubership.cloud.devops.cli.pojo.dto.input.InputData;
import org.qubership.cloud.devops.cli.pojo.dto.sd.SBApplicationDTO;
import org.qubership.cloud.devops.cli.pojo.dto.sd.SolutionBomDTO;
import org.qubership.cloud.devops.cli.pojo.dto.shared.SharedData;
import org.qubership.cloud.devops.cli.utils.FileSystemUtils;
import org.qubership.cloud.devops.commons.exceptions.FileParseException;
import org.qubership.cloud.devops.commons.pojo.applications.dto.ApplicationLinkDTO;
import org.qubership.cloud.devops.commons.pojo.clouds.dto.CloudDTO;
import org.qubership.cloud.devops.commons.pojo.consumer.ConsumerDTO;
import org.qubership.cloud.devops.commons.pojo.consumer.Property;
import org.qubership.cloud.devops.commons.pojo.credentials.dto.CredentialDTO;
import org.qubership.cloud.devops.commons.pojo.cs.CompositeStructureDTO;
import org.qubership.cloud.devops.commons.pojo.namespaces.dto.NamespaceDTO;
import org.qubership.cloud.devops.commons.pojo.profile.dto.ProfileFullDto;
import org.qubership.cloud.devops.commons.pojo.registries.dto.RegistryDTO;
import org.qubership.cloud.devops.commons.pojo.tenants.dto.TenantDTO;
import org.qubership.cloud.devops.commons.repository.interfaces.FileDataConverter;
import org.qubership.cloud.devops.commons.repository.interfaces.FileDataRepository;
import org.qubership.cloud.devops.commons.utils.BomReaderUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import static org.qubership.cloud.devops.cli.constants.GenericConstants.*;


@ApplicationScoped
public class FileDataRepositoryImpl implements FileDataRepository {
    private final FileDataConverter fileDataConverter;
    private final InputData inputData;
    private final String sourceDir;
    private final SharedData sharedData;

    private final BomReaderUtils bomReaderUtils;
    private final FileSystemUtils fileSystemUtils;


    @Inject
    public FileDataRepositoryImpl(FileDataConverter fileDataConverter,
                                  SharedData sharedData,
                                  InputData inputData,
                                  BomReaderUtils bomReaderUtils,
                                  FileSystemUtils fileSystemUtils) {
        this.fileDataConverter = fileDataConverter;
        this.inputData = inputData;
        this.sharedData = sharedData;
        this.bomReaderUtils = bomReaderUtils;
        this.fileSystemUtils = fileSystemUtils;
        this.sourceDir = String.format("%s/%s", sharedData.getEnvsPath(), sharedData.getEnvId());
    }

    /*  **
     * Reads files from given environment folder in remote repo.
     *
     * @param nsWithAppsFromSD    A map where key is namespace name and value as only applications populated based
     *                            on solution sbom.                           .
     * @param profilesMap         A map where profile name is key and profile dto as value read from Profiles folder.
     * @param namespaceMap        A map where namespace name is key and value as namespace dto read from Namespaces folder.
     *                            Populates only the namespace that are part of solution sbom.
     * @param cloudApps           List of applications from Applications folder that are part of solution sbom.
     * @param appsToProcess       List of application names that are part of solution sbom.
     * @param appsOnNamespace     List of applications that should be linked to namespace
     *                            from Namespaces/{ns}/Applications folder that are part of solution sbom.
     **/

    @Override
    public void prepareProcessingEnv() {
        try {
            Map<String, List<String>> nsWithAppsFromSD = new HashMap<>();
            Set<String> appsToProcess = new HashSet<>();
            loadSDData(nsWithAppsFromSD, appsToProcess);
            SolutionBomDTO solutionDescriptor = inputData.getSolutionBomDTO();
            loadRegistryData();
            loadConsumerData();
            traverseSourceDirectory(nsWithAppsFromSD, appsToProcess);
            fileSystemUtils.createEffectiveSetFolder(solutionDescriptor.getApplications());
        } catch (Exception e) {
            throw new FileParseException("Error preparing data due to " + e.getMessage());
        }

    }

    private void loadConsumerData() {
        Map<String, ConsumerDTO> consumerDTOMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(sharedData.getPcsspPaths())) {
            sharedData.getPcsspPaths().forEach(path -> {
                try {
                    String name = FilenameUtils.getBaseName(path);
                    ConsumerDTO consumerDTO = ConsumerDTO.builder().build();
                    List<Property> properties = new ArrayList<>();
                    String jsonContent = new String(Files.readAllBytes(Paths.get(path)));
                    JSONObject consumerObject = new JSONObject(jsonContent);
                    JSONArray requiredFieldArray = (JSONArray) consumerObject.get("required");
                    List<String> requiredFields = requiredFieldArray.toList().stream().map(Object::toString).collect(Collectors.toList());
                    JSONObject propertiesObject = (JSONObject) consumerObject.get("properties");
                    Map<String, Object> propertiesObjectMap = propertiesObject.toMap();
                    propertiesObjectMap.forEach((key, value) -> {
                        Property property = Property.builder().build();
                        property.setName(key);
                        property.setRequired(requiredFields.contains(key));
                        Map<String, String> map = (Map<String, String>) value;
                        property.setValue(map.get("default"));
                        property.setType(map.get("type"));
                        properties.add(property);
                    });
                    consumerDTO.setProperties(properties);
                    consumerDTOMap.put(name, consumerDTO);
                } catch (IOException e) {
                    throw new FileParseException(String.format(ExceptionMessage.FILE_READ_ERROR, path, e.getMessage()));
                }
            });
        }
        inputData.setConsumerDTOMap(consumerDTOMap);
    }

    private void traverseSourceDirectory(Map<String, List<String>> nsWithAppsFromSD, Set<String> appsToProcess) {
        Map<String, ProfileFullDto> profilesMap = new HashMap<>();
        Map<String, NamespaceDTO> namespaceMap = new HashMap<>();
        List<ApplicationLinkDTO> cloudApps = new ArrayList<>();
        Map<String, List<ApplicationLinkDTO>> appsOnNamespace = new HashMap<>();
        Path basePath = Paths.get(sourceDir);
        Set<String> foldersToVisit = Set.of(NS_FOLDER, APPS_FOLDER, CREDS_FOLDER, PROFILES_FOLDER, basePath.getFileName().toString());
        try {
            Files.walkFileTree(basePath, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getParent().getFileName().toString().equals(GenericConstants.NS_FOLDER) &&
                            !nsWithAppsFromSD.containsKey(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    if (!dir.getParent().getFileName().toString().equals(GenericConstants.NS_FOLDER)
                            && !foldersToVisit.contains(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(GenericConstants.YAML_EXT) || file.toString().endsWith(GenericConstants.YML_EXT)) {
                        handleYamlFile(file, profilesMap, namespaceMap, appsOnNamespace, nsWithAppsFromSD, cloudApps, appsToProcess);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    String currentFolder = dir.getFileName().toString();
                    if (currentFolder.equals(GenericConstants.PROFILES_FOLDER)) {
                        inputData.setProfileFullDtoMap(profilesMap);

                    } else if (currentFolder.equals(GenericConstants.NS_FOLDER)) {
                        List<ApplicationLinkDTO> applicationLinkDTOList = new ArrayList<>();
                        namespaceMap.replaceAll((name, namespaceDTO) -> {
                            List<ApplicationLinkDTO> applications = appsOnNamespace.get(name);
                            applicationLinkDTOList.addAll(applications);
                            return namespaceDTO.toBuilder().applications(applications).build();
                        });
                        inputData.setApplicationLinkDTOMap(applicationLinkDTOList.stream().
                                collect(Collectors.toMap(ApplicationLinkDTO::getName, applicationLinkDTO -> applicationLinkDTO)));
                        inputData.setNamespaceDTOMap(namespaceMap);

                    } else if (currentFolder.equals(basePath.getFileName().toString())) {
                        inputData.setCloudDTO(inputData.getCloudDTO().toBuilder().applications(cloudApps).build());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            throw new FileParseException("Failure in reading input Directory", e);
        }
    }

    private void handleYamlFile(Path file, Map<String, ProfileFullDto> profilesMap, Map<String, NamespaceDTO> namespaceMap,
                                Map<String, List<ApplicationLinkDTO>> appsOnNamespace, Map<String, List<String>> nsWithAppsFromSD,
                                List<ApplicationLinkDTO> cloudApps, Set<String> appsToProcess) {
        Path parent = file.getParent();
        String name = file.getFileName().toString().split("\\.")[0];

        switch (name) {
            case "tenant":
                TenantDTO tenantDTO = fileDataConverter.parseInputFile(TenantDTO.class, file.toFile());
                inputData.setTenantDTO(tenantDTO);
                break;
            case "cloud":
                CloudDTO cloudDTO = fileDataConverter.parseInputFile(CloudDTO.class, file.toFile());
                inputData.setCloudDTO(cloudDTO);
                break;
            case "namespace":
                String namespace = parent.getFileName().toString();
                NamespaceDTO namespaceDTO = fileDataConverter.parseInputFile(NamespaceDTO.class, file.toFile());
                namespaceMap.putIfAbsent(namespace, namespaceDTO);
                break;
            case "credentials":
                TypeReference<Map<String, CredentialDTO>> typeReference = new TypeReference<>() {
                };
                Map<String, CredentialDTO> credentialDTOMap = fileDataConverter.parseInputFile(typeReference, file.toFile());
                if (credentialDTOMap != null) {
                    credentialDTOMap.replaceAll((id, cred) ->
                            CredentialDTO.builder().credentialsId(id)
                                    .data(cred.getData()).description(cred.getDescription()).build());
                    inputData.setCredentialDTOMap(credentialDTOMap);
                }
                break;
            case "composite_structure":
                CompositeStructureDTO compositeStructureDTO = fileDataConverter.parseInputFile(CompositeStructureDTO.class, file.toFile());
                inputData.setCompositeStructureMap(getObjectMap(compositeStructureDTO));
                break;
            default:
                processOtherFiles(file, parent, profilesMap, appsOnNamespace, nsWithAppsFromSD, cloudApps, appsToProcess);
                break;
        }
    }

    private void processOtherFiles(Path file, Path parent, Map<String, ProfileFullDto> profilesMap,
                                   Map<String, List<ApplicationLinkDTO>> appsOnNamespace, Map<String, List<String>> nsWithAppsFromSD
            , List<ApplicationLinkDTO> cloudApps, Set<String> appsToProcess) {

        String folderName = parent.getFileName().toString();
        if (folderName.equals(GenericConstants.PROFILES_FOLDER)) {
            ProfileFullDto profileFullDto = fileDataConverter.parseInputFile(ProfileFullDto.class, file.toFile());
            profileFullDto.setApplications(profileFullDto.getApplications().stream()
                    .filter(app -> appsToProcess.contains(app.getName())).collect(Collectors.toList()));
            profilesMap.putIfAbsent(profileFullDto.getName(), profileFullDto);

        } else if (folderName.equals(GenericConstants.APPS_FOLDER)) {
            processApplicationFiles(file, parent, appsOnNamespace, nsWithAppsFromSD, cloudApps, appsToProcess);
        }
    }

    private void processApplicationFiles(Path file, Path parent, Map<String, List<ApplicationLinkDTO>> appsOnNamespace,
                                         Map<String, List<String>> nsWithAppsFromSD, List<ApplicationLinkDTO> cloudApps,
                                         Set<String> appsToProcess) {

        ApplicationLinkDTO applicationLinkDTO = fileDataConverter.parseInputFile(ApplicationLinkDTO.class, file.toFile());
        if (parent.getParent().getParent().getFileName().toString().equals(GenericConstants.NS_FOLDER)) {
            String namespace = parent.getParent().getFileName().toString();
            if (checkIfAppValid(namespace, file.getFileName().toString().split("\\.")[0], nsWithAppsFromSD)) {
                appsOnNamespace.computeIfAbsent(namespace, k -> new ArrayList<>()).add(applicationLinkDTO);
            }
        } else {
            if (appsToProcess.contains(applicationLinkDTO.getName())) cloudApps.add(applicationLinkDTO);
        }
    }

    private boolean checkIfAppValid(String namespace, String app, Map<String, List<String>> nsWithAppsFromSD) {
        List<String> sdApps = nsWithAppsFromSD.get(namespace);
        return sdApps != null && sdApps.contains(app);
    }

    private void loadSDData(Map<String, List<String>> nsWithAppsFromSD, Set<String> appsToProcess) {
        Bom bomContent = fileDataConverter.parseInputFile(Bom.class, new File(sharedData.getSolsbomPath()));
        List<SBApplicationDTO> applications = bomContent.getComponents().stream()
                .filter(component -> "application".equals(component.getType().getTypeName()))
                .map(component -> {
                    return getSbApplicationDTO(nsWithAppsFromSD, appsToProcess, component);
                }).collect(Collectors.toList());

        inputData.setSolutionBomDTO(SolutionBomDTO.builder().applications(applications).build());
    }

    private SBApplicationDTO getSbApplicationDTO(Map<String, List<String>> nsWithAppsFromSD, Set<String> appsToProcess, Component component) {
        String namespace = bomReaderUtils.getPropertyValue(component, "deployPostfix");
        String appFileRef = String.format("%s/%s", sharedData.getSbomsPath(),
                bomReaderUtils.getExternalRefValue(component, "bom").replace("file://", ""));
        SBApplicationDTO dto = SBApplicationDTO.builder()
                .appName(component.getName())
                .appVersion(component.getVersion())
                .namespace(namespace)
                .appFileRef(appFileRef)
                .build();
        appsToProcess.add(dto.getAppName());
        nsWithAppsFromSD.computeIfAbsent(namespace, k -> new ArrayList<>()).add(dto.getAppName());
        return dto;
    }

    private void loadRegistryData() {
        Map<String, RegistryDTO> registries = fileDataConverter.parseInputFile(new TypeReference<HashMap<String, RegistryDTO>>() {
        }, new File(sharedData.getRegistryPath()));
        inputData.setRegistryDTOMap(registries);
    }

    public Map<String, Object> getObjectMap(CompositeStructureDTO compositeStructureDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper
                .convertValue(compositeStructureDTO, new TypeReference<Map<String, Object>>() {
                });
    }


}
