/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.models.ExternalDocumentation;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * CodegenModel represents a schema object in a OpenAPI document.
 */
@JsonIgnoreProperties({"parentModel", "interfaceModels"})
public class CodegenModel implements IJsonSchemaValidationProperties {
    // The parent model name from the schemas. The parent is determined by inspecting the allOf, anyOf and
    // oneOf attributes in the OAS. First codegen inspects 'allOf', then 'anyOf', then 'oneOf'.
    // If there are multiple object references in the attribute ('allOf', 'anyOf', 'oneOf'), and one of the
    // object is a discriminator, that object is set as the parent. If no discriminator is specified,
    // codegen returns the first one in the list, i.e. there is no obvious parent in the OpenAPI specification.
    // When possible, the mustache templates should use 'allParents' to handle multiple parents.
    @Getter @Setter
    public String parent, parentSchema;
    @Getter @Setter
    public List<String> interfaces;
    // The list of parent model name from the schemas. In order of preference, the parent is obtained
    // from the 'allOf' attribute, then 'anyOf', and finally 'oneOf'.
    @Getter @Setter
    public List<String> allParents;

    // References to parent and interface CodegenModels. Only set when code generator supports inheritance.
    @Getter @Setter
    public CodegenModel parentModel;
    @Getter @Setter
    public List<CodegenModel> interfaceModels;
    @Getter @Setter
    public List<CodegenModel> children;

    // anyOf, oneOf, allOf
    public Set<String> anyOf = new TreeSet<>();
    public Set<String> oneOf = new TreeSet<>();
    public Set<String> allOf = new TreeSet<>();

    // direct descendants that are allowed to extend the current model
    public List<String> permits = new ArrayList<>();

    // The schema name as written in the OpenAPI document
    // If it's a reserved word, it will be escaped.
    @Getter @Setter
    public String name;
    // The original schema name as written in the OpenAPI document.
    @Getter @Setter
    public String schemaName;
    // The language-specific name of the class that implements this schema.
    // The name of the class is derived from the OpenAPI schema name with formatting rules applied.
    // The classname is derived from the OpenAPI schema name, with sanitization and escaping rules applied.
    @Getter @Setter
    public String classname;
    // The value of the 'title' attribute in the OpenAPI document.
    @Getter @Setter
    public String title;
    @Getter @Setter
    public String description, classVarName, modelJson, dataType, xmlPrefix, xmlNamespace, xmlName;
    @Getter @Setter
    public String classFilename; // store the class file name, mainly used for import
    @Getter @Setter
    public String unescapedDescription;
    /**
     * -- GETTER --
     * Returns the discriminator for this schema object, or null if no discriminator has been specified.
     * The list of all possible schema discriminator mapping values is obtained
     * from explicit discriminator mapping values in the OpenAPI document, and from
     * inherited discriminators through oneOf, allOf, anyOf.
     * For example, a discriminator may be defined in a 'Pet' schema as shown below.
     * The Dog and Cat schemas inherit the discriminator through the allOf reference.
     * In the 'Pet' schema, the supported discriminator mapping values for the
     * 'objectType' properties are 'Dog' and 'Cat'.
     * The allowed discriminator mapping value for the Dog schema is 'Dog'.
     * The allowed discriminator mapping value for the Cat schema is 'Dog'.
     * Pet:
     * type: object
     * discriminator:
     * propertyName: objectType
     * required:
     * - objectType
     * properties:
     * objectType:
     * type: string
     * Dog:
     * allOf:
     * - $ref: '#/components/schemas/Pet'
     * - type: object
     * properties:
     * p1:
     * type: string
     * Cat:
     * allOf:
     * - $ref: '#/components/schemas/Pet'
     * - type: object
     * properties:
     * p2:
     * type: string
     *
     */
    @Getter public CodegenDiscriminator discriminator;
    @Getter @Setter
    public String defaultValue;
    @Getter @Setter
    public String arrayModelType;
    public boolean isAlias; // Is this effectively an alias of another simple type
    public boolean isString, isInteger, isLong, isNumber, isNumeric, isFloat, isDouble, isDate, isDateTime,
            isDecimal, isShort, isUnboundedInteger, isPrimitiveType, isBoolean, isFreeFormObject;
    private boolean additionalPropertiesIsAnyType;
    public List<CodegenProperty> vars = new ArrayList<>(); // all properties (without parent's properties)
    @Getter @Setter
    public List<CodegenProperty> allVars = new ArrayList<>(); // all properties (with parent's properties)
    public List<CodegenProperty> requiredVars = new ArrayList<>(); // a list of required properties
    @Getter @Setter
    public List<CodegenProperty> optionalVars = new ArrayList<>(); // a list of optional properties
    @Getter @Setter
    public List<CodegenProperty> readOnlyVars = new ArrayList<>(); // a list of read-only properties
    @Getter @Setter
    public List<CodegenProperty> readWriteVars = new ArrayList<>(); // a list of properties for read, write
    @Getter @Setter
    public List<CodegenProperty> parentVars = new ArrayList<>();
    public List<CodegenProperty> parentRequiredVars = new ArrayList<>();
    @Getter @Setter
    public List<CodegenProperty> nonNullableVars = new ArrayList<>(); // a list of non-nullable properties
    @Getter @Setter
    public Map<String, Object> allowableValues;

