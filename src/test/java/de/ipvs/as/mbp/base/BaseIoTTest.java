package de.ipvs.as.mbp.base;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.Cookie;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.ComponentDTO;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.rules.RuleActionType;
import de.ipvs.as.mbp.util.IoTDeviceContainer;
import de.ipvs.as.mbp.util.testexecution.IoTDeviceTest;
import de.ipvs.as.mbp.util.testexecution.RequiresMQTTExtension;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StreamUtils;
import org.testcontainers.junit.jupiter.Container;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IoTDeviceTest
@ExtendWith({RequiresMQTTExtension.class})
public abstract class BaseIoTTest extends BaseIntegrationTest {

    protected final static String testScript = "#!/bin/bash\n" +
            "echo  $(date): Test Script was called | tee -a /home/mbp/calllog.log";

    @Container
    public IoTDeviceContainer device = new IoTDeviceContainer();

    public OperatorRoutine getRoutineFromClasspath(String name, String type, String path) throws Exception {
        InputStream classPathInput = getClass().getClassLoader().getResourceAsStream(path);
        ByteArrayOutputStream fileData = new ByteArrayOutputStream();
        StreamUtils.copy(classPathInput, fileData);

        return new OperatorRoutine(name, type, fileData.toByteArray());
    }

    public void printStageMessage(String message) {
        message = "#  " + message + "  #";
        StringBuilder sep = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            sep.append('#');
        }
        System.out.printf("\n\n%s\n%s\n%s\n", sep.toString(), message.toUpperCase(), sep.toString());
    }

    public Device createNewDevice(IoTDeviceContainer container, Cookie sessionCookie, String name) throws Exception {
        DeviceDTO requestDto = new DeviceDTO();
        requestDto.setName(name);
        requestDto.setUsername("mbp");
        requestDto.setPassword("password");
        requestDto.setIpAddress("127.0.0.1");
        requestDto.setPort(container.getSshPort());
        requestDto.setComponentType("Computer");

        MvcResult result = mockMvc.perform(post(Constants.BASE_PATH + "/devices")
                        .cookie(sessionCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding("utf-8"))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Device.class);
    }

    public DataModel createDataModel(Cookie sessionCookie, String dataModelName, String description, JSONArray treeNodes) throws Exception {
        JSONObject dataModelObj = new JSONObject();
        dataModelObj.put("name", dataModelName);
        dataModelObj.put("description", description);
        dataModelObj.put("treeNodes", treeNodes);

        MvcResult result = mockMvc.perform(post(Constants.BASE_PATH + "/data-models")
                        .headers(getMBPAccessHeaderForAdmin())
                        .cookie(sessionCookie)
                        .contentType(REQUEST_CONTENT_TYPE)
                        .content(dataModelObj.toString())
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("admin"))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), DataModel.class);
    }

    public Operator createOperator(Cookie sessionCookie, String dataModelID, String operatorName, String operatorUnit, OperatorRoutine... scripts) throws Exception {
        JSONArray routines = new JSONArray();

        for (OperatorRoutine script : scripts) {
            routines.put(script.toJSONObject());
        }

        JSONObject operatorObj = new JSONObject();
        operatorObj.put("name", operatorName);
        operatorObj.put("dataModelId", dataModelID);
        operatorObj.put("parameters", new JSONArray());
        operatorObj.put("unit", operatorUnit);
        operatorObj.put("errors", new JSONObject());
        operatorObj.put("routines", routines);

        MvcResult result = mockMvc.perform(post(Constants.BASE_PATH + "/operators")
                        .headers(getMBPAccessHeaderForAdmin())
                        .cookie(sessionCookie)
                        .contentType(REQUEST_CONTENT_TYPE)
                        .content(operatorObj.toString())
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("admin"))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Operator.class);
    }

    public void deploySensor(Cookie sessionCookie, String sensorId) throws Exception {
        deployOperator(sessionCookie, "sensor", sensorId);
    }

    public void deployActuator(Cookie sessionCookie, String actuatorId) throws Exception {
        deployOperator(sessionCookie, "actuator", actuatorId);
    }

    public void startSensor(Cookie sessionCookie, String sensorId) throws Exception {
        startOperator(sessionCookie, "sensor", sensorId);
    }

    public void startActuator(Cookie sessionCookie, String actuatorId) throws Exception {
        startOperator(sessionCookie, "actuator", actuatorId);
    }

    private void deployOperator(Cookie sessionCookie, String operatorType, String operatorId) throws Exception {
        mockMvc.perform(post(Constants.BASE_PATH + "/deploy/" + operatorType + "/" + operatorId)
                        .headers(getMBPAccessHeaderForAdmin())
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andDo(print());
    }

    private void startOperator(Cookie sessionCookie, String operatorType, String operatorId) throws Exception {
        mockMvc.perform(post(Constants.BASE_PATH + "/start/" + operatorType + "/" + operatorId)
                        .headers(getMBPAccessHeaderForAdmin())
                        .cookie(sessionCookie)
                        .contentType(REQUEST_CONTENT_TYPE)
                        .content("[]"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    public String createActuatorRuleAction(Cookie sessionCookie, String actuatorId, String ruleName, String actionSuffix) throws Exception {
        return createRuleAction(sessionCookie, actuatorId, ruleName, actionSuffix, RuleActionType.ACTUATOR_ACTION);
    }

    public String createRuleAction(Cookie sessionCookie, String actuatorId, String ruleName, String actionSuffix, RuleActionType type) throws Exception {
        JSONObject ruleAction = new JSONObject();
        ruleAction.put("name", ruleName);
        ruleAction.put("type", type.name());

        JSONObject ruleActionParams = new JSONObject();
        ruleActionParams.put("action", actionSuffix);
        ruleActionParams.put("actuator", actuatorId);
        ruleAction.put("parameters", ruleActionParams);

        MvcResult response = mockMvc.perform(post(Constants.BASE_PATH + "/rule-actions")
                        .headers(getMBPAccessHeaderForAdmin())
                        .cookie(sessionCookie)
                        .contentType(REQUEST_CONTENT_TYPE)
                        .content(ruleAction.toString()))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        // Only Retrieve id to resolve occational issues when trying to parse using the object mapper
        return new JSONObject(response.getResponse().getContentAsString()).getString("id");
    }

    public boolean testRuleAction(Cookie sessionCookie, String ruleActionId) throws Exception {
        MvcResult response = mockMvc.perform(post(Constants.BASE_PATH + "/rule-actions/test/" + ruleActionId)
                        .headers(getMBPAccessHeaderForAdmin())
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        return Boolean.parseBoolean(response.getResponse().getContentAsString());
    }

    public Sensor createSensor(Cookie sessionCookie, String sensorName, String sensorType, String deviceId, String operatorId) throws Exception {
        ComponentDTO sensorReq = new ComponentDTO();
        sensorReq.setDeviceId(deviceId);
        sensorReq.setComponentType(sensorType);
        sensorReq.setOperatorId(operatorId);
        sensorReq.setName(sensorName);

        MvcResult result = mockMvc.perform(post(Constants.BASE_PATH + "/sensors")
                .headers(getMBPAccessHeaderForAdmin())
                .cookie(sessionCookie)
                .contentType(REQUEST_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(sensorReq))
        ).andExpect(status().isOk()).andDo(print()).andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Sensor.class);
    }

    public Actuator createActuator(Cookie sessionCookie, String actuatorName, String actuatorType, String deviceId, String operatorId) throws Exception {
        ComponentDTO actuatorCreationReq = new ComponentDTO();
        actuatorCreationReq.setDeviceId(deviceId);
        actuatorCreationReq.setComponentType(actuatorType);
        actuatorCreationReq.setOperatorId(operatorId);
        actuatorCreationReq.setName(actuatorName);

        MvcResult result = mockMvc.perform(post(Constants.BASE_PATH + "/actuators")
                .headers(getMBPAccessHeaderForAdmin())
                .cookie(sessionCookie)
                .contentType(REQUEST_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(actuatorCreationReq))
        ).andExpect(status().isOk()).andDo(print()).andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Actuator.class);
    }

    public void ensureDeviceHasSSH(Cookie sessionCookie, String deviceId) throws Exception {
        mockMvc.perform(get(Constants.BASE_PATH + "/devices/" + deviceId + "/state/")
                        .headers(getMBPAccessHeaderForAdmin())
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("SSH_AVAILABLE"))
                .andDo(print());
    }

    public void ensureSensorIsReady(Cookie sessionCookie, String sensorId) throws Exception {
        mockMvc.perform(get(Constants.BASE_PATH + "/sensors/state/" + sensorId)
                        .headers(getMBPAccessHeaderForAdmin())
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("READY"))
                .andDo(print());
    }

    public static class OperatorRoutine {
        public String name;
        public byte[] content;
        public String contentType;

        public OperatorRoutine(String name, String contentType, String content) {
            this(name, contentType, content.getBytes(StandardCharsets.UTF_8));
        }

        public OperatorRoutine(String name, String contentType, byte[] content) {
            this.name = name;
            this.content = content;
            this.contentType = contentType;
        }

        public OperatorRoutine(String name, String content) {
            this(name, "application/x-shellscript", content);
        }

        public JSONObject toJSONObject() throws Exception {
            JSONObject testScriptObj = new JSONObject();
            testScriptObj.put("name", name);
            testScriptObj.put("content",
                    String.format("data:%s;base64,%s", this.contentType,
                            Base64.getEncoder().encodeToString(this.content)));
            return testScriptObj;
        }
    }
}
