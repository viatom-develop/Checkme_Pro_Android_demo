package com.viatom.azur.element;

/**
 * Created by wangxiaogang on 2016/11/24.
 */

public class Patient {

    /**
     * resourceType : Patient
     * identifier : {"system":"http://cloud.viatomtech.com/fhir","value":"--","medicalId":"--"}
     * name : --
     * gender : --
     * birthDate : --
     * height : --
     * weight : --
     * stepSize : --
     */

    private String resourceType;
    private IdentifierBean identifier;
    private String name;
    private String gender;
    private String birthDate;
    private String height;
    private String weight;
    private String stepSize;

    public Patient( IdentifierBean identifier, String name, String gender, String birthDate, String height, String weight ) {
        this.resourceType = "Patient";
        this.identifier = identifier;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.stepSize ="--";
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public IdentifierBean getIdentifier() {
        return identifier;
    }

    public void setIdentifier(IdentifierBean identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getStepSize() {
        return stepSize;
    }

    public void setStepSize(String stepSize) {
        this.stepSize = stepSize;
    }

    public static class IdentifierBean {
        /**
         * system : http://cloud.viatomtech.com/fhir
         * value : --
         * medicalId : --
         */

        private String system;
        private String value;
        private String medicalId;

        public IdentifierBean( String value, String medicalId) {
            this.system = "http://cloud.viatomtech.com/fhir";
            this.value = value;
            this.medicalId = medicalId;
        }

        public String getSystem() {
            return system;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getMedicalId() {
            return medicalId;
        }

        public void setMedicalId(String medicalId) {
            this.medicalId = medicalId;
        }

        @Override
        public String toString() {
            return "IdentifierBean{" +
                    "system='" + system + '\'' +
                    ", value='" + value + '\'' +
                    ", medicalId='" + medicalId + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Patient{" +
                "resourceType='" + resourceType + '\'' +
                ", identifier=" + identifier.toString() +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", height='" + height + '\'' +
                ", weight='" + weight + '\'' +
                ", stepSize='" + stepSize + '\'' +
                '}';
    }
}