    // Sorted sets of required parameters.
    @Getter @Setter
    public Set<String> mandatory = new TreeSet<>(); // without parent's required properties
    @Getter @Setter
    public Set<String> allMandatory = new TreeSet<>(); // with parent's required properties

    @Getter @Setter
    public Set<String> imports = new TreeSet<>();
    @Getter @Setter
    public boolean emptyVars;
    public boolean hasVars, hasMoreModels, hasEnums, isEnum, hasValidation;
    /**
     * Indicates the OAS schema specifies "nullable: true".
     */
    public boolean isNullable;
    /**
     * Indicates the type has at least one required property.
     */
    public boolean hasRequired;
    /**
     * Indicates the type has at least one optional property.
     */
    public boolean hasOptional;
    public boolean isArray;
    public boolean hasChildren;
    public boolean isMap;
    /**
     * datatype is the generic inner parameter of a std::optional for C++, or Optional (Java)
     */
    public boolean isOptional;
    public boolean isNull;
    public boolean isVoid = false;
    /**
     * Indicates the OAS schema specifies "deprecated: true".
     */
    public boolean isDeprecated;
    /**
     * Indicates the type has at least one read-only property.
     */
    public boolean hasReadOnly;
    /**
     * Indicates the all properties of the type are read-only.
     */
    public boolean hasOnlyReadOnly = true;
    @Getter @Setter
    public ExternalDocumentation externalDocumentation;

    @Getter @Setter
    public Map<String, Object> vendorExtensions = new HashMap<>();
    private CodegenComposedSchemas composedSchemas;
    private boolean hasMultipleTypes = false;
    public HashMap<String, SchemaTestCase> testCases = new HashMap<>();
    private boolean schemaIsFromAdditionalProperties;
    private boolean isBooleanSchemaTrue;
    private boolean isBooleanSchemaFalse;
    private String format;
    private LinkedHashMap<String, List<String>> dependentRequired;
    private CodegenProperty contains;

    /**
     * The type of the value for the additionalProperties keyword in the OAS document.
     * Used in map like objects, including composed schemas.
     * <p>
     * In most programming languages, the additional (undeclared) properties are stored
     * in a map data structure, such as HashMap in Java, map in golang, or a dict in Python.
     * There are multiple ways to implement the additionalProperties keyword, depending
     * on the programming language and mustache template.
     * One way is to use class inheritance. For example in the generated Java code, the
     * generated model class may extend from HashMap to store the additional properties.
     * In that case 'CodegenModel.parent' is set to represent the class hierarchy.
     * Another way is to use CodegenModel.additionalPropertiesType. A code generator
     * such as Python does not use class inheritance to model additional properties.
     * <p>
     * For example, in the OAS schema below, the schema has a declared 'id' property
     * and additional, undeclared properties of type 'integer' are allowed.
     * <p>
     * type: object
     * properties:
     * id:
     * type: integer
     * additionalProperties:
     * type: integer
     */
    @Getter @Setter
    public String additionalPropertiesType;

    /**
     * True if additionalProperties is set to true (boolean value), any type, free form object, etc
     * <p>
     * TODO: we may rename this to isAdditionalPropertiesEnabled or something
     * else to avoid confusions
     */
    public boolean isAdditionalPropertiesTrue;

    private Integer maxProperties;
    private Integer minProperties;
    private boolean uniqueItems;
    private Boolean uniqueItemsBoolean;
    private Integer maxItems;
    private Integer minItems;
    private Integer maxLength;
    private Integer minLength;
    private boolean exclusiveMinimum;
    private boolean exclusiveMaximum;
    private String minimum;
    private String maximum;
    private String pattern;
    private Number multipleOf;
    private CodegenProperty items;
    private CodegenProperty additionalProperties;
    private boolean isModel;
    private boolean hasRequiredVars;
    private boolean hasDiscriminatorWithNonEmptyMapping;
    private boolean isAnyType;
    private boolean isUuid;
    private boolean isUri;
    private Map<String, CodegenProperty> requiredVarsMap;
    private String ref;

    @Override
    public CodegenProperty getContains() {
        return contains;
    }

    @Override
    public void setContains(CodegenProperty contains) {
        this.contains = contains;
    }

    @Override
    public LinkedHashMap<String, List<String>> getDependentRequired() {
        return dependentRequired;
    }

