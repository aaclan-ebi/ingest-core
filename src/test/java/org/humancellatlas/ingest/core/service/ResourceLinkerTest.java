package org.humancellatlas.ingest.core.service;


import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.file.web.FileController;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.web.ProcessController;
import org.humancellatlas.ingest.state.ValidationState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.EntityLinks;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


/**
 * Created by rolando on 11/06/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8088")
public class ResourceLinkerTest {
    @Autowired ResourceLinker resourceLinker;
    @Autowired ProcessRepository processRepository;
    @Autowired FileRepository fileRepository;

    @Test
    public void testAddRefToList() {
        HttpServletRequest httpServletRequestMock = new MockHttpServletRequest();
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequestMock);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        // try link a Process to a File through the inputFiles link
        // TODO: use generic mock HTTP resources here
        Process process = new Process();
        process.setValidationState(ValidationState.DRAFT);
        process = processRepository.save(process);

        File file = new File();
        file.setValidationState(ValidationState.DRAFT);
        file = fileRepository.save(file);

        assertThat(fileRepository.findOne(file.getId()).getDerivedByProcesses().size() == 0);

        resourceLinker.addToRefList(file, process, "derivedByProcesses");

        assertThat(fileRepository.findOne(file.getId()).getDerivedByProcesses().size() > 0);
    }

    @Test
    public void testAddRefToList_DoesNotTriggerOptimisticLock() {
        HttpServletRequest httpServletRequestMock = new MockHttpServletRequest();
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequestMock);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        // TODO: use generic mock HTTP resources here
        Process process = new Process();
        process.setValidationState(ValidationState.DRAFT);
        process = processRepository.save(process);

        File file = new File();
        file.setValidationState(ValidationState.DRAFT);
        file = fileRepository.save(file);

        // let's modify the File/Processes through mongo and then try to link
        long fileVersion = file.getVersion();
        file.setFileName("mock-file-name");
        fileRepository.save(file);
        assertThat(fileRepository.findOne(file.getId()).getVersion() != fileVersion);

        long processVersion = file.getVersion();
        process.setValidationState(ValidationState.INVALID);
        processRepository.save(process);
        assertThat(processRepository.findOne(process.getId()).getVersion() != processVersion);

        try {
            resourceLinker.addToRefList(file, process, "derivedByProcesses");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}