package de.ipvs.as.mbp.domain.discovery.device.scoring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.util.Json;
import org.reflections8.Reflections;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Deserializer for arrays of {@link ScoringCriterion}s, provided as JSON.
 */
public class ScoringCriteriaDeserializer extends StdDeserializer<List<ScoringCriterion>> {

    private static final String CRITERIA_PACKAGE = "de.ipvs.as.mbp.domain.discovery.device.scoring";
    private final static Map<String, Class<? extends ScoringCriterion>> CRITERIA_TYPES = new HashMap<>();

    static {
        //Get all available scoring criteria classes
        Reflections reflections = new Reflections(CRITERIA_PACKAGE);
        Set<Class<? extends ScoringCriterion>> scoringCriteriaClasses = reflections.getSubTypesOf(ScoringCriterion.class);

        //Iterate over all scoring criteria classes
        for (Class<? extends ScoringCriterion> criteriaClass : scoringCriteriaClasses) {
            //Skip abstract classes and interfaces
            if (Modifier.isAbstract(criteriaClass.getModifiers()) || criteriaClass.isInterface()) continue;

            try {
                //Create new instance of class
                ScoringCriterion scoringCriteria = criteriaClass.getDeclaredConstructor().newInstance();

                //Get and remember the name of this criteria type
                CRITERIA_TYPES.put(scoringCriteria.getTypeName().toLowerCase(), criteriaClass);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Creates the scoring criteria deserializer without any parameters. Required for bean instantiation.
     */
    public ScoringCriteriaDeserializer() {
        this(null);
    }

    /**
     * Creates a new scoring criteria deserializer for a given class.
     *
     * @param vc The class to use
     */
    protected ScoringCriteriaDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * Method that can be called to ask implementation to deserialize
     * JSON content into the value type this serializer handles.
     * Returned instance is to be constructed by method itself.
     * <p>
     * Pre-condition for this method is that the parser points to the
     * first event that is part of value to deserializer (and which
     * is never JSON 'null' literal, more on this below): for simple
     * types it may be the only value; and for structured types the
     * Object start marker or a FIELD_NAME.
     * </p>
     * <p>
     * The two possible input conditions for structured types result
     * from polymorphism via fields. In the ordinary case, Jackson
     * calls this method when it has encountered an OBJECT_START,
     * and the method implementation must advance to the next token to
     * see the first field name. If the application configures
     * polymorphism via a field, then the object looks like the following.
     * <pre>
     *      {
     *          "@class": "class name",
     *          ...
     *      }
     *  </pre>
     * Jackson consumes the two tokens (the <tt>@class</tt> field name
     * and its value) in order to learn the class and select the deserializer.
     * Thus, the stream is pointing to the FIELD_NAME for the first field
     * after the @class. Thus, if you want your method to work correctly
     * both with and without polymorphism, you must begin your method with:
     * <pre>
     *       if (p.currentToken() == JsonToken.START_OBJECT) {
     *         p.nextToken();
     *       }
     *  </pre>
     * This results in the stream pointing to the field name, so that
     * the two conditions align.
     * <p>
     * Post-condition is that the parser will point to the last
     * event that is part of deserialized value (or in case deserialization
     * fails, event that was not recognized or usable, which may be
     * the same event as the one it pointed to upon call).
     * <p>
     * Note that this method is never called for JSON null literal,
     * and thus deserializers need (and should) not check for it.
     *
     * @param jsonParser             Parsed used for reading JSON content
     * @param deserializationContext Context that can be used to access information about this deserialization activity.
     * @return Deserialized value
     */
    @Override
    public List<ScoringCriterion> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        //Retrieve root node
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

        //Root node must be an array
        if (!rootNode.isArray()) {
            throw new EntityValidationException("Invalid format.");
        }

        //Create result list
        List<ScoringCriterion> scoringCriteriaList = new ArrayList<>();

        //Iterate over the array of criteria
        for (JsonNode node : rootNode) {
            //Check if type field is present
            if ((!node.has("type")) || (!node.get("type").isTextual())) {
                continue;
            }

            //Get criterion type
            String type = node.get("type").asText("").toLowerCase();

            //Check if a criterion with this type exists
            if (!CRITERIA_TYPES.containsKey(type)) {
                continue;
            }

            //Deserialize scoring criterion for its corresponding class
            ScoringCriterion scoringCriterion = Json.MAPPER.treeToValue(node, CRITERIA_TYPES.get(type));

            //Check if scoring criterion is valid and add it to list
            if (scoringCriterion != null) {
                scoringCriteriaList.add(scoringCriterion);
            }
        }

        //Return resulting criteria list
        return scoringCriteriaList;
    }
}