    @Override
    public void setDependentRequired(LinkedHashMap<String, List<String>> dependentRequired) {
        this.dependentRequired = dependentRequired;
    }

    @Override
    public boolean getIsBooleanSchemaTrue() {
        return isBooleanSchemaTrue;
    }

    @Override
    public void setIsBooleanSchemaTrue(boolean isBooleanSchemaTrue) {
        this.isBooleanSchemaTrue = isBooleanSchemaTrue;
    }

    @Override
    public boolean getIsBooleanSchemaFalse() {
        return isBooleanSchemaFalse;
    }

    @Override
    public void setIsBooleanSchemaFalse(boolean isBooleanSchemaFalse) {
        this.isBooleanSchemaFalse = isBooleanSchemaFalse;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public boolean getSchemaIsFromAdditionalProperties() {
        return schemaIsFromAdditionalProperties;
    }

    @Override
    public void setSchemaIsFromAdditionalProperties(boolean schemaIsFromAdditionalProperties) {
        this.schemaIsFromAdditionalProperties = schemaIsFromAdditionalProperties;
    }

    /**
     * Return true if the classname property is sanitized, false if it is the same as the OpenAPI schema name.
     * The OpenAPI schema name may be any valid JSON schema name, including non-ASCII characters.
     * The name of the class may have to be sanitized with character escaping.
     *
     * @return true if the classname property is sanitized
     */
    public boolean getIsClassnameSanitized() {
        return !StringUtils.equals(classname, name);
    }

    public void setDiscriminator(CodegenDiscriminator discriminator) {
        this.discriminator = discriminator;
        if (discriminator != null && !discriminator.getMappedModels().isEmpty()) {
            this.hasDiscriminatorWithNonEmptyMapping = true;
        }
    }

    /**
     * Returns the name of the discriminator property for this schema in the OpenAPI document.
     * In the OpenAPI document, the discriminator may be specified in the local schema or
     * it may be inherited, such as through a 'allOf' schema which references another schema
     * that has a discriminator, recursively.
     *
     * @return the name of the discriminator property.
     */
    public String getDiscriminatorName() {
        return discriminator == null ? null : discriminator.getPropertyName();
    }


    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getMaximum() {
        return maximum;
    }

    @Override
    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    @Override
    public String getMinimum() {
        return minimum;
    }

    @Override
    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    @Override
    public boolean getExclusiveMaximum() {
        return exclusiveMaximum;
    }

    @Override
    public void setExclusiveMaximum(boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    @Override
    public boolean getExclusiveMinimum() {
        return exclusiveMinimum;
    }

    @Override
    public void setExclusiveMinimum(boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public Integer getMinLength() {
        return minLength;
    }

    @Override
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    @Override
    public Integer getMaxLength() {
        return maxLength;
    }

    @Override
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public Integer getMinItems() {
        return minItems;
    }

    @Override
    public void setMinItems(Integer minItems) {
        this.minItems = minItems;
    }

    @Override
    public Integer getMaxItems() {
        return maxItems;
    }

    @Override
    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public boolean getUniqueItems() {
        return uniqueItems;
    }

    @Override
    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    @Override
    public Boolean getUniqueItemsBoolean() {
        return uniqueItemsBoolean;
    }

    @Override
    public void setUniqueItemsBoolean(Boolean uniqueItemsBoolean) {
        this.uniqueItemsBoolean = uniqueItemsBoolean;
    }

    @Override
    public Integer getMinProperties() {
        return minProperties;
    }

    @Override
    public void setMinProperties(Integer minProperties) {
        this.minProperties = minProperties;
    }

    @Override
    public Integer getMaxProperties() {
        return maxProperties;
    }

    @Override
    public void setMaxProperties(Integer maxProperties) {
        this.maxProperties = maxProperties;
    }

    @Override
    public Number getMultipleOf() {
        return multipleOf;
    }

    @Override
    public void setMultipleOf(Number multipleOf) {
        this.multipleOf = multipleOf;
    }

    @Override
    public CodegenProperty getItems() {
        return items;
    }

    @Override
    public void setItems(CodegenProperty items) {
        this.items = items;
    }

    @Override
    public boolean getIsModel() {
        return isModel;
    }

    @Override
    public void setIsModel(boolean isModel) {
        this.isModel = isModel;
    }

    @Override
    public boolean getIsDate() {
        return isDate;
    }

    @Override
    public void setIsDate(boolean isDate) {
        this.isDate = isDate;
    }

    @Override
    public boolean getIsDateTime() {
        return isDateTime;
    }

    @Override
    public void setIsDateTime(boolean isDateTime) {
        this.isDateTime = isDateTime;
    }

    @Override
    public boolean getIsMap() {
        return isMap;
    }

    @Override
    public void setIsMap(boolean isMap) {
        this.isMap = isMap;
    }

    @Override
    public boolean getIsOptional() {
        return isOptional;
    }

    @Override
    public void setIsOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    @Override
    public boolean getIsArray() {
        return isArray;
    }

    @Override
    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    @Override
    public boolean getIsShort() {
        return isShort;
    }

    @Override
    public void setIsShort(boolean isShort) {
        this.isShort = isShort;
    }

    @Override
    public boolean getIsBoolean() {
        return isBoolean;
    }

    @Override
    public void setIsBoolean(boolean isBoolean) {
        this.isBoolean = isBoolean;
    }

    @Override
    public boolean getIsUnboundedInteger() {
        return isUnboundedInteger;
    }

    @Override
    public void setIsUnboundedInteger(boolean isUnboundedInteger) {
        this.isUnboundedInteger = isUnboundedInteger;
    }

    @Override
    public boolean getIsPrimitiveType() {
        return isPrimitiveType;
    }

    @Override
    public void setIsPrimitiveType(boolean isPrimitiveType) {
        this.isPrimitiveType = isPrimitiveType;
    }

    @Override
    public CodegenProperty getAdditionalProperties() {
        return additionalProperties;
    }

    @Override
    public void setAdditionalProperties(CodegenProperty additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public boolean getHasValidation() {
        return hasValidation;
    }

    @Override
    public void setHasValidation(boolean hasValidation) {
        this.hasValidation = hasValidation;
    }

    @Override
    public List<CodegenProperty> getRequiredVars() {
        return requiredVars;
    }

    @Override
    public void setRequiredVars(List<CodegenProperty> requiredVars) {
        this.requiredVars = requiredVars;
    }

    @Override
    public List<CodegenProperty> getVars() {
        return vars;
    }

    @Override
    public void setVars(List<CodegenProperty> vars) {
        this.vars = vars;
    }

    @Override
    public boolean getIsNull() {
        return isNull;
    }

    @Override
    public void setIsNull(boolean isNull) {
        this.isNull = isNull;
    }

    @Override
    public boolean getIsVoid() {
        return isVoid;
    }

    @Override
    public void setIsVoid(boolean isVoid) {
        this.isVoid = isVoid;
    }

    @Override
    public boolean getAdditionalPropertiesIsAnyType() {
        return additionalPropertiesIsAnyType;
    }

    @Override
    public void setAdditionalPropertiesIsAnyType(boolean additionalPropertiesIsAnyType) {
        this.additionalPropertiesIsAnyType = additionalPropertiesIsAnyType;
    }

    @Override
    public boolean getHasVars() {
        return this.hasVars;
    }

    @Override
    public void setHasVars(boolean hasVars) {
        this.hasVars = hasVars;
    }

    @Override
    public boolean getHasRequired() {
        return this.hasRequired;
    }

    @Override
    public void setHasRequired(boolean hasRequired) {
        this.hasRequired = hasRequired;
    }

    @Override
    public boolean getHasDiscriminatorWithNonEmptyMapping() {
        return hasDiscriminatorWithNonEmptyMapping;
    }

    @Override
    public void setHasDiscriminatorWithNonEmptyMapping(boolean hasDiscriminatorWithNonEmptyMapping) {
        this.hasDiscriminatorWithNonEmptyMapping = hasDiscriminatorWithNonEmptyMapping;
    }

    @Override
    public boolean getIsString() {
        return isString;
    }

    @Override
    public void setIsString(boolean isString) {
        this.isString = isString;
    }

    @Override
    public boolean getIsNumber() {
        return isNumber;
    }

    @Override
    public void setIsNumber(boolean isNumber) {
        this.isNumber = isNumber;
    }

    @Override
    public boolean getIsAnyType() {
        return isAnyType;
    }

    @Override
    public void setIsAnyType(boolean isAnyType) {
        this.isAnyType = isAnyType;
    }

    @Override
    public boolean getIsFreeFormObject() {
        return isFreeFormObject;
    }

    @Override
    public void setIsFreeFormObject(boolean isFreeFormObject) {
        this.isFreeFormObject = isFreeFormObject;
    }

    @Override
    public boolean getIsUuid() {
        return isUuid;
    }

    @Override
    public void setIsUuid(boolean isUuid) {
        this.isUuid = isUuid;
    }

    public boolean getIsUri() {
        return isUri;
    }

    public void setIsUri(boolean isUri) {
        this.isUri = isUri;
    }

    @Override
    public void setComposedSchemas(CodegenComposedSchemas composedSchemas) {
        this.composedSchemas = composedSchemas;
    }

    @Override
    public CodegenComposedSchemas getComposedSchemas() {
        return composedSchemas;
    }

    @Override
    public boolean getHasMultipleTypes() {
        return hasMultipleTypes;
    }

    @Override
    public void setHasMultipleTypes(boolean hasMultipleTypes) {
        this.hasMultipleTypes = hasMultipleTypes;
    }

    @Override
    public boolean getIsFloat() {
        return isFloat;
    }

    @Override
    public void setIsFloat(boolean isFloat) {
        this.isFloat = isFloat;
    }

    @Override
    public boolean getIsDouble() {
        return isDouble;
    }

    @Override
    public void setIsDouble(boolean isDouble) {
        this.isDouble = isDouble;
    }

    @Override
    public boolean getIsInteger() {
        return isInteger;
    }

    @Override
    public void setIsInteger(boolean isInteger) {
        this.isInteger = isInteger;
    }

    @Override
    public boolean getIsLong() {
        return isLong;
    }

    @Override
    public void setIsLong(boolean isLong) {
        this.isLong = isLong;
    }

    @Override
    public boolean getIsBinary() {
        return false;
    }

    @Override
    public void setIsBinary(boolean isBinary) {
    }

    @Override
    public boolean getIsByteArray() {
        return false;
    }

    @Override
    public void setIsByteArray(boolean isByteArray) {
    }

    @Override
    public boolean getIsDecimal() {
        return isDecimal;
    }

    @Override
    public void setIsDecimal(boolean isDecimal) {
        this.isDecimal = isDecimal;
    }

    @Override
    public boolean getIsEnum() {
        return isEnum;
    }

    @Override
    public void setIsEnum(boolean isEnum) {
        this.isEnum = isEnum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodegenModel)) return false;
        CodegenModel that = (CodegenModel) o;
        return isAlias == that.isAlias &&
                isString == that.isString &&
                isInteger == that.isInteger &&
                isShort == that.isShort &&
                isLong == that.isLong &&
                isUnboundedInteger == that.isUnboundedInteger &&
                isBoolean == that.isBoolean &&
                isNumber == that.isNumber &&
                isNumeric == that.isNumeric &&
                isFloat == that.isFloat &&
                isDouble == that.isDouble &&
                isDate == that.isDate &&
                isDateTime == that.isDateTime &&
                hasVars == that.hasVars &&
                emptyVars == that.emptyVars &&
                hasMoreModels == that.hasMoreModels &&
                hasEnums == that.hasEnums &&
                isEnum == that.isEnum &&
                isNullable == that.isNullable &&
                hasRequired == that.hasRequired &&
                hasOptional == that.hasOptional &&
                isArray == that.isArray &&
                hasChildren == that.hasChildren &&
                isMap == that.isMap &&
                isOptional == that.isOptional &&
                isDeprecated == that.isDeprecated &&
                hasReadOnly == that.hasReadOnly &&
                hasOnlyReadOnly == that.hasOnlyReadOnly &&
                isNull == that.isNull &&
                hasValidation == that.hasValidation &&
                isDecimal == that.isDecimal &&
                hasMultipleTypes == that.getHasMultipleTypes() &&
                hasDiscriminatorWithNonEmptyMapping == that.getHasDiscriminatorWithNonEmptyMapping() &&
                isUuid == that.getIsUuid() &&
                isUri == that.getIsUri() &&
                isBooleanSchemaTrue == that.getIsBooleanSchemaTrue() &&
                isBooleanSchemaFalse == that.getIsBooleanSchemaFalse() &&
                getSchemaIsFromAdditionalProperties() == that.getSchemaIsFromAdditionalProperties() &&
                getIsAnyType() == that.getIsAnyType() &&
                getAdditionalPropertiesIsAnyType() == that.getAdditionalPropertiesIsAnyType() &&
                getUniqueItems() == that.getUniqueItems() &&
                getExclusiveMinimum() == that.getExclusiveMinimum() &&
                getExclusiveMaximum() == that.getExclusiveMaximum() &&
                Objects.equals(contains, that.getContains()) &&
                Objects.equals(dependentRequired, that.getDependentRequired()) &&
                Objects.equals(format, that.getFormat()) &&
                Objects.equals(uniqueItemsBoolean, that.getUniqueItemsBoolean()) &&
                Objects.equals(ref, that.getRef()) &&
                Objects.equals(requiredVarsMap, that.getRequiredVarsMap()) &&
                Objects.equals(composedSchemas, that.composedSchemas) &&
                Objects.equals(parent, that.parent) &&
                Objects.equals(parentSchema, that.parentSchema) &&
                Objects.equals(interfaces, that.interfaces) &&
                Objects.equals(allParents, that.allParents) &&
                Objects.equals(parentModel, that.parentModel) &&
                Objects.equals(interfaceModels, that.interfaceModels) &&
                Objects.equals(children, that.children) &&
                Objects.equals(permits, that.permits) &&
                Objects.equals(anyOf, that.anyOf) &&
                Objects.equals(oneOf, that.oneOf) &&
                Objects.equals(allOf, that.allOf) &&
                Objects.equals(name, that.name) &&
                Objects.equals(schemaName, that.schemaName) &&
                Objects.equals(classname, that.classname) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(classVarName, that.classVarName) &&
                Objects.equals(modelJson, that.modelJson) &&
                Objects.equals(dataType, that.dataType) &&
                Objects.equals(xmlPrefix, that.xmlPrefix) &&
                Objects.equals(xmlNamespace, that.xmlNamespace) &&
                Objects.equals(xmlName, that.xmlName) &&
                Objects.equals(classFilename, that.classFilename) &&
                Objects.equals(unescapedDescription, that.unescapedDescription) &&
                Objects.equals(discriminator, that.discriminator) &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(arrayModelType, that.arrayModelType) &&
                Objects.equals(vars, that.vars) &&
                Objects.equals(allVars, that.allVars) &&
                Objects.equals(nonNullableVars, that.nonNullableVars) &&
                Objects.equals(requiredVars, that.requiredVars) &&
                Objects.equals(optionalVars, that.optionalVars) &&
                Objects.equals(readOnlyVars, that.readOnlyVars) &&
                Objects.equals(readWriteVars, that.readWriteVars) &&
                Objects.equals(parentVars, that.parentVars) &&
                Objects.equals(allowableValues, that.allowableValues) &&
                Objects.equals(mandatory, that.mandatory) &&
                Objects.equals(allMandatory, that.allMandatory) &&
                Objects.equals(imports, that.imports) &&
                Objects.equals(externalDocumentation, that.externalDocumentation) &&
                Objects.equals(vendorExtensions, that.vendorExtensions) &&
                Objects.equals(additionalPropertiesType, that.additionalPropertiesType) &&
                Objects.equals(isAdditionalPropertiesTrue, that.isAdditionalPropertiesTrue) &&
                Objects.equals(getMaxProperties(), that.getMaxProperties()) &&
                Objects.equals(getMinProperties(), that.getMinProperties()) &&
                Objects.equals(getMaxItems(), that.getMaxItems()) &&
                Objects.equals(getMinItems(), that.getMinItems()) &&
                Objects.equals(getMaxLength(), that.getMaxLength()) &&
                Objects.equals(getMinLength(), that.getMinLength()) &&
                Objects.equals(getMinimum(), that.getMinimum()) &&
                Objects.equals(getMaximum(), that.getMaximum()) &&
                Objects.equals(getPattern(), that.getPattern()) &&
                Objects.equals(getItems(), that.getItems()) &&
                Objects.equals(getAdditionalProperties(), that.getAdditionalProperties()) &&
                Objects.equals(getIsModel(), that.getIsModel()) &&
                Objects.equals(getMultipleOf(), that.getMultipleOf());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParent(), getParentSchema(), getInterfaces(), getAllParents(), getParentModel(),
                getInterfaceModels(), getChildren(), permits, anyOf, oneOf, allOf, getName(), getSchemaName(), getClassname(), getTitle(),
                getDescription(), getClassVarName(), getModelJson(), getDataType(), getXmlPrefix(), getXmlNamespace(),
                getXmlName(), getClassFilename(), getUnescapedDescription(), getDiscriminator(), getDefaultValue(),
                getArrayModelType(), isAlias, isString, isInteger, isLong, isNumber, isNumeric, isFloat, isDouble,
                isDate, isDateTime, isNull, hasValidation, isShort, isUnboundedInteger, isBoolean,
                getVars(), getAllVars(), getNonNullableVars(), getRequiredVars(), getOptionalVars(), getReadOnlyVars(), getReadWriteVars(),
                getParentVars(), getAllowableValues(), getMandatory(), getAllMandatory(), getImports(), hasVars,
                isEmptyVars(), hasMoreModels, hasEnums, isEnum, isNullable, hasRequired, hasOptional, isArray,
                hasChildren, isMap, isOptional, isDeprecated, hasReadOnly, hasOnlyReadOnly, getExternalDocumentation(), getVendorExtensions(),
                getAdditionalPropertiesType(), getMaxProperties(), getMinProperties(), getUniqueItems(), getMaxItems(),
                getMinItems(), getMaxLength(), getMinLength(), getExclusiveMinimum(), getExclusiveMaximum(), getMinimum(),
                getMaximum(), getPattern(), getMultipleOf(), getItems(), getAdditionalProperties(), getIsModel(),
                getAdditionalPropertiesIsAnyType(), hasDiscriminatorWithNonEmptyMapping,
                isAnyType, getComposedSchemas(), hasMultipleTypes, isDecimal, isUuid, isUri, requiredVarsMap, ref,
                uniqueItemsBoolean, schemaIsFromAdditionalProperties, isBooleanSchemaTrue, isBooleanSchemaFalse,
                format, dependentRequired, contains);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CodegenModel{");
        sb.append("name='").append(name).append('\'');
        sb.append(", schemaName='").append(schemaName).append('\'');
        sb.append(", parent='").append(parent).append('\'');
        sb.append(", parentSchema='").append(parentSchema).append('\'');
        sb.append(", interfaces=").append(interfaces);
        sb.append(", interfaceModels=").append(interfaceModels != null ? interfaceModels.size() : "[]");
        sb.append(", allParents=").append(allParents);
        sb.append(", parentModel=").append(parentModel);
        sb.append(", children=").append(children != null ? children.size() : "[]");
        sb.append(", permits=").append(permits != null ? permits.size() : "[]");
        sb.append(", anyOf=").append(anyOf);
        sb.append(", oneOf=").append(oneOf);
        sb.append(", allOf=").append(allOf);
        sb.append(", classname='").append(classname).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", classVarName='").append(classVarName).append('\'');
        sb.append(", modelJson='").append(modelJson).append('\'');
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", xmlPrefix='").append(xmlPrefix).append('\'');
        sb.append(", xmlNamespace='").append(xmlNamespace).append('\'');
        sb.append(", xmlName='").append(xmlName).append('\'');
        sb.append(", classFilename='").append(classFilename).append('\'');
        sb.append(", unescapedDescription='").append(unescapedDescription).append('\'');
        sb.append(", discriminator=").append(discriminator);
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", arrayModelType='").append(arrayModelType).append('\'');
        sb.append(", isAlias=").append(isAlias);
        sb.append(", isString=").append(isString);
        sb.append(", isInteger=").append(isInteger);
        sb.append(", isShort=").append(isShort);
        sb.append(", isLong=").append(isLong);
        sb.append(", isUnboundedInteger=").append(isUnboundedInteger);
        sb.append(", isBoolean=").append(isBoolean);
        sb.append(", isNumber=").append(isNumber);
        sb.append(", isNumeric=").append(isNumeric);
        sb.append(", isFloat=").append(isFloat);
        sb.append(", isDouble=").append(isDouble);
        sb.append(", isDate=").append(isDate);
        sb.append(", isDateTime=").append(isDateTime);
        sb.append(", vars=").append(vars);
        sb.append(", allVars=").append(allVars);
        sb.append(", nonNullableVars=").append(nonNullableVars);
        sb.append(", requiredVars=").append(requiredVars);
        sb.append(", optionalVars=").append(optionalVars);
        sb.append(", readOnlyVars=").append(readOnlyVars);
        sb.append(", readWriteVars=").append(readWriteVars);
        sb.append(", parentVars=").append(parentVars);
        sb.append(", allowableValues=").append(allowableValues);
        sb.append(", mandatory=").append(mandatory);
        sb.append(", allMandatory=").append(allMandatory);
        sb.append(", imports=").append(imports);
        sb.append(", hasVars=").append(hasVars);
        sb.append(", emptyVars=").append(emptyVars);
        sb.append(", hasMoreModels=").append(hasMoreModels);
        sb.append(", hasEnums=").append(hasEnums);
        sb.append(", isEnum=").append(isEnum);
        sb.append(", isNullable=").append(isNullable);
        sb.append(", hasRequired=").append(hasRequired);
        sb.append(", hasOptional=").append(hasOptional);
        sb.append(", isArray=").append(isArray);
        sb.append(", hasChildren=").append(hasChildren);
        sb.append(", isMap=").append(isMap);
        sb.append(", isOptional=").append(isOptional);
        sb.append(", isDeprecated=").append(isDeprecated);
        sb.append(", hasReadOnly=").append(hasReadOnly);
        sb.append(", hasOnlyReadOnly=").append(hasOnlyReadOnly);
        sb.append(", externalDocumentation=").append(externalDocumentation);
        sb.append(", vendorExtensions=").append(vendorExtensions);
        sb.append(", additionalPropertiesType='").append(additionalPropertiesType).append('\'');
        sb.append(", isAdditionalPropertiesTrue='").append(isAdditionalPropertiesTrue).append('\'');
        sb.append(", maxProperties=").append(maxProperties);
        sb.append(", minProperties=").append(minProperties);
        sb.append(", uniqueItems=").append(uniqueItems);
        sb.append(", uniqueItemsBoolean=").append(uniqueItemsBoolean);
        sb.append(", maxItems=").append(maxItems);
        sb.append(", minItems=").append(minItems);
        sb.append(", maxLength=").append(maxLength);
        sb.append(", minLength=").append(minLength);
        sb.append(", exclusiveMinimum=").append(exclusiveMinimum);
        sb.append(", exclusiveMaximum=").append(exclusiveMaximum);
        sb.append(", minimum='").append(minimum).append('\'');
        sb.append(", maximum='").append(maximum).append('\'');
        sb.append(", pattern='").append(pattern).append('\'');
        sb.append(", multipleOf='").append(multipleOf).append('\'');
        sb.append(", items='").append(items).append('\'');
        sb.append(", additionalProperties='").append(additionalProperties).append('\'');
        sb.append(", isModel='").append(isModel).append('\'');
        sb.append(", isNull='").append(isNull).append('\'');
        sb.append(", hasValidation='").append(hasValidation).append('\'');
        sb.append(", getAdditionalPropertiesIsAnyType=").append(getAdditionalPropertiesIsAnyType());
        sb.append(", getHasDiscriminatorWithNonEmptyMapping=").append(hasDiscriminatorWithNonEmptyMapping);
        sb.append(", getIsAnyType=").append(getIsAnyType());
        sb.append(", composedSchemas=").append(composedSchemas);
        sb.append(", hasMultipleTypes=").append(hasMultipleTypes);
        sb.append(", isDecimal=").append(isDecimal);
        sb.append(", isUUID=").append(isUuid);
        sb.append(", isURI=").append(isUri);
        sb.append(", requiredVarsMap=").append(requiredVarsMap);
        sb.append(", ref=").append(ref);
        sb.append(", schemaIsFromAdditionalProperties=").append(schemaIsFromAdditionalProperties);
        sb.append(", isBooleanSchemaTrue=").append(isBooleanSchemaTrue);
        sb.append(", isBooleanSchemaFalse=").append(isBooleanSchemaFalse);
        sb.append(", format=").append(format);
        sb.append(", dependentRequired=").append(dependentRequired);
        sb.append(", contains=").append(contains);
        sb.append('}');
        return sb.toString();
    }

    /*
     * To clean up mapped models if needed and add mapped models to imports
     *
     * @param cleanUpMappedModels Clean up mapped models if set to true
     */
    public void addDiscriminatorMappedModelsImports(boolean cleanUpMappedModels) {
        if (discriminator == null || discriminator.getMappedModels() == null) {
            return;
        }

        if (cleanUpMappedModels && !this.hasChildren && // no child
                (this.oneOf == null || this.oneOf.isEmpty()) && // not oneOf
                (this.anyOf == null || this.anyOf.isEmpty())) { // not anyOf
            //clear the mapping
            discriminator.setMappedModels(null);
            return;
        }

        // import child schemas defined in mapped models
        for (CodegenDiscriminator.MappedModel mm : discriminator.getMappedModels()) {
            if (!"".equals(mm.getModelName())) {
                imports.add(mm.getModelName());
            }
        }
    }

    public boolean getHasItems() {
        return this.items != null;
    }

    @Override
    public Map<String, CodegenProperty> getRequiredVarsMap() {
        return requiredVarsMap;
    }

    @Override
    public void setRequiredVarsMap(Map<String, CodegenProperty> requiredVarsMap) {
        this.requiredVarsMap = requiredVarsMap;
    }

    /**
     * Remove duplicated properties in all variable list
     */
    public void removeAllDuplicatedProperty() {
        // remove duplicated properties
        vars = removeDuplicatedProperty(vars);
        optionalVars = removeDuplicatedProperty(optionalVars);
        requiredVars = removeDuplicatedProperty(requiredVars);
        parentVars = removeDuplicatedProperty(parentVars);
        allVars = removeDuplicatedProperty(allVars);
        nonNullableVars = removeDuplicatedProperty(nonNullableVars);
        readOnlyVars = removeDuplicatedProperty(readOnlyVars);
        readWriteVars = removeDuplicatedProperty(readWriteVars);
    }

    private List<CodegenProperty> removeDuplicatedProperty(List<CodegenProperty> vars) {
        // clone the list first
        List<CodegenProperty> newList = new ArrayList<>();
        for (CodegenProperty cp : vars) {
            newList.add(cp.clone());
        }

        Set<String> propertyNames = new TreeSet<>();
        Set<String> duplicatedNames = new TreeSet<>();

        ListIterator<CodegenProperty> iterator = newList.listIterator();
        while (iterator.hasNext()) {
            CodegenProperty element = iterator.next();

            if (propertyNames.contains(element.baseName)) {
                duplicatedNames.add(element.baseName);
                iterator.remove();
            } else {
                propertyNames.add(element.baseName);
            }
        }

        return newList;
    }

    /**
     * Remove self reference import
     */
    public void removeSelfReferenceImport() {
        for (CodegenProperty cp : allVars) {
            if (cp == null) {
                // TODO cp shouldn't be null. Show a warning message instead
            } else {
                // detect self import
                if (this.classname.equalsIgnoreCase(cp.dataType) ||
                        (cp.isContainer && cp.items != null && this.classname.equalsIgnoreCase(cp.items.dataType))) {
                    this.imports.remove(this.classname); // remove self import
                    cp.isSelfReference = true;
                }
            }
        }
    }
}
