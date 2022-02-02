package de.ipvs.as.mbp.service.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.data_model.DataModelCreateValidator;
import de.ipvs.as.mbp.domain.operator.Code;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.OperatorCreateValidator;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterType;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.repository.DataModelRepository;
import de.ipvs.as.mbp.repository.OperatorRepository;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

/**
 * This service provides means for the management of default operators that may be added to the operator repository
 * on user request.
 */
@Service
public class DefaultOperatorService {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private OperatorCreateValidator operatorCreateValidator;

    @Autowired
    private DataModelRepository dataModelRepository;

    @Autowired
    private DataModelCreateValidator dataModelCreateValidator;

    private static final String DESCRIPTOR_FILE_OPERATOR_JSON = "operator.json";

    private static final String DESCRIPTOR_FILE_OPERATOR_DATA_MODEL = "dataModel.json";

    /**
     * Loads default operators from the resources directory and adds them to the operator repository so that they
     * can be used in actuators and sensors by all users.
     */
    public void addDefaultOperators(List<String> whiteList) {
        Logger logger = Logger.getLogger(DefaultOperatorService.class.getName());
        logger.info("" + whiteList);
        //Iterate over all default operator paths
        for (String operatorPath : whiteList) {
            //Create new operator object to add it later to the repository
            Operator newOperator = new Operator();

            //New operator is not owned by anyone
            newOperator.setOwner(null);

            //Build path of descriptor
            String descriptorPath = "/static" + operatorPath + "/" + DESCRIPTOR_FILE_OPERATOR_JSON;
            String dataModelPath = "/static" + operatorPath + "/" + DESCRIPTOR_FILE_OPERATOR_DATA_MODEL;

            String descriptorContent = getResourceFileContent(logger, descriptorPath);
            if (descriptorContent == null) {
                continue;
            }

            try {
                //Read descriptor file
                JSONObject descriptorJSON = new JSONObject(descriptorContent);

                //Get operator name
                String operatorName = descriptorJSON.optString("name");

                //Check if a default entity with this name already exists
                if (operatorRepository.existsByNameAndDefaultEntity(operatorName, true)) {
                    continue;
                }

                //Set operator properties from the descriptor
                newOperator.setName(operatorName);
                newOperator.setDescription(descriptorJSON.optString("description"));
                newOperator.setUnit(descriptorJSON.optString("unit"));

                //Flag operator as default operator
                newOperator.setDefaultEntity(true);

                // Set operator data model
                newOperator.setDataModel(getDataModelFromServletContext(getResourceFileContent(logger, dataModelPath)));

                //Get parameters
                JSONArray parameterArray = descriptorJSON.optJSONArray("parameters");

                //Check if there are parameters
                if (parameterArray != null) {

                    //Create new list for parameters
                    List<Parameter> parameterList = new ArrayList<>();

                    //Iterate over all parameters
                    for (int i = 0; i < parameterArray.length(); i++) {
                        //Get parameter JSON object
                        JSONObject parameterObject = parameterArray.getJSONObject(i);

                        //Create new parameter object
                        Parameter newParameter = new Parameter();
                        newParameter.setName(parameterObject.optString("name"));
                        newParameter.setType(ParameterType.create(parameterObject.optString("type")));
                        newParameter.setUnit(parameterObject.optString("unit"));
                        newParameter.setMandatory(parameterObject.optBoolean("mandatory", false));

                        //Add parameter to list
                        parameterList.add(newParameter);
                    }

                    //Add parameter list to operator
                    newOperator.setParameters(parameterList);
                }

                //Get files
                JSONArray fileArray = descriptorJSON.optJSONArray("files");

                //Skip operator if no files are associated with it
                if ((fileArray == null) || (fileArray.length() < 1)) {
                    continue;
                }

                //Iterate over all files
                for (int i = 0; i < fileArray.length(); i++) {
                    //Get current file path and create a file object
                    String operatorFilePath = "/static" + operatorPath + "/" + fileArray.getString(i);
                    File operatorFile = new File(servletContext.getRealPath(operatorFilePath));

                    //Determine mime type of the file
                    String operatorFileMime = servletContext.getMimeType(operatorFilePath);

                    if ((operatorFileMime == null) || (operatorFileMime.isEmpty())) {
                        operatorFileMime = "application/octet-stream";
                    }

                    //Try to read the file
                    String resourceFileContent = getResourceFileContent(logger, operatorFilePath);
                    if (resourceFileContent == null) {
                        logger.warning(String.format("Problem accessing file content of path: %s", operatorFilePath));
                        continue;
                    }

                    StringReader stringReader = new StringReader(resourceFileContent);
                    byte[] operatorFileBytes = IOUtils.toByteArray(stringReader);
                    stringReader.close();

                    //Convert file content to base64 with mime type prefix
                    String base64String = Base64.getEncoder().encodeToString(operatorFileBytes);
                    base64String = "data:" + operatorFileMime + ";base64," + base64String;

                    //Create new code object for this file
                    Code newCode = new Code();
                    newCode.setName(operatorFile.getName());
                    newCode.setContent(base64String);

                    //Add code to operator
                    newOperator.addRoutine(newCode);
                }

                //Replace possibly bad line breaks in the operator files
                newOperator.replaceLineBreaks();

                //Insert new operator into repository
                operatorCreateValidator.validateCreatable(newOperator);
                operatorRepository.insert(newOperator);
            } catch (Exception e) {
                System.out.println(newOperator.getName());
                e.printStackTrace();
            }
        }
    }

    private String getResourceFileContent(Logger logger, String descriptorPath) {
        ClassPathResource classPathResource = new ClassPathResource(descriptorPath);
        //Check if there is a descriptor file, otherwise skip the operator
        if (!classPathResource.exists()) {
            logger.warning(String.format("Cannot find operator information at: %s", descriptorPath));
            return null;
        }

        String content = null;
        try(InputStream inputStream = classPathResource.getInputStream()) {
            content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * Creates a data model entity based on a classpath resource.
     *
     * @param descriptorContent Path to the dataModel.json
     * @return The {@link DataModel} created from the json file.
     * @throws JSONException           If the POJO mapping JSON --> DataModel.class fails.
     * @throws EntityNotFoundException If the writing to the database fails.
     */
    private DataModel getDataModelFromServletContext(String descriptorContent) throws IOException, EntityNotFoundException {
        // JSON to POJO mapping using Jackson to retrieve the data model
        ObjectMapper objectMapper = new ObjectMapper();
        DataModel dataModel = objectMapper.readValue(descriptorContent, DataModel.class);
        // Flag the data model as default as no user created it
        dataModel.setDefaultEntity(true);

        // Validate the data model (which also calls some necessary methods for the proper data model instantiation
        dataModelCreateValidator.validateCreatable(dataModel);
        // Add the dataModel entity to the mongoDB repository
        dataModelRepository.save(dataModel);

        return dataModel;
    }

}
