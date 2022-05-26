/*
 * Copyright 2021 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.streamis.jobmanager.restful.api;

import com.webank.wedatasphere.streamis.jobmanager.launcher.entity.JobConfDefinition;
import com.webank.wedatasphere.streamis.jobmanager.launcher.entity.vo.JobConfDefinitionVo;
import com.webank.wedatasphere.streamis.jobmanager.launcher.service.StreamJobConfService;
import com.webank.wedatasphere.streamis.jobmanager.manager.exception.JobErrorException;
import com.webank.wedatasphere.streamis.jobmanager.manager.service.JobService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping(path = "/streamis/streamJobManager/config")
@RestController
public class JobConfRestfulApi {

    private static final Logger LOG = LoggerFactory.getLogger(JobConfRestfulApi.class);

    @Resource
    private StreamJobConfService streamJobConfService;

    @Resource
    private JobService jobService;
    /**
     * Definitions
     * @return message
     */
    @RequestMapping(value = "/definitions")
    public Message definitions(){
        Message result = Message.ok("success");
        try {
            List<JobConfDefinition> definitionList = this.streamJobConfService.loadAllDefinitions();
            Map<String, JobConfDefinitionVo> definitionRelation = new HashMap<>();
            definitionList.forEach(definition -> definitionRelation.put(String.valueOf(definition.getId()),
                    new JobConfDefinitionVo(definition)));
            definitionList.forEach(definition -> {
                Long parentRef = definition.getParentRef();
                if (Objects.nonNull(parentRef)) {
                    JobConfDefinitionVo definitionVo = definitionRelation.get(String.valueOf(parentRef));
                    if (Objects.nonNull(definitionVo)) {
                        List<JobConfDefinitionVo> childDef = Optional.ofNullable(definitionVo.getChildDef()).orElse(new ArrayList<>());
                        childDef.add(definitionRelation.get(String.valueOf(definition.getId())));
                        definitionVo.setChildDef(childDef);
                    }
                }
            });

            List<JobConfDefinitionVo> def =
                    definitionRelation.values().stream().filter(definitionVo -> definitionVo.getLevel() == 0)
                            .sorted((o1, o2) -> o2.getSort() - o1.getSort()).collect(Collectors.toList());
            def.forEach(definitionVo -> {
                if (Objects.isNull(definitionVo.getChildDef())){
                    definitionVo.setChildDef(Collections.emptyList());
                }
            });
            result.data("def", def);
        }catch(Exception e){
            String message = "Fail to obtain StreamJob configuration definitions(获取任务配置定义集失败), message: " + e.getMessage();
            LOG.warn(message, e);
            result = Message.error(message);
        }
        return result;
    }

    /**
     * Query job config json
     * @return config json
     */
    @RequestMapping(value = "/json/{jobId:\\w+}", method = RequestMethod.GET)
    public Message queryConfig(@PathVariable("jobId") Long jobId, HttpServletRequest request){
        Message result = Message.ok("success");
        try {
            String userName = SecurityFilter.getLoginUsername(request);
            if (!jobService.hasPermission(jobId, userName)){
                throw new JobErrorException(-1, "Have no permission to view StreamJob [" + jobId + "] configuration");
            }
            result.setData(new HashMap<>(this.streamJobConfService.getJobConfig(jobId)));
        }catch(Exception e){
            String message = "Fail to view StreamJob configuration(查看任务配置失败), message: " + e.getMessage();
            LOG.warn(message, e);
            result = Message.error(message);
        }
        return result;
    }

    /**
     * Save job config json
     * @param jobId job id
     * @param configContent config content
     * @param request request
     * @return
     */
    @RequestMapping(value = "/json/{jobId:\\w+}", method = RequestMethod.POST)
    public Message saveConfig(@PathVariable("jobId") Long jobId, @RequestBody Map<String, Object> configContent,
                              HttpServletRequest request){
        Message result = Message.ok("success");
        try{
            String userName = SecurityFilter.getLoginUsername(request);
            if (!jobService.hasPermission(jobId, userName)){
                throw new JobErrorException(-1, "Have no permission to save StreamJob [" + jobId + "] configuration");
            }
            this.streamJobConfService.saveJobConfig(jobId, configContent);
        }catch(Exception e){
           String message = "Fail to save StreamJob configuration(保存/更新任务配置失败), message: " + e.getMessage();
           LOG.warn(message, e);
           result = Message.error(message);
        }
        return result;
    }
}
